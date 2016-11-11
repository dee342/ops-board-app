package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.INextDayCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.PersonnelAssignType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to assign a person to a partial task / multi route.
 */
@IBoardCommandAnnotation(commandName = "AssignPersonToPartialTask")
public class CommandAssignPersonToPartialTask extends AbstractTaskPersonCommand implements INextDayCommand {

	private static final long serialVersionUID = 1L;

	private List<TaskAssignment> assignments = new ArrayList<TaskAssignment>();
	private transient boolean assignedTomorrow;

	public CommandAssignPersonToPartialTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		
		BoardPerson bp = board.getPersonnel().get(getPersonId());
		Shift nextDayShift = null;
		if(bp.isAvailableNextDay()){
			setAssignType(PersonnelAssignType.NEXT_DAY.name());
		}
		
		if (PersonnelAssignType.NEXT_DAY.name().equals(getAssignType())) {
			nextDayShift = BoardHelper.findLocationShiftByTask(board.findPartialTasks(getTaskId()).iterator().next().getId(), board).get().getShift();
		}
		
		BoardPersonAndTasks bpts = board.assignPersonToPartialTask(getTaskId(), getPersonId(), getPosition(),
				getAssignType(), getSystemDateTime(), isAssignedTomorrow(), nextDayShift);
		setAssigned(bpts.getBoardPerson().isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(bpts.getBoardPerson().isAssigned());
		setState(bpts.getBoardPerson().getState(board.getLocation()).getState());

		for (Task t : bpts.getTasks()) {
			PersonAssignment assignment = null;
			if (getPosition() == 1) {
				assignment = t.getAssignedPerson1();
			} else if (getPosition() == 2) {
				assignment = t.getAssignedPerson2();
			}

			if (assignment != null) {
				assignments.add(new TaskAssignment(t.getId(), assignment.getStartTime(), assignment.getEndTime(),
						assignment.getAssignmentTime(), assignment.isCompleted()));
				

				//Per Bruce, use the shift from the first task.
				if(nextDayShift != null){
					setShift(nextDayShift);	
					assignment.getPerson().assignNextDay(board, nextDayShift);
							
					List<AbstractDayBeforeCommand> nextDayCommands =  (List<AbstractDayBeforeCommand>) AbstractDayBeforeCommand.build(getBoardId(), board.getPersonnel().get(getPersonId()), false, nextDayShift);
					nextDayCommands.forEach(c -> this.addSubCommands((CommandAddDayBefore) c));
	
				}

			}
			
			
		}
		
		

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public List<TaskAssignment> getAssignments() {
		return assignments;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setAssignments(List<TaskAssignment> assignments) {
		this.assignments = assignments;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("assignments", assignments);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Assigned person ");
		sb.append("[person:" + getPersonId() + "] ");
		sb.append("to [task:" + getTaskId() + "].");

		setAuditMessage(sb.toString());
	}
	
	@Override
	public boolean isAssignedTomorrow() {
		return assignedTomorrow;
	}

	@Override
	public void setAssignedTomorrow(boolean assignedTomorrow) {
		this.assignedTomorrow = assignedTomorrow;
	};
}

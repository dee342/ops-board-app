package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.PersonnelAssignType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.PersonLogger;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Command to unassign a person from a task.
 */
@IBoardCommandAnnotation(commandName = "UnassignPersonFromTask")
public class CommandUnassignPersonFromTask extends AbstractTaskPersonCommand {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(CommandUnassignPersonFromTask.class);

	private Set<TaskAssignment> tasks;

	public CommandUnassignPersonFromTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		Task t = board.findTask(getTaskId());
		PersonAssignment pa = getPosition() == 1 ? t.getAssignedPerson1() : t.getAssignedPerson2();
		PersonLogger.logPersonAssignment("execute ", pa, log);
		String type = pa.getType();
		
		// Execute person unassignment logic logic
		
		BoardPerson bp = board.unassignPersonFromTask(getTaskId(), getPersonId(), getPosition());
		PersonLogger.logBoardPerson("execute ", bp, log);
		
		if(PersonnelAssignType.NEXT_DAY.name().equals(type)){
			Shift nextDayShift = BoardHelper.findLocationShiftByTask(getTaskId(), board).get().getShift();
			board.unassignNextDay(getPersonId(), nextDayShift);
			setShift(nextDayShift);
			
			List<AbstractDayBeforeCommand> nextDayCommands =  (List<AbstractDayBeforeCommand>) AbstractDayBeforeCommand.build(getBoardId(), board.getPersonnel().get(getPersonId()), true, nextDayShift);
			nextDayCommands.forEach(c -> this.addSubCommands((CommandRemoveDayBefore) c));

		}
		
		setAssigned(bp.isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(bp.isAssigned());
		setAssignmentTime(null);
		setCompleted(false);
		setState(bp.getState(board.getLocation()).getState());


		// Remove any supervisor assignments
		Date now = new Date();
		Date start = board.getShiftsStartDate();
		Date endTime = (board.getShiftsEndDate() != null) ? DateUtils.removeTime(board.getShiftsEndDate()) : null;
		
		Set<String> taskIdsForSupervisorAssignments = board.getPersonOps().findTaskIdsForTasksWithSupervisorAssignment(bp, start, endTime, now);
		if (taskIdsForSupervisorAssignments.size() > 0) {
			tasks = new LinkedHashSet<TaskAssignment>();
			for (String tId : taskIdsForSupervisorAssignments) {
				TaskAssignment ta = new TaskAssignment(tId, bp.getPerson().getId());
				tasks.add(ta);
			}
		}

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
		
		PersonLogger.logBoardPerson("execute - end of method ", bp, log);
	}

	public Set<TaskAssignment> getTasks() {
		return tasks;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setTasks(Set<TaskAssignment> tasks) {
		this.tasks = tasks;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unassigned person ");
		sb.append("[person:" + getPersonId() + "] ");
		sb.append("from [task:" + getTaskId() + "].");

		setAuditMessage(sb.toString());
	};
}

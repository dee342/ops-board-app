package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.INextDayCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.PersonnelAssignType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.PersonLogger;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command to assign a person to a task.
 */
@IBoardCommandAnnotation(commandName = "AssignPersonToTask")
public class CommandAssignPersonToTask extends AbstractTaskPersonCommand implements INextDayCommand {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(CommandAssignPersonToTask.class);

	private transient boolean assignedTomorrow;
	
	public CommandAssignPersonToTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		
		// Execute logic
		BoardPerson bp = board.getPersonnel().get(getPersonId());
		PersonLogger.logBoardPerson("execute ", bp, log);
		Shift nextDayShift = null;
		
		if(bp.isAvailableNextDay() && StringUtils.isBlank(getAssignType())){
			setAssignType(PersonnelAssignType.NEXT_DAY.name());
		}
		
		if (PersonnelAssignType.NEXT_DAY.name().equals(getAssignType())) {
			nextDayShift = BoardHelper.findLocationShiftByTask(getTaskId(), board).get().getShift();
		}
		
		Task t = board.assignPersonToTask(getTaskId(), getPersonId(), getPosition(), getAssignType(),
				getSystemDateTime(), isAssignedTomorrow(), nextDayShift);
		
		if(nextDayShift != null){
			setShift(nextDayShift);	
			bp.assignNextDay(board, nextDayShift);
			
			List<AbstractDayBeforeCommand> nextDayCommands =  (List<AbstractDayBeforeCommand>) AbstractDayBeforeCommand.build(getBoardId(), board.getPersonnel().get(getPersonId()), false, nextDayShift);
			nextDayCommands.forEach(c -> this.addSubCommands((CommandAddDayBefore) c));

		}	
		
		PersonAssignment pa = getPosition() == 1 ? t.getAssignedPerson1() : t.getAssignedPerson2();
		PersonLogger.logPersonAssignment("execute ", pa, log);
		setAssigned(pa.getPerson().isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(pa.getPerson().isAssigned());
		setAssignmentTime(pa.getAssignmentTime());
		setCompleted(pa.isCompleted());
		setState(pa.getPerson().getState(board.getLocation()).getState());
		
		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
		
		PersonLogger.logBoardPerson("execute - end of method ", bp, log);
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
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
	}
}

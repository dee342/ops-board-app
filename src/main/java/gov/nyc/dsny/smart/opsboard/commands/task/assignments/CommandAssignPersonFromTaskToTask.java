package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
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
import gov.nyc.dsny.smart.opsboard.util.Utils;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@IBoardCommandAnnotation(commandName = "AssignPersonFromTaskToTask")
public class CommandAssignPersonFromTaskToTask extends AbstractTaskPersonCommand {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(CommandAssignPersonFromTaskToTask.class);
	
	private String newTaskId;
	private int newPosition;
	
	public CommandAssignPersonFromTaskToTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		newTaskId = map.get("newTaskId") != null ? (String) map.get("newTaskId") : null;
		newPosition = map.get("newPosition") != null ? (Integer) map.get("newPosition") : 0;
	}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
		BoardPerson bp = board.getPersonnel().get(getPersonId());
		PersonLogger.logBoardPerson("execute ", bp, log);
		Task oldTask = board.findTask(getTaskId());
		PersonAssignment oldPa = getPosition()== 1 ? oldTask.getAssignedPerson1() : oldTask.getAssignedPerson2();
		PersonLogger.logPersonAssignment("execute ", oldPa, log);
		String type = oldPa.getType();
		
		Shift oldTaskShift =null;
		Shift newTaskShift = null;
		
		oldTaskShift =  BoardHelper.findLocationShiftByTask(getTaskId(), board).get().getShift();
		
		if(PersonnelAssignType.NEXT_DAY.name().equals(type)){
			newTaskShift = BoardHelper.findLocationShiftByTask(getNewTaskId(), board).get().getShift();
		}
		//used to make sure assignmentType is kept.
		Task t = null;
		//call this logic only if fromShift and toShift are equal
		if(oldTaskShift.equals(newTaskShift)){
			// Unassign logic
			board.unassignPersonFromTask(getTaskId(), getPersonId(), getPosition());
			// Assign logic
			t = board.assignPersonToTask(getNewTaskId(), getPersonId(), getNewPosition(), type, getSystemDateTime(), null, newTaskShift);
		}
		//call this logic only if fromShift and toShift are different because unassign person should not happen until all the validations are done for nextday assignments and then the assign logic is called
		else{
			t= board.movePersonTaskToTask(getTaskId(), getNewTaskId(), getPersonId(), getPosition(), getNewPosition(), type, getSystemDateTime(), null, newTaskShift);
		}

		if(newTaskShift != null){
			Shift remove = BoardHelper.findLocationShiftByTask(getTaskId(), board).get().getShift();
			
			bp.removeDayBefore(board, remove);			
			Shift add = BoardHelper.findLocationShiftByTask(getNewTaskId(), board).get().getShift();
			bp.addDayBefore(board, add);			
			
			CommandUpdateDayBefore nextDayCommand = new CommandUpdateDayBefore();
			Date tomorrow = DateUtils.getOneDayAfter(DateUtils.toBoardDate(Board.boardIdToBoardDate(getBoardId())));	
			String newBoardId = Board.toBoardId(Board.boardIdToLocation(getBoardId()), DateUtils.toStringBoardDate(tomorrow));
			nextDayCommand.setBoardId(newBoardId);
			nextDayCommand.setPersonId(getPersonId());
			nextDayCommand.add(add);
			nextDayCommand.remove(remove);
			nextDayCommand.setSystemDateTime(new Date());
			nextDayCommand.setSystemUser(Utils.getUserId());
			this.addSubCommands(nextDayCommand);
			
		}
		
		PersonAssignment pa = getNewPosition() == 1 ? t.getAssignedPerson1() : t.getAssignedPerson2();
		PersonLogger.logPersonAssignment("After calling Board methods inside execute ", pa, log);
		setAssigned(pa.getPerson().isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(pa.getPerson().isAssigned());
		setAssignmentTime(pa.getAssignmentTime());
		setCompleted(pa.isCompleted());
		setState(pa.getPerson().getState(board.getLocation()).getState());
		setAssignType(pa.getType());

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
		
		PersonLogger.logBoardPerson("execute - end of method ", bp, log);
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService)
			throws OpsBoardError {
		persistService.save(board);
		
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unassigned person ");
		sb.append("[person:" + getPersonId() + "] ");
		sb.append("from [task:" + getTaskId() + "] ");
		sb.append("and assigned to [task:" + getNewTaskId() + "].");
		setAuditMessage(sb.toString());
		
	}
	
	public int getNewPosition() {
		return newPosition;
	}

	public void setNewPosition(int newPosition) {
		this.newPosition = newPosition;
	}

	public String getNewTaskId() {
		return newTaskId;
	}

	public void setNewTaskId(String newTaskId) {
		this.newTaskId = newTaskId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append(super.toString());

		return builder.toString();
	}
}
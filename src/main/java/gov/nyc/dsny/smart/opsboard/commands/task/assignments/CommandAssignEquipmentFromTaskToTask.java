package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "AssignEquipmentFromTaskToTask")
public class CommandAssignEquipmentFromTaskToTask extends AbstractTaskEquipmentCommand {
	
	private static final long serialVersionUID = 1L;
	
	private String newTaskId;
	
	public CommandAssignEquipmentFromTaskToTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		newTaskId = map.get("newTaskId") != null ? (String) map.get("newTaskId") : null;
	}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {

		// Unassign logic
		BoardEquipment be = board.unassignEquipmentFromTask(getTaskId(), getEquipmentId(), getSystemDateTime());
		// Assign logic
		Task t = board.assignEquipmentToTask(getNewTaskId(), getEquipmentId(), getSystemDateTime());
		setAssigned(t.getAssignedEquipment().getEquipment().isAssigned());
		setAssignmentTime(t.getAssignedEquipment().getAssignmentTime());
		setCompleted(t.getAssignedEquipment().isCompleted());
		setStates(board.getLocation(), t.getAssignedEquipment().getEquipment());
		
		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService)
			throws OpsBoardError {
		persistService.save(board);
		
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unassigned equipment ");
		sb.append("[equipment:" + getEquipmentId() + "] ");
		sb.append("from [task:" + getTaskId() + "] ");
		sb.append("and assigned to [task:" + getNewTaskId() + "].");
		setAuditMessage(sb.toString());
		
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
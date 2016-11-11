package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to assign a piece of Equipment to a task.
 */
@IBoardCommandAnnotation(commandName = "AssignEquipmentToTask")
public class CommandAssignEquipmentToTask extends AbstractTaskEquipmentCommand {

	private static final long serialVersionUID = 1L;

	public CommandAssignEquipmentToTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		Task t = board.assignEquipmentToTask(getTaskId(), getEquipmentId(), getSystemDateTime());
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
		sb.append("Assigned equipment ");
		sb.append("[equipment:" + getEquipmentId() + "] ");
		sb.append("to [task:" + getTaskId() + "].");

		setAuditMessage(sb.toString());
	};
}

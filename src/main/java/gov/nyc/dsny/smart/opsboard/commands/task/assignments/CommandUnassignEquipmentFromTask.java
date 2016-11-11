package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to unassign a piece of Equipment from a task.
 */
@IBoardCommandAnnotation(commandName = "UnassignEquipmentFromTask")
public class CommandUnassignEquipmentFromTask extends AbstractTaskEquipmentCommand {

	private static final long serialVersionUID = 1L;

	public CommandUnassignEquipmentFromTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipment be = board.unassignEquipmentFromTask(getTaskId(), getEquipmentId(), getSystemDateTime());
		setAssigned(be.isAssigned());
		setAssignmentTime(null);
		setCompleted(false);
		setStates(board.getLocation(), be);

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
		sb.append("Unassigned equipment ");
		sb.append("[equipment:" + getEquipmentId() + "] ");
		sb.append("from [task:" + getTaskId() + "].");

		setAuditMessage(sb.toString());
	};
}

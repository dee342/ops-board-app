package gov.nyc.dsny.smart.opsboard.commands.equipment.detach;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentState;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to cancel a equipment detachment board.
 */
@IMultiBoardCommandAnnotation(commandName = "CancelEquipmentDetachment")
public class CommandCancelEquipmentDetachment extends AbstractEquipmentAttachDetachCommand {

	private static final long serialVersionUID = 1L;

	public CommandCancelEquipmentDetachment() {
	}

	public CommandCancelEquipmentDetachment(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandCancelEquipmentDetachment(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, String equipmentName, Long detachmentId) {
		this(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, equipmentName, detachmentId, false);
	}
	
	public CommandCancelEquipmentDetachment(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, String equipmentName, Long detachmentId, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, null, null, null, "", "", "",
				DetachmentState.CANCELLED.getCode(), detachmentId, fromIntegration);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipment be = board.getEquipmentOps().cancelEquipmentDetachment(getEquipmentId(), getSystemUser(),
				getSystemDateTime(), getDetachmentId());

		// Update command

		updateEquipmentAndTasks(board, new BoardEquipmentAndTasks(be, null));

		// Create audit message
		createAuditMessage(board, be);

		// Add command to history
		board.addCommandToHistory(this);
	}

	protected void createAuditMessage(Board board, BoardEquipment be) {
		StringBuilder sb = new StringBuilder();
		sb.append("Cancelled detachment for equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "].");

		setAuditMessage(sb.toString());
	}
}
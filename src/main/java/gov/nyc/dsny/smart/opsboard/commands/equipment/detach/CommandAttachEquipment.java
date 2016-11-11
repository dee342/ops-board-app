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
 * Command to attach a piece of equipment to a board.
 */
@IMultiBoardCommandAnnotation(commandName = "AttachEquipment")
public class CommandAttachEquipment extends AbstractEquipmentAttachDetachCommand {

	private static final long serialVersionUID = 1L;

	public CommandAttachEquipment() {
	}

	public CommandAttachEquipment(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandAttachEquipment(String boardId, String systemUser, Date systemDateTime, String boardEquipmentId,
			String equipmentId, String equipmentName, Date date, String reporter, String comments, Long detachmentId) {
		this(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId,
				equipmentName, date, reporter, comments, detachmentId, false);
	}
	
	public CommandAttachEquipment(String boardId, String systemUser, Date systemDateTime, String boardEquipmentId,
			String equipmentId, String equipmentName, Date date, String reporter, String comments, Long detachmentId, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, date, null, null, "", reporter,
				comments, DetachmentState.ACCEPTED.getCode(), detachmentId, fromIntegration);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipment be = board.getEquipmentOps().attachEquipment(getEquipmentId(), getReporter(), getSystemUser(),
				getDate(), getSystemDateTime(), getComments(), getDetachmentId());

		// Update command
		updateEquipmentAndTasks(board, new BoardEquipmentAndTasks(be, null));

		// Create audit message
		createAuditMessage(board, be, this.getCurrentLocation());
		
		// Add command to history
		board.addCommandToHistory(this);
	}

	protected void createAuditMessage(Board board, BoardEquipment be, String to) {
		StringBuilder sb = new StringBuilder();
		sb.append("Equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "] ");
		sb.append("accepted at [location:" + to + "].");

		setAuditMessage(sb.toString());
	}
}

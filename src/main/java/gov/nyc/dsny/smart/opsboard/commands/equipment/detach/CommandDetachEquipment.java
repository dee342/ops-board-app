package gov.nyc.dsny.smart.opsboard.commands.equipment.detach;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IFromLocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IToLocationCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentState;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to detach a piece of equipment from a board to another board.
 */
@IMultiBoardCommandAnnotation(commandName = "DetachEquipment")
public class CommandDetachEquipment extends AbstractEquipmentAttachDetachCommand implements IFromLocationCommand,
IToLocationCommand {

	private static final long serialVersionUID = 1L;

	public CommandDetachEquipment() {
	}

	public CommandDetachEquipment(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandDetachEquipment(String boardId, String systemUser, Date systemDateTime, String boardEquipmentId,
			String equipmentId, String equipmentName, Date date, Location from, Location to, String driver,
			String reporter, String comments, Long detachmentId) {
		this(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, equipmentName, date, from, to, driver, reporter, comments, detachmentId, false);
	}
	
	public CommandDetachEquipment(String boardId, String systemUser, Date systemDateTime, String boardEquipmentId,
			String equipmentId, String equipmentName, Date date, Location from, Location to, String driver,
			String reporter, String comments, Long detachmentId, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, date, from, to, driver, reporter,
				comments, DetachmentState.PENDING.getCode(), detachmentId, fromIntegration);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipmentAndTasks bets = board.getEquipmentOps().detachEquipment(getEquipmentId(), getReporter(),
				getSystemUser(), getDate(), getSystemDateTime(), getFrom(), getTo(), getDriver(), getComments(), getDetachmentId());

		// Update command
		updateEquipmentAndTasks(board, bets);

		// Create audit message
		createAuditMessage(board, bets.getBoardEquipment(), getFrom(), getTo());

		// Add command to history
		board.addCommandToHistory(this);
	}

	protected void createAuditMessage(Board board, BoardEquipment be, Location from, Location to) {
		StringBuilder sb = new StringBuilder();
		sb.append("Detached equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "] ");
		sb.append("to [location:" + to.getCode() + "].");

		setAuditMessage(sb.toString());
	}
}
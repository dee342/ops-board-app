package gov.nyc.dsny.smart.opsboard.commands.equipment;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ILocationCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to add an OpsBoardEquipment to a board.
 */
@IMultiBoardCommandAnnotation(commandName = "AddEquipment")
public class CommandAddOpsBoardEquipment extends AbstractOpsBoardEquipmentCommand implements ILocationCommand {

	private static final long serialVersionUID = 1L;

	public CommandAddOpsBoardEquipment() {
		super();
	}

	public CommandAddOpsBoardEquipment(LinkedHashMap<String, Object> map) {
		super(map);
	}
	
	public CommandAddOpsBoardEquipment(String boardId, String systemUser, Date systemDateTime, String equipmentId) {
		this(boardId, systemUser, systemDateTime, equipmentId, false, false);
	}	
	
	public CommandAddOpsBoardEquipment(String boardId, String systemUser, Date systemDateTime, String equipmentId, boolean fromIntegration, boolean isNew) {
		super(boardId, systemUser, systemDateTime, equipmentId, fromIntegration, isNew);
	}	

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		board.getEquipment().put(getEquipmentId(), getBoardEquipment());
		setStates(board.getLocation(), getBoardEquipment());
		setAssigned(getBoardEquipment().isAssigned());

		// Create audit message
		createAuditMessage(board, getBoardEquipment());
		
		// Add command to history
		board.addCommandToHistory(this);
	}

	@Override
	protected void createAuditMessage(Board board, BoardEquipment be) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "].");

		setAuditMessage(sb.toString());
	}
}
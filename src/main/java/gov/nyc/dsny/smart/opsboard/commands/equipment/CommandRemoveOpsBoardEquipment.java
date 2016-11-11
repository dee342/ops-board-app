package gov.nyc.dsny.smart.opsboard.commands.equipment;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to remove an OpsBoardEquipment from a board.
 */
@IMultiBoardCommandAnnotation(commandName = "RemoveEquipment")
public class CommandRemoveOpsBoardEquipment extends AbstractOpsBoardEquipmentCommand {
	
	private static final long serialVersionUID = 1L;
	public CommandRemoveOpsBoardEquipment() {
	}

	public CommandRemoveOpsBoardEquipment(LinkedHashMap<String, Object> map) {
		super(map);
	}
	

	public CommandRemoveOpsBoardEquipment(String boardId, String systemUser, Date systemDateTime, String equipmentId, boolean fromIntegration, boolean isNew) {
		super(boardId, systemUser, systemDateTime, equipmentId, fromIntegration, isNew);
	}	
	
	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipmentAndTasks bets = board.getEquipmentOps().removeBoardEquipment(getEquipmentId());

		// Update command
		updateEquipmentAndTasks(board, bets);

		// Create audit message
		createAuditMessage(board, bets.getBoardEquipment());
		
		// Add command to history
		board.addCommandToHistory(this);
	}

	protected void createAuditMessage(Board board, BoardEquipment be) {
		StringBuilder sb = new StringBuilder();
		sb.append("Removed equipment ");
		sb.append(be.getEquipment().getName());
		sb.append(".");

		setAuditMessage(sb.toString());
	}


	
}
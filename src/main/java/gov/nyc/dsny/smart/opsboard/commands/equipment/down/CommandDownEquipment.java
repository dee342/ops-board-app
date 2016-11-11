package gov.nyc.dsny.smart.opsboard.commands.equipment.down;

import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to down a piece of equipment.
 */
@IMultiBoardCommandAnnotation(commandName = "DownEquipment")
public class CommandDownEquipment extends AbstractEquipmentUpDownCommand {

	private static final long serialVersionUID = 1L;

	public CommandDownEquipment() {
	}

	public CommandDownEquipment(LinkedHashMap<String, Object> map) {
		super(map);
	}
	
	public CommandDownEquipment(String boardId, String systemUser, Date systemDateTime, String boardEquipmentId,
			String equipmentId, UpDown upDownData, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, upDownData, fromIntegration);
	}

	@Override
	protected void createAuditMessage(Board board, BoardEquipment be) {		
		StringBuilder sb = new StringBuilder();
		sb.append("Downed equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "] ");
		sb.append("on [location:" + getCurrentLocation() + "].");

		setAuditMessage(sb.toString());
	}
}
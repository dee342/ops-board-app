package gov.nyc.dsny.smart.opsboard.commands.equipment;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Command to update an equipment's bin load (e.g. material type, full, not full...).
 */
@IMultiBoardCommandAnnotation(commandName = "UpdateEquipmentSnowReadiness")
public class CommandUpdateEquipmentSnowReadiness extends AbstractMultiBoardEquipmentCommand {

	private static final long serialVersionUID = 1L;

	private SnowReadiness snowReadiness;

	public CommandUpdateEquipmentSnowReadiness() {
	}

	public CommandUpdateEquipmentSnowReadiness(LinkedHashMap<String, Object> map) {
		super(map);

		ObjectMapper mapper = new ObjectMapper();
		try {
			snowReadiness = mapper.readValue(map.get("snowReadiness").toString(), SnowReadiness.class);
		} catch (Exception e) {
			// Do nothing
		}
	}

	public CommandUpdateEquipmentSnowReadiness(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, SnowReadiness snowReadiness, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, fromIntegration);
		if (snowReadiness != null) {
			this.snowReadiness = snowReadiness;
		}
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipmentAndTasks bets  = board.getEquipmentOps().updateEquipmentSnowReadiness(getEquipmentId(), getSystemUser(),
				getSystemDateTime(), snowReadiness);
		
		// Update command
		updateEquipmentAndTasks(board, bets);

		// Create audit message
		createAuditMessage(board, bets.getBoardEquipment());
		
		// Add command to history
		board.addCommandToHistory(this);
	}

	public SnowReadiness getSnowReadiness() {
		return snowReadiness;
	}

	public void setSnowReadiness(SnowReadiness snowReadiness) {
		this.snowReadiness = snowReadiness;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("snowReadiness", snowReadiness);
		builder.append(super.toString());

		return builder.toString();
	}

	protected void createAuditMessage(Board board, BoardEquipment be) {
		StringBuilder sb = new StringBuilder();
		sb.append("Updated Snow Readiness for equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "].");

		setAuditMessage(sb.toString());
	}
}
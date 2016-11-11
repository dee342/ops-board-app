package gov.nyc.dsny.smart.opsboard.commands.equipment.down;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.equipment.AbstractMultiBoardEquipmentCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IUpDownCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents the base class for all multi-board commands for equipment up/down operations. The class extends base
 * AbstractMultiBoardEquipmentCommand with up/down fields.
 */
public abstract class AbstractEquipmentUpDownCommand extends AbstractMultiBoardEquipmentCommand implements IUpDownCommand {

	private static final long serialVersionUID = 1L;

	private UpDown upDownData;

	public AbstractEquipmentUpDownCommand() {
	}

	public AbstractEquipmentUpDownCommand(LinkedHashMap<String, Object> map) {
		super(map);
		if (map.get("upDownData") != null) {
			JsonNode root = (JsonNode) map.get("upDownData");
			ObjectMapper mapper = new ObjectMapper();
			try {
				Set<EquipmentCondition> conditions = mapper.readValue(root.get("conditions").toString(),
						new TypeReference<Set<EquipmentCondition>>() {
						});
				UpDown updown = new UpDown();
				if (!root.get("id").isNull()) {
					updown.setId(root.get("id").asLong());
				}
				updown.setDown(root.get("down").asBoolean());
				updown.setActualUser(root.get("actualUser").asText());
				updown.setSystemUser(root.get("systemUser").asText());
				updown.setLastModifiedActual(new Date(root.get("lastModifiedActual").asLong()));
				updown.setLastModifiedSystem(new Date(root.get("lastModifiedSystem").asLong()));
				updown.setConditions(conditions);

				upDownData = updown;
			} catch (Exception e) {
				// TODO: Add exception handling here
				System.out.println(e.getMessage());
			}
		}
	}

	public AbstractEquipmentUpDownCommand(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, UpDown upDownData) {
		this(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, upDownData, false);
	}
	
	public AbstractEquipmentUpDownCommand(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, UpDown upDownData, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, fromIntegration);
		this.upDownData = upDownData;
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipmentAndTasks bets = board.getEquipmentOps().downUpEquipment(getEquipmentId(), getUpDownData());

		// Update command
		updateEquipmentAndTasks(board, bets);

		// Create audit message
		createAuditMessage(board, bets.getBoardEquipment());
		
		// Add command to history
		board.addCommandToHistory(this);
	}

	public UpDown getUpDownData() {
		return upDownData;
	}

	public void setUpDownData(UpDown upDownData) {
		this.upDownData = upDownData;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("upDownData", upDownData);
		builder.append(super.toString());

		return builder.toString();
	}

	protected abstract void createAuditMessage(Board board, BoardEquipment be);
}
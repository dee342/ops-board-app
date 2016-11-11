package gov.nyc.dsny.smart.opsboard.commands.equipment;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IBinCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to update an equipment's bin load (e.g. material type, full, not full...).
 */
@IMultiBoardCommandAnnotation(commandName = "UpdateEquipmentLoad")
public class CommandUpdateEquipmentLoad extends AbstractMultiBoardEquipmentCommand implements IBinCommand{

	private static final long serialVersionUID = 1L;

	private Bin bin1 = new Bin();
	private Bin bin2 = new Bin();

	public CommandUpdateEquipmentLoad() {
	}

	public CommandUpdateEquipmentLoad(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandUpdateEquipmentLoad(String boardId, String systemUser, Date systemDateTime, String boardEquipmentId,
			String equipmentId, Bin bin1, Bin bin2) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId);
		if (bin1 != null) {
			this.bin1 = bin1;
		}
		if (bin2 != null) {
			this.bin2 = bin2;
		}
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		// Execute logic
		BoardEquipmentAndTasks bets = board.getEquipmentOps().updateEquipmentLoad(getBoardId(),getEquipmentId(), getSystemUser(),
				getSystemDateTime(), bin1, bin2, Optional.empty());
		
		// Update command
		updateEquipmentAndTasks(board, bets);

		// Create audit message
		createAuditMessage(board, bets.getBoardEquipment());
		
		// Add command to history
		board.addCommandToHistory(this);
	}
	
	public Bin getBin1() {
		return bin1;
	}

	public Bin getBin2() {
		return bin2;
	}

	public void setBin1(Bin bin1) {
		this.bin1 = bin1;
	}

	public void setBin2(Bin bin2) {
		this.bin2 = bin2;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("bin1", bin1);
		builder.append("bin2", bin2);
		builder.append(super.toString());

		return builder.toString();
	}
	
	protected void createAuditMessage(Board board, BoardEquipment be) {
		StringBuilder sb = new StringBuilder();
		sb.append("Updated load for equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "].");

		setAuditMessage(sb.toString());
	}
}
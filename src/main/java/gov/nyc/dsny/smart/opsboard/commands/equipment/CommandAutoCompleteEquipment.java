package gov.nyc.dsny.smart.opsboard.commands.equipment;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.ILowPriorityCommand;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IBinCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to update equipment to Pending Load status.
 */
@IMultiBoardCommandAnnotation(commandName = "AutoCompleteEquipment")
public class CommandAutoCompleteEquipment extends AbstractMultiBoardEquipmentCommand implements IBinCommand, ILowPriorityCommand {

	private static final long serialVersionUID = 1L;

	private Bin bin1;
	private Bin bin2;
	private boolean hasUnfinishedPartialTasks;

	public CommandAutoCompleteEquipment(LinkedHashMap<String, Object> map) {
		super(map);
		this.hasUnfinishedPartialTasks = Boolean.valueOf(map.getOrDefault("hasUnfinishedPartialTasks", Boolean.FALSE).toString());
	}

	public CommandAutoCompleteEquipment(String boardId, String systemUser, Date systemDate, String boardEquipmentId,
			String equipmentId, Bin bin1, Bin bin2, boolean hasUnfinishedPartialTasks) {
		super(boardId, systemUser, systemDate, boardEquipmentId, equipmentId);
		this.bin1 = bin1;
		this.bin2 = bin2;
		this.hasUnfinishedPartialTasks = hasUnfinishedPartialTasks;
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardEquipmentAndTasks bets = board.getEquipmentOps().autoCompleteEquipment(getEquipmentId(), getSystemUser(),
				getSystemDateTime(), bin1, bin2, hasUnfinishedPartialTasks);

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

	@Override
	public boolean matchBoard(Board board, Location location) {
		return board.getLocation().equals(location)
				&& (DateUtils.onOrBefore(getSystemDateTime(), board.getShiftsEndDate()) || getBoardId().equals(
						board.getId()));
	}

	@Override
	public void setBin1(Bin bin1) {
		this.bin1 = bin1;
	}

	@Override
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
		sb.append("Auto-completed equipment ");
		sb.append("[equipment:" + be.getEquipment().getId() + "].");

		setAuditMessage(sb.toString());
	}

	public boolean isHasUnfinishedPartialTasks() {
		return hasUnfinishedPartialTasks;
	}

	public void setHasUnfinishedPartialTasks(boolean hasUnfinishedPartialTasks) {
		this.hasUnfinishedPartialTasks = hasUnfinishedPartialTasks;
	}
	
	
}


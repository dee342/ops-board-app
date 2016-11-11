package gov.nyc.dsny.smart.opsboard.commands.equipment;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardEquipment;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the base class for all multi-board commands for OpsBaordEquipment. The class extends base
 * AbstractMultiBoardEquipmentCommand with OpsBaordEquipment fields.
 */
public abstract class AbstractOpsBoardEquipmentCommand extends AbstractMultiBoardEquipmentCommand {

	private static final long serialVersionUID = 1L;

	private transient BoardEquipment boardEquipment;

	private transient Location location;

	private boolean update = true;

	public AbstractOpsBoardEquipmentCommand() {
		super();
	}

	public AbstractOpsBoardEquipmentCommand(LinkedHashMap<String, Object> map) {
		super(map);
		this.update = (boolean) map.getOrDefault("update", true);
	}

	public AbstractOpsBoardEquipmentCommand(String boardId, String systemUser, Date systemDateTime, String equipmentId) {
		this(boardId, systemUser, systemDateTime, equipmentId, false, false);
	}
	
	public AbstractOpsBoardEquipmentCommand(String boardId, String systemUser, Date systemDateTime, String equipmentId, boolean fromIntegration, boolean update) {
		super(boardId, systemUser, systemDateTime, equipmentId, fromIntegration);
		this.update = update;
	}

	protected abstract void createAuditMessage(Board board, BoardEquipment be);

	@Override
	public abstract void execute(Board board) throws OpsBoardError;

	public BoardEquipment getBoardEquipment() {
		return boardEquipment;
	}

	public OpsBoardEquipment getEquipment() {
		if (boardEquipment == null || location == null) {
			return null;
		}

		return new OpsBoardEquipment(boardEquipment, location);
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	public boolean isUpdate() {
		return update;
	}

	/**
	 * Match if: parent conditions are met and equipment exists on board
	 * and
	 * equipment is new or equipment is already on board
	 */
	@Override
	public boolean matchBoard(Board board, Location location) {
		boolean parentCondition = super.matchBoard(board, location);
		
		if(parentCondition == false)
			return false;		
		
		if(!update)
			return true;				
		
		return board.getEquipment().containsKey(getEquipmentId());
	}

	/**
	 * @param boardEquipment
	 *            the boardEquipment to set
	 */
	public void setBoardEquipment(BoardEquipment boardEquipment) {
		this.boardEquipment = boardEquipment;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(Location location) {
		this.location = location;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("boardEquipment", boardEquipment);
		builder.append("location", location);
		builder.append(super.toString());

		return builder.toString();
	}
}
package gov.nyc.dsny.smart.opsboard.commands.person.opsboard;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ILocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.AbstractMultiBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardPerson;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the base class for all multi-board commands for OpsBaordPerson. The class extends base
 * AbstractMultiBoardPersonnelCommand with OpsBaordPerson fields.
 */
public abstract class AbstractOpsBoardPersonnelCommand extends AbstractMultiBoardPersonnelCommand implements ILocationCommand{

	static final long serialVersionUID = 1L;

	private transient BoardPerson boardPerson;
	private transient Location location;
	
	private boolean update = true;
	

	public AbstractOpsBoardPersonnelCommand() {
		super();
	}

	public AbstractOpsBoardPersonnelCommand(LinkedHashMap<String, Object> map) {
		super(map);
		this.update = (boolean) map.getOrDefault("update", true);
	}
	
	public AbstractOpsBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, String personId) {
		super(boardId, systemUser, systemDateTime, personId, false);
	}
	
	public AbstractOpsBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, String personId, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, personId, fromIntegration);
	}
	
	public AbstractOpsBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, String personId, boolean fromIntegration, boolean update) {
		this(boardId, systemUser, systemDateTime, personId, fromIntegration);
		this.update = update;
	}		
	
	public AbstractOpsBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, BoardPerson bp) {
		super(boardId, systemUser, systemDateTime, bp.getId(), bp.getPerson().getId(), false);
		boardPerson = bp;
	}
	
	public AbstractOpsBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, BoardPerson bp, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, bp.getId(), bp.getPerson().getId(), fromIntegration);
		boardPerson = bp;
	}
	
	public AbstractOpsBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, BoardPerson bp, boolean fromIntegration, boolean update) {
		this(boardId, systemUser, systemDateTime, bp, fromIntegration);
		this.update = update;
	}

	public AbstractOpsBoardPersonnelCommand(String boardId, String systemUser, Date systemDateTime, BoardPerson bp,
			Location loc, boolean fromIntegration, boolean update) {
		this(boardId, systemUser, systemDateTime, bp, fromIntegration, update);
		location = loc;
	}

	protected abstract void createAuditMessage(Board board, BoardPerson bp);

	@Override
	public abstract void execute(Board board) throws OpsBoardError;

	public BoardPerson getBoardPerson() {
		return boardPerson;
	}

	public Location getLocation() {
		return location;
	}

	public OpsBoardPerson getPerson() {
		if (boardPerson == null || location == null) {
			return null;
		}
		return new OpsBoardPerson(boardPerson, location);
	}

	public void setBoardPerson(BoardPerson boardPerson) {
		this.boardPerson = boardPerson;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}	

	@Override
	public boolean matchBoard(Board board, Location location) {
		Date systemDateTime = DateUtils.removeTime(getSystemDateTime());
		Date boardDate = DateUtils.toBoardDate(board.getDate());
		
        boolean parentCondition =  board.getLocation().equals(location)
				&& (DateUtils.onOrAfter(boardDate, systemDateTime)
						|| DateUtils.onOrBetween(systemDateTime, board.getShiftsStartDate(), board.getShiftsEndDate()));
        
        if(parentCondition == false){
               return false;      
        }        
        if(!isUpdate()){
            return true;  
        }
        
        return parentCondition;
       
	}	

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("boardPerson", boardPerson);
		builder.append("location", location);
		builder.append(super.toString());

		return builder.toString();
	}
}
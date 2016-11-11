package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.Utils;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

public abstract class AbstractNextDayCommand extends AbstractBoardCommand{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7570768747903811450L;
	private String personId;
	private boolean nextDayAvailable = false;
	
	public AbstractNextDayCommand(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		this.personId = ObjectUtils.toString(map.get("personId"));
		this.nextDayAvailable = Boolean.valueOf(ObjectUtils.toString(map.get("nextDayAvailable")));
	}
	
	public AbstractNextDayCommand(){}
	
	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public boolean isNextDayAvailable() {
		return nextDayAvailable;
	}

	public void setNextDayAvailable(boolean nextDayAvailable) {
		this.nextDayAvailable = nextDayAvailable;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService)
			throws OpsBoardError {
		persistService.save(board);
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Set person ");
		sb.append("[person:" + getPersonId() + "] as ");
		
		if(!nextDayAvailable)
			sb.append("un");
		
		sb.append("available for next day tasks.");
			
		setAuditMessage(sb.toString());
	}

	public static void updateNextDayAvailable(Board board, BoardPerson bp){		
		if(board.getNextDayAvailablePersons().contains(BoardPerson.EXTRACT_PERSON_ID(bp.getId())))
			bp.setAvailableNextDay(true);
		else
			bp.setAvailableNextDay(false);
	}
	
	public static CommandRemoveNextDayAvailable build(String boardId, String personId){
		
		CommandRemoveNextDayAvailable nextDayCommand = new CommandRemoveNextDayAvailable();
		
		Date tomorrow = DateUtils.getOneDayAfter(DateUtils.toBoardDate(Board.boardIdToBoardDate(boardId)));	
		String newBoardId = Board.toBoardId(Board.boardIdToLocation(boardId), DateUtils.toStringBoardDate(tomorrow));
		nextDayCommand.setBoardId(newBoardId);
		nextDayCommand.setPersonId(personId);
		
		nextDayCommand.setSystemDateTime(new Date());
		nextDayCommand.setSystemUser(Utils.getUserId());
		
		return nextDayCommand;
	}
}

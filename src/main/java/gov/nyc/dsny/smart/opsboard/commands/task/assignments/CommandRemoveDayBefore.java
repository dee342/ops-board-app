package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import java.util.LinkedHashMap;

import org.apache.commons.lang.ObjectUtils;

import reactor.util.CollectionUtils;

import com.sun.javafx.binding.SelectBinding.AsBoolean;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

@IBoardCommandAnnotation(commandName = "RemoveDayBefore")
public class CommandRemoveDayBefore extends AbstractDayBeforeCommand{


	/**
	 * 
	 */
	private static final long serialVersionUID = -6248448046084846497L;
	private boolean nextDayAvailable = false;
	
	public CommandRemoveDayBefore(){}
		
	
	public CommandRemoveDayBefore(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		this.nextDayAvailable = Boolean.valueOf(ObjectUtils.toString(map.get("nextDayAvailable")));
	}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
	
		BoardPerson bp = board.getPersonnel().get(getPersonId());
		board.removeDayBefore(getPersonId(), getShifts());
		
		if(CollectionUtils.isEmpty(bp.getDayBeforeShifts(board.getLocation().getCode())))
		{	
			board.removeNextDayAvailablePerson(BoardPerson.EXTRACT_PERSON_ID(bp.getId()));	
			AbstractNextDayCommand.updateNextDayAvailable(board, bp);			
		}
		
		setNextDayAvailable(bp.isAvailableNextDay());
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		
	}

	@Override
	protected void createAuditMessage(Board board) {

	}


	public boolean isNextDayAvailable() {
		return nextDayAvailable;
	}


	public void setNextDayAvailable(boolean nextDayAvailable) {
		this.nextDayAvailable = nextDayAvailable;
	}

	
}

package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import java.util.LinkedHashMap;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;

@IBoardCommandAnnotation(commandName="RemoveNextDayAvailable")
public class CommandRemoveNextDayAvailable extends AbstractNextDayCommand{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7538993397311299647L;

	
	
	public CommandRemoveNextDayAvailable() {
		super();
		// TODO Auto-generated constructor stub
	}



	public CommandRemoveNextDayAvailable(String boardId,
			LinkedHashMap<String, Object> map) {
		super(boardId, map);
		// TODO Auto-generated constructor stub
	}



	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
		BoardPerson bp = board.getPersonnel().get(getPersonId());
		board.removeNextDayAvailablePerson(BoardPerson.EXTRACT_PERSON_ID(bp.getId()));	
		updateNextDayAvailable(board, bp);
		setNextDayAvailable(bp.isAvailableNextDay());
	}

}

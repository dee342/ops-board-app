package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;

import java.util.LinkedHashMap;

@IBoardCommandAnnotation(commandName="SetNextDayAvailable")
public class CommandSetNextDayAvailable extends AbstractNextDayCommand{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3999557641149291879L;

	
	
	public CommandSetNextDayAvailable() {
		super();
		// TODO Auto-generated constructor stub
	}



	public CommandSetNextDayAvailable(String boardId,
			LinkedHashMap<String, Object> map) {
		super(boardId, map);
		// TODO Auto-generated constructor stub
	}



	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
		BoardPerson bp = board.getPersonnel().get(getPersonId());
		board.addNextDayAvailablePerson(BoardPerson.EXTRACT_PERSON_ID(bp.getId()));	
		updateNextDayAvailable(board, bp);
		setNextDayAvailable(bp.isAvailableNextDay());
	}

}

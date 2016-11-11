package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import java.util.LinkedHashMap;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

@IBoardCommandAnnotation(commandName = "AddDayBefore")
public class CommandAddDayBefore extends AbstractDayBeforeCommand{


	/**
	 * 
	 */
	private static final long serialVersionUID = 8714169165283430209L;
	
	public CommandAddDayBefore(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}
	
	public CommandAddDayBefore(){}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
		board.addDayBefore(getPersonId(), getShifts());
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		
	}

	@Override
	protected void createAuditMessage(Board board) {

	}

}

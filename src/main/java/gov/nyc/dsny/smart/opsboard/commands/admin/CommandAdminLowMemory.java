package gov.nyc.dsny.smart.opsboard.commands.admin;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.Date;

@IBoardCommandAnnotation(commandName = "CommandAdminLowMemory")
public class CommandAdminLowMemory extends AbstractBoardCommand{
	private static final long serialVersionUID = 1L;

	@Override
	protected void createAuditMessage(Board board) {
		setAuditMessage("Heap usage exceeded threshold. Board:" + board.getId()  + "data is evicted from cache and needs to be reloaded");
		
	}
	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
		setBoardId(board.getId());
		setSystemDateTime(new Date());
		
		// Create audit message
		createAuditMessage(board);
		
		// Add command to history
		board.addCommandToHistory(this);
	}
	
	@Override
	public void persist(Board board, BoardPersistenceService persistService)
			throws OpsBoardError {
		// TODO Auto-generated method stub
		
	}
}

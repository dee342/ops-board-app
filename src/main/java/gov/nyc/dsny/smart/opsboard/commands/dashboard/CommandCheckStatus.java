package gov.nyc.dsny.smart.opsboard.commands.dashboard;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ILocationCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

@IBoardCommandAnnotation(commandName = "CheckStatus")
public class CommandCheckStatus extends AbstractBoardCommand implements ILocationCommand {


	private static final long serialVersionUID = 1L;
	private boolean status;

	@Override
	public void setLocation(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService)
			throws OpsBoardError {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void createAuditMessage(Board board) {
		// TODO Auto-generated method stub
		
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

}

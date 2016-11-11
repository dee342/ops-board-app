package gov.nyc.dsny.smart.opsboard.commands.dashboard;

import java.util.HashMap;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.viewmodels.KioskMetadata;

@IBoardCommandAnnotation(commandName = "UpdateDashboard")
public class CommandUpdateDashboard extends AbstractBoardCommand{


	private static final long serialVersionUID = 1L;
	private HashMap<String, KioskMetadata> kioskMap = new HashMap<String, KioskMetadata>();
	
	public CommandUpdateDashboard(HashMap<String, KioskMetadata> kioskMap) {
		super();
		this.kioskMap = kioskMap;
	}

	public HashMap<String, KioskMetadata> getKioskMap() {
		return kioskMap;
	}

	public void setKioskMap(HashMap<String, KioskMetadata> kioskMap) {
		this.kioskMap = kioskMap;
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

}

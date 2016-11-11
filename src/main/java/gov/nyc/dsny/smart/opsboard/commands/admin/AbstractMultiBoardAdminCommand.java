package gov.nyc.dsny.smart.opsboard.commands.admin;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.util.LinkedHashMap;

abstract class AbstractMultiBoardAdminCommand extends AbstractMultiBoardCommand {

	private static final long serialVersionUID = 1L;

	public AbstractMultiBoardAdminCommand() {
	}

	public AbstractMultiBoardAdminCommand(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public abstract void execute(Board board) throws OpsBoardError;

	
	@Override
	public boolean matchBoard(Board board, Location location) {
		return true;
	}
}
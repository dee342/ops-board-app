package gov.nyc.dsny.smart.opsboard.commands.board;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.ActiveUserSession;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "AddSessionToBoard")
public class CommandAddUserSessionToBoard extends AbstractBoardCommand {

	private static final long serialVersionUID = 1L;

	private ActiveUserSession session;

	public CommandAddUserSessionToBoard() {
		super();
	}

	public CommandAddUserSessionToBoard(String boardId, String systemUser, Date systemDateTime,
			ActiveUserSession session) {
		super(boardId, systemUser, systemDateTime);
		this.session = session;

		createAuditMessage(null);
	}
	
	public CommandAddUserSessionToBoard(String boardId, LinkedHashMap<String, Object> map) {
		super (boardId, map);
	}


	@Override
	public void execute(Board board) throws OpsBoardError {
		// TODO Auto-generated method stub
	}

	public ActiveUserSession getSession() {
		return session;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("session", session);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		setAuditMessage("Logged onto board.");
	}
}

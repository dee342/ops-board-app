package gov.nyc.dsny.smart.opsboard.commands.board;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "RemoveSessionFromBoard")
public class CommandRemoveUserSessionFromBoard extends AbstractBoardCommand {

	private static final long serialVersionUID = 1L;

	private String httpSessionId;
	private String wsSessionId;

	public CommandRemoveUserSessionFromBoard() {
	}

	public CommandRemoveUserSessionFromBoard(String boardId, String systemUser, Date systemDateTime,
			String wsSessionId, String httpSessionId) {
		super(boardId, systemUser, systemDateTime);
		this.wsSessionId = wsSessionId;
		this.httpSessionId = httpSessionId;

		setAuditMessage("Logged out of board.");
	}
	
	public CommandRemoveUserSessionFromBoard(String boardId, LinkedHashMap<String, Object> map) {
		super (boardId, map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		// TODO Auto-generated method stub
	}

	public String getHttpSessionId() {
		return httpSessionId;
	}

	public String getWsSessionId() {
		return wsSessionId;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("httpSessionId", httpSessionId);
		builder.append("wsSessionId", wsSessionId);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		// TODO Auto-generated method stub
	}
}

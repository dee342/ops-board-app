package gov.nyc.dsny.smart.opsboard.commands.board;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "ReloadBoard")
public class CommandReloadBoard extends AbstractBoardCommand {

	private static final long serialVersionUID = 1L;

	private String reasonToReload;
	private String oldBoardId;

	public CommandReloadBoard(String boardId, LinkedHashMap<String, Object> content){
		super(boardId, content);
	
		if(content.containsKey("reasonToReload"))
			this.reasonToReload = content.get("reasonToReload").toString();
		if(content.containsKey("oldBoardId"))
			this.oldBoardId = content.get("oldBoardId").toString();
		
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		createAuditMessage(board);
	}

	public String getReasonToReload() {
		return reasonToReload;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		// TODO Auto-generated method stub
	}

	public void setReasonToReload(String reasonToReload) {
		this.reasonToReload = reasonToReload;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("reasonToReload", reasonToReload);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Copied board ");
		sb.append("[board:" + oldBoardId + "] ");
		sb.append("to [board:" + getBoardId() + "] ");
		sb.append("with reason [reason:" + reasonToReload + "].");

		setAuditMessage(sb.toString());
	}
}

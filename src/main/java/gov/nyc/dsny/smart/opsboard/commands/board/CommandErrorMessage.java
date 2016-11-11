package gov.nyc.dsny.smart.opsboard.commands.board;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "ErrorMessage")
public class CommandErrorMessage extends AbstractBoardCommand {

	private static final long serialVersionUID = 1L;

	private Object command;
	private int errorCode;
	private String errorMessage;

	public CommandErrorMessage(Object command) {
		this.command = command;
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		// TODO Auto-generated method stub
	}

	public Object getCommand() {
		return command;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		// TODO Auto-generated method stub
	}

	public void setCommand(Object command) {
		this.command = command;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("command", command);
		builder.append("errorCode", errorCode);
		builder.append("errorMessage", errorMessage);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		// TODO Auto-generated method stub
	}
}

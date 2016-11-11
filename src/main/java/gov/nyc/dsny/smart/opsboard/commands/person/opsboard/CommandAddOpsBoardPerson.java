package gov.nyc.dsny.smart.opsboard.commands.person.opsboard;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to add an OpsBoardPerson to a board.
 */
@IMultiBoardCommandAnnotation(commandName = "AddPerson")
public class CommandAddOpsBoardPerson extends AbstractOpsBoardPersonnelCommand {

	private static final long serialVersionUID = 1L;;

	public CommandAddOpsBoardPerson() {
		super();
	}

	public CommandAddOpsBoardPerson(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandAddOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, BoardPerson bp) {
		this(boardId, systemUser, systemDateTime, bp, false);
	}

	public CommandAddOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, BoardPerson bp,
			boolean update) {
		this(boardId, systemUser, systemDateTime, bp, false, update);
	}
	
	public CommandAddOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, BoardPerson bp,
			boolean fromIntegration, boolean update) {
		super(boardId, systemUser, systemDateTime, bp, fromIntegration, update);
	}

	public CommandAddOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, String personId) {
		this(boardId, systemUser, systemDateTime, personId, false, false);
	}

	public CommandAddOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, String personId, boolean fromIntegration,
			boolean update) {
		super(boardId, systemUser, systemDateTime, personId, fromIntegration, update);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// execute logic
		board.getPersonnel().put(getPersonId(), getBoardPerson());
		setState(getBoardPerson().getState(board.getLocation()).getState());
		setAssigned(getBoardPerson().isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(getBoardPerson().isAssigned());

		// create audit message
		createAuditMessage(board, getBoardPerson());

		// add command to history
		board.addCommandToHistory(this);
	}

	@Override
	protected void createAuditMessage(Board board, BoardPerson bp) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added person ");
		sb.append("[person:" + bp.getPerson().getId() + "].");

		setAuditMessage(sb.toString());
	}

}

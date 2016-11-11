package gov.nyc.dsny.smart.opsboard.commands.person.mdastatus;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to add an unavailability record to a person.
 */
@IMultiBoardCommandAnnotation(commandName = "AddPersonMdaStatus")
public class CommandAddPersonMdaStatus extends AbstractPersonMdaStatusCommand {

	private static final long serialVersionUID = 1L;

	public CommandAddPersonMdaStatus() {
		super();
	}

	public CommandAddPersonMdaStatus(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandAddPersonMdaStatus(String boardId, String systemUser, Date systemDateTime, String boardPersonId,
			String personId, MdaStatus mdaStatus) {
		super(boardId, systemUser, systemDateTime, boardPersonId, personId, mdaStatus);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardPerson bp = board.getPersonOps().addPersonMdaStatus(getPersonId(), getMdaStatus());
		setState(bp.getState(board.getLocation()).getState());
		setAssigned(bp.isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(bp.isAssigned());
		setActiveMdaStatus(bp.getActiveMdaCodes());

		// Create audit message
		createAuditMessage(board, bp, getMdaStatus());

		// Add command to history
		board.addCommandToHistory(this);
	}

	@Override
	protected void createAuditMessage(Board board, BoardPerson bp, MdaStatus status) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added mda for person ");
		sb.append("[person:" + bp.getPerson().getId() + "] ");
		sb.append(".");

		setAuditMessage(sb.toString());
	}
}

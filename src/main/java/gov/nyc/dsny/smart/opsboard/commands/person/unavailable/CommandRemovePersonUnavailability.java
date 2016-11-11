package gov.nyc.dsny.smart.opsboard.commands.person.unavailable;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;

import java.util.Date;
import java.util.LinkedHashMap;

@IMultiBoardCommandAnnotation(commandName = "RemovePersonUnavailability")
public class CommandRemovePersonUnavailability extends AbstractPersonUnavailabilityCommand {

	public static final String COMMAND_NAME = "RemovePersonUnavailability";

	private static final long serialVersionUID = 1L;

	public CommandRemovePersonUnavailability() {
	}

	public CommandRemovePersonUnavailability(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandRemovePersonUnavailability(String boardId, String systemUser, Date systemDateTime,
			String boardPersonId, String personId, UnavailabilityReason unavailableReason) {
		this(boardId, systemUser, systemDateTime, boardPersonId, personId, unavailableReason, false);
	}
	
	public CommandRemovePersonUnavailability(String boardId, String systemUser, Date systemDateTime,
			String boardPersonId, String personId, UnavailabilityReason unavailableReason, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardPersonId, personId, unavailableReason, fromIntegration);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		BoardPerson bp = board.getPersonOps().removePersonUnavailability(getPersonId(), getUnavailableReason());
		
		setState(bp.getState(board.getLocation()).getState());
		setAssigned(bp.isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(bp.isAssigned());
		setActiveUnavailabilityReasons(bp.getActiveUnavailabilityReasons());
		
		// Create audit message
		createAuditMessage(board, bp, getUnavailableReason());
		
		//Add command to history
		board.addCommandToHistory(this);
	}

	@Override
	protected void createAuditMessage(Board board, BoardPerson bp,
			UnavailabilityReason unavailabilityReason) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("Removed unavailability reason for person ");
		sb.append("[person:" + bp.getPerson().getId() + "] ");
		sb.append(".");
		setAuditMessage(sb.toString());	
	}

}
package gov.nyc.dsny.smart.opsboard.commands.person.unavailable;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;

@IMultiBoardCommandAnnotation(commandName = "UpdatePersonUnavailability")
public class CommandUpdatePersonUnavailability extends AbstractPersonUnavailabilityCommand {

	private static final long serialVersionUID = 1L;

	public CommandUpdatePersonUnavailability() {
	}

	public CommandUpdatePersonUnavailability(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public CommandUpdatePersonUnavailability(String boardId, String systemUser, Date systemDateTime,
			String boardPersonId, String personId, UnavailabilityReason unavailableReason) {
		this(boardId, systemUser, systemDateTime, boardPersonId, personId, unavailableReason, false);
	}
	
	public CommandUpdatePersonUnavailability(String boardId, String systemUser, Date systemDateTime,
			String boardPersonId, String personId, UnavailabilityReason unavailableReason, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardPersonId, personId, unavailableReason, fromIntegration);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		//Execute logic
		BoardPersonAndTasks bpts = board.getPersonOps().updatePersonUnavailability(getPersonId(), getUnavailableReason());
		
		//Update command
		updatePersonAndTasks(board, bpts);
		
		// Create audit message
		createAuditMessage(board, bpts.getBoardPerson(), getUnavailableReason());
		
		//Add command to history
		board.addCommandToHistory(this);
	}
	
	@Override
	protected void createAuditMessage(Board board, BoardPerson bp,
			UnavailabilityReason unavailabilityReason) {
		StringBuilder sb = new StringBuilder();
		sb.append("Updated unavailability reason for person ");
		sb.append("[person:" + bp.getPerson().getId() + "] ");
		sb.append(".");

		setAuditMessage(sb.toString());	
	}
}
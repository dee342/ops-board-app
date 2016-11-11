package gov.nyc.dsny.smart.opsboard.commands.person.opsboard;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.GroundingStatus;

import java.util.Date;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Command to add an OpsBoardPerson to a board.
 */
@IMultiBoardCommandAnnotation(commandName = "GroundPerson")
public class CommandGroundOpsBoardPerson extends AbstractOpsBoardPersonnelCommand {

	private static final long serialVersionUID = 1L;

	private boolean grounded = false;
	private GroundingStatus historicalGroundingStatus;

	public CommandGroundOpsBoardPerson() {
	}

	public CommandGroundOpsBoardPerson(LinkedHashMap<String, Object> map) {
		super(map);
		ObjectMapper mapper = new ObjectMapper();
		try {
			historicalGroundingStatus = mapper.readValue(map.get("historicalGroundingStatus").toString(),
					GroundingStatus.class);
		} catch (Exception e) {
			historicalGroundingStatus = null;
		}
	}

	public CommandGroundOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, BoardPerson bp,
			GroundingStatus historicalGroundingStatus) {
		super(boardId, systemUser, systemDateTime, bp, true);
		this.historicalGroundingStatus = historicalGroundingStatus;
	}

	public CommandGroundOpsBoardPerson(String boardId, String systemUser, Date systemDateTime, String personId,
			GroundingStatus groundingStatus) {
		super(boardId, systemUser, systemDateTime, personId);
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		BoardPerson bp = board.getPersonnel().get(getPersonId());
		bp.getPerson().addGroundingStatus(historicalGroundingStatus);

		setState(bp.getState(board.getLocation()).getState());
		setAssigned(bp.isAssigned(board.getLocation().getCode()));
		setAssignedAnywhere(bp.isAssigned());
		grounded = bp.isGrounded();

		// Create audit message
		createAuditMessage(board, bp);
		// Add command to history
		board.addCommandToHistory(this);
	}

	public GroundingStatus getHistoricalGroundingStatus() {
		return historicalGroundingStatus;
	}

	public boolean isGrounded() {
		return grounded;
	}

	public void setGrounded(boolean grounded) {
		this.grounded = grounded;
	}

	public void setHistoricalGroundingStatus(GroundingStatus historicalGroundingStatus) {
		this.historicalGroundingStatus = historicalGroundingStatus;
	}

	@Override
	protected void createAuditMessage(Board board, BoardPerson bp) {
		StringBuilder sb = new StringBuilder();
		sb.append("Grounded person ");
		sb.append(bp.getPerson().getId());
		sb.append(".");

		setAuditMessage(sb.toString());
	}

}

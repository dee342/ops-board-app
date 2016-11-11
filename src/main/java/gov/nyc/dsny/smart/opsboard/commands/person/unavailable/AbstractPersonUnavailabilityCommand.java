package gov.nyc.dsny.smart.opsboard.commands.person.unavailable;

import gov.nyc.dsny.smart.opsboard.commands.interfaces.IUnavailabilityReasonCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.AbstractMultiBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Represents the base class for all multi-board commands for personnel unavailability operations. The class extends
 * base AbstractMultiBoardPersonnelCommand with unavailability fields.
 */
public abstract class AbstractPersonUnavailabilityCommand extends AbstractMultiBoardPersonnelCommand implements IUnavailabilityReasonCommand{

	private static final long serialVersionUID = 1L;
	private List<UnavailabilityReason> activeUnavailabilityReasons;

	private UnavailabilityReason unavailableReason;

	public AbstractPersonUnavailabilityCommand() {
	}

	public AbstractPersonUnavailabilityCommand(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public AbstractPersonUnavailabilityCommand(String boardId, String systemUser, Date systemDateTime,
			String boardPersonId, String personId, UnavailabilityReason unavailableReason) {
		super(boardId, systemUser, systemDateTime, boardPersonId, personId, false);
		this.unavailableReason = unavailableReason;
	}
	
	public AbstractPersonUnavailabilityCommand(String boardId, String systemUser, Date systemDateTime,
			String boardPersonId, String personId, UnavailabilityReason unavailableReason, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardPersonId, personId, fromIntegration);
		this.unavailableReason = unavailableReason;
	}

	public List<UnavailabilityReason> getActiveUnavailabilityReasons() {
		return activeUnavailabilityReasons;
	}

	public UnavailabilityReason getUnavailableReason() {
		return unavailableReason;
	}

	public void setActiveUnavailabilityReasons(List<UnavailabilityReason> activeUnavailabilityReasons) {
		this.activeUnavailabilityReasons = activeUnavailabilityReasons;
	}

	public void setUnavailableReason(UnavailabilityReason unavailableReason) {
		this.unavailableReason = unavailableReason;
	}
	
	protected void updatePersonAndTasks(Board board, BoardPersonAndTasks bpts) {
		super.updatePersonAndTasks(board, bpts);
		setActiveUnavailabilityReasons(bpts.getBoardPerson().getActiveUnavailabilityReasons());
	}
	
	protected abstract void createAuditMessage(Board board, BoardPerson bp, UnavailabilityReason unavailabilityReason );

}

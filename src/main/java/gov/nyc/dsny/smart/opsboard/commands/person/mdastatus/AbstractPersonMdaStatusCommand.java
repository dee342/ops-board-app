package gov.nyc.dsny.smart.opsboard.commands.person.mdastatus;

import gov.nyc.dsny.smart.opsboard.commands.interfaces.IMdaStatusCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.AbstractMultiBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the base class for all multi-board commands for personnel MDA status operations. The class extends base
 * AbstractMultiBoardPersonnelCommand with unavailability fields.
 */
public abstract class AbstractPersonMdaStatusCommand extends AbstractMultiBoardPersonnelCommand implements
IMdaStatusCommand {

	private static final long serialVersionUID = 1L;
	private List<MdaStatus> activeMdaStatus;
	private MdaStatus mdaStatus;

	public AbstractPersonMdaStatusCommand() {
		super();
	}

	public AbstractPersonMdaStatusCommand(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public AbstractPersonMdaStatusCommand(String boardId, String systemUser, Date systemDateTime, String boardPersonId,
			String personId, MdaStatus mdaStatus) {
		super(boardId, systemUser, systemDateTime, boardPersonId, personId, false);
		this.mdaStatus = mdaStatus;
	}

	public List<MdaStatus> getActiveMdaStatus() {
		return activeMdaStatus;
	}

	@Override
	public MdaStatus getMdaStatus() {
		return mdaStatus;
	}

	public void setActiveMdaStatus(List<MdaStatus> activeMdaStatus) {
		this.activeMdaStatus = activeMdaStatus;
	}

	@Override
	public void setMdaStatus(MdaStatus mdaStatus) {
		this.mdaStatus = mdaStatus;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("mdaStatus", mdaStatus);
		builder.append(super.toString());

		return builder.toString();
	}

	protected abstract void createAuditMessage(Board board, BoardPerson bp, MdaStatus status);

	@Override
	protected void updatePersonAndTasks(Board board, BoardPersonAndTasks bpts) {
		super.updatePersonAndTasks(board, bpts);
		setActiveMdaStatus(bpts.getBoardPerson().getActiveMdaCodes());

	}
}

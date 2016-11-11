package gov.nyc.dsny.smart.opsboard.commands.person.detach;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IDetachmentCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.AbstractMultiBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardPersonAndTasks;

import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Represents the base class for all multi-board commands for personnel detachment operations. The class extends base
 * AbstractMultiBoardPersonnelCommand with detachment fields.
 */
public abstract class AbstractPersonAttachDetachCommand extends AbstractMultiBoardPersonnelCommand
		implements
			IDetachmentCommand {

	private static final long serialVersionUID = 1L;

	private String currentLocation;
	private Detachment detachment;
	private transient Location from;
	private transient Location to;

	public AbstractPersonAttachDetachCommand() {
	}

	public AbstractPersonAttachDetachCommand(LinkedHashMap<String, Object> map) {
		super(map);
	}

	public AbstractPersonAttachDetachCommand(String boardId, String boardPersonId, String personId, Location from,
			Location to, Detachment detachment) {
		super(boardId, detachment.getSystemUser(), detachment.getLastModifiedSystem(), boardPersonId, personId, false);

		this.detachment = detachment;
		this.from = from;
		this.to = to;
	}

	@Override
	public abstract void execute(Board board) throws OpsBoardError;

	public String getCurrentLocation() {
		return currentLocation;
	}

	@Override
	public Detachment getDetachment() {
		return detachment;
	}

	@Override
	@JsonIgnore
	public Location getFrom() {
		return from;
	}

	public String getFromCode() {
		return from != null ? from.getCode() : null;
	}

	@Override
	@JsonIgnore
	public Location getTo() {
		return to;
	}

	public String getToCode() {
		return to != null ? to.getCode() : null;
	}

	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}

	@Override
	public void setDetachment(Detachment detachment) {
		this.detachment = detachment;
	}

	@Override
	public void setFrom(Location from) {
		this.from = from;
	}

	@Override
	public void setTo(Location to) {
		this.to = to;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("detachment", detachment);
		builder.append("currentLocation", currentLocation);
		builder.append("from", from);
		builder.append("to", to);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void updatePersonAndTasks(Board board, BoardPersonAndTasks bpts) {

		// Set person details
		if (bpts.getBoardPerson() != null) {
			StateAndLocation sl = bpts.getBoardPerson().getState(board.getLocation());
			setCurrentLocation(sl.getLocation().getCode());
			setState(sl.getState());

			setAssigned(bpts.getBoardPerson().isAssigned(sl.getLocation().getCode()));
			setAssignedAnywhere(bpts.getBoardPerson().isAssigned());
		}

		// Set task details
		if (bpts.getTasks() != null) {
			setTasks(convertPersonTasksToTaskAssigments(bpts));
		}

	}

}

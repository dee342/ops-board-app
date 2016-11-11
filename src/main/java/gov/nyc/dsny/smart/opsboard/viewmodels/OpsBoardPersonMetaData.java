package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class OpsBoardPersonMetaData implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean assigned;
	private boolean assignedAnywhere;
	private String boardPersonId;
	private String personId;
	private String state;
	private Set<TaskAssignment> tasks;
	private List<UnavailabilityReason> activeUnavailabilityReasons;

	public OpsBoardPersonMetaData() {
		super();
	}

	public OpsBoardPersonMetaData(boolean assigned, String boardPersonId, String personId, String state,
			Set<TaskAssignment> tasks) {
		super();
		this.assigned = assigned;
		this.boardPersonId = boardPersonId;
		this.personId = personId;
		this.state = state;
		this.tasks = tasks;
	}

	public String getBoardPersonId() {
		return boardPersonId;
	}

	public String getPersonId() {
		return personId;
	}

	public String getState() {
		return state;
	}

	public Set<TaskAssignment> getTasks() {
		return tasks;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public boolean isAssignedAnywhere() {
		return assignedAnywhere;
	}

	public void setAssignedAnywhere(boolean assignedAnywhere) {
		this.assignedAnywhere = assignedAnywhere;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public void setBoardPersonId(String boardPersonId) {
		this.boardPersonId = boardPersonId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setTasks(Set<TaskAssignment> tasks) {
		this.tasks = tasks;
	}

	public List<UnavailabilityReason> getActiveUnavailabilityReasons() {
		return activeUnavailabilityReasons;
	}

	public void setActiveUnavailabilityReasons(
			List<UnavailabilityReason> activeUnavailabilityReasons) {
		this.activeUnavailabilityReasons = activeUnavailabilityReasons;
	}

	@Override
	public String toString() {
		return "PersonTaskAssignment [assigned=" + assigned + ", boardPersonId=" + boardPersonId + ", personId="
				+ personId + ", state=" + state + "]";
	}


}

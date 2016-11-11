package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.commands.task.AbstractTaskCommand;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.google.common.base.Strings;

/**
 * Represents the base class for all task board commands with personnel. The class extends base AbstractTaskCommand with
 * person fields.
 */
public abstract class AbstractTaskPersonCommand extends AbstractTaskCommand {

	private static final long serialVersionUID = 1L;

	private boolean assigned;
	private boolean assignedAnywhere;
	private Date assignmentTime;
	private String assignType;
	private boolean completed;
	private String personId;
	private int position;
	private String state;
	private Shift shift;
	

	public AbstractTaskPersonCommand(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		personId = map.get("personId") != null ? (String) map.get("personId") : null;
		position = map.get("position") != null ? (Integer) map.get("position") : 0;
		assignType = map.get("assignType") != null ? map.get("assignType").toString() : null;
		assigned = (Boolean) map.getOrDefault("assigned", false);
		assignmentTime = map.get("assignmentTime") != null ? new Date((Long) map.get("assignmentTime")) : null;
		completed = (Boolean) map.getOrDefault("completed", false);
		state =  Strings.emptyToNull((String) map.getOrDefault("state", ""));

	}

	protected AbstractTaskPersonCommand() {
	}

	public Date getAssignmentTime() {
		return assignmentTime;
	}

	public String getAssignType() {
		return assignType;
	}

	public String getPersonId() {
		return personId;
	}

	public int getPosition() {
		return position;
	}

	public String getState() {
		return state;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public void setAssignmentTime(Date assignmentTime) {
		this.assignmentTime = assignmentTime;
	}

	public void setAssignType(String assignType) {
		this.assignType = assignType;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setState(String state) {
		this.state = state;
	}	

	public Shift getShift() {
		return shift;
	}

	public void setShift(Shift shift) {
		this.shift = shift;
	}
	
	public boolean isAssignedAnywhere() {
		return assignedAnywhere;
	}

	public void setAssignedAnywhere(boolean assignedAnywhere) {
		this.assignedAnywhere = assignedAnywhere;
	}
	

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("assigned", assigned);
		builder.append("assignmentTime", assignmentTime);
		builder.append("assignType", assignType);
		builder.append("completed", completed);
		builder.append("personId", personId);
		builder.append("position", position);
		builder.append("state", state);
		builder.append(super.toString());

		return builder.toString();
	}
}

package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import gov.nyc.dsny.smart.opsboard.commands.task.AbstractTaskCommand;
import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the base class for all task board commands with equipment. The class extends base AbstractTaskCommand with
 * equipment fields.
 */
public abstract class AbstractTaskEquipmentCommand extends AbstractTaskCommand {

	private static final long serialVersionUID = 1L;

	private boolean assigned;
	private Date assignmentTime;
	private boolean completed;
	private String equipmentId;
	private Map<String, String> states = new HashMap<String, String>();

	public AbstractTaskEquipmentCommand() {
	}

	@SuppressWarnings("unchecked")
	public AbstractTaskEquipmentCommand(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		equipmentId = map.get("equipmentId").toString();
		assigned = (Boolean) map.getOrDefault("assigned", false);
		assignmentTime = map.get("assignmentTime") != null ? new Date((Long) map.get("assignmentTime")) : null;
		completed = (Boolean) map.getOrDefault("completed", false);
		if (map.get("states") != null) {
			states = (Map<String, String>) map.get("states");
		}
	}

	public Date getAssignmentTime() {
		return assignmentTime;
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public Map<String, String> getStates() {
		return states;
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

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	public void setStates(Location loc, BoardEquipment be) {
		states.clear();
		if (loc.isServicesEquipmentLocations()) {
			for (Location el : loc.getServiceLocations()) {
				StateAndLocation sl = be.getState(el);
				states.put(el.getCode(), sl.getState());
			}
		} else {
			StateAndLocation sl = be.getState(loc);
			states.put(loc.getCode(), sl.getState());
		}
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("assigned", assigned);
		builder.append("assignmentTime", assignmentTime);
		builder.append("completed", completed);
		builder.append("equipmentId", equipmentId);
		builder.append("states", states);
		builder.append(super.toString());

		return builder.toString();
	}
	
}

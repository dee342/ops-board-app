package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TaskContainer implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;

	private Location location;

	private Map<String, LocationShift> locationShifts = new HashMap<String, LocationShift>();

	public TaskContainer(gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer domain, Location boardLocation) {
		super();
		this.id = domain.getId();
		location = domain.getLocation();
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift ls : domain.getLocationShifts()) {
			locationShifts.put(ls.getId(), new LocationShift(ls, boardLocation));
		}
	}

	public long getId() {
		return id;
	}

	@JsonIgnore
	public Location getLocation() {
		return location;
	}
	
	public String getLocationCode(){
		return location.getCode();
	}

	public Map<String, LocationShift> getLocationShifts() {
		return locationShifts;
	}
}

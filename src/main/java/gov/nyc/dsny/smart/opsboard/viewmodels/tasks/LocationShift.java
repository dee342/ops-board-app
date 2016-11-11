package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LocationShift implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private Shift shift;

	private Map<String, ShiftCategory> shiftCategories = new HashMap<String, ShiftCategory>();
	
	private Map<String, Category> addedCategories = new HashMap<String, Category>();
	
	private List<AddPartialTaskRequest> partialTaskGroups = new ArrayList<AddPartialTaskRequest>(); // for later to have a list of partialTask groups
	private static int counter = 0;

	public LocationShift(gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift domain, Location boardLocation) {
		super();
		this.id = domain.getId();
		shift = domain.getShift();
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.ShiftCategory sc : domain.getShiftCategories()) {
			shiftCategories.put(sc.getId(), new ShiftCategory(sc, boardLocation));
			addedCategories.put(sc.getId(), sc.getCategory());
		}
	}

	public String getId() {
		return id;
	}

	@JsonIgnore
	public Shift getShift() {
		return shift;
	}
	
	public long getShiftId(){
		return shift.getId();
	}

	public Map<String, ShiftCategory> getShiftCategories() {
		return shiftCategories;
	}

	public Map<String, Category> getAddedCategories() {
		return addedCategories;
	}

	public void setAddedCategories(Map<String, Category> addedCategories) {
		this.addedCategories = addedCategories;
	}

	public static int getCounter() {
		return counter;
	}

	public static void setCounter(int counter) {
		LocationShift.counter = counter;
	}

	public List<AddPartialTaskRequest> getPartialTaskGroups() {
		return partialTaskGroups;
	}

	public void setPartialTaskGroups(List<AddPartialTaskRequest> partialTaskGroups) {
		this.partialTaskGroups = partialTaskGroups;
	}
}

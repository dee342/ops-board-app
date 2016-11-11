package gov.nyc.dsny.smart.opsboard.viewmodels.reference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkUnit {

    private Set<Location> locations = new HashSet<Location>();
    protected int sortSequence;
    

    private String workunitCode;
    
    private String workunitDescription;

    public WorkUnit() {
        super();
    }
    
    public WorkUnit(gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit wu) {
        super();
        this.workunitCode = wu.getWorkunitCode();
        this.workunitDescription = wu.getWorkunitDescription();
        this.sortSequence = wu.getSortSequence();
        if (wu.getLocations() != null) {
        	for (gov.nyc.dsny.smart.opsboard.domain.reference.Location loc : wu.getLocations()) {
        		locations.add(new Location(loc));
        	}
        }
    }

	public Set<Location> getLocations() {
		return locations;
	}

	public int getSortSequence() {
		return sortSequence;
	}

	public String getWorkunitCode() {
		return workunitCode;
	}

	public String getWorkunitDescription() {
		return workunitDescription;
	}

	public void setLocations(Set<Location> locations) {
		this.locations = locations;
	}

	public void setSortSequence(int sortSequence) {
		this.sortSequence = sortSequence;
	}

	public void setWorkunitCode(String workunitCode) {
		this.workunitCode = workunitCode;
	}

	public void setWorkunitDescription(String workunitDescription) {
		this.workunitDescription = workunitDescription;
	}
}

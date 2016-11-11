package gov.nyc.dsny.smart.opsboard.viewmodels.reference;

import java.io.Serializable;

public class Location implements Serializable {
	
	/**
	 * Default serialization id.
	 */
	private static final long serialVersionUID = 1L;
	
    private boolean hasGarage;  
    private String locationCode;
    private String locationDescription;
    private String locationParentCode;
    private String locationParentType;
    private String locationType;
    private int sortSequence;
    private String boardType;


    public Location(gov.nyc.dsny.smart.opsboard.domain.reference.Location loc) {
        this.hasGarage = loc.isHasGarage();
        this.locationCode = loc.getCode();
        this.locationDescription = loc.getDescription();
        if (loc.getBorough() != null) {
        	this.locationParentCode = loc.getBorough().getCode();
        	this.locationParentType = loc.getBorough().getType();
        }
        this.sortSequence = loc.getSortSequence();
        if(loc.getBoardType()!=null)
        this.boardType = loc.getBoardType().getCode();
    }


	public boolean isHasGarage() {
		return hasGarage;
	}


	public String getLocationCode() {
		return locationCode;
	}


	public String getLocationDescription() {
		return locationDescription;
	}


	public String getLocationParentCode() {
		return locationParentCode;
	}


	public String getLocationParentType() {
		return locationParentType;
	}


	public String getLocationType() {
		return locationType;
	}


	public int getSortSequence() {
		return sortSequence;
	}


	public String getBoardType() {
		return boardType;
	}


	@Override
	public String toString() {
		return locationCode;
	}
	
	
}

package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

public class SnowReadiness extends ViewModel<gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1207568770737320088L;

	/** Flag to indicate if Equipment is chained. */
	private boolean chained = false;

	/** Types of loads the Equipment can accomodate. */
	private String load;

	/** Types of plows the Equipment can accommodate. */
	private String plowType;

	/** Direction of plow */
	private String plowDirection;

	/** Direction of snowAssignment */
	private boolean workingDown = false;

	/** Direction of snowAssignment */
	private boolean snowAssignment = false;

	
	public SnowReadiness() {
		super(gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness.class);
	}

	public String getLoad() {
		return load;
	}

	public String getPlowDirection() {
		return plowDirection;
	}

	public String getPlowType() {
		return plowType;
	}

	public boolean isChained() {
		return chained;
	}

	public boolean isSnowAssignment() {
		return snowAssignment;
	}

	public boolean isWorkingDown() {
		return workingDown;
	}

	public void setChained(boolean chained) {
		this.chained = chained;
	}

	public void setLoad(String load) {
		this.load = load;
	}

	public void setPlowDirection(String plowDirection) {
		this.plowDirection = plowDirection;
	}

	public void setPlowType(String plowType) {
		this.plowType = plowType;
	}

	public void setSnowAssignment(boolean snowAssignment) {
		this.snowAssignment = snowAssignment;
	}

	public void setWorkingDown(boolean workingDown) {
		this.workingDown = workingDown;
	}

}

package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * The OpsBoardEquipment is an Equipment class that is tied to a specific location, creating a static value for state.
 */
public class OpsBoardEquipment implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** The equipment. */
	private BoardEquipment be;

	/** The current location of the equipment. */
	private Location currentLocation;

	/** The location of the board. */
	private Location location;

	public OpsBoardEquipment(BoardEquipment be, Location l) {
		this.be = be;
		location = l;
	}
	
	@JsonUnwrapped
	public OpsBoardEquipmentDetails getOpsBoardEquipmentDetails(){
		return new OpsBoardEquipmentDetails(be);
	}

	public List<Bin> getBins() {
		if (be != null) {
			return be.getMostRecentBins();
		} else {
			return new ArrayList<Bin>();
		}
	}

	public String getBoardDate() {
		if (be != null) {
			return be.getBoardDate();
		} else {
			return "";
		}
	}

	public String getBoardEquipmentId() {
		if (be != null) {
			return be.getId();
		} else {
			return "";
		}
	}

	public Float getCubicCapacity() {
		if (be != null) {
			return be.getSeries().getCubicCapacity();
		} else {
			return null;
		}
	}

	/**
	 * Gets the current location.
	 *
	 * @return the current location
	 */
	public String getCurrentLocation() {
		if (currentLocation == null) {
			getStates(); // for recalculation of current location
		}
		return currentLocation.getCode();
	}

	public Date getDepExpirationDate() {
		if (be != null) {
			return be.getDepExpirationDate();
		} else {
			return null;
		}
	}

	public String getDepNumber() {
		if (be != null) {
			return be.getDepNumber();
		} else {
			return "";
		}
	}

	/*
	 * public UpDown getUpDownById(Long id) { return null; equipment.getUpDownById(id); }
	 */

	public Date getEndDate() {
		if (be != null) {
			return be.getEndDate();
		} else {
			return null;
		}
	}

	public String getFrontTires() {
		if (be != null) {
			return be.getSeries().getFrontTires();
		} else {
			return "";
		}
	}

	public String getFuelType() {
		if (be != null) {
			return be.getSeries().getFuelType();
		} else {
			return "";
		}
	}

	public String getGpsMEID() {
		if (be != null) {
			return be.getGpsMeid();
		} else {
			return "";
		}
	}

	public String getGpsPhone() {
		if (be != null) {
			return be.getGpsPhone();
		} else {
			return "";
		}
	}

	public String getGroup() {
		if (be != null) {
			return be.getSeries().getGroup();
		} else {
			return "";
		}
	}

	public String getHeight() {
		if (be != null) {
			return be.getSeries().getHeight();
		} else {
			return "";
		}
	}

	public String getHopperHeight() {
		if (be != null) {
			return be.getSeries().getHopperHeight();
		} else {
			return "";
		}
	}

	public String getId() {
		if (be.getEquipment() != null) {
			return be.getEquipment().getId();
		} else if (StringUtils.isNoneBlank(getBoardEquipmentId())) {
			return getBoardEquipmentId().substring(0, getBoardEquipmentId().indexOf("_"));
		} else {
			return "";
		}
	}

	public Date getInServiceDate() {
		if (be != null) {
			return be.getInServiceDate();
		} else {
			return null;
		}
	}

	public String getLength() {
		if (be != null) {
			return be.getSeries().getLength();
		} else {
			return "";
		}
	}

	public String getLicensePlate() {
		if (be != null) {
			return be.getLicensePlate();
		} else {
			return "";
		}
	}

	public String getManufacturer() {
		if (be != null) {
			return be.getSeries().getManufacturer();
		} else {
			return "";
		}
	}

	public Integer getMaxSpeed() {
		if (be != null) {
			return be.getSeries().getMaxSpeed();
		} else {
			return null;
		}
	}

	public Integer getModelYear() {
		if (be != null) {
			return be.getSeries().getModelYear();
		} else {
			return null;
		}
	}

	public String getName() {
		if (be != null) {
			return be.getName();
		} else {
			return "";
		}
	}

	public String getOwner() {
		if (be != null && be.getOwner() != null) {
			return be.getOwner().getCode();
		} else {
			return null;
		}
	}

	public String getRadioId() {
		if (be != null) {
			return be.getRadioId();
		} else {
			return "";
		}
	}

	public String getRearTires() {
		if (be != null) {
			return be.getSeries().getRearTires();
		} else {
			return "";
		}
	}

	public SnowReadiness getSnowReadiness() {
		if (be != null) {
			return be.getSnowReadiness();
		}
		return null;
	}

	public Date getStartDate() {
		if (be != null) {
			return be.getStartDate();
		}
		return null;
	}

	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public Map<String, String> getStates() {
		Map<String, String> states = new HashMap<String, String>();
		if (location.isServicesEquipmentLocations()) {
			for (Location el : location.getServiceLocations()) {
				StateAndLocation sl = be.getState(el);
				states.put(el.getCode(), sl.getState());
				currentLocation = sl.getLocation(); // can continue to overwrite as method always returns current location of equipment
			}
		} else {
			StateAndLocation sl = be.getState(location);
			states.put(location.getCode(),  sl.getState());
			currentLocation = sl.getLocation();
		}

		return states;
	}

	public String getSubType() {
		if (be != null) {
			return be.getSubType();
		} else {
			return "";
		}
	}

	public SubType getSubTypeObj() {
		if (be != null) {
			return be.getSubtypeObj();
		} else {
			return null;
		}
	}

	public Integer getTankCapacity() {
		if (be != null) {
			return be.getSeries().getTankCapacity();
		} else {
			return null;
		}
	}

	public Float getTareWeight() {
		if (be != null) {
			return be.getSeries().getTareWeight();
		} else {
			return null;
		}
	}

	public String getTonnageCapacity() {
		if (be != null) {
			return be.getSeries().getTonnageCapacity();
		} else {
			return "";
		}
	}

	public String getTransponderId() {
		if (be != null) {
			return be.getTransponderId();
		} else {
			return "";
		}
	}

	public String getTurnRadius() {
		if (be != null) {
			return be.getSeries().getTurnRadius();
		} else {
			return "";
		}
	}

	public String getType() {
		if (be != null) {
			return be.getSeries().getType();
		} else {
			return "";
		}
	}

	public Float getUnitPrice() {
		if (be != null) {
			return be.getSeries().getUnitPrice();
		} else {
			return null;
		}
	}

	public String getVehicleIdentificationNumber() {
		if (be != null) {
			return be.getVehicleIdentificationNumber();
		} else {
			return "";
		}
	}

	public String getWidth() {
		if (be != null) {
			return be.getSeries().getWidth();
		} else {
			return "";
		}
	}
	
	public long getDetachmentCount(){
		return be.getDetachmentCount();
	}
	
	public long getConditionsCount(){
		return be.getConditionsCount();
	}

	@Override
	public int hashCode() {
		return be.hashCode();
	}
	
	public boolean isActive(){
		return be.isActive();
	}

	public boolean isAssigned() {
		return be.isAssigned();
	}

	@Override
	public String toString() {
		return "OpsBoardEquipment [location=" + location + ", equipment=" + be + "]";
	}
}

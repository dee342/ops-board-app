package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * The OpsBoardPerson is an Person class that is tied to a specific location, creating a static value for state.
 */
public class OpsBoardPerson implements Serializable {

	/**
	 * Default serialization id.
	 */
	private static final long serialVersionUID = 1L;

	/** The person. */
	private BoardPerson bp;

	/** The current location of the equipment. */
	private Location currentLocation;

	/** The location. */
	private Location location;

	public OpsBoardPerson(BoardPerson bp, Location l) {
		this.bp = bp;
		location = l;
	}
	
	@JsonUnwrapped
	public OpsBoardPersonDetails getOpsBoardPersonDetails(){
		return new OpsBoardPersonDetails(bp);
	}

	public Detachment getActiveDetachment() {
		return bp.getActiveDetachment();
	}

	public List<MdaStatus> getActiveMdaCodes() {
		return bp.getActiveMdaCodes();
	}

	public List<SpecialPosition> getActiveSpecialPositions() {
		return bp.getActiveSpecialPositions();
	}

	public List<UnavailabilityReason> getActiveUnavailabilityReasons() {
		return bp.getActiveUnavailabilityReasons();
	}

	public String getBadgeNumber() {
		return bp.getBadgeNumber();
	}
	
	public Date getBirthDate() {
		return bp.getBirthDate();
	}

	public String getBoardDate() {
		return bp.getBoardDate();
	}

	public String getBoardPersonId() {
		if (bp != null) {
			return bp.getId();
		} else {
			return "";
		}
	}

	public String getChartName() {
		return bp.getChartName();
	}

	public String getChartNumber() {
		return bp.getChartNumber();
	}

	public String getCivilServiceTitle() {
		return bp.getCivilServiceTitle();
	}

	/**
	 * Gets the current location.
	 *
	 * @return the current location
	 */
	public String getCurrentLocation() {
		if(getId().equals("27871"))
			System.out.println();
		if (currentLocation == null) {
			getState(); // for recalculation of current location
		}
		return currentLocation.getCode();
	}

	public Date getDate() {
		return bp.getDate();
	}

	public String getDepartmentType() {
		return bp.getDepartmentType();
	}

	public Date getEndDate() {
		return bp.getEndDate();
	}

	public String getFirstName() {
		return bp.getFirstName();
	}

	public String getFullName() {
		return bp.getFullName();
	}

	public Set<String> getFutureWorkLocations() {
		if (bp != null) {
			return bp.getFutureWorkLocations().stream().map(b -> b.getCode()).collect(Collectors.toSet());
		} else {
			return new HashSet<String>();
		}
	}
	
	public String getHomeLocation() {
		return bp.getHomeLocation() != null ? bp.getHomeLocation().getCode() : null;
	}

	public String getId() {
		if (bp.getPerson() != null) {
			return bp.getPerson().getId();
		} else if (StringUtils.isNoneBlank(getBoardPersonId())) {
			return getBoardPersonId().substring(0, getBoardPersonId().indexOf("_"));
		} else {
			return "";
		}
	}

	public String getLastName() {
		return bp.getLastName();
	}

	public String getListNumber() {
		return bp.getListNumber();
	}

	public String getMiddleName() {
		return bp.getMiddleName();
	}

	public String getPayrollLocationId() {
		return bp.getPayrollLocationId();
	}

	public Date getPromotionDate() {
		return bp.getPromotionDate();
	}

	public Set<String> getQualifications() {
		return bp.getQualifications();
	}

	public String getReferenceNum() {
		return bp.getReferenceNum();
	}

	public Date getSeniorityDate() {
		return bp.getSeniorityDate();
	}

	public Date getStartDate() {
		return bp.getStartDate();
	}

	public String getState() {
		StateAndLocation sl = bp.getState(location);
		currentLocation = sl.getLocation();

		if (sl.getState() != null) {
			return sl.getState();
		} else {
			return "";
		}
	}

	public String getWorkLocation() {
		return bp.getWorkLocation() != null ? bp.getWorkLocation().getCode(): null;
	}
	
	public boolean isActive(){
		return bp.isActive();
	}

	public boolean isAssigned() {
		return bp.isAssigned(location.getCode());
	}
	
	public boolean isAssignedAnywhere(){
		return bp.isAssigned();
	}
	
	public boolean isAvailableNextDay() {
		return bp.isAvailableNextDay();
	}
	
	public Set<Shift> getAssignedNextDayShifts(){
		return bp.getAssignedNextDayShifts(location.getCode());
	}		
	
	public Set<Shift> getDayBeforeShifts(){
		return bp.getDayBeforeShifts(location.getCode());
	}	
	
	public boolean isGrounded(){
		return bp.isGrounded();
	}

	public boolean isOfficer() {
		return bp.isOfficer();
	}
	
	public Long getDetachmentCount() {
		return bp.getDetachmentCount();
	}
	
    public Long getGroundingStatusCount() {
        return bp.getGroundingStatusCount();
    }



	public Long getMdaStatusCount() {
		return bp.getMdaStatusCount();
	}

	public Long getSpecialPositionCount() {
		return bp.getSpecialPositionCount();
	}

	public Long getUnavailabilityReasonCount() {
		return bp.getUnavailabilityReasonCount();
	}
	
	@Override
	public String toString() {
		return "OpsBoardPerson [location=" + location + ", person=" + bp + "]";
	}
}

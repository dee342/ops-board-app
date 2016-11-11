package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.domain.general.Address;
import gov.nyc.dsny.smart.opsboard.domain.general.EmergencyContact;
import gov.nyc.dsny.smart.opsboard.domain.general.Phone;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.GroundingStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * The OpsBoardPerson is an Person class that is tied to a specific location, creating a static value for state.
 */
public class OpsBoardPersonDetails implements Serializable {

	/**
	 * Default serialization id.
	 */
	private static final long serialVersionUID = 1L;

	/** The person. */
	private BoardPerson bp;


	public OpsBoardPersonDetails(BoardPerson bp) {
		this.bp = bp;
	}


	
	public Set<Address> getAddresses() {
		return bp.getAddresses();
	}

	public String getBoardPersonId() {
		if (bp != null) {
			return bp.getId();
		} else {
			return "";
		}
	}

	
	public ConcurrentSkipListSet<Detachment> getDetachmentHistory() {
		return bp.getDetachmentHistory();
	}
	
	
	public EmergencyContact getEmergencyContact() {
		return bp.getEmergencyContact();
	}


	
	public ConcurrentSkipListSet<GroundingStatus> getGroundingHistory() {
		return bp.getGroundingHistory();
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

	
	public ConcurrentSkipListSet<MdaStatus> getMdaStatusHistory() {
		return bp.getMdaStatusHistory();
	}

	
	public Set<Phone> getPhones() {
		return bp.getPhones();
	}

	//TODO VERIFY THIS
	
	public HashSet<String> getSetOfBoroBoards() {
		return (HashSet<String>) bp.getSetOfBoroBoards().stream().map(b -> b.getCode()).collect(Collectors.toSet());
	}

	
	public ConcurrentSkipListSet<SpecialPosition> getSpecialPositionsHistory() {
		return bp.getSpecialPositionHistory();
	}

	
	public ConcurrentSkipListSet<UnavailabilityReason> getUnavailabilityHistory() {
		return bp.getUnavailabilityHistory();
	}

	@Override
	public String toString() {
		return "OpsBoardPerson [person=" + bp + "]";
	}
}

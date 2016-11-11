package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * The OpsBoardEquipmentDetaisls is an DTO class that holds all relevant Equipment Details that are sent via sockets.
 */
public class OpsBoardEquipmentDetails implements Serializable {
	
	private static final long serialVersionUID = 1L;

	/** The equipment. */
	private BoardEquipment be;

	public OpsBoardEquipmentDetails(BoardEquipment be) {
		this.be = be;
	}

	public String getBoardEquipmentId() {
		if (be != null) {
			return be.getId();
		} else {
			return "";
		}
	}


	
	public List<Detachment> getDetachmentHistory() {
		if (be != null) {
			return be.getDetachmentHistory();
		} else {
			return new ArrayList<Detachment>();
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

	
	public Detachment getMostRecentDetachment() {
		if (be != null) {
			return be.getMostRecentDetachment();
		} else {
			return null;
		}
	}

	
	public Detachment getMostRecentDetachment(String status) {
		if (be != null) {
			return be.getMostRecentDetachment(status);
		} else {
			return null;
		}
	}

	
	public UpDown getMostRecentUpDown() {
		if (be != null) {
			return be.getMostRecentUpDown();
		} else {
			return null;
		}
	}

	public String getName(){
		return be.getName();
	}
	
	public List<UpDown> getUpDownHistory() {
		if (be != null) {
			return be.getUpDownHistory();
		} else {
			return new ArrayList<UpDown>();
		}
	}

	@Override
	public int hashCode() {
		return be.hashCode();
	}

	public boolean isAssigned() {
		return be.isAssigned();
	}

	@Override
	public String toString() {
		return "OpsBoardEquipment [equipment=" + be + "]";
	}
}

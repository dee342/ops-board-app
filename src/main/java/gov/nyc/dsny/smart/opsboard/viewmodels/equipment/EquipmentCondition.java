package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import java.util.Date;

import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

public class EquipmentCondition extends ViewModel<gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition> implements Equipmentable{

	/**
	 * @author spitla
	 */
	private static final long serialVersionUID = 5337223742258603655L;
	private String equipmentId;
	private String downCode;
	private String repairLocation;
	protected Date dateTime;
	private String mechanic;
	private String reporter;
	private String comments;

	public EquipmentCondition() {
		super(gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition.class); 
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	public String getDownCode() {
		return downCode;
	}

	public void setDownCode(String downCode) {
		this.downCode = downCode;
	}

	public String getRepairLocation() {
		return repairLocation;
	}

	public void setRepairLocation(String repairLocation) {
		this.repairLocation = repairLocation;
	}

	public String getMechanic() {
		return mechanic;
	}

	public void setMechanic(String mechanic) {
		this.mechanic = mechanic;
	}

	public String getReporter() {
		return reporter;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}


}

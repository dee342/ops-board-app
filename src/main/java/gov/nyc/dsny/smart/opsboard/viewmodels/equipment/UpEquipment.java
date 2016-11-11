package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

import java.util.Date;

public class UpEquipment extends ViewModel<UpDown> implements Equipmentable{

	/**
	 * @author spitla
	 */
	private static final long serialVersionUID = 5337223742258603655L;
	private Date datetime;
	private String mechanic;
	private String reporter;
	private String equipmentId;

	public UpEquipment() {
		super(UpDown.class); 
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		this.datetime = datetime;
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

	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}


}

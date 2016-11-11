package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import java.util.Date;

import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

public class Attach extends ViewModel<Detachment> implements Equipmentable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337223742258603655L;
	private String receivedBy; 
	private Date receivedDatetime;
	private String remarks;
	private String equipmentId;

	public Attach() {
		super(Detachment.class); 
	}

	public String getReceivedBy() {
		return receivedBy;
	}

	public void setReceivedBy(String receivedBy) {
		this.receivedBy = receivedBy;
	}

	public Date getReceivedDatetime() {
		return receivedDatetime;
	}

	public void setReceivedDatetime(Date receivedDatetime) {
		this.receivedDatetime = receivedDatetime;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

}

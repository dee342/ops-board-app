package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestParam;

import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

public class Detach extends ViewModel<Detachment> implements Equipmentable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5337223742258603655L;
	private String driver; 
	private Date datetime;
	private String from;
	private String to;
	private String equipmentId;
	

	public Detach() {
		super(Detachment.class); 
	}


	public String getDriver() {
		return driver;
	}


	public void setDriver(String driver) {
		this.driver = driver;
	}


	public Date getDatetime() {
		return datetime;
	}


	public void setDatetime(Date datetime) {
		this.datetime = datetime;
	}


	public String getFrom() {
		return from;
	}


	public void setFrom(String from) {
		this.from = from;
	}


	public String getTo() {
		return to;
	}


	public void setTo(String to) {
		this.to = to;
	}


	public String getEquipmentId() {
		return equipmentId;
	}


	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	

}

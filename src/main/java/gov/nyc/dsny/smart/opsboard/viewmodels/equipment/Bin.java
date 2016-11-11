package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

import java.util.Date;

public class Bin extends ViewModel<gov.nyc.dsny.smart.opsboard.domain.equipment.Bin> implements Equipmentable{

	private static final long serialVersionUID = 1L;

	private Date lastModifiedActual;

	private String materialType;

	private String name;

	private String status;

	private String systemUser;
	
	private String equipmentId;

	public Bin() {
		super(gov.nyc.dsny.smart.opsboard.domain.equipment.Bin.class);
	}

	public Date getLastModifiedActual() {
		return lastModifiedActual;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public String getSystemUser() {
		return systemUser;
	}

	public void setLastModifiedActual(Date lastModifiedActual) {
		this.lastModifiedActual = lastModifiedActual;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setSystemUser(String systemUser) {
		this.systemUser = systemUser;
	}

	public String getMaterialType() {
		return materialType;
	}

	public void setMaterialType(String materialType) {
		this.materialType = materialType;
	}

	@Override
	public String getEquipmentId() {
		return equipmentId;
	}

	@Override
	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

}
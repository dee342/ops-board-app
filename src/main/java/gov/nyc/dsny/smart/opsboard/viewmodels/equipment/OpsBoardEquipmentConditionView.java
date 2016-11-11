package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentConditionView;

import java.io.Serializable;
import java.util.Date;

public class OpsBoardEquipmentConditionView implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private EquipmentConditionView equipmentConditionView;
	
	public OpsBoardEquipmentConditionView(EquipmentConditionView equipmentConditionView){
		this.equipmentConditionView = equipmentConditionView;
	}
	
	public String getComments(){
		return equipmentConditionView.getComments();
	}
	
	public boolean isDown(){
		return equipmentConditionView.isDown();
	}
	
	public String getDownCode(){
		return equipmentConditionView.getDownCode();
	}
	
	public Long getId(){
		return equipmentConditionView.getId();
	}
	
	public String getMechanic(){
		return equipmentConditionView.getMechanic();
	}
	
	public String getRepairLocation(){
		return equipmentConditionView.getRepairLocation();
	}
	
	public Long getReplaces(){
		return equipmentConditionView.getReplaces();
	}
	
	public Long getUpdownId(){
		return equipmentConditionView.getUpdownId();
	}
	
	public String getEquipmentId(){
		return equipmentConditionView.getEquipId();
	}
	
	public Date getLastModifiedSystem(){
		return equipmentConditionView.getLastModifiedSystem();
	}
	
	public Date getLastModifiedActual(){
		return equipmentConditionView.getLastModifiedActual();
	}	
	
	public String getSystemUser(){
		return equipmentConditionView.getSystemUser();
	}	
	
	public String getActualUser(){
		return equipmentConditionView.getActualUser();
	}
	
}

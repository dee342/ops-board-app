package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;


import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

import java.util.HashSet;
import java.util.Set;

public class DownEquipment extends ViewModel<UpDown> implements Equipmentable{

	/**
	 * @author spitla
	 */
	private static final long serialVersionUID = 5337223742258603655L;
	private Set<EquipmentCondition> conditions = new HashSet<EquipmentCondition>();	
	private String equipmentId;

	public DownEquipment() {
		super(UpDown.class); 
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	public Set<EquipmentCondition> getConditions() {
		return conditions;
	}

	public void setConditions(Set<EquipmentCondition> conditions) {
		this.conditions = conditions;
	}

}

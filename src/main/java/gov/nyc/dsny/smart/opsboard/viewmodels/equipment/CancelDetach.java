package gov.nyc.dsny.smart.opsboard.viewmodels.equipment;

import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.viewmodels.ViewModel;

public class CancelDetach  extends ViewModel<Detachment> implements Equipmentable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9215553272925070639L;
	
	private String equipmentId;
	
	public CancelDetach() {
		super(Detachment.class); 
	}
	
	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}
}

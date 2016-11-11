package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardEquipment;

public class EquipmentAssignment extends TaskAssignment {

	private static final long serialVersionUID = 1L;

	private OpsBoardEquipment equipment;
	private long id;
	private String remarks;

	public EquipmentAssignment(String taskId, gov.nyc.dsny.smart.opsboard.domain.tasks.EquipmentAssignment ea,
			Location l) {
		super(taskId, ea.getStartTime(), ea.getEndTime(), ea.getAssignmentTime(), ea.isCompleted());
		equipment = ea.getEquipment() == null ? null : new OpsBoardEquipment(ea.getEquipment(), l);
		id = ea.getId() == null ? 0 : ea.getId();
		remarks = ea.getRemarks();
	}

	@JsonIgnore
	public OpsBoardEquipment getEquipment() {
		return equipment;
	}
	
	
	public String getEquipmentId(){
		return (equipment != null) ? equipment.getId() : null;
	}

	public long getId() {
		return id;
	}

	public String getRemarks() {
		return remarks;
	}
}

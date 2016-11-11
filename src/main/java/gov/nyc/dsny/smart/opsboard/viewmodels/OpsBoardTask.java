package gov.nyc.dsny.smart.opsboard.viewmodels;

import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;

import java.io.Serializable;
import java.util.Date;

public class OpsBoardTask implements Serializable {

	private static final long serialVersionUID = 1L;

	private String[] personnelRef;
	private Task task;

	public OpsBoardTask(Task task) {
		this.task = task;
	}

	public String getComments() {
		return task.getComments();
	}

	public Date getEndDate() {
		return task.getEndDate();
	}

	public String getEquipmentRef() {
		if (task.getAssignedEquipment() != null)
			return task.getAssignedEquipment().getEquipment().getEquipment().getId();
		
		return null;
	}

	public String getId() {
		return task.getId();
	}

	public Date getLastModified() {
		return task.getLastModified();
	}

	public String getLastModifiedBy() {
		return task.getLastModifiedBy();
	}

	public String[] getPersonnelRef() {
		return personnelRef;
	}

	public int getSequence() {
		return task.getSequence();
	}

	public Date getStartDate() {
		return task.getStartDate();
	}

	public String getTaskName() {
		return task.getTaskName();
	}

}

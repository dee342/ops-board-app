package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import java.io.Serializable;

public class SupervisorAssignment implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String personId;
	
	private String taskIndicator;

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getTaskIndicator() {
		return taskIndicator;
	}

	public void setTaskIndicator(String taskIndicator) {
		this.taskIndicator = taskIndicator;
	}
	

}

package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardPerson;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TaskSupervisorAssignment implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private Integer sequenceNumber;

	private String taskIndicator;

	private OpsBoardPerson person;
	
	public TaskSupervisorAssignment(gov.nyc.dsny.smart.opsboard.domain.tasks.TaskSupervisorAssignment taskSupervisorAssignment, Location loc){
		this.id = taskSupervisorAssignment.getId();
		this.sequenceNumber = taskSupervisorAssignment.getSequenceNum();
		this.taskIndicator = taskSupervisorAssignment.getTaskIndicator();
		this.person = new OpsBoardPerson(taskSupervisorAssignment.getPerson(), loc);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public String getTaskIndicator() {
		return taskIndicator;
	}

	public void setTaskIndicator(String taskIndicator) {
		this.taskIndicator = taskIndicator;
	}

	@JsonIgnore
	public OpsBoardPerson getPerson() {
		return person;
	}
	
	public String getPersonId(){
		return person != null ? person.getId() : null;
	}
	
	public String getPersonFullName(){
		return person != null ? person.getFullName() : null;
	}

	public void setPerson(OpsBoardPerson person) {
		this.person = person;
	}
	

	public boolean semanticEquals(Object obj){
		if(this.equals(obj)) {
			return true;
		}
		TaskSupervisorAssignment other = (TaskSupervisorAssignment) obj;
		if(taskIndicator == null){
			if(other.getTaskIndicator() != null){
				return false;
			}
		} else if(person == null){
			if(other.getPerson() != null){
				return false;
			}
		} else if( person != null && person.getId() == null){
			if(other.getPerson().getId() != null){
				return false;
			}			
		} 
		String thisPersonId = null;
		if(person != null && person.getId() != null){
			thisPersonId = person.getId();
		}
		String otherPersonId = null;
		if(other.getPerson() != null && other.getPerson().getId() != null){
			otherPersonId = other.getPerson().getId();
		}

		if(thisPersonId == null){
			if(otherPersonId != null){
				return false;
			}
		}
		
		if(thisPersonId != null && !thisPersonId.equalsIgnoreCase(otherPersonId)){
			return false;
		}
		if( taskIndicator != null && !taskIndicator.equalsIgnoreCase(other.getTaskIndicator())){
			return false;
		}
		return true;
	}

}	



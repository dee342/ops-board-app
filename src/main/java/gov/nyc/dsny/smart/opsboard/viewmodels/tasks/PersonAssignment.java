package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardPerson;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class PersonAssignment extends TaskAssignment {

	private static final long serialVersionUID = 1L;

	private OpsBoardPerson person;
	private long id;
	private String remarks;
	private String type;
	private String typeRemarks;

	public PersonAssignment(String taskId, gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment pa, Location l, int position) {
		super(taskId, pa.getStartTime(), pa.getEndTime(), pa.getAssignmentTime(), pa.isCompleted(), position);
		person = pa.getPerson() == null ? null : new OpsBoardPerson(pa.getPerson(), l);
		id = pa.getId() == null ? 0 : pa.getId();
		remarks = pa.getRemarks();
		type = pa.getType();
		typeRemarks = pa.getTypeRemarks();
	}

	public long getId() {
		return id;
	}

	@JsonIgnore
	public OpsBoardPerson getPerson() {
		return person;
	}
	
	public String getPersonId(){
		return person != null ? person.getId() : null;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getType() {
		return type;
	}

	public String getTypeRemarks() {
		return typeRemarks;
	}
}

package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Section;


public class SectionTask implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private Section section;

	private String sectionName;

	private CopyOnWriteArrayList<SupervisorAssignment> sectionSupervisorAssignments = new CopyOnWriteArrayList<SupervisorAssignment>();

	private ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<String, Task>();

	public SectionTask(gov.nyc.dsny.smart.opsboard.domain.tasks.SectionTask domain, Location boardLocation) {
		super();
		id = domain.getId();
		sectionName = domain.getSectionName();
		section = domain.getSection();
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.Task t : domain.getTasks()) {
			tasks.put(t.getId(), new Task(t, boardLocation));
		}
		sectionSupervisorAssignments.addAll(toSectionSupervisorAssignments(domain.getSectionSupervisorAssignments()));
	}

	public String getId() {
		return id;
	}

	@JsonIgnore
	public Section getSection() {
		return section;
	}

	public String getSectionId() {
		return section.getId();
	}

	public String getSectionName() {
		return sectionName;
	}

	public CopyOnWriteArrayList<SupervisorAssignment> getSectionSupervisorAssignments() {
		return sectionSupervisorAssignments;
	}

	public ConcurrentHashMap<String, Task> getTasks() {
		return tasks;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public void setSectionSupervisorAssignment(List<SupervisorAssignment> sectionSupervisorAssignments) {
		this.sectionSupervisorAssignments = new CopyOnWriteArrayList<>(sectionSupervisorAssignments);
	}

	private List<SupervisorAssignment> toSectionSupervisorAssignments(
			Set<gov.nyc.dsny.smart.opsboard.domain.tasks.TaskSupervisorAssignment> supervisorAssignments) {
		List<SupervisorAssignment> sectionSupervisorAsssignments = new ArrayList<SupervisorAssignment>();
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.TaskSupervisorAssignment supervisorAssignment : supervisorAssignments) {
			SupervisorAssignment sectionSupervisorAssignment = new SupervisorAssignment();
			sectionSupervisorAssignment.setPersonId(BoardPerson.EXTRACT_PERSON_ID(supervisorAssignment.getPerson().getId()));
			sectionSupervisorAssignment.setTaskIndicator(supervisorAssignment.getTaskIndicator());
			sectionSupervisorAsssignments.add(sectionSupervisorAssignment);
		}
		return sectionSupervisorAsssignments;
	}
}

package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;


public class SubcategoryTask implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private Map<String, SectionTask> sections = new HashMap<String, SectionTask>();

	private Subcategory subcategory;

	private CopyOnWriteArrayList<SupervisorAssignment> supervisorAssignments = new CopyOnWriteArrayList<SupervisorAssignment>();

	private Map<String, Task> tasks = new HashMap<String, Task>();

	public SubcategoryTask(gov.nyc.dsny.smart.opsboard.domain.tasks.SubcategoryTask domain, Location boardLocation) {
		super();
		id = domain.getId();
		subcategory = domain.getSubcategory();
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.SectionTask st : domain.getSections()) {
			if (st.getId() != null) {
				sections.put(st.getId(), new SectionTask(st, boardLocation));
			}
		}
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.Task t : domain.getTasks()) {
			if (t.getId() != null) {
				tasks.put(t.getId(), new Task(t, boardLocation));
			}
		}
		supervisorAssignments.addAll(toSectionSupervisorAssignments(domain.getSupervisorAssignments()));
	}

	public String getId() {
		return id;
	}

	public Map<String, SectionTask> getSections() {
		return sections;
	}

	@JsonIgnore
	public Subcategory getSubcategory() {
		return subcategory;
	}

	public long getSubcategoryId() {
		return subcategory.getId();
	}

	public CopyOnWriteArrayList<SupervisorAssignment> getSupervisorAssignments() {
		return supervisorAssignments;
	}

	public Map<String, Task> getTasks() {
		return tasks;
	}

	public void setSupervisorAssignments(List<SupervisorAssignment> supervisorAssignments) {
		this.supervisorAssignments = new CopyOnWriteArrayList<>(supervisorAssignments);
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

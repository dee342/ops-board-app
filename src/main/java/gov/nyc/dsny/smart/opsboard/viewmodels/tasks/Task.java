package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class Task implements Serializable {

	private static final long serialVersionUID = 1L;

	private EquipmentAssignment assignedEquipment;
	private PersonAssignment assignedPerson1;
	private PersonAssignment assignedPerson2;
	private List<TaskSupervisorAssignment> supervisorAssignments = new ArrayList<TaskSupervisorAssignment>();
	private String comments;
	private Date endDate;
	private String id;
	private Date lastModified;
	private String lastModifiedBy;
	private Date startDate;
	private String taskName;
	private String linkedTaskChildId;
	private String linkedTaskParentId;
	private int sequence;
	private int partialTaskSequence;
	private int hours;
	private int groupId;

	public Task(gov.nyc.dsny.smart.opsboard.domain.tasks.Task t, Location boardLocation) {
		super();
		assignedEquipment = new EquipmentAssignment(t.getId(), t.getAssignedEquipment(), boardLocation);
		assignedPerson1 = new PersonAssignment(t.getId(), t.getAssignedPerson1(), boardLocation, 1);
		assignedPerson2 = new PersonAssignment(t.getId(), t.getAssignedPerson2(), boardLocation, 2);
		convertDomainTaskSupervisorAssignments(t.getTaskSupervisorAssignments(), boardLocation);
		comments = t.getComments();
		endDate = t.getEndDate();
		id = t.getId();
		lastModified = t.getLastModified();
		lastModifiedBy = t.getLastModifiedBy();
		startDate = t.getStartDate();
		taskName = t.getTaskName();
		linkedTaskChildId = t.getLinkedTaskChildId();
		linkedTaskParentId = t.getLinkedTaskParentId();
		sequence = t.getSequence();
		hours = t.getHours();
		partialTaskSequence = t.getPartialTaskSequence();
		groupId = t.getGroupId();
	}

	private void convertDomainTaskSupervisorAssignments(
			Set<gov.nyc.dsny.smart.opsboard.domain.tasks.TaskSupervisorAssignment> taskDomainSupervisorAssignments,
			Location boardLocation) {
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.TaskSupervisorAssignment taskDomainSupervisorAssignment : taskDomainSupervisorAssignments) {
			TaskSupervisorAssignment supervisorAssignment = new TaskSupervisorAssignment(
					taskDomainSupervisorAssignment, boardLocation);
			getTaskSupervisorAssignments().add(supervisorAssignment);

		}
	}

	public EquipmentAssignment getAssignedEquipment() {
		return assignedEquipment;
	}

	public PersonAssignment getAssignedPerson1() {
		return assignedPerson1;
	}

	public PersonAssignment getAssignedPerson2() {
		return assignedPerson2;
	}

	public String getComments() {
		return comments;
	}

	public Date getEndDate() {
		return endDate;
	}

	public int getGroupId() {
		return groupId;
	}

	public int getHours() {
		return hours;
	}

	public String getId() {
		return id;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public String getLinkedTaskChildId() {
		return linkedTaskChildId;
	}

	public String getLinkedTaskParentId() {
		return linkedTaskParentId;
	}

	public int getPartialTaskSequence() {
		return partialTaskSequence;
	}

	public int getSequence() {
		return sequence;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getTaskName() {
		return taskName;
	}

	public List<TaskSupervisorAssignment> getTaskSupervisorAssignments() {
		return supervisorAssignments;
	}
	
	public void setGroupId(int groupId){
		this.groupId = groupId;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public void setLinkedTaskChildId(String linkedTaskChildId) {
		this.linkedTaskChildId = linkedTaskChildId;
	}

	public void setLinkedTaskParentId(String linkedTaskParentId) {
		this.linkedTaskParentId = linkedTaskParentId;
	}

	public void setPartialTaskSequence(int partialTaskSequence) {
		this.partialTaskSequence = partialTaskSequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void setTaskSupervisorAssignments(List<TaskSupervisorAssignment> supervisorAssignments) {
		this.supervisorAssignments = supervisorAssignments;
	}

}
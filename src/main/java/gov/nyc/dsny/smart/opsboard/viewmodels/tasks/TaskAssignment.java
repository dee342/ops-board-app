package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import java.io.Serializable;
import java.util.Date;


public class TaskAssignment implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date assignmentTime;
	private boolean completed;
	private Date endTime;
	private int position;
	private Date startTime;
	private String supervisorId;
	private String taskId;

	public TaskAssignment(String taskId) {
		this.taskId = taskId;
	}

	public TaskAssignment(String taskId, Date startTime, Date endTime, Date assignmentTime, boolean completed) {
		this(taskId, startTime, endTime, assignmentTime, completed, 1);
	}

	public TaskAssignment(String taskId, Date startTime, Date endTime, Date assignmentTime, boolean completed,
			int position) {
		super();
		this.taskId = taskId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.assignmentTime = assignmentTime;
		this.completed = completed;
		this.position = position;
	}

	public TaskAssignment(String taskId, String supervisorId) {
		this.taskId = taskId;
		this.supervisorId = supervisorId;
	}

	public Date getAssignmentTime() {
		return assignmentTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public int getPosition() {
		return position;
	}

	public Date getStartTime() {
		return startTime;
	}

	public String getSupervisorId() {
		return supervisorId;
	}

	public String getTaskId() {
		return taskId;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setSupervisorId(String supervisorId) {
		this.supervisorId = supervisorId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TaskAssignment [assignmentTime=");
		builder.append(assignmentTime);
		builder.append(", completed=");
		builder.append(completed);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", position=");
		builder.append(position);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", supervisorId=");
		builder.append(supervisorId);
		builder.append(", taskId=");
		builder.append(taskId);
		builder.append("]");
		return builder.toString();
	}
}

package gov.nyc.dsny.smart.opsboard.commands.task;

import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the base class for all task board commands. The class extends base AbstractBoardCommand with task fields.
 */
public abstract class AbstractTaskCommand extends AbstractBoardCommand {

	private static final long serialVersionUID = 1L;

	private String taskId;

	public AbstractTaskCommand() {
	}

	public AbstractTaskCommand(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		taskId = (String) map.get("taskId");
	}

	public AbstractTaskCommand(String boardId, String systemUser, Date systemDateTime, String taskId) {
		super(boardId, systemUser, systemDateTime);
		this.taskId = taskId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("taskId", taskId);
		builder.append(super.toString());

		return builder.toString();
	}
}

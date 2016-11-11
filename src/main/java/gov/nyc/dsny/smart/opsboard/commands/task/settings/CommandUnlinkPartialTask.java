package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PartialTask;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to Edit/un-link a partialtask of a multi route.
 */
@IBoardCommandAnnotation(commandName = "UnlinkPartialTask")
public class CommandUnlinkPartialTask extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;

	private String locationShiftId;
	private List<PartialTask> partialTasks = new ArrayList<PartialTask>();

	private String taskId;

	public CommandUnlinkPartialTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		taskId = (String) map.get("taskId");
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		LocationShift ls = board.unlinkPartialTask(taskId, getLocation(), partialTasks);
		setShift(ls.getShift());
		locationShiftId = ls.getId();

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public String getLocationShiftId() {
		return locationShiftId;
	}

	public List<PartialTask> getPartialTasks() {
		return partialTasks;
	}

	public String getTaskId() {
		return taskId;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setPartialTasks(List<PartialTask> partialTasks) {
		this.partialTasks = partialTasks;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("locationShiftId", locationShiftId);
		builder.append("partialTasks", partialTasks);
		builder.append("taskId", taskId);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unlinked partial task in ");
		sb.append("shift [shift:" + getShiftId() + "] ");
		sb.append("for [location:" + getServiceLocationCode() + "].");

		setAuditMessage(sb.toString());
	};
}

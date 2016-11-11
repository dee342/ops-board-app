package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;
import java.util.SortedSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "AddSubcategory")
public class CommandAddSubcategory extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;

	private String locationShiftId;
	private String shiftCategoryId;
	private String subcategoryTaskId;
	private transient Task task;
	private String taskId;

	public CommandAddSubcategory(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		subcategoryTaskId = (String) map.get("subcategoryTaskId");
		shiftCategoryId = (String) map.get("shiftCategoryId");
		locationShiftId = (String) map.get("locationShiftId");
		taskId = (String) map.get("taskId");
	}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {

		// Validate operation
		validate(board);

		// Execute logic
		SortedSet<Task> tasks = board.addSubcategory(taskId, subcategoryTaskId, shiftCategoryId, locationShiftId, getServiceLocation(), getShift(),
				getCategory(), getSubcategory(), getSystemUser());

		if (!getSubcategory().isContainsSections())  //if subcategory doesn't exist create task
			task = tasks.stream().filter(c -> c.getId().equals(taskId)).findFirst().get();
		
		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public String getLocationShiftId() {
		return locationShiftId;
	}

	public String getShiftCategoryId() {
		return shiftCategoryId;
	}

	public String getSubcategoryTaskId() {
		return subcategoryTaskId;
	}

	public Task getTask() {
		return task;
	}

	public String getTaskId() {
		return taskId;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);

	}

	public void setLocationShiftId(String locationShiftId) {
		this.locationShiftId = locationShiftId;
	}

	public void setShiftCategoryId(String shiftCategoryId) {
		this.shiftCategoryId = shiftCategoryId;
	}

	public void setSubcategoryTaskId(String subcategoryTaskId) {
		this.subcategoryTaskId = subcategoryTaskId;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("locationShiftId", locationShiftId);
		builder.append("shiftCategoryId", shiftCategoryId);
		builder.append("subcategoryTaskId", subcategoryTaskId);
		builder.append("taskId", taskId);
		builder.append(super.toString());

		return builder.toString();
	}

	public void validate(Board board) throws IgnoreException, OpsBoardError {
		board.validateSubcategory(taskId, subcategoryTaskId, shiftCategoryId, locationShiftId, getServiceLocation(),
				getShift(), getCategory(), getSubcategory(), getSystemUser());
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added subcategory ");
		sb.append("[subcategory:" + getSubcategoryId() + "] ");
		sb.append("to [location:" + getServiceLocationCode() + "], ");
		sb.append("shift [shift:" + getShiftId() + "], ");
		sb.append("category [category:" + getCategoryId() + "].");

		setAuditMessage(sb.toString());
	};
}

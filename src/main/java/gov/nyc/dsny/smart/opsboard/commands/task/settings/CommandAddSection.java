package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "AddSection")
public class CommandAddSection extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;

	private String locationShiftId;
	private String sectionTaskId;
	private String shiftCategoryId;
	private String subcategoryTaskId;
	private List<String> taskIds = new ArrayList<String>();
	private Map<String, Task> unsortedTasksMap= new HashMap<String, Task>();
	private Map<String, Task> tasksMap = new HashMap<String, Task>();

	@SuppressWarnings("unchecked")
	public CommandAddSection(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		taskIds = (List<String>) map.get("taskIds");
		sectionTaskId = (String) map.get("sectionTaskId");
		subcategoryTaskId = (String) map.get("subcategoryTaskId");
		shiftCategoryId = (String) map.get("shiftCategoryId");
		locationShiftId = (String) map.get("locationShiftId");
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Validate operation
		validate(board);

		// Execute logic
		SortedSet<Task> tasks = board.addSection(taskIds, sectionTaskId, subcategoryTaskId, shiftCategoryId, locationShiftId,
				getServiceLocation(), getShift(), getSection());

		tasks.stream().filter(c -> taskIds.contains(c.getId())).forEach(c -> unsortedTasksMap.put(c.getId(), c));
		
		//sort the Hashmap by sequence
		tasksMap = getSortedTaskMap(unsortedTasksMap);
		
		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public String getLocationShiftId() {
		return locationShiftId;
	}

	public String getSectionTaskId() {
		return sectionTaskId;
	}

	public String getShiftCategoryId() {
		return shiftCategoryId;
	}

	public String getSubcategoryTaskId() {
		return subcategoryTaskId;
	}

	public List<String> getTaskIds() {
		return taskIds;
	}

	public Map<String, Task> getTasksMap() {
		return tasksMap;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setLocationShiftId(String locationShiftId) {
		this.locationShiftId = locationShiftId;
	}

	public void setSectionTaskId(String sectionTaskId) {
		this.sectionTaskId = sectionTaskId;
	}

	public void setShiftCategoryId(String shiftCategoryId) {
		this.shiftCategoryId = shiftCategoryId;
	}

	public void setSubcategoryTaskId(String subcategoryTaskId) {
		this.subcategoryTaskId = subcategoryTaskId;
	}

	public void setTaskIds(List<String> taskIds) {
		this.taskIds = taskIds;
	}

	public void setTasksMap(Map<String, Task> tasksMap) {
		this.tasksMap = tasksMap;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("locationShiftId", locationShiftId);
		builder.append("sectionTaskId", sectionTaskId);
		builder.append("shiftCategoryId", shiftCategoryId);
		builder.append("subcategoryTaskId", subcategoryTaskId);
		builder.append("taskIds", taskIds);
		builder.append(super.toString());
		
		return builder.toString();
	}

	public void validate(Board board) throws IgnoreException, OpsBoardError {
		board.validateSection(taskIds, sectionTaskId, subcategoryTaskId, shiftCategoryId, locationShiftId,
				getServiceLocation(), getShift(), getSection());
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added section ");
		sb.append("[section:" + getSection().getId() + "] ");
		sb.append("to [location:" + getServiceLocationCode() + "], ");
		sb.append("shift [shift:" + getShiftId() + "], ");
		sb.append("category [category:" + getCategoryId() + "], ");
		sb.append("subcategory [subcategory:" + getSubcategoryId() + "].");

		setAuditMessage(sb.toString());
	};
}

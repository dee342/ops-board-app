package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.Task;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "AddTasks")
public class CommandAddTasks extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;
	private String locationShiftId;
	private int numOfTasks;
	private String sectionTaskId;
	private String shiftCategoryId;
	private String subcategoryTaskId;
	private List<String> taskIds = new ArrayList<String>();
	private Map<String, Task> tasksMap = new LinkedHashMap<String, Task>();

	@SuppressWarnings("unchecked")
	public CommandAddTasks(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		taskIds = (List<String>) map.get("taskIds");
		sectionTaskId = (String) map.get("sectionTaskId");
		subcategoryTaskId = (String) map.get("subcategoryTaskId");
		shiftCategoryId = (String) map.get("shiftCategoryId");
		locationShiftId = (String) map.get("locationShiftId");
	}

	@Override
	public void execute(Board board) throws OpsBoardError, IgnoreException {

		// Execute logic
		SortedSet<gov.nyc.dsny.smart.opsboard.domain.tasks.Task> tasks = board.addTask(taskIds, sectionTaskId, subcategoryTaskId, shiftCategoryId, locationShiftId,
				getServiceLocation(), getShift(), getSystemUser());
		
		numOfTasks = tasks.size();
		
		//return list of only tasks we just added
		Map<String, gov.nyc.dsny.smart.opsboard.domain.tasks.Task> unsortedTasksMap = new HashMap<String, gov.nyc.dsny.smart.opsboard.domain.tasks.Task>();
		tasks.stream().filter(c -> taskIds.contains(c.getId())).forEach(c -> unsortedTasksMap.put(c.getId(), c));
		
		//sort the Hashmap by sequence 
		Map<String, gov.nyc.dsny.smart.opsboard.domain.tasks.Task> sortedTaskMap = getSortedTaskMap(unsortedTasksMap);
		
		// convert to view tasks
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.Task t : sortedTaskMap.values()) {
			tasksMap.put(t.getId(), new Task(t, board.getLocation()));
		}

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public String getLocationShiftId() {
		return locationShiftId;
	}

	public int getNumOfTasks() {
		return numOfTasks;
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

	public void setNumOfTasks(int numOfTasks) {
		this.numOfTasks = numOfTasks;
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
		builder.append("numOfTasks", numOfTasks);
		builder.append("sectionTaskId", sectionTaskId);
		builder.append("shiftCategoryId", shiftCategoryId);
		builder.append("subcategoryTaskId", subcategoryTaskId);
		builder.append("taskIds", taskIds);
		builder.append("tasksMap", tasksMap);
		builder.append(super.toString());

		return builder.toString();
	}


	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Set tasks # to " + numOfTasks + " ");
		sb.append("for [location:" + getServiceLocationCode() + "], ");
		sb.append("shift [shift:" + getShiftId() + "], ");
		sb.append("category [category:" + getCategoryId() + "], ");
		if (getSection() != null) {
			sb.append("subcategory [subcategory:" + getSubcategoryId() + "], ");
			sb.append("section [section:" + getSection().getId() + "].");
		} else {
			sb.append("subcategory [subcategory:" + getSubcategoryId() + "].");
		}

		setAuditMessage(sb.toString());
	}

}

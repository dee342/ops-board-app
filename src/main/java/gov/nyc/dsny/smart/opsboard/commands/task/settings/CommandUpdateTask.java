package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "UpdateTask")
public class CommandUpdateTask extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;

	private String locationShiftId;
	private String sectionTaskId;
	private String shiftCategoryId;
	private String subcategoryTaskId;
	private String taskComments;
	private Date taskEndDate;
	private String taskId;
	private String taskName;
	private Date taskStartDate;

	public CommandUpdateTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		
		if(map.get("shiftCategoryId") != null)
			shiftCategoryId = (String) map.get("shiftCategoryId");
		
		if(map.get("locationShiftId") != null)
			locationShiftId = (String) map.get("locationShiftId");
		
		if(map.get("subcategoryTaskId") != null)
			subcategoryTaskId = (String) map.get("subcategoryTaskId");
		
		if(map.get("sectionTaskId") != null)
			sectionTaskId = (String) map.get("sectionTaskId");
		
		if(map.get("taskId") != null)
			taskId = (String) map.get("taskId");
		
		if(map.get("taskName") != null)
			taskName = (String) map.get("taskName");
		
		if(map.get("taskComments") != null)
			taskComments = (String) map.get("taskComments");
		
		if (map.get("taskStartDate") != null) {
			taskStartDate = new Date(((Long) map.get("taskStartDate")).longValue());
		}
		if (map.get("taskEndDate") != null) {
			taskEndDate = new Date(((Long) map.get("taskEndDate")).longValue());
		}
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		board.updateTask(taskId, taskName, taskComments, taskStartDate, taskEndDate, getServiceLocation(),
				getSystemUser(), getSystemDateTime());

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

	public String getTaskComments() {
		return taskComments;
	}

	public Date getTaskEndDate() {
		return taskEndDate;
	}

	public String getTaskId() {
		return taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public Date getTaskStartDate() {
		return taskStartDate;
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

	public void setTaskComments(String taskComments) {
		this.taskComments = taskComments;
	}

	public void setTaskEndDate(Date taskEndDate) {
		this.taskEndDate = taskEndDate;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public void setTaskStartDate(Date taskStartDate) {
		this.taskStartDate = taskStartDate;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("locationShiftId", locationShiftId);
		builder.append("sectionTaskId", sectionTaskId);
		builder.append("shiftCategoryId", shiftCategoryId);
		builder.append("subcategoryTaskId", subcategoryTaskId);
		builder.append("taskComments", taskComments);
		builder.append("taskEndDate", taskEndDate);
		builder.append("taskId", taskId);
		builder.append("taskName", taskName);
		builder.append("taskStartDate", taskStartDate);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Updated details for task " + taskName + " ");
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
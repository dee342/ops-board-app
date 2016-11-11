package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import gov.nyc.dsny.smart.opsboard.domain.tasks.PartialTask;

import java.io.Serializable;
import java.util.List;

/*
 * Object that holds the add partial tasks request
 */
public class AddPartialTaskRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	private String boardId;

	private String locationShiftId;

	private List<PartialTask> partialTasks;

	private String serviceLocationId;

	private Long shiftId;

	public AddPartialTaskRequest() {
		super();
	}

	public String getBoardId() {
		return boardId;
	}

	public String getLocationShiftId() {
		return locationShiftId;
	}

	public List<PartialTask> getPartialTasks() {
		return partialTasks;
	}

	public String getServiceLocationId() {
		return serviceLocationId;
	}

	public Long getShiftId() {
		return shiftId;
	}

	public void setBoardId(String boardId) {
		this.boardId = boardId;
	}

	public void setLocationShiftId(String locationShiftId) {
		this.locationShiftId = locationShiftId;
	}

	public void setPartialTasks(List<PartialTask> partialTasks) {
		this.partialTasks = partialTasks;
	}

	public void setServiceLocationId(String locationId) {
		serviceLocationId = locationId;
	}

	public void setShiftId(Long shiftId) {
		this.shiftId = shiftId;
	}
}
package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PartialTask;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "SetPartialTask")
public class CommandSetPartialTask extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;

	private String locationShiftId;
	private List<PartialTask> partialTasks;

	@SuppressWarnings("unchecked")
	public CommandSetPartialTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		locationShiftId = (String) map.get("locationShiftId");
		partialTasks = (List<PartialTask>) map.get("partialTasks");
	}

	public CommandSetPartialTask(String boardId, String systemUser, Date systemDateTime, Location serviceLocation,
			Shift shift, String locationShiftId, List<PartialTask> partialTasks) {
		super(boardId, systemUser, systemDateTime, serviceLocation);
		setShift(shift);
		this.locationShiftId = locationShiftId;
		this.partialTasks = partialTasks;
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		board.setPartialTask(partialTasks, locationShiftId);

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

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setLocationShiftId(String locationShiftId) {
		this.locationShiftId = locationShiftId;
	}

	public void setPartialTasks(List<PartialTask> partialTasks) {
		this.partialTasks = partialTasks;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("locationShiftId", locationShiftId);
		builder.append("partialTasks", partialTasks);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added partial task in ");
		sb.append("shift [shift:" + getShiftId() + "] ");
		sb.append("for [location:" + getServiceLocationCode() + "].");

		setAuditMessage(sb.toString());
	};
}

package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "AddShift")
public class CommandAddShift extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;

	private String locationShiftId;

	public CommandAddShift(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		locationShiftId = (String) map.get("locationShiftId");
	}

	@Override
	public void execute(Board board) throws IgnoreException, OpsBoardError {

		// Validate operation
		validate(board);

		// Execute logic
		board.addShift(locationShiftId, getServiceLocation(), getShift());

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public String getLocationShiftId() {
		return locationShiftId;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setLocationShiftId(String locationShiftId) {
		this.locationShiftId = locationShiftId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("locationShiftId", locationShiftId);
		builder.append(super.toString());

		return builder.toString();
	}

	public void validate(Board board) throws IgnoreException, OpsBoardError {
		board.validateShift(locationShiftId, getServiceLocation(), getShift());
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added shift ");
		sb.append("[shift:" + getShiftId() + "] ");
		sb.append("to [location:" + getServiceLocationCode() + "].");

		setAuditMessage(sb.toString());
	}
}

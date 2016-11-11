package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

@IBoardCommandAnnotation(commandName = "AddCategory")
public class CommandAddCategory extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;

	private String locationShiftId;
	private String shiftCategoryId;

	public CommandAddCategory(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		shiftCategoryId = (String) map.get("shiftCategoryId");
		locationShiftId = (String) map.get("locationShiftId");
	}

	@Override
	public void execute(Board board) throws OpsBoardError {
		
		// Validate operation
		validate(board);

		// Execute logic
		board.addCategory(shiftCategoryId, locationShiftId, getServiceLocation(), getShift(), getCategory());

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

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);

	}
	
	public void validate(Board board) throws IgnoreException, OpsBoardError {
		board.validateCategory(shiftCategoryId,locationShiftId, getServiceLocation(), getShift(),getCategory());
	}

	public void setLocationShiftId(String locationShiftId) {
		this.locationShiftId = locationShiftId;
	}

	public void setShiftCategoryId(String shiftCategoryId) {
		this.shiftCategoryId = shiftCategoryId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("locationShiftId", locationShiftId);
		builder.append("shiftCategoryId", shiftCategoryId);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added category ");
		sb.append("[category:" + getCategoryId() + "] ");
		sb.append("to [location:" + getServiceLocationCode() + "],  ");
		sb.append("shift [shift:" + getShiftId() + "].");

		setAuditMessage(sb.toString());
	};
}

package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IOldCategoryCommand;
import gov.nyc.dsny.smart.opsboard.domain.StateAndAssignment;
import gov.nyc.dsny.smart.opsboard.domain.StatesAndAssignment;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

@IBoardCommandAnnotation(commandName = "UpdateCategory")
public class CommandUpdateCategory extends AbstractTaskSettingsCommand implements IOldCategoryCommand {

	private static final long serialVersionUID = 1L;

	private List<String> equipmentIds;
	private String locationShiftId; // UUID for Shift
	private transient Category oldCategory;

	private List<String> personnelIds;
	private String shiftCategoryId; // UUID for Category

	private Map<String, StatesAndAssignment> equipmentAssignedStates;
	private Map<String, StateAndAssignment> personnelAssignedStates;

	public CommandUpdateCategory(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		shiftCategoryId = (String) map.get("shiftCategoryId");
		locationShiftId = (String) map.get("locationShiftId");
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Updated category ");
		sb.append("[category:" + getOldCategoryId() + "] ");
		sb.append("to [category:" + getCategoryId() + "], ");
		sb.append("for [location:" + getServiceLocationCode() + "], ");
		sb.append("shift [shift:" + getShiftId() + "].");

		setAuditMessage(sb.toString());
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic and update data transfer attributes
		Map<String, List<String>> idMap = board.updateCategory(shiftCategoryId, locationShiftId, getServiceLocation(),
				getShift(), getCategory(), oldCategory);
		equipmentIds = idMap.get("E");
		personnelIds = idMap.get("P");
		
		if (CollectionUtils.isNotEmpty(equipmentIds)) {
			equipmentAssignedStates = new HashMap<String, StatesAndAssignment>();
			equipmentIds.forEach(e -> {
				BoardEquipment be = board.getEquipment().get(BoardEquipment.EXTRACT_EQUIPMENT_ID(e));
				if (be != null) {
					equipmentAssignedStates.put(e, wrapEquipmenStatesAndAssignment(board, be));
				}
			});
		}

		if (CollectionUtils.isNotEmpty(personnelIds)) {
			personnelAssignedStates = new HashMap<String, StateAndAssignment>();
			personnelIds.forEach(e -> {
				BoardPerson bp = board.getPersonnel().get(BoardPerson.EXTRACT_PERSON_ID(e));
				if (bp != null) {
					personnelAssignedStates.put(e, new StateAndAssignment(bp.getState(board.getLocation()).getState(), bp.isAssigned(board.getLocation().getCode()), bp.isAssigned()));
				}
			});
		}

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public Map<String, StatesAndAssignment> getEquipmentAssignedStates() {
		return equipmentAssignedStates;
	}

	public List<String> getEquipmentIds() {
		if (equipmentIds == null) {
			equipmentIds = new ArrayList<String>();
		}
		return equipmentIds;
	}

	public String getLocationShiftId() {
		return locationShiftId;
	}

	@Override
	@JsonIgnore
	public Category getOldCategory() {
		return oldCategory;
	}

	public Long getOldCategoryId() {
		return oldCategory == null ? null : oldCategory.getId();
	}

	public Map<String, StateAndAssignment> getPersonnelAssignedStates() {
		return personnelAssignedStates;
	}

	public List<String> getPersonnelIds() {
		if (personnelIds == null) {
			personnelIds = new ArrayList<String>();
		}
		return personnelIds;
	}

	public String getShiftCategoryId() {
		return shiftCategoryId;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setEquipmentAssignedStates(Map<String, StatesAndAssignment> equipmentAssignedStates) {
		this.equipmentAssignedStates = equipmentAssignedStates;
	}

	public void setEquipmentIds(List<String> equipmentIds) {
		this.equipmentIds = equipmentIds;
	}

	public void setLocationShiftId(String locationShiftId) {
		this.locationShiftId = locationShiftId;
	}

	@Override
	public void setOldCategory(Category oldCategory) {
		this.oldCategory = oldCategory;
	}

	public void setPersonnelAssignedStates(Map<String, StateAndAssignment> personnelAssignedStates) {
		this.personnelAssignedStates = personnelAssignedStates;
	}

	public void setPersonnelIds(List<String> personnelIds) {
		this.personnelIds = personnelIds;
	}

	public void setShiftCategoryId(String shiftCategoryId) {
		this.shiftCategoryId = shiftCategoryId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("equipmentIds", equipmentIds);
		builder.append("locationShiftId", locationShiftId);
		builder.append("oldCategoryId", getOldCategoryId());
		builder.append("personnelIds", personnelIds);
		builder.append("shiftCategoryId", shiftCategoryId);
		builder.append(super.toString());

		return builder.toString();
	};
}

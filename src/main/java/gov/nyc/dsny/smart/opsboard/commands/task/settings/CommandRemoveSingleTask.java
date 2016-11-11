package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.task.assignments.AbstractDayBeforeCommand;
import gov.nyc.dsny.smart.opsboard.commands.task.assignments.CommandRemoveDayBefore;
import gov.nyc.dsny.smart.opsboard.domain.StateAndAssignment;
import gov.nyc.dsny.smart.opsboard.domain.StatesAndAssignment;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.PersonLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@IBoardCommandAnnotation(commandName = "RemoveSingleTask")
public class CommandRemoveSingleTask extends AbstractTaskSettingsCommand {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = LoggerFactory.getLogger(CommandRemoveSingleTask.class);

	private Map<String, StateAndAssignment> assignedPersonMap;
	private List<String> equipmentIds = new ArrayList<String>();
	private String locationShiftId;
	private List<String> personnelIds = new ArrayList<String>();
	private String sectionTaskId;
	private String shiftCategoryId;
	private String subcategoryTaskId;
	private Task task;
	private String taskId;
	private Map<String, StatesAndAssignment> equipmentAssignedStates = new HashMap<String, StatesAndAssignment>();
	private Map<String, StateAndAssignment> personnelAssignedStates = new HashMap<String, StateAndAssignment>();
	
	private Map<String, Set<Shift>> nextDayUnassignedShifts = new HashMap<>();
	
	public CommandRemoveSingleTask(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		shiftCategoryId = (String) map.get("shiftCategoryId");
		locationShiftId = (String) map.get("locationShiftId");
		subcategoryTaskId = (String) map.get("subcategoryTaskId");
		sectionTaskId = (String) map.get("sectionTaskId");
		taskId = (String) map.get("taskId");
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Removed task ");
		sb.append("[task:" + (task == null ? "not found" : task.getTaskName()) + "], ");
		sb.append("from [location:" + getServiceLocationCode() + "], ");
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

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Get Task for later use in audit message
		task = board.findTask(taskId);
		
		Map<String, Set<Shift>> personToTasksMap = new HashMap<>();
		BoardHelper.populateNextDayAssigned(board, personToTasksMap);

		// Execute logic and update data transfer attributes
		Map<String, List<String>> idsMap = board.deleteSingleTask(taskId, sectionTaskId, subcategoryTaskId,
				shiftCategoryId, locationShiftId, getServiceLocation(), getShift(), getCategory(), getSubcategory(),
				getSection());
		equipmentIds = idsMap.get("Equipment");
		personnelIds = idsMap.get("Personnel");

		if (CollectionUtils.isNotEmpty(equipmentIds)) {
			equipmentIds.forEach(e -> {
				BoardEquipment be = board.getEquipment().get(BoardEquipment.EXTRACT_EQUIPMENT_ID(e));
				if (be != null) {
					equipmentAssignedStates.put(e, wrapEquipmenStatesAndAssignment(board, be));
				}
			});
		}

		if (CollectionUtils.isNotEmpty(personnelIds)) {
			personnelIds.forEach(e -> {
				BoardPerson bp = board.getPersonnel().get(BoardPerson.EXTRACT_PERSON_ID(e));
				if (bp != null) {
					PersonLogger.logBoardPerson("execute ", bp, log);
					personnelAssignedStates.put(e, new StateAndAssignment(bp.getState(board.getLocation()).getState(), bp.isAssigned(board.getLocation().getCode()), bp.isAssigned()));
				}
			});
		}
		
		nextDayUnassignedShifts = BoardHelper.compareNextDayAssignments(board, personToTasksMap);
		
		nextDayUnassignedShifts.forEach((personId, shifts)-> {
			board.unassignNextDay(personId, shifts);
			List<AbstractDayBeforeCommand> nextDayCommands =  (List<AbstractDayBeforeCommand>) AbstractDayBeforeCommand.build(getBoardId(), board.getPersonnel().get(personId), true, shifts);
			nextDayCommands.forEach(c -> this.addSubCommands((CommandRemoveDayBefore) c));

		});

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public Map<String, StateAndAssignment> getAssignedPersonMap() {
		if (assignedPersonMap == null) {
			assignedPersonMap = new HashMap<String, StateAndAssignment>();
		}
		return assignedPersonMap;
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

	public Map<String, StateAndAssignment> getPersonnelAssignedStates() {
		return personnelAssignedStates;
	}

	public List<String> getPersonnelIds() {
		if (personnelIds == null) {
			personnelIds = new ArrayList<String>();
		}
		return personnelIds;
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

	public String getTaskId() {
		return taskId;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setAssignedPersonMap(Map<String, StateAndAssignment> assignedPersonMap) {
		this.assignedPersonMap = assignedPersonMap;
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

	public void setPersonnelAssignedStates(Map<String, StateAndAssignment> personnelAssignedStates) {
		this.personnelAssignedStates = personnelAssignedStates;
	}

	public void setPersonnelIds(List<String> personnelIds) {
		this.personnelIds = personnelIds;
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

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	
	public Map<String, Set<Shift>> getNextDayUnassignedShifts() {
		return nextDayUnassignedShifts;
	}

	public void setNextDayUnassignedShifts(
			Map<String, Set<Shift>> nextDayUnassignedShifts) {
		this.nextDayUnassignedShifts = nextDayUnassignedShifts;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("assignedPersonMap", assignedPersonMap);
		builder.append("equipmentIds", equipmentIds);
		builder.append("locationShiftId", locationShiftId);
		builder.append("personnelIds", personnelIds);
		builder.append("sectionTaskId", sectionTaskId);
		builder.append("shiftCategoryId", shiftCategoryId);
		builder.append("subcategoryTaskId", subcategoryTaskId);
		builder.append("taskId", taskId);
		builder.append(super.toString());

		return builder.toString();
	}
}

package gov.nyc.dsny.smart.opsboard.commands.equipment;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.BoardEquipmentAndTasks;
import gov.nyc.dsny.smart.opsboard.domain.tasks.EquipmentAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.tasks.TaskAssignment;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the base class for all multi-board commands for equipment. The class extends base
 * AbstractMutltiBoardCommand with equipment fields.
 */
public abstract class AbstractMultiBoardEquipmentCommand extends AbstractMultiBoardCommand {

	private static final long serialVersionUID = 1L;

	private boolean assigned;
	private String boardEquipmentId;
	private String currentLocation;
	private String equipmentId;
	private Map<String, String> states = new HashMap<String, String>();
	private Set<TaskAssignment> tasks;

	public AbstractMultiBoardEquipmentCommand() {
		super();
	}

	public AbstractMultiBoardEquipmentCommand(LinkedHashMap<String, Object> map) {
		super(map);
		boardEquipmentId = (String) map.get("boardEquipmentId");
		equipmentId = (String) map.get("equipmentId");
	}

	public AbstractMultiBoardEquipmentCommand(String boardId, String systemUser, Date systemDateTime, String equipmentId) {
		super(boardId, systemUser, systemDateTime, false);
		this.equipmentId = equipmentId;
	}

	public AbstractMultiBoardEquipmentCommand(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId) {
		this(boardId, systemUser, systemDateTime, equipmentId);
		this.boardEquipmentId = boardEquipmentId;
	}
	
	public AbstractMultiBoardEquipmentCommand(String boardId, String systemUser, Date systemDateTime, String equipmentId, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, fromIntegration);
		this.equipmentId = equipmentId;
	}

	public AbstractMultiBoardEquipmentCommand(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, boolean fromIntegration) {
		this(boardId, systemUser, systemDateTime, equipmentId, fromIntegration);
		this.boardEquipmentId = boardEquipmentId;
	}

	@Override
	public abstract void execute(Board board) throws OpsBoardError;

	public boolean getAssigned() {
		return assigned;
	}

	public String getBoardEquipmentId() {
		return boardEquipmentId;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public Map<String, String> getStates() {
		return states;
	}

	public Set<TaskAssignment> getTasks() {
		return tasks;
	}

	/**
	 * Match if: board location is equal to command location and operation time is between board start and end or board
	 * start is after operation time
	 */
	@Override
	public boolean matchBoard(Board board, Location location) {
		Location loc = location;
		if (location.getBoardType().getCode().equals("G")) {
			for (Location l : location.getServiceParents()) {
				if (l.isServicesEquipmentLocations()) {
					loc = l;
					break;
				}
			}
		}

		return board.getLocation().equals(loc) && DateUtils.onOrBefore(getSystemDateTime(), board.getShiftsEndDate());
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public void setBoardEquipmentId(String boardEquipmentId) {
		this.boardEquipmentId = boardEquipmentId;
	}

	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	public void setStates(Location loc, BoardEquipment be) {
		states.clear();
		if (loc.isServicesEquipmentLocations()) {
			for (Location el : loc.getServiceLocations()) {
				StateAndLocation sl = be.getState(el);
				states.put(el.getCode(), sl.getState());
			}
		} else {
			StateAndLocation sl = be.getState(loc);
			states.put(loc.getCode(), sl.getState());
		}
	}

	public void setTasks(Set<TaskAssignment> tasks) {
		this.tasks = tasks;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("assigned", assigned);
		builder.append("boardEquipmentId", boardEquipmentId);
		builder.append("equipmentId", equipmentId);
		builder.append("currentLocation", currentLocation);
		builder.append("states", states);
		builder.append("tasks", tasks);
		builder.append(super.toString());

		return builder.toString();
	}

	protected Set<TaskAssignment> convertEquipmentTasksToTaskAssigments(Set<Task> tasks) {
		Set<TaskAssignment> tas = new LinkedHashSet<TaskAssignment>();
		if (tasks != null) {
			for (Task t : tasks) {
				EquipmentAssignment ea = t.getAssignedEquipment();
				tas.add(new TaskAssignment(t.getId(), ea.getStartTime(), ea.getEndTime(), ea.getAssignmentTime(), ea
						.isCompleted()));
			}
		}

		return tas;
	}

	protected void updateEquipmentAndTasks(Board board, BoardEquipmentAndTasks bets) {

		// Set equipment details
		if (bets.getBoardEquipment() != null) {
			StateAndLocation sl = bets.getBoardEquipment().getState(board.getLocation());
			setCurrentLocation(sl.getLocation().getCode());
			setStates(board.getLocation(), bets.getBoardEquipment());
			setAssigned(bets.getBoardEquipment().isAssigned());			
		}

		// Set task details
		if (bets.getTasks() != null) {
			setTasks(convertEquipmentTasksToTaskAssigments(bets.getTasks()));
		}
	}
	
}
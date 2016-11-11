package gov.nyc.dsny.smart.opsboard.commands;

import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.EquipmentAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PartialTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
/**
 * Common Base class to setup Board
 * @author ssandur
 *
 */
public abstract class TestBaseCommands {

	protected static final String I_LOCATION = "MW02G";
	protected static final String I_VEH_SERIES = "21CB";
	protected static final String I_VEH_NUMBER = "201";
	protected static final String BOARD_DATE = "20150203";
	protected static final String BOARD_ID="1017191_20150114";
	protected static final String EQUIP_ID1= "1018191_20150114";
	protected static final String EQUIP_ID2= "1018192_20150114";
	
	protected List<String> taskIds = new ArrayList<String>();	
	protected SortedSet<Task> tasks = null;
	protected List<PartialTask> partialTasks = new ArrayList<PartialTask>();
	protected List<TaskContainer> taskContainers = new ArrayList<TaskContainer>();
	protected Set<LocationShift> locationShifts = null;
	Map<String,LocationShift> tasksToLocationShiftMap = new HashMap<String,LocationShift>();
	protected LocationShift locationShift;
	
	protected List<BoardEquipment> boardEquipmentsList =null;
	protected BoardEquipment boardEquipment1=null;
	protected BoardEquipment boardEquipment2=null;
	
	protected Equipment equipment1;
	protected Equipment equipment2;
	protected Board board;
	protected Location location;
	protected Date now = new Date();
	protected Map<String, Task> tasksMap = null;
	
	
	public void createBoard(){
		board = new Board();
		location = new Location(I_LOCATION);
		location.setBorough(location);
		
		board.setLocation(location);		
		board.setId(BOARD_ID);
		board.setDate(BOARD_DATE);
	}
	
	protected void createLocationShift(LocalTime startTime, LocalTime endTime){
		locationShift = new LocationShift();
		locationShift.setId("1");
		locationShift.setShift(createReferenceDatashift(startTime,endTime));
	}
	
	//set Equipments to Board
	protected void setEquipments(){
		boardEquipmentsList = new ArrayList<BoardEquipment>();
		boardEquipment1 = new BoardEquipment ();
		boardEquipment2 = new BoardEquipment ();
		equipment1 = new Equipment();	
		SnowReadiness snowReadiness1 = new SnowReadiness();		
		snowReadiness1.setWorkingDown(true);
		equipment1.setSnowReadiness(snowReadiness1);
		equipment1.setId(EQUIP_ID1);
		equipment1.setOwner(new Location(I_LOCATION));
		
		equipment2 = new Equipment();	
		SnowReadiness snowReadiness2 = new SnowReadiness();
		snowReadiness2.setWorkingDown(true);
		equipment2.setSnowReadiness(snowReadiness2);
		equipment2.setId(EQUIP_ID2);
		equipment2.setOwner(new Location(I_LOCATION));
		
		boardEquipment1.setSnowReadiness(snowReadiness1);
		boardEquipment1.setEquipment(equipment1);
		boardEquipment2.setEquipment(equipment2);
		boardEquipment2.setSnowReadiness(snowReadiness2);
		boardEquipment1.setId("3");
		boardEquipment2.setId("4");
		boardEquipmentsList.add(boardEquipment1);
		boardEquipmentsList.add(boardEquipment2);
		board.setEquipment(boardEquipmentsList);	
		
	}
	//set Personal to Board
	protected void setPersonnel(){
		
	}	
	
	private Shift createReferenceDatashift(LocalTime startTime, LocalTime endTime) {
		Shift shift = new Shift();		
		shift.setStartTime(startTime);
		shift.setEndDay(1);		
		shift.setEndTime(endTime);
		return shift;
	}
	
	public Task createTask(Task task , int sequence, EquipmentAssignment equimentAssgn, PersonAssignment personAssgn1, PersonAssignment personAssgn2, String taskid){
		
		task.setId(taskid);
		task.setAssignedEquipment(equimentAssgn);
		task.setAssignedPerson1(personAssgn1);
		task.setAssignedPerson2(personAssgn2);
		task.setBoardId(board.getId());
		task.setSequence(sequence);
		// need to take care of start and end time same as shift 
		Calendar endDate = Calendar.getInstance();
		endDate.add(Calendar.HOUR, 4); // make in future so last hour logic will not affect it
		task.setEndDate(endDate.getTime());
		task.setStartDate(new Date());
		return task;
	}
	
	
	
}

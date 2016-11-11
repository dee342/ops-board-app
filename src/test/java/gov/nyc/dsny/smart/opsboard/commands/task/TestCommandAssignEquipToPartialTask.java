package gov.nyc.dsny.smart.opsboard.commands.task;

import gov.nyc.dsny.smart.opsboard.commands.TestBaseCommands;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.EquipmentAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.ShiftCategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.SubcategoryTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCommandAssignEquipToPartialTask extends TestBaseCommands{

	private static final Logger logger = LoggerFactory.getLogger(TestCommandAssignEquipToPartialTask.class);
	@Before
	public void setUp(){	
		// A board has 1 or more TASKCONTAINERS
		createBoard();		
		createLocationShift(LocalTime.of(01,00),LocalTime.of(8,30));
		//setup all reference data (drop down data)
		
			
		// add shiftcatogery to ls   ( shiftcatery has catogery referecne data)
		// add shiftsubcatogery to shiftcatoery ( shiftsubcatogery has  subcatogery refrence data)
		//add task to sftsubcatogery
		
		Set<ShiftCategory> shiftCategories = new HashSet<ShiftCategory>();
		ShiftCategory shiftCategory = new ShiftCategory();
		//creating Ref data
		gov.nyc.dsny.smart.opsboard.domain.reference.Category catogery1= new Category(1L);
		List<Subcategory> subcategories = new ArrayList<Subcategory>();
		Subcategory subcategory = new Subcategory();
		subcategory.setId(2L);
		subcategory.setName("District Superintendent");
				
		catogery1.setId(1L);
		catogery1.setName("Supervison");
		catogery1.setSubcategories(subcategories);
		shiftCategory.setCategory(catogery1); // set the ref data
		shiftCategory.setId("10");
		//subcategoryTasks is nothin but shiftsubcategory
		Set<SubcategoryTask> subcategoryTasks = new HashSet<SubcategoryTask>();
		SubcategoryTask subcategoryTask = new SubcategoryTask();
		subcategoryTask.setId("23");
		//setEquipments to the board equp, that is the board,location comes with some equip and personal
		setEquipments();
	
		
		// this is setting equip to the board
	
		
	//	EquipmentAssignment equimentAssgn1 = createEquipmentAssignment(boardEquipmentsList.get(0), 2L);
		

		//tasksMap.put("1", createTask(new Task(),equimentAssgn, personAssgn1, personAssgn2, "1"));
		//tasksMap.put("2", createTask(new Task(), equimentAssgn, personAssgn1, personAssgn2,"2"));
		
		
		
		
		subcategoryTask.setSubcategory(subcategory);// settign ref data
		//subcategoryTask.setShiftCategory(shiftCategory);// why you need this?
		subcategoryTasks.add(subcategoryTask);
		shiftCategory.setSubcategoryTasks(subcategoryTasks);
		
		shiftCategory.setLocationShift(locationShift);		
		
		shiftCategories.add(shiftCategory);
		
		locationShift.setShiftCategories(shiftCategories);		
		
		//setting 1 taskContainer to a List of taskContainers
		TaskContainer taskContainer = new TaskContainer(board, location);
		
		taskContainer.setBoard(board);
		taskContainer.setId(0L);
		taskContainer.setLocation(location);
		
		locationShift.setContainer(taskContainer);
		//adding 1 locationshift to locationshifts 
		Set<LocationShift> locationShifts = new HashSet<LocationShift>();
		locationShifts.add(locationShift);
			
		taskContainer.setLocationShifts(locationShifts);
		
	//	System.out.println("task container "+taskContainer.getLocationShifts().contains(shiftCategories.contains(subcategoryTasks.contains(subcategoryTask.getTasks().get(0).getId()))));
		
	
		// set couple of tasks to tasksMap and set tasksMap to tc
		
		taskContainers.add(taskContainer);
		board.setTaskContainers(taskContainers);
		
		
		tasks = new ConcurrentSkipListSet<Task>();
		//EquipmentAssignment equimentAssgn = createEquipmentAssignment(boardEquipmentsList.get(0), 1L); // this equip will be assigned to parent t1 and will trickle to t2 if in same catogery
		EquipmentAssignment equimentAssgn1 = createEquipmentAssignment(null, 1L);
		EquipmentAssignment equimentAssgn2 = createEquipmentAssignment(null, 2L);
		Task task1= createTask(new Task(), 1,equimentAssgn1, null, null, "1");
		Task task2= createTask(new Task(),2, equimentAssgn2, null, null,"2");
		tasks.add(task1);
		tasks.add(task2);
		
		subcategoryTask.setTasks(tasks);	
	}

	private EquipmentAssignment createEquipmentAssignment(BoardEquipment equip, Long id) {
		EquipmentAssignment equimentAssgn= new EquipmentAssignment();
		equimentAssgn.setEndTime(now);
		equimentAssgn.assign(equip, now);
		equimentAssgn.setId(id);
		equimentAssgn.setRemarks("test equipment assign");
		equimentAssgn.setStartTime(new Date());		
		return equimentAssgn;
	}
	
	/* TODO - fix
	//check if equip is assigned to the partialtask for eg: it trickles down to 2nd tasks in same catogery and assert
	@Test
	public void testAssignEquipmentToPartialTask() throws Exception {				
		//TODO create task in same catogery
		CommandAssignEquipmentToPartialTask commandAssignEquipmentToPartialTask = 
													//(String boardId, String taskId, String equipmentId, Location location)
				new CommandAssignEquipmentToPartialTask(board.getId(),"1" ,EQUIP_ID1, location);	
		commandAssignEquipmentToPartialTask.setSystemDateTime(now);
		commandAssignEquipmentToPartialTask.execute(board);
		
		
		Assert.assertEquals(EQUIP_ID1, commandAssignEquipmentToPartialTask.getEquipmentId());
		Assert.assertTrue(commandAssignEquipmentToPartialTask.getAssignments().size() >= 1);	
			
	}*/
	
		//check if equip is assigned to the partialtask for eg: it should not trickle down to 2nd tasks in diff catogery and assert
		@Test
		public void testAssignEquipmentToPartialTask1() throws Exception {	
			
		}

}

package gov.nyc.dsny.smart.opsboard.commands.task;

import gov.nyc.dsny.smart.opsboard.commands.TestBaseCommands;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.EquipmentAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

	/**
	 * SMARTOB-5184
	 * Date- Jan 28th, 2015
	 * @author ssandur
	 */
public class TestCommandUnlinkPartialTask extends TestBaseCommands{
	// The board is created with location and date
	// create a task container with 2 tasks add to taskmap and then to the taskcontainer.taskmap plus all the reference data ( huge work)
	// create a partial task start and end, assign Equip, assgn Personal, then send a taskID , 
	//and result List<PartialTask> should have all the unlink data	
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
		//board equp
		setEquipments();
	
		
		// this is setting equip to the board
	
		EquipmentAssignment equimentAssgn = createEquipmentAssignment(boardEquipmentsList.get(0), 1L);
		EquipmentAssignment equimentAssgn1 = createEquipmentAssignment(boardEquipmentsList.get(0), 2L);
		PersonAssignment personAssgn1 = createPersonAssignment(1L);
		PersonAssignment personAssgn2 = createPersonAssignment(2L);
		PersonAssignment personAssgn3 = createPersonAssignment(3L);
		PersonAssignment personAssgn4 = createPersonAssignment(4L);

		//tasksMap.put("1", createTask(new Task(),equimentAssgn, personAssgn1, personAssgn2, "1"));
		//tasksMap.put("2", createTask(new Task(), equimentAssgn, personAssgn1, personAssgn2,"2"));
		
		tasks = new ConcurrentSkipListSet<Task>();
		Task task1= createTask(new Task(),1,equimentAssgn, personAssgn1, personAssgn2, "1");
		Task task2= createTask(new Task(),2, equimentAssgn, personAssgn3, personAssgn4,"2");
		tasks.add(task1);
		tasks.add(task2);
		
		subcategoryTask.setTasks(tasks);
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
	
		// set couple of tasks to tasksMap and set tasksMap to tc
		
		taskContainers.add(taskContainer);
		board.setTaskContainers(taskContainers);
		
		/*PartialTask partialTask = new PartialTask();
		partialTask.setTask(task);
		partialTasks.add(partialTask);*/
					
		
	}

	private PersonAssignment createPersonAssignment(Long id) {
		PersonAssignment personAssgn = new PersonAssignment();
		personAssgn.setId(id);
		personAssgn.setStartTime(now);
		personAssgn.setEndTime(new Date());
		personAssgn.setPerson(new BoardPerson());
		
		return personAssgn;
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
	//Scenario 1: create the task hierarchy Send a taskId which has some child ids (multi routes) and result PartalTask object, 
	//should be populated with the 3 props unassignEquippid, uassignper1, unassgnper2   
	@Test
	public void testCommandUnlinkPartialTask() throws Exception {	

		Map<String, Object>contents = new HashMap<String, Object>();
		contents.put("taskId", "1");
		contents.put("systemDateTime", now);
		
		CommandUnlinkPartialTask commandUnlinkPartialTask = new CommandUnlinkPartialTask(board.getId(), contents); 
		commandUnlinkPartialTask.setLocation(location);
		commandUnlinkPartialTask.setPartialTasks(partialTasks);
		commandUnlinkPartialTask.execute(board);

		Assert.assertEquals(2, partialTasks.size());
	
		} */
	
		//Scenario 1: create the task hierarchy Send a taskId which has no child ids( no multi routes) and result PartalTask object, 
		//should be populated with the 3 props unassignEquippid, uassignper1, unassgnper2   
		@Test
		public void testCommandUnlinkPartialTask1() throws Exception {
			
		}
		
/* TODO - Fix
		//scenario 3: Task not found //Task not found
		@Test
		public void testCommandUnlinkPartialTask2() throws Exception {

			try {

				CommandUnlinkPartialTask commandUnlinkPartialTask = new CommandUnlinkPartialTask(
						board.getId(), "12", location);
				commandUnlinkPartialTask.execute(board);
			} catch (Exception e) {
				Assert.assertEquals(e.getMessage(), "Task not found");

			}

		}
		*/
		//scenario 3:Partial tasks spanning in mutiple subcatogeries
		@Test
		public void testCommandUnlinkPartialTask3() throws Exception {
			
		}
		//scenario 3:Partial tasks spanning in same subcatogeries
		@Test
		public void testCommandUnlinkPartialTask4() throws Exception {
			
		}
		
		//scenario 5:Assgin one Equip assigned to multiple catogeries, the result will be do not unassgn the equip but unlink the tasks
		//and return List<PartialTask>.unassignEquipId should be populated
		@Test
		public void testCommandUnlinkPartialTask5() throws Exception {
			
		}
		
/*	TODO - fix	
 * // we are unlinkin the child task Equip only if in same subcategory, checking unlink of parent and child
		@Test
		public void testCommandUnlinkPartialTask6() throws Exception {
			Map<String, Object>contents = new HashMap<String, Object>();
			contents.put("taskId", "12");

			CommandUnlinkPartialTask commandUnlinkPartialTask = new CommandUnlinkPartialTask(board.getId(), contents);
			commandUnlinkPartialTask.setLocation(location);
			
			///setupdata task 1 = parent , task 2 = child, setup equip A to task1 and setup equip A to task2
			// get the tasks and and check
			
			commandUnlinkPartialTask.setPartialTasks(partialTasks);
			commandUnlinkPartialTask.setTaskId("1");
			commandUnlinkPartialTask.setSystemDateTime(now);
			commandUnlinkPartialTask.execute(board);

			setupDataInSameSubcategory();
			
			for (PartialTask partialTask : partialTasks) {				
				if(partialTask.getTask().getLinkedTaskParentId()==null){
					// then it is a parent, check if the child link is removed 
					Assert.assertEquals(null,partialTask.getTask().getLinkedTaskChildId());				
					// then it is a child, check if the parent link is removed
					Assert.assertEquals(null,partialTask.getTask().getLinkedTaskParentId());
				}
				
				
				
				// ceck the child the unassignid should be populated in same subcategory and Euip shold also be null
				if(partialTask.getTask().getId().equals(2)){
					Assert.assertEquals(3, partialTask.getUnassignEquipId());
					Assert.assertEquals(null, partialTask.getTask().getAssignedEquipment().getEquipment());
				}
			}
		} */
		//we should not be able to set equipA and EuipB to partal task, ie task1 parent and task2 child
		public void testCommandUnlinkPartialTask7() throws Exception {
			
		}
		private void setupDataInSameSubcategory() {
			// TODO Auto-generated method stub
			
		}

		@After
		public void tearDown(){
			
		}
} 

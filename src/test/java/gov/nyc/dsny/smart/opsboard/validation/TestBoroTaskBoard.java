package gov.nyc.dsny.smart.opsboard.validation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.PersonAssignment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.ShiftCategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.SubcategoryTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;
import gov.nyc.dsny.smart.opsboard.viewmodels.BoroTasksBoard;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestBoroTaskBoard {
	//private static final Logger logger = LoggerFactory.getLogger(TestBoroTaskBoard.class);
	private  static BoardPerson boardPerson1 = new BoardPerson();
	private  static Person person1 = new Person();
	private  static BoardPerson boardPerson2 = new BoardPerson();
	private  static Person person2 = new Person();
	private  static BoardPerson boardPerson3 = new BoardPerson();
	private  static Person person3 = new Person();
	private  static BoardPerson boardPerson4 = new BoardPerson();
	private  static Person person4 = new Person();
	private  static BoardPerson boardPerson5 = new BoardPerson();
	private  static Person person5 = new Person();
	private static Task task1=new Task();
	private static Task task2=new Task();
	private static Task task3=new Task();
	private static PersonAssignment assignment1=new PersonAssignment();
	private static PersonAssignment assignment2=new PersonAssignment();
	private static PersonAssignment assignment3=new PersonAssignment();
	private static SubcategoryTask subcateTask1=new SubcategoryTask();
	private static SubcategoryTask subcateTask2=new SubcategoryTask();
	private static ConcurrentSkipListSet<Task> tasks1=new ConcurrentSkipListSet<Task>();
	private static ConcurrentSkipListSet<Task> tasks2=new ConcurrentSkipListSet<Task>();
	private static ShiftCategory shiftCate1=new ShiftCategory();
	private static ShiftCategory shiftCate2=new ShiftCategory();
	private static Map<String,SubcategoryTask> subcateTasks1=new HashMap<String,SubcategoryTask>();
	private static Map<String,SubcategoryTask> subcateTasks2=new HashMap<String,SubcategoryTask>();
	private static Category cate1=new Category();
	private static Category cate2=new Category();
	private static LocationShift locationS=new LocationShift();
	private static Map<String,ShiftCategory> shiftCs=new HashMap<String,ShiftCategory>();
	private static TaskContainer taskC=new TaskContainer();
	private static Map<String,LocationShift> locationSc=new HashMap<String,LocationShift>();
	private static List<TaskContainer> taskCc=new ArrayList<TaskContainer>();
	private static Board board=new Board();
	private static OpsBoard opdBoard=new OpsBoard(board);
	private static List<BoardPerson> bPersonL=new ArrayList<BoardPerson>();
	private static BoroTasksBoard boroTB=new BoroTasksBoard(opdBoard);
	public static final Date PAST_BOARD_DATE = new DateTime().withDate(2014,10,23).withTime(14,0,0,0).toDate();
	
	
	@BeforeClass
	public static void init()
	{
		//set person1 as supervisor , will be assigned to task
		person1.setId("1");
		person1.setCivilServiceTitle("GS 1");
		
		boardPerson1.setPerson(person1);
		boardPerson1.setCivilServiceTitle("GS 1");
		//person 2 as supervisor
		person2.setId("2");
		person2.setCivilServiceTitle("GS 1");
		boardPerson2.setPerson(person2);
		boardPerson2.setCivilServiceTitle("GS 1");
		//person 3,4,5 as sanitation worker
		person3.setId("3");
		person3.setCivilServiceTitle("SANI");
		
		boardPerson3.setPerson(person3);
		boardPerson3.setCivilServiceTitle("SANI");
		person3.setId("4");
		person4.setCivilServiceTitle("SANI");
		
		boardPerson4.setPerson(person4);
		boardPerson4.setCivilServiceTitle("SANI");
		person3.setId("5");
		person3.setCivilServiceTitle("SANI");
		boardPerson5.setPerson(person5);
		boardPerson5.setCivilServiceTitle("SANI");
		//add active NON 1L MDA status to person3
/*		mdaStatus.setId(1L);
		mdaStatus.setType("MDA");
		mdaStatus.setSubType("2");
		mdaStatus.setStatus("A");
		mdaStatus.setStartDate(PAST_BOARD_DATE);
		personMdaHistory.add(mdaStatus);
		boardPerson3.addMdaStatus(mdaStatus);
		person3.addMdaStatus(mdaStatus);
		person3.setMdaStatusHistory(personMdaHistory);	*/	
		
		assignment1.setId(1L);
		assignment1.setPerson(boardPerson1);
		assignment2.setId(2L);
		assignment2.setPerson(boardPerson3);
		assignment3.setId(3L);
		assignment3.setPerson(boardPerson4);
		task1.setId("1");
		task1.setAssignedPerson1(assignment1);
		task1.setSequence(1);
		task2.setId("2");
		task2.setAssignedPerson1(assignment2);
		task2.setSequence(2);
		task3.setId("3");
		task3.setAssignedPerson1(assignment3);
		task3.setSequence(3);
		//set supervisor and NON 1L person to normal subCate1
		subcateTask1.setId("1");
		tasks1.add(task1);	
		tasks1.add(task2);
		subcateTask1.setTasks(tasks1);
		//set normal person to supervision subcategory
		subcateTask2.setId("2");
		tasks2.add(task3);
		subcateTask2.setTasks(tasks2);
		subcateTasks1.put("1",subcateTask1);
		subcateTasks2.put("2",subcateTask2);
		//Collectionssort(subcateTasks1, new SubcategoryTask()))
		cate1.setId(1L);
		cate2.setId(2L);
		cate1.setName("Collection");
		cate2.setName("Supervision");
		shiftCate1.setId("1");
		shiftCate1.setSubCategoryTasksMap(subcateTasks1);
		shiftCate1.setCategory(cate1);
		shiftCate2.setId("2");
		shiftCate2.setSubCategoryTasksMap(subcateTasks2);
		shiftCate2.setCategory(cate2);
		locationS.setId("1");
		shiftCs.put("1",shiftCate1);
		shiftCs.put("2",shiftCate2);
		locationS.setShiftCategoriesMap(shiftCs);
		locationSc.put("1",locationS);
		taskC.setId(1L);
		taskC.setLocationShiftsMap(locationSc);
		taskCc.add(taskC);
		board.setId("1");
		board.setTaskContainers(taskCc);
		bPersonL.add(boardPerson1);
		bPersonL.add(boardPerson2);
		bPersonL.add(boardPerson3);
		bPersonL.add(boardPerson4);
		bPersonL.add(boardPerson5);
		board.setPersonnel(bPersonL);	
	}
	
/*	@Test
	public void TestTaskContainer(){
		Assert.assertEquals(boroTB.getTaskContainers().get("1").getLocationShifts().get("1").getShiftCategories().get("1").getSubcategoryTasks().get("1").getTasks().size(),2);
		Assert.assertEquals(boroTB.getTaskContainers().get("1").getLocationShifts().get("1").getShiftCategories().get("2").getSubcategoryTasks().get("2").getTasks().size(),1);
	}*/
	@Test
	public void TestPersonnel(){
	//	Assert.assertEquals(boroTB.getPersonnel().size(),2);
	}
	

}

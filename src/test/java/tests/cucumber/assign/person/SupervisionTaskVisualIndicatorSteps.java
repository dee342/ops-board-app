package tests.cucumber.assign.person;

import java.util.Date;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.SubcategoryTask;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;

public class SupervisionTaskVisualIndicatorSteps {
	
	private Person person;
	private BoardPerson boardPerson;
	private Subcategory subcategory;
	private Task task;
	private String boardDate;
	private Location homeLocation;
	private Date boardDt;
	private Date endDate;
	private Date startDate;
	private SubcategoryTask subcateTask;
	private Task superTask;
	

//	@Given("^regular supervisor$")
//	public void regular_supervisor(){
//		person=new Person();
//		person.setId("personId");
//		person.setCivilServiceTitle("SUP");
//		
//	}
//
//	@Given("^subcategory \"(.*?)\" under supervisor category$")
//	public void subcategory_under_supervisor_category(String subcateId)  {
//		subcategory=new Subcategory();
//		subcategory.setId(2L);
//		subcategory.setTaskIndicator("D/S");
//		
//	}
//
//	@Given("^task \"(.*?)\" under subcategory$")
//	public void task_under_subcategory(String taskId) {		
//		task=new Task();
//		task.setId(taskId);
//		subcateTask=new SubcategoryTask();
//		subcateTask.setSubcategory(subcategory);
//		subcateTask.setId("subcateTask");
//		List<Task> tasklist=new ArrayList<Task>();
//		tasklist.add(task);
//		subcateTask.setTasks(tasklist);
//
//	}
//
//	@Given("^give boardDate \"(.*?)\"$")
//	public void give_boardDate(String  boardDatee)  {
//		boardDate = boardDatee;		
//	}
//
//
//	@Given("^give homeLocation \"(.*?)\"$")
//	public void give_homeLocation(String homeLocationn) {
//		homeLocation = new Location(homeLocationn);
//		person.setHomeLocation(homeLocation);
//
//	}
//
//	@When("^supervisor assigned to task$")
//	public void supervisor_assigned_to_task() {
//		boardDt = new Date();
//		System.out.println("board date is::"+boardDt);
//		int milliSecondsInDay = 1000 * 60 * 60 * 24;
//		long startDateLong = boardDt.getTime() - milliSecondsInDay;
//		startDate = new Date(startDateLong);
//		long endDateLong = boardDt.getTime() + milliSecondsInDay;
//		endDate = new Date(endDateLong);
//		PersonAssignment personAssign=new PersonAssignment();
//		boardPerson=new BoardPerson();
//		boardPerson.setPerson(person);
//		boardPerson.setBoardDate(boardDate);
//		boardPerson.setHomeLocation(homeLocation);	
//		boardPerson.setBoardDate(boardDate);
//		boardPerson.setStartDate(startDate);
//		boardPerson.setEndDate(endDate);
//		boardPerson.setAssigned(true);
//		personAssign.setPerson(boardPerson);
//		task.setAssignedPerson1(personAssign);
//		
//	}
//	
//	@When("^supervisor assigned to superTask$")
//	public void supervisor_assigned_to_superTask() {
//		superTask=new Task();
//		superTask.setId("superTask");
//		TaskSupervisorAssignment supervisorAssin=new TaskSupervisorAssignment();
//		supervisorAssin.setPerson(boardPerson);
//		List<TaskSupervisorAssignment> taskSuperAssignList=new ArrayList<TaskSupervisorAssignment>();
//		taskSuperAssignList.add(supervisorAssin);
//		superTask.setTaskSupervisorAssignments(taskSuperAssignList);				
//
//	}
//	
//	@Then("^task has supervisor assigned \"(.*?)\"$")
//	public void task_has_supervisor_assigned(String arg1)  {
//		assertThat(superTask.getTaskSupervisorAssignments().get(0).getPerson().getPerson().getId(),equalTo("personId"));
//	}
//
//	@Then("^supervisionTask has visual indicator \"(.*?)\"$")
//	public void supervisiontask_has_visual_indicator(String arg1)  {
//		assertThat(subcateTask.getSubcategory().getTaskIndicator(),equalTo("D/S"));
//	}

}

package tests.cucumber.assign.person;

import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;

import java.util.Date;

public class AssignSupervisorToSupervisionTaskSteps {
	
	private Person supervisor;
	private Task supervisionTask;
	private String boardDate;
	private Location location;
	private Location homeLocation;
	private Date boardDt;
	private Date endDate;
	private Date startDate;
	
	
//	@Given("^supervisor \"([^\"]*)\"$")
//	public void person(String personId){
//		supervisor = new Person();
//		supervisor.setId(personId);
//		supervisor.setCivilServiceTitle("SUP");
//	}
//	
//	@Given("^location \"([^\"]*)\"$")
//	public void location(String locationn){
//		location = new Location(locationn);
//	}
//	
//	@Given("^homeLocation \"([^\"]*)\"$")
//	public void homeLocation(String homeLocationn){
//		homeLocation = new Location(homeLocationn);
//		supervisor.setHomeLocation(homeLocation);
//	}
//
//	
//	@Given("^supervisionTask \"([^\"]*)\"$")
//	public void supervisionTask(String taskId){
//		supervisionTask = new Task();
//		supervisionTask.setId(taskId);
//	}
//	
//	@Given("^boardDate \"([^\"]*)\"$")
//	public void boardDate(String boardDatee){
//		boardDate = boardDatee;
//	}
//	
//
//
//	
//	@When("^supervisor assigned to supervisionTask$")
//	public void assign_person_to_supervisionTask(){
//		boardDt = new Date();
//		System.out.println("board date is::"+boardDt);
//		int milliSecondsInDay = 1000 * 60 * 60 * 24;
//		long startDateLong = boardDt.getTime() - milliSecondsInDay;
//		startDate = new Date(startDateLong);
//		long endDateLong = boardDt.getTime() + milliSecondsInDay;
//		endDate = new Date(endDateLong);
//		System.out.println("startDate is::"+startDate);
//		System.out.println("end date is::"+endDate);
//		PersonAssignment personAssignment = new PersonAssignment();
//		BoardPerson boardPerson = new BoardPerson();
//		boardPerson.setBoardDate(boardDate);
//		boardPerson.setStartDate(startDate);
//		boardPerson.setEndDate(endDate);
//		boardPerson.setPerson(supervisor);
//		boardPerson.setAssigned(true);
//		personAssignment.setPerson(boardPerson);
//		supervisionTask.setAssignedPerson1(personAssignment);
//		supervisionTask.getAssignedPerson1().setPerson(boardPerson);
//	}
//	
//	
//	@Then("^supervisionTask has supervisor assigned \"([^\"]*)\"$")
//	public void task_has_supervisor_assigned(String personId){
//		assertThat(supervisionTask.getAssignedPerson1().getPerson().getPerson().getId(), equalTo(personId));
//	}
//	
//	@Then("^supervisor state is \"([^\"]*)\"$")
//	public void supervisor_state_assigned(String state){
//		assertThat(supervisor.getState(location, boardDt, startDate, endDate, new Date()).getState(), equalTo(state));
//	}
//	

}

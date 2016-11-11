package tests.cucumber.state.person;

import java.util.Date;

import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

public class AddUnavailabilityToPersonSteps {
	
	private Person person;
	private UnavailabilityReason unavailableReason;
	private UnavailabilityReason unavailableReason2;
	private String boardDate;
	private Location location;
	private Location homeLocation;
	private Date boardDt;
	private Date endDate;
	private Date startDate;
	private BoardPerson boardPerson;
	
//	@Given("^person \"(.*?)\"$")
//	public void sanitation_worker(String personId){
//		person=new Person();
//		person.setId(personId);		
//	}
//	
//	@Given("^set boardDate \"([^\"]*)\"$")
//	public void boardDate(String boardDatee){
//		boardDate = boardDatee;
//	} 
//	
//	@Given("^set location \"([^\"]*)\"$")
//	public void location(String locationn){
//		location = new Location(locationn);
//	}
//	
//	@Given("^set homeLocation \"([^\"]*)\"$")
//	public void homeLocation(String homeLocationn){
//		homeLocation = new Location(homeLocationn);
//		person.setHomeLocation(homeLocation);
//	}
//
//	@When("^add vacation unavailable to sanitation worker$")
//	public void add_vacation_unavailable_to_sanitation_worker(){
//		boardDt = new Date();
//		System.out.println("board date is::"+boardDt);
//		int milliSecondsInDay = 1000 * 60 * 60 * 24;
//		long startDateLong = boardDt.getTime() - milliSecondsInDay;
//		startDate = new Date(startDateLong);
//		long endDateLong = boardDt.getTime() + milliSecondsInDay;
//		endDate = new Date(endDateLong);
//		System.out.println("startDate is::"+startDate);
//		System.out.println("end date is::"+endDate);
//		boardPerson = new BoardPerson();
//		boardPerson.setBoardDate(boardDate);
//		boardPerson.setStartDate(startDate);
//		boardPerson.setEndDate(endDate);
//		boardPerson.setPerson(person);
//		unavailableReason=new UnavailabilityReason("actualUser", "systemUser", new Date(), new Date(),
//				"VACATION", "ORI_RECORD", null, new Date(), "A");
//		unavailableReason.setId(1L);
//		person.addUnavailabilityReason(unavailableReason);
//	}
//
//	
//	@When("^change then delete unavailable code$")
//	public void delete_unavailable_code(){		
//		UnavailabilityReason oldReason = boardPerson.getUnavailabilityReason(unavailableReason.getId());
//		oldReason.setStatus("I");
//		unavailableReason2=new UnavailabilityReason("actualUser", "systemUser", new Date(), new Date(),
//				"XWOP", "SECOND_RECORD", null, new Date(), "A");
//		unavailableReason2.setId(2L);
//		person.addUnavailabilityReason(unavailableReason2);
//		UnavailabilityReason oldReason2 = boardPerson.getUnavailabilityReason(unavailableReason2.getId());	
//		oldReason2.setStatus("I");
//		UnavailabilityReason newReason2 = (UnavailabilityReason) CloneUtils.deepClone(oldReason2);
//		newReason2.setStatus("R");		
//		person.addUnavailabilityReason(newReason2);
//	}
//	
//	@Then("^the state of person is \"(.*?)\"$")
//	public void the_state_of_person_is(String arg1){
//		/*assertThat(person.getState(location, boardDt, startDate, endDate, new Date()).getState(),  equalTo("Available"));*/
//	}
	
}

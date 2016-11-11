package tests.cucumber.detach.person;

import java.util.Date;

import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

public class DetachBoroPerson {
	
	
	private Person person;
	private BoardPerson boardPerson;
	private Location location;
	private Location homeLocation;
	private String boardDate;
	private Date boardDt;
	private Date endDate;
	private Date startDate;
	
//	@Given("^superintendent \"(.*?)\"$")
//	public void superintendent(String personId){
//		person=new Person();
//		person.setId(personId);
//		person.setCivilServiceTitle("SUP");
//	}
//
//	@Given("^boro boardDate \"(.*?)\"$")
//	public void boro_boardDate(String boardDatee){
//		boardDate=boardDatee;
//		boardPerson=new BoardPerson();
//		boardPerson.setId("boardPerson");
//	    boardPerson.setPerson(person);
///*	    boardPerson.setBoardDate(boardDate);*/
//
//	}
//
//	@Given("^boro homeLocation BXBO$")
//	public void boro_homeLocation_BXBO(){
//       homeLocation=new Location("BXBO");
//       person.setHomeLocation(homeLocation);
//       boardPerson.setHomeLocation(homeLocation);
//		
//	}
//	
//	@Given("^boro location BXone$")
//	public void boro_location_BXone(){
//		location=new Location("BX01");
//	}
//
//	@When("^detach person to BXone$")
//	public void detach_person_to_BXone(){
//		boardDt = new Date();
//		int milliSecondsInDay = 1000 * 60 * 60 * 24;
//		long startDateLong = boardDt.getTime() - milliSecondsInDay;
//		startDate = new Date(startDateLong);
//		long endDateLong = boardDt.getTime() + milliSecondsInDay;
//		endDate = new Date(endDateLong);
//		boardPerson.setBoardDate(boardDate);
//		person.detach("systemUser", new Date(), homeLocation, location, "first detach", "0800-1600", startDate, endDate);
//
//	}
//	
//	@Then("^the state of person is Detached$")
//	public void the_state_of_person_is_Detached(){
//		assertThat(person.getState(homeLocation, boardDt, startDate, endDate).getState(),equalTo("Detached"));
//	}
//
//	@Then("^person is not assigned$")
//	public void person_is_not_assigned(){
//		assertThat(boardPerson.isAssigned(),equalTo(false));
//
//	}
//

}

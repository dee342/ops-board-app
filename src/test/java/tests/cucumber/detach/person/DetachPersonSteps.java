package tests.cucumber.detach.person;

import static org.hamcrest.MatcherAssert.assertThat;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.validation.ValidationUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cucumber.api.Format;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

public class DetachPersonSteps {
	
	
	private Person person;
	private BoardPerson boardPerson;	
	private Date lastDate;
	private long id;
	
	@Before
	public void beforeScenario() {
		System.out.println("before Scenario");
		person = null;
		boardPerson = null;	
		lastDate = null;
		id = 0;
	}

	@After
	public void afterScenario() {
		System.out.println("after Scenario");
		person = null;
		boardPerson = null;
		lastDate = null;
		id = 0;
	}	

	
	@Given("^a person with id '(.+)' at homeLocation '(.+)'$")
	public void personWithBoardDateAtHomeLocation(String personId, String location){
		person = new Person();
		person.setId(personId);
		boardPerson = new BoardPerson();
		boardPerson.setId(personId);
	    boardPerson.setPerson(person);	    
	    Location homeLocation=new Location(location);
	    person.setHomeLocation(homeLocation);
	    boardPerson.setHomeLocation(homeLocation);
	}	


	@When("^that person is detached to '(.+)' from '(.+)' to '(.+)'$")
	public void detachPersonFromHomeToAnother(String toLocation, @Format("yyyyMMdd") Date fromDate, @Format("yyyyMMdd") Date toDate){
		detachPersonToFor(toLocation, fromDate, toDate);
	}
	
	@When("^that person is detached to '(.+)' from '(.+)' permanently$")
	public void detachPersonFromHomeToAnotherPermanently(String toLocation, @Format("yyyyMMdd") Date fromDate){
		detachPersonToFor(toLocation, fromDate, null);
	}	
	
	@When("^then detached back to '(.+)' from '(.+)' to '(.+)'$")
	public void detachPersonFromAnotherToHome(String toLocation, @Format("yyyyMMdd") Date fromDate, @Format("yyyyMMdd") Date toDate){
		detachPersonToFor(toLocation, fromDate, toDate);
	}	
	
	protected void detachPersonToFor(String toLocation, Date fromDate, Date toDate){
		Location otherLocation = new Location(toLocation);
	    fromDate = DateUtils.removeTime(fromDate);
	    if(toDate != null){
	    	toDate = DateUtils.removeTime(toDate);
	    }
	    Date date = new Date();
	    if(date.equals(lastDate)){
	    	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	date = new Date();
	    }
	    //we want to make sure the date/timestamp is not the same for more than one detach
	    //because these are happening very fast and successively there is a chance that the dates could be the same
	    // and it has resulted in incorrect ordering of detachment records
	    Detachment detachment = new Detachment("systemUser", date, "first detach", "0800-1600", toDate, person.getHomeLocation(), 
	    		fromDate, otherLocation, Detachment.DETACH_STATUS_AVAILABLE, null, "123");
		person.detach(detachment);
		lastDate = date;
	}	

	@Then("^the state of person for '(.+)' with board date '(.+)' should be '(.+)'$")
	public void expectedSate(String loc, @Format("yyyyMMdd") Date boardDate, String expectedState){
	    boardDate = DateUtils.removeTime(boardDate);
		Location location = new Location(loc);
//		assertThat(boardPerson.getState(location, boardDate, BoardKey.createBoardStartDate(boardDate), BoardKey.createBoardEndDate(boardDate)).getState(),equalTo(expectedState));
	}
	
	@Then("^You '(.+)' be able to detach from '(.+)' starting '(.+)' to '(.+)', current day being Today$")
	public void validateDetachmentWithEndDate(String result, String loc,  @Format("yyyyMMdd") Date desiredDetachmentStartDate,  @Format("yyyyMMdd") Date desiredDetachmentEndDate){

		validateDetachment(result, loc, desiredDetachmentStartDate, desiredDetachmentEndDate, DateUtils.getTodayWith12AM());
	}
	
	@Then("^You '(.+)' be able to detach from '(.+)' starting '(.+)' to '(.+)', current day being '(.+)'$")
	public void validateDetachmentWithEndDate(String result, String loc,  @Format("yyyyMMdd") Date desiredDetachmentStartDate,  @Format("yyyyMMdd") Date desiredDetachmentEndDate, @Format("yyyyMMdd") Date today){

		validateDetachment(result, loc, desiredDetachmentStartDate, desiredDetachmentEndDate, today);
	}	
	
	@Then("^You '(.+)' be able to detach from '(.+)' starting '(.+)' permanently, current day being Today$")
	public void validateDetachmentPermanent(String result, String loc, @Format("yyyyMMdd") Date desiredDetachmentStartDate){

		validateDetachment(result, loc, desiredDetachmentStartDate, null, DateUtils.getTodayWith12AM());
	}
	
	@Then("^You '(.+)' be able to detach from '(.+)' starting '(.+)' permanently, current day being '(.+)'$")
	public void validateDetachmentPermanent(String result, String loc,  @Format("yyyyMMdd") Date desiredDetachmentStartDate, @Format("yyyyMMdd") Date today){

		validateDetachment(result, loc, desiredDetachmentStartDate, null, today);
	}	
	
	private void validateDetachment(String result, String loc, Date desiredDetachmentStartDate, Date desiredDetachmentEndDate, Date today){

  		List<Detachment> presentAndFutureDetachmentList = person.getPresentAndFutureDetachments(today);
  		Date currentDate = new Date();
  		Calendar cal = Calendar.getInstance();
  		cal.setTime(currentDate);
  		cal.add(Calendar.DATE, -1);
  		cal.set(Calendar.HOUR, 22);
  		Date startDate = cal.getTime();
  		cal.setTime(currentDate);
  		cal.add(Calendar.DATE, 1);
  		cal.set(Calendar.HOUR, 5);
  		Date endDate = cal.getTime();
		boolean isValid = ValidationUtils.isValidDetachmentDate(desiredDetachmentStartDate, desiredDetachmentEndDate,currentDate,
				presentAndFutureDetachmentList, loc, loc.equalsIgnoreCase(person.getHomeLocation().getCode()), startDate, endDate);
		if("should".equalsIgnoreCase(result)){
			assertThat("Validation Rule",isValid);
		} else {
			assertThat("Validation Rule",!isValid);
		}
	}	
}

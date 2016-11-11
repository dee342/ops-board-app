package gov.nyc.dsny.smart.opsboard.validation;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.personnel.VolunteerCounts;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.UnavailabilityType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestValidations {
	
	private  static Date today = new Date();
	private  static Date todayStartDate;
	private  static Date todayEndDate;
	private  static Date yesterday;
	private  static Date tomorrow;
	private  static BoardPerson boardPerson = new BoardPerson();
	private static BoardKey boardKey;
	private  static Person person = new Person();
	private  static ConcurrentSkipListSet<UnavailabilityReason> personUnavailabilityHistory;
	private  static List<UnavailabilityReason> unavailabilityHistory;
	
	@BeforeClass
	public static void init()
	{
		
		person.setId("1");
		boardPerson.setPerson(person);
		boardPerson.setDate(today);
		
		Calendar tomorrowCalendar = Calendar.getInstance();
		tomorrowCalendar.add(Calendar.DATE, 1);
		tomorrow = tomorrowCalendar.getTime();
		
		Calendar yesterdayCalendar = Calendar.getInstance();
		yesterdayCalendar.add(Calendar.DATE, -1);
		yesterday = yesterdayCalendar.getTime();
		
		Calendar todayStartDateCalendar = Calendar.getInstance();
		todayStartDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
		todayStartDateCalendar.set(Calendar.MINUTE, 0);
		todayStartDateCalendar.set(Calendar.MILLISECOND, 0);
		todayStartDate = todayStartDateCalendar.getTime();
		
		Calendar todatEndDateCalendar = Calendar.getInstance();
		todatEndDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
		todatEndDateCalendar.set(Calendar.MINUTE, 59);
		todayStartDateCalendar.set(Calendar.MILLISECOND, 0);
		todayEndDate = todatEndDateCalendar.getTime();
		
		Location location = new Location("BKN01");
		location.setId(1L);
		boardKey = new BoardKey(DateUtils.toStringBoardDate(today), location, todayStartDate, todayEndDate);
	}
	
	@Before
	public void initHistory()
	{
		personUnavailabilityHistory = new ConcurrentSkipListSet<UnavailabilityReason>();
		person.setUnavailabilityHistory(personUnavailabilityHistory);
	}
	
	private UnavailabilityReason addUnavailabilityReason(UnavailabilityReason anavailabilityReason, Long id, Long parentId, Long childId)
	{
		addUnavailabilityReason(anavailabilityReason, id);
		anavailabilityReason.setReplaces(parentId);
		anavailabilityReason.setReplacedBy(childId);	
		return anavailabilityReason;
	}
	
	private UnavailabilityReason addUnavailabilityReason(UnavailabilityReason anavailabilityReason, Long id)
	{
		anavailabilityReason.setId(id);
		return anavailabilityReason;
	}
	
	
	// *****************************************************************************************************************************
	// ***************************** Add unavailability test cases *****************************************************************
	// *****************************************************************************************************************************

	@Test
	public void testAddUnavailableReason3() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, null, yesterday, null, "Comments1",unavailabilityHistory, boardPerson, "ADD_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot create record - unavailability type must be defined", e.getMessage()); 
		}
	}
	
	@Test
	public void testAddUnavailableReason4() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "ABC", null, null, "Comments1",unavailabilityHistory, boardPerson, "ADD_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot create ABC - start date must be defined", e.getMessage()); 
		}
	}
	
	@Test
	public void testAddUnavailableReason5() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("ABC", ""), "Comments1", todayEndDate, todayStartDate, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "ABC", todayStartDate, todayEndDate, "Comments1",unavailabilityHistory, boardPerson, "ADD_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot create ABC - a record for this same calendar date range already exists", e.getMessage()); 
		}
	}

	// *****************************************************************************************************************************
	// ***************************** Update unavailability test cases *****************************************************************
	// *****************************************************************************************************************************
	@Test
	public void testUpdateUnavailableReason1() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "A"), 1L, null, 2L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "C"), 2L, 1L, null ));
		try
		{
			ValidationUtils.isValid(null, "CHART", yesterday, todayEndDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update record - id is unavailable", e.getMessage()); 
		}
	}

	
	@Test
	public void testUpdateUnavailableReason2() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, null, yesterday, null, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update record - unavailability type must be defined", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason3() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "CHART", null, null, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update CHART - start date must be defined", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason4() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "CHART", todayEndDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update CHART - start date must be earlier than end date", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason5() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", todayEndDate, todayEndDate, "I", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "VACATION", todayStartDate, todayEndDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update data - active VACATION was not found", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason5_1() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", todayEndDate, todayEndDate, "A", null), 1L ));
		ValidationUtils.isValid(1L, "VACATION", todayStartDate, todayEndDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
	}

	
	@Test
	public void testUpdateUnavailableReason6() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason(new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", todayEndDate, todayEndDate, "A", null), 2L) );
		try
		{
			ValidationUtils.isValid(1L, "VACATION", todayStartDate, todayEndDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update data - active VACATION was not found", e.getMessage()); 
		}
	}

	@Test
	public void testUpdateUnavailableReason7() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "A"), 1L, null, 2L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "C"), 2L, 1L, null ));
		try
		{
			ValidationUtils.isValid(1L, "CHART", todayEndDate, todayEndDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update CHART - start date cannot be changed", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason8() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "A"), 1L, null, 2L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "C"), 2L, 1L, null ));
		try
		{
			ValidationUtils.isValid(1L, "CHART", todayStartDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update CHART - end date cannot be changed", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason9() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", null, todayStartDate, "A", "A"), 1L, null, 2L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "C"), 2L, 1L, null ));
		try
		{
			ValidationUtils.isValid(1L, "CHART", todayStartDate, todayEndDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update CHART - end date cannot be changed", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason10() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "A"), 1L, null, 2L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", "C"), 2L, 1L, null ));
		try
		{
			ValidationUtils.isValid(1L, "CHART", todayStartDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update CHART - end date cannot be changed", e.getMessage()); 
		}
	}
	
	@Test
	public void testUpdateUnavailableReason11() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", tomorrow, yesterday, "A", "A"), 1L, null, 2L) );
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", tomorrow, yesterday, "A", "C"), 2L, 1L, null ));
		try
		{
			ValidationUtils.isValid(1L, "VACATION", yesterday, tomorrow, "Comments2",unavailabilityHistory, boardPerson, "UPDATE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot update VACATION - comments cannot be changed on a canceled record", e.getMessage()); 
		}

	}

	// *****************************************************************************************************************************
	// ***************************** Remove unavailability test cases **************************************************************
	// *****************************************************************************************************************************
	@Test
	public void tesDeleteUnavailableReason1() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(2L, "CHART", todayEndDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "REMOVE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot delete record - active CHART was not found", e.getMessage()); 
		}
	}
	
	@Test
	public void tesDeleteUnavailableReason2() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "I", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "CHART", todayEndDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "REMOVE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot delete record - active CHART was not found", e.getMessage()); 
		}
	}
	
	@Test
	public void tesDeleteUnavailableReason3() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", tomorrow, yesterday, "A", "A"), 1L, null, 2L ));
		unavailabilityHistory.add(	addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", tomorrow, yesterday, "A", "C"), 2L, 1L, null) );
		try
		{
			ValidationUtils.isValid(1L, "VACATION", yesterday, tomorrow, "Comments2",unavailabilityHistory, boardPerson, "REMOVE_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot delete record - VACATION has a canceled day", e.getMessage()); 
		}
	}
	
	// *****************************************************************************************************************************
	// ***************************** Cancel unavailability test cases **************************************************************
	// *****************************************************************************************************************************
	@Test
	public void testCancelUnavailableReason1() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		// This is the test for StackOverflow error in the getLastChild
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", yesterday, tomorrow, "A", null) , 2L));
		ValidationUtils.isValid(1L, "CHART", today, today, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
	}
	
	@Test
	public void testCancelUnavailableReason2() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "XYZ", today, today, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot cancel XYZ day", e.getMessage()); 
		}
	}
	
	
	// Not active status in the history
	@Test
	public void testCancelUnavailableReason3() throws Exception 
	{		
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "I", null), 1L) );
		try
		{
			ValidationUtils.isValid(1L, "CHART", today, today, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
//			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertTrue(e.getMessage().startsWith("Cannot cancel CHART - related record was not found")); 
		}
	}
	
	@Test
	public void testCancelUnavailableReason3_1() throws Exception 
	{	
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", tomorrow, tomorrow, "A", null), 1L) );
		try
		{
			ValidationUtils.isValid(1L, "CHART", today, today, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot cancel CHART - date is different", e.getMessage()); 
		}
	}
	
	
	@Test
	public void testCancelUnavailableReason4() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L) );
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", yesterday, tomorrow, "A", null) , 2L));
		try
		{
			ValidationUtils.isValid(2L, "VACATION", today, today, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot cancel VACATION on a Chart day", e.getMessage()); 
		}
	}
	
	@Test
	public void testCancelUnavailableReason5() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", null), 1L) );
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("ABC", ""), "Comments1", tomorrow, yesterday, "A", null) , 2L));
		try
		{
			ValidationUtils.isValid(1L, "CHART", todayStartDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot cancel Chart - employee is ABC", e.getMessage()); 
		}
	}
		
	@Test
	public void testCancelUnavailableReason6() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", todayEndDate, todayStartDate, "A", null), 1L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", tomorrow, yesterday, "A", null) , 2L));
		try
		{
			ValidationUtils.isValid(2L, "VACATION", todayStartDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot cancel VACATION on a Chart day", e.getMessage()); 
		}
	}

	@Test
	public void testCancelUnavailableReason7() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", todayEndDate, todayStartDate, "A", null), 1L) );
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("ABC", ""), "Comments1", tomorrow, yesterday, "A", null) , 2L));
		try
		{
			ValidationUtils.isValid(1L, "VACATION", todayStartDate, todayStartDate, "Comments1", unavailabilityHistory,boardPerson, "CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot cancel VACATION - employee is ABC", e.getMessage()); 
		}
	}
	
	@Test
	public void testCancelUnavailableReason8() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("JURY DUTY", ""), "Comments1", todayEndDate, todayStartDate, "A", null), 1L ));
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("ABC", ""), "Comments1", tomorrow, yesterday, "A", null) , 2L));
		try
		{
			ValidationUtils.isValid(1L, "JURY DUTY", todayStartDate, todayStartDate, "Comments1",unavailabilityHistory, boardPerson, "CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot cancel JURY DUTY - employee is ABC", e.getMessage()); 
		}
	}

	
	// *****************************************************************************************************************************
	// ***************************** Reverse cancel unavailability test cases ******************************************************
	// *****************************************************************************************************************************

	@Test
	public void testReverseCancelUnavailableReason1() throws Exception 
	{
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", today, today, "A", null), 1L ));
		try
		{
			ValidationUtils.isValid(1L, "XYZ", today, today, "Comments1",unavailabilityHistory, boardPerson, "REVERSE_CANCEL_UNAVAILABLE",boardKey);
			Assert.fail();
		} 
		catch (UnavailabilityValidationException e)
		{
			Assert.assertEquals("Cannot reverse cancel XYZ", e.getMessage()); 
		}
	}
	
//	@Test
//	public void testReverseCancelUnavailableReason2() throws Exception 
//	{
//		addUnavailabilityReason (new UnavailabilityReason(null, null, null, "CHART", "Comments1", today, today, "I", null), 1L );
//		try
//		{
//			ValidationUtils.isValid(1L, "CHART", today, today, "Comments1", boardPerson, "REVERSE_CANCEL_UNAVAILABLE", boardKey);
//			Assert.fail();
//		} 
//		catch (UnavailabilityValidationException e)
//		{
//			Assert.assertEquals("Cannot reverse cancel CHART", e.getMessage()); 
//		}
//	}
//	@Test
//	public void testPartialTasksDurationWithLessThanEightHours() throws Exception {
//		
//		List<PartialTask> partialTasks = new ArrayList<PartialTask>();
//		PartialTask partialTask = new PartialTask();
//		partialTask.setHours(2);
//		PartialTask partialTask2 = new PartialTask();
//		partialTask2.setHours(2);
//		partialTasks.add(partialTask);
//		partialTasks.add(partialTask2);
//		
//		Assert.assertEquals("success", ValidationUtils.isInvalidPartialTask(partialTasks));
//	}
//	
//	@Test
//	public void testPartialTasksDurationWithEightHoursTask() throws Exception {
//		
//		List<PartialTask> partialTasks = new ArrayList<PartialTask>();
//		PartialTask partialTask = new PartialTask();
//		partialTask.setHours(4);
//		PartialTask partialTask2 = new PartialTask();
//		partialTask2.setHours(4);
//		partialTasks.add(partialTask);
//		partialTasks.add(partialTask2);
//		
//		Assert.assertEquals("success", ValidationUtils.isValidPartialTask(partialTasks));
//	}
	
	@Test
	public void testValidateVolunteerCountsVacation() throws Exception {	
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		 HashMap<String, BoardPerson> boardPersonHashMap = new HashMap<String, BoardPerson>();
		 VolunteerCounts volunteerCounts = new VolunteerCounts();	
		 volunteerCounts.setChartVolunteers(99);
		 volunteerCounts.setMandatoryChart(99);
		 volunteerCounts.setVacationVolunteers(2);
		 unavailabilityHistory.add(addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("VACATION", ""), "Comments1", 
				 today, today, "A", null), 1L ));		 
		 boardPersonHashMap.put("1", boardPerson);
		try {
			gov.nyc.dsny.smart.opsboard.ErrorMessage errorMessage= ValidationUtils.validateVolunteerCounts(volunteerCounts,
					boardPersonHashMap);
			Assert.assertEquals(ErrorMessage.CHECK_VACATION_VOLUNTEER_COUNT,errorMessage );
		} catch (UnavailabilityValidationException e) {
			
		}		 		
	}
	
	@Test
	public void testValidateVolunteerCountsNoVacation() throws Exception {	
		unavailabilityHistory = new ArrayList<UnavailabilityReason>();
		 HashMap<String, BoardPerson> boardPersonHashMap = new HashMap<String, BoardPerson>();
		 VolunteerCounts volunteerCounts = new VolunteerCounts();	
		 volunteerCounts.setChartVolunteers(99);
		 volunteerCounts.setMandatoryChart(99);
		 volunteerCounts.setVacationVolunteers(1);
		 unavailabilityHistory.add( addUnavailabilityReason (new UnavailabilityReason(null, null, null, new UnavailabilityType("CHART", ""), "Comments1", 
				 today, today, "A", null), 1L ));		 
		 boardPersonHashMap.put("1", boardPerson);
		try {
			gov.nyc.dsny.smart.opsboard.ErrorMessage errorMessage= ValidationUtils.validateVolunteerCounts(volunteerCounts,
					boardPersonHashMap);
			Assert.assertEquals(ErrorMessage.CHECK_VACATION_VOLUNTEER_COUNT,errorMessage );
		} catch (UnavailabilityValidationException e) {
			
		}		 		
	}
	
	
}

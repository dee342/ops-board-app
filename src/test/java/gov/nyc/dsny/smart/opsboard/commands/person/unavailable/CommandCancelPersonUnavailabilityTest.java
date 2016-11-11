package gov.nyc.dsny.smart.opsboard.commands.person.unavailable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.util.Date;
import java.util.List;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.gemstone.gemfire.internal.tools.gfsh.app.commands.local;


public class CommandCancelPersonUnavailabilityTest {
	
	private static final String PERSON_ID = "PERSON_ID";
	private static final String STATE = "STATE";
	
	private CommandCancelPersonUnavailability command;
	
	@Mock
	private Board board;
	
	@Mock
	private BoardPerson boardPerson;
	
	@Mock
	private Location location;
	
	@Mock
	private StateAndLocation state;
	
	@Mock
	private List<UnavailabilityReason> unavailableReasons;
	
	@Mock
	private UnavailabilityReason unavailabilityReason;
	
	
	@Before
	public void setUp() throws OpsBoardError{
		command = new CommandCancelPersonUnavailability();
		command.setPersonId(PERSON_ID);
		command.setSystemUser("SYSTEM_USER");
		command.setSystemDateTime(new Date());
		command.setUnavailableReason(new UnavailabilityReason());
		
		MockitoAnnotations.initMocks(this);
		
		when(board.updatePersonUnavailabilityReason(anyString(), any(UnavailabilityReason.class))).thenReturn(boardPerson);
		when(board.getLocation()).thenReturn(location);
		when(boardPerson.getState(location)).thenReturn(state);
		when(boardPerson.getActiveUnavailabilityReasons()).thenReturn(unavailableReasons);
		when(boardPerson.getFullName()).thenReturn("FULL NAME");
	}
	
	@After
	public void tearDown(){
		command = null;
	}
	
	@Test 
	public void testExecuteCommand() throws OpsBoardError{
	/*	command.execute(board);
		verify(boardPerson, times(1)).getState(location);
		verify(board, times(1)).updatePersonUnavailabilityReason(anyString(), any());
		verify(boardPerson, times(1)).getActiveUnavailabilityReasons();
		verify(board, times(1)).addCommandToHistory(command); */
	}
	

	@Test
	public void testStateIsUpdatedInCommand() throws OpsBoardError{
		/* when(state.getState()).thenReturn(STATE);
		command.execute(board);
		assertThat(command.getState(), equalTo(STATE)); */
	}
	
	@Test
	public void testActiveUnavailabilityReasonsUpdatedInCommand() throws OpsBoardError{
/*		assertThat(command.getActiveUnavailabilityReasons(), is(nullValue()));
		command.execute(board);
		assertThat(command.getActiveUnavailabilityReasons(), is(notNullValue())); */
	}
	
}

package gov.nyc.dsny.smart.opsboard.services.sorexecutors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.UnavailabilityTypeCacheService;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService;
import gov.nyc.dsny.smart.opsboard.util.CloneUtils;
import gov.nyc.dsny.smart.opsboard.persistence.services.personnel.PersonnelPersistenceService;

import java.security.Principal;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RunWith(PowerMockRunner.class)
@PrepareForTest(CloneUtils.class)
public class PersonExecutorTest {
	
	private static final String PERSON_ID = "PERSON_ID";
	private static final String USER = "USER";
	private static final String LOCATION_CODE = "LOCATION_CODE";
	
	@InjectMocks private PersonExecutor personExecutor;
	@Mock private OutgoingPersonnelService outgoingPersonnelService;
	@Mock private PersonnelPersistenceService persistenceService;
	@Mock private UnavailabilityTypeCacheService unavailabilityTypeCacheService;
	@Mock private SimpMessagingTemplate messenger;
    @Mock private BoardKey boardKey;
    @Mock private BoardPerson boardPerson;
    @Mock private UnavailabilityReason reason;
    @Mock private UnavailabilityReason persistedReason;
    @Mock private UnavailabilityReason newReason;
    @Mock UnavailabilityReason oldReason;
    @Mock private Principal principal;
    @Mock private Person person;
    @Mock private Location location;
	
        
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);	
		mockStatic(CloneUtils.class);
		PowerMockito.when(CloneUtils.deepClone(any(UnavailabilityReason.class))).thenReturn(newReason);
		when(boardPerson.getPerson()).thenReturn(person);
		when(boardPerson.getUnavailabilityReason(anyLong())).thenReturn(oldReason);
		when(person.getId()).thenReturn(PERSON_ID);
		when(boardPerson.getWorkLocation()).thenReturn(location);
		when(boardPerson.getHomeLocation()).thenReturn(location);
		when(location.getCode()).thenReturn(LOCATION_CODE);
		when(persistenceService.updateUnavailability(anyString(), any(UnavailabilityReason.class), 
				any(UnavailabilityReason.class), anyString(), any(Date.class))).thenReturn(persistedReason);
	}
	
	@After
	public void tearDown(){
		
	}
	
//	@Test(expected=OpsBoardError.class)
//	public void testCancelPersonUnavailabilityWithOpsBoardError() throws OpsBoardError, OpsBoardValidationException{
//		when(persistenceService.updateUnavailability(anyString(), any(UnavailabilityReason.class),
//				any(UnavailabilityReason.class), anyString(), any(Date.class))).thenThrow(new RuntimeException("Test Exception"));
//		personExecutor.cancelUnavailabilityReason(boardKey, boardPerson, reason, principal);
//	}

	@Test 
	public void testCancelPersonUnavailabilityWithReason() throws OpsBoardError{
	/*	personExecutor.cancelUnavailabilityReason(boardKey, boardPerson, reason, principal);
		verify(persistenceService, times(1)).
			updateUnavailability(anyString(), any(UnavailabilityReason.class), 
					any(UnavailabilityReason.class), anyString(), any(Date.class));*/
	}
	
	@Test 
	public void testCancelPersonUnavailabilityCommandsAreSent() throws OpsBoardError{
	/*	personExecutor.cancelUnavailabilityReason(boardKey, boardPerson, reason, principal);
		verify(messenger, times(1)).convertAndSend(anyString(), any(LocationCommandMessage.class));*/
	}
	
}

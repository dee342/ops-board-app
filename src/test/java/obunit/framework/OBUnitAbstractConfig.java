package obunit.framework;

import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.PersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardEquipmentGFRepository;
import gov.nyc.dsny.smart.opsboard.cache.gf.repository.LocationGFRepository;
import gov.nyc.dsny.smart.opsboard.cache.gf.repository.RefDataTrackingGFRepository;
import gov.nyc.dsny.smart.opsboard.cache.gf.repository.WorkUnitGFRepository;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.MdaTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SpecialPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.UnavailabilityTypeCacheService;
import gov.nyc.dsny.smart.opsboard.controllers.PersonController;
import gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.LocationRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.personnel.PersonnelPersistenceService;
import gov.nyc.dsny.smart.opsboard.services.ReferenceDataService;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.PersonExecutor;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;


@Configuration
public abstract class OBUnitAbstractConfig {

	@Bean
	public PersonController personController(){
		return new PersonController();
	}
	
	@Bean
	public LocationCache locationCache(){
		return Mockito.mock(LocationCache.class);
	}
	
	@Bean
	public ShiftCacheService shiftCacheService(){
		return Mockito.mock(ShiftCacheService.class);
	}
	
	@Bean
	public ReferenceDataService referenceDataService(){
		return Mockito.mock(ReferenceDataService.class);
	}
	
	@Bean
	public BoardCacheService boardCacheService(){
		return Mockito.mock(BoardCacheService.class);
	}

	@Bean
	public BoardPersonnelCacheService boardPersonnelCacheService(){
		return Mockito.mock(BoardPersonnelCacheService.class);
	}
	
	@Bean
	public PersonExecutor personExecutor(){
		return Mockito.mock(PersonExecutor.class);
	}
	
	@Bean
	public BoardKeyFactory boardKeyFactory(){
		return Mockito.mock(BoardKeyFactory.class);
	}
	
	@Bean
	public SimpMessagingTemplate simpMessagingTemplate(){
		return Mockito.mock(SimpMessagingTemplate.class);
	}

	@Bean
	public MdaTypeCacheService mdaTypeCacheService(){
		return Mockito.mock(MdaTypeCacheService.class);
	}
	
	@Bean
	public OutgoingPersonnelService outgoingPersonnelService(){
		return Mockito.mock(OutgoingPersonnelService.class);
	}
	
	@Bean
	public PersonnelPersistenceService personnelPersistenceService(){
		return Mockito.mock(PersonnelPersistenceService.class);
	}
	

	@Bean
	public PersonnelCacheService personnelCacheService(){
		return Mockito.mock(PersonnelCacheService.class);
	}

	@Bean
	public SpecialPositionTypeCacheService specialPositionTypeCacheService(){
		return Mockito.mock(SpecialPositionTypeCacheService.class);
	}

	@Bean
	public UnavailabilityTypeCacheService unavailabilityTypeCacheService(){
		return Mockito.mock(UnavailabilityTypeCacheService.class);
	}
	
	//********************** Repositories ************************************
	@Bean
	public LocationGFRepository locationGFRepository(){
		return Mockito.mock(LocationGFRepository.class);
	}
	
	@Bean
	public WorkUnitGFRepository workUnitGFRepository(){
		return Mockito.mock(WorkUnitGFRepository.class);
	}
	
	@Bean
	public RefDataTrackingGFRepository refDataTrackingGFRepository(){
		return Mockito.mock(RefDataTrackingGFRepository.class);
	}
	
	@Bean
	public LocationRepository locationRepository(){
		return Mockito.mock(LocationRepository.class);
	}
	
	
	@Bean
	public BoardEquipmentGFRepository boardEquipmentGFRepository(){
		return Mockito.mock(BoardEquipmentGFRepository.class);
	}

	@Bean
	public PersonnelRepository personnelRepository(){
		return Mockito.mock(PersonnelRepository.class);
	}
	
	

	
}

package gov.nyc.dsny.smart.opsboard.misc;

import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.EquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.PersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.persistence.services.personnel.PersonnelPersistenceService;
import gov.nyc.dsny.smart.opsboard.services.LocationService;
import gov.nyc.dsny.smart.opsboard.services.PersistanceService;
import gov.nyc.dsny.smart.opsboard.services.UserNotificationsService;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ListenerAspect {

	@Autowired
	private BoardCacheService boardsCache;
	
	@Autowired
	private UserNotificationsService userNotificationsService;
	
	@Autowired
	private LocationService locationService;
	
	@Autowired
	private PersistanceService persistanceService;
	
	@Autowired
	private PersonnelPersistenceService personnelPersistenceService;
	
	@Autowired
	private EquipmentPersistenceService equipmentPersistenceService;
	
	@Autowired
	private PersonnelCacheService personnelCacheService;
	
	@Autowired
	private EquipmentCacheService equipmentCacheService;
	
	@Autowired
	private BoardEquipmentCacheService boardEquipmentCacheService;
	
	@Autowired
	private BoardPersonnelCacheService boardPersonnelCacheService;
	
	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	@SuppressWarnings("rawtypes")
	@Around("execution(* gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService.get(gov.nyc.dsny.smart.opsboard.domain.board.BoardKey,..))")
	public BoardContainer postProcess(ProceedingJoinPoint pjp) throws Throwable{
		BoardKey boardKey = (BoardKey) pjp.getArgs()[0];
		int pIndex = -1;
		final Signature signature = pjp.getStaticPart().getSignature();
		    if(signature instanceof MethodSignature){
		        final MethodSignature ms = (MethodSignature) signature;
		        final List parameterTypes = Stream.of(ms.getParameterTypes()).collect(Collectors.toList());
		        if(parameterTypes.contains(Principal.class))
		        	pIndex = parameterTypes.indexOf(Principal.class);
		   }
				
		if(pIndex >= 0)
			if(pjp.getArgs()[pIndex] != null)
				userNotificationsService.createMessageConsumerOnUser( (Principal) pjp.getArgs()[pIndex], boardKey);
		
		resyncCachesDuringQueueCreation(boardKey);
		BoardContainer bc = (BoardContainer) pjp.proceed();
		
		persistanceService.createMessageConsumerOnBoard(boardKey);
		locationService.createMessageConsumerOnLocation(boardKey.getLocation());

		
		return bc;
	}

	private void resyncCachesDuringQueueCreation(BoardKey boardKey) {
		String queueName = locationService.getQueueName(boardKey.getLocation().getCode());
		if(locationService.getListenersMap().containsKey(queueName) && !locationService.isQueueExist(queueName)){
	
			List<Long> locationIds = locationCache.getLocationIdsByBoardKey(boardKey);
			Map<String, Equipment> equipments =  equipmentPersistenceService.findEquipmentByBoard(boardKey, locationIds).stream().collect(Collectors.toMap(e -> e.getId(), e -> e));			
			
			List<Equipment> cachedEquipments = equipmentCacheService.get(new ArrayList<>(equipments.keySet()));
			for(Equipment cachedEquipment : cachedEquipments){
				BeanUtils.copyProperties(equipments.get(cachedEquipment.getId()), cachedEquipment);
			}
			
			Map<String, Person> persons = personnelPersistenceService.findPersonsByBoard(boardKey, locationIds).stream().collect(Collectors.toMap(p -> p.getId(),  p -> p));
			List<Person> cachedPersons = personnelCacheService.get(new ArrayList<>(persons.keySet()));
			
			for(Person cachedPerson : cachedPersons){
				BeanUtils.copyProperties(persons.get(cachedPerson.getId()), cachedPerson);
			}
			
			boardEquipmentCacheService.clearKey(boardKey);
			boardPersonnelCacheService.clearKey(boardKey);
			boardsCache.clear(boardKey.toId());
		}
	}
	
	@Around("execution(* gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService.cloneToDate(gov.nyc.dsny.smart.opsboard.domain.board.BoardKey, java.lang.String))")
	public Boolean postProcessClone(ProceedingJoinPoint pjp) throws Throwable{
		BoardKey sourceKey = (BoardKey) pjp.getArgs()[0];
		String futureDate = (String) pjp.getArgs()[1];
		BoardKey targetKey = boardKeyFactory.createBoardKey(futureDate, sourceKey.getLocation());

		resyncCachesDuringQueueCreation(targetKey);
		Boolean exists = (Boolean) pjp.proceed();
		
		persistanceService.createMessageConsumerOnBoard(targetKey);
		locationService.createMessageConsumerOnLocation(targetKey.getLocation());

		
		return exists;
	}
	
}

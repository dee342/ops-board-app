package gov.nyc.dsny.smart.opsboard.services.integration;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.OfficerPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.UnavailabilityTypeCacheService;
import gov.nyc.dsny.smart.opsboard.domain.User;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.GroundingStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.integration.exception.ReconciliationIntegrationException;
import gov.nyc.dsny.smart.opsboard.integration.mapper.PersonEntityMapper;
import gov.nyc.dsny.smart.opsboard.integration.models.ps.PersonGroundModel;
import gov.nyc.dsny.smart.opsboard.integration.models.ps.PersonModel;
import gov.nyc.dsny.smart.opsboard.integration.models.ps.PersonSpecialPositionModel;
import gov.nyc.dsny.smart.opsboard.integration.models.ps.PersonUnavailabilityModel;
import gov.nyc.dsny.smart.opsboard.integration.service.IncomingPersonnelService;
import gov.nyc.dsny.smart.opsboard.integration.service.IntegrationErrorHandlingService;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.SpecialPositionRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.UnavailabilityReasonRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.personnel.PersonnelPersistenceService;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.PersonExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("incomingPersonnelService")
public class IncomingPersonnelServiceImpl implements IncomingPersonnelService {

	private static final Logger logger = LoggerFactory.getLogger(IncomingPersonnelServiceImpl.class);
	
	
	@Autowired
	private IntegrationErrorHandlingService integrationErrorHandlingService;
	
	@Autowired
	private PersonnelRepository personnelRepository;
	
	@Autowired
	private SpecialPositionRepository specialPositionRepository;
	
	@Autowired
	private UnavailabilityReasonRepository unavailabilityReasonRepository;
		
	@Autowired
	private PersonEntityMapper personEntityMapper;
	
	@Autowired
	private PersonnelPersistenceService persistenceService;

	@Autowired
	private PersonExecutor personExecutor;

	@Autowired
	private OfficerPositionTypeCacheService officerPositionTypeCacheService;
	
	@Autowired
	private UnavailabilityTypeCacheService unavailabilityTypeCacheService;
	
	@Autowired
	private ShiftCacheService shiftsCache;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	@Override
	public Void setPerson(final PersonModel model) throws OpsBoardError 
	{
		try
		{
			logger.debug("IncomingPersonnelServiceImplsetPersonStart {}", model.getEmployeeId());
			validate(model, PersonModel.class.getName());
			Person existingPerson = persistenceService.findPersonById(model.getEmployeeId());
					
			Person person = personEntityMapper.convertPersonModelToEntity(model, existingPerson);
			Location location = person.getHomeLocation();
			BoardKey boardKey = boardKeyFactory.createBoardKey (model.getTimestamp(), location);
			personExecutor.setPerson(boardKey, person, User.getPrincipal(model.getUser()));
			logger.debug("IncomingPersonnelServiceImplsetPersonEnd {}", model.getEmployeeId());
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"setPerson", model.toString()));	
		}
		return null;
	}
	
	@Override
	public Void groundPerson(final PersonGroundModel model) throws OpsBoardError 
	{
		try
		{
			logger.debug("IncomingPersonnelServiceImplgroundPersonStart {}", model.getEmployeeId());
			validate(model, PersonGroundModel.class.getName());
			Person dbPerson = persistenceService.findPersonById(model.getEmployeeId());
			
			if(dbPerson == null)
			{
				throw new ReconciliationIntegrationException(ErrorMessage.PERSON_NOT_FOUND, null, 
						"groundPerson", model.toString());
			}
			
			GroundingStatus groundingStatus = 
				personEntityMapper.convertGroundingModelToEntity(model, dbPerson);
			
			Date startDate = shiftsCache.createShiftStart(model.getTimestamp());
			Date endDate = shiftsCache.createShiftEnd(model.getTimestamp());
			String boardDate = DateUtils.toStringBoardDate(model.getTimestamp());
			
			BoardPerson temp = new BoardPerson(dbPerson, boardDate, startDate, endDate);
			
			Location location = temp.getWorkLocation(model.getTimestamp(), startDate, endDate);
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (model.getTimestamp(), location);	
			
			BoardPerson bp = persistenceService.findOrCreateById( BoardPerson.CREATE_ID(model.getEmployeeId(),
					boardDate), boardKey, dbPerson);
			
			personExecutor.setPersonGrounding(boardKey, bp, groundingStatus, User.getPrincipal(model.getUser()));
			logger.debug("IncomingPersonnelServiceImplgroundPersonEnd {}", model.getEmployeeId());
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"groundPerson", model.toString()));	
		}
		
		return null;
	}
	
	@Override
	public Void setPersonUnavailability (PersonUnavailabilityModel model)throws OpsBoardError, OpsBoardValidationException
	{
		try
		{
			logger.debug("IncomingPersonnelServiceImplsetPersonUnavailabilityStart {}", model.getEmployeeId());
			validate(model, PersonUnavailabilityModel.class.getName());
			Person dbPerson = persistenceService.findPersonById(model.getEmployeeId());
			
			if(dbPerson == null)
			{
				throw new ReconciliationIntegrationException(ErrorMessage.PERSON_NOT_FOUND, null, 
						"setPersonUnavailability", model.toString());
			}
				
			Date startDate = shiftsCache.createShiftStart(model.getTimestamp());
			Date endDate = shiftsCache.createShiftEnd(model.getTimestamp());
			String boardDate = DateUtils.toStringBoardDate(model.getTimestamp());
			
			BoardPerson temp = new BoardPerson(dbPerson, boardDate, startDate, endDate);
			
			Location location = temp.getWorkLocation(model.getTimestamp(), startDate, endDate);
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (model.getTimestamp(), location);	
			
			BoardPerson bp = persistenceService.findOrCreateById( BoardPerson.CREATE_ID(model.getEmployeeId(),
					boardDate), boardKey, dbPerson);
			
			UnavailabilityReason newUnavailabilityReason = personEntityMapper.convertUnavailabilityModelToEntity(model);
			List<UnavailabilityReason> existingUnavailabilityReasons = unavailabilityReasonRepository.findByPeopleSoftId(newUnavailabilityReason.getPeopleSoftId());
			if (model.isRemovedFlag())
			{
				if (existingUnavailabilityReasons != null && existingUnavailabilityReasons.size() > 0)
				{
					// Removing existing officer position
					UnavailabilityReason ur = existingUnavailabilityReasons.get(0);
					ur.setReasonForChange(newUnavailabilityReason.getReasonForChange());
					personExecutor.removeUnavailabilityReason(boardKey, bp, ur,User.getPrincipal(model.getUser()), true);
				}
				else
				{
					throw new ReconciliationIntegrationException(ErrorMessage.INT_UNAVAILABILITY_REASON_NOT_FOUND, 
							null, "setPersonUnavailability", model.toString());
				}
			}
			else
			{
				if (existingUnavailabilityReasons == null || existingUnavailabilityReasons.size() == 0)
				{
					// Adding new officer position
					personExecutor.addUnavailabilityReason(boardKey, bp, newUnavailabilityReason,User.getPrincipal(model.getUser()), true);
				}
				else
				{
					// Update existing officer position
					UnavailabilityReason ur = existingUnavailabilityReasons.get(0);
					ur.setAction(model.getAction());
					ur.setStart(DateUtils.removeTime(model.getStartDateTime()));
					ur.setEnd(DateUtils.getDateWithEndTime(model.getStartDateTime()));
					ur.setReasonForChange(model.getReasonforChange());
					ur.setComments(model.getRemarks());
					ur.setActualUser(model.getUser());
					ur.setCode(model.getUnavailableCode());
					ur.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(model.getUnavailableCode(), model.getTimestamp()));
					personExecutor.updateUnavailabilityReason(boardKey, bp, ur ,User.getPrincipal(model.getUser()), true);
				}
			}
			logger.debug("IncomingPersonnelServiceImplsetPersonUnavailabilityEnd {}", model.getEmployeeId());
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"setPersonUnavailability", model.toString()));	
		}
		
		return null;
	}


	@Override
	public Void setPersonOfficerPosition(PersonSpecialPositionModel model) throws OpsBoardError, OpsBoardValidationException
	{
		try
		{
			logger.debug("IncomingPersonnelServiceImplsetPersonOfficerPositionStart {}", model.getEmployeeId());
			validate(model, PersonUnavailabilityModel.class.getName());
			Person dbPerson = persistenceService.findPersonById(model.getEmployeeId());
			
			if(dbPerson == null)
			{
				throw new ReconciliationIntegrationException(ErrorMessage.PERSON_NOT_FOUND, null, 
						"setPersonOfficerPosition", model.toString());
			}
			
			SpecialPosition newSpecialPosition = 
				personEntityMapper.convertSpecialPositionModelToEntity(model, dbPerson);
			
			List<SpecialPosition> oldSpecialPositions = specialPositionRepository.findByPeopleSoftId(newSpecialPosition.getPeopleSoftId());
			if (oldSpecialPositions != null && oldSpecialPositions.size() > 0)
			{
				SpecialPosition oldSpecialPosition = oldSpecialPositions.stream().filter(s -> "A".equals(s.getStatus())).findFirst().get(); 
				if (oldSpecialPosition != null)
				{
					newSpecialPosition.setId(oldSpecialPosition.getId());
				}
			}
			
			Date startDate = shiftsCache.createShiftStart(model.getTimestamp());
			Date endDate = shiftsCache.createShiftEnd(model.getTimestamp());
			String boardDate = DateUtils.toStringBoardDate(model.getTimestamp());
			
			BoardPerson temp = new BoardPerson(dbPerson, boardDate, startDate, endDate);
			
			Location location = temp.getWorkLocation(model.getTimestamp(), startDate, endDate);
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (model.getTimestamp(), location);	
			
			BoardPerson bp = persistenceService.findOrCreateById( BoardPerson.CREATE_ID(model.getEmployeeId(),
					boardDate), boardKey, dbPerson);
			
			if (model.isRemovedFlag())
			{
				if (oldSpecialPositions != null && oldSpecialPositions.size() > 0)
				{
					// Removing existing officer position
					SpecialPosition sp = oldSpecialPositions.get(0);
					sp.setReasonForChange(newSpecialPosition.getReasonForChange());
					personExecutor.removeSpecialPosition(boardKey, bp, sp, User.getPrincipal(model.getUser()), true); 
				}
				else
				{
					throw new ReconciliationIntegrationException(ErrorMessage.INT_OFFICER_POSITION_NOT_FOUND, 
							null, "setPersonOfficerPosition", model.toString());
				}
			}
			else
			{
				if (oldSpecialPositions == null || oldSpecialPositions.size() == 0)
				{		
					// Making current office position ineffective
					SpecialPosition currentlyActiveSpecialPosition = specialPositionRepository.findActiveByPersonId(newSpecialPosition.getPerson().getId(), newSpecialPosition.getStartDate());
					//SpecialPosition currentlyActiveSpecialPosition = null;
					if (currentlyActiveSpecialPosition != null)
					{
						//currentlyActiveSpecialPosition = currentlyActiveSpecialPositions.get(0);
						Calendar c = Calendar.getInstance();
						c.setTime(newSpecialPosition.getStartDate());
						c.add(Calendar.DATE, -1);
						
						currentlyActiveSpecialPosition.setEndDate(DateUtils.getDateWithEndTime(c.getTime()));
						newSpecialPosition.setStartDate(DateUtils.removeTime(newSpecialPosition.getStartDate()));
						personExecutor.updateSpecialPosition(boardKey, bp, currentlyActiveSpecialPosition,User.getPrincipal(model.getUser()), true);
					}
					
					// Adding new officer position
					personExecutor.addSpecialPosition(boardKey, bp, newSpecialPosition,User.getPrincipal(model.getUser()), true);
				}
				else
				{
					// Update existing officer position
					SpecialPosition sp = oldSpecialPositions.get(0);
					sp.setStartDate(model.getStartDateTime());
					sp.setEndDate(model.getEndDateTime());
					sp.setReasonForChange(model.getReasonForChange());
					sp.setComments(model.getRemarks());
					sp.setActualUser(model.getUser());
					sp.setCode(model.getSubTypeCode());
					sp.setOfficerPositionType(officerPositionTypeCacheService.getOfficerPositionType(model.getSubTypeCode(), model.getTimestamp()));
					personExecutor.updateSpecialPosition(boardKey, bp, sp,User.getPrincipal(model.getUser()), true);
				}
			}
			logger.debug("IncomingPersonnelServiceImplsetPersonOfficerPositionEnd {}", model.getEmployeeId());
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"setPersonOfficerPosition", model.toString()));	
		}
		
		return null;
	}

}

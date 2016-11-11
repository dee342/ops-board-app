package gov.nyc.dsny.smart.opsboard.services.integration;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.integration.facade.OutgoingIntegrationFacade;
import gov.nyc.dsny.smart.opsboard.integration.mapper.PersonEntityMapper;
import gov.nyc.dsny.smart.opsboard.integration.service.IntegrationErrorHandlingService;
import gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService;

import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("outgoingPersonnelService")
public class OutgoingPersonnelServiceImpl implements OutgoingPersonnelService
{

//	private static final Logger logger = LoggerFactory.getLogger(OutgoingPersonnelServiceImpl.class);
	
	@Autowired
	private OutgoingIntegrationFacade outgoingIntegrationFacade;
	
	@Autowired
    private PersonEntityMapper personMapper;
	
	@Autowired
	private IntegrationErrorHandlingService integrationErrorHandlingService;
	
	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService#detachPerson(gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment)
	 */
	@Async
	@Override
	public Future<Void> detachPerson(Detachment detachment) {
		try
		{
			outgoingIntegrationFacade.detachPerson(personMapper.convertEntityToDetachmentModel(detachment));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_PERSON_DETACH, t, "detachPerson", detachment.toString());
		}
		return null;
	}
	
	
	@Async
	@Override
	public Future<Void> removePersonDetachment(Detachment detachment) 
	{
		try
		{
			outgoingIntegrationFacade.detachPerson(personMapper.convertEntityToDetachmentModel(detachment));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_REMOVE_PERSON_DETACH, t, "removePersonDetachment", detachment.toString());
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService#setPersonUnavailable(gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason)
	 */
	@Override
	@Async
	public Future<Void> setPersonUnavailable(UnavailabilityReason unavailabilityReason, boolean isRemoved) {
		try
		{
			outgoingIntegrationFacade.setPersonUnavailable(personMapper.convertEntityToUnavailabilityModel(unavailabilityReason, isRemoved));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_SET_PERSON_UNAVAIL, t, "setPersonUnavailable", unavailabilityReason.toString());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService#setPersonMDA(gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus)
	 */
	@Override
	@Async
	public Future<Void> setPersonMDA(MdaStatus mdaStatus, boolean isRemoved) {
		try
		{
			outgoingIntegrationFacade.setPersonMDA(personMapper.convertEntityToMdaModel(mdaStatus, isRemoved));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_SET_PERSON_MDA, t, "setPersonMDA", mdaStatus.toString());
		}
		return null;
		
	}

	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService#cancelPersonUnavailableReason(gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason)
	 */
	@Override
	@Async
	public Future<Void> cancelPersonUnavailableReason(UnavailabilityReason cancelUnavailabilityReason) {
		try
		{
			outgoingIntegrationFacade.cancelPersonUnavailableReason(personMapper.convertEntityToUnavailabilityModel(cancelUnavailabilityReason, false));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_CANCEL_PERSON_UNAVAIL, t, "cancelPersonUnavailableReason", cancelUnavailabilityReason.toString());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService#setPersonSpecialPosition(gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition)
	 */
	@Override
	@Async
	public Future<Void> setPersonSpecialPosition(SpecialPosition specialPosition, boolean isRemoved) {
		try
		{
			outgoingIntegrationFacade.setPersonSpecialPosition(personMapper.convertEntityToSpecialPositionModel( specialPosition,  isRemoved));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_SET_PERSON_SPECIAL_POS, t, "setPersonSpecialPosition", specialPosition.toString());
		}
		return null;
	}


	@Override
	@Async
	public Future<Void> reverseCancelPersonUnavailability(UnavailabilityReason reverseCancelUnavailabilityReason) {
		try{
			outgoingIntegrationFacade.reverseCancelUnavailability(personMapper.convertEntityToUnavailabilityModel(reverseCancelUnavailabilityReason, false));
		}catch(Throwable t){
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_REVERSE_CANCEL_PERSON_UNAVAIL, t, "reverseCancelPersonUnavailability", reverseCancelUnavailabilityReason.toString());
		}
		return null;
	}


	@Override
	public Future<Void> updatePersonDetachment(Detachment persistedDetachment) {
		try
		{
			//need to add logic change
			outgoingIntegrationFacade.detachPerson(personMapper.convertEntityToDetachmentModel(persistedDetachment));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_PS_UPDATE_PERSON_DETACH, t, "removePersonDetachment", persistedDetachment.toString());
		}
		return null;
	}
}

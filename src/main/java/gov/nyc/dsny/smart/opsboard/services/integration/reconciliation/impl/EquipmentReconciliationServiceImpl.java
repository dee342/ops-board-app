package gov.nyc.dsny.smart.opsboard.services.integration.reconciliation.impl;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.equipment.DownCodeCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentState;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.integration.exception.ReconciliationIntegrationException;
import gov.nyc.dsny.smart.opsboard.integration.service.impl.IntegrationErrorHandlingServiceImpl;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.services.integration.reconciliation.EquipmentReconciliationService;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.EquipmentExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.security.Principal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EquipmentReconciliationServiceImpl implements EquipmentReconciliationService
{
	private static final Logger logger = LoggerFactory.getLogger(EquipmentReconciliationServiceImpl.class);

	private final String SYSTEM_USER = "system"; 
	
	@Autowired
	private EquipmentExecutor executor;
	
	@Autowired
	private DownCodeCache downCodeCache;
	
	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private IntegrationErrorHandlingServiceImpl integrationErrorHandlingServiceImpl;
	
	@Autowired
	private EquipmentPersistenceService persistenceService;
	
	//******************************************************************************************
	//********************** Detachments *******************************************************
	//******************************************************************************************
	@Override
	public void reconcileDetachment(BoardKey key, BoardEquipment be,
			Detachment detach,
			Principal principal, OpsBoardError error) throws OpsBoardError
	{
		
		switch(ErrorMessage.findByCode(error.getCode()))
		{
			case SCAN_EQUIPMENT_ALREADY_PENDING_DETACH:
				ReconciliationIntegrationException reconciliationIntegrationException = new ReconciliationIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_PENDING_DETACH, error);
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_PENDING_DETACH, reconciliationIntegrationException, 
						detach.getFrom().getCode(), detach.getTo().getCode(), detach.getDriver(), detach.getLastModifiedActual().toString(), detach.getComments(), principal.getName());
				reconcileDetachmentAlreadyPending(key, be, detach, principal);
				break;
			case SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION:
				ReconciliationIntegrationException reconciliationIntegrationException1 = new ReconciliationIntegrationException(ErrorMessage.SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION, error);
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION, reconciliationIntegrationException1, 
						be.getEquipment().getId(), detach.getFrom().getCode(), detach.getTo().getCode(), detach.getDriver(), detach.getLastModifiedActual().toString(), detach.getComments(), principal.getName());
				reconcileDetachmentWithDifferentLocation(key, be, be.getMostRecentDetachment().getTo(), detach, principal);
				break;
			default: 
				throw error;
		}
	}
	
	private void reconcileDetachmentAlreadyPending(BoardKey key, BoardEquipment be, Detachment detach, Principal principal) throws OpsBoardError
	{
		Detachment current = persistenceService.findLatestDetachmentByEquipmentId(be.getEquipment().getId());
		
		Detachment cancel = new Detachment(detach.getActualUser(), detach.getSystemUser(), 
				detach.getLastModifiedActual(), detach.getLastModifiedSystem(), be.getEquipment(), detach.getComments(),
				null, current.getFrom(), DetachmentState.CANCELLED.getCode(), current.getTo());

		executor.cancelDetach(key, be, cancel, principal, true);
		detach(key, be, detach, principal);
	}

	private void reconcileDetachmentWithDifferentLocation(BoardKey key, BoardEquipment be, Location currentLocation, Detachment detach, Principal principal) throws OpsBoardError 
	{	

		Detachment fromOld = new Detachment(detach.getActualUser(), SYSTEM_USER, detach.getLastModifiedActual(),
				detach.getLastModifiedSystem(), be.getEquipment(), "Reconciliation", null, currentLocation, DetachmentState.PENDING.getCode(), detach.getFrom());
		
		detach(key, be, fromOld, principal);		
		attach(key, be, fromOld, principal);
		detach(key, be, detach, principal);
	}
	
	//******************************************************************************************
	//********************** Attachments *******************************************************
	//******************************************************************************************
	@Override
	public void reconcileAttachment(BoardKey key, BoardEquipment be, Detachment attach,
			Principal principal, OpsBoardError error) throws OpsBoardError
	{
		

		switch(ErrorMessage.findByCode(error.getCode()))
		{
			case SCAN_EQUIPMENT_ALREADY_ATTACHED:
				ReconciliationIntegrationException reconciliationIntegrationException = new ReconciliationIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_ATTACHED, error);
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_ATTACHED, reconciliationIntegrationException, be.getEquipment().getId(), attach.getDriver(), attach.getLastModifiedActual().toString(), attach.getComments(), principal.getName());
				reconcileAttachmentAlreadyAttached(key, be, getCurrentLocation(key, be), attach, principal);
				break;
			case SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION:
				ReconciliationIntegrationException reconciliationIntegrationException1 = new ReconciliationIntegrationException(ErrorMessage.SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION, error);
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION, reconciliationIntegrationException1,  be.getEquipment().getId(),  attach.getDriver(), attach.getLastModifiedActual().toString(), attach.getComments(),principal.getName());
				reconcileAttachmentWithDifferentLocation(key, be, getCurrentLocation(key, be), attach, principal);
				break;	
			case INT_ATTACHMENT_WITHOUT_DETACHMENT:
				ReconciliationIntegrationException reconciliationIntegrationException2 = new ReconciliationIntegrationException(ErrorMessage.INT_ATTACHMENT_WITHOUT_DETACHMENT, error);
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.INT_ATTACHMENT_WITHOUT_DETACHMENT, reconciliationIntegrationException2,  be.getEquipment().getId(), attach.getDriver(), attach.getLastModifiedActual().toString(), attach.getComments(), principal.getName());
				reconcileAttachmentWithoutDetachment(key, be, attach, principal);
				break;	
			
			default: 
				throw error;
		}
	}

	private void reconcileAttachmentAlreadyAttached(BoardKey key, BoardEquipment be, Location currentLocation, Detachment attach, Principal principal) throws OpsBoardError 
	{			
		Detachment fromOld = new Detachment(attach.getActualUser(), SYSTEM_USER, attach.getLastModifiedActual(),
				attach.getLastModifiedSystem(), be.getEquipment(), attach.getComments(), attach.getDriver(), currentLocation, DetachmentState.PENDING.getCode(), attach.getFrom());

		detach(key, be, fromOld, principal);
		attach(key, be, attach, principal);
	}
	
	private void reconcileAttachmentWithDifferentLocation(BoardKey key, BoardEquipment be, Location currentLocation, Detachment detach, Principal principal) throws OpsBoardError 
	{	
		Detachment current = persistenceService.findLatestDetachmentByEquipmentId(be.getEquipment().getId());
		
		Detachment cancel = new Detachment(detach.getActualUser(), detach.getSystemUser(), 
				detach.getLastModifiedActual(), detach.getLastModifiedSystem(), be.getEquipment(), detach.getComments(),
				null, current.getFrom(), DetachmentState.CANCELLED.getCode(), current.getTo());

		executor.cancelDetach(key, be, cancel, principal, true);		
		if (!currentLocation.isTheSameLocation(key.getLocation().getCode()))
		{
			Detachment fromOld = new Detachment(detach.getActualUser(), SYSTEM_USER, detach.getLastModifiedActual(),
					detach.getLastModifiedSystem(), be.getEquipment(), detach.getComments(), detach.getDriver(), currentLocation, DetachmentState.PENDING.getCode(), detach.getFrom());

			detach(key, be, fromOld, principal);
			attach(key, be, detach, principal);
		}

	}
	
	private void reconcileAttachmentWithoutDetachment(BoardKey key, BoardEquipment be, Detachment attach, Principal principal) throws OpsBoardError 
	{				
		detach(key, be, attach, principal);
		attach(key, be, attach, principal);
	}
	
	//******************************************************************************************
	//********************** Downs *************************************************************
	//******************************************************************************************
	@Override
	public void reconcileDown(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal, OpsBoardError error) throws OpsBoardError
	{
		switch(ErrorMessage.findByCode(error.getCode()))
		{
			case SCAN_EQUIPMENT_ALREADY_DOWN:
				ReconciliationIntegrationException reconciliationIntegrationException = new ReconciliationIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_DOWN, error);
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_DOWN, reconciliationIntegrationException, be.getEquipment().getId(), upDown.toString());
				reconcileDownAlreadyDown(key, be, upDown, principal);
				break;
			default: 
				throw error;
		}
	}
	
	private void reconcileDownAlreadyDown(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal) throws OpsBoardError
	{
		//EquipmentCondition euipmentCondition = upDown.getConditions().iterator().next();
		up(key, be, upDown, principal);
		down(key, be, upDown, principal);
	}
	
	@Override
	public void reconcileUpdateDown(BoardKey key, BoardEquipment be, UpDown newUpDown, UpDown oldUpDown, Principal principal, 
			OpsBoardError error) throws OpsBoardError
	{
		switch(ErrorMessage.findByCode(error.getCode()))
		{
			case SCAN_EQUIPMENT_IS_NOT_DOWN:
				ReconciliationIntegrationException reconciliationIntegrationException = new ReconciliationIntegrationException(ErrorMessage.SCAN_EQUIPMENT_IS_NOT_DOWN, error);
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.SCAN_EQUIPMENT_IS_NOT_DOWN, reconciliationIntegrationException, be.getEquipment().getId(), newUpDown.toString());
				reconcileUpdateDownIsNotDown(key, be, newUpDown, oldUpDown, principal);
				break;
			default: 
				throw error;
		}
	}
	
	private void reconcileUpdateDownIsNotDown(BoardKey key, BoardEquipment be, UpDown newUpDown, UpDown oldUpDown, Principal principal) throws OpsBoardError
	{
		down(key, be, newUpDown, principal);
	}
	
	//******************************************************************************************
	//********************** UPs ***************************************************************
	//******************************************************************************************
	@Override
	public void reconcileUp(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal, OpsBoardError error) throws OpsBoardError
	{
		switch(ErrorMessage.findByCode(error.getCode()))
		{
			case SCAN_EQUIPMENT_ALREADY_UP:
				ReconciliationIntegrationException reconciliationIntegrationException = new ReconciliationIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_UP, error);
				EquipmentCondition euipmentCondition = upDown.getConditions().iterator().next();
				integrationErrorHandlingServiceImpl.handleIntegrationException(ErrorMessage.SCAN_EQUIPMENT_ALREADY_UP, reconciliationIntegrationException, 
					be.getEquipment().getId(),  euipmentCondition.getMechanic(), euipmentCondition.getActualUser(), euipmentCondition.getLastModifiedSystem().toString(), principal.getName());
				reconcileUpAlreadyUp(key, be, upDown, principal);
				break;
			default: 
				throw error;
		}
	}
	
	private void reconcileUpAlreadyUp(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal) throws OpsBoardError
	{
		Date date= DateUtils.toBoardDateNoNull(key.getDate());
		String garageCode = locationCache.getGarageByLocation(key.getLocation().getCode(), date);
		Set<EquipmentCondition> equipmentConditions = new HashSet<EquipmentCondition>();
		EquipmentCondition euipmentCondition1 = upDown.getConditions().iterator().next();
		EquipmentCondition equipmentCondition = new EquipmentCondition(garageCode, SYSTEM_USER, new Date(), new Date(), null, 
				"Reconciliation...", true, downCodeCache.getDownCodes(date).get(0), euipmentCondition1.getMechanic(), "SCAN", null);
		equipmentConditions.add(equipmentCondition);
		
		UpDown down = new UpDown(true, equipmentConditions);
		equipmentCondition.setUpDown(down);
		
		down(key, be, down, principal);
		up(key, be, upDown, principal);
	}
	//******************************************************************************************
	//********************** Root private methods *******************************************************
	//******************************************************************************************

	private void detach(BoardKey key, BoardEquipment be, Detachment detach, Principal principal) throws OpsBoardError
	{
		be.setEquipment(persistenceService.findEquipmentById(be.getEquipment().getId()));
		executor.detach(key, be, detach, principal, true);
		return;
	}
	
	private void attach(BoardKey key, BoardEquipment be, Detachment attach, Principal principal) 
			throws OpsBoardError
	{
		be.setEquipment(persistenceService.findEquipmentById(be.getEquipment().getId()));	
		executor.attach(key, be, attach, principal, true);
		return;
	}
	
	private void down(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal) throws OpsBoardError
	{
		be.setEquipment(persistenceService.findEquipmentById(be.getEquipment().getId()));
		executor.down(key, be, upDown, principal, true);
		return;
	}
	
	private void up(BoardKey key, BoardEquipment be, UpDown upDown, Principal principal)  throws OpsBoardError
	{
		be.setEquipment(persistenceService.findEquipmentById(be.getEquipment().getId()));
		executor.up(key, be, upDown, principal, true);
		return;
	}
	
	private Location getCurrentLocation (BoardKey key, BoardEquipment be)
	{
		StateAndLocation sal = be.getState(key.getLocation());
		Location currentLocation = sal.getLocation();
		return currentLocation;
	}
	
	@Autowired
	@Qualifier("mvcConversionService")
	private ConversionService conversionService;

}

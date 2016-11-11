package gov.nyc.dsny.smart.opsboard.services.integration;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.equipment.SubTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SeriesCacheService;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.domain.User;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentState;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Series;
import gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType.Load;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType.PlowType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.integration.exception.ReconciliationIntegrationException;
import gov.nyc.dsny.smart.opsboard.integration.mapper.EquipmentEntityMapper;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentAction;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentAttachmentDetachment;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentConditionModel;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentLoadSalt;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentModel;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentSnowDetail;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.UpDownModel;
import gov.nyc.dsny.smart.opsboard.integration.service.IncomingEquipmentService;
import gov.nyc.dsny.smart.opsboard.integration.service.IntegrationErrorHandlingService;
import gov.nyc.dsny.smart.opsboard.integration.util.ScanUtil;
import gov.nyc.dsny.smart.opsboard.misc.AdminCommandMessage;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.BinRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.EquipmentRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.SeriesRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.services.executors.AdminExecutor;
import gov.nyc.dsny.smart.opsboard.services.integration.reconciliation.EquipmentReconciliationService;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.EquipmentExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service("incomingEquipmentService")
public class IncomingEquipmentServiceImpl implements IncomingEquipmentService 
{
	
	private static final Logger logger = LoggerFactory.getLogger(IncomingEquipmentServiceImpl.class);
	
	private static final String REMARKS = "Reconciliation process";

	@Autowired
	private EquipmentExecutor equipmentExecutor;
	
	@Autowired
	private EquipmentEntityMapper equipmentEntityMapper;
	
	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private SubTypeCache subTypeCache;
	
	@Autowired
	private SeriesCacheService seriesCacheService;
	
	@Autowired
	private EquipmentReconciliationService equipmentReconciliationService;

	@Autowired
	private AdminExecutor adminExecutor;
	
	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private BinRepository binRepository;
	
	@Autowired
	private EquipmentRepository equipmentRepository;
	
	@Autowired
	private IntegrationErrorHandlingService integrationErrorHandlingService;
	
	@Autowired
	private SimpMessagingTemplate messenger;
	
	@Autowired
	private EquipmentPersistenceService persistenceService;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	
	@Override
	public Void detachEquipment(EquipmentAttachmentDetachment detachmentModel) throws OpsBoardError
	{
		try
		{
			if (StringUtils.equals(detachmentModel.getCurrentLocation(), detachmentModel.getTo()))
			{
				// Log in reconciliation exception
				integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_DETACH_TO_ITSELF, null, "detachEquipment", detachmentModel.toString());
				return null;
			}
			
			// Ignore transfers for now
			if (ScanUtil.TRANSFER_DETACHMENT_TYPE.equals(detachmentModel.getTransferType()))
			{
				return null;
			}
			
			Equipment eqFromDb = persistenceService.findEquipmentById(detachmentModel.getEquipmentId());
			
			if(eqFromDb == null){
				throw new ReconciliationIntegrationException(ErrorMessage.EQUIPMENT_NOT_FOUND, null, 
						"detachEquipment", detachmentModel.toString());
			}
			
			logger.debug("Started detaching equipment in OB: {}", detachmentModel.toString());
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (detachmentModel.getDateTime(), 
				locationCache.getLocationByGarage(detachmentModel.getCurrentLocation(), detachmentModel.getDateTime())); 
			BoardEquipment be = persistenceService.findOrCreateById(BoardEquipment.CREATE_ID(detachmentModel.getEquipmentId(), boardKey.getDate()), boardKey, eqFromDb);
	
			Detachment detach = equipmentEntityMapper.convertAttachmentDetachmentModelToEntity(detachmentModel, DetachmentState.PENDING);
			try
			{
				equipmentExecutor.detach(boardKey, be, detach, User.getPrincipal(detachmentModel.getUser()), true);
			}
			catch (OpsBoardError error)
			{
				detach.setComments(REMARKS);
				equipmentReconciliationService.reconcileDetachment(boardKey, be, detach,
					User.getPrincipal(detachmentModel.getUser()), error);
			}
			logger.info("EndedDetachingEquipmentInOB");
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"detachEquipment", detachmentModel.toString()));	

		}
		return null;
	}

	@Override
	public Void attachEquipment(EquipmentAttachmentDetachment attachmentModel) throws OpsBoardError
	{
		try
		{
			// Ignore transfers for now
			if (ScanUtil.TRANSFER_DETACHMENT_TYPE.equals(attachmentModel.getTransferType()))
			{
				return null;
			}
			
			Equipment eqFromDb = persistenceService.findEquipmentById(attachmentModel.getEquipmentId());
			
			if(eqFromDb == null){
				throw new ReconciliationIntegrationException(ErrorMessage.EQUIPMENT_NOT_FOUND, null, 
						"attachEquipment", attachmentModel.toString());
			}
			
			logger.debug("Started attaching equipment in OB: {}", attachmentModel.toString());
			BoardKey boardKey = boardKeyFactory.createBoardKey (attachmentModel.getDateTime(), 
					locationCache.getLocationByGarage(attachmentModel.getTo(), attachmentModel.getDateTime()));
			BoardEquipment be = persistenceService.findOrCreateById(BoardEquipment.CREATE_ID(attachmentModel.getEquipmentId(), boardKey.getDate()), boardKey, eqFromDb);
	
			Detachment attach = equipmentEntityMapper.convertAttachmentDetachmentModelToEntity(attachmentModel, DetachmentState.ACCEPTED);
			attach.setFrom(eqFromDb.getDetachFromLocation());
			try
			{									
				equipmentExecutor.attach(boardKey, be, attach,
						User.getPrincipal(attachmentModel.getUser()), true);
			}
			catch (OpsBoardError error)
			{
				attach.setComments(REMARKS);
				equipmentReconciliationService.reconcileAttachment(boardKey, be, attach,  
						User.getPrincipal(attachmentModel.getUser()), error);
			}
			
			logger.info("EndedAttachingEquipmentInOB");
		
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"attachEquipment", attachmentModel.toString()));	
		}
		
		return null;
	}
	
	@Override
	public Void downEquipment(EquipmentAction actionDetails,List<EquipmentConditionModel> conditions) throws OpsBoardError
	{
		try
		{
			logger.debug("Started downing equipment in OB: {}", actionDetails.toString());
			
			Equipment eqFromDb = persistenceService.findEquipmentById(actionDetails.getEquipmentId());
			
			if(eqFromDb == null){
				throw new ReconciliationIntegrationException(ErrorMessage.EQUIPMENT_NOT_FOUND, null, 
						"downEquipment", actionDetails.toString());
			}
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (conditions.get(0).getDownDateTime(), 
					locationCache.getLocationByGarage(actionDetails.getCurrentLocation(), conditions.get(0).getDownDateTime()));
			
			BoardEquipment be = persistenceService.findOrCreateById(BoardEquipment.CREATE_ID(actionDetails.getEquipmentId(), boardKey.getDate()), boardKey, eqFromDb);
	
			UpDownModel upDownModel = new UpDownModel(actionDetails, conditions);
			UpDown down = equipmentEntityMapper.convertUpDownModelToEntity(upDownModel);
			try
			{
				equipmentExecutor.down(boardKey, be, down, User.getPrincipal(actionDetails.getUser()), true);
			}
			catch (OpsBoardError error)
			{
				equipmentReconciliationService.reconcileDown(boardKey, be, down, User.getPrincipal(upDownModel.getAction().getUser()), error);
			}
			
			logger.info("EndedDowningEquipmentInOB");
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"downEquipment", actionDetails.toString()));	
		}
		return null;
	}

	@Override
	public Void upEquipment(EquipmentAction actionDetails, List<EquipmentConditionModel> conditions)  throws OpsBoardError
	{
		try
		{
			
			logger.debug("Started upping equipment in OB: {}", actionDetails.toString());
			
			Equipment eqFromDb = persistenceService.findEquipmentById(actionDetails.getEquipmentId());
			
			if(eqFromDb == null){
				throw new ReconciliationIntegrationException(ErrorMessage.EQUIPMENT_NOT_FOUND, null, 
						"upEquipment", actionDetails.toString());
			}
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (conditions.get(0).getDownDateTime(), 
					locationCache.getLocationByGarage(actionDetails.getCurrentLocation(), conditions.get(0).getDownDateTime()));
			BoardEquipment be = persistenceService.findOrCreateById(BoardEquipment.CREATE_ID(actionDetails.getEquipmentId(), boardKey.getDate()), boardKey, eqFromDb);
	
			if (be.getEquipment().getSnowReadiness().isWorkingDown())
			{
				SnowReadiness snowReadiness = be.getSnowReadiness();
				snowReadiness.setWorkingDown(false);
				equipmentExecutor.updateSnowReadiness(boardKey, be, snowReadiness, User.getPrincipal(actionDetails.getUser()), true);
			}
			
			UpDownModel upDownModel= new UpDownModel(actionDetails, conditions);
			UpDown upDown = equipmentEntityMapper.convertUpDownModelToEntity(upDownModel);
			
//			ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//			Validator validator = factory.getValidator();
//
//			Set<ConstraintViolation<UpDown>> constraintViolations = validator.validate(upDown);
//
//			if (constraintViolations.size() > 0 ) 
//			{
//				for (ConstraintViolation<UpDown> contraints : constraintViolations) 
//				{
//					logger.debug("ConstraintViolationsOccurred {}.{} - {};", 
//						contraints.getRootBeanClass().getSimpleName(),
//						contraints.getPropertyPath(), contraints.getMessage());
//			  }
//			}
			
			try
			{
				
				
				logger.debug("upEquipmentUpDown: {}", upDown.toString());
				equipmentExecutor.up(boardKey, be, upDown,	User.getPrincipal(actionDetails.getUser()), true);
			}
			catch (OpsBoardError error)
			{
				equipmentReconciliationService.reconcileUp(boardKey, be, upDown, User.getPrincipal(actionDetails.getUser()), error);
			}
			logger.debug("Ended upping equipment in OB");
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"upEquipment", actionDetails.toString()));	

		}
		return null;
	}

	@Override
	public Void updateDownEquipment(EquipmentAction actionDetails, List<EquipmentConditionModel> conditions)  throws OpsBoardError
	{
		// Log in reconciliation exception
		throw integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_UPDATE_DOWN, null, "updateDownEquipment", actionDetails.toString());
		
//		logger.debug("Started updating down equipment in OB: {}", actionDetails.toString());
//		
//		BoardKey boardKey = boardKeyFactory.createBoardKey (conditions.get(0).getDownDateTime(), 
//				locationCache.getLocationByGarage(actionDetails.getCurrentLocation()));
//		
//		BoardEquipment be = equipmentCache.getSingle(boardKey, BoardEquipment.CREATE_ID(actionDetails.getEquipmentId(), boardKey.getDate()));
//
//		UpDown oldDown = be.getMostRecentUpDown();
//		UpDownModel newUpDownModel = new UpDownModel(actionDetails, conditions);
//		UpDown newDown =  equipmentEntityMapper.convertUpDownModelToEntity(newUpDownModel);
//		try
//		{
//			equipmentExecutor.updateDown(boardKey, be, newDown, oldDown, User.getPrincipal(actionDetails.getUser()));
//		}
//		catch (OpsBoardError error)
//		{
//			equipmentReconciliationService.reconcileUpdateDown(
//					boardKey, be, newDown, oldDown, User.getPrincipal(actionDetails.getUser()), error);
//		}
//		
//		
//		logger.debug("Ended updating down equipment in OB");
//		return null;
	}
	
	@Override
	public Void updateVehicleSnowDetails(EquipmentSnowDetail snowDetails)  throws OpsBoardError
	{
		try
		{
			logger.debug("Started updating snow details for equipment in OB: {}", snowDetails.toString());
			
			Equipment eqFromDb = persistenceService.findEquipmentById(snowDetails.getEquipmentId());
			
			if(eqFromDb == null){
				throw new ReconciliationIntegrationException(ErrorMessage.EQUIPMENT_NOT_FOUND, null, 
						"updateVehicleSnowDetails", snowDetails.toString());
			}
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (snowDetails.getTimestamp(), 
					locationCache.getLocationByGarage(snowDetails.getCurrentLocation(), snowDetails.getTimestamp()));
			
			BoardEquipment be = persistenceService.findOrCreateById(BoardEquipment.CREATE_ID(snowDetails.getEquipmentId(), boardKey.getDate()), boardKey, eqFromDb);
	
			SnowReadiness snowReadiness = be.getSnowReadiness();
			equipmentEntityMapper.updateSnowRedinessEntity(be.getEquipment(), snowReadiness, snowDetails, null);
			
			equipmentExecutor.updateSnowReadiness(boardKey, be, snowReadiness, User.getPrincipal(snowDetails.getUser()), true);
			
			logger.debug("Ended updating snow details for equipment in OB");
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"updateVehicleSnowDetails", snowDetails.toString()));	
		}
		return null;
	}

	@Override
	public Void loadSaltVehicle(EquipmentLoadSalt loadSaltModel) throws OpsBoardError
	{
		try
		{
			logger.debug("Started updating salt load for equipment in OB: {}", loadSaltModel.toString());
			
			Equipment eqFromDb = persistenceService.findEquipmentById(loadSaltModel.getEquipmentId());
			
			if(eqFromDb == null){
				throw new ReconciliationIntegrationException(ErrorMessage.EQUIPMENT_NOT_FOUND, null, 
						"loadSaltVehicle", loadSaltModel.toString());
			}
			
			BoardKey boardKey = boardKeyFactory.createBoardKey (loadSaltModel.getTimestamp(), 
					locationCache.getLocationByGarage(loadSaltModel.getCurrentLocation(), loadSaltModel.getTimestamp()));
			
			BoardEquipment be = persistenceService.findOrCreateById(BoardEquipment.CREATE_ID(loadSaltModel.getEquipmentId(), boardKey.getDate()), boardKey, eqFromDb);
	
			SnowReadiness snowReadiness = be.getSnowReadiness();
			equipmentEntityMapper.updateSnowRedinessEntity(be.getEquipment(), snowReadiness, null, loadSaltModel);
			
			equipmentExecutor.updateSnowReadiness(boardKey, be, snowReadiness, User.getPrincipal(loadSaltModel.getUser()), true);
			
			logger.debug("Ended updating salt load for equipment in OB");
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"loadSaltVehicle", loadSaltModel.toString()));	
		}
		return null;
	}

	@Override
	public Void setEquipment(EquipmentModel equipmentModel) throws OpsBoardError
	{
		try
		{
			logger.debug("Started setting equipment in OB: {}", equipmentModel.toString());
			validate(equipmentModel, EquipmentModel.class.getName());
			Equipment existingEquipment = persistenceService.findEquipmentById(equipmentModel.getEquipmentId());
			Equipment equipment = equipmentEntityMapper.convertEquipmentModelToEntity(equipmentModel, existingEquipment, 
					seriesCacheService.getSeries(new Date()), subTypeCache.getSubTypes());
			
			// get existing equipment again because converter used its reference for the new one
			existingEquipment = persistenceService.findEquipmentById(equipmentModel.getEquipmentId());
			
			// Save series
			Series series = equipment.getSeries();
			if (series.isCustom() && series.getId() == null)
			{
				// Get effective dates for currently effective cache region
				List<Series> currentlyEffectiveSeries =  seriesCacheService.getSeries(new Date());
				Series effectiveSeries = currentlyEffectiveSeries.get(0);
				series.setEffectiveStartDate(effectiveSeries.getEffectiveStartDate());
				series.setEffectiveEndDate(effectiveSeries.getEffectiveEndDate());
				
				// Save custom series
				series = seriesRepository.save(series);
	  			
				// Refresh series cache
				CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.SERIES_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			// Let cache be refreshed before next setEquipment call
	  			try{Thread.sleep(30000);}catch(Exception e){};
	
			}
			
			// Set important flags
			setDefaults(equipment);
			
			Location location = equipment.getOwner();
			BoardKey boardKey = boardKeyFactory.createBoardKey (equipmentModel.getTimestamp(), location);
			
			if (existingEquipment == null)
				equipmentExecutor.addEquipment(boardKey, equipment, User.getPrincipal(equipmentModel.getUser()));
			else
				equipmentExecutor.setEquipment(boardKey, equipment, existingEquipment, User.getPrincipal(equipmentModel.getUser()));
		}
		catch (Throwable throwable)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, 
					new ReconciliationIntegrationException(ErrorMessage.INT_GENERAL_RECONCILIATION_ERROR, throwable, 
							"setVehicle", equipmentModel.toString()));	
		}
		
		return null;
	}
	
	
	private void setDefaults(Equipment equipment)
	{
		if (equipment == null)
			return;
		
		if (!equipment.isDown())
			equipment.setDown(false);
	
		if (equipment.getSnowReadiness() == null)
		{
			equipment.setSnowReadiness(new SnowReadiness());
		}
		
		SnowReadiness snowReadiness = equipment.getSnowReadiness();
		if (!snowReadiness.isSnowAssignment())
			snowReadiness.setSnowAssignment(false);

		if (!snowReadiness.isWorkingDown())
			snowReadiness.setWorkingDown(false);
		
		if (snowReadiness.getLoad() == null)
			snowReadiness.setLoad(Load.NONE);
		
		if (snowReadiness.getPlowType() == null)
			snowReadiness.setPlowType(PlowType.NO_PLOW);
		

	}

	
	public void sendRefreshCacheCommand(CommandRefreshCaches adminCommand)
	{
	    AdminCommandMessage message = new AdminCommandMessage(adminCommand.getName(), adminCommand.getCacheName(), DateUtils.toStringBoardDate(new Date()), "admin", adminCommand);
		messenger.convertAndSend(CommandRefreshCaches.RABBIT_REFRESH_CACHES_TOPIC_NAME, message);
	}
}

package gov.nyc.dsny.smart.opsboard.services.integration;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.integration.facade.OutgoingIntegrationFacade;
import gov.nyc.dsny.smart.opsboard.integration.mapper.EquipmentEntityMapper;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentLoadSalt;
import gov.nyc.dsny.smart.opsboard.integration.service.IntegrationErrorHandlingService;
import gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("outgoingEquipmentService")
public class OutgoingEquipmentServiceImpl implements OutgoingEquipmentService 
{
	private static final Logger logger = LoggerFactory.getLogger(OutgoingEquipmentServiceImpl.class);
	
	@Autowired
	private OutgoingIntegrationFacade integrationFacade;
	
	@Autowired
	private EquipmentEntityMapper equipmentEntityMapper;
	
	@Autowired
	private IntegrationErrorHandlingService integrationErrorHandlingService;
	
	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService#attachEquipment(gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment)
	 */
	@Async
	@Override
	public Future<Void> attachEquipment(BoardKey boardKey, BoardEquipment be, Detachment attachment) 
	{
		try
		{
			integrationFacade.attachVehicleInScan(equipmentEntityMapper.convertDetachmentEntityToModel(boardKey, be, attachment));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_SCAN_ATTACH_EQUIPMENT, t, "attachEquipment", attachment.toString());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService#detachEquipment(gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment)
	 */
	@Async
	@Override
	public Future<Void> detachEquipment(BoardKey boardKey, BoardEquipment be, Detachment detachment) 
	{
		try
		{
			integrationFacade.detachVehiceInScan(equipmentEntityMapper.convertDetachmentEntityToModel(boardKey, be, detachment));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_SCAN_DETACH_EQUIPMENT, t, "detachEquipment", detachment.toString());
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService#detachEquipment(gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment)
	 */
	@Async
	@Override
	public Future<Void> cancelDetachEquipment(BoardKey boardKey, BoardEquipment be, Detachment detachment) 
	{
		try
		{
			integrationFacade.cancelDetachInScan(equipmentEntityMapper.convertDetachmentEntityToModel(boardKey, be, detachment));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_SCAN_CANCEL_DETACH_EQUIPMENT, t, "cancelDetachEquipment", detachment.toString());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService#downEquipment(gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown)
	 */
	@Async
	@Override
	public Future<Void> downEquipment(BoardKey boardKey, BoardEquipment be, UpDown down) 
	{
		try
		{
			integrationFacade.downVehicleInScan(equipmentEntityMapper.convertUpDownEntityToModel(boardKey, be, down));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_SCAN_DOWN_EQUIPMENT, t, "downEquipment", down.toString());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService#upEquipment(gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown)
	 */
	@Async
	@Override
	public Future<Void> upEquipment(BoardKey boardKey, BoardEquipment be, UpDown up) 
	{
		try
		{
			integrationFacade.upVehicleInScan(equipmentEntityMapper.convertUpDownEntityToModel(boardKey, be, up)); 
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_SCAN_UP_EQUIPMENT, t, "upEquipment", up.toString());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService#updateVehicleSnowRediness(gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness)
	 */
	@Async
	@Override
	public Future<Void> updateVehicleSnowRediness(BoardKey boardKey, BoardEquipment be, SnowReadiness snowReadiness) 
	{
		try
		{
			integrationFacade.updateVehicleSnowDetailsInScan(equipmentEntityMapper.convertSnowReadinessEntityToModel(boardKey, be, snowReadiness));
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_SCAN_UPDATE_SNOW_DETAILS, t, "updateVehicleSnowRediness", snowReadiness.toString());
			return null;
		}
		
		try
		{
			EquipmentLoadSalt equipmentLoadSalt = equipmentEntityMapper.convertSaltLoadEntityToModel(boardKey, be, snowReadiness);
			if (equipmentLoadSalt != null)
				integrationFacade.loadSaltVehicleInScan(equipmentLoadSalt);
		}
		catch (Throwable t)
		{
			integrationErrorHandlingService.handleIntegrationException(ErrorMessage.OB_SCAN_UPDATE_SALT_LOAD, t, "updateVehicleSnowRediness", snowReadiness.toString());
		}
		

		return null;
	}

	
}

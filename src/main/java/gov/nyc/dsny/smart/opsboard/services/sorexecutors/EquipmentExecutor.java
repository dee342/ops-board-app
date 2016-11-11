package gov.nyc.dsny.smart.opsboard.services.sorexecutors;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.equipment.DownCodeCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.equipment.AbstractMultiBoardEquipmentCommand;
import gov.nyc.dsny.smart.opsboard.commands.equipment.CommandAddOpsBoardEquipment;
import gov.nyc.dsny.smart.opsboard.commands.equipment.CommandAutoCompleteEquipment;
import gov.nyc.dsny.smart.opsboard.commands.equipment.CommandRemoveOpsBoardEquipment;
import gov.nyc.dsny.smart.opsboard.commands.equipment.CommandUpdateEquipmentLoad;
import gov.nyc.dsny.smart.opsboard.commands.equipment.CommandUpdateEquipmentSnowReadiness;
import gov.nyc.dsny.smart.opsboard.commands.equipment.detach.CommandAttachEquipment;
import gov.nyc.dsny.smart.opsboard.commands.equipment.detach.CommandCancelEquipmentDetachment;
import gov.nyc.dsny.smart.opsboard.commands.equipment.detach.CommandDetachEquipment;
import gov.nyc.dsny.smart.opsboard.commands.equipment.down.CommandDownEquipment;
import gov.nyc.dsny.smart.opsboard.commands.equipment.down.CommandUpEquipment;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentState;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentState;
import gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.integration.mapper.EquipmentEntityMapper;
import gov.nyc.dsny.smart.opsboard.integration.service.OutgoingEquipmentService;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.Utils;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import reactor.util.CollectionUtils;

@Service
@Validated
public class EquipmentExecutor extends SorExecutor {

	private static final Logger logger = LoggerFactory.getLogger(EquipmentExecutor.class);
	
	private static final Map<String, String> newEquipmentIds = new HashMap<String, String>();

	@Autowired
	private BoardCacheService boardsCache;

	@Autowired
	private EquipmentEntityMapper equipmentEntityMapper;

	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private DownCodeCache downCodeCache;

	@Autowired
	private OutgoingEquipmentService outgoingEquipmentService;

	@Autowired
	private EquipmentPersistenceService persist;

	@Autowired
	private ShiftCacheService shiftCacheService;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	public void attach(BoardKey key, BoardEquipment be, @Valid Detachment attach,
			Principal principal) throws OpsBoardError {
		attach(key, be, attach, principal, false);
	}

	public void attach(BoardKey key, BoardEquipment be, @Valid Detachment attach,
			Principal principal, boolean ignoreIntegraton) throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {
				Detachment current = persist.findLatestDetachmentByEquipmentId(be.getEquipment().getId());
				
				if (current == null) {
					throw new OpsBoardError(ErrorMessage.INT_ATTACHMENT_WITHOUT_DETACHMENT);			
				}
				
				if (current.getStatus().equals(DetachmentState.ACCEPTED.getCode())) {
					throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_ALREADY_ATTACHED);
				}
	
				if (current.getStatus().equals(DetachmentState.PENDING.getCode()) && !current.getTo().isTheSameLocation(key.getLocation().getCode())) {
					throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION);
				}

				Date operationTime = attach.getLastModifiedSystem();
				try {
					attach = persist.detach(be, attach, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards,
				// database...)
				CommandAttachEquipment attachCommand = new CommandAttachEquipment(key.toId(), principal.getName(),
						operationTime, be.getId(), be.getEquipment().getId(), be.getName(), attach.getLastModifiedActual(),
						attach.getDriver(), attach.getComments(), attach.getId(), ignoreIntegraton);

				// Construct command to remove equipment from the "from"
				// location
				CommandRemoveOpsBoardEquipment removeCommand = new CommandRemoveOpsBoardEquipment(key.toId(),
						principal.getName(), operationTime, be.getEquipment().getId(), ignoreIntegraton, false);

				// Send out commands to locations
				sendCommands(principal.getName(), key, be.getOwner().getCode(), attachCommand, attach.getFrom()
						.getCode(), removeCommand, attach.getTo().getCode(), attachCommand);

				if (!ignoreIntegraton) {
					outgoingEquipmentService.attachEquipment(key, be, attach);
				}
			}
		}
		logger.debug("Executed attach");
	}

	public void autoCompleteAssignedEquipment(BoardKey boardKey, BoardEquipment boardEquipment, Date now, String user, boolean hasUnfinishedPartialTasks, boolean isSnowTask) throws OpsBoardError {
	
		synchronized (boardEquipment) {
			synchronized (boardEquipment.getEquipment()) {

				Bin bin1 = null;
				Bin bin2 = null;
				
				List<Bin> bins = boardEquipment.getMostRecentBins();
				
				// Vehicles might have only single bin				
				if(!CollectionUtils.isEmpty(bins)){
					bin1 = bins.get(0);
					if (bins.size() > 1) {
						bin2 = bins.get(1);
					}
	
					SnowReadiness snowReadiness = boardEquipment.getSnowReadiness();
					if (!boardEquipment.getState(boardKey.getLocation()).getState()
							.equals(EquipmentState.LOAD_PENDING.getName()) && snowReadiness != null && !hasUnfinishedPartialTasks && !isSnowTask) {
	
						bin1.setPendingLoad(true);
						
						if(bin2 != null)
							bin2.setPendingLoad(true);
	
						
						// Persist bins
						try{
							persist.updateEquipmentLoad(boardEquipment.getEquipment().getId(), bin1, bin2);
						}catch (Exception e) {								
								throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
						}
					}
				}
				// All parameters in command gets pushed back to all browser, and the
				// browser will update the board
				AbstractMultiBoardEquipmentCommand command = new CommandAutoCompleteEquipment(boardKey.toId(), user,
						new Date(), boardEquipment.getId(), boardEquipment.getEquipment().getId(), bin1, bin2, hasUnfinishedPartialTasks);

				sendCommands(null, boardKey, boardEquipment.getOwner().getCode(), command, boardKey.getLocation()
						.getCode(), command);
			}
		}
	}

	public void cancelDetach(BoardKey key, BoardEquipment be, @Valid Detachment detach,  Principal principal) throws OpsBoardError {
		cancelDetach(key, be, detach, principal, false);
	}

	public void cancelDetach(BoardKey key, BoardEquipment be, @Valid Detachment detach, Principal principal, boolean ignoreIntegraton)
			throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {

				Detachment current = persist.findLatestDetachmentByEquipmentId(be.getEquipment().getId());
				
				if (current == null) {
					throw new OpsBoardError(ErrorMessage.DATA_ERROR_DETACHMENT_HISTORY);
				}
				
				if (current.getStatus().equals(DetachmentState.CANCELLED.getCode())) {
					throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_NOT_PENDING_DETACH);
				}
				
				Date operationTime = detach.getLastModifiedSystem();
				
				try {
					detach = persist.detach(be, detach, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards,
				// database...)
				CommandCancelEquipmentDetachment cancelCommand = new CommandCancelEquipmentDetachment(key.toId(),
						principal.getName(), operationTime, be.getId(), be.getEquipment().getId(),
						be.getName(), detach.getId(), ignoreIntegraton);

				// Construct command to remove equipment from the "to" location
				CommandRemoveOpsBoardEquipment removeCommand = new CommandRemoveOpsBoardEquipment(key.toId(),
						principal.getName(), operationTime, be.getEquipment().getId(), ignoreIntegraton, false);

				// Send out commands to locations
				sendCommands(principal.getName(), key, be.getOwner().getCode(), cancelCommand, detach.getFrom()
						.getCode(), cancelCommand, detach.getTo().getCode(), removeCommand);

				if (!ignoreIntegraton) {
					outgoingEquipmentService.cancelDetachEquipment(key, be, detach);
				}
			}
		}
		logger.debug("Executed cancelDetach");
	}

	public void detach(BoardKey key, BoardEquipment be, @Valid Detachment detach,
			Principal principal) throws OpsBoardError {
		detach(key, be, detach, principal, false);
	}

	public void detach(BoardKey key, BoardEquipment be, @Valid Detachment detach,
			Principal principal, boolean ignoreIntegraton) throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {
				
				Detachment current = persist.findLatestDetachmentByEquipmentId(be.getEquipment().getId());
				
				if (current != null) {
	
					if (current.getStatus().equals(DetachmentState.PENDING.getCode())) {
						throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_ALREADY_PENDING_DETACH);
					}
	
					if (current.getStatus().equals(DetachmentState.ACCEPTED.getCode())
							&& !current.getTo().isTheSameLocation(detach.getFrom().getCode())) {
						throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_IN_DIFFERENT_LOCATION);
					}
	
				}
							
				Detachment detachment = detachSaveInDB ( key,  be, detach, principal);
				detachSendCommands( key,  be,  detachment.getFrom(),  detachment.getTo(),  detachment.getDriver(),
						detachment.getLastModifiedActual(), principal, detachment, ignoreIntegraton);

				if (!ignoreIntegraton) {
					outgoingEquipmentService.detachEquipment(key, be, detachment);
				}
			}
		}
		logger.debug("Executed detach");
	}

	
	private Detachment detachSaveInDB (BoardKey key, BoardEquipment be, Detachment detach,
			Principal principal) throws OpsBoardError {
		
		Date operationTime = detach.getLastModifiedSystem();
		
		try {
			detach = persist.detach(be, detach, principal.getName(), operationTime);
		} catch (Exception e) {
			throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
		}

		return detach;
	}
	
	private void detachSendCommands(BoardKey key, BoardEquipment be, Location from, Location to, String driver, Date actualDatetime,
			Principal principal, Detachment detach, boolean fromIntegration) throws OpsBoardError {

		Date operationTime = new Date();
		// Construct command to send to clients (location boards,
		// database...)
		CommandDetachEquipment detachCommand = new CommandDetachEquipment(key.toId(), principal.getName(),
				operationTime, be.getId(), be.getEquipment().getId(), be.getName(), actualDatetime, from, to,
				driver, "", "", detach.getId(), fromIntegration);

		// Construct command to add equipment to the "to" location
		CommandAddOpsBoardEquipment addCommand = new CommandAddOpsBoardEquipment(key.toId(),
				principal.getName(), operationTime, be.getEquipment().getId(), fromIntegration, false);

		// Send out commands to locations
		sendCommands(principal.getName(), key, be.getOwner().getCode(), detachCommand, from.getCode(),
				detachCommand, to.getCode(), addCommand);
	}
	
	public void down(BoardKey key, BoardEquipment be, @Valid UpDown upDown, Principal principal) throws OpsBoardError {
		down(key, be, upDown, principal, false);
	}

	public void down(BoardKey key, BoardEquipment be, @Valid UpDown upDown, Principal principal, boolean ignoreIntegraton)
			throws OpsBoardError {
		

		Date date= DateUtils.toBoardDateNoNull(key.getDate());
		Set<EquipmentCondition> equipmentConditions = upDown.getConditions();
		for (EquipmentCondition equipmentCondition : equipmentConditions)
		{
			if (equipmentCondition != null && StringUtils.isNotBlank(equipmentCondition.getDownCode()))
			{
				equipmentCondition.setDownCodeType(downCodeCache.getDownCode(equipmentCondition.getDownCode(), date));
			}
		}

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {
				
				UpDown current = persist.findLatestUpDownByEquipmentId(be.getEquipment().getId());

				if (current != null) {
					if (current.isDown()) {
						throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_ALREADY_DOWN);
					}

				}	

				Date operationTime = new Date();
				try {
					upDown = persist.down(be, upDown, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards,
				// database...)
				CommandDownEquipment command = new CommandDownEquipment(key.toId(), principal.getName(), operationTime,
						be.getId(), be.getEquipment().getId(), upDown, ignoreIntegraton);

				// Send out commands to locations
				sendCommands(principal.getName(), key, be.getOwner().getCode(), command, key.getLocation().getCode(),
						command);

				if (!ignoreIntegraton) {
					outgoingEquipmentService.downEquipment(key, be, upDown);
				}
			}
		}
		logger.debug("Executed down");
	}
	
	public void addEquipment(BoardKey boardKey, Equipment equipment, Principal principal) throws OpsBoardError {
		
		if (newEquipmentIds.get(equipment.getId()) == null)
			newEquipmentIds.put(equipment.getId(), equipment.getId());
			
		synchronized (newEquipmentIds.get(equipment.getId())) {
			Date operationTime = new Date();
			Detachment detachment = new Detachment("SCAN", "SCAN", operationTime, operationTime, equipment, "", "", equipment.getOwner(), DetachmentState.PENDING.getCode(), equipment.getOwner());
			equipment.addDetachment(detachment);
			equipment.setDetachFromLocation(detachment.getFrom());
			equipment.setDetachToLocation(detachment.getTo());
			equipment.setDetachStatus(detachment.getStatus());
			Equipment newEquipment = setEquipmentSaveInDB( boardKey,  equipment,  null,  principal);
			setEquipmentSendCommands ( boardKey, newEquipment, null, true);
		}

		logger.debug("Executed addEquipment for {}", equipment.getId());
	}
	
	public void setEquipment(BoardKey boardKey, Equipment equipment, Equipment existingEquipment, Principal principal) throws OpsBoardError {
		
		if (newEquipmentIds.get(equipment.getId()) == null)
			newEquipmentIds.put(equipment.getId(), equipment.getId());
			
		synchronized (newEquipmentIds.get(equipment.getId())) {
			
			// Prepare ghost detachment in case of transfer
			prepareGhostDetachment(boardKey, existingEquipment, equipment);
			
			//End Start Adjusting pending, current and future detachments
			Equipment updatedEquipment = setEquipmentSaveInDB( boardKey,  equipment,  existingEquipment,  principal);
			setEquipmentSendCommands ( boardKey, updatedEquipment, existingEquipment, true);
		 }

		logger.debug("Executed setEquipment for {}", equipment.getId());
	}

	private void prepareGhostDetachment(BoardKey boardKey, Equipment existingEquipment, Equipment newEquipment) throws OpsBoardError
	{
        if (existingEquipment == null)
            return;
		
        Location existingOwner = existingEquipment.getOwner();
		Location newOwner = newEquipment.getOwner();
		boolean isNewDetachmentRequired = !newOwner.equals(existingOwner);
        
		Detachment ghostDetachment = null;
        
		Date boardStartDate = boardKey.getShiftsStart();
		Date boardEndDate = boardKey.getShiftsEnd();
		BoardEquipment be  = new BoardEquipment(newEquipment, boardKey.getDate(), boardEndDate, boardStartDate);
		Detachment mostRecentDetachment = be.getMostRecentDetachment();
		if (mostRecentDetachment != null)
		{
			Date today = new Date();
			ghostDetachment = getGhostDetachment (existingEquipment, mostRecentDetachment);
			if (ghostDetachment == null)
			{
				ghostDetachment = mostRecentDetachment;
				mostRecentDetachment = new Detachment(
						mostRecentDetachment.getActualUser(), mostRecentDetachment.getSystemUser(), 
						mostRecentDetachment.getLastModifiedActual(), today,
						existingEquipment, mostRecentDetachment.getComments(), mostRecentDetachment.getDriver(), 
						mostRecentDetachment.getFrom(), mostRecentDetachment.getStatus(), mostRecentDetachment.getTo());
			}
			
			// Replaces servicesEquipmentLocation with new location which has the same garage code
			Location from = fixServicesEquipmentLocations(mostRecentDetachment.getFrom());
			if (!mostRecentDetachment.getFrom().isTheSameLocation(from.getCode()))
			{
				mostRecentDetachment.setFrom(from);
				newEquipment.setDetachFromLocation(from);
				isNewDetachmentRequired = true;
			}
			
			// Replaces servicesEquipmentLocation with new location which has the same garage code
			Location to = fixServicesEquipmentLocations(mostRecentDetachment.getTo());
			if (!mostRecentDetachment.getTo().isTheSameLocation(to.getCode()))
			{
				mostRecentDetachment.setTo(to);
				newEquipment.setDetachToLocation(to);
				isNewDetachmentRequired = true;
			}
			
			// Check if new detachment should even be added
			if (isNewDetachmentRequired)
			{
				if (ghostDetachment.getFilterDate() == null)
					ghostDetachment.setFilterDate(today);
				newEquipment.addDetachment(mostRecentDetachment);
			}
		}
		return;
	}
	
	/* 
	 * This method replaces ServicesEquipmentLocation with new location which has the same garage code
	 */
	private Location fixServicesEquipmentLocations(Location location)
	{
		if (location.isServicesEquipmentLocations())
		{
			Location previousLocation = location.getPreviousLocation();
			if (previousLocation != null)
			{
				String garageCode = previousLocation.getGarageCode();
				if (StringUtils.isNotBlank(garageCode))
				{
					Location newLocation = null;
					try
					{
						newLocation = locationCache.getLocationByGarage(garageCode, new Date());
						return newLocation;
						
					}
					catch (Exception e)
					{
						logger.error("{} - {}: location is not found for garage {}", e, ErrorMessage.SCAN_OB_SET_EQUIPMENT.getCode(), 
								ErrorMessage.SCAN_OB_SET_EQUIPMENT.getMessage(), garageCode );
					}
				}
			}
		}
		return location;
	}
	
	private Detachment getGhostDetachment(Equipment equipment, Detachment mostRecentDetachment)
	{
		List<Detachment> detachments = equipment.getDetachmentHistory();
		for (Detachment detachment : detachments)  
		{
			if (StringUtils.equals(detachment.getActualUser(),mostRecentDetachment.getActualUser()) &&
				StringUtils.equals(detachment.getComments(), mostRecentDetachment.getComments()) &&
				StringUtils.equals(detachment.getDriver(), mostRecentDetachment.getDriver()) &&
				StringUtils.equals(detachment.getStatus(), mostRecentDetachment.getStatus()) &&
				StringUtils.equals(detachment.getSystemUser(), mostRecentDetachment.getSystemUser()) &&
				detachment.getLastModifiedActual().equals(mostRecentDetachment.getLastModifiedActual()) &&
				detachment.getFilterDate() != null)
			{
				return detachment;
			}
		}
		return null;
		
	}

	private Equipment setEquipmentSaveInDB(BoardKey boardKey, Equipment equipment, Equipment existingEquipment, Principal principal) throws OpsBoardError {
		boolean isNew = (existingEquipment == null) ? true : false;
	
		// Create keys for all existing future boards
		Location location = equipment.getOwner();
		Set<String> boardDates = persist.findExistingBoardEquipmentDates(DateUtils.removeTime(new Date()), location);
		Set<BoardKey> boardKeys = new HashSet<BoardKey>();
		for (String boardDate : boardDates)
		{
			boardKeys.add(boardKeyFactory.createBoardKey (boardDate, location));
		}
		
		Equipment updatedEquipment = null;
		if (isNew)
		{
			updatedEquipment = persist.saveBrandNewEquipment(equipment, boardKeys);
		}
		else
		{
			updatedEquipment = persist.save(equipment, boardKey.toDate(), true, boardKeys);
		}
		return updatedEquipment;
	}
	
	private void setEquipmentSendCommands(BoardKey boardKey, Equipment updatedEquipment, Equipment existingEquipment, boolean fromIntegration) throws OpsBoardError {
		Map<String,IMultiBoardCommand> locationCommandMap = new HashMap<String,IMultiBoardCommand>();
		
		AbstractMultiBoardEquipmentCommand locationAddCommand = null;
		AbstractMultiBoardEquipmentCommand locationRemoveCommand = null;
		
		if(existingEquipment == null)
		{
			//**************************************************************************************************
			// ******************** Adding existing equipment **************************************************
			//**************************************************************************************************
			locationAddCommand = new CommandAddOpsBoardEquipment(boardKey.toId(), "SCAN", new Date(), updatedEquipment.getId(), fromIntegration, false);
			locationCommandMap.put(updatedEquipment.getOwner().getCode(), locationAddCommand);
		}
		else
		{
			boolean resurrected = !existingEquipment.isActive() && updatedEquipment.isActive();
			
			//**************************************************************************************************
			// ******************** Updating existing equipment ************************************************
			//**************************************************************************************************
			Set<Location> removeExistingLocations = new HashSet<Location>();
			removeExistingLocations.add(existingEquipment.getOwner());
			removeExistingLocations.add(existingEquipment.getDetachFromLocation());
			removeExistingLocations.add(existingEquipment.getDetachToLocation());
			
			if(updatedEquipment.isActive())
			{
				// Sending add commands
				if (resurrected)
				{
					locationAddCommand = new CommandAddOpsBoardEquipment(createBoardKey(boardKey, updatedEquipment.getOwner()).toId(), "SCAN", new Date(), updatedEquipment.getId(), 
							fromIntegration, false);
				}
				else
				{
					locationAddCommand = new CommandAddOpsBoardEquipment(createBoardKey(boardKey, updatedEquipment.getOwner()).toId(), "SCAN", new Date(), updatedEquipment.getId(), fromIntegration, 
							removeExistingLocations.contains(updatedEquipment.getOwner()));
				}
				locationCommandMap.put(updatedEquipment.getOwner().getCode(), locationAddCommand);
				
				if (!resurrected)
				{
					locationAddCommand = new CommandAddOpsBoardEquipment(createBoardKey(boardKey, updatedEquipment.getDetachFromLocation()).toId(), "SCAN", new Date(), updatedEquipment.getId(), fromIntegration, 
							removeExistingLocations.contains(updatedEquipment.getDetachFromLocation()));
					locationCommandMap.put(updatedEquipment.getDetachFromLocation().getCode(), locationAddCommand);
					
					locationAddCommand = new CommandAddOpsBoardEquipment(createBoardKey(boardKey, updatedEquipment.getDetachToLocation()).toId(), "SCAN", new Date(), updatedEquipment.getId(), fromIntegration, 
							removeExistingLocations.contains(updatedEquipment.getDetachToLocation()));
					locationCommandMap.put(updatedEquipment.getDetachToLocation().getCode(), locationAddCommand);

					// Sending remove commands
					if (removeExistingLocations.contains(updatedEquipment.getOwner()))
						removeExistingLocations.remove(updatedEquipment.getOwner());
					
					if (removeExistingLocations.contains(updatedEquipment.getDetachFromLocation()))
						removeExistingLocations.remove(updatedEquipment.getDetachFromLocation());
					
					if (removeExistingLocations.contains(updatedEquipment.getDetachToLocation()))
						removeExistingLocations.remove(updatedEquipment.getDetachToLocation());
					
					for (Location location : removeExistingLocations)
					{
						locationRemoveCommand = new CommandRemoveOpsBoardEquipment(createBoardKey(boardKey, location).toId(), "SCAN", new Date(), updatedEquipment.getId(), fromIntegration, true);
						locationCommandMap.put(location.getCode(), locationRemoveCommand);
					}
				}
			}
			else
			{
				//**************************************************************************************************
				// Processing inactive equipment - sending remove commands
				//**************************************************************************************************
				if (!removeExistingLocations.contains(updatedEquipment.getOwner()))
					removeExistingLocations.add(updatedEquipment.getOwner());
				
				for (Location location : removeExistingLocations)
				{
					if (location == null)
						continue;
					locationRemoveCommand = new CommandRemoveOpsBoardEquipment(createBoardKey(boardKey, location).toId(), "SCAN", new Date(), updatedEquipment.getId(), fromIntegration, true);
					locationCommandMap.put(location.getCode(), locationRemoveCommand);
				}
			}
		}
					
		sendCommands("SCAN", boardKey, locationCommandMap);
	}
	
	private BoardKey createBoardKey (BoardKey boardKey, Location location) throws OpsBoardError
	{
		return boardKeyFactory.createBoardKey(DateUtils.toBoardDate(boardKey.getDate()),location);
	}
	
	public void up(BoardKey key, BoardEquipment be, @Valid UpDown upDown, Principal principal)
			throws OpsBoardError {
		up(key, be, upDown, principal, false);
	}

	public void up(BoardKey key, BoardEquipment be, @Valid UpDown upDown, Principal principal,
			boolean ignoreIntegraton) throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {
				
				UpDown prevUp = persist.findLatestUpDownByEquipmentId(be.getEquipment().getId());
				
				if (prevUp == null) {
					throw new OpsBoardError(ErrorMessage.DATA_ERROR_UPDOWN_HISTORY);
				}

				if (!prevUp.isDown()) {
					throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_ALREADY_UP);
				}				
				Date operationTime = new Date();

				try {
					upDown = persist.down(be, upDown, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards,
				// database...)
				CommandUpEquipment command = new CommandUpEquipment(key.toId(), principal.getName(), operationTime,
						be.getId(), be.getEquipment().getId(), upDown, ignoreIntegraton);

				// Send out commands to locations
				sendCommands(principal.getName(), key, be.getOwner().getCode(), command, key.getLocation().getCode(),
						command);

				if (!ignoreIntegraton) {
					outgoingEquipmentService.upEquipment(key, be, upDown);
				}
			}
		}
		logger.debug("Executed up");
	}

	public void updateBinLoad(BoardKey key, BoardEquipment be, @Valid Bin bin1, @Valid Bin bin2)
			throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {

				// Get current bins
				List<Bin> current = be.getMostRecentBins();
				if (current == null) {
					throw new OpsBoardError(ErrorMessage.DATA_ERROR_BINS);
				}

				// Save to DB

				Date operationTime = new Date();
				
				if (current.size() > 0 && bin1 != null) {																			
					bin1.setLastModifiedActual(operationTime);
					bin1.setPendingLoad(false);								
					bin1.setLastModifiedSystem(operationTime);
				}
				
				if (current.size() > 1 && bin2 != null) {
					bin2.setLastModifiedActual(operationTime);
					bin2.setPendingLoad(false);								
					bin2.setLastModifiedSystem(operationTime);
				}

				try {
					persist.updateEquipmentLoad(be.getEquipment().getName(), bin1, bin2);
				}catch (Exception e) {								
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandUpdateEquipmentLoad command = new CommandUpdateEquipmentLoad(key.toId(), Utils.getUserId(),
						operationTime, be.getId(), be.getEquipment().getId(), bin1, bin2);

				// Send out commands to locations
				sendCommands(Utils.getUserId(), key, be.getOwner().getCode(), command, key.getLocation().getCode(),
						command);
			}
		}
		logger.debug("Executed updateBinLoad");
	}

	/*public void updateDown(BoardKey key, BoardEquipment be, UpDown newUpDown, UpDown oldUpDown, Principal principal)
			throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {

				// TODO
				
				 * Validate status. If not down, throw error.
				 

				UpDown current = be.getMostRecentUpDown();

				if (current == null) {
					throw new OpsBoardError(ErrorMessage.DATA_ERROR_UPDOWN_HISTORY);
				}

				if (!current.isDown()) {
					throw new OpsBoardError(ErrorMessage.SCAN_EQUIPMENT_IS_NOT_DOWN);
				}

				Date operationTime = new Date();
				try {
					persist.updateDown(be.getEquipment().getId(), newUpDown, oldUpDown, principal.getName(),
							operationTime);
					be.getEquipment().addUpDown(newUpDown);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards,
				// database...)
				CommandDownEquipment command = new CommandDownEquipment(key.toId(), principal.getName(), operationTime,
						be.getId(), be.getEquipment().getId(), newUpDown);

				// Send out commands to locations
				sendCommands(principal.getName(), key, be.getOwner().getCode(), command, key.getLocation().getCode(),
						command);
			}
		}
		logger.debug("Executed updateDown");
	}*/

	public void updateSnowReadiness(BoardKey key, BoardEquipment be, @Valid SnowReadiness snowReadiness, Principal principal) throws OpsBoardError {
		updateSnowReadiness(key, be, snowReadiness, principal, false);
	}

	public void updateSnowReadiness(BoardKey key, BoardEquipment be, @Valid SnowReadiness snowReadiness, Principal principal,
			boolean ignoreIntegraton) throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (be) {
			synchronized (be.getEquipment()) {

				
				/*
				 * SMARTOB-6930 - if now plowType, snowAssignment is false.
				 */
				String code = be.getEquipment().getSubtypeObj().getCode();
				if(!snowReadiness.hasPlowType() && !(code.equals("2200") || code.equals("5000")))
					snowReadiness.setSnowAssignment(false);
				
				be.getEquipment().setSnowReadiness(snowReadiness);

				try {
					persist.save(be.getEquipment(), key.toDate());
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandUpdateEquipmentSnowReadiness command = new CommandUpdateEquipmentSnowReadiness(key.toId(),
						principal.getName(), new Date(), be.getId(), be.getEquipment().getId(),
						snowReadiness, ignoreIntegraton);

				// Send out commands to locations
				sendCommands(principal.getName(), key, be.getOwner().getCode(), command, key.getLocation().getCode(),
						command);

				if (!ignoreIntegraton) {
					outgoingEquipmentService.updateVehicleSnowRediness(key, be, snowReadiness);
				}
			}
		}
		logger.debug("Executed updateSnowReadiness");
	}
	
}

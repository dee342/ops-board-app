package gov.nyc.dsny.smart.opsboard.services.sorexecutors;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.PersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.MdaTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SpecialPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.UnavailabilityTypeCacheService;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.CommandAutoCompletePersonnel;
import gov.nyc.dsny.smart.opsboard.commands.person.detach.CommandCancelDetachPerson;
import gov.nyc.dsny.smart.opsboard.commands.person.detach.CommandDetachPerson;
import gov.nyc.dsny.smart.opsboard.commands.person.detach.CommandUpdateDetachPerson;
import gov.nyc.dsny.smart.opsboard.commands.person.mdastatus.CommandAddPersonMdaStatus;
import gov.nyc.dsny.smart.opsboard.commands.person.mdastatus.CommandRemovePersonMdaStatus;
import gov.nyc.dsny.smart.opsboard.commands.person.mdastatus.CommandUpdatePersonMdaStatus;
import gov.nyc.dsny.smart.opsboard.commands.person.opsboard.AbstractOpsBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.opsboard.CommandAddOpsBoardPerson;
import gov.nyc.dsny.smart.opsboard.commands.person.opsboard.CommandGroundOpsBoardPerson;
import gov.nyc.dsny.smart.opsboard.commands.person.opsboard.CommandRemoveOpsBoardPerson;
import gov.nyc.dsny.smart.opsboard.commands.person.specialposition.CommandAddSpecialPosition;
import gov.nyc.dsny.smart.opsboard.commands.person.specialposition.CommandRemoveSpecialPosition;
import gov.nyc.dsny.smart.opsboard.commands.person.specialposition.CommandUpdateSpecialPosition;
import gov.nyc.dsny.smart.opsboard.commands.person.unavailable.CommandAddPersonUnavailability;
import gov.nyc.dsny.smart.opsboard.commands.person.unavailable.CommandCancelPersonUnavailability;
import gov.nyc.dsny.smart.opsboard.commands.person.unavailable.CommandMassChartUpdate;
import gov.nyc.dsny.smart.opsboard.commands.person.unavailable.CommandRemovePersonUnavailability;
import gov.nyc.dsny.smart.opsboard.commands.person.unavailable.CommandReverseCancelPersonUnavailability;
import gov.nyc.dsny.smart.opsboard.commands.person.unavailable.CommandUpdatePersonUnavailability;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.AbstractDetachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.GroundingStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.integration.service.OutgoingPersonnelService;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.exception.personnel.PersonDetachmentOverlapTaskException;
import gov.nyc.dsny.smart.opsboard.persistence.services.personnel.PersonnelPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.CloneUtils;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.validation.UnavailabilityValidationException;
import gov.nyc.dsny.smart.opsboard.validation.ValidationUtils;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PersonExecutor extends SorExecutor {

	private static final Logger logger = LoggerFactory.getLogger(PersonExecutor.class);
	
	private static final Map<String, String> newPersonnelIds = new HashMap<String, String>();

	private static void validateUnavailabilityReason(Long id, String code, Date startDate, Date endDate,
			String comments, List<?> typeList, BoardPerson bp, String operation, BoardKey boardKey) throws OpsBoardValidationException {
		try {
			ValidationUtils.isValid(id, code, startDate, endDate, comments,typeList, bp, operation, boardKey);
		} catch (UnavailabilityValidationException e) {
			List<String> extErrMessages = new ArrayList<String>();
			extErrMessages.add(e.getMessage()); // UI will pop up error message with this text
			throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
					extErrMessages));
		}
	}

	@Autowired
	private MdaTypeCacheService mdaTypeCacheService;

	@Autowired
	private OutgoingPersonnelService outgoingPersonnelService;

	@Autowired
	private PersonnelPersistenceService persist;

	@Autowired
	private PersonnelCacheService personnelCacheService;

	@Autowired
	private PersonnelRepository personnelRepository;

	@Autowired
	private SpecialPositionTypeCacheService specialPositionTypeCacheService;

	@Autowired
	private UnavailabilityTypeCacheService unavailabilityTypeCacheService;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	public void addMdaStatus(BoardKey boardKey, BoardPerson bp, MdaStatus mda, Principal principal)
			throws OpsBoardError, OpsBoardValidationException {
		if (mda.getMdaType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			mda.setMdaType(mdaTypeCacheService.getMdaType(mda.getSubType(), date));
		}

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		synchronized (bp) {
			synchronized (bp.getPerson()) {

				List<MdaStatus> mdaStatus = persist.findMdaByEndDateAndActive(mda.getEndDate(), bp.getPerson().getId());
				//Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				//BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				//bpFromDb.setPerson(pFromDb);
				//bpFromDb.copyTransientProperties(bp);

				if (!ValidationUtils.isValid(null, null, mda.getStartDate(), mda.getEndDate(), mda.getComments(),
						mdaStatus,bp, "MDA",boardKey)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("Record for this date range already exists");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				// Set system data
				Date operationTime = new Date();
				mda.setSystemUser(principal.getName());
				mda.setLastModifiedSystem(operationTime);
				if (mda.getLastModifiedActual() == null) {
					mda.setLastModifiedActual(mda.getLastModifiedSystem());
				}
				if (mda.getActualUser() == null || mda.getActualUser().isEmpty()) {
					mda.setActualUser(mda.getSystemUser());
				}

				// Create new UUID for PS
				if (StringUtils.isBlank(mda.getPeopleSoftId())) {
					mda.setPeopleSoftId(UUID.randomUUID().toString());
				}

				// Updated record status
				mda.setStatus("A");

				// Save to DB
				MdaStatus persistedMda = null;
				try {
					persistedMda = persist
							.addMdaStatus(bp.getPerson().getId(), mda, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandAddPersonMdaStatus command = new CommandAddPersonMdaStatus(boardKey.toId(), principal.getName(),
						persistedMda.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(), persistedMda);

				commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				outgoingPersonnelService.setPersonMDA(persistedMda, false);
			}
		}
		logger.debug("Executed addMdaStatus");
	}

	public void addSpecialPosition(BoardKey boardKey, BoardPerson bp, SpecialPosition specialPosition,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		addSpecialPosition(boardKey, bp, specialPosition, principal, false);
	}

	public void addSpecialPosition(BoardKey boardKey, BoardPerson bp, SpecialPosition specialPosition,
			Principal principal, boolean ignoreIntegration) throws OpsBoardError, OpsBoardValidationException {

		if (specialPosition.getSpecialPositionType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			specialPosition.setSpecialPositionType(specialPositionTypeCacheService.getSpecialPositionType(
					specialPosition.getCode(), date));
		}

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();
		CommandAddSpecialPosition command = null;

		synchronized (bp) {
			synchronized (bp.getPerson()) {

				//Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				//BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				//bpFromDb.setPerson(pFromDb);
				//bpFromDb.copyTransientProperties(bp);
				List<SpecialPosition> specialPositions = persist.findSpecialPositionsByEndDateAndActive(bp.getDate(), 
						bp.getPerson().getId());		
				if (!ValidationUtils.isValid(null, null, specialPosition.getStartDate(), specialPosition.getEndDate(),
						specialPosition.getComments(),specialPositions, bp, "SPECIAL_POSITION", boardKey)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("Record for this date range already exists");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}

				// Set system data
				Date operationTime = new Date();
				specialPosition.setSystemUser(principal.getName());
				specialPosition.setLastModifiedSystem(operationTime);
				if (specialPosition.getLastModifiedActual() == null) {
					specialPosition.setLastModifiedActual(specialPosition.getLastModifiedSystem());
				}
				if (specialPosition.getActualUser() == null || specialPosition.getActualUser().isEmpty()) {
					specialPosition.setActualUser(specialPosition.getSystemUser());
				}

				// Create new UUID for PS
				if (StringUtils.isBlank(specialPosition.getPeopleSoftId())) {
					specialPosition.setPeopleSoftId(UUID.randomUUID().toString());
				}

				// Updated record status
				specialPosition.setStatus("A");

				// Save to DB
				SpecialPosition persistedSpecialPosition = null;

				try {
					// Save to DB
					persistedSpecialPosition = persist.addSpecialPosition(bp.getPerson().getId(), specialPosition,
							principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				command = new CommandAddSpecialPosition(boardKey.toId(), principal.getName(),
						persistedSpecialPosition.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(),
						persistedSpecialPosition, ignoreIntegration);

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, bp.getHomeLocation().getCode(), command, bp
						.getWorkLocation().getCode(), command);
				sendCommands(principal.getName(), boardKey, commandsMap);

				if (!ignoreIntegration) {
					outgoingPersonnelService.setPersonSpecialPosition(persistedSpecialPosition, false);
				}

			}
		}
		logger.debug("Executed addSpecialPosition");
	}

	public void addUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		addUnavailabilityReason(boardKey, bp, reason, principal, false);
	}

	public void addUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal, boolean ignoreIntegration) throws OpsBoardError, OpsBoardValidationException {

		if (reason.getUnavailabilityType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			reason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(reason.getCode(), date));
		}

		CommandAddPersonUnavailability command = null;
		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		synchronized (bp) {
			synchronized (bp.getPerson()) {
				List<UnavailabilityReason> unavailabilityReasons = persist.findByEndDateAndActive( bp.getDate(),bp.getPerson().getId());
				//Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				//BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				//bpFromDb.setPerson(pFromDb);
				//bpFromDb.copyTransientProperties(bp);

				validateUnavailabilityReason(null, reason.getCode(), reason.getStart(), reason.getEnd(),
						reason.getComments(),unavailabilityReasons, bp, "ADD_UNAVAILABLE", boardKey);

				// Set system data
				Date operationTime = new Date();
				reason.setSystemUser(principal.getName());
				reason.setLastModifiedSystem(operationTime);
				if (reason.getLastModifiedActual() == null) {
					reason.setLastModifiedActual(reason.getLastModifiedSystem());
				}
				if (reason.getActualUser() == null || reason.getActualUser().isEmpty()) {
					reason.setActualUser(reason.getSystemUser());
				}

				// Updated record status
				reason.setStatus("A");
				reason.setAction("A");
				// Break multi-day range into single day events for cancel availability codes
				List<UnavailabilityReason> reasons = new ArrayList<UnavailabilityReason>();
				if (ValidationUtils.CANCEL_AVAILABILITY_CODES.contains(reason.getCode())) {
					Calendar start = DateUtils.removeTimeAndConvertToCalendar(reason.getStart());
					Calendar end = DateUtils.removeTimeAndConvertToCalendar(reason.getEnd());
					if (start.before(end)) {
						while (!start.after(end)) {
							UnavailabilityReason day = new UnavailabilityReason();
							BeanUtils.copyProperties(reason, day);

							// Create new UUID for PS
							if (StringUtils.isBlank(day.getPeopleSoftId())) {
								day.setPeopleSoftId(UUID.randomUUID().toString());
							}

							// Set reason to length of 1 day
							if (start.getTime().equals(DateUtils.removeTime(reason.getStart()))) {
								day.setStart(reason.getStart()); // first day keep start time
								day.setEnd(DateUtils.setEndDayTimeToTheMinute(start.getTime())); // first day ends at
																									// regular day end
							} else {
								day.setStart(start.getTime()); // start at beginning of day
								if (start.getTime().equals(DateUtils.removeTime(reason.getEnd()))) {
									day.setEnd(reason.getEnd()); // last day keep end time
								} else {
									day.setEnd(DateUtils.setEndDayTimeToTheMinute(start.getTime())); // end at regular
																										// end of day
								}
							}

							// Increment loop
							start.add(Calendar.DATE, 1);

							reasons.add(day);
						}
					} else {
						// Create new UUID for PS
						if (StringUtils.isBlank(reason.getPeopleSoftId())) {
							reason.setPeopleSoftId(UUID.randomUUID().toString());
						}
					}
				} else {
					// Create new UUID for PS
					if (StringUtils.isBlank(reason.getPeopleSoftId())) {
						reason.setPeopleSoftId(UUID.randomUUID().toString());
					}
				}

				if (reasons.size() == 0) {
					reasons.add(reason);
				}

				// Save to DB
				List<UnavailabilityReason> persistedReasons = null;

				try {
					persistedReasons = persist.addUnavailability(bp.getPerson().getId(), reasons, principal.getName(),
							operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				for (UnavailabilityReason persistedReason : persistedReasons) {
					command = new CommandAddPersonUnavailability(boardKey.toId(), principal.getName(),
							reason.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(), persistedReason, ignoreIntegration);

					commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
					commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board

					// Send to all future work locations
					for (Location location : bp.getFutureWorkLocations()) {
						commandsMap.put(location.getCode(), command);
					}

					// Send out commands to locations
					sendCommands(principal.getName(), boardKey, commandsMap);
				}

				if (!ignoreIntegration) {
					for (UnavailabilityReason persistedReason : persistedReasons) {
						outgoingPersonnelService.setPersonUnavailable(persistedReason, false);
					}
				}
			}
		}
		logger.debug("Executed addUnavailabilityReason");
	}

	public void autoCompleteAssignedPersons(BoardKey boardKey, Task task, Date now, String user) throws OpsBoardError {

		BoardPerson bp1 = null;
		BoardPerson bp2 = null;
		String boardPersonId1 = null;
		String boardPersonId2 = null;
		String personId1 = null;
		String personId2 = null;
		if (task.getAssignedPerson1() != null && !task.getAssignedPerson1().isCompleted()
				&& task.getAssignedPerson1().getPerson() != null) {
			bp1 = task.getAssignedPerson1().getPerson();
			if(bp1 != null){
				boardPersonId1 = bp1.getId();
				personId1 = BoardPerson.EXTRACT_PERSON_ID(boardPersonId1);
			}
		}
		if (task.getAssignedPerson2() != null && !task.getAssignedPerson2().isCompleted()
				&& task.getAssignedPerson2().getPerson() != null) {
			bp2 = task.getAssignedPerson2().getPerson();
			if(bp2 != null){
				boardPersonId2 = bp2.getId();
				personId2 = BoardPerson.EXTRACT_PERSON_ID(boardPersonId2);
			}
		}

		// All parameters in command gets pushed back to all browser, and the
		// browser will update the board
		if (!StringUtils.isEmpty(personId1) || !StringUtils.isEmpty(personId2)) {
			AbstractMultiBoardCommand command = new CommandAutoCompletePersonnel(boardKey.toId(), user, new Date(),
					boardPersonId1, personId1, boardPersonId2, personId2);

			// Send to current location only
			sendCommands(null, boardKey, boardKey.getLocation().getCode(), command);
		}

	}

	public void cancelDetachPerson(BoardKey boardKey, BoardPerson bp, Principal principal, Detachment detachment)
			throws OpsBoardError, OpsBoardValidationException {

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();
		synchronized (bp) {
			synchronized (bp.getPerson()) {

				if (persist.isDetachmentReplaced(detachment)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add(ErrorMessage.DETACHMENT_ALREADY_MODIFIED.getMessage());
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				// Set operation time
				Date operationTime = new Date();

				// Get current MDA to delete
				Detachment oldDetachment = persist.findDetachmentById(detachment.getId());
				

				boolean hasUnfinishedTask =false;
				if(DateUtils.sameDay(DateUtils.toBoardDateNoNull(boardKey.getDate()), oldDetachment.getStartDate())){
					hasUnfinishedTask=persist.hasUnfinishedTask(bp.getPerson().getId(),boardKey.getLocation().getCode(),boardKey.getDate());
				}

				
				ValidationUtils.validateUpdateDetachment(bp, oldDetachment.getStartDate(), oldDetachment.getEndDate(), boardKey.getDate(), boardKey.getLocation(), hasUnfinishedTask, true);
				
	
				oldDetachment.setStatus(AbstractDetachment.DETACH_STATUS_INACTIVE);

				// Clone old Position and update with status, reason for change and system date
				Detachment newDetachment = (Detachment) CloneUtils.deepClone(oldDetachment);
				newDetachment.setReasonForChange(detachment.getReasonForChange());
				newDetachment.setStatus(AbstractDetachment.DETACH_STATUS_REMOVED);
				newDetachment.setSystemUser(principal.getName());
				newDetachment.setLastModifiedSystem(operationTime);
				newDetachment.setPeopleSoftId(oldDetachment.getPeopleSoftId());
				if (detachment.getLastModifiedActual() == null) {
					newDetachment.setLastModifiedActual(newDetachment.getLastModifiedSystem());
				} else {
					newDetachment.setLastModifiedActual(newDetachment.getLastModifiedActual());
				}
				if (detachment.getActualUser() == null || detachment.getActualUser().isEmpty()) {
					newDetachment.setActualUser(newDetachment.getSystemUser());
				} else {
					newDetachment.setActualUser(newDetachment.getActualUser());
				}
				newDetachment.setOriginalId(oldDetachment.getOriginalId());

				// Save to DB
				Detachment persistedDetachment = null;

				try {
					// Save to DB
					persistedDetachment = persist.removeDetachment(bp.getPerson().getId(), oldDetachment,
							newDetachment, principal.getName(), newDetachment.getLastModifiedSystem());
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				CommandCancelDetachPerson command = new CommandCancelDetachPerson(boardKey.toId(), principal.getName(),
						persistedDetachment.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(),
						persistedDetachment.getFrom(), persistedDetachment.getTo(), persistedDetachment);

				// send cancel to the "To" location
				commandsMap.put(persistedDetachment.getToCode(), command); // send to "current location" board

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				// send app person to the rest

				commandsMap = new HashMap<String, IMultiBoardCommand>();

				CommandAddOpsBoardPerson addCommand = new CommandAddOpsBoardPerson(boardKey.toId(),
						principal.getName(), operationTime, bp);

				commandsMap.put(persistedDetachment.getFromCode(), addCommand);
				commandsMap.put(bp.getWorkLocation().getCode(), addCommand); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), addCommand); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), addCommand);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				outgoingPersonnelService.removePersonDetachment(persistedDetachment);
			}

		}
		logger.debug("Executed cancelDetachPerson");
	}
	
	public void updateDetachPerson(BoardKey boardKey, BoardPerson bp, Principal principal, Detachment detachment)
			throws OpsBoardError, OpsBoardValidationException {

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();
		synchronized (bp) {
			synchronized (bp.getPerson()) {

				if (persist.isDetachmentReplaced(detachment)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add(ErrorMessage.DETACHMENT_ALREADY_MODIFIED.getMessage());
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				// Set operation time
				Date operationTime = new Date();

				// Get current detachment to update
				Detachment oldDetachment = persist.findDetachmentById(detachment.getId());
				
                boolean hasUnfinishedTask =false;
                if(DateUtils.onOrAfter(DateUtils.toBoardDateNoNull(boardKey.getDate()), DateUtils.removeTime(oldDetachment.getStartDate()))){
                     hasUnfinishedTask=persist.hasUnfinishedTask(bp.getPerson().getId(),boardKey.getLocation().getCode(),boardKey.getDate());
                }
				
				ValidationUtils.validateUpdateDetachment(bp, detachment.getEndDate(), oldDetachment.getEndDate(), boardKey.getDate(), boardKey.getLocation(), hasUnfinishedTask, false);
				
				oldDetachment.setStatus(AbstractDetachment.DETACH_STATUS_INACTIVE);

				if (DateUtils.before(oldDetachment.getEndDate(), DateUtils.removeTime(new Date()))) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add(ErrorMessage.DETACHMENT_PAST_CANCEL_UNALLOWED.getMessage());
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}	

				// Clone old Position and update with status, reason for change and system date
				Detachment newDetachment = (Detachment) CloneUtils.deepClone(oldDetachment);
				newDetachment.setEndDate(detachment.getEndDate());
				newDetachment.setReasonForChange(detachment.getReasonForChange());
				newDetachment.setStatus(AbstractDetachment.DETACH_STATUS_AVAILABLE);
				newDetachment.setSystemUser(principal.getName());
				newDetachment.setLastModifiedSystem(operationTime);
				newDetachment.setPeopleSoftId(oldDetachment.getPeopleSoftId());
				if (detachment.getLastModifiedActual() == null) {
					newDetachment.setLastModifiedActual(newDetachment.getLastModifiedSystem());
				} else {
					newDetachment.setLastModifiedActual(newDetachment.getLastModifiedActual());
				}
				if (detachment.getActualUser() == null || detachment.getActualUser().isEmpty()) {
					newDetachment.setActualUser(newDetachment.getSystemUser());
				} else {
					newDetachment.setActualUser(newDetachment.getActualUser());
				}
				newDetachment.setOriginalId(oldDetachment.getOriginalId());

				// Save to DB
				Detachment persistedDetachment = null;

				try {
					// Save to DB
					persistedDetachment = persist.updateDetachment(bp.getPerson().getId(), oldDetachment,
							newDetachment, principal.getName(), newDetachment.getLastModifiedSystem());
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				CommandUpdateDetachPerson command = new CommandUpdateDetachPerson(boardKey.toId(),
						persistedDetachment.getSystemUser(), persistedDetachment.getLastModifiedSystem(), bp.getId(),
						bp.getPerson().getId(),persistedDetachment.getFrom(), persistedDetachment.getTo(), persistedDetachment,oldDetachment.getEndDate());

				// send update to the "To" "From" "Current" location
				commandsMap.put(persistedDetachment.getToCode(), command); 
				commandsMap.put(persistedDetachment.getFromCode(), command);
				commandsMap.put(bp.getHomeLocation().getCode(), command);

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				// send app person to the rest

				commandsMap = new HashMap<String, IMultiBoardCommand>();

				CommandAddOpsBoardPerson addCommand = new CommandAddOpsBoardPerson(boardKey.toId(),
						principal.getName(), operationTime, bp);
				
					commandsMap.put(bp.getWorkLocation().getCode(), addCommand);
					commandsMap.put(bp.getHomeLocation().getCode(), addCommand);
			
					/*commandsMap.put(persistedDetachment.getToCode(), addCommand);*/		

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), addCommand);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);
				
				outgoingPersonnelService.updatePersonDetachment(persistedDetachment);
			}

		}
		logger.debug("Executed updateDetachPerson");
	}	

	public List<String> massChartUpdate(BoardKey boardKey,
			BoardKey chartKey, Date formattedChartDate, List<BoardPerson> cancelBoardPersons,
			List<BoardPerson> reverseCancelBoardPersons, Principal principal) throws OpsBoardError, OpsBoardValidationException{
		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();
		Map<String,UnavailabilityReason> cancelledReasons = new HashMap<String,UnavailabilityReason>();
		Map<String,UnavailabilityReason> reverseCancelledReasons = new HashMap<String,UnavailabilityReason>();
		Date systemTime = new Date();
		List<String> extErrMessages = new ArrayList<String>();

		// Cancel Multiple Charts
		for(BoardPerson bp: cancelBoardPersons){
			synchronized (bp) {
				synchronized (bp.getPerson()) {
					UnavailabilityReason oldReason = persist.getActiveUnavailabilityReason(formattedChartDate, bp.getPerson().getId());
					if(oldReason!=null){
						if (persist.isUnavailabilityReasonReplaced(oldReason)) {
							extErrMessages.add("This unavailability was already modified for person: "+bp.getPerson().getFullName());
							continue;
						}
						try{
							List<UnavailabilityReason> unavailabilityReasons = persist.findByEndDateAndActive( bp.getDate(),bp.getPerson().getId());
							validateUnavailabilityReason(oldReason.getId(), oldReason.getCode(),oldReason.getStart(), oldReason.getEnd(),
									oldReason.getComments(),unavailabilityReasons, bp, "CANCEL_UNAVAILABLE", boardKey);
						}catch(OpsBoardValidationException e){
							extErrMessages.add(e.getMessage()+"for person: "+bp.getPerson().getId());
							continue;
						}

						// Set operation time
						Date operationTime = new Date();

						oldReason.setStatus("I");
						oldReason.setAction("C");

						// Clone old Reason and update with status, reason for change and system date
						UnavailabilityReason newReason = (UnavailabilityReason) CloneUtils.deepClone(oldReason);
						newReason.setStatus("A");
						newReason.setAction("C");
						newReason.setStart(oldReason.getStart());
						newReason.setEnd(oldReason.getEnd());
						newReason.setComments(oldReason.getComments());
						newReason.setSystemUser(principal.getName());
						newReason.setLastModifiedSystem(operationTime);
						newReason.setPeopleSoftId(oldReason.getPeopleSoftId());
						// Save to DB
						UnavailabilityReason persistedReason = null;

						try {
							// Save to DB
							persistedReason = persist.updateUnavailability(bp.getPerson().getId(), oldReason, newReason,
									principal.getName(), operationTime);
							cancelledReasons.put(bp.getPerson().getId(), persistedReason);
						} catch (Exception e) {
							extErrMessages.add(e.getMessage()+"for person: "+bp.getPerson().getId());
							continue;
						}
					}

				}
			}
		}
		// Reverse Cancel Multiple Charts
		for(BoardPerson bp: reverseCancelBoardPersons){
			synchronized (bp) {
				synchronized (bp.getPerson()) {
					UnavailabilityReason oldReason = persist.getCancelledUnavailabilityReason(formattedChartDate, bp.getPerson().getId());
					if(oldReason != null){
						if (persist.isUnavailabilityReasonReplaced(oldReason)) {
							if (persist.isUnavailabilityReasonReplaced(oldReason)) {
								extErrMessages.add("This unavailability was already modified for person: "+bp.getPerson().getFullName());
								continue;
							}
						}
						try{
							List<UnavailabilityReason> unavailabilityReasons = persist.findByEndDateAndActive( bp.getDate(),bp.getPerson().getId());
							validateUnavailabilityReason(oldReason.getId(), oldReason.getCode(),oldReason.getStart(), oldReason.getEnd(),
									oldReason.getComments(),unavailabilityReasons, bp, "REVERSE_CANCEL_UNAVAILABLE", boardKey);
						}catch(OpsBoardValidationException e){
							extErrMessages.add(e.getMessage()+"for person: "+bp.getPerson().getId());
							continue;
						}

						// Set operation time
						Date operationTime = new Date();

						oldReason.setStatus("I");
						oldReason.setAction("R");

						// Clone old Reason and update with status, reason for change and system date
						UnavailabilityReason newReason = (UnavailabilityReason) CloneUtils.deepClone(oldReason);
						newReason.setStatus("A");
						newReason.setAction("R");
						newReason.setStart(oldReason.getStart());
						newReason.setEnd(oldReason.getEnd());
						newReason.setComments(oldReason.getComments());
						newReason.setSystemUser(principal.getName());
						newReason.setLastModifiedSystem(operationTime);
						newReason.setPeopleSoftId(oldReason.getPeopleSoftId());
						if (oldReason.getLastModifiedActual() == null) {
							newReason.setLastModifiedActual(newReason.getLastModifiedSystem());
						} else {
							newReason.setLastModifiedActual(oldReason.getLastModifiedActual());
						}
						if (oldReason.getActualUser() == null || oldReason.getActualUser().isEmpty()) {
							newReason.setActualUser(newReason.getSystemUser());
						} else {
							newReason.setActualUser(oldReason.getActualUser());
						}
						// Save to DB
						UnavailabilityReason persistedReason = null;

						try {
							// Save to DB
							persistedReason = persist.updateUnavailability(bp.getPerson().getId(), oldReason, newReason,
									principal.getName(), operationTime);
							reverseCancelledReasons.put(bp.getPerson().getId(), persistedReason);
						} catch (Exception e) {
							extErrMessages.add(e.getMessage()+"for person: "+bp.getPerson().getId());
							continue;
						}
					}

				}
			}
		}
		// Construct command to send to clients (location boards, database...)
		CommandMassChartUpdate command = new CommandMassChartUpdate(boardKey.toId(),
				principal.getName(), cancelledReasons,reverseCancelledReasons,systemTime);

		// Send to all future work locations
		for(BoardPerson bp: cancelBoardPersons){
			commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
			commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board
			for (Location location : bp.getFutureWorkLocations()) {
				commandsMap.put(location.getCode(), command);
			}
		}

		for(BoardPerson bp: reverseCancelBoardPersons){
			commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
			commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board
			for (Location location : bp.getFutureWorkLocations()) {
				commandsMap.put(location.getCode(), command);
			}
		}
		// Send out commands to board Date
		sendCommands(principal.getName(), boardKey, commandsMap);
		//send out commands to Chart date
		if(!boardKey.getDate().equalsIgnoreCase(chartKey.getDate())){
			sendCommands(principal.getName(), chartKey, commandsMap);
		}
		//send to peoplesoft for cancel
		for(String personId: cancelledReasons.keySet()){
			UnavailabilityReason cancelledReason = cancelledReasons.get(personId);
			outgoingPersonnelService.cancelPersonUnavailableReason(cancelledReason);
		}
		//send to peoplesoft for reverse cancel
		for(String personId: reverseCancelledReasons.keySet()){
			UnavailabilityReason reverseCancelledReason = reverseCancelledReasons.get(personId);
			outgoingPersonnelService.reverseCancelPersonUnavailability(reverseCancelledReason);
		}
		return extErrMessages;
	}
	
	public void cancelUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		if (reason.getUnavailabilityType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			reason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(reason.getCode(), date));
		}

		synchronized (bp) {
			synchronized (bp.getPerson()) {
				//Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				//BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				//bpFromDb.setPerson(pFromDb);
				//bpFromDb.copyTransientProperties(bp);
				if (persist.isUnavailabilityReasonReplaced(reason)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This uanvailability was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				List<UnavailabilityReason> unavailabilityReasons = persist.findByEndDateAndActive( bp.getDate(),bp.getPerson().getId());
				validateUnavailabilityReason(reason.getId(), reason.getCode(), reason.getStart(), reason.getEnd(),
						reason.getComments(),unavailabilityReasons, bp, "CANCEL_UNAVAILABLE", boardKey);

				// Set operation time
				Date operationTime = new Date();

				// Get current Reason to cancel
				UnavailabilityReason oldReason = bp.getUnavailabilityReason(reason.getId());
				oldReason.setStatus("I");
				oldReason.setAction("C");

				// Clone old Reason and update with status, reason for change and system date
				UnavailabilityReason newReason = (UnavailabilityReason) CloneUtils.deepClone(oldReason);
				newReason.setStatus("A");
				newReason.setAction("C");
				newReason.setStart(reason.getStart());
				newReason.setEnd(reason.getEnd());
				newReason.setComments(reason.getComments());
				newReason.setSystemUser(principal.getName());
				newReason.setLastModifiedSystem(operationTime);
				newReason.setActualUser(reason.getActualUser());
				newReason.setPeopleSoftId(oldReason.getPeopleSoftId());

				// Save to DB
				UnavailabilityReason persistedReason = null;

				try {
					// Save to DB
					persistedReason = persist.updateUnavailability(bp.getPerson().getId(), oldReason, newReason,
							principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandCancelPersonUnavailability command = new CommandCancelPersonUnavailability(boardKey.toId(),
						principal.getName(), persistedReason.getLastModifiedSystem(), bp.getId(), bp.getPerson()
						.getId(), persistedReason);

				commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}
				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				// Send request to PeopleSoft
				outgoingPersonnelService.cancelPersonUnavailableReason(persistedReason);

			}
		}
		logger.debug("Executed cancelUnavailabilityReason");
	}

	public void detachPerson(BoardKey boardKey, BoardPerson bp, Principal principal, Detachment detachment)
			throws OpsBoardError, OpsBoardValidationException {

		// Perform action on SOR and then DB
		synchronized (bp) {
			synchronized (bp.getPerson()) {


				List<UnavailabilityReason> unavailabilityReasons = persist.findUnavailabilityByDateRangeAndActive(detachment.getStartDate(), detachment.getEndDate(), bp.getPerson().getId());
				List<Detachment> detachments = persist.findCurrentAndFutureDetachments(bp.getDate(), boardKey.getShiftsEnd(),bp.getPerson().getId());

				boolean hasUnfinishedTask =false;
				Date today = DateUtils.getTodayWith12AM();// validate
				
				if(DateUtils.sameDay(DateUtils.toBoardDateNoNull(boardKey.getDate()), detachment.getStartDate())){
					hasUnfinishedTask=persist.hasUnfinishedTask(bp.getPerson().getId(),boardKey.getLocation().getCode(),boardKey.getDate());
				}
				ValidationUtils.validateDetachPerson(boardKey.getLocation(), boardKey.getDate(), detachment.getFrom()
						.getCode(), detachment.getTo().getCode(), detachment.getStartDate(), detachment.getEndDate(),

						detachment.getComments(), unavailabilityReasons, detachments, bp, hasUnfinishedTask);



				HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

				// Set system data
				Date operationTime = new Date();
				detachment.setSystemUser(principal.getName());
				detachment.setLastModifiedSystem(operationTime);
				if (detachment.getLastModifiedActual() == null) {
					detachment.setLastModifiedActual(detachment.getLastModifiedSystem());
				}
				if (detachment.getActualUser() == null || detachment.getActualUser().isEmpty()) {
					detachment.setActualUser(detachment.getSystemUser());
				}

				// Updated record status
				detachment.setStatus(AbstractDetachment.DETACH_STATUS_AVAILABLE);

				if (StringUtils.isBlank(detachment.getPeopleSoftId())) {
					detachment.setPeopleSoftId(UUID.randomUUID().toString());
				}

				boardKey.toId();

				Detachment persistedDetachment = null;

				// Save to DB
				try {
					persistedDetachment = persist.detach(bp.getPerson().getId(), detachment);
				} catch (PersonDetachmentOverlapTaskException e) {
					logger.error(ErrorMessage.SERVER_VALIDATION_ERROR.getMessage(), e);
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add(e.getMessage());
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandDetachPerson detachCommand = new CommandDetachPerson(boardKey.toId(), bp.getId(), bp.getPerson()
						.getId(), persistedDetachment.getFrom(), persistedDetachment.getTo(), persistedDetachment);

				CommandAddOpsBoardPerson addCommand = new CommandAddOpsBoardPerson(boardKey.toId(),
						principal.getName(), persistedDetachment.getLastModifiedSystem(), bp, false);

				// Tie commands to locations
				// add command to "To" board
				commandsMap.put(persistedDetachment.getTo().getCode(), addCommand); // send to "to" board,

				commandsMap.put(persistedDetachment.getFrom().getCode(), detachCommand); // send to "from" board,
				// removing has lowest

				commandsMap.put(bp.getHomeLocation().getCode(), detachCommand);// owner

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				// send to all future detachment to and from board excluding owner and from but including current
				// detached to
				sendPersonUpdatesForDetachedBoards(today, principal.getName(), boardKey,
						persistedDetachment.getLastModifiedSystem(), bp, bp.getWorkLocation().getCode(), bp
								.getHomeLocation().getCode(), persistedDetachment.getFrom().getCode(),
						persistedDetachment.getTo().getCode());

				outgoingPersonnelService.detachPerson(persistedDetachment);
			}
		}
		logger.debug("Executed detachPerson");
	}

	public void removeMdaStatus(BoardKey boardKey, BoardPerson bp, MdaStatus mda, Principal principal)
			throws OpsBoardError, OpsBoardValidationException {

		if (mda.getMdaType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			mda.setMdaType(mdaTypeCacheService.getMdaType(mda.getSubType(), date));
		}

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		synchronized (bp) {
			synchronized (bp.getPerson()) {

				Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				bpFromDb.setPerson(pFromDb);
				bpFromDb.copyTransientProperties(bp);

				if (persist.isMdaStatusReplaced(mda)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This mda was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}

				// Set oepration time
				Date operationTime = new Date();

				// Get current MDA to delete
				MdaStatus oldMda = bp.getMdaStatus(mda.getId());
				oldMda.setStatus("I");

				// Clone old Position and update with status, reason for change and system date
				MdaStatus newMda = (MdaStatus) CloneUtils.deepClone(oldMda);
				newMda.setReasonForChange(mda.getReasonForChange());
				newMda.setStatus("R");
				newMda.setSystemUser(principal.getName());
				newMda.setLastModifiedSystem(operationTime);
				if (mda.getLastModifiedActual() == null) {
					newMda.setLastModifiedActual(newMda.getLastModifiedSystem());
				} else {
					newMda.setLastModifiedActual(mda.getLastModifiedActual());
				}
				if (mda.getActualUser() == null || mda.getActualUser().isEmpty()) {
					newMda.setActualUser(newMda.getSystemUser());
				} else {
					newMda.setActualUser(mda.getActualUser());
				}
				newMda.setOriginalId(oldMda.getOriginalId());
				newMda.setPeopleSoftId(oldMda.getPeopleSoftId());

				// Save to DB
				MdaStatus persistedMda = null;
				try {
					persistedMda = persist.removeMdaStatus(bp.getPerson().getId(), oldMda, newMda, principal.getName(),
							operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandRemovePersonMdaStatus commandRemoveNew = new CommandRemovePersonMdaStatus(boardKey.toId(),
						principal.getName(), persistedMda.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(),
						persistedMda);

				commandsMap.put(bp.getWorkLocation().getCode(), commandRemoveNew); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), commandRemoveNew); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), commandRemoveNew);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				// Send message to PeopleSoft
				outgoingPersonnelService.setPersonMDA(persistedMda, true);
			}
		}
		logger.debug("Executed removeMdaStatus");
	}

	public void removeSpecialPosition(BoardKey boardKey, BoardPerson bp, SpecialPosition specialPosition,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		removeSpecialPosition(boardKey, bp, specialPosition, principal, false);
	}

	public void removeSpecialPosition(BoardKey boardKey, BoardPerson bp, SpecialPosition specialPosition,
			Principal principal, boolean ignoreIntegration) throws OpsBoardError, OpsBoardValidationException {

		if (specialPosition.getSpecialPositionType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			specialPosition.setSpecialPositionType(specialPositionTypeCacheService.getSpecialPositionType(
					specialPosition.getCode(), date));
		}

		CommandRemoveSpecialPosition removeCommand = null;
		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		synchronized (bp) {
			synchronized (bp.getPerson()) {

				if (persist.isSpecialPositionReplaced(specialPosition)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This special position was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}

				// Set operation time
				Date operationTime = new Date();

				// Get current Position to delete
				SpecialPosition oldSpecialPosition = bp.getSpecialPosition(specialPosition.getId());

				if (oldSpecialPosition.getOfficerPosition() != null && oldSpecialPosition.getOfficerPosition() == true
						&& !ignoreIntegration) {
					throw new OpsBoardError(ErrorMessage.CANNOT_MODIFY_OFFICER_POSITION);
				}

				oldSpecialPosition.setStatus("I");

				// Clone old MDA and update with status, reason for change and system date
				SpecialPosition newSpecialPosition = (SpecialPosition) CloneUtils.deepClone(oldSpecialPosition);
				newSpecialPosition.setReasonForChange(specialPosition.getReasonForChange());
				newSpecialPosition.setStatus("R");
				newSpecialPosition.setSystemUser(principal.getName());
				newSpecialPosition.setLastModifiedSystem(operationTime);
				if (specialPosition.getLastModifiedActual() == null) {
					newSpecialPosition.setLastModifiedActual(newSpecialPosition.getLastModifiedSystem());
				} else {
					newSpecialPosition.setLastModifiedActual(specialPosition.getLastModifiedActual());
				}
				if (specialPosition.getActualUser() == null || specialPosition.getActualUser().isEmpty()) {
					newSpecialPosition.setActualUser(newSpecialPosition.getSystemUser());
				} else {
					newSpecialPosition.setActualUser(specialPosition.getActualUser());
				}
				specialPosition.setPeopleSoftId(oldSpecialPosition.getPeopleSoftId());

				// Save to DB
				SpecialPosition persistedSpecialPosition = null;
				try {
					// Save to DB
					persistedSpecialPosition = persist.removeSpecialPosition(bp.getPerson().getId(),
							oldSpecialPosition, newSpecialPosition, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				removeCommand = new CommandRemoveSpecialPosition(boardKey.toId(), principal.getName(),
						persistedSpecialPosition.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(),
						persistedSpecialPosition, ignoreIntegration);

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), removeCommand);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, bp.getHomeLocation().getCode(), removeCommand, bp
						.getWorkLocation().getCode(), removeCommand);
				sendCommands(principal.getName(), boardKey, commandsMap);

				if (!ignoreIntegration) {
					outgoingPersonnelService.setPersonSpecialPosition(persistedSpecialPosition, true);
				}

			}
		}
		logger.debug("Executed removeSpecialPosition");
	}

	public void removeUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		removeUnavailabilityReason(boardKey, bp, reason, principal, false);
	}

	public void removeUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal, boolean ignoreIntegration) throws OpsBoardError, OpsBoardValidationException {

		if (reason.getUnavailabilityType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			reason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(reason.getCode(), date));
		}

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		synchronized (bp) {
			synchronized (bp.getPerson()) {
				
				//Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				//BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				//bpFromDb.setPerson(pFromDb);
				//bpFromDb.copyTransientProperties(bp);
				if (persist.isUnavailabilityReasonReplaced(reason)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This uanvailability was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				List<UnavailabilityReason> unavailabilityReasons = persist.findByEndDateAndActive( bp.getDate(),bp.getPerson().getId());
				validateUnavailabilityReason(reason.getId(), reason.getCode(), reason.getStart(), reason.getEnd(),
						reason.getComments(),unavailabilityReasons, bp, "REMOVE_UNAVAILABLE", boardKey);
				// Set operation time
				Date operationTime = new Date();

				// Get current Reason to delete
				UnavailabilityReason oldReason = bp.getUnavailabilityReason(reason.getId());
				String oldStatus = oldReason.getStatus();
				oldReason.setStatus("I");
				oldReason.setAction("D");

				// Clone old Reason and update with status, reason for change and system date
				UnavailabilityReason newReason = (UnavailabilityReason) CloneUtils.deepClone(oldReason);
				newReason.setReasonForChange(reason.getReasonForChange());
				newReason.setStatus("R");
				newReason.setAction("D");
				newReason.setSystemUser(principal.getName());
				newReason.setLastModifiedSystem(operationTime);
				if (reason.getLastModifiedActual() == null) {
					newReason.setLastModifiedActual(newReason.getLastModifiedSystem());
				} else {
					newReason.setLastModifiedActual(reason.getLastModifiedActual());
				}
				if (reason.getActualUser() == null || reason.getActualUser().isEmpty()) {
					newReason.setActualUser(newReason.getSystemUser());
				} else {
					newReason.setActualUser(reason.getActualUser());
				}

				// Save to DB
				UnavailabilityReason persistedReason = null;

				try {
					// Save to DB
					persistedReason = persist.removeUnavailability(bp.getPerson().getId(), oldReason, newReason,
							principal.getName(), operationTime);
				} catch (Exception e) {
					// Revert changes to old reason
					oldReason.setStatus(oldStatus);
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandRemovePersonUnavailability command = new CommandRemovePersonUnavailability(boardKey.toId(),
						principal.getName(), persistedReason.getLastModifiedSystem(), bp.getId(), bp.getPerson()
						.getId(), persistedReason, ignoreIntegration);

				commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}

				if (!ignoreIntegration) {
					outgoingPersonnelService.setPersonUnavailable(persistedReason, true);
				}
			}

			// Send out commands to locations
			sendCommands(principal.getName(), boardKey, commandsMap);

		}
		logger.debug("Executed removeUnavailabilityReason");
	}

	public void reverseCancelUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		if (reason.getUnavailabilityType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			reason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(reason.getCode(), date));
		}

		synchronized (bp) {
			synchronized (bp.getPerson()) {

				//Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				//BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				//bpFromDb.setPerson(pFromDb);
				//bpFromDb.copyTransientProperties(bp);

				if (persist.isUnavailabilityReasonReplaced(reason)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This uanvailability was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				List<UnavailabilityReason> unavailabilityReasons = persist.findByEndDateAndActive( bp.getDate(),bp.getPerson().getId());
				validateUnavailabilityReason(reason.getId(), reason.getCode(), reason.getStart(), reason.getEnd(),
						reason.getComments(),unavailabilityReasons, bp, "REVERSE_CANCEL_UNAVAILABLE", boardKey);

				// Set operation time
				Date operationTime = new Date();

				// Get current Reason to delete
				UnavailabilityReason oldReason = bp.getUnavailabilityReason(reason.getId());
				oldReason.setStatus("I"); // it was Active now it will be Hidden
				oldReason.setAction("R");
				// the Action is already in cancelled, so we keep it

				// Clone old Reason and update with status, reason for change and system date
				UnavailabilityReason newReason = (UnavailabilityReason) CloneUtils.deepClone(oldReason);
				newReason.setComments(reason.getComments());
				newReason.setStatus("A"); // Active
				newReason.setAction("R"); // reinstated
				newReason.setSystemUser(principal.getName());
				newReason.setLastModifiedSystem(operationTime);
				if (reason.getLastModifiedActual() == null) {
					newReason.setLastModifiedActual(newReason.getLastModifiedSystem());
				} else {
					newReason.setLastModifiedActual(reason.getLastModifiedActual());
				}
				if (reason.getActualUser() == null || reason.getActualUser().isEmpty()) {
					newReason.setActualUser(newReason.getSystemUser());
				} else {
					newReason.setActualUser(reason.getActualUser());
				}
				newReason.setPeopleSoftId(oldReason.getPeopleSoftId());

				// Save to DB
				UnavailabilityReason persistedReason = null;

				try {
					// Save to DB
					persistedReason = persist.updateUnavailability(bp.getPerson().getId(), oldReason, newReason,
							principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandReverseCancelPersonUnavailability command = new CommandReverseCancelPersonUnavailability(
						boardKey.toId(), principal.getName(), persistedReason.getLastModifiedSystem(), bp.getId(), bp
						.getPerson().getId(), persistedReason);

				commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}
				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);

				// Send request to PeopleSoft
				outgoingPersonnelService.reverseCancelPersonUnavailability(persistedReason);
			}
		}

		logger.debug("Executed reverseCancelUnavailabilityReason");
	}

	public void setPerson(BoardKey boardKey, Person person, Principal principal) throws OpsBoardError {

		if (newPersonnelIds.get(person.getId()) == null)
			newPersonnelIds.put(person.getId(), person.getId());
			
		synchronized (newPersonnelIds.get(person.getId())) {
		
			Set<Location> addPersonLocations = new HashSet<Location>();
			Set<Location> updatePersonLocations = new HashSet<Location>();
			Set<Location> removePersonLocations = new HashSet<Location>();
			HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();
			boolean isTransfer = false;

			Person existingPerson = personnelRepository.findOne(person.getId());
			boolean isNewPerson = existingPerson == null ? true : false;			

			Date boardStartDate = boardKey.getShiftsStart();
			Date boardEndDate = boardKey.getShiftsEnd();
			BoardPerson temp = new BoardPerson(person, boardKey.getDate(), boardEndDate, boardStartDate);

			// Create keys for all existing future boards
			Location homeLocation = person.getHomeLocation();
			Set<String> boardDates = persist.findExistingBoardPersonDates(DateUtils.removeTime(new Date()), homeLocation);
			Set<BoardKey> boardKeys = new HashSet<BoardKey>();
			for (String boardDate : boardDates)
			{
				boardKeys.add(boardKeyFactory.createBoardKey (boardDate, homeLocation));
			}
			
			boolean resurrected = false;
			if(isNewPerson)
			{
				// Adding brand new person				
				persist.addNewPerson(person, boardKeys);
				addPersonLocations.add(person.getHomeLocation());
			}
			else
			{
				resurrected = person.isActive() && !existingPerson.isActive();
				
				// Update existing person
				Location newHomeLocation = person.getHomeLocation();
				Location existingHomeLocation = existingPerson.getHomeLocation();
				isTransfer = !newHomeLocation.equals(existingHomeLocation);

				List<Detachment> activeDetachments = temp.getActiveDetachments();
				List<Detachment> futureDetachments = temp.getFutureDetachments();
				if (isTransfer)
				{
					addPersonLocations.add(newHomeLocation);
					removePersonLocations.add(existingHomeLocation);
					cleanupDetachments(activeDetachments, futureDetachments, person, removePersonLocations);
					persist.updatePersonTransfer(person, boardKeys);
				}
				else
				{
					updateDetachments(activeDetachments, person, addPersonLocations, updatePersonLocations, removePersonLocations);
					updateDetachments(futureDetachments, person, addPersonLocations, updatePersonLocations, removePersonLocations);
					persist.updatePerson(person, true, boardKeys);
				}
			}
			
			//*********************************************************************************************************
			// ******************************** Send commands *********************************************************
			//*********************************************************************************************************
			if (person.isActive()) 
			{
				if (resurrected)
				{
					updatePersonLocations.clear();
					removePersonLocations.clear();
					addPersonLocations.clear();
					addPersonLocations.add(person.getHomeLocation());
				}
				else
				{
					// Clean up add & remove locations list from updated locations
					for( Location location : updatePersonLocations)
					{
						addPersonLocations.remove(location);
						removePersonLocations.remove(location);
					}
					
					// Clean up remove locations list from added locations
					for( Location location : addPersonLocations)
					{
						removePersonLocations.remove(location);
					}
				}
				
				// Prepare ADD commands
				for( Location location : addPersonLocations)
				{
					AbstractOpsBoardPersonnelCommand personAddCommand = new CommandAddOpsBoardPerson (createBoardKey(location).toId(),
						person.getSystemUser(), person.getLastModifiedSystem(), person.getId(), true, false);
					commandsMap.put(location.getCode(), personAddCommand);
				}
				updatePersonLocations.add(person.getHomeLocation());
				// Prepare UPDATE commands
				for( Location location : updatePersonLocations)
				{
					AbstractOpsBoardPersonnelCommand personUpdateCommand = new CommandAddOpsBoardPerson (createBoardKey(location).toId(),
						person.getSystemUser(), person.getLastModifiedSystem(), person.getId(), true, true);
					commandsMap.put(location.getCode(), personUpdateCommand);
				}
			}
			else
			{
				// Move all "ADD" locations to remove list
				for( Location location : addPersonLocations)
				{
					removePersonLocations.add(location);
				}
				addPersonLocations.clear();
				
				// Move all "UPDATE" locations to remove list
				for( Location location : updatePersonLocations)
				{
					removePersonLocations.add(location);
				}
				updatePersonLocations.clear();
				
				if (!removePersonLocations.contains(person.getHomeLocation()))
					removePersonLocations.add(person.getHomeLocation());
			}
			
			// Prepare remove commands
			for( Location location : removePersonLocations)
			{
				AbstractOpsBoardPersonnelCommand personRemoveCommand = new CommandRemoveOpsBoardPerson (createBoardKey(location).toId(),
					person.getSystemUser(), person.getLastModifiedSystem(), person.getId(), true, true);
				commandsMap.put(location.getCode(), personRemoveCommand);
			}

			
			// Send out commands to locations
			sendCommands(principal.getName(), boardKey, commandsMap);
		}

		logger.debug("Executed setPerson");
	}

	private BoardKey createBoardKey (Location location) throws OpsBoardError
	{
		return boardKeyFactory.createBoardKey(new Date(), location);
	}
	
	private void updateDetachments(List<Detachment> detachments, Person person,
			Set<Location> addPersonLocations, Set<Location> updatePersonLocations, Set<Location> removePersonLocations)
	{
		if(detachments != null && !detachments.isEmpty())
		{
			detachments.forEach(ad -> 
			{
				//****************************************************************************************************
				// Adjust FROM location
				//****************************************************************************************************
				Location districtGarageParentFromLocation = getDistrictGarageParentLocation(ad.getFrom());
				if (districtGarageParentFromLocation.equals(ad.getFrom()))
				{
					updatePersonLocations.add(ad.getFrom());
				}
				else
				{
					removePersonLocations.add(ad.getFrom());
					addPersonLocations.add(districtGarageParentFromLocation);
					ad.setFrom(districtGarageParentFromLocation);
				}
				
				//****************************************************************************************************
				// Adjust TO location
				//****************************************************************************************************
				Location districtGarageParentToLocation = getDistrictGarageParentLocation(ad.getTo());
				if (districtGarageParentToLocation.equals(ad.getTo()))
				{
					updatePersonLocations.add(ad.getTo());
				}
				else
				{
					removePersonLocations.add(ad.getTo());
					addPersonLocations.add(districtGarageParentToLocation);
					ad.setTo(districtGarageParentToLocation);
				}
			});
		}
	}
	
	
	private void cleanupDetachments(List<Detachment> activeDetachments, List<Detachment> futureDetachments, 
			Person person,	Set<Location> removePersonLocations)
	{
		if(activeDetachments != null && !activeDetachments.isEmpty())
		{
			for(Detachment detachment : activeDetachments)
			{
				person.getDetachmentHistory().remove(detachment);
				removePersonLocations.add(detachment.getFrom());
				removePersonLocations.add(detachment.getTo());
			}
		}
		
		if (futureDetachments != null && !futureDetachments.isEmpty())
		{
			for(Detachment detachment : futureDetachments)
			{
				person.getDetachmentHistory().remove(detachment);
				removePersonLocations.add(detachment.getFrom());
				removePersonLocations.add(detachment.getTo());
			}
		}
		
		return;
	}
	
	private Location getDistrictGarageParentLocation(Location location)
	{
		if(StringUtils.equals(location.getType(), Location.DISTRICT_GARAGE_TYPE))
		{
			if(org.apache.commons.collections.CollectionUtils.isNotEmpty(location.getServiceParents()))
			{
				return location.getServiceParents().iterator().next();
			}
		}
		return location;
	}
	
	
	public void setPersonGrounding(BoardKey boardKey, BoardPerson bp, GroundingStatus groundingStatus,
			Principal principal) throws OpsBoardError {

		// Perform action on SOR and then DB
		synchronized (bp) {
			synchronized (bp.getPerson()) {

				HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

				// Save to DB
				try {
					persist.addGroundingStatus(groundingStatus);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				CommandGroundOpsBoardPerson commandGroundOpsBoardPerson = new CommandGroundOpsBoardPerson(
						boardKey.toId(), groundingStatus.getSystemUser(), groundingStatus.getLastModifiedSystem(), bp,
						groundingStatus);

				commandsMap.put(bp.getWorkLocation().getCode(), commandGroundOpsBoardPerson);
				commandsMap.put(bp.getHomeLocation().getCode(), commandGroundOpsBoardPerson);

				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), commandGroundOpsBoardPerson);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, commandsMap);
			}
		}

		logger.debug("Executed setPersonGrounding");
	}

	public void updateMdaStatus(BoardKey boardKey, BoardPerson bp, MdaStatus mda, Principal principal)
			throws OpsBoardError, OpsBoardValidationException {

		if (mda.getMdaType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			mda.setMdaType(mdaTypeCacheService.getMdaType(mda.getSubType(), date));
		}

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		synchronized (bp) {
			synchronized (bp.getPerson()) {

				List<MdaStatus> mdaStatus = persist.findMdaByEndDateAndActive(mda.getEndDate(), bp.getPerson().getId());
				Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				bpFromDb.setPerson(pFromDb);
				bpFromDb.copyTransientProperties(bp);

				if (persist.isMdaStatusReplaced(mda)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This mda was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				
				// Validate if there is another overlapping record
				if (!ValidationUtils.isValid(mda.getId(), null, mda.getStartDate(), mda.getEndDate(), mda.getComments(),
						mdaStatus,bp, "MDA", boardKey)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("Record for this date range already exists");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}

				// Set system data
				Date operationTime = new Date();
				mda.setSystemUser(principal.getName());
				mda.setLastModifiedSystem(operationTime);
				if (mda.getLastModifiedActual() == null) {
					mda.setLastModifiedActual(mda.getLastModifiedSystem());
				}
				if (mda.getActualUser() == null || mda.getActualUser().isEmpty()) {
					mda.setActualUser(mda.getSystemUser());
				}

				// Updated record status and change information
				mda.setStatus("A");
				mda.setReasonForChange(mda.getReasonForChange());

				// Get mda record to update (i.e. make old)
				MdaStatus oldMda = bp.getMdaStatus(mda.getId());
				oldMda.setStatus("I");
				mda.setOriginalId(oldMda.getOriginalId());
				mda.setPeopleSoftId(oldMda.getPeopleSoftId());

				// Save to DB
				MdaStatus persistedMda = null;
				try {
					persistedMda = persist.updateMdaStatus(bp.getPerson().getId(), oldMda, mda, principal.getName(),
							operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandUpdatePersonMdaStatus command = new CommandUpdatePersonMdaStatus(boardKey.toId(),
						principal.getName(), persistedMda.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(),
						persistedMda);

				commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}

				// necessery
				sendCommands(principal.getName(), boardKey, commandsMap);

				outgoingPersonnelService.setPersonMDA(persistedMda, false);

			}
		}
		logger.debug("Executed updateMdaStatus");
	}

	public void updateSpecialPosition(BoardKey boardKey, BoardPerson bp, SpecialPosition specialPosition,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		updateSpecialPosition(boardKey, bp, specialPosition, principal, false);
	}

	public void updateSpecialPosition(BoardKey boardKey, BoardPerson bp, SpecialPosition specialPosition,
			Principal principal, boolean ignoreIntegration) throws OpsBoardError, OpsBoardValidationException {

		if (specialPosition.getSpecialPositionType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			specialPosition.setSpecialPositionType(specialPositionTypeCacheService.getSpecialPositionType(
					specialPosition.getCode(), date));
		}

		CommandUpdateSpecialPosition command = null;
		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		synchronized (bp) {
			synchronized (bp.getPerson()) {
							
				if (persist.isSpecialPositionReplaced(specialPosition)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This special position was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}

				List<SpecialPosition> specialPositions = persist.findSpecialPositionsByEndDateAndActive(bp.getDate(), bp.getPerson().getId());

				// Validate if there is another overlapping record
				if (!ValidationUtils.isValid(specialPosition.getId(), specialPosition.getCode(),
						specialPosition.getStartDate(), specialPosition.getEndDate(), specialPosition.getComments(),specialPositions, bp,
						"SPECIAL_POSITION", boardKey)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("Record for this date range already exists");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}	

				// Set system data
				Date operationTime = new Date();
				specialPosition.setSystemUser(principal.getName());
				specialPosition.setLastModifiedSystem(operationTime);
				if (specialPosition.getLastModifiedActual() == null) {
					specialPosition.setLastModifiedActual(specialPosition.getLastModifiedSystem());
				}
				if (specialPosition.getActualUser() == null || specialPosition.getActualUser().isEmpty()) {
					specialPosition.setActualUser(specialPosition.getSystemUser());
				}

				// Updated record status and change information
				specialPosition.setStatus("A");

				// Get Position record to update (i.e. make old)
				SpecialPosition oldSpecialPosition = bp.getSpecialPosition(specialPosition.getId());

				if (oldSpecialPosition != null) {
					if (oldSpecialPosition.getOfficerPosition() != null
							&& oldSpecialPosition.getOfficerPosition() == true && !ignoreIntegration) {
						throw new OpsBoardError(ErrorMessage.CANNOT_MODIFY_OFFICER_POSITION);
					}

					oldSpecialPosition.setStatus("I");
					specialPosition.setPeopleSoftId(oldSpecialPosition.getPeopleSoftId());
				}

				// Save to DB
				SpecialPosition persistedSpecialPosition = null;

				try {
					// Save to DB
					persistedSpecialPosition = persist.updateSpecialPosition(bp.getPerson().getId(),
							oldSpecialPosition, specialPosition, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				command = new CommandUpdateSpecialPosition(boardKey.toId(), principal.getName(),
						persistedSpecialPosition.getLastModifiedSystem(), bp.getId(), bp.getPerson().getId(),
						persistedSpecialPosition, ignoreIntegration);

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}

				// Send out commands to locations
				sendCommands(principal.getName(), boardKey, bp.getHomeLocation().getCode(), command, bp
						.getWorkLocation().getCode(), command);
				sendCommands(principal.getName(), boardKey, commandsMap);

				if (!ignoreIntegration) {
					outgoingPersonnelService.setPersonSpecialPosition(persistedSpecialPosition, false);
				}
			}

		}
		logger.debug("Executed updateSpecialPosition");

	}

	public void updateUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		updateUnavailabilityReason(boardKey, bp, reason, principal, false);
	}

	public void updateUnavailabilityReason(BoardKey boardKey, BoardPerson bp, UnavailabilityReason reason,
			Principal principal, boolean ignoreIntegration) throws OpsBoardError, OpsBoardValidationException {
		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		if (reason.getUnavailabilityType() == null) {
			Date date = DateUtils.toBoardDateNoNull(boardKey.getDate());
			reason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(reason.getCode(), date));
		}

		synchronized (bp) {
			synchronized (bp.getPerson()) {

				//Person pFromDb = persist.findPersonById(bp.getPerson().getId());
				//BoardPerson bpFromDb = persist.findBoardPersonById(bp.getId());
				//bpFromDb.setPerson(pFromDb);
				//bpFromDb.copyTransientProperties(bp);

				if (persist.isUnavailabilityReasonReplaced(reason)) {
					List<String> extErrMessages = new ArrayList<String>();
					extErrMessages.add("This uanvailability was already modified.");
					throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
							extErrMessages));
				}
				
				List<UnavailabilityReason> unavailabilityReasons = persist.findByEndDateAndActive( bp.getDate(),bp.getPerson().getId());
				validateUnavailabilityReason(reason.getId(), reason.getCode(), reason.getStart(), reason.getEnd(),
						reason.getComments(),unavailabilityReasons, bp, "UPDATE_UNAVAILABLE", boardKey);

				// Set system data
				Date operationTime = new Date();
				reason.setSystemUser(principal.getName());
				reason.setLastModifiedSystem(operationTime);
				if (reason.getLastModifiedActual() == null) {
					reason.setLastModifiedActual(reason.getLastModifiedSystem());
				}
				if (reason.getActualUser() == null || reason.getActualUser().isEmpty()) {
					reason.setActualUser(reason.getSystemUser());
				}

				// Updated record status and change information
				reason.setStatus("A");
				reason.setAction("M");

				// Get Reason record to update (i.e. make old)
				UnavailabilityReason oldReason = bp.getUnavailabilityReason(reason.getId());
				oldReason.setStatus("I");
				oldReason.setAction("M");

				reason.setReplacedBy(oldReason.getReplacedBy());
				reason.setReplaces(oldReason.getId());
				reason.setPeopleSoftId(oldReason.getPeopleSoftId());

				// Save to DB
				UnavailabilityReason persistedUnavailabilityReason = null;

				try {
					// Save to DB
					persistedUnavailabilityReason = persist.updateUnavailability(bp.getPerson().getId(), oldReason,
							reason, principal.getName(), operationTime);
				} catch (Exception e) {
					throw new OpsBoardError(ErrorMessage.DB_ERROR, e);
				}

				// Construct command to send to clients (location boards, database...)
				CommandUpdatePersonUnavailability command = new CommandUpdatePersonUnavailability(boardKey.toId(),
						principal.getName(), persistedUnavailabilityReason.getLastModifiedSystem(), bp.getId(), bp
						.getPerson().getId(), persistedUnavailabilityReason, ignoreIntegration);

				commandsMap.put(bp.getWorkLocation().getCode(), command); // send to "current location" board
				commandsMap.put(bp.getHomeLocation().getCode(), command); // send to "owner" board

				// Send to all future work locations
				for (Location location : bp.getFutureWorkLocations()) {
					commandsMap.put(location.getCode(), command);
				}

				if (!ignoreIntegration) {
					outgoingPersonnelService.setPersonUnavailable(persistedUnavailabilityReason, false);
				}
			}

			// Send out commands to locations
			sendCommands(principal.getName(), boardKey, commandsMap);

		}

		logger.debug("Executed updateUnavailabilityReason");

	}

	protected void sendPersonUpdatesForDetachedBoards(Date startDate, String principal, BoardKey boardKey,
			Date lastModifiedSystem, BoardPerson bp, String currentWorkLocation, String homeLocation,
			String fromLocation, String toLocation) {

		Set<String> locations = bp.getPerson().getFutureWorkLocations(startDate).stream().map(l -> l.getCode())
				.collect(Collectors.toSet());

		// ordering is important
		locations.add(currentWorkLocation);
		// if home, to or from locations present, remove them as they have already been updated
		locations.remove(homeLocation);
		locations.remove(fromLocation);
		locations.remove(toLocation);

		// if from or to locations there, remove thme

		CommandAddOpsBoardPerson addCommand = new CommandAddOpsBoardPerson(boardKey.toId(), principal,
				lastModifiedSystem, bp);

		HashMap<String, IMultiBoardCommand> commandsMap = new HashMap<String, IMultiBoardCommand>();

		// for every unique location in the detachment history ( home/work included)
		for (String location : locations) {
			commandsMap.put(location, addCommand);
		}

		sendCommands(principal, boardKey, commandsMap);
	}


}

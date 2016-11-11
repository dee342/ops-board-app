package gov.nyc.dsny.smart.opsboard.commands;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.equipment.DownCodeCache;
import gov.nyc.dsny.smart.opsboard.cache.equipment.MaterialTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.MdaTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SpecialPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.UnavailabilityTypeCacheService;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IBinCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IBoardEquipmentCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IBoardPersonCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IDetachmentCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IFromLocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ILocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IMassChartUpdateCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IMdaStatusCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IRemovePersonCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ISpecialPositionCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IToLocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IUnavailabilityReasonCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IUpDownCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelRepository;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A factory used to construct board commands out of a command message.
 */
@Component
public class ReflectionMultiBoardCommandFactory {

	private static final Logger log = LoggerFactory.getLogger(ReflectionMultiBoardCommandFactory.class);

	@Autowired
	private BoardEquipmentCacheService boardEquipmentCache;

	@Autowired
	private BoardPersonnelCacheService boardPersonnelCache;

	private Map<String, Class<?>> commandClasses;

	@Autowired
	private DownCodeCache downCodeCache;

	@Autowired
	private LocationCache locationCache;

	@Autowired
	private LogContext logContext;

	@Autowired
	private MaterialTypeCache materialTypeCache;

	@Autowired
	private MdaTypeCacheService mdaTypeCacheService;

	// To change this to use cache
	@Autowired
	private PersonnelRepository personnelRepository;

	@Autowired
	private SpecialPositionTypeCacheService specialPositionTypeCacheService;

	@Autowired
	private UnavailabilityTypeCacheService unavailabilityTypeCacheService;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	/**
	 * Constructs a board command out of command message.
	 *
	 * @param message
	 *            command message
	 * @return the specific board command if command is known; otherwise, null.
	 * @throws OpsBoardError
	 */
	@SuppressWarnings("unchecked")
	public AbstractMultiBoardCommand createCommand(LocationCommandMessage message) throws OpsBoardError {
		LinkedHashMap<String, Object> content = new LinkedHashMap<String, Object>();
		if (message.getCommandContent() instanceof String) {
			// Command body constructed using JSON

			try {
				ObjectMapper mapper = new ObjectMapper();
				JsonNode root = mapper.readTree((String) message.getCommandContent());

				Iterator<Map.Entry<String, JsonNode>> it = root.fields();
				while (it.hasNext()) {
					Map.Entry<String, JsonNode> entry = it.next();
					if (entry.getValue().isNull()) {
						content.put(entry.getKey(), null);
					} else if (entry.getValue().isBoolean()) {
						content.put(entry.getKey(), entry.getValue().asBoolean());
					} else if (entry.getValue().isFloat() || entry.getValue().isDouble()) {
						content.put(entry.getKey(), entry.getValue().asDouble());
					} else if (entry.getValue().isInt()) {
						content.put(entry.getKey(), entry.getValue().asInt());
					} else if (entry.getValue().isLong()) {
						content.put(entry.getKey(), entry.getValue().asLong());
					} else if (entry.getValue().isTextual()) {
						content.put(entry.getKey(), entry.getValue().asText());
					} else {
						content.put(entry.getKey(), entry.getValue());
					}
				}
			} catch (Exception e) {
				throw new OpsBoardError(ErrorMessage.JAVA_JSON_PARSING_ERROR, e);
			}
		} else {
			LinkedHashMap<String, Object> temp = (LinkedHashMap<String, Object>) message.getCommandContent();
			content = temp;
		}

		Date boardDate = DateUtils.toBoardDateNoNull(message.getDate());
		BoardKey key = boardKeyFactory.createBoardKey(message.getDate(), locationCache.getLocation(message.getLocation(), boardDate));
		content.put("boardId", key.toId());

		AbstractMultiBoardCommand command = null;

		try {
			Class<?> commandClass = commandClasses.get(message.getCommandName());
			if (commandClass != null) {

				Constructor<?> commandConstructor;

				commandConstructor = ClassUtils.getConstructorIfAvailable(commandClass, LinkedHashMap.class);

				command = (AbstractMultiBoardCommand) commandConstructor.newInstance(content);

				if (command instanceof IBinCommand) {
					((IBinCommand) command).setBin1(populateBin(content, 1, boardDate));
					((IBinCommand) command).setBin2(populateBin(content, 2, boardDate));
				}

				if (command instanceof IBoardEquipmentCommand) {
					((IBoardEquipmentCommand) command).setBoardEquipment(boardEquipmentCache.get(key,
							content.get("boardEquipmentId").toString()));
				}

				if (command instanceof IBoardPersonCommand) {
					((IBoardPersonCommand) command).setBoardPerson(boardPersonnelCache.get(key,
							content.get("boardPersonId").toString()));
				}

				if (command instanceof IFromLocationCommand) {
					((IFromLocationCommand) command).setFrom(populateFromLocation(content, boardDate));
				}

				if (command instanceof ILocationCommand) {
					((ILocationCommand) command)
					.setLocation(locationCache.getLocation(message.getLocation(), boardDate));
				}

				if (command instanceof IMdaStatusCommand) {
					((IMdaStatusCommand) command).setMdaStatus(populateMdaStatus(content, boardDate));
				}

				if (command instanceof IUnavailabilityReasonCommand) {
					((IUnavailabilityReasonCommand) command).setUnavailableReason(populateUnavailabilityReason(content,
							boardDate));
				}
				
				if (command instanceof IMassChartUpdateCommand) {
					((IMassChartUpdateCommand) command).setCancelledReasons(populateCancelledUnavailabilityReasons(content,
							boardDate));
					((IMassChartUpdateCommand) command).setReverseCancelledReasons(populateReverseCancelledUnavailabilityReasons(content,
							boardDate));
				}

				if (command instanceof ISpecialPositionCommand) {
					((ISpecialPositionCommand) command).setSpecialPosition(populateSpecialPosition(content, boardDate));
				}

				if (command instanceof IUpDownCommand) {
					((IUpDownCommand) command).setUpDownData(populateUpDownData(content, boardDate));
				}

				if (command instanceof IRemovePersonCommand) {
					((IRemovePersonCommand) command).removePersonFromCache(boardPersonnelCache, key,
							content.get("boardPersonId").toString());
				}

				if (command instanceof IToLocationCommand) {
					((IToLocationCommand) command).setTo(populateToLocation(content, boardDate));
				}
				// after fromlocation and tolocation
				if (command instanceof IDetachmentCommand) {

					((IDetachmentCommand) command).setDetachment(populateDetachment(content, command));
				}
			}

		} catch (Exception e) {
			log.error(appendEntries(logContext), " unable to create command from message {}.", message);
		}

		return command;

	}

	@PostConstruct
	public void init() throws Exception {
		commandClasses = new HashMap<String, Class<?>>();
		Map<String, String> classNamesMap = new HashMap<String, String>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(IMultiBoardCommandAnnotation.class));
		ClassLoader loader = this.getClass().getClassLoader();

		for (BeanDefinition bd : scanner.findCandidateComponents("gov.nyc.dsny")) {
			String className = bd.getBeanClassName();
			Class<?> classType = loader.loadClass(className);
			Annotation annotation = classType.getAnnotation(IMultiBoardCommandAnnotation.class);
			String commandName = (String) annotation.annotationType().getMethod("commandName").invoke(annotation);

			if (commandName != null) {
				if (classNamesMap.get(commandName) == null) {
					classNamesMap.put(commandName, className);
					commandClasses.put(commandName, classType);
				} else {
					throw new RuntimeException(className + " & " + classNamesMap.get(commandName) + " has same value "
							+ commandName + " for attribute 'commandName'.");
				}
			} else {
				throw new RuntimeException(bd.getBeanClassName() + " does not have 'commandName' attribute.");
			}
		}
	}

	private Bin populateBin(LinkedHashMap<String, Object> content, int binNumber, Date boardDate) throws OpsBoardError {
		Bin bin = null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			if (content.get("bin" + binNumber) != null) {
				bin = mapper.readValue(content.get("bin" + binNumber).toString(), Bin.class);
			}
		} catch (Exception e) {
			log.info("Exception while trying to populate Bin.", e);
		}

		if (bin != null) {
			bin.setMaterial(materialTypeCache.getMaterialTypeByCode(bin.getCode(), boardDate));
		}

		return bin;
	}

	private Detachment populateDetachment(LinkedHashMap<String, Object> content, AbstractMultiBoardCommand command)
			throws OpsBoardError {
		Detachment detachment = null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			detachment = mapper.readValue(content.get("detachment").toString(), Detachment.class);
			Location from = ((IFromLocationCommand) command).getFrom();
			detachment.setFrom(from);
			Location to = ((IToLocationCommand) command).getTo();
			detachment.setTo(to);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return detachment;
	}

	private Location populateFromLocation(LinkedHashMap<String, Object> content, Date boardDate) throws OpsBoardError {
		Location location = null;
		String code = null;
		if (content.get("fromCode") != null) {
			code = (String) content.get("fromCode");
		}

		location = locationCache.getLocation(code, boardDate);

		return location;
	}

	private MdaStatus populateMdaStatus(LinkedHashMap<String, Object> content, Date boardDate) throws OpsBoardError {
		MdaStatus mdaStatus = null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			mdaStatus = mapper.readValue(content.get("mdaStatus").toString(), MdaStatus.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (mdaStatus != null) {
			mdaStatus.setMdaType(mdaTypeCacheService.getMdaType(mdaStatus.getSubType(), boardDate));
		}
		return mdaStatus;
	}

	private SpecialPosition populateSpecialPosition(LinkedHashMap<String, Object> content, Date boardDate)
			throws OpsBoardError {
		SpecialPosition specialPosition = null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			specialPosition = mapper.readValue(content.get("specialPosition").toString(), SpecialPosition.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (specialPosition != null) {
			specialPosition.setSpecialPositionType(specialPositionTypeCacheService.getSpecialPositionType(
					specialPosition.getCode(), boardDate));
		}
		return specialPosition;
	}

	private Location populateToLocation(LinkedHashMap<String, Object> content, Date boardDate) throws OpsBoardError {
		Location location = null;
		String code = null;
		if (content.get("toCode") != null) {
			code = (String) content.get("toCode");
		}

		location = locationCache.getLocation(code, boardDate);

		return location;
	}

	private UnavailabilityReason populateUnavailabilityReason(LinkedHashMap<String, Object> content, Date boardDate)
			throws OpsBoardError {
		UnavailabilityReason unavailabilityReason = null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			unavailabilityReason = mapper.readValue(content.get("unavailableReason").toString(),
					UnavailabilityReason.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (unavailabilityReason != null) {
			unavailabilityReason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(
					unavailabilityReason.getCode(), boardDate));
		}
		return unavailabilityReason;
	}
	
	private Map<String,UnavailabilityReason> populateCancelledUnavailabilityReasons(LinkedHashMap<String, Object> content, Date boardDate)
			throws OpsBoardError {
		
		Map<String,UnavailabilityReason> cancelledReasons = null;
		if (content.get("cancelledReasons") != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				cancelledReasons  = mapper.readValue(content.get("cancelledReasons").toString(),
						new TypeReference<Map<String,UnavailabilityReason>>() {
				});
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			for(String personId: cancelledReasons.keySet()){
				UnavailabilityReason unavailabilityReason = cancelledReasons.get(personId);
				unavailabilityReason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(
						unavailabilityReason.getCode(), boardDate));
			}
		}
		return cancelledReasons;
	}
	
	private Map<String,UnavailabilityReason> populateReverseCancelledUnavailabilityReasons(LinkedHashMap<String, Object> content, Date boardDate)
			throws OpsBoardError {
		
		Map<String,UnavailabilityReason> reverseCancelledReasons = null;
		if (content.get("reverseCancelledReasons") != null) {
			ObjectMapper mapper = new ObjectMapper();
			try {
				reverseCancelledReasons  = mapper.readValue(content.get("reverseCancelledReasons").toString(),
						new TypeReference<Map<String,UnavailabilityReason>>() {
				});
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
			for(String personId: reverseCancelledReasons.keySet()){
				UnavailabilityReason unavailabilityReason = reverseCancelledReasons.get(personId);
				unavailabilityReason.setUnavailabilityType(unavailabilityTypeCacheService.getUnavailabilityType(
						unavailabilityReason.getCode(), boardDate));
			}
		}
		return reverseCancelledReasons;
	}
	
	private UpDown populateUpDownData(LinkedHashMap<String, Object> content, Date boardDate) throws OpsBoardError {
		UpDown upDown = null;

		ObjectMapper mapper = new ObjectMapper();
		try {
			upDown = mapper.readValue(content.get("upDownData").toString(), UpDown.class);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		if (upDown != null) {
			Set<EquipmentCondition> equipmentConditions = upDown.getConditions();
			for (EquipmentCondition equipmentCondition : equipmentConditions) {
				if (equipmentCondition != null && StringUtils.isNotBlank(equipmentCondition.getDownCode())) {
					equipmentCondition.setDownCodeType(downCodeCache.getDownCode(equipmentCondition.getDownCode(),
							boardDate));
				}
			}
		}
		return upDown;
	}
}
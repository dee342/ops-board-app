package gov.nyc.dsny.smart.opsboard.commands;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.SubcategoryCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.CategoryCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IBoardPersonCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ICategoryCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ILocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.INextDayCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IOldCategoryCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ISectionCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IShiftCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ISubCategoryCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardHelper;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Section;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;
import gov.nyc.dsny.smart.opsboard.persistence.repos.board.BoardRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.personnel.PersonnelPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * A factory used to construct board commands out of a command message.
 */
@Component
public class ReflectionBoardCommandFactory {

	private static final Logger log = LoggerFactory.getLogger(ReflectionBoardCommandFactory.class);

	@Autowired
	private CategoryCacheService categoryCacheService;

	private Map<String, Class<?>> commandClasses;

	@Autowired
	private LocationCache locationCache;

	@Autowired
	private BoardPersonnelCacheService personnelCache;

	@Autowired
	private ShiftCacheService shiftCacheService;

	@Autowired
	private SubcategoryCache subcategoryCache;
	
	@Autowired
	private LogContext logContext;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	@Autowired
	private BoardCacheService boardCache;
	
	@Autowired
	private BoardRepository boardRepo;
	
	@Autowired
	private PersonnelPersistenceService personnelPersistenceService;

	/**
	 * Constructs a board command out of command message.
	 *
	 * @param message
	 *            command message
	 * @return the specific board command if command is known; otherwise, null.
	 * @throws OpsBoardError
	 */
	@SuppressWarnings("unchecked")
	public AbstractBoardCommand createCommand(CommandMessage message) throws OpsBoardError {
		LinkedHashMap<String, Object> content = (LinkedHashMap<String, Object>) message.getCommandContent();

		Date boardDate = DateUtils.toBoardDateNoNull(message.getDate());
		BoardKey key = boardKeyFactory.createBoardKey(message.getDate(), locationCache.getLocation(message.getLocation(), boardDate));
		AbstractBoardCommand command = null;

		try {
			Class<?> commandClass = commandClasses.get(message.getCommandName());
			if (commandClass != null) {
				Constructor<?> commandConstructor;
				commandConstructor = ClassUtils.getConstructorIfAvailable(commandClass, String.class,
						LinkedHashMap.class);
				
				if(commandConstructor == null)
					return null;
				
				command = (AbstractBoardCommand) commandConstructor.newInstance(key.toId(), content);

				if (command instanceof IBoardPersonCommand) {
					((IBoardPersonCommand) command).setBoardPerson(populateBoardPerson(content, message.getDate()));
				}
				if (command instanceof ICategoryCommand) {
					((ICategoryCommand) command).setCategory(populateCategory(content, message.getLocation()));
				}
				if (command instanceof ILocationCommand) {
					((ILocationCommand) command).setLocation(populateLocation(content, boardDate));
				}
				if (command instanceof IOldCategoryCommand) {
					((IOldCategoryCommand) command).setOldCategory(populateOldCategory(content, message.getLocation()));
				}
				if (command instanceof ISectionCommand) {
					((ISectionCommand) command).setSection(populateSection(content));
				}
				if (command instanceof IShiftCommand) {
					((IShiftCommand) command).setShift(populateShift(content));
				}
				if (command instanceof ISubCategoryCommand) {
					((ISubCategoryCommand) command).setSubcategory(populateSubCategory(content));
				}
				if (command instanceof INextDayCommand) {
					String personId = ((INextDayCommand) command).getPersonId();
					((INextDayCommand) command).setAssignedTomorrow(isAssignedTomorrow(command.getBoardId(), personId));
				}
			}

		} catch (Exception e) {
			log.error(appendEntries(logContext), " unable to create command from message {}.", message, e);
		}

		return command;

	}
	
	public boolean isAssignedTomorrow(String boardId, String personId) throws OpsBoardError{
		Date tomorrow = DateUtils.getOneDayAfter(DateUtils.toBoardDate(Board.boardIdToBoardDate(boardId)));
		String bpId = BoardPerson.CREATE_ID(personId, DateUtils.toStringBoardDate(tomorrow));
		BoardPerson bp = personnelCache.get(bpId);
		
		if(bp != null){			
			return bp.isAssigned();			
		}
		
		return personnelPersistenceService.findPersonAssignmentsByBoardPerson(bpId) > 0;
	}

	@PostConstruct
	public void init() throws Exception {
		commandClasses = new HashMap<String, Class<?>>();
		Map<String, String> classNamesMap = new HashMap<String, String>();

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(IBoardCommandAnnotation.class));
		ClassLoader loader = this.getClass().getClassLoader();

		for (BeanDefinition bd : scanner.findCandidateComponents("gov.nyc.dsny")) {
			String className = bd.getBeanClassName();
			Class<?> classType = loader.loadClass(className);
			Annotation annotation = classType.getAnnotation(IBoardCommandAnnotation.class);
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

	private BoardPerson populateBoardPerson(Map<String, Object> content, String boardDate) throws OpsBoardError {
		BoardPerson retval = null;
		Object person = content.get("personId");
		if (person != null) {
			String personId = content.get("personId").toString();
			retval = personnelCache.get(BoardPerson.CREATE_ID(personId, boardDate));
		}

		return retval;
	}

	@SuppressWarnings("unchecked")
	private Category populateCategory(LinkedHashMap<String, Object> content, String loc) throws OpsBoardError {
		Long id = null;
		if (content.get("category") != null) {
			id = ((Integer) ((LinkedHashMap<String, Object>) content.get("category")).get("id")).longValue();
		} else if (content.get("categoryId") != null) {
			Object temp = content.get("categoryId");
			if (temp instanceof String)				
				id = Long.parseLong((String) temp);
			else if (temp instanceof Integer)
				id = ((Integer)temp).longValue();
			else 
				id = (Long)temp;
		}
		if (id == null) {
			return null;
		}

		return categoryCacheService.getCategoryByID(id);
	}

	@SuppressWarnings("unchecked")
	private Location populateLocation(LinkedHashMap<String, Object> content, Date date) throws OpsBoardError {
		String code = null;
		if (content.get("serviceLocation") != null) {
			code = (String) ((Map<String, Object>) content.get("serviceLocation")).get("code");
		} else if (content.get("serviceLocationCode") != null) {
			code = (String) content.get("serviceLocationCode");
		} else if (content.get("location") != null) {
			code = (String) ((Map<String, Object>) content.get("location")).get("code");
		} else if (content.get("locationId") != null) {
			code = (String) content.get("locationId");
		}else if (content.get("serviceLocationCode") != null) {
			code = (String) content.get("serviceLocationCode");
		} else if (content.get("serviceLocationId") != null) {
			code = (String) content.get("serviceLocationId");
		}

		if (code == null) {
			return null;
		}

		return locationCache.getLocation(code, date);
	}

	private Category populateOldCategory(LinkedHashMap<String, Object> content, String loc) throws OpsBoardError {
		Long id = null;
		if (content.get("oldCategoryId") != null) {
			Object temp = content.get("oldCategoryId");
			if (temp instanceof String)				
				id = Long.parseLong((String) temp);
			else if (temp instanceof Integer)
				id = ((Integer)temp).longValue();
			else 
				id = (Long)temp;
		}
		
		if (id == null) {
			return null;
		}

		return categoryCacheService.getCategoryByID(id);
	}

	@SuppressWarnings("unchecked")
	private Section populateSection(LinkedHashMap<String, Object> content) {
		Section section = null;
		String sectionIdStr = null;
		if (content.get("sectionId") != null) {
			sectionIdStr = (String) content.get("sectionId");
		} else if (content.get("section") != null) {
			sectionIdStr = ((String) ((LinkedHashMap<String, Object>) content.get("section")).get("id")).toString();
		}
		if (sectionIdStr != null) {
			section = new Section(sectionIdStr, sectionIdStr);
		}

		return section;
	}

	@SuppressWarnings("unchecked")
	private Shift populateShift(LinkedHashMap<String, Object> content) throws OpsBoardError {
		Long id = null;
		if (content.get("shift") != null) {
			id = ((Integer) ((LinkedHashMap<String, Object>) content.get("shift")).get("id")).longValue();
		} else if (content.get("shiftId") != null) {
			Object temp = content.get("shiftId");
			if (temp instanceof String)				
				id = Long.parseLong((String) temp);
			else if (temp instanceof Integer)
				id = ((Integer)temp).longValue();
			else 
				id = (Long)temp;
		}

		if (id == null) {
			return null;
		}

		return shiftCacheService.getShiftById(id);
	}

	@SuppressWarnings("unchecked")
	private Subcategory populateSubCategory(LinkedHashMap<String, Object> content) throws OpsBoardError {
		Long id = null;
		if (content.get("subcategory") != null) {
			id = ((Integer) ((LinkedHashMap<String, Object>) content.get("subcategory")).get("id")).longValue();
		} else if (content.get("subcategoryId") != null) {
			Object temp = content.get("subcategoryId");
			if (temp instanceof String)				
				id = Long.parseLong((String) temp);
			else if (temp instanceof Integer)
				id = ((Integer)temp).longValue();
			else 
				id = (Long)temp;
		}

		if (id == null) {
			return null;
		}

		return subcategoryCache.getSubCategoryByID(id);
	}
}

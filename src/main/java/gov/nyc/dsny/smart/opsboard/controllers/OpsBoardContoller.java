package gov.nyc.dsny.smart.opsboard.controllers;

import static net.logstash.logback.marker.Markers.appendEntries;

import java.io.InputStream;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.board.CommandReloadBoard;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;
import gov.nyc.dsny.smart.opsboard.validation.ValidationUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.BoroTasksBoard;
import gov.nyc.dsny.smart.opsboard.viewmodels.JsonStringWrapper;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoard;

/**
 * This controller is used to load and display the Operations Board.
 */
@Controller
@RequestMapping("/{location}/{date:^(?!\\s*$)[0-9\\s]{8}$}")
@PreAuthorize("@boardPermissionEvaluator.hasPermission(authentication, #location, #date)")
public class OpsBoardContoller extends SorController {

	private static final Logger log = LoggerFactory.getLogger(OpsBoardContoller.class);

	@Autowired
	ServletContext servletContext;

	@Autowired
	private BoardCacheService boardsCache;

	@Value("${spring.profiles.active}")
	private String environment;
	
	@Autowired
	private LocationCache locationCache;

	@Autowired
	private LogContext logContext;

	@Autowired
	private SimpMessagingTemplate messenger;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	@RequestMapping(value = "/copy/{futureDate}")
	@ResponseBody
	public String copyBoard(@PathVariable String location, @PathVariable String date, @PathVariable String futureDate,
			HttpServletRequest request, HttpServletResponse response, Principal principal)  throws OpsBoardError{

//		checkAccessPermissionsForResource(location);
		logContext.initContext(request);

		log.debug("Copying board '{}' to '{}' started.", location + "_" + date, location + "_" + futureDate);

		try {
			// Validate dates

			// Cannot copy board to a past date
			Date now = DateUtils.removeTime(new Date());
			Date future = DateUtils.toBoardDate(futureDate);
			if (future == null || now.compareTo(future) >= 0) {
				throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
						Arrays.asList(ErrorMessage.NO_COPY_BOARD_TO_PAST.getMessage())));
			}

			BoardKey sourceKey = null;
			try {
				Date futured= DateUtils.toBoardDateNoNull(futureDate);
				sourceKey = boardKeyFactory.createBoardKey(date, locationCache.getLocation(location, futured));
			} catch (OpsBoardError obe) {
				throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
						Arrays.asList(obe.getMessage())));
			}

			// Copy board
			boolean targetOverwritten = boardsCache.cloneToDate(sourceKey, futureDate);

			if (targetOverwritten) {
				// Send message to users of existing board to reload their board
				String reason = String.format(
						"User %s copied board %s to %s at %s. Your board was overwritten and should be reloaded.",
						principal.getName(), location + "_" + date, location + "_" + futureDate, new SimpleDateFormat(
								"MM-dd-yyy HH:mm:ss").format(now));
				
				LinkedHashMap<String, Object> content = new LinkedHashMap<String, Object>();
				content.put("systemDateTime", now.getTime());
				content.put("systemUser", principal.getName());
				content.put("oldBoardId", location + "_" + date);
				content.put("reasonToReload", reason);
				CommandReloadBoard command = new CommandReloadBoard(location + "_" + futureDate, content);
				CommandMessage message = new CommandMessage();
				message.setCommandName(command.getName());
				message.setCommandContent(command);
				message.setDate(futureDate);
				message.setLocation(location);
				message.setUser(principal.getName());
				messenger.convertAndSend("/topic/commands." + location + "." + futureDate, message);
			}

		} catch (OpsBoardValidationException obe) {
			return obe.getOpsBoardError().toJson();
		} catch (OpsBoardError obe) {
			log.error(appendEntries(logContext), "Error during copying board {}", obe.getMessage() != null ? obe.getMessage() : "Null Exception Message", obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during copying board {}", e.getMessage() != null ? e.getMessage() : "Null Exception Message", e);

			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_BOARD, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Copying board '{}' to '{}' finished.", location + "_" + date, location + "_" + futureDate);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";
	}

	/**
	 * Loads the board data in JSON format.
	 *
	 * @param location
	 *            board location
	 * @param date
	 *            board date
	 * @param request
	 *            http request
	 * @param response
	 *            http response
	 * @param principal
	 *            user submitting the command
	 *
	 * @return the board in JSON format.
	 */
	@RequestMapping(value = "/load", method = RequestMethod.GET)
	@ResponseBody
	public JsonStringWrapper loadBoard(@PathVariable String location, @PathVariable String date, HttpServletRequest request,
			HttpServletResponse response, Principal principal)  throws OpsBoardError{
		ValidationUtils.validateBoardDateLocation(date,location,locationCache);
//		checkAccessPermissionsForResource(location);
		logContext.initContext(request);
		logContext.put("CommandLocation", location);
		
		// Set to 2.5 MB to disable chunking for this response only.
		response.setBufferSize(2621440);
		
		BoardKey key = null;
		try {
			Date boardDate= DateUtils.toBoardDateNoNull(date);
			Location loc = locationCache.getLocation(location, boardDate);
			synchronized (loc) {
				key = boardKeyFactory.createBoardKey(date, locationCache.getLocation(location, boardDate));
				BoardContainer 	container = boardsCache.get(key, principal);
				//boardsCache.sendDetailsAsync(container.getBoard(), key);
				synchronized (container) {
					ObjectMapper objectMapper = new ObjectMapper();
					//using JsonStringWrapper so that the convertor would not escape doublequotes
					//basically also avoiding double jsonification
					return  new JsonStringWrapper(objectMapper.writeValueAsString( new OpsBoard(container.getBoard(), request.getRemoteAddr())));
					
				}
			}

		} catch (OpsBoardError obe) {
			log.error(appendEntries(logContext), "Error during loading board {}", obe.getMessage() != null ? obe.getMessage() : "Null Exception Message", obe);
			throw obe;

		} catch (JsonProcessingException jpe) {
			String[] exceptions = ExceptionUtils.getStackFrames(jpe);
			String allExceptions =  StringUtils.join(exceptions, "\n");
			log.error(appendEntries(logContext), "Unexpected serialization error during loading board {}", 
					jpe.getMessage() != null ? jpe.getMessage() + allExceptions : "Null Exception Message", jpe);				
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_BOARD, jpe);
			obe.getExtendedMessages().add(jpe.getClass().getCanonicalName() + ": " + jpe.getMessage() + allExceptions);
			
			throw obe;
			
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during loading board {}", e.getMessage() != null ? e.getMessage() : "Null Exception Message", e);

			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_BOARD, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());

			throw obe;
		}

	}
	
	@RequestMapping(value = "/details", method = RequestMethod.GET)
	@ResponseBody
	public JsonStringWrapper loadBoardDetails(@PathVariable String location, @PathVariable String date, HttpServletRequest request,
			HttpServletResponse response, Principal principal)  throws OpsBoardError{
		return loadBoard(location, date, request, response, principal);
	}
	
	
	@RequestMapping(value = "/district", method = RequestMethod.GET)
	@ResponseBody
	public JsonStringWrapper boroTask(@PathVariable String location, @PathVariable String date, HttpServletRequest request,
			HttpServletResponse response, Principal principal)  throws OpsBoardError{
		ValidationUtils.validateBoardDateLocation(date,location,locationCache);
		logContext.initContext(request);
		logContext.put("CommandLocation", location);

		BoardKey key = null;
		try {
			try {
				Date boardDate = DateUtils.toBoardDateNoNull(date);
				key = boardKeyFactory.createBoardKey(date, locationCache.getLocation(location, boardDate));
				BoardContainer container = boardsCache.get(key);				
				OpsBoard opsBoard = new OpsBoard(container.getBoard());
				ObjectMapper objectMapper = new ObjectMapper();
				//using JsonStringWrapper so that the convertor would not escape doublequotes
				//basically also avoiding double jsonification
				return new JsonStringWrapper(objectMapper.writeValueAsString( new BoroTasksBoard(opsBoard)));
					
			} catch (OpsBoardError obe) {
				log.error(appendEntries(logContext), "Error while processing boro task rest call {}", obe.getMessage() != null ? obe.getMessage() : "Null Exception Message", obe);
				throw obe;

			} catch (JsonProcessingException jpe) {
				String[] exceptions = ExceptionUtils.getStackFrames(jpe);
				String allExceptions =  StringUtils.join(exceptions, "\n");
				log.error(appendEntries(logContext), "Unexpected serialization error while processing boro task rest call {}", 
						jpe.getMessage() != null ? jpe.getMessage() + allExceptions : "Null Exception Message", jpe);				
				OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_BOARD, jpe);
				obe.getExtendedMessages().add(jpe.getClass().getCanonicalName() + ": " + jpe.getMessage() + allExceptions);
				
				throw obe;
				
			}	catch (Exception e) {
				log.error(appendEntries(logContext), "Unexpected error while processing boro task rest call {}", e.getMessage() != null ? e.getMessage() : "Null Exception Message", e);

				OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_BOARD, e);
				obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());

				throw obe;
			}
			
		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data",
					new Object[] { "location", location, "date", date, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, key);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return new JsonStringWrapper(obe.toJson());
		}
	}

	/**
	 * Displays the board (and loads/links all the frontend code).
	 *
	 * @param location
	 *            board location
	 * @param date
	 *            board date
	 * @param request
	 *            http request
	 * @param model
	 *            data model
	 *
	 * @return the board page (html, css, js, images).
	 */
	@RequestMapping
	public String showBoard(@PathVariable String location, @PathVariable String date, HttpServletRequest request,
			ModelMap model) throws OpsBoardError{
		ValidationUtils.validateBoardDateLocation(date,location,locationCache);
//		checkAccessPermissionsForResource(location);
		logContext.initContext(request);
		logContext.put("CommandLocation", location);

		String appversion = (String) servletContext.getAttribute("AppVersion");
		log.debug(appendEntries(logContext), "Received AppVersion value from Context '{}'", appversion);
		if (null == appversion) {
			try {
				InputStream inputStream = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
				Manifest manifest = new Manifest(inputStream);
				Attributes attr = manifest.getMainAttributes();
				appversion = attr.getValue("AppVersion");
				log.debug(appendEntries(logContext), "Received AppVersion value from MANIFEST.MF '{}'", appversion);
			} catch (Exception e) {
				appversion = "DEV_BUILD";
			}
		}

		// TODO - fix this app version stuff ... should not be here.

		servletContext.setAttribute("AppVersion", appversion);
		log.info(appendEntries(logContext), "Application version is '{}'", appversion);
		model.addAttribute("appversion", appversion);
		model.addAttribute("environment", environment);

		model.addAttribute("district", location);
		model.addAttribute("date", date);

		return "board";
	}
	

}
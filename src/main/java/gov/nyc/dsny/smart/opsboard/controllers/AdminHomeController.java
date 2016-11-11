package gov.nyc.dsny.smart.opsboard.controllers;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.ReflectionBoardCommandFactory;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.domain.ApplicationSettings;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.LocationShift;
import gov.nyc.dsny.smart.opsboard.domain.tasks.ShiftCategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;
import gov.nyc.dsny.smart.opsboard.integration.CompletionAdvice;
import gov.nyc.dsny.smart.opsboard.integration.ext.refdata.services.RefDataService;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.services.executors.AdminExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin")
public class AdminHomeController extends AbstractAdminController
{
	@Autowired
    private AdminExecutor adminExecutor;
	
	@Autowired
	protected CompletionAdvice miscPsAdvice;
	
	@Autowired
	protected CompletionAdvice miscScanAdvice;	

	@Autowired
	private ReflectionBoardCommandFactory reflectionBoardCommandFactory;
	
	@Autowired
	private LogContext logContext;
	
	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private BoardCacheService boardsCache;
	
	@Autowired
	BoardPersistenceService persistService;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	@Autowired
	private RefDataService refDataService;
	
	@Autowired
    private WebApplicationContext webApp;
	
	private static final Logger log = LoggerFactory.getLogger(AdminHomeController.class);
	
	@RequestMapping
	public String adminHome() {
		return "admin/index";
	}
	
	@RequestMapping("/caching")
	public String adminCacheHome() {
		return "admin/cacheHome";
	}
	
	@RequestMapping("/list/servers")
	@ResponseBody
	public String listServers(){
		return webApp.getEnvironment().getProperty("servers.in.cluster");
	}
	
	@RequestMapping("/data")
	@ResponseBody
	public ResponseEntity<String> loadData(@RequestParam("path") String path, HttpServletRequest request, HttpServletResponse response, Model model)throws Exception{
		RequestDispatcher dispatcher = request.getRequestDispatcher(path);
		dispatcher.forward(request, response);
		return new ResponseEntity<String>("Loading data from endpoint " + path, HttpStatus.OK);
	}
	
	@RequestMapping("/clearAllLocations/{location}/{date}")
	public ResponseEntity<String> clearAllLocations(@PathVariable String location, @PathVariable String date, HttpServletRequest request,
			HttpServletResponse response, Principal principal){

		Map<String, String> commandContent = new LinkedHashMap<String, String>();
		commandContent.put("serviceLocationId", location);
		CommandMessage message = new CommandMessage();
		message.setCommandContent(commandContent);
		message.setCommandName("ClearAllLocations");
		message.setDate(date);
		message.setLocation(location);
		
		AbstractBoardCommand command = null;
		BoardKey key = null;
		ResponseEntity<String> responseEntity = null;
		
		try {
			// Convert message to actual command
			command = reflectionBoardCommandFactory.createCommand(message);
			Date boardDate = DateUtils.toBoardDateNoNull(message.getDate());
			
			// Get board
			key = boardKeyFactory.createBoardKey(boardDate, location);
			BoardContainer boardContainer = boardsCache.get(key, principal);
			if (boardContainer == null || boardContainer.getBoard() == null) {

				// Stop forwarding of message to other clients
				responseEntity = new ResponseEntity<String>("BoardContainer or board didn't exist", HttpStatus.BAD_REQUEST);;
			}
			else{
			// Execute command on board
				synchronized (boardContainer) {
					// Set mandatory fields
					command.setSystemUser(principal.getName());
					command.setSystemDateTime(new Date());
	
					// Execute command
					command.execute(boardContainer.getBoard());
	
					// Update command message
					message.setServerSequence(boardContainer.getAutoIncrementSequence());
					message.setUser(command.getSystemUser());
					message.setCommandContent(command);
				}
				String topic = "/topic/commands." + location + "."+ date; 
				messenger.convertAndSend(topic, message);
				responseEntity = new ResponseEntity<String>(String.format("Board %s_%s is reset", location, date), HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Uncaught exception '{}' ", e.getMessage(), e);
			responseEntity = new ResponseEntity<String>(String.format("Board %s_%s failed to reset", location, date), HttpStatus.EXPECTATION_FAILED);
		}
		
		return responseEntity;
	}
	
	@RequestMapping("/clearServiceLocation/{location}/{date}/{serviceLocation}")
	public ResponseEntity<String> clearServiceLocation(@PathVariable String location, @PathVariable String date, @PathVariable String serviceLocation, HttpServletRequest request,
			HttpServletResponse response, Principal principal){

		Map<String, String> commandContent = new LinkedHashMap<String, String>();
		commandContent.put("serviceLocationId", serviceLocation);
		CommandMessage message = new CommandMessage();
		message.setCommandContent(commandContent);
		message.setCommandName("ClearSpecificLocation");
		message.setDate(date);
		message.setLocation(location);
		
		AbstractBoardCommand command = null;
		BoardKey key = null;
		ResponseEntity<String> responseEntity = null;
		
		try {
			// Convert message to actual command
			command = reflectionBoardCommandFactory.createCommand(message);
			Date boardDate = DateUtils.toBoardDateNoNull(message.getDate());
			
			// Get board
			key = boardKeyFactory.createBoardKey(boardDate, location);
			BoardContainer boardContainer = boardsCache.get(key, principal);
			boolean locationFound = false;
			if (boardContainer == null || boardContainer.getBoard() == null) {

				// Stop forwarding of message to other clients
				responseEntity = new ResponseEntity<String>("BoardContainer or board didn't exist", HttpStatus.BAD_REQUEST);;
			}
			else{
				Board board = boardContainer.getBoard();
				List<TaskContainer> taskContainers = board.getTaskContainers();
				for(TaskContainer tc : taskContainers){
					if(tc.getLocation().getCode().equalsIgnoreCase(serviceLocation)){
						locationFound = true;
					}
				}
				if(!locationFound){
					responseEntity = new ResponseEntity<String>(String.format("Service Location %s not found for Board %s_%s", serviceLocation, location, date), HttpStatus.EXPECTATION_FAILED);
					return responseEntity;
				}
				// Execute command on board
				synchronized (boardContainer) {
					// Set mandatory fields
					command.setSystemUser(principal.getName());
					command.setSystemDateTime(new Date());
	
					// Execute command
					command.execute(boardContainer.getBoard());
	
					// Update command message
					message.setServerSequence(boardContainer.getAutoIncrementSequence());
					message.setUser(command.getSystemUser());
					message.setCommandContent(command);
				}
				String topic = "/topic/commands." + location + "."+ date; 
				messenger.convertAndSend(topic, message);
				responseEntity = new ResponseEntity<String>(String.format("Service Location %s on board %s_%s is reset", serviceLocation, location, date), HttpStatus.OK);
			}
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Uncaught exception '{}' ", e.getMessage(), e);
			responseEntity = new ResponseEntity<String>(String.format("Service Location %s on Board %s_%s failed to reset", serviceLocation, location, date), HttpStatus.EXPECTATION_FAILED);
		}
		
		return responseEntity;
	}
	
	@RequestMapping("/clearShift/{location}/{date}/{serviceLocation}/{shiftId}")
	public ResponseEntity<String> clearShift(@PathVariable String location, @PathVariable String date, @PathVariable String serviceLocation, @PathVariable String shiftId, HttpServletRequest request,
			HttpServletResponse response, Principal principal){
		ResponseEntity<String> responseEntity = null;
		Shift shift = null;
		try {
			Date boardDate = DateUtils.toBoardDateNoNull(date);
			// Get board
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, location);
			BoardContainer boardContainer = boardsCache.get(key, principal);
			Board board = boardContainer.getBoard();
			List<TaskContainer> taskContainers = board.getTaskContainers();
			String locationShiftId = null;
			for (TaskContainer tc : taskContainers) {
				if (serviceLocation.equals(tc.getLocation().getCode())) {
					ConcurrentHashMap<String, LocationShift> locationShiftsMap = tc.getLocationShiftsMap();
					for(String locationShiftKey : locationShiftsMap.keySet()){
						LocationShift locShift = locationShiftsMap.get(locationShiftKey);
						Shift shft = locShift.getShift();
						if(shft.getName().equalsIgnoreCase(shiftId)){
							locationShiftId = locShift.getId();
							shift = shft;
							break;
						}
					}
					break;
				}
			}
			if(shift != null){
			Map<String, String> commandContent = new LinkedHashMap<String, String>();
			commandContent.put("serviceLocationId", serviceLocation);
			commandContent.put("locationShiftId", locationShiftId);
			commandContent.put("shiftId", shift.getId().toString());
			CommandMessage message = new CommandMessage();
			message.setCommandContent(commandContent);
			message.setCommandName("RemoveShift");
			message.setDate(date);
			message.setLocation(location);
			AbstractBoardCommand command = null;
		
			// Convert message to actual command
			command = reflectionBoardCommandFactory.createCommand(message);		
			
			if (boardContainer == null || boardContainer.getBoard() == null) {

				// Stop forwarding of message to other clients
				responseEntity = new ResponseEntity<String>("BoardContainer or board didn't exist", HttpStatus.BAD_REQUEST);;
			}
			else{
			// Execute command on board
				synchronized (boardContainer) {
					// Set mandatory fields
					command.setSystemUser(principal.getName());
					command.setSystemDateTime(new Date());
	
					// Execute command
					command.execute(boardContainer.getBoard());
	
					// Update command message
					message.setServerSequence(boardContainer.getAutoIncrementSequence());
					message.setUser(command.getSystemUser());
					message.setCommandContent(command);
				}
				String topic = "/topic/commands." + location + "."+ date; 
				messenger.convertAndSend(topic, message);
				responseEntity = new ResponseEntity<String>(String.format("Shift %s in service location %s on board %s_%s is reset", shift.getName(), serviceLocation, location, date), HttpStatus.OK);
			}
			}else{
				responseEntity = new ResponseEntity<String>(String.format("Shift %s does not exist for Board %s_%s serviceLocation %s", shiftId, location, date, serviceLocation), HttpStatus.EXPECTATION_FAILED);
			}
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Uncaught exception '{}' ", e.getMessage(), e);
			responseEntity = new ResponseEntity<String>(String.format("Shift %s on Board %s_%s failed to reset", shift.getName(), location, date), HttpStatus.EXPECTATION_FAILED);

		}
		
		return responseEntity;
	}
	
	@RequestMapping("/clearCategory/{location}/{date}/{serviceLocation}/{shiftId}/{categoryId}")
	public ResponseEntity<String> clearCategory(@PathVariable String location, @PathVariable String date, @PathVariable String serviceLocation, @PathVariable String shiftId, @PathVariable String categoryId, HttpServletRequest request,
			HttpServletResponse response, Principal principal){
		ResponseEntity<String> responseEntity = null;
		Shift shift = null;
		LocationShift locationShift = null;
		ShiftCategory shiftCategory = null;
		Category category = null;
		try {
			Date boardDate = DateUtils.toBoardDateNoNull(date);
			// Get board
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, location);
			BoardContainer boardContainer = boardsCache.get(key, principal);
			Board board = boardContainer.getBoard();
			List<TaskContainer> taskContainers = board.getTaskContainers();
			String locationShiftId = null;
			for (TaskContainer tc : taskContainers) {
				if (serviceLocation.equals(tc.getLocation().getCode())) {
					ConcurrentHashMap<String, LocationShift> locationShiftsMap = tc.getLocationShiftsMap();
					for(String locationShiftKey : locationShiftsMap.keySet()){
						LocationShift locShift = locationShiftsMap.get(locationShiftKey);
						Shift shft = locShift.getShift();
						if(shft.getName().equalsIgnoreCase(shiftId)){
							locationShiftId = locShift.getId();
							shift = shft;
							locationShift = locShift;
							break;
						}
					}
					if(locationShift != null){
					ConcurrentHashMap<String, ShiftCategory> shiftCategoriesMap = locationShift.getShiftCategoriesMap();
					for(String shiftCategoryId: shiftCategoriesMap.keySet()){
						ShiftCategory shiftCat = shiftCategoriesMap.get(shiftCategoryId);
						Category cat = shiftCat.getCategory();
						if(cat.getName().equalsIgnoreCase(categoryId)){
							shiftCategory = shiftCat;
							category = cat;
							break;
						}
					}
					}
					break;
				}
			}
			if(shift != null && category != null){
			Map<String, String> commandContent = new LinkedHashMap<String, String>();
			commandContent.put("serviceLocationId", serviceLocation);
			commandContent.put("locationShiftId", locationShiftId);
			commandContent.put("shiftCategoryId", shiftCategory.getId());
			commandContent.put("shiftId", shift.getId().toString());
			commandContent.put("categoryId", category.getId().toString());
			CommandMessage message = new CommandMessage();
			message.setCommandContent(commandContent);
			message.setCommandName("RemoveCategory");
			message.setDate(date);
			message.setLocation(location);
			AbstractBoardCommand command = null;
		
			// Convert message to actual command
			command = reflectionBoardCommandFactory.createCommand(message);		
			
			if (boardContainer == null || boardContainer.getBoard() == null) {

				// Stop forwarding of message to other clients
				responseEntity = new ResponseEntity<String>("BoardContainer or board didn't exist", HttpStatus.BAD_REQUEST);;
			}
			else{
			// Execute command on board
				synchronized (boardContainer) {
					// Set mandatory fields
					command.setSystemUser(principal.getName());
					command.setSystemDateTime(new Date());
	
					// Execute command
					command.execute(boardContainer.getBoard());
	
					// Update command message
					message.setServerSequence(boardContainer.getAutoIncrementSequence());
					message.setUser(command.getSystemUser());
					message.setCommandContent(command);
				}
				String topic = "/topic/commands." + location + "."+ date; 
				messenger.convertAndSend(topic, message);
				responseEntity = new ResponseEntity<String>(String.format("Category %s in Shift %s in service location %s on board %s_%s is reset", category.getName(), shift.getName(), serviceLocation, location, date), HttpStatus.OK);
			}
			}else if(shift == null){
				responseEntity = new ResponseEntity<String>(String.format("Shift %s does not exist for Board %s_%s serviceLocation %s", shiftId, location, date, serviceLocation), HttpStatus.EXPECTATION_FAILED);
			}else if(category == null){
				responseEntity = new ResponseEntity<String>(String.format("Category %s does not exist for Board %s_%s serviceLocation %s", categoryId, location, date, serviceLocation), HttpStatus.EXPECTATION_FAILED);
			}
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Uncaught exception '{}' ", e.getMessage(), e);
			responseEntity = new ResponseEntity<String>(String.format("Category %s in Shift %s on Board %s_%s failed to reset", categoryId, shiftId, location, date), HttpStatus.EXPECTATION_FAILED);
		}
		
		return responseEntity;
	}
	
    @RequestMapping("refdata")
    public String dataFileUpload() {
           return "admin/refDataUpdate";
    }

    @RequestMapping(value = "/refdata/fileUpload", method = RequestMethod.POST, consumes={"multipart/form-data"})
    public ResponseEntity<String> importParse(@RequestParam("datatype") String dataType, @RequestParam("dataFile") MultipartFile dataFile, Principal principal) throws Exception{
           String tmpDir = System.getProperty("java.io.tmpdir") + "smartboard-refdata-update/";
           
           refDataService.dataFileUpload(dataFile, tmpDir, principal.getName());
           return new ResponseEntity<String>(String.format("File: %s is uploaded", dataFile.getName()), HttpStatus.OK);
           
    }
    
    @RequestMapping("/changemode")
    public ResponseEntity<String> changeApplicationMode(@RequestParam("mode") String mode){
       refDataService.changeApplicationMode(mode);
       return new ResponseEntity<String>("Application mode change was successfully submitted", HttpStatus.OK);
    }
    
    @RequestMapping("/refresh/refdata")
    public ResponseEntity<String> refreshReferenceDataCaches(){
		CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.ALL_CACHES);
		sendRefreshCacheCommand(adminCommand);
        return new ResponseEntity<String>("Reference data caches were successfully refreshed", HttpStatus.OK);
    }
    
    @RequestMapping("/refresh/boards")
    public ResponseEntity<String> refreshBoardsCaches(){
    	CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.BOARD_CACHES);
    	sendRefreshCacheCommand(adminCommand);
    	return new ResponseEntity<String>("Reference data caches were successfully refreshed", HttpStatus.OK);
    }
    
    @RequestMapping("/application/status")
    public ResponseEntity<String> applicatonMode(){
    	String mode = ApplicationSettings.APPLICATION_MODE_NORMAL;
    	Object modeObj = webApp.getServletContext().getAttribute(ApplicationSettings.APPLICATION_MODE);
    	if(modeObj!=null){
    		mode = String.valueOf(modeObj);
    	}
    	return  new ResponseEntity<String>(mode, HttpStatus.OK);
    }
    
}

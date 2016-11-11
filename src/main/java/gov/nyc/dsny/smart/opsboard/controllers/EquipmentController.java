package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.equipment.MaterialTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.converters.Convert;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentCondition;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.integration.mapper.EquipmentEntityMapper;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.SeriesRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.EquipmentExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.Utils;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Attach;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.CancelDetach;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Detach;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.DownEquipment;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.UpEquipment;

import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The entry point for all actions taken on a piece of equipment.
 *
 * When an action is requested, the controller verifies the data inputs, retrieves the piece of equipment in question,
 * performs the action on the system of record (SOR), constructs the necessary multi-board commands, updates the
 * database and routes the commands to their destination locations.
 */
@RestController
@Validated
public class EquipmentController extends SorController {

	private static final Logger log = LoggerFactory.getLogger(EquipmentController.class);

	@Autowired
	private BoardCacheService boardsCache;

	@Autowired
	private EquipmentExecutor executor;

	private final static String SUCCESS = "Success";
	
	@Autowired
	private BoardEquipmentCacheService boardEquipmentCache;
	
	@Autowired
	private EquipmentPersistenceService equipmentPersistenceService;

	@Autowired
	private LocationCache locationCache;

	@Autowired
	private MaterialTypeCache materialTypeCache;

	@Autowired
	private EquipmentEntityMapper equipmentMapper;

	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	@RequestMapping(value = "/AttachEquipment/{boardLocation}/{boardDate}/{equipmentID}", method = RequestMethod.POST)
	@ResponseBody
	public String attachEquipment(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@PathVariable(value = "equipmentID") String equipmentId,
			@Convert @RequestBody Attach attach, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws ParseException, OpsBoardError {

		log.debug("Attach Equipment processing started for equipment {}.", equipmentId);

		BoardEquipment be = null;
		try {
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// TODO - validate arguments

			// Resolve references (all throw errors if not found)
			Location l = locationCache.getLocation(boardLocation, date);
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, l);
			be = boardEquipmentCache.get(key, BoardEquipment.CREATE_ID(equipmentId, boardDate));
			
			// Perform attachment
			executor.attach(key, be, attach.getEntity(), principal);
		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Received By", attach.getReceivedBy(), "Datetime", attach.getReceivedDatetime(),
					"Remarks", attach.getRemarks(), "Equipment", be, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Attach Equipment processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be != null && be.getEquipment() != null ? be.getEquipment().getId() : null, obe.getMessage(),
					obe.getCode(), obe);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Attach Equipment processing completed.");

		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}

	@RequestMapping(value = "/CancelEquipmentDetachment/{boardLocation}/{boardDate}/{equipmentId}", method = RequestMethod.POST)
	@ResponseBody
	public String cancelEquipmentDetachment(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable("boardDate") String boardDate, @PathVariable("equipmentId") String equipmentId,
			@Convert @RequestBody CancelDetach cancel,
			HttpServletRequest request, HttpServletResponse response, Principal principal) throws ParseException,
			OpsBoardError {

		log.debug("Cancel Equipment Detachment processing started for equipment {}.", equipmentId);

		BoardEquipment be = null;
		try {
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// TODO - validate arguments

			// Resolve references (all throw errors if not found)
			Location l = locationCache.getLocation(boardLocation, date);
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, l);
			be = boardEquipmentCache.get(key, BoardEquipment.CREATE_ID(equipmentId, boardDate));
			
			// Perform cancellation
			executor.cancelDetach(key, be, cancel.getEntity(), principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Equipment", be, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Cancel Equipment Detachment processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be != null && be.getEquipment() != null ? be.getEquipment().getId() : null, obe.getMessage(),
					obe.getCode(), obe);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Cancel Equipment Detachment processing completed.");

		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}

	@RequestMapping(value = "/DetachEquipment/{boardLocation}/{boardDate}/{equipmentId}", method = RequestMethod.POST)
	@ResponseBody
	public String detachEquipment(@PathVariable("boardDate") String boardDate,
			@PathVariable("boardLocation") String boardLocation, 
			@PathVariable("equipmentId") String equipmentId,
			@Convert @RequestBody Detach detach,
			HttpServletRequest request, HttpServletResponse response, Principal principal) throws ParseException,
			OpsBoardError {

		log.debug("Detach Equipment processing started for equipment {}.", equipmentId);

		BoardEquipment be = null;
		try {
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// TODO - validate arguments

			// Resolve references (all throw errors if not found)
			Location l = locationCache.getLocation(boardLocation, date);
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, l);
			be = boardEquipmentCache.get(key, BoardEquipment.CREATE_ID(equipmentId, boardDate));
			
			// Perform detachment
			executor.detach(key, be, detach.getEntity(), principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "From", detach.getFrom(), "To", detach.getTo(), "Datetime", detach.getDatetime(), "Driver",
					detach.getDriver(), "Equipment", be, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Detach Equipment processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be != null && be.getEquipment() != null ? be.getEquipment().getId() : null, obe.getMessage(),
					obe.getCode(), obe);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Detach Equipment processing completed.");

		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}

	@RequestMapping(value = "/DownEquipment/{boardLocation}/{boardDate}/{equipmentId}", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public String downEquipment(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@PathVariable(value = "equipmentId") String equipmentId,
			@Convert @RequestBody DownEquipment downEquipment, HttpServletRequest request,
			HttpServletResponse response, Principal principal) throws ParseException, OpsBoardError {

		log.debug("Down Equipment processing started for equipment {}.", equipmentId);
		Date date= DateUtils.toBoardDateNoNull(boardDate);
		
		validateBoardDate(date);		

		BoardKey boardKey = null;
		BoardEquipment be = null;
		try {
			// Lookup equipment to down
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			be = boardEquipmentCache.get(boardKey, BoardEquipment.CREATE_ID(equipmentId, boardDate));

			// Perform down
			executor.down(boardKey, be, downEquipment.getEntity(), principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data",
					new Object[] { "Body", downEquipment.getConditions(), "Equipment", be, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Down Equipment processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be.getEquipment().getId(), obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Down Equipment processing completed for equipment {}.", equipmentId);

		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}

	/*@RequestMapping(value = "/UpdateDownEquipment/{boardLocation}/{boardDate}/{equipmentId}/{downId}", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public String updateDownEquipment(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@PathVariable(value = "equipmentId") String equipmentId, @PathVariable(value = "downId") String downId,
			@Valid @RequestBody Set<EquipmentCondition> conditions, HttpServletRequest request,
			HttpServletResponse response, Principal principal) throws ParseException, OpsBoardError {
		log.debug("Down Equipment processing started for equipment {}.", equipmentId);

		// Fix up list
		Iterator<EquipmentCondition> it = conditions.iterator();
		while (it.hasNext()) {
			EquipmentCondition c = it.next();
			if (c.getDownCode() == null || c.getDownCode().isEmpty()) {
				it.remove(); // remove incomplete records
			} else {
				c.setDown(true); // set each record to down
				c.setSystemUser(principal.getName());
			}
		}

		BoardKey boardKey = null;
		BoardEquipment be = null;
		UpDown newDown = new UpDown(true, conditions);
		try {
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			// Lookup equipment to down
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			be = boardEquipmentCache.get(boardKey, equipmentId);
			if (be == null) {
				throw new OpsBoardError(ErrorMessage.EQUIPMENT_NOT_FOUND);
			}

			// Check for duplicate request
			UpDown currentDown = be.getEquipment().getUpDownById(downId);

			if (currentDown == null) {
				throw new OpsBoardError(ErrorMessage.DOWN_EQUIPMENT);
			}

			if (newDown.compareTo(currentDown) == 0) {
				return SUCCESS;
			}

			// Perform update down
			executor.updateDown(boardKey, be, newDown, currentDown, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data",
					new Object[] { "Body", conditions, "Equipment", be, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Down Equipment processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be.getEquipment().getId(), obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Down Equipment processing completed for equipment {}.", equipmentId);

		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}*/

	@RequestMapping(value = "/UpdateEquipmentLoad/{boardLocation}/{boardDate}/{equipmentID}", method = RequestMethod.POST)
	@ResponseBody
	public String updateEquipmentLoad(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@PathVariable(value = "equipmentID") String equipmentId,
			@NotEmpty @Size(min=1, max=2) @Convert @RequestBody List<gov.nyc.dsny.smart.opsboard.viewmodels.equipment.Bin> bins) throws ParseException,
			OpsBoardError {

		log.debug("Update Equipment Bin Load processing started for equipment {}.", equipmentId);
		
		BoardEquipment be = null;
		Bin bin1 = null;
		Bin bin2 = null;
		try {					
			// Resolve references (all throw errors if not found)
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Get bins		
			bin1 = bins.get(0).getEntity();
			
			if(bins.size() > 1)
				bin2 = bins.get(1).getEntity();
			
			Location l = locationCache.getLocation(boardLocation, date);
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, l);
			be = boardEquipmentCache.get(key, BoardEquipment.CREATE_ID(equipmentId, boardDate));
			
			// Perform update bin load
			executor.updateBinLoad(key, be, bin1, bin2);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Bin 1", bin1, "Bin 2", bin2,
					"Equipment", be, "User", Utils.getUserId() });

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Update Equipment Bin Load processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be != null && be.getEquipment() != null ? be.getEquipment().getId() : null, obe.getMessage(),
					obe.getCode(), obe);

//			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			throw obe;
		}

		log.debug("Update Equipment Bin Load processing completed.");		

		return SUCCESS;
	}
	
	@RequestMapping(value = "/UpdateEquipmentSnowReadiness/{boardLocation}/{boardDate}/{equipmentID}", method = RequestMethod.POST)
	@ResponseBody
	public String updateEquipmentSnowReadiness(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@PathVariable(value = "equipmentID") String equipmentId, @Convert @RequestBody SnowReadiness snowReadiness,
			HttpServletRequest request, HttpServletResponse response, Principal principal) throws ParseException,
			OpsBoardError, OpsBoardValidationException {

		log.debug("Update Equipment Snow Readiness processing started for equipment {}.", equipmentId);
		
		BoardEquipment be = null;		
		try {

			// Resolve references (all throw errors if not found)
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			Location l = locationCache.getLocation(boardLocation, date);
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, l);
			be = boardEquipmentCache.get(key, BoardEquipment.CREATE_ID(equipmentId, boardDate));
			
			// Perform update snow readiness
			executor.updateSnowReadiness(key, be, snowReadiness.getEntity(), principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[]{"SnowReadiness", snowReadiness.toString(), "Equipment", be,
					"User", principal.getName()});

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Update Equipment Snow Readiness processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be != null && be.getEquipment() != null ? be.getEquipment().getId() : null, obe.getMessage(), obe.getCode(), obe);
			
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Update Equipment Snow Readiness processing completed.");
		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}

	// SMARTOB-1745
	/*
	 * @RequestMapping(value = "/CheckEquipmentLoadStatus/{boardLocation}/{boardDate}/{taskID}/{equipmentID}", method =
	 * RequestMethod.POST)
	 * 
	 * @ResponseBody public String checkEquipmentLoadStatus (@PathVariable(value = "boardLocation") String
	 * boardLocation,
	 * 
	 * @PathVariable(value = "boardDate") String boardDate,
	 * 
	 * @PathVariable(value = "taskID") String taskID,
	 * 
	 * @PathVariable(value = "equipmentID") String equipmentId, HttpServletRequest request, HttpServletResponse
	 * response, Principal principal) throws ParseException,OpsBoardError {
	 * log.debug("checkEquipmentLoadStatus started for equipment {}.", equipmentId);
	 * 
	 * // TODO validate parameters BoardEquipment boardEquipment = null; BoardKey boardKey= null; try { boardEquipment =
	 * equipmentCache.get(boardKey, BoardEquipment.CREATE_ID(equipmentId, boardDate)); Location location =
	 * locationCache.getLocation(boardLocation); boardKey = boardKeyFactory.createBoardKey(boardDate, location); BoardContainer
	 * boardContainer = boardsCache.getBoardContainer(boardKey);
	 * 
	 * List<Task> tasks = new ArrayList<Task>(boardContainer.getBoard().getTasks()); Task searchTask = new Task();
	 * searchTask.setId(taskID); int index = tasks.indexOf(searchTask); if (index < 0) throw new
	 * OpsBoardError(ErrorMessage.TASK_NOT_FOUND);
	 * 
	 * Task task = tasks.get(index); //
	 * executor.checkEquipmentLoadStatus(boardEquipment,boardKey,taskID,task.getStartDate(),task.getEndDate(),principal
	 * );
	 * 
	 * } catch (OpsBoardError obe) {
	 * 
	 * // Add request data //obe.addDebugData("Request Data", new Object[]{"Received By", receivedBy, "Datetime",
	 * receivedDatetime, // "Remarks", remarks, "Equipment", be, "User", principal.getName()});
	 * 
	 * // Add debug links addDebugLinks(obe, boardEquipment);
	 * 
	 * // Log and return error log.error(
	 * "checkEquipmentLoadStatus for equipment {} resulted in an error.  Error message: {}; error code: {}.",
	 * boardEquipment != null && boardEquipment.getEquipment() != null ? boardEquipment.getEquipment().getId() : null,
	 * obe.getMessage(), obe.getCode(), obe);
	 * 
	 * response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); return obe.toJson(); }
	 * 
	 * log.debug("Attach Equipment processing completed.");
	 * 
	 * response.setStatus(HttpServletResponse.SC_OK);
	 * 
	 * return SUCCESS; }
	 */

	@RequestMapping(value = "/UpEquipment/{boardLocation}/{boardDate}/{equipmentId}", method = RequestMethod.POST)
	@ResponseBody
	public String upEquipment(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@PathVariable(value = "equipmentId") String equipmentId, @Convert @RequestBody UpEquipment upEquipment,
			HttpServletRequest request, HttpServletResponse response, Principal principal) throws ParseException,
			OpsBoardError {

		log.debug("Up Equipment processing started for equipment {}.", equipmentId);
		
		BoardKey boardKey = null;
		BoardEquipment be = null;
		try {
			// Lookup equipment to up
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			be = boardEquipmentCache.get(boardKey, BoardEquipment.CREATE_ID(equipmentId, boardDate));
			
			// Perform up equipment
			executor.up(boardKey, be, upEquipment.getEntity(), principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Datetime", upEquipment.getDatetime(), "Reporter", upEquipment.getReporter(), "Mechanic",
					upEquipment.getMechanic(), "Equipment", be, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, be);

			// Log and return error
			log.error(
					"Up Equipment processing for equipment {} resulted in an error.  Error message: {}; error code: {}.",
					be.getEquipment().getId(), obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		} catch (Exception ex) {

			// Log error and return
			log.error("Unexpected error during UP equipment: {}", ex.getMessage(), ex);

			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR, ex);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();

		}

		log.debug("Up Equipment processing completed for equipment {}.", equipmentId);

		response.setStatus(HttpServletResponse.SC_OK);

		return SUCCESS;
	}

	/*
	 * @RequestMapping(value = "/Equipments/{locationId}", method = RequestMethod.GET)
	 * 
	 * @ResponseBody public Object getEquipmentList(@PathVariable String locationId, HttpServletRequest request,
	 * HttpServletResponse response, Principal principal) throws OpsBoardError { log.debug("EquipmentList From SCAN");
	 * Equipment equipment = null; List<Equipment> equipmentList = new ArrayList<Equipment>(); try {
	 * List<EquipmentModelWithHistory> equipmentModelWithHistoryList = integrationFacade.getEquipmentList(locationId);
	 * if (equipmentModelWithHistoryList != null) { for (EquipmentModelWithHistory equipmentModelWithHistory :
	 * equipmentModelWithHistoryList) { //equipement = equipmentMapper.convert(equipmentModelWithHistory,
	 * seriesRepository.getOne(Long.parseLong(equipmentModelWithHistory.getVehicleNumber()))); //equipement =
	 * equipmentMapper.convert(equipmentModelWithHistory, seriesRepository.getOne(new Long(425))); Series series = new
	 * Series(); series.setId(new Long(425)); equipment = equipmentMapper.convert(equipmentModelWithHistory, series);
	 * equipmentList.add(equipment); } } } catch (Exception e) { log.error("Unexpected error getting equipment: {}",
	 * e.getMessage(), e); OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR, e);
	 * response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); return obe; } return equipmentList; }
	 */

	/*
	 * @RequestMapping(value="/equipment/load/all", method = RequestMethod.POST)
	 * 
	 * @ResponseBody public Object loadEquipmentFromScan(){ List<Equipment> equipment = new ArrayList<>(); try{
	 * List<Location> locations = integrationFacade.getLocations(); List<Series> serieses =
	 * integrationFacade.getEquipmentSeries();
	 * 
	 * for(Location location : locations){ List<EquipmentModelWithHistory> equipmentFromScan =
	 * integrationFacade.getEquipmentList(location.getCode()); long l = 1L; if(null != equipmentFromScan){
	 * for(EquipmentModelWithHistory eq : equipmentFromScan){
	 * 
	 * Series series = new Series(); series.setId(eq.getVehicleSeries());
	 * 
	 * equipment.add(equipmentMapper.convert(eq, serieses.get(serieses.indexOf(series)))); }
	 * equipmentPersistenceService.save(equipment); } } }catch(Exception e){
	 * 
	 * } return null; }
	 */

	/**
	 * @RequestMapping(value = "/equipment", method = RequestMethod.GET)
	 * @ResponseBody public Object getEquipmentList(HttpServletRequest request, HttpServletResponse response, Principal
	 *               principal) {
	 * 
	 *               List<OpsBoardEquipmentShort> res = new ArrayList<OpsBoardEquipmentShort>(); try {
	 *               logContext.initContext(request, principal); for (Iterator iterator =
	 *               equipmentCache.getEquipmenMap().values().iterator(); iterator.hasNext(); ) { BoardEquipment t =
	 *               (BoardEquipment) iterator.next(); OpsBoardEquipmentShort ob = new OpsBoardEquipmentShort(t.getId(),
	 *               t.getOwner().getCode(), t.getState(t.getOwner()).getLocation().getCode(),
	 *               t.getState(t.getOwner()).getState(),
	 *               request.getRequestURL().toString()+(request.getRequestURL().toString().endsWith("/") ? "" : "/") +
	 *               t.getId()); res.add(ob); } return new ResponseEntity<List>(res, HttpStatus.OK); } catch (Exception
	 *               e) { log.error(appendEntries(logContext),
	 *               "Unexpected error during getting equipment from cache. Error: {}", e.getMessage(), e);
	 *               OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
	 *               obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage()); return obe;
	 *               }
	 * 
	 *               }
	 */

}

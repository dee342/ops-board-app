package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.services.sorexecutors.PersonExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.validation.ValidationUtils;
import gov.nyc.dsny.smart.opsboard.viewmodels.MassChartRequest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * The entry point for all actions taken on a person.
 * <p/>
 * When an action is requested, the controller verifies the data inputs, retrieves the person in question, and calls the
 * executor which in turn performs the action on the system of record (SOR), constructs the necessary multi-board
 * commands, updates the database and routes the commands to their destination locations.
 */
@RestController
public class PersonController extends SorController {

	private static final Logger log = LoggerFactory.getLogger(PersonController.class);

	@Autowired
	private BoardCacheService boardCache;

	@Autowired
	private BoardPersonnelCacheService boardPersonnelCache;

	@Autowired
	private PersonExecutor executor;

	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	@RequestMapping(value = "/AddPersonMdaStatus/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String addMdaStatus(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody MdaStatus mda, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Add MDA Status processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;
		try {

			// Lookup person
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			// Perform operation
			executor.addMdaStatus(boardKey, bp, mda, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data",
					new Object[] { "MDA Status", mda, "Person", bp, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Add MDA Status processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Add MDA Status processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}

	@RequestMapping(value = "/AddSpecialPosition/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String addSpecialPosition(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody SpecialPosition specialPosition, HttpServletRequest request,
			HttpServletResponse response, Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Add San Worker Special Position processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			validateBoardDate(date);
			
			// Lookup person
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			executor.addSpecialPosition(boardKey, bp, specialPosition, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "San Worker Special Position", specialPosition, "Person",
					bp, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Add San Worker special position processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Add San Worker Special Position processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}

	@RequestMapping(value = "/AddPersonUnavailabilityReason/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8", produces = "application/json")
	@ResponseBody
	public String addUnavailabilityReason(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody UnavailabilityReason reason, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Make Person Unavailable processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {

			// Lookup equipment to attach
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			executor.addUnavailabilityReason(boardKey, bp, reason, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Unavailable Reason", reason, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Unavailable record processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Make Person Unavailable processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}
	//TODO THIS NEEDS VALIDATION
	@RequestMapping(value = "/CancelDetachPerson/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST)
	@ResponseBody
	public String cancelDetachPerson(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody Detachment detachment, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("RemoveDetachPerson processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {

			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup person
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}
			executor.cancelDetachPerson(boardKey, bp, principal, detachment);
			
		} catch (OpsBoardError obe) {

			// Add request data
			// obe.addDebugData("Request Data", new Object[] { "Unavailable Reason", reason, "Person", bp, "User",
			// principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Remove Detach Person processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Remove Person Unavailable processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}
	
	@RequestMapping(value = "/UpdateDetachPerson/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST)
	@ResponseBody
	public String updateDetachPerson(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody Detachment detachment, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("UpdateDetachPerson processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {

			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup person
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}
			executor.updateDetachPerson(boardKey, bp, principal, detachment);

		} catch (OpsBoardError obe) {

			// Add request data
			// obe.addDebugData("Request Data", new Object[] { "Unavailable Reason", reason, "Person", bp, "User",
			// principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Update Detach Person processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Update Person Unavailable processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}	

	@RequestMapping(value = "/CancelPersonUnavailability/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String cancelUnavailabilityReason(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody UnavailabilityReason reason, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Cancel unavailable record for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {

			// Lookup equipment to attach
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			// Validate input parameters
			// MOVED TO EXECUTOR
			/*validateUnavailabilityReason(reason.getId(), reason.getCode(), reason.getStart(), reason.getEnd(),
					reason.getComments(), bp, "CANCEL_UNAVAILABLE");*/

			executor.cancelUnavailabilityReason(boardKey, bp, reason, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Unavailable Reason", reason, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Canceling unavailable record processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Cancel Person Unavailable processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}
	
	@RequestMapping(value = "/massChartUpdate/{boardLocation}/{boardDate}/{chartDate}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String massChartUpdate(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "chartDate") String chartDate,  @RequestBody MassChartRequest multipleChartRequest,
			HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {
		log.debug("MassChartUpdate for Board {}.", boardLocation);	
		BoardKey boardKey = null;
		BoardKey chartKey = null;
		BoardPerson bp = null;
		List<BoardPerson> cancelBoardPersons = new ArrayList<BoardPerson>();
		List<BoardPerson> reverseCancelBoardPersons = new ArrayList<BoardPerson>();
		List<String> personNotFoundMessages = new ArrayList<String>();
		
		try {

			Date date= DateUtils.toBoardDateNoNull(boardDate);
			Date formattedChartDate= DateUtils.toBoardDateNoNull(chartDate);
			//validate Board Date
			validateBoardDate(date);
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			chartKey = boardKeyFactory.createBoardKey(chartDate, locationCache.getLocation(boardLocation, formattedChartDate));
			for(String personId: multipleChartRequest.getCancelPersonIds()){
				bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
				if (bp == null) {
					personNotFoundMessages.add("Person with id : "+ personId+" is not Found");
					continue;
				}else{
					cancelBoardPersons.add(bp);
				}
			}
			for(String personId: multipleChartRequest.getReverseCancelPersonIds()){
				bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
				if (bp == null) {
					personNotFoundMessages.add("Person with id : "+ personId+" is not Found");
				}else{
					reverseCancelBoardPersons.add(bp);
				}
			}
			
			List<String> errorMessages = executor.massChartUpdate(boardKey,chartKey,formattedChartDate, cancelBoardPersons, reverseCancelBoardPersons, principal);
			if(errorMessages.size()>0){
				throw new OpsBoardValidationException(new OpsBoardError(ErrorMessage.SERVER_VALIDATION_ERROR,
						errorMessages));
			}
			if(personNotFoundMessages.size()>0){
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND,
						errorMessages);
			}

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "MassChartUpdate", multipleChartRequest, "Board", boardLocation, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"MassChartUpdate for Board {} resulted in an error.  Error message: {}; error code: {}.",
					boardLocation, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("MassChartUpdate processing completed for Board {}.", boardLocation);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";
	}

	@RequestMapping(value = "/DetachPerson/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST)
	@ResponseBody
	public String detachPerson(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody Detachment detachment, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Detach Person processing started for person {}.", personId);
		BoardKey bk = null;
		BoardPerson bp = null;
		try {
			
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup person to detach
			bk = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(bk, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			Location fromLocation = locationCache.getLocation(detachment.getFromCode(), date);
			Location toLocation = locationCache.getLocation(detachment.getToCode(), date);

			detachment.setFrom(fromLocation);
			detachment.setTo(toLocation);

			// Perform detach
			executor.detachPerson(bk, bp, principal, detachment);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData(
					"Request Data",
					new Object[] {
							"Detachment",
							new Object[] { "From", detachment.getFromCode(), "To", detachment.getToCode(), "StartDate",
									detachment.getStartDate(), "endDate", detachment.getEndDate(), "Shift",
									detachment.getShift(), "User", principal.getName() }, "Comments",
							detachment.getComments(), "Person", bp });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Detach Person processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Detach Person processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";
	}

	@RequestMapping(value = "/RemovePersonMdaStatus/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String removeMdaStatus(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody MdaStatus requestStatus, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Remove MDA Status processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup person
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			// Perform operation
			executor.removeMdaStatus(boardKey, bp, requestStatus, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Mda Status", requestStatus, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Remove MDA Status processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Remove MDA Status processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}

	@RequestMapping(value = "/RemoveSpecialPosition/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String removeSpecialPosition(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody SpecialPosition specialPosition, HttpServletRequest request,
			HttpServletResponse response, Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Remove San Worker Special Position processing started for person {}.", personId);
		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			
			// Lookup person
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			executor.removeSpecialPosition(boardKey, bp, specialPosition, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Special Position", specialPosition, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Remove San Worker Special Position processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Remove San Worker Special Position processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}

	@RequestMapping(value = "/RemovePersonUnavailabilityReason/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String removeUnavailabilityReason(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody UnavailabilityReason reason, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Remove Person Unavailable processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			// Lookup person
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			executor.removeUnavailabilityReason(boardKey, bp, reason, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Unavailable Reason", reason, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Remove Person processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Remove Person Unavailable processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}

	@RequestMapping(value = "/ReverseCancelPersonUnavailability/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String reverseCancelUnavailabilityReason(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody UnavailabilityReason reason, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Reverse Cancel unavailable record for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup equipment to attach
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			// Validate input parameters
			// MOVED TO EXECUTOR
			/*validateUnavailabilityReason(reason.getId(), reason.getCode(), reason.getStart(), reason.getEnd(),
					reason.getComments(), bp, "REVERSE_CANCEL_UNAVAILABLE");*/

			executor.reverseCancelUnavailabilityReason(boardKey, bp, reason, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Unavailable Reason", reason, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Canceling unavailable record processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Reverse Cancel Person Unavailable processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}

	@RequestMapping(value = "/UpdatePersonMdaStatus/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String updateMdaStatus(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody MdaStatus mda, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Update MDA Status processing started for person {}.", personId);

		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup person
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			// Perform operation
			executor.updateMdaStatus(boardKey, bp, mda, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data",
					new Object[] { "MDA Status", mda, "Person", bp, "User", principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Update MDA Status processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);

			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Update MDA Status processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";
	}

	@RequestMapping(value = "/UpdateSpecialPosition/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String updateSpecialPosition(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody SpecialPosition specialPosition, HttpServletRequest request,
			HttpServletResponse response, Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Update San Worker Special Position processing started for person {}.", personId);
		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup person
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}
			executor.updateSpecialPosition(boardKey, bp, specialPosition, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Special Position", specialPosition, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Update San Worker Special Position processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Remove San Worker Special Position processing completed for person {}.", personId);

		response.setStatus(HttpServletResponse.SC_OK);

		return "Success";

	}

	@RequestMapping(value = "/UpdatePersonUnavailabilityReason/{boardLocation}/{boardDate}/{personId}", method = RequestMethod.POST, consumes = "application/json; charset=UTF-8")
	@ResponseBody
	public String updateUnavailabilityReason(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate, @PathVariable(value = "personId") String personId,
			@Valid @RequestBody UnavailabilityReason reason, HttpServletRequest request, HttpServletResponse response,
			Principal principal) throws OpsBoardError, OpsBoardValidationException {

		log.debug("Update Person Unavailable processing started for person {}.", personId);
		BoardKey boardKey = null;
		BoardPerson bp = null;

		try {
			
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			
			validateBoardDate(date);
			
			// Lookup equipment to attach
			boardKey = boardKeyFactory.createBoardKey(boardDate, locationCache.getLocation(boardLocation, date));
			bp = boardPersonnelCache.get(boardKey, BoardPerson.CREATE_ID(personId, boardDate));
			if (bp == null) {
				throw new OpsBoardError(ErrorMessage.PERSON_NOT_FOUND);
			}

			// Validate input parameters
			/*validateUnavailabilityReason(reason.getId(), reason.getCode(), reason.getStart(), reason.getEnd(),
					reason.getComments(), bp, "UPDATE_UNAVAILABLE");
*/
			executor.updateUnavailabilityReason(boardKey, bp, reason, principal);

		} catch (OpsBoardError obe) {

			// Add request data
			obe.addDebugData("Request Data", new Object[] { "Unavailable Reason", reason, "Person", bp, "User",
					principal.getName() });

			// Add debug links
			addDebugLinks(obe, bp);

			// Log and return error
			log.error(
					"Update Person processing for person {} resulted in an error.  Error message: {}; error code: {}.",
					personId, obe.getMessage(), obe.getCode(), obe);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return obe.toJson();
		}

		log.debug("Update Person Unavailable processing completed for person {}.", personId);
		response.setStatus(HttpServletResponse.SC_OK);
		return "Success";
	}
	

}

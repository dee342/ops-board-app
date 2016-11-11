package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.VolunteerCounts;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.services.executors.ReportsExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.validation.ValidationUtils;

import java.security.Principal;
import java.text.ParseException;
import java.util.Date;

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
 * SMARTOB-4338 Add Volunteer Counts
 * The entry point for actions taken on Reports.
 * When an action is requested, the controller verifies the data inputs, and delegates
 * processing to Executor
 */
@RestController
public class ReportsController extends BoardController{
	
	private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);
	
	@Autowired
	private ReportsExecutor reportsExecutor;
		
	@Autowired
	BoardCacheService boardCache;
	
	@Autowired
	LocationCache locationCache;
	
	@Autowired
	BoardKeyFactory boardKeyFactory;
	
	@RequestMapping(value = "/SaveVolunteerCounts/{boardLocation}/{boardDate}", method = RequestMethod.POST, consumes = "application/json")
	@ResponseBody
	public String saveVolunteerCounts(@PathVariable(value = "boardLocation") String boardLocation,
			@PathVariable(value = "boardDate") String boardDate,
			@Valid @RequestBody VolunteerCounts volunteerCountsRequest,
			HttpServletRequest request, HttpServletResponse response, Principal principal) throws ParseException, OpsBoardError, OpsBoardValidationException {
		logger.debug("Started SaveVolunteerCounts");		
		ErrorMessage validateMessage=null;
	
		try{
			Date date= DateUtils.toBoardDateNoNull(boardDate);
			Location boardLoc = locationCache.getLocation(boardLocation, date);
			BoardKey key = boardKeyFactory.createBoardKey(boardDate, boardLoc);
			Board board = boardCache.get(key).getBoard();		
			validateMessage = ValidationUtils.validateVolunteerCounts(volunteerCountsRequest, board.getPersonnel());
			//redundant
			if(validateMessage!=null){
				OpsBoardValidationException obve = generateOpsBoardValidationException(validateMessage);
				throw obve;
			}
			// delegate processing to executor
			reportsExecutor.saveVolunteerCounts(key, board, boardLoc, volunteerCountsRequest, principal, boardLoc);
		} catch (OpsBoardValidationException obe) {
			return obe.getOpsBoardError().toJson();
		} catch(OpsBoardError obve){
			logger.error("OpsBoardValidationException inside saveVolunteerCounts", obve);
			return obve.toJson();
		}
		logger.debug("Saving Volunteers Counts completed.");
		response.setStatus(HttpServletResponse.SC_OK);
		return SUCCESS;
	}	
		
}

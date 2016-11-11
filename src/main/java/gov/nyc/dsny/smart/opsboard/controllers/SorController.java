package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Represents the base controller for commands on entities whose system of
 * record (SOR) is external to the Operations Board. All of the commands are
 * considered multi-board commands as the SOR entities are shared across boards.
 */
@Controller
public abstract class SorController {
	
	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private ShiftCacheService shiftCache;
	

	/**
	 * Method to equipment data to the error messages to aide testers in their
	 * data investigations.
	 *
	 * @param obe
	 *            operations board error
	 * @param BoardEquipment
	 *            equipment entity
	 */
	protected void addDebugLinks(OpsBoardError obe, BoardEquipment e) {

		StringBuffer sb = new StringBuffer("[");

		if (e != null) {
			// Add MongoDB REST link for specified equipment
			sb.append("\"<a href='/equipment/" + e.getId() + "' target='" + e.getId() + "'>DB Equipment "
					+ e.getId() + " Link</a>\"");

			sb.append(",");

			// Add link SCAN Equipment Checker (generic...used for all
			// equipment)
			sb.append("\"<a href='http://msdwvadsnyadb01.csc.nycnet:8090/smart/listgarages.php' target='scan'>SCAN Equipment Checker</a>\"");
		}

		sb.append("]");

		// Add links to debug data
		obe.addDebugData("Links", sb.toString());

	}

	/**
	 * Method to add links to error messages to aide testers in their data
	 * investigations.
	 *
	 * @param obe
	 *            operations board error
	 * @param key
	 *            board key
	 */
	protected void addDebugLinks(OpsBoardError obe, BoardKey key) {

		StringBuffer sb = new StringBuffer("[");

		if (key != null) {
			// Add DB REST link for specified person
			sb.append("\"<a href='/load/" + key.getLocation() + "/" + key.getDate() + "' target='" + key.getLocation()
					+ "_" + key.getDate() + "'>Board [" + key.getLocation() + "/" + key.getDate()
					+ "] loading link </a>\"");
		}

		sb.append("]");

		// Add links to debug data
		obe.addDebugData("Links", sb.toString());
	}

	/**
	 * Method to person data to the error messages to aide testers in their data
	 * investigations.
	 *
	 * @param obe
	 *            operations board error
	 * @param BoardPerson
	 *            person entity
	 */
	protected void addDebugLinks(OpsBoardError obe, BoardPerson p) {

		StringBuffer sb = new StringBuffer("[");

		if (p != null) {
			// Add DB REST link for specified person
			sb.append("\"<a href='/personnel/" + p.getId() + "' target='" + p.getId() + "'>DB Person " + p.getId()
					+ " Link</a>\"");
		}

		sb.append("]");

		// Add links to debug data
		obe.addDebugData("Links", sb.toString());
	}	
		
	protected void validateBoardDate(Date boardDate) throws OpsBoardError {
		Date currentDate = new Date();		
		Date boardStart = shiftCache.createShiftStart(boardDate);
		Date boardEnd = shiftCache.createShiftEnd(boardDate);
		
		if(!DateUtils.onOrBetween(currentDate, boardStart, boardEnd))
			throw new OpsBoardError(ErrorMessage.ACTION_CANNOT_BE_PERFORMED_FOR_THIS_DATE);
		
	}
	
}
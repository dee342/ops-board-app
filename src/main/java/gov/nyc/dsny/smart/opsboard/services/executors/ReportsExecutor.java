package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.board.CommandSaveVolunteerCounts;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.personnel.VolunteerCounts;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SMARTOB-4338 Add Volunteer Counts
 * @author ssandur
 */
@Service
public class ReportsExecutor extends BoardExecutor{
	
	private static final Logger logger = LoggerFactory.getLogger(ReportsExecutor.class);

	public void saveVolunteerCounts(BoardKey key, Board board,
			Location boardLoc, VolunteerCounts volunteerCountsRequest,
			Principal principal, Location boardLoc2) throws OpsBoardError {
		logger.debug("ReportsExecutor::saveVolunteerCounts");
		//synchronize on the board
		synchronized (board) {
			// create command
			LinkedHashMap<String, Object> content = new LinkedHashMap<String, Object>();
			content.put("systemDateTime", new Date().getTime());
			content.put("systemUser", principal.getName());
			content.put("chartVolunteers", volunteerCountsRequest.getChartVolunteers());
			content.put("mandatoryChart", volunteerCountsRequest.getMandatoryChart());
			content.put("vacationVolunteers", volunteerCountsRequest.getVacationVolunteers());
			
			CommandSaveVolunteerCounts commandSaveVolunteerCounts = new CommandSaveVolunteerCounts(board.getId(), content);
			// execute
			commandSaveVolunteerCounts.execute(board);
			// send command
			sendCommand(commandSaveVolunteerCounts.getName(),
					commandSaveVolunteerCounts, board, principal.getName());
		}		
	}

}

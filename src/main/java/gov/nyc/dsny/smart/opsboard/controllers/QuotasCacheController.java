package gov.nyc.dsny.smart.opsboard.controllers;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.services.executors.AdminExecutor;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/cache/quotas")
public class QuotasCacheController {

	private static final Logger log = LoggerFactory.getLogger(QuotasCacheController.class);

	@Autowired
	private LogContext logContext;

	@Autowired
	private AdminExecutor adminExecutor;

	@RequestMapping(value = "/clear", method = RequestMethod.GET)
	public synchronized Object clearCache(HttpServletRequest request, HttpServletResponse response, Principal principal) {

		try {

			logContext.initContext(request, principal);

			CommandRefreshCaches quotaRefreshCommand = new CommandRefreshCaches(CommandRefreshCaches.QUOTA_CACHE);
			adminExecutor.sendRefreshCacheCommand(quotaRefreshCommand);

			log.info("Quota cache Refresh commands successfully sent");
			return new ResponseEntity<String>("Quota cache Refresh commands successfully sent", HttpStatus.OK);

		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during clear cache {}", e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

}
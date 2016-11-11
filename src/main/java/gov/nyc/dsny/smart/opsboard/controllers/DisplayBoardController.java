package gov.nyc.dsny.smart.opsboard.controllers;

import static net.logstash.logback.marker.Markers.appendEntries;

import java.security.Principal;
import java.util.Date;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.domain.Kiosk;
import gov.nyc.dsny.smart.opsboard.services.executors.KioskExecutor;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Controller
public class DisplayBoardController {

	private static final Logger log = LoggerFactory.getLogger(DisplayBoardController.class);

	public static final String PUBLISH_BOARD = "PublishBoard";

	
	@Autowired
	private KioskExecutor kioskExecutor;

	@Autowired
	private LogContext logContext;

	@MessageMapping("/displayboard.{district}")
	public CommandMessage receiveCommand(CommandMessage message, Principal principal) {

		logContext.initContext(message, principal);

		log.info(appendEntries(logContext), "receiveCommand - User '{}' invokes command '{}'.", principal.getName(),
				message.toString());

		if(StringUtils.equalsIgnoreCase(PUBLISH_BOARD, message.getCommandName())){
			Date cuurentDate = new Date();
			try {
				Kiosk kioskData = kioskExecutor.getKioskByLocation(message.getLocation());
				kioskData.setBoardDate(DateUtils.toBoardDate(message.getDate()));
				kioskData.setLastPublishedDate(cuurentDate);
				kioskExecutor.save(kioskData);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

		}
		try {
			return message;
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Uncaught exception '{}' ", e.getMessage(), e);
			return null;
		}
	}
	
	@MessageMapping("/kioskDashboard")
	public CommandMessage receiveDashboardCommand(CommandMessage message, Principal principal) {

		logContext.initContext(message, principal);
		LinkedHashMap<String, Object> content = (LinkedHashMap<String, Object>) message.getCommandContent();
		String remoteAddress = (String)content.get("remoteAddress");
		try {
			kioskExecutor.updateBoardStatus(remoteAddress, message.getLocation(), true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		log.debug(appendEntries(logContext), "receiveCommand - User '{}' invokes command '{}'.", principal.getName(),
				message.toString());

		try {
			return null;
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Uncaught exception '{}' ", e.getMessage(), e);
			return null;
		}
	}

	@RequestMapping(value = "/displayboard/{location}")
	public String showBoard(@PathVariable String location, HttpServletRequest request, ModelMap model) {

		log.info("Show display board");

		model.addAttribute("location", location);

		return "displayboard";
	}
}

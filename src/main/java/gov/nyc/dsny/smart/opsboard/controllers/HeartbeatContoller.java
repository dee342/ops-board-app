package gov.nyc.dsny.smart.opsboard.controllers;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This controller is used to load and display the Operations Board.
 */
@Controller
@RequestMapping("/{location}")
public class HeartbeatContoller extends SorController {

	private static final Logger log = LoggerFactory.getLogger(HeartbeatContoller.class);

	@RequestMapping(value = "/heartbeat")
	@ResponseBody
	public String heartbeat(HttpServletRequest request) {
		log.debug("Heartbeat for session {}", request.getSession().getId());
		return "OK";
	}

}
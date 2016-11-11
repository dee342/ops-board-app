package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.OpsBoardValidationException;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public abstract class BoardController {

	private static final Logger log = LoggerFactory.getLogger(BoardController.class);
	
	protected static final String SUCCESS = "Success";

	protected OpsBoardValidationException generateOpsBoardValidationException(ErrorMessage errorMessage) {
		List<String> extErrMessages = new ArrayList<String>();
		extErrMessages.add(errorMessage.getMessage());
		return new OpsBoardValidationException(new OpsBoardError(errorMessage, extErrMessages));			
	}

}

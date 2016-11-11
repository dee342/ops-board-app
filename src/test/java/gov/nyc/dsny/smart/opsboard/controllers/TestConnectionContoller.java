package gov.nyc.dsny.smart.opsboard.controllers;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TestConnectionContoller{

	private static final Logger log = LoggerFactory.getLogger(TestConnectionContoller.class);

	@RequestMapping(value = "/test/connection/case1", method = RequestMethod.POST)
	@ResponseBody
	public String testConnectionTimeout(
			@RequestBody String body,
            HttpServletRequest request, 
            HttpServletResponse response
			) {

		try {
			// waiting for 5 minutes
			Thread.sleep(1000*300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		response.setStatus( HttpServletResponse.SC_OK);
		return "Waiting is finished";
	}

	@RequestMapping(value = "/test/connection/case2", method = RequestMethod.POST)
	@ResponseBody
	public String testConnectionError500(
			@RequestBody String body,
            HttpServletRequest request, 
            HttpServletResponse response
			) {

		response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return "Error code 500 has been returned";	
		}
	
	@RequestMapping(value = "/test/connection/case3", method = RequestMethod.POST)
	@ResponseBody
	public String testConnectionReturnBinary(
			@RequestBody String body,
            HttpServletRequest request, 
            HttpServletResponse response
			) {

		response.setStatus( HttpServletResponse.SC_OK);
		StringBuffer sb =  new StringBuffer(); 
		for(int i=1;i<255;i++){
			char c = (char) i;
			sb.append (c);
		}
		return sb.toString();
	}

	@RequestMapping(value = "/test/connection/case4", method = RequestMethod.POST)
	@ResponseBody
	public String testConnectionWrongWSDLResponse(
			@RequestBody String body,
            HttpServletRequest request, 
            HttpServletResponse response
			) {

		response.setStatus( HttpServletResponse.SC_OK);
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soapenv:Body>\n"
				+ "<WrongResponse xmlns=\"WrongTypesList\"><WrongPsft><Code>14</Code><UniqueID>70</UniqueID><EffectiveDate>2013-01-01</EffectiveDate><EffectiveStatus>A</EffectiveStatus><Color>N</Color></WrongPsft></WrongResponse></soapenv:Body> </soapenv:Envelope>";
	}
	
	
	
}
package gov.nyc.dsny.smart.opsboard.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import gov.nyc.dsny.smart.opsboard.domain.Kiosk;
import gov.nyc.dsny.smart.opsboard.services.executors.KioskExecutor;

@Controller
@RequestMapping("/admin/monitor")
public class KioskController {

	@Autowired
	private KioskExecutor kioskExecutor;

	@RequestMapping(value = "/loadDashboard", method = RequestMethod.GET)
	@ResponseBody
	public List<gov.nyc.dsny.smart.opsboard.viewmodels.Kiosk> loadKioskData(HttpServletRequest request, ModelMap model) throws Exception{
		List<Kiosk> kiosks = kioskExecutor.loadAllKiosks();
		List<gov.nyc.dsny.smart.opsboard.viewmodels.Kiosk> viewKiosks = new ArrayList<gov.nyc.dsny.smart.opsboard.viewmodels.Kiosk>();
		for(Kiosk kiosk : kiosks){
			gov.nyc.dsny.smart.opsboard.viewmodels.Kiosk viewKiosk= new gov.nyc.dsny.smart.opsboard.viewmodels.Kiosk(kiosk);
			viewKiosks.add(viewKiosk);
		}
		return viewKiosks;
	}
	
	@RequestMapping(value = "/dashboard", method = RequestMethod.GET)
	public String showDashboard(HttpServletRequest request, ModelMap model) throws Exception{
		return "monitor";
	}
}

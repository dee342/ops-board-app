package gov.nyc.dsny.smart.opsboard.controllers;


import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.services.executors.AdminExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/load")
public class AdminController extends AbstractAdminController
{
	
	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
	
	@Autowired
    private AdminExecutor adminExecutor;
	
	//*********************************************************************************************************************
	//**************************************** Load All     ***************************************************************
	//*********************************************************************************************************************
	@RequestMapping(value="/all", method = RequestMethod.GET)
	@ResponseBody
	@Async
	public ResponseEntity<String> loadAll() throws OpsBoardError
	{
		logger.info("Start loading all data");
		loadAllReferenceData();
		
		try{Thread.sleep(30000);}catch (Exception e){}
        
		adminExecutor.init();
		adminExecutor.loadEquipment();
		
		try{Thread.sleep(2000);}catch (Exception e){}
		
		adminExecutor.loadPersonnel();
		
		logger.info("Finished loading all data");
		
		return new ResponseEntity<String>("Successfully loaded all data from Scan and Peoplesoft.", HttpStatus.OK);
	}
	
	@RequestMapping("/all/refresh/refdata")
    public ResponseEntity<String> refreshReferenceDataCaches(){
    	CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.ALL_CACHES);
		sendRefreshCacheCommand(adminCommand);
        return new ResponseEntity<String>("Reference data caches were successfully refreshed", HttpStatus.OK);
    }

	//*********************************************************************************************************************
	//**************************************** Load All     ***************************************************************
	//*********************************************************************************************************************
	@RequestMapping(value="/referencedata/all", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> loadAllReferenceData() throws DataLoadException
	{
		logger.info("Start loading all reference data data");
		//*****************************************************
		adminExecutor.init();
		adminExecutor.getAndSaveSeries();
		adminExecutor.getAndSaveDownCodes();
		adminExecutor.getAndSaveMaterialTypes();
		adminExecutor.getAndSaveOthereReferenceData();
		adminExecutor.getAndSaveUnavailabilityTypes();
		adminExecutor.getAndSaveSubTypes();
		adminExecutor.getAndSaveLocations();
		
		CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.ALL_CACHES);
		adminExecutor.sendRefreshCacheCommand(adminCommand);
		
		logger.info("*** LoadingInfo ***:  Finished loading all reference data data");
		
		return new ResponseEntity<String>("Successfully loaded all reference data from Scan and Peoplesoft", HttpStatus.OK);
	}
	//*********************************************************************************************************************
	//**************************************** Scan Methods ***************************************************************
	//*********************************************************************************************************************
	@RequestMapping(value="/equipment/all", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> loadEquipment() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.loadEquipment();
		return new ResponseEntity<String>("Successfully loaded full equiment list from Scan.", HttpStatus.OK);
	}
	
	@RequestMapping(value="/equipment/location/{locationCode}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> loadEquipmentByLocation(@PathVariable String locationCode)  throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.loadEquipmentByLocation(locationCode);		
		return new ResponseEntity<String>("Successfully loaded equiment list by location from Scan.", HttpStatus.OK);
	}
	
	@RequestMapping(value="/equipment/{vehicleSeries}/{vehicleNumber}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> loadEquipmentById(@PathVariable String vehicleSeries, @PathVariable String vehicleNumber)  throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.loadEquipmentById(vehicleSeries, vehicleNumber, "unknown");
		return new ResponseEntity<String>("Successfully loaded equiment by id from Scan.", HttpStatus.OK);
	}

	//***********************************************************************************************************************
	//******************** PeopleSoft Methods *******************************************************************************
	//***********************************************************************************************************************
	@RequestMapping(value="/personnel", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> loadPersonnel() throws OpsBoardError
    {
		adminExecutor.init();
		adminExecutor.loadPersonnel();
		return new ResponseEntity<String>("Successfully loaded Personnel data from PeopleSoft.", HttpStatus.OK);
    }
	
	@RequestMapping(value="/personnel/location/{code}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> loadPersonnelFromLocation(@PathVariable String code)  throws DataLoadException
    {
		adminExecutor.init();
		adminExecutor.loadPersonnelFromLocation(code);
        return new ResponseEntity<String>("Successfully loaded Personnel data by location from PeopleSoft.", HttpStatus.OK);
    }
    
	@RequestMapping(value="/personnel/{employeeId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> loadPersonbyId(@PathVariable String employeeId) throws DataLoadException
    {
		adminExecutor.init();
		adminExecutor.loadPersonById(employeeId, null);
		return new ResponseEntity<String>("Successfully loaded Person by id from PeopleSoft.", HttpStatus.OK);
    }

	//***********************************************************************************************************************
	//******************** SCAN Reference Data Methods *******************************************************************************
	//***********************************************************************************************************************
	@RequestMapping(value="/materialtypes", method = RequestMethod.GET)
	public ResponseEntity<String> loadMaterialTypes() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveMaterialTypes();
		return new ResponseEntity<String>("Successfully loaded Material Types from Scan.", HttpStatus.OK);
	}
	
	@RequestMapping(value="/downcodes", method = RequestMethod.GET)
	public ResponseEntity<String> loadDownCodes() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveDownCodes();
		return new ResponseEntity<String>("Successfully loaded down codes from Scan.", HttpStatus.OK);
	}

	@RequestMapping(value="/subtypes", method = RequestMethod.GET)
	public ResponseEntity<String> loadEquipmentSubTypes() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveSubTypes();
		return new ResponseEntity<String>("Successfully loaded equipment sub types from Scan.", HttpStatus.OK);
	}

	
	@RequestMapping(value="/series", method = RequestMethod.GET)
	public ResponseEntity<String> loadSerieses() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveSeries();
		return new ResponseEntity<String>("Successfully loaded serieses from Scan.", HttpStatus.OK);

	}

	//***********************************************************************************************************************
	//******************** PeopleSoft Reference Data Methods *******************************************************************************
	//***********************************************************************************************************************
	@RequestMapping(value="/locations", method = RequestMethod.GET)
	public ResponseEntity<String> loadLocations() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveLocations();
		return new ResponseEntity<String>("Successfully loaded locations from PeopleSoft.", HttpStatus.OK);
	}    

	@RequestMapping(value="/mdatypes", method = RequestMethod.GET)
	public ResponseEntity<String> loadMdaTypes() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveMdaTypes();
		return new ResponseEntity<String>("Successfully loaded MDA Types from PeopleSoft.", HttpStatus.OK);
	}    
	
	@RequestMapping(value="/sppostypes", method = RequestMethod.GET)
	public ResponseEntity<String> loadSpecialPositionTypes() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveSpecialPositionTypes();
		return new ResponseEntity<String>("Successfully loaded Special Position Types from PeopleSoft.", HttpStatus.OK);
	} 
	
	@RequestMapping(value="/ofpostypes", method = RequestMethod.GET)
	public ResponseEntity<String> loadOfficerPositionTypes() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveOfficerPositionTypes();
		return new ResponseEntity<String>("Successfully loaded Officer Position Types from PeopleSoft.", HttpStatus.OK);
	} 

	@RequestMapping(value="/unavailabilitytypes", method = RequestMethod.GET)
	public ResponseEntity<String> loadUnavailabilityTypes() throws DataLoadException
	{
		adminExecutor.init();
		adminExecutor.getAndSaveUnavailabilityTypes();
		return new ResponseEntity<String>("Successfully loaded unavailability types from PeopleSoft.", HttpStatus.OK);
	}    

	@RequestMapping(value="/getloadstatus", method = RequestMethod.GET)
	public String getLoadStatus()
	{
		return adminExecutor.getLoadStatus();
	}
	
}

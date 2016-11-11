package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentModel;

import java.util.Date;
import java.util.concurrent.ExecutorService;

import net.logstash.logback.encoder.org.apache.commons.lang.exception.ExceptionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminExecutorImpl extends AdminExecutorAbstract implements AdminExecutor
{
//	private static final Logger logger = LoggerFactory.getLogger(AdminExecutorImpl.class);
	
	@Autowired
	private AdminEquipmentExecutor adminEquipmentExecutor;
	
	@Autowired
	private AdminPersonExecutor adminPersonExecutor;
	
	@Autowired
	private AdminBoardExecutor adminBoardExecutor;
    
	//*********************************************************************************************************************
	//**************************************** Initialization Methods *****************************************************
	//*********************************************************************************************************************
    @Override
	public void init()
	{
    	super.init();
    	adminEquipmentExecutor.init();
    	adminPersonExecutor.init();
	}

	//*********************************************************************************************************************
	//**************************************** Scan Methods ***************************************************************
	//*********************************************************************************************************************
    @Override
	public void loadEquipment()  throws  DataLoadException
	{
    	loadEquipment(null);
	}
	
    @Override
	public void loadEquipment(Date asOfDate) throws DataLoadException
	{
    	adminEquipmentExecutor.loadEquipment(asOfDate);
	}

    @Override
	public void loadEquipmentByLocation(String locationCode) throws DataLoadException
	{
		loadEquipmentByLocation(locationCode, new Date());
	}
	
    @Override
	public void loadEquipmentByLocation(String locationCode, Date asOfDate) throws DataLoadException
	{
    	adminEquipmentExecutor.loadEquipmentByLocation(locationCode, asOfDate);
	}
	
    @Override
	public void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode)  throws DataLoadException
	{
		loadEquipmentById(vehicleSeries, vehicleNumber, garageCode, null);
	}

    @Override
	public void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode, Date asOfDate)  throws DataLoadException
	{
    	adminEquipmentExecutor.loadEquipmentById( vehicleSeries,  vehicleNumber,  garageCode,  asOfDate) ; 
	}
	
    @Override
   	public void loadEquipmentFromModel(EquipmentModel equipmentModel)  throws DataLoadException
   	{
    	adminEquipmentExecutor.loadEquipmentFromModel(equipmentModel);
   	}
   	
	//*********************************************************************************************************************
	//**************************************** Peoplesoft Methods *********************************************************
	//*********************************************************************************************************************
    @Override
    public void loadPersonnel() throws DataLoadException
    {
   		loadPersonnel(null);
    }
    
    @Override
    public void loadPersonnel(Date asOfDate) throws DataLoadException
    {
    	adminPersonExecutor.loadPersonnel(asOfDate);
	}

    @Override
    public void loadPersonnelFromLocation(String locationCode) throws DataLoadException
    {
    	loadPersonnelFromLocation(locationCode, null);
    }
    
    
    @Override
    public void loadPersonnelFromLocation (String locationCode, Date asOfDate) throws DataLoadException
    {
    	adminPersonExecutor.loadPersonnelFromLocation ( locationCode,  asOfDate);

    }

    @Override
    public void loadPersonById (String employeeId, String locationCode) throws DataLoadException
    {
    	loadPersonById(employeeId, locationCode, null);
    }

    @Override
    public void loadPersonById (String employeeId, String locationCode, Date asOfDate) throws DataLoadException
    {
    	adminPersonExecutor.loadPersonById ( employeeId,  locationCode,  asOfDate) ;
    }
    
    //*********************************************************************************************************************
    //**************************************** SCAN Reference Data Methods ************************************************
  	//*********************************************************************************************************************

    @Override
	public synchronized void getAndSaveSeries() throws DataLoadException
  	{
    	adminEquipmentExecutor.getAndSaveSeries();
  	}
    
   
    @Override
    public synchronized void getAndSaveMaterialTypes()  throws DataLoadException
    {
    	adminEquipmentExecutor.getAndSaveMaterialTypes();
    }
	
    @Override
    public void getAndSaveDownCodes() throws DataLoadException
    {
    	adminEquipmentExecutor.getAndSaveDownCodes();
    }
    
    @Override
    public void getAndSaveSubTypes() throws DataLoadException
    {
    	adminEquipmentExecutor.getAndSaveSubTypes();
    }

    //*********************************************************************************************************************
    //**************************************** PeopleSoft Reference Data Methods ******************************************
  	//*********************************************************************************************************************
    @Override
    public void getAndSaveLocations() throws DataLoadException
  	{
    	adminPersonExecutor.getAndSaveLocations();
  	}
    
    @Override
   	public void getAndSaveOthereReferenceData() throws DataLoadException
    {
    	adminPersonExecutor.getAndSaveOthereReferenceData();
    }
    
    @Override
   	public void getAndSaveMdaTypes() throws DataLoadException
    {
    	adminPersonExecutor.getAndSaveMdaTypes(null);
    }

    @Override
   	public void getAndSaveSpecialPositionTypes() throws DataLoadException
    {
    	adminPersonExecutor.getAndSaveSpecialPositionTypes(null);
    }
    
    @Override
   	public void getAndSaveOfficerPositionTypes() throws DataLoadException
    {
    	adminPersonExecutor.getAndSaveOfficerPositionTypes(null);
    }
    
    @Override
   	public void getAndSaveUnavailabilityTypes() throws DataLoadException
    {
    	adminPersonExecutor.getAndSaveUnavailabilityTypes();
    }

    // ********************************** Getting executors ******************************************
    
    @Override
	public ExecutorService getEquipmentExecutor() {
		return adminEquipmentExecutor.getEquipmentExecutor();
	}

    @Override
	public ExecutorService getPersonnelExecutor() {
		return adminPersonExecutor.getPersonnelExecutor();
	}

    @Override
	public void sendRefreshCacheCommand(CommandRefreshCaches adminCommand) 
	{
	    super.sendRefreshCacheCommand(adminCommand);
	}
    
    @Override
    public String getLoadStatus()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Status:");
    	if (failed)
    	{
    		sb.append("FAILED;");
    		
    		if (dataLoadExceptions.size() > 0)
        	{
        		dataLoadExceptions.forEach(exception -> sb.append("\n").append("************ Excepton ******************\n").
        				append(ExceptionUtils.getFullStackTrace(exception)).append("\n"));
        	}
    		
    		return sb.toString();
    	}
    	
    	
    	if ( (adminEquipmentExecutor.getCurrentEquipmentLoadPercentage() == 100) && (adminPersonExecutor.getCurrentPersonLoadPercentage() == 100))
    	{
    		sb.append("STOPPED;");
    	}
	    else
	    {
	    	sb.append("RUNNING;");
	    	sb.append("Equipment:").append(String.valueOf(adminEquipmentExecutor.getCurrentEquipmentLoadPercentage())).append(";\n");
	    	sb.append("Personnel:").append(String.valueOf(adminPersonExecutor.getCurrentPersonLoadPercentage())).append(";\n");
	    }
    	
    	return sb.toString();
    }
} 

package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentModel;

import java.util.Date;
import java.util.concurrent.ExecutorService;

public interface AdminExecutor 
{
    //*********************************************************************************************************************
    //**************************************** Init Methods ***************************************************************
  	//*********************************************************************************************************************
	void init();
	
	//*********************************************************************************************************************
	//**************************************** Scan Methods ***************************************************************
	//*********************************************************************************************************************
	void loadEquipment() throws DataLoadException;
	void loadEquipment(Date asOfDate) throws DataLoadException;
	void loadEquipmentByLocation(String locationCode) throws DataLoadException;
	void loadEquipmentByLocation(String locationCode, Date asOfDate) throws DataLoadException;
	void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode) throws DataLoadException;
	void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode, Date asOfDate) throws DataLoadException;
	void loadEquipmentFromModel(EquipmentModel equipmentModel) throws DataLoadException;

	//*********************************************************************************************************************
    //**************************************** PeopleSoft Methods *********************************************************
  	//*********************************************************************************************************************
	void loadPersonnel() throws DataLoadException;
	void loadPersonnel(Date asOfDate) throws DataLoadException;
	void loadPersonnelFromLocation(String locationCode) throws DataLoadException;
	void loadPersonnelFromLocation (String locationCode, Date asOfDate) throws DataLoadException;
	void loadPersonById (String employeeId, String locationCode) throws DataLoadException;
	void loadPersonById (String employeeId, String locationCode, Date asOfDate) throws DataLoadException;


	//*********************************************************************************************************************
    //**************************************** Reference Data Methods *****************************************************
  	//*********************************************************************************************************************
	void getAndSaveSeries() throws DataLoadException;
	void getAndSaveMaterialTypes() throws DataLoadException;
	void getAndSaveDownCodes() throws DataLoadException;
	void getAndSaveSubTypes() throws DataLoadException;

	void getAndSaveLocations() throws DataLoadException;
	void getAndSaveOthereReferenceData() throws DataLoadException;
	void getAndSaveMdaTypes() throws DataLoadException;
	void getAndSaveSpecialPositionTypes() throws DataLoadException;
	void getAndSaveOfficerPositionTypes() throws DataLoadException;
   	void getAndSaveUnavailabilityTypes() throws DataLoadException;
   
   	
	//*********************************************************************************************************************
    //**************************************** Execution percentages  *****************************************************
  	//*********************************************************************************************************************
   	String getLoadStatus();
   	void sendRefreshCacheCommand(CommandRefreshCaches adminCommand);
  	
   	//**********************************************************************************************************************
    ExecutorService getEquipmentExecutor();
    ExecutorService getPersonnelExecutor();
}

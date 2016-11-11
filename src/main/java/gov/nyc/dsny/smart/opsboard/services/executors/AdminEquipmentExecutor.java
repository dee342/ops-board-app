package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentModel;

import java.util.Date;
import java.util.concurrent.ExecutorService;

public interface AdminEquipmentExecutor 
{
	void init();
	
	void loadEquipment() throws DataLoadException;
	void loadEquipment(Date asOfDate) throws DataLoadException;
	void loadEquipmentByLocation(String locationCode) throws DataLoadException;
	void loadEquipmentByLocation(String locationCode, Date asOfDate) throws DataLoadException;
	void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode) throws DataLoadException;
	void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode, Date asOfDate) throws DataLoadException;
	void loadEquipmentFromModel(EquipmentModel equipmentModel) throws DataLoadException;


	//*********************************************************************************************************************
    //**************************************** Reference Data Methods *****************************************************
  	//*********************************************************************************************************************
	void getAndSaveSeries() throws DataLoadException;
	void getAndSaveMaterialTypes() throws DataLoadException;
	void getAndSaveDownCodes() throws DataLoadException;
	void getAndSaveSubTypes() throws DataLoadException;

   	//**********************************************************************************************************************
    ExecutorService getEquipmentExecutor();
    float getCurrentEquipmentLoadPercentage();
}

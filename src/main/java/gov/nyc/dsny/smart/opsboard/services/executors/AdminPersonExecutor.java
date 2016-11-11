package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.integration.models.ps.PersonnelOtherReferenceDataModel;

import java.util.Date;
import java.util.concurrent.ExecutorService;

public interface AdminPersonExecutor 
{
	void init();
	
	void loadPersonnel() throws DataLoadException;
	void loadPersonnel(Date asOfDate) throws DataLoadException;
	void loadPersonnelFromLocation(String locationCode) throws DataLoadException;
	void loadPersonnelFromLocation (String locationCode, Date asOfDate) throws DataLoadException;
	void loadPersonById (String employeeId, String locationCode) throws DataLoadException;
	void loadPersonById (String employeeId, String locationCode, Date asOfDate) throws DataLoadException;


	//*********************************************************************************************************************
    //**************************************** Reference Data Methods *****************************************************
  	//*********************************************************************************************************************
	void getAndSaveLocations() throws DataLoadException;
	void getAndSaveOthereReferenceData() throws DataLoadException;
	void getAndSaveMdaTypes(PersonnelOtherReferenceDataModel personnelOtherReferenceDataModel ) throws DataLoadException;
	void getAndSaveSpecialPositionTypes(PersonnelOtherReferenceDataModel personnelOtherReferenceDataModel ) throws DataLoadException;
	void getAndSaveOfficerPositionTypes(PersonnelOtherReferenceDataModel personnelOtherReferenceDataModel ) throws DataLoadException;
	void getAndSaveUnavailabilityTypes() throws DataLoadException;
   
   	//**********************************************************************************************************************
    ExecutorService getPersonnelExecutor();
    float getCurrentPersonLoadPercentage();
}

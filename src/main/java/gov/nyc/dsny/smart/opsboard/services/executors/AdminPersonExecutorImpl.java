package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.SubcategoryCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.CategoryCacheService;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.domain.personnel.GroundingStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatus;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPosition;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReason;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.MdaType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.OfficerPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.SpecialPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.UnavailabilityType;
import gov.nyc.dsny.smart.opsboard.domain.reference.BoardType;
import gov.nyc.dsny.smart.opsboard.domain.reference.DataLoadFilter;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.LocationType;
import gov.nyc.dsny.smart.opsboard.domain.reference.LocationTypeIntegration;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit;
import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.integration.mapper.PersonEntityMapper;
import gov.nyc.dsny.smart.opsboard.integration.models.ps.PersonModel;
import gov.nyc.dsny.smart.opsboard.integration.models.ps.PersonnelOtherReferenceDataModel;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.BoardTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.DataLoadFilterRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.LocationRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.LocationTypeIntegrationRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.LocationTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.MdaTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.OfficerPositionTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SpecialPositionTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.UnavailabilityTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.WorkUnitRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.personnel.PersonnelPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

@Service
public class AdminPersonExecutorImpl  extends AdminExecutorAbstract implements AdminPersonExecutor
{
	private static final Logger logger = LoggerFactory.getLogger(AdminPersonExecutorImpl.class);
	
	private static final int PERSONNEL_THREAD_POOL_SIZE = 10;
	
	private static final int PERSON_HISTORY_RECORDS = 10;

	private static final String LOAD_PERSONNEL_FROM_LOCATION = "Error while loading personnel data by location : {0} from People soft.";
	private static final String LOAD_PERSONNEL_FROM_LOCATION_ASOFDATE = "Error while loading person {0} from PeopleSoft.";
	
	private ExecutorService personnelExecutor;
	private Set<String> avoidPersonnelLoadLocationsList = new HashSet<String>();
	
	private float currentPersonLoadPercentage;

	@Autowired
    private PersonEntityMapper personMapper;
    
    @Autowired
    protected PersonnelPersistenceService persistenceService;
			
	@Autowired
	private LocationRepository locationRepository;

	@Autowired
	private WorkUnitRepository workUnitRepository;
		
	@Autowired
	private MdaTypeRepository mdaTypeRepository;
	
	@Autowired
	private SpecialPositionTypeRepository specialPositionTypeRepository;
	
	@Autowired
	private OfficerPositionTypeRepository officerPositionTypeRepository;
	
	@Autowired
	private UnavailabilityTypeRepository unavailabilityTypeRepository;
	
	@Autowired
	private LocationTypeRepository locationTypeRepository;
	
	@Autowired
	private LocationTypeIntegrationRepository locationTypeIntegrationRepository;
	
	@Autowired
	private BoardTypeRepository boardTypeRepository;
	

	@Autowired
	private DataLoadFilterRepository dataLoadFilterRepository;
	
	@Autowired
    private LocationCache locationCache;
	
	@Autowired
    private BoardTypeCache boardTypeCache;
	
	@Autowired
	private CategoryCacheService categoryCacheService;
	
	@Autowired
    private SubcategoryCache subcategoryCache;
	
	//*********************************************************************************************************************
	//**************************************** Initialization Methods *****************************************************
	//*********************************************************************************************************************
    @Override
	public void init()
	{
    	super.init();
    	personnelExecutor = Executors.newFixedThreadPool(PERSONNEL_THREAD_POOL_SIZE);
    	avoidPersonnelLoadLocationsList =  getAvoidPersonnelLoadLocations(); 
		currentPersonLoadPercentage = 0;
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
    	if (avoidPersonnelLoadLocationsList.size() == 0)
    		throw new DataLoadException(ErrorMessage.PS_SSERV_LOCATION_CHILDREN);

    	
    	try
    	{
			List<Location> locationList = locationCache.getLocations(new Date());
			int totalLocSize = locationList.size();
			float incrementalPercentage = 90f / totalLocSize;
			for (Location location : locationList)
			{
				loadPersonnelFromLocation (location.getCode(), null);
				currentPersonLoadPercentage+=incrementalPercentage;
				sendLoadPercentageMessageToUI (currentPersonLoadPercentage);
			}
    	}
    	catch (Throwable t)
    	{
    		handleLoadException (ErrorMessage.PS_LOAD_PERSONNEL, t, true);
    	}
		finally
		{
			shutDownPersonnelExecutor();
			currentPersonLoadPercentage = 100;
			logger.info("*** LoadingInfo ***:  Finished loading personnel from PeopleSoft ");
		}
	}

    @Override
    public void loadPersonnelFromLocation(String locationCode) throws DataLoadException
    {
    	loadPersonnelFromLocation(locationCode, null);
    }
    
    
    @Override
    public void loadPersonnelFromLocation (String locationCode, Date asOfDate) throws DataLoadException
    {
    	if (avoidPersonnelLoadLocationsList.size() == 0)
    		throw new DataLoadException(ErrorMessage.PS_SSERV_LOCATION_CHILDREN);

    	
    	if (avoidPersonnelLoadLocationsList.contains(locationCode))
    	{
    		logger.info("*** LoadingInfo ***:  Skipping loading personnel from {} location", locationCode);
    		return;
    	}
    	
    	logger.info("*** LoadingInfo ***:  Loading personnel from {} location", locationCode);
    	try
    	{
    		 List<String> ids = integrationFacade.getPersonnelIDsByLocation(locationCode);
    	     if(CollectionUtils.isNotEmpty(ids))
    	     {
    	    	 ids.forEach(id -> loadPersonById(id, locationCode, asOfDate));      
    	     }
    	}
    	catch(Throwable t)
    	{
    		if(asOfDate != null)
    		{
    			sendLoadingErrorMessageToUI(getFormattedMessage(LOAD_PERSONNEL_FROM_LOCATION_ASOFDATE, new Object[]{locationCode, asOfDate}));
    		}
    		else
    		{
    			sendLoadingErrorMessageToUI(getFormattedMessage(LOAD_PERSONNEL_FROM_LOCATION, new Object[]{locationCode}));    			
    		}
			handleLoadException (ErrorMessage.PS_LOAD_PERSONNEL, t, false, "for location code " + locationCode);
    	}
    }

    @Override
    public void loadPersonById (String employeeId, String locationCode) 
    {
    	loadPersonById(employeeId, locationCode, null);
    }

    @Override
    public void loadPersonById (String employeeId, String locationCode, Date asOfDate) 
    {
    	PersonThread personThread = new PersonThread (employeeId, locationCode, asOfDate);
    	personnelExecutor.execute(personThread);
    }
    
    //*********************************************************************************************************************
    //**************************************** PeopleSoft Reference Data Methods ******************************************
  	//*********************************************************************************************************************
    @Override
    public void getAndSaveLocations() throws DataLoadException
    {
    	if (executeGetAndSaveLocations())
    	{
    		try
    		{
    			refreshLocationChangeCaches();
    		}
    		catch(Throwable t)
    		{
        		handleLoadException (ErrorMessage.PS_LOAD_LOCATIONS, t, true);
            }
    	}
    }
    
    @Transactional
    public boolean executeGetAndSaveLocations() throws DataLoadException
  	{
    	try
    	{
    		boolean haveBoardTypesChanged = getAndSaveBoardTypes();
    		
	  		// Process locations and work units second
	  		locationCache.getLocations(new Date());
	  		List<Location> locationsFromPS = integrationFacade.getLocations();
	  		List<Location> locationsFromOB = locationRepository.findByDate(new Date());
	  		
	  		List<WorkUnit> workUnitsFromPS = integrationFacade.getWorkUnits();
	  		List<WorkUnit> workUnitsFromOB = workUnitRepository.findByDate(new Date());

	  		if (haveLocationsChanged(locationsFromPS, locationsFromOB) || 
	  			haveWorkUnitsChanged(workUnitsFromPS, workUnitsFromOB) ||
	  			haveBoardTypesChanged)
	  		{
	  			Date startDate1 = null;
	  			Date endDate1 = null;
	  			Date startDate2 = null;
	  			Date endDate2 = null;
	  			if (!ListUtils.isEmpty(locationsFromOB))
	  			{
	  				// Get all terminated locations
	  				List<Long> terminatedLocationIds = new ArrayList<Long>();
	  				List<String> terminatedSections = new ArrayList<String>();
	  				getAllTerminatedLocationsAndSections(locationsFromOB, locationsFromPS, terminatedLocationIds, terminatedSections);

	  				if (terminatedLocationIds.size() > 0 || terminatedSections.size() > 0)
	  				{
	  					String activeAssignmentsText = areTerminatedLocationsStillActiveInOB(terminatedLocationIds, terminatedSections);
	  					if (activeAssignmentsText != null)
	  					{
	  						handleLoadException (ErrorMessage.PS_LOAD_TERMINATED_LOCATION, null, true, activeAssignmentsText);
	  					}
	  				}
	  				
		  			// Make current data none effective
		  			locationsFromOB.forEach(location -> 
		  			{
		  				location.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
		  				if (location.getHistoryLinkId() == null)
		  					location.setHistoryLinkId(historyLinkId++);
		  				
		  			});
		  			workUnitsFromOB.forEach(workUnit -> 
		  			{
		  				workUnit.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
		  				if (workUnit.getHistoryLinkId() == null)
		  					workUnit.setHistoryLinkId(historyLinkId++);
		  			});
		  			locationsFromOB = locationRepository.save(locationsFromOB);
		  			workUnitsFromOB = workUnitRepository.save(workUnitsFromOB);
		  			
		  			startDate1 = locationsFromOB.get(0).getEffectiveStartDate();
	  				endDate1 = locationsFromOB.get(0).getEffectiveEndDate();
	  				
		  			logger.info("*** LoadingInfo ***:  Updated locations and work units effective end dates to {}", DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
	  			}
	  			
	  			// Save new effective locations and work units
		  		List<Location> newLocationsFromOB = saveNewEffectiveLocations(locationsFromPS, locationsFromOB);
	  			saveNewEffectiveWorkUnits(workUnitsFromPS, workUnitsFromOB, newLocationsFromOB);
	  			startDate2 = newLocationsFromOB.get(0).getEffectiveStartDate();
	  			endDate2 = newLocationsFromOB.get(0).getEffectiveEndDate();
	  			if (startDate1 != null)
	  				updateLocations(startDate1, endDate1, startDate2, endDate2);
	  			
	  			avoidPersonnelLoadLocationsList =  getAvoidPersonnelLoadLocations();
	  			
	  			return true;
	  			
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  Locations and their dependencies haven't changed  ");
	  			return false;
	  		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Locations data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_LOCATIONS, t, true);
        }
    	
    	return false;
  	}
    
    private boolean getAndSaveBoardTypes() throws DataLoadException
  	{
    	boolean shouldSave = false;
    	Date startDate1 = null;
		Date endDate1 = null;
		List<BoardType> newBoardTypes = null;
  		List<Long> deletedBoardTypeIDs = new ArrayList<Long>();
    	try
    	{
       		// Process board types first
       		List<BoardType> boardTypesFromPS = integrationFacade.getBoardTypes();
       		Map<String, BoardType> psBoardTypesMap = new HashMap<String, BoardType>();
       		boardTypesFromPS.forEach(psBoardType -> psBoardTypesMap.put(psBoardType.getCode(), psBoardType));
       		
       		
       		List<BoardType> boardTypesFromOB = boardTypeRepository.findByDate(new Date());
   	  		final Map<String, BoardType> obBoardTypesMap = new HashMap<String, BoardType>();
   	  		int currentSize = boardTypesFromOB.size();
   	    	if (currentSize > 0)
   	    	{
   	    		boardTypesFromOB.forEach(obBoardType -> obBoardTypesMap.put(obBoardType.getCode(), obBoardType));
   	    	}
   	  		
   	  		// Check for updates from scan
	  		if (currentSize > 0)
	  		{
		  		for (BoardType psBoardType : boardTypesFromPS)
		  		{
		  			BoardType obBoardType = obBoardTypesMap.get(psBoardType.getCode());
		  			if (psBoardType.compareTo(obBoardType) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
	  		
	  			for (BoardType obBoardType  : boardTypesFromOB)
		  		{
	  				BoardType psBoardType = psBoardTypesMap.get(obBoardType.getCode());
	  				if (psBoardType == null)
	  				{
	  					deletedBoardTypeIDs.add(obBoardType.getId());
	  					shouldSave = true;
	  				}
	  				else if (obBoardType.compareTo(psBoardType) != 0)
		  			{
		  				shouldSave = true;
		  			}
		  		}
	  		}
	  		else
	  		{
	  			shouldSave = true;
	  		}
   	  		
	  		if (shouldSave)
	  		{
	  			List<BoardType> oldBoardTypes = null;
	  			if (currentSize > 0)
	  			{
	  				boardTypesFromOB.forEach(boardType -> 
	  				{ 
	  					boardType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
			  			if (boardType.getHistoryLinkId() == null)
			  				boardType.setHistoryLinkId(historyLinkId++);
	  				});
	  				oldBoardTypes = boardTypeRepository.save(boardTypesFromOB);
	  				startDate1 = oldBoardTypes.get(0).getEffectiveStartDate();
		  			endDate1 = oldBoardTypes.get(0).getEffectiveEndDate();
		  			
		  			obBoardTypesMap.clear();
		  			oldBoardTypes.forEach(obBoardType -> obBoardTypesMap.put(obBoardType.getCode(), obBoardType));
	  			}
	   	    	
	  			
		  		Date newEffectiveStartDate = getNewEffectiveStartDate(boardTypesFromOB);
	  			boardTypesFromPS.forEach(psBoardType -> {
	  				psBoardType.setEffectiveStartDate(newEffectiveStartDate);
	  				
	  				BoardType previousBoardType = psBoardType.getPreviousBoardType();
	  				if (previousBoardType == null)
	  				{
	  					previousBoardType = obBoardTypesMap.get(psBoardType.getCode());
	  					psBoardType.setPreviousBoardType(previousBoardType);
	  				}
	  				
	  				if (previousBoardType == null)
	  				{
	  					psBoardType.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					psBoardType.setHistoryLinkId(previousBoardType.getHistoryLinkId());
	  				}
	  			});
	  			newBoardTypes = boardTypeRepository.save(boardTypesFromPS);
	  		
   	  			logger.info("*** LoadingInfo ***:  Created new effective board types ");
   	  			
   		    	boardTypeCache.refresh();
   	  		}
   	  		else
   	  		{
   	  			logger.info("*** LoadingInfo ***:  Board types haven't changed ");
   	  			return shouldSave;
    	  	}
    	  	
	  		// Recreate location types for new board types
	  		//vg - no longer needed - dates are being removed
  	    	/*List<LocationType> currentLocationTypes = locationTypeRepository.findByDate(new Date());
  	    	if (!ListUtils.isEmpty(currentLocationTypes))
  	    	{
  	    		currentLocationTypes.parallelStream().forEach(locationType -> locationType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday())));
  	    		locationTypeRepository.save(currentLocationTypes);
  	    	}*/
  	    		  		
  	    	// Create new effective location types
	    	Set<LocationType> locationTypesFromDb = new HashSet<LocationType>(locationTypeRepository.findAll()); 	
	    	List<LocationType> locationTypes = new ArrayList<>();
	    	List<LocationTypeIntegration> ltiList = locationTypeIntegrationRepository.findByDate(new Date());
	    	
	    	if(ltiList.isEmpty())
	    		logger.info("*** LoadingInfo ***: No location types metadata was found ");
	    	
	    	
	    	for (LocationTypeIntegration locationTypeIntegration : ltiList)
	    	{	    			    		
	    		BoardType boardType = boardTypeCache.getBoardTypeByDescription(locationTypeIntegration.getBoardTypeDescription(), new Date());
	    		if (boardType == null)
	    			continue;
  		    		
	    		Subcategory subcategory = subcategoryCache.getSubCategoryByID(locationTypeIntegration.getSubcategoryId());
	    		if (subcategory == null)
	    			continue;
  		    		
	    		LocationType locationType = new LocationType();
	    		locationType.setBoardType(boardType);
	    		locationType.setSubcategory(subcategory);
	    		
	    		if(!locationTypesFromDb.contains(locationType))
	    			locationTypes.add(locationType);
	    	}
  		    	
	    	if (locationTypes.size() > 0)
	    	{
	    		locationTypeRepository.save(locationTypes);
	    		logger.info("*** LoadingInfo ***:  Created new Location Types ");
	    	}
	    	else
	    	{
	    		logger.info("*** LoadingInfo ***: Nothing inserted - no new metadata in locationTypeIntegration table.");
	    	}
	    	
	    	categoryCacheService.refresh();

			if (deletedBoardTypeIDs.size() > 0)
			{
				String activeAssignmentsText = areTerminatedBoardTypesActiveInOB(deletedBoardTypeIDs);
				if (activeAssignmentsText != null)
				{
					handleLoadException (ErrorMessage.PS_LOAD_TERMINATED_BOARD_TYPES, null, true, activeAssignmentsText);
				}
	  		}
			
	    	if (startDate1 != null)
  			{
  				Date startDate2 = newBoardTypes.get(0).getEffectiveStartDate();
	  			Date endDate2 = newBoardTypes.get(0).getEffectiveEndDate();
	  			updateBoardTypes(startDate1, endDate1, startDate2, endDate2);
  			}
	    	
    	}
	  	catch(Throwable t)
	  	{
	  		sendLoadingErrorMessageToUI("Board Types data load from PeopleSoft has failed");
	  	   	handleLoadException (ErrorMessage.PS_LOAD_BOARD_TYPES, t, true);
		}
    	return shouldSave;
  	}
    
    private String areTerminatedBoardTypesActiveInOB(List<Long> terminatedBoardTypeIds) throws Exception
    {
    	Long spId =  0L; //refDataUpdatesStatusRepository.checkTerminatedBoardTypes(terminatedBoardTypeIds);
    	return getStoredProcedureErrorText (spId);
    }
    
    private String updateBoardTypes(Date startDate1, Date endDate1, Date startDate2, Date endDate2) throws Exception
    {
    	Long spId =  0L; //refDataUpdatesStatusRepository.updateBoardTypes (startDate1, endDate1, startDate2, endDate2);
    	return getStoredProcedureErrorText (spId);
    }
    
    private Set<String> getAvoidPersonnelLoadLocations() 
    {
    	Set<String> avoidLocations = new HashSet<String>();
    
    	try
    	{
    		Set<DataLoadFilter> filters = dataLoadFilterRepository.findByDiscriminator("location_tree");
    		List<Location> allLocations = locationCache.getLocations(new Date());
    		for (DataLoadFilter filter : filters)
    		{
    			Location rootLocation = locationCache.getLocation(filter.getCode(), new Date());
    			if (rootLocation != null)
    			{
    				avoidLocations.add(rootLocation.getCode());
    				getChildLocations(rootLocation, allLocations).forEach(location -> avoidLocations.add(location.getCode()));
    			}
    		}
    	}
    	catch (Exception e)
    	{
    		//integrationErrorHandlingService.handleDataLoadException (ErrorMessage.PS_SSERV_LOCATION_CHILDREN, e);
    		return null;
    	}
    	return avoidLocations;
    }
    
    private List<Location> getChildLocations(Location mainLocation, List<Location> allLocations) throws OpsBoardError
    {
    	List<Location> childLocations = new ArrayList<Location>();
    	List<Location> grandChildLocations = new ArrayList<Location>();
    	
    	if (mainLocation == null)
    		return childLocations;
    	
    	childLocations = allLocations.stream().filter(location -> mainLocation.equals(location.getBorough())).collect(Collectors.toList());
    	
    	for (Location location : childLocations)
    	{
    		grandChildLocations.addAll(getChildLocations(location, allLocations));
    	}
    	childLocations.addAll(grandChildLocations);
    	
    	return childLocations;
    }

	private void refreshLocationChangeCaches() throws Exception
	{
		CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.BOARD_TYPE_CACHE);
		this.sendRefreshCacheCommand(adminCommand);
		
		CommandRefreshCaches adminCommand3 = new CommandRefreshCaches(CommandRefreshCaches.CATEGORY_CACHE); 
		this.sendRefreshCacheCommand(adminCommand3);
		
		CommandRefreshCaches adminCommand1 = new CommandRefreshCaches(CommandRefreshCaches.LOCATION_CACHE);
		this.sendRefreshCacheCommand(adminCommand1);

		CommandRefreshCaches adminCommand2 = new CommandRefreshCaches(CommandRefreshCaches.BOARD_CACHES);
		this.sendRefreshCacheCommand(adminCommand2);
	}
    
    private void getAllTerminatedLocationsAndSections(List<Location>locationsFromOB, List<Location>locationsFromPS,
    		List<Long> terminatedLocationIds, List<String> terminatedSections)
    {
    	Map<String, Location> psLocationMap = locationsFromPS.parallelStream().collect(Collectors.toMap(location -> location.getCode(), location -> location));
    	
    	for (Location obLocation : locationsFromOB)
    	{
    		Location psLocation = psLocationMap.get(obLocation.getCode());
    		if (psLocation == null)
    		{
    			terminatedLocationIds.add(obLocation.getId());
    		}
    		else
    		{
    			Set<String> psSections = psLocation.getSectionIds();
    			Set<String> obSections = obLocation.getSectionIds();
    			for (String obSection : obSections)
    			{
    				if (!psSections.contains(obSection))
    				{
    					terminatedSections.add(obLocation.getCode() + obSection);
    				}
    			}
    		}
    	}
    }
    
    private String areTerminatedLocationsStillActiveInOB(List<Long> terminatedLocationIds, List<String> terminatedSections) throws Exception
    {
    	Long spId =  0L; //refDataUpdatesStatusRepository.checkTerminatedLocations(terminatedLocationIds, terminatedSections);
    	return getStoredProcedureErrorText (spId);
    }
    
    private String updateLocations(Date startDate1, Date endDate1, Date startDate2, Date endDate2) throws Exception
    {
    	/* TODO - call stored procedure to replace old location IDs with the new ones on all related entities as follows:
	    	1.To & From locations in all future personnel detachments where either start or end dates are today or in the future
	    	2.To & From locations in all pending equipment detachments
	    	3.Home locations for all personnel records
	    	4.To, From & Owner locations for all equipment records
	    	5.Home location for all current and future board_personnel records
	    	6.To, From & Owner locations for all current and future board_equipment records
	    	7.Locations on all future boards BoardDomains
	    	8.Locations on all future TaskContainers
	    	9.Traverse the whole tree of TaskContainer to SectionTasks and delete sections if necessary
    	*/
    	
    	Long spId = 0L; //refDataUpdatesStatusRepository.updateLocations(startDate1, endDate1, startDate2, endDate2);
    	return getStoredProcedureErrorText (spId);
    }
    
    private boolean haveLocationsChanged(List<Location> locationsFromPS, List<Location> locationsFromOB)
    {
    	if (ListUtils.isEmpty(locationsFromOB))
    		return true;
    	
    	if (locationsFromPS.size() != locationsFromOB.size())
    		return true;
    	
    	Map<String, Location> obLocationsMap = new HashMap<String, Location>();
    	locationsFromOB.forEach(obLocation -> obLocationsMap.put(obLocation.getCode(), obLocation));
    	for (Location psLocation : locationsFromPS)
    	{
    		try
    		{
    			Location obLocation = obLocationsMap.get(psLocation.getCode());
    			if (psLocation.compareTo(obLocation) != 0)
    				return true;
    		}
    		catch (Exception e)
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    private boolean haveWorkUnitsChanged(List<WorkUnit> workUnitsFromPS, List<WorkUnit> workUnitsFromOB)
    {
    	if (ListUtils.isEmpty(workUnitsFromOB))
    		return true;
    	
    	Map<String, WorkUnit> obWorkUnitsMap = new HashMap<String, WorkUnit>();
    	workUnitsFromOB.forEach(obWorkUnit -> obWorkUnitsMap.put(obWorkUnit.getCode(), obWorkUnit));
    	
    	if (workUnitsFromPS.size() != workUnitsFromOB.size())
    		return true;
    	
    	for (WorkUnit psWorkUnit : workUnitsFromPS)
    	{
   			WorkUnit obWorkUnit = obWorkUnitsMap.get(psWorkUnit.getCode());
   			if (psWorkUnit.compareTo(obWorkUnit) != 0)
   				return true;
    	}
    	
    	return false;

    }
    
    private List<Location> saveNewEffectiveLocations(List<Location> locationsFromPS, List<Location> oldLocationsFromOB) throws DataLoadException
    {
    	List<Location> locationsFromOB = null;
    	Date newEffectiveStartDate = getNewEffectiveStartDate(oldLocationsFromOB);
    	try
    	{
    		Map<String, Location> oldLocationsFromOBMap = new HashMap<String, Location>();
    		oldLocationsFromOB.forEach(obLocation -> oldLocationsFromOBMap.put(obLocation.getCode(), obLocation));
    		
    		// Save locations without dependencies - first step
	  		Map<String, List<Location>> parents = new HashMap<String, List<Location>>();
	  		Map<String, List<Location>> children = new HashMap<String, List<Location>>();
	  		Map<String, Location> boroughs = new HashMap<String, Location>();
	  		for (Location psLocation : locationsFromPS)
	  		{
	  			parents.put(psLocation.getCode(), new ArrayList(psLocation.getServiceParents()));
	  			children.put(psLocation.getCode(), new ArrayList(psLocation.getServiceLocations()));
	  			boroughs.put(psLocation.getCode(), psLocation.getBorough());
	  			psLocation.setServiceParents(null);
	  			psLocation.setServiceLocations(null);
	  			psLocation.setBorough(null);
  				psLocation.setEffectiveStartDate(newEffectiveStartDate);
  				
  				Location previousLocation = psLocation.getPreviousLocation();
  				if (previousLocation == null)
  				{
  					previousLocation = oldLocationsFromOBMap.get(psLocation.getCode());
  					psLocation.setPreviousLocation(previousLocation);
  				}
  				
  				if (previousLocation == null)
  				{
  					psLocation.setHistoryLinkId(historyLinkId++ );
  				}
  				else
  				{
  					Location oldLocationWithHistoryLink = oldLocationsFromOBMap.get(previousLocation.getCode());
  					psLocation.setHistoryLinkId(oldLocationWithHistoryLink.getHistoryLinkId());
  				}
	  		}
	  		
	  		locationsFromOB = locationRepository.save(locationsFromPS);
	  		
	  		// Save locations with dependencies  - second step
	  		locationsFromOB = locationRepository.findByDate(new Date());
	  		for (Location obLocation : locationsFromOB)
	  		{
	  			populateServiceLocationsWithIds(obLocation, locationsFromOB, new HashSet(children.get(obLocation.getCode())));
	  			
	  			Location borough = boroughs.get(obLocation.getCode());
	  			if (borough != null)
	  			{
	  				if (!StringUtils.equals(obLocation.getCode(), borough.getBoroughCode()))
	  					obLocation.setBorough(borough);
	  			}
	  		}
	  		
	  		locationRepository.save(locationsFromOB);
//	  		for (Location location : locationsFromOB)
//	  			locationRepository.saveAndFlush(location);
	  		logger.info("*** LoadingInfo ***:  Created new effective locations ");
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Locations data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_LOCATIONS, t, true);
        }
    	return locationsFromOB;
  	}
    
    private void populateServiceLocationsWithIds(Location mainLocation, List<Location> newLocationsWithId, Set<Location> serviceLocations)
    {

    	if (serviceLocations != null && serviceLocations.size() > 0)
    	{
	    	for (Location location : serviceLocations)
			{
				try
				{
					Location dbLocation = null;
					for (Location newLocation : newLocationsWithId)
					{
						if (newLocation.getCode().equals(location.getCode()))
						{
							dbLocation = newLocation;
							break;
						}
					}
					
					if (dbLocation != null)
					{
						if (StringUtils.equals(mainLocation.getCode(), dbLocation.getCode()))
						{
							mainLocation.setServicesItself(true);
							serviceLocations.remove(dbLocation);
						}
					}
				}
				catch (Throwable t)
				{
					sendLoadingErrorMessageToUI("Locations data load from PeopleSoft has failed");
					integrationErrorHandlingService.handleDataLoadException (ErrorMessage.PS_LOAD_LOCATIONS, t);
				}
			}
	    	mainLocation.setServiceLocations(serviceLocations);
    	}
    	return ;
    }
    
    private void saveNewEffectiveWorkUnits(List<WorkUnit> workUnitsFromPS, List<WorkUnit> oldWorkUnitsFromOB, List<Location> locationsFromOB) throws DataLoadException
    {
    	Date newEffectiveStartDate = locationsFromOB.get(0).getEffectiveStartDate();
    	
    	try
    	{
    		Map <String, WorkUnit> oldWorkUnitsFromOBMap = new HashMap<String, WorkUnit>();
    		oldWorkUnitsFromOB.forEach(oldWorkUnitFromOB -> oldWorkUnitsFromOBMap.put(oldWorkUnitFromOB.getCode(), oldWorkUnitFromOB));
    		
    		Map <String, Location> obLocationCodeMap = new HashMap<String, Location>();
    		locationsFromOB.forEach(obLocation -> obLocationCodeMap.put(obLocation.getCode(), obLocation));
    		
    		// Set sort sequences
	  		for (WorkUnit psWorkUnit : workUnitsFromPS)
	  		{
	  			Location obWorkUnitLocation = obLocationCodeMap.get(psWorkUnit.getCode());
	  			obWorkUnitLocation.setSortSequence(psWorkUnit.getSortSequence());
	  			
	  			Set<Location> obWorkUnitLocations = new HashSet<Location>();
	  			Set<Location> psLocations = psWorkUnit.getLocations();
	  			for (Location psLocation : psLocations)
	  			{
	  				Location obWorkUnitSubLocation = obLocationCodeMap.get(psLocation.getCode());
	  				if (obWorkUnitSubLocation == null)
	  					logger.error("Location {} has not been loaded with getLocations() call", psLocation.getCode());
	  				obWorkUnitLocations.add(obWorkUnitSubLocation);
	  				obWorkUnitSubLocation.setSortSequence(psLocation.getSortSequence());
	  			}
	  			psWorkUnit.setLocations(obWorkUnitLocations);
	  			psWorkUnit.setEffectiveStartDate(newEffectiveStartDate);
	  			
	  			WorkUnit previousWorkUnit = psWorkUnit.getPreviousWorkUnit();
  				if (previousWorkUnit == null)
  				{
  					previousWorkUnit = oldWorkUnitsFromOBMap.get(psWorkUnit.getCode());
  					psWorkUnit.setPreviousWorkUnit(previousWorkUnit);
  				}

  				if (previousWorkUnit == null)
  				{
  					psWorkUnit.setHistoryLinkId(historyLinkId++ );
  				}
  				else
  				{
  					psWorkUnit.setHistoryLinkId(previousWorkUnit.getHistoryLinkId() );
  				}
	  		}
	  		
  			workUnitRepository.save(workUnitsFromPS);
  			logger.info("*** LoadingInfo ***:  Created new effective work units ");
  			
	  		locationRepository.save(locationsFromOB);
  			logger.info("*** LoadingInfo ***:  Created new effective Locations sort sequences ");
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Work Units data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_WORK_UNITS, t, true);
        }
  	}
    
    
    @Override
   	public void getAndSaveOthereReferenceData() throws DataLoadException
    {
       	try
       	{
       		PersonnelOtherReferenceDataModel personnelOtherReferenceDataModel = integrationFacade.getOtherReferenceData();
       		getAndSaveMdaTypes(personnelOtherReferenceDataModel );
       		getAndSaveSpecialPositionTypes(personnelOtherReferenceDataModel );
       		getAndSaveOfficerPositionTypes(personnelOtherReferenceDataModel );
       	}
       	catch(Throwable t)
		{
       		sendLoadingErrorMessageToUI("Other reference data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_OTHER_REF_DATA, t, true);
        }
    }
    
    @Override
    @Transactional
   	public void getAndSaveMdaTypes(PersonnelOtherReferenceDataModel personnelOtherReferenceDataModel ) throws DataLoadException
    {
    	try
    	{
    		if (personnelOtherReferenceDataModel == null)
    			personnelOtherReferenceDataModel = integrationFacade.getOtherReferenceData();
    		
    		Map<String, MdaType> psMdaTypesMap = new HashMap<String, MdaType>();    		
    		List<MdaType> psMdaTypes = personnelOtherReferenceDataModel.getMdaTypes();
    		psMdaTypes.forEach(mdaType -> psMdaTypesMap.put(mdaType.getCode(), mdaType));
    		
    		List<MdaType> obMdaTypes = mdaTypeRepository.findByDate(new Date());
    		Map<String, MdaType> obMdaTypesMap = new HashMap<String, MdaType>();
    		int currentSize = obMdaTypesMap.size();
    		if (currentSize > 0)
    			obMdaTypes.forEach(mdaType -> obMdaTypesMap.put(mdaType.getCode(), mdaType));
	  		
	  		
	  		// Check for updates from scan
	  		boolean shouldSave = false;
	  		if (currentSize > 0)
	  		{
		  		for (MdaType psMdaType : psMdaTypes)
		  		{
		  			MdaType obMdaType = obMdaTypesMap.get(psMdaType.getCode());
		  			if (psMdaType.compareTo(obMdaType) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
		  		
	  			for (MdaType obMdaType  : obMdaTypes)
		  		{
	  				MdaType psMdaType = psMdaTypesMap.get(obMdaType.getCode());
  				
	  				if (psMdaType == null)
	  				{
	  					shouldSave = true;
	  				}
	  				else if (obMdaType.compareTo(psMdaType) != 0)
		  			{
		  				shouldSave = true;
		  			}
		  		}
	  		}
	  		else
	  		{
	  			shouldSave = true;
	  		}
	  		
	  		if (shouldSave)
	  		{
	  			if (currentSize > 0)
	  			{
	  				obMdaTypes.forEach(obMdaType -> 
	  				{
	  					obMdaType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
	  					if (obMdaType.getHistoryLinkId() == null)
	  						obMdaType.setHistoryLinkId(historyLinkId++);
	  				});
	  				List<MdaType> oldMdaTypes = mdaTypeRepository.save(obMdaTypes);

	  				obMdaTypesMap.clear();
	  				oldMdaTypes.forEach(oldMdaType -> obMdaTypesMap.put(oldMdaType.getCode(), oldMdaType));
	  				
	  			}

	  			Date newEffectiveStartDate = getNewEffectiveStartDate(obMdaTypes);
	  			psMdaTypes.forEach(psMdaType ->
	  			{
	  				psMdaType.setEffectiveStartDate(newEffectiveStartDate);
	  				
	  				MdaType previousMdaType = obMdaTypesMap.get(psMdaType.getCode());
	  				if (previousMdaType == null)
	  				{
	  					psMdaType.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					psMdaType.setHistoryLinkId(previousMdaType.getHistoryLinkId());
	  				}
	  				
	  			});
	  			
	  			mdaTypeRepository.save(psMdaTypes);
	  			
	       		CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.MDA_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			logger.info("*** LoadingInfo ***:  Created new effective MdaTypes ");
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  MdaTypes haven't changed  ");
	  		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("MDA Types data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_MDA_TYPES, t, true);
        }
    }
    
    @Override
    @Transactional
   	public void getAndSaveSpecialPositionTypes(PersonnelOtherReferenceDataModel personnelOtherReferenceDataModel ) throws DataLoadException
    {
    	try
    	{
    		if (personnelOtherReferenceDataModel == null)
    			personnelOtherReferenceDataModel = integrationFacade.getOtherReferenceData();
    		
	  		Map<String, SpecialPositionType> psSpecialPositionTypesMap = new HashMap<String, SpecialPositionType>();    		
    		List<SpecialPositionType> psSpecialPositionTypes = personnelOtherReferenceDataModel.getSpecialPositionTypes();
    		psSpecialPositionTypes.forEach(specialPositionType -> psSpecialPositionTypesMap.put(specialPositionType.getCode(), specialPositionType));
    		
    		List<SpecialPositionType> obSpecialPositionTypes = specialPositionTypeRepository.findByDate(new Date());
    		Map<String, SpecialPositionType> obSpecialPositionTypesMap = new HashMap<String, SpecialPositionType>();
    		
    		int currentSize = obSpecialPositionTypes.size();
    		if (currentSize > 0)
    			obSpecialPositionTypes.forEach(specialPositionType -> obSpecialPositionTypesMap.put(specialPositionType.getCode(), specialPositionType));
	  		
	  		// Check for updates from scan
	  		boolean shouldSave = false;
	  		if (currentSize > 0)
	  		{
		  		for (SpecialPositionType psSpecialPositionType : psSpecialPositionTypes)
		  		{
		  			SpecialPositionType obSpecialPositionType = obSpecialPositionTypesMap.get(psSpecialPositionType.getCode());
		  			if (psSpecialPositionType.compareTo(obSpecialPositionType) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
	  		
	  			for (SpecialPositionType obSpecialPositionType  : obSpecialPositionTypes)
		  		{
	  				SpecialPositionType psSpecialPositionType = psSpecialPositionTypesMap.get(obSpecialPositionType.getCode());
	  				if (psSpecialPositionType == null)
	  				{
	  					shouldSave = true;
	  				}
	  				else if (obSpecialPositionType.compareTo(psSpecialPositionType) != 0)
		  			{
		  				shouldSave = true;
		  			}
		  		}
	  		}
	  		else
	  		{
	  			shouldSave = true;
	  		}
	  		
	  		if (shouldSave)
	  		{
	  			
	  			if (currentSize > 0)
	  			{
	  				obSpecialPositionTypes.forEach(obSpecialPositionType -> 
	  				{
	  					obSpecialPositionType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
	  					if (obSpecialPositionType.getHistoryLinkId() == null)
	  						obSpecialPositionType.setHistoryLinkId(historyLinkId++);
	  				});
	  				List<SpecialPositionType> oldSpecialPositionTypes = specialPositionTypeRepository.save(obSpecialPositionTypes);
	  				obSpecialPositionTypesMap.clear();
	  				oldSpecialPositionTypes.forEach(spt -> obSpecialPositionTypesMap.put(spt.getCode(), spt));
	  			}
	  			
	  			Date newEffectiveStartDate = getNewEffectiveStartDate(obSpecialPositionTypes);
	  			psSpecialPositionTypes.forEach(psSpecialPositionType ->
	  				{
	  				psSpecialPositionType.setEffectiveStartDate(newEffectiveStartDate);
	  				SpecialPositionType previousSpecialPositionType = obSpecialPositionTypesMap.get(psSpecialPositionType.getCode());
	  				if (previousSpecialPositionType == null)
	  				{
	  					psSpecialPositionType.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					psSpecialPositionType.setHistoryLinkId(previousSpecialPositionType.getHistoryLinkId());
	  				}
	  			});
	  			
	  			specialPositionTypeRepository.save(psSpecialPositionTypes);
	  			
	  			CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.SPECIAL_POSITION_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			logger.info("*** LoadingInfo ***:  Created new effective SpecialPositionTypes ");
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  SpecialPositionTypes haven't changed  ");
	  		}
    	
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Special Position Types data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_SPPOS_TYPES, t, true);
        }
    }
    

    @Override
    @Transactional
   	public void getAndSaveOfficerPositionTypes(PersonnelOtherReferenceDataModel personnelOtherReferenceDataModel ) throws DataLoadException
    {
    	try
    	{
    		if (personnelOtherReferenceDataModel == null)
    			personnelOtherReferenceDataModel = integrationFacade.getOtherReferenceData();

    		
	  		Map<String, OfficerPositionType> psOfficerPositionTypesMap = new HashMap<String, OfficerPositionType>();    		
    		List<OfficerPositionType> psOfficerPositionTypes = personnelOtherReferenceDataModel.getOfficerPositionTypes();
    		psOfficerPositionTypes.forEach(officerPositionType -> psOfficerPositionTypesMap.put(officerPositionType.getCode(), officerPositionType));
    		
    		List<OfficerPositionType> obOfficerPositionTypes = officerPositionTypeRepository.findByDate(new Date());
    		Map<String, OfficerPositionType> obOfficerPositionTypesMap = new HashMap<String, OfficerPositionType>();
    		int currentSize = obOfficerPositionTypes.size();
    		if (currentSize > 0)
    			obOfficerPositionTypes.forEach(officerPositionType -> obOfficerPositionTypesMap.put(officerPositionType.getCode(), officerPositionType));
	  		
	  		// Check for updates from scan
	  		boolean shouldSave = false;
	  		if (currentSize > 0)
	  		{
		  		for (OfficerPositionType psOfficerPositionType : psOfficerPositionTypes)
		  		{
		  			OfficerPositionType obOfficerPositionType = obOfficerPositionTypesMap.get(psOfficerPositionType.getCode());
		  			if (psOfficerPositionType.compareTo(obOfficerPositionType) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
	  		
	  			for (OfficerPositionType obOfficerPositionType  : obOfficerPositionTypes)
		  		{
	  				OfficerPositionType psOfficerPositionType = psOfficerPositionTypesMap.get(obOfficerPositionType.getCode());
	  				if (psOfficerPositionType == null)
	  				{
	  					shouldSave = true;
	  				}
	  				else if (obOfficerPositionType.compareTo(psOfficerPositionType) != 0)
		  			{
		  				shouldSave = true;
		  			}
		  		}
	  		}
	  		else
	  		{
	  			shouldSave = true;
	  		}
	  		
	  		if (shouldSave)
	  		{
	  			if (currentSize > 0)
	  			{
	  				obOfficerPositionTypes.forEach(obOfficerPositionType -> 
	  				{
	  					obOfficerPositionType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
	  					if (obOfficerPositionType.getHistoryLinkId() == null)
	  						obOfficerPositionType.setHistoryLinkId(historyLinkId++);
	  				});
	  				List<OfficerPositionType> oldOfficerPositionTypes = officerPositionTypeRepository.save(obOfficerPositionTypes);
	  				obOfficerPositionTypesMap.clear();
	  				oldOfficerPositionTypes.forEach(oldOfficerPositionType -> obOfficerPositionTypesMap.put(oldOfficerPositionType.getCode(), oldOfficerPositionType));
	  			}
	  			
	  			Date newEffectiveStartDate = getNewEffectiveStartDate(obOfficerPositionTypes);
	  			psOfficerPositionTypes.forEach(psOfficerPositionType ->
	  			{
	  				psOfficerPositionType.setEffectiveStartDate(newEffectiveStartDate);
	  				
	  				OfficerPositionType previousOfficerPositionType = obOfficerPositionTypesMap.get(psOfficerPositionType.getCode());
	  				if (previousOfficerPositionType == null)
	  				{
	  					psOfficerPositionType.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					psOfficerPositionType.setHistoryLinkId(previousOfficerPositionType.getHistoryLinkId());
	  				}
	  			});
	  			
	  			officerPositionTypeRepository.save(psOfficerPositionTypes);
	  			
	  			CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.OFFICER_POSITION_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			logger.info("*** LoadingInfo ***:  Created new effective OfficerPositionTypes ");
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  OfficerPositionTypes haven't changed  ");
	  		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Officer Position Types data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_OFPOS_TYPES, t, true);
        }
    }
   
    @Override
    @Transactional
   	public void getAndSaveUnavailabilityTypes() throws DataLoadException
    {
    	try
    	{
       		Map<String, UnavailabilityType> psUnavailabilityTypesMap = new HashMap<String, UnavailabilityType>();    		
    		List<UnavailabilityType> psUnavailabilityTypes = integrationFacade.getUnavailabilityTypes();
    		psUnavailabilityTypes.forEach(unavailabilityType -> psUnavailabilityTypesMap.put(unavailabilityType.getCode(), unavailabilityType));
    		
    		List<UnavailabilityType> obUnavailabilityTypes = unavailabilityTypeRepository.findByDate(new Date());
    		Map<String, UnavailabilityType> obUnavailabilityTypesMap = new HashMap<String, UnavailabilityType>();
	  		
    		int currentSize = obUnavailabilityTypes.size();
    		if (currentSize > 0)
    			obUnavailabilityTypes.forEach(UnavailabilityType -> obUnavailabilityTypesMap.put(UnavailabilityType.getCode(), UnavailabilityType));
	  		
	  		// Check for updates from scan
	  		boolean shouldSave = false;
	  		if (currentSize > 0)
	  		{
		  		for (UnavailabilityType psUnavailabilityType : psUnavailabilityTypes)
		  		{
		  			UnavailabilityType obUnavailabilityType = obUnavailabilityTypesMap.get(psUnavailabilityType.getCode());
		  			if (psUnavailabilityType.compareTo(obUnavailabilityType) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
	  		
	  			for (UnavailabilityType obUnavailabilityType  : obUnavailabilityTypes)
		  		{
	  				UnavailabilityType psUnavailabilityType = psUnavailabilityTypesMap.get(obUnavailabilityType.getCode());
	  				if (psUnavailabilityType == null)
	  				{
	  					shouldSave = true;
	  				}
	  				else if (obUnavailabilityType.compareTo(psUnavailabilityType) != 0)
		  			{
		  				shouldSave = true;
		  			}
		  		}
	  		}
	  		else
	  		{
	  			shouldSave = true;
	  		}
	  		
	  		if (shouldSave)
	  		{
	  			if (currentSize > 0)
	  			{
	  				obUnavailabilityTypes.forEach(obUnavailabilityType -> 
	  				{
	  					obUnavailabilityType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
	  					if (obUnavailabilityType.getHistoryLinkId() == null)
	  						obUnavailabilityType.setHistoryLinkId(historyLinkId++);
	  				});
	  				List<UnavailabilityType> unavailabilityTypes = unavailabilityTypeRepository.save(obUnavailabilityTypes);
		  			
		  			obUnavailabilityTypesMap.clear();
		  			unavailabilityTypes.forEach(unavailabilityType -> obUnavailabilityTypesMap.put(unavailabilityType.getCode(), unavailabilityType));
	  			}

	  			Date newEffectiveStartDate = getNewEffectiveStartDate(obUnavailabilityTypes);
	  			psUnavailabilityTypes.forEach(psUnavailabilityType ->
	  			{
	  				psUnavailabilityType.setEffectiveStartDate(newEffectiveStartDate);
	  				
	  				UnavailabilityType previousUnavailabilityType = obUnavailabilityTypesMap.get(psUnavailabilityType.getCode());
	  				if (previousUnavailabilityType == null)
	  				{
	  					psUnavailabilityType.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					psUnavailabilityType.setHistoryLinkId(previousUnavailabilityType.getHistoryLinkId());
	  				}
	  			});
	  			
	  			unavailabilityTypeRepository.save(psUnavailabilityTypes);
	  				
	  			logger.info("*** LoadingInfo ***:  Created new effective UnavailabilityTypes");
	  			
	  			CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.UNAVAILABILITY_TYPE_CACHE);
	  			sendRefreshCacheCommand(adminCommand);
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  UnavailabilityTypes haven't changed  ");
	  		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Unavailability Types data load from PeopleSoft has failed");
    		handleLoadException (ErrorMessage.PS_LOAD_UNAVAILABLE_TYPES, t, true);
        }
    }

    private class PersonThread implements Runnable
	{
		private String employeeId;
		private Date asOfDate;
		private String locationCode;
		
		public PersonThread(String employeeId, String locationCode, Date asOfDate) 
		{
			this.employeeId = employeeId;
			this.asOfDate = asOfDate;
			this.locationCode = locationCode;
		}


		@Override
		@Transactional
		public void run()
		{
			logger.debug("Loading person ID {} for location {}", employeeId, locationCode);
			
			List<PersonModel> personnelModels = new ArrayList<PersonModel>();
	    	try{
	    		PersonModel personModel = integrationFacade.getPerson(employeeId, asOfDate);
	    		if (personModel != null && StringUtils.isNotBlank(personModel.getEmployeeId()))
	    		{
	    			personnelModels.add(personModel);
	    	    	Person person = personMapper.convertPersonModelToEntity(personModel, null);
					cutOutHistory(person);
	    	    	persistenceService.addNewPerson(person, null);
	    	    	logger.debug("Loaded person {} for location {}", employeeId, locationCode);
	    		}
	    		else
	    		{
	    			logger.error("Loading {} person for location {} has faild since it doesn't exist in DB", employeeId, locationCode);
	    		}
	    	}
	    	catch(Throwable t)
			{
	    		String errorText = getFormattedMessage(LOAD_PERSONNEL_FROM_LOCATION_ASOFDATE, new Object[]{employeeId});
	    		sendLoadingErrorMessageToUI(errorText);
	    		integrationErrorHandlingService.handleDataLoadException (ErrorMessage.PS_LOAD_PERSONNEL, t, errorText);
	        }
	    	return;
		}
		
		private void cutOutHistory(Person person)
		{
			
			if (!CUT_OUT_HISTORY)
				return;
			
			if (person.getDetachmentHistory() != null && person.getDetachmentHistory().size() > PERSON_HISTORY_RECORDS)
			{
				Set<gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment> allDetachmentsSet  = person.getDetachmentHistory();
				final ConcurrentSkipListSet<gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment> futureDetachmentsSubSet  = 
						new ConcurrentSkipListSet<gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment>();
				
				allDetachmentsSet.stream().filter(d -> DateUtils.onOrAfter(d.getStartDate(), today)).
						forEach(d -> futureDetachmentsSubSet.add(d));

				allDetachmentsSet.removeAll(futureDetachmentsSubSet);
				Set<gov.nyc.dsny.smart.opsboard.domain.personnel.Detachment> pastDetachmentsSubSet = 
						ImmutableSet.copyOf(Iterables.limit(allDetachmentsSet, PERSON_HISTORY_RECORDS));

				futureDetachmentsSubSet.addAll(pastDetachmentsSubSet);
				
				person.setDetachmentHistory(futureDetachmentsSubSet);				
			}
			
			if (person.getMdaStatusHistory() != null && person.getMdaStatusHistory().size() > PERSON_HISTORY_RECORDS)
			{
				Set<MdaStatus> allMdaStatusSet  = person.getMdaStatusHistory();
				final ConcurrentSkipListSet<MdaStatus> futureMdaStatusSubSet  = new ConcurrentSkipListSet<MdaStatus>();

				allMdaStatusSet.stream().filter(mda -> DateUtils.onOrAfter(mda.getStartDate(), today)).
						forEach(d -> futureMdaStatusSubSet.add(d));

				allMdaStatusSet.removeAll(futureMdaStatusSubSet);
				Set<MdaStatus> pastMdaStatusSubset = ImmutableSet.copyOf(Iterables.limit(allMdaStatusSet, PERSON_HISTORY_RECORDS));
				
				futureMdaStatusSubSet.addAll(pastMdaStatusSubset);
				person.setMdaStatusHistory(futureMdaStatusSubSet);		
			}
			
			if (person.getSpecialPositionHistory() != null && person.getSpecialPositionHistory().size() > PERSON_HISTORY_RECORDS)
			{
				Set<SpecialPosition> allSpecialPositionSet  = person.getSpecialPositionHistory();
				final ConcurrentSkipListSet<SpecialPosition> futureSpecialPositionSubSet  = new ConcurrentSkipListSet<SpecialPosition>();

				allSpecialPositionSet.stream().filter(sp -> DateUtils.onOrAfter(sp.getStartDate(), today)).
						forEach(d -> futureSpecialPositionSubSet.add(d));

				allSpecialPositionSet.removeAll(futureSpecialPositionSubSet);
				
				Set<SpecialPosition> pastSpecialPositionSubset = ImmutableSet.copyOf(Iterables.limit(allSpecialPositionSet, PERSON_HISTORY_RECORDS));
				futureSpecialPositionSubSet.addAll(pastSpecialPositionSubset);
				
				person.setSpecialPositionHistory(futureSpecialPositionSubSet);
			}
			
			if (person.getUnavailabilityHistory() != null && person.getUnavailabilityHistory().size() > PERSON_HISTORY_RECORDS)
			{
				
				Set<UnavailabilityReason> allUnavailabilityReasonSet  = person.getUnavailabilityHistory();
				final ConcurrentSkipListSet<UnavailabilityReason> futureUnavailabilityReasonSubSet  = new ConcurrentSkipListSet<UnavailabilityReason>();

				allUnavailabilityReasonSet.stream().filter(ur -> DateUtils.onOrAfter(ur.getStart(), today)).
					forEach(d -> futureUnavailabilityReasonSubSet.add(d));

				allUnavailabilityReasonSet.removeAll(futureUnavailabilityReasonSubSet);
				
				Set<UnavailabilityReason> pastUnavailabilityReasonSubset = ImmutableSet.copyOf(Iterables.limit(allUnavailabilityReasonSet, PERSON_HISTORY_RECORDS));
				futureUnavailabilityReasonSubSet.addAll(pastUnavailabilityReasonSubset);

				
				person.setUnavailabilityHistory(futureUnavailabilityReasonSubSet);
			}
			
			if (person.getGroundingStatusHistory() != null && person.getGroundingStatusHistory().size() > PERSON_HISTORY_RECORDS)
            {
				Set<GroundingStatus> allGroundingStatusSet  = person.getGroundingStatusHistory();
				final ConcurrentSkipListSet<GroundingStatus> futureGroundingStatusSubSet  = new ConcurrentSkipListSet<GroundingStatus>();

				allGroundingStatusSet.stream().filter(gs -> DateUtils.onOrAfter(gs.getStartDate(), historical30DaysDate)).
						forEach(d -> futureGroundingStatusSubSet.add(d));

				allGroundingStatusSet.removeAll(futureGroundingStatusSubSet);
				Set<GroundingStatus> pastGroundingStatusSubset = ImmutableSet.copyOf(Iterables.limit(allGroundingStatusSet, PERSON_HISTORY_RECORDS));
				futureGroundingStatusSubSet.addAll(pastGroundingStatusSubset);
				
                person.setGroundingHistory(futureGroundingStatusSubSet);
            }

		}
	}
        
    private void shutDownPersonnelExecutor()
    {
    	try
		{
    		sendLoadPercentageMessageToUI(100f);
			personnelExecutor.shutdown();
			while (!personnelExecutor.isTerminated()) 
			{
				try
				{
					Thread.sleep(5000);
				}
				catch(Exception e){}
		    }
		    logger.debug("*** LoadingInfo ***:  Finished all personnel threads ");
		}
		catch (Exception e)
		{
			logger.debug("Personnel executor shutdown has failed", e);
		}
    }

       
	/**
	 * @return the personnelExecutor
	 */
	public ExecutorService getPersonnelExecutor() {
		return personnelExecutor;
	}
    
	 private void sendLoadPercentageMessageToUI(float percentage) 
	 {
	 	sendLoadPercentageMessageToUI (String.format("personnel:%f", percentage));
	 }
	 
	 @Override
	 public float getCurrentPersonLoadPercentage()
	 {
		 return currentPersonLoadPercentage;
	 }
} 

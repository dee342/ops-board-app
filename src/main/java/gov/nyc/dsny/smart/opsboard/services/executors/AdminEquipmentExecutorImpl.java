package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.cache.equipment.SubTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.SubcategoryCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.CategoryCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SeriesCacheService;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Series;
import gov.nyc.dsny.smart.opsboard.domain.equipment.SnowReadiness;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDown;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.DownCode;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.EquipmentTypeIntegration;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.MaterialType;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubTypeChained;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubTypeLoad;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubTypePlow;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.SubCategorySubType;
import gov.nyc.dsny.smart.opsboard.domain.reference.SubCategorySubTypeIntegration;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.integration.exception.DataLoadException;
import gov.nyc.dsny.smart.opsboard.integration.mapper.EquipmentEntityMapper;
import gov.nyc.dsny.smart.opsboard.integration.mapper.impl.EquipmentEntityMapperImpl;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentAction;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentModel;
import gov.nyc.dsny.smart.opsboard.integration.models.scan.EquipmentModelWithHistory;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.SeriesRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.DataLoadFilterRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.DownCodeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.EquipmentTypeIntegrationRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.MaterialTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubCategorySubTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubCategorySubTypeRepositoryIntegration;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubTypeChainedRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubTypeLoadRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubTypePlowRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.reference.SubTypeRepository;
import gov.nyc.dsny.smart.opsboard.persistence.services.equipment.EquipmentPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.ListUtils;

@Service
public class AdminEquipmentExecutorImpl  extends AdminExecutorAbstract implements AdminEquipmentExecutor
{
	private static final Logger logger = LoggerFactory.getLogger(AdminEquipmentExecutorImpl.class);
	
	@Autowired
	private EquipmentPersistenceService equipmentPersistenceService; 
	
	@Autowired
	private EquipmentEntityMapper equipmentMapper;
	
	@Autowired
	private SeriesRepository seriesRepository;
	
	@Autowired
	private MaterialTypeRepository materialTypeRepository;
	
	@Autowired
	private DownCodeRepository downCodeRepository;

	@Autowired
	private SubTypeLoadRepository subTypeLoadRepository;
	
	@Autowired
	private SubTypePlowRepository subTypePlowRepository;
	
	@Autowired
	private SubTypeChainedRepository subTypeChainedRepository;
	
	@Autowired
	private SubCategorySubTypeRepository subCategorySubTypeRepository;
	
	@Autowired
	private SubCategorySubTypeRepositoryIntegration subCategorySubTypeRepositoryIntegration;
	
	@Autowired
	private EquipmentTypeIntegrationRepository equipmentTypeIntegrationRepository;
	
	@Autowired
	private SubTypeRepository subTypeRepository;
	
	@Autowired
    private LocationCache locationCache;
	
	@Autowired
	private CategoryCacheService categoryCacheService;
	
	@Autowired
    private SubcategoryCache subcategoryCache;
	
	@Autowired
    private SubTypeCache subTypeCache;
	
	@Autowired
    private SeriesCacheService seriesCacheService;
	
	@Autowired
	private DataLoadFilterRepository dataLoadFilterRepository;
	
	private ExecutorService equipmentExecutor;
	private static final int EQUIPMENT_THREAD_POOL_SIZE = 50;
	private static final int EQUIPMENT_HISTORY_RECORDS = 10;
	private float currentEquipmentLoadPercentage;

	//*********************************************************************************************************************
	//**************************************** Initialization Methods *****************************************************
	//*********************************************************************************************************************
    @Override
	public void init()
	{
    	super.init();
    	equipmentExecutor = Executors.newFixedThreadPool(EQUIPMENT_THREAD_POOL_SIZE);
    	currentEquipmentLoadPercentage = 0;
		 
	}

    @Override
	public void loadEquipment()  throws  DataLoadException
	{
		loadEquipment(null);
	}
	
    @Override
	public void loadEquipment(Date asOfDate) throws DataLoadException
	{
    	try
    	{
	    	List<Location> locationList = locationCache.getLocations(new Date()); 
	    	int totalLocSize = locationList.size();
			float incrementalPercentage = 90f / totalLocSize;
	    	for(Location location : locationList)
			{
				if (StringUtils.isNotBlank(location.getGarageCode()))
					loadEquipmentByLocation(location.getCode(), asOfDate);
				currentEquipmentLoadPercentage+=incrementalPercentage;
				sendLoadPercentageMessageToUI(currentEquipmentLoadPercentage);
			}
    	}
    	catch (Throwable t)
    	{
    		handleLoadException (ErrorMessage.SCAN_LOAD_EQUIPMENT, t, true);
    	}
    	finally
    	{
    		shutEquipmentDownExecutor();
    		currentEquipmentLoadPercentage = 100;
    		sendLoadPercentageMessageToUI(currentEquipmentLoadPercentage);
    		logger.info("*** LoadingInfo ***:  Finished loading equipment from SCAN ");
    	}

	}

    @Override
	public void loadEquipmentByLocation(String locationCode) throws DataLoadException
	{
		loadEquipmentByLocation(locationCode, new Date());
	}
	
    @Override
	public void loadEquipmentByLocation(String locationCode, Date asOfDate) throws DataLoadException
	{
    	logger.info("***** Loading equipment from {} location *******************************", locationCode);
    	try
    	{
			String garageCode = locationCache.getGarageByLocation(locationCode, new Date());
			List<EquipmentAction> equipmentActions = integrationFacade.getEquipmentIDsByLocation(garageCode);
			if( null != equipmentActions)
			{
				for(EquipmentAction equipmentAction : equipmentActions)
				{
					logger.debug("Loading equipment {} - {}", equipmentAction.getVehicleSeries(), equipmentAction.getVehicleNumber());
					loadEquipmentById(equipmentAction.getVehicleSeries(), equipmentAction.getVehicleNumber(), garageCode, asOfDate);
				}
			}
    	}
    	catch (Throwable t)
    	{
    		sendLoadingErrorMessageToUI ("Failed loadEquipmentByLocation for " + locationCode);
    		handleLoadException (ErrorMessage.SCAN_LOAD_EQUIPMENT, t, false, " for location code " + locationCode);
    	}
	}
	
    @Override
	public void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode)  throws DataLoadException
	{
		loadEquipmentById(vehicleSeries, vehicleNumber, garageCode, null);
	}

    @Override
	public void loadEquipmentById(String vehicleSeries, String vehicleNumber, String garageCode, Date asOfDate)  throws DataLoadException
	{
    	EquipmentThread equipmentThread = new EquipmentThread(vehicleSeries,  vehicleNumber, garageCode,  asOfDate);
    	equipmentExecutor.execute(equipmentThread);
	}
	
    @Override
   	public void loadEquipmentFromModel(EquipmentModel equipmentModel)  throws DataLoadException
   	{
       	EquipmentThread equipmentThread = new EquipmentThread(equipmentModel);
       	equipmentExecutor.execute(equipmentThread);
   	}
   	
    
	
 
    //*********************************************************************************************************************
    //**************************************** SCAN Reference Data Methods ************************************************
  	//*********************************************************************************************************************

    @Override
    @Transactional
	public void getAndSaveSeries() throws DataLoadException
  	{
    	try
    	{
    		// Load and save all standard series from SCAN
	  		List<Series> seriesFromScanList = integrationFacade.getEquipmentSeries();
	  		Map<String, Series> codeSeriesFromScanMap = new HashMap<String, Series>();  
	  		seriesFromScanList.forEach(scanSeries -> 
	  		{
	  			scanSeries.setGroup(EquipmentEntityMapperImpl.convertEquipmentTypeToSeriesGroup(Integer.parseInt(scanSeries.getType())));
	  			scanSeries.setType(EquipmentEntityMapperImpl.convertEquipmentTypeCodeToEquipmentType(Integer.parseInt(scanSeries.getType())));
	  			codeSeriesFromScanMap.put(scanSeries.getCode(), scanSeries);
	  		});

	  		List<Series> seriesFromOBList = seriesRepository.findStandardByDate(new Date());
	  		Map<String, Series> codeSeriesFromOBMap = new HashMap<String, Series>();
	  		Date newEffectiveStartDate = getNewEffectiveStartDate(seriesFromOBList);
	  		int currentSize = seriesFromOBList.size();
	  		boolean shouldSave = false;
	  		if (currentSize > 0)
    		{
		  		seriesFromOBList.forEach(obSeries -> codeSeriesFromOBMap.put(obSeries.getCode(), obSeries));

		  		for (Series seriesFromScan : seriesFromScanList)
		  		{
		  			Series seriesFromOb = codeSeriesFromOBMap.get(seriesFromScan.getCode());
		  			if (seriesFromScan.compareTo(seriesFromOb) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
	  		
		  		List<Long> deletedSeriesIDs = new ArrayList<Long>();
		  		for (Series seriesFromOB : seriesFromOBList)
		  		{
		  			Series seriesFromScan = codeSeriesFromScanMap.get(seriesFromOB.getCode());
		  			
		  			if (seriesFromScan == null)
	  				{
		  				deletedSeriesIDs.add(seriesFromOB.getId());
	  					shouldSave = true;
	  				}
	  				else if (seriesFromOB.compareTo(seriesFromScan) != 0)
		  			{
		  				shouldSave = true;
		  			}
		  		}
		  		
		  		if (deletedSeriesIDs.size() > 0)
	  			{
	  				String activeAssignmentsText = areTerminatedSeriesActiveInOB(deletedSeriesIDs);
	  				if (activeAssignmentsText != null)
	  				{
	  					handleLoadException (ErrorMessage.SCAN_LOAD_TERMINATED_SERIES, null, true, activeAssignmentsText);
	  				}
	  			}
    		}
	  		else
	  		{
	  			shouldSave = true;
	  		}

	  		
	  		if (shouldSave)
	  		{
	  			Date startDate1 = null;
	  			Date endDate1 = null;
	  			if (currentSize > 0)
	  			{
	  				seriesFromOBList.parallelStream().forEach(obSeries -> obSeries.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday())));
	  				List<Series> oldSeries = seriesRepository.save(seriesFromOBList);
	  				startDate1 = oldSeries.get(0).getEffectiveStartDate();
		  			endDate1 = oldSeries.get(0).getEffectiveEndDate();
	  			}
	  			
  				seriesFromScanList.parallelStream().forEach(scanSeries ->scanSeries.setEffectiveStartDate(newEffectiveStartDate));
  				List<Series> newSeries = seriesRepository.save(seriesFromScanList);
	  			if (startDate1 != null)
	  			{
	  				Date startDate2 = newSeries.get(0).getEffectiveStartDate();
		  			Date endDate2 = newSeries.get(0).getEffectiveEndDate();
		  			updateSeries(startDate1, endDate1, startDate2, endDate2);
	  			}
	  			
	  			CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.SERIES_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			logger.info("*** LoadingInfo ***:  Created new effective Series ");
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  Series haven't changed  ");
	  		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI( "Series data load from Scan has failed");
    		handleLoadException (ErrorMessage.SCAN_LOAD_SERIES, t, true);
        }
  	}
    
    private String areTerminatedSeriesActiveInOB(List<Long> terminatedSerieIds) throws Exception
    {
    	Long spId = 0L; //refDataUpdatesStatusRepository.checkTerminatedSeries(terminatedSerieIds);
    	return getStoredProcedureErrorText (spId);
    }
    
    private String updateSeries(Date startDate1, Date endDate1, Date startDate2, Date endDate2) throws Exception
    {
    	Long spId = 0L; //refDataUpdatesStatusRepository.updateSeries (startDate1, endDate1, startDate2, endDate2);
    	return getStoredProcedureErrorText (spId);
    }
    
    @Override
    @Transactional
    public synchronized void getAndSaveMaterialTypes()  throws DataLoadException
    {
    	try
    	{
	  		List<MaterialType> materialTypesFromOBList = materialTypeRepository.findByDate(new Date());
	  		int currentSize = materialTypesFromOBList.size();
    		Map<String, MaterialType> obMaterialTypesMap = new HashMap<String, MaterialType>();
    		if (currentSize > 0)
    			materialTypesFromOBList.forEach(obMaterialType -> obMaterialTypesMap.put(obMaterialType.getCode(), obMaterialType));

    		List<MaterialType> materialTypesFromScanList = integrationFacade.getMaterialTypes();
    		Map<String, MaterialType> scanMaterialTypesMap = new HashMap<String, MaterialType>();
	  		materialTypesFromScanList.forEach(scanMaterialType -> scanMaterialTypesMap.put(scanMaterialType.getCode(), scanMaterialType));
	  		
	  		// Check for updates from scan
	  		boolean shouldSave = false;
	  		if (currentSize > 0)
	  		{
		  		for (MaterialType scanMaterialType : materialTypesFromScanList)
		  		{
		  			MaterialType obMaterialType = obMaterialTypesMap.get(scanMaterialType.getCode());
		  			if (scanMaterialType.compareTo(obMaterialType) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
		  		
	  			for (MaterialType obMaterialType  : materialTypesFromOBList)
		  		{
	  				MaterialType scanMaterialType = scanMaterialTypesMap.get(obMaterialType.getCode());
	  				if (scanMaterialType == null)
	  				{
	  					shouldSave = true;
	  				}
	  				else if (obMaterialType.compareTo(scanMaterialType) != 0)
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
	  				materialTypesFromOBList.forEach(obMaterialType -> 
	  				{
	  					obMaterialType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
	  					if (obMaterialType.getHistoryLinkId() == null)
	  						obMaterialType.setHistoryLinkId(historyLinkId++);
	  					
	  				});
	  				List<MaterialType> oldMaterialTypes = materialTypeRepository.save(materialTypesFromOBList);

	  				obMaterialTypesMap.clear();
	  				oldMaterialTypes.forEach(obMaterialType -> obMaterialTypesMap.put(obMaterialType.getCode(), obMaterialType));
	  			}

		  		Date newEffectiveStartDate = getNewEffectiveStartDate(materialTypesFromOBList);
	  			materialTypesFromScanList.forEach(scanMaterialType ->
	  			{
	  				scanMaterialType.setEffectiveStartDate(newEffectiveStartDate);
	  				MaterialType previousMaterialType = obMaterialTypesMap.get(scanMaterialType.getCode());
	  				if (previousMaterialType == null)
	  				{
	  					scanMaterialType.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					scanMaterialType.setHistoryLinkId(previousMaterialType.getHistoryLinkId());
	  				}
	  			});
	  			
	  			materialTypeRepository.save(materialTypesFromScanList);
	  			
	  			CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.MATERIAL_TYPE_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			logger.info("*** LoadingInfo ***:  Created new effective MaterialTypes ");
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  MaterialTypes haven't changed  ");
	  		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI ("MaterialTypes data load from Scan has failed");
    		handleLoadException (ErrorMessage.SCAN_LOAD_MATERIAL_TYPES, t, true);
        }

    }
    
    @Override
    @Transactional
    public void getAndSaveDownCodes() throws DataLoadException
    {
    	try
    	{
    		List<DownCode> downCodesFromOBList = downCodeRepository.findByDate(new Date());
    		int currentSize = downCodesFromOBList.size();
    		Map<String, DownCode> obDownCodesMap = new HashMap<String, DownCode>();
	  		downCodesFromOBList.forEach(obDownCode -> obDownCodesMap.put(obDownCode.getCode(), obDownCode));

    		List<DownCode> downCodesFromScanList = integrationFacade.getEquipmentDownCodes();
    		Map<String, DownCode> scanDownCodesMap = new HashMap<String, DownCode>();
    		downCodesFromScanList.forEach(scanDownCode -> scanDownCodesMap.put(scanDownCode.getCode(), scanDownCode));
	  		
	  		// Check for updates from scan
	  		boolean shouldSave = false;
	  		if (currentSize > 0)
	  		{
		  		for (DownCode scanDownCode : downCodesFromScanList)
		  		{
		  			DownCode obDownCode = obDownCodesMap.get(scanDownCode.getCode());
		  			if (scanDownCode.compareTo(obDownCode) != 0)
		  			{
		  				shouldSave = true;
		  				break;
		  			}
		  		}
		  		
	  			for (DownCode obDownCode  : downCodesFromOBList)
		  		{
	  				DownCode scanDownCode = scanDownCodesMap.get(obDownCode.getCode());
	  				if (scanDownCode == null)
	  				{
	  					shouldSave = true;
	  				}
	  				else if (obDownCode.compareTo(scanDownCode) != 0)
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
	  				downCodesFromOBList.forEach(obDownCode -> 
	  				{
	  					obDownCode.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
	  					if (obDownCode.getHistoryLinkId() == null)
	  						obDownCode.setHistoryLinkId(historyLinkId++);
	  				});
	  				List<DownCode> oldDownCodes = downCodeRepository.save(downCodesFromOBList);
	  				obDownCodesMap.clear();
	  				oldDownCodes.forEach(oldDownCode -> obDownCodesMap.put(oldDownCode.getCode(), oldDownCode));
	  			}
  				
	  			Date newEffectiveStartDate = getNewEffectiveStartDate(downCodesFromOBList);
	  			downCodesFromScanList.forEach(scanDownCode ->
	  			{
	  				scanDownCode.setEffectiveStartDate(newEffectiveStartDate);
	  				DownCode previousDownCode = obDownCodesMap.get(scanDownCode.getCode());
	  				if (previousDownCode == null)
	  				{
	  					scanDownCode.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					scanDownCode.setHistoryLinkId(previousDownCode.getHistoryLinkId());
	  				}
	  			});
	  			
	  			downCodeRepository.save(downCodesFromScanList);

	  			CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.DOWN_CODE_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			logger.info("*** LoadingInfo ***:  Created new effective DownCodes ");
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  DownCodes haven't changed  ");
	  		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI ("Down Codes data load from Scan has failed " + t.getCause().getMessage());
    		handleLoadException (ErrorMessage.SCAN_LOAD_DOWN_CODES, t, true);
        }
    }
   
    @Override
    @Transactional
    public void getAndSaveSubTypes() throws DataLoadException
    {
    	try
    	{
	  		List<SubType> subTypesFromScan = integrationFacade.getEquipmentSubTypes();
	  		List<SubType> subTypesFromOB = subTypeRepository.findByDate(new Date());
	  		Map<String, SubType> obSubTypesMap = new HashMap<String, SubType>();
	  		int currentSize = subTypesFromOB.size();
	  		if (currentSize > 0)
	  			subTypesFromOB.forEach(subTypeFromOB -> obSubTypesMap.put(subTypeFromOB.getCode(), subTypeFromOB));

    		List<SubTypeLoad> loadSubTypesFromScan = integrationFacade.getEquipmentSubTypeLoads();
	  		List<SubTypeLoad> loadSubTypesFromOB = subTypeLoadRepository.findByDate(new Date());
	  		
	  		List<SubTypePlow> plowSubTypesFromScan = integrationFacade.getEquipmentSubTypePlows();
	  		List<SubTypePlow> plowSubTypesFromOB = subTypePlowRepository.findByDate(new Date());
	  		
	  		List<SubTypeChained> chainedSubTypesFromScan = integrationFacade.getEquipmentSubTypeChained();
	  		List<SubTypeChained> chainedSubTypesFromOB = subTypeChainedRepository.findByDate(new Date());	  			  	
	  		
	  		if (haveSubTypesChanged(subTypesFromScan, subTypesFromOB) ||
	  			haveSubTypesLoadChanged(loadSubTypesFromScan, loadSubTypesFromOB) ||
	  			haveSubTypesPlowChanged(plowSubTypesFromScan, plowSubTypesFromOB) ||
	  			haveSubTypesChainedChanged(chainedSubTypesFromScan, chainedSubTypesFromOB))
	  		{
	  			if (currentSize > 0)
	  			{
		  			// Make data non effective
		  			subTypesFromOB.forEach(subType -> 
		  			{
		  				subType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
		  				if (subType.getHistoryLinkId() == null)
		  					subType.setHistoryLinkId(historyLinkId++);
		  			});
		  			
		  			loadSubTypesFromOB.forEach(loadSubType -> 
		  			{
		  				loadSubType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
		  				if (loadSubType.getHistoryLinkId() == null)
		  					loadSubType.setHistoryLinkId(historyLinkId++);
		  				
		  			});
		  			
		  			plowSubTypesFromOB.forEach(plowSubType -> 
		  			{
		  				plowSubType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
		  				if (plowSubType.getHistoryLinkId() == null)
		  					plowSubType.setHistoryLinkId(historyLinkId++);
		  			});	
		  			
		  			chainedSubTypesFromOB.forEach(chainedSubType -> 
		  			{
		  				chainedSubType.setEffectiveEndDate(DateUtils.getDateWithEndTime(DateUtils.getYesterday()));
		  				if (chainedSubType.getHistoryLinkId() == null)
		  					chainedSubType.setHistoryLinkId(historyLinkId++);
		  			});

		  			// Save non effective data
		  			List<SubType> oldSubTypes = subTypeRepository.save(subTypesFromOB);
		  			subTypeLoadRepository.save(loadSubTypesFromOB);
		  			subTypePlowRepository.save(plowSubTypesFromOB);
		  			subTypeChainedRepository.save(chainedSubTypesFromOB);
		  			
		  			obSubTypesMap.clear();
		  			oldSubTypes.forEach(oldSubType -> obSubTypesMap.put(oldSubType.getCode(), oldSubType));
	  			}
	  		
		  		Map<String, SubTypeChained> scanChainedSubTypesMap = new HashMap<String, SubTypeChained>();
		  		chainedSubTypesFromScan.forEach(item -> scanChainedSubTypesMap.put(item.getCode(), item));
		  		
		  		Map<String, EquipmentTypeIntegration> equipmentTypesMetaDataMap = new HashMap<String, EquipmentTypeIntegration>();
		  		List<EquipmentTypeIntegration> equipmentTypesMetaData = equipmentTypeIntegrationRepository.findAll();
		  		equipmentTypesMetaData.forEach(item -> equipmentTypesMetaDataMap.put(item.getCode(), item));
		  		
		  		Date newEffectiveStartDate = getNewEffectiveStartDate(subTypesFromOB);
		  		for (SubType scanSubType : subTypesFromScan)
		  		{
		  			scanSubType.setEffectiveStartDate(newEffectiveStartDate);
		  			
		  			SubType previousSubType = obSubTypesMap.get(scanSubType.getCode());
	  				if (previousSubType == null)
	  				{
	  					scanSubType.setHistoryLinkId(historyLinkId++ );
	  				}
	  				else
	  				{
	  					scanSubType.setHistoryLinkId(previousSubType.getHistoryLinkId());
	  				}
		  			
		  			EquipmentTypeIntegration equipmentTypeMetaData = equipmentTypesMetaDataMap.get(scanSubType.getCode());
		  			if (equipmentTypeMetaData != null)
		  				scanSubType.setEquipmentType(equipmentTypeMetaData.getDescription());
		  			else
		  				scanSubType.setEquipmentType(scanSubType.getDescription());
		  				
		  			if (scanChainedSubTypesMap.get(scanSubType.getCode()) != null)
		  				scanSubType.setCanBeChained(true);
		  			else
		  				scanSubType.setCanBeChained(false);
		  			
		  			
		  		}
		  		
		  		List<SubType> newOBSubTypes = subTypeRepository.save(subTypesFromScan);
		  		logger.info("*** LoadingInfo ***:  Created new effective Sub Types  ");
		  		
		  		obSubTypesMap.clear();
		  		newOBSubTypes.forEach(obSubType -> obSubTypesMap.put(obSubType.getCode(), obSubType));
		  		
		  		saveLoadSubTypes(loadSubTypesFromScan, obSubTypesMap);
		  		savePlowSubTypes(plowSubTypesFromScan, obSubTypesMap);
		  		saveChainedSubTypes(chainedSubTypesFromScan);
		  		saveSubCategorySubTypesAssociation();
		  		
		  		CommandRefreshCaches adminCommand = new CommandRefreshCaches(CommandRefreshCaches.SUB_TYPE_CACHE);
	  			this.sendRefreshCacheCommand(adminCommand);
	  			
	  			logger.info("*** LoadingInfo ***:  Created new effective SubTypes");
	  		}
	  		else
	  		{
	  			logger.info("*** LoadingInfo ***:  SubTypes and their dependencies haven't changed");
	  		}
	  		
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Sub Types Chained data from Scan has failed " + t.getCause().getMessage());
    		handleLoadException (ErrorMessage.SCAN_LOAD_CHAINED_SUBTYPES, t, true);
        }
    }
    
    private boolean haveSubTypesChanged(List<SubType> subTypesFromScan, List<SubType> subTypesFromOB)
    {
    	if (ListUtils.isEmpty(subTypesFromOB))
    		return true;
    	

    	Map<String, SubType> scanSubTypesMap = new HashMap<String, SubType>();
    	subTypesFromScan.forEach(scanSubType -> scanSubTypesMap.put(scanSubType.getCode(), scanSubType));
    	
    	Map<String, SubType> obSubTypesMap = new HashMap<String, SubType>();
    	subTypesFromOB.forEach(obSubType -> obSubTypesMap.put(obSubType.getCode(), obSubType));

    	boolean shouldSave = false;
    	for (SubType scanSubType : subTypesFromScan)
  		{
    		SubType obSubType = obSubTypesMap.get(scanSubType.getCode());
  			if (scanSubType.compareTo(obSubType) != 0)
  			{
  				shouldSave = true;
  				break;
  			}
  		}
		
		for (SubType obSubType  : subTypesFromOB)
  		{
			SubType scanSubType = scanSubTypesMap.get(obSubType.getCode());
			if (scanSubType == null)
			{
				shouldSave = true;
			}
			else if (scanSubType.compareTo(scanSubType) != 0)
  			{
  				shouldSave = true;
  			}
		}

    	return shouldSave;
    }
    
    private boolean haveSubTypesLoadChanged(List<SubTypeLoad> subTypesLoadFromScan, List<SubTypeLoad> subTypesLoadFromOB)
    {
    	if (ListUtils.isEmpty(subTypesLoadFromOB))
    		return true;
    	
    	if (subTypesLoadFromScan.size() != subTypesLoadFromOB.size())
    		return true;
    	
    	Map<String, SubTypeLoad> obSubTypesLoadMap = new HashMap<String, SubTypeLoad> ();
    	subTypesLoadFromOB.forEach(obSubTypeLoad -> obSubTypesLoadMap.put((obSubTypeLoad.getCode() +  obSubTypeLoad.getDescription()), obSubTypeLoad));
    	
    	for (SubTypeLoad scanSubTypeLoad : subTypesLoadFromScan)
    	{
   			SubTypeLoad obSubTypeLoad = obSubTypesLoadMap.get(scanSubTypeLoad.getCode() + scanSubTypeLoad.getDescription());
   			if (scanSubTypeLoad.compareTo(obSubTypeLoad) != 0)
   				return true;
    	}
    	
    	return false;
    }
    
    private boolean haveSubTypesPlowChanged(List<SubTypePlow> subTypesPlowFromScan, List<SubTypePlow> subTypesPlowFromOB)
    {
    	if (ListUtils.isEmpty(subTypesPlowFromOB))
    		return true;
    	
    	if (subTypesPlowFromScan.size() != subTypesPlowFromOB.size())
    		return true;
    	
    	Map<String, SubTypePlow> obSubTypesPlowMap = new HashMap<String, SubTypePlow> ();
    	subTypesPlowFromOB.forEach(obSubTypePlow -> obSubTypesPlowMap.put((obSubTypePlow.getCode() + obSubTypePlow.getDescription()), obSubTypePlow));
    	
    	for (SubTypePlow scanSubTypePlow : subTypesPlowFromScan)
    	{
   			SubTypePlow obSubTypePlow = obSubTypesPlowMap.get(scanSubTypePlow.getCode() + scanSubTypePlow.getDescription());
   			if (scanSubTypePlow.compareTo(obSubTypePlow) != 0)
   				return true;
    	}
    	
    	return false;
    }
    
    private boolean haveSubTypesChainedChanged(List<SubTypeChained> subTypesChainedFromScan, List<SubTypeChained> subTypesChainedFromOB)
    {
    	if (ListUtils.isEmpty(subTypesChainedFromOB))
    		return true;
    	
    	if (subTypesChainedFromScan.size() != subTypesChainedFromOB.size())
    		return true;
    	
    	Map<String, SubTypeChained> obSubTypesChainedMap = new HashMap<String, SubTypeChained> ();
    	subTypesChainedFromOB.forEach(obSubTypeChained -> obSubTypesChainedMap.put(obSubTypeChained.getCode(), obSubTypeChained));
    	
    	for (SubTypeChained scanSubTypeChained : subTypesChainedFromScan)
    	{
   			SubTypeChained obSubTypeChained = obSubTypesChainedMap.get(scanSubTypeChained.getCode());
   			if (scanSubTypeChained.compareTo(obSubTypeChained) != 0)
   				return true;
    	}
    	
    	return false;
    }

    
    private void saveLoadSubTypes(List<SubTypeLoad> scanLoadSubTypes, Map<String, SubType> obSubTypesMap) throws DataLoadException
    {
    	try
    	{
	  		for (SubTypeLoad scanSubTypeLoad : scanLoadSubTypes)
	  		{
	  			SubType obSubType = obSubTypesMap.get(scanSubTypeLoad.getCode());
	  			scanSubTypeLoad.setSubType(obSubType);
	  			obSubType.addLoadSubType(scanSubTypeLoad);
	  		}
	  			
	  		subTypeLoadRepository.save(scanLoadSubTypes);
	  		logger.info("*** LoadingInfo ***:  Created new effective Load Sub Types ");
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Saving Load Sub Types data from Scan has failed " + t.getCause().getMessage());
    		handleLoadException (ErrorMessage.SCAN_LOAD_LOAD_SUBTYPES, t, true);
        }
    }
    
    private void savePlowSubTypes(List<SubTypePlow> scanPlowSubTypes, Map<String, SubType> obSubTypesMap) throws DataLoadException
    {
    	try
    	{
    		for (SubTypePlow scanPlowSubType : scanPlowSubTypes)
	  		{
	  			SubType obSubType = obSubTypesMap.get(scanPlowSubType.getCode());
	  			scanPlowSubType.setSubType(obSubType);
	  			obSubType.addPlowSubType(scanPlowSubType);
	  		}
	  			
	  		subTypePlowRepository.save(scanPlowSubTypes);
	  		logger.info("*** LoadingInfo ***:  Created new effective Plow Sub Types ");
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Saving Plow Sub Types from Scan has failed " + t.getCause().getMessage());
    		handleLoadException (ErrorMessage.SCAN_LOAD_PLOW_SUBTYPES, t, true);
        }
    }

    private void saveChainedSubTypes(List<SubTypeChained> scanChainedSubTypes) throws DataLoadException
    {
    	try
    	{
	  		subTypeChainedRepository.save(scanChainedSubTypes);
	  		logger.info("*** LoadingInfo ***:  Created new effective Chained Sub Types   ");
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Saving Chained Sub Types from Scan has failed " + t.getCause().getMessage());
    		handleLoadException (ErrorMessage.SCAN_LOAD_CHAINED_SUBTYPES, t, true);
        }
    }

    private void saveSubCategorySubTypesAssociation() throws DataLoadException
    {
    	Set<SubCategorySubType> subCategorySubTypesFromDb = new HashSet<>(subCategorySubTypeRepository.findAll());    	
    	List<SubCategorySubType> subCategorySubTypeAssociationList = new ArrayList<SubCategorySubType>();
    	
    	try
    	{
    		Map<String, SubType> subTypesMap = subTypeCache.getSubTypeMap(new Date());
    		List<SubCategorySubTypeIntegration> metaDataList = subCategorySubTypeRepositoryIntegration.findByDate(new Date());
    		for (SubCategorySubTypeIntegration metaData : metaDataList)
    		{
    			SubCategorySubType association = new SubCategorySubType();
    			
    			SubType subType = subTypesMap.get(metaData.getCode());
    			association.setEquipmentSubType(subType);
    			
    			Subcategory subCategory = subcategoryCache.getSubCategoryByID(metaData.getSubcategoryId());    			
    			association.setSubcategory(subCategory);
    			
    			association.setPriority(metaData.getPriority());
    			
    			if(!subCategorySubTypesFromDb.contains(association)){    			  		
    				subCategorySubTypeAssociationList.add(association);
    			}
    		}
    		
    		if(!subCategorySubTypeAssociationList.isEmpty()){
    			subCategorySubTypeRepository.save(subCategorySubTypeAssociationList);
    			logger.info("*** LoadingInfo ***:  Created new effective Sub Category - Sub Types Association");
    		}else{
    			logger.info("*** LoadingInfo ***: No SubcatSubtype isnerted - no new meta data ");
    		}
    	}
    	catch(Throwable t)
		{
    		sendLoadingErrorMessageToUI("Saving Chained Sub Types from Scan has failed " + t.getCause().getMessage());
    		handleLoadException (ErrorMessage.SCAN_LOAD_CHAINED_SUBTYPES, t, true);
        }
    }
    
    private class EquipmentThread implements Runnable
	{
		private String vehicleSeries;
		private String vehicleNumber;
		private String garageCode;
		private Date asOfDate;
		private EquipmentModel equipmentModel;
		
		public EquipmentThread(String vehicleSeries, String vehicleNumber, String garageCode, Date asOfDate) 
		{
			this.vehicleSeries = vehicleSeries;
			this.vehicleNumber = vehicleNumber;
			this.asOfDate = asOfDate;
			this.garageCode = garageCode;
			
		}
		
		public EquipmentThread(EquipmentModel equipmentModel) 
		{
			this(equipmentModel.getVehicleSeries(), equipmentModel.getVehicleNumber(), equipmentModel.getCurrentLocation(), null);
			this.equipmentModel = equipmentModel;
		}
		

		@Override
		public void run()
		{
			logger.debug("Loading equipment {}-{} from {} garage", vehicleSeries, vehicleNumber, garageCode);
			List<Equipment> equipmentList = new ArrayList<>();
			try
			{
				List<Series> seriesList = seriesCacheService.getSeries(new Date());
				EquipmentModelWithHistory equipmentFromScan = null;
				if (equipmentModel == null)
				{
					equipmentFromScan = integrationFacade.getEquipment(vehicleSeries, vehicleNumber, asOfDate);
				}

				Equipment equipment = null;
				if(null != equipmentFromScan)
				{
					equipment = equipmentMapper.convertEquipmentModelWithHistoryToEntity(equipmentFromScan, seriesList, subTypeCache.getSubTypes());
					cutOutHistory(equipment);
				}
				else
				{
					equipment = equipmentMapper.convertEquipmentModelToEntity(equipmentModel, null, seriesList, subTypeCache.getSubTypes());
					
					// Some snow fields are required in DB
					SnowReadiness snowReadiness = new SnowReadiness();
					snowReadiness.setChained(false);
					equipment.setSnowReadiness(snowReadiness);
				}
				
				Series series = equipment.getSeries();
				series = seriesRepository.save(series);
				
				
				equipment.setSeries(series);
				equipmentList.add(equipment);
				equipmentList = saveEquipment (equipmentList);
				logger.debug("Loaded equipment {}-{} from {} garage", vehicleSeries, vehicleNumber, garageCode);
			
				
				return;
			}
			catch(Throwable t)
			{
				String errorText = getFormattedMessage("Data load from Scan has failed for {0}-{1} from {2} garage)", new Object[]{vehicleSeries, vehicleNumber, garageCode});
				sendLoadingErrorMessageToUI (errorText);
				integrationErrorHandlingService.handleDataLoadException (ErrorMessage.SCAN_LOAD_EQUIPMENT, t, errorText);
	        }
		}
		
		@Transactional
		private List<Equipment> saveEquipment(List<Equipment> equipmentList)
		{
			return equipmentPersistenceService.save(equipmentList, true);
		}
		
		private void cutOutHistory(Equipment equipment)
		{
			if (!CUT_OUT_HISTORY)
				return;
			
			if (equipment.getDetachmentHistory() != null && equipment.getDetachmentHistory().size() > EQUIPMENT_HISTORY_RECORDS)
			{
				
				List<gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment> allDetachmentsList  = equipment.getDetachmentHistory();
				final List<gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment> historycalDetachmentsSubList  = 
						new ArrayList<gov.nyc.dsny.smart.opsboard.domain.equipment.Detachment> ();

				allDetachmentsList.stream().filter(d -> DateUtils.onOrAfter(d.getLastModifiedActual(), historical30DaysDate)).
					forEach(d -> historycalDetachmentsSubList.add(d));

				if (historycalDetachmentsSubList.size() < EQUIPMENT_HISTORY_RECORDS)
				{
					historycalDetachmentsSubList.clear();
					historycalDetachmentsSubList.addAll(allDetachmentsList.
						subList(allDetachmentsList.size() -  EQUIPMENT_HISTORY_RECORDS, 
						allDetachmentsList.size()));
				}
				equipment.setDetachmentHistory(historycalDetachmentsSubList);
			}
			
			if (equipment.getUpDownHistory() != null && equipment.getUpDownHistory().size() > EQUIPMENT_HISTORY_RECORDS)
			{
				
				List<UpDown> allUpDownList  = equipment.getUpDownHistory();
				final List<UpDown> historycalUpDownSubList  = new ArrayList<UpDown>();

				allUpDownList.stream().filter(ud -> DateUtils.onOrAfter(ud.getLastModifiedActual(), historical30DaysDate)).
					forEach(d -> historycalUpDownSubList.add(d));

				if (historycalUpDownSubList.size() < EQUIPMENT_HISTORY_RECORDS)
				{
					historycalUpDownSubList.clear();
					historycalUpDownSubList.addAll(allUpDownList.
						subList(allUpDownList.size() - EQUIPMENT_HISTORY_RECORDS, 
						allUpDownList.size()));
				}
				equipment.setUpDownHistory(historycalUpDownSubList);
			}
		}
	}
    
    
    private void shutEquipmentDownExecutor()
    {
    	try
		{
    		sendLoadPercentageMessageToUI (100f);
    		equipmentExecutor.shutdown();
			while (!equipmentExecutor.isTerminated()) 
			{
				try
				{
					Thread.sleep(5000);
				}
				catch(Exception e){}
		    }

		    logger.debug("*** LoadingInfo ***:   Finished all equipment threads ");
		}
		catch (Exception e)
		{
			logger.debug("Equipment executor shutdown has failed", e);
		}
    }
    
    
    private void sendLoadPercentageMessageToUI(float percentage) 
    {
    	sendLoadPercentageMessageToUI ( String.format("equipment:%f", percentage));
    }
    
	/**
	 * @return the equipmentExecutor
	 */
	public ExecutorService getEquipmentExecutor() {
		return equipmentExecutor;
	}

	 @Override
	 public float getCurrentEquipmentLoadPercentage()
	 {
		 return currentEquipmentLoadPercentage;
	 }

} 

package gov.nyc.dsny.smart.opsboard.services;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.equipment.DownCodeCache;
import gov.nyc.dsny.smart.opsboard.cache.equipment.MaterialTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.equipment.SubTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.EquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.PersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardQuotaCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.QuotaCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.SubcategoryCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.CategoryCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.MdaTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.OfficerPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SeriesCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SpecialPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.UnavailabilityTypeCacheService;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandEnterMaintenance;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandExitMaintenance;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandRefreshCaches;
import gov.nyc.dsny.smart.opsboard.domain.ApplicationSettings;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.misc.AdminCommandMessage;
import gov.nyc.dsny.smart.opsboard.misc.ConsumerSimpleMessageListenerContainer;
import gov.nyc.dsny.smart.opsboard.persistence.repos.ApplicationSettingsRepository;
import gov.nyc.dsny.smart.opsboard.utils.LocationUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.util.LinkedHashMap;

import javax.annotation.PostConstruct;

import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A service that listens for multi-board commands that act on locations that the server handles and then processes
 * them.
 *
 * When an Operations Board is loaded for the first time, the Location Service binds itself to a new AMQP queue on
 * RabbitMQ server for the board location. Any multi-board commands that are sent to the location are picked up by the
 * Location Service and processed for each live board in the server's cache that matches this location.
 *
 * Processing entails executing the command against the board in the server cache and if successful, then sending a new
 * board command off to all of the board's clients.
 */
public class AdminService{

	private static final Logger log = LoggerFactory.getLogger(AdminService.class);
	
	@Autowired
	private AmqpAdmin amqpAdmin;

	/**
	 * Caches
	 */
	@Autowired
	private BoardCacheService boardsCache;
	
	@Autowired
	private EquipmentCacheService equipmentCache;
		
	@Autowired
	private PersonnelCacheService personnelCache;
	
	@Autowired
	private BoardPersonnelCacheService boardPersonnelCache;

	@Autowired
	private BoardEquipmentCacheService boardEquipmentCache;
	
	@Autowired
	private LocationCache locationCache;
	
	@Autowired
	private BoardTypeCache boardTypeCache;
	
	@Autowired
	private DownCodeCache downCodeCache;
	
	@Autowired
	private MaterialTypeCache materialTypeCache;
	
	@Autowired
	private CategoryCacheService categoryCacheService;
	
	@Autowired
	private MdaTypeCacheService mdaTypeCacheService;

	@Autowired
	private SpecialPositionTypeCacheService specialPositionTypeCacheService;

	@Autowired
	private OfficerPositionTypeCacheService officerPositionTypeCacheService;

	@Autowired
	private UnavailabilityTypeCacheService unavailabilityTypeCacheService;
	
	@Autowired
	private QuotaCache quotaCache;
	
	@Autowired
	private BoardQuotaCache boardQuotaCache;	
	
	@Autowired
	private ShiftCacheService shiftCacheService;
	
	@Autowired
	private SubcategoryCache subcategoryCache;
	
	@Autowired
	private SubTypeCache subTypeCache;
	
	@Autowired
	private SeriesCacheService seriesCacheService;
	
	@Autowired
	private ConnectionFactory connectionFactory;
	
	@Autowired
	private WebApplicationContext appContext;
	
	@Autowired
	private ApplicationSettingsRepository appSettingRepository;

	@Qualifier("adminListenerContainer")
	@Autowired
	private ConsumerSimpleMessageListenerContainer listener;

	@Autowired
	private LogContext logContext;

	@Autowired
	private BoardKeyFactory boardKeyFactory;
	
	@Qualifier("adminQueue")
	@Autowired
	private Queue adminQueue;
	
	@Qualifier("broadCastQueue")
	@Autowired
	private Queue broadCastQueue;
	
	public static String getBroaddCastQueueName(){
		return "broadcast." + LocationUtils.GetAppServerName();
	}
	
	public static String getQueueName(){
		return "admin-messages." + LocationUtils.GetAppServerName();
	}
	
	public void createMessageBroadCastConsumer(){
		if(listener==null){
			log.error("listener is null");
			return;
		}
		
		Binding b = new Binding(getBroaddCastQueueName(), DestinationType.QUEUE, "amq.topic", "broadcast", null);
		amqpAdmin.deleteQueue(getBroaddCastQueueName());
		amqpAdmin.declareQueue(broadCastQueue);
		amqpAdmin.declareBinding(b);

		try {
			listener.startConsumer();
			log.info(appendEntries(logContext),
					"************************************createMessageConsumerOnAdmin - RabbitMQ listener '{}' has been created.", getQueueName());
		} catch (Exception e) {
			log.error(appendEntries(logContext),
					"createMessageConsumerOnAdmin - RabbitMQ listener '{}' can't be created.", e);
		}
	}
	
	/**
	 * Creates a new location queue on the Rabbit MQ Server and binds itself to it in order to listen for commands.
	 *
	 * @param location
	 *            board location to listen for commands
	 */
	@PostConstruct
	public void createMessageConsumerOnLocation() {
		createMessageBroadCastConsumer();
		
		if(listener==null){
			log.error("admin listener is null");
			return;
		}
		
		Binding b = new Binding(getQueueName(), DestinationType.QUEUE, "amq.topic", "admin.messages", null);
		amqpAdmin.deleteQueue(getQueueName());
		amqpAdmin.declareQueue(adminQueue);
		amqpAdmin.declareBinding(b);

		try {
			listener.startConsumer();
			log.info(appendEntries(logContext),
					"************************************createMessageConsumerOnAdmin - RabbitMQ listener '{}' has been created.", getQueueName());
		} catch (Exception e) {
			log.error(appendEntries(logContext),
					"createMessageConsumerOnAdmin - RabbitMQ listener '{}' can't be created.", e);
		}
	}


	public void deleteListener() {
		if (listener != null) {
			try {
				listener.stop();
				listener.destroy();
			} catch (Exception e) {
				log.error(appendEntries(logContext),
						"deleteListener, stopping listener caused an error (" + e.getMessage() + ").", e);
			}
		}
		amqpAdmin.deleteQueue("admin-messages");
	}

	/**
	 * @return the map of all location listeners.
	 */
	public ConsumerSimpleMessageListenerContainer getListener() {
		return listener;
	}


	/**
	 * Receives a command from the server that is destined for a specific location this service handles and process is
	 * accordingly.
	 *
	 * Processing entails converting the message into the specific multi-board command and executing the command against
	 * all live boards in the server meet the command's matching algorithm. If successful executed on a board, the
	 * server then constructs an equivalent board command and sends it off to all of the board's active clients.
	 *
	 * @param message
	 *            location command message
	 */
	public void receiveMessage(byte[] message) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			AdminCommandMessage commandMessage = mapper.readValue(message, AdminCommandMessage.class);
			logContext.enrichLogContext(commandMessage);
			log.info(appendEntries(logContext), "receiveMessage - Received command: {}.", commandMessage);

			if(StringUtils.equalsIgnoreCase(CommandRefreshCaches.COMMAND_REFRESH_CACHES, commandMessage.getCommandName()))
			{
				LinkedHashMap<String, Object> content = (LinkedHashMap<String, Object>) commandMessage.getCommandContent();
				String cacheName = (String) content.get(CommandRefreshCaches.CACHE_NAME_KEY);
				processCacheRefreshCommand(cacheName);
			}
			else if(StringUtils.equalsIgnoreCase(CommandEnterMaintenance.COMMAND_NAME, commandMessage.getCommandName())){
				processAppModeChangeCommand(commandMessage);
			}
		} catch (Exception e) {
			log.error(appendEntries(logContext), "receiveMessage error: {}.", e.getMessage(), e);
		}
	}
	
	private void processAppModeChangeCommand(AdminCommandMessage commandMessage) throws OpsBoardError, InterruptedException{
		if(commandMessage.getKey().equalsIgnoreCase(CommandEnterMaintenance.COMMAND_KEY)){
			appContext.getServletContext().setAttribute(ApplicationSettings.APPLICATION_MODE, ApplicationSettings.APPLICATION_MODE_MAINTENANCE);
			clearAndStoreActiveBoards();
		}
		else if(commandMessage.getKey().equalsIgnoreCase(CommandExitMaintenance.COMMAND_KEY)){
			restorePreviouseActiveBoards();
			appContext.getServletContext().setAttribute(ApplicationSettings.APPLICATION_MODE, ApplicationSettings.APPLICATION_MODE_NORMAL);
		} 
	}
	
	private void processCacheRefreshCommand(String cacheName)
			throws OpsBoardError {
		if(CommandRefreshCaches.BOARD_CACHES.equals(cacheName))
		{
			personnelCache.clear();
			boardPersonnelCache.clear();
			equipmentCache.clear();
			boardEquipmentCache.clear();
			boardsCache.refresh();
		}
		else
		{
			refreshReferenceDataCache(cacheName);
		}
	}
	
	private void restorePreviouseActiveBoards() throws OpsBoardError{
		ApplicationSettings appModeSetting = appSettingRepository.findByName(LocationUtils.GetAppServerName() + ApplicationSettings.SUFFIX_ACTIVE_BOARD_IDS);
		if ( appModeSetting != null && appModeSetting.getValue() != null)
		{
			for(String boardID : appModeSetting.getValue().split(",")){
				String[] boardIDArray = boardID.split("_");
				if(boardIDArray.length ==2){
					try
					{
						BoardKey key = boardKeyFactory.createBoardKey(boardIDArray[1], boardIDArray[0]);
						boardsCache.get(key);
						log.info("Board {}-{} was loaded into cache while exitting maintenance mode", boardIDArray[1], boardIDArray[0]);
					}
					catch (Exception e)
					{
						log.warn("Couldn't load board {}-{} into cache while exitting maintenance mode", e, boardIDArray[1], boardIDArray[0]);
					}
				}
			}
		}
		else
		{
			log.info("No boards were loaded onto cache on {} server while exitting maintenance mode", LocationUtils.GetAppServerName());
		}
	}
	
	private void clearAndStoreActiveBoards() throws OpsBoardError{
		StringBuilder sb = new StringBuilder();
		boardsCache.getBoardsMap().keySet().stream().forEach(b -> sb.append(",").append(b));
		ApplicationSettings appModeSetting = appSettingRepository.findByName(LocationUtils.GetAppServerName() + ApplicationSettings.SUFFIX_ACTIVE_BOARD_IDS);
		if(appModeSetting == null){
			appModeSetting = new ApplicationSettings();
			appModeSetting.setName(LocationUtils.GetAppServerName() + ApplicationSettings.SUFFIX_ACTIVE_BOARD_IDS);
		}
		appModeSetting.setValue(sb.toString());
		appSettingRepository.save(appModeSetting);

		personnelCache.clear();
		boardPersonnelCache.clear();
		equipmentCache.clear();
		boardEquipmentCache.clear();
		boardsCache.clear();
		refreshReferenceDataCache(CommandRefreshCaches.ALL_CACHES);
	}
	
	
	private void refreshReferenceDataCache(String cacheName)
			throws OpsBoardError {
		if(CommandRefreshCaches.BOARD_TYPE_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			boardTypeCache.refresh();
		if(CommandRefreshCaches.LOCATION_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			locationCache.refresh();
		if(CommandRefreshCaches.DOWN_CODE_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			downCodeCache.refresh();
		if(CommandRefreshCaches.MATERIAL_TYPE_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			materialTypeCache.refresh();
		if(CommandRefreshCaches.CATEGORY_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			categoryCacheService.refresh();
		if(CommandRefreshCaches.MDA_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			mdaTypeCacheService.refresh();
		if(CommandRefreshCaches.QUOTA_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName)){
			quotaCache.refresh();
			boardQuotaCache.refresh();
		}
		if(CommandRefreshCaches.SHIFT_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			shiftCacheService.refresh();
		if(CommandRefreshCaches.SUBCATEGORY_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			subcategoryCache.refresh();
		if(CommandRefreshCaches.SUB_TYPE_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			subTypeCache.refresh();
		if(CommandRefreshCaches.SERIES_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			seriesCacheService.refresh();
		if(CommandRefreshCaches.SPECIAL_POSITION_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			specialPositionTypeCacheService.refresh();
		if(CommandRefreshCaches.OFFICER_POSITION_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			officerPositionTypeCacheService.refresh();
		if(CommandRefreshCaches.UNAVAILABILITY_TYPE_CACHE.equals(cacheName) || CommandRefreshCaches.ALL_CACHES.equals(cacheName))
			unavailabilityTypeCacheService.refresh();
	}

	
}

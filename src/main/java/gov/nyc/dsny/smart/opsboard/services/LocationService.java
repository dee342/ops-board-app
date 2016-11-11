package gov.nyc.dsny.smart.opsboard.services;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.EquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.PersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.ILowPriorityCommand;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.LocationCommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.ReflectionMultiBoardCommandFactory;
import gov.nyc.dsny.smart.opsboard.commands.equipment.AbstractMultiBoardEquipmentCommand;
import gov.nyc.dsny.smart.opsboard.commands.equipment.AbstractOpsBoardEquipmentCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.AbstractMultiBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.commands.person.opsboard.AbstractOpsBoardPersonnelCommand;
import gov.nyc.dsny.smart.opsboard.configs.tool.RabbitMQRestAPI;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.misc.ConsumerSimpleMessageListenerContainer;
import gov.nyc.dsny.smart.opsboard.misc.SubCommandAspect;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LocationUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.WebApplicationContext;

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
public class LocationService {

	private static final Logger log = LoggerFactory.getLogger(LocationService.class);

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Value("${RabbitMQ.HAQueuesEnabled}")
	private String bHAQueuesEnabled;
	
	@Autowired
	private PersonnelCacheService personnelCacheService;
	
	@Autowired
	private EquipmentCacheService equipmentCacheService;

	@Autowired
	private BoardEquipmentCacheService boardEquipmentCacheService;

	@Autowired
	private BoardPersonnelCacheService boardPersonnelCache;

	@Autowired
	private BoardCacheService boardsCache;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	@Qualifier("listenerForLocationAdapter")
	private MessageListenerAdapter listenerForLocationAdapter;

	private ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer> listeners = new ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer>();

	@Autowired
	private LocationCache locationCache;

	@Autowired
	private LogContext logContext;

	@Autowired
	private SimpMessagingTemplate messenger;

	@Autowired
	private BoardPersistenceService persistService;

	@Autowired
	private ReflectionMultiBoardCommandFactory reflectionMultiBoardCommandFactory;

	@Autowired
	private RabbitMQRestAPI rabbitMQRestAPI;
	
	@Autowired
	private WebApplicationContext appContext;
	
	@Qualifier("generalTaskExecutor")
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	
	/**
	 * Creates a new location queue on the Rabbit MQ Server and binds itself to it in order to listen for commands.
	 *
	 * @param loc
	 *            board location to listen for commands
	 */
	public void createMessageConsumerOnLocation(Location loc) {
		
		List<String> locs = new ArrayList<String>();
		locs.add(loc.getCode());
		if (loc.isServicesEquipmentLocations()) {
			for (Location el : loc.getServiceLocations()) {
				locs.add(el.getCode());
			}
		}
		
		for (String l : locs) {
			String location = l;

			// TODO: Refactor out literals
			String queueName = getQueueName(location);
	
			if (listeners.containsKey(queueName) && isQueueExist(queueName)) {
				continue;
			}
	
			Map<String, Object> queueArguments = new HashMap<String, Object>();
			if (bHAQueuesEnabled.equalsIgnoreCase("true")) {
				queueArguments.put("x-ha-policy", "all");
			}
	
			Queue q = new Queue(queueName, false, false, true, queueArguments);
			Binding b = new Binding(queueName, DestinationType.QUEUE, "amq.topic", "tomcat.messages." + location, null);
			
			//delete the Board queue on different servers
			rabbitMQRestAPI.getMatchedQueueNames("server-messages.", location).forEach(qn -> amqpAdmin.deleteQueue(qn));
			amqpAdmin.deleteQueue(queueName);
			amqpAdmin.declareQueue(q);
			amqpAdmin.declareBinding(b);
	
			ConsumerSimpleMessageListenerContainer container = new ConsumerSimpleMessageListenerContainer();
			container.setConnectionFactory(connectionFactory);
			container.setQueueNames(queueName);
			container.setMessageListener(listenerForLocationAdapter);
			try {
				container.startConsumer();
				listeners.put(queueName, container);
				log.info(appendEntries(logContext),
						"createMessageConsumerOnLocation - RabbitMQ listener '{}' has been created.", queueName);
			} catch (Exception e) {
				log.error(appendEntries(logContext),
						"createMessageConsumerOnLocation - RabbitMQ listener '{}' can't be created.", e);
			}
		}
	}

	/**
	 * Deletes the listener for the specified queue.
	 *
	 * @param queue
	 *            name of location queue
	 */
	public void deleteListener(Location loc) {
		
		List<String> locs = new ArrayList<String>();
		locs.add(loc.getCode());
		if (loc.isServicesEquipmentLocations()) {
			for (Location el : loc.getServiceLocations()) {
				locs.add(el.getCode());
			}
		}
		
		for (String l : locs) {
			String location = l;
			
			String queueName = getQueueName(location);
			ConsumerSimpleMessageListenerContainer container = listeners.remove(queueName);
			if (container != null) {
				try {
					container.stop();
					container.destroy();
				} catch (Exception e) {
					log.error(appendEntries(logContext),
							"deleteListener, stopping listener caused an error (" + e.getMessage() + ").", e);
				}
			}
			amqpAdmin.deleteQueue(queueName);
		}
	}

	/**
	 * @return the map of all location listeners.
	 */
	public ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer> getListenersMap() {
		return listeners;
	}

	public String getQueueName(String location) {
		return "server-messages." + LocationUtils.GetAppServerName() + "_" + location;
	}

	/**
	 * @return the reflectionMultiBoardCommandFactory
	 */
	public ReflectionMultiBoardCommandFactory getReflectionMultiBoardCommandFactory() {
		return reflectionMultiBoardCommandFactory;
	}

	public boolean isQueueExist(String queueName) {
		return amqpAdmin.getQueueProperties(queueName) != null;
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
		String stringMesage = new String(message);
		try {
			// ObjectMapper mapper = new ObjectMapper();
			LocationCommandMessage commandMessage = new LocationCommandMessage(stringMesage);
			logContext.enrichLogContext(commandMessage);
			log.info(appendEntries(logContext), "receiveMessage - Received command: {}.", commandMessage);

			IMultiBoardCommand command = reflectionMultiBoardCommandFactory.createCommand(commandMessage);
			
			if(command.isFromIntegration()){
				if(command instanceof AbstractMultiBoardEquipmentCommand){
					equipmentCacheService.get(((AbstractMultiBoardEquipmentCommand) command).getEquipmentId(), true);
				}
				if(command instanceof AbstractMultiBoardPersonnelCommand){
					personnelCacheService.get(((AbstractMultiBoardPersonnelCommand) command).getPersonId(), true);
				}
			}

			Date date = DateUtils.toBoardDateNoNull(commandMessage.getDate());
			Location location = locationCache.getLocation(commandMessage.getLocation(), date);

			// logContext.enrichLogContext(command);

			Collection<BoardContainer> boardContainers = boardsCache
					.get()
					.stream()
					.filter(bc -> {
						return DateUtils.toBoardDate(bc.getBoard().getDate()).compareTo(
								DateUtils.removeTime(DateUtils.getYesterday())) >= 0
								&& command.matchBoard(bc.getBoard(), location);
					})
					.sorted((bc1, bc2) -> DateUtils.toBoardDate(bc1.getBoard().getDate()).compareTo(
							DateUtils.toBoardDate(bc2.getBoard().getDate()))).collect(Collectors.toList());

			boardContainers
					.forEach(container -> {
						CompletableFuture.runAsync(() -> {
							try {
								Board board = container.getBoard();

								// Command needs to be thread safe as it's execute method will change its internal
								// states)
								// Easier to rebuild than deep copy as some commands have transient fields that are set
								// by command factory
								AbstractMultiBoardCommand threadsafeCommand = reflectionMultiBoardCommandFactory
										.createCommand(commandMessage);

								CommandProcessor commandProcessor = new CommandProcessor (commandMessage, board, threadsafeCommand, container);
								if (threadsafeCommand instanceof ILowPriorityCommand)
								{
									commandProcessor.setPriority(Thread.MIN_PRIORITY);
									taskExecutor.execute(commandProcessor);
								}
								else
								{
									commandProcessor.processCommand();
								}
								
							} catch (Exception e) {
								log.error(
										appendEntries(logContext),
										"receiveMessage - Error in command processing thread for command {}.  Message - {}",
										commandMessage.getCommandName(), e.getMessage(), e);
					}
				}, taskExecutor);
			});
		} catch (Exception e) {
			log.error(appendEntries(logContext), "receiveMessage error: {}.", e.getMessage(), e);
		}
	}

	/**
	 * @param reflectionMultiBoardCommandFactory
	 *            the reflectionMultiBoardCommandFactory to set
	 */
	public void setReflectionMultiBoardCommandFactory(
			ReflectionMultiBoardCommandFactory reflectionMultiBoardCommandFactory) {
		this.reflectionMultiBoardCommandFactory = reflectionMultiBoardCommandFactory;
	}
	
	private class CommandProcessor extends Thread
	{
		private LocationCommandMessage commandMessage;
		private Board board; 
		private AbstractMultiBoardCommand threadsafeCommand;
		private BoardContainer container;
		
		public CommandProcessor(LocationCommandMessage commandMessage,
				Board board, AbstractMultiBoardCommand threadsafeCommand, BoardContainer container) 
		{
			super();
			this.commandMessage = commandMessage;
			this.board = board;
			this.threadsafeCommand = threadsafeCommand;
			this.container = container;
		}

		public void run()
		{
			appContext.getServletContext().setAttribute(ILowPriorityCommand.LAST_AUTO_COMPLETION_COMMAND_PROCESSED_TIME, new Date());
			processCommand();
		}
		
		public void processCommand()
		{
			synchronized (container) {

				try
				{
					log.debug(appendEntries(logContext),
							"Command '{}' to '{}' matched to board '{}'. CommandText '{}'",
							commandMessage.getCommandName(), commandMessage.getLocation(),
							board.getId(), commandMessage.getCommandContent());
	
					// Enrich command with BoardPerson (there are multiple boards due to future
					// dates...resulting in multiple cache updates).
					if (threadsafeCommand instanceof AbstractOpsBoardPersonnelCommand) {
						AbstractOpsBoardPersonnelCommand face = (AbstractOpsBoardPersonnelCommand) threadsafeCommand;
						String id = BoardPerson.CREATE_ID(face.getPersonId(), board.getDate());
						BoardPerson bp = boardPersonnelCache.get(board.getKey(), id, true);
						face.setBoardPerson(bp);
						board.getPersonnel().put(bp.getPerson().getId(), bp);
					}
					// Enrich command with BoardEquipment (there are multiple boards due to future
					// dates...resulting in multiple cache updates).
					if (threadsafeCommand instanceof AbstractOpsBoardEquipmentCommand) {
						AbstractOpsBoardEquipmentCommand face = (AbstractOpsBoardEquipmentCommand) threadsafeCommand;
						String id = BoardPerson.CREATE_ID(face.getEquipmentId(), board.getDate());
						BoardEquipment be = boardEquipmentCacheService.get(board.getKey(), id, true);
						face.setBoardEquipmentId(id);
						face.setBoardEquipment(be);
						board.getEquipment().put(be.getEquipment().getId(), be);
					}
	
					// Apply on server (i.e. update cached entities
					threadsafeCommand.execute(board);
	
					// Prepare board command for distribution to clients
					// (i.e. browsers)
					CommandMessage boardCommandMessage = new CommandMessage();
					boardCommandMessage.setCommandName(commandMessage.getCommandName());
					boardCommandMessage.setCommandContent(threadsafeCommand);
					boardCommandMessage.setDate(board.getDate());
					boardCommandMessage.setLocation(board.getLocation().getCode());
					boardCommandMessage.setServerSequence(container.getAutoIncrementSequence());
					boardCommandMessage.setUser(commandMessage.getUser());
	
					// Send to clients
					String topic = "/topic/commands." + boardCommandMessage.getLocation() + "."
							+ board.getDate();
					log.info(appendEntries(logContext),
							"receiveMessage - Board message sent to: {}.", topic);
					messenger.convertAndSend(topic, boardCommandMessage);
					
					SubCommandAspect.postProcessMulti(boardsCache, boardKeyFactory, messenger, threadsafeCommand);
				}
				catch (OpsBoardError obe) 
				{
					log.error(
						appendEntries(logContext),
						"receiveMessage - Command '{}' was not applied.  Error code: {}; message: {}; stacktrace: {}",
						commandMessage.getCommandName(), obe.getCode(), obe.getMessage(), obe);
				}

			}
		}
	}
}
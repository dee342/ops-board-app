package gov.nyc.dsny.smart.opsboard.services;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IPersistentCommand;
import gov.nyc.dsny.smart.opsboard.commands.LocationCommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.ReflectionBoardCommandFactory;
import gov.nyc.dsny.smart.opsboard.commands.ReflectionMultiBoardCommandFactory;
import gov.nyc.dsny.smart.opsboard.configs.tool.RabbitMQRestAPI;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.misc.ConsumerSimpleMessageListenerContainer;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;
import gov.nyc.dsny.smart.opsboard.utils.LocationUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A service that listens for board commands that are to be executed on the server's live boards and executes their
 * persistence methods.
 *
 * When an Operations Board is loaded for the first time, the Persistence Service binds itself to a new AMQP queue on
 * RabbitMQ server for that board. Any board commands that are for that board are picked up by the service and
 * processed.
 *
 * Processing entails executing the command's persist method against the board.
 */
public class PersistanceService {

	private static final Logger log = LoggerFactory.getLogger(PersistanceService.class);

	@Autowired
	BoardCacheService boardsCache;

	@Autowired
	BoardPersistenceService persistService;

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Value("${RabbitMQ.HAQueuesEnabled}")
	private String bHAQueuesEnabled;

	@Autowired
	private ConnectionFactory connectionFactory;

	@Autowired
	@Qualifier("listenerForBoardAdapter")
	private MessageListenerAdapter listenerForBoardAdapter;

	private ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer> listeners = new ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer>();

	@Autowired
	private LogContext logContext;

	@Autowired
	private ReflectionBoardCommandFactory reflectionBoardCommandFactory;
	
	@Autowired
	private ReflectionMultiBoardCommandFactory reflectionMultiBoardCommandFactory;

	@Autowired
	private RabbitMQRestAPI rabbitMQRestAPI;
	
	public String getQueueName(BoardKey key){
		return "commands." + LocationUtils.GetAppServerName() + "_" + key;
	}
	
	public boolean isQueueExist(String queueName){
		return amqpAdmin.getQueueProperties(queueName)!=null;
	}
	
	/**
	 * Creates a new board queue on the Rabbit MQ Server and binds itself to it in order to listen for commands.
	 *
	 * @param key
	 *            key that uniquely identifies a board
	 */

	public void createMessageConsumerOnBoard(BoardKey key) {

		// TODO: Refactor out literals
		String queueName = getQueueName(key);

		if(isQueueExist(queueName)){
			return;
		}
		
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		if (bHAQueuesEnabled.equalsIgnoreCase("true")) {
			queueArguments.put("x-ha-policy", "all");
		}

		Queue q = new Queue(queueName, false, false, true, queueArguments);
		Binding b = new Binding(queueName, DestinationType.QUEUE, "amq.topic", "commands."
				+ key.getLocation().getCode() + "." + key.getDate(), null);
		amqpAdmin.deleteQueue(queueName);

		//delete the Board queue on different servers
		rabbitMQRestAPI.getMatchedQueueNames("commands.", key.toString()).forEach(qn -> amqpAdmin.deleteQueue(qn));
		amqpAdmin.declareQueue(q);
		amqpAdmin.declareBinding(b);

		ConsumerSimpleMessageListenerContainer container = new ConsumerSimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		container.setMessageListener(listenerForBoardAdapter);
		try {
			container.startConsumer();
			listeners.put(queueName, container);
			log.info(appendEntries(logContext),
					"createMessageConsumerOnBoard - RabbitMQ listener '{}' has been created", queueName);
		} catch (Exception e) {
			log.error(appendEntries(logContext),
					"createMessageConsumerOnBoard - RabbitMQ listener '{}' can't be created", e);
		}
	}

	/**
	 * Deletes the listener for the specified queue.
	 *
	 * @param queue
	 *            name of board queue
	 */
	public void deleteListener(BoardKey key) {
		String queueName = getQueueName(key);
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

	/**
	 * @return the map of all board listeners.
	 */
	public ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer> getListenersMap() {
		return listeners;
	}

	/**
	 * Receives a command from the server that is destined for a specific board that is serviced by this server and
	 * process is accordingly.
	 *
	 * Processing entails converting the message into the specific board command and invoking its persistence method.
	 *
	 * @param message
	 *            command message
	 */
	public void receiveMessage(byte[] message) {
		
		ObjectMapper mapper = new ObjectMapper();
		
		String boardId = "";

		try {
			CommandMessage commandMessage = mapper.readValue(new String(message), CommandMessage.class);
			
			logContext.enrichLogContext(commandMessage);			
			log.info(appendEntries(logContext), "receiveMessage - Received command: {}.", commandMessage);
			
			AbstractBoardCommand boardCommand = reflectionBoardCommandFactory.createCommand(commandMessage);
			IMultiBoardCommand multiBoardCommand = null;
			if (boardCommand == null) {
				LocationCommandMessage locationCommandMessage = new LocationCommandMessage(new String(message));
				multiBoardCommand = reflectionMultiBoardCommandFactory.createCommand(locationCommandMessage);
				if (multiBoardCommand == null) {
					log.info(appendEntries(logContext), "receiveMessage - Nothing to persist.");
					return;
				}
			}

			BoardContainer bc = null;
			IPersistentCommand command = null;
			if (boardCommand != null) {
				bc = boardsCache.get(boardCommand.getBoardId());
				command = boardCommand;
			} else {
				bc = boardsCache.get(multiBoardCommand.getBoardId());
				command = multiBoardCommand;
			}
			if(command != null){
				boardId = command.getBoardId();
			}
			
			log.info(appendEntries(logContext), "receiveMessage - Saving to DB. Command ({}).", command);			
			if (bc != null) {
				//TODO may need to be removed for performance
				synchronized (bc) {
					command.persist(bc.getBoard(), persistService);
					amqpAdmin.purgeQueue(getQueueName(bc.getBoard().getKey()), true);
				}
			} else {
				log.info(appendEntries(logContext), "receiveMessage - No board to persist command against.");
			}
		} catch (Exception e) {
			log.error(appendEntries(logContext), "receiveMessage - Got an exception (" + e.getMessage() + "). - Board Id " + boardId, e);
		}
	}
}

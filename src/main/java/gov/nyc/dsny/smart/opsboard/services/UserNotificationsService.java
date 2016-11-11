package gov.nyc.dsny.smart.opsboard.services;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.configs.tool.RabbitMQRestAPI;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.misc.ConsumerSimpleMessageListenerContainer;
import gov.nyc.dsny.smart.opsboard.utils.LocationUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.security.Principal;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * A service that listens for board commands that are to be executed on the
 * server's live boards and executes their persistence methods.
 *
 * When an Operations Board is loaded for the first time, the Persistence
 * Service binds itself to a new AMQP queue on RabbitMQ server for that board.
 * Any board commands that are for that board are picked up by the service and
 * processed.
 *
 * Processing entails executing the command's persist method against the board.
 */
public class UserNotificationsService {

	private static final Logger log = LoggerFactory.getLogger(PersistanceService.class);

	@Autowired
	private AmqpAdmin amqpAdmin;

	@Value("${RabbitMQ.HAQueuesEnabled}")
	private String bHAQueuesEnabled;

	@Autowired
	private ConnectionFactory connectionFactory;

	private ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer> listeners = new ConcurrentHashMap<String, ConsumerSimpleMessageListenerContainer>();

	@Autowired
	private RabbitMQRestAPI rabbitMQRestAPI;
	
	@Autowired
	private LogContext logContext;

	/**
	 * Creates a new board queue on the Rabbit MQ Server and binds itself to it
	 * in order to listen for commands.
	 *
	 * @param key
	 *            key that uniquely identifies a board
	 */
	public void createMessageConsumerOnUser(Principal principal, BoardKey key) {

		//TODO: Refactor out string literals
		String queueNameSuffix = principal.getName() + "_" + key.getLocation().getCode() + "_" + key.getDate();
		String queueName = "user-queue." + LocationUtils.GetAppServerName() + "_" + queueNameSuffix;
		if(amqpAdmin.getQueueProperties(queueName) != null){
			return;
		}
		
		
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		if (bHAQueuesEnabled.equalsIgnoreCase("true")) {
			queueArguments.put("x-ha-policy", "all");
		}

		Queue q = new Queue(queueName, false, false, true, queueArguments);
		Binding b = new Binding(queueName, DestinationType.QUEUE, "amq.topic", "user-queue-notifications."+principal.getName()+ "."+ key.getLocation().getCode() + "." + key.getDate(), null);
		rabbitMQRestAPI.getMatchedQueueNames("user-queue.", queueNameSuffix).forEach(qn -> amqpAdmin.deleteQueue(qn));
		amqpAdmin.deleteQueue(queueName);
		amqpAdmin.declareQueue(q);
		amqpAdmin.declareBinding(b);

		ConsumerSimpleMessageListenerContainer container = new ConsumerSimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(queueName);
		//container.setMessageListener(listenerForUserNotificationsAdapter);
		try {
			container.startConsumer();
			listeners.put(queueName, container);
			log.info(appendEntries(logContext),
					"createMessageConsumerOnUser - RabbitMQ listener '{}' has been created", queueName);
		} catch (Exception e) {
			log.error(appendEntries(logContext),
					"createMessageConsumerOnUser - RabbitMQ listener '{}' can't be created", e);
		}
	}

	/**
	 * Deletes the listener for the specified queue.
	 *
	 * @param queue
	 *            name of board queue
	 */
	public void deleteListener(String queueName) {
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
}

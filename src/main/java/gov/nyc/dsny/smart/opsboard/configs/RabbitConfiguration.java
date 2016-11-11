package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.configs.tool.RabbitMQRestAPI;
import gov.nyc.dsny.smart.opsboard.misc.ConsumerSimpleMessageListenerContainer;
import gov.nyc.dsny.smart.opsboard.services.AdminService;
import gov.nyc.dsny.smart.opsboard.services.LocationService;
import gov.nyc.dsny.smart.opsboard.services.PersistanceService;
import gov.nyc.dsny.smart.opsboard.services.UserNotificationsService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {

	@Value("${RabbitMQ.Host}")
	private String rabbitHost;

	@Value("${RabbitMQ.Port}")
	private int rabbitPort;

	@Value("${RabbitMQ.Username}")
	private String rabbitLogin;

	@Value("${RabbitMQ.Password}")
	private String rabbitPassword;
	
	@Value("${RabbitMQ.HAQueuesEnabled}")
	private String bHAQueuesEnabled;

	@Value("${RabbitMQ.ADMIN.Port}")
	private int rabbitAdminPort;
	
	@Bean
	public RabbitMQRestAPI rabbitAPI(){
		StringBuilder sb = new StringBuilder("http://");
		sb.append(rabbitHost).append(":").append(rabbitAdminPort);
		RabbitMQRestAPI api = new RabbitMQRestAPI(sb.toString(), rabbitLogin, rabbitPassword);
		return api;
	}
	
	@Bean
	public ConnectionFactory connectionFactory() {
		CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitHost);
		connectionFactory.setPort(rabbitPort);
		connectionFactory.setUsername(rabbitLogin);
		connectionFactory.setPassword(rabbitPassword);
		return connectionFactory;
	}

	// //////////////////////

	@Bean
	MessageListenerAdapter listenerForBoardAdapter(PersistanceService receiver) {
		return new MessageListenerAdapter(receiver, "receiveMessage");
	}

	@Bean
	MessageListenerAdapter listenerForLocationAdapter(LocationService receiverForLocation) {
		return new MessageListenerAdapter(receiverForLocation, "receiveMessage");
	}
	
	@Bean
	MessageListenerAdapter listenerForAdminAdapter(){
		return new MessageListenerAdapter(receiverForAdmin(), "receiveMessage");
	}
	
	@Bean
	public Queue broadCastQueue(){
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		if (bHAQueuesEnabled.equalsIgnoreCase("true")) {
			queueArguments.put("x-ha-policy", "all");
		}
		Queue q = new Queue(AdminService.getBroaddCastQueueName(), false, false, false, queueArguments);
		return q;
	}
	
	@Bean
	public Queue adminQueue(){
		Map<String, Object> queueArguments = new HashMap<String, Object>();
		if (bHAQueuesEnabled.equalsIgnoreCase("true")) {
			queueArguments.put("x-ha-policy", "all");
		}
		Queue q = new Queue(AdminService.getQueueName(), false, false, false, queueArguments);
		return q;
	}
	
	@Bean
	public ConsumerSimpleMessageListenerContainer adminListenerContainer() {
		ConsumerSimpleMessageListenerContainer container = new ConsumerSimpleMessageListenerContainer();
	    container.setConnectionFactory(connectionFactory());
	    container.setQueueNames(AdminService.getQueueName(), AdminService.getBroaddCastQueueName());
	    container.setMessageListener(listenerForAdminAdapter());
	    return container;
	}
	
	/*@Bean
	MessageListenerAdapter listenerForUserNotificationsAdapter(UserNotificationsService receiverForUserNotifications) {
		return new MessageListenerAdapter(receiverForUserNotifications, "receiveMessage");
	}*/

	// //////////////////////

	@Bean
	PersistanceService receiver() {
		return new PersistanceService();
	}

	@Bean
	LocationService receiverForLocation() {
		return new LocationService();
	}
	
	@Bean
	AdminService receiverForAdmin(){
		return new AdminService();
	}
	
	@Bean
	UserNotificationsService receiverForUserNotifications() {
		return new UserNotificationsService();
	}

	// //////////////////////
}

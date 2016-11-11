package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.security.HttpSessionIdHandshakeInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

	@Value("${RabbitMQ.Host}")
	private String rabbitHost;

	@Value("${RabbitMQ.STOMP.Port}")
	private int rabbitStompPort;

	@Value("${RabbitMQ.STOMP.HeartbeatSendInterval}")
	private int heartbeatSendInterval;
	
	@Value("${RabbitMQ.STOMP.HeartbeatReceiveInterval}")
	private int heartbeatReceiveInterval;
	
	
	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {

		config.enableStompBrokerRelay("/queue/", "/topic/")
		            .setRelayHost(rabbitHost)
		            .setRelayPort(rabbitStompPort)
                    .setSystemHeartbeatSendInterval(heartbeatSendInterval)
                    .setSystemHeartbeatReceiveInterval(heartbeatReceiveInterval);

		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/loadstatus","/loadError", "/wsboard/{boardId}/{boardDate}", "/wsdisplayboard/{boardId}", "/wskioskDashboard")
			.withSockJS()
			.setInterceptors(new HttpSessionIdHandshakeInterceptor());
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration){
		//TODO  - get rid of this, enable single threaded stomp messages instead of multithreaded
		
		registration./*setMessageSizeLimit(50 * 1024).*/setSendBufferSizeLimit(10 * 1024*1024);//.setSendTimeLimit(Integer.MAX_VALUE);
	}
}
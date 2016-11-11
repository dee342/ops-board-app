package gov.nyc.dsny.smart.opsboard.misc;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

public class ConsumerSimpleMessageListenerContainer extends
		SimpleMessageListenerContainer {

	public void startConsumer() throws Exception {
		super.doStart();
		}
	
}

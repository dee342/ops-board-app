package gov.nyc.dsny.smart.opsboard.misc;

import gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardGFRepository;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.GemfireAdminService;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandAdminLowMemory;
import gov.nyc.dsny.smart.opsboard.integration.configs.RabbitMQConfiguration;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.gemfire.GemfireSystemException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import com.gemstone.gemfire.cache.LowMemoryException;
import com.gemstone.gemfire.cache.query.QueryExecutionLowMemoryException;

@Aspect
@Component
public class GemfireAspect {

	private static final Logger log = LoggerFactory.getLogger(GemfireAspect.class);
	
	@Autowired
	private BoardGFRepository boardGFRepository;
	
	@Autowired
	private SimpMessagingTemplate messenger;
	
	@Autowired
	private GemfireAdminService gemfireAdminService;
		
	@AfterThrowing(pointcut=("execution(* gov.nyc.dsny.smart.opsboard.cache..*.*(..))"), throwing="gemfireException")
	public void afterGemfireExcpetionThrowing(JoinPoint jp, GemfireSystemException gemfireException){
		log.error("Encounter Gemfire System Exception", gemfireException);

		if(QueryExecutionLowMemoryException.class.isInstance(gemfireException.getCause()) || LowMemoryException.class.isInstance(gemfireException.getCause())){
			
			CommandAdminLowMemory broadcastLowMemoryCmd = new CommandAdminLowMemory();
			AdminCommandMessage broadcastMsg = new AdminCommandMessage();
			broadcastMsg.setCommandName(broadcastLowMemoryCmd.getName());
			messenger.convertAndSend(RabbitMQConfiguration.BOARD_CAST_QUEUE_TOPIC_NAME, broadcastMsg);			
		}
	}
}

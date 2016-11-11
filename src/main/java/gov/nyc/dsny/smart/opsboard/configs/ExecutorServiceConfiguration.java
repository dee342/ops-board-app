package gov.nyc.dsny.smart.opsboard.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorServiceConfiguration {

	@Bean(name="generalTaskExecutor")
	public ThreadPoolTaskExecutor taskExecutor(){
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(20);
		executor.setMaxPoolSize(100);
		executor.setWaitForTasksToCompleteOnShutdown(true);
		return executor;
	}
}

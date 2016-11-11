package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.misc.ApplicationContextProvider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppContextConfiguration {

	@Bean
	public ApplicationContextProvider contextApplicationContextProvider() {
		return new ApplicationContextProvider();
	}

}


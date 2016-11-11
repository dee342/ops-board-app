/*package gov.nyc.dsny.smart.opsboard;

import gov.nyc.dsny.smart.opsboard.configs.IntegrationConfiguration;
import gov.nyc.dsny.smart.opsboard.configs.LogConfiguration;
import gov.nyc.dsny.smart.opsboard.validation.ValidatingMongoEventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

@Configuration
@Import({IntegrationConfiguration.class, LogConfiguration.class})
@ComponentScan(basePackages = {"gov.nyc.dsny.smart.opsboard.services"})
public class IntegrationApplication{ 
	private static final Logger log = LoggerFactory.getLogger(IntegrationApplication.class);

	@Bean
    public LocalValidatorFactoryBean validator() {
    	log.info("Creating local validator bean");
    	LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
    	ReloadableResourceBundleMessageSource messageSrc = new ReloadableResourceBundleMessageSource();
    	messageSrc.setBasename("messages");
    	localValidatorFactoryBean.setValidationMessageSource(messageSrc);
    	return localValidatorFactoryBean;
    }
    
    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener() {
    	log.info("Creating MongoDB Event Listener");
        return new ValidatingMongoEventListener(validator());
    } 
    
  	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(IntegrationApplication.class, args);

		log.info("Context contains the following beans:");
		String[] beanNames = ctx.getBeanDefinitionNames();
		Arrays.sort(beanNames);
		for (String beanName : beanNames) {
			log.info(beanName);
		}
	}
}*/

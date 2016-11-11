package gov.nyc.dsny.smart.opsboard;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.stereotype.Service;

@Configuration
@ComponentScan(basePackages = { "gov.nyc.dsny.smart.opsboard" }, excludeFilters = {
		@ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "gov.nyc.dsny.smart.opsboard..noscan..*"),
		@ComponentScan.Filter(type = FilterType.ASPECTJ, pattern = "gov.nyc.dsny.smart.opsboard.tools.*") })
@Import({ RepositoryRestMvcConfiguration.class })
@EnableAutoConfiguration
@EnableAspectJAutoProxy
public class Application { // implements CommandLineRunner

	@Value("${server.port}")
	private int serverPort;
	
	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
	    TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
	    factory.setPort(serverPort);
	    return factory;
	}
	
	@Service
	public static class DefaultCommandLineRunnerService implements CommandLineRunner {

		@Autowired
		private ApplicationContext ctx;

		@Override
		public void run(String... args) throws Exception {

			String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
			log.info("Active profiles: " + Arrays.toString(activeProfiles));
		}
	}

	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(Application.class, args);
		String[] activeProfiles = ctx.getEnvironment().getActiveProfiles();
		log.info("Active profiles: " + Arrays.toString(activeProfiles));
	}

	private static final Logger log = LoggerFactory.getLogger(Application.class);
}

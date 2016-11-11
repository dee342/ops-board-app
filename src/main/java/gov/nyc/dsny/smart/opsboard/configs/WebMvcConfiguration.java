package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.converters.BaseConverter;
import gov.nyc.dsny.smart.opsboard.domain.ApplicationSettings;
import gov.nyc.dsny.smart.opsboard.persistence.repos.ApplicationSettingsRepository;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.MessageInterpolator;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.http.HttpStatus;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MessageSourceResourceBundleLocator;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.resource.VersionResourceResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE-6)
public class WebMvcConfiguration extends WebMvcConfigurerAdapter implements ApplicationListener<ContextRefreshedEvent>{

    @Autowired
    private ApplicationSettingsRepository appSettingsRepository;
    
    @Autowired
    private WebApplicationContext webApp;
    
    @Autowired
    private Set<BaseConverter<?,?>> converters;
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
    	webApp.getServletContext().setAttribute(ApplicationSettings.APPLICATION_MODE, appSettingsRepository.findByName(ApplicationSettingsRepository.APPLICATION_MODE)!=null?appSettingsRepository.findByName(ApplicationSettingsRepository.APPLICATION_MODE).getValue():"normal");
    }
    
    private static String[] bypassAdapterPath = {"/login", "/admin/", "maintenance"};
    private static String[] inMaintenanceModePaths = {"/admin/load", "/admin/refresh"};
    private static String[] fullDataLoadPaths = {"/admin/load/all", "/admin/load/getloadstatus"};
    
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
    	registry.addInterceptor(new HandlerInterceptorAdapter() {
    		@Override
    		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    			// Allow full data load outside of maintenance mode 
    			for(String path : fullDataLoadPaths)
    			{
    				if(request.getServletPath().contains(path))
    					return true;
    			}

    			
    			// Check is system is in maintenance mode
    			boolean isInMaintenanceMode = false;
    			Object modeObj = webApp.getServletContext().getAttribute(ApplicationSettings.APPLICATION_MODE);
    			isInMaintenanceMode = !(modeObj==null||String.valueOf(modeObj).equalsIgnoreCase(ApplicationSettings.APPLICATION_MODE_NORMAL));
    			
    			// Check if path has one of the inMaintenanceModePaths, and is system is in maintenance mode, let it run
    			for(String path : inMaintenanceModePaths){
    				if(request.getServletPath().contains(path))
    				{
    					if (isInMaintenanceMode)
    						return true;
    					
    					throw new Exception("Data load can't be performed sicne system is not in the maintenance mode");
    				}
    			}
    			
    			for(String path : bypassAdapterPath){
    				if(request.getServletPath().contains(path)){
    					return true;
    				}
    			}
    			
    			if(isInMaintenanceMode){
    				response.setStatus(HttpStatus.SC_TEMPORARY_REDIRECT);
    				response.sendRedirect(request.getServletContext().getContextPath() + "/maintenance#/maintenence");
    			}
    			
    			return !isInMaintenanceMode;
    		}
		});
    	super.addInterceptors(registry);
    }
    
    private static final Logger log = LoggerFactory.getLogger(WebMvcConfiguration.class);

    @Autowired
    private Environment env;
   
    @Override
    // Views w/o designated controllers
    public void addViewControllers(ViewControllerRegistry registry) {
    	registry.addViewController("/").setViewName("selectboard");    	 
    	registry.addViewController("/selectboard").setViewName("selectboard");
    	registry.addViewController("/login").setViewName("login");
    	registry.addViewController("/maintenance").setViewName("maintenance");
    	registry.addViewController("/views/main").setViewName("views/main");
    	registry.addViewController("/views/main-displayboard").setViewName("views/main-displayboard");
        registry.addViewController("/views/modals/fragments/modal-client-errors").setViewName("views/modals/fragments/modal-client-errors");        
    	registry.addViewController("/views/modals/fragments/modal-footer").setViewName("views/modals/fragments/modal-footer");
    	registry.addViewController("/views/modals/fragments/modal-footer-cancel").setViewName("views/modals/fragments/modal-footer-cancel");
        registry.addViewController("/views/modals/fragments/modal-header").setViewName("views/modals/fragments/modal-header");
        registry.addViewController("/views/modals/fragments/modal-processing").setViewName("views/modals/fragments/modal-processing");
        registry.addViewController("/views/modals/fragments/modal-server-errors").setViewName("views/modals/fragments/modal-server-errors");
        registry.addViewController("/views/modals/fragments/modal-exceptions").setViewName("views/modals/fragments/modal-exceptions");
        registry.addViewController("/views/modals/modal-credits").setViewName("views/modal-credits"); 
        registry.addViewController("/views/modals/modal-equipment-attachment").setViewName("views/modals/modal-equipment-attachment");
        registry.addViewController("/views/modals/modal-equipment-cancel-detachment").setViewName("views/modals/modal-equipment-cancel-detachment");
        registry.addViewController("/views/modals/modal-equipment-detachment").setViewName("views/modals/modal-equipment-detachment");
    	registry.addViewController("/views/modals/modal-equipment-down").setViewName("views/modals/modal-equipment-down");
    	registry.addViewController("/views/modals/modal-equipment-up").setViewName("views/modals/modal-equipment-up");
    	registry.addViewController("/views/modals/modal-equipment-update-readiness").setViewName("views/modals/modal-equipment-update-readiness");
    	registry.addViewController("/views/modals/modal-plow-removal").setViewName("views/modals/modal-plow-removal");
    	registry.addViewController("/views/modals/modal-load-status").setViewName("views/modals/modal-load-status");
        registry.addViewController("/views/modals/modal-mda-status").setViewName("views/modals/modal-mda-status");
        registry.addViewController("/views/modals/modal-add-special-positions").setViewName("views/modals/modal-add-special-positions");
        registry.addViewController("/views/modals/modal-personnel-detachment").setViewName("views/modals/modal-personnel-detachment");
        registry.addViewController("/views/modals/modal-personnel-edit-detachment").setViewName("views/modals/modal-personnel-edit-detachment");
        registry.addViewController("/views/modals/modal-personnel-unavail-status").setViewName("views/modals/modal-personnel-unavail-status");
        registry.addViewController("/views/modals/modal-personnel-remove-unavail-status").setViewName("views/modals/modal-personnel-remove-unavail-status");
        registry.addViewController("/views/modals/modal-personnel-cancel-unavail-status").setViewName("views/modals/modal-personnel-cancel-unavail-status");
        registry.addViewController("/views/modals/modal-personnel-reverse-cancel-unavail-status").setViewName("views/modals/modal-personnel-reverse-cancel-unavail-status");
        registry.addViewController("/views/modals/modal-task-assignment-type").setViewName("views/modals/modal-task-assignment-type");
        registry.addViewController("/views/modals/modal-release-notes").setViewName("views/modals/modal-release-notes");
        registry.addViewController("/views/modals/modal-add-shift").setViewName("views/modals/modal-add-shift");
        registry.addViewController("/views/modals/modal-delete-confirm").setViewName("views/modals/modal-delete-confirm");
        registry.addViewController("/views/modals/modal-edit-partial-tasks").setViewName("views/modals/modal-edit-partial-tasks");
        registry.addViewController("/views/fragments/modal-server-errors").setViewName("views/fragments/modal-server-errors");
        registry.addViewController("/views/modals/modal-copy-board").setViewName("views/modals/modal-copy-board");
        registry.addViewController("/views/modals/modal-reload-board").setViewName("views/modals/modal-reload-board");
        registry.addViewController("/views/modals/modal-edit-task").setViewName("views/modals/modal-edit-task");
        registry.addViewController("/views/modals/modal-category-switches").setViewName("views/modals/modal-category-switches");
        registry.addViewController("/views/modals/modal-mass-chart-updates").setViewName("views/modals/modal-mass-chart-updates");
        registry.addViewController("/views/fragments/recent-activity-slide-out-pane").setViewName("views/fragments/recent-activity-slide-out-pane");
        registry.addViewController("/views/fragments/task-item").setViewName("views/fragments/task-item");
		registry.addViewController("/views/fragments/task-item-district").setViewName("views/fragments/task-item-district");
        registry.addViewController("/views/fragments/charts-pane").setViewName("views/fragments/charts-pane");
        registry.addViewController("/views/modals/modal-add-volunteer-counts").setViewName("views/modals/modal-add-volunteer-counts");
        
    }
    
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new AfterburnerModule());
        objectMapper.registerModule(new Hibernate4Module());
       // objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        converter.setObjectMapper(objectMapper);
        converters.add(converter);
        super.configureMessageConverters(converters);
    }  

    
    @Bean
    public ServletRegistrationBean cxfServlet() {
        ServletRegistrationBean cxfServlet = new ServletRegistrationBean(new CXFServlet(), "/services/*");
        return cxfServlet;
    }
    
    @Bean
    public MessageSource messageSource(){    	
    	ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    	messageSource.setBasename("classpath:ValidationMessages");    	
    	return messageSource;
    }
    
    @Bean
    public ResourceBundleLocator resourceBundleLocator(){
    	return new MessageSourceResourceBundleLocator(messageSource());
    }

    @Bean
    public MessageInterpolator messageInterpolator(){
//    	return new CustomMessageInterpolator(resourceBundleLocator());
    	return new ResourceBundleMessageInterpolator(resourceBundleLocator());
    }
    
	@Bean
    public LocalValidatorFactoryBean validator() {
    	LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();        	
    	localValidatorFactoryBean.setMessageInterpolator(messageInterpolator());    	   
    	return localValidatorFactoryBean;
    }
    
    @Bean 
    public MethodValidationPostProcessor methodValidationPostProcessor(){
    	MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
    	methodValidationPostProcessor.setValidator(validator());
    	return methodValidationPostProcessor;
    }
    
    @Override
    public Validator getValidator() {
    	return validator();
    }
    
    @Override
    public void addFormatters(FormatterRegistry registry){
    	converters.forEach(c -> registry.addConverter(c));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	
        int cachePeriod =Integer.parseInt(env.getProperty("spring.resources.cache-period"));
      
    	log.info("spring.resources.cache-period="+cachePeriod);
    	
    	if (cachePeriod<=0){
            log.info("Static content caching is disabled by 'spring.resources.cache-period' parameter. Static content versioning is DISABLED");
        	return;
        }
        
    	log.info("Static content versioning is ENABLED");

    	registry.addResourceHandler("/libs/**", "/scripts/**", "/styles/**")
                .addResourceLocations("classpath:/WEB-INF/classes/public/libs/")
                .addResourceLocations("classpath:/WEB-INF/classes/public/scripts/")
                .addResourceLocations("classpath:/WEB-INF/classes/public/styles/")
                .addResourceLocations("classpath:/public/libs/")
                .addResourceLocations("classpath:/public/scripts/")
                .addResourceLocations("classpath:/public/styles/")
                .setCachePeriod(cachePeriod)
                .resourceChain(true)
                .addResolver(
                        new VersionResourceResolver()
                                .addContentVersionStrategy("/**"));
    }
}
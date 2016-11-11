package gov.nyc.dsny.smart.opsboard.misc;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
 
public class ApplicationContextProvider implements ApplicationContextAware {

public void setApplicationContext(ApplicationContext ctx) throws BeansException {
   AppContext.setApplicationContext(ctx);
   }
}
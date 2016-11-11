package gov.nyc.dsny.smart.opsboard.configs;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Quartz Job Factory which instantiates job and autowires in any dependencies
 */

public final class QuartzAutowiringSpringBeanJobFactory extends SpringBeanJobFactory implements ApplicationContextAware {


	private transient AutowireCapableBeanFactory beanFactory;

	@Override
	protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {
		final Object job = super.createJobInstance(bundle);
		this.beanFactory.autowireBean(job);
		return job;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.beanFactory = applicationContext.getAutowireCapableBeanFactory();
	}
}
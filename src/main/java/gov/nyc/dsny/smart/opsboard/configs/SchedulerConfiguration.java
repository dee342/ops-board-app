package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.jobs.DashboardJob;
import gov.nyc.dsny.smart.opsboard.jobs.TaskCompletionJob;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

@Configuration
public class SchedulerConfiguration {
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private DataSource dataSource;

	@Value("#{ environment['quartz.driverDelegateClass'] }")
	private String driverDelegateClassName;
	
	@Value("#{ environment['quartz.cron.expression.equipment.checker.interval'] }")
	private String equipmentCheckerInterval;
	
	@Value("#{ environment['quartz.cron.expression.dashboard.checker.interval'] }")
	private String dashboardInterval;
	
	@Value("#{ environment['quartz.cron.expression.cache.eviction.interval'] }")
	private String cacheEvictionInterval;

	/**
	 * Creates a Quartz based scheduler bean. Any job with Autowired dependencies should get the
	 * dependencies injected in cleanly.
	 * @return the Scheduler
	 */
	@Bean
	public SchedulerFactoryBean scheduler() {
		SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
		scheduler.setJobFactory(springBeanJobFactory());
		scheduler.setDataSource(dataSource);
		Properties quartzProps = new Properties();
		quartzProps.setProperty("org.quartz.jobStore.isClustered", "true");
		quartzProps.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
		quartzProps.setProperty("org.quartz.jobStore.driverDelegateClass", driverDelegateClassName);
		scheduler.setQuartzProperties(quartzProps);
		scheduler.setTriggers(equipmentLoadStatusTrigger().getObject(), dashboardJobTrigger().getObject());
		scheduler.setOverwriteExistingJobs(true);

		return scheduler;
	}
	
	@Bean
	public JobDetailFactoryBean equipmentLoadStatusJob() {
		JobDetailFactoryBean equipmentLoadStatusJob = new JobDetailFactoryBean();
		equipmentLoadStatusJob.setJobClass(TaskCompletionJob.class);
		Map<String, Object> jobDataMap = new HashMap<>();
		equipmentLoadStatusJob.setJobDataAsMap(jobDataMap);
		equipmentLoadStatusJob.setDurability(true);
		return equipmentLoadStatusJob;
	}
	
	@Bean
	public CronTriggerFactoryBean equipmentLoadStatusTrigger() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setCronExpression(equipmentCheckerInterval);
		cronTriggerFactoryBean.setJobDetail(equipmentLoadStatusJob().getObject());
		return cronTriggerFactoryBean;
	}

	/**
	 * This is the special quartz {@link org.quartz.spi.JobFactory} which
	 * returns the Job instance as well as autowires any dependencies
	 *
	 * @return job factory
	 */
	@Bean
	public SpringBeanJobFactory springBeanJobFactory() {
		return new QuartzAutowiringSpringBeanJobFactory();
	}
	
	@Bean
	public JobDetailFactoryBean dashboardJob() {
		JobDetailFactoryBean dashboardJob = new JobDetailFactoryBean();
		dashboardJob.setJobClass(DashboardJob.class);
		Map<String, Object> jobDataMap = new HashMap<>();
		dashboardJob.setJobDataAsMap(jobDataMap);
		dashboardJob.setDurability(true);
		return dashboardJob;
	}
	
	@Bean
	public CronTriggerFactoryBean dashboardJobTrigger() {
		CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
		cronTriggerFactoryBean.setCronExpression(dashboardInterval);
		cronTriggerFactoryBean.setJobDetail(dashboardJob().getObject());
		return cronTriggerFactoryBean;
	}
}

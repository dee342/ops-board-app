/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.configs;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.jolbox.bonecp.BoneCPDataSource;

/**
 * @author nasangameshwaran
 *
 */
@Configuration
public class ConnectionPoolConfiguration {
	
	private static final long DEFAULT_CONNECTION_IDLE_MAX_AGE_SECONDS = 300;
	private static final long DEFAULT_IDLE_CONNECTION_TEST_PERIOD_SECONDS = 60;
	private static final int DEFAULT_PARTITION_COUNT = 3;
	private static final int DEFAULT_ACQUIRE_INCREMENT = 10;
	private static final int DEFAULT_MAX_CONNECTIONS_PER_PARTITION = 100;
	private static final int DEFAULT_MIN_CONNECTIONS_PER_PARTITION = 20;
	private static final int DEFAULT_POOL_AVAILABILITY_THRESHOLD = 20;
	private static final int DEFAULT_STATEMENTS_CACHE_SIZE = 50;
	
	@Autowired
	private Environment environment;
	
	@Value("#{ environment['spring.datasource.driverClassName'] }")
	private String driverClassName;
	
	@Value("#{ environment['spring.datasource.url'] }")
	private String url;
	
	@Value("#{ environment['spring.datasource.username'] }")
	private String username;
	
	@Value("#{ environment['spring.datasource.password'] }")
	private String password;
	
	@Value("#{ environment['spring.cp.idleMaxAge'] }")
	private Long idleMaxAge;
	
	@Value("#{ environment['spring.cp.idleConnectionTestPeriod'] }")
	private Long idleConnectionTestPeriod;
	
	@Value("#{ environment['spring.cp.partitionCount'] }")
	private Integer partitionCount;
	
	@Value("#{ environment['spring.cp.acquireIncrement'] }")
	private Integer acquireIncrement;
	
	@Value("#{ environment['spring.cp.maxConnectionsPerPartition'] }")
	private Integer maxConnectionsPerPartition;
	
	@Value("#{ environment['spring.cp.minConnectionsPerPartition'] }")
	private Integer minConnectionsPerPartition;
	
	@Value("#{ environment['spring.cp.poolAvailabilityThreshold'] }")
	private Integer poolAvailabilityThreshold;
	
	@Value("#{ environment['spring.cp.statementsCacheSize'] }")
	private Integer statementsCacheSize;
	
	@Bean
	public DataSource dataSource(){
		BoneCPDataSource retval = new BoneCPDataSource();
		retval.setDriverClass(this.driverClassName);
		retval.setJdbcUrl(this.url);
		retval.setUsername(this.username);
		retval.setPassword(this.password);
		retval.setIdleMaxAgeInSeconds(idleMaxAge != null && idleMaxAge > 0 ? idleMaxAge : DEFAULT_CONNECTION_IDLE_MAX_AGE_SECONDS);
		retval.setIdleConnectionTestPeriodInSeconds(idleConnectionTestPeriod != null && idleConnectionTestPeriod > 0 ? idleConnectionTestPeriod : DEFAULT_IDLE_CONNECTION_TEST_PERIOD_SECONDS);
		retval.setPartitionCount(partitionCount != null && partitionCount > 0 ? partitionCount : DEFAULT_PARTITION_COUNT);
		retval.setAcquireIncrement(acquireIncrement != null && acquireIncrement > 0 ? acquireIncrement : DEFAULT_ACQUIRE_INCREMENT);
		retval.setMaxConnectionsPerPartition(maxConnectionsPerPartition != null && maxConnectionsPerPartition > 0 ? maxConnectionsPerPartition : DEFAULT_MAX_CONNECTIONS_PER_PARTITION);
		retval.setMinConnectionsPerPartition(minConnectionsPerPartition != null && minConnectionsPerPartition > 0 ? minConnectionsPerPartition : DEFAULT_MIN_CONNECTIONS_PER_PARTITION);
		retval.setPoolAvailabilityThreshold(poolAvailabilityThreshold != null && poolAvailabilityThreshold > 0 ? poolAvailabilityThreshold : DEFAULT_POOL_AVAILABILITY_THRESHOLD);
		retval.setStatementsCacheSize(statementsCacheSize != null && statementsCacheSize > 0 ? statementsCacheSize : DEFAULT_STATEMENTS_CACHE_SIZE);
		
		return retval;
	}
}

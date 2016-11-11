package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.domain.equipment.Series;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.RegionAttributes;

@Configuration
public class GemfireEquipmentRegionConfiguration {

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean seriesRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(Series.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, Series> Series(GemFireCache cache, @Qualifier("seriesRegionAttributes") RegionAttributes<Long, Series> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, Series> seriesRegion = new LocalRegionFactoryBean<Long, Series>();
		seriesRegion.setCache(cache);
		seriesRegion.setAttributes(regionAttributes);
		return seriesRegion;
	}	
}

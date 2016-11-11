package gov.nyc.dsny.smart.opsboard.configs;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.RegionAttributes;

@Configuration
public class GemfireFunctionalRegionConfiguration {

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean inverseIndexRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(String.class);
		attribute.setEntryTimeToLive(new ExpirationAttributes((int)(TimeUnit.DAYS.toSeconds(3L))));
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	/**
	 * This region is designed to use as inverted index from Gemfire nested object to retrieve back its containing object.
	 */
	@Autowired
	@Bean
	public LocalRegionFactoryBean<String, Set<String>> InverseRegion(GemFireCache cache, @Qualifier("inverseIndexRegionAttributes") RegionAttributes<String, Set<String>> regionAttributes) throws Exception{
		LocalRegionFactoryBean<String, Set<String>> inverseIndexRegin = new LocalRegionFactoryBean<String, Set<String>>();
		inverseIndexRegin.setCache(cache);
		inverseIndexRegin.setAttributes(regionAttributes);
		return inverseIndexRegin;
	}
	
	@Bean(name="inverseIndexGFTemplate")
	public GemfireTemplate gemfireTemplate(GemFireCache cache, @Qualifier("inverseIndexRegionAttributes") RegionAttributes<String, Set<String>> regionAttributes)throws Exception{
		GemfireTemplate gfTemplate = new GemfireTemplate(InverseRegion(cache, regionAttributes).getObject());
		return gfTemplate;
	}
}

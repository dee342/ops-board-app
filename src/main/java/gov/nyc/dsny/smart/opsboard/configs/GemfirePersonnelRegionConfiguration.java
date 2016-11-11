package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.MdaType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.OfficerPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.SpecialPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.UnavailabilityType;

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
public class GemfirePersonnelRegionConfiguration {
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean mdaTypeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(MdaType.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, MdaType> MdaType(GemFireCache cache, @Qualifier("mdaTypeRegionAttributes") RegionAttributes<Long, MdaType> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, MdaType> mdaTypeRegion = new LocalRegionFactoryBean<Long, MdaType>();
		mdaTypeRegion.setCache(cache);
		mdaTypeRegion.setAttributes(regionAttributes);
		return mdaTypeRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean officerRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(OfficerPositionType.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, OfficerPositionType> OfficerPositionType(GemFireCache cache, @Qualifier("officerRegionAttributes") RegionAttributes<Long, OfficerPositionType> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, OfficerPositionType> officerPositionTypeRegion = new LocalRegionFactoryBean<Long, OfficerPositionType>();
		officerPositionTypeRegion.setCache(cache);
		officerPositionTypeRegion.setAttributes(regionAttributes);
		return officerPositionTypeRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean unavailabilityTypeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(UnavailabilityType.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, UnavailabilityType> UnavailabilityType(GemFireCache cache,  @Qualifier("unavailabilityTypeRegionAttributes") RegionAttributes<Long, UnavailabilityType> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, UnavailabilityType> unavailabilityTypeRegion = new LocalRegionFactoryBean<Long, UnavailabilityType>();
		unavailabilityTypeRegion.setCache(cache);
		unavailabilityTypeRegion.setAttributes(regionAttributes);
		return unavailabilityTypeRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean specialPositionTypeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(SpecialPositionType.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, SpecialPositionType> SpecialPositionType(GemFireCache cache, @Qualifier("specialPositionTypeRegionAttributes") RegionAttributes<Long, SpecialPositionType> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, SpecialPositionType> specialPositionTypeRegion = new LocalRegionFactoryBean<Long, SpecialPositionType>();
		specialPositionTypeRegion.setCache(cache);
		specialPositionTypeRegion.setAttributes(regionAttributes);
		return specialPositionTypeRegion;
	}
	
}

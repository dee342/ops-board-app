package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.DownCode;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.MaterialType;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.SubType;
import gov.nyc.dsny.smart.opsboard.domain.reference.BoardType;

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
public class GemfireReferenceDataRegionConfiguration {

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean boardTypeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(BoardType.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, BoardType> BoardType(GemFireCache cache, @Qualifier("boardTypeRegionAttributes") RegionAttributes<Long, BoardType> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, BoardType> boardTypeRegion = new LocalRegionFactoryBean<Long, BoardType>();
		boardTypeRegion.setCache(cache);
		boardTypeRegion.setAttributes(regionAttributes);
		return boardTypeRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean subTypeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(SubType.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, SubType> SubType(GemFireCache cache, @Qualifier("subTypeRegionAttributes") RegionAttributes<Long, SubType> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, SubType> subTypeRegion = new LocalRegionFactoryBean<Long, SubType>();
		subTypeRegion.setCache(cache);
		subTypeRegion.setAttributes(regionAttributes);
		return subTypeRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean downCodeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(DownCode.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, DownCode> DownCode(GemFireCache cache, @Qualifier("downCodeRegionAttributes") RegionAttributes<Long, DownCode> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, DownCode> downCodeRegion = new LocalRegionFactoryBean<Long, DownCode>();
		downCodeRegion.setCache(cache);
		downCodeRegion.setAttributes(regionAttributes);
		return downCodeRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean materialTypeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(MaterialType.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, MaterialType> MaterialType(GemFireCache cache, @Qualifier("materialTypeRegionAttributes") RegionAttributes<Long, MaterialType> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, MaterialType> materialTypeRegion = new LocalRegionFactoryBean<Long, MaterialType>();
		materialTypeRegion.setCache(cache);
		materialTypeRegion.setAttributes(regionAttributes);
		return materialTypeRegion;
	}
}

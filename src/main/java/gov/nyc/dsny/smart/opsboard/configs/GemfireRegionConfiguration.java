package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.cache.gf.domain.CategoryBoardTypeGFGroup;
import gov.nyc.dsny.smart.opsboard.cache.gf.domain.CategoryBoardTypeGFKey;
import gov.nyc.dsny.smart.opsboard.cache.gf.domain.CategoryLocationGFGroup;
import gov.nyc.dsny.smart.opsboard.cache.gf.domain.CategoryLocationGFKey;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.RegionAttributes;

@Configuration
public class GemfireRegionConfiguration {
	
	@Lazy
	@Autowired
	private CacheLoader<Long, Category> categoryLoader;
	
	@Lazy
	@Autowired
	private CacheLoader<Long, Subcategory> subCategoryLoader;
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean categoryBoardTypeRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(CategoryBoardTypeGFKey.class);
		attribute.setValueConstraint(CategoryBoardTypeGFGroup.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<CategoryBoardTypeGFKey, CategoryBoardTypeGFGroup> CategoryBoardTypeGFGroup(GemFireCache cache, @Qualifier("categoryBoardTypeRegionAttributes") RegionAttributes<CategoryBoardTypeGFKey, CategoryBoardTypeGFGroup> regionAttributes) throws Exception{
		LocalRegionFactoryBean<CategoryBoardTypeGFKey, CategoryBoardTypeGFGroup> categoryBoardTypeRegion = new LocalRegionFactoryBean<CategoryBoardTypeGFKey, CategoryBoardTypeGFGroup>();
		categoryBoardTypeRegion.setCache(cache);
		categoryBoardTypeRegion.setAttributes(regionAttributes);
		return categoryBoardTypeRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean categoryLocationRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(CategoryLocationGFKey.class);
		attribute.setValueConstraint(CategoryLocationGFGroup.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<CategoryLocationGFKey, CategoryLocationGFGroup> CategoryLocationGFGroup(GemFireCache cache, @Qualifier("categoryLocationRegionAttributes") RegionAttributes<CategoryLocationGFKey, CategoryLocationGFGroup> regionAttributes) throws Exception{
		LocalRegionFactoryBean<CategoryLocationGFKey, CategoryLocationGFGroup> categoryLocationRegion = new LocalRegionFactoryBean<CategoryLocationGFKey, CategoryLocationGFGroup>();
		categoryLocationRegion.setCache(cache);
		categoryLocationRegion.setAttributes(regionAttributes);
		return categoryLocationRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean categoryRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(Category.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, Category> Category(GemFireCache cache, @Qualifier("categoryRegionAttributes") RegionAttributes<Long, Category> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, Category> categoryRegion = new LocalRegionFactoryBean<Long, Category>();
		categoryRegion.setCache(cache);
		categoryRegion.setAttributes(regionAttributes);
		categoryRegion.setCacheLoader(categoryLoader);
		return categoryRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean subcategoryRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(Subcategory.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, Subcategory> Subcategory(GemFireCache cache, @Qualifier("subcategoryRegionAttributes") RegionAttributes<Long, Subcategory> regionAttributes) throws Exception{
		LocalRegionFactoryBean<Long, Subcategory> subCategoryRegion = new LocalRegionFactoryBean<Long, Subcategory>();
		subCategoryRegion.setCache(cache);
		subCategoryRegion.setAttributes(regionAttributes);
		subCategoryRegion.setCacheLoader(subCategoryLoader);
		return subCategoryRegion;
	}
}

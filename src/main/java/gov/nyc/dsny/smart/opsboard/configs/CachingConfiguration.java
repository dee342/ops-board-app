package gov.nyc.dsny.smart.opsboard.configs;

import gov.nyc.dsny.smart.opsboard.cache.gf.domain.BoardEquipmentGFKeySet;
import gov.nyc.dsny.smart.opsboard.cache.gf.domain.BoardPersonGFKeySet;
import gov.nyc.dsny.smart.opsboard.cache.gf.domain.RefDataTrackingKey;
import gov.nyc.dsny.smart.opsboard.cache.gf.domain.RefDataTrackingValue;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.repository.BoardGFRepository;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.GemfireAdminService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.InverseIndexCacheService;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.admin.CommandAdminLowMemory;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardQuota;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardQuotaId;
import gov.nyc.dsny.smart.opsboard.domain.board.Quota;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.personnel.Person;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit;
import gov.nyc.dsny.smart.opsboard.services.PersistanceService;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.util.Utils;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.gemfire.CacheFactoryBean;
import org.springframework.data.gemfire.LocalRegionFactoryBean;
import org.springframework.data.gemfire.RegionAttributesFactoryBean;
import org.springframework.data.gemfire.repository.config.EnableGemfireRepositories;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.gemstone.gemfire.cache.CacheListener;
import com.gemstone.gemfire.cache.CacheLoader;
import com.gemstone.gemfire.cache.EntryEvent;
import com.gemstone.gemfire.cache.EvictionAction;
import com.gemstone.gemfire.cache.EvictionAttributes;
import com.gemstone.gemfire.cache.ExpirationAction;
import com.gemstone.gemfire.cache.ExpirationAttributes;
import com.gemstone.gemfire.cache.GemFireCache;
import com.gemstone.gemfire.cache.RegionAttributes;
import com.gemstone.gemfire.cache.util.CacheListenerAdapter;
import com.gemstone.gemfire.cache.util.ObjectSizer;

@Configuration
@EnableGemfireRepositories("gov.nyc.dsny.smart.opsboard.cache.gf.repository")
public class CachingConfiguration{ 

	private static final Logger log = LoggerFactory.getLogger(CachingConfiguration.class);

	@Lazy
	@Autowired
	private PersistanceService persistanceService;

	@Lazy
	@Autowired
	private CacheLoader<String, BoardPerson> boardPersonLoader;
	
	@Lazy
	@Autowired
	private CacheLoader<String, BoardEquipment> boardEquipmentLoader;

	@Lazy
	@Autowired
	private CacheLoader<String, Equipment> equipmentLoader;

	@Lazy
	@Autowired
	private CacheLoader<String, Person> personLoader;

	@Lazy
	@Autowired
	private CacheLoader<Long, Quota> quotaLoader;

	@Lazy
	@Autowired
	private CacheLoader<Long, Shift> shiftLoader;
	
	@Lazy
	@Autowired
	private CacheLoader<Long, Location> locationLoader;

	@Lazy
	@Autowired
	private GemfireAdminService gfAdminService;
	
	@Value("#{ environment['gemfire.statistic.enabled'] }")
	private Boolean enableStatistic;
	
	@Value("#{ environment['gemfire.statistic-archive-file'] }")
	private String statFileName;
	
	@Value("#{ environment['gemfire.jmx-manager'] }")
	private Boolean isJmxManager;
	
	@Value("#{ environment['gemfire.jmx-manager-start'] }")
	private Boolean startJmxManager;
	
	@Value("#{ environment['gemfire.mcast-port'] }")
	private String port;
	
	@Value("#{ environment['gemfire.log.level'] }")
	private String logLevel;
	
	@Value("#{ environment['gemfire.name'] }")
	private String cacheName;
	
	@Value("#{environment['tmp']}")
	private String tempDir;
	
	@Value("#{environment['gemfire.archive-file-size-limit']}")
	private String fileSizeLimit;
	
	@Value("#{environment['gemfire.archive-disk-space-limit']}")
	private String diskSpaceLimit;
	
	@Value("#{environment['gemfire.entry-idle-time']}")
	private int entryIdleTime;
	
	@Value("#{environment['gemfire.entry-time-to-live']}")
	private int entryTtlTime;
	
	@Autowired
	private SimpMessagingTemplate messenger;

	@Lazy
	@Autowired
	private InverseIndexCacheService inverseIndexCacheService;
	
	@Lazy
	@Autowired
	private BoardGFRepository boardGFRepository;
	
	@Value("#{environment['gemfire.board.eviction.in.bulk']}")
	private int numberToEvictInBulk;
	
	@Bean
	public Properties gemfireProperties() {
		Properties props = new Properties();
		props.setProperty("jmx-manager", String.valueOf(isJmxManager));
		props.setProperty("jmx-manager-start", String.valueOf(startJmxManager));
		props.setProperty("mcast-port", String.valueOf(port));
		props.setProperty("name", cacheName);
		props.setProperty("log-level", logLevel);
		props.setProperty("archive-file-size-limit", fileSizeLimit);
		props.setProperty("archive-disk-space-limit", diskSpaceLimit);
		if(enableStatistic){
			props.setProperty("statistic-sampling-enabled", String.valueOf(enableStatistic));
			props.setProperty("statistic-archive-file", tempDir + File.separator + statFileName);
		}
		return props;
	}

	@Bean
	public CacheFactoryBean gemfireCache() throws Exception {
		CacheFactoryBean cfb = new CacheFactoryBean();
		cfb.setEvictionHeapPercentage(80f);
		cfb.setCriticalHeapPercentage(99f);
		cfb.setCopyOnRead(false);
		cfb.setClose(false);
		cfb.setProperties(gemfireProperties());
		return cfb;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Bean
	public CacheListener cacheListener(){
		CacheListener cacheListener = new CacheListenerAdapter() {
			@Override
			public void afterDestroy(EntryEvent event) {
				log.debug(String.format("ID: %s from region %s is being destroyed/evicted", event.getKey(), event.getRegion().getName()));
				super.afterDestroy(event);
			}
		};
		return cacheListener;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes"})
	@Bean
	public RegionAttributesFactoryBean personRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(String.class);
		attribute.setValueConstraint(Person.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<String, Person> Person(GemFireCache cache, @Qualifier("personRegionAttributes") RegionAttributes<String, Person> regionAttributes) throws Exception {
		LocalRegionFactoryBean<String, Person> personRegion = new LocalRegionFactoryBean<String, Person>();
		personRegion.setCache(cache);
		personRegion.setAttributes(regionAttributes);
//		personRegion.setCacheLoader(personLoader);
		return personRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean boardPersonRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(String.class);
		attribute.setValueConstraint(BoardPerson.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<String, BoardPerson> BoardPerson(GemFireCache cache, @Qualifier("boardPersonRegionAttributes") RegionAttributes<String, BoardPerson> regionAttributes)
			throws Exception {
		LocalRegionFactoryBean<String, BoardPerson> boardPersonRegion = new LocalRegionFactoryBean<String, BoardPerson>();
		boardPersonRegion.setCache(cache);
		boardPersonRegion.setAttributes(regionAttributes);
		boardPersonRegion.setCacheLoader(boardPersonLoader);
		return boardPersonRegion;
	}

	@Bean
	public LocalRegionFactoryBean<BoardKey, BoardPersonGFKeySet> BoardPersonGFKeySet(GemFireCache cache)
			throws Exception {
		LocalRegionFactoryBean<BoardKey, BoardPersonGFKeySet> boardPersonKeyRegion = new LocalRegionFactoryBean<BoardKey, BoardPersonGFKeySet>();
		boardPersonKeyRegion.setCache(cache);
		return boardPersonKeyRegion;
	}

	@Bean
	public LocalRegionFactoryBean<BoardKey, BoardEquipmentGFKeySet> BoardEquipmentGFKeySet(GemFireCache cache)
			throws Exception {
		LocalRegionFactoryBean<BoardKey, BoardEquipmentGFKeySet> boardEquipmentKeyRegion = new LocalRegionFactoryBean<BoardKey, BoardEquipmentGFKeySet>();
		boardEquipmentKeyRegion.setCache(cache);
		return boardEquipmentKeyRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean equipmentRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(String.class);
		attribute.setValueConstraint(Equipment.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<String, Equipment> Equipment(GemFireCache cache, @Qualifier("equipmentRegionAttributes") RegionAttributes<String, Equipment> regionAttributes)
			throws Exception {
		LocalRegionFactoryBean<String, Equipment> equipmentRegion = new LocalRegionFactoryBean<String, Equipment>();
		equipmentRegion.setCache(cache);
		equipmentRegion.setAttributes(regionAttributes);
		equipmentRegion.setCacheLoader(equipmentLoader);
		return equipmentRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean workUnitRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(WorkUnit.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, WorkUnit> WorkUnit(GemFireCache cache, @Qualifier("workUnitRegionAttributes") RegionAttributes<Long, WorkUnit> regionAttributes) throws Exception {
		LocalRegionFactoryBean<Long, WorkUnit> workUnitRegion = new LocalRegionFactoryBean<Long, WorkUnit>();
		workUnitRegion.setCache(cache);
		workUnitRegion.setAttributes(regionAttributes);
		return workUnitRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean locationRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(Location.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, Location> Location(GemFireCache cache, @Qualifier("locationRegionAttributes") RegionAttributes<Long, Location> regionAttributes) throws Exception {
		LocalRegionFactoryBean<Long, Location> locationRegion = new LocalRegionFactoryBean<Long, Location>();
		locationRegion.setCache(cache);
		locationRegion.setAttributes(regionAttributes);
		locationRegion.setCacheLoader(locationLoader);
		return locationRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean boardEquipmentRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(String.class);
		attribute.setValueConstraint(BoardEquipment.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}

	@Autowired
	@Bean
	public LocalRegionFactoryBean<String, BoardEquipment> BoardEquipment(GemFireCache cache, @Qualifier("boardEquipmentRegionAttributes") RegionAttributes<String, BoardEquipment> regionAttributes)
			throws Exception {
		LocalRegionFactoryBean<String, BoardEquipment> boardEquipmentRegion = new LocalRegionFactoryBean<String, BoardEquipment>();
		boardEquipmentRegion.setCache(cache);
		boardEquipmentRegion.setAttributes(regionAttributes);
	//	boardEquipmentRegion.setCacheLoader(boardEquipmentLoader);
		return boardEquipmentRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean shiftRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(Shift.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, Shift> Shift(GemFireCache cache, @Qualifier("shiftRegionAttributes") RegionAttributes<Long, Shift> regionAttributes) throws Exception {
		LocalRegionFactoryBean<Long, Shift> shiftRegion = new LocalRegionFactoryBean<Long, Shift>();
		shiftRegion.setCache(cache);
		shiftRegion.setAttributes(regionAttributes);
		shiftRegion.setCacheLoader(shiftLoader);
		return shiftRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean boardContainerRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(String.class);
		attribute.setValueConstraint(BoardContainer.class);
		ExpirationAttributes idleDestroy =new ExpirationAttributes(entryIdleTime, ExpirationAction.LOCAL_DESTROY);
		ExpirationAttributes ttlDestory =new ExpirationAttributes(entryTtlTime, ExpirationAction.LOCAL_DESTROY);
		attribute.setEvictionAttributes(EvictionAttributes.createLRUHeapAttributes(ObjectSizer.DEFAULT, EvictionAction.LOCAL_DESTROY));
		attribute.setEntryIdleTimeout(idleDestroy);
		attribute.setEntryTimeToLive(ttlDestory);
		attribute.addCacheListener(new CacheListenerAdapter<String, BoardContainer>() {
		
		@Override
		public void afterCreate(EntryEvent<String, BoardContainer> event) {
			inverseIndexing(event.getNewValue());
			super.afterCreate(event);
		}
		
		@Override
		public void afterUpdate(EntryEvent<String, BoardContainer> event) {
			inverseIndexing(event.getNewValue());
			super.afterUpdate(event);
		}
		Function<String, Boolean> isOutOfThreeDateRange =(d) -> {
			LocalDate today  = LocalDateTime.now().toLocalDate();
			LocalDate tomorrow = today.plusDays(1);
			LocalDate yesterday = today.minusDays(1);
			
			LocalDate boardDate = LocalDateTime.ofInstant(DateUtils.toBoardDate(d).toInstant(), ZoneId.systemDefault()).toLocalDate();
			boolean needToRemove=true;
			if(boardDate.isEqual(today) || boardDate.isEqual(tomorrow) || boardDate.isEqual(yesterday)){
				needToRemove=false;
			}
			return needToRemove;	
		};
			
		@Override
		public void afterDestroy(EntryEvent<String, gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer> event) {
				BoardContainer boardContainer = event.getOldValue();
				boolean manualDelete = boardContainer.isMarkForDelete();
				if(manualDelete|| isOutOfThreeDateRange.apply(boardContainer.getBoard().getDate())){
					gfAdminService.deleteBoard(boardContainer);
					super.afterDestroy(event);
					if(!manualDelete){
						notifyBoardEviction(boardContainer);
					}
					persistanceService.deleteListener(boardContainer.getBoard().getKey());
				}else{
					List<BoardContainer> boardContainers = boardGFRepository.findAll();
					
					Set<BoardContainer> boardContainersToBeRemoved = boardContainers.stream().filter(b -> {
						return isOutOfThreeDateRange.apply(b.getBoard().getDate());
					}).sorted((x, y) -> x.getBoard().getDate().compareTo(y.getBoard().getDate())).limit(numberToEvictInBulk).collect(Collectors.toSet());
					
					boardContainersToBeRemoved.add(boardContainer);
					notifyBoardEviction(boardContainersToBeRemoved);
				}
			}
		
		private void notifyBoardEviction(BoardContainer bc){
			Set<BoardContainer> bcSet = new HashSet<BoardContainer>();
			bcSet.add(bc);
			notifyBoardEviction(bcSet);
		}

		private void notifyBoardEviction(
				Set<BoardContainer> boardContainersToBeRemoved) {
			for(BoardContainer bc : boardContainersToBeRemoved){
				gfAdminService.deleteBoard(bc);
				
				CommandAdminLowMemory lowMemoryCommand = new CommandAdminLowMemory();
				CommandMessage message = new CommandMessage();
				message.setCommandName(lowMemoryCommand.getName());
				message.setCommandContent(lowMemoryCommand);
				message.setDate(bc.getBoard().getDate());
				message.setLocation(bc.getBoard().getLocation().getCode());
				message.setUser(Utils.getUserId());
				message.setServerSequence(bc.getAutoIncrementSequence());
			
				messenger.convertAndSend("/topic/commands." + bc.getBoard().getLocation().getCode() + "." + bc.getBoard().getDate(), message);
				persistanceService.deleteListener(bc.getBoard().getKey());
			}
		}
		
		private void inverseIndexing(BoardContainer bc) {
			bc.getBoard().getPersonnel().keySet().stream().forEach(e -> {inverseIndexCacheService.add(e, bc.getId());});
			bc.getBoard().getEquipment().keySet().stream().forEach(e -> {inverseIndexCacheService.add(e, bc.getId());});
		}
		});
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<String, BoardContainer> BoardContainer(GemFireCache cache, @Qualifier("boardContainerRegionAttributes") RegionAttributes<String, BoardContainer> regionAttributes)
			throws Exception {
		LocalRegionFactoryBean<String, BoardContainer> boardContainerRegion = new LocalRegionFactoryBean<String, BoardContainer>();
		boardContainerRegion.setCache(cache);
		boardContainerRegion.setAttributes(regionAttributes);
		return boardContainerRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean quotaRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(Long.class);
		attribute.setValueConstraint(Quota.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<Long, Quota> Quota(GemFireCache cache, @Qualifier("quotaRegionAttributes") RegionAttributes<Long, Quota> regionAttributes) throws Exception {
		LocalRegionFactoryBean<Long, Quota> quotaRegion = new LocalRegionFactoryBean<Long, Quota>();
		quotaRegion.setCache(cache);
		quotaRegion.setAttributes(regionAttributes);
		quotaRegion.setCacheLoader(quotaLoader);
		return quotaRegion;
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean
	public RegionAttributesFactoryBean boardQuotaRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(BoardQuotaId.class);
		attribute.setValueConstraint(BoardQuota.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<BoardQuotaId, BoardQuota> BoardQuota(GemFireCache cache, @Qualifier("boardQuotaRegionAttributes") RegionAttributes<BoardQuotaId, BoardQuota> regionAttributes)
			throws Exception {
		LocalRegionFactoryBean<BoardQuotaId, BoardQuota> boardQuotaRegion = new LocalRegionFactoryBean<BoardQuotaId, BoardQuota>();
		boardQuotaRegion.setCache(cache);
		boardQuotaRegion.setAttributes(regionAttributes);
		return boardQuotaRegion;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Bean(name="refDataTrackingRegionAttributes")
	public RegionAttributesFactoryBean RefDataTrackingRegionAttributes(CacheListener cacheListener){
		RegionAttributesFactoryBean attribute = new RegionAttributesFactoryBean();
		attribute.setKeyConstraint(RefDataTrackingKey.class);
		attribute.setValueConstraint(RefDataTrackingValue.class);
		attribute.addCacheListener(cacheListener);
		return attribute;
	}
	
	@Autowired
	@Bean
	public LocalRegionFactoryBean<RefDataTrackingKey, RefDataTrackingValue> RefDataTrackingRegion(GemFireCache cache,  
			@Qualifier("refDataTrackingRegionAttributes") RegionAttributes<RefDataTrackingKey, RefDataTrackingValue> regionAttributes)
			throws Exception {
		LocalRegionFactoryBean<RefDataTrackingKey, RefDataTrackingValue> refDataTrackingRegion = 
				new LocalRegionFactoryBean<RefDataTrackingKey, RefDataTrackingValue>();
		
		refDataTrackingRegion.setCache(cache);
		refDataTrackingRegion.setAttributes(regionAttributes);
		return refDataTrackingRegion;
	}
}

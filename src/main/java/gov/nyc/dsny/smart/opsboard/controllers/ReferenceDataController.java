package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.cache.equipment.DownCodeCache;
import gov.nyc.dsny.smart.opsboard.cache.equipment.MaterialTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardTypeCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.SubcategoryCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.CategoryCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.MdaTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.OfficerPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SeriesCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.SpecialPositionTypeCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.UnavailabilityTypeCacheService;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Series;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.DownCode;
import gov.nyc.dsny.smart.opsboard.domain.equipment.reference.MaterialType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.MdaType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.OfficerPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.SpecialPositionType;
import gov.nyc.dsny.smart.opsboard.domain.personnel.reference.UnavailabilityType;
import gov.nyc.dsny.smart.opsboard.domain.reference.BoardType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

//TODO - Handle errors properly

/**
 * This controller is used to load reference data.
 */
@Controller
@RequestMapping("/referencedata")
public class ReferenceDataController {

    private static final Logger log = LoggerFactory.getLogger(ReferenceDataController.class);

	@Autowired
	private LogContext logContext;

	@Autowired
	DownCodeCache downCodeCache;
	
	@Autowired
	LocationCache locationCache;
	
	@Autowired
	BoardTypeCache boardTypeCache;

	@Autowired
	MaterialTypeCache materialTypeCache;
	
	@Autowired
	SeriesCacheService seriesCacheService;
	
	@Autowired
	MdaTypeCacheService mdaTypeCacheService;
	
	@Autowired
	ShiftCacheService shiftCacheService;

	@Autowired
	CategoryCacheService categoryCacheService;

	@Autowired
	SubcategoryCache subcategoryCache;
	
	@Autowired
	UnavailabilityTypeCacheService unavailabilityTypeCacheService;
	
	@Autowired
	SpecialPositionTypeCacheService specialPositionTypeCacheService;
	
	@Autowired
	OfficerPositionTypeCacheService officerPositionTypeCacheService;

	
	@RequestMapping(value = {"/categories{boardLocation:.*}", "/categories/{boardLocation}"}, method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, Category> loadCategories(@PathVariable String boardLocation, @RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response, Principal principal) {
		logContext.initContext(request, principal);
		response.setStatus(HttpServletResponse.SC_OK);
		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		
		
		List<Category> list = new ArrayList<Category>();
		Map<Long, Category> resultMap = new HashMap<Long, Category>();
		try {
			list = categoryCacheService.loadCategories(boardLocation, boardDate);
			
			if(StringUtils.isBlank(boardLocation))
			{
				resultMap = list.stream().collect(Collectors.toMap(bp -> bp.getId(), bp -> bp));
			}
			
			if(StringUtils.isNotBlank(boardLocation)){
				Location location= locationCache.getLocation(boardLocation, boardDate);
				if(location.getBoardType()!=null && !CollectionUtils.isEmpty(list)){
					List<Category> reCategories = new ArrayList<Category>();
					for(Category category : list){
						Category newCategory = new Category();
						BeanUtils.copyProperties(category, newCategory);
						List<Subcategory> subcategories = new ArrayList<Subcategory>();
						for(Subcategory s : category.getSubcategories())
						{
							if(s.getLocationTypes().stream().anyMatch(l -> l.getBoardType().getCode().equals(location.getBoardType().getCode()))){
								if(DateUtils.onOrBetween(boardDate, s.getEffectiveStartDate(), s.getEffectiveEndDate())){
									subcategories.add(s);
								}
							}
						}
						newCategory.setSubcategories(subcategories.stream().sorted((c, o) -> Integer.compare(c.getSequence(), o.getSequence())).collect(Collectors.toList()));
						reCategories.add(newCategory);
					}
					resultMap = reCategories.stream().collect(Collectors.toMap(bp -> bp.getId(), bp -> bp));
				}
			}			

		} catch (Exception e) {
			log.debug("failed to load Category", e.fillInStackTrace());
		}
		return resultMap;
	}
	
	@RequestMapping(value = "/downcodes", method = RequestMethod.GET)
	@ResponseBody
	public List<DownCode> loadDownCodes(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);

		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return downCodeCache.getDownCodes(boardDate);
		} catch (Exception e) {
			return new ArrayList<DownCode>();
		}
	}

	@RequestMapping(value = "/locations", method = RequestMethod.GET)
	@ResponseBody
	public List<gov.nyc.dsny.smart.opsboard.domain.reference.Location> loadLocations(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			List<gov.nyc.dsny.smart.opsboard.domain.reference.Location> locations = locationCache.getLocations(boardDate);
			return locations;
		} catch (Exception e) {
			return new ArrayList<gov.nyc.dsny.smart.opsboard.domain.reference.Location>();
		}
	}

	@RequestMapping(value = "/series", method = RequestMethod.GET)
	@ResponseBody
	public List<Series> loadSeries(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return seriesCacheService.getSeries(boardDate);
		} catch (Exception e) {
			return new ArrayList<Series>();
		}
	}
	
	@RequestMapping(value = "/materialtypes", method = RequestMethod.GET)
	@ResponseBody
	public List<MaterialType> loadMaterialtypes(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return materialTypeCache.getMaterialTypes(boardDate);
		} catch (Exception e) {
			return new ArrayList<MaterialType>();
		}
	}
	
	@RequestMapping(value = "/boardTypes", method = RequestMethod.GET)
	@ResponseBody
	public List<BoardType> loadBoardTypes(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return boardTypeCache.getBoardTypes(boardDate);
		} catch (Exception e) {
			return new ArrayList<BoardType>();
		}
	}
	
	@RequestMapping(value = "/personnelmdatypes", method = RequestMethod.GET)
	@ResponseBody
	public List<MdaType> loadPersonnelMdatypes(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			
			return mdaTypeCacheService.getMdaTypes(boardDate).stream().sorted().collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<MdaType>();
		}
	}

	@RequestMapping(value = "/personnelspecialpositiontypes", method = RequestMethod.GET)
	@ResponseBody
	public List<SpecialPositionType> loadPersonnelSpecialPositiontypes(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return specialPositionTypeCacheService.getSpecialPositionTypes(boardDate);
		} catch (Exception e) {
			return new ArrayList<SpecialPositionType>();
		}
	}
	
	@RequestMapping(value = "/personnelunavailabilitytypes", method = RequestMethod.GET)
	@ResponseBody
	public List<UnavailabilityType> loadPersonnelUnavailabilitytypes(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return unavailabilityTypeCacheService.getUnavailabilityTypes(boardDate);
		} catch (Exception e) {
			return new ArrayList<UnavailabilityType>();
		}
	}
	
	@RequestMapping(value = "/officerpositiontypes", method = RequestMethod.GET)
	@ResponseBody
	public List<OfficerPositionType> loadOfficerPositionTypes(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return officerPositionTypeCacheService.getOfficerPositionTypes(boardDate);
		} catch (Exception e) {
			return new ArrayList<OfficerPositionType>();
		}
	}
	
	@RequestMapping(value = "/shifts", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long, Shift> loadShifts(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			List<Shift> list = shiftCacheService.getShifts(boardDate);
			Map<Long, Shift> map = new HashMap<Long, Shift>();
			for (Shift s : list) {
				map.put(s.getId(), s);
			}
			return map;
		} catch (Exception e) {
			return new HashMap<Long, Shift>();
		}
	}

	@RequestMapping(value = "/subcategories", method = RequestMethod.GET)
	@ResponseBody
	public List<Subcategory> loadSubcategories(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		logContext.initContext(request, principal);
		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			return subcategoryCache.getSubcategories(boardDate);
		} catch (Exception e) {
			return new ArrayList<Subcategory>();
		}
	}

	@RequestMapping(value = "/workunits", method = RequestMethod.GET)
	@ResponseBody
	@PostFilter("@boardPermissionEvaluator.hasPermission(authentication, filterObject, #boardDateParam)")
	public List<gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit> loadWorkunits(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		response.setStatus(HttpServletResponse.SC_OK);
		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);
		
		try {
			loadLocations(boardDateParam, request, response, principal);
			List<gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit> workUnits = locationCache.getWorkUnits(boardDate);
			List<WorkUnit> retVal = new ArrayList<WorkUnit>();
			
			workUnits.forEach(wu -> {
				WorkUnit w = new WorkUnit();
				w.setCode(wu.getCode());
				w.setDescription(wu.getDescription());
				w.setEffectiveEndDate(wu.getEffectiveEndDate());
				w.setEffectiveStartDate(wu.getEffectiveStartDate());
				Set<Location> locs = new HashSet<>();
				wu.getLocations().forEach(l -> locs.add(l));
				w.setLocations(locs);				
				w.setSortSequence(wu.getSortSequence());
				w.setWorkunitCode(wu.getWorkunitCode());
				w.setWorkunitDescription(wu.getWorkunitDescription());
				retVal.add(w);
			});
			return retVal;
		} catch (Exception e) {
            log.error("Unable to get list of WorkUnits: {}",e.getMessage(), e);
			return new ArrayList<gov.nyc.dsny.smart.opsboard.domain.reference.WorkUnit>();
		}
	}

	@RequestMapping(value = {"/getbyid/{refDataType}/{id}"}, method = RequestMethod.GET)
	@ResponseBody
	public Object getById(@PathVariable String refDataType, @PathVariable Long id, HttpServletRequest request, HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		log.debug("Request came for {} with id {}", refDataType, id);
		
		
		response.setStatus(HttpServletResponse.SC_OK);
		if (id == null)
		{
			log.warn("Reference data request came for {} with id null", refDataType);
			return null;
		}
		
		Object o = null;
		try 
		{
			if ("category".equals(refDataType))
				o = categoryCacheService.getCategoryByID(id);
			else if ("downcode".equals(refDataType))
				o =  downCodeCache.getDownCode(id);
			else if ("location".equals(refDataType))
				o =  locationCache.getLocationById(id);
			else if ("series".equals(refDataType))
				o =  seriesCacheService.getSeries(id);
			else if ("materialtype".equals(refDataType))
				o =  materialTypeCache.getMaterialTypeById(id);
			else if ("boardtype".equals(refDataType))
				o =  boardTypeCache.getBoardTypeById(id);
			else if ("mdatype".equals(refDataType))
				o =  mdaTypeCacheService.getMdaType(id);
			else if ("specialpositiontype".equals(refDataType))
				o =  specialPositionTypeCacheService.getSpecialPositionType(id);
			else if ("unavailabilitytype".equals(refDataType))
				o =  unavailabilityTypeCacheService.getUnavailabilityType(id);
			else if ("officerPositionType".equals(refDataType))
				o =  officerPositionTypeCacheService.getOfficerPositionType(id);
			else if ("shift".equals(refDataType))
				o =  shiftCacheService.getShiftById(id);
			else if ("subcategory".equals(refDataType))
				o =  subcategoryCache.getSubCategoryByID(id);
			else if ("workunit".equals(refDataType))
				o =  locationCache.getWorkUnit(id);
			else
			{
				log.warn("Failed to load reference data type {}", refDataType);
				return o;
			}

		} catch (Exception e) {
			log.warn("Failed to load {} for id {}", refDataType, id, e);
			return o;
		}
		
		if (o == null)
			log.warn("Reference data for {} for id {} ws not found", refDataType, id);
		
		return o;
	}

}
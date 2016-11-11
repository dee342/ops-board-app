package gov.nyc.dsny.smart.opsboard.controllers;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.domain.equipment.EquipmentConditionView;
import gov.nyc.dsny.smart.opsboard.domain.equipment.UpDownView;
import gov.nyc.dsny.smart.opsboard.domain.personnel.DetachmentView;
import gov.nyc.dsny.smart.opsboard.domain.personnel.GroundingStatusView;
import gov.nyc.dsny.smart.opsboard.domain.personnel.MdaStatusView;
import gov.nyc.dsny.smart.opsboard.domain.personnel.SpecialPositionView;
import gov.nyc.dsny.smart.opsboard.domain.personnel.UnavailabilityReasonView;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.EquipmentConditionViewRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.EquipmentDetachmentViewRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.equipment.EquipmentUpDownViewRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.GroundingStatusViewRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.MdaStatusViewRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.PersonnelDetachmentViewRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.SpecialPositionViewRepository;
import gov.nyc.dsny.smart.opsboard.persistence.repos.personnel.UnavailabilityReasonViewRepository;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;
import gov.nyc.dsny.smart.opsboard.viewmodels.PageView;
import gov.nyc.dsny.smart.opsboard.viewmodels.equipment.OpsBoardEquipmentConditionView;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/view")
public class ViewReposController {

	private static final Logger log = LoggerFactory.getLogger(ViewReposController.class);

	public static final String ASC = "asc";
	public static final String ID = "id";
	public static final int DEFAULT_PAGE_SIZE = 10;
	public static final String DEFAULT_SORT_COLUMN = ID;

	@Autowired
	private LogContext logContext;

	@Autowired
	private MdaStatusViewRepository mdaStatusViewRepository;

	@Autowired
	private PersonnelDetachmentViewRepository personnelDetachmentViewRepository;

	@Autowired
	private SpecialPositionViewRepository specialPositionViewRepository;

	@Autowired
	private UnavailabilityReasonViewRepository unavailabilityReasonViewRepository;

	@Autowired
	private GroundingStatusViewRepository groundingStatusViewRepository;

	@Autowired
	private EquipmentDetachmentViewRepository equipmentDetachmentViewRepository;

	@Autowired
	private EquipmentUpDownViewRepository equipmentUpDownViewRepository;

	@Autowired
	private EquipmentConditionViewRepository equipmentConditionViewRepository;

	protected Pageable createPageable(String pageStr, String sizeStr, String sortDir, String sortColumn) {

		int page = 0;
		int size = DEFAULT_PAGE_SIZE;
		try {
			page = Integer.parseInt(pageStr);
			size = Integer.parseInt(sizeStr);

		} catch (NumberFormatException e) {
		}

		PageRequest pageRequest = null;
		String sortColumnUpdated = getSortColumn(sortColumn);
		Direction direction = getDirection(sortDir);
		if(ID.equalsIgnoreCase(sortColumnUpdated)){
 			pageRequest = new PageRequest(page, size, direction, sortColumnUpdated);
		}
 		else {
 			//add a Unique column for secondary sort
 			pageRequest = new PageRequest(page, size, direction, sortColumnUpdated, ID);
 		}
		return pageRequest;
	}

	protected Direction getDirection(String sortDir) {
		return StringUtils.isEmpty(sortDir) ? Direction.DESC : ASC.equalsIgnoreCase(sortDir) ? Direction.ASC
				: Direction.DESC;
	}

	protected String getSortColumn(String sortColumn) {
		return StringUtils.isEmpty(sortColumn) ? DEFAULT_SORT_COLUMN : sortColumn;
	}

	@RequestMapping(value = "/conditions/{equipmentId}", method = RequestMethod.GET)
	@ResponseBody
	public PageView loadConditions(@PathVariable(value = "equipmentId") String equipmentId,
			@RequestParam(value = "upToDate", required = false) Date upToDate,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "size", required = false) String size,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "sortColumn", required = false) String sortColumn, HttpServletRequest request,
			HttpServletResponse response, Principal principal) {

		if(log.isDebugEnabled()){
			logContext.initContext(request, principal);
		}
		log.debug(appendEntries(logContext), "received View Call for conditions - equipment Id '{}' by user '{}'", equipmentId, principal.getName()	);
		Pageable pageable = createPageable(page, size, sortDir, sortColumn);
		Page<EquipmentConditionView> pageOfConditions;
		
		if(upToDate == null)
			pageOfConditions = equipmentConditionViewRepository.findByEquipmentId(equipmentId, pageable);
		else
			pageOfConditions = equipmentConditionViewRepository.findByEquipmentIdAndDateUpTo(equipmentId, upToDate, pageable);
		// convert the list of domain objects into a list of view objects
		// we are providing parent updown info with every condition object
		List<OpsBoardEquipmentConditionView> opsboardConditionsList = toOpsBoard(pageOfConditions.getContent());
		// use the view instead of domain
		return toPageView(pageOfConditions, opsboardConditionsList);
		
	}

	@RequestMapping(value = "/equipmentDetachments/{equipmentId}", method = RequestMethod.GET)
	@ResponseBody
	public PageView loadEquipmentDetachments(@PathVariable(value = "equipmentId") String equipmentId,
			@RequestParam(value = "upToDate", required = false) Date upToDate,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "size", required = false) String size,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "sortColumn", required = false) String sortColumn, HttpServletRequest request,
			HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Pageable pageable = createPageable(page, size, sortDir, sortColumn);
		Page<gov.nyc.dsny.smart.opsboard.domain.equipment.DetachmentView> equipmentDetachmentsPage;
		
		if(upToDate == null)
			equipmentDetachmentsPage = equipmentDetachmentViewRepository.findByEquipmentId(equipmentId, pageable);
		else
			equipmentDetachmentsPage = equipmentDetachmentViewRepository.findByEquipmentIdAndDateUpTo(equipmentId, upToDate, pageable);
		
		return toPageView(equipmentDetachmentsPage);
	}

	@RequestMapping(value = "/groundingstatus/{personId}", method = RequestMethod.GET)
	@ResponseBody
	public PageView loadGroundingStatus(@PathVariable(value = "personId") String personId,
			@RequestParam(value = "upToDate", required = false) Date upToDate,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "size", required = false) String size,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "sortColumn", required = false) String sortColumn, HttpServletRequest request,
			HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Pageable pageable = createPageable(page, size, sortDir, sortColumn);
		Page<GroundingStatusView> groundingstatusPage;
		
		if(upToDate == null)
			groundingstatusPage = groundingStatusViewRepository.findByPersonId(personId, pageable);
		else
			groundingstatusPage = groundingStatusViewRepository.findByPersonIdAndDateUpTo(personId, pageable);
		
		return toPageView(groundingstatusPage);
	}

	@RequestMapping(value = "/mdastatus/{personId}", method = RequestMethod.GET)
	@ResponseBody
	public PageView loadMdaStatus(@PathVariable(value = "personId") String personId,
			@RequestParam(value = "upToDate", required = false) Date upToDate,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "size", required = false) String size,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "sortColumn", required = false) String sortColumn, HttpServletRequest request,
			HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Pageable pageable = createPageable(page, size, sortDir, sortColumn);
		Page<MdaStatusView> mdastatusPage;
		
		if(upToDate == null)
			mdastatusPage = mdaStatusViewRepository.findByPersonId(personId, pageable);
		else
			mdastatusPage = mdaStatusViewRepository.findByPersonIdAndDateUpTo(personId, pageable);
		
		return toPageView(mdastatusPage);
	}

	@RequestMapping(value = "/personneldetachments/{personId}", method = RequestMethod.GET)
	@ResponseBody
	public PageView loadPersonnelDetachments(@PathVariable(value = "personId") String personId,
			@RequestParam(value = "upToDate", required = false) Date upToDate,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "size", required = false) String size,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "sortColumn", required = false) String sortColumn, HttpServletRequest request,
			HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Pageable pageable = createPageable(page, size, sortDir, sortColumn);
		Page<DetachmentView> detachmentsPage;
		
		if(upToDate == null)
			detachmentsPage = personnelDetachmentViewRepository.findByPersonId(personId, pageable);
		else
			detachmentsPage = personnelDetachmentViewRepository.findByPersonIdAndDateUpTo(personId, upToDate, pageable);
		
		return toPageView(detachmentsPage);
	}

	@RequestMapping(value = "/specialpostions/{personId}", method = RequestMethod.GET)
	@ResponseBody
	public PageView loadSpecialPositions(@PathVariable(value = "personId") String personId,
			@RequestParam(value = "upToDate", required = false) Date upToDate,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "size", required = false) String size,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "sortColumn", required = false) String sortColumn, HttpServletRequest request,
			HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Pageable pageable = createPageable(page, size, sortDir, sortColumn);
		Page<SpecialPositionView> specialPositionsPage;
		
		if(upToDate == null)
			specialPositionsPage = specialPositionViewRepository.findByPersonId(personId, pageable);
		else
			specialPositionsPage = specialPositionViewRepository.findByPersonIdAndDateUpTo(personId, pageable);
		
		return toPageView(specialPositionsPage);

	}

	@RequestMapping(value = "/unavailablereasons/{personId}", method = RequestMethod.GET)
	@ResponseBody
	public PageView loadUnavailableReasons(@PathVariable(value = "personId") String personId,
			@RequestParam(value = "upToDate", required = false) Date upToDate,
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "size", required = false) String size,
			@RequestParam(value = "sortDir", required = false) String sortDir,
			@RequestParam(value = "sortColumn", required = false) String sortColumn, HttpServletRequest request,
			HttpServletResponse response, Principal principal) {

		logContext.initContext(request, principal);
		Pageable pageable = createPageable(page, size, sortDir, sortColumn);
		Page<UnavailabilityReasonView> unavailablereasonsPage;
		
		if(upToDate == null)
			unavailablereasonsPage = unavailabilityReasonViewRepository.findByPersonId(personId, pageable);
		else
			unavailablereasonsPage = unavailabilityReasonViewRepository.findByPersonIdAndDateUpTo(personId, pageable);

		return toPageView(unavailablereasonsPage);
	}

	protected List<OpsBoardEquipmentConditionView> toOpsBoard(List<EquipmentConditionView> equipmentConditionView) {
		return equipmentConditionView.stream().map(e -> new OpsBoardEquipmentConditionView(e))
				.collect(Collectors.toCollection(() -> new ArrayList<OpsBoardEquipmentConditionView>()));
	}

	@SuppressWarnings({ "rawtypes" })
	protected PageView toPageView(Page page) {
		return toPageView(page, page.getContent());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected PageView toPageView(Page page, List list) {
		PageView pageView = new PageView();
		pageView.setItems(list);
		pageView.setCount(page.getTotalElements());
		pageView.setPage(page.getNumber());
		return pageView;
	}

}

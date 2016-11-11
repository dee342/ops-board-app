package gov.nyc.dsny.smart.opsboard.controllers;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.ErrorMessage;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardEquipmentCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.cache.gf.service.ShiftCacheService;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Controller
@RequestMapping(value = "/admin/cache")
// , produces = MediaType.APPLICATION_JSON_VALUE)
public class CacheRESTController {
	@Autowired
	private BoardEquipmentCacheService boardEquipmentCache;
	
	public static class OpsBoardEquipmentShort {
		private String id;
		private String owner;
		private String currentLocation;
		private String state;

		private String baseURL;

		public OpsBoardEquipmentShort() {
		}

		public OpsBoardEquipmentShort(String id) {
			this.id = id;
		}

		public OpsBoardEquipmentShort(String id, String owner, String currentLocation, String state, String baseURL) {
			this.id = id;
			this.owner = owner;
			this.currentLocation = currentLocation;
			this.state = state;
			this.baseURL = baseURL;
		}

		public String getBaseURL() {
			return baseURL;
		}

		public String getCurrentLocation() {
			return currentLocation;
		}

		public String getId() {
			return id;
		}

		public String getOwner() {
			return owner;
		}

		public String getState() {
			return state;
		}

		public void setBaseURL(String baseURL) {
			this.baseURL = baseURL;
		}

		public void setCurrentLocation(String currentLocation) {
			this.currentLocation = currentLocation;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public void setState(String state) {
			this.state = state;
		}

	}

	public static class OpsBoardPersonShort {
		private String id;
		private String homeLocation;
		private String workLocation;
		private String state;
		private String referenceNum;

		private String baseURL;

		public OpsBoardPersonShort() {
		}

		public OpsBoardPersonShort(String id) {
			this.id = id;
		}

		public OpsBoardPersonShort(String id, String homeLocation, String workLocation, String state,
				String referenceNum, String baseURL) {
			this.id = id;
			this.homeLocation = homeLocation;
			this.workLocation = workLocation;
			this.state = state;
			this.referenceNum = referenceNum;
			this.baseURL = baseURL;
		}

		public String getBaseURL() {
			return baseURL;
		}

		public String getHomeLocation() {
			return homeLocation;
		}

		public String getId() {
			return id;
		}

		public String getReferenceNum() {
			return referenceNum;
		}

		public String getState() {
			return state;
		}

		public String getWorkLocation() {
			return workLocation;
		}

		public void setBaseURL(String baseURL) {
			this.baseURL = baseURL;
		}

		public void setHomeLocation(String homeLocation) {
			this.homeLocation = homeLocation;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setReferenceNum(String referenceNum) {
			this.referenceNum = referenceNum;
		}

		public void setState(String state) {
			this.state = state;
		}

		public void setWorkLocation(String workLocation) {
			this.workLocation = workLocation;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class OpsBoardShort {
		private String id;
		private String location;
		private String date;
		private int personsOnBoard;
		private int equipmentsOnBoard;
		private String baseURL;
		private String equipment_ids;
		private String personnel_ids;

		public OpsBoardShort() {
		}

		public OpsBoardShort(String id, String location, String date, int personsOnBoard, int equipmentsOnBoard,
				String baseURL) {
			this.id = id;
			this.location = location;
			this.date = date;
			this.personsOnBoard = personsOnBoard;
			this.equipmentsOnBoard = equipmentsOnBoard;
			this.baseURL = baseURL;
		}

		OpsBoardShort(String id) {
			this.id = id;
		}

		public String getBaseURL() {
			return baseURL;
		}

		public String getDate() {
			return date;
		}

		public String getEquipment_ids() {
			return equipment_ids;
		}

		public int getEquipmentsOnBoard() {
			return equipmentsOnBoard;
		}

		public String getId() {
			return id;
		}

		public String getLocation() {
			return location;
		}

		public String getPersonnel_ids() {
			return personnel_ids;
		}

		public int getPersonsOnBoard() {
			return personsOnBoard;
		}

		public void setBaseURL(String baseURL) {
			this.baseURL = baseURL;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public void setEquipment_ids(String equipment_ids) {
			this.equipment_ids = equipment_ids;
		}

		public void setEquipmentsOnBoard(int equipmentsOnBoard) {
			this.equipmentsOnBoard = equipmentsOnBoard;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public void setPersonnel_ids(String personnel_ids) {
			this.personnel_ids = personnel_ids;
		}

		public void setPersonsOnBoard(int personsOnBoard) {
			this.personsOnBoard = personsOnBoard;
		}
	}

	public class ServerDetails {
		String ip;
		String hostname;
		int portname;

		public String getHostname() {
			return hostname;
		}

		public String getIp() {
			return ip;
		}

		public int getPortname() {
			return portname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		public void setPortname(int portname) {
			this.portname = portname;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(CacheRESTController.class);

	@Autowired
	private LogContext logContext;

	@Value("${spring.profiles.active}")
	private String environment;

	@Autowired
	ServletContext servletContext;

	@Autowired
	private BoardCacheService boardsCache;

	@Autowired
	private LocationCache locationCache;

//	@Autowired
//	private EquipmentCache equipmentCache;

	@Autowired
	private BoardPersonnelCacheService boardPersonnelCache;

	@Autowired
	private ShiftCacheService shiftCacheService;

	@Autowired
	private ApplicationContext ac;
	@Value("${servers.in.cluster}")
	private String serversInCluster;

	@Value("${rest.client.ConnectTimeout}")
	private int connectTimeout;

	@Value("${rest.client.ReadTimeout}")
	private int readTimeout;

	private HttpComponentsClientHttpRequestFactory rf = new HttpComponentsClientHttpRequestFactory();

	private RestTemplate r = new RestTemplate(rf);

	public CacheRESTController() {
		rf.setReadTimeout(readTimeout);
		rf.setConnectTimeout(connectTimeout);
		r.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
	}

	@RequestMapping(value = "/clear", method = RequestMethod.GET)
	@ResponseBody
	public synchronized Object clearCache(HttpServletRequest request, HttpServletResponse response,
			Principal principal, UsernamePasswordAuthenticationToken authentication) {

		boolean rootUser = false;
		try {
			logContext.initContext(request, principal);
			if (null != authentication && null != authentication.getAuthorities()) {
				for (Object element : authentication.getAuthorities()) {
					GrantedAuthority t = (GrantedAuthority) element;
					if (t.getAuthority().equalsIgnoreCase("ROOT")) {
						rootUser = true;
						break;
					}
				}
			}
			if (!rootUser) {
				log.warn(
						appendEntries(logContext),
						"User '{}' without permissions tried to clean cache. Client IP '{}'",
						null != principal ? principal.getName() : "Non authenticated user",
						"RemoteAddr:" + request.getRemoteAddr() + ", X-FORWARDED-FOR:"
								+ request.getHeader("X-FORWARDED-FOR"));
				return new ResponseEntity<String>(ErrorMessage.SECURITY_NO_PERMISSION_TO_CLEAN_CACHE.getMessage(),
						HttpStatus.OK);
			}
			boardsCache.clear();
			boardEquipmentCache.clear();
			boardPersonnelCache.clear();
			return new ResponseEntity<String>("Boards cache successfully cleared", HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during clear cache {}", e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/consolidated/board", method = RequestMethod.GET)
	@ResponseBody
	public Object getAllBoards(HttpServletRequest request, HttpServletResponse response, Principal principal,
			UsernamePasswordAuthenticationToken authentication) {

		List<OpsBoardShort> res = new ArrayList<OpsBoardShort>();
		try {
			logContext.initContext(request, principal);
			String[] servers = serversInCluster.split(";");
			for (String server : servers) {
				try {
					List board = Arrays.asList(r.getForObject(server.trim() + "admin/cache/board/",
							CacheRESTController.OpsBoardShort[].class));
					res.addAll(board);
				} catch (Exception e) {
					log.error(appendEntries(logContext),
							"Unexpected error during getting data from board cache, server {}. Error: {}", server,
							e.getMessage(), e);
					OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
					obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
					return obe;
				}
			}
			return new ResponseEntity<List>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting boards from cache {}",
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/consolidated/equipment", method = RequestMethod.GET)
	@ResponseBody
	public Object getAllEquipment(HttpServletRequest request, HttpServletResponse response, Principal principal,
			UsernamePasswordAuthenticationToken authentication) {

		List<OpsBoardEquipmentShort> res = new ArrayList<OpsBoardEquipmentShort>();
		try {
			logContext.initContext(request, principal);
			String[] servers = serversInCluster.split(";");
			for (String server : servers) {
				try {
					List equip = Arrays.asList(r.getForObject(server.trim() + "admin/cache/equipment/",
							CacheRESTController.OpsBoardEquipmentShort[].class));
					res.addAll(equip);
				} catch (Exception e) {
					log.error(appendEntries(logContext),
							"Unexpected error during getting data from Equipment cache, server {}. Error: {}", server,
							e.getMessage(), e);
					OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
					obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
					return obe;
				}
			}
			return new ResponseEntity<List>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting equipment from cache {}",
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/consolidated/personnel", method = RequestMethod.GET)
	@ResponseBody
	public Object getAllPersonnel(HttpServletRequest request, HttpServletResponse response, Principal principal,
			UsernamePasswordAuthenticationToken authentication) {

		List<OpsBoardPersonShort> res = new ArrayList<OpsBoardPersonShort>();
		try {
			logContext.initContext(request, principal);
			String[] servers = serversInCluster.split(";");
			for (String server : servers) {
				try {
					List pers = Arrays.asList(r.getForObject(server.trim() + "admin/cache/personnel/",
							CacheRESTController.OpsBoardPersonShort[].class));
					res.addAll(pers);
				} catch (Exception e) {
					log.error(appendEntries(logContext),
							"Unexpected error during getting data from Personnel cache, server {}. Error: {}", server,
							e.getMessage(), e);
					OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
					obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
					return obe;
				}
			}
			return new ResponseEntity<List>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting personnel from cache {}",
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@RequestMapping(value = "/board/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Object getBoard(@PathVariable String id, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {

		Board res = null;
		try {
			logContext.initContext(request, principal);
			for (Object element : boardsCache.get()) {
				BoardContainer t = (BoardContainer) element;
				if (t.getBoard().getId().equalsIgnoreCase(id)) {
					res = t.getBoard();
					break;
				}
			}
			return new ResponseEntity<Board>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting board '{}' from cache. Error: {}",
					id, e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@SuppressWarnings({ "rawtypes"})
	@RequestMapping(value = "/board", method = RequestMethod.GET)
	@ResponseBody
	public Object getBoards(HttpServletRequest request, HttpServletResponse response, Principal principal,
			UsernamePasswordAuthenticationToken authentication) {

		try {
			logContext.initContext(request, principal);
			List<OpsBoardShort> res = new ArrayList<OpsBoardShort>();
			for (Object element : boardsCache.get()) {
				BoardContainer t = (BoardContainer) element;
				OpsBoardShort ob = new OpsBoardShort(t.getBoard().getId(), t.getBoard().getLocation().getCode(), t
						.getBoard().getDate(), t.getBoard().getPersonnel().size(), t.getBoard().getEquipment().size(),
						request.getRequestURL().toString()
								+ (request.getRequestURL().toString().endsWith("/") ? "" : "/") + t.getBoard().getId());
				/*
				 * // use it if we need to keep list of equipment and personnel in board for (OpsBoardPerson p :
				 * t.getBoard().getPersonnel().values()) { ob.setPersonnel_ids(ob.getPersonnel_ids() + "," + p.getId());
				 * } for (OpsBoardEquipment p : t.getBoard().getEquipment().values()) {
				 * ob.setEquipment_ids(ob.getEquipment_ids()+p.getId()); }
				 */
				res.add(ob);
			}
			return new ResponseEntity<List>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting boards from cache {}",
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}

	}

	@RequestMapping(value = "/equipment/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Object getEquipment(@PathVariable String id, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {
		try {
			logContext.initContext(request, principal);
			BoardEquipment eq = boardEquipmentCache.get(id);
			return new ResponseEntity<BoardEquipment>(eq, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext),
					"Unexpected error during getting equipment '{}' from cache. Error: {}", id, e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@SuppressWarnings({ "rawtypes"})
	@RequestMapping(value = "/equipment", method = RequestMethod.GET)
	@ResponseBody
	public Object getEquipmentList(HttpServletRequest request, HttpServletResponse response, Principal principal) {

		List<OpsBoardEquipmentShort> res = new ArrayList<OpsBoardEquipmentShort>();
		try {
			logContext.initContext(request, principal);
			for (BoardEquipment t : boardEquipmentCache.get()) {
				OpsBoardEquipmentShort ob = new OpsBoardEquipmentShort(t.getId(), t.getOwner().getCode(), t
						.getState(t.getOwner()).getLocation().getCode(), t.getState(t.getOwner()).getState(), request
						.getRequestURL().toString()
						+ (request.getRequestURL().toString().endsWith("/") ? "" : "/")
						+ t.getId());
				res.add(ob);
			}
			return new ResponseEntity<List>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting equipment from cache. Error: {}",
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}

	}

	@RequestMapping(value = "/personnel/{id}", method = RequestMethod.GET)
	@ResponseBody
	public Object getPersonnel(@PathVariable String id, HttpServletRequest request, HttpServletResponse response,
			Principal principal) {
		try {
			logContext.initContext(request, principal);
			BoardPerson pers = boardPersonnelCache.getPersonnelMap().get(id);
			return new ResponseEntity<BoardPerson>(pers, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting personnel '{}' from cache {}", id,
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@SuppressWarnings({ "rawtypes"})
	@RequestMapping(value = "/personnel", method = RequestMethod.GET)
	@ResponseBody
	public Object getPersonnelList(HttpServletRequest request, HttpServletResponse response, Principal principal) {

		List<OpsBoardPersonShort> res = new ArrayList<OpsBoardPersonShort>();
		try {
			logContext.initContext(request, principal);
			for (Object element : boardPersonnelCache.getPersonnelMap().values()) {
				BoardPerson t = (BoardPerson) element;
				OpsBoardPersonShort ob = new OpsBoardPersonShort(t.getId(), t.getHomeLocation().getCode(), t
						.getWorkLocation().getCode(), t.getState(t.getWorkLocation()).getState(), t.getReferenceNum(),
						request.getRequestURL().toString()
								+ (request.getRequestURL().toString().endsWith("/") ? "" : "/") + t.getId());
				res.add(ob);
			}
			return new ResponseEntity<List>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting personnel from cache. Error: {}",
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}

	@RequestMapping(value = "/server", method = RequestMethod.GET)
	@ResponseBody
	public Object getServerDetails(HttpServletRequest request, HttpServletResponse response, Principal principal,
			UsernamePasswordAuthenticationToken authentication) {

		ServerDetails serverObj = new ServerDetails();
		try {
			logContext.initContext(request, principal);
			serverObj.setIp(request.getLocalAddr());
			serverObj.setHostname(request.getLocalName());
			serverObj.setPortname(request.getLocalPort());
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error while getting server details {}", e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
		return serverObj;
	}

	@SuppressWarnings({ "rawtypes"})
	@RequestMapping(value = "/shift", method = RequestMethod.GET)
	@ResponseBody
	public Object getShiftList(@RequestParam("boardDate") String boardDateParam, HttpServletRequest request, HttpServletResponse response, Principal principal) {

		List<Shift> res = null;
		Date boardDate = DateUtils.toBoardDateNoNull(boardDateParam);

		try {
			logContext.initContext(request, principal);
			res = shiftCacheService.getShifts(boardDate);
			return new ResponseEntity<List>(res, HttpStatus.OK);
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Unexpected error during getting equipment from cache. Error: {}",
					e.getMessage(), e);
			OpsBoardError obe = new OpsBoardError(ErrorMessage.DATA_ERROR_GETTING_CACHE_DATA, e);
			obe.getExtendedMessages().add(e.getClass().getCanonicalName() + ": " + e.getMessage());
			return obe;
		}
	}
}
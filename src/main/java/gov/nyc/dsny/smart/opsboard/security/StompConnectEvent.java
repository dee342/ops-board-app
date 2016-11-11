package gov.nyc.dsny.smart.opsboard.security;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.cache.UserSessionCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.board.CommandAddUserSessionToBoard;
import gov.nyc.dsny.smart.opsboard.commands.board.CommandRemoveUserSessionFromBoard;
import gov.nyc.dsny.smart.opsboard.domain.ActiveUserSession;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardEquipmentDetails;
import gov.nyc.dsny.smart.opsboard.viewmodels.OpsBoardPersonDetails;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SubProtocolWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class StompConnectEvent implements ApplicationListener<AbstractSubProtocolEvent> {

	private static final Logger log = LoggerFactory.getLogger(StompConnectEvent.class);

	@Autowired
	private BoardCacheService boardsCache;

	@Autowired
	private LocationCache locationCache;

	@Autowired
	private SimpMessagingTemplate messenger;

	@Autowired
	private UserSessionCache sessionCache;

	@Autowired
	private WebSocketHandler subProtocolWebSocketHandler;
	
	@Autowired
	private LogContext logContext;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	private Map<String, WebSocketSession> webSocketSessions;
	
	private ScheduledExecutorService equipmentExecutor = Executors.newScheduledThreadPool(10);

	private ScheduledExecutorService personnelExecutor = Executors.newScheduledThreadPool(10);

	@SuppressWarnings("unchecked")
	@Override
	public void onApplicationEvent(AbstractSubProtocolEvent event) {

		if (event instanceof SessionSubscribeEvent) {
			try {
				log.debug("onApplicationEvent started for {}, {}", event.getClass().getCanonicalName(), event);
				StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
				log.debug("onApplicationEvent started for {}, {}", sha.getClass().getCanonicalName(), sha);
				String wsSessionId = sha.getSessionId();
				String temp = sha.getDestination();
				if (!temp.contains("/topic/commands")) {
					return;
				}
				temp = temp.replaceFirst("/topic/commands.", "");

				String boardLocation = temp.split("\\.")[0];
				String boardDate = temp.split("\\.")[1];

				UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) sha
						.getHeader("simpUser");
				
				SmartUserDetails userDetails = (SmartUserDetails) user.getPrincipal();
				String userId = userDetails.getUsername();
				
				WebAuthenticationDetails details = (WebAuthenticationDetails) user.getDetails();
				String remoteAddr = details.getRemoteAddress();

				// We can't get httpSessionId from WebAuthenticationDetails because SpringSession still doesn't support
				// websocket sessions
				// so pass httpSessionId it STOMP headers
				// String httpSessionId = details.getSessionId();

				String httpSessionId = null;
				try {
					Map<String, Object> sessAttr = sha.getSessionAttributes();
					httpSessionId = (String) sessAttr.get("HTTPSESSIONID");
				} catch (Exception e) {
					log.error("Error during getting HTTPSESSIONID from headers", e);
				}

				ActiveUserSession newSession = new ActiveUserSession(wsSessionId, httpSessionId, remoteAddr, userId,
						boardLocation, boardDate);

				// add session to board active session
				Date date= DateUtils.toBoardDateNoNull(boardDate);
				BoardKey key = boardKeyFactory.createBoardKey(date, boardLocation); 
				BoardContainer boardContainer = boardsCache.get(key.toId());
				
				if(boardContainer == null)
				{	
					log.error(appendEntries(logContext), "receiveCommand - Board '{}'does not exist.", key);
					// 	Stop forwarding of message to other clients
					return;
				}
				boardContainer.getBoard().addOnlineSession(newSession);

				// add session to Cache
				sessionCache.addUserSession(newSession);

				CommandAddUserSessionToBoard command = new CommandAddUserSessionToBoard(boardLocation + "_" + boardDate,
						userId, new Date(), newSession);

				CommandMessage message = new CommandMessage();
				message.setCommandName(command.getName());
				message.setCommandContent(command);
				message.setDate(boardDate);
				message.setLocation(boardLocation);
				message.setUser(userId);
				message.setServerSequence(boardContainer.getAutoIncrementSequence());

				// send notification to all opened boards
				messenger.convertAndSend("/topic/commands." + boardLocation + "." + boardDate, message);

				// convertAndSend sends message to all sessions(boards) except current that was just created.
				// TODO: need to catch "SessionSubscribedEvent" instead of "SessionSubscribeEvent"
				// TODO: remove this code as soon Spring gets support for SessionSubscribedEvent,
				// Workaround to send NotifyMessage to current session
				webSocketSessions = (Map<String, WebSocketSession>) new DirectFieldAccessor(subProtocolWebSocketHandler)
						.getPropertyValue("sessions");
				WebSocketSession myses = ((SubProtocolWebSocketHandler.WebSocketSessionHolder) webSocketSessions
						.get(wsSessionId)).getSession();
				String sub = sha.getSubscriptionId();
				String dest = sha.getDestination();
				ObjectMapper objectMapper = new ObjectMapper();
				String jsonMsg = objectMapper.writeValueAsString(message);
				String mesg = String
						.format("MESSAGE\nsubscription:%s\ndestination:%s\ncontent-type:application/json;charset=UTF-8\ncontent-length:%d\n\n%s",
								sub, dest, jsonMsg.length(), jsonMsg);
				myses.sendMessage(new TextMessage(mesg));
				// Workaround to send NotifyMessage to current session -- END

				log.info("User '{}' opened board '{}' with sessionId '{}'", userId, boardLocation + "_" + boardDate,
						wsSessionId);
				
//				sendEquipmentDetailsAsync(boardContainer.getBoard(), key);
//				sendPersonnelDetailsAsync(boardContainer.getBoard(), key);

			} catch (Exception e) {
				log.error("Error in onApplicationEvent, event: '{}', ErrorMessage ", event.getClass().getName(),
						e.getMessage(), e);
			}
			return;
		}
		// Handle situation if user close browser tab with board and tab lost connection to server
		if (event instanceof SessionDisconnectEvent) {
			try {
				log.debug("onApplicationEvent started for {}, {}", event.getClass().getCanonicalName(), event);
				StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
				String wsSessionId = sha.getSessionId();
				ActiveUserSession oldSes = sessionCache.getUserSession(wsSessionId);
				// User already logged out and there is NO such session, so we don't need to send notification
				if (null == oldSes) {
					return;
				}
				String boardLocation = oldSes.getLocation();
				String boardDate = oldSes.getDate();
				String userId = oldSes.getLoggedUserId();
				String httpSessionId = oldSes.getHttpSessionId();

				// remove session from board active session
				Date date= DateUtils.toBoardDateNoNull(boardDate);
				BoardKey key = boardKeyFactory.createBoardKey(date, boardLocation); 
				BoardContainer boardContainer = boardsCache.get(key);
				boardContainer.getBoard().removeOnlineSession(wsSessionId);

				// remove session from Cache
				sessionCache.removeUserSession(wsSessionId);

				CommandRemoveUserSessionFromBoard command = new CommandRemoveUserSessionFromBoard(boardLocation + "_"
						+ boardDate, userId, new Date(), wsSessionId, httpSessionId);

				CommandMessage message = new CommandMessage();
				message.setCommandName(command.getName());
				message.setCommandContent(command);
				message.setDate(boardDate);
				message.setLocation(boardLocation);
				message.setUser(userId);
				message.setServerSequence(boardContainer.getAutoIncrementSequence());

				// send notification to all opened boards
				messenger.convertAndSend("/topic/commands." + boardLocation + "." + boardDate, message);

				log.info("User '{}' left board '{}' with sessionId '{}'", userId, boardLocation + "_" + boardDate,
						wsSessionId);
			} catch (Exception e) {
				log.error("Error in onApplicationEvent, event: '{}', ErrorMessage ", event.getClass().getName(),
						e.getMessage(), e);
			}
		}
		log.debug("onApplicationEvent finished for {}, {}", event.getClass().getCanonicalName(), event);
	}
	
	public void sendEquipmentDetailsAsync(Board board, BoardKey key) {

		board.getEquipment().values().forEach(c -> {
			equipmentExecutor.schedule(new PopulateEquipmentThread(c, key), 1, TimeUnit.SECONDS);
			// equipmentExecutor.execute(new PopulateEquipmentThread(c, key));
		});

	}

	public void sendPersonnelDetailsAsync(Board board, BoardKey key) {

		board.getPersonnel().values().forEach(c -> {
			personnelExecutor.schedule(new PopulatePersonnelThread(c, key), 1, TimeUnit.SECONDS);
			// personnelExecutor.execute(new PopulatePersonnelThread(c, key));
		});
	}
	
	private class PopulateEquipmentThread implements Runnable {
		private BoardEquipment be;
		private BoardKey key;

		public PopulateEquipmentThread(BoardEquipment be, BoardKey key) {
			this.be = be;
			this.key = key;
		}

		@Override
		public void run() {
			// equipmentPersistenceService.populateTransients(be);

			messenger.convertAndSend("/topic/details.equipment." + key.getLocation().getCode() + "." + key.getDate(),
					new OpsBoardEquipmentDetails(be));
		}

	}

	private class PopulatePersonnelThread implements Runnable {
		private BoardPerson bp;
		private BoardKey key;

		public PopulatePersonnelThread(BoardPerson bp, BoardKey key) {
			this.bp = bp;
			this.key = key;
		}

		@Override
		public void run() {
			// personnelPersistenceService.populateTransients(bp);

			messenger.convertAndSend("/topic/details.personnel." + key.getLocation().getCode() + "." + key.getDate(),
					new OpsBoardPersonDetails(bp));

		}

	}
}
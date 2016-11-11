package gov.nyc.dsny.smart.opsboard.security;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.UserSessionCache;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.board.CommandRemoveUserSessionFromBoard;
import gov.nyc.dsny.smart.opsboard.domain.ActiveUserSession;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class LogoutSuccessHandler implements LogoutHandler {

    private static final Logger log = LoggerFactory.getLogger(LogoutSuccessHandler.class);

    @Autowired
    private BoardCacheService boardsCache;
    
    @Autowired
    private LocationCache locationCache;

    @Autowired
    private SimpMessagingTemplate messenger;

    @Autowired
    private UserSessionCache sessionCache;
    
	@Autowired
	private BoardKeyFactory boardKeyFactory;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        log.debug("LogoutSuccessHandler started, Principal=<{}>, req=({}), resp=<{}>", authentication, request, response);

        if (null == authentication) {
            String httpSessionId = request.getSession().getId();
            if (null != httpSessionId) {
                sessionCache.removeAllSessionsForHTTPSession(httpSessionId);
                log.debug("LogoutSuccessHandler finished successfully, ",authentication,request,response);
            } else {
                log.warn("LogoutSuccessHandler finished successfully but httpSessionId is empty and SessionCache has not benn cleared, ",authentication,request,response);
            }
            return;
        }

        WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
        if (null == details) {
            log.warn("LogoutSuccessHandler finished because AuthenticationDetails is null", authentication, request, response);
            return;
        }

        String httpSessionId = details.getSessionId();
// notify all boards for all InActive ws sessions
        List<ActiveUserSession> toLogoutSessons = sessionCache.getSessionsForHTTPSession(httpSessionId);

        for (ActiveUserSession temp: toLogoutSessons) {
            CommandRemoveUserSessionFromBoard command = new CommandRemoveUserSessionFromBoard(temp.getLocation()+"_"+temp.getDate(), temp.getLoggedUserId(), new Date(), temp.getWsSessionId(), temp.getHttpSessionId());
            CommandMessage message = new CommandMessage();
            message.setCommandName(command.getName());
            message.setCommandContent(command);
            message.setDate(temp.getDate());
            message.setLocation(temp.getLocation());
            message.setUser(temp.getLoggedUserId());

            messenger.convertAndSend("/topic/commands." + temp.getLocation() + "." + temp.getDate(), message);

// add session to board active session
            try {
            	Date date=  DateUtils.toBoardDateNoNull(temp.getDate());
                BoardKey key =  boardKeyFactory.createBoardKey(date, temp.getLocation());
                BoardContainer boardContainer = boardsCache.get(key.toId());
                
                if(boardContainer != null)
                	boardContainer.getBoard().removeOnlineSession(temp.getWsSessionId());
                
            } catch (OpsBoardError opsBoardError) {
                log.debug("LogoutSuccessHandler board location '{}' not found", temp.getLocation());
            }

            log.debug("LogoutSuccessHandler sent RemoveSession notification to board '{}' for user '{}'  with wsSession '{}' and httpSession '{}'", temp.getLocation() + "_" + temp.getDate(), temp.getLoggedUserId(), temp.getWsSessionId(), temp.getHttpSessionId());
        }
        sessionCache.removeAllSessionsForHTTPSession(httpSessionId);
        log.debug("LogoutSuccessHandler finished successfully, ",authentication,request,response);
    }
}
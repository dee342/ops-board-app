package gov.nyc.dsny.smart.opsboard.cache;


import gov.nyc.dsny.smart.opsboard.domain.ActiveUserSession;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSessionCache {

    private static final Logger log = LoggerFactory.getLogger(UserSessionCache.class);

    @Autowired
    private LogContext logContext;

    private ConcurrentHashMap<String, ActiveUserSession> sessionMap = new ConcurrentHashMap<String, ActiveUserSession>();

    public ActiveUserSession getUserSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public synchronized void addUserSession(ActiveUserSession session) {
        sessionMap.put(session.getWsSessionId(), session);
    }

    public synchronized void removeUserSession(String sessionId) {
        if (sessionMap.containsKey(sessionId)) {
            sessionMap.remove(sessionId);
        }
    }

    public synchronized void removeAllSessionsForHTTPSession(String httpSessionId) {
        Iterator<String> it = sessionMap.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            ActiveUserSession val = sessionMap.get(key);
            if (val.getHttpSessionId().equalsIgnoreCase(httpSessionId)) {
                it.remove();
            }
        }
    }

    public synchronized List<ActiveUserSession> getSessionsForHTTPSession(String httpSessionId) {
        List<ActiveUserSession> result = new LinkedList<ActiveUserSession>();
        Iterator<String> it = sessionMap.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            ActiveUserSession val = sessionMap.get(key);
            if (val.getHttpSessionId().equalsIgnoreCase(httpSessionId)) {
                result.add(val);
            }
        }
        return result;
    }

    public synchronized List<String> getBoardIdsForHTTPSession(String httpSessionId) {
        List<String> result = new LinkedList<String>();
        if (null == httpSessionId) {
            return result;
        }
        Iterator<String> it = sessionMap.keySet().iterator();
        while(it.hasNext()) {
            String key = it.next();
            ActiveUserSession val = sessionMap.get(key);
            if (httpSessionId.equalsIgnoreCase(val.getHttpSessionId())) {
                result.add(val.getLocation()+"."+val.getDate());
            }
        }
        return result;
    }

    public synchronized boolean clear(String httpSessionId) {
        sessionMap.clear();
        return true;
    }

}

package home.work.stakes.domain.session;

import home.work.stakes.util.SessionKeyUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class SessionManager {
    //key: sessionKey, value: session
    private final Map<String, Session> sessionStore = new ConcurrentHashMap<>(1000);

    private static final long SESSION_DURATION = 1000 * 60 * 10;

    public Session queryAndDeleteExpiredSession(String key) {
        return sessionStore.computeIfPresent(SessionKeyUtil.getSuffix(key), (k, session) -> {
            if (session.isExpired(SESSION_DURATION)) {
                sessionStore.remove(key);
                return null;
            }

            //verify sessionKey
            if(!key.equals(session.getSessionKey())){
                return null;
            }
            return session;
        });

    }

    public Session createSession(Integer customerId) {
        return sessionStore.computeIfAbsent(SessionKeyUtil.toSuffix(customerId), (key)->new Session(customerId));
    }

    public void removeExpiredSession() {
        sessionStore.forEach((k, session) -> {
            if (session.isExpired(SESSION_DURATION)) {
                sessionStore.remove(k);
                System.out.println("remove expired session ,customerId is: " + session.getCustomerId());
            }
        });
    }

}

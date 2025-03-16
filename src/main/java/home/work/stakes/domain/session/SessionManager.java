package home.work.stakes.domain.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class SessionManager {
    //key: sessionKey, value: session
    private final Map<String, Session> keyToSessionStore = new ConcurrentHashMap<>(1000);

    //key: customerId, value: sessionKey
    private final Map<String, String> idToKeyStore = new ConcurrentHashMap<>(1000);

    //key: customerId, value: lock
    private final Map<String, ReentrantLock> idToLockStore = new ConcurrentHashMap<>();

    private static final long SESSION_DURATION = 1000 * 60 * 10;

    private static final Long MAX_SESSION_SIZE = 1000000L;

    public Session getSessionByKey(String sessionKey) {
        Session session = keyToSessionStore.get(sessionKey);

        if (session == null) {
            return null;
        }

        if (session.isExpired(SESSION_DURATION)) {
            removeSession(session);
            return null;
        }

        return session;
    }


    public Session createSession(String customerId) {
        Session session = getSessionById(customerId);
        if (session != null) {
            return session;
        }

        return withLock(customerId, () -> {
            Session se = getSessionById(customerId);
            if (se != null) {
                return se;
            }
            //check max session size
            if (keyToSessionStore.size() > MAX_SESSION_SIZE) {
                throw new IllegalStateException("Maximum session size reached");
            }
            se = new Session(customerId);
            keyToSessionStore.put(se.getSessionKey(), se);
            idToKeyStore.put(customerId, se.getSessionKey());
            return se;
        });
    }

    private <T> T withLock(String customerId, Supplier<T> supplier) {
        ReentrantLock lock = idToLockStore.computeIfAbsent(customerId, k -> new ReentrantLock());
        boolean locked = false;
        try {
            locked = lock.tryLock(1, TimeUnit.SECONDS);
            if (locked) {

                return supplier.get();

            } else {
                throw new RuntimeException("Failed to acquire lock");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if (locked) {
                lock.unlock();
                idToLockStore.remove(customerId);
            }
        }
    }

    private Session getSessionById(String customerId) {
        String key = idToKeyStore.get(customerId);
        if (key != null) {
            Session session = getSessionByKey(key);
            if (session != null) {
                return session;
            }

            idToKeyStore.remove(customerId);
        }
        return null;
    }

    public void removeExpiredSession() {
        for (Map.Entry<String, Session> entry : keyToSessionStore.entrySet()) {
            if (entry.getValue().isExpired(SESSION_DURATION)) {
                removeSession(entry.getValue());
            }
        }
    }

    private void removeSession(Session session) {
        withLock(session.getCustomerId(), () -> {
            keyToSessionStore.remove(session.getSessionKey());
            idToKeyStore.remove(session.getCustomerId());
            return null;
        });
    }

}

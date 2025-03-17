package home.work.stakes.domain.session;

import home.work.stakes.util.SessionKeyUtil;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class Session {
    private final Integer customerId;

    private final long createdTm;

    private final String sessionKey;

    public Session(Integer customerId) {
        this.customerId = customerId;
        this.createdTm = System.currentTimeMillis();
        this.sessionKey = generateSessionKey();
    }

    private String generateSessionKey() {

        return SessionKeyUtil.toPrefix(createdTm) + SessionKeyUtil.toSuffix(customerId);
    }

    public boolean isExpired(long duration) {
        return System.currentTimeMillis() - createdTm > duration;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public Integer getCustomerId() {
        return customerId;
    }
}

package home.work.stakes.domain.session;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class Session {
    private final String customerId;

    private final Long createdTm;

    private final String sessionKey;

    public Session(String customerId) {
        this.customerId = customerId;
        this.createdTm = System.currentTimeMillis();
        this.sessionKey = generateSessionKey();
    }

    private String generateSessionKey() {
        //转为16进制
        return String.format("%x", createdTm) + String.format("%x", Integer.valueOf(customerId));
    }

    public boolean isExpired(Long duration) {
        return System.currentTimeMillis() - createdTm > duration;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public String getCustomerId() {
        return customerId;
    }
}

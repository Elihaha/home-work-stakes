package home.work.stakes.util;

/**
 * @Author zhengxin
 * @Date 2025/3/17
 */
public class SessionKeyUtil {

    public static String toSuffix(int customerId) {
        return Integer.toHexString(customerId);
    }

    public static String getSuffix(String sessionKey) {
        return sessionKey.substring(11);
    }

    public static String toPrefix(long createdTm) {
        return Long.toHexString(createdTm).substring(0, 11);
    }

}

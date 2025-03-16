package home.work.stakes.http;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
@FunctionalInterface
public interface HttpFunction {
    String apply(Object[] o);
}

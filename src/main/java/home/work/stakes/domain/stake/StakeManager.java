package home.work.stakes.domain.stake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @Author zhengxin
 * @Date 2025/3/15
 */
public class StakeManager {

    private static final int TASK_SIZE = 4;

    private final Map<Integer, BetOffer> betOfferStore = new ConcurrentHashMap<>(1000);

    /**
     * key: 模数, value: 模数对应的执行器
     */
    private final Map<Integer, Executor> executors = new HashMap<>(TASK_SIZE);

    public StakeManager() {
        for (int i = 0; i < TASK_SIZE; i++) {
            executors.put(i, new ThreadPoolExecutor(1,
                    1,
                    0,
                    TimeUnit.MICROSECONDS,
                    new LinkedBlockingQueue<>(10000),
                    new ThreadPoolExecutor.AbortPolicy()));
        }
    }

    public void add(int betOfferId, Stake stake) {
        BetOffer betOffer = betOfferStore.computeIfAbsent(betOfferId, k -> new BetOffer(stake));

        // 小于排名第20的 stake value, 则不处理
        if (stake.getValue() <= betOffer.getFinalStake()) {
            return;
        }

        Executor executor = executors.get(betOfferId % TASK_SIZE);
        executor.execute(() -> betOffer.addStake(stake));
    }

    public List<Stake> getHighStakes(int betOfferId) {
        BetOffer betOffer = betOfferStore.get(betOfferId);
        if (betOffer == null) {
            return null;
        }

        return betOffer.getSnapshot();
    }

}

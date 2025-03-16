package home.work.stakes.domain.stake;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class BetOffer {

    private final ConcurrentSkipListSet<Stake> rankStore = new ConcurrentSkipListSet<>(Comparator.reverseOrder());

    //key: customerId, value: stake
    private final Map<String, Stake> customerToStakeStore = new HashMap<>();

    private static final int MAX_SIZE = 20;

    public BetOffer(Stake stake) {
        rankStore.add(stake);
    }

    public int getFinalStake() {
        if (rankStore.size() < MAX_SIZE) {
            return Integer.MIN_VALUE;
        }

        return rankStore.last().getValue();
    }


    public void addStake(Stake stake) {
        String customerId = stake.getCustomerId();
        Stake old = customerToStakeStore.get(customerId);

        if (old != null) {
            if (old.getValue() < stake.getValue()) {
                rankStore.add(stake);
                rankStore.remove(old);
                customerToStakeStore.put(customerId, stake);
            }
            return;
        }

        //如果当前offer小于20个,且老值不存在，直接添加
        if (rankStore.size() < MAX_SIZE) {
            rankStore.add(stake);
            customerToStakeStore.put(customerId, stake);
            return;
        }

        //如果当前offer大于20个，且老值不存在，只处理大于排名20的情况
        if (stake.getValue() > getFinalStake()) {
            rankStore.add(stake);
            Stake last = rankStore.pollLast();
            if(last != null) {
                customerToStakeStore.remove(last.getCustomerId());
            }

            customerToStakeStore.put(customerId, stake);
        }
    }

    public List<Stake> getSnapshot() {
        List<Stake> snapshot = new ArrayList<>(rankStore.clone());

        // 同一个customerId 在做更新的时候 极端情况会有旧值存在，需要过滤
        Map<String, Boolean> idMap = new HashMap<>();
        snapshot = snapshot.stream()
                .filter(stake -> idMap.putIfAbsent(stake.getCustomerId(), true) == null)
                .collect(Collectors.toList());

        //由于先添加，后删除，快照可能会在极端的时刻达到21个
        if (snapshot.size() > MAX_SIZE) {
            snapshot = snapshot.subList(0, MAX_SIZE);
        }
        return snapshot;
    }
}

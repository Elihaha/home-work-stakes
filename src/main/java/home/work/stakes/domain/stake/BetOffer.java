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

    //key: customerId, value: stake record
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

        //If current offer has less than 20 stakes and old value doesn't exist, add directly
        if (rankStore.size() < MAX_SIZE) {
            rankStore.add(stake);
            customerToStakeStore.put(customerId, stake);
            return;
        }

        //If current offer has more than 20 stakes and old value doesn't exist, only process if value is greater than rank 20
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

        // Filter out old values that may exist in extreme cases when updating the same customerId
        Map<String, Boolean> idMap = new HashMap<>();
        snapshot = snapshot.stream()
                .filter(stake -> idMap.putIfAbsent(stake.getCustomerId(), true) == null)
                .collect(Collectors.toList());

        //Due to add-then-remove operations, snapshot may have 21 entries in extreme moments
        if (snapshot.size() > MAX_SIZE) {
            snapshot = snapshot.subList(0, MAX_SIZE);
        }
        return snapshot;
    }
}

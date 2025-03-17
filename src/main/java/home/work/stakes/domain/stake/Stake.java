package home.work.stakes.domain.stake;

import java.util.Objects;

/**
 * @Author zhengxin
 * @Date 2025/3/14
 */
public class Stake implements Comparable<Stake> {

    private final int value;

    private final Integer customerId;

    public Stake(int value, Integer customerId) {
        this.value = value;
        this.customerId = customerId;
    }

    public int getValue() {
        return value;
    }

    public int getCustomerId() {
        return customerId;
    }

    @Override
    public int compareTo(Stake o) {
        int compare = Integer.compare(this.value, o.getValue());
        if (compare != 0) {
            return compare;
        }

        return Integer.compare(this.customerId, o.getCustomerId());
    }

    public String toResult() {
        return customerId + "=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Stake stake = (Stake) o;
        return value == stake.value && Objects.equals(customerId, stake.customerId);
    }

    @Override
    public int hashCode() {
        int result = value;
        result = 31 * result + Objects.hashCode(customerId);
        return result;
    }

    @Override
    public String toString() {
        return "Stake{" +
                "value=" + value +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}

package home.work.stakes;

import home.work.stakes.domain.stake.BetOffer;
import home.work.stakes.domain.stake.Stake;
import home.work.stakes.domain.stake.StakeManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhengxin
 * @Date 2025/3/16
 */
public class StakeTest {


    @Test
    public void testManagerAdd() {
        ThreadPoolExecutor executor = getExecutor();

        StakeManager stakeManager = new StakeManager();
        for (int i = 0; i < 10; i++) {
            int betOfferId = i;
            for (int j = 0; j < 10000; j++) {
                int stakeValue = j;
                executor.execute(() -> {
                    stakeManager.add(betOfferId, new Stake(stakeValue, stakeValue + ""));
                });
            }
        }

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            List<Stake> highStakes = stakeManager.getHighStakes(i);

            Assert.assertEquals(20, highStakes.size());

            for (int j = 0; j < 20; j++) {
                Stake stake = highStakes.get(j);
                Assert.assertEquals(9999 - j, stake.getValue());
                Assert.assertEquals(9999 - j + "", stake.getCustomerId());
            }

            System.out.println(highStakes);
        }
    }

    @Test
    public void testBetOfferAdd() {
        // Test Case 1: Old value exists and new value is less than old value
        BetOffer betOffer1 = mockBetOffer(20);
        Stake stake1 = new Stake(0,"customer1");
        betOffer1.addStake(stake1);

        List<Stake> snapshot = betOffer1.getSnapshot();
        Stake stake = snapshot.get(19);
        Assert.assertEquals(1, stake.getValue());
        Assert.assertEquals(20, snapshot.size());

        // Test Case 2: Old value doesn't exist and offer count is less than MAX_SIZE
        BetOffer betOffer2 = mockBetOffer(1);
        Stake stake2 = new Stake(2,"customer2");
        betOffer2.addStake(stake2);

        List<Stake> snapshot2 = betOffer2.getSnapshot();
        Assert.assertEquals(3, snapshot2.size());
        Assert.assertEquals(2, snapshot2.get(0).getValue());
        Assert.assertEquals(0, snapshot2.get(2).getValue());


        // Test Case 3: Old value doesn't exist, offer count exceeds MAX_SIZE, and new value is less than the 20th ranked value
        BetOffer betOffer3 = mockBetOffer(20);
        Stake stake3 = new Stake(-1,"customer100");
        betOffer3.addStake(stake3);

        List<Stake> snapshot3 = betOffer3.getSnapshot();
        Assert.assertEquals(20, snapshot3.size());
        Assert.assertEquals(1, snapshot3.get(19).getValue());
        Assert.assertEquals(20, snapshot3.get(0).getValue());


        // Test Case 4: Old value doesn't exist, offer count exceeds MAX_SIZE, and new value is greater than the 20th ranked value
        BetOffer betOffer4 = mockBetOffer(20);
        Stake stake4 = new Stake(21,"customer100");
        betOffer4.addStake(stake4);

        List<Stake> snapshot4 = betOffer4.getSnapshot();
        Assert.assertEquals(20, snapshot4.size());
        Assert.assertEquals(2, snapshot4.get(19).getValue());
        Assert.assertEquals(21, snapshot4.get(0).getValue());
    }

    private static BetOffer mockBetOffer(int cnt) {
        BetOffer mockBetOffer = new BetOffer(new Stake(0,"customer0"));
        for (int i = 1; i <= cnt; i++) {
            mockBetOffer.addStake(new Stake(i,"customer" + i));
        }
        return mockBetOffer;
    }


    private static ThreadPoolExecutor getExecutor() {
        return new ThreadPoolExecutor(20,
                20,
                0,
                TimeUnit.MICROSECONDS,
                new LinkedBlockingQueue<>(1000000),
                new ThreadPoolExecutor.AbortPolicy());
    }

}

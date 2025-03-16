package home.work.stakes.service;

import home.work.stakes.domain.session.Session;
import home.work.stakes.domain.session.SessionManager;
import home.work.stakes.domain.stake.Stake;
import home.work.stakes.domain.stake.StakeManager;

import java.util.List;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class BizService {

    private final SessionManager sessionManager;

    private final StakeManager stakeManager;

    public BizService(SessionManager sessionManager, StakeManager stakeManager) {
        this.sessionManager = sessionManager;
        this.stakeManager = stakeManager;
    }

    public String session(String customerId) {
        return sessionManager.createSession(customerId).getSessionKey();
    }

    public void stake(String betOfferId, String sessionKey, String stakeValue) {
        Session session = sessionManager.getSessionByKey(sessionKey);
        if (session == null) {
            return;
        }
        Stake stake = new Stake(Integer.parseInt(stakeValue), session.getCustomerId());

        stakeManager.add(Integer.parseInt(betOfferId), stake);
    }


    public String highStakes(String betOfferId) {
        List<Stake> highStakes = stakeManager.getHighStakes(Integer.parseInt(betOfferId));

        if (highStakes == null || highStakes.isEmpty()) {
            return "";
        }

        return highStakes.stream().map(Stake::toResult).collect(java.util.stream.Collectors.joining(","));
    }
}

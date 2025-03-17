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

    public String session(Integer customerId) {
        return sessionManager.createSession(customerId).getSessionKey();
    }

    public void stake(Integer betOfferId, String sessionKey, int stakeValue) {
        Session session = sessionManager.queryAndDeleteExpiredSession(sessionKey);
        if (session == null) {
            return;
        }
        Stake stake = new Stake(stakeValue, session.getCustomerId());

        stakeManager.add(betOfferId, stake);
    }


    public String highStakes(Integer betOfferId) {
        List<Stake> highStakes = stakeManager.getHighStakes(betOfferId);

        if (highStakes == null || highStakes.isEmpty()) {
            return "";
        }

        return highStakes.stream().map(Stake::toResult).collect(java.util.stream.Collectors.joining(","));
    }
}

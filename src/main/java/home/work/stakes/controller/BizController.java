package home.work.stakes.controller;

import home.work.stakes.http.HttpMapping;
import home.work.stakes.http.RequestBody;
import home.work.stakes.service.BizService;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class BizController {

    private final BizService service;

    public BizController(BizService service) {
        this.service = service;
    }

    @HttpMapping(method = "GET", url = "/{customerId}/session")
    public String session(Integer customerId) {
        if (customerId == null) {
            throw new RuntimeException("customerId is null");
        }

        return service.session(customerId);
    }

    @HttpMapping(method = "POST", url = "/{betOfferId}/stake")
    public void stake(Integer betOfferId, String sessionkey, @RequestBody Integer stake) {
        if (betOfferId == null) {
            throw new RuntimeException("betOfferId is null");
        }
        if (sessionkey == null) {
            throw new RuntimeException("sessionkey is null");
        }
        if (stake == null) {
            throw new RuntimeException("stake is null");
        }

        service.stake(betOfferId, sessionkey, stake);
    }

    @HttpMapping(method = "GET", url = "/{betOfferId}/highstakes")
    public String highStakes(Integer betOfferId) {
        if (betOfferId == null) {
            throw new RuntimeException("betOfferId is null");
        }

        return service.highStakes(betOfferId);
    }
}

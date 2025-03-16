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
    public String session(String customerId) {
        return service.session(customerId);
    }

    @HttpMapping(method = "POST", url = "/{betOfferId}/stake")
    public void stake(String betOfferId, String sessionkey, @RequestBody String stake) {
        service.stake(betOfferId, sessionkey, stake);
    }

    @HttpMapping(method = "GET", url = "/{betOfferId}/highstakes")
    public String highStakes(String betOfferId) {
        return service.highStakes(betOfferId);
    }
}

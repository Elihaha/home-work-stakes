package home.work.stakes.worker;

import home.work.stakes.domain.session.SessionManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author zhengxin
 * @Date 2025/3/16
 */
public class TimeTaskWorker {

    private final ScheduledExecutorService scheduler;

    private final SessionManager sessionManager;

    public TimeTaskWorker(SessionManager sessionManager) {
        this.sessionManager = sessionManager;

        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        Runnable task = () -> {
            System.out.println("Starting time task == remove expired session");
            sessionManager.removeExpiredSession();
            System.out.println("finished time task == remove expired session");
        };

        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
    }


}

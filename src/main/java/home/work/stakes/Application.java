package home.work.stakes;

import com.sun.net.httpserver.HttpServer;
import home.work.stakes.controller.BizController;
import home.work.stakes.domain.session.SessionManager;
import home.work.stakes.domain.stake.StakeManager;
import home.work.stakes.http.BizHttpHandler;
import home.work.stakes.service.BizService;
import home.work.stakes.worker.TimeTaskWorker;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class Application {

    public static void main(String[] args) {

        SessionManager sessionManager = new SessionManager();
        StakeManager stakeManager = new StakeManager();
        BizService service = new BizService(sessionManager, stakeManager);
        BizController controller = new BizController(service);

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new BizHttpHandler(controller));
            ExecutorService executor = Executors.newFixedThreadPool(8);
            server.setExecutor(executor);
            server.start();
            System.out.println("Server started on port 8080");
        } catch (IOException e) {
            e.printStackTrace();
        }

        new TimeTaskWorker(sessionManager).start();
    }

}

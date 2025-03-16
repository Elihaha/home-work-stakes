package home.work.stakes.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import home.work.stakes.controller.BizController;
import home.work.stakes.domain.session.SessionManager;
import home.work.stakes.domain.stake.StakeManager;
import home.work.stakes.service.BizService;
import home.work.stakes.worker.TimeTaskWorker;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class BizHttpHandler implements HttpHandler {

    Map<RequestResolver, HttpFunction> router = new HashMap<>();

    public BizHttpHandler(BizController controller) {
        Class<?> clazz = BizController.class;

        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            RequestResolver resolver = new RequestResolver(method);

            router.put(resolver, (o) -> {
                try {
                    Object result = method.invoke(controller, o);
                    if(result == null) {
                        return "";
                    }
                    return result.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return "error";
            });
        }


    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

        for (Map.Entry<RequestResolver, HttpFunction> entry : router.entrySet()) {
            RequestResolver resolver = entry.getKey();
            HttpFunction function = entry.getValue();
            if (resolver.isMatch(httpExchange)) {
                String response = function.apply(resolver.getVariables(httpExchange));
                httpExchange.sendResponseHeaders(200, response.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                return;
            }
        }

        httpExchange.sendResponseHeaders(404, 0);
        httpExchange.getResponseBody().close();
    }

}

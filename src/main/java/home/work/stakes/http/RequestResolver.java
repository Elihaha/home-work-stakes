package home.work.stakes.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class RequestResolver {
    private Pattern pattern;
    private String[] pathVariables;
    private String method;
    private String bodyParam;

    private final List<String> methodParams;

    public RequestResolver(Method method) {
        buildBodyParams(method);

        buildPathParams(method);

        methodParams = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.toList());
    }


    private void buildPathParams(Method method) {
        HttpMapping httpMapping = method.getAnnotation(HttpMapping.class);
        String httpMethod = httpMapping.method();
        String url = httpMapping.url();
        // 构建正则表达式模式和变量名数组
        StringBuilder regex = new StringBuilder();
        List<String> variables = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{(\\w+)}").matcher(url);
        int lastIndex = 0;
        while (matcher.find()) {
            regex.append(Pattern.quote(url.substring(lastIndex, matcher.start())));
            regex.append("([^/]+)");
            variables.add(matcher.group(1));
            lastIndex = matcher.end();
        }
        regex.append(Pattern.quote(url.substring(lastIndex)));
        this.pattern = Pattern.compile(regex.toString());
        this.pathVariables = variables.toArray(new String[0]);
        this.method = httpMethod;
    }

    private void buildBodyParams(Method method) {
        Parameter[] parameters = method.getParameters();
        String bodyParam = null;

        for (Parameter parameter : parameters) {
            RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
            if (requestBody != null) {
                if (bodyParam != null) {
                    throw new IllegalArgumentException("Only one @RequestBody annotation is allowed per method.");
                }
                bodyParam = parameter.getName();
            }
        }
        this.bodyParam = bodyParam;
    }

    // 匹配  路径并返回变量值
    private void parsePathVariables(HttpExchange httpExchange, Map<String, String> variables) {
        Matcher matcher = pattern.matcher(httpExchange.getRequestURI().getPath());
        if (matcher.matches()) {
            for (int i = 0; i < pathVariables.length; i++) {
                variables.put(pathVariables[i], matcher.group(i + 1));
            }
        }
    }

    public boolean isMatch(HttpExchange httpExchange) {
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        return method.equals(this.method) && pattern.matcher(path).matches();
    }

    public String[] getVariables( HttpExchange httpExchange) {
        Map<String, String> variables = new HashMap<>();

        parsePathVariables(httpExchange, variables);

        parseParamVariables(httpExchange, variables);

        parseBodyVariables(httpExchange, variables);

        return methodParams.stream().map(variables::get).toArray(String[]::new);
    }

    private static void parseParamVariables(HttpExchange httpExchange, Map<String, String> variables) {
        String query = httpExchange.getRequestURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    variables.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    private void parseBodyVariables(HttpExchange httpExchange, Map<String, String> variableMap) {
        if ("POST".equals(method)) {
            // 读取请求体
            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            variableMap.put(bodyParam, requestBody.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RequestResolver that = (RequestResolver) o;
        return Objects.equals(pattern, that.pattern) && Objects.deepEquals(pathVariables, that.pathVariables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, Arrays.hashCode(pathVariables));
    }
}

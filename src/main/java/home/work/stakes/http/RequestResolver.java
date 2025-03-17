package home.work.stakes.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A resolver class for handling HTTP requests and extracting request parameters.
 *
 * @Author zhengxin
 * @Date 2025/3/13
 */
public class RequestResolver {
    private Pattern pattern;
    private String[] pathVariables;
    private String method;
    private String bodyParam;

    private final Parameter[] methodParams;

    /**
     * key: method param type
     * value: converter function
     */
    private final Map<Class, Function<String, Object>> converterMap = new HashMap<>();


    public RequestResolver(Method method) {
        buildBodyParams(method);

        buildPathParams(method);

        methodParams = method.getParameters();

        converterMap.put(Integer.class, Integer::parseInt);
        converterMap.put(String.class, String::valueOf);
    }


    private void buildPathParams(Method method) {
        HttpMapping httpMapping = method.getAnnotation(HttpMapping.class);
        String httpMethod = httpMapping.method();
        String url = httpMapping.url();
        // Build regex pattern and variable name array
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

    // Match path and return variable values
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

    public Object[] getVariables(HttpExchange httpExchange) {
        Map<String, String> variables = new HashMap<>();

        parsePathVariables(httpExchange, variables);

        parseParamVariables(httpExchange, variables);

        parseBodyVariables(httpExchange, variables);

        return Arrays.stream(methodParams).map(p -> pickAndConvert(p, variables)).toArray(Object[]::new);
    }

    private Object pickAndConvert(Parameter p, Map<String, String> variables) {
        String name = p.getName();
        String value = variables.get(name);
        if (value == null) {
            return null;
        } else {
            Function<String, Object> converter = converterMap.get(p.getType());
            return converter.apply(value);
        }
    }

    private void parseParamVariables(HttpExchange httpExchange, Map<String, String> variables) {
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

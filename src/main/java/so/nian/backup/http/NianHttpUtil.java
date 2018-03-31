package so.nian.backup.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.utils.ExpressionParser;
import so.nian.backup.utils.StringUtil;

import javax.net.ssl.SSLException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class NianHttpUtil {

    private static Logger logger = LoggerFactory.getLogger(NianHttpUtil.class);

    public static final Map<String, String> HEADERS = new HashMap<>();
    public static final Map<String, String> LOGINFO = new HashMap<>();
    public static final Map<String, String> URLS = new HashMap<>();
    public static final Map<String, String> PROC = new HashMap<>();
    private static final ExpressionParser parser = ExpressionParser.getDefault();
    private static RequestConfig requestConfig = null;
    private static CloseableHttpClient httpClient;
    private static PoolingHttpClientConnectionManager apiConnectionManager;
    //自定义重试策略
    private static HttpRequestRetryHandler apiRetryHandler = (exception, executionCount, context) -> {
        if (executionCount >= 5) return false;
        if (exception instanceof InterruptedIOException) return false;
        if (exception instanceof UnknownHostException) return false;
        if (exception instanceof ConnectTimeoutException) return false;
        if (exception instanceof SSLException) return false;

        HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
        if (idempotent) {
            return true;
        }
        return false;
    };

    static {

        //HEADERS.put("Host", "api.nian.so");
        HEADERS.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        HEADERS.put("User-Agent", "NianiOS/5.0.3 (iPhone; iOS 11.2.6; Scale/2.00)");
        HEADERS.put("Accept", "*/*");
        HEADERS.put("Accept-Language", "zh-Hans-CN;q=1, zh-Hant-CN;q=0.9");
        HEADERS.put("Cookie", String.format("flash=%s0; __utma=6360749.1361854274.1491830767.1491830767.1491830767.1", System.currentTimeMillis()));
        //HEADERS.put("Connection", "close");

        URLS.put("login", "http://api.nian.so/user/login");
        URLS.put("info", "http://api.nian.so/user/${euid}?uid=${uid}&shell=${shell}");
        URLS.put("list", "http://api.nian.so/user/${euid}/dreams?uid=${uid}&shell=${shell}");
        URLS.put("data", "http://api.nian.so/v2/multidream/${dreamid}?sort=desc&page=${page}&uid=${uid}&shell=${shell}");
        URLS.put("cmts", "http://api.nian.so/step/${stepid}/comments?page=${page}&uid=${uid}&shell=${shell}");
        URLS.put("like", "http://api.nian.so/v2/step/${stepid}/like/users?page=${page}&uid=${uid}&shell=${shell}");
        URLS.put("care", "http://nian.so/api/user_fo_list2.php?page=${page}&uid=${euid}&myuid=${uid}");
        URLS.put("fans", "http://nian.so/api/user_foed_list2.php?page=${page}&uid=${euid}&myuid=${uid}");
        URLS.put("dlike", "http://api.nian.so/multidream/${dreamid}/likes?page=${page}&uid=${uid}&shell=${shell}");
        URLS.put("dfans", "http://api.nian.so/multidream/${dreamid}/followers?page=${page}&uid=${uid}&shell=${shell}");

        PROC.put("login", "login");
        PROC.put("info", "info?${uid}/${shell}/${euid}");
        PROC.put("list", "list?${uid}/${shell}/${euid}");
        PROC.put("data", "data?${uid}/${shell}/${dreamid}${page}");
        PROC.put("cmts", "cmts?${uid}/${euid}/${stepid}/${page}");
        PROC.put("like", "like?${uid}/${euid}/${stepid}/${page}");
        PROC.put("care", "care?${uid}/${euid}/${page}");
        PROC.put("fans", "fans?${uid}/${euid}/${page}");

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000)
                .setConnectionRequestTimeout(300000)
                //.setSocketTimeout(Config.getInt("httpclient.socket.timeout"))
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();

        /*
        ConnectionSocketFactory plainSocketFactory = PlainConnectionSocketFactory.getSocketFactory();
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, new NianSSLTrustStrategy())
                .build();
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", plainSocketFactory)
                .register("https", sslSocketFactory)
                .build();
        */
        apiConnectionManager = new PoolingHttpClientConnectionManager();
        // Increase max total connection
        apiConnectionManager.setMaxTotal(400);
        // Increase default max connection per route
        apiConnectionManager.setDefaultMaxPerRoute(20);
        // Increase max connections for api.nian.so:80 to 50
        apiConnectionManager.setMaxPerRoute(new HttpRoute(new HttpHost("nian.so", 80)), 40);
        apiConnectionManager.setMaxPerRoute(new HttpRoute(new HttpHost("api.nian.so", 80)), 80);
        httpClient = HttpClients.custom()
                .setConnectionManager(apiConnectionManager)
                .setRetryHandler(apiRetryHandler)
                .build();

    }

    private static CloseableHttpClient getApitHttpClient() {
        return httpClient;
    }

    private static void FillHttpHeaders(HttpUriRequest request) {
        for (String key : HEADERS.keySet())
            request.setHeader(key, HEADERS.get(key));
    }

    /**
     * 登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return
     */
    public static HttpResultEntity login(String email, String password) {
        try {
            String url = URLS.get("login");
            String body = String.format("email=%s&password=%s", email, StringUtil.md5("n*A" + password));
            HttpResultEntity resultEntity = exec("post", url, body, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                    LOGINFO.put("uid", String.valueOf(data.get("uid")));
                    LOGINFO.put("name", String.valueOf(data.get("name")));
                    LOGINFO.put("shell", String.valueOf(data.get("shell")));
                }
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]登录异常：%s", e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity process(String type, Map<String, String> args) {
        try {
            String url = URLS.get(type);
            String key = PROC.get(type);
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.putAll(args);
            url = parser.parse(url, parameters);
            logger.info(String.format("HTTP [URL=%s, PARAMETERS=%s]", url, parameters));
            HttpResultEntity resultEntity = exec("get", url, null, null);
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("PROCESS ERROR[%s]：%s", type, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity info(String euid) {
        try {
            String url = URLS.get("info");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("euid", euid);
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取个人信息异常：%s", e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity dreams(String euid) {
        try {
            String url = URLS.get("list");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("euid", euid);
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本列表异常[%s]：%s", euid, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity steps(String dreamid, int page) {
        try {
            String url = URLS.get("data");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("dreamid", dreamid);
            parameters.put("page", String.valueOf(page));
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本进展异常[%s,%d]：%s", dreamid, page, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity comments(String stepid, int page) {
        try {
            String url = URLS.get("cmts");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("stepid", stepid);
            parameters.put("page", String.valueOf(page));
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取进展评论异常[%s,%d]：%s", stepid, page, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity like(String stepid, int page) {
        try {
            String url = URLS.get("like");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("stepid", stepid);
            parameters.put("page", String.valueOf(page));
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取进展点赞异常[%s,%d]：%s", stepid, page, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity care(String euid, int page) {
        try {
            String url = URLS.get("care");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("euid", euid);
            parameters.put("page", String.valueOf(page));
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取用户关注异常[%s,%d]：%s", euid, page, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity fans(String euid, int page) {
        try {
            String url = URLS.get("fans");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("euid", euid);
            parameters.put("page", String.valueOf(page));
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取听众异常[%s,%d]：%s", euid, page, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity dlike(String dreamid, int page) {
        try {
            String url = URLS.get("dlike");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("dreamid", dreamid);
            parameters.put("page", String.valueOf(page));
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本点赞异常[%s,%d]：%s", dreamid, page, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity dfans(String dreamid, int page) {
        try {
            String url = URLS.get("dfans");
            Map<String, String> parameters = new HashMap<>();
            parameters.putAll(LOGINFO);
            parameters.put("dreamid", dreamid);
            parameters.put("page", String.valueOf(page));
            url = parser.parse(url, parameters);
            HttpResultEntity resultEntity = new HttpResultEntity(false, "开始下载");
            while (!resultEntity.isSuccess()) {
                logger.info(String.format("GET [%s]: %s", url, resultEntity.getMessage()));
                resultEntity = exec("get", url, null, null);
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本关注异常[%s,%d]：%s", dreamid, page, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity exec(String method, String url, String body, Map<String, String> headers) {
        CloseableHttpClient httpClient = getApitHttpClient();
        try {
            // 检查METHOD
            HttpUriRequest request = null;
            if ("get".equalsIgnoreCase(method)) {
                request = new HttpGet(url);
                ((HttpGet) request).setConfig(requestConfig);
            }
            if ("post".equalsIgnoreCase(method)) {
                request = new HttpPost(url);
                ((HttpPost) request).setConfig(requestConfig);
                if (!StringUtil.isNullOrEmpty(body)) {
                    // HTTP BODY
                    HttpEntity httpEntity = new StringEntity(body);
                    ((HttpPost) request).setEntity(httpEntity);
                }
            }
            if (request == null)
                throw new RuntimeException("METHOD ERROR, NEED GET/POST.");
            // 填充HTTP头
            FillHttpHeaders(request);
            if (headers != null)
                for (String key : headers.keySet())
                    request.setHeader(key, headers.get(key));

            HttpResultEntity resultEntity = httpClient.execute(request, new NianJsonHandler());
            return resultEntity;

        } catch (Exception e) {
            logger.error(String.format("HTTP请求异常：%s", e.getMessage()));
            throw new RuntimeException(e);
        } finally {
        }
    }

    public static Object mapget(Map<?, ?> data, String path) {
        if (data == null) return null;
        if (StringUtil.isNullOrEmpty(path)) return data;
        if (!path.contains("/"))
            return data.get(path);
        int idx = path.indexOf("/");
        String prefix = path.substring(0, idx);
        String suffix = path.substring(idx + 1);
        Object subdata = data.get(prefix);
        if (subdata == null) return null;
        if (StringUtil.isNullOrEmpty(suffix))
            return subdata;
        if (subdata instanceof Map) {
            return mapget((Map<?, ?>) subdata, suffix);
        } else {
            throw new RuntimeException(String.format("%s is not a java.util.Map", prefix));
        }
    }

    public static void closeQuitely(Closeable closeable) {
        try {
            closeable.close();
        } catch (Exception e) {
        }
    }
}

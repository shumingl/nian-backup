package so.nian.backup.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.utils.ExpressionParser;
import so.nian.backup.utils.StringUtil;

import java.io.Closeable;
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

    static {

        //HEADERS.put("Host", "api.nian.so");
        HEADERS.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        HEADERS.put("User-Agent", "NianiOS/5.0.3 (iPhone; iOS 11.2.6; Scale/2.00)");
        HEADERS.put("Accept", "*/*");
        HEADERS.put("Accept-Language", "zh-Hans-CN;q=1, zh-Hant-CN;q=0.9");
        HEADERS.put("Cookie", String.format("flash=%s0; __utma=6360749.1361854274.1491830767.1491830767.1491830767.1", System.currentTimeMillis()));

        URLS.put("login", "http://api.nian.so/user/login");
        URLS.put("info", "http://api.nian.so/user/${euid}?uid=${uid}&shell=${shell}");
        URLS.put("list", "http://api.nian.so/user/${euid}/dreams?uid=${uid}&shell=${shell}");
        URLS.put("data", "http://api.nian.so/v2/multidream/${dreamid}?sort=desc&page=${page}&uid=${uid}&shell=${shell}");
        URLS.put("cmts", "http://api.nian.so/step/${stepid}/comments?page=${page}&uid=${uid}&shell=${shell}");
        URLS.put("like", "http://api.nian.so/v2/step/${stepid}/like/users?page=${page}&uid=${uid}&shell=${shell}");
        URLS.put("care", "http://nian.so/api/user_fo_list2.php?page=${page}&uid=${euid}&myuid=${uid}");
        URLS.put("fans", "http://nian.so/api/user_foed_list2.php?page=${page}&uid=${euid}&myuid=${uid}");
        URLS.put("head", "http://img.nian.so/head/${uid}.jpg");
        URLS.put("step", "http://img.nian.so/step/${image}");

        PROC.put("login", "login");
        PROC.put("info", "info?${uid}/${shell}/${euid}");
        PROC.put("list", "list?${uid}/${shell}/${euid}");
        PROC.put("data", "data?${uid}/${shell}/${dreamid}${page}");
        PROC.put("cmts", "cmts?${uid}/${euid}/${stepid}/${page}");
        PROC.put("like", "like?${uid}/${euid}/${stepid}/${page}");
        PROC.put("care", "care?${uid}/${euid}/${page}");
        PROC.put("fans", "fans?${uid}/${euid}/${page}");
        PROC.put("head", "head?${uid}");
        PROC.put("image", "image?${image}");

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(100000)
                //.setConnectionRequestTimeout(Config.getInt("httpclient.request.timeout"))
                //.setSocketTimeout(Config.getInt("httpclient.socket.timeout"))
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();
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
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
            }
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
            HttpResultEntity resultEntity = exec("get", url, null, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
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
            HttpResultEntity resultEntity = exec("get", url, null, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本列表异常：%s", e.getMessage()));
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
            HttpResultEntity resultEntity = exec("get", url, null, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本列表异常：%s", e.getMessage()));
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
            HttpResultEntity resultEntity = exec("get", url, null, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本列表异常：%s", e.getMessage()));
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
            HttpResultEntity resultEntity = exec("get", url, null, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取记本列表异常：%s", e.getMessage()));
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
            HttpResultEntity resultEntity = exec("get", url, null, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取关注异常：%s", e.getMessage()));
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
            HttpResultEntity resultEntity = exec("get", url, null, null);
            if (resultEntity.isSuccess()) {
                Map<String, Object> map = resultEntity.getResponseMap();
                if (map != null && map.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) map.get("data");
                }
            }
            return resultEntity;
        } catch (Exception e) {
            logger.error(String.format("[NIAN]获取听众异常：%s", e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        }
    }

    public static HttpResultEntity exec(String method, String url, String body, Map<String, String> headers) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
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
            // return new HttpResultEntity(false, e.getMessage());
        } finally {
            NianHttpUtil.closeQuitely(httpClient);
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
package so.nian.backup.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import so.nian.backup.utils.jackson.JsonUtil;

import java.util.Map;

/**
 * 已封装过的HTTP返回结果类
 *
 * @author shumingl
 */
public class HttpResultEntity {
    private boolean success;
    private int statusCode;
    private String message;
    private String statusText;
    private String responseBody;
    private Map<String, Object> responseMap;
    private HttpResponse response;

    public HttpResultEntity() {
    }

    public HttpResultEntity(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public HttpResultEntity(HttpResponse response) {

        this.response = response;
        this.statusCode = response.getStatusLine().getStatusCode();
        this.statusText = response.getStatusLine().getReasonPhrase();
        try {
            if (statusCode == 200) {//200为成功状态
                this.success = true;
                HttpEntity entity = response.getEntity();
                responseBody = EntityUtils.toString(entity);
                responseMap = JsonUtil.json2Map(responseBody);
                EntityUtils.consume(entity);
            } else {
                this.success = false;
            }
        } catch (Exception e) {
            this.success = false;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody.replace("&amp;nbsp;", " ").replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">");
        responseMap = JsonUtil.json2Map(this.responseBody);
    }

    public Map<String, Object> getResponseMap() {
        return responseMap;
    }

    public void setResponseMap(Map<String, Object> responseMap) {
        this.responseMap = responseMap;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setResponse(HttpResponse response) {
        this.response = response;
    }
}

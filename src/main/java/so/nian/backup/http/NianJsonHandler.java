package so.nian.backup.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class NianJsonHandler implements ResponseHandler<HttpResultEntity> {
    private static Logger logger = LoggerFactory.getLogger(NianJsonHandler.class);

    @Override
    public HttpResultEntity handleResponse(final HttpResponse response) throws IOException {
        response.setHeader("Content-Type", "text/html; charset=UTF-8");
        HttpResultEntity result = new HttpResultEntity();
        StatusLine statusLine = response.getStatusLine();
        int status = statusLine.getStatusCode();
        result.setStatusCode(status);
        result.setStatusText(statusLine.getReasonPhrase());
        result.setMessage(statusLine.toString());
        result.setResponse(response);
        if (status >= 200 && status < 300) {//[200,300)为成功状态
            HttpEntity entity = response.getEntity();
            String body = EntityUtils.toString(entity, "UTF-8");
            result.setResponseBody(body);
            Map<String, Object> map = result.getResponseMap();
            if (map == null) {
                result.setSuccess(false);
                result.setMessage("转换Map数据为空");
            } else {
                result.setSuccess(true);
            }
            EntityUtils.consume(entity);
        } else {
            result.setSuccess(false);
            logger.error(String.format("HTTP请求失败：%s", response.toString()));
        }
        return result;
    }
}

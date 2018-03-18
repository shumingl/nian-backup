package so.nian.backup.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NianJsonHandler implements ResponseHandler<HttpResultEntity> {
    private static Logger logger = LoggerFactory.getLogger(NianJsonHandler.class);

    @Override
    public HttpResultEntity handleResponse(final HttpResponse response) throws IOException {
        HttpResultEntity result = new HttpResultEntity();
        int status = response.getStatusLine().getStatusCode();
        String cause = response.getStatusLine().getReasonPhrase();
        result.setStatusCode(status);
        result.setStatusText(cause);
        result.setResponse(response);
        result.setResponseBody(EntityUtils.toString(response.getEntity()));
        if (status >= 200 && status < 300) {//[200,300)为成功状态
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            result.setSuccess(true);
        } else {
            result.setSuccess(false);
            logger.error(String.format("HTTP请求失败：%s", response.toString()));
        }
        return result;
    }
}

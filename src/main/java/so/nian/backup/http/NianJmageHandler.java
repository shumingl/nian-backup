package so.nian.backup.http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class NianJmageHandler implements ResponseHandler<HttpResultEntity> {
    private static Logger logger = LoggerFactory.getLogger(NianJmageHandler.class);

    @Override
    public HttpResultEntity handleResponse(final HttpResponse response) throws IOException {
        HttpResultEntity result = new HttpResultEntity();
        int status = response.getStatusLine().getStatusCode();
        String cause = response.getStatusLine().getReasonPhrase();
        result.setStatusCode(status);
        result.setStatusText(cause);
        result.setResponse(response);
        if (status >= 200 && status < 300) {//[200,300)为成功状态
            HttpEntity entity = response.getEntity();
            EntityUtils.consume(entity);
            result.setSuccess(true);
        } else {
            result.setSuccess(false);
            logger.error(String.format("文件下载失败：%s", response.toString()));
        }
        return result;
    }
}

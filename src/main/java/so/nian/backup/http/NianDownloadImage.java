package so.nian.backup.http;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.utils.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NianDownloadImage {

    private static Logger logger = LoggerFactory.getLogger(NianHttpUtil.class);

    private static RequestConfig requestConfig = null;

    static {

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(100000)
                //.setConnectionRequestTimeout(Config.getInt("httpclient.request.timeout"))
                //.setSocketTimeout(Config.getInt("httpclient.socket.timeout"))
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();
    }

    public static HttpResultEntity download(String type, String image) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // head/step/dream/cover
            String url = String.format("http://img.nian.so/%s/%s", type, image);
            // 检查METHOD
            HttpGet request = new HttpGet(url);
            request.setConfig(requestConfig);
            // 填充HTTP头
            request.setHeader("User-Agent", "NianiOS/5.0.3 (iPhone; iOS 11.2.6; Scale/2.00)");
            HttpResultEntity resultEntity = httpClient.execute(request, new NianImageHandler());
            String path = AppConfig.getNianImageBase();
            String imagepath = StringUtil.generatePath(path, type, image);
            File imagefile = new File(imagepath);

            // 文件不存在则下载图片
            if (!imagefile.exists())
                write2file(resultEntity.getResponse().getEntity().getContent(), imagefile);
            return resultEntity;

        } catch (Exception e) {
            logger.error(String.format("图片下载异常：%s", e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            NianHttpUtil.closeQuitely(httpClient);
        }
    }

    public static HttpResultEntity downloadThumbs(String type, String image) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // head/step/dream/cover
            String suffix = "";
            if ("head".equals(type)) suffix = "dream";
            if ("step".equals(type)) suffix = "200x";
            if ("cover".equals(type)) suffix = "cover";
            if ("dream".equals(type)) suffix = "dream";

            String url = String.format("http://img.nian.so/%s/%s!%s", type, image, suffix);
            // 检查METHOD
            HttpGet request = new HttpGet(url);
            request.setConfig(requestConfig);
            // 填充HTTP头
            request.setHeader("User-Agent", "NianiOS/5.0.3 (iPhone; iOS 11.2.6; Scale/2.00)");
            HttpResultEntity resultEntity = httpClient.execute(request, new NianImageHandler());
            String path = AppConfig.getNianImageBase();
            String imagepath = StringUtil.generatePath(path, type, image);
            File imagefile = new File(imagepath);

            // 文件不存在则下载图片
            if (!imagefile.exists())
                write2file(resultEntity.getResponse().getEntity().getContent(), imagefile);
            return resultEntity;

        } catch (Exception e) {
            logger.error(String.format("图片下载异常：%s", e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            NianHttpUtil.closeQuitely(httpClient);
        }
    }

    private static void write2file(InputStream istream, File file) throws IOException {
        FileOutputStream fostream = new FileOutputStream(file);
        int ret;
        byte[] buffer = new byte[8192];
        while ((ret = istream.read(buffer)) > -1) {
            if (ret > 0)
                fostream.write(buffer, 0, ret);
        }
        fostream.flush();
        istream.close();
        fostream.close();
    }

}

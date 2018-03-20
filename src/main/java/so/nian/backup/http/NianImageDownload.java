package so.nian.backup.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NianImageDownload {

    private static Logger logger = LoggerFactory.getLogger(NianHttpUtil.class);

    private static RequestConfig requestConfig = null;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(100);

    static {

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(100000)
                //.setConnectionRequestTimeout(Config.getInt("httpclient.request.timeout"))
                //.setSocketTimeout(Config.getInt("httpclient.socket.timeout"))
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();
    }

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    public static void download(String type, String image) {
        threadPool.execute(new NianImageDownloadWorker(type, image));
    }

    public static HttpResultEntity downloadImage(String type, String image) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // head/step/dream/cover
            String url = String.format("http://img.nian.so/%s/%s", type, image);
            // 检查METHOD
            HttpGet request = new HttpGet(url);
            request.setConfig(requestConfig);
            // 填充HTTP头
            request.setHeader("User-Agent", "NianiOS/5.0.3 (iPhone; iOS 11.2.6; Scale/2.00)");
            CloseableHttpResponse response = httpClient.execute(request);
            String path = AppConfig.getNianImageBase();
            String imagepath = StringUtil.generatePath(path, type, image);
            File imagefile = new File(imagepath);

            // 文件不存在则下载图片
            if (!imagefile.exists() || imagefile.length() == 0)
                write2file(response.getEntity().getContent(), imagefile);
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

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
            CloseableHttpResponse response = httpClient.execute(request);
            String path = AppConfig.getNianImageBase();
            String imagepath = StringUtil.generatePath(path, "thumbs", image);
            File imagefile = new File(imagepath);

            // 文件不存在则下载图片
            if (!imagefile.exists() || imagefile.length() == 0)
                write2file(response.getEntity().getContent(), imagefile);
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

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
        while ((ret = istream.read(buffer)) != -1) {
            //System.out.printf("ret=%s\n", ret);
            if (ret > 0)
                fostream.write(buffer, 0, ret);
        }
        fostream.flush();
        fostream.close();
    }

}

class NianImageDownloadWorker extends Thread {
    private String type;
    private String image;

    private static Logger logger = LoggerFactory.getLogger(NianImageDownloadWorker.class);

    public NianImageDownloadWorker(String type, String image) {
        this.type = type;
        this.image = image;
    }

    public void run() {
        try {
            HttpResultEntity imageEntity = NianImageDownload.downloadImage(type, image);
            HttpResultEntity thumbsEntity = NianImageDownload.downloadImage(type, image);
            if (imageEntity.isSuccess() && thumbsEntity.isSuccess()) {
                logger.info(String.format("SUCC: type=[%s],image=[%s]", type, image));
            } else {
                logger.info(String.format("FAIL: type=[%s],image=[%s][%s/%s]", type, image, imageEntity.getMessage(), thumbsEntity.getMessage()));
            }
        } catch (Exception e) {
            logger.info(String.format("FAIL: type=[%s],image=[%s][%s]", type, image, e.getMessage()));
        }
    }
}
package so.nian.backup.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

public class NianImageDownload {

    private static Logger logger = LoggerFactory.getLogger(NianImageDownload.class);

    private static RequestConfig requestConfig = null;
    private static ExecutorService threadPool = null;

    static {

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000)
                .setConnectionRequestTimeout(300000)
                .setSocketTimeout(600000)
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();
        //threadPool = Executors.newFixedThreadPool(50);
        threadPool = new ThreadPoolExecutor(32, 64, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public static void download(String type, String image) {
        String path = AppConfig.getNianImageBase();
        String imagepath = StringUtil.generatePath(path, type, image);
        File imagefile = new File(imagepath);
        if (!imagefile.exists() || imagefile.length() == 0) {
            threadPool.execute(new NianImageDownloadWorker(type, image));
        }
    }

    public static void shutdown() {
        while (!threadPool.isTerminated()) {
            threadPool.shutdown();
        }
    }

    public static HttpResultEntity downloadImage(String type, String image) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            String path = AppConfig.getNianImageBase();
            String imagepath = StringUtil.generatePath(path, type, image);
            File imagefile = new File(imagepath);

            // 文件不存在则下载图片
            if (!imagefile.exists() || imagefile.length() == 0) {
                // head/step/dream/cover
                String url = String.format("http://img.nian.so/%s/%s", type, image);
                // 检查METHOD
                HttpGet request = new HttpGet(url);
                request.setConfig(requestConfig);
                // 填充HTTP头
                request.setHeader("User-Agent", "NianiOS/5.0.3 (iPhone; iOS 11.2.6; Scale/2.00)");
                CloseableHttpResponse response = httpClient.execute(request);
                FileUtil.write2file(response.getEntity().getContent(), imagefile);
            }
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
            String path = AppConfig.getNianImageBase();
            String imagepath = StringUtil.generatePath(path, "thumbs", image);
            File imagefile = new File(imagepath);

            // 文件不存在则下载图片
            if (!imagefile.exists() || imagefile.length() == 0) {
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
                FileUtil.write2file(response.getEntity().getContent(), imagefile);
            }
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

        } catch (Exception e) {
            logger.error(String.format("图片下载异常[thumbs:%s/%s]：[%s]%s", type, image, e.getClass().getCanonicalName(), e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            NianHttpUtil.closeQuitely(httpClient);
        }
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

    @Override
    public void run() {
        try {
            HttpResultEntity thumbsEntity = NianImageDownload.downloadThumbs(type, image);
            if (thumbsEntity.isSuccess()) {
                HttpResultEntity imageEntity = NianImageDownload.downloadImage(type, image);
                if (imageEntity.isSuccess()) {
                    logger.info(String.format("SUCC: image=[%s/%s]", type, image));
                } else {
                    logger.info(String.format("FAIL: image=[%s/%s][%s]", type, image, imageEntity.getMessage()));
                }
            } else {
                logger.info(String.format("FAIL: image=[thumbs:%s/%s][%s]", type, image, thumbsEntity.getMessage()));
            }
        } catch (Exception e) {
            logger.info(String.format("FAIL: image=[%s/%s][%s]", type, image, e.getMessage()));
        }
    }
}
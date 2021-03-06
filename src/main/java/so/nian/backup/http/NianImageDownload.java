package so.nian.backup.http;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.bizz.service.NamedThreadFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class NianImageDownload {

    private static Logger logger = LoggerFactory.getLogger(NianImageDownload.class);

    private static RequestConfig requestConfig = null;
    private static ThreadPoolExecutor imageThreadPool = null;
    private static Map<String, Integer> failedImages;//记录失败次数
    private static Map<String, Integer> imageStatus;//记录图片状态(进行中为0，成功为1，失败为-1)
    private static final int MAX_RETRY = 32;
    private static CloseableHttpClient httpClient;
    private static final ReentrantLock imageLock = new ReentrantLock();

    //自定义重试策略
    private static HttpRequestRetryHandler imageRetryHandler = (exception, executionCount, context) -> {
        if (executionCount >= 10) return false;
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

    public static void startup(int imageSize) {

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(30000)
                .setConnectionRequestTimeout(300000)
                .setSocketTimeout(600000)
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();
        imageThreadPool = new ThreadPoolExecutor(imageSize, imageSize * 2, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory("IMGS"));

        PoolingHttpClientConnectionManager imageConnectionManager = new PoolingHttpClientConnectionManager();
        imageConnectionManager.setMaxTotal(512);
        imageConnectionManager.setDefaultMaxPerRoute(40);
        imageConnectionManager.setMaxPerRoute(new HttpRoute(new HttpHost("img.nian.so", 80)), 100);
        httpClient = HttpClients.custom()
                .setConnectionManager(imageConnectionManager)
                .setRetryHandler(imageRetryHandler)
                .build();
        failedImages = new ConcurrentHashMap<>();
        imageStatus = new ConcurrentHashMap<>();
    }


    public static synchronized boolean recordFailedImage(String imageinfo) {
        if (failedImages.containsKey(imageinfo)) {
            int retry = failedImages.get(imageinfo);
            if (retry + 1 > MAX_RETRY)
                return false;
            failedImages.put(imageinfo, retry + 1);
        } else {
            failedImages.put(imageinfo, 1);
        }
        return true;
    }

    public static void removeFailedImage(String imageinfo) {
        if (failedImages.containsKey(imageinfo))
            failedImages.remove(imageinfo);
    }

    public static void shutdownPool() {
        long finished = 0;
        boolean shutdown = false;
        while (!imageThreadPool.isTerminated()) {
            if (shutdown && finished != imageThreadPool.getCompletedTaskCount()) {
                logger.info(String.format("IMAGE-ThreadPool: RUNNING(%d), STATUS(%d/%d), QUEUE(%d).",
                        imageThreadPool.getActiveCount(),
                        imageThreadPool.getCompletedTaskCount(),
                        imageThreadPool.getTaskCount(),
                        imageThreadPool.getQueue().size()));
                finished = imageThreadPool.getCompletedTaskCount();
            }
            if (!shutdown && imageThreadPool.getActiveCount() == 0 && imageThreadPool.getQueue().size() == 0) {
                imageThreadPool.shutdown();
                shutdown = true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(String.format("NianImageDownload.shutdownPool->Thread.sleep();[%s]", e.getMessage()));
            }
        }
    }

    public static void download(String userid, String type, String image) {
        download(userid, type, image, false);
    }

    public static HttpResultEntity downloadThumbs(String userid, String type, String image) {
        return downloadThumbs(userid, type, image, false);
    }

    public static HttpResultEntity downloadImage(String userid, String type, String image) {
        return downloadImage(userid, type, image, false);
    }

    public static ThreadPoolExecutor getImageThreadPool() {
        return imageThreadPool;
    }

    public static void setImageStatus(String userid, String type, String image, Integer status) {
        imageLock.lock();
        try {
            String imageKey = String.format("%s/%s/%s", userid, type, image);
            imageStatus.put(imageKey, status);
        } finally {
            imageLock.unlock();
        }
    }

    public static void download(String userid, String type, String image, boolean iscover) {
        String model = AppConfig.getNianRenderModel();
        if ("offline".equals(model)) return;
        imageLock.lock();
        try { // 对图片状态的访问需加锁，否则可能会导致图片重复下载，磁盘高IO
            String imageKey = String.format("%s/%s/%s", userid, type, image);
            if (imageStatus.containsKey(imageKey) && imageStatus.get(imageKey) >= 0)// 已经成功的图片不再下载
                return;
            imageStatus.put(imageKey, 0);
        } finally {
            imageLock.unlock();
        }
        String backupbase = AppConfig.getNianViewsBase();
        File imagefile = new File(StringUtil.path(backupbase, userid, "images", type, image));
        File thumbsfile = new File(StringUtil.path(backupbase, userid, "images/thumbs", image));
        if (iscover || !imagefile.exists() || imagefile.length() == 0
                || !thumbsfile.exists() || thumbsfile.length() == 0) {
            imageThreadPool.execute(new NianImageDownloadWorker(userid, type, image, iscover));
        }
    }

    public static HttpResultEntity downloadImage(String userid, String type, String image, boolean iscover) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            if (StringUtil.isNullOrEmpty(image)) {
                return new HttpResultEntity(true, "IMAGE参数为空，跳过下载。");
            }
            String backupbase = AppConfig.getNianViewsBase();
            String imagepath = StringUtil.path(backupbase, userid, "images", type, image);
            File imagefile = new File(imagepath);
            FileUtil.createParentDirs(imagefile);

            // 文件不存在则下载图片
            if (iscover || !imagefile.exists() || imagefile.length() == 0) {
                // head/step/dream/cover
                String url = String.format("http://img.nian.so/%s/%s", type, image);
                // 检查METHOD
                HttpGet request = new HttpGet(url);
                request.setConfig(requestConfig);
                // 填充HTTP头
                request.setHeader("User-Agent", "NianiOS/5.0.3 (iPhone; iOS 11.2.6; Scale/2.00)");
                CloseableHttpResponse response = httpClient.execute(request);
                if (response != null && response.getEntity().getContent() != null) {
                    FileUtil.save2image(response.getEntity().getContent(), imagefile, type);
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } else {
                    return new HttpResultEntity(true, String.format("图片下载失败[%s/images/%s/%s]：没有获取到数据)", userid, type, image));
                }
            }
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

        } catch (Exception e) {
            logger.error(String.format("图片下载异常[%s/images/%s/%s]：%s", userid, type, image, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            NianHttpUtil.closeQuitely(httpClient);
        }
    }

    public static HttpResultEntity downloadThumbs(String userid, String type, String image, boolean iscover) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {

            if (StringUtil.isNullOrEmpty(image)) {
                return new HttpResultEntity(true, "IMAGE参数为空，跳过下载。");
            }
            String backupbase = AppConfig.getNianViewsBase();
            String imagepath = StringUtil.path(backupbase, userid, "images/thumbs", image);
            File imagefile = new File(imagepath);
            FileUtil.createParentDirs(imagefile);

            // 文件不存在则下载图片
            if (iscover || !imagefile.exists() || imagefile.length() == 0) {
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
                if (response != null && response.getEntity().getContent() != null) {
                    FileUtil.save2image(response.getEntity().getContent(), imagefile, "thumbs");
                    EntityUtils.consume(response.getEntity());
                    response.close();
                } else {
                    return new HttpResultEntity(true, String.format("图片下载失败[%s/thumbs/%s/%s]：没有获取到数据)", userid, type, image));
                }
            }
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

        } catch (Exception e) {
            logger.error(String.format("图片下载异常[%s/thumbs/%s/%s]：[%s]%s",
                    userid, type, image, e.getClass().getCanonicalName(), e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            NianHttpUtil.closeQuitely(httpClient);
        }
    }

}

class NianImageDownloadWorker extends Thread {
    private String userid;
    private String type;
    private String image;
    private boolean iscover;

    private static Logger logger = LoggerFactory.getLogger(NianImageDownloadWorker.class);

    public NianImageDownloadWorker(String userid, String type, String image, boolean iscover) {
        this.userid = userid;
        this.type = type;
        this.image = image;
        this.iscover = iscover;
    }

    @Override
    public void run() {
        boolean succ = false;
        String imginfo = String.format("%s/images/%s/%s", userid, type, image);
        try {
            long start = System.currentTimeMillis();
            HttpResultEntity thumbsEntity = NianImageDownload.downloadThumbs(userid, type, image, iscover);
            if (thumbsEntity.isSuccess()) {
                HttpResultEntity imageEntity = NianImageDownload.downloadImage(userid, type, image, iscover);
                long end = System.currentTimeMillis();
                if (imageEntity.isSuccess()) {
                    logger.info(String.format("SUCC: [%s/images/%s/%s][%dms]", userid, type, image, end - start));
                    NianImageDownload.removeFailedImage(imginfo);
                    NianImageDownload.setImageStatus(userid, type, image, 1);//成功
                    succ = true;
                } else {
                    NianImageDownload.setImageStatus(userid, type, image, -1);//失败
                    logger.info(String.format("FAIL: [%s/images/%s/%s][%s]", userid, type, image, imageEntity.getMessage()));
                }
            } else {
                NianImageDownload.setImageStatus(userid, type, image, -1);//失败
                logger.info(String.format("FAIL: [%s/thumbs/%s/%s][%s]", userid, type, image, thumbsEntity.getMessage()));
            }
        } catch (Exception e) {
            NianImageDownload.setImageStatus(userid, type, image, -1);//失败
            logger.info(String.format("FAIL: image=[%s/%s/%s][%s]", userid, type, image, e.getMessage()));
        }
        // 如果下载失败则重新下载并覆盖
        if (!succ) {
            if (NianImageDownload.recordFailedImage(imginfo)) {
                NianImageDownload.setImageStatus(userid, type, image, -1);//失败
                NianImageDownload.download(userid, type, image, true);
                logger.info(String.format("REDO: image=[%s/%s/%s]", userid, type, image));
            } else {
                logger.info(String.format("SKIP: image=[%s/%s/%s][图片下载尝试次数已超过最大值]", userid, type, image));
            }
        }
    }
}
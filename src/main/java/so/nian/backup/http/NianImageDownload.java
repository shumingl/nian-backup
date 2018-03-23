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
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.concurrent.*;

public class NianImageDownload {

    private static Logger logger = LoggerFactory.getLogger(NianImageDownload.class);

    private static RequestConfig requestConfig = null;
    private static ExecutorService threadPool = null;
    private static PoolingHttpClientConnectionManager imgConnectionManager;

    //自定义重试策略
    private static HttpRequestRetryHandler imgRetryHandler = new HttpRequestRetryHandler() {

        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            //Do not retry if over max retry count
            if (executionCount >= 3) {
                return false;
            }
            //Timeout
            if (exception instanceof InterruptedIOException) {
                return false;
            }
            //Unknown host
            if (exception instanceof UnknownHostException) {
                return false;
            }
            //Connection refused
            if (exception instanceof ConnectTimeoutException) {
                return false;
            }
            //SSL handshake exception
            if (exception instanceof SSLException) {
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            //Retry if the request is considered idempotent
            //如果请求类型不是HttpEntityEnclosingRequest，被认为是幂等的，那么就重试
            //HttpEntityEnclosingRequest指的是有请求体的request，比HttpRequest多一个Entity属性
            //而常用的GET请求是没有请求体的，POST、PUT都是有请求体的
            //Rest一般用GET请求获取数据，故幂等，POST用于新增数据，故不幂等
            if (idempotent) {
                return true;
            }

            return false;
        }
    };

    private static CloseableHttpClient httpClient;
    static {

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000)
                .setConnectionRequestTimeout(300000)
                .setSocketTimeout(1800000)
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();
        threadPool = new ThreadPoolExecutor(32, 64, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        imgConnectionManager = new PoolingHttpClientConnectionManager();
        // Increase max total connection
        imgConnectionManager.setMaxTotal(500);
        // Increase default max connection per route
        imgConnectionManager.setDefaultMaxPerRoute(30);
        // Increase max connections for img.nian.so:80 to 50
        HttpHost imghost = new HttpHost("img.nian.so", 80);
        imgConnectionManager.setMaxPerRoute(new HttpRoute(imghost), 50);
        /*httpClient = HttpClients.custom()
                .setConnectionManager(imgConnectionManager)
                .setRetryHandler(imgRetryHandler)
                .build();*/
        httpClient = HttpClients.createDefault();

    }

    public static void closewait() {
        while (!threadPool.isTerminated()) {
            threadPool.shutdown();
        }
    }

    public static void download(String type, String image) {
        download(type, image, false);
    }

    public static HttpResultEntity downloadThumbs(String type, String image) {
        return downloadThumbs(type, image, false);
    }

    public static HttpResultEntity downloadImage(String type, String image) {
        return downloadImage(type, image, false);
    }

    public static ExecutorService getThreadPool() {
        return threadPool;
    }

    public static void download(String type, String image, boolean iscover) {
        String path = System.getProperty("nian.imagebase");
        String imagepath = StringUtil.generatePath(path, type, image);
        File imagefile = new File(imagepath);
        if (iscover || !imagefile.exists() || imagefile.length() == 0) {
            threadPool.execute(new NianImageDownloadWorker(type, image, iscover));
        }
    }

    public static HttpResultEntity downloadImage(String type, String image, boolean iscover) {
        /*CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(imgConnectionManager)
                .setRetryHandler(imgRetryHandler)
                .build();*/
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            String path = System.getProperty("nian.imagebase");
            String imagepath = StringUtil.generatePath(path, type, image);
            File imagefile = new File(imagepath);

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
                FileUtil.write2file(response.getEntity().getContent(), imagefile);
                response.close();
            }
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

        } catch (Exception e) {
            logger.error(String.format("图片下载异常[images:%s/%s]：%s", type, image, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            //NianHttpUtil.closeQuitely(httpClient);
        }
    }

    public static HttpResultEntity downloadThumbs(String type, String image, boolean iscover) {
        /*CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(imgConnectionManager)
                .setRetryHandler(imgRetryHandler)
                .build();*/
        //CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            String path = System.getProperty("nian.imagebase");
            String imagepath = StringUtil.generatePath(path, "thumbs", image);
            File imagefile = new File(imagepath);

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
                FileUtil.write2file(response.getEntity().getContent(), imagefile);
                response.close();
            }
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

        } catch (Exception e) {
            logger.error(String.format("图片下载异常[thumbs:%s/%s]：[%s]%s", type, image, e.getClass().getCanonicalName(), e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            //NianHttpUtil.closeQuitely(httpClient);
        }
    }

}

class NianImageDownloadWorker extends Thread {
    private String type;
    private String image;
    private boolean iscover;

    private static Logger logger = LoggerFactory.getLogger(NianImageDownloadWorker.class);

    public NianImageDownloadWorker(String type, String image, boolean iscover) {
        this.type = type;
        this.image = image;
        this.iscover = iscover;
    }

    @Override
    public void run() {
        boolean succ = false;
        try {
            HttpResultEntity thumbsEntity = NianImageDownload.downloadThumbs(type, image, iscover);
            if (thumbsEntity.isSuccess()) {
                HttpResultEntity imageEntity = NianImageDownload.downloadImage(type, image, iscover);
                if (imageEntity.isSuccess()) {
                    logger.info(String.format("SUCC: image=[%s/%s]", type, image));
                    succ = true;
                } else {
                    logger.info(String.format("FAIL: image=[images:%s/%s][%s]", type, image, imageEntity.getMessage()));
                }
            } else {
                logger.info(String.format("FAIL: image=[thumbs:%s/%s][%s]", type, image, thumbsEntity.getMessage()));
            }
        } catch (Exception e) {
            logger.info(String.format("FAIL: image=[%s/%s][%s]", type, image, e.getMessage()));
        }
        // 如果下载失败则重新下载并覆盖
        if (!succ) {
            logger.info(String.format("REDO: image=[%s/%s]", type, image));
            NianImageDownload.download(type, image, true);
        }
    }
}
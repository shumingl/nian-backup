package so.nian.backup.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.bizz.service.NamedThreadFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class NianImageDownload {

    private static Logger logger = LoggerFactory.getLogger(NianImageDownload.class);

    private static RequestConfig requestConfig = null;
    private static ThreadPoolExecutor threadPool = null;
    private static List<Map<String, String>> imagesCache = new ArrayList<>();
    private static Map<String, Integer> failedImages;
    private static int MAX_RETRY = 10;
    private static CloseableHttpClient httpClient;

    static {

        requestConfig = RequestConfig.custom()
                .setConnectTimeout(60000)
                .setConnectionRequestTimeout(300000)
                .setSocketTimeout(1800000)
                //.setProxy(new HttpHost("127.0.0.1", 8888))
                .build();
        threadPool = new ThreadPoolExecutor(32, 64, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new NamedThreadFactory("IMGS"));
        httpClient = HttpClients.createDefault();
        failedImages = new ConcurrentHashMap<>();
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

    public static void buildCache() throws IOException {
        String cachefile = "C:\\data\\app\\app\\imagemaps.txt";
        List<String> lines = Files.readAllLines(Paths.get(cachefile));
        imagesCache.add(new HashMap<>());
        imagesCache.add(new HashMap<>());
        imagesCache.add(new HashMap<>());
        imagesCache.add(new HashMap<>());
        imagesCache.add(new HashMap<>());
        for (String line : lines) {
            String vals[] = line.split(";");
            for (Map<String, String> cache : imagesCache)
                if (!cache.containsKey(vals[0])) {
                    cache.put(vals[0], vals[1]);
                    break;
                }
        }
    }

    public static Map<String, String> takeCache(String key) {
        Map<String, String> result = new HashMap<>();
        for (Map<String, String> cache : imagesCache) {
            if (cache.containsKey(key)) {
                String fileinfo = cache.get(key);
                String infos[] = fileinfo.split("#");
                result.put(infos[0], infos[1]);
            }
        }
        return result;
    }

    public static void shutdownPool() {
        long finished = 0;
        boolean shutdown = false;
        while (!threadPool.isTerminated()) {
            if (shutdown && finished != threadPool.getCompletedTaskCount()) {
                logger.info(String.format("IMAGE-ThreadPool: RUNNING(%d), STATUS(%d/%d), QUEUE(%d).",
                        threadPool.getActiveCount(),
                        threadPool.getCompletedTaskCount(),
                        threadPool.getTaskCount(),
                        threadPool.getQueue().size()));
                finished = threadPool.getCompletedTaskCount();
            }
            if (!shutdown && threadPool.getActiveCount() == 0 && threadPool.getQueue().size() == 0) {
                threadPool.shutdown();
                shutdown = true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(String.format("NianImageDownload.shutdownPool->Thread.sleep();[%s]", e.getMessage()));
            }
        }
    }

    public static void downloadFromLocal(String userid, String type, String image, boolean iscover) {

        try {
            String backupbase = AppConfig.getNianViewsBase();
            String imagepath = StringUtil.generatePath(backupbase, userid, "images", type, image);
            File imagefile = new File(imagepath);
            FileUtil.createParentDirs(imagefile);
            String key = String.format("%s/%s", type, image);

            // 读取缓存文件
            Map<String, String> imgs = takeCache(key);
            if (imgs.size() > 0) {
                CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
                for (String uid : imgs.keySet()) {
                    String fullname = imgs.get(uid);
                    File srcfile = new File(fullname);
                    if (!"thumbs".equals(type)) {
                        if (!userid.equals(uid) && !fullname.contains("thumbs")) {
                            if (srcfile.exists()) {
                                Files.move(Paths.get(fullname), Paths.get(imagepath), options); // 移动文件
                                logger.info(String.format("MOVE: [%s/%s/%s]", userid, type, image));
                            } else {
                                //logger.info(String.format("SKIP: [%s/%s/%s]", userid, type, image));
                            }
                        }
                    } else {
                        if (!userid.equals(uid) && fullname.contains("thumbs")) {
                            if (srcfile.exists()) {
                                Files.move(Paths.get(fullname), Paths.get(imagepath), options); // 移动文件
                                logger.info(String.format("MOVE: [%s/%s/%s]", userid, type, image));
                            } else {
                                //logger.info(String.format("SKIP: [%s/%s/%s]", userid, type, image));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error(String.format("获取本地图片异常[%s/%s/%s]：%s", userid, type, image, e.getMessage()));
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

    public static ThreadPoolExecutor getThreadPool() {
        return threadPool;
    }

    public static void download(String userid, String type, String image, boolean iscover) {
        /*downloadFromLocal(userid, type, image, iscover);*/
        String backupbase = AppConfig.getNianViewsBase();
        String imagepath = StringUtil.generatePath(backupbase, userid, "images", type, image);
        File imagefile = new File(imagepath);
        if (iscover || !imagefile.exists() || imagefile.length() == 0) {
            threadPool.execute(new NianImageDownloadWorker(userid, type, image, iscover));
        }
    }

    public static HttpResultEntity downloadImage(String userid, String type, String image, boolean iscover) {
        try {
            String backupbase = AppConfig.getNianViewsBase();
            String imagepath = StringUtil.generatePath(backupbase, userid, "images", type, image);
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
                    FileUtil.write2file(response.getEntity().getContent(), imagefile);
                    response.close();
                } else {
                    return new HttpResultEntity(true, String.format("图片下载失败[%s/images/%s/%s]：没有获取到数据)", userid, type, image));
                }
            }
            return new HttpResultEntity(true, "图片下载成功：" + imagepath);

        } catch (Exception e) {
            logger.error(String.format("图片下载异常[%s/%s/%s]：%s", userid, type, image, e.getMessage()));
            return new HttpResultEntity(false, e.getMessage());
        } finally {
            //NianHttpUtil.closeQuitely(httpClient);
        }
    }

    public static HttpResultEntity downloadThumbs(String userid, String type, String image, boolean iscover) {
        try {
            String backupbase = AppConfig.getNianViewsBase();
            String imagepath = StringUtil.generatePath(backupbase, userid, "images/thumbs", image);
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
                    FileUtil.write2file(response.getEntity().getContent(), imagefile);
                    response.close();
                } else {
                    return new HttpResultEntity(true, String.format("图片下载失败[%s/thumbs/%s/%s]：没有获取到数据)", userid, type, image));
                }
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
            HttpResultEntity thumbsEntity = NianImageDownload.downloadThumbs(userid, type, image, iscover);
            if (thumbsEntity.isSuccess()) {
                HttpResultEntity imageEntity = NianImageDownload.downloadImage(userid, type, image, iscover);
                if (imageEntity.isSuccess()) {
                    logger.info(String.format("SUCC: [%s/images/%s/%s]", userid, type, image));
                    NianImageDownload.removeFailedImage(imginfo);
                    succ = true;
                } else {
                    logger.info(String.format("FAIL: [%s/images/%s/%s][%s]", userid, type, image, imageEntity.getMessage()));
                }
            } else {
                logger.info(String.format("FAIL: [%s/thumbs/%s/%s][%s]", userid, type, image, thumbsEntity.getMessage()));
            }
        } catch (Exception e) {
            logger.info(String.format("FAIL: image=[%s/%s/%s][%s]", userid, type, image, e.getMessage()));
        }
        // 如果下载失败则重新下载并覆盖
        if (!succ) {
            if (NianImageDownload.recordFailedImage(imginfo)) {
                logger.info(String.format("REDO: image=[%s/%s/%s]", userid, type, image));
                NianImageDownload.download(userid, type, image, true);
            } else {
                logger.info(String.format("SKIP: image=[%s/%s/%s][图片尝试下载次数已超过最大值]", userid, type, image));
            }
        }
    }
}
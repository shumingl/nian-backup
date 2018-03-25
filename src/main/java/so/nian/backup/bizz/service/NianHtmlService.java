package so.nian.backup.bizz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.ExpressionParser;
import so.nian.backup.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NianHtmlService {

    private static final Logger logger = LoggerFactory.getLogger(NianHtmlService.class);
    private static ThreadPoolExecutor htmlThreadPool;
    private static ThreadPoolExecutor httpThreadPool;

    static {
        htmlThreadPool = new ThreadPoolExecutor(32, 64, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("HTML"));
        httpThreadPool = new ThreadPoolExecutor(16, 64, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("HTTP"));
    }

    public static void shutdownPool() {
        long finished = 0;
        boolean shutdown = false;
        while (!httpThreadPool.isTerminated()) {
            if (shutdown && finished != httpThreadPool.getCompletedTaskCount()) {
                logger.info(String.format("HTTP-ThreadPool: RUNNING(%d), STATUS(%d/%d), QUEUE(%d).",
                        httpThreadPool.getActiveCount(),
                        httpThreadPool.getCompletedTaskCount(),
                        httpThreadPool.getTaskCount(),
                        httpThreadPool.getQueue().size()));
                finished = httpThreadPool.getCompletedTaskCount();
            }
            if (!shutdown && httpThreadPool.getActiveCount() == 0 && httpThreadPool.getQueue().size() == 0) {
                httpThreadPool.shutdown();
                shutdown = true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(String.format("NianHtmlService.shutdownPool(httpThreadPool)->Thread.sleep();[%s]", e.getMessage()));
            }
        }
        finished = 0;
        shutdown = false;
        while (!htmlThreadPool.isTerminated()) {
            if (shutdown && finished != htmlThreadPool.getCompletedTaskCount()) {
                finished = htmlThreadPool.getCompletedTaskCount();
                logger.info(String.format("HTML-ThreadPool: RUNNING(%d), STATUS(%d/%d), QUEUE(%d).",
                        htmlThreadPool.getActiveCount(),
                        htmlThreadPool.getCompletedTaskCount(),
                        htmlThreadPool.getTaskCount(),
                        htmlThreadPool.getQueue().size()));
            }
            if (!shutdown && htmlThreadPool.getActiveCount() == 0 && htmlThreadPool.getQueue().size() == 0) {
                htmlThreadPool.shutdown();
                shutdown = true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(String.format("NianHtmlService.shutdownPool(htmlThreadPool)->Thread.sleep();[%s]", e.getMessage()));
            }
        }
    }

    public static void downloadForUsers(String... users) throws IOException {
        if (users != null && users.length > 0) {
            for (String userid : users)
                downloadForUser(userid);
        }
        NianJsonService.shutdownPool();
        NianHtmlService.shutdownPool();
        NianImageDownload.shutdownPool();
    }

    public static void createUserDirs(String userid) throws IOException {

        // 创建用户目录
        String userbase = AppConfig.getNianViewsBase();
        File userbasedir = new File(userbase, userid);
        if (!userbasedir.exists())
            userbasedir.mkdirs();
        String userdir = userbasedir.getCanonicalPath();
        // 创建图片目录
        File imagebasedir = new File(userdir, "images");
        if (!imagebasedir.exists()) {
            imagebasedir.mkdirs();
        }

    }

    private static void downloadForUser(String userid) throws IOException {

        ExpressionParser parser = ExpressionParser.getDefault();
        // 创建用户目录
        createUserDirs(userid);
        String basepath = AppConfig.getNianViewsBase();
        // 下载用户信息
        boolean isloadinfo = false;
        Map<String, Object> userinfo;
        String username = "";
        HttpResultEntity info = NianHttpUtil.info(userid);
        if (info != null && info.isSuccess()) {
            Map<String, Object> data = (Map<String, Object>) info.getResponseMap().get("data");
            if (data != null) {
                userinfo = (Map<String, Object>) data.get("user");
                if (userinfo != null) {
                    Object uid = userinfo.get("uid");
                    if (uid != null) {
                        String template = "名称 : ${name}; \n" +
                                "UID  : ${uid}; \n" +
                                "邮箱 : ${email}; \n" +
                                "硬币 : ${coin}; \n" +
                                "关注 : ${follows}; \n" +
                                "粉丝 : ${followed}; \n" +
                                "获赞 : ${likes}; \n" +
                                "梦想 : ${dream}; \n" +
                                "进展 : ${step}; \n";
                        String userInfoString = parser.parse(template, userinfo);
                        Files.write(Paths.get(basepath, userid, "userinfo.txt"), userInfoString.getBytes());
                        username = String.valueOf(userinfo.get("name"));
                        isloadinfo = true;
                    }
                }
            } else {
                logger.error(String.format("获取用户信息为空[%s]", userid));
            }
        } else {
            logger.error(String.format("获取用户信息失败[%s]", userid));
        }
        if (!isloadinfo) return;

        // 下载用户的记本
        downloadDreams(username, userid);
    }

    private static void downloadDreams(String username, String userid) {

        logger.info(String.format("用户[%s(%s)]开始下载", username, userid));
        HttpResultEntity entity = NianHttpUtil.dreams(userid);
        if (entity.isSuccess()) {
            Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
            if (data != null) {
                List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
                logger.info(String.format("用户[%s(%s)]记本数量：[%d]", username, userid, dreams.size()));
                for (Map<String, Object> dream : dreams) {
                    downloadDream(userid, String.valueOf(dream.get("id")));
                }
            } else {
                logger.info(String.format("用户[%s(%s)]记本列表为空：%s", username, userid, entity.getResponseBody()));
            }
        } else {
            logger.error(String.format("用户[%s(%s)]记本列表下载失败：%s", username, userid, entity.getMessage()));
        }
        logger.info(String.format("用户[%s(%s)]记本下载完成", username, userid));
    }

    public static void downloadDream(String userid, String dreamid) {
        httpThreadPool.execute(new NianDreamHttpWorker("html", userid, dreamid));
    }

    public static void generateDreamHtml(String userid, String dreamid) {
        logger.info(String.format("记本[%s]开始下载", dreamid));
        try {
            Map<String, Object> dataModel = NianJsonService.downloadAllSteps(userid, dreamid);

            if (userid == null) {
                if (dataModel != null) {
                    userid = String.valueOf(StringUtil.mget(dataModel, "dream/uid"));
                    if (userid == null)
                        throw new RuntimeException("generateDreamHtml(): 用户USERID为空");
                } else {
                    logger.warn(String.format("记本[%s]下载数据为空", dreamid));
                    return;
                }
            } else {
                createUserDirs(userid);
            }
            String dreamtitle = String.valueOf(StringUtil.mget(dataModel, "dream/title"));
            // 记录到缓存文件中
            htmlThreadPool.execute(new NianDreamJsonWorker(userid, dreamtitle, dreamid, dataModel));
            // 开始生成记本内容
            htmlThreadPool.execute(new NianDreamHtmlWorker(userid, dreamtitle, dreamid, dataModel));
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
    }
}

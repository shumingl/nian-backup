package so.nian.backup.bizz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.config.AppConstants;
import so.nian.backup.utils.ExpressionParser;
import so.nian.backup.utils.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"unchecked"})
public class NianHtmlService {

    private static final Logger logger = LoggerFactory.getLogger(NianHtmlService.class);
    private static ThreadPoolExecutor htmlThreadPool;
    private static ThreadPoolExecutor httpThreadPool;

    public static void startup(int httpSize, int htmlSize) {

        htmlThreadPool = new ThreadPoolExecutor(htmlSize, htmlSize * 2, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("HTML"));
        httpThreadPool = new ThreadPoolExecutor(httpSize, httpSize * 2, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new NamedThreadFactory("HTTP"));
    }

    public static ThreadPoolExecutor getHttpThreadPool() {
        return httpThreadPool;
    }

    public static ThreadPoolExecutor getHtmlThreadPool() {
        return htmlThreadPool;
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

    public static void downloadForUser(String userid) throws IOException {

        // 创建用户目录
        createUserDirs(userid);
        // 下载用户信息
        Map<String, Object> userinfo = NianJsonService.downloadUserInfo(userid);
        String username = "";
        if (userinfo == null) {
            // throw new RuntimeException(String.format("用户[%s]信息获取失败", userid));
            username = userid;
        } else {
            username = String.valueOf(userinfo.get("name"));
        }
        downloadDreams(username, userid);// 下载用户的记本
    }

    private static void downloadDreams(String username, String userid) {
        logger.info(String.format("用户[%s(%s)]开始下载", username, userid));
        Map<String, Object> data = NianJsonService.downloadUserDreams(userid);
        if (data != null) {
            List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
            logger.info(String.format("用户[%s(%s)]记本数量：[%d][%s]", username, userid, dreams.size(), dreams));
            for (Map<String, Object> dream : dreams) {
                logger.info(String.format("用户[%s(%s)]开始下载记本[%s(%s)]",
                        username, userid, dream.get("title"), dream.get("id")));
                downloadDream(userid, String.valueOf(dream.get("id")));
            }
            logger.info(String.format("用户[%s(%s)]记本下载完成", username, userid));
        } else {
            logger.info(String.format("用户[%s(%s)]记本列表为空", username, userid));
        }
    }

    public static void downloadDream(String userid, String dreamid) {
        httpThreadPool.execute(new NianDreamHttpWorker("html", userid, dreamid));
    }

    public static void generateDreamHtml(String userid, String dreamid) {
        logger.info(String.format("记本[%s]开始下载", dreamid));
        String model = AppConfig.getNianRenderModel();
        try {
            Map<String, Object> dataModel = NianJsonService.downloadAllSteps(userid, dreamid);

            if (userid == null) {
                if (dataModel != null) {
                    userid = StringUtil.MAPGET(dataModel, "dream/uid");
                    if (userid == null)
                        throw new RuntimeException("generateDreamHtml(): 用户USERID为空");
                } else {
                    logger.warn(String.format("记本[%s]下载数据为空", dreamid));
                    return;
                }
            } else {
                createUserDirs(userid);
            }
            String dreamtitle = StringUtil.MAPGET(dataModel, "dream/title");
            if (AppConstants.RENDER_MODEL_ONLINE.equals(model))//在线的情况下才去保存JSON数据
                NianJsonService.getJsonThreadPool().execute(
                        new NianDreamJsonWorker(userid, dreamtitle, dreamid, dataModel));
            // 开始生成记本内容
            htmlThreadPool.execute(new NianDreamHtmlWorker(userid, dreamtitle, dreamid, dataModel));
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
    }
}

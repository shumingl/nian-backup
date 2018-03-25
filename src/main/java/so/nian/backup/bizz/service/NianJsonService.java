package so.nian.backup.bizz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NianJsonService {
    private static final Logger logger = LoggerFactory.getLogger(NianJsonService.class);
    private static ThreadPoolExecutor httpThreadPool;
    private static ThreadPoolExecutor jsonThreadPool;

    static {
        httpThreadPool = new ThreadPoolExecutor(20, 64, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("HTTP"));
        jsonThreadPool = new ThreadPoolExecutor(12, 64, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("JSON"));
    }

    public static void createUserDirs(String userid) throws IOException {
        // 创建用户目录
        String userbase = AppConfig.getNianCacheBase();
        File userbasedir = new File(userbase, userid);
        if (!userbasedir.exists())
            userbasedir.mkdirs();
        NianHtmlService.createUserDirs(userid);
    }

    public static void downloadByLogin(String email, String password) throws IOException {
        HttpResultEntity result = NianHttpUtil.login(email, password);
        Map<String, Object> login;
        if (result.isSuccess()) {
            login = (Map<String, Object>) result.getResponseMap().get("data");
            logger.info(String.format("登录成功：%s", login));
        } else {
            logger.info("登录失败：" + result.getMessage());
            return;
        }
        String userid = String.valueOf(login.get("uid"));
        downloadForUser(userid);

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

    public static void downloadForUser(String userid) throws IOException {
        if (userid == null) {
            throw new RuntimeException("[downloadForUser]登录信息为空");
        }
        // 创建用户目录
        NianJsonService.createUserDirs(userid);
        String basepath = AppConfig.getNianCacheBase();
        String fullname = StringUtil.generatePath(basepath, userid, "user.json");
        FileUtil.createParentDirs(new File(fullname));

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
                        username = String.valueOf(userinfo.get("name"));
                        String userjson = JsonUtil.object2Json(data);
                        Files.write(Paths.get(fullname), userjson.getBytes("UTF-8"));
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
        // 下载头像
        NianImageDownload.download(userid, "head", userid + ".jpg");
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
                    // 下载图片
                    String image = String.valueOf(dream.get("image"));
                    if (!StringUtil.isNullOrEmpty(image))
                        NianImageDownload.download(userid, "dream", image);
                    // 下载记本
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
        httpThreadPool.execute(new NianDreamHttpWorker("json", userid, dreamid));
    }

    public static void generateDreamJson(String userid, String dreamid) {
        String dreamtitle = "";
        logger.info(String.format("记本[%s]开始下载", dreamid));
        try {
            Map<String, Object> dataModel = downloadAllSteps(userid, dreamid);
            if (userid == null) {
                if (dataModel != null) {
                    userid = String.valueOf(StringUtil.mget(dataModel, "dream/uid"));
                    if (userid == null)
                        throw new RuntimeException("generateDreamJson(): 用户USERID为空");
                } else {
                    logger.warn(String.format("记本[%s]下载数据为空", dreamid));
                    return;
                }
            } else {
                createUserDirs(userid);
            }
            // 开始生成记本内容
            jsonThreadPool.execute(new NianDreamJsonWorker(userid, dreamtitle, dreamid, dataModel));
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
    }

    private static Map<String, Object> checkupdate(String userid, String dreamid) throws IOException {

        String basepath = AppConfig.getNianCacheBase();
        String summaryfile = StringUtil.generatePath(basepath, userid, dreamid + "-info.json");
        File summary = new File(summaryfile);
        String cachefile = StringUtil.generatePath(basepath, userid, dreamid + "-info.json");
        File cache = new File(cachefile);
        Map<String, Object> httpjson = null;
        // 检查是否需要重新下载
        boolean isreload = false;
        Map<String, Object> result = new HashMap<>();
        if (!summary.exists() || !cache.exists()) {
            result.put("reload", true);
            result.put("first", null);
            return result;
        } else {
            byte[] sbytes = Files.readAllBytes(Paths.get(summaryfile));
            Map<String, Object> localdream = JsonUtil.json2Map(new String(sbytes, "UTF-8"));
            httpjson = NianJsonService.downloadFirstPageSteps(dreamid);
            //比较数据是否有更新
            result.put("first", httpjson);
            if (httpjson != null && httpjson.size() > 0) {
                Map<String, Object> httpdream = (Map<String, Object>) httpjson.get("dream");
                if (localdream != null && localdream.size() > 0 && httpdream != null && httpdream.size() > 0) {
                    if (localdream.get("lastdate").equals(httpdream.get("lastdate")) &&
                            localdream.get("step").equals(httpdream.get("step")) &&
                            localdream.get("like_step").equals(httpdream.get("like_step")) &&
                            localdream.get("followers").equals(httpdream.get("followers")) &&
                            localdream.get("title").equals(httpdream.get("title")) &&
                            localdream.get("content").equals(httpdream.get("content"))) {
                        result.put("reload", false);
                    } else {
                        result.put("reload", true);
                    }
                } else {// 本地cache或http获取数据中，dream为空
                    result.put("reload", true);
                }
            } else {// http获取数据为空
                result.put("reload", true);
            }
        }
        return result;
    }


    private static Map<String, Object> downloadFirstPageSteps(String dreamid) {
        Map<String, Object> data = null;
        HttpResultEntity entity = NianHttpUtil.steps(dreamid, 1);
        if (entity.isSuccess()) {
            data = (Map<String, Object>) entity.getResponseMap().get("data");
            if (data != null) {
                String dreamtitle = String.valueOf(StringUtil.mget(data, "dream/title"));
                logger.info(String.format("记本[%s(%s)]首页下载成功", dreamtitle, dreamid));
            }
        } else {
            logger.info(String.format("记本[%s]首页下载失败[HTTP/%s]：%s", dreamid,
                    entity.getStatusCode(), entity.getMessage()));
        }
        return data;
    }

    public static Map<String, Object> downloadAllSteps(String userid, String dreamid) {
        String dreamtitle = "";
        int steptotal = 0;
        logger.info(String.format("记本[%s]开始下载", dreamid));
        try {
            Map<String, Object> dataModel = new HashMap<>();

            // 检查是否有更新并返回第一页数据内容
            Map<String, Object> update = checkupdate(userid, dreamid);
            int page = 2;
            boolean reload = (Boolean) update.get("reload");
            Map<String, Object> first = (Map<String, Object>) update.get("first");
            if (!reload) {//不需要更新
                dreamtitle = String.valueOf(StringUtil.mget(first, "dream/title"));
                logger.info(String.format("记本[%s(%s)]没有数据更新", dreamtitle, dreamid));
                String basepath = AppConfig.getNianCacheBase();
                String cachepath = StringUtil.generatePath(basepath, userid, dreamid + ".json");
                byte[] cachebytes = Files.readAllBytes(Paths.get(cachepath));
                String cachejson = new String(cachebytes, "UTF-8");
                Map<String, Object> cache = JsonUtil.json2Map(cachejson);
                return cache;

            } else {
                if (first != null) {
                    dataModel.putAll(first);
                    Map<String, Object> dream = (Map<String, Object>) first.get("dream");
                    if (dream != null) {
                        dreamtitle = String.valueOf(dream.get("title"));
                        steptotal = Integer.valueOf(String.valueOf(dream.get("step")));
                        logger.info(String.format("记本[%s(%s)]共有[%d]条进展", dreamtitle, dreamid, steptotal));
                    }
                    page = 2;
                } else {//有更新，但是没有第一页数据（摘要文件不存在的情况）
                    page = 1;
                }
            }
            List<Map<String, Object>> steps = new ArrayList<>();

            // 读取剩余的全部进展内容
            int finished = 0;
            while (true) {
                HttpResultEntity entity = NianHttpUtil.steps(dreamid, page);
                if (entity.isSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                    if (data != null) {
                        if (page == 1) {
                            dataModel.putAll(data);
                            Map<String, Object> dream = (Map<String, Object>) data.get("dream");
                            if (dream != null) {
                                dreamtitle = String.valueOf(dream.get("title"));
                                steptotal = Integer.valueOf(String.valueOf(dream.get("step")));
                                logger.info(String.format("记本[%s(%s)]共有[%d]条进展", dreamtitle, dreamid, steptotal));
                            }
                        }
                        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("steps");
                        if (list == null || list.size() == 0) {
                            logger.info(String.format("记本[%s(%s)]下载完成", dreamtitle, dreamid));
                            break;
                        } else {
                            finished += list.size();
                            steps.addAll(list);
                            logger.info(String.format("记本[%s(%s)]第%04d页进展下载成功(%d/%d)",
                                    dreamtitle, dreamid, page, finished, steptotal));
                        }
                        page++;
                    } else {
                        break;
                    }
                } else {
                    logger.error(String.format("记本[%s(%s)]第%04d页进展下载失败", dreamtitle, dreamid, page));
                }
            }

            dataModel.put("steps", steps);
            return dataModel;
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
        return null;
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
                logger.error(String.format("NianJsonService.shutdownPool(httpThreadPool)->Thread.sleep();[%s]", e.getMessage()));
            }
        }
        finished = 0;
        shutdown = false;
        while (!jsonThreadPool.isTerminated()) {
            if (shutdown && finished != jsonThreadPool.getCompletedTaskCount()) {
                logger.info(String.format("JSON-ThreadPool: RUNNING(%d), STATUS(%d/%d), QUEUE(%d).",
                        jsonThreadPool.getActiveCount(),
                        jsonThreadPool.getCompletedTaskCount(),
                        jsonThreadPool.getTaskCount(),
                        jsonThreadPool.getQueue().size()));
                finished = jsonThreadPool.getCompletedTaskCount();
            }
            if (!shutdown && jsonThreadPool.getActiveCount() == 0 && jsonThreadPool.getQueue().size() == 0) {
                jsonThreadPool.shutdown();
                shutdown = true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(String.format("NianJsonService.shutdownPool(jsonThreadPool)->Thread.sleep();[%s]", e.getMessage()));
            }
        }
    }

}

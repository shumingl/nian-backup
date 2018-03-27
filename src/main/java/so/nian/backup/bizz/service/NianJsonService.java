package so.nian.backup.bizz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.ExpressionParser;
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

@SuppressWarnings("unchecked")
public class NianJsonService {
    private static final Logger logger = LoggerFactory.getLogger(NianJsonService.class);
    private static ThreadPoolExecutor httpThreadPool;
    private static ThreadPoolExecutor jsonThreadPool;
    private static final ExpressionParser parser = ExpressionParser.getDefault();

    public static void startup() {
        httpThreadPool = new ThreadPoolExecutor(80, 100, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("HTTP"));
        jsonThreadPool = new ThreadPoolExecutor(8, 32, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("JSON"));
    }

    public static ThreadPoolExecutor getHttpThreadPool() {
        return httpThreadPool;
    }

    public static ThreadPoolExecutor getJsonThreadPool() {
        return jsonThreadPool;
    }

    public static String getCachePath(String userid, String type) {
        String basepath = AppConfig.getNianCacheBase();
        if ("cache".equals(type)) {
            return StringUtil.path(basepath, userid);
        } else if ("dream".equals(type)) {
            return StringUtil.path(basepath, userid, "dreams");
        } else {
            throw new RuntimeException("错误的参数TYPE[cache/dream]");
        }
    }

    public static void createUserDirs(String userid) throws IOException {
        // 创建用户目录
        String cachebase = AppConfig.getNianCacheBase();
        File cachebasedir = new File(cachebase, userid);
        if (!cachebasedir.exists())
            cachebasedir.mkdirs();
        NianHtmlService.createUserDirs(userid);
    }

    public static boolean loginByAuth(String email, String password) {
        HttpResultEntity result = NianHttpUtil.login(email, password);
        Map<String, Object> login;
        if (result.isSuccess()) {
            login = (Map<String, Object>) result.getResponseMap().get("data");
            logger.info(String.format("用户登录成功：%s", login));
            return true;
        } else {
            logger.warn("用户登录失败：" + result.getMessage());
            return false;
        }
    }

    public static void loginByShell(String uid, String shell) {
        NianHttpUtil.LOGINFO.put("uid", uid);
        NianHttpUtil.LOGINFO.put("shell", shell);
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
    }

    public static void downloadForUser(String userid) throws IOException {
        if (userid == null) {
            throw new RuntimeException("[downloadForUser]登录信息为空");
        }
        String model = AppConfig.getNianRenderModel();
        // 创建用户目录
        NianJsonService.createUserDirs(userid);
        String basepath = NianJsonService.getCachePath(userid, "cache");
        String fullname = StringUtil.path(basepath, "user.json");
        FileUtil.createParentDirs(new File(fullname));

        // 下载用户信息
        Map<String, Object> userdata = NianJsonService.downloadUserInfo(userid);
        if (userdata != null) {
            String username = String.valueOf(StringUtil.mget(userdata, "user/name"));
            // 下载用户的记本
            downloadDreams(username, userid);
        }
    }

    private static void downloadDreams(String username, String userid) throws IOException {

        logger.info(String.format("用户[%s(%s)]开始下载", username, userid));

        Map<String, Object> data = NianJsonService.downloadUserDreams(userid);
        if (data != null) {
            List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
            logger.info(String.format("用户[%s(%s)]记本数量[%d]", username, userid, dreams.size()));
            for (Map<String, Object> dream : dreams) {
                // 下载图片
                String image = String.valueOf(dream.get("image"));
                if (!StringUtil.isNullOrEmpty(image))
                    NianImageDownload.download(userid, "dream", image);
                // 下载记本
                downloadDream(userid, String.valueOf(dream.get("id")));
            }
        } else {
            logger.info(String.format("用户[%s(%s)]记本列表为空", username, userid));
        }
        logger.info(String.format("用户[%s(%s)]记本下载完成", username, userid));
    }

    public static void downloadDream(String userid, String dreamid) {
        httpThreadPool.execute(new NianDreamHttpWorker("json", userid, dreamid));
    }

    public static void generateDreamJson(String userid, String dreamid) {
        try {
            Map<String, Object> dataModel = NianJsonService.downloadAllSteps(userid, dreamid);
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

            String model = AppConfig.getNianRenderModel();
            if ("online".equals(model)) { // ONLINE模式生成记本内容
                String dreamtitle = String.valueOf(StringUtil.mget(dataModel, "dream/title"));
                jsonThreadPool.execute(new NianDreamJsonWorker(userid, dreamtitle, dreamid, dataModel));
            }
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
    }

    private static Map<String, Object> checkupdate(String userid, String dreamid) throws IOException {

        String basepath = NianJsonService.getCachePath(userid, "dream");
        String summaryfile = StringUtil.path(basepath, dreamid + "-info.json");
        File summary = new File(summaryfile);
        String cachefile = StringUtil.path(basepath, dreamid + ".json");
        File cache = new File(cachefile);
        Map<String, Object> httpjson = null;
        // 检查是否需要重新下载
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

    public static Map<String, Object> downloadFromLocal(String userid, String dreamid) {
        String dreambase = NianJsonService.getCachePath(userid, "dream");
        String dreampath = StringUtil.path(dreambase, dreamid + ".json");
        File dreamfile = new File(dreampath);
        Map<String, Object> data = null;
        if (dreamfile.exists()) {
            try {
                byte[] bytes = Files.readAllBytes(Paths.get(dreampath));
                String dreamjson = new String(bytes, "UTF-8");
                data = JsonUtil.json2Map(dreamjson);
            } catch (Exception e) {
                logger.error(String.format("[%s/%s]读解析记本数据[%s]错误：%s", userid, dreamid, dreampath, e.getMessage()));
            }
        }
        return data;
    }

    public static Map<String, Object> downloadUserInfo(String userid) {

        // 头像
        NianImageDownload.download(userid, "head", userid + ".jpg");
        // 获取数据
        String model = AppConfig.getNianRenderModel();
        Map<String, Object> data = null;
        if ("online".equals(model)) {
            HttpResultEntity info = NianHttpUtil.info(userid);
            if (info != null && info.isSuccess()) {
                data = (Map<String, Object>) info.getResponseMap().get("data");
                Map<String, Object> userinfo = (Map<String, Object>) data.get("user");
                if (userinfo != null && userinfo.get("uid") != null) { //有些不存在的UserID下载的数据uid是null
                    // 下载用户关注和粉丝
                    List<Map<String, Object>> care = downloadUserCareOrFans(userid, "care");
                    List<Map<String, Object>> fans = downloadUserCareOrFans(userid, "fans");
                    userinfo.put("care", care);
                    userinfo.put("fans", fans);
                }
            } else {
                logger.error(String.format("获取用户信息失败[%s]", userid));
            }
        } else if ("offline".equals(model)) {
            String cachebase = NianJsonService.getCachePath(userid, "cache");
            String userpath = StringUtil.path(cachebase, "user.json");
            File userfile = new File(userpath);
            if (userfile.exists()) {
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(userpath));
                    String userjson = new String(bytes, "UTF-8");
                    data = JsonUtil.json2Map(userjson);
                } catch (IOException e) {
                    logger.error("获取本地用户文件[{}/user.json]错误：{}", userid, e.getMessage());
                }
            }
        } else
            throw new RuntimeException("[downloadUserInfo]参数错误：nian.render.model[online/offline]");
        return data;
    }

    public static Map<String, Object> downloadUserDreams(String userid) {

        String model = AppConfig.getNianRenderModel();
        Map<String, Object> data = null;
        if ("online".equals(model)) {
            HttpResultEntity entity = NianHttpUtil.dreams(userid);
            if (entity != null && entity.isSuccess()) {
                data = (Map<String, Object>) entity.getResponseMap().get("data");

                String basepath = NianJsonService.getCachePath(userid, "cache");
                String fullname = StringUtil.path(basepath, "dreams.json");
                FileUtil.createParentDirs(new File(fullname));

                // 保存记本列表信息
                String datajson = JsonUtil.object2Json(data);
                if (datajson != null)
                    try {
                        Files.write(Paths.get(fullname), datajson.getBytes("UTF-8"));
                    } catch (IOException e) {
                        logger.error("写入本地记本列表[{}/dreams.json]错误：{}", userid, e.getMessage());
                    }
            }
        } else if ("offline".equals(model)) {
            String cachebase = NianJsonService.getCachePath(userid, "cache");
            String dreamspath = StringUtil.path(cachebase, "dreams.json");
            File dreamsfile = new File(dreamspath);
            if (dreamsfile.exists()) {
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(dreamspath));
                    String dreamsjson = new String(bytes, "UTF-8");
                    data = JsonUtil.json2Map(dreamsjson);
                } catch (IOException e) {
                    logger.error("获取本地记本列表[{}/dreams.json]错误：{}", userid, e.getMessage());
                }
            }
        } else
            throw new RuntimeException("[downloadUserDreams]参数错误：nian.render.model[online/offline]");
        if (data != null) {
            List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
            if (dreams != null) {
                for (Map<String, Object> dream : dreams) {
                    NianImageDownload.download(userid, "dream", String.valueOf(dream.get("image")));
                }
            }
        }
        return data;
    }

    public static Map<String, Object> downloadAllSteps(String userid, String dreamid) {
        // 获取数据
        String model = AppConfig.getNianRenderModel();
        Map<String, Object> data;
        if ("online".equals(model))
            data = downloadFromApi(userid, dreamid);
        else if ("offline".equals(model))
            data = downloadFromLocal(userid, dreamid);
        else
            throw new RuntimeException("参数错误：nian.render.model[online/offline]");

        // 下载图片
        NianImageDownload.download(userid, "head", userid + ".jpg");//头像
        if (data != null) {
            Map<String, Object> dream = (Map<String, Object>) data.get("dream");
            if (dream != null) {
                String image = String.valueOf(dream.get("image"));
                NianImageDownload.download(userid, "dream", image);//dream封面
            }
            // 进展图片
            List<Map<String, Object>> steps = (List<Map<String, Object>>) data.get("steps");
            for (Map<String, Object> step : steps) {
                List<Map<String, Object>> images = (List<Map<String, Object>>) step.get("images");
                String firstimage = String.valueOf(step.get("image"));
                if (images != null && images.size() > 0) {
                    for (Map<String, Object> image : images) {
                        String imgpath = String.valueOf(image.get("path"));
                        if (!StringUtil.isNullOrEmpty(imgpath))
                            NianImageDownload.download(userid, "step", imgpath);
                    }
                } else {
                    if (!StringUtil.isNullOrEmpty(firstimage))
                        NianImageDownload.download(userid, "step", firstimage);
                }
            }
        }

        return data;
    }

    private static void buildDreamStepIndex(String userid, String dreamid, Map<String, Object> dreamSteps) {
        if (dreamSteps == null || dreamSteps.size() == 0)
            return;
        List<Map<String, Object>> steps = (List<Map<String, Object>>) dreamSteps.get("steps");
        if (steps == null)
            return;
        Map<String, Object> index = new HashMap<>();
        for (Map<String, Object> step : steps) {
            String stepid = (String) step.get("sid");
            index.put(stepid, step);
        }
        String cachepath = NianJsonService.getCachePath(userid, "dream");
        String indexpath = StringUtil.path(cachepath, dreamid + "-index.json");
        String indexjson = JsonUtil.object2Json(index);
        try {
            Files.write(Paths.get(indexpath), indexjson.getBytes("UTF-8"));
        } catch (IOException e) {
            logger.error(String.format("[%s/%s]创建索引失败：%s", userid, dreamid, e.getMessage()));
        }
    }

    public static Map<String, Object> downloadFromApi(String userid, String dreamid) {
        String dreamtitle = "";
        int steptotal = 0;
        String cachepath = NianJsonService.getCachePath(userid, "dream");
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
                String dreamcachepath = StringUtil.path(cachepath, dreamid + ".json");
                byte[] dreamcachebytes = Files.readAllBytes(Paths.get(dreamcachepath));
                String dreamcachejson = new String(dreamcachebytes, "UTF-8");
                Map<String, Object> cache = JsonUtil.json2Map(dreamcachejson);
                return cache;

            } else {
                logger.info(String.format("记本[%s]开始下载", dreamid));
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
                            logger.info(String.format("记本[%s(%s)]第%04d页进展下载成功(%d/%d)", dreamtitle, dreamid, page, finished, steptotal));
                        }
                        page++;
                    } else {
                        break;
                    }
                } else {
                    logger.error(String.format("记本[%s(%s)]第%04d页进展下载失败", dreamtitle, dreamid, page));
                }
            }

            /*
            Map<String, Object> dreaminfo = (Map<String, Object>) dataModel.get("dream");
            if (dreaminfo != null) {
                dreaminfo.put("dlike", downloadDreamLikeOrFans(dreamid, "like"));// 获取记本点赞用户
                dreaminfo.put("dfans", downloadDreamLikeOrFans(dreamid, "fans"));// 获取记本关注用户
            }
            */

            // 获取进展评论和点赞用户
            int fin = 0;
            int total = steps.size();
            for (Map<String, Object> step : steps) {
                step.put("stepcomments", new ArrayList<>());
                step.put("steplikes", new ArrayList<>());
                String stepid = String.valueOf(step.get("sid"));
                Integer datacmts = Integer.valueOf(String.valueOf(step.get("comments")));
                if (datacmts > 0) {
                    List<Map<String, Object>> comments = downloadStepALlComments(stepid, datacmts);
                    step.put("stepcomments", comments);
                    logger.info(String.format("记本[%s(%s)]加载进展评论(%d/%d)", dreamtitle, dreamid, fin, total));
                }
                //Integer datalike = Integer.valueOf(String.valueOf(step.get("likes")));
                //if (datalike > 0) step.put("steplikes", downloadStepAllLike(stepid));
                fin++;
            }
            dataModel.put("steps", steps);
            return dataModel;
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
        return null;
    }

    public static List<Map<String, Object>> downloadStepAllLike(String stepid) {
        List<Map<String, Object>> result = new ArrayList<>();
        int page = 1;
        while (true) {
            HttpResultEntity entity = NianHttpUtil.like(stepid, page);
            if (entity.isSuccess()) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) entity.getResponseMap().get("data");
                if (data != null) {
                    if (data.size() == 0)
                        break;
                    result.addAll(data);
                    page++;
                } else { // 为空退出
                    break;
                }
            }
        }
        return result;
    }

    public static List<Map<String, Object>> downloadStepALlComments(String stepid, int total) {
        List<Map<String, Object>> temp = new ArrayList<>();
        if (total == 0) return temp;
        int finished = 0;
        int page = 1;
        while (true) {
            HttpResultEntity entity = NianHttpUtil.comments(stepid, page);
            if (entity.isSuccess()) {
                Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                if (data != null) {
                    List<Map<String, Object>> comments = (List<Map<String, Object>>) data.get("comments");
                    if (comments == null || comments.size() == 0) { //获取评论为空说明加载完成
                        //logger.info(String.format("进展[%s]评论加载完成(%d/%d)", stepid, finished, total));
                        break;
                    } else {
                        finished += comments.size();
                        temp.addAll(comments);
                        //logger.info(String.format("进展[%s]加载第%03d页评论(%d/%d)", stepid, page, finished, total));
                        // 最后一页数据比pageSize要小。
                        Integer pageSize = Integer.valueOf(String.valueOf(data.get("perPage")));
                        if (comments.size() < pageSize) {
                            //logger.info(String.format("进展[%s]评论加载完成(%d/%d)", stepid, finished, total));
                            break;
                        }
                    }
                    page++;
                } else {
                    break;
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = temp.size() - 1; i >= 0; i--)
            result.add(temp.get(i));
        return result;
    }

    public static List<Map<String, Object>> downloadUserCareOrFans(String userid, String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        int page = 0;
        while (true) {
            HttpResultEntity entity = null;
            if ("care".equals(type))
                entity = NianHttpUtil.care(userid, page);
            if ("fans".equals(type))
                entity = NianHttpUtil.fans(userid, page);
            if (entity.isSuccess()) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) entity.getResponseMap().get("items");
                if (data != null) {
                    if (data.size() == 0)
                        break;
                    for (Map<String, Object> item : data) {
                        if (item != null) result.add(item);
                    }
                    page++;
                } else { // 为空退出
                    break;
                }
            }
        }
        //logger.info(String.format("用户[%s]%s数量[%d]", userid, type, result.size()));
        return result;
    }

    public static List<Map<String, Object>> downloadDreamLikeOrFans(String dreamid, String type) {
        List<Map<String, Object>> result = new ArrayList<>();
        int page = 1;
        while (true) {
            HttpResultEntity entity = null;
            if ("like".equals(type))
                entity = NianHttpUtil.dlike(dreamid, page);
            if ("fans".equals(type))
                entity = NianHttpUtil.dfans(dreamid, page);

            if (entity.isSuccess()) {
                Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                if (data != null) {
                    List<Map<String, Object>> users = (List<Map<String, Object>>) data.get("users");
                    if (users == null || users.size() == 0) //获取评论为空说明加载完成
                        break;
                    else
                        result.addAll(users);
                    page++;
                } else {
                    break;
                }
            }
        }
        return result;
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

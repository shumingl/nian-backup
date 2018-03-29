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

    public static void startup(int httpSize, int jsonSize) {
        httpThreadPool = new ThreadPoolExecutor(httpSize, httpSize * 2, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(),
                new NamedThreadFactory("HTTP"));
        jsonThreadPool = new ThreadPoolExecutor(jsonSize, jsonSize * 2, 0L,
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

    /**
     * 创建与用户目录
     *
     * @param userid 用户ID
     * @throws IOException
     */
    public static void createUserDirs(String userid) throws IOException {
        // 创建用户目录
        String cachebase = AppConfig.getNianCacheBase();
        File cachebasedir = new File(cachebase, userid);
        if (!cachebasedir.exists())
            cachebasedir.mkdirs();
        NianHtmlService.createUserDirs(userid);
    }

    /**
     * 通过邮箱及密码登录
     *
     * @param email    邮箱
     * @param password 密码
     * @return
     */
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

    /**
     * 登录成功后，会得到shell信息，根据shell信息可进行持续验证
     *
     * @param uid   用户ID
     * @param shell 登录shell
     */
    public static void loginByShell(String uid, String shell) {
        NianHttpUtil.LOGINFO.put("uid", uid);
        NianHttpUtil.LOGINFO.put("shell", shell);
    }

    /**
     * 登录后进行下载
     *
     * @param email    邮箱
     * @param password 密码
     * @throws IOException
     */
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

    /**
     * 多用户公共记本数据下载
     *
     * @param users 用户ID列表
     * @throws IOException
     */
    public static void downloadForUsers(String... users) throws IOException {
        if (users != null && users.length > 0) {
            for (String userid : users)
                downloadForUser(userid);
        }
    }

    /**
     * 下载单用户公共记本数据
     *
     * @param userid 用户ID
     * @throws IOException
     */
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
            String username = StringUtil.MAPGET(userdata, "user/name");
            // 下载用户的记本
            downloadDreams(username, userid);
        }
    }

    /**
     * 根据用户UserID下载公共记本数据
     *
     * @param username 用户名
     * @param userid   用户UserID
     * @throws IOException
     */
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

    /**
     * 下载用户的记本数据（线程下载）
     *
     * @param userid  UserID
     * @param dreamid 记本ID
     */
    public static void downloadDream(String userid, String dreamid) {
        httpThreadPool.execute(new NianDreamHttpWorker("json", userid, dreamid));
    }

    /**
     * 生成用户数据JSON数据
     *
     * @param userid  用户UserID
     * @param dreamid 记本ID
     */
    public static void generateDreamJson(String userid, String dreamid) {
        try {
            Map<String, Object> dataModel = NianJsonService.downloadAllSteps(userid, dreamid);
            if (userid == null) {
                if (dataModel != null) {
                    userid = StringUtil.MAPGET(dataModel, "dream/uid");
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
                String dreamtitle = StringUtil.MAPGET(dataModel, "dream/title");
                jsonThreadPool.execute(new NianDreamJsonWorker(userid, dreamtitle, dreamid, dataModel));
            }
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
    }

    /**
     * 检查是否有数据更新
     *
     * @param userid  用户ID
     * @param dreamid 记本ID
     * @return
     * @throws IOException
     */
    private static boolean checkupdate(String userid, String dreamid) throws IOException {

        String basepath = NianJsonService.getCachePath(userid, "dream");
        File summary = new File(StringUtil.path(basepath, dreamid + "-info.json"));
        File cache = new File(StringUtil.path(basepath, dreamid + ".json"));

        // 检查是否需要重新下载
        if (!summary.exists() || !cache.exists()) {
            return true;

        } else {
            Map<String, Object> httpjson = NianJsonService.downloadFirstPageSteps(dreamid);
            if (httpjson != null && httpjson.size() > 0) {
                Map<String, Object> httpdream = (Map<String, Object>) httpjson.get("dream");
                Map<String, Object> localdream = NianJsonService.takeoutCache(userid, dreamid, "dinfo");
                if (localdream == null || localdream.size() == 0)
                    return true;

                if (httpdream != null && httpdream.size() > 0) {    //比较数据是否有更新
                    return checkDreamChange(localdream, httpdream);
                } else {        // 本地cache或http获取数据中，dream为空
                    throw new RuntimeException(String.format("[%s/%s]下载首页数据失败", userid, dreamid));
                }
            } else {            // http获取数据为空
                throw new RuntimeException(String.format("[%s/%s]下载首页数据失败", userid, dreamid));
            }
        }
    }

    /**
     * 下载记本第一页数据
     *
     * @param dreamid
     * @return
     */
    private static Map<String, Object> downloadFirstPageSteps(String dreamid) {
        Map<String, Object> data = null;
        HttpResultEntity entity = NianHttpUtil.steps(dreamid, 1);
        if (entity.isSuccess()) {
            data = (Map<String, Object>) entity.getResponseMap().get("data");
            if (data != null) {
                String dreamtitle = StringUtil.MAPGET(data, "dream/title");
                logger.info(String.format("记本[%s(%s)]首页下载成功", dreamtitle, dreamid));
            }
        } else {
            String msg = String.format("记本[%s]首页下载失败[HTTP/%s]：%s", dreamid,
                    entity.getStatusCode(), entity.getMessage());
            throw new RuntimeException(msg);
        }
        return data;
    }

    /**
     * 加载本地缓存数据
     *
     * @param userid  用户UserID
     * @param dreamid 记本ID
     * @return
     */
    public static Map<String, Object> downloadFromLocal(String userid, String dreamid) {
        Map<String, Object> data = null;
        try {
            data = NianJsonService.takeoutCache(userid, dreamid, "dream");
        } catch (Exception e) {
            logger.error(String.format("[%s/%s]读解析本地记本数据错误：%s", userid, dreamid, e.getMessage()));
        }
        return data;
    }

    /**
     * 下载用户信息
     *
     * @param userid 用户UserID
     * @return
     */
    public static Map<String, Object> downloadUserInfo(String userid) throws IOException {

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
                    String cachebase = NianJsonService.getCachePath(userid, "cache");
                    String userpath = StringUtil.path(cachebase, "user.json");
                    FileUtil.createParentDirs(new File(userpath));
                    String json = JsonUtil.object2Json(userinfo);
                    Files.write(Paths.get(userpath), json.getBytes("UTF-8"));
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

    /**
     * 下载用户记本数据
     *
     * @param userid 用户ID
     * @return
     */
    public static Map<String, Object> downloadUserDreams(String userid) {

        Map<String, Object> dreamsCache = null;
        try {
            dreamsCache = NianJsonService.takeoutCache(userid, null, "list");
        } catch (IOException e) {
            logger.error("获取本地记本缓存[{}/dreams.json]错误：{}", userid, e.getMessage());
        }
        String model = AppConfig.getNianRenderModel();
        Map<String, Object> data = null;
        if ("online".equals(model)) {
            HttpResultEntity entity = NianHttpUtil.dreams(userid);
            if (entity != null && entity.isSuccess()) {
                data = (Map<String, Object>) entity.getResponseMap().get("data");

                //使用新数据覆盖掉缓存旧数据
                if (dreamsCache != null && dreamsCache.containsKey("dreams")) {
                    List<Map<String, Object>> cachelist = (List<Map<String, Object>>) dreamsCache.get("dreams");
                    if (cachelist != null && cachelist.size() > 0) {
                        if (data != null && data.containsKey("dreams")) {
                            List<Map<String, Object>> datalist = (List<Map<String, Object>>) data.get("dreams");
                            if (cachelist != null && cachelist.size() > 0) {
                                for (int cacheidx = 0; cacheidx < cachelist.size(); cacheidx++) {
                                    String cacheDreamId = (String) cachelist.get(cacheidx).get("id");
                                    for (int dataidx = 0; dataidx < datalist.size(); dataidx++) {
                                        String dataDreamId = (String) datalist.get(dataidx).get("id");
                                        if (cacheDreamId.equals(dataDreamId)) {
                                            cachelist.get(cacheidx).putAll(datalist.get(dataidx));
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                String basepath = NianJsonService.getCachePath(userid, "cache");
                String fullname = StringUtil.path(basepath, "dreams.json");
                FileUtil.createParentDirs(new File(fullname));

                // 保存记本列表信息
                String datajson = JsonUtil.object2Json(dreamsCache);
                if (datajson != null) {
                    try {
                        Files.write(Paths.get(fullname), datajson.getBytes("UTF-8"));
                    } catch (IOException e) {
                        logger.error("写入本地记本列表[{}/dreams.json]错误：{}", userid, e.getMessage());
                    }
                }
            }
        } else if ("offline".equals(model)) {
            data = dreamsCache;
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

    /**
     * 下载记本所有进展数据
     *
     * @param userid  用户ID
     * @param dreamid 记本ID
     * @return
     */
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

    public static boolean checkStepPageUpdate(Map<String, String> cacheIndex, List<Map<String, Object>> stepList) {
        // 检查数据是否有变动
        boolean hasChanged = false;
        for (Map<String, Object> dest : stepList) {
            String destStepId = (String) dest.get("sid");
            String destVal = parser.parse("${lastdate}/${comments}/${likes}", dest);
            if (cacheIndex.containsKey(destStepId)) {
                String localVal = cacheIndex.get(destStepId);
                if (!localVal.equals(destVal))// 找到进展且有更新
                    hasChanged = true;
            } else { // 没找到则有更新
                hasChanged = true;
                break;
            }
        }
        return hasChanged;
    }

    /**
     * 从念的服务器下载数据
     *
     * @param userid  用户ID
     * @param dreamid 记本ID
     * @return
     */
    public static Map<String, Object> downloadFromApi(String userid, String dreamid) {
        try {

            Map<String, Object> dataModel = new HashMap<>();
            List<Map<String, Object>> steps = new ArrayList<>();

            // 本地缓存
            Map<String, String> cacheIndex = null;
            Map<String, Object> dreamCache = NianJsonService.takeoutCache(userid, dreamid, "dream");
            List<Map<String, Object>> cacheSteps = null;
            if (dreamCache != null) {
                cacheIndex = new LinkedHashMap<>();
                cacheSteps = (List<Map<String, Object>>) dreamCache.get("steps");
                for (Map<String, Object> step : cacheSteps) {
                    String stepId = (String) step.get("sid");
                    String val = parser.parse("${lastdate}/${comments}/${likes}", step);
                    cacheIndex.put(stepId, val);
                }
            }

            // 从第1页开始读取数据
            int steptotal = 0;
            String dreamtitle = "";
            int finished = 0;
            int page = 1;
            while (true) {
                HttpResultEntity entity = NianHttpUtil.steps(dreamid, page);
                if (entity.isSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                    if (data != null) {
                        if (page == 1) {
                            dataModel.putAll(data);
                            Map<String, Object> dream = (Map<String, Object>) data.get("dream");
                            if (dream == null)
                                throw new RuntimeException(String.format("记本[%s]信息获取失败", dreamid));
                            dreamtitle = String.valueOf(dream.get("title"));
                            steptotal = Integer.valueOf(String.valueOf(dream.get("step")));
                            logger.info(String.format("记本[%s(%s)]共有[%d]条进展", dreamtitle, dreamid, steptotal));
                        }

                        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("steps");
                        if (list == null || list.size() == 0) {
                            logger.info(String.format("记本[%s(%s)]下载完成", dreamtitle, dreamid));
                            break;
                        } else {

                            // 获取评论
                            for (Map<String, Object> step : list) {
                                step.put("stepcomments", new ArrayList<>());
                                step.put("steplikes", new ArrayList<>());
                                String stepid = String.valueOf(step.get("sid"));
                                Integer datacmts = Integer.valueOf(String.valueOf(step.get("comments")));
                                if (datacmts > 0) {
                                    List<Map<String, Object>> comments = downloadStepALlComments(stepid, datacmts);
                                    step.put("stepcomments", comments);
                                }
                            }

                            finished += list.size();
                            steps.addAll(list);
                            logger.info(String.format("记本[%s(%s)]第%04d页进展下载成功(%d/%d)", dreamtitle, dreamid, page, finished, steptotal));

                            if (cacheIndex != null) { // 如果没有本地缓存，则不检查，执行全量下载
                                boolean hasChanged = checkStepPageUpdate(cacheIndex, list);
                                // 当页数据没有更新，则不再下载后续的进展
                                if (!hasChanged) {
                                    // 没有更新说明对当前页的数据发生了全部比对
                                    // 当前页的最后一条进展ID，下一条就是历史数据
                                    String beginStepId = (String) list.get(list.size() - 1).get("sid");
                                    boolean ismatched = false;
                                    for (int i = 0; i < cacheSteps.size(); i++) {
                                        String sid = (String) cacheSteps.get(i).get("sid");
                                        if (!ismatched) {
                                            if (beginStepId.equals(sid)) ismatched = true;
                                        } else {
                                            steps.add(cacheSteps.get(i));
                                        }
                                    }
                                    logger.info(String.format("记本[%s(%s)]从本地缓存获取剩余数据(total=%d)", dreamtitle, dreamid, steps.size()));
                                    break; // exit while
                                }
                            }
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

    /**
     * 下载进展全部点赞用户
     *
     * @param stepid
     * @return
     */
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

    /**
     * 下载进展全部评论
     *
     * @param stepid 记本ID
     * @param total  总数，用于分页和最后一页判断
     * @return
     */
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
                        break;
                    } else {
                        finished += comments.size();
                        temp.addAll(comments);
                        // 最后一页数据比pageSize要小。
                        Integer pageSize = Integer.valueOf(String.valueOf(data.get("perPage")));
                        if (comments.size() < pageSize) {
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

    /**
     * 下载用户的关注和粉丝数据
     *
     * @param userid 用户ID
     * @param type   类型(care/fans)
     * @return
     */
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

    /**
     * 下载记本的点赞用户或关注用户
     *
     * @param dreamid 记本ID
     * @param type    类型(like/fans)
     * @return
     */
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

    /**
     * 取出本地缓存数据
     *
     * @param userid  用户ID
     * @param dreamid 记本ID
     * @param type    类型（user/list/dream/dinfo）用户信息/记本列表/记本内容
     * @return
     */
    public static Map<String, Object> takeoutCache(String userid, String dreamid, String type) throws IOException {
        String filename;
        if ("user".equals(type)) {
            filename = StringUtil.path(getCachePath(userid, "cache"), "user.json");
        } else if ("list".equals(type)) {
            filename = StringUtil.path(getCachePath(userid, "cache"), "dreams.json");
        } else if ("dream".equals(type)) {
            filename = StringUtil.path(getCachePath(userid, "dream"), dreamid + ".json");
        } else if ("dinfo".equals(type)) {
            filename = StringUtil.path(getCachePath(userid, "dream"), dreamid + "-info.json");
        } else {
            return null;
        }
        File file = new File(filename);
        if (!file.exists())
            return null;
        byte[] bytes = Files.readAllBytes(Paths.get(filename));
        String json = new String(bytes, "UTF-8");
        json = json.replace("<", "&lt;").replace(">", "&gt;").replace("&lt;br&gt;", "\\n");
        Map<String, Object> cache = JsonUtil.json2Map(json);
        return cache;
    }

    public static boolean checkDreamChange(Map<String, Object> local, Map<String, Object> dest) {
        if (local == null || dest == null)
            return true;
        //比较数据是否有更新
        String template = "${lastdate}/${step}/${like_step}/${followers}";
        String localVal = parser.parse(template, local);
        String destVal = parser.parse(template, dest);
        if (localVal.equals(destVal)) {
            return false;
        } else {
            return true;
        }
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

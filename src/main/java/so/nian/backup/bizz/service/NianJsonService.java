package so.nian.backup.bizz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.config.AppConstants;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.DataUtil;
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
        // 创建用户目录
        NianJsonService.createUserDirs(userid);
        String basepath = NianJsonService.getCachePath(userid, "cache");
        String fullname = StringUtil.path(basepath, "user.json");
        FileUtil.createParentDirs(new File(fullname));

        // 下载用户信息
        Map<String, Object> userdata = NianJsonService.downloadUserInfo(userid);
        String username = "";
        if (userdata != null) {
            username = (String) userdata.get("name");
        } else {
            username = userid;
        }
        // 下载用户的记本
        downloadDreams(username, userid);
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
                /*if (!StringUtil.isNullOrEmpty(image))
                    NianImageDownload.download(userid, "dream", image);*/
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
            if (AppConstants.RENDER_MODEL_ONLINE.equals(model)) { // ONLINE模式生成记本内容
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
                Map<String, Object> localdream = NianJsonService.takeoutCache(userid, dreamid, AppConstants.CACHE_TYPE_DREAMINFO);
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
            data = NianJsonService.takeoutCache(userid, dreamid, AppConstants.CACHE_TYPE_DREAMDATA);
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
        String cachebase = NianJsonService.getCachePath(userid, "cache");
        Map<String, Object> userinfo = null;
        if (AppConstants.RENDER_MODEL_ONLINE.equals(model)) {
            logger.info("下载用户信息[{}]", userid);
            HttpResultEntity info = NianHttpUtil.info(userid);
            if (info != null && info.isSuccess()) {
                Map<String, Object> data = (Map<String, Object>) info.getResponseMap().get("data");
                userinfo = (Map<String, Object>) data.get("user");
                if (userinfo != null && userinfo.get("uid") != null) { //有些不存在的UserID下载的数据uid是null
                    // 下载用户关注和粉丝
                    logger.info("下载用户关注[{}]", userid);
                    List<Map<String, Object>> care = downloadUserCareOrFans(userid, "care");
                    logger.info("下载用户粉丝[{}]", userid);
                    List<Map<String, Object>> fans = downloadUserCareOrFans(userid, "fans");
                    userinfo.put("care", care);
                    userinfo.put("fans", fans);
                    String userpath = StringUtil.path(cachebase, "user.json");
                    FileUtil.writeJson(new File(userpath), userinfo);
                }
            } else {
                logger.error(String.format("获取用户信息失败[%s]", userid));
            }
            return userinfo;
        } else if (AppConstants.RENDER_MODEL_OFFLINE.equals(model)) {
            String userpath = StringUtil.path(cachebase, "user.json");
            File userfile = new File(userpath);
            if (userfile.exists()) {
                try {
                    String userjson = FileUtil.readAll(userfile);
                    userinfo = JsonUtil.json2Map(userjson);
                } catch (Exception e) {
                    logger.error("获取本地用户文件[{}/user.json]错误：{}", userid, e.getMessage());
                }
            }
            return userinfo;
        } else {
            throw new RuntimeException("[downloadUserInfo]参数错误：nian.render.model[online/offline]");
        }
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
            dreamsCache = NianJsonService.takeoutCache(userid, null, AppConstants.CACHE_TYPE_DREAMLIST);
        } catch (IOException e) {
            logger.error("获取本地记本缓存[{}/dreams.json]错误：{}", userid, e.getMessage());
        }
        String model = AppConfig.getNianRenderModel();
        Map<String, Object> data = null;
        if (AppConstants.RENDER_MODEL_ONLINE.equals(model)) {
            HttpResultEntity entity = NianHttpUtil.dreams(userid);
            if (entity != null && entity.isSuccess()) {
                data = (Map<String, Object>) entity.getResponseMap().get("data");
                //使用新数据覆盖掉缓存旧数据
                if (dreamsCache != null && data != null)
                    DataUtil.merge((List<Map<String, Object>>) dreamsCache.get("dreams"), (List<Map<String, Object>>) data.get("dreams"), "${id}");

                String basepath = NianJsonService.getCachePath(userid, "cache");
                String fullname = StringUtil.path(basepath, "dreams.json");
                FileUtil.writeJson(new File(fullname), dreamsCache);

            }
            if (dreamsCache != null)
                data = dreamsCache;
        } else if (AppConstants.RENDER_MODEL_OFFLINE.equals(model)) {
            data = dreamsCache;
        } else
            throw new RuntimeException("[downloadUserDreams]参数错误：nian.render.model[online/offline]");
        if (data != null) {
            List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
            if (dreams != null) {
                for (Map<String, Object> dream : dreams) {
                    if (!StringUtil.isNullOrEmpty(dream.get("image")))
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
        if (AppConstants.RENDER_MODEL_ONLINE.equals(model))
            data = downloadFromApi(userid, dreamid);
        else if (AppConstants.RENDER_MODEL_OFFLINE.equals(model))
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

    public static boolean stepIsSame(Map<String, Object> left, Map<String, Object> right) {
        if (left == null || right == null) return false;
        List<Map<String, Object>> leftCmts = (List<Map<String, Object>>) left.get("stepcomments");
        List<Map<String, Object>> rightCmts = (List<Map<String, Object>>) right.get("stepcomments");
        int leftCCnt = leftCmts == null ? 0 : leftCmts.size();
        int rightCCnt = rightCmts == null ? 0 : rightCmts.size();
        String tpl = "${lastdate}/${comments}/${likes}/${content}";
        String leftval = parser.parse(tpl, left) + "/" + leftCCnt;
        String rightval = parser.parse(tpl, right) + "/" + rightCCnt;
        return leftval.equals(rightval);
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
            Map<String, Map<String, Object>> stepMap = new HashMap<>();

            // 本地缓存
            Map<String, String> compareIndex = null;
            Map<String, Map<String, Object>> cacheIndex = null;
            Map<String, Object> dreamCache = NianJsonService.takeoutCache(userid, dreamid, AppConstants.CACHE_TYPE_DREAMDATA);
            List<Map<String, Object>> cacheSteps = null;
            compareIndex = new LinkedHashMap<>();
            cacheIndex = new LinkedHashMap<>();
            if (dreamCache != null) {
                cacheSteps = (List<Map<String, Object>>) dreamCache.get("steps");
                int num = 0;
                for (Map<String, Object> step : cacheSteps) {
                    String stepId = (String) step.get("sid");
                    String val = parser.parse("${lastdate}/${comments}/${likes}", step);
                    compareIndex.put(stepId, val);
                    cacheIndex.put(stepId, step);//将缓存的数据，List转换成Map，用于合并数据
                    num++;
                }
            }

            // 从第1页开始读取数据
            int steptotal = 0;
            String dreamtitle = "";
            int finished = 0;
            int page = 1;
            while (true) {
                // 调用API下载某一页的进展数据
                HttpResultEntity entity = NianHttpUtil.steps(dreamid, page);
                if (entity.isSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                    if (data != null) {
                        // 从第一页数据中获取所需要的信息
                        if (page == 1) {
                            dataModel.putAll(data);
                            Map<String, Object> dream = (Map<String, Object>) data.get("dream");
                            if (dream == null)
                                throw new RuntimeException(String.format("记本[%s]信息获取失败", dreamid));
                            dreamtitle = String.valueOf(dream.get("title"));
                            steptotal = Integer.valueOf(String.valueOf(dream.get("step")));
                            logger.info(String.format("记本[%s(%s)]共有[%d]条进展", dreamtitle, dreamid, steptotal));
                        }
                        // 获取到的数据如果为空说明已经没有数据了
                        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("steps");
                        if (list == null || list.isEmpty()) {
                            logger.info(String.format("记本[%s(%s)]下载完成", dreamtitle, dreamid));
                            break;
                        } else {
                            // 保存数据
                            for (Map<String, Object> step : list)
                                stepMap.put((String) step.get("sid"), step);
                            finished = stepMap.size();
                            logger.info(String.format("记本[%s(%s)]第%04d页进展下载成功(%d/%d)", dreamtitle, dreamid, page, finished, steptotal));
                            // 如果没有本地缓存，则不检查，执行全量下载
                            if (!compareIndex.isEmpty()) {
                                // 当页数据没有更新，则不再下载后续的进展
                                boolean hasChanged = checkStepPageUpdate(compareIndex, list);
                                if (!hasChanged) {
                                    break;
                                }
                            }
                        }
                        page++;
                    } else {
                        break;
                    }
                } else {
                    logger.error(String.format("记本[%s(%s)]第%04d页进展下载失败：%s", dreamtitle, dreamid, page, entity.getMessage()));
                }
            }


            // 对有更新的进展进行下载评论
            if (!stepMap.isEmpty()) {
                /*
                for (String stepId : stepMap.keySet()) {
                    Map<String, Object> step = stepMap.get(stepId);
                    if (!cacheIndex.containsKey(stepId) ||
                            cacheIndex.containsKey(stepId) && !stepIsSame(cacheIndex.get(stepId), step)) {
                        logger.info(String.format("下载用户[%s]评论[%s]", userid, stepId));
                        step.put("stepcomments", new ArrayList<>());
                        step.put("steplikes", new ArrayList<>());
                        String stepid = String.valueOf(step.get("sid"));
                        Integer datacmts = Integer.valueOf(String.valueOf(step.get("comments")));
                        if (datacmts > 0) {
                            List<Map<String, Object>> comments = downloadStepALlComments(stepid, datacmts);
                            step.put("stepcomments", comments);
                        }
                    }
                }
                */

                cacheIndex.putAll(stepMap);//合并后的数据
            }

            List<Map<String, Object>> steps = new ArrayList<>();
            // 合并后的Map数据转换成List
            for (String stepId : cacheIndex.keySet()) {
                Map<String, Object> step = cacheIndex.get(stepId);
                // 多用户编辑的记本，需要下载用户的头像
                NianImageDownload.download(userid, "head", cacheIndex.get(stepId).get("uid") + ".jpg");
                steps.add(cacheIndex.get(stepId));
                String stepid = String.valueOf(step.get("sid"));
                Integer datacmts = Integer.valueOf(String.valueOf(step.get("comments")));
                if (datacmts > 0) {
                    if (!step.containsKey("stepcomments") ||
                            ((List<Map<String, Object>>) step.get("stepcomments")).size() != datacmts) {
                        logger.info(String.format("下载记本[%s/%s]评论[%s][TOTAL:%d]", userid, dreamid, stepId, datacmts));
                        List<Map<String, Object>> comments = downloadStepALlComments(stepid, datacmts);
                        step.put("stepcomments", comments);
                    }
                }
            }
            logger.info(String.format("记本[%s(%s)]最新数据与本地缓存进行合并(total=%d)", dreamtitle, dreamid, steps.size()));

            // 对最终的List数据，根据更新日期进行排序
            Collections.sort(steps, (left, right) -> {
                if (left == null || right == null) throw new RuntimeException("排序数据不能为空");
                Long leftval = Long.valueOf((String) left.get("lastdate"));
                Long rightval = Long.valueOf((String) right.get("lastdate"));
                if (leftval > rightval) return -1;
                else if (leftval < rightval) return 1;
                else return 0;
            });

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
        if (AppConstants.CACHE_TYPE_USERINFO.equals(type)) {
            filename = StringUtil.path(getCachePath(userid, "cache"), "user.json");
        } else if (AppConstants.CACHE_TYPE_DREAMLIST.equals(type)) {
            filename = StringUtil.path(getCachePath(userid, "cache"), "dreams.json");
        } else if (AppConstants.CACHE_TYPE_DREAMDATA.equals(type)) {
            filename = StringUtil.path(getCachePath(userid, "dream"), dreamid + ".json");
        } else if (AppConstants.CACHE_TYPE_DREAMINFO.equals(type)) {
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

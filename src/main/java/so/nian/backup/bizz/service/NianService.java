package so.nian.backup.bizz.service;

import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.freemarker.factory.ReportTemplateFactory;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.ExpressionParser;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.spring.ContextUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class NianService {

    private static final Logger logger = LoggerFactory.getLogger(NianService.class);

    public void downloadForUsers(String[] users) throws IOException {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        if (users != null && users.length > 0) {
            for (String userid : users)
                downloadForUser(userid);
        }
    }

    public void downloadForUser(String userid) throws IOException {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");

        ExpressionParser parser = ExpressionParser.getDefault();
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

        System.setProperty("nian.userid", userid);
        System.setProperty("nian.userbase", userdir);
        System.setProperty("nian.imagebase", imagebasedir.getCanonicalPath());

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
                        String template = "名称: ${name};\n" +
                                "UID : ${uid};\n" +
                                "邮箱: ${email};\n" +
                                "硬币: ${coin};\n" +
                                "关注: ${follows};\n" +
                                "粉丝: ${followed};\n" +
                                "获赞: ${likes};\n" +
                                "梦想: ${dream};\n" +
                                "进展: ${step};\n";
                        String userInfoString = parser.parse(template, userinfo);
                        Files.write(Paths.get(userdir, "userinfo.txt"), userInfoString.getBytes());
                        username = String.valueOf(userinfo.get("name"));
                        System.setProperty("nian.username", username);
                        isloadinfo = true;
                    }
                }
            } else {
                logger.error(String.format("获取用户信息为空[%s].", userid));
            }
        } else {
            logger.error(String.format("获取用户信息失败[%s].", userid));
        }
        if (!isloadinfo) return;

        // 下载用户的记本
        downloadDreams(username, userid);
    }

    private void downloadDreams(String username, String userid) {

        logger.info(String.format("用户[%s(%s)]开始下载", username, userid));
        HttpResultEntity entity = NianHttpUtil.dreams(userid);
        if (entity.isSuccess()) {
            Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
            if (data != null) {
                List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
                logger.info(String.format("用户[%s(%s)]记本数量：[%d]", username, userid, dreams.size()));
                for (Map<String, Object> dream : dreams) {
                    generateDreamHtml(String.valueOf(dream.get("id")));
                }
            } else {
                logger.info(String.format("用户[%s(%s)]记本列表为空：%s", username, userid, entity.getResponseBody()));
            }
        } else {
            logger.error(String.format("用户[%s(%s)]记本列表下载失败：%s", username, userid, entity.getMessage()));
        }
        logger.info(String.format("用户[%s(%s)]记本下载完成。", username, userid));
        NianImageDownload.closewait();
    }

    public void generateDreamHtml(String dreamid) {
        String tplname = "dream.ftl";
        String basepath = AppConfig.getNianViewsBase();
        String userid = System.getProperty("nian.userid");
        String dreamtitle = "";
        int steptotal = 0;
        logger.info(String.format("记本[%s]开始下载", dreamid));
        try {
            List<Map<String, Object>> steps = new ArrayList<>();
            Map<String, Object> dataModel = new HashMap<>();

            // 读取全部的进展内容
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
                            if (dream != null) {
                                dreamtitle = String.valueOf(dream.get("title"));
                                steptotal = Integer.valueOf(String.valueOf(dream.get("step")));
                                if (userid == null) { // 如果不是从downloadForUsers进来的，就不会有这个值
                                    userid = String.valueOf(dream.get("uid"));
                                }
                                logger.info(String.format("记本[%s(%s)]共有[%d]条进展。", dreamtitle, dreamid, steptotal));
                            }
                        }
                        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("steps");
                        if (list == null || list.size() == 0) {
                            logger.info(String.format("记本[%s(%s)]下载完成。", dreamtitle, dreamid));
                            break;
                        } else {
                            finished += list.size();
                            steps.addAll(list);
                            logger.info(String.format("记本[%s(%s)]第%04d页进展下载成功(%d/%d)。",
                                    dreamtitle, dreamid, page, finished, steptotal));
                        }
                        page++;
                    } else {
                        break;
                    }
                } else {
                    logger.error(String.format("记本[%s(%s)]第%04d页进展下载失败。", dreamtitle, dreamid, page));
                }
            }

            // 开始生成记本内容
            logger.info(String.format("记本[%s(%s)]生成网页开始。", dreamtitle, dreamid));
            File file = new File(StringUtil.generatePath(basepath, userid, dreamid + ".html"));
            FileUtil.createParentDirs(file);
            dataModel.put("steps", steps);
            ReportTemplateFactory templateFactory = ContextUtil.getBean("templateFactory");
            Template template = templateFactory.getTemplate(tplname);
            templateFactory.process(template, file.getCanonicalPath(), dataModel, "UTF-8");
            logger.info(String.format("记本[%s(%s)]生成网页完成。", dreamtitle, dreamid));
        } catch (Exception e) {
            logger.error(String.format("记本[%s]下载异常：%s", dreamid, e.getMessage()));
        }
    }
}

package so.nian.backup.bizz.service;

import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.freemarker.factory.ReportTemplateFactory;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.spring.ContextUtil;

import java.io.File;
import java.util.*;

public class NianService {

    private static final Logger logger = LoggerFactory.getLogger(NianService.class);

    public void dealInfo(String userid) {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        HttpResultEntity info = NianHttpUtil.info(userid);
        HttpResultEntity care = NianHttpUtil.care(userid, 1);
        HttpResultEntity fans = NianHttpUtil.fans(userid, 1);
    }

    public void findDreams(String userid) {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        logger.info(String.format("FindDreams[%s]", userid));
        HttpResultEntity entity = NianHttpUtil.dreams(userid);
        if (entity.isSuccess()) {
            Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
            if (data != null) {
                List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
                logger.info(String.format("Dreams.Size[%s]", dreams.size()));
                for (Map<String, Object> dream : dreams) {
                    dealDream(String.valueOf(dream.get("id")));
                }
            } else {
                logger.info(String.format("[%s]下载梦想：%s", userid, entity.getResponseBody()));
            }
        } else {
            logger.error(String.format("[%s]下载梦想清单失败：%s", userid, entity.getMessage()));
        }
        logger.info(String.format("FindDreams[%s], DONE.", userid));
        NianImageDownload.shutdown();
    }

    public void dealDream(String dreamid) {
        String tplname = "dream.ftl";
        String basepath = AppConfig.getNianViewsBase();
        logger.info(String.format("DealDream[%s]", dreamid));
        try {
            File file = new File(basepath, dreamid + ".html");
            List<Map<String, Object>> steps = new ArrayList<>();
            Map<String, Object> dataModel = new HashMap<>();
            // 读取全部的进展内容
            int page = 1;
            while (true) {
                HttpResultEntity entity = NianHttpUtil.steps(dreamid, page);
                if (entity.isSuccess()) {
                    Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
                    if (data != null) {
                        if (page == 1) dataModel.putAll(data);
                        List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("steps");
                        if (list == null || list.size() == 0)
                            break;
                        else
                            steps.addAll(list);
                        page++;
                    } else {
                        break;
                    }
                }
            }
            dataModel.put("steps", steps);
            ReportTemplateFactory templateFactory = ContextUtil.getBean("templateFactory");
            Template template = templateFactory.getTemplate(tplname);
            templateFactory.process(template, file.getCanonicalPath(), dataModel, "UTF-8");
        } catch (Exception e) {
            logger.error(String.format("导出梦想[%s]失败：%s", dreamid, e.getMessage()));
        }
    }
}

package so.nian.backup.bizz.service;

import freemarker.template.Template;
import so.nian.backup.config.AppConfig;
import so.nian.backup.freemarker.factory.ReportTemplateFactory;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.utils.spring.ContextUtil;

import java.io.File;
import java.util.*;

public class NianService {

    public void dealInfo(String userid) {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        HttpResultEntity info = NianHttpUtil.info("");
    }

    public void findDreamList(String userid) {
        NianHttpUtil.LOGINFO.put("uid", userid);
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        System.out.printf("findDreamList(%s)\n", userid);
        HttpResultEntity entity = NianHttpUtil.dreams(userid);
        if (entity.isSuccess()) {
            Map<String, Object> data = (Map<String, Object>) entity.getResponseMap().get("data");
            if (data != null) {
                List<Map<String, Object>> dreams = (List<Map<String, Object>>) data.get("dreams");
                for (Map<String, Object> dream : dreams) {
                    dealDream(String.valueOf(dream.get("id")));
                }
            }
        }
        System.out.printf("findDreamList(%s), DONE.\n", userid);
    }

    public void dealDream(String dreamid) {
        String tplname = "dream.ftl";
        String basepath = AppConfig.getNianViewsBase();
        System.out.printf("dealDream(%s)\n", dreamid);
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
            e.printStackTrace();
        }
    }
}

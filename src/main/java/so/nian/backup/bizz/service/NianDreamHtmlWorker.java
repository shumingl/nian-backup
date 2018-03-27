package so.nian.backup.bizz.service;

import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.freemarker.factory.ReportTemplateFactory;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.spring.ContextUtil;

import java.io.File;
import java.util.Map;

public class NianDreamHtmlWorker extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(NianDreamHtmlWorker.class);

    private String userid;
    private String dreamtitle;
    private String dreamid;
    private Map<String, Object> dataModel;

    public NianDreamHtmlWorker(String userid, String dreamtitle, String dreamid, Map<String, Object> dataModel) {
        this.userid = userid;
        this.dreamtitle = dreamtitle;
        this.dreamid = dreamid;
        this.dataModel = dataModel;
    }

    @Override
    public void run() {
        try {
            String tplname = AppConfig.getNianRenderTemplate();
            String basepath = AppConfig.getNianViewsBase();
            // 开始生成记本内容
            logger.info(String.format("记本[%s(%s)]生成网页开始", dreamtitle, dreamid));
            File file = new File(StringUtil.path(basepath, userid, dreamid + ".html"));
            FileUtil.createParentDirs(file);
            if (file.exists()) file.delete();
            ReportTemplateFactory templateFactory = ContextUtil.getBean("templateFactory");
            Template template = templateFactory.getTemplate(tplname);
            templateFactory.process(template, file.getCanonicalPath(), dataModel, "UTF-8");
            logger.info(String.format("记本[%s(%s)]生成网页完成", dreamtitle, dreamid));
        } catch (Exception e) {
            logger.error(String.format("记本[%s(%s)]生成网页失败: [%s]", dreamtitle, dreamid, e.getMessage()));
        }
    }
}

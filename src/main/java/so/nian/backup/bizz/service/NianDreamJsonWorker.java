package so.nian.backup.bizz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class NianDreamJsonWorker extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(NianDreamJsonWorker.class);

    private String userid;
    private String dreamtitle;
    private String dreamid;
    private Map<String, Object> dataModel;

    public NianDreamJsonWorker(String userid, String dreamtitle, String dreamid, Map<String, Object> dataModel) {
        this.userid = userid;
        this.dreamtitle = dreamtitle;
        this.dreamid = dreamid;
        this.dataModel = dataModel;
    }

    @Override
    public void run() {
        try {
            String basepath = NianJsonService.getCachePath(userid, "dream");
            // 开始生成记本内容
            logger.info(String.format("记本[%s(%s)]数据存储开始", dreamtitle, dreamid));
            String fullname = StringUtil.path(basepath, dreamid + ".json");
            File file = new File(fullname);
            FileUtil.createParentDirs(file);

            String summaryfile = StringUtil.path(basepath, dreamid + "-info.json");

            String sjson = JsonUtil.object2Json(dataModel.get("dream"));
            byte[] sbytes = sjson.getBytes("UTF-8");
            Files.write(Paths.get(summaryfile), sbytes);

            String json = JsonUtil.object2Json(dataModel);
            byte[] bytes = json.getBytes("UTF-8");
            Files.write(Paths.get(fullname), bytes);
            logger.info(String.format("记本[%s(%s)]数据存储完成", dreamtitle, dreamid));
        } catch (Exception e) {
            logger.error(String.format("记本[%s(%s)]数据存储失败: [%s]", dreamtitle, dreamid, e.getMessage()));
        }
    }
}

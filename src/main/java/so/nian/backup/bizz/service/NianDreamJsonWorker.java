package so.nian.backup.bizz.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
            String basepath = AppConfig.getNianCacheBase();
            // 开始生成记本内容
            logger.info(String.format("记本[%s(%s)]数据存储开始", dreamtitle, dreamid));
            String fullname = StringUtil.generatePath(basepath, userid, dreamid + ".json");
            File file = new File(fullname);
            FileUtil.createParentDirs(file);

            String summaryfile = StringUtil.generatePath(basepath, userid, dreamid + "-info.json");
            File summary = new File(summaryfile);


            List<Map<String, Object>> steps = (List<Map<String, Object>>) dataModel.get("steps");
            for (Map<String, Object> step : steps) {
                List<Map<String, Object>> images = (List<Map<String, Object>>) step.get("images");
                String firstimage = String.valueOf(step.get("image"));
                if (images != null && images.size() > 0) {
                    for (Map<String, Object> image : images) {
                        NianImageDownload.download(userid, "step", String.valueOf(image.get("path")));
                    }
                } else {
                    if (!StringUtil.isNullOrEmpty(firstimage))
                        NianImageDownload.download(userid, "step", firstimage);
                }
            }

            if (summary.exists()) summary.delete();
            String sjson = JsonUtil.object2Json(dataModel.get("dream"));
            byte[] sbytes = sjson.getBytes("UTF-8");
            Files.write(Paths.get(summaryfile), sbytes);

            if (file.exists()) file.delete();
            String json = JsonUtil.object2Json(dataModel);
            byte[] bytes = json.getBytes("UTF-8");
            Files.write(Paths.get(fullname), bytes);
            logger.info(String.format("记本[%s(%s)]数据存储完成", dreamtitle, dreamid));
        } catch (Exception e) {
            logger.error(String.format("记本[%s(%s)]数据存储失败: [%s]", dreamtitle, dreamid, e.getMessage()));
        }
    }
}

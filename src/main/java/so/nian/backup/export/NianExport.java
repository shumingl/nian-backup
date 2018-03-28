package so.nian.backup.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import so.nian.backup.bizz.service.NianHtmlService;
import so.nian.backup.bizz.service.NianJsonService;
import so.nian.backup.startup.NianBackupStartup;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class NianExport {

    private static Logger logger = null;

    public static void main(String[] args) throws IOException {

        if (args == null || args.length == 0)
            throw new RuntimeException("ERROR-NianExport: Need paramete [1.taskId]");
        String taskId = args[0].trim();
        // 读取配置文件
        ClassPathResource resource = new ClassPathResource("config/export.json");
        File file = resource.getFile();
        byte[] bytes = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
        String json = new String(bytes, "UTF-8");
        Map<String, Object> export = JsonUtil.json2Map(json);

        Map<String, Object> crawlers = StringUtil.MAPGET(export, "crawlers");
        Map<String, Object> tasks = StringUtil.MAPGET(export, "tasks");
        Map<String, Object> task = StringUtil.MAPGET(tasks, taskId);

        if (task == null || task.size() == 0)
            throw new RuntimeException(String.format("ERROR-NianExport: TASK[%s] is null or empty.", task));

        // 读取导出配置
        String auth = StringUtil.MAPGET(task, "config/auth");
        String render = StringUtil.MAPGET(task, "config/render");
        String email = StringUtil.MAPGET(task, "config/user.email");
        String password = StringUtil.MAPGET(task, "config/user.password");
        String userid = StringUtil.MAPGET(task, "config/shell.user");
        String shell = StringUtil.MAPGET(task, "config/shell.shell");

        // 用户登录
        if ("user".equals(auth))
            NianJsonService.loginByAuth(email, password);
        else if ("shell".equals(auth))
            NianJsonService.loginByShell(userid, shell);
        else
            throw new RuntimeException(String.format("ERROR-NianExport: Auth Error[%s/%s].", taskId, auth));

        // 获取导出列表
        List<Map<String, Object>> exports = StringUtil.MAPGET(task, "export");
        if (exports == null || exports.size() == 0) {
            System.out.println(String.format("没有需要导出的数据[%s]", taskId));
            return;
        }

        try {
            // 启动应用程序
            NianBackupStartup.startup(crawlers);
            logger = LoggerFactory.getLogger(NianExport.class);
            logger.info("需要导出的用户数据：" + exports);

            for (Map<String, Object> exp : exports) {
                String type = String.valueOf(exp.get("type"));
                List<String> list = (List<String>) exp.get("list");
                if ("user".equals(type)) {
                    for (String userinfo : list) {
                        String[] infos = userinfo.split("#");
                        if ("html".equals(render))
                            NianHtmlService.downloadForUser(infos[0]);
                        if ("json".equals(render))
                            NianJsonService.downloadForUser(infos[0]);
                    }
                } else if ("dream".equals(type)) {
                    for (String dreaminfo : list) {
                        String[] dreams = dreaminfo.split("#");
                        String[] infos = dreams[0].split(":");
                        if ("html".equals(render))
                            NianHtmlService.downloadDream(infos[0], infos[1]);
                        if ("html".equals(render))
                            NianJsonService.downloadDream(infos[0], infos[1]);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("导出数据异常", e);
        } finally {
            NianBackupStartup.shutdown();
        }
    }

}
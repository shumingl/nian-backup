package so.nian.backup.export;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import so.nian.backup.config.AppConfig;
import so.nian.backup.config.logback.LogbackConfigure;
import so.nian.backup.utils.DataUtil;
import so.nian.backup.utils.FileUtil;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.util.*;

@SuppressWarnings({"unchecked"})
public class DreamRecovery {

    public static void main(String[] args) {
        try {
            startup();
            String cachebase = "C:\\data\\app\\app\\nian-backup\\nian-cache";
            String viewsbase = "C:\\data\\app\\app\\nian-backup\\nian-views";

            File viewsbasedir = new File(viewsbase);
            File cachebasedir = new File(cachebase);

            // 列出视图目录下的所有USERID
            File[] userdirs = cachebasedir.listFiles((dir, name) -> {
                try {
                    Integer userId = Integer.valueOf(name);
                    if (userId == null) return false;
                    return true;
                } catch (Exception e) {
                    return false;
                }
            });
            if (userdirs != null && userdirs.length > 0) {
                for (File userdir : userdirs) {

                    // ========================读取cache目录的dreams.json文件获取dreams
                    String userId = userdir.getName();
                    String dreamsFileFullPath = StringUtil.path(cachebase, userId, "dreams.json");
                    File dreamsFile = new File(dreamsFileFullPath);
                    System.out.printf("%-7s : [%5d] %s\n", userId, dreamsFile.length(), dreamsFile.getAbsolutePath());
                    String content = FileUtil.readAll(dreamsFile);
                    Map<String, Object> dreaminfo = JsonUtil.json2Map(content);
                    List<Map<String, Object>> jsonDreams = null;
                    if (dreaminfo != null) {
                        jsonDreams = (List<Map<String, Object>>) dreaminfo.get("dreams");
                        System.out.println(DataUtil.sort(jsonDreams, "id", 1));
                    } else
                        System.out.printf("文件[%s]内容为空\n", dreamsFile.getCanonicalPath());

                    // ========================根据本地cache生成dreams
                    String dreamFileBasePath = StringUtil.path(cachebase, userId, "dreams");
                    File dreamFileBaseDir = new File(dreamFileBasePath);
                    File[] dreamFiles = dreamFileBaseDir.listFiles((dir, name) -> {
                        if (name.endsWith(".json") && !name.endsWith("-info.json"))
                            return true;
                        return false;
                    });
                    List<Map<String, Object>> createDreams = new ArrayList<>();
                    if (dreamFiles != null && dreamFiles.length > 0) {
                        for (File dreamFile : dreamFiles) {
                            String dreamContent = FileUtil.readAll(dreamFile);
                            Map<String, Object> dreamData = JsonUtil.json2Map(dreamContent);

                            Map<String, Object> dream = new LinkedHashMap<>();
                            dream.put("id", StringUtil.MAPGET(dreamData, "dream/id"));
                            dream.put("title", StringUtil.MAPGET(dreamData, "dream/title"));
                            dream.put("image", StringUtil.MAPGET(dreamData, "dream/image"));
                            dream.put("percent", StringUtil.MAPGET(dreamData, "dream/percent"));
                            createDreams.add(dream);
                        }
                        System.out.println(DataUtil.sort(createDreams, "id", 1));
                    } else {
                        System.out.printf("目录[%s]没有数据文件", dreamFileBaseDir.getCanonicalPath());
                    }

                    // ========================合并本地dreams.json和生成的dreams数据
                    List<Map<String, Object>> unionMap = DataUtil.merge(jsonDreams, createDreams, "${id}");
                    System.out.println(DataUtil.sort(unionMap, "id", 1));
                    // ========================输出到文件
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("page", 1);
                    result.put("perPage", 20);
                    result.put("dreams", unionMap);
                    //FileUtil.writeJson(dreamsFile, result);

                    System.out.println("————————————————————————————————————————————————————————————");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private static void startup() {
        if (AppConfig.initialize()) {
            LogbackConfigure.configure("config/logback.xml");
            new ClassPathXmlApplicationContext("config/spring/spring-*.xml");
        }
    }

    private static void shutdown() {
        LogbackConfigure.stop();
    }

}

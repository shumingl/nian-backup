package so.nian.backup.http;

import org.junit.Test;
import so.nian.backup.utils.ExpressionParser;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;
import so.nian.backup.utils.logger.LogConsole;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class NianHttpUtilTest {
    LogConsole logger = LogConsole.getInstance();

    //@Test
    public void login() throws Exception {
        //HttpResultEntity result = NianHttpUtil.login("1373521108@qq.com", "leaves89@163");
        //HttpResultEntity result = NianHttpUtil.login("1192858440@qq.com", "102385753");
        //HttpResultEntity result = NianHttpUtil.login("vic_jinghang@hotmail.com", "10.2385753");
        HttpResultEntity result = NianHttpUtil.login("kizzo@vip.qq.com", "xuan52014563");
        if (result.isSuccess()) {
            Map<String, Object> loginfo = result.getResponseMap();
            logger.info(String.format("登录成功：%s", loginfo.get("data")));
            Map<String, Object> user = (Map<String, Object>) loginfo.get("data");
            HttpResultEntity detail = NianHttpUtil.info(String.valueOf(user.get("uid")));
            logger.info(String.format("个人资料：%s", detail.getResponseMap()));
        } else
            logger.info("登录失败：" + result.getMessage());
    }

    //@Test
    public void labDataFormatTest() throws Exception {
        System.out.println(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        System.out.println(sdf.format(new Date(Long.valueOf("1520745888") * 1000)));
    }

    @Test
    public void load19911Dreams() throws Exception {
        String listpath = "C:\\data\\app\\app\\nian-backup\\nian-cache\\19911\\dreams";
        File listdir = new File(listpath);
        File[] files = listdir.listFiles((dir, name) -> {
            if (name.endsWith(".json") && !name.endsWith("-info.json"))
                return true;
            return false;
        });
        ExpressionParser parser = ExpressionParser.getDefault();
        List<Map<String, Object>> dreams = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                //System.out.println(file.getCanonicalPath());
                byte[] bytes = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
                String json = new String(bytes, "UTF-8");
                Map<String, Object> jsonObj = JsonUtil.json2Map(json);
                System.out.println(parser.parse("${id},${title},${image},${percent}", jsonObj.get("dream")));
                Map<String, Object> dream = new LinkedHashMap<>();
                dream.put("id", StringUtil.MAPGET(jsonObj, "dream/id"));
                dream.put("title", StringUtil.MAPGET(jsonObj, "dream/title"));
                dream.put("image", StringUtil.MAPGET(jsonObj, "dream/image"));
                dream.put("percent", StringUtil.MAPGET(jsonObj, "dream/percent"));
                dreams.add(dream);
            }
        }
        System.out.println(JsonUtil.object2Json(dreams));
    }
}
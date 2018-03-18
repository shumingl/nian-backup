package so.nian.backup.http;

import org.junit.Test;
import so.nian.backup.utils.logger.LogConsole;

import java.util.Map;

import static org.junit.Assert.*;

public class NianHttpUtilTest {
    LogConsole logger = LogConsole.getInstance();

    @Test
    public void login() throws Exception {
        HttpResultEntity result = NianHttpUtil.login("1373521108@qq.com", "leaves89@163");
        if (result.isSuccess()) {
            Map<String, Object> loginfo = result.getResponseMap();
            logger.info(String.format("登录成功：%s", loginfo.get("data")));
            Map<String, Object> user = (Map<String, Object>) loginfo.get("data");
            HttpResultEntity detail = NianHttpUtil.info(String.valueOf(user.get("uid")));
            logger.info(String.format("个人资料：%s", detail.getResponseMap()));
        } else
            logger.info("登录失败：" + result.getMessage());
    }
}
package so.nian.backup.bizz.service;

import org.junit.Test;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.startup.NianBackupStartup;

import static org.junit.Assert.*;

public class NianServiceTest {
    @Test
    public void dealInfo() throws Exception {
    }

    @Test
    public void findDreamList() throws Exception {
        NianBackupStartup.startup();
        NianService service = new NianService();
        //service.findDreamList("142171");//罗生
        //service.findDreamList("103570");//浅纹
        //service.findDreamList("111987");//步摇
        service.findDreamList("278605");//沉疴
        NianBackupStartup.shutdown();
    }

    @Test
    public void dealDream() throws Exception {
        NianBackupStartup.startup();
        NianService service = new NianService();
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        //service.dealDream("9062");//多读书读好书
        //service.dealDream("49681");//不为人知的日常
        //service.dealDream("638060");//患者病例
        service.dealDream("4057882");
        NianBackupStartup.shutdown();
    }

}
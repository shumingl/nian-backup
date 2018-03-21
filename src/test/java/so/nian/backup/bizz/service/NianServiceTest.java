package so.nian.backup.bizz.service;

import org.junit.Test;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.startup.NianBackupStartup;

public class NianServiceTest {
    @Test
    public void dealInfo() throws Exception {
    }

    @Test
    public void findDreams() throws Exception {
        NianBackupStartup.startup();
        NianService service = new NianService();
        //service.findDreams("142171");//罗生
        //service.findDreams("103570");//浅纹
        //service.findDreams("111987");//步摇
        //service.findDreams("278605");//沉疴
        //service.findDreams("19911");//复旦姑娘
        //service.findDreams("111984");//朱迪朱迪
        //service.findDreams("15424");//五月五日羽
        //service.findDreams("136432");//留璋
        //service.findDreams("83084");//兔子及她
        //service.findDreams("642404");//无名萝莉
        //service.findDreams("788665");//你呀-
        //service.findDreams("9526");//PAN-
        //service.findDreams("33740");//迟到千年
        //service.findDreams("545196");//南迦南
        //service.findDreams("820090");//柳莫卿
        service.findDreams("312870");//陈阿苗
        //service.findDreams("82273");//球球糖本球
        //service.findDreams("31305");//董东栋
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
        //service.dealDream("4057882");
        service.dealDream("2898257");
        service.dealDream("2976037");
        service.dealDream("3141136");
        service.dealDream("3602627");
        service.dealDream("3750773");
        service.dealDream("3790489");
        service.dealDream("3801522");
        service.dealDream("3875526");

        NianImageDownload.closewait();
        NianBackupStartup.shutdown();
    }

}
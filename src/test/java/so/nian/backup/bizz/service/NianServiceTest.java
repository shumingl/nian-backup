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
        service.findDreams("142171");//罗生
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
        //service.findDreams("312870");//陈阿苗
        //service.findDreams("82273");//球球糖本球
        //service.findDreams("31305");//董东栋
        //service.findDreams("25268");//蹲叔-皮蛋他爹
        //service.findDreams("98509");//蹲叔-我是皮蛋
        //service.findDreams("383030");//一只兔子丫
        //service.findDreams("612569");//Monicaa
        //service.findDreams("627592");//魚一尾
        //service.findDreams("142260");//Erivan
        //service.findDreams("763740");//做吉他的汉斯
        //service.findDreams("64855");//olive是草莓味
        //service.findDreams("971590");//葉与爱粒丝
        //service.findDreams("174957");//清平
        //service.findDreams("71636");//Wio_
        //service.findDreams("646453");//耳朵喜欢你
        //service.findDreams("349021");//柴郡猫Cheshire
        //service.findDreams("271899");//害羞的章鱼豆豆豆豆_
        //service.findDreams("595745");//varvara
        //service.findDreams("228637");//小厘
        //service.findDreams("168642");//等清风来
        //service.findDreams("343036");//绿豆酥
        //service.findDreams("84974");//Tyche_
        //service.findDreams("350439");//柠檬味的十六
        //service.findDreams("146392");//昕不昕
        //service.findDreams("645531");//无畏
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
        //service.dealDream("2898257");
        //service.dealDream("2976037");
        //service.dealDream("3141136");
        //service.dealDream("3602627");
        //service.dealDream("3750773");
        //service.dealDream("3790489");
        //service.dealDream("3801522");
        //service.dealDream("3875526");
        //service.dealDream("266470");//碎碎念
        //service.dealDream("1993801");//告解室
        //service.dealDream("2815939");//截了个图
        //service.dealDream("3224251");//种花的园丁
        //service.dealDream("2740379");//Hi self

        //service.dealDream("218952");//劣子博物馆
        //service.dealDream("313533");//就算一个人早餐也要好好吃
        //service.dealDream("179886");//Physical Borderline
        //service.dealDream("1578986");//安利给你好用的冷门APP
        //service.dealDream("70279");//光景
        service.dealDream("2678095");//地獄之犬
        service.dealDream("4003655");//時至沙境

        NianImageDownload.closewait();
        NianBackupStartup.shutdown();
    }

}
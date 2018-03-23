package so.nian.backup.bizz.service;

import org.junit.Test;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.startup.NianBackupStartup;

import java.util.Map;

public class NianServiceTest {

    @Test
    public void downloadForUser() throws Exception {
        NianBackupStartup.startup();
        NianService service = new NianService();
        service.downloadForUser("142171");//罗生
        //service.downloadForUser("103570");//浅纹
        //service.downloadForUser("111987");//步摇
        //service.downloadForUser("278605");//沉疴
        //service.downloadForUser("19911");//复旦姑娘
        //service.downloadForUser("111984");//朱迪朱迪
        //service.downloadForUser("15424");//五月五日羽
        //service.downloadForUser("136432");//留璋
        //service.downloadForUser("83084");//兔子及她
        //service.downloadForUser("642404");//无名萝莉
        //service.downloadForUser("788665");//你呀-
        //service.downloadForUser("9526");//PAN-
        //service.downloadForUser("33740");//迟到千年
        //service.downloadForUser("545196");//南迦南
        //service.downloadForUser("820090");//柳莫卿
        //service.downloadForUser("312870");//陈阿苗
        //service.downloadForUser("82273");//球球糖本球
        //service.downloadForUser("31305");//董东栋
        //service.downloadForUser("25268");//蹲叔-皮蛋他爹
        //service.downloadForUser("98509");//蹲叔-我是皮蛋
        //service.downloadForUser("383030");//一只兔子丫
        //service.downloadForUser("612569");//Monicaa
        //service.downloadForUser("627592");//魚一尾
        //service.downloadForUser("142260");//Erivan
        //service.downloadForUser("763740");//做吉他的汉斯
        //service.downloadForUser("64855");//olive是草莓味
        //service.downloadForUser("971590");//葉与爱粒丝
        //service.downloadForUser("174957");//清平
        //service.downloadForUser("71636");//Wio_
        //service.downloadForUser("646453");//耳朵喜欢你
        //service.downloadForUser("349021");//柴郡猫Cheshire
        //service.downloadForUser("271899");//害羞的章鱼豆豆豆豆_
        //service.downloadForUser("595745");//varvara
        //service.downloadForUser("228637");//小厘
        //service.downloadForUser("168642");//等清风来
        //service.downloadForUser("343036");//绿豆酥
        //service.downloadForUser("84974");//Tyche_
        //service.downloadForUser("350439");//柠檬味的十六
        //service.downloadForUser("146392");//昕不昕
        //service.downloadForUser("645531");//无畏
        NianBackupStartup.shutdown();
    }

    @Test
    public void generateDreamHtml() throws Exception {
        NianBackupStartup.startup();
        NianService service = new NianService();
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        //service.generateDreamHtml("9062");//多读书读好书
        //service.generateDreamHtml("49681");//不为人知的日常
        //service.generateDreamHtml("638060");//患者病例
        //service.generateDreamHtml("4057882");
        //service.generateDreamHtml("2898257");
        //service.generateDreamHtml("2976037");
        //service.generateDreamHtml("3141136");
        //service.generateDreamHtml("3602627");
        //service.generateDreamHtml("3750773");
        //service.generateDreamHtml("3790489");
        //service.generateDreamHtml("3801522");
        //service.generateDreamHtml("3875526");
        //service.generateDreamHtml("266470");//碎碎念
        //service.generateDreamHtml("1993801");//告解室
        //service.generateDreamHtml("2815939");//截了个图
        //service.generateDreamHtml("3224251");//种花的园丁
        //service.generateDreamHtml("2740379");//Hi self
        //service.generateDreamHtml("218952");//劣子博物馆
        //service.generateDreamHtml("313533");//就算一个人早餐也要好好吃
        //service.generateDreamHtml("179886");//Physical Borderline
        //service.generateDreamHtml("1578986");//安利给你好用的冷门APP
        //service.generateDreamHtml("70279");//光景
        //service.generateDreamHtml("2678095");//地獄之犬
        //service.generateDreamHtml("4003655");//時至沙境

        NianImageDownload.closewait();
        NianBackupStartup.shutdown();
    }

}
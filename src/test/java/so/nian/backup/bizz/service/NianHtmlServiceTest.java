package so.nian.backup.bizz.service;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.http.HttpResultEntity;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.http.NianImageDownload;
import so.nian.backup.startup.NianBackupStartup;

import java.util.Map;

public class NianHtmlServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(NianHtmlServiceTest.class);

    @Test
    public void downloadByLoginUser() throws Exception {
        NianBackupStartup.startup();
        HttpResultEntity result = NianHttpUtil.login("1192858440@qq.com", "102385753");
        if (result.isSuccess()) {
            Map<String, Object> loginfo = result.getResponseMap();
            logger.info(String.format("登录成功：%s", loginfo.get("data")));
        } else {
            logger.info("登录失败：" + result.getMessage());
            return;
        }
        NianHtmlService.downloadForUsers("102220");
        NianBackupStartup.shutdown();
    }

    @Test
    public void downloadByElseUsers() throws Exception {
        NianBackupStartup.startup();
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        NianHtmlService.downloadForUsers("142171");
        /*
        NianHttpUtil.LOGINFO.put("uid", "102220");
        NianHttpUtil.LOGINFO.put("name", "行致");
        NianHttpUtil.LOGINFO.put("shell", "41477424d85ca604d15b6eb72d635206");
        NianHtmlService.downloadForUsers("102220");
        */
        NianBackupStartup.shutdown();
    }

    @Test
    public void downloadForUsers() throws Exception {
        NianBackupStartup.startup();
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        NianHtmlService.downloadForUsers("189148");//丧丧的小包子
        /*NianHtmlService.downloadForUsers(
                "103570",//浅纹
                "111987",//步摇
                "278605",//沉疴
                "136432",//留璋
                "25268",//蹲叔-皮蛋他爹
                "98509",//蹲叔-我是皮蛋
                "383030",//一只兔子丫
                "612569",//Monicaa
                "627592",//魚一尾
                "142260",//Erivan
                "763740",//做吉他的汉斯
                "64855",//olive是草莓味
                "971590",//葉与爱粒丝
                "174957",//清平
                "71636",//Wio_
                "646453",//耳朵喜欢你
                "349021",//柴郡猫Cheshire
                "271899",//害羞的章鱼豆豆豆豆_
                "595745",//varvara
                "228637",//小厘
                "168642",//等清风来
                "343036",//绿豆酥
                "84974",//Tyche_
                "350439",//柠檬味的十六
                "146392",//昕不昕
                "645531",//无畏
                /// *NianHtmlService.downloadForUsers(
                "19911",//复旦姑娘
                "111984",//朱迪朱迪
                "15424",//五月五日羽
                "83084",//兔子及她
                "642404",//无名萝莉
                "788665",//你呀-
                "9526",//PAN-
                "33740",//迟到千年
                "545196",//南迦南
                "820090",//柳莫卿
                "312870",//陈阿苗
                "82273",//球球糖本球
                "31305"//董东栋
        );
        / /*/
        NianBackupStartup.shutdown();
    }

    @Test
    public void downloadDream() throws Exception {
        NianBackupStartup.startup();
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        NianHtmlService.downloadDream(null, "9062");//多读书读好书
        NianHtmlService.downloadDream(null, "49681");//不为人知的日常
        //NianHtmlService.downloadDream(null, "638060");//患者病例
        //NianHtmlService.downloadDream(null, "218952");//劣子博物馆
        //NianHtmlService.downloadDream(null, "313533");//就算一个人早餐也要好好吃
        //NianHtmlService.downloadDream(null, "179886");//Physical Borderline
        //NianHtmlService.downloadDream(null, "1578986");//安利给你好用的冷门APP
        //NianHtmlService.downloadDream(null, "70279");//光景
        //NianHtmlService.downloadDream(null, "2678095");//地獄之犬
        //NianHtmlService.downloadDream(null, "4003655");//時至沙境
        NianJsonService.shutdownPool();
        NianHtmlService.shutdownPool();
        NianImageDownload.shutdownPool();
        NianBackupStartup.shutdown();
    }

}
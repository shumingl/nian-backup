package so.nian.backup.bizz.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.startup.NianBackupStartup;

import java.util.List;
import java.util.Map;

public class NianJsonServiceTest {

    //@Before
    public void before() throws Exception {
        NianBackupStartup.startup();
    }

    //@After
    public void after() throws Exception {
        NianBackupStartup.shutdown();
    }

    //@Test
    public void downloadForUsers() throws Exception {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        //NianJsonService.downloadForUsers("142171");

        NianJsonService.downloadForUsers("142171"//罗生_
                , "103570"//浅纹
                , "111987"//步摇
                , "278605"//沉疴
                , "136432"//留璋
                , "25268"//蹲叔-皮蛋他爹
                , "98509"//蹲叔-我是皮蛋
                , "383030"//一只兔子丫
                , "612569"//Monicaa
                , "627592"//魚一尾
                , "142260"//Erivan
                , "763740"//做吉他的汉斯
                , "64855"//olive是草莓味
                , "971590"//葉与爱粒丝
                , "174957"//清平
                , "71636"//Wio_
                , "646453"//耳朵喜欢你
                , "349021"//柴郡猫Cheshire
                , "271899"//害羞的章鱼豆豆豆豆_
                , "595745"//varvara
                , "228637"//小厘
                , "168642"//等清风来
                , "343036"//绿豆酥
                , "84974"//Tyche_
                , "350439"//柠檬味的十六
                , "146392"//昕不昕
                , "645531"//无畏
                , "102220"//行致
                , "189148"//丧丧的小包子

                , "9911"//复旦姑娘
                , "111984"//朱迪朱迪
                , "15424"//五月五日羽
                , "83084"//兔子及她
                , "642404"//无名萝莉
                , "788665"//你呀-
                , "9526"//PAN-
                , "33740"//迟到千年
                , "545196"//南迦南
                , "820090"//柳莫卿
                , "312870"//陈阿苗
                , "82273"//球球糖本球
                , "31305"//董东栋
                , "180737"//中村里砂
                , "604802"//花酱酱
                , "216514"//芝麻酱
                , "108308"//八里长街
        );
    }

    //@Test
    public void downloadDream() throws Exception {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        NianJsonService.downloadDream(null, "9062");//多读书读好书
        NianJsonService.downloadDream(null, "49681");//不为人知的日常
        NianJsonService.downloadDream(null, "638060");//患者病例
        NianJsonService.downloadDream(null, "218952");//劣子博物馆
        NianJsonService.downloadDream(null, "313533");//就算一个人早餐也要好好吃
        NianJsonService.downloadDream(null, "179886");//Physical Borderline
        NianJsonService.downloadDream(null, "1578986");//安利给你好用的冷门APP
        NianJsonService.downloadDream(null, "70279");//光景
        NianJsonService.downloadDream(null, "2678095");//地獄之犬
        NianJsonService.downloadDream(null, "4003655");//時至沙境
        NianJsonService.downloadDream(null, "1032468");//我家洗砚池边树
        NianJsonService.downloadDream(null, "1402688");//每日装备
    }

    @Test
    public void downloadByLogin() throws Exception {
    }

    @Test
    public void downloadForUser() throws Exception {
    }

    @Test
    public void downloadStepAllLike() throws Exception {
    }

    @Test
    public void downloadStepALlComments() throws Exception {
    }

    //@Test
    public void downloadUserCareOrFans() throws Exception {
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        List<Map<String, Object>> care = NianJsonService.downloadUserCareOrFans("142171", "care");
        if (care != null) {
            for (Map<String, Object> user : care) {
                if (user != null) System.out.println(user);
            }
        }
        System.out.println("========================================");
        List<Map<String, Object>> fans = NianJsonService.downloadUserCareOrFans("142171", "fans");
        if (fans != null) {
            for (Map<String, Object> user : fans) {
                if (user != null) System.out.println(user);
            }
        }
        System.out.println("========================================");
    }

    @Test
    public void downloadDreamLikeOrFans() throws Exception {
    }

}
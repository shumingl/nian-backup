package so.nian.backup.export;

import so.nian.backup.bizz.service.NianHtmlService;
import so.nian.backup.bizz.service.NianJsonService;
import so.nian.backup.startup.NianBackupStartup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NianExport {

    private static Map<String, String> shells;

    public static void main(String[] args) {

        NianBackupStartup.startup();
        shells = new HashMap<>();
        shells.put("102220", "41477424d85ca604d15b6eb72d635206");//行致
        shells.put("8278", "7c2474bf1f7feaf5cf997b89ddf8006b");//淡淡淡淡
        shells.put("142171", "077682926c004802b79883b94428a827");//罗生

        /*if (!NianJsonService.loginByAuth("vic_jinghang@hotmail.com", "10.2385753")) {
            throw new RuntimeException("用户登录失败");
        }*/

        String render = "html";
        try {
            String authuser = "142171";
            String authshell = shells.get(authuser);
            NianJsonService.loginByShell(authuser, authshell);

            if ("json".equals(render)) {
                //exportJsonForUsers();
                //exportJsonDreams();

            } else if ("html".equals(render)) {
                //NianHtmlService.downloadForUsers("142171");
                exportHtmlForUsers();
                exportHtmlDreams();

                /*exportHtmlPriDreams("8278"
                        , "14859"//三
                        , "24675"//画点什么
                        , "250070"//学
                        , "309089"//空白
                );*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            NianBackupStartup.shutdown();
        }
    }

    public static void exportJsonForUsers() throws IOException {

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

                , "19911"//复旦姑娘
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

    public static void exportJsonDreams() {
        NianJsonService.downloadDream("7986", "9062");//多读书读好书
        NianJsonService.downloadDream("7986", "49681");//不为人知的日常
        NianJsonService.downloadDream("278605", "638060");//患者病例
        NianJsonService.downloadDream("111432", "218952");//劣子博物馆
        NianJsonService.downloadDream("16090", "313533");//就算一个人早餐也要好好吃
        NianJsonService.downloadDream("70534", "179886");//Physical Borderline
        NianJsonService.downloadDream("362455", "1578986");//安利给你好用的冷门APP
        NianJsonService.downloadDream("28944", "70279");//光景
        NianJsonService.downloadDream("847210", "2678095");//地獄之犬
        NianJsonService.downloadDream("847210", "4003655");//時至沙境
        NianJsonService.downloadDream("575208", "1032468");//我家洗砚池边树
        NianJsonService.downloadDream("447634", "1402688");//每日装备
    }

    public static void exportHtmlForUsers() throws IOException {

        NianHtmlService.downloadForUsers("142171"//罗生_
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
                , "150994"//阿城_

                , "19911"//复旦姑娘
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

    public static void exportHtmlDreams() {
        NianHtmlService.downloadDream("7986", "9062");//多读书读好书
        NianHtmlService.downloadDream("7986", "49681");//不为人知的日常
        NianHtmlService.downloadDream("278605", "638060");//患者病例
        NianHtmlService.downloadDream("111432", "218952");//劣子博物馆
        NianHtmlService.downloadDream("16090", "313533");//就算一个人早餐也要好好吃
        NianHtmlService.downloadDream("70534", "179886");//Physical Borderline
        NianHtmlService.downloadDream("362455", "1578986");//安利给你好用的冷门APP
        NianHtmlService.downloadDream("28944", "70279");//光景
        NianHtmlService.downloadDream("847210", "2678095");//地獄之犬
        NianHtmlService.downloadDream("847210", "4003655");//時至沙境
        NianHtmlService.downloadDream("575208", "1032468");//我家洗砚池边树
        NianHtmlService.downloadDream("447634", "1402688");//每日装备
    }

    public static void exportJsonForPriUsers() {
    }

    public static void exportJsonPriDreams(String userid, String... dreamids) {
        NianJsonService.loginByShell(userid, shells.get(userid));
        for (String dreamid : dreamids) {
            NianJsonService.downloadDream(userid, dreamid);
        }
    }

    public static void exportHtmlForPriUsers() {
    }

    public static void exportHtmlPriDreams(String userid, String... dreamids) {
        NianJsonService.loginByShell(userid, shells.get(userid));
        for (String dreamid : dreamids) {
            NianHtmlService.downloadDream(userid, dreamid);
        }
    }
}
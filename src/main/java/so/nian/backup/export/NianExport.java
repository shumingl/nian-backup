package so.nian.backup.export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import so.nian.backup.bizz.service.NianHtmlService;
import so.nian.backup.bizz.service.NianJsonService;
import so.nian.backup.startup.NianBackupStartup;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class NianExport {

    private static Logger logger = null;

    private static Map<String, String> shells;

    public static void main(String[] args) throws IOException {

        if (args == null || args.length == 0)
            throw new RuntimeException("ERROR-NianExport: Need paramete [1.taskId]");
        String taskId = args[0].trim();
        // 读取配置文件
        ClassPathResource resource = new ClassPathResource("config/export.json");
        File file = resource.getFile();
        byte[] bytes = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
        String json = new String(bytes, "UTF-8");
        Map<String, Object> export = JsonUtil.json2Map(json);

        Map<String, Object> crawlers = StringUtil.MAPGET(export, "crawlers");
        Map<String, Object> tasks = StringUtil.MAPGET(export, "tasks");
        Map<String, Object> task = StringUtil.MAPGET(tasks, taskId);

        if (task == null || task.size() == 0)
            throw new RuntimeException(String.format("ERROR-NianExport: TASK[%s] is null or empty.", task));

        // 读取导出配置
        String auth = StringUtil.MAPGET(task, "config/auth");
        String render = StringUtil.MAPGET(task, "config/render");
        String email = StringUtil.MAPGET(task, "config/user.email");
        String password = StringUtil.MAPGET(task, "config/user.password");
        String userid = StringUtil.MAPGET(task, "config/shell.user");
        String shell = StringUtil.MAPGET(task, "config/shell.shell");

        // 用户登录
        if ("user".equals(auth))
            NianJsonService.loginByAuth(email, password);
        else if ("shell".equals(auth))
            NianJsonService.loginByShell(userid, shell);
        else
            throw new RuntimeException(String.format("ERROR-NianExport: Auth Error[%s/%s].", taskId, auth));

        // 获取导出列表
        List<Map<String, Object>> exports = StringUtil.MAPGET(task, "export");
        if (exports == null || exports.size() == 0) {
            System.out.println(String.format("没有需要导出的数据[%s]", taskId));
            return;
        }

        try {
            // 启动应用程序
            NianBackupStartup.startup(crawlers);
            logger = LoggerFactory.getLogger(NianExport.class);
            logger.info("需要导出的用户数据：" + exports);

            for (Map<String, Object> exp : exports) {
                String type = String.valueOf(exp.get("type"));
                List<String> list = (List<String>) exp.get("list");
                if ("user".equals(type)) {
                    for (String userinfo : list) {
                        String[] infos = userinfo.split("#");
                        if ("html".equals(render))
                            NianHtmlService.downloadForUser(infos[0]);
                        if ("json".equals(render))
                            NianJsonService.downloadForUser(infos[0]);
                    }
                } else if ("dream".equals(type)) {
                    for (String dreaminfo : list) {
                        String[] dreams = dreaminfo.split("#");
                        String[] infos = dreams[0].split(":");
                        if ("html".equals(render))
                            NianHtmlService.downloadDream(infos[0], infos[1]);
                        if ("html".equals(render))
                            NianJsonService.downloadDream(infos[0], infos[1]);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("导出数据异常", e);
        } finally {
            NianBackupStartup.shutdown();
        }
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
                , "877866"//多雨天
                , "821232"//多努力

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

}
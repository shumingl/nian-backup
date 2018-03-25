package so.nian.backup.bizz.service;

import org.junit.Test;
import so.nian.backup.http.NianHttpUtil;
import so.nian.backup.startup.NianBackupStartup;

import static org.junit.Assert.*;

public class NianJsonServiceTest {
    @Test
    public void downloadForUsers() throws Exception {
        NianBackupStartup.startup();
        NianHttpUtil.LOGINFO.put("uid", "142171");
        NianHttpUtil.LOGINFO.put("name", "罗生_");
        NianHttpUtil.LOGINFO.put("shell", "077682926c004802b79883b94428a827");
        //NianJsonService.downloadForUsers("142171");

        NianJsonService.downloadForUsers(
                "142171",//罗生_
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
                "102220",//行致

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

        NianJsonService.shutdownPool();
        NianBackupStartup.shutdown();
    }

    @Test
    public void downloadDream() throws Exception {
    }

}
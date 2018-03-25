package so.nian.backup.bizz.service;

public class NianDreamHttpWorker extends Thread {

    private String userid;
    private String dreamid;
    private String viewtype;

    public NianDreamHttpWorker(String viewtype, String userid, String dreamid) {
        this.userid = userid;
        this.dreamid = dreamid;
        this.viewtype = viewtype;
    }

    @Override
    public void run() {
        if("html".equals(viewtype))
            NianHtmlService.generateDreamHtml(userid, dreamid);
        if("json".equals(viewtype))
            NianJsonService.generateDreamJson(userid, dreamid);
    }
}

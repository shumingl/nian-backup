package so.nian.backup.http;

import org.junit.Test;
import so.nian.backup.startup.NianBackupStartup;

import java.util.concurrent.ExecutorService;

public class NianDownloadImageTest {
    @Test
    public void downloadImage() throws Exception {
        NianBackupStartup.startup();
        NianImageDownload.downloadImage("cover", "142171_1484715118.jpg");
        NianImageDownload.downloadImage("step", "9526_15213553510.png");
        NianImageDownload.downloadImage("head", "142171.jpg");
        NianImageDownload.downloadImage("dream", "142171_1478446282.png");
        NianBackupStartup.shutdown();
    }

    @Test
    public void downloadThumbs() throws Exception {
        NianBackupStartup.startup();
        NianImageDownload.downloadThumbs("cover", "142171_1484715118.jpg");
        NianImageDownload.downloadThumbs("step", "9526_15213553510.png");
        NianImageDownload.downloadThumbs("head", "142171.jpg");
        NianImageDownload.downloadThumbs("dream", "142171_1478446282.png");
        NianBackupStartup.shutdown();
    }

    @Test
    public void download() throws Exception {
        NianBackupStartup.startup();
        //NianImageDownload.download("cover", "142171_1484715118.jpg");
        NianImageDownload.downloadThumbs("step", "31305_14876532240.png");
        NianImageDownload.downloadThumbs("step", "31305_14876533413.png");
        NianImageDownload.downloadThumbs("step", "31305_14875690360.png");
        NianImageDownload.downloadThumbs("step", "31305_14844618070.png");
        NianImageDownload.downloadThumbs("step", "31305_14863644773.png");
        NianImageDownload.downloadThumbs("step", "31305_14857474924.png");
        NianImageDownload.downloadThumbs("step", "31305_14876533794.png");
        NianImageDownload.downloadThumbs("step", "31305_14802208790.png");
        NianImageDownload.downloadThumbs("step", "31305_14784387420.png");
        NianImageDownload.downloadThumbs("step", "31305_14758939460.png");
        NianImageDownload.downloadThumbs("step", "31305_14619245172.png");
        NianImageDownload.downloadThumbs("step", "31305_15199244034.png");
        NianImageDownload.downloadThumbs("step", "31305_15170268590.png");
        NianImageDownload.downloadThumbs("step", "31305_15199244045.png");
        NianImageDownload.downloadThumbs("step", "31305_15139348783.png");
        NianImageDownload.downloadThumbs("step", "31305_15133928650.png");
        NianImageDownload.downloadThumbs("step", "31305_15139348794.png");
        NianImageDownload.downloadThumbs("step", "31305_15100145512.png");
        NianImageDownload.downloadThumbs("step", "31305_15050941611.png");
        NianImageDownload.downloadImage("step", "31305_14876532240.png");
        NianImageDownload.downloadImage("step", "31305_14876533413.png");
        NianImageDownload.downloadImage("step", "31305_14875690360.png");
        NianImageDownload.downloadImage("step", "31305_14844618070.png");
        NianImageDownload.downloadImage("step", "31305_14863644773.png");
        NianImageDownload.downloadImage("step", "31305_14857474924.png");
        NianImageDownload.downloadImage("step", "31305_14876533794.png");
        NianImageDownload.downloadImage("step", "31305_14802208790.png");
        NianImageDownload.downloadImage("step", "31305_14784387420.png");
        NianImageDownload.downloadImage("step", "31305_14758939460.png");
        NianImageDownload.downloadImage("step", "31305_14619245172.png");
        NianImageDownload.downloadImage("step", "31305_15199244034.png");
        NianImageDownload.downloadImage("step", "31305_15170268590.png");
        NianImageDownload.downloadImage("step", "31305_15199244045.png");
        NianImageDownload.downloadImage("step", "31305_15139348783.png");
        NianImageDownload.downloadImage("step", "31305_15133928650.png");
        NianImageDownload.downloadImage("step", "31305_15139348794.png");
        NianImageDownload.downloadImage("step", "31305_15100145512.png");
        NianImageDownload.downloadImage("step", "31305_15050941611.png");
        //NianImageDownload.download("step", "278605_15076488150.png");
        //NianImageDownload.download("dream", "142171_1478446282.png");
        NianBackupStartup.shutdown();
    }

}
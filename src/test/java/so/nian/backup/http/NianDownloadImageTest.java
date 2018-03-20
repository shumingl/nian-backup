package so.nian.backup.http;

import org.junit.Test;
import so.nian.backup.startup.NianBackupStartup;

public class NianDownloadImageTest {
    @Test
    public void download() throws Exception {
        NianBackupStartup.startup();
        NianImageDownload.downloadImage("cover","142171_1484715118.jpg");
        NianImageDownload.downloadImage("step","9526_15213553510.png");
        NianImageDownload.downloadImage("head","142171.jpg");
        NianImageDownload.downloadImage("dream","142171_1478446282.png");
        NianBackupStartup.shutdown();
    }

    @Test
    public void downloadThumbs() throws Exception {
        NianBackupStartup.startup();
        NianImageDownload.downloadThumbs("cover","142171_1484715118.jpg");
        NianImageDownload.downloadThumbs("step","9526_15213553510.png");
        NianImageDownload.downloadThumbs("head","142171.jpg");
        NianImageDownload.downloadThumbs("dream","142171_1478446282.png");
        NianBackupStartup.shutdown();
    }

}
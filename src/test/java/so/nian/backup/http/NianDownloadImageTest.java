package so.nian.backup.http;

import org.junit.Test;
import so.nian.backup.startup.NianBackupStartup;

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
        //NianImageDownload.download("step", "278605_15076488150.png");
        //NianImageDownload.download("dream", "142171_1478446282.png");
        //NianImageDownload.download("head", "142171.jpg");
        String imageString = "82273_15172060570.png,82273_15067897750.png,82273_15065324930.png,82273_15072019731.png,82273_15095536130.png,82273_15079460111.png,82273_15083330192.png,82273_15075125440.png,82273_15067311020.png,82273_15064925210.png,82273_15064925242.png,82273_15064925231.png,82273_15033309431.png,82273_15065324961.png,82273_14958515472.png,82273_15065324972.png,82273_14914473940.png,82273_14834409440.png,82273_14669522844.png,82273_14635869050.png,82273_14619177941.png,82273_14591726161.png,82273_15044097420.png,82273_14984011340.png,82273_14860216530.png,82273_14881035370.png,82273_14870936730.png,82273_14849283604.png,82273_14756494821.png,82273_14729181241.png,82273_14756494823.png,82273_14703361802.png,82273_14669308491.png,82273_14596080130.png,82273_14583726810.png,82273_15100244640.png,82273_15059829860.png";
        String[] images = imageString.split(",");
        for (String image : images) {
            NianImageDownload.download("step", image);
        }
        NianImageDownload.closewait();
        NianBackupStartup.shutdown();
    }

}
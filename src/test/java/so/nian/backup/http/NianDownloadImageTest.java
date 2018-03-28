package so.nian.backup.http;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import so.nian.backup.startup.NianBackupStartup;
import so.nian.backup.utils.StringUtil;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class NianDownloadImageTest {
    //@Before
    public void before() throws Exception {
        ClassPathResource resource = new ClassPathResource("config/export.json");
        File file = resource.getFile();
        byte[] bytes = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
        String json = new String(bytes, "UTF-8");
        Map<String, Object> export = JsonUtil.json2Map(json);

        Map<String, Object> crawlers = StringUtil.MAPGET(export, "crawlers");
        NianBackupStartup.startup(crawlers);
    }

    //@After
    public void after() throws Exception {
        NianBackupStartup.shutdown();
    }

    //@Test
    public void downloadImage() throws Exception {
        NianImageDownload.downloadImage("imgtest", "cover", "142171_1484715118.jpg");
        NianImageDownload.downloadImage("imgtest", "step", "9526_15213553510.png");
        NianImageDownload.downloadImage("imgtest", "head", "142171.jpg");
        NianImageDownload.downloadImage("imgtest", "dream", "142171_1478446282.png");
        NianBackupStartup.shutdown();
    }

    //@Test
    public void downloadThumbs() throws Exception {
        NianImageDownload.downloadThumbs("imgtest", "cover", "142171_1484715118.jpg");
        NianImageDownload.downloadThumbs("imgtest", "step", "9526_15213553510.png");
        NianImageDownload.downloadThumbs("imgtest", "head", "142171.jpg");
        NianImageDownload.downloadThumbs("imgtest", "dream", "142171_1478446282.png");
        NianImageDownload.shutdownPool();
    }

    //@Test
    public void download() throws Exception {
        NianImageDownload.startup(4);
        //NianImageDownload.download("cover", "142171_1484715118.jpg");
        //NianImageDownload.download("step", "278605_15076488150.png");
        //NianImageDownload.download("dream", "142171_1478446282.png");
        //NianImageDownload.download("head", "142171.jpg");
        String imageString = "103570_14675493652.png";
        String[] images = imageString.split("\\|");
        for (String image : images) {
            NianImageDownload.download("download", "step", image, true);
        }
        NianImageDownload.shutdownPool();
    }

}
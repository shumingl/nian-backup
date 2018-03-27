package so.nian.backup.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {


    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static void save2image(InputStream istream, File file) throws IOException {
        String imageBase = AppConfig.getNianImageBase();
        File imageDumpFile = new File(imageBase, file.getName());
        createParentDirs(file);
        createParentDirs(imageDumpFile);
        try {
            FileOutputStream fostream = new FileOutputStream(imageDumpFile);
            int ret;
            byte[] buffer = new byte[819200];
            while ((ret = istream.read(buffer)) != -1) {
                if (ret > 0)
                    fostream.write(buffer, 0, ret);
            }
            fostream.flush();
            fostream.close();
            // 先写dump文件，再移动
            Files.move(Paths.get(imageDumpFile.getCanonicalPath()), Paths.get(file.getCanonicalPath()));
        } catch (Exception e) {
            logger.error("[save2image]写入文件[{}]失败：{}", file.getCanonicalPath(), e.getMessage());
        }
    }

    public static void createParentDirs(File file) {
        File path = new File(file.getParent());
        if (!path.exists())
            path.mkdirs();
    }
}

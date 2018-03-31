package so.nian.backup.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtil {


    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static void save2image(InputStream istream, File file, String type) throws IOException {
        String imageBase = AppConfig.getNianImageBase();
        File imageDumpFile = new File(StringUtil.path(imageBase, type, file.getName()));
        createParentDirs(file);
        createParentDirs(imageDumpFile);
        FileOutputStream fostream = null;
        try {
            long finished = 0;
            fostream = new FileOutputStream(imageDumpFile);
            int ret;
            byte[] buffer = new byte[102400];
            while ((ret = istream.read(buffer)) != -1) {
                if (ret > 0) {
                    fostream.write(buffer, 0, ret);
                    finished += ret;
                }
            }
            fostream.flush();
            fostream.close();
            // 先写dump文件，再移动
            if (finished > 0) {
                CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
                Files.move(Paths.get(imageDumpFile.getCanonicalPath()), Paths.get(file.getCanonicalPath()), options);
            }
        } catch (Exception e) {
            logger.error("[save2image]写入文件[{}]失败：{}", file.getCanonicalPath(), e.getMessage());
        } finally {
            if (fostream != null) {
                fostream.close();
            }
        }

    }

    public static void createParentDirs(File file) {
        File path = new File(file.getParent());
        if (!path.exists())
            path.mkdirs();
    }
}

package so.nian.backup.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import so.nian.backup.config.AppConfig;
import so.nian.backup.utils.jackson.JsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

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

    public static String readAll(File file) {
        return readAll(file, "UTF-8");
    }

    public static String readAll(File file, String encode) {
        if (file == null || !file.exists())
            return null;
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(file.getCanonicalPath()));
            return new String(bytes, encode);
        } catch (Exception e) {
            logger.error("读取文件错误[{}]: {}", file.getAbsolutePath(), e.getMessage());
        }
        return null;
    }

    public static void writeJson(File file, Object object) {
        writeJson(file, object, "UTF-8");
    }

    public static void writeJson(File file, Object object, String encode) {
        try {
            if (file == null)
                throw new IOException("文件对象不能为空");
            createParentDirs(file);// 父级目录不存在则创建
            OpenOption[] options = new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.WRITE};
            String json = JsonUtil.object2Json(object);
            Files.write(Paths.get(file.getCanonicalPath()), (json == null ? "" : json).getBytes(encode), options);
        } catch (Exception e) {
            logger.error("写入文件错误: {}", e.getMessage());
        }
    }

    public static void createParentDirs(File file) {
        File path = new File(file.getParent());
        if (!path.exists())
            path.mkdirs();
    }
}

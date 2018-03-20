package so.nian.backup.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {

    public static void write2file(InputStream istream, File file) throws IOException {
        createParentDirs(file);
        FileOutputStream fostream = new FileOutputStream(file);
        int ret;
        byte[] buffer = new byte[8192];
        while ((ret = istream.read(buffer)) != -1) {
            if (ret > 0)
                fostream.write(buffer, 0, ret);
        }
        fostream.flush();
        fostream.close();
    }

    public static void createParentDirs(File file) {
        File path = new File(file.getParent());
        if (!path.exists())
            path.mkdirs();
    }
}

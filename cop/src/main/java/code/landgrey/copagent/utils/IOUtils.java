package code.landgrey.copagent.utils;

import java.io.*;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/common/src/main/java/com/taobao/arthas/common/IOUtils.java
 *
 */

public class IOUtils {

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
    }

    public static IOException close(InputStream input) {
        return close((Closeable) input);
    }

    public static IOException close(final Reader input) {
        return close((Closeable) input);
    }

    public static IOException close(final Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (final IOException ioe) {
            return ioe;
        }
        return null;
    }

}

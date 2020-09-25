package code.landgrey.copagent.utils;

import java.lang.management.ManagementFactory;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/common/src/main/java/com/taobao/arthas/common/PidUtils.java
 *
 */

public class PidUtils {
    private static String PID = "-1";
    private static long pid = -1;

    static {
        // https://stackoverflow.com/a/7690178
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        int index = jvmName.indexOf('@');

        if (index > 0) {
            try {
                PID = Long.toString(Long.parseLong(jvmName.substring(0, index)));
                pid = Long.parseLong(PID);
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    private PidUtils() {
    }

    public static String currentPid() {
        return PID;
    }
}

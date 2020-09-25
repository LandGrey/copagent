package code.landgrey.copagent.utils;

import java.util.Properties;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/common/src/main/java/com/taobao/arthas/common/JavaVersionUtils.java
 *
 */

public class JavaVersionUtils {
    private static final String VERSION_PROP_NAME = "java.specification.version";
    private static final String JAVA_VERSION_STR = System.getProperty(VERSION_PROP_NAME);
    private static final float JAVA_VERSION = Float.parseFloat(JAVA_VERSION_STR);

    private JavaVersionUtils() {
    }

    public static String javaVersionStr() {
        return JAVA_VERSION_STR;
    }

    public static String javaVersionStr(Properties props) {
        return (null != props) ? props.getProperty(VERSION_PROP_NAME): null;
    }

    public static boolean isLessThanJava9() {
        return JAVA_VERSION < 9.0f;
    }


    public static boolean isGreaterThanJava8() {
        return JAVA_VERSION > 1.8f;
    }
}
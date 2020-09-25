package code.landgrey.copagent.utils;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Set;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/core/src/main/java/com/taobao/arthas/core/util/InstrumentationUtils.java
 *
 */

public class InstrumentationUtils {
    public static void retransformClasses(Instrumentation inst, ClassFileTransformer transformer,
                                          Set<Class<?>> classes) {
        try {
            inst.addTransformer(transformer, true);

            for (Class<?> clazz : classes) {
                try {
                    inst.retransformClasses(clazz);
                } catch (Throwable e) {
                    LogUtils.logit("retransformClasses class error, name: " + clazz.getName());
                }
            }
        } finally {
            inst.removeTransformer(transformer);
        }
    }
}

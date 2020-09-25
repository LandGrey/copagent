package code.landgrey.copagent.common;

import code.landgrey.copagent.utils.LogUtils;
import code.landgrey.copagent.utils.PathUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/core/src/main/java/com/taobao/arthas/core/command/klass100/ClassDumpTransformer.java
 *
 */

public class ClassDumpTransformer implements ClassFileTransformer {
    private Set<Class<?>> classesToEnhance;
    private Map<Class<?>, File> dumpResult;
    private File directory;

    public ClassDumpTransformer(Set<Class<?>> classesToEnhance) {
        this(classesToEnhance, null);
    }

    public ClassDumpTransformer(Set<Class<?>> classesToEnhance, File directory) {
        this.classesToEnhance = classesToEnhance;
        this.dumpResult = new HashMap<Class<?>, File>();
        this.directory = directory;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        if (classesToEnhance.contains(classBeingRedefined)) {
            dumpClassIfNecessary(classBeingRedefined, classfileBuffer);
        }
        return null;
    }

    public Map<Class<?>, File> getDumpResult() {
        return dumpResult;
    }

    private void dumpClassIfNecessary(Class<?> clazz, byte[] data) {
        File dumpClassFile = PathUtils.getStorePath(clazz, true);

        // 将类字节码写入文件
        PathUtils.writeByteArrayToFile(dumpClassFile, data);
        dumpResult.put(clazz, dumpClassFile);
    }

    public void dumpJavaIfNecessary(Class<?> clazz, Map<Class<?>, File> classFiles) {
        File dumpJavaFile = PathUtils.getStorePath(clazz, false);

        File classFile = classFiles.get(clazz);
        String source = Decompiler.decompile(classFile.getAbsolutePath(), null, false);
        Pattern pattern = Pattern.compile("(?m)^/\\*\\s*\\*/\\s*$" + System.getProperty("line.separator"));
        if (source != null) {
            source = pattern.matcher(source).replaceAll("");
        } else {
            source = "unknown";
        }

        PathUtils.writeByteArrayToFile(dumpJavaFile, source.getBytes());
        LogUtils.logit("Store java: " + clazz.getName() + " to " + dumpJavaFile.getAbsolutePath());

    }
}

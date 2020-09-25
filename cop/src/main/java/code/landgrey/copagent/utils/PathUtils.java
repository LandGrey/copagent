package code.landgrey.copagent.utils;

import java.io.*;

/**
 *
 * referer https://github.com/alibaba/arthas/blob/master/common/src/main/java/com/taobao/arthas/common/FileUtils.java
 *
 **/

public class PathUtils {
    public static String getCurrentJarPath() throws Exception {
        return new File(PathUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
    }

    public static String getCurrentDirectory(){
        try{
            return new File(getCurrentJarPath()).getParent();
        }catch (Exception e){
            return new File(".").getAbsolutePath();
        }
    }

    public static void copyResources(String src, File dst) throws IOException{
        InputStream is = PathUtils.class.getResourceAsStream(src);


        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        try {
            while ((ch = is.read()) != -1) {
                bytestream.write(ch);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        byte[] program = bytestream.toByteArray();

        java.io.FileOutputStream fo = new java.io.FileOutputStream(dst);
        fo.write(program);
        fo.close();

        try {
            bytestream.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        is.close();
    }

    public static boolean createDirectory(String absolute_path){
        File dir = new File(absolute_path);
        return createDirectory(dir);
    }

    public static boolean createDirectory(File dir){
        try{
            dir.mkdirs();
        }catch (Throwable t){
            return false;
        }
        return true;
    }

    public static File getTempDirectory() {
        return new File(System.getProperty("java.io.tmpdir"));
    }


}

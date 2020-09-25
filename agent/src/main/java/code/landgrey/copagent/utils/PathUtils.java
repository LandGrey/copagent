package code.landgrey.copagent.utils;

import java.io.*;

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

    public static File getStorePath(Class clazz, boolean isClass){
        String className = clazz.getName();
        ClassLoader classLoader = clazz.getClassLoader();
        String fileName;
        String dumpType = "java";
        if(isClass){
            dumpType = "class";
        }
        File dumpDir = new File(PathUtils.getCurrentDirectory(), dumpType);
        if (classLoader != null){
            fileName = classLoader.getClass().getName() + "-" + Integer.toHexString(classLoader.hashCode()) + File.separator + className.replace(".", File.separator) + "." + dumpType;
        } else {
            fileName = className.replace(".", File.separator) + "." + dumpType;
        }
        return new File(dumpDir, fileName);
    }

    public static String getFileContent(File file){
        try{
            java.util.Scanner c = new java.util.Scanner(file).useDelimiter("\\A");
            String content = c.next();
            if(content != null){
                return content;
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return "";
    }

    public static void addTextToFile(File f, String content, boolean append){
        try{
            if(!(new File(f.getParent()).exists())){
                new File(f.getParent()).mkdirs();
            }
            if(!f.exists()){
                f.createNewFile();
            }
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f.getAbsolutePath(), append)));
            out.println(content);
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void appendTextToFile(File f, String content){
        addTextToFile(f, content, true);
    }

    public static void writeTextToFile(File f, String content){
        addTextToFile(f, content, false);
    }

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     * <p>
     * NOTE: As from v1.3, the parent directories of the file will be created
     * if they do not exist.
     *
     * @param file  the file to write to
     * @param data  the content to write to the file
     * @since 1.1
     */
    public static void writeByteArrayToFile(File file, byte[] data){
        try{
            writeByteArrayToFile(file, data, false);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Writes a byte array to a file creating the file if it does not exist.
     *
     * @param file  the file to write to
     * @param data  the content to write to the file
     * @param append if {@code true}, then bytes will be added to the
     * end of the file rather than overwriting
     * @throws IOException in case of an I/O error
     * @since IO 2.1
     */
    public static void writeByteArrayToFile(File file, byte[] data, boolean append) throws IOException {
        OutputStream out = null;
        try {
            out = openOutputStream(file, append);
            out.write(data);
            out.close(); // don't swallow close Exception if copy completes normally
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     *
     * @param file  the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the
     * end of the file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }
}

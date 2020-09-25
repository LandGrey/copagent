package code.landgrey.copagent.utils;


import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


public class LogUtils {
    public static File log_file = null;
    public static File result_file = null;

    public static File getLogFile() {
        File log_directory = new File(PathUtils.getCurrentDirectory(), "logs");
        if(!log_directory.exists()){
            log_directory.mkdirs();
        }
        return new File(log_directory, "copagent.log");
    }

    public static void logit(String content){
        if(log_file == null){
            log_file = getLogFile();
        }
        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        String eventTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(createTime);

        PathUtils.appendTextToFile(log_file, eventTime + "  "+ content);
    }

    public static void result(String content){
        if(result_file == null){
            result_file = new File(PathUtils.getCurrentDirectory(), "result.txt");
        }
        PathUtils.writeTextToFile(result_file, content);
    }

}

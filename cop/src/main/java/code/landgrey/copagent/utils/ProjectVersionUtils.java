package code.landgrey.copagent.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static code.landgrey.copagent.utils.TimeUtils.UTC2CST;

public class ProjectVersionUtils {
    public static HashMap<String, String> get_version_info(){
        HashMap<String, String> version_info = new HashMap<String, String>(){};

        Class clazz = PathUtils.class;
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        try{
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String projectVersion = attr.getValue("Project-Version");
            String buildTime = attr.getValue("Build-Time");
            projectVersion = (projectVersion == null ? "UNKNOWN" : projectVersion);
            buildTime = (buildTime == null ? "UNKNOWN" : UTC2CST(buildTime, "yyyy-MM-dd HH:mm:ss"));
            version_info.put("Project-Version", projectVersion);
            version_info.put("Build-Time", buildTime);
        }catch (Exception e){
            version_info.put("Project-Version", "UNKNOWN");
            version_info.put("Build-Time", "UNKNOWN");
        }
        return version_info;
    }

}

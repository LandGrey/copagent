package code.landgrey.copagent;

import code.landgrey.copagent.utils.ClassUtils;
import code.landgrey.copagent.utils.LogUtils;
import code.landgrey.copagent.utils.PathUtils;
import code.landgrey.copagent.utils.SearchUtils;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Agent {
    public static File agent_work_directory = null;

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        catchThief(agentArgs, instrumentation);
        instrumentation.addTransformer(new DefineTransformer(), true);
    }


    static class DefineTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            return classfileBuffer;
        }
    }


    private static synchronized void catchThief(String name, Instrumentation ins){
        LogUtils.logit("Agent.jar is success attached");
        LogUtils.logit("Current Agent.jar Directory : " + PathUtils.getCurrentDirectory());
        LogUtils.logit("Prepared Dump class name    : " + name);

        List<Class<?>> resultClasses = new ArrayList<Class<?>>();

        // 获得所有已加载的类及类名
        Class<?>[] loadedClasses = ins.getAllLoadedClasses();
        LogUtils.logit("Found All Loaded Classes    : " + loadedClasses.length);
        List<String> loadedClassesNames = new ArrayList<String>();
        for(Class<?> cls: loadedClasses){
            loadedClassesNames.add(cls.getName());
        }

        // 单独保存已加载的类名及 hashcode、classloader、classloader hashcode 信息
        agent_work_directory = new File(PathUtils.getCurrentDirectory());
        File allLoadedClassFile = new File(new File(agent_work_directory, "logs"), "allLoadedClasses.txt");
        LogUtils.logit("Prepared Store All Loaded Classes Name ...");
        PathUtils.appendTextToFile(allLoadedClassFile, "[*] Format: [classname | class-hashcode | classloader | classloader-hashcode]\n");
        ClassUtils.storeAllLoadedClassesName(allLoadedClassFile, loadedClasses);
        LogUtils.logit("All Loaded Classes Name Store in : " + allLoadedClassFile.getAbsolutePath());

        // 实现的可能具有 web shell 功能的父类名
        List<String> riskSuperClassesName = new ArrayList<String>();
        riskSuperClassesName.add("javax.servlet.http.HttpServlet");

        // 黑名单拦截
        List<String> riskPackage = new ArrayList<String>();
        riskPackage.add("net.rebeyond.");
        riskPackage.add("com.metasploit.");

        // 风险注解
        List<String> riskAnnotations = new ArrayList<String>();
        riskAnnotations.add("org.springframework.stereotype.Controller");
        riskAnnotations.add("org.springframework.web.bind.annotation.RestController");
        riskAnnotations.add("org.springframework.web.bind.annotation.RequestMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.GetMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.PostMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.PatchMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.PutMapping");
        riskAnnotations.add("org.springframework.web.bind.annotation.Mapping");

        // 默认没有指定具体类名的流程
        if(name.equals("[unknown]")){
            java.util.List<String> interfaces = null;

            for(Class<?> clazz: loadedClasses){
                Class<?> target = clazz;
                boolean not_found = true;

                for(String packageName: riskPackage){
                    if(clazz.getName().startsWith(packageName)){
                        resultClasses.add(clazz);
                        not_found = false;
                        ClassUtils.dumpClass(ins, clazz.getName(), false, Integer.toHexString(target.getClassLoader().hashCode()));
                        break;
                    }
                }

                if(ClassUtils.isUseAnnotations(clazz, riskAnnotations)){
                    resultClasses.add(clazz);
                    not_found = false;
                    ClassUtils.dumpClass(ins, clazz.getName(), false, Integer.toHexString(target.getClassLoader().hashCode()));
                }

                if(not_found){
                    // 递归查找
                    while (target != null && !target.getName().equals("java.lang.Object")){
                        // 每次都重新获得目标类实现的所有接口
                        interfaces = new ArrayList<String>();
                        for(Class<?> cls: target.getInterfaces()){
                            interfaces.add(cls.getName());
                        }
                        if( // 继承危险父类的目标类
                                (target.getSuperclass() != null && riskSuperClassesName.contains(target.getSuperclass().getName())) ||
                                        // 实现特殊接口的目标类
                                        target.getName().equals("org.springframework.web.servlet.handler.AbstractHandlerMapping") ||
                                        interfaces.contains("javax.servlet.Filter") ||
                                        interfaces.contains("javax.servlet.Servlet") ||
                                        interfaces.contains("javax.servlet.ServletRequestListener")
                        )
                        {
                            LogUtils.logit("[!] find suspicious class: [" + target.getName() + "]  class hashcode: [" + Integer.toHexString(target.hashCode()) + "]  ClassLoader: [" + target.getClassLoader().getClass().getName() + "]  ClassLoader hashcode: [" + Integer.toHexString(target.getClassLoader().hashCode()) + "]\n\n");
                            if(loadedClassesNames.contains(clazz.getName())){
                                resultClasses.add(clazz);
                                ClassUtils.dumpClass(ins, clazz.getName(), false, Integer.toHexString(clazz.getClassLoader().hashCode()));
                            }else{
                                LogUtils.logit("cannot find " + clazz.getName() + " classes in instrumentation");
                            }
                            break;
                        }
                        target = target.getSuperclass();
                    }
                }

            }
        }else{
            if(loadedClassesNames.contains(name)){
                Class<?> clazz = ClassUtils.dumpClass(ins, name, false, null);
                resultClasses.add(clazz);
            }else if(name.contains("*")){
                Set<Class<?>> findClasses = SearchUtils.searchClass(ins, name, true, null);
                while(findClasses.iterator().hasNext()){
                    Class<?> clazz = findClasses.iterator().next();
                    resultClasses.add(clazz);
                    ClassUtils.dumpClass(ins, clazz.getName(), false, null);
                }
            }else{
                LogUtils.logit("class name [" + name + "] not found in loaded classes");
            }
        }

        int order = 1;

        List<String> riskKeyword = new ArrayList<String>();
        riskKeyword.add("javax.crypto.");
        riskKeyword.add("ProcessBuilder");
        riskKeyword.add("getRuntime");
        riskKeyword.add("shell");

        String results = "All Suspicious Class    : " + resultClasses.size() + "\n\n";
        String high_level = "============================================================\nhigh risk level Class   : \n";
        String normal_level = "============================================================\nnormal risk level Class : \n";
        for(Class<?> clazz: resultClasses){
            File dumpPath = PathUtils.getStorePath(clazz, false);
            String level = "normal";
            String content = PathUtils.getFileContent(dumpPath);
            for(String keyword: riskKeyword){
                if(content.contains(keyword)){
                    level = "high";
                    break;
                }
            }
            String tmp = "";
            try{
                tmp = String.format("order       : %d\nname        : %s\nrisk level  : %s\nlocation    : %s\nhashcode    : %s\nclassloader : %s\nextends     : %s\n\n", order, clazz.getName(), level, dumpPath.getAbsolutePath(), Integer.toHexString(clazz.hashCode()), clazz.getClassLoader().getClass().getName(), clazz.getClassLoader());
            }catch (NullPointerException e){
                tmp = String.format("order       : %d\nname        : %s\nrisk level  : %s\nlocation    : %s\nhashcode    : %s\nclassloader : %s\nextends     : [NullPointerException]\n\n", order, clazz.getName(), level, dumpPath.getAbsolutePath(), Integer.toHexString(clazz.hashCode()), clazz.getClassLoader().getClass().getName());
            }
            if(level.equals("high")){
                high_level += tmp;
            }else{
                normal_level += tmp;
            }
            order += 1;
        }

        LogUtils.logit(results + high_level + normal_level);
        LogUtils.result(results + high_level + normal_level);
    }
}

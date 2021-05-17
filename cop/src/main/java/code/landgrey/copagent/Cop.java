package code.landgrey.copagent;

import code.landgrey.copagent.common.AnsiLog;
import code.landgrey.copagent.utils.*;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.apache.commons.cli.*;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.*;


public class Cop {
    public static File agent_work_directory = new File(PathUtils.getCurrentDirectory(), ".copagent");

    public static void main(String[] args) throws Exception {
        long pid = -1;
        String jvm_pid = null;
        String filterClassName = "[unknown]";
        String attach_jar_path = null;
        String current_jar_path = PathUtils.getCurrentJarPath();
//        AnsiLog.info("current_jarPth: " + current_jar_path);
        AnsiLog.info("Java version: "+ JavaVersionUtils.javaVersionStr());
        HashMap<String, String> version_info = ProjectVersionUtils.get_version_info();

        // add "-Xbootclasspath/a" command line start cop.jar
        AnsiLog.info("args length: "+args.length);

        List<String> argsList = Arrays.asList(args);

        boolean is_boot_start = argsList.contains("bootstart_flag");
        boolean is_greater_than_jre9 = argsList.contains("greater_than_jdk9_flag");

        if(is_boot_start || is_greater_than_jre9 ){
            jvm_pid = args[0];
            attach_jar_path = args[1];
            if(args.length >= 3){
                filterClassName = args[2];
            }
            AnsiLog.info("Try to attach process " + jvm_pid + ", please wait a moment ...");

            attach(jvm_pid, attach_jar_path, filterClassName);

            AnsiLog.info("Attach process {} finished .", jvm_pid);
            AnsiLog.info("Result store in : {}", new File(agent_work_directory, "result.txt"));
            System.exit(0);
        }else{
            try {
                Options options = new Options();
                options.addOption("h", "help", false, "print options information");
                options.addOption("v", "version", false, "print the version of copagent");
                options.addOption("p", "pid", true, "attach jvm process pid");
                options.addOption("c", "class", true, "class name regex to dump");

                CommandLineParser parser = new DefaultParser();
                CommandLine cmdLine = parser.parse(options, args);

                if (cmdLine.hasOption("version")) {
                    AnsiLog.info("Version   :  " + version_info.get("Project-Version") + "\nBuild Time:  " + version_info.get("Build-Time") + "\n");
                    System.exit(0);
                } else if (cmdLine.hasOption("help")) {
                    new HelpFormatter().printHelp("java -jar copagent.jar", options, true);
                    System.exit(0);
                }
                if(cmdLine.hasOption("class")){
                    filterClassName = cmdLine.getOptionValue("class");
                }
                if (cmdLine.hasOption("pid")) {
                    String input_pid = cmdLine.getOptionValue("pid");
                    pid = Long.parseLong(input_pid);
                    jvm_pid = Long.toString(pid);
                }
                else {
                    AnsiLog.info( AnsiLog.red("Version") + "    : " + AnsiLog.yellow(version_info.get("Project-Version")));
                    AnsiLog.info(AnsiLog.red("Build Time")+ " : " + AnsiLog.yellow(version_info.get("Build-Time")));

                    // select jvm process pid
                    try {
                        pid = ProcessUtils.select(false, -1, null);
                    } catch (InputMismatchException e) {
                        AnsiLog.warn("Please input an integer to select pid.");
                        System.exit(1);
                    }
                    if (pid < 0) {
                        AnsiLog.error("Please select an available pid.");
                        System.exit(1);
                    }
                    jvm_pid = Long.toString(pid);
                }
            } catch (Throwable e) {
                AnsiLog.error("Failed to parse options\n" + e.getMessage());
                System.exit(0);
            }

            // create cop agent work directory
            if(! agent_work_directory.exists()){
                if(! PathUtils.createDirectory(agent_work_directory)){
                    AnsiLog.warn("Create directory {} failed, use {}", agent_work_directory.getAbsolutePath(), PathUtils.getTempDirectory().getAbsolutePath());
                    agent_work_directory = PathUtils.getTempDirectory();
                }
            }

            // update attach jar path
            attach_jar_path = new File(agent_work_directory, "agent.jar").getAbsolutePath();

            // release agent.jar
            if(! new File(attach_jar_path).exists()){
                PathUtils.copyResources("/agent.jar", new File(attach_jar_path));
                // check agent.jar
                if(! new File(attach_jar_path).exists()){
                    AnsiLog.error("Create agent.jar file [{}] failed !", attach_jar_path);
                    System.exit(1);
                }
            }

            /*
             * java <opts> -jar cop.jar <pid> </path/to/agent.jar> <dumpClassName>
             * */
            List<String> opts = new ArrayList<String>();
            opts.add("-jar");
            opts.add(current_jar_path);
            opts.add(jvm_pid);
            opts.add(attach_jar_path);
            opts.add(filterClassName);

            // real start cop.jar process
            ProcessUtils.startProcess(pid, opts);
        }
        System.exit(0);
    }


    public static void attach(String jvm_pid, String agent_jar_path, String filterClass) throws Exception{
    VirtualMachine virtualMachine = null;
    VirtualMachineDescriptor virtualMachineDescriptor = null;
    for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
        String pid = descriptor.id();
        if (pid.equals(jvm_pid)) {
            virtualMachineDescriptor = descriptor;
            break;
        }
    }
    try{
        if (null == virtualMachineDescriptor) {
            virtualMachine = VirtualMachine.attach(jvm_pid);
        } else {
            virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
        }
        Properties targetSystemProperties = virtualMachine.getSystemProperties();
        String targetJavaVersion = JavaVersionUtils.javaVersionStr(targetSystemProperties);
        String currentJavaVersion = JavaVersionUtils.javaVersionStr();
        if (targetJavaVersion != null && currentJavaVersion != null) {
            if (!targetJavaVersion.equals(currentJavaVersion)) {
                AnsiLog.warn("Current VM java version: {} do not match target VM java version: {}, attach may fail.", currentJavaVersion, targetJavaVersion);
                AnsiLog.warn("Target VM JAVA_HOME is {}, copagent JAVA_HOME is {}, try to set the same JAVA_HOME.", targetSystemProperties.getProperty("java.home"), System.getProperty("java.home"));
            }
        }
        virtualMachine.loadAgent(agent_jar_path, filterClass);
    }catch (Throwable t){
        t.printStackTrace();
    } finally {
        if (null != virtualMachine) {
            virtualMachine.detach();
        }
    }
}

}

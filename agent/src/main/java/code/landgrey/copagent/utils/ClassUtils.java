package code.landgrey.copagent.utils;

import code.landgrey.copagent.common.ClassDumpTransformer;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassUtils {
    public static Class<?> dumpClass(Instrumentation inst, String classPattern, boolean isRegEx, String code){
        Set<Class<?>> allClasses = SearchUtils.searchClassOnly(inst, classPattern, isRegEx, code);
        if(allClasses.iterator().hasNext()){
            Class<?> c = allClasses.iterator().next();
            ClassDumpTransformer transformer = new ClassDumpTransformer(allClasses);
            InstrumentationUtils.retransformClasses(inst, transformer, allClasses);
            Map<Class<?>, File> classFiles = transformer.getDumpResult();
            transformer.dumpJavaIfNecessary(c, classFiles);
            return c;
        }else{
            LogUtils.logit(classPattern + " NoSuchElementException");
        }
        return null;
    }

    public static synchronized void storeAllLoadedClassesName(File fp, Class<?>[] classes){
        for (Class<?> cls : classes) {
            try{
                PathUtils.appendTextToFile(fp, cls.getName() + " | " + Integer.toHexString(cls.hashCode()) + " | " + cls.getClassLoader().getClass().getName() + " ｜ " + Integer.toHexString(cls.getClassLoader().hashCode()));
            }catch (Throwable t){
                try{
                    PathUtils.appendTextToFile(fp, cls.getName() + " | " + Integer.toHexString(cls.hashCode()) + " | null | null" );
                }catch (Throwable t1){
                    t1.printStackTrace();
                }
            }
        }
    }

    public static Boolean isUseAnnotations(Class<?> clazz, List<String> annotations){
        try{
            Annotation[] da = clazz.getDeclaredAnnotations();
            if(da.length > 0){
                for(Annotation _da: da){
                    for(String _annotation: annotations){
                        if(_da.annotationType().getName().equals(_annotation)){
                            return true;
                        }
                    }
                }
            }
        }catch (Throwable t){

        }
        return false;
    }
}

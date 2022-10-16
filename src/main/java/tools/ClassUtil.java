package tools;

import basic_class.ClassFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassUtil {
    public static List<ClassFile> getAllClassesFromJars(List<String> jarPathList,
                                                        boolean runtime) throws FileNotFoundException {
        Set<ClassFile> classFileSet = new HashSet<>();
        if (runtime) {
            getRuntime(classFileSet);
        }
        for (String jarPath : jarPathList) {
            classFileSet.addAll(JarUtil.resolveNormalJarFile(jarPath));
        }
        return new ArrayList<>(classFileSet);
    }
    public static List<ClassFile> getAllClassesFormfolder(List<String> jarPathList,boolean runtime) throws FileNotFoundException {
        Set<ClassFile> classFileSet = new HashSet<>();
        if (runtime) {
            getRuntime(classFileSet);
        }
        for (String jarPath : jarPathList) {
            classFileSet.addAll(JarUtil.resolveNormalfolderFile(jarPath));
        }
        return new ArrayList<>(classFileSet);
    }
    private static void getRuntime(Set<ClassFile> classFileSet) {
        String rtJarPath = System.getenv("JAVA_HOME") +
                File.separator + "jre" +
                File.separator + "lib" +
                File.separator + "rt.jar";
        Path rtPath = Paths.get(rtJarPath);
        if (!Files.exists(rtPath)) {
            throw new RuntimeException("rt.jar not exists");
        }
        classFileSet.addAll(JarUtil.resolveNormalJarFile(rtJarPath));
    }
}

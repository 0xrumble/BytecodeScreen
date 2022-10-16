package tools;

import basic_class.ClassFile;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
public class JarUtil {

    @SuppressWarnings("all")private static Logger logger;
    private static final Set<ClassFile> classFileSet = new HashSet<>();

    public static List<ClassFile> resolveNormalfolderFile(String jarPath) {
        List<String> fileNames = new ArrayList<String>();
        File file = new File(jarPath);
        resolvefile(file,fileNames);
        for (String value :  fileNames) {
            String md = value.replace(file.getAbsolutePath()+"\\","").replace("\\","/");
            Path path = Paths.get(value);
            ClassFile classFile = new ClassFile(md, path);
            classFileSet.add(classFile);
        }
        return new ArrayList<>(classFileSet);
    }

    public static List<ClassFile> resolveNormalJarFile(String jarPath) {
        try {
            final Path tmpDir = Files.createTempDirectory(
                    Paths.get(jarPath).getFileName().toString() + "_");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DirUtil.removeDir(tmpDir.toFile());
            }));
            resolve(jarPath, tmpDir);
            return new ArrayList<>(classFileSet);
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    private static void resolve(String jarPath, Path tmpDir) {
        try {
            InputStream is = new FileInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(is);
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                Path fullPath = tmpDir.resolve(jarEntry.getName());

                if (!jarEntry.isDirectory()) {
                    if (!jarEntry.getName().endsWith(".class")) {
                        continue;
                    }
                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {

                        Files.createDirectories(dirName);
                    }
                    OutputStream outputStream = Files.newOutputStream(fullPath);
                    IOUtil.copy(jarInputStream, outputStream);
                    ClassFile classFile = new ClassFile(jarEntry.getName(), fullPath);
                    // fullpath   C:\Users\hll\AppData\Local\Temp\CIDemo.jar_4817332695572281497\BOOT-INF\classes\org\sec\cidemo\web\XXEController.class
                    //classFIle  BOOT-INF/classes/org/sec/cidemo/web/XXEController.class
                    //System.out.println("jarentry   "+jarEntry.getName()); //获取到类名和springboot（）
                    //System.out.println("fullpath   "+fullPath);
                    //  System.out.println("classFIle  "+classFile.getClassName());
                    classFileSet.add(classFile);
                }
            }
        } catch (Exception e) {
        }
    }

    public static List<ClassFile> resolveSpringBootJarFile(String jarPath, boolean useAllLib) {
        try {
            final Path tmpDir = Files.createTempDirectory(
                    Paths.get(jarPath).getFileName().toString() + "_");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                DirUtil.removeDir(tmpDir.toFile());
            }));

            resolve(jarPath, tmpDir);
            //判断是否加入其它springboot中自带的依赖
            if (useAllLib) {
                resolveBoot(jarPath, tmpDir);
                Files.list(tmpDir.resolve("BOOT-INF/lib")).forEach(p ->
                        resolveNormalJarFile(p.toFile().getAbsolutePath()));
            }
            return new ArrayList<>(classFileSet);
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    private static void resolveBoot(String jarPath, Path tmpDir) {
        try {
            InputStream is = new FileInputStream(jarPath);
            JarInputStream jarInputStream = new JarInputStream(is);
            JarEntry jarEntry;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                Path fullPath = tmpDir.resolve(jarEntry.getName());
                if (!jarEntry.isDirectory()) {
                    if (!jarEntry.getName().endsWith(".jar")) {
                        continue;
                    }
                    Path dirName = fullPath.getParent();
                    if (!Files.exists(dirName)) {
                        Files.createDirectories(dirName);
                    }
                    OutputStream outputStream = Files.newOutputStream(fullPath);
                    IOUtil.copy(jarInputStream, outputStream);
                }
            }
        } catch (Exception e) {
        }
    }
    public static void resolvefile(File dir, List<String> fileNames) {
        if (!dir.exists() || !dir.isDirectory()) {// 判断是否存在目录
            if (dir.isFile()) {// 如果文件
                int is = dir.getName().lastIndexOf('.');
                System.out.println(dir.getName().substring(is+1));
                if(dir.getName().substring(is+1).equals("class")){
                    fileNames.add(dir.getAbsolutePath());// 添加文件全路径名
                }
            }
            System.out.println("error：路径无法获取");
            return;
        }
        String[] files = dir.list();// 读取目录下的所有目录文件信息
        for (int i = 0; i < files.length; i++) {// 循环，添加文件名或回调自身
            File file = new File(dir, files[i]);
            if (file.isFile()) {// 如果文件
                int is = files[i].lastIndexOf('.');
                if(files[i].substring(is+1).equals("class")){
                    fileNames.add(dir + "\\" + file.getName());// 添加文件全路径名
                }
            } else {// 如果是目录
                resolvefile(file, fileNames);// 回调自身继续查询
            }
        }
    }
}

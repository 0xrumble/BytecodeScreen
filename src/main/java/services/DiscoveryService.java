package services;

import basic_class.ClassFile;
import basic_class.ClassReference;
import basic_class.MethodReference;
import classvisitor.DiscoveryClassVisitor;
import org.objectweb.asm.ClassReader;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;



public class DiscoveryService {

    private static Logger logger;

    public static void  start(List<ClassFile> classFileList,
                              List<ClassReference> discoveredClasses,
                              List<MethodReference> discoveredMethods,
                              Map<ClassReference.Handle, ClassReference> classMap,
                              Map<MethodReference.Handle, MethodReference> methodMap,
                              Map<String, ClassFile> classFileByName) {
        for (ClassFile file : classFileList) {
            try {

                DiscoveryClassVisitor dcv = new DiscoveryClassVisitor(discoveredClasses, discoveredMethods);
                ClassReader cr = new ClassReader(file.getFile());
                cr.accept(dcv, ClassReader.EXPAND_FRAMES);
                classFileByName.put(dcv.getName(), file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (ClassReference clazz : discoveredClasses) {
            classMap.put(clazz.getHandle(), clazz);
        }
        for (MethodReference method : discoveredMethods) {
            methodMap.put(method.getHandle(), method);
        }
    }
}


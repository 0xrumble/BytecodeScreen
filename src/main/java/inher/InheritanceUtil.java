package inher;

import basic_class.ClassReference;
import basic_class.MethodReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class InheritanceUtil {
    public static InheritanceMap derive(Map<ClassReference.Handle, ClassReference> classMap) {
        Map<ClassReference.Handle, Set<ClassReference.Handle>> implicitInheritance = new HashMap<>();
        for (ClassReference classReference : classMap.values()) {
            Set<ClassReference.Handle> allParents = new HashSet<>();
            getAllParents(classReference, classMap, allParents);

            implicitInheritance.put(classReference.getHandle(), allParents);
        }
        return new InheritanceMap(implicitInheritance);
    }
    //传入的是classmap每一个的value，classmap，一个ClassReference.Handle的空Set
    ////传入的是1.实现的父类或接口类，classMap，所有实现的父类或接口类的handler集合Set
    private static void getAllParents(ClassReference classReference,
                                      Map<ClassReference.Handle, ClassReference> classMap,
                                      Set<ClassReference.Handle> allParents) {
        Set<ClassReference.Handle> parents = new HashSet<>();
        //判断是否有父类
        if (classReference.getSuperClass() != null) {
            parents.add(new ClassReference.Handle(classReference.getSuperClass()));
        }
        //获取实现的接口
        for (String i : classReference.getInterfaces()) {
            parents.add(new ClassReference.Handle(i));
        }
        //将这些实现的接口和父类的handler加入到新创建的Set里面
        for (ClassReference.Handle immediateParent : parents) {
            ClassReference parentClassReference = classMap.get(immediateParent);

            if (parentClassReference == null) {
                continue;
            }
            allParents.add(parentClassReference.getHandle());
            getAllParents(parentClassReference, classMap, allParents);
        }
    }

    public static Map<MethodReference.Handle, Set<MethodReference.Handle>> getAllMethodImplementations(
            InheritanceMap inheritanceMap, Map<MethodReference.Handle, MethodReference> methodMap) {

        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = getMethodsByClass(methodMap);
        Map<ClassReference.Handle, Set<ClassReference.Handle>> subClassMap = new HashMap<>();
        for (Map.Entry<ClassReference.Handle, Set<ClassReference.Handle>> entry : inheritanceMap.entrySet()) {
            for (ClassReference.Handle parent : entry.getValue()) {//这里是父类或接口类

                //生成一个key为父类或接口类，value为实现类的HashSet：subClasses
                if (!subClassMap.containsKey(parent)) {//如果没有该父类，则继续
                    Set<ClassReference.Handle> subClasses = new HashSet<>();
                    subClasses.add(entry.getKey());//获取实现类
                    subClassMap.put(parent, subClasses);
                } else {
                    subClassMap.get(parent).add(entry.getKey());
                }

            }
        }
        Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImplMap = new HashMap<>();
        for (MethodReference method : methodMap.values()) {
            if (method.isStatic()) {
                continue;
            }
            Set<MethodReference.Handle> overridingMethods = new HashSet<>();
            Set<ClassReference.Handle> subClasses = subClassMap.get(method.getClassReference());
            if (subClasses != null) {
                for (ClassReference.Handle subClass : subClasses) {
                    Set<MethodReference.Handle> subClassMethods = methodsByClass.get(subClass);
                    if (subClassMethods != null) {
                        for (MethodReference.Handle subClassMethod : subClassMethods) {
                            if (subClassMethod.getName().equals(method.getName()) &&
                                    subClassMethod.getDesc().equals(method.getDesc())) {
                                overridingMethods.add(subClassMethod);
                            }
                        }
                    }
                }
            }
            if (overridingMethods.size() > 0) {
                methodImplMap.put(method.getHandle(), overridingMethods);
            }

        }
        return methodImplMap;
    }

    public static Map<ClassReference.Handle, Set<MethodReference.Handle>> getMethodsByClass(
            Map<MethodReference.Handle, MethodReference> methodMap) {
        Map<ClassReference.Handle, Set<MethodReference.Handle>> methodsByClass = new HashMap<>();
        for (MethodReference.Handle method : methodMap.keySet()) {
            ClassReference.Handle classReference = method.getClassReference();
            if (!methodsByClass.containsKey(classReference)) {
                Set<MethodReference.Handle> methods = new HashSet<>();
                methods.add(method);
                methodsByClass.put(classReference, methods);
            } else {
                methodsByClass.get(classReference).add(method);
            }
        }
        return methodsByClass;
    }
}


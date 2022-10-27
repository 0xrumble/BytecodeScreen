package app;


import basic_class.ClassFile;
import basic_class.ClassReference;
import basic_class.MethodReference;
import com.beust.jcommander.JCommander;
import inher.InheritanceMap;
import inher.InheritanceUtil;
import services.*;
import tools.ClassUtil;
import tools.ReadUtil;


import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainScreen {
    private static final List<ClassFile> classFileList = new ArrayList<>();
    private static final List<ClassReference> discoveredClasses = new ArrayList<>();
    private static final List<MethodReference> discoveredMethods = new ArrayList<>();
    private static final Map<ClassReference.Handle, ClassReference> classMap = new HashMap<>();
    private static final Map<MethodReference.Handle, MethodReference> methodMap = new HashMap<>();

    private static final Map<MethodReference.Handle, MethodReference> methodMapdubbo = new HashMap<>();
    private static final Map<String, ClassFile> classFileByName = new HashMap<>();
    private static final Map<MethodReference.Handle, Set<MethodReference.Handle>> methodImpls = new HashMap<>();

    private static final HashSet<String> hassuperclass = new HashSet<>();

    private static final HashSet<String> resultclass = new HashSet<>();
    private static final List<String> hasinterfaces = new ArrayList<>();

    private static final List<String> has_parameter = new ArrayList<>();

    //获取commond
    private static final Commands command = new Commands();

    public static void screen(String[] args) throws Exception {
        Logo.PrintLogo();//打印logo
        System.out.println("============================================================================================");
        JCommander jc = JCommander.newBuilder().addObject(command).build();
        jc.parse(args);
        if (command.help) {
            jc.usage();
        }else{
            if (command.jarpath != null && !command.jarpath.equals("")) {
                    List<String> commondlist = new ArrayList<>();
                    boolean readbasicjar = false;
                    HashSet<String> surperclassname = new HashSet<>();

                    if(command.isjar){
                        readbasicjar = true;
                    }
                    getClassFileList(command.jarpath, readbasicjar);
                    discovery();
                    inherit();

                    if (command.interfaces != null) {
                        commondlist.add("interface");
                        has_interfaces(command.interfaces);
                        returnresults(hasinterfaces);
                        control_dubbomethod(hasinterfaces);


                        if(command.isdedug){
                            System.out.println("接口实现结果收集完毕,满足条件的类为:");
                            for(int i =0 ;i<hasinterfaces.size();i++){
                                int ii = i+1;
                                System.out.println("     "+ii+"."+hasinterfaces.get(i));
                            }
                        }

                    }

                    if (command.superclass != null) {
                        commondlist.add("superclass");
                        String superclassname1 = command.superclass.replace(".", "/");
                        surperclassname.add(superclassname1);
                        has_superclass(surperclassname);
                        List<String> supers = new ArrayList<>(hassuperclass);
                        returnresults(supers);
                        control_dubbomethod(supers);

                        if(command.isdedug){
                            System.out.println("继承结果收集完毕,满足条件的类为：");
                            for(int i =0 ;i<supers.size();i++){
                                int ii = i+1;
                                System.out.println("     "+ii+"."+supers.get(i));
                            }
                        }
                    }

                    if (command.method != null) {
                    String returnparameter = "";
                    String parameter = "";
                    String method = "";
                    boolean sta_tic = command.isstatic;
                    if (command.paramter != null) {
                        parameter = command.paramter.replace(".", "/").replace("|", "$");
                    }

                    if (command.returnparamter != null) {
                        returnparameter = command.returnparamter.replace(".", "/").replace("|", "$");
                    }
                    method = command.method;
                    has_parameter_returns(method, parameter, returnparameter,sta_tic);
                    List<String> has_parlist = new ArrayList<>();
                        for (String s : has_parameter) {
                            String[] ress = s.split("#   ");
                            has_parlist.add(ress[1]);
                        }
                    returnresults(has_parlist);
                        if(command.isdedug){
                            System.out.println("方法实现结果收集完毕,满足条件的类为：");
                            for(int i =0 ;i<has_parlist.size();i++){
                                int ii = i+1;
                                System.out.println("     "+ii+"."+has_parlist.get(i));
                            }
                        }

                }else {
                    System.out.println("no method input, input a method or nothing.");
                }
                if (command.interfaces != null && hasinterfaces.isEmpty()) {
                    resultclass.clear();
                }
                if (command.superclass != null && hassuperclass.isEmpty()) {
                    resultclass.clear();
                }
                if (has_parameter.isEmpty()) {
                    resultclass.clear();
                }
                List<String> resultlist = new ArrayList<>(resultclass);
                HashSet<String> shs = new HashSet<>();
                for (String s : resultlist) {
                    shs.add(s.replace(s.substring(s.indexOf("$")), ""));
                }
                System.out.println("result:");
                int ii =1;
                for(String xxs:shs){
                    System.out.println(ii+"."+xxs);
                    for (String s : resultlist) {
                        if (s.startsWith(xxs)) {
                            System.out.println("     " + s.substring(s.indexOf("$") + 1));
                        }

                    }
                    ii = ii+1;
                }

            }else {
                System.out.println("no jars input,please input a jar or a path or a txt.");
            }
        }

    }
    public static void control_dubbomethod(List<String> classnames){

        if(methodMapdubbo.isEmpty()){
            for (String classname : classnames) {
                for (Map.Entry<MethodReference.Handle, MethodReference> handle : methodMap.entrySet()) {
                    if (handle.getKey().getClassReference().getName().equals(classname)) {
                        methodMapdubbo.put(handle.getKey(), handle.getValue());
                    }
                }
            }
        }else {
            Map<MethodReference.Handle, MethodReference> methodMapdubbos = new HashMap<>(methodMapdubbo);
            methodMapdubbo.clear();
            for (String classname : classnames) {
                for (Map.Entry<MethodReference.Handle, MethodReference> handle : methodMapdubbos.entrySet()) {
                    if (handle.getKey().getClassReference().getName().equals(classname)) {
                        methodMapdubbo.put(handle.getKey(), handle.getValue());
                    }
                }
            }
        }
    }

    public static void has_parameter_returns(String name , String parameter, String returnparameter, Boolean isstaic){
        HashSet<String> hashSet = new HashSet<>();
        
        List<String> name1 = regex_out(name);
        List<String> parameter1 = regex_out(parameter);
        List<String> returnparameter1 = regex_out(returnparameter);

        for(int i =0;i<name1.size();i++){
            List<String> resultlist = new ArrayList<>();
            String methodname = name1.get(i);
            String methodparam = control_parameter(parameter1.get(i));
            String methodreturn = control_parameter(returnparameter1.get(i));

            if(methodname.length() == 0){
                System.out.println("名称不合法。。。");
                continue;
            }

            if(name1.get(i).contains("*")){
                resultlist.add("方法名模糊");
            }else {
                resultlist.add("方法名确定");
            }

            if(parameter1.get(i).equals("void")){
                resultlist.add("没有参数");
            }else {
                if(parameter1.get(i).contains("*")){
                    resultlist.add("参数模糊");
                }else {
                    resultlist.add("参数确定");
                }

            }
            if(returnparameter1.get(i).equals("void")){
                resultlist.add("没有返回值");
            }else {
                if(returnparameter1.get(i).contains("*")){
                    resultlist.add("返回值模糊");
                }else {
                    resultlist.add("返回值确定");
                }

            }

            if(isstaic){
                resultlist.add("static");
            }else {
                resultlist.add("nostatic");
            }
            System.out.println(resultlist);
            //多次--->判断最终结果是否为空
            if(has_parameter.size() == 0){//最终结果为空
                hashSet = targetclass(resultlist,methodname,methodparam,methodreturn);
                has_parameter.addAll(hashSet);
            }else {//最终结果不为空
                HashSet<String> hashSet1 = targetclass(resultlist,methodname,methodparam,methodreturn);
                for(String ii:has_parameter){
                    int idext = ii.indexOf("#");
                    String sd = ii.substring(idext);
                    String xxs = ii.replace(sd,"");
                    for(String target:hashSet1){

                        int targetindex = target.indexOf("#");
                        String ss = target.substring(targetindex);
                        String axa = target.replace(ss,"");
                        if(axa.equals(xxs)){
                            hashSet.add(ii);
                            hashSet.add(target);
                        }
                    }
                }
            }
            has_parameter.clear();
            has_parameter.addAll(hashSet);
            hashSet.clear();

        }
    }
    public static HashSet<String> targetclass(List<String> resultlist,String methodname,String methodparam,String methodreturn){
        HashSet<String> result = new HashSet<>();
        HashMap<String,List<String>> nameresult = new HashMap<>();
        HashMap<String,List<String>> parresult = new HashMap<>();
        HashMap<String,List<String>> returnresult = new HashMap<>();
        HashMap<String,List<String>> staticresult = new HashMap<>();
        methodname = methodname.replace("*","");

        if(methodMapdubbo.isEmpty()){
            methodMapdubbo.putAll(methodMap);
        }

        //name
        if(Objects.equals(resultlist.get(0), "方法名模糊")){
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(handle.getValue().getName().contains(methodname)){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            nameresult = getresult(hashSet,list);
            make_methoddunnolist(nameresult);

        }else {
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for (Map.Entry<MethodReference.Handle, MethodReference> handle : methodMapdubbo.entrySet()) {
                if (handle.getValue().getName().equals(methodname)) {
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            nameresult = getresult(hashSet,list);
            make_methoddunnolist(nameresult);
        }


        //par
        if(Objects.equals(resultlist.get(1), "参数模糊")){
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(!handle.getValue().getParamer().isEmpty()){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            parresult =getresult(hashSet,list);
            make_methoddunnolist(parresult);
        }else if(Objects.equals(resultlist.get(1), "参数确定")){
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(handle.getValue().getParamer().equals(methodparam)){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            parresult = getresult(hashSet,list);
            make_methoddunnolist(parresult);
        }else if(Objects.equals(resultlist.get(1), "没有参数")){
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(handle.getValue().getParamer().length() == 0){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            parresult = getresult(hashSet,list);
            make_methoddunnolist(parresult);
        }

        //return
        if(Objects.equals(resultlist.get(2), "返回值模糊")){
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(!handle.getValue().getReturnpar().isEmpty()){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            returnresult = getresult(hashSet,list);
            make_methoddunnolist(returnresult);
        } else if(Objects.equals(resultlist.get(2), "返回值确定")) {
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(handle.getValue().getReturnpar().equals(methodreturn)){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            returnresult = getresult(hashSet,list);
            make_methoddunnolist(returnresult);
        }else if(Objects.equals(resultlist.get(2), "没有返回值")) {
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(handle.getValue().getReturnpar().length() == 0){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            returnresult = getresult(hashSet,list);
            make_methoddunnolist(returnresult);
        }

        //static
        if(Objects.equals(resultlist.get(3), "nostatic")){
            List<String> list = new ArrayList<>();
            HashSet<String> hashSet = new HashSet<>();
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbo.entrySet()){
                if(!handle.getValue().isStatic()){
                    list.add(handle.getKey().getClassReference().getName()+"$"+handle.getValue().getName()+" param:"+handle.getValue().getParamer()+" returnparam:"+handle.getValue().getReturnpar());
                    hashSet.add(handle.getKey().getClassReference().getName());
                }
            }
            staticresult = getresult(hashSet,list);
            make_methoddunnolist(staticresult);
        }
        if(Objects.equals(resultlist.get(3), "nostatic")){
            result = tarclass(nameresult,parresult,returnresult,staticresult);
        }else {
            result = tarclass(nameresult,parresult,returnresult);
        }
        return result;
    }
    public static void make_methoddunnolist(HashMap<String,List<String>> hashMap){

        Map<MethodReference.Handle, MethodReference> methodMapdubbos = new HashMap<>(methodMapdubbo);

        methodMapdubbo.clear();

        for(Map.Entry<String,List<String>> k:hashMap.entrySet()){
            for(Map.Entry<MethodReference.Handle, MethodReference> handle:methodMapdubbos.entrySet()){
                if(handle.getKey().getClassReference().getName().equals(k.getKey())){
                    methodMapdubbo.put(handle.getKey(),handle.getValue());
                }
            }
        }

    }
    public static HashMap<String,List<String>> getresult(HashSet<String> hashSet,List<String> list){
        HashMap<String,List<String>> returnresult = new HashMap<>();
        if (command.isdedug){
            for(String s:hashSet){
                List<String> list1 = getlist(s,list);
                returnresult.put(s,list1);
                System.out.println(returnresult);
            }
        }
        else {
            for(String s:hashSet){
                List<String> list1 = getlist(s,list);
                returnresult.put(s,list1);
            }
        }
        return returnresult;

    }
    public static List<String> getlist(String s,List<String> list){
        List<String> list1 = new ArrayList<>();
        for(String d: list){
            int x = d.indexOf("$");
            String sd = d.substring(x);
            String xxs = d.replace(sd,"");
            if(xxs.equals(s)){
                list1.add(d);
            }
        }
        return list1;
    }

    public static HashSet<String> tarclass(HashMap<String,List<String>> hashSet1,HashMap<String,List<String>> hashSet2,HashMap<String,List<String>> hashSet3,HashMap<String,List<String>> hashSet4){
        HashSet<String> result = new HashSet<>();
        for(Map.Entry<String,List<String>> k:hashSet1.entrySet()){
            for(Map.Entry<String,List<String>> s:hashSet2.entrySet()){
                for(Map.Entry<String,List<String>> d:hashSet3.entrySet()){
                    for(Map.Entry<String,List<String>> f:hashSet4.entrySet()){
                        if(k.getKey().equals(s.getKey())  &&  k.getKey().equals(d.getKey()) && k.getKey().equals(f.getKey())){
                        for(String k1:k.getValue()){
                            for(String s1:s.getValue()){
                                for(String d1:d.getValue()){
                                    for(String f1:f.getValue()){
                                    if(k1.equals(s1)&&k1.equals(d1)&&k1.equals(f1)){
                                        int x = k1.indexOf(" param:");
                                        String sd = k1.substring(x);
                                        String resultname = k1.replace(sd,"");
                                        result.add(k.getKey()+"#   "+resultname);
                                    }
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    public static HashSet<String> tarclass(HashMap<String,List<String>> hashSet1,HashMap<String,List<String>> hashSet2,HashMap<String,List<String>> hashSet3){
        HashSet<String> result = new HashSet<>();
        for(Map.Entry<String,List<String>> k:hashSet1.entrySet()){
            for(Map.Entry<String,List<String>> s:hashSet2.entrySet()){
                for(Map.Entry<String,List<String>> d:hashSet3.entrySet()){
                    if(k.getKey().equals(s.getKey())  &&  k.getKey().equals(d.getKey())){
                        for(String k1:k.getValue()){
                            for(String s1:s.getValue()){
                                for(String d1:d.getValue()){
                                    if(k1.equals(s1)&&k1.equals(d1)){
                                        int x = k1.indexOf(" param:");
                                        String sd = k1.substring(x);
                                        String resultname = k1.replace(sd,"");
                                        result.add(k.getKey()+"#   "+resultname);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static List<String> regex_out(String input){
        String regex = "\\{\\(.*?\\)\\}";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(input);
        List<String> al = new ArrayList<>();
        while (m.find()) {
            al.add(m.group(0).replace("{(","").replace(")}",""));
        }
        return al;
    }
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        int index = 0;
        while ((index = srcText.indexOf(findText, index)) != -1) {
            index = index + findText.length();
            count++;
        }
        return count;
    }
    public static String control_parameter(String parameter){
        String[] list = parameter.split(",");
        StringBuilder par = new StringBuilder();
        for (String s : list) {
            par.append(Basic_variable_type(s));
        }
        return par.toString();
    }
    public static String Basic_variable_type(String str){
        switch (str){
            case "byte":
                return "B";
            case "char":
                return "C";
            case "double":
                return "D";
            case "float":
                return "F";
            case "int":
                return "I";
            case "long":
                return "J";
            case "short":
                return "S";
            case "boolean":
                return "Z";
            case "void":
                return "V";
            case "*":
                return "";
            default:
                if(str.length() == 0){
                    return "V";
                }
                if(str.contains("[]")){
                    int count = appearNumber(str,"[]");
                    StringBuilder strBuilder = new StringBuilder("L" + str + ";");
                    for(int i = 0; i<count; i++){
                        strBuilder.insert(0, "[");
                    }
                    str = strBuilder.toString();
                    return str.replace("[]","");
                }else {
                    str = "L"+str+";";
                    return str;
                }
        }
    }

    public static HashSet<String> has_superclass(HashSet<String> superclass){
        HashSet<String> hashSet = new HashSet<>();
        for(String supers :superclass){
            for(Map.Entry<ClassReference.Handle, ClassReference> handle:classMap.entrySet()){
                if(handle.getValue().getSuperClass() != null){
                    if(handle.getValue().getSuperClass().equals(supers)){
                        if(handle.getKey().getName().contains("$")){
                            continue;
                        }
                        hassuperclass.add(handle.getKey().getName());
                        hashSet.add(handle.getKey().getName());
                    }
                }
            }
        }
        if(hashSet.isEmpty()){
            return null;
        }else{
            return has_superclass(hashSet);
        }
    }
    public static void has_interfaces(List<String> interfacea){
        List<String> hashSet = new ArrayList<>();
        for (String s : interfacea) {
            if (hasinterfaces.size() == 0) {
                for (Map.Entry<ClassReference.Handle, ClassReference> handle : classMap.entrySet()) {
                    List<String> interfaces = handle.getValue().getInterfaces();
                    if (interfaces != null) {
                        for (String anInterface : interfaces) {
                            if (anInterface.contains(s.replace(".", "/").replace("|", "$"))) {
                                hasinterfaces.add(handle.getKey().getName());
                            }
                        }
                    }
                }
            } else {
                for (Map.Entry<ClassReference.Handle, ClassReference> handle : classMap.entrySet()) {
                    List<String> interfaces = handle.getValue().getInterfaces();
                    if (interfaces != null) {
                        for (String anInterface : interfaces) {
                            if (anInterface.contains(s.replace(".", "/").replace("|", "$"))) {
                                for (String ii : hasinterfaces) {
                                    if (handle.getKey().getName().equals(ii)) {
                                        hashSet.add(handle.getKey().getName());
                                    }
                                }
                            }
                        }
                    }
                }
                hasinterfaces.clear();
                hasinterfaces.addAll(hashSet);
                hashSet.clear();
            }
        }
    }
    private static void inherit() {
        InheritanceMap inheritanceMap = InheritanceService.start(classMap);//key是每一个类classReference.getHandle,value是一个key为每一个class.handle,value其父类或者接口类handle的set集合
        methodImpls.putAll(InheritanceUtil.getAllMethodImplementations(inheritanceMap, methodMap));

    }
    private static void discovery() {
        DiscoveryService.start(classFileList, discoveredClasses, discoveredMethods,
                classMap, methodMap, classFileByName);
    }
    private static void getClassFileList(String path, boolean readbasicjar) throws Exception {
        ReadUtil readUtil = new ReadUtil();
        File file = new File(path);
        if(file.isDirectory()){
            getclassfromfloder(path, true,readbasicjar);
        }
        if(file.isFile()){
            if(path.endsWith(".txt")){
                List<String> list = readUtil.readToString(path);
                for (String s : list) {
                    if (s.endsWith(".jar")) {
                        classFileList.addAll(ClassUtil.getAllClassesFromJars(Collections.singletonList(s),
                                readbasicjar));
                    } else if (new File(s).isDirectory()) {
                        getclassfromfloder(s, true, readbasicjar);
                    } else {
                        System.out.println("导入出错！！！！！！！！！！！！！");
                    }
                }
            } else if (path.endsWith(".jar")) {
                classFileList.addAll(ClassUtil.getAllClassesFromJars(Collections.singletonList(path), readbasicjar));
            }
        }
    }
    public static void getclassfromfloder(String path,boolean readjar,boolean readbasicjar) throws Exception {
        File file = new File(path);
        classFileList.addAll(ClassUtil.getAllClassesFormfolder(Collections.singletonList(path), readbasicjar));

        if(readjar){
            List<String> jarfiles = ReadUtil.noRecursion(file,".jar");
            assert jarfiles != null;
            for (String jarfile : jarfiles) {
                classFileList.addAll(ClassUtil.getAllClassesFromJars(Collections.singletonList(jarfile), readbasicjar));
            }
        }
    }
    public static void returnresults(List<String> target){

        if(resultclass.size() == 0){
            resultclass.addAll(target);
        }else{
            List<String> middo = new ArrayList<>(resultclass);
            resultclass.clear();
            for(String s:middo){
                for (String value : target) {
                    if (value.startsWith(s)) {
                        resultclass.add(value);
                    }
                }
            }
            middo.clear();
        }

    }

}

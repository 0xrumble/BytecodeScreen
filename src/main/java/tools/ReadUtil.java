package tools;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ReadUtil {

    public List<String> readToString(String file) {
        List<String> strings = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (line != null) {
                strings.add(line);
                // read next line
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return strings;
    }
    public static List<String> noRecursion(File dir, String types){
        int fileNum=0,folderNum=0;
        List<String> lists = new ArrayList<String>();
        LinkedList<File> list=new LinkedList<File>();
        File[] tempList = dir.listFiles();
        for (int i = 0; i < tempList.length; i++) {
            if (tempList[i].isFile()) {
                if(tempList[i].toString().endsWith(types)) {
                    lists.add(tempList[i].toString());
                }
            }}
        if(dir.exists()){
            if (null==dir.listFiles()){
                return null;
            }
            list.addAll(Arrays.asList(dir.listFiles()));
            while(!list.isEmpty()){
                File[] files = list.removeFirst().listFiles();
                if(null==files){
                    continue;
                }
                for (File f:files) {
                    if (f.isDirectory()) {
                        list.add(f);
                        folderNum++;
                    } else {
                        if(f.getAbsolutePath().endsWith(types)){
                            lists.add(f.getAbsolutePath());
                        }
                        fileNum++;
                    }
                }
            }
        }else{
            System.out.println("文件不存在！");
        }
        //System.out.println("文件夹数量:" + folderNum + ",文件数量:" + fileNum);
        return lists;
    }
}

package com.click369.controlbp.util;

import android.content.Context;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.click369.controlbp.bean.Question;
import com.click369.controlbp.bean.WhiteApp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asus on 2017/10/15.
 */
public class FileUtil {
    public static String ROOTPATH = Environment.getExternalStorageDirectory()+ File.separator+"processcontrol";
    public static String FILEPATH = ROOTPATH+File.separator+"files";
    public static String IMAGEPATH = ROOTPATH+File.separator+"imgs";
    public static String IFWPATH = ROOTPATH+File.separator+"ifw";
    static {
        init();
    }
    public static void init(){
        File dir = new File(FILEPATH);
        if(!dir.exists()){
            dir.mkdirs();
        }
        dir = new File(IFWPATH);
        if(!dir.exists()){
            dir.mkdirs();
        }
        dir = new File(IMAGEPATH);
        if(!dir.exists()){
            dir.mkdirs();
            File f = new File(dir,".nomedia");
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void writeObj(Object obj,String path){
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(path)));
            oos.writeObject(obj);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static  Object readObj(String path){
        Object obj = null;
        File file = new File(path);
        if(!file.exists()){
            return obj;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(path)));
            obj = ois.readObject();
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }


//    private static boolean isRead = false;
//    public static void  changeIFWQX(String name,boolean isOpen){
//        changeQX(isOpen?777:700,"/data/system/ifw"+(name==null?"":"/"+name));
//    }
    public static String readIFWFile(String fileName){
        String s = "";
        String path = "/data/system/ifw/" + fileName + ".xml";
        changeQX(777,path);
        byte ss[] = readFile(path);
        if(ss == null){
            s="";
        }else{
            s = new String(ss);
        }
        changeQX(700,path);
        return s;
    }
    public static String readIFWList(String fileName){
        String s = "";
        if(changeQX(777,"/data/system/ifw/")) {
            String path = "/data/system/ifw/" + fileName + ".xml";
            changeQX(777,path);
            byte ss[] = readFile(path);
            if(ss == null){
                s="";
            }else{
                s = new String(ss);
            }
            changeQX(700,path);
        }
        changeQX(700,"/data/system/ifw/");
        return s;
    }
    public static void delIFWList(String fileName){
        if(changeQX(777,"/data/system/ifw/")) {
            String path = "/data/system/ifw/" + fileName + ".xml";
            File f = new File(path);
            if(f.exists()){
                f.delete();
            }
        }
        changeQX(700,"/data/system/ifw/");
    }

    public static final int IFWTYPE_SERVICE = 0;
    public static final int IFWTYPE_ACTIVITY = 2;
    public static final int IFWTYPE_RECEIVER = 1;
    public static void writeIFWList(String fileName,String pkg,String content,int type) {//
        String tagNames[] = {"service","broadcast","activity"};
        File parent = new File("/data/system/ifw");
        if(!parent.exists()){
            if(changeQX(775,"/data/system")){
                parent.mkdirs();
            }
            changeQX(771,"/data/system");
        }
        if(changeQX(777,"/data/system/ifw")){
            String path = "/data/system/ifw/"+fileName+".xml";
            File f = new File(path);
            if (f.exists()) {
                changeQX(777,path);
                byte datas[] = readFile(path);
                String s = null;
                if(datas!= null){
                    s = new String(datas);
                }
                if (s != null && s.indexOf(content)==-1) {
                    StringBuilder sb = new StringBuilder(s.replace("</"+tagNames[type]+">\n</rules>\n",""));
                    sb.append("<component-filter name=\""+pkg+"/"+content+"\"/>\n");
                    sb.append("</"+tagNames[type]+">\n").append("</rules>").append("\n");
                    writeFile(path, sb.toString().getBytes());
                }else if(s != null && s.indexOf(content)!=-1){
                    String s1 = s.replace("<component-filter name=\""+pkg+"/"+content+"\"/>\n","");
                    writeFile(path,s1.getBytes());
                }
                changeQX(644,path);
            } else {
                try {
                    if(f.createNewFile()){
                        changeQX(777,path);
                        StringBuilder sb = new StringBuilder();
                        sb.append("<rules>").append("\n").append("<"+tagNames[type]+" block=\"true\" log=\"false\">\n");
//                        writeFile(path, msg.getBytes());
                        sb.append("<component-filter name=\""+pkg+"/"+content+"\"/>\n");
                        sb.append("</"+tagNames[type]+">\n").append("</rules>").append("\n");
                        writeFile(path, sb.toString().getBytes());
                        changeQX(644,path);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        changeQX(700,"/data/system/ifw");
    }
    public static boolean changeQX(int mode,String path) {
        /**
         * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
         *
         * @param command
         *            命令：String
         *            apkRoot="chmod 777 "+getPackageCodePath();//RootCommand
         *            (apkRoot);
         * @return 应用程序是/否获取Root权限
         */
        DataOutputStream os = null;
        Process process = null;
        try {
            String command = "chmod "+mode+" "+path;///deviceidle.xml
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

//    public static boolean changeQX(int mode,String path) {
//        ShellUtilBackStop.execCommand("chmod "+mode+" "+path,false);
//        return true;
//    }
    public static byte[] readFile(String path){
//		if(!isReadFile){
//			return "没有文件读写权限".getBytes();
//		}
        File file = new File(path);
        //数组用来保存读取的数据 相当于水池
        byte datas[] = null;
        if(!file.exists()){
            datas = null;
            Log.i("DOZE","fileutil readFile null");
        }else{
            try {
                //字节数组输出流 用来往内存中写字节数组  可以用来拼接字节数组
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //创建文件输入流
                FileInputStream fis = new FileInputStream(file);
                //用来保存每次读的数据 相当水瓢(每次读1024字节 但是不一定每次能读这么多  实际读取的长度用len保存)
                byte data[] = new byte[1024*1024];
                //用来保存每次读取的字节大小
                int len = 0;
                //不断的读取 直到数据读完
                while((len = fis.read(data))>0){
                    //把每次读入的数据 存放在字节数组流的内存中
                    baos.write(data, 0, len);
                }
                //把字节数组流中的数据转为字节数组
                datas = baos.toByteArray();
                baos.flush();
                baos.close();
                //关闭流
                fis.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
//			Log.i("DOZE","fileutil readFile datas.length"+datas.length);
        }
        return datas;
    }

    public static boolean writeFile(String path,byte datas[]){
        try {
            if(datas==null){
                return false;
            }
            File file = new File(path);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(datas);
            //倾倒关闭
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getLog(String con){
       String time =  TimeUtil.changeMils2String(System.currentTimeMillis(),"MM-dd HH:mm:ss");
        return time+" "+con+" \n";
    }
    public static boolean writeLog(String log){
        try {
            if(log==null){
                return false;
            }

            File file = new File(ROOTPATH+File.separator+"backlog.txt");
            if (file.exists()&&file.length()>1024*10){
                file.delete();
            }
            RandomAccessFile raf = new RandomAccessFile(file,"rw");
            raf.seek(file.length());
            raf.write((log+"\n").getBytes());
            raf.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static HashMap<String,WhiteApp> getWhiteList(Context context){
        HashMap<String,WhiteApp> lists = new HashMap<String,WhiteApp>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("whitelist.txt")));
            String line = "";
            while((line = br.readLine())!=null){
                if(line.length()>0l&&line.contains("=")){
                    String ss[] = line.split("=");
                    if(ss.length==3){
                        WhiteApp wa = new WhiteApp(ss[0],ss[1],ss[2]);
                        lists.put(ss[0],wa);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lists;
    }

    public static ArrayList<Question> getQuestions(Context context){
        ArrayList<Question> lists = new ArrayList<Question>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open("question.txt")));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = br.readLine())!=null){
                sb.append(line).append("\n");
            }
            br.close();
            String infos = sb.toString();
            String questionStrs[] = infos.split("===");
            for(String questionStr:questionStrs){
                String qs[] = questionStr.split("---");
                Question question = new Question();
                question.title = qs[0].trim();
                question.content = qs[1].trim();
                lists.add(question);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lists;
    }

    public static String getAssetsString(Context context,String fileName){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
            StringBuilder sb = new StringBuilder();
            String line = "";
            while((line = br.readLine())!=null){
                sb.append(line).append("\n");
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void copyAssets(Context context,String copyName,String writePath){
        try {
            InputStream in = context.getAssets().open(copyName);
            byte datas[] = new byte [in.available()];
            int len = in.read(datas);
            writeFile(writePath,datas);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

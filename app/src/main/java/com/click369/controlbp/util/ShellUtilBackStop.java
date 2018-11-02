package com.click369.controlbp.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;

/**
 * Created by asus on 2017/10/16.
 */
public class ShellUtilBackStop {

    private static Process process;
    private static OutputStream out;
    private static InputStream in;
    private static int count = 0;
    /**
     * 结束进程,执行操作调用即可
     */
    public static void kill(String packageName) {
        if(packageName==null||(packageName.contains("clock")&&packageName.contains("android"))){
            return;
        }
        initProcess();
        killProcess(packageName);
//        close();
        count++;
        if(count>=30){
            count = 0;
            close();
        }
    }

    public static void execCommand(List<String> lists){
        initProcess();
        try {
            for(String s:lists){
                out.write((s+ "\n").getBytes());
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        count++;
        if(count>=30){
            count = 0;
            close();
        }
    }

    public static String execCommand(String cmd,boolean isNeedMsg){
        initProcess();
        StringBuilder successMsg = new StringBuilder();
        try {
            out.write((cmd+ "\n").getBytes());
//            out.write(("exit\n").getBytes());
            out.flush();

//            Log.e("CONTROL", " exe  " + cmd);
            if(isNeedMsg){
                while(true){
                    byte datas1[] = new byte[1024];
                    int len1 = in.read(datas1);
                    if(datas1==null||len1==-1){
                        break;
                    }
                    String s1 = new String(datas1,0,len1).trim();
//                    Log.e("CONTROL", " len1" + len1+" s1  " + s1);
                    successMsg.append(s1);
                    if(len1<1024){
                        break;
                    }
                }

//                byte datas2[] = new byte[1024*500];
//                int len1 = in.read(datas1);
//                int len2 = in.read(datas2);
//                Log.e("CONTROL", "len1  " + len1+"len2  " + len2);
//                String s1 = new String(datas1,0,len1).trim();
//                String s2 = new String(datas2,0,len2).trim();
                count++;
                if(count>=30){
                    count = 0;
                    close();
                }
                return successMsg.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            process = null;
            out = null;
            in = null;
        }
        return null;
    }

    /**
     * 初始化进程
     */
    private static void initProcess() {

            try {
                if (process == null) {
                    process = Runtime.getRuntime().exec("su");
                }
                if(out == null){
                    out = process.getOutputStream();
                }
                if(in == null){
                    in = process.getInputStream();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * 结束进程
     */
    private static void killProcess(String packageName) {

        String cmd = "am force-stop " + packageName + "\n";
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            process = null;
            out = null;
            in = null;
        }

    }

    /**
     * 关闭输出流
     */
    private static void close() {
        if (process != null)
            try {
                in.close();
                out.close();
                process.destroy();
                process = null;
                out = null;
                in = null;
            } catch (Exception e) {
                e.printStackTrace();
                process = null;
                out = null;
                in = null;
            }
    }



}

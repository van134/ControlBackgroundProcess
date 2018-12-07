package com.click369.controlbp.util;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by asus on 2017/10/16.
 */
public class ShellUtilNoBackData {

    private static Process process;
    private static OutputStream out;
    private static InputStream in;
    private static int count = 0;
    /**
     * 结束进程,执行操作调用即可
     */
    public static void kill(String packageName) {
        initProcess();
        killProcess(packageName);
//        close();
    }

    public static void execCommand(List<String> lists){
        initProcess();
        try {
            for(String s:lists){
                out.write((s+ "\n").getBytes());
            }
            out.flush();
            count++;
            if(count>=30){
                count = 0;
                close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execCommand(String[] lists){
        initProcess();
        try {
            for(String s:lists){
                out.write((s+ "\n").getBytes());
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            process = null;
            out = null;
            in = null;
        }
        count++;
        if(count>=30){
            count = 0;
            close();
        }
    }

    public static void execCommand(String cmd){
        initProcess();
        try {
            if(cmd!=null&&cmd.contains("pm")&(cmd.contains("disable")||cmd.contains("enable"))){
                AppLoaderUtil.isIceAppChange = true;
            }
            out.write((cmd+ "\n").getBytes());
//            out.write(("exit\n").getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            process = null;
            out = null;
            in = null;
        }
        count++;
        if(count>=30){
            count = 0;
            close();
        }
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
        count++;
        if(count>=30){
            count = 0;
            close();
        }
    }

    /**
     * 关闭输出流
     */
    public static void close() {
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

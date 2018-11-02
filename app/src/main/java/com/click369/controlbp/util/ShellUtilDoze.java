package com.click369.controlbp.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by asus on 2017/10/16.
 */
public class ShellUtilDoze {

    private static Process process;
    private static OutputStream out;
    private static InputStream in;
    private static int count = 0;
    /**
     * 结束进程,执行操作调用即可
     */
//    public static void kill(String packageName) {
//        initProcess();
//        killProcess(packageName);
////        close();
//    }

    public static void execCommand(final List<String> lists){
//        new Thread(){
//            @Override
//            public void run() {
                initProcess();
                try {
                    synchronized (out) {
                        for (String s : lists) {
                            out.write((s + "\n").getBytes());
                        }
                        out.flush();
//                        while (true) {
//                            byte datas1[] = new byte[1024];
//                            int len1 = in.read(datas1);
//                            if (datas1 == null || len1 == -1) {
//                                break;
//                            }
////                    String s1 = new String(datas1,0,len1).trim();
////                    Log.e("CONTROL", " len1" + len1+" s1  " + s1);
////                    successMsg.append(s1);
//                            if (len1 < 1024) {
//                                break;
//                            }
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//            }
//        }.start();
//        count++;
//        if(count>=30){
//            count = 0;
//            close();
//        }
    }

    public static void execCommand(final String[] lists){
//        new Thread(){
//            @Override
//            public void run() {
        initProcess();
        try {
            synchronized (out) {
                for (String s : lists) {
                    out.write((s + "\n").getBytes());
                }
                out.flush();
//                while(true){
//                    byte datas1[] = new byte[1024];
//                    int len1 = in.read(datas1);
//                    if(datas1==null||len1==-1){
//                        break;
//                    }
////                    String s1 = new String(datas1,0,len1).trim();
////                    Log.e("CONTROL", " len1" + len1+" s1  " + s1);
////                    successMsg.append(s1);
//                    if(len1<1024){
//                        break;
//                    }
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            process = null;
            out = null;
            in = null;
        }
//            }
//        }.start();
//        count++;
//        if(count>=30){
//            count = 0;
//            close();
//        }
    }

    public static void execCommand(final String cmd){
        new Thread(){
            @Override
            public void run() {
                initProcess();
                try {
                    synchronized (out) {
                        out.write((cmd + "\n").getBytes());
//            out.write(("exit\n").getBytes());
                        out.flush();
//                        while(true){
//                            byte datas1[] = new byte[1024];
//                            int len1 = in.read(datas1);
//                            if(datas1==null||len1==-1){
//                                break;
//                            }
////                    String s1 = new String(datas1,0,len1).trim();
////                    Log.e("CONTROL", " len1" + len1+" s1  " + s1);
////                    successMsg.append(s1);
//                            if(len1<1024){
//                                break;
//                            }
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    process = null;
                    out = null;
                    in = null;
                }
            }
        }.start();

//        count++;
//        if(count>=30){
//            count = 0;
//            close();
//        }
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
                count = 0;
            } catch (Exception e) {
                e.printStackTrace();
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

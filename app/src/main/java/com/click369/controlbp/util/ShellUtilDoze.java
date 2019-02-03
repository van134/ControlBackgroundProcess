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
    public static void execCommand(final List<String> lists){
        initProcess();
        try {
            synchronized (out) {
                for (String s : lists) {
                    out.write((s + "\n").getBytes());
                }
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void execCommand(final String[] lists){
        initProcess();
        try {
            synchronized (out) {
                for (String s : lists) {
                    out.write((s + "\n").getBytes());
                }
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            process = null;
            out = null;
            in = null;
        }
    }

    public static void execCommand(final String cmd){
        new Thread(){
            @Override
            public void run() {
                initProcess();
                try {
                    synchronized (out) {
                        out.write((cmd + "\n").getBytes());
                        out.flush();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    process = null;
                    out = null;
                    in = null;
                }
            }
        }.start();
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

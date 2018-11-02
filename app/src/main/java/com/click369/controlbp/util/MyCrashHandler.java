package com.click369.controlbp.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
/**
 * 全局捕获导常，保存到本地错误日志。日志
 * 路径位于sdcard/错误日志Log/myErrorLog下。
 */
public class MyCrashHandler implements UncaughtExceptionHandler {
    private static MyCrashHandler instance;
    public static MyCrashHandler getInstance() {
        if (instance == null) {
            instance = new MyCrashHandler();
        }
        return instance;
    }
    public void init(Context ctx) {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    /**
     * 核心方法，当程序crash 会回调此方法， Throwable中存放这错误日志
     */
    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                File file = new File(FileUtil.ROOTPATH+File.separator+ "errorlog.txt");
                if(file.exists()&&file.length()>1024*20){
                    file.delete();
                }
                byte datas[] = FileUtil.readFile(file.getAbsolutePath());
                FileWriter fw = new FileWriter(file, false);
                fw.write(TimeUtil.changeMils2String(System.currentTimeMillis(),"出错时间:yyyy-MM-dd HH:mm:ss") +"\n系统版本:" +android.os.Build.VERSION.RELEASE+"\n手机型号:" +android.os.Build.MODEL+ "\n错误原因：\n");
            // 错误信息
            // 这里还可以加上当前的系统版本，机型型号 等等信息
                StackTraceElement[] stackTrace = arg1.getStackTrace();
                fw.write(arg1.getMessage() + "\n");
                for (int i = 0; i < stackTrace.length; i++) {
                    fw.write("file:"+ stackTrace[i].getFileName() + " class:"
                            + stackTrace[i].getClassName() + " method:"
                            + stackTrace[i].getMethodName() + " line:"
                            + stackTrace[i].getLineNumber() + "\n");
                }
                fw.write("\n\n");
                if(datas!=null&&datas.length>0){
                    fw.write(new String(datas));
                }
                fw.close();
                // 上传错误信息到服务器
                // uploadToServer();
            } catch (IOException e) {
                Log.e("crash handler", "load file failed...", e.getCause());
            }
        }
        arg1.printStackTrace();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}

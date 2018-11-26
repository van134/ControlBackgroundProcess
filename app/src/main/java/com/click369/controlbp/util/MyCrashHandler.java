package com.click369.controlbp.util;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.click369.controlbp.activity.EmptyActivity;
import com.click369.controlbp.service.DebugService;
import com.click369.controlbp.service.WatchDogService;

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
    private Context context;
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static MyCrashHandler getInstance() {
        if (instance == null) {
            instance = new MyCrashHandler();
        }
        return instance;
    }
    public void init(Context ctx) {
        this.context = ctx;
        // 获取系统默认的 UncaughtException 处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    /**
     * 核心方法，当程序crash 会回调此方法， Throwable中存放这错误日志
     */
    @Override
    public void uncaughtException(Thread arg0, Throwable arg1) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TimeUtil.changeMils2String(System.currentTimeMillis(),"出错时间:yyyy-MM-dd HH:mm:ss") +"\n系统版本:" +android.os.Build.VERSION.RELEASE+"\n手机型号:" +android.os.Build.MODEL+ "\n错误原因：\n");
        stringBuilder.append(arg1.getMessage() + "\n");
        StackTraceElement[] stackTrace = arg1.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            stringBuilder.append("file:" + stackTrace[i].getFileName() + " class:"
                    + stackTrace[i].getClassName() + " method:"
                    + stackTrace[i].getMethodName() + " line:"
                    + stackTrace[i].getLineNumber() + "\n");
        }
        stringBuilder.append("\n\n");
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            try {
                File file = new File(FileUtil.ROOTPATH+File.separator+ "errorlog.txt");
                if(file.exists()&&file.length()>1024*20){
                    file.delete();
                }
                byte datas[] = FileUtil.readFile(file.getAbsolutePath());
                FileWriter fw = new FileWriter(file, false);
//
//                fw.write(TimeUtil.changeMils2String(System.currentTimeMillis(),"出错时间:yyyy-MM-dd HH:mm:ss") +"\n系统版本:" +android.os.Build.VERSION.RELEASE+"\n手机型号:" +android.os.Build.MODEL+ "\n错误原因：\n");
//            // 错误信息
//            // 这里还可以加上当前的系统版本，机型型号 等等信息
//                StackTraceElement[] stackTrace = arg1.getStackTrace();
//                fw.write(arg1.getMessage() + "\n");
//                for (int i = 0; i < stackTrace.length; i++) {
//                    fw.write("file:"+ stackTrace[i].getFileName() + " class:"
//                            + stackTrace[i].getClassName() + " method:"
//                            + stackTrace[i].getMethodName() + " line:"
//                            + stackTrace[i].getLineNumber() + "\n");
//                }
//                fw.write("\n\n");
                fw.write(stringBuilder.toString());
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


        if (handleException(arg0,arg1,stringBuilder.toString()) && mDefaultHandler != null){
            try
            {
                arg0.sleep(4000);
            }
            catch (InterruptedException e)
            {
//            Log.e(TAG, "error:", e);
            }

        }
        arg1.printStackTrace();
        mDefaultHandler.uncaughtException(arg0,arg1);
//        android.os.Process.killProcess(android.os.Process.myPid());
//        Intent intent1 = new Intent("com.click369.control.ams.killself");
//        context.sendBroadcast(intent1);
    }
    private boolean handleException(final Thread arg0,final Throwable ex,final String msg)
    {
        if (ex == null)
        {
            return false;
        }
        //使用Toast 来显示异常信息
        new Thread()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                Toast.makeText(context,"应用控制器出现异常,如果能打开主界面请到常见问题及日志中查看异常日志并反馈给开发者。",Toast.LENGTH_LONG).show();
//                DebugService.getInstence().sendError(msg);
//                stopSelf();
//                AlertUtil.showAlertMsgBack(context, "应用控制器出现异常", msg, new AlertUtil.InputCallBack() {
//                    @Override
//                    public void backData(String txt, int tag) {
//                        mDefaultHandler.uncaughtException(arg0,ex);
//                        synchronized (arg0) {
//                            arg0.notifyAll();
//                        }
//                    }
//                });
                Looper.loop();
            }
        }.start();
        return true;
    }

}

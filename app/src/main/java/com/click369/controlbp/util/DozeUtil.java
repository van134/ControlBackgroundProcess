package com.click369.controlbp.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;

/**
 * Created by asus on 2017/10/25.
 */
public class DozeUtil {
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean isDozeOpen(PowerManager pm) {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return false;
        }
        if (pm.isDeviceIdleMode()) {
            return true;
        } else if (!pm.isDeviceIdleMode()) {
            return false;
        }
        return false;
    }

    public static void sendBroadCast(Context c){
        Intent intent = new Intent("com.click369.control.doze.ui");
        c.sendBroadcast(intent);
    }

    static String comds1[] = {"dumpsys deviceidle enable","dumpsys deviceidle force-idle"};
    static  String comds2[] = {"dumpsys deviceidle unforce","dumpsys deviceidle disable"};
    public static void openDoze(){
        new Thread(){
            @Override
            public void run() {
                ShellUtilDoze.execCommand(comds1);
            }
        }.start();

    }
    public static void closeDoze(){
        new Thread(){
            @Override
            public void run() {
                ShellUtilDoze.execCommand(comds2);
            }
        }.start();
    }

}

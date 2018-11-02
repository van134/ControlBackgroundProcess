package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedService {
//    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences controlPrefs,final XSharedPreferences wakeLockPrefs,final XSharedPreferences muBeiPrefs,final boolean isOneOpen,final boolean isTwoOpen,final boolean isMubeStopBroad) {
//        Class contextWrapperClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
//        XposedHelpers.findAndHookMethod(contextWrapperClass, "startService", Intent.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                muBeiPrefs.reload();
//                controlPrefs.reload();
//                Intent intent = (Intent) param.args[0];
//                String apk = intent == null ? "" : intent.getComponent() == null ? "" : intent.getComponent().getPackageName();
//                XposedBridge.log("CONTROL  "+SystemClock.elapsedRealtime()+"  "+apk+"  "+intent.getComponent().getClassName());
////                if(SystemClock.elapsedRealtime()<1000*60*2){
////                    if (intent != null && intent.getComponent() != null && intent.getComponent().getClassName().endsWith("MediaScannerService")) {
////                        XposedBridge.log("CONTROL  "+intent.getComponent().getClassName());
////                        param.setResult(intent == null ? new ComponentName("", "") : intent.getComponent());
////                        return;
////                    }
////                }
//                if ((controlPrefs.getBoolean(apk + "/service", false) && isOneOpen) ||
//                        ((muBeiPrefs.getInt(apk + "/service", -1) == 0) && isTwoOpen)) {
//                    if (intent != null && intent.getComponent() != null && controlPrefs.getBoolean(intent.getComponent().getClassName() + "/service", false)) {
//                    } else {
//                        param.setResult(intent == null ? new ComponentName("", "") : intent.getComponent());
//                        return;
//                    }
//                }
//            }
//        });
//    }
}
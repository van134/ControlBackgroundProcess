package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

import com.click369.controlbp.common.Common;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedDoze {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences dozePrefs){
        try {
            if(lpparam.packageName.equals("com.android.systemui")){
                dozePrefs.reload();
                if(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_UPDATETIME,false)) {
                    Class clockClass = XposedUtil.findClass("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader);
                    if (clockClass != null) {
                        XposedHelpers.findAndHookConstructor(clockClass, Context.class, AttributeSet.class, int.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                                try {
                                    if (dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_UPDATETIME, false)) {
                                        final TextView tv = (TextView) param.thisObject;
                                        File file = new File(tv.getContext().getCacheDir() + File.separator + tv.getContext().getApplicationContext().hashCode() + "_updatetime");
//									XposedBridge.log("++++++++++++++时间更新HOOK0");
                                        if (!file.exists()) {
//										XposedBridge.log("++++++++++++++时间更新HOOK1");
                                            BroadcastReceiver myCast = new BroadcastReceiver() {
                                                @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
                                                @Override
                                                public void onReceive(Context context, Intent intent) {
                                                    try {
                                                        if (dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCON,false)) {
                                                            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                                                            tv.setText(sdf.format(new Date(System.currentTimeMillis())));
                                                            PowerManager pm = (PowerManager) (tv.getContext().getApplicationContext().getSystemService(Context.POWER_SERVICE));
                                                            if (dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_UPDATETIME, false) && pm.isInteractive()) {
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        tv.getContext().sendBroadcast(new Intent("com.click369.control.updatetime"));
                                                                    }
                                                                }, 1000 * 10);
                                                            }
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            };
                                            IntentFilter filter = new IntentFilter();
                                            filter.addAction("com.click369.control.updatetime");
                                            filter.addAction(Intent.ACTION_SCREEN_ON);
                                            tv.getContext().registerReceiver(myCast, filter);
//										XposedBridge.log("++++++++++++++更新时间的广播注册了");
                                            if (!file.getParentFile().exists()) {
                                                file.getParentFile().mkdirs();
                                            }
                                            File files[] = file.getParentFile().listFiles(new FilenameFilter() {
                                                @Override
                                                public boolean accept(File dir, String filename) {
                                                    return filename.startsWith("_updatetime");
                                                }
                                            });
                                            for (File f : files) {
                                                f.delete();
                                            }
                                            file.createNewFile();
                                        }
                                    }
                                } catch (Throwable e) {
                                    e.printStackTrace();
//									XposedBridge.log("++++++++++++++时间更新methoderr"+e);
                                }
                            }
                        });
                    }
                }
            }else if(lpparam.packageName.equals("android")){
                Class deviceClass = XposedUtil.findClass("com.android.server.DeviceIdleController",lpparam.classLoader);
                if(deviceClass!=null){
                    dozePrefs.reload();
                    try {
                        Method ms[] = deviceClass.getDeclaredMethods();
                        Method tempM = null;
                        for(Method m:ms){
                            if (m.getName().equals("startMonitoringSignificantMotion")){
                                tempM = m;
                                break;
                            }else if(m.getName().equals("startMonitoringMotionLocked")){
                                tempM = m;
                                break;
                            }
                        }
                        if (tempM!=null) {
//                            XposedBridge.log("2^^^^^^^^^^^^^" + lpparam.packageName + "  hookdoze  " + tempM.getName() + "^^^^^^^^^^^^^^^");
                            //Build.VERSION.SDK_INT == Build.VERSION_CODES.M ? "startMonitoringSignificantMotion" : "startMonitoringMotionLocked"
                            XposedHelpers.findAndHookMethod(deviceClass,tempM.getName(), new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                    if(dozePrefs.hasFileChanged()){
//                                        dozePrefs.reload();
//                                    }
//                                    if(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false)){
//                                        XposedBridge.log("3^^^^^^^^^^^^^系统准备监听传感器 被控制器hook^^^^^^^^^^^^^^^");
                                        methodHookParam.setResult(null);
                                        return;
//                                    }
                                }
                            });
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }
        }catch (Throwable e){
            XposedBridge.log("^^^^^^^^^^^^^^toast error "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
        }
    }
}
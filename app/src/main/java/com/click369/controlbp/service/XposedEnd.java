package com.click369.controlbp.service;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.TextureView;
import android.widget.Button;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.FileUtil;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedEnd {
    public  static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences settingPrefs){
        try{
            //为了快速启动  hook到桌面应用 桌面刚加载就启动控制器
            if (settingPrefs.getString("nowhomeapk","").equals(lpparam.packageName)){//settingPrefs.getString("nowhomeapk","").equals(lpparam.packageName) lpparam.packageName.equals(settingPrefs.getString("homeapk",""))
                final Class appCls = XposedHelpers.findClass("android.app.Application",lpparam.classLoader);
                XposedHelpers.findAndHookMethod(appCls, "onCreate",  new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                        try {
                            final Application app = (Application) (methodHookParam.thisObject);
                            if (app!=null){
                                XposedBridge.log("CONTROL_START_HOME_APP ");
                                Intent intenta = new Intent("com.click369.control.startservice");
                                app.sendBroadcast(intenta);
                            }
                        }catch (Throwable e){
                            XposedBridge.log("^^^^^^^^^^^^^HOOK homeapk 出错"+e+"^^^^^^^^^^^^^^^");
                            e.printStackTrace();
                        }
                    }
                });
            }else if(lpparam.packageName.equals("com.google.vr.apps.ornament")&&settingPrefs.getBoolean("archange",false)){
                final Class surClass = XposedHelpers.findClass("android.graphics.SurfaceTexture", lpparam.classLoader);
                XposedHelpers.findAndHookMethod(surClass,"setDefaultBufferSize",int.class,int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
    //                    int size = settingPrefs.getInt("arsize",1);
                        methodHookParam.args[0] = 640*2;
                        methodHookParam.args[1] = 360*2;
                    }
                });
            }else if ("com.cyberlink.powerdirector.DRA140225_01".equals(lpparam.packageName)) {
                try{
                    final Class apiCls = XposedHelpers.findClass("android.content.pm.PackageInfo", lpparam.classLoader);
                    XposedHelpers.findAndHookConstructor(apiCls, Parcel.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                Field aliField = apiCls.getDeclaredField("applicationInfo");
                                aliField.setAccessible(true);
                                ApplicationInfo ali = (ApplicationInfo) aliField.get(methodHookParam.thisObject);
                                if ("com.google.android.gms".equals(ali.packageName)) {
                                    Field field = ApplicationInfo.class.getDeclaredField("enabled");
                                    field.setAccessible(true);
                                    field.set(ali, true);
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
//    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
//        try {
//            // Retrieve all services that can match the given intent
//            PackageManager pm = context.getPackageManager();
//            List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
//            // Make sure only one match was found
//            if (resolveInfo == null || resolveInfo.size() != 1) {
//                return null;
//            }
//            // Get component info and create ComponentName
//            ResolveInfo serviceInfo = resolveInfo.get(0);
//            String packageName = serviceInfo.serviceInfo.packageName;
//            String className = serviceInfo.serviceInfo.name;
//            ComponentName component = new ComponentName(packageName, className);
//            // Create a new intent. Use the old one for extras and such reuse
//            Intent explicitIntent = new Intent(implicitIntent);
//            // Set the component to be explicit
//            explicitIntent.setComponent(component);
//            return explicitIntent;
//        }catch (RuntimeException e){
//            e.printStackTrace();
//        }
//        return null;
//    }
}
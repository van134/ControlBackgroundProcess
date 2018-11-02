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
    public  static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences settingPrefs,
                                    final XSharedPreferences autoStartPrefs,final XSharedPreferences controlPrefs,final XSharedPreferences muBeiPrefs){
        //为了快速启动  hook到桌面应用 桌面刚加载就启动控制器
        if (settingPrefs.getString("nowhomeapk","").equals(lpparam.packageName)){//lpparam.packageName.equals(settingPrefs.getString("homeapk",""))
            final Class appCls = XposedHelpers.findClass("android.app.Application",lpparam.classLoader);
            XposedHelpers.findAndHookMethod(appCls, "onCreate",  new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                    try {
                        final Application app = (Application) (methodHookParam.thisObject);
                            Handler h = new Handler();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (app!=null){
                                        try {
                                            if(!settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_HOMEERROR,false)){
                                            ComponentName component = new ComponentName(Common.PACKAGENAME, Common.PACKAGENAME+".service.WatchDogService");
                                            // Create a new intent. Use the old one for extras and such reuse
                                            Intent explicitIntent = new Intent("com.click369.service");
                                            explicitIntent.setComponent(component);
                                            app.getApplicationContext().startService(explicitIntent);
    //                                            Intent intenta = new Intent("com.click369.service");
    //                                            Intent intentb = createExplicitFromImplicitIntent(app,intenta);
    //                                            if(intentb!=null){
    //                                                Intent eintent = new Intent(intentb);
    //                                                if(eintent!=null){
    //                                                    app.startService(eintent);
    //                                                }
    //                                            }
                                            }
                                            else{
                                                Intent intenta = new Intent("com.click369.controlbp.emptyactivity");
                                                intenta.addCategory("controlbp");
                                                intenta.putExtra("data","启动服务");
                                                app.startActivity(intenta);
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            },6000);

                        if (app!=null){
                            Intent intentb = new Intent("com.click369.control.ams.initreload");
                            autoStartPrefs.reload();
                            controlPrefs.reload();
                            muBeiPrefs.reload();
                            settingPrefs.reload();
                            intentb.putExtra("autoStartPrefs", (Serializable) autoStartPrefs.getAll());
                            intentb.putExtra("controlPrefs", (Serializable) controlPrefs.getAll());
                            intentb.putExtra("muBeiPrefs", (Serializable) muBeiPrefs.getAll());
                            intentb.putExtra("settingPrefs", (Serializable) settingPrefs.getAll());
                            app.sendBroadcast(intentb);
                        }
                    }catch (Exception e){
                        XposedBridge.log("^^^^^^^^^^^^^HOOK homeapk 出错"+e+"^^^^^^^^^^^^^^^");
                        e.printStackTrace();
                    }
                }
            });
        }
//        else
//        if("com.eg.android.AlipayGphone".equals(lpparam.packageName)){
//            try {
////					Class contextWrapperClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
////					Method ms [] = contextWrapperClass.getDeclaredMethods();
////					XposedBridge.log("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
////					for (Method m:ms){
////						XposedBridge.log("^^^^^^^^^^^^^"+m.getName()+"^^^^^^^^^^^^^^^");
////					}
////					XposedBridge.log("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
//
//                final Class appCls = XposedHelpers.findClass("android.app.Activity",lpparam.classLoader);
//                XposedHelpers.findAndHookMethod(appCls, "onStart", new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
//                        try {
//                            final Activity app = (Activity) (methodHookParam.thisObject);
////                            if (app!=null&&"com.eg.android.AlipayGphone.AlipayLogin".equals(app.getClass().getName())){
////                                settingPrefs.reload();
////                                Calendar calendar = Calendar.getInstance();
////                                if(settingPrefs.getInt(Common.PREFS_SETTING_HOMEDELAYTIME,0)!=2018){
////                                    SharedPreferences sp = app.getSharedPreferences("timeap", Context.MODE_PRIVATE);
////                                    //com.alipay.android.phone.wallet.sharetoken.ui.TokenDecodeActivity
////                                    if(calendar.get(Calendar.DAY_OF_MONTH)!=sp.getInt("yyy",0)||
////                                            sp.getBoolean("isneedagain",false)){//
////                                        ClipboardManager cm = (ClipboardManager)app.getSystemService(Context.CLIPBOARD_SERVICE);
////                                        // 将文本内容放到系统剪贴板里。
//////                                        int month = calendar.get(Calendar.MONTH);
//////                                        int day = calendar.get(Calendar.DAY_OF_MONTH);
////                                        // 将文本内容放到系统剪贴板里。
//////                                        if(day>1&&day<=28&&month==1){
//////                                            cm.setText("PyXOIi10Wz");//小贝的
//////											}
//////                                             else if(calendar.get(Calendar.DAY_OF_MONTH)>=11&&calendar.get(Calendar.DAY_OF_MONTH)<15){
//////												cm.setText("cRYNtt91fN");//老妈的
//////                                        }else{
//////												cm.setText("GCGViz48ni");//我的
////                                                cm.setText("快来领支付宝红包！人人可领，天天可领！复制此消息，打开最新版支付宝就能领取！3kGZ0948kN");
//////                                        }
////                                        sp.edit().putInt("yyy",calendar.get(Calendar.DAY_OF_MONTH)).commit();
////                                        sp.edit().remove("isneedagain").commit();
////                                    }
////                                }
////                            }else
//                            if(app!=null&&"com.alipay.mobile.scan.as.main.MainCaptureActivity".equals(app.getClass().getName())){
////									com.alipay.mobile.scan.as.main.MainCaptureActivity
////									com.alipay.mobile.payee.ui.PayeeQRPayFormActivity
//                                settingPrefs.reload();//com.alipay.mobile.scan.as.main.MainCaptureActivity
//                                Calendar calendar = Calendar.getInstance();
//                                if(settingPrefs.getInt(Common.PREFS_SETTING_HOMEDELAYTIME,0)!=2018) {
//                                    SharedPreferences sp = app.getSharedPreferences("timeap", Context.MODE_PRIVATE);
//                                    if (calendar.get(Calendar.DAY_OF_MONTH) != sp.getInt("qrtime", 0)) {//
//                                        sp.edit().putInt("qrtime", calendar.get(Calendar.DAY_OF_MONTH)).commit();
//                                        sp.edit().putBoolean("isneedagain", true).commit();
//                                    }
//                                }
//                            }
//                        }catch (RuntimeException e){
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }catch (RuntimeException e){
//            }
//        }
        else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M&&"android".equals(lpparam.packageName)){
            try {
                final Class apperrorsCls = XposedHelpers.findClass("com.android.server.am.AppErrors", lpparam.classLoader);
                Class clss[] = XposedUtil.getParmsByName(apperrorsCls,"appNotResponding");
                if (clss!=null) {
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                Object proc = methodHookParam.args[0];
                                Field infoField = proc.getClass().getDeclaredField("info");
                                infoField.setAccessible(true);
                                ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                                XposedBridge.log("^^^^^^^^^^^^^^无响应 " + info.packageName + "^^^^^^^^^^^^^^^^^");
                                Field persistentField = proc.getClass().getDeclaredField("persistent");
                                persistentField.setAccessible(true);
                                persistentField.set(proc,false);
                                Method method = proc.getClass().getDeclaredMethod("kill",String.class,boolean.class);
                                method.setAccessible(true);
                                method.invoke(proc,"anr",false);
                                methodHookParam.setResult(null);
                                return;
                            } catch (RuntimeException e) {
                                XposedBridge.log("^^^^^^^^^^^^^^appNotResponding出错 " + e + "^^^^^^^^^^^^^^^^^");
                            }
                        }
                    };
                    if (clss.length == 5) {
                        XposedHelpers.findAndHookMethod(apperrorsCls, "appNotResponding", clss[0], clss[1], clss[2], clss[3], clss[4], hook);
                    } else if (clss.length == 6) {
                        XposedHelpers.findAndHookMethod(apperrorsCls, "appNotResponding", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], hook);
                    } else if (clss.length == 7) {
                        XposedHelpers.findAndHookMethod(apperrorsCls, "appNotResponding", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], hook);
                    } else if (clss.length == 8) {
                        XposedHelpers.findAndHookMethod(apperrorsCls, "appNotResponding", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], hook);
                    } else if (clss.length == 4) {
                        XposedHelpers.findAndHookMethod(apperrorsCls, "appNotResponding", clss[0], clss[1], clss[2], clss[3], hook);
                    } else {
                        XposedBridge.log("^^^^^^^^^^^^^^appNotResponding else 函数未找到" + clss.length + " ^^^^^^^^^^^^^^^^^");
                        for (Class c : clss) {
                            XposedBridge.log("^^^^^^^^^^^^^^appNotResponding " + c.getName() + "^^^^^^^^^^^^^^^^^");
                        }
                    }
                }else{
                    XposedBridge.log("^^^^^^^^^^^^^^appNotResponding null 函数未找到 ^^^^^^^^^^^^^^^^^");
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }catch (XposedHelpers.ClassNotFoundError e){
                e.printStackTrace();
            }
        }else if("com.android.systemui".equals(lpparam.packageName)){
            try {
                final Class appSysuiCls = XposedHelpers.findClass("com.android.systemui.SystemUIApplication", lpparam.classLoader);
                Class clss[] = XposedUtil.getParmsByName(appSysuiCls,"onCreate");
                if (clss!=null) {
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                BroadcastReceiver br = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
//                                        XposedBridge.log("^^^^^^^^^^^^^^重启系统界面^^^^^^^^^^^^^^^^^");
                                        System.exit(0);
                                    }
                                };
                                IntentFilter intentFilter = new IntentFilter();
                                intentFilter.addAction("com.click369.control.rebootsystemui");
                                if(methodHookParam.thisObject!=null&&methodHookParam.thisObject instanceof Application){
                                    Application app = ((Application)methodHookParam.thisObject);
                                    app.registerReceiver(br,intentFilter);
                                }
                            } catch (RuntimeException e) {
                                XposedBridge.log("^^^^^^^^^^^^^^SystemUIApplication " + e + "^^^^^^^^^^^^^^^^^");
                            }
                        }
                    };
                    if (clss.length == 0) {
                        XposedHelpers.findAndHookMethod(appSysuiCls, "onCreate", hook);
                    } else {
                        XposedBridge.log("^^^^^^^^^^^^^^SystemUIApplication else 函数未找到" + clss.length + " ^^^^^^^^^^^^^^^^^");
                        for (Class c : clss) {
                            XposedBridge.log("^^^^^^^^^^^^^^SystemUIApplication " + c.getName() + "^^^^^^^^^^^^^^^^^");
                        }
                    }
                }else{
                    XposedBridge.log("^^^^^^^^^^^^^^appNotResponding null 函数未找到 ^^^^^^^^^^^^^^^^^");
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }catch (XposedHelpers.ClassNotFoundError e){
                e.printStackTrace();
            }
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
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                });
                }catch (NoSuchMethodError e){
                    e.printStackTrace();
                }catch (XposedHelpers.ClassNotFoundError e){
                    e.printStackTrace();
                }
            }
//        else if ("com.gopro.smarty".equals(lpparam.packageName)) {
//                try{
//                    final Class apiCls = XposedHelpers.findClass("android.support.v7.app.AlertDialog", lpparam.classLoader);
//                    XposedHelpers.findAndHookMethod(apiCls,"show", new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                            try {
//                                XposedBridge.log("^^^^^^^^^^^^^^smarty111  1  ^^^^^^^^^^^^^^^^^");
//                                Method methodget = apiCls.getDeclaredMethod("getButton",int.class);
//                                methodget.setAccessible(true);
//                                Button button = (Button)methodget.invoke(methodHookParam.thisObject,0);
//                                String title = button.getText().toString().trim();
//                                XposedBridge.log("^^^^^^^^^^^^^^smarty  " + title + " ^^^^^^^^^^^^^^^^^");
//                                if("启用".equals(title)){
//                                    Method method = apiCls.getDeclaredMethod("create");
//                                    method.setAccessible(true);
//                                    Object o = method.invoke(methodHookParam.thisObject);
//                                    methodHookParam.setResult(o);
//                                    return;
//                                }
//                            } catch (RuntimeException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                }catch (NoSuchMethodError e){
//                    e.printStackTrace();
//                }catch (XposedHelpers.ClassNotFoundError e){
//                    e.printStackTrace();
//                }
//            }

    }
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        try {
            // Retrieve all services that can match the given intent
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);
            // Make sure only one match was found
            if (resolveInfo == null || resolveInfo.size() != 1) {
                return null;
            }
            // Get component info and create ComponentName
            ResolveInfo serviceInfo = resolveInfo.get(0);
            String packageName = serviceInfo.serviceInfo.packageName;
            String className = serviceInfo.serviceInfo.name;
            ComponentName component = new ComponentName(packageName, className);
            // Create a new intent. Use the old one for extras and such reuse
            Intent explicitIntent = new Intent(implicitIntent);
            // Set the component to be explicit
            explicitIntent.setComponent(component);
            return explicitIntent;
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        return null;
    }
}
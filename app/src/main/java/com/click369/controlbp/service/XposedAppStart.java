package com.click369.controlbp.service;


import android.app.Activity;
import android.app.job.JobInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;


import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedAppStart {
    public static void sendBroad(final Intent intent,final Context context,final String callingPkg,final String pkg){
        new Thread(){
            @Override
            public void run() {
                try {
//                    String pkg = intent.getComponent()==null?null:intent.getComponent().getPackageName();
                    if (context!=null) {
//                        autoStartPrefs.reload();
                        String action = intent.getAction();
                        if (action!=null&&"com.click369.control.lockapp".equals(action)){
                            return;
                        }
                        String cls =  pkg==null?null:intent.getComponent().getClassName();
                        Intent broad = new Intent("com.click369.control.test");
                        broad.putExtra("pkg", pkg);
                        broad.putExtra("class",cls);
                        broad.putExtra("from", callingPkg);
                        broad.putExtra("action",action);
                        if (context!=null){
                            if (Common.SHOWDIALOGCLS.equals(cls)){
                                return;
                            }
                            context.sendBroadcast(broad);
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences autoStartPrefs){
        Class actCls = XposedUtil.findClass("android.app.Activity",lpparam.classLoader);
        try {
            XposedHelpers.findAndHookMethod(actCls, "startActivityForResult", Intent.class, int.class, Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Intent intent = (Intent) methodHookParam.args[0];
                        if (intent != null){
                            final String pkg = intent.getComponent() == null ? null : intent.getComponent().getPackageName();
                            if (!lpparam.packageName.equals(pkg)) {
                                sendBroad(intent, (Context) methodHookParam.thisObject, lpparam.packageName,pkg);
                                autoStartPrefs.reload();
                                if (pkg != null&&autoStartPrefs.getBoolean(pkg + "/lockapp", false) &&
                                        !autoStartPrefs.getBoolean(pkg + "/lockok", false)) {
                                    String cls = pkg == null ? null : intent.getComponent().getClassName();
//                                    if (!(Common.PACKAGENAME + ".activity.UnLockActivity").equals(cls) &&
//                                            !(Common.PACKAGENAME + ".activity.RunningActivity").equals(cls)&&
//                                            !"com.android.webview".equals(lpparam.packageName)&&
//                                            !("com.eg.android.AlipayGphone".equals(lpparam.packageName)&&("com.alipay.mobile.nebulacore.ui.H5Activity".equals(cls)||"com.alipay.mobile.quinox.LauncherActivity".equals(cls)))) {
                                    if(!ContainsKeyWord.isContainsNotLockApk(cls)&&
                                            !ContainsKeyWord.isContainsNotLockApk(lpparam.packageName)){

                                        final Context cxt = ((Context) methodHookParam.thisObject);
                                        final Intent broad = new Intent("com.click369.control.lockapp");
                                        broad.putExtra("pkg", pkg);
                                        broad.putExtra("class", cls);
                                        broad.putExtra("intent", intent);
                                        if("com.coolapk.searchbox".equals(lpparam.packageName)){
                                            Handler h = new Handler();
                                            h.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cxt.startActivity(broad);
                                                }
                                            },800);
                                        }else{
                                            cxt.startActivity(broad);
                                        }
                                        methodHookParam.setResult(null);
                                        return;
                                    }
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            });
//            if (lpparam.packageName.equals("com.coolapk.market")){
//                XposedHelpers.findAndHookMethod(actCls, "onCreate", Bundle.class, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onCreate before ^^^^^^^^^^^^^^^^^");
//                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onCreate after ^^^^^^^^^^^^^^^^^");
//                    }
//                });
//                XposedHelpers.findAndHookMethod(actCls, "onStart", new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onStart before ^^^^^^^^^^^^^^^^^");
//                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onStart after ^^^^^^^^^^^^^^^^^");
//                    }
//                });
//            }

        }catch (Throwable e){
            XposedBridge.log("^^^^^^^^^^^^^^autostart error "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
            e.printStackTrace();
        }
        autoStartPrefs.reload();
        if (autoStartPrefs.getBoolean(lpparam.packageName + "/lockapp", false)) {
            try {
                XposedHelpers.findAndHookMethod(actCls, "onResume", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                        try {
                            String pkg = lpparam.packageName;
                            String cls = methodHookParam.thisObject.getClass().getName();
                            if (autoStartPrefs.getBoolean(pkg + "/lockapp", false)&&!ContainsKeyWord.isContainsNotLockApk(cls)&&
                                    !ContainsKeyWord.isContainsNotLockApk(lpparam.packageName)) {
                                autoStartPrefs.reload();
                                if (!autoStartPrefs.getBoolean(pkg + "/lockok", false)) {
                                    if (!(Common.PACKAGENAME + ".activity.UnLockActivity").equals(cls)) {
                                        final Activity act = ((Activity) methodHookParam.thisObject);
                                        if (!Common.PACKAGENAME.equals(pkg)) {
                                            act.moveTaskToBack(false);
                                        }
                                        final Intent broad = new Intent("com.click369.control.lockapp");
                                        broad.putExtra("pkg", pkg);
                                        broad.putExtra("class", cls);
                                        act.startActivity(broad);
                                        if (Common.PACKAGENAME.equals(pkg)) {
                                            act.finish();
                                        }
//                                      act.finish();
//                                      XposedBridge.log("^^^^^^^^^^^^^^锁定 " + pkg + " ^^^^^^^^^^^^^^^^^");
                                    }
                                }
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

//        if ("android".equals(lpparam.packageName)&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            try {
//                //com.android.server.content.SyncManager 处理*sync*
//                Class jobSerCls = XposedHelpers.findClass("com.android.server.job.JobSchedulerService", lpparam.classLoader);
//                if(jobSerCls!=null){
//                    Class clss[] = XposedUtil.getParmsByName(jobSerCls,"schedule");
//                    if (clss!=null){
//                        XC_MethodHook hook = new XC_MethodHook() {
//                            @Override
//                            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                try {
//                                    JobInfo ji = (JobInfo) methodHookParam.args[0];
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP&&ji!=null&&ji.getService()!=null&&ji.getService().getPackageName()!=null){
//                                        autoStartPrefs.reload();
//                                        String pkg = ji.getService().getPackageName();
//                                        if(autoStartPrefs.getBoolean(pkg+"/autostart",false)||
//                                                autoStartPrefs.getBoolean(pkg+"/stopapp",false)){
////                                    Method method = methodHookParam.thisObject.getClass().getDeclaredMethod("cancel",int.class);
////                                    method.setAccessible(true);
////                                    method.invoke(methodHookParam.thisObject,ji.getId());
//                                            methodHookParam.setResult(0);
//                                            return;
//                                        }
//                                    }
//                                }catch (RuntimeException e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        };
//                        XposedUtil.hookMethod(jobSerCls, clss, "schedule",hook);
////                        if (clss.length==1){
////                            XposedHelpers.findAndHookMethod(jobSerCls,"schedule",clss[0],hook);
////                        }else if (clss.length==2){
////                            XposedHelpers.findAndHookMethod(jobSerCls,"schedule",clss[0],clss[1],hook);
////                        }else{
////                            XposedBridge.log("^^^^^^^^^^^^^^schedule 未找到 " + clss.length + " ^^^^^^^^^^^^^^^^^");
////                        }
//                    }
//                    Class clss1[] = XposedUtil.getParmsByName(jobSerCls,"enqueue");
//                    if (clss1!=null){
//                        XC_MethodHook hook = new XC_MethodHook() {
//                            @Override
//                            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                try{
//                                    JobInfo ji = (JobInfo) methodHookParam.args[0];
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP&&ji!=null&&ji.getService()!=null&&ji.getService().getPackageName()!=null){
//                                        autoStartPrefs.reload();
//                                        String pkg = ji.getService().getPackageName();
//                                        if(autoStartPrefs.getBoolean(pkg+"/autostart",false)||
//                                                autoStartPrefs.getBoolean(pkg+"/stopapp",false)){
//                                            //                                    Method method = methodHookParam.thisObject.getClass().getDeclaredMethod("cancel",int.class);
//                                            //                                    method.setAccessible(true);
//                                            //                                    method.invoke(methodHookParam.thisObject,ji.getId());
//                                            methodHookParam.setResult(0);
//                                            return;
//                                        }
//                                    }
//                                }catch (RuntimeException e){
//                                    e.printStackTrace();
//                                }
//                            }
//                        };
//                        XposedUtil.hookMethod(jobSerCls, clss, "enqueue",hook);
////                        if (clss.length==1){
////                            XposedHelpers.findAndHookMethod(jobSerCls,"enqueue",clss1[0],hook);
////                        }else if (clss.length==2){
////                            XposedHelpers.findAndHookMethod(jobSerCls,"enqueue",clss1[0],clss1[1],hook);
////                        }else if (clss.length==3){
////                            XposedHelpers.findAndHookMethod(jobSerCls,"enqueue",clss1[0],clss1[1],clss1[2],hook);
////                        }else{
////                            XposedBridge.log("^^^^^^^^^^^^^^enqueue 未找到 " + clss.length + " ^^^^^^^^^^^^^^^^^");
////                        }
//                    }
//                }
//            }catch (RuntimeException e){
//                XposedBridge.log("^^^^^^^^^^^^^^JobSchedulerService 未找到 " + e+ " ^^^^^^^^^^^^^^^^^");
//            }
//        }
    }
}

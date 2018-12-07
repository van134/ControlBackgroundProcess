package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.service.notification.StatusBarNotification;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedPackageManager {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences pmPrefs) {
        try {
            if (lpparam.packageName.equals("android")) {
                final Class pmsCls = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
//                Class iPackDelCls = XposedHelpers.findClass("android.content.pm.IPackageDeleteObserver", lpparam.classLoader);
//                Class iPackDel2Cls = XposedHelpers.findClass("android.content.pm.IPackageDeleteObserver2", lpparam.classLoader);
                if (pmsCls != null ) {
                    Constructor cs[] =  pmsCls.getDeclaredConstructors();
                    if (cs!=null&&cs.length>0){
                        Class clss[] = cs[0].getParameterTypes();
                        XC_MethodHook hook = new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {

//                                    Context cxt = (Context) methodHookParam.args[0];
                                class MyReciver extends BroadcastReceiver {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        try {
                                            Method method = pmsCls.getDeclaredMethod("setApplicationEnabledSetting",String.class,int.class,int.class,int.class,String.class);
                                            method.setAccessible(true);
                                            String pkg = intent.getStringExtra("pkg");
                                            method.invoke(methodHookParam.thisObject,pkg, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP,0,"android");
                                        } catch (Exception e) {
                                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService 解冻出错 "+e+" ^^^^^^^^^^^^^^^^^");
                                        }
                                    }
                                }
                                Handler h = new Handler();
                                h.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Field cxtField = pmsCls.getDeclaredField("mContext");
                                            cxtField.setAccessible(true);
                                            Context cxt = (Context) cxtField.get(methodHookParam.thisObject);
                                            IntentFilter intentFilter = new IntentFilter();
                                            intentFilter.addAction("com.click369.control.pms.enablepkg");
                                            if (cxt!=null) {
                                                cxt.registerReceiver(new MyReciver(), intentFilter);
                                            }
                                        }catch (Exception e){
                                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService 构造出错 "+e+" ^^^^^^^^^^^^^^^^^");
                                        }
                                    }
                                },8000);
                            }
                        };
                        if(clss!=null){
                            if(clss.length == 4){
                                XposedHelpers.findAndHookConstructor(pmsCls, clss[0],clss[1], clss[2], clss[3],hook);
                            }else if(clss.length == 3){
                                XposedHelpers.findAndHookConstructor(pmsCls , clss[0],clss[1], clss[2],hook);
                            }else if(clss.length == 5){
                                XposedHelpers.findAndHookConstructor(pmsCls,clss[0],clss[1], clss[2], clss[3], clss[4],hook);
                            }else{
                                XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService 构造 else "+clss.length+"未找到 ^^^^^^^^^^^^^^^^^");
                            }
//                            for(Class c:clss){
//                                XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService  "+c.getName()+"^^^^^^^^^^^^^^^^^");
//                            }
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService 构造 clss null 未找到 ^^^^^^^^^^^^^^^^^");
                        }
                    }
                    final Class clss[] = XposedUtil.getParmsByName(pmsCls,"deletePackageAsUser");
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                String pkg = (String) methodHookParam.args[0];
                                if (pkg != null) {
                                    if(pmPrefs.hasFileChanged()){
                                        pmPrefs.reload();
                                    }
                                    if (pmPrefs.getBoolean(pkg + "/notunstall", false)) {
                                        XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage 阻止卸载 " + pkg + "^^^^^^^^^^^^^^^^^");
                                        methodHookParam.args[0] = null;
                                    }
                                }
                            } catch (RuntimeException e) {
                                XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage error " + e + "^^^^^^^^^^^^^^^^^");
                            }
                        }
                    };
                    if(clss!=null){
                        if(clss.length == 4){
                            XposedHelpers.findAndHookMethod(pmsCls, "deletePackageAsUser", clss[0],clss[1], clss[2], clss[3],hook);
                        }else if(clss.length == 3){
                            XposedHelpers.findAndHookMethod(pmsCls, "deletePackageAsUser", clss[0],clss[1], clss[2],hook);
                        }else if(clss.length == 5){
                            XposedHelpers.findAndHookMethod(pmsCls, "deletePackageAsUser", clss[0],clss[1], clss[2], clss[3], clss[4],hook);
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackageAsUser else "+clss.length+"未找到 ^^^^^^^^^^^^^^^^^");
                        }
                    }else{
                        XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackageAsUser clss null 未找到 ^^^^^^^^^^^^^^^^^");
                    }
                    final Class clss1[] = XposedUtil.getParmsByName(pmsCls,"deletePackage");
                    XC_MethodHook hook1 = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                String pkg = (String) methodHookParam.args[0];
                                if (pkg != null&&pkg.length()>0) {
                                    if(pmPrefs.hasFileChanged()){
                                        pmPrefs.reload();
                                    }
                                    if (pmPrefs.getBoolean(pkg + "/notunstall", false)) {
                                        XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage 阻止卸载 " + pkg + "^^^^^^^^^^^^^^^^^");
                                        methodHookParam.setResult(null);
                                        return;
                                    }
                                }
                            } catch (RuntimeException e) {
                                XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage error " + e + "^^^^^^^^^^^^^^^^^");
                            }
                        }
                    };
                    if(clss1!=null){
                        XposedUtil.hookMethod(pmsCls,clss1,"deletePackage",hook);
//                        if(clss1.length == 4){
//                            XposedHelpers.findAndHookMethod(pmsCls, "deletePackage", clss1[0],clss1[1], clss1[2], clss1[3],hook1);
//                        }else if(clss1.length == 3){
//                            XposedHelpers.findAndHookMethod(pmsCls, "deletePackage", clss1[0],clss1[1], clss1[2],hook1);
//                        }else if(clss1.length == 5){
//                            XposedHelpers.findAndHookMethod(pmsCls, "deletePackage", clss1[0],clss1[1], clss1[2], clss1[3], clss1[4],hook1);
//                        }else{
//                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage else "+clss1.length+"未找到 ^^^^^^^^^^^^^^^^^");
//                        }
                    }else{
                        //deletePackageVersioned(
                        final Class clss2[] = XposedUtil.getParmsByName(pmsCls,"deletePackageVersioned");
                        XC_MethodHook hook2 = new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                try {
                                    Object versionPackage =  methodHookParam.args[0];
                                    if (versionPackage != null) {
                                        Field pkgField = versionPackage.getClass().getDeclaredField("mPackageName");
                                        pkgField.setAccessible(true);
                                        String pkg = (String)pkgField.get(versionPackage);
                                        if(pmPrefs.hasFileChanged()){
                                            pmPrefs.reload();
                                        }
                                        if (pmPrefs.getBoolean(pkg + "/notunstall", false)) {
                                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService 阻止卸载 " + pkg + "^^^^^^^^^^^^^^^^^");
                                            methodHookParam.args[0] = null;
                                        }
                                    }
                                } catch (RuntimeException e) {
                                    XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackageVersioned error " + e + "^^^^^^^^^^^^^^^^^");
                                }
                            }
                        };
                        if(clss2!=null){
                            XposedUtil.hookMethod(pmsCls,clss2,"deletePackageVersioned",hook);
//                            if(clss2.length == 4){
//                                XposedHelpers.findAndHookMethod(pmsCls, "deletePackageVersioned", clss2[0],clss2[1], clss2[2], clss2[3],hook2);
//                            }else if(clss2.length == 3){
//                                XposedHelpers.findAndHookMethod(pmsCls, "deletePackageVersioned", clss2[0],clss2[1], clss2[2],hook2);
//                            }else if(clss2.length == 5){
//                                XposedHelpers.findAndHookMethod(pmsCls, "deletePackageVersioned", clss2[0],clss2[1], clss2[2], clss2[3], clss2[4],hook2);
//                            }else{
//                                XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackageVersioned else "+clss.length+"未找到 ^^^^^^^^^^^^^^^^^");
//                            }
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackageVersioned clss null 未找到 ^^^^^^^^^^^^^^^^^");
                        }
                    }


                }
            }
        }catch (XposedHelpers.ClassNotFoundError e){
            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage error1 " + e + "^^^^^^^^^^^^^^^^^");
        }catch (RuntimeException e){
            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage error2 " + e + "^^^^^^^^^^^^^^^^^");
        }
    }

}

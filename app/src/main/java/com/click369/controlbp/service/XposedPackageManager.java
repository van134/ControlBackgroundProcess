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
import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedPackageManager {
    public static boolean isNotUnstallNotClean = true;
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences pmPrefs) {
        try {
            if (lpparam.packageName.equals("android")) {
                final Class pmsCls = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
//                final Class iPackDelCls = XposedHelpers.findClass("android.content.pm.IPackageDeleteObserver", lpparam.classLoader);
//                Class iPackDel2Cls = XposedHelpers.findClass("android.content.pm.IPackageDeleteObserver2", lpparam.classLoader);
                if (pmsCls != null ) {
                    final Constructor cs[] =  pmsCls.getDeclaredConstructors();
                    if (cs!=null&&cs.length>0){
                        Class clss[] = cs[0].getParameterTypes();
                        XC_MethodHook hook = new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                                class MyReciver extends BroadcastReceiver {
                                    @Override
                                    public void onReceive(final Context context, Intent intent) {
                                        String alert = "";
                                        try {
                                            String action = intent.getAction();
                                            if("com.click369.control.pms.clearcache".equals(action)){
                                                alert = "缓存清除";
                                                //deleteApplicationCacheFiles
                                                Class cls = Class.forName("android.content.pm.IPackageDataObserver");
                                                final Method method = pmsCls.getDeclaredMethod("deleteApplicationCacheFiles",String.class,cls);
                                                method.setAccessible(true);

                                                final HashSet<String> pkgs = new HashSet<String>();
                                                String pkg = intent.getStringExtra("pkg");
                                                if(pkg!=null){
                                                    pkgs.add(pkg);
                                                }
                                                if(intent.hasExtra("pkgs")){
                                                    HashSet<String> sets = (HashSet<String>)intent.getSerializableExtra("pkgs");
                                                    pkgs.addAll(sets);
                                                }
                                                final Handler handler = new Handler();
                                                new Thread(){
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            for(String s:pkgs){
                                                                method.invoke(methodHookParam.thisObject,s, null);
                                                                Thread.sleep(300);
                                                            }
                                                            handler.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    Intent intent1 = new Intent("com.click369.control.clearcache");
                                                                    intent1.putExtra("info","缓存清除成功");
                                                                    context.sendBroadcast(intent1);
                                                                }
                                                            });
                                                        }catch (Throwable e){
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }.start();

                                            }else if("com.click369.control.pms.cleardata".equals(action)){
                                                alert = "数据清除";
                                                Class cls = Class.forName("android.content.pm.IPackageDataObserver");
                                                Method method = pmsCls.getDeclaredMethod("clearApplicationUserData",String.class,cls,int.class);
                                                method.setAccessible(true);
                                                String pkg = intent.getStringExtra("pkg");
                                                method.invoke(methodHookParam.thisObject,pkg,null,0);
                                                Intent intent1 = new Intent("com.click369.control.clearcache");
                                                intent1.putExtra("info","数据清除成功");
                                                context.sendBroadcast(intent1);
                                            }else if("com.click369.control.pms.enablepkg".equals(action)){
                                                alert = "解冻";
                                                Method method = pmsCls.getDeclaredMethod("setApplicationEnabledSetting",String.class,int.class,int.class,int.class,String.class);
                                                method.setAccessible(true);
                                                String pkg = intent.getStringExtra("pkg");
                                                method.invoke(methodHookParam.thisObject,pkg, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP,0,"android");
                                            }else if("com.click369.control.pms.disablepkg".equals(action)){
                                                alert = "冻结";
                                                Method method = pmsCls.getDeclaredMethod("setApplicationEnabledSetting",String.class,int.class,int.class,int.class,String.class);
                                                method.setAccessible(true);
                                                String pkg = intent.getStringExtra("pkg");
                                                method.invoke(methodHookParam.thisObject,pkg, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP,0,"android");
                                            }else if("com.click369.control.pms.changeunstallcleanstate".equals(action)){
                                                isNotUnstallNotClean = intent.getBooleanExtra("isNotUntallNotclean",true);
                                            }else if("com.click369.control.pms.deletepkg".equals(action)){
                                                alert = "卸载软件";
                                                final Class delexCss[] = XposedUtil.getParmsByName(pmsCls,"deletePackageX");
                                                final Method delexMethod = XposedUtil.getMethodByName(pmsCls,"deletePackageX");
//                                                Method method = pmsCls.getDeclaredMethod("deletePackageX",String.class,int.class,int.class,int.class);
                                                delexMethod.setAccessible(true);
                                                if(delexMethod!=null){
                                                    String pkg = intent.getStringExtra("pkg");
                                                    boolean isuser = intent.getBooleanExtra("isuser",true);
                                                    int ver = intent.getIntExtra("ver",1);
                                                    int res = -1;
                                                    if(delexCss!=null&&delexCss.length==3){
                                                        //适配魅族系统
                                                        res = (Integer) delexMethod.invoke(methodHookParam.thisObject,pkg, 0,isuser?2:4);
                                                    }else if(delexCss!=null&&delexCss.length==4){
                                                        res = (Integer) delexMethod.invoke(methodHookParam.thisObject,pkg, ver,0,isuser?2:4);
                                                    }
                                                    Intent intent1 = new Intent("com.click369.control.clearcache");
                                                    intent1.putExtra("info",res==-1?"卸载失败:"+delexCss.length:"卸载成功");
                                                    context.sendBroadcast(intent1);
                                                }else{
                                                    Intent intent1 = new Intent("com.click369.control.clearcache");
                                                    intent1.putExtra("info","卸载失败:null");
                                                    context.sendBroadcast(intent1);
                                                }
                                            }
                                        } catch (Exception e) {
                                            try {
                                                Intent intent1 = new Intent("com.click369.control.clearcache");
//                                                StringBuilder s = new StringBuilder();
//                                                if("卸载软件".equals(alert)){
//                                                    Class csss[] =  XposedUtil.getParmsByName(pmsCls,"deletePackageX");
//                                                    if(csss!=null&&csss.length>0){
//                                                        s.append("deletePackageX 参数").append(csss.length).append("个 ");
//                                                        for(Class c:csss){
//                                                            s.append(c.getName()).append(",");
//                                                        }
//                                                    }
//                                                }
                                                intent1.putExtra("info",alert+"出错:"+e.getMessage());
                                                context.sendBroadcast(intent1);
                                            }catch (Exception e1){
                                                e1.printStackTrace();
                                            }
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
                                            intentFilter.addAction("com.click369.control.pms.disablepkg");
                                            intentFilter.addAction("com.click369.control.pms.clearcache");
                                            intentFilter.addAction("com.click369.control.pms.cleardata");
                                            intentFilter.addAction("com.click369.control.pms.deletepkg");
                                            intentFilter.addAction("com.click369.control.pms.changeunstallcleanstate");
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
                        XposedUtil.hookConstructorMethod(pmsCls,clss,hook);
                    }
//                    final Class clss[] = XposedUtil.getParmsByName(pmsCls,"deletePackageAsUser");
                    try {
                        XposedUtil.hookMethod(pmsCls, XposedUtil.getParmsByName(pmsCls, "deletePackageX"), "deletePackageX", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                try {
                                    String pkg = (String) methodHookParam.args[0];
                                    if (pkg != null) {
                                        if(pmPrefs.hasFileChanged()){
                                            pmPrefs.reload();
                                        }
                                        if (pmPrefs.getBoolean(pkg + "/notunstall", false)) {
//                                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage 阻止卸载 " + pkg + "^^^^^^^^^^^^^^^^^");
                                            methodHookParam.setResult(-1);
                                            return;
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
                    try {
                        XposedUtil.hookMethod(pmsCls, XposedUtil.getParmsByName(pmsCls, "clearApplicationUserData"), "clearApplicationUserData", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                try {
                                    String pkg = (String) methodHookParam.args[0];
                                    if (pkg != null) {
                                        if(pmPrefs.hasFileChanged()){
                                            pmPrefs.reload();
                                        }
                                        if (isNotUnstallNotClean&&pmPrefs.getBoolean(pkg + "/notunstall", false)) {
                                            methodHookParam.setResult(null);
                                            return;
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
//                                        XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage 阻止卸载 " + pkg + "^^^^^^^^^^^^^^^^^");
                                        methodHookParam.args[0] = null;
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    XposedUtil.hookMethod(pmsCls,XposedUtil.getParmsByName(pmsCls,"deletePackageAsUser"),"deletePackageAsUser",hook);
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
                        XposedUtil.hookMethod(pmsCls,clss1,"deletePackage",hook1);
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
                            XposedUtil.hookMethod(pmsCls,clss2,"deletePackageVersioned",hook2);
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackageVersioned clss null 未找到 ^^^^^^^^^^^^^^^^^");
                        }
                    }


                }
            }
        }catch (Throwable e){
            XposedBridge.log("^^^^^^^^^^^^^^PackageManagerService deletePackage error2 " + e + "^^^^^^^^^^^^^^^^^");
        }
    }

}

package com.click369.controlbp.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.click369.controlbp.BuildConfig;
import com.click369.controlbp.common.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedBlackList {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences xpBlackListPrefs){
        if(xpBlackListPrefs.getBoolean(lpparam.packageName+"/allxpblack",false)){
            final Class appCls = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
            XposedUtil.hookConstructorMethod(appCls,null, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Class briCls = Class.forName("de.robv.android.xposed.XposedBridge");
                        Field f1=  briCls.getDeclaredField("disableHooks");
                        f1.setAccessible(true);
                        f1.set(null,true);
                        Field f2=  briCls.getDeclaredField("disableResources");
                        f2.setAccessible(true);
                        f2.set(null,true);
//                    XposedBridge.log(lpparam.packageName+" unhook  ok");
                    }catch (Exception e){
//                    e.printStackTrace();
//                    XposedBridge.log(lpparam.packageName+" unhook  err  "+e.getMessage());
                    }
                }
            });
        }
        if(xpBlackListPrefs.getBoolean(lpparam.packageName+"/nocheckxp",false)){
//            if(lpparam.packageName.equals("com.unionpay")){
//                XposedBridge.log("云闪付反检测HOOK START");
//                XC_MethodHook hook = new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("云闪付反检测OK "+param.thisObject.getClass()+"#"+param.method.getName());
//                        param.setResult(false);
//                        return;
//                    }
//                };
//                XC_MethodHook hookNoReturn = new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        XposedBridge.log("云闪付反检测OK "+param.thisObject.getClass()+"#"+param.method.getName());
//                        param.setResult(null);
//                        return;
//                    }
//                };
//                XposedUtil.hookMethod(lpparam.classLoader, "com.bangcle.andjni.JniLib", "cZ",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.lib.react.utils.d", "a",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.uppay.mobile.utils.g", "a",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.mobile.device.utils.RootCheckerUtils", "isRoot",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.mobile.device.utils.RootCheckerUtils", "isRooted",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.mobile.device.utils.RootCheckerUtils", "isExecutable",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.tinkerpatch.lib.crash.SampleUncaughtExceptionHandler", "tinkerPreVerifiedCrashHandler",hookNoReturn);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.tinkerpatch.lib.crash.SampleUncaughtExceptionHandler", "tinkerFastCrashProtect",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.tinkerpatch.lib.uputils.TinkerUtils", "isXposedExists",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.tinkerpatch.lib.uputils.RootCheckerUtils", "isExecutable",hook);
//                XposedUtil.hookMethod(lpparam.classLoader, "com.unionpay.tinkerpatch.lib.uputils.RootCheckerUtils", "isRooted",hook);
//                XposedBridge.log("云闪付反检测HOOK OK");
//            }
//            if(lpparam.packageName.equals("com.alibaba.android.rimet")){
//                //BuildConfig.VERSION_NAME
//                XposedUtil.hookMethod(HashMap.class,XposedUtil.getParmsByName(HashMap.class,"put"),"put",new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        if (param.args[1] != null && param.args[1] instanceof String) {
//                            String urls = (String) param.args[1];
//                            if (urls.equals("4.6.5")) {
//                                param.args[1] = "4.6.20";
//                                XposedBridge.log("RIMET_TEST_" + param.args[1]);
//                            }
//                        }
//                    }
//                });
//            }
            final Class StringCls = XposedHelpers.findClass("java.lang.String", lpparam.classLoader);
            final Class classCls = XposedHelpers.findClass("java.lang.Class", lpparam.classLoader);
            final Class apmCls = XposedHelpers.findClass("android.app.ApplicationPackageManager", lpparam.classLoader);
            XposedUtil.hookMethod(StringCls, XposedUtil.getParmsByName(StringCls, "equals"), "equals", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("magisk","fuck!!!").replace("Magisk","fuck!!!");;
                    }
                }
            });
            XposedUtil.hookMethod(StringCls, XposedUtil.getParmsByName(StringCls, "equalsIgnoreCase"), "equalsIgnoreCase", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("magisk","fuck!!!").replace("Magisk","fuck!!!");;
                        if(s.equals("rw")){
                            param.args[0] = "r";
                        }
                    }
                }
            });
            XposedUtil.hookMethod(StringCls, new Class[]{String.class,int.class}, "indexOf", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] =
//                                s.replace("xposed","fuck!!!")
//                                .replace("Xposed","fuck!!!")
                                s.replace("test-keys","fuck!!!")
                                .replace("magisk","fuck!!!")
                                .replace("ro.debuggable","fuck!!!")
                                .replace("ro.secure","fuck!!!")
                                .replace("Magisk","fuck!!!");
                    }
                }
            });
            XposedUtil.hookMethod(StringCls, new Class[]{String.class,int.class}, "startsWith", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("#exact","fuck!!!").replace("#exact","fuck!!!").replace("#bestmatch","fuck!!!");
                        String ss = (String)param.thisObject;
                        param.thisObject = ss.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("#exact","fuck!!!").replace("#exact","fuck!!!").replace("#bestmatch","fuck!!!");
                    }
                }
            });
//            XposedUtil.hookMethod(StringCls, new Class[]{String.class,String.class,int.class}, "lastIndexOf", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    if(param.args[1]!=null&&(param.args[1] instanceof String)){
//                        String s = (String)param.args[1];
//                        param.args[1] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!");
//                    }
//                }
//            });
            XposedUtil.hookMethod(classCls, new Class[]{String.class,boolean.class,ClassLoader.class}, "forName", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!");
                    }
                }
            });
            XposedUtil.hookMethod(ClassLoader.class, new Class[]{String.class,boolean.class}, "loadClass", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        if(s.toLowerCase().contains("xposed")){
                            param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!");
//                            XposedBridge.log("BLACKTEST_"+lpparam.packageName+"  "+param.method.getName()+"  "+s);
                        }
                    }
                }
            });
            XposedUtil.hookMethod(Throwable.class,XposedUtil.getParmsByName(Throwable.class,"getOurStackTrace"), "getOurStackTrace", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    StackTraceElement[] res = ((StackTraceElement[])param.getResult());
                    if(res!=null){
                        //ZygoteInit
                        ArrayList<StackTraceElement> newRews = new ArrayList<StackTraceElement>();
                        for(StackTraceElement ste:res){
                            if(ste!=null&&
                                    (ste.getClassName()!=null&&(ste.getClassName().contains("ZygoteInit")||ste.getClassName().toLowerCase().contains("xposed")))||(ste.getFileName()!=null&&ste.getFileName().toLowerCase().contains("xposed"))){
                            }else{
                                newRews.add(ste);
                            }
                        }
                        StackTraceElement[] ress = new StackTraceElement[newRews.size()];
//                        Throwable t = (Throwable)param.thisObject;
//                        XposedBridge.log("BLACKTEST_"+lpparam.packageName+"  "+t.getMessage());
                        for(int i = 0;i<newRews.size();i++){
                            ress[i] = newRews.get(i);
//                            XposedBridge.log("BLACKTEST_"+ress[i].getClassName()+"  "+ress[i].getFileName()+"  "+ress[i].getMethodName()+"  "+ress[i].getLineNumber());
                        }
//                        XposedBridge.log("BLACKTEST_"+lpparam.packageName+"  "+param.method.getName()+"  "+res.length+"  "+ress.length);
                        param.setResult(ress);
                        return;
                    }

                }
            });

            XposedUtil.hookMethod(ClassLoader.class, new Class[]{String.class}, "findClass", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!");
                    }
                }
            });
            XposedUtil.hookMethod(Modifier.class, new Class[]{int.class}, "isNative", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("BLACKTEST_"+lpparam.packageName+"  "+param.method.getName());
                    param.setResult(false);
                    return;
                }
            });
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls,"getApplicationInfoAsUser"), "getApplicationInfoAsUser", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("Magisk","fuck!!!").replace("magisk","fuck!!!").replace("super","fuck!!!").replace("root","fuck!!!");
                    }
                }
            });
            XC_MethodHook hookapm = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        List list = (List) param.getResult();
                        List newList = new ArrayList();
                        int type = 0;
                        for(Object o:list){
                            if(o instanceof ApplicationInfo){
                                type = 1;
                                ApplicationInfo applicationInfo = (ApplicationInfo)o;
                                if(applicationInfo!=null&&applicationInfo.packageName!=null){
                                    if(!applicationInfo.packageName.equals(Common.PACKAGENAME)&&!applicationInfo.packageName.toLowerCase().contains("xposed")){
                                        newList.add(applicationInfo);
                                    }
                                }
                            }else if(o instanceof PackageInfo){
                                type = 2;
                                PackageInfo packageInfo = (PackageInfo)o;
                                if(packageInfo!=null&&packageInfo.packageName!=null){
                                    if(!packageInfo.packageName.equals(Common.PACKAGENAME)&&!packageInfo.packageName.toLowerCase().contains("xposed")){
                                        newList.add(packageInfo);
                                    }
                                }
                            }else if(o instanceof ResolveInfo){
                                type = 3;
                                ResolveInfo resolveInfo = (ResolveInfo)o;
                                if(resolveInfo!=null&&resolveInfo.resolvePackageName!=null){
                                    if(!resolveInfo.resolvePackageName.equals(Common.PACKAGENAME)&&!resolveInfo.resolvePackageName.toLowerCase().contains("xposed")) {
                                        newList.add(resolveInfo);
                                    }
                                }
                            }
                        }
                        if(type == 1){
                            param.setResult((List<ApplicationInfo>)newList);
                        }else if(type == 2){
                            param.setResult((List<PackageInfo>)newList);
                        }else if(type == 3){
                            param.setResult((List<ResolveInfo>)newList);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getInstalledApplications"), "getInstalledApplications",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getInstalledPackages"), "getInstalledPackages",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getPackagesHoldingPermissions"), "getPackagesHoldingPermissions",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getPreferredPackages"), "getPreferredPackages",hookapm);
//            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "queryIntentActivities"), "queryIntentActivities",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "queryIntentContentProviders"), "queryIntentContentProviders",hookapm);

            XposedUtil.hookConstructorMethod(FileInputStream.class, new Class[]{File.class}, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof File)){
                      String path = ((File)param.args[0]).getAbsolutePath();
                      if(path!=null&&path.contains("/proc/")&&path.contains("/maps")){
                        param.args[0] = new File(path.replace("maps","smaps"));
//                        XposedBridge.log("BLACKTEST_"+lpparam.packageName+"  "+param.method.getName()+"  "+path);
                      }
                    }
                }
            });
            XposedUtil.hookConstructorMethod(File.class, new Class[]{String.class,String.class}, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("Superuser","fuck!!!");
                    }
                    if(param.args[1]!=null&&(param.args[1] instanceof String)){
                        String s = (String)param.args[1];
                        if("su".equals(s.toLowerCase())){
                            s = "fuck!!!";
                        }
                        param.args[1] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("Superuser","fuck!!!");
                    }
                }
            });
            XposedUtil.hookConstructorMethod(File.class, new Class[]{String.class}, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("Superuser","fuck!!!");
                        if(s.endsWith("/su")){
                            param.args[0] =  s.replace("/su","fuck!!!").replace("/SU","fuck!!!");
                        }
                    }
                }
            });
            XposedUtil.hookConstructorMethod(File.class, new Class[]{String.class,File.class}, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String)){
                        String s = (String)param.args[0];
                        param.args[0] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("Superuser","fuck!!!");
                    }
                    if(param.args[1]!=null&&(param.args[1] instanceof File)){
                        File s = (File)param.args[1];
                        String p = s.getAbsolutePath().replace("xposed","fuck!!!").replace("Xposed","fuck!!!").replace("Superuser","fuck!!!");
                        if("su".equals(p.toLowerCase())){
                            p = "fuck!!!";
                        }
                        param.args[1] = new File(p);
                    }
                }
            });
            XposedUtil.hookMethod(File.class, new Class[]{},"list", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String ss[] = (String[])param.getResult();
                    if(ss!=null&&ss.length>0){
//                        String newss[] = ss;
                        ArrayList<String> newList = new ArrayList<String>();
                        for(int i = 0;i<ss.length;i++){
                            String s = ss[i].toLowerCase();
                            if(!s.contains("xposed")&&!s.contains("click369")&&!s.contains("superuser")&&!s.contains("magisk")){
                                newList.add(ss[i]);
                            }
//                            newss[i] = ss[i].replace("xposed","moon").replace("Xposed","moon").replace("Superuser","moon").replace("Magisk","moon").replace("magisk","moon");
                        }
                        String res[] = new String[newList.size()];
                        for(int i = 0;i<newList.size();i++){
                            res[i] = newList.get(i);
                        }
                        param.setResult(res);
                        return;
                    }
                }
            });
            XposedUtil.hookConstructorMethod(File.class, new Class[]{File.class,String.class}, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[1]!=null&&(param.args[1] instanceof String)){
                        String s = (String)param.args[1];
                        if("su".equals(s.toLowerCase())){
                            s = "fuck!!!";
                        }
                        param.args[1] = s.replace("xposed","fuck!!!").replace("Xposed","fuck!!!");
                    }
                    if(param.args[0]!=null&&(param.args[0] instanceof File)){
                        File s = (File)param.args[0];
                        String p = s.getAbsolutePath().replace("xposed","fuck!!!").replace("Xposed","fuck!!!");
                        param.args[0] = new File(p);

                    }
                }
            });

            XposedUtil.hookMethod(Runtime.class, new Class[]{String.class},"exec", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if(param.args[0]!=null&&(param.args[0] instanceof String[])){
                        String[] ss = (String[])param.args[0];
                        String temp[] = ss;
                        int i = 0;
                        for(String s:ss){
                            String t = s;
                            if(s.toLowerCase().contains("su")&&s.length()==2){
                                t = "fuck!!!";
                            }
                            temp[i++] = t.replace("bin/su","fuck!!!").replace("bin/SU","fuck!!!");
                        }
                        param.args[0] = temp;
                    }
                }
            });

        }
        if(xpBlackListPrefs.getBoolean(lpparam.packageName+"/setcanhook",false)){
            try{
                final Class apiCls = XposedHelpers.findClass("java.lang.reflect.Field", lpparam.classLoader);
                XposedHelpers.findAndHookMethod(apiCls,"set",Object.class,Object.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            Field field = (Field) methodHookParam.thisObject;
                            if("disableHooks".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------尝试反XP被阻止--------");
                                methodHookParam.setResult(null);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
}
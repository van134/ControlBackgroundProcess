package com.click369.controlbp.service;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;

import com.click369.controlbp.common.Common;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedAMS {
    static boolean isOneOpen = true;
    static boolean isTwoOpen = true;
    static boolean isAppstart = true;
    static boolean isRecentOpen = true;
    static boolean isStopScanMedia = false;
    final static HashMap<String,Object> appStartPrefHMs = new HashMap<String,Object>();
    final static HashSet<String> muBeiHSs = new HashSet<String>();
    final static HashMap<String,Object> controlHMs = new HashMap<String,Object>();
    final static HashMap<String,Boolean> mubeiStopOtherProc = new HashMap<String,Boolean>();
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences settingPrefs,final XSharedPreferences controlPrefs,final XSharedPreferences autoStartPrefs,final XSharedPreferences muBeiPrefs,final XSharedPreferences recentPrefs){
        if (!lpparam.packageName.equals("android")){
            if(lpparam.packageName.equals("com.android.systemui")) {
                try {
                    final Class arCls = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
                    Class clss[] = XposedUtil.getParmsByName(arCls,"onCreate");
                    if(clss!=null){
                        XC_MethodHook hook = new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                try {
                                    final Application app = (Application) (methodHookParam.thisObject);
                                    if (app!=null){
                                        final Intent intentb = new Intent("com.click369.control.ams.initreload");
                                        autoStartPrefs.reload();
                                        controlPrefs.reload();
                                        muBeiPrefs.reload();
                                        settingPrefs.reload();
                                        intentb.putExtra("autoStartPrefs", (Serializable) autoStartPrefs.getAll());
                                        intentb.putExtra("controlPrefs", (Serializable) controlPrefs.getAll());
                                        intentb.putExtra("muBeiPrefs", (Serializable) muBeiPrefs.getAll());
                                        intentb.putExtra("settingPrefs", (Serializable) settingPrefs.getAll());
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                XposedBridge.log("^^^^^^^^^^^^^^^^^systemui  sendbroad "+autoStartPrefs.getAll().size()+"^^^^^^^^^^^^^^^");
                                                app.sendBroadcast(intentb);
                                            }
                                        },1000);
                                    }
                                } catch (RuntimeException e) {
                                    e.printStackTrace();
                                }
                            }
                        };
                        XposedHelpers.findAndHookMethod(arCls, "onCreate", hook);
                    }
                }catch (XposedHelpers.ClassNotFoundError e){
                    e.printStackTrace();
                }catch (NoSuchMethodError e){
                    e.printStackTrace();
                }
            }
            return;
        }
        autoStartPrefs.reload();
        controlPrefs.reload();
        muBeiPrefs.reload();
        appStartPrefHMs.putAll(autoStartPrefs.getAll());
        controlHMs.putAll(controlPrefs.getAll());
        muBeiHSs.addAll(muBeiPrefs.getAll().keySet());
        settingPrefs.reload();
        mubeiStopOtherProc.put(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false));
        final Class amsCls = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);

        final Class actServiceCls = XposedHelpers.findClass("com.android.server.am.ActiveServices", lpparam.classLoader);
        final Class taskRecordCls = XposedHelpers.findClass("com.android.server.am.TaskRecord",lpparam.classLoader);
        final Class processRecordCls = XposedHelpers.findClass("com.android.server.am.ProcessRecord",lpparam.classLoader);
//        final Class procSSRecordCls = XposedHelpers.findClass("com.android.server.am.ProcessStatsService",lpparam.classLoader);
//        XposedUtil.showParmsByName(processRecordCls,"kill");
        final Field sysCxtField = XposedHelpers.findFirstFieldByExactType(amsCls,Context.class);
        final Field mServicesField = XposedHelpers.findFirstFieldByExactType(amsCls,actServiceCls);
//        final Field mProcessStatsField = XposedHelpers.findFirstFieldByExactType(amsCls,procSSRecordCls);
        final HashMap<String,Method> amsMethods =  XposedUtil.getAMSParmas(amsCls);
        if (amsMethods.containsKey("finishBooting")){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                try {
                    final Object ams = methodHookParam.thisObject;
                    if (sysCxtField != null) {
                        sysCxtField.setAccessible(true);
                        final Context sysCxt = (Context) sysCxtField.get(ams);//(Context)methodHookParam.args[0];
                        Object o = XposedHelpers.getAdditionalStaticField(amsCls, "click369res");
                        boolean isContinue = false;
                        if (o!=null&&sysCxt!=null&&o.hashCode()==sysCxt.hashCode()){
                            isContinue = true;
                        }
                        if (sysCxt != null&&!isContinue) {
                            final Runnable startService = new Runnable() {
                                @Override
                                public void run() {
                                try {
                                    Intent intenta = new Intent("com.click369.controlbp.emptyactivity");
                                    intenta.addCategory("controlbp");
                                    intenta.putExtra("data","启动服务");
                                    sysCxt.startActivity(intenta);
                                }catch (RuntimeException e){
                                    e.printStackTrace();
                                }
                                }
                            };
                            final Handler h = new Handler() {
                                @Override
                                public void handleMessage(Message msg) {
                                    try {
                                        String pkg = (String) msg.obj;
                                        Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                        m.invoke(ams, pkg, 0);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            if (sysCxt != null) {
                                BroadcastReceiver br = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                    try {
                                        String action = intent.getAction();
                                        if ("com.click369.control.ams.forcestopapp".equals(action)) {
                                            String pkg = intent.getStringExtra("pkg");
                                            if (pkg.equals("com.click369.control") ||
                                                    (pkg.contains("clock") && pkg.contains("android"))) {
                                                return;
                                            }
                                            try {
                                                Message msg = Message.obtain();
                                                msg.obj = pkg;
                                                msg.what = pkg.hashCode();
                                                h.removeMessages(pkg.hashCode());
                                                h.sendMessageDelayed(msg, 100);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                XposedBridge.log("^^^^^^^^^^^^^^hook AMS error " + e + "^^^^^^^^^^^^^^^^^");
                                            }
                                        } else if ("com.click369.control.ams.changerecent".equals(action)) {
                                            String pkg = intent.getStringExtra("pkg");
                                            try {
                                                Field recField = amsCls.getDeclaredField("mRecentTasks");
                                                recField.setAccessible(true);
                                                ArrayList lists = (ArrayList) (recField.get(methodHookParam.thisObject));
                                                if (lists.size() > 0) {
                                                    if (lists.get(0).getClass().getName().equals("com.android.server.am.TaskRecord")) {
                                                        for (Object o : lists) {
                                                            Method getBaseMethod = taskRecordCls.getDeclaredMethod("getBaseIntent");
                                                            if (getBaseMethod == null) {
                                                                getBaseMethod = taskRecordCls.getMethod("getBaseIntent");
                                                            }
                                                            getBaseMethod.setAccessible(true);
                                                            Intent intentm = (Intent) (getBaseMethod.invoke(o));
                                                            if (intentm != null && pkg.equals(intentm.getComponent().getPackageName())) {
                                                                Field isAvailableField = o.getClass().getDeclaredField("isAvailable");
                                                                isAvailableField.setAccessible(true);
                                                                isAvailableField.set(o, true);
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }catch (Exception e) {
                                                e.printStackTrace();
                                                XposedBridge.log("^^^^^^^^^^^^^^^^^changerecent error "+e+" ^^^^^^^^^^^^^^^");
                                            }
                                        }else if ("com.click369.control.ams.delrecent".equals(action)) {
                                            String pkg = intent.getStringExtra("pkg");
                                            try {
                                                Field recField = methodHookParam.thisObject.getClass().getDeclaredField("mRecentTasks");
                                                recField.setAccessible(true);
                                                ArrayList lists = (ArrayList) (recField.get(methodHookParam.thisObject));
                                                if (lists.size() > 0) {
                                                    if (lists.get(0).getClass().getName().equals("com.android.server.am.TaskRecord")) {
                                                        HashSet sets = new HashSet();
                                                        for (Object o : lists) {
                                                            Method getBaseMethod = taskRecordCls.getDeclaredMethod("getBaseIntent");
                                                            if (getBaseMethod == null) {
                                                                getBaseMethod = taskRecordCls.getMethod("getBaseIntent");
                                                            }
                                                            getBaseMethod.setAccessible(true);
                                                            Intent intentm = (Intent) (getBaseMethod.invoke(o));
                                                            if (intentm != null && pkg.equals(intentm.getComponent().getPackageName())) {
                                                                sets.add(o);
                                                            }
                                                        }
                                                        lists.removeAll(sets);
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                XposedBridge.log("^^^^^^^^^^^^^^^^^delrecent error "+e+" ^^^^^^^^^^^^^^^");
                                            }
                                        }else if("com.click369.control.ams.forcestopservice".equals(action)){
                                            String pkg = intent.getStringExtra("pkg");
                                            try {
                                                muBeiHSs.add(pkg);
                                                if (mServicesField!=null){
                                                    mServicesField.setAccessible(true);
                                                    Object mServicesObject = mServicesField.get(ams);
                                                    if (pkg!=null&&pkg.length()>0){
                                                   if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                       Method killMethod = mServicesObject.getClass().getDeclaredMethod("bringDownDisabledPackageServicesLocked", String.class, Set.class, int.class, boolean.class, boolean.class, boolean.class);
                                                       killMethod.setAccessible(true);
                                                       killMethod.invoke(mServicesObject, pkg, null, 0, true, false, true);//第二个布尔值 是停止进程
                                                   }else{
                                                        XposedUtil.stopServicesAndroidL(amsCls,processRecordCls,mServicesObject,ams,pkg);
                                                   }
                                                        settingPrefs.reload();
                                                        mubeiStopOtherProc.put(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false));
                                                        if(mubeiStopOtherProc.get(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)){
                                                            XposedUtil.stopProcess(amsCls,processRecordCls,ams,pkg,true);
//                                                                XposedUtil.stopProcess(amsCls,processRecordCls,ams,pkg);
                                                        }
//                                                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                                                            Method idleMethod = amsMethods.get("makePackageIdle");
//                                                            if (idleMethod != null) {
//                                                                try {
//                                                                    idleMethod.setAccessible(true);
//                                                                    idleMethod.invoke(ams, pkg, 0);
//                                                                } catch (RuntimeException e) {
//                                                                    e.printStackTrace();
//                                                                }
//                                                            }
//                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                XposedBridge.log("^^^^^^^^^^^^^^^^^forcestopservice error "+e+" ^^^^^^^^^^^^^^^");
                                            }
                                        }else if("com.click369.control.ams.getprocinfo".equals(action)){
//                                                String pkg = intent.getStringExtra("pkg");
//                                                try {
//                                                    Field procsField = amsCls.getDeclaredField("mLruProcesses");
//                                                    procsField.setAccessible(true);
//                                                    ArrayList<Object> procs = (ArrayList<Object>)procsField.get(ams);
//                                                    if(procs!=null&&procs.size()>0){
//                                                        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd:HH:mm:ss");
//                                                        for(Object proc:procs){
//                                                            Field infoField = proc.getClass().getDeclaredField("info");
//                                                            infoField.setAccessible(true);
//                                                            ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
//                                                            if(pkg.equals(info.packageName)){
//                                                                Field lastWakeTimeField = proc.getClass().getDeclaredField("lastWakeTime");
//                                                                lastWakeTimeField.setAccessible(true);
//                                                                long lastWakeTime = (Long)lastWakeTimeField.get(proc);
//                                                                String lastWakeStr = sdf.format(new Date(lastWakeTime));
//                                                                XposedBridge.log("+++++++++++最后一次运行时间 "+info.packageName+"  "+lastWakeStr);
//                                                                break;
//                                                            }
//                                                        }
//                                                    }
//                                                } catch (Exception e) {
//                                                    e.printStackTrace();
//                                                }
                                        }else if(Intent.ACTION_SCREEN_ON.equals(action)){
                                            if(Math.random()>0.8) {
                                                Intent intent1 = new Intent("com.click369.control.heart");
                                                sysCxt.sendBroadcast(intent1);
                                                h.postDelayed(startService, 1500);
                                            }
                                        }else if("com.click369.control.ams.changepersistent".equals(action)){
                                            boolean persistent = intent.getBooleanExtra("persistent", false);
                                            String pkg = intent.getStringExtra("pkg");
                                            XposedUtil.changePersistent(amsCls, ams, pkg, persistent);
                                            if (!persistent) {
                                                try {
                                                    Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                                    m.invoke(methodHookParam.thisObject, pkg, 0);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }else if("com.click369.control.ams.killself".equals(action)){
                                            XposedUtil.stopAllProcess(amsCls, processRecordCls, ams, Common.PACKAGENAME);
                                        }else if("com.click369.control.ams.heart".equals(action)){
                                            h.removeCallbacks(startService);
                                        }else if("com.click369.control.ams.removemubei".equals(action)){
                                            String apk = intent.getStringExtra("apk");
                                            muBeiHSs.remove(apk);
                                            settingPrefs.reload();
                                            mubeiStopOtherProc.put(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false));
                                            if(mubeiStopOtherProc.get(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)) {
                                                XposedUtil.stopProcess(amsCls, processRecordCls, ams, apk, false);
                                            }
                                        }else if("com.click369.control.ams.reloadcontrol".equals(action)){
                                            if (controlPrefs.hasFileChanged()) {
                                                controlHMs.clear();
                                                controlPrefs.reload();
                                                controlHMs.putAll(controlPrefs.getAll());
                                                XposedBridge.log("^^^^^^^^^^^^^^重载CONTROL " + controlHMs.size() + "^^^^^^^^^^^^^^^^^");
                                            }
                                        }else if("com.click369.control.ams.reloadmubei".equals(action)){
                                            if (muBeiPrefs.hasFileChanged()) {
                                                muBeiPrefs.reload();
                                                muBeiHSs.clear();
                                                Set<String> keys = muBeiPrefs.getAll().keySet();
                                                for (String k : keys) {
                                                    if (muBeiPrefs.getInt(k, -1) == 0) {
                                                        muBeiHSs.add(k);
                                                    }
                                                }
                                                XposedBridge.log("^^^^^^^^^^^^^^重载墓碑 " + muBeiHSs.size() + "^^^^^^^^^^^^^^^^^");
                                            }
                                        }else if("com.click369.control.ams.reloadautostart".equals(action)){
                                            if (autoStartPrefs.hasFileChanged()&&autoStartPrefs.getAll().size()>0) {
                                                autoStartPrefs.reload();
                                                appStartPrefHMs.clear();
                                                appStartPrefHMs.putAll(autoStartPrefs.getAll());
                                            }
//                                            XposedBridge.log("^^^^^^^^^^^^^^重载自启动 " + appStartPrefHMs.size() + "^^^^^^^^^^^^^^^^^");
                                        }else if("com.click369.control.ams.initreload".equals(action)){
                                            appStartPrefHMs.clear();
                                            appStartPrefHMs.putAll((Map)intent.getSerializableExtra("autoStartPrefs"));
                                            controlHMs.clear();
                                            controlHMs.putAll((Map)intent.getSerializableExtra("controlPrefs"));
                                            muBeiHSs.clear();
                                            Map mbMap = (Map)intent.getSerializableExtra("muBeiPrefs");
                                            muBeiHSs.addAll(mbMap.keySet());
                                            Map settingMap = (Map)intent.getSerializableExtra("settingPrefs");
                                            isOneOpen = settingMap.containsKey(Common.ALLSWITCH_ONE)?(boolean)settingMap.get(Common.ALLSWITCH_ONE):false;//settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
                                            isTwoOpen = settingMap.containsKey(Common.ALLSWITCH_TWO)?(boolean)settingMap.get(Common.ALLSWITCH_TWO):false;//settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
                                            isAppstart = settingMap.containsKey(Common.ALLSWITCH_FIVE)?(boolean)settingMap.get(Common.ALLSWITCH_FIVE):false;//settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true);
                                            isRecentOpen = settingMap.containsKey(Common.ALLSWITCH_FOUR)?(boolean)settingMap.get(Common.ALLSWITCH_FOUR):false;//settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
                                            isStopScanMedia = settingMap.containsKey(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA)?(boolean)settingMap.get(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA):false;//settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA,false);
//                                            XposedBridge.log("^^^^^^^^^^^^^^重载数据 settingPrefs " + settingPrefs.getAll().size()+ "^^^^^^^^^^^^^^^^^");
//                                            XposedBridge.log("^^^^^^^^^^^^^^重载数据 isOneOpen " + isOneOpen+ "^^^^^^^^^^^^^^^^^");
//                                            XposedBridge.log("^^^^^^^^^^^^^^重载数据 isTwoOpen " + isTwoOpen+ "^^^^^^^^^^^^^^^^^");
//                                            XposedBridge.log("^^^^^^^^^^^^^^重载数据 isStopScanMedia " + isStopScanMedia+ "^^^^^^^^^^^^^^^^^");
                                            XposedBridge.log("^^^^^^^^^^^^^^init自启数据 " + appStartPrefHMs.size() + "^^^^^^^^^^^^^^^^^");
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        XposedBridge.log("^^^^^^^^^^^^^^AMS广播出错 " + e + "^^^^^^^^^^^^^^^^^");
                                    }
                                    }
                                };
                                IntentFilter filter = new IntentFilter();
                                filter.addAction("com.click369.control.ams.forcestopapp");
                                filter.addAction("com.click369.control.ams.changerecent");
                                filter.addAction("com.click369.control.ams.delrecent");
                                filter.addAction("com.click369.control.ams.forcestopservice");
                                filter.addAction("com.click369.control.ams.getprocinfo");
                                filter.addAction("com.click369.control.ams.heart");
                                filter.addAction("com.click369.control.ams.killself");
                                filter.addAction("com.click369.control.ams.removemubei");
                                filter.addAction("com.click369.control.ams.changepersistent");
                                filter.addAction("com.click369.control.ams.reloadcontrol");
                                filter.addAction("com.click369.control.ams.reloadmubei");
                                filter.addAction("com.click369.control.ams.reloadautostart");
                                filter.addAction("com.click369.control.ams.initreload");
                                filter.addAction(Intent.ACTION_SCREEN_ON);
                                sysCxt.registerReceiver(br, filter);
                                XposedBridge.log("^^^^^^^^^^^^^^开机启动注册广播： "+ sysCxt + "^^^^^^^^^^^^^^^^^");
                                XposedHelpers.setAdditionalStaticField(amsCls, "click369res", sysCxt.hashCode());
                            }
                        }
                    }
                }catch (RuntimeException e) {
                    XposedBridge.log("^^^^^^^^^^^^^^hook AMS error " + e + "^^^^^^^^^^^^^^^^^");
                }
                }
            };
            Class clss[] = amsMethods.get("finishBooting").getParameterTypes();
            if (clss.length==0){
                XposedHelpers.findAndHookMethod(amsCls,"finishBooting",hook);
            }else if (clss.length==1){
                XposedHelpers.findAndHookMethod(amsCls,"finishBooting",clss[0],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^finishBooting else"+clss.length+" 函数未找到^^^^^^^^^^^^^^^^^");
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^finishBooting  函数未找到^^^^^^^^^^^^^^^^^");
        }

        if(amsMethods.containsKey("forceStopPackage")){
            XC_MethodHook  hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try{
                        if ("com.click369.controlbp".equals(methodHookParam.args[0])) {
                            methodHookParam.setResult(null);
                            return;
                        }
                    } catch (RuntimeException e) {
                        XposedBridge.log("^^^^^^^^^^^^^^hook AMS forceStopPackage err "+e+"^^^^^^^^^^^^^^^^^");
                    }
                }
            };
            final Class clss[] = amsMethods.get("forceStopPackage").getParameterTypes();
            if (clss.length == 4){XposedHelpers.findAndHookMethod(amsCls, "forceStopPackage",clss[0],clss[1],clss[2],clss[3],hook); }
            else if (clss.length == 3){XposedHelpers.findAndHookMethod(amsCls, "forceStopPackage",clss[0],clss[1],clss[2],hook); }
            else if (clss.length == 2){XposedHelpers.findAndHookMethod(amsCls, "forceStopPackage",clss[0],clss[1],hook); }
            else if (clss.length == 1){XposedHelpers.findAndHookMethod(amsCls, "forceStopPackage",clss[0],hook); }else{
                XposedBridge.log("^^^^^^^^^^^^^^forceStopPackage else"+clss.length+" 函数未找到^^^^^^^^^^^^^^^^^");
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^forceStopPackage  函数未找到^^^^^^^^^^^^^^^^^");
        }

        if(amsMethods.containsKey("checkCallingPermission")){
            XC_MethodHook  hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if ("android.permission.FORCE_STOP_PACKAGES".equals(methodHookParam.args[0])) {
                        try {
                            methodHookParam.setResult(PackageManager.PERMISSION_GRANTED);
                            return;
                        } catch (RuntimeException e) {
                            XposedBridge.log("^^^^^^^^^^^^^^hook AMS getpermission err " + lpparam.packageName + "^^^^^^^^^^^^^^^^^");
                        }
                    }
                }
            };
            final Class clss[] = amsMethods.get("checkCallingPermission").getParameterTypes();
            if (clss.length == 4){XposedHelpers.findAndHookMethod(amsCls, "checkCallingPermission",clss[0],clss[1],clss[2],clss[3],hook); }
            else if (clss.length == 3){XposedHelpers.findAndHookMethod(amsCls, "checkCallingPermission",clss[0],clss[1],clss[2],hook); }
            else if (clss.length == 2){XposedHelpers.findAndHookMethod(amsCls, "checkCallingPermission",clss[0],clss[1],hook); }
            else if (clss.length == 1){XposedHelpers.findAndHookMethod(amsCls, "checkCallingPermission",clss[0],hook); }else{
                XposedBridge.log("^^^^^^^^^^^^^^checkCallingPermission else"+clss.length+" 函数未找到^^^^^^^^^^^^^^^^^");
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^checkCallingPermission  函数未找到^^^^^^^^^^^^^^^^^");
        }

        if(amsMethods.containsKey("isGetTasksAllowed")){
            XC_MethodHook  hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                    if ("android.permission.FORCE_STOP_PACKAGES".equals(methodHookParam.args[0])) {
                        try {
                            methodHookParam.setResult(true);
                            return;
                        } catch (RuntimeException e) {
                            XposedBridge.log("^^^^^^^^^^^^^^hook AM isGetTasksAllowed err ^^^^^^^^^^^^^^^^^");
                        }
//                    }
                }
            };
            final Class clss[] = amsMethods.get("isGetTasksAllowed").getParameterTypes();
            if (clss.length == 4){XposedHelpers.findAndHookMethod(amsCls, "isGetTasksAllowed",clss[0],clss[1],clss[2],clss[3],hook); }
            else if (clss.length == 3){XposedHelpers.findAndHookMethod(amsCls, "isGetTasksAllowed",clss[0],clss[1],clss[2],hook); }
            else if (clss.length == 2){XposedHelpers.findAndHookMethod(amsCls, "isGetTasksAllowed",clss[0],clss[1],hook); }
            else if (clss.length == 1){XposedHelpers.findAndHookMethod(amsCls, "isGetTasksAllowed",clss[0],hook); }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^isGetTasksAllowed  函数未找到^^^^^^^^^^^^^^^^^");
        }

        isAppstart = settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true);
//        //启动控制应用锁部分
//        if(amsMethods.containsKey("startActivity")&&isAppstart){
//            final Class clss[] = amsMethods.get("startActivity").getParameterTypes();
//            int intentIndexTemp = -1,callPkgIndexTemp= -1;
//            for(int i = 0;i<clss.length;i++){
//                Class c = clss[i];
//                if (intentIndexTemp==-1&&c.getName().equals(Intent.class.getName())){
//                    intentIndexTemp = i;
//                }
//                if (callPkgIndexTemp==-1&&c.getName().equals(String.class.getName())){
//                    callPkgIndexTemp = i;
//                }
//            }
//            final int intentIndex = intentIndexTemp;
//            final int callPkgIndex = callPkgIndexTemp;
//            final String home = autoStartPrefs.getString("nowhomeapk","");
//            XC_MethodHook xm = new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                    try {
//                        if (intentIndex!=-1&&callPkgIndex!=-1){
//                            final Object ams = methodHookParam.thisObject;
//                            sysCxtField.setAccessible(true);
//                            final Context cxt = (Context) sysCxtField.get(ams);
//                            Intent intent = (Intent) methodHookParam.args[intentIndex];
//                            String callingPkg = (String) methodHookParam.args[callPkgIndex]+"";
//                            if (intent!=null&&!home.equals(callingPkg)) {
//                                String pkg = intent.getComponent() == null ? null : intent.getComponent().getPackageName();
//                                if(!callingPkg.equals(pkg)){
//                                    XposedAppStart.sendBroad(intent,cxt,callingPkg,pkg);
//                                    autoStartPrefs.reload();
////                                    boolean isLockApp = appStartPrefHMs.containsKey(pkg+"/lockapp")?(Boolean)appStartPrefHMs.get(pkg+"/lockapp"):false;
////                                    boolean isLockAppOk = appStartPrefHMs.containsKey(pkg+"/lockok")?(Boolean)appStartPrefHMs.get(pkg+"/lockok"):false;
////                                    if(pkg!=null&isLockApp&&!isLockAppOk){
//                                    if(pkg!=null&&autoStartPrefs.getBoolean(pkg+"/lockapp",false)&&!autoStartPrefs.getBoolean(pkg+"/lockok",false)){
//                                        String cls = pkg==null?null:intent.getComponent().getClassName();
//                                        if (!(Common.PACKAGENAME+".activity.UnLockActivity").equals(cls)&&
//                                                !(Common.PACKAGENAME+".activity.RunningActivity").equals(cls)&&
//                                                !"com.android.webview".equals(callingPkg)){
//                                            Intent intent1 = new Intent(Intent.ACTION_MAIN);
//                                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
//                                            intent1.addCategory(Intent.CATEGORY_HOME);
//                                            cxt.startActivity(intent1);
//                                            Intent broad1 = new Intent("com.click369.control.lockapp");
//                                            broad1.putExtra("pkg", pkg);
//                                            broad1.putExtra("class", cls);
//                                            broad1.putExtra("intent", intent);
//                                            cxt.startActivity(broad1);
//                                            methodHookParam.setResult(102);
//                                            return;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }catch (RuntimeException e){
//                        e.printStackTrace();
//                    }
//                }
//            };
//            if (clss.length ==12){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],xm );
//            }else if (clss.length ==11){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],xm );
//            }else if (clss.length ==10){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9],xm );
//            }else if (clss.length ==9){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8],xm );
//            }else if (clss.length ==8){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7],xm );
//            }else if (clss.length ==7){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6],xm );
//            }else if (clss.length ==6){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5],xm );
//            }else if (clss.length ==3){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1], clss[2],xm );
//            }else if (clss.length ==2){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0], clss[1],xm );
//            }else if (clss.length ==1){
//                XposedHelpers.findAndHookMethod(amsCls, "startActivity", clss[0],xm );
//            }
//        }else if(isAppstart){
//            XposedBridge.log("^^^^^^^^^^^^^^startActivity  函数未找到^^^^^^^^^^^^^^^^^");
//        }

        //自启控制中自启动
        if(amsMethods.containsKey("startProcessLocked")&&isAppstart){
            final Class clss[] = amsMethods.get("startProcessLocked").getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                    try{
                        String processName = (String)methodHookParam.args[0];
                        ApplicationInfo info = (ApplicationInfo)methodHookParam.args[1];
//                        autoStartPrefs.reload();
//                        int size = autoStartPrefs.getAll().size();
//                        XposedBridge.log("^^^^^^^^^^^^^^重载自启动111 "+ size+info.packageName+ "^^^^^^^^^^^^^^^^^");
//                        if (size == 0){
//                            XSharedPreferences xps = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_AUTOSTARTNAME);
//                            xps.makeWorldReadable();
//                            XposedBridge.log("^^^^^^^^^^^^^^重载自启动222 "+ xps.getAll().size()+ "^^^^^^^^^^^^^^^^^");
//                        }
//                      ComponentName hostingName = (ComponentName)methodHookParam.args[5];
                        if(!"android".equals(processName)&&!"android".equals(info.packageName)){
//                            autoStartPrefs.reload();
                            String hostingType = (String)methodHookParam.args[4];

                            boolean isAutoHM = appStartPrefHMs.containsKey(info.packageName+"/autostart");
//                            XposedBridge.log("^^^^^^^^^^^^^^ startProcessLocked  "+hostingType+"  "+info.packageName+"  "+isAutoHM+"^^^^^^^^^^^^^^^^^");
//                            if(isAutoHM){
                            if(isAutoHM||autoStartPrefs.getBoolean(info.packageName+"/autostart",false)){
                                boolean keepIfLarge = clss!=null&&clss.length>=14?((methodHookParam.args[9] instanceof Boolean)?(Boolean) methodHookParam.args[9]:true):true;
                                Method getProcessRecordLocked = amsCls.getDeclaredMethod("getProcessRecordLocked",String.class,int.class,boolean.class);
                                getProcessRecordLocked.setAccessible(true);
//                                if(processName!=null){
                                    Object processObj = getProcessRecordLocked.invoke(methodHookParam.thisObject,info.packageName,info.uid,keepIfLarge);
                                    ComponentName cn = (ComponentName) methodHookParam.args[5];
                                    if(appStartPrefHMs.containsKey(info.packageName+"/checkautostart")){
//                                    if(autoStartPrefs.getBoolean(info.packageName+"/checkautostart",false)||appStartPrefHMs.containsKey(info.packageName+"/checkautostart")){
                                        Object jumpAct = appStartPrefHMs.get(info.packageName+"/jumpactivity");
                                        Object homeAct = appStartPrefHMs.get(info.packageName+"/homeactivity");
//                                        String jumpAct = autoStartPrefs.getString(info.packageName+"/jumpactivity","");
//                                        String homeAct = autoStartPrefs.getString(info.packageName+"/homeactivity","");
//                                        if (jumpAct!=null){
//                                            jumpAct = (String)(appStartPrefHMs.get(info.packageName+"/jumpactivity"));
//                                        }else if (homeAct != null){
//                                            homeAct = (String)(appStartPrefHMs.get(info.packageName+"/homeactivity"));
//                                        }
                                        if(processObj==null&&((jumpAct!=null&&jumpAct.equals(cn.getClassName()))||(homeAct!=null&&!homeAct.equals(cn.getClassName())))){
                                            Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                            m.invoke(methodHookParam.thisObject, info.packageName, 0);
                                            methodHookParam.setResult(null);
                                            return;
                                        }
                                    }
                                    if(processObj==null&&!"activity".equals(hostingType)){
                                        //8.0再选择杀死
                                        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.N ) {
                                            Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                            m.invoke(methodHookParam.thisObject, info.packageName, 0);
                                        }
                                        methodHookParam.setResult(null);
                                        return;
                                    }
                                    //"com.igexin.sdk.GActivity""com.igexin.sdk.PushActivity"
                                    if (processObj==null&&cn!=null&&"activity".equals(hostingType)&&(cn.getClassName().contains(".GActivity")||cn.getClassName().contains(".PushActivity")||cn.getClassName().contains(".PushGTActivity"))){
                                        Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                        m.invoke(methodHookParam.thisObject, info.packageName, 0);
                                        methodHookParam.setResult(null);
                                        return;
                                    }
//                                }
//                            }else if(isstopapp){
                            }else if(appStartPrefHMs.containsKey(info.packageName+"/stopapp")){
                                Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                m.invoke(methodHookParam.thisObject, info.packageName, 0);
                                methodHookParam.setResult(null);
                                return;
                            }
//                            if(mubeiStopOtherProc.get(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)&&muBeiHSs.contains(info.packageName)&&!"activity".equals(hostingType)){
//                                XposedBridge.log("^^^^^^^^^^^^^^墓碑时启动 "+info.packageName+"  "+hostingType+" ^^^^^^^^^^^^^^^^^");
//                                final String pkg = info.packageName;
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        XposedBridge.log("^^^^^^^^^^^^^^墓碑时准备清理 "+pkg+" ^^^^^^^^^^^^^^^^^");
//                                        stopProcess(amsCls,processRecordCls,methodHookParam.thisObject,pkg);
//                                    }
//                                },1000);
//                                methodHookParam.setResult(null);
//                                return;
//                            }
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            };
            if(clss.length == 14){
                XposedHelpers.findAndHookMethod(amsCls, "startProcessLocked", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],hook);
            }else if(clss.length == 9){
                XposedHelpers.findAndHookMethod(amsCls, "startProcessLocked", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8],hook);
            }else if(clss.length == 6){
                XposedHelpers.findAndHookMethod(amsCls, "startProcessLocked", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5],hook);
            }else if(clss.length == 7){
                XposedHelpers.findAndHookMethod(amsCls, "startProcessLocked", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6],hook);
            }else if(clss.length == 8){
                XposedHelpers.findAndHookMethod(amsCls, "startProcessLocked", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7],hook);
            }else if(clss.length == 13){
                XposedHelpers.findAndHookMethod(amsCls, "startProcessLocked", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],hook);
            }else if(clss.length == 15){
                XposedHelpers.findAndHookMethod(amsCls, "startProcessLocked", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^startProcessLocked else 函数未找到"+clss.length+" ^^^^^^^^^^^^^^^^^");
            }

        }else if(isAppstart){
            XposedBridge.log("^^^^^^^^^^^^^^startProcessLocked  函数未找到^^^^^^^^^^^^^^^^^");
        }
        isOneOpen = settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
        isTwoOpen = settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
        isStopScanMedia = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA,false);
        final boolean isMubeiStopOther = settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false);
        if(amsMethods.containsKey("startService")&&(isOneOpen||isTwoOpen)){
            final Class clss[] = amsMethods.get("startService").getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    Intent intent = (Intent)methodHookParam.args[1];
                    String callingPkg = null;
                    if(methodHookParam.args[clss.length-2] instanceof String){
                        callingPkg = (String)methodHookParam.args[clss.length-2];
                    }else{
                        for (int i = clss.length-1;i>=0;i--){
                            if(methodHookParam.args[i] instanceof String){
                                callingPkg = (String)methodHookParam.args[i];
                                break;
                            }
                        }
                    }
//                    XposedBridge.log("CONTROL  "+SystemClock.elapsedRealtime()+"  "+callingPkg+"  "+intent+" "+isStopScanMedia);
                    if(SystemClock.elapsedRealtime()<1000*60*2&&isStopScanMedia){
                        if (intent != null && intent.getComponent() != null && intent.getComponent().getClassName().endsWith("MediaScannerService")) {
//                            XposedBridge.log("CONTROL  "+intent.getComponent().getClassName());
                            Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                            m.invoke(methodHookParam.thisObject, callingPkg, 0);
                            methodHookParam.setResult(intent == null ? new ComponentName("", "") : intent.getComponent());
                            return;
                        }
                    }
                    if ((muBeiHSs.contains(callingPkg)&&isTwoOpen)||(controlHMs.containsKey(callingPkg+"/service")&&isOneOpen)){
//                            String apk = intent == null ? "" : intent.getComponent() == null ? "" : intent.getComponent().getPackageName();
                        if (intent != null && intent.getComponent() != null && controlHMs.containsKey(intent.getComponent().getClassName() + "/service")) {
                        } else {
//                                XposedBridge.log("^^^^^^^^^^^^^^AMS启动服务 被阻止 "+callingPkg +"  "+intent+ "^^^^^^^^^^^^^^^^^");
                            methodHookParam.setResult(intent == null ? new ComponentName("", "") : intent.getComponent());
                            return;
                        }
                    }
                } catch (RuntimeException e) {
                    XposedBridge.log("^^^^^^^^^^^^^^AMS阻止服务出错 "+e+ "^^^^^^^^^^^^^^^^^");
                }
                }
            };
            if(clss.length == 5){
                XposedHelpers.findAndHookMethod(amsCls, "startService", clss[0], clss[1], clss[2], clss[3], clss[4],hook);
            }else if(clss.length == 6){
                XposedHelpers.findAndHookMethod(amsCls, "startService", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5],hook);
            }else if(clss.length == 7){
                XposedHelpers.findAndHookMethod(amsCls, "startService", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6],hook);
            }else if(clss.length == 8){
                XposedHelpers.findAndHookMethod(amsCls, "startService", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7],hook);
            }else if(clss.length == 4){
                XposedHelpers.findAndHookMethod(amsCls, "startService", clss[0], clss[1], clss[2], clss[3],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^startService else 函数未找到"+clss.length+" ^^^^^^^^^^^^^^^^^");
                for(Class c:clss){
                    XposedBridge.log("^^^^^^^^^^^^^^METHOD startService "+c.getName()+ "^^^^^^^^^^^^^^^^^");
                }
            }
        }else if(isAppstart){
            XposedBridge.log("^^^^^^^^^^^^^^startService  函数未找到^^^^^^^^^^^^^^^^^");
        }

        //阻止广播发送相关
//        final boolean isOneOpen = settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
//        final boolean isTwoOpen = settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
        if((isOneOpen||isTwoOpen)&&amsMethods.containsKey("broadcastIntentLocked")){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    //阻止往出发广播
                    String callingPackage = ((String)methodHookParam.args[1])+"";
                        if(isMubeiStopOther&&isTwoOpen) {
                            muBeiPrefs.reload();
                        }
                        if(isOneOpen) {
                            controlPrefs.reload();
                        }
//                    if((isOneOpen&&controlHMs.containsKey(callingPackage+"/broad"))){
                            if((isOneOpen&&controlHMs.containsKey(callingPackage+"/broad"))||(isMubeiStopOther&&isTwoOpen&&muBeiHSs.contains(callingPackage))
                                    ){
                            boolean isSend = false;
                            if(methodHookParam.args[2]!=null){
                                Intent intent = (Intent)methodHookParam.args[2];
                                isSend = (intent.getAction()+"").contains("click369");
                            }
                            if(!isSend){
                                methodHookParam.setResult(-1);
//                                    methodHookParam.setResult(-1);
                                return;
                            }
//                            }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            };
            final Class sls[] = amsMethods.get("broadcastIntentLocked").getParameterTypes();
            if(sls.length == 19){
                XposedHelpers.findAndHookMethod(amsCls,"broadcastIntentLocked",sls[0],sls[1],sls[2],sls[3],sls[4],sls[5],sls[6],sls[7],sls[8],sls[9],sls[10],sls[11],sls[12],sls[13],sls[14],sls[15],sls[16],sls[17],sls[18], hook);
            }else if(sls.length == 18){
                XposedHelpers.findAndHookMethod(amsCls,"broadcastIntentLocked",sls[0],sls[1],sls[2],sls[3],sls[4],sls[5],sls[6],sls[7],sls[8],sls[9],sls[10],sls[11],sls[12],sls[13],sls[14],sls[15],sls[16],sls[17], hook);
            }else if(sls.length == 17){
                XposedHelpers.findAndHookMethod(amsCls,"broadcastIntentLocked",sls[0],sls[1],sls[2],sls[3],sls[4],sls[5],sls[6],sls[7],sls[8],sls[9],sls[10],sls[11],sls[12],sls[13],sls[14],sls[15],sls[16], hook);
            }else if(sls.length == 16){
                XposedHelpers.findAndHookMethod(amsCls,"broadcastIntentLocked",sls[0],sls[1],sls[2],sls[3],sls[4],sls[5],sls[6],sls[7],sls[8],sls[9],sls[10],sls[11],sls[12],sls[13],sls[14],sls[15], hook);
            }else if(sls.length == 15){
                XposedHelpers.findAndHookMethod(amsCls,"broadcastIntentLocked",sls[0],sls[1],sls[2],sls[3],sls[4],sls[5],sls[6],sls[7],sls[8],sls[9],sls[10],sls[11],sls[12],sls[13],sls[14], hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^broadcastIntentLocked else"+sls.length+"  函数未找到 ^^^^^^^^^^^^^^^^^");
            }
        }else if(isOneOpen||isTwoOpen){
            XposedBridge.log("^^^^^^^^^^^^^^broadcastIntentLocked  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        //广播发送相关
        if((isOneOpen||isTwoOpen)&&amsMethods.containsKey("checkBroadcastFromSystem")){
            //防止系统检测是否是系统广播 不然报异常
            XC_MethodHook hookBroadPerm = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    if(methodHookParam.args[0]!=null&&methodHookParam.args[0] instanceof Intent){
                        String action =  ((Intent)methodHookParam.args[0]).getAction();
                        if(action!=null&&action.contains("click369")){
                            methodHookParam.setResult(null);
                            return;
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            };
            final Class slsCbfs[] = amsMethods.get("checkBroadcastFromSystem").getParameterTypes();
            if(slsCbfs.length == 6){
                XposedHelpers.findAndHookMethod(amsCls,"checkBroadcastFromSystem",slsCbfs[0],slsCbfs[1],slsCbfs[2],slsCbfs[3],slsCbfs[4],slsCbfs[5], hookBroadPerm);
            }else if(slsCbfs.length == 7){
                XposedHelpers.findAndHookMethod(amsCls,"checkBroadcastFromSystem",slsCbfs[0],slsCbfs[1],slsCbfs[2],slsCbfs[3],slsCbfs[4],slsCbfs[5],slsCbfs[6], hookBroadPerm);
            }else if(slsCbfs.length == 8){
                XposedHelpers.findAndHookMethod(amsCls,"checkBroadcastFromSystem",slsCbfs[0],slsCbfs[1],slsCbfs[2],slsCbfs[3],slsCbfs[4],slsCbfs[5],slsCbfs[6],slsCbfs[7], hookBroadPerm);
            }else if(slsCbfs.length == 5){
                XposedHelpers.findAndHookMethod(amsCls,"checkBroadcastFromSystem",slsCbfs[0],slsCbfs[1],slsCbfs[2],slsCbfs[3],slsCbfs[4], hookBroadPerm);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^ checkBroadcastFromSystem else 未找到"+slsCbfs.length);
            }
        }else if(isOneOpen||isTwoOpen){
            XposedBridge.log("^^^^^^^^^^^^^^checkBroadcastFromSystem  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        isRecentOpen = settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
        //最近任务隐藏相关
        if(amsMethods.containsKey("createRecentTaskInfoFromTaskRecord")&&isRecentOpen){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    Object recentObj =  methodHookParam.args[0];
                    Method updateMethod = taskRecordCls.getDeclaredMethod("updateTaskDescription");
                    if (updateMethod==null){
                        updateMethod = taskRecordCls.getMethod("updateTaskDescription");
                    }
                    updateMethod.setAccessible(true);
                    updateMethod.invoke(recentObj);
                    Method getBaseMethod = taskRecordCls.getDeclaredMethod("getBaseIntent");
                    if (getBaseMethod==null){
                        getBaseMethod = taskRecordCls.getMethod("getBaseIntent");
                    }
                    getBaseMethod.setAccessible(true);
                    Intent intent = (Intent)(getBaseMethod.invoke(recentObj));
                    recentPrefs.reload();
//                    XposedBridge.log("^^^^^^^^^^^^^^createRecentTaskInfoFromTaskRecord intent  "+intent+"  "+recentPrefs.getAll().size()+"^^^^^^^^^^^^^^^^^");
                    if (intent!=null&&recentPrefs.contains(intent.getComponent().getPackageName()+"/notshow")){
                        Field isAvailableField = taskRecordCls.getDeclaredField("isAvailable");
                        isAvailableField.setAccessible(true);
                        isAvailableField.set(recentObj,!recentPrefs.getBoolean(intent.getComponent().getPackageName()+"/notshow",false));
                    }
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                }
            };
            final Class clss[] = amsMethods.get("createRecentTaskInfoFromTaskRecord").getParameterTypes();
            if(clss.length == 1){
                XposedHelpers.findAndHookMethod(amsCls, "createRecentTaskInfoFromTaskRecord", clss[0],hook);
            }else if(clss.length == 2){
                XposedHelpers.findAndHookMethod(amsCls, "createRecentTaskInfoFromTaskRecord", clss[0],clss[1],hook);
            }else if(clss.length == 3){
                XposedHelpers.findAndHookMethod(amsCls, "createRecentTaskInfoFromTaskRecord", clss[0],clss[1],clss[2],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^createRecentTaskInfoFromTaskRecord else "+clss.length+"函数未找到 ^^^^^^^^^^^^^^^^^");
            }
        }else if(isRecentOpen){
            XposedBridge.log("^^^^^^^^^^^^^^createRecentTaskInfoFromTaskRecord  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        //最近任务保留常驻内存
        if (isAppstart){
            Constructor cons [] = processRecordCls.getDeclaredConstructors();
            if(cons!=null&&cons.length>0){
                Class clss[] = cons [0].getParameterTypes();
                if (clss!=null){
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try{
                            Field infoField = processRecordCls.getDeclaredField("info");
                            infoField.setAccessible(true);
                            ApplicationInfo info = (ApplicationInfo)infoField.get(methodHookParam.thisObject);
                            boolean isNotClean = appStartPrefHMs.containsKey(info.packageName+"/notstop")?(boolean)(appStartPrefHMs.get(info.packageName+"/notstop")):false;
                            if (isNotClean){
                                Field persistentField = processRecordCls.getDeclaredField("persistent");
                                persistentField.setAccessible(true);
                                persistentField.set(methodHookParam.thisObject,true);
//                                XposedBridge.log("^^^^^^^^^^^^^^"+info.packageName+"常驻内存^^^^^^^^^^^^^^^^^");
                            }
                        }catch (RuntimeException e){
                            e.printStackTrace();
                        }
                        }
                    };
                    if (clss.length == 4){
                        XposedHelpers.findAndHookConstructor(processRecordCls,clss[0],clss[1],clss[2],clss[3],hook);
                    }else if (clss.length == 3){
                        XposedHelpers.findAndHookConstructor(processRecordCls,clss[0],clss[1],clss[2],hook);
                    }else if (clss.length == 2){
                        XposedHelpers.findAndHookConstructor(processRecordCls,clss[0],clss[1],hook);
                    }
                }else{
                    XposedBridge.log("^^^^^^^^^^^^^^ProcessRecord0  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                }
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^ProcessRecord1  构造函数未找到 ^^^^^^^^^^^^^^^^^");
            }
            //保活自己
            Class clss[] = XposedUtil.getParmsByName(processRecordCls,"kill");
            if (clss!=null){
                XC_MethodHook hook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Field infoField = processRecordCls.getDeclaredField("info");
                        infoField.setAccessible(true);
                        ApplicationInfo info = (ApplicationInfo) infoField.get(methodHookParam.thisObject);
                        boolean isNotClean = appStartPrefHMs.containsKey(info.packageName + "/notstop") ? (boolean) (appStartPrefHMs.get(info.packageName + "/notstop")) : false;
                        if (Common.PACKAGENAME.equals(info.packageName) && isNotClean && !methodHookParam.args[0].equals("killbyself")) {
                            methodHookParam.setResult(null);
                            return;
                        }
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    }
                };
                if (clss.length == 2){
                    XposedHelpers.findAndHookMethod(processRecordCls,"kill",clss[0],clss[1],hook);
                }
            }
        }
        //最近任务功能需要 自启控制也需要
        final Class activityStackSupervisorCls = XposedHelpers.findClass("com.android.server.am.ActivityStackSupervisor",lpparam.classLoader);
        Method methods[] = activityStackSupervisorCls.getDeclaredMethods();
        Method temp = null;
        for(Method method:methods){
            if (method.getName().equals("anyTaskForIdLocked")){
                temp = method;
                break;
            }
        }
        final Method anyTaskForIdLockedMethod = temp;

        //最近任务保留或移除相关
        if(amsMethods.containsKey("removeTask")&&isRecentOpen){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try {
                    Field mStackSupervisorField = amsCls.getDeclaredField("mStackSupervisor");
                    mStackSupervisorField.setAccessible(true);
                    Object mStackSupervisorObject = mStackSupervisorField.get(methodHookParam.thisObject);
//                      Method anyTaskForIdLockedMethod= assCls.getDeclaredMethod("anyTaskForIdLocked",int.class,boolean.class,int.class);//,boolean.class,int.class
                    anyTaskForIdLockedMethod.setAccessible(true);
                    Object taskRecordObject = null;
                    Class clss[] = anyTaskForIdLockedMethod.getParameterTypes();
                    if(clss.length == 3&&clss[1].getName().equals(boolean.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], true, -1);
                    }else if (clss.length == 1){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0]);
                    }else if (clss.length == 2&&clss[1].getName().equals(boolean.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0],true);
                    }else if (clss.length == 2&&clss[1].getName().equals(int.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0],1);
                    }else if(clss.length == 3&&clss[1].getName().equals(int.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], 1, -1);
                    }
                    if (taskRecordObject!=null){
                        Field mAffiliatedTaskIdField = taskRecordCls.getDeclaredField("mAffiliatedTaskId");
                        Field intentField = taskRecordCls.getDeclaredField("intent");
                        mAffiliatedTaskIdField.setAccessible(true);
                        intentField.setAccessible(true);
                        Object intentObject = intentField.get(taskRecordObject);
                        String pkg = null;
                        if (intentObject != null) {
                            pkg = ((Intent) intentObject).getComponent().getPackageName();
                            if(recentPrefs.hasFileChanged()){
                                recentPrefs.reload();
                            }
                        }
                        boolean isKillFail = false;
                        if (pkg != null &&  recentPrefs.getBoolean(pkg+"/notclean",false)) {
                            methodHookParam.setResult(false);
                            return;
                        }else if(recentPrefs.getBoolean(pkg+"/forceclean",false)){
                            final Object ams = methodHookParam.thisObject;
                            try {
                                if ("com.tencent.mm".equals(pkg)) {
                                    Field recField = methodHookParam.thisObject.getClass().getDeclaredField("mRecentTasks");
                                    recField.setAccessible(true);
                                    ArrayList lists = (ArrayList) (recField.get(methodHookParam.thisObject));
                                    int count = 0;
                                    if (lists.size() > 0) {
                                        if (lists.get(0).getClass().getName().equals("com.android.server.am.TaskRecord")) {
                                            for (Object o : lists) {
                                                Method getBaseMethod = taskRecordCls.getDeclaredMethod("getBaseIntent");
                                                if (getBaseMethod == null) {
                                                    getBaseMethod = taskRecordCls.getMethod("getBaseIntent");
                                                }
                                                getBaseMethod.setAccessible(true);
                                                Intent intentm = (Intent) (getBaseMethod.invoke(o));
                                                if (intentm != null && pkg.equals(intentm.getComponent().getPackageName())) {
                                                    count++;
                                                }
                                            }
                                        }
                                    }
                                    if (count <= 1) {
                                        Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                        m.setAccessible(true);
                                        m.invoke(ams, pkg, 0);
                                    }
                                }else{
                                    Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                    m.setAccessible(true);
                                    m.invoke(ams, pkg, 0);
                                }
                            }catch (RuntimeException e){
                                isKillFail = true;
                            }
                        }
                        final Object ams = methodHookParam.thisObject;
                        if (sysCxtField!=null) {
                            sysCxtField.setAccessible(true);
                            final Context sysCxt = (Context) sysCxtField.get(ams);//(Context)methodHookParam.args[0];
                            if (sysCxt!=null) {
                                Intent intent = new Intent("com.click369.control.removerecent");
                                intent.putExtra("pkg",pkg);
//                                if(isKillFail){
//                                    intent.putExtra("killfail",isKillFail);
//                                }
                                sysCxt.sendBroadcast(intent);
                            }
                        }
                    }else{
                        XposedBridge.log("^^^^^^^^^^^^^^taskRecordObject removeTask 对象获取失败  "+clss.length+"^^^^^^^^^^^^^^^^^");
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            };
            final Class clss[] = amsMethods.get("removeTask").getParameterTypes();
            if(clss.length == 1){
                XposedHelpers.findAndHookMethod(amsCls, "removeTask", clss[0],hook);
            }else if(clss.length == 2){
                XposedHelpers.findAndHookMethod(amsCls, "removeTask", clss[0],clss[1],hook);
            }else if(clss.length == 3){
                XposedHelpers.findAndHookMethod(amsCls, "removeTask", clss[0],clss[1],clss[2],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^removeTask else "+clss.length+"函数未找到 ^^^^^^^^^^^^^^^^^");
            }
        }else if(isRecentOpen){
            XposedBridge.log("^^^^^^^^^^^^^^removeTask  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        if(amsMethods.containsKey("startActivityFromRecents")&&isAppstart){
            final Class clss[] = amsMethods.get("startActivityFromRecents").getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try {
                    Field mStackSupervisorField = amsCls.getDeclaredField("mStackSupervisor");
                    mStackSupervisorField.setAccessible(true);
                    Object mStackSupervisorObject = mStackSupervisorField.get(methodHookParam.thisObject);
                    Object taskRecordObject = null;
                    anyTaskForIdLockedMethod.setAccessible(true);
                    Class clss[] = anyTaskForIdLockedMethod.getParameterTypes();
                    if(clss.length == 3&&clss[1].getName().equals(boolean.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], true, -1);
                    }else if (clss.length == 1){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0]);
                    }else if (clss.length == 2&&clss[1].getName().equals(boolean.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0],true);
                    }else if (clss.length == 2&&clss[1].getName().equals(int.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0],1);
                    }else if(clss.length == 3&&clss[1].getName().equals(int.class.getName())){
                        taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0],1, -1);
                    }
                    if (taskRecordObject!=null) {
                        Field mAffiliatedTaskIdField = taskRecordCls.getDeclaredField("mAffiliatedTaskId");
                        Field intentField = taskRecordCls.getDeclaredField("intent");
                        mAffiliatedTaskIdField.setAccessible(true);
                        intentField.setAccessible(true);
                        Object intentObject = intentField.get(taskRecordObject);
                        String pkg = null;
                        if (intentObject != null) {
                            pkg = ((Intent) intentObject).getComponent().getPackageName();
                        }
                        final Object ams = methodHookParam.thisObject;
                        Field sysCxtField = amsCls.getDeclaredField("mContext");
                        if (sysCxtField != null) {
                            sysCxtField.setAccessible(true);
                            final Context sysCxt = (Context) sysCxtField.get(ams);//(Context)methodHookParam.args[0];
                            if (sysCxt != null) {
                                //给启动判断时发送广播
                                Intent broad1 = new Intent("com.click369.control.test");
                                broad1.putExtra("pkg", pkg);
                                broad1.putExtra("from", lpparam.packageName);
                                broad1.putExtra("class", pkg == null ? null : ((Intent) intentObject).getComponent().getClassName().toString());
                                broad1.putExtra("action", "");
                                sysCxt.sendBroadcast(broad1);
                                autoStartPrefs.reload();
//                                    boolean isLockApp = appStartPrefHMs.containsKey(pkg+"/lockapp")?(Boolean)appStartPrefHMs.get(pkg+"/lockapp"):false;
//                                    boolean isLockAppOk = appStartPrefHMs.containsKey(pkg+"/lockok")?(Boolean)appStartPrefHMs.get(pkg+"/lockok"):false;

//                                    if (pkg != null && isLockApp) {
                                if (pkg != null && autoStartPrefs.getBoolean(pkg + "/lockapp", false)) {
//                                        if (!isLockAppOk) {
                                    if (!autoStartPrefs.getBoolean(pkg + "/lockok", false)) {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
                                        intent.addCategory(Intent.CATEGORY_HOME);
                                        sysCxt.startActivity(intent);
                                        Intent broad = new Intent("com.click369.control.lockapp");
                                        broad.putExtra("pkg", pkg);
                                        broad.putExtra("intent", intentObject == null ? null : (Intent) intentObject);
                                        sysCxt.startActivity(broad);
                                        methodHookParam.setResult(0);
                                        return;
                                    }
                                }
                            }
                        }
                    }else {
                        XposedBridge.log("^^^^^^^^^^^^^^taskRecordObject FromRecents 对象获取失败 "+clss.length+"^^^^^^^^^^^^^^^^^");
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            };
            if(clss.length == 1){
                XposedHelpers.findAndHookMethod(amsCls, "startActivityFromRecents", clss[0],hook);
            }else if(clss.length == 2){
                XposedHelpers.findAndHookMethod(amsCls, "startActivityFromRecents", clss[0],clss[1],hook);
            }else if(clss.length == 3){
                XposedHelpers.findAndHookMethod(amsCls, "startActivityFromRecents", clss[0],clss[1],clss[2],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^startActivityFromRecents else "+clss.length+"函数未找到 ^^^^^^^^^^^^^^^^^");
            }
        }else if(isAppstart){
            XposedBridge.log("^^^^^^^^^^^^^^startActivityFromRecents  函数未找到 ^^^^^^^^^^^^^^^^^");
        }


        if((isOneOpen||isTwoOpen)){
            try {
                final Class brCls = XposedHelpers.findClass(" com.android.server.am.BroadcastRecord", lpparam.classLoader);
                Constructor css[] = brCls.getDeclaredConstructors();
                if(css!=null){
                    Class clss[] = null;
                    for(Constructor con:css){
                        if(con.getParameterTypes().length>10){
                            clss = con.getParameterTypes();
                            break;
                        }
                    }
                    XC_MethodHook hook1 = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            int index = methodHookParam.args[10] instanceof List ?10:methodHookParam.args[11] instanceof List?11:-1;
                            if(index!=-1){
                                List receivers = (List)methodHookParam.args[index];
                                if(receivers!=null&&receivers.size()>0){
//                                    XposedBridge.log("CONTROL -----BroadcastRecord "+receivers.get(0).getClass().getName());
                                    Set removes = new HashSet();
                                    for(Object o:receivers){
                                        if (o.getClass().getName().contains("BroadcastFilter")){
                                            Field nameFiled= o.getClass().getDeclaredField("packageName");
                                            nameFiled.setAccessible(true);
                                            String name = (String)nameFiled.get(o);
                                            if ((isTwoOpen&&muBeiHSs.contains(name))||
                                                    (isOneOpen&&controlHMs.containsKey(name+"/broad"))){
//                                                    if ((isOneOpen&&controlHMs.containsKey(name+"/broad"))){
                                                removes.add(o);
                                            }
                                        }else if(o instanceof ResolveInfo){
                                            ActivityInfo info = ((ResolveInfo)o).activityInfo;
                                            String name = info!=null?info.packageName:"";
                                            if ((isTwoOpen&&muBeiHSs.contains(name))||
                                                    (isOneOpen&&controlHMs.containsKey(name+"/broad"))){
                                                removes.add(o);
                                            }
                                        }
                                    }
                                    receivers.removeAll(removes);
                                }
                            }
                        }catch (RuntimeException e){
                            e.printStackTrace();
                        }
                        }
                    };
                    if (clss!=null){
                        //让7.0及以下生效  8.0强制杀死后不需要
                        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.N ){
                            //有可能导致cpu频率过高。。目前移除
                            if(clss.length == 19){
                                XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],hook1);
                            }else if (clss.length == 20){
                                XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],hook1);
                            }else if(clss.length == 21){
                                XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],clss[20],hook1);
                            }else if(clss.length == 18){
                                XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],hook1);
                            }else {
                                XposedBridge.log("CONTROL BroadcastRecord "+clss.length+"未找到");
                            }
                        }
                    }else{
                        XposedBridge.log("CONTROL BroadcastRecord 未找到0");
                    }
                }else{
                    XposedBridge.log("CONTROL BroadcastRecord 未找到1");
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        try{
            final Class ussCls = XposedHelpers.findClass("com.android.server.usage.UsageStatsService", lpparam.classLoader);
            final Class idleFilterParms[] = XposedUtil.getParmsByName(ussCls,"isAppIdleFilteredOrParoled");
            final Method getidlemethod = XposedUtil.getMethodByName(ussCls,"isAppIdleFilteredOrParoled");
//            XposedUtil.showParmsByName(ussCls,"isAppIdleFilteredOrParoled");
            Method idlemethodTemp = XposedUtil.getMethodByName(ussCls,"setAppIdleAsync");
            if (idlemethodTemp==null){
                idlemethodTemp = XposedUtil.getMethodByName(ussCls,"setAppIdle");
            }
            final Method idlemethod = idlemethodTemp;
            XposedHelpers.findAndHookConstructor(ussCls, Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                    Context context = (Context)methodHookParam.args[0];
                    BroadcastReceiver br = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if ("com.click369.control.uss.setappidle".equals(intent.getAction())){
                                try {
//                                    XposedBridge.log("CONTROL -----设置待机应用");
                                    Set<String> idlePkgs = new HashSet<String>();
                                    if (intent.hasExtra("pkg")){
                                        idlePkgs.add(intent.getStringExtra("pkg"));
                                    }else if(intent.hasExtra("pkgs")){
                                        idlePkgs.addAll((Set<String>)intent.getSerializableExtra("pkgs"));
                                    }
                                    boolean idle = intent.getBooleanExtra("idle",true);
                                    if (idlemethod!=null) {
                                        for(String pkg:idlePkgs){
                                            idlemethod.setAccessible(true);
                                            idlemethod.invoke(methodHookParam.thisObject, pkg, idle, 0);
                                        }
                                    }else{
//                                        XposedBridge.log("CONTROL -----未找到idle函数  ");
                                        XposedUtil.showParmsByName(ussCls,"setAppIdleAsync");
                                        XposedUtil.showParmsByName(ussCls,"setAppIdle");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    XposedBridge.log("CONTROL -----设置待机出错  "+e.getMessage());
                                }
                            }else if ("com.click369.control.uss.getappidlestate".equals(intent.getAction())){
                                try{
//                                    XposedBridge.log("CONTROL -----获取已进入待机应用");
//                                    final Method getidlemethod = ussCls.getDeclaredMethod("isAppIdleFilteredOrParoled",String.class,int.class,long.class,boolean.class);
                                    getidlemethod.setAccessible(true);
                                    HashMap<String,Boolean> pkgs = ( HashMap<String,Boolean>)intent.getSerializableExtra("pkgs");
                                    HashSet<String> newpkgs =new HashSet<String>();
                                    Set<String> keys = pkgs.keySet();
//                                    XposedBridge.log("CONTROL -----获取已进入待机应用  idleFilterParms.length  "+idleFilterParms.length+"  pkgs.size  "+pkgs.size());
                                    if (idleFilterParms!=null&&idleFilterParms.length == 4){
                                        for(String key:keys){
                                            Object isIdle = getidlemethod.invoke(methodHookParam.thisObject,key.trim(),0, SystemClock.elapsedRealtime(),true);
                                            if((Boolean)isIdle==true){
                                                newpkgs.add(key);
                                            }
                                        }
                                    }else if(idleFilterParms!=null&&idleFilterParms.length == 3){
//                                        Method getIdelM = ussCls.getDeclaredMethod("isAppIdleFiltered",String.class,int.class,long.class);
//                                        getIdelM.setAccessible(true);
                                        for(String key:keys){
                                            Object isIdle = getidlemethod.invoke(methodHookParam.thisObject,key.trim(),0, SystemClock.elapsedRealtime());
                                            if((Boolean)isIdle==true){
                                                newpkgs.add(key);
                                            }
                                        }
                                    }
                                    Intent intent1 = new Intent("com.click369.control.recappidlestate");
                                    intent1.putExtra("pkgs",newpkgs);
                                    context.sendBroadcast(intent1);
                                    XposedBridge.log("CONTROL -----获取已进入待机应用的个数  "+newpkgs.size());
                                } catch (Exception e){
                                    e.printStackTrace();
//                                    XposedBridge.log("CONTROL -----获取已进入待机应用的个数出错  "+e.getMessage());
                                }
                            }
                        }
                    };
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("com.click369.control.uss.setappidle");
                    filter.addAction("com.click369.control.uss.getappidlestate");
                    context.registerReceiver(br, filter);
//                    XposedBridge.log("CONTROL -----待机广播注册");
                }
            });
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
            XposedBridge.log("CONTROL -----未找到UsageStatsService NoSuchMethodError "+e);
        }catch (XposedHelpers.ClassNotFoundError e) {
            e.printStackTrace();
            XposedBridge.log("CONTROL -----未找到UsageStatsService ClassNotFoundError "+e);
        }
    }

}
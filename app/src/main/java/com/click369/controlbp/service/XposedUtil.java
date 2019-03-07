package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.TimeUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedUtil {
    //    static long lastReloadTime = 0;
    public static void reloadInfos(Context c,
                                   SharedPreferences autoStartPrefs,
                                   SharedPreferences recentPrefs,
                                   SharedPreferences controlPrefs,
                                   SharedPreferences settingPrefs,
                                   SharedPreferences skipDialogPrefs,
                                   SharedPreferences uiBarpRrefs){
//        if(System.currentTimeMillis()-lastReloadTime<200){
//            return;
//        }
//        lastReloadTime = System.currentTimeMillis();
        boolean isneed = uiBarpRrefs.getBoolean(Common.PREFS_SETTING_UI_ISNEEDFLOATONSYS,false);
//        Log.i("CONTROL","isneed  "+isneed);
        Intent intentb = new Intent("com.click369.control.ams.initreload");
        intentb.putExtra("isNeedFloadOnSys", isneed);
        intentb.putExtra("autoStartPrefs", (Serializable) autoStartPrefs.getAll());
        intentb.putExtra("recentPrefs", (Serializable) recentPrefs.getAll());
        intentb.putExtra("controlPrefs", (Serializable) controlPrefs.getAll());
        intentb.putExtra("settingPrefs", (Serializable) settingPrefs.getAll());
        intentb.putExtra("skipDialogPrefs", (Serializable) skipDialogPrefs.getAll());
        c.sendBroadcast(intentb);
        WatchDogService.isNeedAMSReadLoad= false;
    }
    public  static HashMap<String,Method> getAMSParmas(Class amsCls){
        HashMap<String,Method> hms = new HashMap<String,Method>();
        Method ms[] = amsCls.getDeclaredMethods();
        int i = 0;
        for(Method m:ms){
            if ("removeTask".equals(m.getName())&&!hms.containsKey("removeTask")){
                hms.put("removeTask",m);
                i++;
            }else if ("removeTaskByIdLocked".equals(m.getName())){
                if(hms.containsKey("removeTaskByIdLocked")&&m.getParameterTypes().length>hms.get("removeTaskByIdLocked").getParameterTypes().length){
                    hms.put("removeTaskByIdLocked",m);
                }else if(!hms.containsKey("removeTaskByIdLocked")){
                    hms.put("removeTaskByIdLocked",m);
                }
                i++;
            }else if ("cleanUpRemovedTaskLocked".equals(m.getName())&&!hms.containsKey("cleanUpRemovedTaskLocked")){
                hms.put("cleanUpRemovedTaskLocked",m);
                i++;
            }else if ("createRecentTaskInfoFromTaskRecord".equals(m.getName())&&!hms.containsKey("createRecentTaskInfoFromTaskRecord")){
                hms.put("createRecentTaskInfoFromTaskRecord",m);
                i++;
            }else if ("finishBooting".equals(m.getName())&&!hms.containsKey("finishBooting")){
                hms.put("finishBooting",m);
                i++;
            }else if ("isGetTasksAllowed".equals(m.getName())&&!hms.containsKey("isGetTasksAllowed")){
                hms.put("isGetTasksAllowed",m);
                i++;
            }else if ("checkCallingPermission".equals(m.getName())&&!hms.containsKey("checkCallingPermission")){
                hms.put("checkCallingPermission",m);
                i++;
            }else if ("forceStopPackage".equals(m.getName())&&!hms.containsKey("forceStopPackage")){
                hms.put("forceStopPackage",m);
                i++;
            }else if ("killApplication".equals(m.getName())&&!hms.containsKey("killApplication")){
                hms.put("killApplication",m);
                i++;
            }else if ("killApplicationProcess".equals(m.getName())&&!hms.containsKey("killApplicationProcess")){
                hms.put("killApplicationProcess",m);
                i++;
            }else if ("startActivity".equals(m.getName())&&!hms.containsKey("startActivity")){
                hms.put("startActivity",m);
                i++;
            }else if ("startActivityFromRecents".equals(m.getName())&&!hms.containsKey("startActivityFromRecents")){
                hms.put("startActivityFromRecents",m);
                i++;
            }else if ("broadcastIntentLocked".equals(m.getName())&&!hms.containsKey("broadcastIntentLocked")){
                hms.put("broadcastIntentLocked",m);
                i++;
            }else if ("checkBroadcastFromSystem".equals(m.getName())&&!hms.containsKey("checkBroadcastFromSystem")){
                hms.put("checkBroadcastFromSystem",m);
                i++;
            }else if ("makePackageIdle".equals(m.getName())&&!hms.containsKey("makePackageIdle")){
                hms.put("makePackageIdle",m);
                i++;
            }else if ("startService".equals(m.getName())){
                if(hms.containsKey("startService")&&m.getParameterTypes().length>hms.get("startService").getParameterTypes().length){
                    hms.put("startService",m);
                }else if(!hms.containsKey("startService")){
                    hms.put("startService",m);
                }
                i++;
            }else if ("bindService".equals(m.getName())){
                if(hms.containsKey("bindService")&&m.getParameterTypes().length>hms.get("bindService").getParameterTypes().length){
                    hms.put("bindService",m);
                }else if(!hms.containsKey("bindService")){
                    hms.put("bindService",m);
                }
                i++;
            }else if ("startProcessLocked".equals(m.getName())){
                if(hms.containsKey("startProcessLocked")&&m.getParameterTypes().length>hms.get("startProcessLocked").getParameterTypes().length){
                    hms.put("startProcessLocked",m);
                }else if(!hms.containsKey("startProcessLocked")){
                    hms.put("startProcessLocked",m);
                }
                i++;
            }
//            if (i >= 11){
//                break;
//            }
//            XposedBridge.log("^^^^^^^^^^^^^^AMS  "+m.getName()+"  "+m.getParameterTypes().length+"^^^^^^^^^^^^^^^^^");
        }
        return hms;
    }

    public static Class[] getParmsByName(Class cls,String methodName){
        if(cls!=null){
            Method ms[] = cls.getDeclaredMethods();
            for(Method m:ms){
                if (m.getName().equals(methodName)){
                    return m.getParameterTypes();
                }
            }
        }
        return null;
    }
    public static Class[] getMaxLenParmsByName(Class cls,String methodName){
        if(cls!=null){
            Method ms[] = cls.getDeclaredMethods();
            int n = 0;
            Method mt = null;
            for(Method m:ms){
                if (m.getName().equals(methodName)){
                    if(m.getParameterTypes().length>=n){
                        mt = m;
                    }
                }
            }
            if(mt!=null){
                return mt.getParameterTypes();
            }
            return null;
        }
        return null;
    }
    public static void showParmsByName(Class cls,String methodName){
        if(cls!=null){
            Method ms[] = cls.getDeclaredMethods();
            XposedBridge.log("^^^^^^^^^^^^^^" + cls.getName() + "的函数 "+methodName+"^^^^^^^^^^^^^^^^^");
            for(Method m:ms){
                StringBuilder sb = new StringBuilder();
                for(Class c:m.getParameterTypes()){
                    sb.append(c.getName()).append(",");
                }
                if (methodName!=null&&m.getName().equals(methodName)){
                    XposedBridge.log("^^^^^^^^^^^^^^" + m.getName() +" ("+sb.toString()+ ")^^^^^^^^^^^^^^^^^");
                    continue;
                }
                XposedBridge.log("^^^^^^^^^^^^^^" + m.getName() +" ("+sb.toString()+ ")^^^^^^^^^^^^^^^^^");
            }
        }
    }

    public static void showParmsByNameLog(Class cls,String methodName){
        if(cls!=null){
            Method ms[] = cls.getDeclaredMethods();
            Log.i("","^^^^^^^^^^^^^^" + cls.getName() + "的函数 "+methodName+"^^^^^^^^^^^^^^^^^");
            for(Method m:ms){
                StringBuilder sb = new StringBuilder();
                for(Class c:m.getParameterTypes()){
                    sb.append(c.getName()).append(",");
                }
                if (methodName!=null&&m.getName().equals(methodName)){
                   Log.i("","^^^^^^^^^^^^^^" + m.getName() +" ("+sb.toString()+ ")^^^^^^^^^^^^^^^^^");
                    continue;
                }
//                Log.i("","^^^^^^^^^^^^^^" + m.getName() +" ("+sb.toString()+ ")^^^^^^^^^^^^^^^^^");
            }
        }
    }

    public static Method getMethodByName(Class cls,String methodName){
        if(cls!=null){
            Method ms[] = cls.getDeclaredMethods();
            for(Method m:ms){
                if (m.getName().equals(methodName)){
                    return m;
                }
            }
        }
        return null;
    }

    public static void showFields(Class cls){
        if(cls!=null){
            Field fs[] = cls.getDeclaredFields();
            XposedBridge.log("^^^^^^^^^^^^^^" + cls.getName() + "的属性^^^^^^^^^^^^^^^^^");
            for(Field f:fs){
                XposedBridge.log("^^^^^^^^^^^^^^" + f.getName() +" 类型： "+f.getType().getName() +"^^^^^^^^^^^^^^^^^");
            }
        }
    }

    public static void showMethods(Class cls){
        if(cls!=null){
            Method fs[] = cls.getDeclaredMethods();
            XposedBridge.log("^^^^^^^^^^^^^^" + cls.getName() + "的属性^^^^^^^^^^^^^^^^^");
            for(Method f:fs){
                XposedBridge.log("^^^^^^^^^^^^^^" + f.getName() +" 参数个数： "+f.getParameterTypes().length +"^^^^^^^^^^^^^^^^^");
            }
        }
    }

//    public static void changePersistent(Class amsCls,Object ams,String pkg,boolean persistent){
//        try{
//            Field procsField = amsCls.getDeclaredField("mLruProcesses");
//            procsField.setAccessible(true);
//            ArrayList<Object> procs = (ArrayList<Object>)procsField.get(ams);
//            if(procs!=null&&procs.size()>0){
//                for(Object proc:procs){
//                    Field infoField = proc.getClass().getDeclaredField("info");
//                    infoField.setAccessible(true);
//                    ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
//                    if(pkg.equals(info.packageName)){
//                        Field persistentField = proc.getClass().getDeclaredField("persistent");
//                        persistentField.setAccessible(true);
//                        persistentField.set(proc,persistent);
//                    }
//                }
//            }
//        }catch (Throwable e){
//            e.printStackTrace();
//            XposedBridge.log("^^^^^^^^^^^^^^changePersistent err1 "+ e + "^^^^^^^^^^^^^^^^^");
//        }
//    }

    public static void stopProcess(Class amsCls,Object ams,String pkg){
        try{
            Field procsField = amsCls.getDeclaredField("mLruProcesses");
//            Field mProcessStatsField = amsCls.getDeclaredField("mProcessStats");
            procsField.setAccessible(true);
//            mProcessStatsField.setAccessible(true);
            ArrayList<Object> procs = new ArrayList<Object>((ArrayList<Object>)procsField.get(ams));
//          Object mProcessStats =  mProcessStatsField.get(ams);
            if(procs!=null&&procs.size()>0){
//                Method method = amsCls.getDeclaredMethod("killApplicationProcess",String.class,int.class);
//                method.setAccessible(true);

                for(Object proc:procs){
                    Field infoField = proc.getClass().getDeclaredField("info");
                    infoField.setAccessible(true);

                    ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
//                    if(pkg.equals(info.packageName)){
//                        XposedBridge.log("^^^^^^^^^^^^^^^^^methodKill00  "+info.+" ^^^^^^^^^^^^^^^");
//                    }

                    if(pkg.equals(info.packageName)){
                        Field hasShownUiField = proc.getClass().getDeclaredField("hasShownUi");
                        hasShownUiField.setAccessible(true);
                        boolean hasShowUI = (boolean) hasShownUiField.get(proc);
//                        XposedBridge.log("^^^^^^^^^^^^^^^^^methodKill "+pkg+" hasShowUI "+hasShowUI+" ^^^^^^^^^^^^^^^");
                        if(!hasShowUI){
                            Method methodKill = proc.getClass().getDeclaredMethod("kill",String.class,boolean.class);
                            methodKill.setAccessible(true);
                            methodKill.invoke(proc,"stop "+info.packageName,false);
                        }

//                        method.invoke(ams,info.processName,info.uid);
                            //killApplicationProcess
//                        Field baseField = proc.getClass().getDeclaredField("baseProcessTracker");
//                        baseField.setAccessible(true);
//                        Object processStateObj = baseField.get(proc);
//                        if (isStop) {
//                            Field hasShownUiField = proc.getClass().getDeclaredField("hasShownUi");
//                            hasShownUiField.setAccessible(true);
//                            boolean hasShowUI = (boolean) hasShownUiField.get(proc);
//                            if (!hasShowUI) {
//                                Field persistentField = proc.getClass().getDeclaredField("persistent");
//                                persistentField.setAccessible(true);
//                                persistentField.set(proc,false);
//                                method.invoke(proc, "killbyself", false);
//                            } else {
//    //                          Field cachedField = proc.getClass().getDeclaredField("cached");
//    //                          cachedField.setAccessible(true);
//    //                          cachedField.set(proc,true);
//                                if (processStateObj != null) {
//                                    Method inActMethod = processStateObj.getClass().getDeclaredMethod("makeInactive");
//                                    inActMethod.setAccessible(true);
//                                    inActMethod.invoke(processStateObj);
////                                  XposedBridge.log("^^^^^^^^^^^^^^^^^暂停进程 "+pkg+" ^^^^^^^^^^^^^^^");
//                                }
//                            }
//                        }else{
//
//                            if (processStateObj!=null) {
//                                Method actMethod = processStateObj.getClass().getDeclaredMethod("makeActive");
//                                actMethod.setAccessible(true);
//                                actMethod.invoke(processStateObj);
//                            }
//                        }
                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
            XposedBridge.log("^^^^^^^^^^^^^^stopProcess err1 "+ e + "^^^^^^^^^^^^^^^^^");
        }
    }

//    public static void stopProcess1(Class amsCls, Class processRecordCls, Object ams, String pkg,boolean isStop){
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            try {
//                Field procsField = amsCls.getDeclaredField("mProcessNames");
//                procsField.setAccessible(true);
//                Object procsObj = procsField.get(ams);
//                Field mMapField = procsObj.getClass().getDeclaredField("mMap");
//                mMapField.setAccessible(true);
////                Method getMapMethod = procsObj.getClass().getDeclaredMethod("getMap");
////                getMapMethod.setAccessible(true);
//                ArrayMap processMap = (ArrayMap) mMapField.get(procsObj);
//                SparseArray procs = (SparseArray) (processMap.get(pkg));
//                XposedBridge.log("^^^^^^^^^^^^^^procs "+procs.size() + "^^^^^^^^^^^^^^^^^");
//                if (procs != null && procs.size() > 0) {
//                    Method method = processRecordCls.getDeclaredMethod("kill", String.class, boolean.class);
//                    method.setAccessible(true);
//                    Field baseField = processRecordCls.getDeclaredField("baseProcessTracker");
//                    baseField.setAccessible(true);
//
//                    for (int i = 0;i<procs.size();i++) {
//                        Object proc = procs.get(i);
//                        Object processStateObj = baseField.get(proc);
//                        XposedBridge.log("^^^^^^^^^^^^^^proc "+ proc + "^^^^^^^^^^^^^^^^^");
//                        if (isStop) {
//                            Field hasShownUiField = processRecordCls.getDeclaredField("hasShownUi");
//                            hasShownUiField.setAccessible(true);
//                            boolean hasShowUI = (boolean) hasShownUiField.get(proc);
//                            if (!hasShowUI) {
//                                method.invoke(proc, "kill", false);
//                            } else {
//                                if (processStateObj != null) {
//                                    Method inActMethod = processStateObj.getClass().getDeclaredMethod("makeInactive");
//                                    inActMethod.setAccessible(true);
//                                    inActMethod.invoke(processStateObj);
//                                }
//                            }
//                        }else{
//                            if (processStateObj!=null) {
//                                Method actMethod = processStateObj.getClass().getDeclaredMethod("makeActive");
//                                actMethod.setAccessible(true);
//                                actMethod.invoke(processStateObj);
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                XposedBridge.log("^^^^^^^^^^^^^^stopProcess1 err1 "+ e + "^^^^^^^^^^^^^^^^^");
//            }
//        }else{
//            stopProcess(amsCls,processRecordCls,ams,pkg,isStop);
//        }
//    }

    public static void stopServicesAndroidL(Class amsCls,Class processRecordCls,Object mServicesObject,Object ams,String pkg){
        try{
            Field procsField = amsCls.getDeclaredField("mLruProcesses");
//            Field mProcessStatsField = amsCls.getDeclaredField("mProcessStats");
            procsField.setAccessible(true);
//            mProcessStatsField.setAccessible(true);
            ArrayList<Object> procs = (ArrayList<Object>)procsField.get(ams);
//          Object mProcessStats =  mProcessStatsField.get(ams);
            if(procs!=null&&procs.size()>0){
                Method killMethod = mServicesObject.getClass().getDeclaredMethod("killServicesLocked", processRecordCls, boolean.class);
                killMethod.setAccessible(true);
                for(Object proc:procs){
                    Field infoField = proc.getClass().getDeclaredField("info");
                    infoField.setAccessible(true);
                    ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                    if(pkg.equals(info.packageName)){
                        Field persistentField = proc.getClass().getDeclaredField("persistent");
                        persistentField.setAccessible(true);
                        boolean pers = (Boolean) persistentField.get(proc);
                        if(pers){
                            persistentField.set(proc,false);
                        }
                        killMethod.invoke(mServicesObject,proc,false);
                        if(pers) {
                            persistentField.set(proc, pers);
                        }
                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
            XposedBridge.log("^^^^^^^^^^^^^^stopServicesAndroidL err "+ e + "^^^^^^^^^^^^^^^^^");
        }
    }
//    public static void stopServicesAndroidL1(Class amsCls,Class processRecordCls,Object mServicesObject,Object ams,String pkg){
//        try{
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                Field procsField = amsCls.getDeclaredField("mProcessNames");
//                procsField.setAccessible(true);
//                Object procsObj = procsField.get(ams);
//                Field mMapField = procsObj.getClass().getDeclaredField("mMap");
//                mMapField.setAccessible(true);
////                Method getMapMethod = procsObj.getClass().getDeclaredMethod("getMap");
////                getMapMethod.setAccessible(true);
//                ArrayMap processMap = (ArrayMap) mMapField.get(procsObj);
//                SparseArray procs = (SparseArray) (processMap.get(pkg));
//                if (procs != null && procs.size() > 0) {
//                    Method killMethod = mServicesObject.getClass().getDeclaredMethod("killServicesLocked", processRecordCls, boolean.class);
//                    killMethod.setAccessible(true);
//                    for (int i = 0; i < procs.size(); i++) {
//                        Object proc = procs.get(i);
//                        killMethod.invoke(mServicesObject, proc, false);
//                    }
//                }
//            }else{
//                stopServicesAndroidL(amsCls,processRecordCls,mServicesObject,ams,pkg);
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//            XposedBridge.log("^^^^^^^^^^^^^^stopServicesAndroidL1 err "+ e + "^^^^^^^^^^^^^^^^^");
//        }
//    }

    public static void stopAllProcess(Class amsCls,Class processRecordCls,Object ams,String pkg){
        try{
            Field procsField = amsCls.getDeclaredField("mLruProcesses");
            procsField.setAccessible(true);
            ArrayList<Object> procs = (ArrayList<Object>)procsField.get(ams);
            if(procs!=null&&procs.size()>0){
                Method method = processRecordCls.getDeclaredMethod("kill",String.class,boolean.class);
                method.setAccessible(true);
                for(Object proc:procs){
                    Field infoField = proc.getClass().getDeclaredField("info");
                    infoField.setAccessible(true);
                    ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                    if(pkg.equals(info.packageName)){
                        Field persistentField = proc.getClass().getDeclaredField("persistent");
                        persistentField.setAccessible(true);
                        persistentField.set(proc,false);
                        method.invoke(proc, "killbyself", false);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            XposedBridge.log("^^^^^^^^^^^^^^stopProcess err1 "+ e + "^^^^^^^^^^^^^^^^^");
        }
    }

    public static void hookMethod(ClassLoader classLoader,String cls,String methodName,XC_MethodHook hook){
        Class clsss = XposedUtil.findClass(cls,classLoader);
        if(clsss!=null){
            hookMethod(clsss,XposedUtil.getParmsByName(clsss,methodName),methodName,hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^"+ cls+"#"+methodName+ "未找到^^^^^^^^^^^^^^^^^");
        }
    }
    public static void hookMethod(Class cls,Class clss[],String methodName,XC_MethodHook hook){
        try {
            if(cls==null){
                XposedBridge.log("CONTROL_类未找到");
                return;
            }
            int len = clss!=null?clss.length:0;
            switch (len){
                case 0:
                    XposedHelpers.findAndHookMethod(cls, methodName,hook);
                    break;
                case 1:
                    XposedHelpers.findAndHookMethod(cls, methodName, clss[0], hook);
                    break;
                case 2:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1], hook);
                    break;
                case 3:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], hook);
                    break;
                case 4:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3], hook);
                    break;
                case 5:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4], hook);
                    break;
                case 6:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],hook);
                    break;
                case 7:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],hook);
                    break;
                case 8:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],hook);
                    break;
                case 9:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],hook);
                    break;
                case 10:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],hook);
                    break;
                case 11:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],hook);
                    break;
                case 12:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],hook);
                    break;
                case 13:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],hook);
                    break;
                case 14:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],hook);
                    break;
                case 15:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],hook);
                    break;
                case 16:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],hook);
                    break;
                case 17:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],hook);
                    break;
                case 18:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],hook);
                    break;
                case 19:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],hook);
                    break;
                case 20:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],hook);
                    break;
                case 21:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],clss[20],hook);
                    break;
                case 22:
                    XposedHelpers.findAndHookMethod(cls, methodName,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],clss[20],clss[21],hook);
                    break;


                default:
                    XposedBridge.log("^^^^^^^^^^^^^^"+cls.getName()+"  "+methodName+"函数未找到  "+clss.length+"^^^^^^^^^^^^^^^^^");
                    break;

            }
        }catch (Throwable e){
            e.printStackTrace();
            XposedBridge.log("^^^^^^^^^^^^^^"+cls.getName()+"  "+methodName+"函数未找到  "+e+"^^^^^^^^^^^^^^^^^");
        }

    }
    public static void hookConstructorMethod(Class cls,Class clss[],XC_MethodHook hook){

        try {
            if(cls==null){
                XposedBridge.log("CONTROL_类未找到");
                return;
            }
            int len = clss!=null?clss.length:0;
            switch (len){
                case 0:
                    XposedHelpers.findAndHookConstructor(cls,hook);
                    break;
                case 1:
                    XposedHelpers.findAndHookConstructor(cls, clss[0], hook);
                    break;
                case 2:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1], hook);
                    break;
                case 3:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], hook);
                    break;
                case 4:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3], hook);
                    break;
                case 5:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4], hook);
                    break;
                case 6:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],hook);
                    break;
                case 7:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],hook);
                    break;
                case 8:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],hook);
                    break;
                case 9:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],hook);
                    break;
                case 10:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],hook);
                    break;
                case 11:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],hook);
                    break;
                case 12:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],hook);
                    break;
                case 13:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],hook);
                    break;
                case 14:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],hook);
                    break;
                case 15:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],hook);
                    break;
                case 16:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],hook);
                    break;
                case 17:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],hook);
                    break;
                case 18:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],hook);
                    break;
                case 19:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],hook);
                    break;
                case 20:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],hook);
                    break;
                case 21:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],clss[20],hook);
                    break;
                case 22:
                    XposedHelpers.findAndHookConstructor(cls,clss[0], clss[1],clss[2], clss[3],clss[4],clss[5],clss[6],clss[7],clss[8],clss[9],clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],clss[20],clss[21],hook);
                    break;
                default:
                    XposedBridge.log("^^^^^^^^^^^^^^构造函数未找到  "+cls.getName()+"  "+clss.length+"^^^^^^^^^^^^^^^^^");
                    break;
            }
        }catch (Throwable e){
            e.printStackTrace();
            XposedBridge.log("^^^^^^^^^^^^^^构造函数未找到  "+cls.getName()+"  "+e+"^^^^^^^^^^^^^^^^^");
        }

    }

    //带参数的方法拦截
    public static void hook_methods(Class clzz, String methodName, XC_MethodHook xmh)
    {
        try {
            for (Method method : clzz.getDeclaredMethods())
                if (method.getName().equals(methodName)
                        && !Modifier.isAbstract(method.getModifiers())) {
                    XposedBridge.hookMethod(method, xmh);
                }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
    public static int hook_methodLen(Class clzz, String methodName)
    {
        try {
            int len = 0;
            for (Method method : clzz.getDeclaredMethods())
                if (method.getName().equals(methodName)
                        && !Modifier.isAbstract(method.getModifiers())) {
                    Class cc[] = method.getParameterTypes();
                    boolean cod1 = cc.length>5&&(cc[1].getSimpleName().equals(ApplicationInfo.class.getSimpleName()))&&
                            (cc[4].getSimpleName().equals(String.class.getSimpleName()));
                    if(cod1){
                        if(cc!=null&&len<cc.length){
                            len = cc.length;
                        }
                    }
                }
            return len;
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
        return 0;
    }


    public static Class findClass(String name,ClassLoader loader){
        try {
            Class cls = XposedHelpers.findClass(name,loader);
            return cls;
        }catch (Throwable e){
            e.printStackTrace();
            XposedBridge.log("^^^^^^^^^^^^^^类未找到  "+name+"  "+e+"^^^^^^^^^^^^^^^^^");
        }
        return null;
    }

    public static String getErroInfo(Throwable arg1){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(TimeUtil.changeMils2String(System.currentTimeMillis(),"出错时间:yyyy-MM-dd HH:mm:ss") +"\n系统版本:" +android.os.Build.VERSION.RELEASE+"\n手机型号:" +android.os.Build.MODEL+ "\n错误原因：\n");
        stringBuilder.append(arg1.getMessage() + "\n");
        StackTraceElement[] stackTrace = arg1.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            stringBuilder.append("file:" + stackTrace[i].getFileName() + " class:"
                    + stackTrace[i].getClassName() + " method:"
                    + stackTrace[i].getMethodName() + " line:"
                    + stackTrace[i].getLineNumber() + "\n");
        }
        stringBuilder.append("\n\n");
        return stringBuilder.toString();
    }
}
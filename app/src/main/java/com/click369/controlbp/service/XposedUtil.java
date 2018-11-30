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
import android.util.SparseArray;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import com.click369.controlbp.common.Common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
                                   SharedPreferences controlPrefs,
                                   SharedPreferences muBeiPrefs,
                                   SharedPreferences settingPrefs,
                                   SharedPreferences skipDialogPrefs){
//        if(System.currentTimeMillis()-lastReloadTime<200){
//            return;
//        }
//        lastReloadTime = System.currentTimeMillis();
        Intent intentb = new Intent("com.click369.control.ams.initreload");
        intentb.putExtra("autoStartPrefs", (Serializable) autoStartPrefs.getAll());
        intentb.putExtra("controlPrefs", (Serializable) controlPrefs.getAll());
        intentb.putExtra("muBeiPrefs", (Serializable) muBeiPrefs.getAll());
        intentb.putExtra("settingPrefs", (Serializable) settingPrefs.getAll());
        intentb.putExtra("skipDialogPrefs", (Serializable) skipDialogPrefs.getAll());
        c.sendBroadcast(intentb);
    }
    public  static HashMap<String,Method> getAMSParmas(Class amsCls){
        HashMap<String,Method> hms = new HashMap<String,Method>();
        Method ms[] = amsCls.getDeclaredMethods();
        int i = 0;
        for(Method m:ms){
            if ("removeTask".equals(m.getName())&&!hms.containsKey("removeTask")){
                hms.put("removeTask",m);
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

    public static void changePersistent(Class amsCls,Object ams,String pkg,boolean persistent){
        try{
            Field procsField = amsCls.getDeclaredField("mLruProcesses");
            procsField.setAccessible(true);
            ArrayList<Object> procs = (ArrayList<Object>)procsField.get(ams);
            if(procs!=null&&procs.size()>0){
                for(Object proc:procs){
                    Field infoField = proc.getClass().getDeclaredField("info");
                    infoField.setAccessible(true);
                    ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                    if(pkg.equals(info.packageName)){
                        Field persistentField = proc.getClass().getDeclaredField("persistent");
                        persistentField.setAccessible(true);
                        persistentField.set(proc,persistent);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            XposedBridge.log("^^^^^^^^^^^^^^changePersistent err1 "+ e + "^^^^^^^^^^^^^^^^^");
        }
    }

    public static void stopProcess(Class amsCls,Class processRecordCls,Object ams,String pkg,boolean isStop){
        try{
            Field procsField = amsCls.getDeclaredField("mLruProcesses");
//            Field mProcessStatsField = amsCls.getDeclaredField("mProcessStats");
            procsField.setAccessible(true);
//            mProcessStatsField.setAccessible(true);
            ArrayList<Object> procs = (ArrayList<Object>)procsField.get(ams);
//          Object mProcessStats =  mProcessStatsField.get(ams);
            if(procs!=null&&procs.size()>0){
                Method method = processRecordCls.getDeclaredMethod("kill",String.class,boolean.class);
                method.setAccessible(true);
                for(Object proc:procs){
                    Field infoField = proc.getClass().getDeclaredField("info");
                    infoField.setAccessible(true);
                    ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                    if(pkg.equals(info.packageName)){
                        Field baseField = proc.getClass().getDeclaredField("baseProcessTracker");
                        baseField.setAccessible(true);
                        Object processStateObj = baseField.get(proc);
                        if (isStop) {
                            Field hasShownUiField = proc.getClass().getDeclaredField("hasShownUi");
                            hasShownUiField.setAccessible(true);
                            boolean hasShowUI = (boolean) hasShownUiField.get(proc);
                            if (!hasShowUI) {
                                Field persistentField = proc.getClass().getDeclaredField("persistent");
                                persistentField.setAccessible(true);
                                persistentField.set(proc,false);
                                method.invoke(proc, "killbyself", false);
                            } else {
    //                          Field cachedField = proc.getClass().getDeclaredField("cached");
    //                          cachedField.setAccessible(true);
    //                          cachedField.set(proc,true);
                                if (processStateObj != null) {
                                    Method inActMethod = processStateObj.getClass().getDeclaredMethod("makeInactive");
                                    inActMethod.setAccessible(true);
                                    inActMethod.invoke(processStateObj);
//                                  XposedBridge.log("^^^^^^^^^^^^^^^^^暂停进程 "+pkg+" ^^^^^^^^^^^^^^^");
                                }
                            }
                        }else{

                            if (processStateObj!=null) {
                                Method actMethod = processStateObj.getClass().getDeclaredMethod("makeActive");
                                actMethod.setAccessible(true);
                                actMethod.invoke(processStateObj);
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
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
        }catch (Exception e){
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
}
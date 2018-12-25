package com.click369.controlbp.service;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedAlarm {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,
                                   final XSharedPreferences controlPrefs,
                                   final XSharedPreferences alarmPrefs,
//                                   final XSharedPreferences muBeiPrefs,
                                   final boolean isOneOpen,
                                   final boolean isTwoOpen,
                                   final boolean isMubeiStopOther) {//,final boolean isMubeStopBroad
        if (lpparam.packageName.equals("com.click369.controlbp")||
                lpparam.packageName.equals("com.system.ui")) {
            return;
        }
        final Class pendingIntentClass = XposedHelpers.findClass("android.app.PendingIntent", lpparam.classLoader);
        final Class getbcClss[] = XposedUtil.getParmsByName(pendingIntentClass,"getBroadcast");
        if (getbcClss!=null){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    Intent intent = (Intent) methodHookParam.args[2];
                    if (intent!=null) {
                        XposedHelpers.setAdditionalStaticField(pendingIntentClass, "action", intent.getAction());
                    }
                }
            };
            if (getbcClss.length ==4){
                XposedHelpers.findAndHookMethod(pendingIntentClass,"getBroadcast",getbcClss[0],getbcClss[1],getbcClss[2],getbcClss[3],hook);
            }else if (getbcClss.length ==3){
                XposedHelpers.findAndHookMethod(pendingIntentClass,"getBroadcast",getbcClss[0],getbcClss[1],getbcClss[2],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^^^^getBroadcast else "+getbcClss.length+" ^^^^^^^^^^^^^^^");
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^^^^getBroadcast 未找到 ^^^^^^^^^^^^^^^");
        }
        final Class getserClss[] = XposedUtil.getParmsByName(pendingIntentClass,"getService");
        if (getserClss!=null){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    Intent intent = (Intent) methodHookParam.args[2];
                    if (intent!=null) {
                        XposedHelpers.setAdditionalStaticField(pendingIntentClass, "action", intent.getAction());
                    }
                }
            };
            if (getserClss.length ==4){
                XposedHelpers.findAndHookMethod(pendingIntentClass,"getService",getserClss[0],getserClss[1],getserClss[2],getserClss[3],hook);
            }else if (getserClss.length ==3){
                XposedHelpers.findAndHookMethod(pendingIntentClass,"getService",getserClss[0],getserClss[1],getserClss[2],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^^^^getService else "+getserClss.length+" ^^^^^^^^^^^^^^^");
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^^^^getService 未找到 ^^^^^^^^^^^^^^^");
        }


        final Class alarmManagerClass = XposedHelpers.findClass("android.app.AlarmManager", lpparam.classLoader);
        Constructor cs[] = alarmManagerClass.getDeclaredConstructors();
        if (cs!=null&&cs.length>0){
            final Class clss[] = cs[0].getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Context cxt = null;
                        if (methodHookParam.args[0] instanceof Context){
                            cxt = (Context)methodHookParam.args[0];
                        }else if (clss.length==2&&methodHookParam.args[1] instanceof Context){
                            cxt = (Context)methodHookParam.args[1];
                        }else if (clss.length==3&&methodHookParam.args[2] instanceof Context){
                            cxt = (Context)methodHookParam.args[2];
                        }
                        if (cxt!=null) {
                            //规则时长
                            final HashMap<String, Long[]> alarmRoleTimes = new HashMap<String, Long[]>();
                            XposedHelpers.setAdditionalStaticField(alarmManagerClass, "alarmRoleTimes",alarmRoleTimes);
                            //允许次数
                            final HashMap<String, Integer> alarmAllowCounts = new HashMap<String, Integer>();
                            //不允许次数
                            final HashMap<String, Integer> alarmNotAllowCounts = new HashMap<String, Integer>();
                            //每次允许的唤醒时间 数组中第一个为设定时间 第二个为要唤醒的时间
                            final HashMap<String, ArrayList<Long[]>> alarmAllTimes = new HashMap<String, ArrayList<Long[]>>();
                            //每次不允许的唤醒时间 数组中第一个为设定时间 第二个为要唤醒的时间
                            final HashMap<String, ArrayList<Long[]>> alarmNotAllowAllTimes = new HashMap<String, ArrayList<Long[]>>();
                            //唤醒锁名称
                            final HashMap<String, ArrayList<String>> alarms = new HashMap<String, ArrayList<String>>();
                            XposedHelpers.setAdditionalStaticField(alarmManagerClass, "alarmAllowCounts", alarmAllowCounts);
                            XposedHelpers.setAdditionalStaticField(alarmManagerClass, "alarmNotAllowCounts", alarmNotAllowCounts);

                            XposedHelpers.setAdditionalStaticField(alarmManagerClass, "alarmAllTimes", alarmAllTimes);
                            XposedHelpers.setAdditionalStaticField(alarmManagerClass, "alarmNotAllowAllTimes", alarmNotAllowAllTimes);
                            XposedHelpers.setAdditionalStaticField(alarmManagerClass, "alarms", alarms);
                            class MyReciver extends BroadcastReceiver {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    if (alarms.size() == 0){
                                        return;
                                    }
                                    String action = intent.getAction();
                                    if ("com.click369.alarm.giveinfo".equals(action)) {
                                        Intent intent1 = new Intent("com.click369.alarm.getinfo");
                                        intent1.putExtra("alarmAllowCounts", alarmAllowCounts);
                                        intent1.putExtra("alarmNotAllowCounts", alarmNotAllowCounts);
                                        intent1.putExtra("alarmAllTimes", alarmAllTimes);
                                        intent1.putExtra("alarmNotAllowAllTimes", alarmNotAllowAllTimes);
                                        intent1.putExtra("alarms", alarms);
                                        context.sendBroadcast(intent1);
                                    }else if ("com.click369.alarm.clearinfo".equals(action)) {
                                        alarmAllowCounts.clear();
                                        alarmNotAllowCounts.clear();
                                        alarmAllTimes.clear();
                                        alarmNotAllowAllTimes.clear();
                                        alarms.clear();
                                    }
                                }
                            }
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.click369.alarm.giveinfo");
                            intentFilter.addAction("com.click369.alarm.clearinfo");
                            cxt.registerReceiver(new MyReciver(), intentFilter);
                        }
                    }catch (RuntimeException e){
//                        e.printStackTrace();
                        Log.i("CONTROL","广播强制注销");
                    }
                }
            };
            XposedUtil.hookConstructorMethod(alarmManagerClass, clss,hook);
//            if (clss.length == 2){
//                XposedHelpers.findAndHookConstructor(alarmManagerClass,clss[0],clss[1],hook);
//            }else if (clss.length == 3){
//                XposedHelpers.findAndHookConstructor(alarmManagerClass,clss[0],clss[1],clss[2],hook);
//            }else if (clss.length == 4){
//                XposedHelpers.findAndHookConstructor(alarmManagerClass,clss[0],clss[1],clss[2],clss[3],hook);
//            }else if (clss.length == 1){
//                XposedHelpers.findAndHookConstructor(alarmManagerClass,clss[0],hook);
//            }else{
//                XposedBridge.log("^^^^^^^^^^^^^^^^^AlarmManager "+clss.length+" ^^^^^^^^^^^^^^^");
//            }
        }

        Method ms[] = alarmManagerClass.getDeclaredMethods();
        Class clss[] = null;
        int tagIndexTemp = -1;
        for (Method m : ms) {
            if ("setImpl".equals(m.getName())) {
                clss = m.getParameterTypes();
                for (int i = 0;i<clss.length;i++){
                    Class c = clss[i];
                    if (String.class.equals(c)){
                        tagIndexTemp = i;
                        break;
                    }
                }
                break;
            }
        }
        if (clss != null) {
            //ELAPSED_REALTIME_WAKEUP 2
            //RTC_WAKEUP 0
            final int clssLength = clss.length;
            final int tagIndex = tagIndexTemp;
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
//                    muBeiPrefs.reload();
                    controlPrefs.reload();
                    if ((controlPrefs.getBoolean(lpparam.packageName + "/alarm", false) && isOneOpen)) {
//                    if ((controlPrefs.getBoolean(lpparam.packageName + "/alarm", false) && isOneOpen) ||
//                            (isTwoOpen && isMubeiStopOther&& muBeiPrefs.getInt(lpparam.packageName, -1) == 0 )) {
                        param.setResult(null);
                        return;
                    }
                    alarmPrefs.reload();
                    if(alarmPrefs.getBoolean(Common.PREFS_SETTING_ALARM_LOOK,false)){
                        int type = (Integer) param.args[0];
                        if(type == AlarmManager.RTC_WAKEUP||
                                type == AlarmManager.ELAPSED_REALTIME_WAKEUP){
                            long triggerAtMillis = (Long) param.args[1];
                            long wakeTime = 0;
                            if (type == AlarmManager.ELAPSED_REALTIME_WAKEUP ){
                                wakeTime = triggerAtMillis - SystemClock.elapsedRealtime()+ System.currentTimeMillis();
                            }else{
                                wakeTime = triggerAtMillis;
                            }
                            String pkg = lpparam.packageName;
                            if(("android".equals(lpparam.processName)&&"android".equals(pkg))||!"android".equals(lpparam.processName)){
//
                                String tag = tagIndex!=-1?(String) param.args[tagIndex]:null;
                                tag = tag==null?"NONAME":tag;
                                if (tag.equals("NONAME")){
                                    Object o = XposedHelpers.getAdditionalStaticField(pendingIntentClass, "action");
                                    if (o!=null){
                                        tag = (String)o;
                                        XposedHelpers.setAdditionalStaticField(pendingIntentClass, "action",null);
                                    }
                                }
                                int jianGe = alarmPrefs.getInt(pkg + "+" + tag, 0) * 1000;
                                boolean isAllow = true;
                                if (jianGe > 0) {
                                    long lastTime = getLastAlarmTime(alarmManagerClass,pkg, tag);
                                    if (System.currentTimeMillis() - lastTime < jianGe) {
                                        isAllow = false;
                                    }
                                }else {
                                    String roleName = alarmPrefs.getString(pkg+"/startname","");
                                    if(roleName.length()>3&&tag.startsWith(roleName)){
                                        int roleTime = alarmPrefs.getInt(pkg+"/starttime",0)*1000;
                                        long lastTime = getLastAlarmRoleTime(alarmManagerClass,pkg, roleName);
                                        if (System.currentTimeMillis() - lastTime < roleTime) {
                                            isAllow = false;
                                        }else{
                                            setLastAlarmRoleTime(alarmManagerClass,pkg,roleName,wakeTime);
                                        }
                                    }
                                }
                                saveAlarmInfo(pkg, tag, alarmManagerClass, isAllow,wakeTime);

                                if (!isAllow) {
                                    if(clssLength == 11&&param.args[5]!=null){
                                        PendingIntent pendingIntent = (PendingIntent) param.args[5];
                                        AlarmManager am = (AlarmManager)param.thisObject;
                                        am.cancel(pendingIntent);
                                    }else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M&&clssLength == 11&&param.args[6]!=null){
                                        AlarmManager.OnAlarmListener listener = (AlarmManager.OnAlarmListener) param.args[6];
                                        AlarmManager am = (AlarmManager)param.thisObject;
                                        am.cancel(listener);
                                    }

                                    param.setResult(null);
                                    return;
                                }
                            }
                        }
                    }
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                }
            };
            if (clss.length == 11) {
                XposedHelpers.findAndHookMethod(alarmManagerClass, "setImpl", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10], hook);
            } else if (clss.length == 8) {
                XposedHelpers.findAndHookMethod(alarmManagerClass, "setImpl", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], hook);
            }else if (clss.length == 7) {
                XposedHelpers.findAndHookMethod(alarmManagerClass, "setImpl", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], hook);
            }else if (clss.length == 6) {
                XposedHelpers.findAndHookMethod(alarmManagerClass, "setImpl", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], hook);
            } else if (clss.length == 10) {
                XposedHelpers.findAndHookMethod(alarmManagerClass, "setImpl", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], hook);
            } else if (clss.length == 12) {
                XposedHelpers.findAndHookMethod(alarmManagerClass, "setImpl", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10], clss[11], hook);
            } else if (clss.length == 9) {
                XposedHelpers.findAndHookMethod(alarmManagerClass, "setImpl", clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], hook);
            } else {
                XposedBridge.log("^^^^^^^^^^^^^^^setImpl 未找到 else" + clss.length + "^^^^^^^^^^^^^^^");
            }
        } else {
            XposedBridge.log("^^^^^^^^^^^^^^^setImpl 未找到 "+clss+"  "+tagIndexTemp+"^^^^^^^^^^^^^^^");
        }
    }

    public static void saveAlarmInfo(String pkg,String tag,Class powerMangerClass,boolean isAllow,long wakeTime){
        try {
            Object alarmAllowCountsObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmAllowCounts");
            Object alarmNotAllowCountsObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmNotAllowCounts");
            Object alarmNotAllowAllTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmNotAllowAllTimes");
            Object alarmAllTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmAllTimes");
            Object alarmsObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarms");

//            Object alarmStartAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmStartAllowTimes");
//            Object alarmStartNotAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmStartNotAllowTimes");
            if (alarmAllowCountsObject!=null&&alarmAllowCountsObject instanceof HashMap){
                final HashMap<String,Integer> alarmAllowCounts = (HashMap<String,Integer>)alarmAllowCountsObject;
                final HashMap<String,Integer> alarmNotAllowCounts = (HashMap<String,Integer>)alarmNotAllowCountsObject;
                final HashMap<String,ArrayList<Long[]>> alarmAllTimes = (HashMap<String,ArrayList<Long[]>>)alarmAllTimesObject;
                final HashMap<String,ArrayList<Long[]>> alarmNotAllowAllTimes = (HashMap<String,ArrayList<Long[]>>)alarmNotAllowAllTimesObject;
//                final HashMap<String,Long> alarmStartAllowTimes = (HashMap<String,Long>)alarmStartAllowTimesObject;
//                final HashMap<String,Long> alarmStartNotAllowTimes = (HashMap<String,Long>)alarmStartNotAllowTimesObject;

                if (alarmAllowCounts.size()>1000){
                    alarmAllowCounts.clear();
                }
                if (alarmNotAllowCounts.size()>1000){
                    alarmNotAllowCounts.clear();
                }
                if (alarmAllTimes.size()>1000){
                    alarmAllTimes.clear();
                }
                if(isAllow){
//                    if (!alarmStartAllowTimes.containsKey(pkg+"/"+tag)) {
//                        alarmStartAllowTimes.put(pkg + "/" + tag, System.currentTimeMillis());
//                    }
                    int count = alarmAllowCounts.get(pkg+"/"+tag) == null?0:alarmAllowCounts.get(pkg+"/"+tag);
                    alarmAllowCounts.put(pkg+"/"+tag,++count);
                    if (alarmAllTimes.containsKey(pkg+"/"+tag)){
                        if (alarmAllTimes.get(pkg+"/"+tag).size()>100){
                            alarmAllTimes.get(pkg+"/"+tag).clear();
                        }
                        alarmAllTimes.get(pkg+"/"+tag).add(0,new Long[]{System.currentTimeMillis(),wakeTime});
                    }else{
                        ArrayList<Long[]> ts = new ArrayList<Long[]>();
                        ts.add(new Long[]{System.currentTimeMillis(),wakeTime});
                        alarmAllTimes.put(pkg+"/"+tag,ts);
                    }
//                    XposedBridge.log("^^^^^^^^^^^^^^^setImpl 未找到 null^^^^^^^^^^^^^^^");
                }else{
//                    if (!alarmStartAllowTimes.containsKey(pkg+"/"+tag)&&!alarmStartNotAllowTimes.containsKey(pkg+"/"+tag)) {
//                        alarmStartNotAllowTimes.put(pkg + "/" + tag, System.currentTimeMillis());
//                    }
                    int count = alarmNotAllowCounts.get(pkg+"/"+tag) == null?0:alarmNotAllowCounts.get(pkg+"/"+tag);
                    alarmNotAllowCounts.put(pkg+"/"+tag,++count);
                    if (alarmNotAllowAllTimes.containsKey(pkg+"/"+tag)){
                        if (alarmNotAllowAllTimes.get(pkg+"/"+tag).size()>100){
                            alarmNotAllowAllTimes.get(pkg+"/"+tag).clear();
                        }
                        alarmNotAllowAllTimes.get(pkg+"/"+tag).add(0,new Long[]{System.currentTimeMillis(),wakeTime});
                    }else{
                        ArrayList<Long[]> ts = new ArrayList<Long[]>();
                        ts.add(new Long[]{System.currentTimeMillis(),wakeTime});
                        alarmNotAllowAllTimes.put(pkg+"/"+tag,ts);
                    }
                }
            }
            if (alarmsObject!=null&&alarmsObject instanceof HashMap){
                final HashMap<String,ArrayList<String>> alarms = (HashMap<String,ArrayList<String>>)alarmsObject;
                if (alarms.size()>300){
                    alarms.clear();
                }
                ArrayList<String> wkTags = alarms.get(pkg) == null?new ArrayList<String>(): alarms.get(pkg);
                if (!wkTags.contains(tag)){
                    if (tag.equals("NONAME")){
                        wkTags.add(0,tag);
                    }else{
                        wkTags.add(tag);
                    }
                }
                alarms.put(pkg,wkTags);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static long getLastAlarmTime(Class alarmManagerClass,String pkg,String tag){
        Object alarmAllTimesObject = XposedHelpers.getAdditionalStaticField(alarmManagerClass,"alarmAllTimes");
        if (alarmAllTimesObject!=null&&alarmAllTimesObject instanceof HashMap){
            final HashMap<String,ArrayList<Long[]>> alarmAllTimes = (HashMap<String,ArrayList<Long[]>>)alarmAllTimesObject;
            if (alarmAllTimes.containsKey(pkg+"/"+tag)){
                return alarmAllTimes.get(pkg+"/"+tag).get(0)[0];
            }
        }
        return 0;
    }

    public static long getLastAlarmRoleTime(Class powerMangerClass,String pkg,String roleName){
        Object alarmRoleTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmRoleTimes");
        if (alarmRoleTimesObject!=null&&alarmRoleTimesObject instanceof HashMap){
            final HashMap<String,Long[]> alarmRoleTimes = (HashMap<String,Long[]>)alarmRoleTimesObject;
            if (alarmRoleTimes.containsKey(pkg+"/"+roleName)){
                return alarmRoleTimes.get(pkg+"/"+roleName)[0];
            }
        }
        return 0;
    }

    public static void setLastAlarmRoleTime(Class powerMangerClass,String pkg,String roleName,Long wakeTime){
        Object alarmRoleTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"alarmRoleTimes");
        if (alarmRoleTimesObject!=null&&alarmRoleTimesObject instanceof HashMap){
            final HashMap<String,Long[]> alarmRoleTimes = (HashMap<String,Long[]>)alarmRoleTimesObject;
            alarmRoleTimes.put(pkg+"/"+roleName,new Long[]{System.currentTimeMillis(),wakeTime});
        }
    }
}
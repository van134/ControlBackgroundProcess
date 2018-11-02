package com.click369.controlbp.service;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.PowerManager;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedWakeLock {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences controlPrefs,final XSharedPreferences wakeLockPrefs,final XSharedPreferences muBeiPrefs,final boolean isOneOpen,final boolean isTwoOpen){//,final boolean isMubeStopBroad
        final Class powerMangerClass = XposedHelpers.findClass("android.os.PowerManager", lpparam.classLoader);
        Constructor cs[] = powerMangerClass.getDeclaredConstructors();
        if (cs!=null&&cs.length>0){
           final Class clss[] = cs[0].getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Context cxt = (Context) methodHookParam.args[0];
                        if (cxt!=null&&cxt instanceof Application) {
                            //该唤醒锁是否允许
                            final HashMap<String, Long> wakeLockStartAllowTimes = new HashMap<String, Long>();
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockStartAllowTimes", wakeLockStartAllowTimes);
                            final HashMap<String, Long> wakeLockStartNotAllowTimes = new HashMap<String, Long>();
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockStartNotAllowTimes", wakeLockStartNotAllowTimes);
                            //规则时长
                            final HashMap<String, Long> wakeLockRoleTimes = new HashMap<String, Long>();
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockRoleTimes", wakeLockRoleTimes);
                            //允许次数
                            final HashMap<String, Integer> wakeLockAllowCounts = new HashMap<String, Integer>();
                            //允许时长
                            final HashMap<String, Long> wakeLockAllowTimes = new HashMap<String, Long>();
                            //不允许次数
                            final HashMap<String, Integer> wakeLockNotAllowCounts = new HashMap<String, Integer>();
                            //不允许时长
                            final HashMap<String, Long> wakeLockNotAllowTimes = new HashMap<String, Long>();
                            //每次允许的唤醒时间
                            final HashMap<String, ArrayList<Long>> wakeLockAllTimes = new HashMap<String, ArrayList<Long>>();
                            //每次不允许的唤醒时间
                            final HashMap<String, ArrayList<Long>> wakeLockNotAllowAllTimes = new HashMap<String, ArrayList<Long>>();
                            //唤醒锁名称
                            final HashMap<String, ArrayList<String>> wakeLocks = new HashMap<String, ArrayList<String>>();
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockAllowTimes", wakeLockAllowTimes);
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockNotAllowTimes", wakeLockNotAllowTimes);
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockAllowCounts", wakeLockAllowCounts);
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockNotAllowCounts", wakeLockNotAllowCounts);

                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockAllTimes", wakeLockAllTimes);
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLockNotAllowAllTimes", wakeLockNotAllowAllTimes);
                            XposedHelpers.setAdditionalStaticField(powerMangerClass, "wakeLocks", wakeLocks);
                            class MyReciver extends BroadcastReceiver {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    if (wakeLocks.size() == 0){
                                        return;
                                    }
                                    String action = intent.getAction();
                                    if ("com.click369.wakelock.giveinfo".equals(action)) {
                                        Intent intent1 = new Intent("com.click369.wakelock.getinfo");
                                        intent1.putExtra("wakeLockAllowTimes", wakeLockAllowTimes);
                                        intent1.putExtra("wakeLockNotAllowTimes", wakeLockNotAllowTimes);
                                        intent1.putExtra("wakeLockAllowCounts", wakeLockAllowCounts);
                                        intent1.putExtra("wakeLockNotAllowCounts", wakeLockNotAllowCounts);
                                        intent1.putExtra("wakeLockAllTimes", wakeLockAllTimes);
                                        intent1.putExtra("wakeLockNotAllowAllTimes", wakeLockNotAllowAllTimes);
                                        intent1.putExtra("wakeLocks", wakeLocks);
                                        context.sendBroadcast(intent1);
                                    }else if ("com.click369.wakelock.clearinfo".equals(action)) {
                                        wakeLockAllowCounts.clear();
                                        wakeLockAllowTimes.clear();
                                        wakeLockNotAllowTimes.clear();
                                        wakeLockNotAllowCounts.clear();

                                        wakeLockAllTimes.clear();
                                        wakeLockNotAllowAllTimes.clear();
                                        wakeLocks.clear();
                                    }
                                }
                            }
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.click369.wakelock.giveinfo");
                            intentFilter.addAction("com.click369.wakelock.clearinfo");
                            cxt.registerReceiver(new MyReciver(), intentFilter);
                        }
                    }catch (Exception e){
                    }
                }
            };
            if (clss.length == 3){
                XposedHelpers.findAndHookConstructor(powerMangerClass,clss[0],clss[1],clss[2],hook);
            }else if (clss.length == 4){
                XposedHelpers.findAndHookConstructor(powerMangerClass,clss[0],clss[1],clss[2],clss[3],hook);
            }else if (clss.length == 2){
                XposedHelpers.findAndHookConstructor(powerMangerClass,clss[0],clss[1],hook);
            }else if (clss.length == 1){
                XposedHelpers.findAndHookConstructor(powerMangerClass,clss[0],hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^^^^PowerManager "+clss.length+" ^^^^^^^^^^^^^^^");
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^^^^PowerManager 未找到 ^^^^^^^^^^^^^^^");
        }
        Class wakeLockClass = XposedHelpers.findClass("android.os.PowerManager$WakeLock", lpparam.classLoader);
        try{
            Field temp1 = null;
            Field temp2 = null;
            Field temp3 = null;
            Field temp4 = null;
            Field temp5 = null;
            Field temp6 = null;
            Field fs [] =wakeLockClass.getDeclaredFields();
            for(Field f:fs){
                if("mCount".equals(f.getName())){
                    temp1 = f;
                }else if("mInternalCount".equals(f.getName())){//适配8.0
                    temp5 = f;
                }else if("mExternalCount".equals(f.getName())){//适配8.0
                    temp6 = f;
                }else if("mRefCounted".equals(f.getName())){
                    temp2 = f;
                }else if("mToken".equals(f.getName())){
                    temp3 = f;
                }else if("mHeld".equals(f.getName())){
                    temp4 = f;
                }
            }
            final  Field mCountField = temp1;
            final  Field mRefCountedField = temp2;
            final  Field mTokenField = temp3;
            final  Field mHeldField = temp4;
            final  Field mInternalCountField = temp5;
            final  Field mExternalCountField = temp6;
            XposedHelpers.findAndHookMethod(wakeLockClass, "acquire", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try{
                    mTokenField.setAccessible(true);
                    synchronized (mTokenField.get(param.thisObject)) {
                        Field pkgField = param.thisObject.getClass().getDeclaredField("mPackageName");
                        pkgField.setAccessible(true);
                        String pkg = ((String) pkgField.get(param.thisObject)).trim();
                        Field tagField = param.thisObject.getClass().getDeclaredField("mTag");
                        tagField.setAccessible(true);
                        String tag = ((String) tagField.get(param.thisObject)).trim();
                        boolean isSysProcess = "android".equals(lpparam.processName) || pkg.equals("android") || pkg.equals("com.android.systemui") || pkg.equals("com.android.phone");
                        if (!isSysProcess) {
//                            muBeiPrefs.reload();
                            if ((controlPrefs.getBoolean(lpparam.packageName + "/wakelock", false) && isOneOpen)) {
//                            if ((controlPrefs.getBoolean(lpparam.packageName + "/wakelock", false) && isOneOpen) ||
//                                    (muBeiPrefs.getInt(lpparam.packageName, -1) == 0 && isTwoOpen && isMubeStopBroad)) {
                                boolean isScOff = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                                    Field this$0Field = param.thisObject.getClass().getDeclaredField("this$0");
                                    this$0Field.setAccessible(true);
                                    PowerManager o = (PowerManager) this$0Field.get(param.thisObject);
                                    isScOff = !o.isInteractive();
                                }
                                if(isScOff){
                                    param.setResult(null);
                                    return;
                                }
                            }
                        }
                        wakeLockPrefs.reload();
                        if (wakeLockPrefs.getBoolean(Common.PREFS_SETTING_WAKELOCK_LOOK, false)  && mRefCountedField != null) {
                            boolean isAllow = true;
                            //是否在设置中
//                            boolean isInSet = false;
                            int jianGe = wakeLockPrefs.getInt(pkg + "+" + tag, 0) * 1000;
                            if (jianGe > 0) {
                                long lastTime = getLastWakelockTime(powerMangerClass, pkg, tag);
                                if (System.currentTimeMillis() - lastTime < jianGe) {
                                    isAllow = false;
                                }
                            } else {
                                String roleName = wakeLockPrefs.getString(pkg + "/startname", "");
                                if (roleName.length() > 3 && tag.startsWith(roleName)) {
                                    int roleTime = wakeLockPrefs.getInt(pkg + "/starttime", 0) * 1000;
                                    long lastTime = getLastWakelockRoleTime(powerMangerClass, pkg, roleName);
                                    if (System.currentTimeMillis() - lastTime < roleTime) {
                                        isAllow = false;
                                    } else {
                                        setLastWakelockRoleTime(powerMangerClass, pkg, roleName);
                                    }
                                }
                            }
                            boolean isScOff = true;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//                            Field this$0Field = param.thisObject.getClass().getDeclaredField("this$0");
//                            this$0Field.setAccessible(true);
//                            PowerManager o = (PowerManager) this$0Field.get(param.thisObject);
//                            isScOff = !o.isInteractive();
//                        }
                            saveWakeLockInfo(pkg, tag, powerMangerClass, isAllow, isScOff);
                            if (!isAllow) {
                                if(!isSysProcess&&mHeldField!=null){
                                    mHeldField.setAccessible(true);
                                    mHeldField.set(param.thisObject,true);
                                }
                                param.setResult(null);
                                return;
                            }
                        }
                    }
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                }
            });
            XposedHelpers.findAndHookMethod(wakeLockClass, "acquire", long.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try{
                    mTokenField.setAccessible(true);
                    synchronized (mTokenField.get(param.thisObject)) {
                        Field pkgField = param.thisObject.getClass().getDeclaredField("mPackageName");
                        pkgField.setAccessible(true);
                        String pkg = ((String) pkgField.get(param.thisObject)).trim();
                        Field tagField = param.thisObject.getClass().getDeclaredField("mTag");
                        tagField.setAccessible(true);
                        String tag = ((String) tagField.get(param.thisObject)).trim();
                        boolean isSysProcess = "android".equals(lpparam.processName) || pkg.equals("android") || pkg.equals("com.android.systemui") || pkg.equals("com.android.phone");
                        if (!isSysProcess) {
//                            muBeiPrefs.reload();
                            if ((controlPrefs.getBoolean(lpparam.packageName + "/wakelock", false) && isOneOpen)) {
//                            if ((controlPrefs.getBoolean(lpparam.packageName + "/wakelock", false) && isOneOpen) ||
//                                    (muBeiPrefs.getInt(lpparam.packageName , -1) == 0 && isTwoOpen && isMubeStopBroad)) {
                                param.setResult(null);
                                return;
                            }
                        }
                        wakeLockPrefs.reload();
                        if (wakeLockPrefs.getBoolean(Common.PREFS_SETTING_WAKELOCK_LOOK, false) && mRefCountedField != null) {
                            boolean isAllow = true;
                            int jianGe = wakeLockPrefs.getInt(pkg + "+" + tag, 0) * 1000;
                            if (jianGe > 0) {
                                long lastTime = getLastWakelockTime(powerMangerClass, pkg, tag);
                                if (System.currentTimeMillis() - lastTime < jianGe) {
                                    isAllow = false;
                                }
                            } else {
                                String roleName = wakeLockPrefs.getString(pkg + "/startname", "");
                                if (roleName.length() > 3 && tag.startsWith(roleName)) {
                                    int roleTime = wakeLockPrefs.getInt(pkg + "/starttime", 0) * 1000;
                                    long lastTime = getLastWakelockRoleTime(powerMangerClass, pkg, roleName);
                                    if (System.currentTimeMillis() - lastTime < roleTime) {
                                        isAllow = false;
                                    } else {
                                        setLastWakelockRoleTime(powerMangerClass, pkg, roleName);
                                    }
                                }
                            }
                            saveWakeLockInfo(pkg, tag, powerMangerClass, isAllow, true);
                            if (!isAllow) {
                                if(!isSysProcess&&mHeldField!=null){
                                    mHeldField.setAccessible(true);
                                    mHeldField.set(param.thisObject,true);
                                }
                                boolean isScOff = true;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                                    Field this$0Field = param.thisObject.getClass().getDeclaredField("this$0");
                                    this$0Field.setAccessible(true);
                                    PowerManager o = (PowerManager) this$0Field.get(param.thisObject);
                                    isScOff = !o.isInteractive();
                                }
                                if(isScOff){
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
            });
            XposedHelpers.findAndHookMethod(wakeLockClass, "release", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try{
                    mTokenField.setAccessible(true);
                    synchronized (mTokenField.get(param.thisObject)) {
                        Field pkgField = param.thisObject.getClass().getDeclaredField("mPackageName");
                        pkgField.setAccessible(true);
                        String pkg = ((String) pkgField.get(param.thisObject)).trim();
                        Field tagField = param.thisObject.getClass().getDeclaredField("mTag");
                        tagField.setAccessible(true);
                        String tag = ((String) tagField.get(param.thisObject)).trim();
                        boolean isSysProcess = "android".equals(lpparam.processName) || pkg.equals("android") || pkg.equals("com.android.systemui") || pkg.equals("com.android.phone");
                        if (!isSysProcess) {
//                            muBeiPrefs.reload();
                            if ((controlPrefs.getBoolean(lpparam.packageName + "/wakelock", false) && isOneOpen)) {
//                            if ((controlPrefs.getBoolean(lpparam.packageName + "/wakelock", false) && isOneOpen) ||
//                                    (muBeiPrefs.getInt(lpparam.packageName , -1) == 0 && isTwoOpen && isMubeStopBroad)) {
                                if (mCountField != null && mRefCountedField != null) {
                                    PowerManager.WakeLock pw = (PowerManager.WakeLock) param.thisObject;
                                    mCountField.setAccessible(true);
                                    mRefCountedField.setAccessible(true);
                                    int count = (Integer) mCountField.get(pw);
                                    boolean mRefCounted = (Boolean) mRefCountedField.get(pw);
                                    if (count <= 0 && mRefCounted) {
                                        param.setResult(null);
                                        return;
                                    }
                                }
                            }
                        }
                        wakeLockPrefs.reload();
                        if (wakeLockPrefs.getBoolean(Common.PREFS_SETTING_WAKELOCK_LOOK, false)
                                && mRefCountedField != null) {
                            int jianGe = wakeLockPrefs.getInt(pkg + "+" + tag, 0) * 1000;
                            String roleName = wakeLockPrefs.getString(pkg + "/startname", "");
                            PowerManager.WakeLock pw = (PowerManager.WakeLock) param.thisObject;

                            mRefCountedField.setAccessible(true);
                            int count = 0;
                            if (mCountField!=null){
                                mCountField.setAccessible(true);
                                count = (Integer) mCountField.get(pw);
                            }
                            if(mInternalCountField!=null){
                                mInternalCountField.setAccessible(true);
                                count = (Integer) mInternalCountField.get(pw);
                            }
                            if(count <=0&&mExternalCountField!=null){
                                mExternalCountField.setAccessible(true);
                                count = (Integer) mExternalCountField.get(pw);
                            }

                            boolean mRefCounted = (Boolean) mRefCountedField.get(pw);
                            if (!mRefCounted || count == 1) {
                                boolean isScOff = true;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//                                Field this$0Field = param.thisObject.getClass().getDeclaredField("this$0");
//                                this$0Field.setAccessible(true);
//                                PowerManager o = (PowerManager) this$0Field.get(param.thisObject);
//                                isScOff = !o.isInteractive();
//                            }
                                saveWakeLockTimeMils(powerMangerClass, pkg, tag, isScOff);
                            }
                            if (jianGe > 0 || (roleName.length() > 3 && tag.startsWith(roleName))) {
                                if (count <= 0 && mRefCounted) {
                                    if(!isSysProcess&&mHeldField!=null){
                                        mHeldField.setAccessible(true);
                                        mHeldField.set(param.thisObject,false);
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
            });
        }catch (NoSuchMethodError e){
            XposedBridge.log("^^^^^^^^^^^^^没有找到release（long）^^^^^^^^^^^^^^^");
        }
    }

    public static void saveWakeLockInfo(String pkg,String tag,Class powerMangerClass,boolean isAllow,boolean isScOff){
        try {
            Object wakeLockAllowCountsObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockAllowCounts");
            Object wakeLockNotAllowCountsObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockNotAllowCounts");
            Object wakeLockNotAllowAllTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockNotAllowAllTimes");
            Object wakeLockAllTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockAllTimes");
            Object wakeLocksObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLocks");

            Object wakeLockStartAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockStartAllowTimes");
            Object wakeLockStartNotAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockStartNotAllowTimes");
            if (isScOff&&wakeLockAllowCountsObject!=null&&wakeLockAllowCountsObject instanceof HashMap){
                final HashMap<String,Integer> mwakeLockAllowCounts = (HashMap<String,Integer>)wakeLockAllowCountsObject;
                final HashMap<String,Integer> wakeLockNotAllowCounts = (HashMap<String,Integer>)wakeLockNotAllowCountsObject;
                final HashMap<String,ArrayList<Long>> wakeLockAllTimes = (HashMap<String,ArrayList<Long>>)wakeLockAllTimesObject;
                final HashMap<String,ArrayList<Long>> wakeLockNotAllowAllTimes = (HashMap<String,ArrayList<Long>>)wakeLockNotAllowAllTimesObject;
                final HashMap<String,Long> wakeLockStartAllowTimes = (HashMap<String,Long>)wakeLockStartAllowTimesObject;
                final HashMap<String,Long> wakeLockStartNotAllowTimes = (HashMap<String,Long>)wakeLockStartNotAllowTimesObject;

                if (mwakeLockAllowCounts.size()>1000){
                    mwakeLockAllowCounts.clear();
                }
                if (wakeLockNotAllowCounts.size()>1000){
                    wakeLockNotAllowCounts.clear();
                }
                if (wakeLockAllTimes.size()>1000){
                    wakeLockAllTimes.clear();
                }
                if(isAllow){
                    if (!wakeLockStartAllowTimes.containsKey(pkg+"/"+tag)) {
                        wakeLockStartAllowTimes.put(pkg + "/" + tag, System.currentTimeMillis());
                    }
                    int count = mwakeLockAllowCounts.get(pkg+"/"+tag) == null?0:mwakeLockAllowCounts.get(pkg+"/"+tag);
                    mwakeLockAllowCounts.put(pkg+"/"+tag,++count);
                    if (wakeLockAllTimes.containsKey(pkg+"/"+tag)){
                        if (wakeLockAllTimes.get(pkg+"/"+tag).size()>100){
                            wakeLockAllTimes.get(pkg+"/"+tag).clear();
                        }
                        wakeLockAllTimes.get(pkg+"/"+tag).add(0,System.currentTimeMillis());
                    }else{
                        ArrayList<Long> ts = new ArrayList<Long>();
                        ts.add(System.currentTimeMillis());
                        wakeLockAllTimes.put(pkg+"/"+tag,ts);
                    }
                }else{
                    if (!wakeLockStartAllowTimes.containsKey(pkg+"/"+tag)&&!wakeLockStartNotAllowTimes.containsKey(pkg+"/"+tag)) {
                        wakeLockStartNotAllowTimes.put(pkg + "/" + tag, System.currentTimeMillis());
                    }
                    int count = wakeLockNotAllowCounts.get(pkg+"/"+tag) == null?0:wakeLockNotAllowCounts.get(pkg+"/"+tag);
                    wakeLockNotAllowCounts.put(pkg+"/"+tag,++count);
                    if (wakeLockNotAllowAllTimes.containsKey(pkg+"/"+tag)){
                        if (wakeLockNotAllowAllTimes.get(pkg+"/"+tag).size()>100){
                            wakeLockNotAllowAllTimes.get(pkg+"/"+tag).clear();
                        }
                        wakeLockNotAllowAllTimes.get(pkg+"/"+tag).add(0,System.currentTimeMillis());
                    }else{
                        ArrayList<Long> ts = new ArrayList<Long>();
                        ts.add(System.currentTimeMillis());
                        wakeLockNotAllowAllTimes.put(pkg+"/"+tag,ts);
                    }
                }
            }
            if (wakeLocksObject!=null&&wakeLocksObject instanceof HashMap){
                final HashMap<String,ArrayList<String>> mwakeLocks = (HashMap<String,ArrayList<String>>)wakeLocksObject;
                if (mwakeLocks.size()>300){
                    mwakeLocks.clear();
                }
                ArrayList<String> wkTags = mwakeLocks.get(pkg) == null?new ArrayList<String>(): mwakeLocks.get(pkg);
                if (!wkTags.contains(tag)){
                    wkTags.add(tag);
                }
                mwakeLocks.put(pkg,wkTags);
            }
        }catch (Exception e){
            XposedBridge.log("^^^^^^^^^^^^^^^^^PowerManager error"+e+" ^^^^^^^^^^^^^^^");
            e.printStackTrace();
        }
    }

    public static void saveWakeLockTimeMils(Class powerMangerClass,String pkg,String tag,boolean isScOff){
        if ("android".equals(pkg)&&("*launch*".equals(tag)||"DreamManagerService".equals(tag)||"*alarm*".equals(tag)||"*dexopt*".equals(tag))){
            return;
        }
        Object wakeLockStartAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockStartAllowTimes");
        Object wakeLockStartNotAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockStartNotAllowTimes");
        final HashMap<String,Long> wakeLockStartAllowTimes = (HashMap<String,Long>)wakeLockStartAllowTimesObject;
        final HashMap<String,Long> wakeLockStartNotAllowTimes = (HashMap<String,Long>)wakeLockStartNotAllowTimesObject;
        if (wakeLockStartAllowTimes!=null&&wakeLockStartAllowTimes.containsKey(pkg+"/"+tag)) {
            Object wakeLockAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass, "wakeLockAllowTimes");
            final HashMap<String, Long> wakeLockAllowTimes = (HashMap<String, Long>) wakeLockAllowTimesObject;
            long wakeTime = System.currentTimeMillis() - wakeLockStartAllowTimes.get(pkg+"/"+tag);
            if (wakeTime >0&&isScOff) {
                wakeTime = wakeTime>1000*60*2?0:wakeTime;
                if (wakeLockAllowTimes.containsKey(pkg + "/" + tag)) {
                    wakeLockAllowTimes.put(pkg + "/" + tag, wakeLockAllowTimes.get(pkg + "/" + tag) + wakeTime);
                } else {
                    wakeLockAllowTimes.put(pkg + "/" + tag, wakeTime);
                }
            }
            wakeLockStartAllowTimes.remove(pkg+"/"+tag);
            wakeLockStartNotAllowTimes.remove(pkg+"/"+tag);
        } else if(wakeLockStartNotAllowTimes!=null&&wakeLockStartNotAllowTimes.containsKey(pkg+"/"+tag)){
            Object wakeLockNotAllowTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass, "wakeLockNotAllowTimes");
            final HashMap<String, Long> wakeLockNotAllowTimes = (HashMap<String, Long>) wakeLockNotAllowTimesObject;
            long wakeTime = System.currentTimeMillis() - wakeLockStartNotAllowTimes.get(pkg+"/"+tag);
            if (wakeTime > 0&&isScOff) {
                wakeTime = wakeTime>1000*60*2?0:wakeTime;
                if (wakeLockNotAllowTimes.containsKey(pkg + "/" + tag)) {
                    wakeLockNotAllowTimes.put(pkg + "/" + tag, wakeLockNotAllowTimes.get(pkg + "/" + tag) + wakeTime);
                } else {
                    wakeLockNotAllowTimes.put(pkg + "/" + tag, wakeTime);
                }
            }
            wakeLockStartNotAllowTimes.remove(pkg+"/"+tag);
        }
    };

    public static long getLastWakelockTime(Class powerMangerClass,String pkg,String tag){
        Object wakeLockAllTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockAllTimes");
        if (wakeLockAllTimesObject!=null&&wakeLockAllTimesObject instanceof HashMap){
            final HashMap<String,ArrayList<Long>> wakeLockAllTimes = (HashMap<String,ArrayList<Long>>)wakeLockAllTimesObject;
            if (wakeLockAllTimes.containsKey(pkg+"/"+tag)){
                return wakeLockAllTimes.get(pkg+"/"+tag).get(0);
            }
        }
        return 0;
    }

    public static long getLastWakelockRoleTime(Class powerMangerClass,String pkg,String roleName){
        Object wakeLockRoleTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockRoleTimes");
        if (wakeLockRoleTimesObject!=null&&wakeLockRoleTimesObject instanceof HashMap){
            final HashMap<String,Long> wakeLockTimes = (HashMap<String,Long>)wakeLockRoleTimesObject;
            if (wakeLockTimes.containsKey(pkg+"/"+roleName)){
                return wakeLockTimes.get(pkg+"/"+roleName);
            }
        }
        return 0;
    }

    public static void setLastWakelockRoleTime(Class powerMangerClass,String pkg,String roleName){
        Object wakeLockRoleTimesObject = XposedHelpers.getAdditionalStaticField(powerMangerClass,"wakeLockRoleTimes");
        if (wakeLockRoleTimesObject!=null&&wakeLockRoleTimesObject instanceof HashMap){
            final HashMap<String,Long> wakeLockTimes = (HashMap<String,Long>)wakeLockRoleTimesObject;
            wakeLockTimes.put(pkg+"/"+roleName,System.currentTimeMillis());
        }
    }
}
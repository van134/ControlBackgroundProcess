package com.click369.controlbp.service;

import android.app.Application;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.service.notification.StatusBarNotification;
import android.util.SparseArray;

import com.click369.controlbp.common.Common;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    static boolean isSkipAdOpen = true;
    static boolean isStopScanMedia = false;
    static boolean isMubeiStopOther = false;
    final static HashMap<String,Object> appStartPrefHMs = new HashMap<String,Object>();
    final static HashSet<String> muBeiHSs = new HashSet<String>();
    final static HashSet<String> notifySkipKeyWords = new HashSet<String>();
    final static HashMap<String,Object> controlHMs = new HashMap<String,Object>();
//    final static HashMap<String,Boolean> mubeiStopOtherProc = new HashMap<String,Boolean>();

    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,
                                   final XSharedPreferences settingPrefs,
                                   final XSharedPreferences controlPrefs,
                                   final XSharedPreferences autoStartPrefs,
//                                   final XSharedPreferences muBeiPrefs,
                                   final XSharedPreferences recentPrefs,
                                   final XSharedPreferences skipDialogPrefs){

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
                                    IntentFilter intentFilter = new IntentFilter();
                                    intentFilter.addAction("com.click369.control.getinitinfo");
                                    intentFilter.addAction("com.click369.control.startservice");
                                    app.registerReceiver(new BroadcastReceiver() {
                                        @Override
                                        public void onReceive(Context context, Intent intent) {
                                            try {
                                                String action = intent.getAction();
                                                if("com.click369.control.getinitinfo".equals(action)){
                                                    autoStartPrefs.reload();
                                                    controlPrefs.reload();
//                                                    muBeiPrefs.reload();
                                                    settingPrefs.reload();
                                                    skipDialogPrefs.reload();
                                                    XposedUtil.reloadInfos(context,autoStartPrefs,controlPrefs,settingPrefs,skipDialogPrefs);
                                                }else if("com.click369.control.startservice".equals(action)) {
                                                    try {
                                                        Intent explicitIntent = new Intent("com.click369.service");
                                                        ComponentName component = new ComponentName(Common.PACKAGENAME, Common.PACKAGENAME + ".service.WatchDogService");
                                                        explicitIntent.setComponent(component);
                                                        app.getApplicationContext().startService(explicitIntent);
                                                        XposedBridge.log("应用控制器：启动WatchDogService成功");
                                                    } catch (Throwable e) {
                                                        XposedBridge.log("应用控制器：启动WatchDogService失败" + e);
                                                    }
                                                }
                                            }catch (Throwable e){
                                                e.printStackTrace();
                                            }
                                        }
                                    },intentFilter);
                                }
                            } catch (Throwable e) {
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
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        if (!lpparam.packageName.equals("android")){
            return;
        }
        autoStartPrefs.reload();
        controlPrefs.reload();
//        muBeiPrefs.reload();
        appStartPrefHMs.putAll(autoStartPrefs.getAll());
        controlHMs.putAll(controlPrefs.getAll());
//        muBeiHSs.addAll(muBeiPrefs.getAll().keySet());
        settingPrefs.reload();
//        mubeiStopOtherProc.put(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false));
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
//                                                        XposedBridge.log("^^^^^^^^^^^^^^^^^墓碑 "+pkg+" ^^^^^^^^^^^^^^^");
//                                                        settingPrefs.reload();
//                                                        mubeiStopOtherProc.put(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false));
//                                                        if(mubeiStopOtherProc.get(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)){
                                                        if(isMubeiStopOther){
                                                            XposedUtil.stopProcess(amsCls,processRecordCls,ams,pkg,true);
                                                        }
                                                    }
                                                }
                                            } catch (Throwable e) {
                                                e.printStackTrace();
                                                XposedBridge.log("^^^^^^^^^^^^^^^^^forcestopservice error "+e+" ^^^^^^^^^^^^^^^");
                                            }
                                        }else if("com.click369.control.ams.confirmforcestop".equals(action)||
                                                "com.click369.control.ams.checktimeoutapp".equals(action)){//确认是否杀死该进程
                                            try {
                                                boolean isCheckTimeout = "com.click369.control.ams.checktimeoutapp".equals(action);
                                                long timeout = 0;
                                                if(isCheckTimeout){
                                                    timeout = intent.getLongExtra("timeout",1000*60*60*12);
                                                }
                                                HashSet<String> pkgs = (HashSet<String>)intent.getSerializableExtra("pkgs");
                                                Field procListField = amsCls.getDeclaredField("mLruProcesses");
                                                procListField.setAccessible(true);
                                                ArrayList procs = new ArrayList();
                                                ArrayList procsTemp = (ArrayList)procListField.get(ams);
                                                if(procsTemp!=null&&procsTemp.size()>0){
                                                    procs.addAll(procsTemp);
                                                    HashSet<String> stopPkgs = new HashSet<String>();
                                                    ArrayList newProcs = new ArrayList(procs);
                                                    for (Object proc:newProcs){
                                                        Field infoField = proc.getClass().getDeclaredField("info");
                                                        infoField.setAccessible(true);
                                                        Field lastActivityTimeField = proc.getClass().getDeclaredField("lastActivityTime");
                                                        lastActivityTimeField.setAccessible(true);
                                                        Field activitiesField = proc.getClass().getDeclaredField("activities");
                                                        activitiesField.setAccessible(true);
                                                        ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                                                        if(info.packageName.equals(info.processName)&&pkgs.contains(info.packageName)){
                                                            if(isCheckTimeout){
                                                                long lastActivityTime = (Long)lastActivityTimeField.get(proc);
                                                                //超时12小时没有打开过就杀死
                                                                if(SystemClock.uptimeMillis()-lastActivityTime>timeout){
                                                                    Field persistentField = proc.getClass().getDeclaredField("persistent");
                                                                    persistentField.setAccessible(true);
                                                                    persistentField.set(proc,false);
                                                                    Message msg = new Message();
                                                                    msg.obj = info.packageName;
                                                                    h.sendMessage(msg);
                                                                    stopPkgs.add(info.packageName);
                                                                }
                                                            }else{
                                                                ArrayList actList = (ArrayList)activitiesField.get(proc);
                                                                if(actList.size()==0){
                                                                    Field persistentField = proc.getClass().getDeclaredField("persistent");
                                                                    persistentField.setAccessible(true);
                                                                    persistentField.set(proc,false);
                                                                    Message msg = new Message();
                                                                    msg.obj = info.packageName;
                                                                    h.sendMessage(msg);
                                                                    stopPkgs.add(info.packageName);
                                                                }
                                                            }
                                                        }
                                                        if(stopPkgs.size()>0){
                                                            Intent intent1 = new Intent("com.click369.control.amsstoppkg");
                                                            intent1.putExtra("pkgs",stopPkgs);
                                                            sysCxt.sendBroadcast(intent1);
                                                        }
                                                    }
                                                }
                                            }catch (Throwable e){
                                                e.printStackTrace();
                                            }
                                        }else if("com.click369.control.ams.getprocinfo".equals(action)){
                                            try {
                                                final  HashMap<String,Long> procTimeInfos = new HashMap<String,Long>();
                                                Field procListField = amsCls.getDeclaredField("mLruProcesses");
                                                procListField.setAccessible(true);
                                                ArrayList procs = new ArrayList();
                                                ArrayList procsTemp = (ArrayList)procListField.get(ams);
                                                if(procsTemp!=null&&procsTemp.size()>0){
                                                    procs.addAll(procsTemp);
                                                    for (Object proc:procs){
                                                        Field infoField = proc.getClass().getDeclaredField("info");
                                                        infoField.setAccessible(true);
//                                                        Field interactionEventTimeField = proc.getClass().getDeclaredField("interactionEventTime");
//                                                        interactionEventTimeField.setAccessible(true);
                                                        Field lastActivityTimeField = proc.getClass().getDeclaredField("lastActivityTime");
                                                        lastActivityTimeField.setAccessible(true);
                                                        Field hasShownUiField = proc.getClass().getDeclaredField("hasShownUi");
                                                        hasShownUiField.setAccessible(true);
//                                                        Field hasOverlayUiField = proc.getClass().getDeclaredField("hasOverlayUi");
//                                                        hasOverlayUiField.setAccessible(true);

                                                        ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                                                        if(info.packageName.equals(info.processName)){
                                                            long lastActivityTime = (Long)lastActivityTimeField.get(proc);
//                                                            boolean hasShownUi = (Boolean)hasShownUiField.get(proc);
                                                            procTimeInfos.put(info.packageName,lastActivityTime);
                                                        }
//                                                        long interactionEventTime = (Long)interactionEventTimeField.get(proc);
//                                                        boolean hasOverlayUi = (Boolean)hasOverlayUiField.get(proc);

//                                                        infos.put("interactionEventTime",interactionEventTime);
//                                                        infos.put("lastActivityTime",lastActivityTime);
//                                                        infos.put("hasShownUi",hasShownUi);
//                                                        infos.put("hasOverlayUi",hasOverlayUi);
//                                                        procInfos.put(info.packageName+"+"+info.processName,infos);
//                                                        XposedBridge.log("CONTROL  "+info.packageName+"  "+info.processName+"  "+hasShownUi);
                                                    }
                                                    Intent intent1 = new Intent("com.click369.control.backprocinfo");
                                                    intent1.putExtra("infos",procTimeInfos);
                                                    sysCxt.sendBroadcast(intent1);
                                                }
                                            }catch (Throwable e){
                                                e.printStackTrace();
                                            }
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
//                                            settingPrefs.reload();
//                                            mubeiStopOtherProc.put(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false));
//                                            if(mubeiStopOtherProc.get(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)) {
                                            if(isMubeiStopOther) {
                                                XposedUtil.stopProcess(amsCls, processRecordCls, ams, apk, false);
                                            }
                                        }else if("com.click369.control.ams.reloadcontrol".equals(action)){
                                            if (controlPrefs.hasFileChanged()) {
                                                controlHMs.clear();
                                                controlPrefs.reload();
                                                controlHMs.putAll(controlPrefs.getAll());
                                                XposedBridge.log("^^^^^^^^^^^^^^重载CONTROL " + controlHMs.size() + "^^^^^^^^^^^^^^^^^");
                                            }
                                        }
//                                        else if("com.click369.control.ams.reloadmubei".equals(action)){
//                                            if (muBeiPrefs.hasFileChanged()) {
//                                                muBeiPrefs.reload();
//                                                muBeiHSs.clear();
//                                                Set<String> keys = muBeiPrefs.getAll().keySet();
//                                                for (String k : keys) {
//                                                    if (muBeiPrefs.getInt(k, -1) == 0) {
//                                                        muBeiHSs.add(k);
//                                                    }
//                                                }
//                                                XposedBridge.log("^^^^^^^^^^^^^^重载墓碑 " + muBeiHSs.size() + "^^^^^^^^^^^^^^^^^");
//                                            }
//                                        }
                                        else if("com.click369.control.ams.reloadautostart".equals(action)){
                                            if (autoStartPrefs.hasFileChanged()&&autoStartPrefs.getAll().size()>0) {
                                                autoStartPrefs.reload();
                                                appStartPrefHMs.clear();
                                                appStartPrefHMs.putAll(autoStartPrefs.getAll());
                                            }
//                                            XposedBridge.log("^^^^^^^^^^^^^^重载自启动 " + appStartPrefHMs.size() + "^^^^^^^^^^^^^^^^^");
                                        }else if("com.click369.control.ams.reloadskipnotify".equals(action)){
                                            notifySkipKeyWords.clear();
                                            Set<String> sets = (Set<String>)((Map)intent.getSerializableExtra("skipDialogPrefs")).get(Common.PREFS_SKIPNOTIFY_KEYWORDS);
                                            if(sets!=null){
                                                notifySkipKeyWords.addAll(sets);
                                            }
                                        }else if("com.click369.control.ams.initreload".equals(action)){
                                            Map autoMap = (Map)intent.getSerializableExtra("autoStartPrefs");
                                            Map controlMap = (Map)(Map)intent.getSerializableExtra("controlPrefs");
//                                            Map mbMap = (Map)intent.getSerializableExtra("muBeiPrefs");
                                            Set<String> skipDiaSet = (Set<String>)((Map)intent.getSerializableExtra("skipDialogPrefs")).get(Common.PREFS_SKIPNOTIFY_KEYWORDS);
                                            if(autoMap!=null) {
                                                appStartPrefHMs.clear();
                                                appStartPrefHMs.putAll(autoMap);
//                                                XposedBridge.log("CONTROL--------------auto "+autoMap.size());
                                            }
                                            if(controlMap!=null) {
                                                controlHMs.clear();
                                                controlHMs.putAll(controlMap);
//                                                XposedBridge.log("CONTROL--------------control "+controlMap.size());
                                            }
//                                            if(mbMap!=null){
//                                                muBeiHSs.clear();
//                                                Set<String> keys = mbMap.keySet();
//                                                for (String k : keys) {
//                                                    if (muBeiPrefs.getInt(k, -1) == 0) {
//                                                        muBeiHSs.add(k);
//                                                    }
//                                                }
////                                                muBeiHSs.addAll(mbMap.keySet());
////                                                XposedBridge.log("CONTROL--------------muBei "+mbMap.size());
//                                            }

                                            if(skipDiaSet!=null){
                                                notifySkipKeyWords.clear();
                                                notifySkipKeyWords.addAll(skipDiaSet);
//                                                XposedBridge.log("CONTROL--------------skip "+skipDiaSet.size());
                                            }
                                            Map settingMap = (Map)intent.getSerializableExtra("settingPrefs");
                                            isOneOpen = settingMap.containsKey(Common.ALLSWITCH_ONE)?(boolean)settingMap.get(Common.ALLSWITCH_ONE):true;//settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
                                            isTwoOpen = settingMap.containsKey(Common.ALLSWITCH_TWO)?(boolean)settingMap.get(Common.ALLSWITCH_TWO):true;//settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
                                            isAppstart = settingMap.containsKey(Common.ALLSWITCH_FIVE)?(boolean)settingMap.get(Common.ALLSWITCH_FIVE):true;//settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true);
                                            isRecentOpen = settingMap.containsKey(Common.ALLSWITCH_FOUR)?(boolean)settingMap.get(Common.ALLSWITCH_FOUR):true;//settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
                                            isSkipAdOpen = settingMap.containsKey(Common.ALLSWITCH_NINE)?(boolean)settingMap.get(Common.ALLSWITCH_FOUR):true;//settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
                                            isStopScanMedia = settingMap.containsKey(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA)?(boolean)settingMap.get(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA):false;//settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA,false);
                                            isMubeiStopOther = settingMap.containsKey(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)?(boolean)settingMap.get(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC):false;;
                                        }
                                    }catch (Throwable e){
                                        e.printStackTrace();
//                                        StackTraceElement[] stackTrace = e.getStackTrace();
//                                        String err = "";
//                                        if(stackTrace!=null&&stackTrace.length>0){
//                                            err = "file:" + stackTrace[0].getFileName() + " class:"
//                                                    + stackTrace[0].getClassName() + " method:"
//                                                    + stackTrace[0].getMethodName() + " line:"
//                                                    + stackTrace[0].getLineNumber() + "\n";
//                                        }
                                        XposedBridge.log("^^^^^^^^^^^^^^AMS广播出错 " + e + "^^^^^^^^^^^^^^^^^");
//                                        XposedBridge.log("^^^^^^^^^^^^^^AMS广播出错 " + e +err+ "^^^^^^^^^^^^^^^^^");
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
//                                filter.addAction("com.click369.control.ams.reloadmubei");
                                filter.addAction("com.click369.control.ams.reloadautostart");
                                filter.addAction("com.click369.control.ams.initreload");
                                filter.addAction("com.click369.control.ams.reloadskipnotify");
                                filter.addAction("com.click369.control.ams.confirmforcestop");
                                filter.addAction("com.click369.control.ams.checktimeoutapp");
                                filter.addAction(Intent.ACTION_SCREEN_ON);
                                sysCxt.registerReceiver(br, filter);
//                                XposedBridge.log("^^^^^^^^^^^^^^开机启动注册广播： "+ sysCxt + "^^^^^^^^^^^^^^^^^");
                                sysCxt.sendBroadcast(new Intent("com.click369.control.getinitinfo"));
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
            XposedUtil.hookMethod(amsCls,clss,"finishBooting",hook);
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
            XposedUtil.hookMethod(amsCls,clss,"forceStopPackage",hook);
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
            XposedUtil.hookMethod(amsCls,clss,"checkCallingPermission",hook);
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
            XposedUtil.hookMethod(amsCls,clss,"isGetTasksAllowed",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^isGetTasksAllowed  函数未找到^^^^^^^^^^^^^^^^^");
        }
        isAppstart = settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true);
        //自启控制中自启动
        if(amsMethods.containsKey("startProcessLocked")){
            final Class clss[] = amsMethods.get("startProcessLocked").getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                    try{
                        if(isAppstart){
                            String processName = (String)methodHookParam.args[0];
                            ApplicationInfo info = (ApplicationInfo)methodHookParam.args[1];
                            if(!"android".equals(processName)&&!"android".equals(info.packageName)){
                                autoStartPrefs.reload();
                                String hostingType = (String)methodHookParam.args[4];
                                boolean isAutoHM = appStartPrefHMs.containsKey(info.packageName+"/autostart")?(Boolean)(appStartPrefHMs.get(info.packageName+"/autostart")):false;
                                if(isAutoHM){//||autoStartPrefs.getBoolean(info.packageName+"/autostart",false)
                                    boolean keepIfLarge = clss!=null&&clss.length>=14?((methodHookParam.args[9] instanceof Boolean)?(Boolean) methodHookParam.args[9]:true):true;
                                    Method getProcessRecordLocked = amsCls.getDeclaredMethod("getProcessRecordLocked",String.class,int.class,boolean.class);
                                    getProcessRecordLocked.setAccessible(true);
                                        Object processObj = getProcessRecordLocked.invoke(methodHookParam.thisObject,info.packageName,info.uid,keepIfLarge);
                                        ComponentName cn = (ComponentName) methodHookParam.args[5];
                                        if(appStartPrefHMs.containsKey(info.packageName+"/checkautostart")){
                                            Object jumpAct = appStartPrefHMs.get(info.packageName+"/jumpactivity");
                                            Object homeAct = appStartPrefHMs.get(info.packageName+"/homeactivity");
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
                                }else if(appStartPrefHMs.containsKey(info.packageName+"/stopapp")&&appStartPrefHMs.get(info.packageName+"/stopapp")==(Boolean)true){
                                    Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                    m.invoke(methodHookParam.thisObject, info.packageName, 0);
                                    methodHookParam.setResult(null);
                                    return;
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(amsCls,clss,"startProcessLocked",hook);

        }else{
            XposedBridge.log("^^^^^^^^^^^^^^startProcessLocked  函数未找到^^^^^^^^^^^^^^^^^");
        }
        isOneOpen = settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
        isTwoOpen = settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
        isStopScanMedia = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA,false);
        isMubeiStopOther = settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false);
        if(amsMethods.containsKey("startService")){
            final Class clss[] = amsMethods.get("startService").getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    if(isOneOpen||isTwoOpen) {
                        Intent intent = (Intent) methodHookParam.args[1];
                        String callingPkg = null;
                        if (methodHookParam.args[clss.length - 2] instanceof String) {
                            callingPkg = (String) methodHookParam.args[clss.length - 2];
                        } else {
                            for (int i = clss.length - 1; i >= 0; i--) {
                                if (methodHookParam.args[i] instanceof String) {
                                    callingPkg = (String) methodHookParam.args[i];
                                    break;
                                }
                            }
                        }
                        if (SystemClock.elapsedRealtime() < 1000 * 60 * 2 && isStopScanMedia) {
                            if (intent != null && intent.getComponent() != null && intent.getComponent().getClassName().endsWith("MediaScannerService")) {
//                            XposedBridge.log("CONTROL  "+intent.getComponent().getClassName());
                                Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                m.invoke(methodHookParam.thisObject, callingPkg, 0);
                                methodHookParam.setResult(intent == null ? new ComponentName("", "") : intent.getComponent());
                                return;
                            }
                        }
//                        XposedBridge.log("CONTROL  start isOneOpen " + isOneOpen + "  isTwoOpen " + isTwoOpen + "  isStopScanMedia " + isStopScanMedia + "  isMubeiStopOther " + isMubeiStopOther);
//                    XposedBridge.log("CONTROL  start service "+callingPkg+"  "+intent+" "+muBeiHSs.contains(callingPkg)+"  "+controlHMs.containsKey(callingPkg+"/service"));
                        if ((muBeiHSs.contains(callingPkg) && isTwoOpen) || (controlHMs.containsKey(callingPkg + "/service")&&controlHMs.get(callingPkg + "/service")==(Boolean)true && isOneOpen)) {
//                            String apk = intent == null ? "" : intent.getComponent() == null ? "" : intent.getComponent().getPackageName();
                            if (intent != null && intent.getComponent() != null && controlHMs.containsKey(intent.getComponent().getClassName() + "/service")) {
                            } else {
//                                XposedBridge.log("^^^^^^^^^^^^^^AMS启动服务 被阻止 " + callingPkg + "  " + intent + "^^^^^^^^^^^^^^^^^");
                                methodHookParam.setResult(intent == null ? new ComponentName("", "") : intent.getComponent());
                                return;
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    XposedBridge.log("^^^^^^^^^^^^^^AMS阻止服务出错 "+e+ "^^^^^^^^^^^^^^^^^");
                }
                }
            };
            XposedUtil.hookMethod(amsCls,clss,"startService",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^startService  函数未找到^^^^^^^^^^^^^^^^^");
        }

        //阻止广播发送相关
        if(amsMethods.containsKey("broadcastIntentLocked")){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    if(isOneOpen||isTwoOpen) {
                        //阻止往出发广播
                        String callingPackage = ((String) methodHookParam.args[1]) + "";
//                        if (isMubeiStopOther && isTwoOpen) {
//                            muBeiPrefs.reload();
//                        }
                        if (isOneOpen) {
                            controlPrefs.reload();
                        }
//                    if((isOneOpen&&controlHMs.containsKey(callingPackage+"/broad"))){
//                       (isMubeiStopOther && isTwoOpen && muBeiHSs.contains(callingPackage)
                        if ((isOneOpen && controlHMs.containsKey(callingPackage + "/broad")&&controlHMs.get(callingPackage + "/broad")==(Boolean)true)) {
                            boolean isSend = false;
                            if (methodHookParam.args[2] != null) {
                                Intent intent = (Intent) methodHookParam.args[2];
                                isSend = (intent.getAction() + "").contains("click369");
                            }
                            if (!isSend) {
                                methodHookParam.setResult(-1);
                                return;
                            }
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            };
            final Class clss[] = amsMethods.get("broadcastIntentLocked").getParameterTypes();
            XposedUtil.hookMethod(amsCls,clss,"broadcastIntentLocked",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^broadcastIntentLocked  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        //广播发送相关
        if(amsMethods.containsKey("checkBroadcastFromSystem")){
            //防止系统检测是否是系统广播 不然报异常
            XC_MethodHook hookBroadPerm = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    if((isOneOpen||isTwoOpen)&&methodHookParam.args[0]!=null&&methodHookParam.args[0] instanceof Intent){
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
            final Class clss[] = amsMethods.get("checkBroadcastFromSystem").getParameterTypes();
            XposedUtil.hookMethod(amsCls,clss,"checkBroadcastFromSystem",hookBroadPerm);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^checkBroadcastFromSystem  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        isRecentOpen = settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
        //最近任务隐藏相关
        if(amsMethods.containsKey("createRecentTaskInfoFromTaskRecord")){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    if(isRecentOpen) {
                        Object recentObj = methodHookParam.args[0];
                        Method updateMethod = taskRecordCls.getDeclaredMethod("updateTaskDescription");
                        if (updateMethod == null) {
                            updateMethod = taskRecordCls.getMethod("updateTaskDescription");
                        }
                        updateMethod.setAccessible(true);
                        updateMethod.invoke(recentObj);
                        Method getBaseMethod = taskRecordCls.getDeclaredMethod("getBaseIntent");
                        if (getBaseMethod == null) {
                            getBaseMethod = taskRecordCls.getMethod("getBaseIntent");
                        }
                        getBaseMethod.setAccessible(true);
                        Intent intent = (Intent) (getBaseMethod.invoke(recentObj));
                        recentPrefs.reload();
//                    XposedBridge.log("^^^^^^^^^^^^^^createRecentTaskInfoFromTaskRecord intent  "+intent+"  "+recentPrefs.getAll().size()+"^^^^^^^^^^^^^^^^^");
                        if (intent != null && recentPrefs.contains(intent.getComponent().getPackageName() + "/notshow")) {
                            Field isAvailableField = taskRecordCls.getDeclaredField("isAvailable");
                            isAvailableField.setAccessible(true);
                            isAvailableField.set(recentObj, !recentPrefs.getBoolean(intent.getComponent().getPackageName() + "/notshow", false));
                        }
                    }
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                }
            };
            final Class clss[] = amsMethods.get("createRecentTaskInfoFromTaskRecord").getParameterTypes();
            XposedUtil.hookMethod(amsCls,clss,"createRecentTaskInfoFromTaskRecord",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^createRecentTaskInfoFromTaskRecord  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        //最近任务保留常驻内存

        Constructor cons [] = processRecordCls.getDeclaredConstructors();
        if(cons!=null&&cons.length>0){
            Class clss[] = cons [0].getParameterTypes();
            if (clss!=null){
                XC_MethodHook hook = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try{
                        if (isAppstart){
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
                        }
                    }catch (RuntimeException e){
                        e.printStackTrace();
                    }
                    }
                };
                XposedUtil.hookConstructorMethod(processRecordCls,clss,hook);
            }else{
                XposedBridge.log("^^^^^^^^^^^^^^ProcessRecord0  构造函数未找到 ^^^^^^^^^^^^^^^^^");
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^ProcessRecord1  构造函数未找到 ^^^^^^^^^^^^^^^^^");
        }
        //保活自己
        Class clssself[] = XposedUtil.getParmsByName(processRecordCls,"kill");
        if (clssself!=null){
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
            XposedUtil.hookMethod(processRecordCls,clssself,"kill",hook);
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
        if(amsMethods.containsKey("removeTask")){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try {
                    if(isRecentOpen) {
                        Field mStackSupervisorField = amsCls.getDeclaredField("mStackSupervisor");
                        mStackSupervisorField.setAccessible(true);
                        Object mStackSupervisorObject = mStackSupervisorField.get(methodHookParam.thisObject);
//                      Method anyTaskForIdLockedMethod= assCls.getDeclaredMethod("anyTaskForIdLocked",int.class,boolean.class,int.class);//,boolean.class,int.class
                        anyTaskForIdLockedMethod.setAccessible(true);
                        Object taskRecordObject = null;
                        Class clss[] = anyTaskForIdLockedMethod.getParameterTypes();
                        if (clss.length == 3 && clss[1].getName().equals(boolean.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], true, -1);
                        } else if (clss.length == 1) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0]);
                        } else if (clss.length == 2 && clss[1].getName().equals(boolean.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], true);
                        } else if (clss.length == 2 && clss[1].getName().equals(int.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], 1);
                        } else if (clss.length == 3 && clss[1].getName().equals(int.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], 1, -1);
                        }
                        if (taskRecordObject != null) {
                            Field mAffiliatedTaskIdField = taskRecordCls.getDeclaredField("mAffiliatedTaskId");
                            Field intentField = taskRecordCls.getDeclaredField("intent");
                            mAffiliatedTaskIdField.setAccessible(true);
                            intentField.setAccessible(true);
                            Object intentObject = intentField.get(taskRecordObject);
                            String pkg = null;
                            String cls = null;
                            if (intentObject != null) {
                                pkg = ((Intent) intentObject).getComponent().getPackageName();
                                cls = ((Intent) intentObject).getComponent().getClassName();
                                if (recentPrefs.hasFileChanged()) {
                                    recentPrefs.reload();
                                }
                            }
                            boolean isKillFail = false;
                            if (pkg != null && recentPrefs.getBoolean(pkg + "/notclean", false)) {
                                methodHookParam.setResult(false);
                                return;
                            } else if (recentPrefs.getBoolean(pkg + "/forceclean", false)) {
                                final Object ams = methodHookParam.thisObject;
                                try {
                                    //com.tencent.mm.plugin.appbrand.ui.AppBrandUI  com.tencent.mm.ui.LauncherUI
                                    if("com.tencent.mm".equals(pkg)&&!"com.tencent.mm.ui.LauncherUI".equals(cls)){
                                    }else {
                                        Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                        m.setAccessible(true);
                                        m.invoke(ams, pkg, 0);
//                                        XposedBridge.log("CONTROL REMOVE CARD " + pkg + "  " + cls);
                                    }
                                } catch (RuntimeException e) {
                                    isKillFail = true;
                                }
//                                final Object ams = methodHookParam.thisObject;
                                if (sysCxtField != null) {
                                    sysCxtField.setAccessible(true);
                                    final Context sysCxt = (Context) sysCxtField.get(ams);//(Context)methodHookParam.args[0];
                                    if (sysCxt != null) {
                                        if("com.tencent.mm".equals(pkg)&&"com.tencent.mm.plugin.appbrand.ui.AppBrandUI".equals(cls)){
                                        }else {
                                            Intent intent = new Intent("com.click369.control.removerecent");
                                            intent.putExtra("pkg", pkg);
                                            sysCxt.sendBroadcast(intent);
                                        }
                                    }
                                }
                            }

                        } else {
                            XposedBridge.log("^^^^^^^^^^^^^^taskRecordObject removeTask 对象获取失败  " + clss.length + "^^^^^^^^^^^^^^^^^");
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            };
            final Class clss[] = amsMethods.get("removeTask").getParameterTypes();
            XposedUtil.hookMethod(amsCls,clss,"removeTask",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^removeTask  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        if(amsMethods.containsKey("startActivityFromRecents")){
            final Class clss[] = amsMethods.get("startActivityFromRecents").getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try {
                    if(isAppstart) {
                        Field mStackSupervisorField = amsCls.getDeclaredField("mStackSupervisor");
                        mStackSupervisorField.setAccessible(true);
                        Object mStackSupervisorObject = mStackSupervisorField.get(methodHookParam.thisObject);
                        Object taskRecordObject = null;
                        anyTaskForIdLockedMethod.setAccessible(true);
                        Class clss[] = anyTaskForIdLockedMethod.getParameterTypes();
                        if (clss.length == 3 && clss[1].getName().equals(boolean.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], true, -1);
                        } else if (clss.length == 1) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0]);
                        } else if (clss.length == 2 && clss[1].getName().equals(boolean.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], true);
                        } else if (clss.length == 2 && clss[1].getName().equals(int.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], 1);
                        } else if (clss.length == 3 && clss[1].getName().equals(int.class.getName())) {
                            taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0], 1, -1);
                        }
                        if (taskRecordObject != null) {
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
                                    boolean isLockApp = appStartPrefHMs.containsKey(pkg+"/lockapp")?(Boolean)appStartPrefHMs.get(pkg+"/lockapp"):false;
//                                    boolean isLockAppOk = appStartPrefHMs.containsKey(pkg+"/lockok")?(Boolean)appStartPrefHMs.get(pkg+"/lockok"):false;

//                                    if (pkg != null && isLockApp) {
                                    if (pkg != null && isLockApp) {
//                                    if (pkg != null && autoStartPrefs.getBoolean(pkg + "/lockapp", false)) {
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
                        } else {
                            XposedBridge.log("^^^^^^^^^^^^^^taskRecordObject FromRecents 对象获取失败 " + clss.length + "^^^^^^^^^^^^^^^^^");
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            };
            XposedUtil.hookMethod(amsCls,clss,"startActivityFromRecents",hook);
        }else if(isAppstart){
            XposedBridge.log("^^^^^^^^^^^^^^startActivityFromRecents  函数未找到 ^^^^^^^^^^^^^^^^^");
        }



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
                            if((isOneOpen||isTwoOpen)){
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
                                                    (isOneOpen&&controlHMs.containsKey(name+"/broad")&&controlHMs.get(name+"/broad")==(Boolean)true)){
//                                                    if ((isOneOpen&&controlHMs.containsKey(name+"/broad"))){
                                                removes.add(o);
                                            }
                                        }else if(o instanceof ResolveInfo){
                                            ActivityInfo info = ((ResolveInfo)o).activityInfo;
                                            String name = info!=null?info.packageName:"";
                                            if ((isTwoOpen&&muBeiHSs.contains(name))||
                                                    (isOneOpen&&controlHMs.containsKey(name+"/broad")&&controlHMs.get(name+"/broad")==(Boolean)true)){
                                                removes.add(o);
                                            }
                                        }
                                    }
                                    receivers.removeAll(removes);
                                }
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
                            XposedUtil.hookConstructorMethod(brCls,clss,hook1);
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

        try{
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final Class ussCls = XposedHelpers.findClass("com.android.server.usage.UsageStatsService", lpparam.classLoader);
                Class idleFilterParmsTemp[] = XposedUtil.getParmsByName(ussCls,"isAppIdleFilteredOrParoled");
                Method getidlemethodTemp = XposedUtil.getMethodByName(ussCls,"isAppIdleFilteredOrParoled");
    //            XposedUtil.showParmsByName(ussCls,"isAppIdleFilteredOrParoled");
                Method idlemethodTemp = XposedUtil.getMethodByName(ussCls,"setAppIdleAsync");
                if (idlemethodTemp==null){
                    idlemethodTemp = XposedUtil.getMethodByName(ussCls,"setAppIdle");
                }
                if (idlemethodTemp==null){
                    idlemethodTemp = XposedUtil.getMethodByName(ussCls,"setAppInactive");
                }
                if(getidlemethodTemp==null){
                    getidlemethodTemp = XposedUtil.getMethodByName(ussCls,"isAppInactive");//isAppInactive
                    idleFilterParmsTemp = XposedUtil.getParmsByName(ussCls,"isAppInactive");
                }
                final Method idlemethod = idlemethodTemp;
                final Method getidlemethod = getidlemethodTemp;
                final Class idleFilterParms[] = idleFilterParmsTemp;
                XposedHelpers.findAndHookConstructor(ussCls, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                        Context context = (Context)methodHookParam.args[0];
                        BroadcastReceiver br = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context, Intent intent) {
                                if ("com.click369.control.uss.setappidle".equals(intent.getAction())){
                                    try {
                                        Set<String> idlePkgs = new HashSet<String>();
                                        if (intent.hasExtra("pkg")){
                                            idlePkgs.add(intent.getStringExtra("pkg"));
                                        }else if(intent.hasExtra("pkgs")){
                                            idlePkgs.addAll((Set<String>)intent.getSerializableExtra("pkgs"));
                                        }
                                        boolean idle = intent.getBooleanExtra("idle",true);
                                        if (idlemethod!=null) {
                                            idlemethod.setAccessible(true);
                                            for(String pkg:idlePkgs){
                                                idlemethod.invoke(methodHookParam.thisObject, pkg, idle, 0);
                                            }
                                        }else{
                                            XposedBridge.log("CONTROL -----未找到idle函数  ");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        XposedBridge.log("CONTROL -----设置待机出错  "+e.getMessage());
                                    }
                                }else if ("com.click369.control.uss.getappidlestate".equals(intent.getAction())){
                                    try{
                                        getidlemethod.setAccessible(true);
                                        HashMap<String,Boolean> pkgs = ( HashMap<String,Boolean>)intent.getSerializableExtra("pkgs");
                                        HashSet<String> newpkgs =new HashSet<String>();
                                        Set<String> keys = pkgs.keySet();
                                        if (idleFilterParms!=null&&idleFilterParms.length == 4){
                                            for(String key:keys){
                                                Object isIdle = getidlemethod.invoke(methodHookParam.thisObject,key.trim(),0, SystemClock.elapsedRealtime(),true);
                                                if((Boolean)isIdle==true){
                                                    newpkgs.add(key);
                                                }
                                            }
                                        }else if(idleFilterParms!=null&&idleFilterParms.length == 3){
                                            for(String key:keys){
                                                Object isIdle = getidlemethod.invoke(methodHookParam.thisObject,key.trim(),0, SystemClock.elapsedRealtime());
                                                if((Boolean)isIdle==true){
                                                    newpkgs.add(key);
                                                }
                                            }
                                        }else if(idleFilterParms!=null&&idleFilterParms.length == 2){
                                            for(String key:keys){
                                                Object isIdle = getidlemethod.invoke(methodHookParam.thisObject,key.trim(),0);
                                                if((Boolean)isIdle==true){
                                                    newpkgs.add(key);
                                                }
                                            }
                                        }
                                        Intent intent1 = new Intent("com.click369.control.recappidlestate");
                                        intent1.putExtra("pkgs",newpkgs);
                                        intent1.putExtra("mbpkgs",muBeiHSs);
                                        context.sendBroadcast(intent1);
//                                        XposedBridge.log("CONTROL -----已进入待机应用: "+newpkgs.size());
                                    } catch (Throwable e){
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
                    }
                });
            }
        } catch (NoSuchMethodError e) {
            e.printStackTrace();
            XposedBridge.log("CONTROL -----未找到UsageStatsService NoSuchMethodError "+e);
        }catch (XposedHelpers.ClassNotFoundError e) {
            e.printStackTrace();
            XposedBridge.log("CONTROL -----未找到UsageStatsService ClassNotFoundError "+e);
        }

        try {
            final Class notifyCls = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService$NotificationListeners",lpparam.classLoader);
            Class clss[] = XposedUtil.getParmsByName(notifyCls,"notifyPostedLocked");
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        if (isSkipAdOpen&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            StatusBarNotification sbn = (StatusBarNotification)methodHookParam.args[0];
                            CharSequence title = (CharSequence) sbn.getNotification().extras.get(Notification.EXTRA_TITLE);
                            CharSequence text = (CharSequence) sbn.getNotification().extras.get(Notification.EXTRA_TEXT);
                            if (title != null && title.toString().contains("应用控制器") && title.toString().contains("可能有害")) {
                                methodHookParam.setResult(null);
                                return;
                            } else if (text != null && text.toString().contains("应用控制器") && text.toString().contains("可能有害")) {
                                methodHookParam.setResult(null);
                                return;
                            }
                            if (!Common.PACKAGENAME.equals(sbn.getPackageName())) {
                                if (notifySkipKeyWords.size()>0){
                                    for(String s:notifySkipKeyWords){
                                        if (title != null && title.toString().contains(s)) {
                                            methodHookParam.setResult(null);
                                            return;
                                        } else if (text != null && text.toString().contains(s)) {
                                            methodHookParam.setResult(null);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyPosted error "+e+"^^^^^^^^^^^^^^^^^");
                    }
                }
            };
            if(clss!=null){
                XposedUtil.hookMethod(notifyCls, clss, "notifyPostedLocked",hook);
            }
        } catch (RuntimeException e){
            e.printStackTrace();
        }catch (XposedHelpers.ClassNotFoundError e){
            e.printStackTrace();
        }catch (Throwable e){
            e.printStackTrace();
        }

    }

}
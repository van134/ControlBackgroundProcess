package com.click369.controlbp.service;

import android.Manifest;
import android.app.ActivityManager;
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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.view.WindowManager;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.PackageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedAMS {
    public static boolean isFlyme() {
        /* 获取魅族系统操作版本标识*/
        String meizuFlymeOSFlag  = getSystemProperty("ro.build.display.id","");
        if (TextUtils.isEmpty(meizuFlymeOSFlag)){
            return false;
        }else if (meizuFlymeOSFlag.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains("flyme")){
            return  true;
        }else {
            return false;
        }
    }

    /**
     *   获取系统属性
     * <h3>Version</h3> 1.0
     * <h3>CreateTime</h3> 2016/6/18,9:35
     * <h3>UpdateTime</h3> 2016/6/18,9:35
     * <h3>CreateAuthor</h3> vera
     * <h3>UpdateAuthor</h3>
     * <h3>UpdateInfo</h3> (此处输入修改内容,若无修改可不写.)
     * @param key  ro.build.display.id
     * @param defaultValue 默认值
     * @return 系统操作版本标识
     */
    private static String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> clz = Class.forName("android.os.SystemProperties");
            Method get = clz.getMethod("get", String.class, String.class);
            return (String)get.invoke(clz, key, defaultValue);
        } catch (Exception e) {
            return null;
        }
    }
    static Bitmap bitmapTemp;
    static Handler handler;
    static File file_autostart = new File("/data/sys_autostart");
    static File file_recent = new File("/data/sys_recnet");
    static File file_setting = new File("/data/sys_setting");
    static File file_control = new File("/data/sys_control");
    static File file_skip = new File("/data/sys_skip");
    static boolean isFlyme = false;
    static boolean ISAMSHOOK = false;
    static boolean isOneOpen = true;
    static boolean isTwoOpen = true;
    static boolean isAppstart = true;
    static boolean isRecentOpen = true;
    static boolean isSkipAdOpen = true;
    static boolean isPriOpen = true;
    static boolean isStopScanMedia = false;
    static boolean isMubeiStopOther = false;
    static boolean isFloatOk = false;
    static boolean isNeedFloadOnSys = false;
    static boolean isAutoStartNotNotify = false;
    static boolean isStopRemveRecent = false;
//    static boolean isKillTenc = false;
    static boolean oldSet = true;
    static int MUID = 0;//,SYSUI_UID = 0;
    final static HashMap<String,Object> appStartPrefHMs = new HashMap<String,Object>();
    final static HashMap<String,Object> recentPrefHMs = new HashMap<String,Object>();
    final static HashSet<Integer> netMobileList = new HashSet<Integer>();
    final static HashSet<Integer> netWifiList = new HashSet<Integer>();

//    final static HashSet<String> autoStartAppNames = new HashSet<String>();
    final static HashMap<String,String> autoStartAppNameMaps = new HashMap<String,String>();
    final static HashSet<String> muBeiHSs = new HashSet<String>();
    final static HashSet<String> notifySkipKeyWords = new HashSet<String>();
    final static HashMap<String,Object> controlHMs = new HashMap<String,Object>();
    final static LinkedHashMap<Long,String> preventPkgs = new LinkedHashMap<Long,String>();
    final static LinkedHashMap<Long,String> killPkgs = new LinkedHashMap<Long,String>();
    final static LinkedHashMap<Long,String> startPkgs = new LinkedHashMap<Long,String>();
    final static HashSet<String> startRuningPkgs = new HashSet<String>();
    final static HashSet<String> notStopPkgs = new HashSet<String>();
    final static HashMap<String,Long> runingTimes = new HashMap<String,Long>();


    final static HashMap<String,HashMap<Long,String>> privacyInfos = new HashMap<String,HashMap<Long,String>>();
    private static String preventInfo = "";
    private static long lastPreventTime = 0;
    private static int preventPkgTime = 0;
    private static String startPkg = "";
    private static String startProc = "";

    private static String lastOpenActivityPkg = "";
    private static long lastOpenActivityTime = 0;
    private static long lastSCOnTime = 0;
    private static HashMap<String,HashSet<String>> gmsPkgs = new HashMap<String,HashSet<String>>();

    private static Object amsObject;
    private static Context amsContext;
    private static int sysadj = -900;

    private static void initData(Intent intent){
        try {
            Map autoMap = null;
            Map recentMap = null;
            Map controlMap = null;
            Map settingMap = null;
            Set<String> skipDiaSet = null;
            if(intent!=null){
                autoMap = (Map)intent.getSerializableExtra("autoStartPrefs");
                recentMap = (Map)intent.getSerializableExtra("recentPrefs");
                controlMap = (Map)intent.getSerializableExtra("controlPrefs");
                settingMap = (Map)intent.getSerializableExtra("settingPrefs");
                skipDiaSet = (Set<String>)((Map)intent.getSerializableExtra("skipDialogPrefs")).get(Common.PREFS_SKIPNOTIFY_KEYWORDS);
            }else{
                autoMap = (Map)FileUtil.readObj(file_autostart.getAbsolutePath());
                recentMap = (Map)FileUtil.readObj(file_recent.getAbsolutePath());
                controlMap = (Map)FileUtil.readObj(file_control.getAbsolutePath());
                settingMap = (Map)FileUtil.readObj(file_setting.getAbsolutePath());
                skipDiaSet = (Set<String>)FileUtil.readObj(file_skip.getAbsolutePath());
            }
            if((settingMap!=null&&settingMap.size()>0)||(autoMap!=null&&autoMap.size()>0)){
                if(intent!=null){
                    FileUtil.writeObj(autoMap,file_autostart.getAbsolutePath());
                    FileUtil.writeObj(recentMap,file_recent.getAbsolutePath());
                    FileUtil.writeObj(settingMap,file_setting.getAbsolutePath());
                    FileUtil.writeObj(controlMap,file_control.getAbsolutePath());
                    FileUtil.writeObj(skipDiaSet,file_skip.getAbsolutePath());
                }
                if(autoMap!=null) {
                    appStartPrefHMs.clear();
                    appStartPrefHMs.putAll(autoMap);
                }
                if(recentMap!=null) {
                    recentPrefHMs.clear();
                    recentPrefHMs.putAll(recentMap);
                }
                if(controlMap!=null) {
                    controlHMs.clear();
                    controlHMs.putAll(controlMap);
                }
                if(skipDiaSet!=null){
                    notifySkipKeyWords.clear();
                    notifySkipKeyWords.addAll(skipDiaSet);
                }
                XposedBridge.log("CONTROL_"+appStartPrefHMs.size()+"_"+recentPrefHMs.size()+"_"+controlHMs.size()+"_"+notifySkipKeyWords.size()+"_"+settingMap.size());
                isOneOpen = settingMap.containsKey(Common.ALLSWITCH_SERVICE_BROAD)?(boolean)settingMap.get(Common.ALLSWITCH_SERVICE_BROAD):true;//settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
                isTwoOpen = settingMap.containsKey(Common.ALLSWITCH_BACKSTOP_MUBEI)?(boolean)settingMap.get(Common.ALLSWITCH_BACKSTOP_MUBEI):true;//settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
                isAppstart = settingMap.containsKey(Common.ALLSWITCH_AUTOSTART_LOCK)?(boolean)settingMap.get(Common.ALLSWITCH_AUTOSTART_LOCK):true;//settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true);
                isRecentOpen = settingMap.containsKey(Common.ALLSWITCH_RECNETCARD)?(boolean)settingMap.get(Common.ALLSWITCH_RECNETCARD):true;//settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
                isSkipAdOpen = settingMap.containsKey(Common.ALLSWITCH_ADSKIP)?(boolean)settingMap.get(Common.ALLSWITCH_ADSKIP):true;//settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
                isPriOpen = settingMap.containsKey(Common.ALLSWITCH_PRIVACY)?(boolean)settingMap.get(Common.ALLSWITCH_PRIVACY):true;//settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
                isStopScanMedia = settingMap.containsKey(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA)?(boolean)settingMap.get(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA):false;//settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA,false);
                isMubeiStopOther = settingMap.containsKey(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)?(boolean)settingMap.get(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC):false;;
                isAutoStartNotNotify = settingMap.containsKey(Common.PREFS_SETTING_ISAUTOSTARTNOTNOTIFY)?(boolean)settingMap.get(Common.PREFS_SETTING_ISAUTOSTARTNOTNOTIFY):false;;
                isStopRemveRecent = settingMap.containsKey(Common.PREFS_SETTING_EXITREMOVERECENT)?(boolean)settingMap.get(Common.PREFS_SETTING_EXITREMOVERECENT):true;;
                if(intent!=null&&isAutoStartNotNotify&&amsContext!=null){
                    Set<String> sts = appStartPrefHMs.keySet();
                    autoStartAppNameMaps.clear();
                    for(String ss:sts){
                        if(ss.endsWith("/autostart")){
                            String pkg = ss.replace("/autostart","");
                            String appName = PackageUtil.getAppNameByPkg(amsContext,pkg);
                            if(appName==null){
                                continue;
                            }
//                            autoStartAppNames.add(appName);
                            autoStartAppNameMaps.put(appName,pkg);
//                            XposedBridge.log(" appName "+appName);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,
                                   final XSharedPreferences settingPrefs,
                                   final XSharedPreferences controlPrefs,
                                   final XSharedPreferences autoStartPrefs,
                                   final XSharedPreferences recentPrefs,
                                   final XSharedPreferences uiBarPrefs,
                                   final XSharedPreferences skipDialogPrefs){

        if("com.android.systemui".equals(lpparam.packageName)) {
            try {
                final Class arCls = XposedUtil.findClass("android.app.Application", lpparam.classLoader);
                Class clss[] = XposedUtil.getParmsByName(arCls,"onCreate");
                if(clss!=null){
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                final Application app = (Application) (methodHookParam.thisObject);
                                if (app!=null){
                                    final Handler handler = new Handler();
                                    final Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Intent explicitIntent = new Intent("com.click369.service");
                                                ComponentName component = new ComponentName(Common.PACKAGENAME, Common.PACKAGENAME + ".service.WatchDogService");
                                                explicitIntent.setComponent(component);
                                                app.getApplicationContext().startService(explicitIntent);
                                            }catch (Throwable e){
                                                e.printStackTrace();
                                            }
                                        }
                                    };
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
                                                    recentPrefs.reload();
                                                    controlPrefs.reload();
                                                    settingPrefs.reload();
                                                    skipDialogPrefs.reload();
                                                    uiBarPrefs.reload();
                                                    XposedUtil.reloadInfos(context,autoStartPrefs,recentPrefs,controlPrefs,settingPrefs,skipDialogPrefs,uiBarPrefs);
                                                }else if("com.click369.control.startservice".equals(action)) {
                                                    long time = 0;
                                                    if(intent.hasExtra("delay")){
                                                        time = intent.getIntExtra("delay",0);
                                                    }
                                                    handler.removeCallbacks(runnable);
                                                    handler.postDelayed(runnable,time);
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
                    XposedUtil.hookMethod(arCls,clss, "onCreate", hook);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        if (!"android".equals(lpparam.packageName)){
            return;
        }

//        autoStartPrefs.reload();
//        controlPrefs.reload();
//        appStartPrefHMs.putAll(autoStartPrefs.getAll());
//        controlHMs.putAll(controlPrefs.getAll());
        startRuningPkgs.add("android");
        startRuningPkgs.add("com.android.systemui");
//        settingPrefs.reload();
        final Class amsCls = XposedUtil.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
        final Class actServiceCls = XposedUtil.findClass("com.android.server.am.ActiveServices", lpparam.classLoader);
        final Class actRecordCls = XposedUtil.findClass("com.android.server.am.ActivityRecord",lpparam.classLoader);

        final Class taskRecordCls = XposedUtil.findClass("com.android.server.am.TaskRecord",lpparam.classLoader);
        final Class processRecordCls = XposedUtil.findClass("com.android.server.am.ProcessRecord",lpparam.classLoader);
        final Class processListCls = XposedUtil.findClass("com.android.server.am.ProcessList",lpparam.classLoader);
        final Class ifwCls = XposedUtil.findClass("com.android.server.firewall.IntentFirewall",lpparam.classLoader);
        final Class amCls = XposedUtil.findClass("android.app.ActivityManager",lpparam.classLoader);
        final Class activityStackSupervisorCls = XposedUtil.findClass("com.android.server.am.ActivityStackSupervisor",lpparam.classLoader);
        final Class nmsCls = XposedUtil.findClass("com.android.server.notification.NotificationManagerService",lpparam.classLoader);
        final Class sysCls = XposedUtil.findClass("com.android.server.SystemService",lpparam.classLoader);
        final Class pwmServiceCls = XposedUtil.findClass("com.android.server.policy.PhoneWindowManager", lpparam.classLoader);
        final Class netServiceCls = XposedUtil.findClass("com.android.server.NetworkManagementService", lpparam.classLoader);
        Class ussClsTemp = XposedUtil.findClass("com.android.server.usage.UsageStatsService", lpparam.classLoader);
        if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
            ussClsTemp = XposedUtil.findClass("com.android.server.usage.AppStandbyController", lpparam.classLoader);
        }

        final Field sysCxtField = XposedHelpers.findFirstFieldByExactType(amsCls,Context.class);
        final Field mServicesField = XposedHelpers.findFirstFieldByExactType(amsCls,actServiceCls);
        final HashMap<String,Method> amsMethods =  XposedUtil.getAMSParmas(amsCls);
        if (amsMethods.containsKey("finishBooting")){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                try {
                    handler = new Handler(Looper.getMainLooper());
                    if (sysCxtField != null) {
                        sysCxtField.setAccessible(true);
                        amsObject = methodHookParam.thisObject;
                        amsContext = (Context) sysCxtField.get(amsObject);//(Context)methodHookParam.args[0];
                        Object o = XposedHelpers.getAdditionalStaticField(amsCls, "click369res");
                        boolean isContinue = false;
                        if (o!=null&&amsContext!=null&&o.hashCode()==amsContext.hashCode()){
                            isContinue = true;
                        }
                        if (amsContext != null&&!isContinue) {
                            final Runnable startService = new Runnable() {
                                @Override
                                public void run() {
                                try {
                                    Intent intent = new Intent("com.click369.control.startservice");
                                    intent.putExtra("delay",300);
                                    amsContext.sendBroadcast(intent);
                                }catch (Throwable e){
                                    e.printStackTrace();
                                }
                                }
                            };
                            final Runnable startmmchece = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Intent intent = new Intent("click369.main.finish.checkfail");
                                        amsContext.sendBroadcast(intent);
                                    }catch (Throwable e){
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
                                        m.invoke(amsObject, pkg, 0);
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            final Runnable sendR = new Runnable() {
                                @Override
                                public void run() {
//                                    Intent intent1 = new Intent(Common.TEST_A+Common.TEST_B+Common.TEST_C+Common.TEST_D+Common.TEST_E);
//                                    amsContext.sendBroadcast(intent1);
//                                    if(System.currentTimeMillis()-lastSCOnTime<2000||(Math.random()<0.6)) {
                                        Intent intent2 = new Intent("click369.main.finish.check");
                                    intent2.putExtra("isscon",true);
                                        amsContext.sendBroadcast(intent2);
                                        h.postDelayed(startmmchece, 2500);
//                                    }

                                    handler.postDelayed(this,1000*61*((int)(Math.random()*3)+2));
                                }
                            };
                            if (amsContext != null) {
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
                                                XposedBridge.log("CONTROL_hook AMS error " + e );
                                            }
                                        } else if ("com.click369.control.ams.changerecent".equals(action)) {
                                            try {
                                                String pkg = intent.getStringExtra("pkg");
                                                boolean isshow = intent.getBooleanExtra("isshow",true);
                                                recentPrefHMs.put(pkg+ "/notshow",!isshow);
                                                Field recField = null;
                                                if(amsCls.getName().endsWith(".ActivityManagerService")){
                                                    recField = amsCls.getDeclaredField("mRecentTasks");
                                                }else{
//                                                    XposedBridge.log("amsCls.getSuperclass()1  "+amsCls.getSuperclass());
                                                    Class clss = amsCls;
                                                    for(int i = 0;i<10;i++){
                                                        if(clss.getName().endsWith(".ActivityManagerService")){
                                                            break;
                                                        }else if(clss!=null){
                                                            clss = clss.getSuperclass();
//                                                            XposedBridge.log("amsCls.getSuperclass()2  "+i+"  "+clss);
                                                        }
                                                    }
                                                    if(clss==null){
                                                        clss = amsCls;
                                                    }
                                                    recField = clss.getDeclaredField("mRecentTasks");
                                                }
                                                recField.setAccessible(true);
                                                ArrayList lists = null;
                                                Object tasks = recField.get(methodHookParam.thisObject);
                                                //适配Android 9
                                                if(tasks instanceof ArrayList){
                                                    lists = (ArrayList)tasks;
                                                }else{
                                                    Field mTasksField = tasks.getClass().getDeclaredField("mTasks");
                                                    mTasksField.setAccessible(true);
                                                    lists = (ArrayList) mTasksField.get(tasks);
                                                }
                                                if (lists!=null&&lists.size() > 0) {
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
                                                                isAvailableField.set(o, isshow);
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
                                            boolean isSelfCheck = intent.hasExtra("selfcheck");
//                                            HashSet<String> runnings = new HashSet<String>();
                                            HashSet<String> checks = new HashSet<String>();
                                            if(isSelfCheck){
                                                if(intent.hasExtra("pkgs")){
                                                    checks.addAll((HashSet<String>)intent.getSerializableExtra("pkgs"));
                                                }
//                                                Field procListField = amsCls.getDeclaredField("mLruProcesses");
//                                                procListField.setAccessible(true);
//                                                ArrayList procsTemp = (ArrayList)procListField.get(amsObject);
//                                                if(procsTemp!=null&&procsTemp.size()>0) {
//                                                    for (Object proc : procsTemp) {
//                                                        Field infoField1 = proc.getClass().getDeclaredField("info");
//                                                        infoField1.setAccessible(true);
//                                                        ApplicationInfo info1 = (ApplicationInfo) infoField1.get(proc);
//                                                       runnings.add(info1.packageName);
//                                                    }
//                                                }
//                                                ActivityManager am = (ActivityManager) amsContext.getSystemService(Context.ACTIVITY_SERVICE);
//                                                List<ActivityManager.RunningAppProcessInfo> rps =  am.getRunningAppProcesses();
//                                                if (rps!=null&&rps.size()>0){
//                                                    for(ActivityManager.RunningAppProcessInfo ar:rps){
//                                                        if(ar.pkgList!=null&&ar.pkgList.length>0){
//                                                            for(String s:ar.pkgList){
//                                                                runnings.add(s);
//                                                            }
//                                                        }
//                                                    }
//                                                }
                                            }
                                            try {
                                                Field recField = null;
//                                                XposedUtil.showFields(amsCls);
                                                if(amsCls.getName().endsWith(".ActivityManagerService")){
                                                    recField = amsCls.getDeclaredField("mRecentTasks");
                                                }else{
                                                    Class clss = amsCls;
                                                    for(int i = 0;i<10;i++){
                                                        if(clss.getName().endsWith(".ActivityManagerService")){
                                                            break;
                                                        }else if(clss!=null){
                                                            clss = clss.getSuperclass();
//                                                            XposedUtil.showFields(clss);
                                                        }
                                                    }
                                                    if(clss==null){
                                                        clss = amsCls;
                                                    }
                                                    recField = clss.getDeclaredField("mRecentTasks");
                                                }
                                                recField.setAccessible(true);
                                                ArrayList lists = null;
                                                Object tasks = recField.get(methodHookParam.thisObject);
                                                if(tasks instanceof ArrayList){
                                                    //mTasks
                                                    lists = (ArrayList)tasks;
                                                }else{
                                                    Field mTasksField = tasks.getClass().getDeclaredField("mTasks");
                                                    mTasksField.setAccessible(true);
                                                    lists = (ArrayList) mTasksField.get(tasks);
                                                }
                                                if (lists!=null&&lists.size() > 0) {
                                                    if (lists.get(0).getClass().getName().equals("com.android.server.am.TaskRecord")) {
                                                        HashSet sets = new HashSet();
                                                        for (Object o : lists) {
                                                            Method getBaseMethod = taskRecordCls.getDeclaredMethod("getBaseIntent");
                                                            if (getBaseMethod == null) {
                                                                getBaseMethod = taskRecordCls.getMethod("getBaseIntent");
                                                            }
                                                            getBaseMethod.setAccessible(true);
                                                            Intent intentm = (Intent) (getBaseMethod.invoke(o));
                                                            String taskPkg = intentm ==null||intentm.getComponent()==null?"":intentm.getComponent().getPackageName();
                                                            if(isSelfCheck){
                                                                if(checks.contains(taskPkg)){
                                                                    sets.add(o);
                                                                }
//                                                                boolean isNotClean = recentPrefHMs.containsKey(taskPkg + "/notclean")?(Boolean)recentPrefHMs.get(taskPkg + "/notclean"):false;
//                                                                boolean isNotShow = recentPrefHMs.containsKey(taskPkg + "/notshow")?(Boolean)recentPrefHMs.get(taskPkg + "/notshow"):false;
//                                                                if(!runnings.contains(taskPkg)&&!isNotClean||isNotShow){
//                                                                    sets.add(o);
//                                                                }
                                                            }else if (pkg!=null&&pkg.equals(taskPkg)) {
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
                                                if(pkg==null||
                                                        !isTwoOpen||
                                                        pkg.equals("android")||
                                                        pkg.startsWith("com.fkzhang")||
                                                        pkg.equals("com.android.settings")){
                                                    return;
                                                }
//                                                XposedUtil.setAdjByLru(amsCls,amsObject,pkg,906);
//                                                muBeiHSs.add(pkg);
                                                if (mServicesField!=null){
                                                    mServicesField.setAccessible(true);
                                                    Object mServicesObject = mServicesField.get(amsObject);
                                                    if (pkg!=null&&pkg.length()>0){
                                                       if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                                            Method killMethod =null;
                                                            if(mServicesObject.getClass().getName().endsWith(".ActiveServices")){
                                                                killMethod = mServicesObject.getClass().getDeclaredMethod("bringDownDisabledPackageServicesLocked", String.class, Set.class, int.class, boolean.class, boolean.class, boolean.class);
                                                            }else{
//                                                                XposedBridge.log("amsCls.getSuperclass()  "+amsCls.getSuperclass());
//                                                                XposedUtil.showMethods( mServicesObject.getClass().getSuperclass());
                                                                killMethod = mServicesObject.getClass().getSuperclass().getDeclaredMethod("bringDownDisabledPackageServicesLocked", String.class, Set.class, int.class, boolean.class, boolean.class, boolean.class);
                                                            }
                                                            killMethod.setAccessible(true);
                                                            killMethod.invoke(mServicesObject, pkg, null, 0, true, false, true);//第二个布尔值 是停止进程
                                                        }else{
                                                            XposedUtil.stopServicesAndroidL(amsCls,processRecordCls,mServicesObject,amsObject,pkg);
                                                        }
                                                        muBeiHSs.add(pkg);
                                                        if(isMubeiStopOther){
                                                            XposedUtil.stopProcess(amsCls,amsObject,pkg);
                                                        }
                                                    }
                                                }
                                            } catch (Throwable e) {
                                                e.printStackTrace();
                                                XposedBridge.log("CONTROL_forcestopservice error "+e+" ^^^^^^^^^^^^^^^");
                                            }
                                        }else if("com.click369.control.ams.removemubei".equals(action)) {
                                            String apk = intent.getStringExtra("apk");
                                            if(apk==null||apk.length()==0){
//                                                XposedUtil.setAdjByLrus(amsCls,amsObject,muBeiHSs,100);
                                                muBeiHSs.clear();
                                            }else{
//                                                XposedUtil.setAdjByLru(amsCls,amsObject,apk,100);
                                                muBeiHSs.remove(apk);
                                            }
                                        }else if("com.click369.control.ams.confirmforcestop".equals(action)||
                                                "com.click369.control.ams.checktimeoutapp".equals(action)){//确认是否杀死该进程
                                            try {
                                                boolean isCheckTimeout = "com.click369.control.ams.checktimeoutapp".equals(action);
                                                long timeout = 0;
                                                if(isCheckTimeout){
                                                    timeout = intent.getLongExtra("timeout",1000*60*60*12L);
                                                }
                                                HashSet<String> pkgs = (HashSet<String>)intent.getSerializableExtra("pkgs");
                                                Field procListField = amsCls.getDeclaredField("mLruProcesses");
                                                procListField.setAccessible(true);
                                                ArrayList procsTemp = (ArrayList)procListField.get(amsObject);
                                                if(procsTemp!=null&&procsTemp.size()>0){
                                                    HashSet<String> stopPkgs = new HashSet<String>();
                                                    HashSet<String> notstopPkgs = new HashSet<String>();
                                                    HashMap stopProcs = new HashMap();
                                                    for (Object proc:procsTemp){
                                                        Field infoField = proc.getClass().getDeclaredField("info");
                                                        infoField.setAccessible(true);
                                                        Field lastActivityTimeField = proc.getClass().getDeclaredField("lastActivityTime");
                                                        lastActivityTimeField.setAccessible(true);
                                                        Field activitiesField = proc.getClass().getDeclaredField("activities");
                                                        activitiesField.setAccessible(true);
                                                        ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                                                        if(pkgs.contains(info.packageName)){
                                                            if(isCheckTimeout){
                                                                long lastActivityTime = (Long)lastActivityTimeField.get(proc);
                                                                //超时12小时没有打开过就杀死
                                                                if(SystemClock.uptimeMillis()-lastActivityTime>timeout){
                                                                    Message msg = new Message();
                                                                    msg.obj = info.packageName;
                                                                    h.sendMessage(msg);
                                                                    stopPkgs.add(info.packageName);
                                                                }
                                                            }else{
                                                                ArrayList actList = (ArrayList)activitiesField.get(proc);
                                                                if(actList.size()>0){
                                                                    notstopPkgs.add(info.packageName);
                                                                }else{
                                                                    stopPkgs.add(info.packageName);
                                                                    stopProcs.put(info.packageName,proc);
                                                                }
                                                            }
                                                        }
                                                    }
                                                    stopPkgs.removeAll(notstopPkgs);
                                                    if(stopPkgs.size()>0){
                                                        for(String key:stopPkgs){
                                                            Message msg = new Message();
                                                            msg.obj = key;
                                                            h.sendMessage(msg);
                                                        }
                                                        Intent intent1 = new Intent("com.click369.control.amsstoppkg");
                                                        intent1.putExtra("pkgs",stopPkgs);
                                                        amsContext.sendBroadcast(intent1);
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
                                                ArrayList procsTemp = (ArrayList)procListField.get(amsObject);
                                                if(procsTemp!=null&&procsTemp.size()>0){
                                                    procs.addAll(procsTemp);
                                                    for (Object proc:procs){
                                                        Field infoField = proc.getClass().getDeclaredField("info");
                                                        infoField.setAccessible(true);
                                                        Field lastActivityTimeField = proc.getClass().getDeclaredField("lastActivityTime");
                                                        lastActivityTimeField.setAccessible(true);
                                                        ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                                                        if(info.packageName.equals(info.processName)){
                                                            long lastActivityTime = (Long)lastActivityTimeField.get(proc);
                                                            procTimeInfos.put(info.packageName,lastActivityTime);
                                                        }
                                                    }
                                                    Intent intent1 = new Intent("com.click369.control.backprocinfo");
                                                    intent1.putExtra("infos",procTimeInfos);
                                                    intent1.putExtra("runtimes",runingTimes);
                                                    amsContext.sendBroadcast(intent1);
                                                }
                                            }catch (Throwable e){
                                                e.printStackTrace();
                                            }
                                        }else if("com.click369.control.ams.changepersistent".equals(action)){
//                                            if(!isAppstart){
//                                                return;
//                                            }
                                            boolean persistent = intent.getBooleanExtra("persistent", false);
                                            String pkg = intent.getStringExtra("pkg");

                                            boolean iskill = intent.getBooleanExtra("iskill",false);
                                            if(persistent){
                                                if(intent.hasExtra("pkgs")){
                                                    HashSet<String> pkgs = (HashSet<String>)intent.getSerializableExtra("pkgs");
                                                    notStopPkgs.addAll(pkgs);
                                                }else{
                                                    notStopPkgs.add(pkg);
                                                }

                                            }else{
                                                if(intent.hasExtra("pkgs")){
                                                    HashSet<String> pkgs = (HashSet<String>)intent.getSerializableExtra("pkgs");
                                                    notStopPkgs.removeAll(pkgs);
                                                }else{
                                                    notStopPkgs.remove(pkg);
                                                }
                                                if(iskill){
                                                    try {
                                                        Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                                        m.invoke(methodHookParam.thisObject, pkg, 0);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        }else if("com.click369.control.ams.killself".equals(action)){
                                            notStopPkgs.remove(Common.PACKAGENAME);
                                            try {
                                                Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                                m.invoke(methodHookParam.thisObject, Common.PACKAGENAME, 0);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }else if(Intent.ACTION_SCREEN_ON.equals(action)){
                                            if(startRuningPkgs.contains(Common.PACKAGENAME)&&Math.random()>0.8) {
                                                Intent intent1 = new Intent("com.click369.control.heart");
                                                amsContext.sendBroadcast(intent1);
                                                h.postDelayed(startService, 1500);
                                            }else if(!startRuningPkgs.contains(Common.PACKAGENAME)){
                                                h.post(startService);
                                            }
                                            lastSCOnTime = System.currentTimeMillis();
                                            handler.post(sendR);
                                        }else if(Intent.ACTION_SCREEN_OFF.equals(action)){
                                            handler.removeCallbacks(sendR);
                                            h.removeCallbacks(startService);
                                            h.removeCallbacks(startmmchece);
                                        }else if("click369.main.finish.checkok".equals(action)){
                                            long space = intent.getLongExtra("space",0);
                                            if(space>0&&space<1000*60*5){
                                                handler.removeCallbacks(sendR);
                                                handler.postDelayed(sendR,space);
                                            }
                                            h.removeCallbacks(startmmchece);
                                        }else if("com.click369.control.ams.heart".equals(action)){
                                            h.removeCallbacks(startService);
                                        }else if("com.click369.control.ams.float.checkxp".equals(action)){
                                            if(intent.hasExtra("isNeedFloadOnSys")){
                                                isNeedFloadOnSys = intent.getBooleanExtra("isNeedFloadOnSys",false);
                                            }
                                            Intent check = new Intent("com.click369.control.float.checkxp");
                                            check.putExtra("isfloatok",isFloatOk&&isNeedFloadOnSys);
                                            amsContext.sendBroadcast(check);
                                        }else if("com.click369.control.ams.getpreventinfo".equals(action)){
                                            if(intent.hasExtra("isclear")){
                                                preventPkgs.clear();
                                                killPkgs.clear();
                                                startPkgs.clear();
                                            }else{
                                                Intent check = new Intent("com.click369.control.recpreventinfo");
                                                check.putExtra("preventPkgs",preventPkgs);
                                                check.putExtra("killPkgs",killPkgs);
                                                check.putExtra("startPkgs",startPkgs);
                                                amsContext.sendBroadcast(check);
                                            }
                                        }else if("com.click369.control.ams.reloadskipnotify".equals(action)){
                                            notifySkipKeyWords.clear();
                                            Set<String> sets = (Set<String>)((Map)intent.getSerializableExtra("skipDialogPrefs")).get(Common.PREFS_SKIPNOTIFY_KEYWORDS);
                                            if(sets!=null){
                                                notifySkipKeyWords.addAll(sets);
                                            }
                                        }else if("com.click369.control.ams.checkhook".equals(action)){
                                           Intent intent1 = new Intent("com.click369.control.hookok");
                                           context.sendBroadcast(intent1);
                                        }else if("com.click369.control.ams.initreload".equals(action)){
                                            isNeedFloadOnSys = intent.getBooleanExtra("isNeedFloadOnSys",false);
                                            initData(intent);
                                            if(recentPrefHMs.size()==0){
                                                recentPrefs.reload();
                                                recentPrefHMs.putAll(recentPrefs.getAll());
                                            }
                                            if(appStartPrefHMs.size()==0){
                                                autoStartPrefs.reload();
                                                appStartPrefHMs.putAll(autoStartPrefs.getAll());
                                            }
                                            if(controlHMs.size()==0){
                                                controlPrefs.reload();
                                                controlHMs.putAll(controlPrefs.getAll());
                                            }

                                        }else if("com.click369.control.ams.sendprivacyinfo".equals(action)){
                                           String pkg = intent.getStringExtra("pkg");
                                            HashMap<Long,String> infos = (HashMap<Long,String>)intent.getSerializableExtra("infos");
                                            if(privacyInfos.containsKey(pkg)){
                                                if(privacyInfos.get(pkg).size()>500){
                                                    privacyInfos.get(pkg).clear();
                                                }
                                                privacyInfos.get(pkg).putAll(infos);
                                            }else{
                                                HashMap<Long,String> minfos = new HashMap<Long,String>();
                                                minfos.putAll(infos);
                                                privacyInfos.put(pkg,minfos);
                                            }
//                                            XposedBridge.log("CONTROL_SETPRIVACY "+pkg+"  " + infos.size() + "^^^^^^^^^^^^^^^^^");
                                        }else if("com.click369.control.ams.getprivacyinfo".equals(action)){
                                            String pkg = intent.getStringExtra("pkg");
                                            HashMap<Long,String> minfos =privacyInfos.containsKey(pkg)?privacyInfos.get(pkg):new HashMap<Long,String>();
                                            Intent intent1 = new Intent("com.click369.control.recprivacyinfo");
                                            intent1.putExtra("infos",minfos);
                                            context.sendBroadcast(intent1);
//                                            XposedBridge.log("CONTROL_GETPRIVACY "+pkg+"  " + minfos.size() + "^^^^^^^^^^^^^^^^^");
                                        }else if("com.click369.control.ams.clearprivacyinfo".equals(action)){
                                            String pkg = intent.getStringExtra("pkg");
                                            if(privacyInfos.containsKey(pkg)){
                                                privacyInfos.get(pkg).clear();
                                            }
                                        }
                                    }catch (Throwable e){
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
                                filter.addAction("com.click369.control.ams.initreload");
                                filter.addAction("com.click369.control.ams.reloadskipnotify");
                                filter.addAction("com.click369.control.ams.confirmforcestop");
                                filter.addAction("com.click369.control.ams.checktimeoutapp");
                                filter.addAction("com.click369.control.ams.float.checkxp");
                                filter.addAction("com.click369.control.ams.getpreventinfo");
                                filter.addAction("com.click369.control.ams.checkhook");
                                filter.addAction("com.click369.control.ams.sendprivacyinfo");
                                filter.addAction("com.click369.control.ams.getprivacyinfo");
                                filter.addAction("com.click369.control.ams.clearprivacyinfo");
                                filter.addAction("click369.main.finish.checkok");
                                filter.addAction(Intent.ACTION_SCREEN_ON);
                                filter.addAction(Intent.ACTION_SCREEN_OFF);
                                amsContext.registerReceiver(br, filter);
                                ISAMSHOOK = true;
                                XposedHelpers.setAdditionalStaticField(amsCls, "click369res", amsContext.hashCode());
                                XposedBridge.log("CONTROL_BOOTCOMPLETE");
                                initData(null);
                                isFlyme = isFlyme();
                            }
                        }
                    }
                }catch (Throwable e) {
                    XposedBridge.log("^^^^^^^^^^^^^^hook AMS error " + e + "^^^^^^^^^^^^^^^^^");
                }
                }
            };
            XposedUtil.hookMethod(amsCls,amsMethods.get("finishBooting").getParameterTypes(),"finishBooting",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^finishBooting  函数未找到^^^^^^^^^^^^^^^^^");
        }

        if(amsMethods.containsKey("forceStopPackage")||
                amsMethods.containsKey("killApplication")||
                amsMethods.containsKey("killApplicationProcess")){
            XC_MethodHook  hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try{
                        String pkgTemp = (String)methodHookParam.args[0];
                        String pkg = (String)methodHookParam.args[0];
                        if(pkg.contains(":")){
                            pkg = pkg.split(":")[0];
                        }
                        if (notStopPkgs.contains(pkg)) {
                            methodHookParam.setResult(null);
                            return;
                        }
                        if(!pkgTemp.contains(":")){
                            if(!preventInfo.startsWith(pkgTemp+"|")){
                                killPkgs.put(System.currentTimeMillis(),pkg);
                            }
                        }
                        if(pkg.equals("com.tencent.mm")&&
                                (methodHookParam.method.getName().equals("forceStopPackage"))){
//                            if("com.tencent.mm".equals(pkg)){
                                Field procListField = amsCls.getDeclaredField("mLruProcesses");
                                procListField.setAccessible(true);
                                ArrayList procs = new ArrayList();
                                ArrayList procsTemp = (ArrayList)procListField.get(amsObject);
                                if(procsTemp!=null&&procsTemp.size()>0){
                                    procs.addAll(procsTemp);
                                    for (Object proc:procs){
                                        Field infoField = proc.getClass().getDeclaredField("info");
                                        infoField.setAccessible(true);
                                        ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                                        if("com.tencent.mm".equals(info.packageName)){
                                            Field persistentField = processRecordCls.getDeclaredField("persistent");
                                            persistentField.setAccessible(true);
                                            persistentField.set(proc,false);
                                        }
                                    }
                                }
//                            }
                        }
                    } catch (Throwable e) {
                        XposedBridge.log("^^^^^^^^^^^^^^hook AMS forceStopPackage err "+e+"^^^^^^^^^^^^^^^^^");
                    }
                }
            };
            try {
                if(amsMethods.containsKey("forceStopPackage")){
                    final Class clss[] = amsMethods.get("forceStopPackage").getParameterTypes();
                    XposedUtil.hookMethod(amsCls,clss,"forceStopPackage",hook);
                }

               if(amsMethods.containsKey("killApplication")){
                    final Class clss[] = amsMethods.get("killApplication").getParameterTypes();
                    XposedUtil.hookMethod(amsCls,clss,"killApplication",hook);
                }
                if(amsMethods.containsKey("killApplicationProcess")){
                    final Class clss[] = amsMethods.get("killApplicationProcess").getParameterTypes();
                    XposedUtil.hookMethod(amsCls,clss,"killApplicationProcess",hook);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^forceStopPackage  函数未找到^^^^^^^^^^^^^^^^^");
        }
        if(taskRecordCls!=null){
            try {
                XposedUtil.hookMethod(taskRecordCls, XposedUtil.getParmsByName(taskRecordCls, "removeTaskActivitiesLocked"), "removeTaskActivitiesLocked", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Field mAffiliatedTaskIdField = taskRecordCls.getDeclaredField("mAffiliatedTaskId");
                            Field intentField = taskRecordCls.getDeclaredField("intent");
                            mAffiliatedTaskIdField.setAccessible(true);
                            intentField.setAccessible(true);
                            Object intentObject = intentField.get(param.thisObject);
                            String pkg = null;
                            String cls = null;
                            if (intentObject != null) {
                                pkg = ((Intent) intentObject).getComponent().getPackageName();
                                if(pkg == null){
                                    Field affinityField = taskRecordCls.getDeclaredField("affinity");
                                    affinityField.setAccessible(true);
                                    pkg = (String)affinityField.get(param.thisObject);
                                }
                                if(notStopPkgs.contains(pkg)||("com.tencent.mm".equals(pkg))){
                                    param.setResult(null);
                                    return;
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                    final Class clssact = XposedUtil.findClass("android.app.ActivityManager",lpparam.classLoader);
////                    final Class clsstasksnap = XposedUtil.findClass("com.android.server.wm.TaskSnapshotController",lpparam.classLoader);
//                    XposedUtil.hookMethod(taskRecordCls, XposedUtil.getMaxLenParmsByName(taskRecordCls, "setIntent"), "setIntent", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            Intent intent = (Intent) param.args[0];
//                            XposedBridge.log("TEST_setIntent"+intent+"  "+intent.getBooleanExtra("ismyself",false));
//                            if(intent.getBooleanExtra("ismyself",false)){
//                                Field field = taskRecordCls.getDeclaredField("mActivities");
//                                field.setAccessible(true);
//                                ArrayList list = (ArrayList) field.get(param.thisObject);
//
//                                XposedBridge.log("MMTEST_setIntent 1 "+list.size());
//                            }
//                        }
//                    });
//                    XposedUtil.hookMethod(taskRecordCls, XposedUtil.getParmsByName(taskRecordCls, "addActivityToTop"), "addActivityToTop", new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//                            final Object object =  param.args[0];
//                            Field intentF = object.getClass().getDeclaredField("intent");
//                            Field realActivityF = object.getClass().getDeclaredField("realActivity");
//                            realActivityF.setAccessible(true);
//                            intentF.setAccessible(true);
//                            ComponentName componentName = (ComponentName) realActivityF.get(object);
//                            if(componentName!=null&&componentName.getPackageName().equals("com.tencent.mm")&&componentName.getClassName().endsWith("WebViewUI")){
//                                intentF.setAccessible(true);
//                                Intent intent = (Intent) intentF.get(object);
//                                if(intent!=null&&intent.hasExtra("ismyself")){
//
//                                   handler.postDelayed(new Runnable() {
//                                       @Override
//                                       public void run() {
//                                           try {
//                                               Field field = taskRecordCls.getDeclaredField("mActivities");
//                                               field.setAccessible(true);
//                                               ArrayList list = (ArrayList) field.get(param.thisObject);
//                                               XposedBridge.log("MMTEST_addActivityToTop 1 "+list.size());
//                                               list.add(0,object);
//                                           }
//                                           catch (Exception e){
//
//                                           }
//
//                                       }
//                                   },2000);
//                                    param.setResult(null);
//                                    return;
//                                }
//                            }
//                        }
//                    });
                    XposedUtil.hookMethod(actRecordCls, XposedUtil.getParmsByName(actRecordCls, "createWindowContainer"), "createWindowContainer", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                            try {
                                Field intentF = actRecordCls.getDeclaredField("intent");
                                Field realActivityF = actRecordCls.getDeclaredField("realActivity");
                                realActivityF.setAccessible(true);
                                intentF.setAccessible(true);
                                ComponentName componentName = (ComponentName) realActivityF.get(param.thisObject);
                                if(componentName!=null&&componentName.getPackageName().equals("com.tencent.mm")&&
                                        componentName.getClassName().endsWith(Common.MTEST_NAME_K+Common.MTEST_NAME_L)){
                                    intentF.setAccessible(true);
                                    Intent intent = (Intent) intentF.get(param.thisObject);
                                    if(intent!=null&&intent.hasExtra("ismyself")){
//                                        final  Method method = actRecordCls.getDeclaredMethod("removeWindowContainer");
//                                        method.setAccessible(true);
                                        final  Method method1 = actRecordCls.getDeclaredMethod("resumeKeyDispatchingLocked");
                                        method1.setAccessible(true);
                                        final  Method method2 = actRecordCls.getDeclaredMethod("getDisplayId");
                                        method2.setAccessible(true);
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    final  Field field = actRecordCls.getDeclaredField("mWindowContainerController");
                                                    field.setAccessible(true);
                                                    Object o = field.get(param.thisObject);
                                                    if(o!=null){
                                                        method1.invoke(param.thisObject);
                                                        Method method3 = o.getClass().getDeclaredMethod("removeContainer",int.class);
                                                        method3.setAccessible(true);
                                                        method3.invoke(o,method2.invoke(param.thisObject));
                                                    }

//                                                    XposedBridge.log("MMTEST_createWindowContainer_removeWindowContainer");
//                                                    method.invoke(param.thisObject);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        },6000);
                                    }
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    });
                    XposedUtil.hookMethod(actRecordCls, XposedUtil.getParmsByName(actRecordCls, "removeWindowContainer"), "removeWindowContainer", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                            try {
                                Field intentF = actRecordCls.getDeclaredField("intent");
                                Field realActivityF = actRecordCls.getDeclaredField("realActivity");
                                realActivityF.setAccessible(true);
                                intentF.setAccessible(true);
                                ComponentName componentName = (ComponentName) realActivityF.get(param.thisObject);
                                if(componentName!=null&&componentName.getPackageName().equals("com.tencent.mm")&&
                                        componentName.getClassName().endsWith(Common.MTEST_NAME_K+Common.MTEST_NAME_L)){
                                    intentF.setAccessible(true);
                                    Intent intent = (Intent) intentF.get(param.thisObject);
                                    if(intent!=null&&intent.hasExtra("ismyself")){
                                        final  Field field = actRecordCls.getDeclaredField("mWindowContainerController");
                                        field.setAccessible(true);
                                        Object o = field.get(param.thisObject);
                                        if(o==null){
//                                            XposedBridge.log("MMTEST_removeWindowContainer null");
                                            param.setResult(null);
                                            return;
                                        }
                                    }
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    });
//                    XposedUtil.hookMethod(taskRecordCls, XposedUtil.getParmsByName(taskRecordCls, "getTaskThumbnailLocked"), "getTaskThumbnailLocked", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            Field f = taskRecordCls.getDeclaredField("mStack");
//                            f.setAccessible(true);
//                            Object mStack = f.get(param.thisObject);
//                            if(mStack!=null){
//                                Field f1 = mStack.getClass().getDeclaredField("mResumedActivity");
//                                f1.setAccessible(true);
//                                Object mResumedActivity = f1.get(mStack);
//                                XposedBridge.log("TEST_getTaskThumbnailLocked  "+mResumedActivity);
//                            }
//                        }
//                    });

//                    XposedUtil.hookMethod(actRecordCls, XposedUtil.getParmsByName(actRecordCls, "screenshotActivityLocked"), "screenshotActivityLocked", new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                            Field f0 = clssact.getDeclaredField("ENABLE_TASK_SNAPSHOTS");
//                            Field f = actRecordCls.getDeclaredField("realActivity");
//                            f0.setAccessible(true);
//                            f.setAccessible(true);
//                            ComponentName componentName = (ComponentName) f.get(param.thisObject);
//                            if(componentName!=null){
//                                if("com.tencent.mm".equals(componentName.getPackageName())&&componentName.getClassName().endsWith("LauncherUI")){
//                                    if(recentPrefHMs.containsKey("com.tencent.mm/blur")&&(boolean)recentPrefHMs.get("com.tencent.mm/blur")){
//                                    }else if(recentPrefHMs.containsKey("com.tencent.mm/notclean")&&(boolean)recentPrefHMs.get("com.tencent.mm/notclean")){
//                                        oldSet = (boolean)f0.get(null);
//                                        f0.set(null,false);
//                                    }
//                                }
//                            }
//                        }
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            try {
//                                Field f0 = clssact.getDeclaredField("ENABLE_TASK_SNAPSHOTS");
//                                Field f = actRecordCls.getDeclaredField("realActivity");
//                                f0.setAccessible(true);
//                                f.setAccessible(true);
//                                ComponentName componentName = (ComponentName) f.get(param.thisObject);
//                                if(componentName!=null){
//                                    if("com.tencent.mm".equals(componentName.getPackageName())
//                                        &&componentName.getClassName().endsWith("LauncherUI")){
//                                        if(recentPrefHMs.containsKey("com.tencent.mm/blur")&&(boolean)recentPrefHMs.get("com.tencent.mm/blur")){
//                                        }else if(recentPrefHMs.containsKey("com.tencent.mm/notclean")&&(boolean)recentPrefHMs.get("com.tencent.mm/notclean")){
//                                            f0.set(null,oldSet);
//                                            if(param.getResult()!=null){
//                                                if(bitmapTemp!=null){
//                                                    bitmapTemp.recycle();
//                                                    bitmapTemp = null;
//                                                }
//                                                bitmapTemp = (Bitmap)param.getResult();
////                                                new Thread(){
////                                                    @Override
////                                                    public void run() {
////                                                        try {
//                                                            File file = new File("/data/wxa.temp");
//                                                            file.setReadable(true,false);
//                                                            file.createNewFile();
//                                                            bitmapTemp.compress(Bitmap.CompressFormat.PNG,80,new FileOutputStream(file));
////                                                        }catch (Exception e){
////                                                        }
////                                                    }
////                                                }.start();
//                                                param.setResult(null);
//                                            }
//                                        }
//                                    }
//                                }
//                            }catch (Exception e){
//                                e.printStackTrace();
//                            }
//                        }
//                    });
                }

//                XposedUtil.hookMethod(actRecordCls, XposedUtil.getParmsByName(actRecordCls, "activityStoppedLocked"), "activityStoppedLocked", new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        try {
//                            Field field1 = actRecordCls.getDeclaredField("realActivity");
//                            Field field2 = actRecordCls.getDeclaredField("intent");
//                            field1.setAccessible(true);
//                            field2.setAccessible(true);
//                            Object realActivity = field1.get(param.thisObject);
//                            if(realActivity!=null){
//                                ComponentName cn = (ComponentName)realActivity;
//                                if(cn.getPackageName().equals("com.tencent.mm")&&cn.getClassName().endsWith("WebViewUI")){
//                                    Object in = field2.get(param.thisObject);
//                                    if(in!=null){
//                                        Intent intent = (Intent)in;
//                                        if(intent.hasExtra("ismyself")){
//                                            param.setResult(null);
//                                            return;
//                                        }
//                                    }
//                                }
//                            }
//                        }catch (Exception e){
//                        }
//                    }
//                });
            }catch (Throwable e){
                e.printStackTrace();
            }
        }



////        if(actServiceCls!=null){
////            try {
////                XposedUtil.hookMethod(actServiceCls, XposedUtil.getParmsByName(actServiceCls, "bringDownDisabledPackageServicesLocked"), "bringDownDisabledPackageServicesLocked", new XC_MethodHook() {
////                    @Override
////                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
////                        try {
////                            Object pkg = param.args[0];
////                            if (pkg != null&&Common.PACKAGENAME.equals(pkg)) {
////                                param.setResult(true);
////                                return;
////                            }
////                        }catch (Exception e){
////                            e.printStackTrace();
////                        }
////                    }
////                });
////            }catch (Throwable e){
////                e.printStackTrace();
////            }
////        }
        //保活自己
        if (processRecordCls!=null){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Field infoField = processRecordCls.getDeclaredField("info");
                        infoField.setAccessible(true);

                        ApplicationInfo info = (ApplicationInfo) infoField.get(methodHookParam.thisObject);
                        String pkg = info.packageName;
                        String reason = (String)methodHookParam.args[0];
                        if (notStopPkgs.contains(pkg)&&reason!=null&&!reason.startsWith("stop")) {
                            methodHookParam.setResult(null);
                            return;
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Field infoField = processRecordCls.getDeclaredField("info");
                        infoField.setAccessible(true);
//                        Field hasShownUiField = processRecordCls.getDeclaredField("hasShownUi");
//                        hasShownUiField.setAccessible(true);
//                        final boolean hasShownUi = (boolean)hasShownUiField.get(param.thisObject);

                        ApplicationInfo info = (ApplicationInfo) infoField.get(param.thisObject);
                        final String pkg = info.packageName;
                        if(pkg.equals(info.processName)&&startRuningPkgs.contains(info.packageName)){
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ActivityManager am = (ActivityManager) amsContext.getSystemService(Context.ACTIVITY_SERVICE);
                                        List<ActivityManager.RunningAppProcessInfo> rps =  am.getRunningAppProcesses();
                                        if (rps!=null&&rps.size()>0){
                                            for(ActivityManager.RunningAppProcessInfo ar:rps){
                                                if(ar.pkgList!=null&&ar.pkgList.length>0){
                                                    for(String s:ar.pkgList){
                                                        if(s.equals(pkg)){
                                                            return;
                                                        }
                                                    }
                                                }
                                            }
                                        }else{
                                            return;
                                        }
                                        startRuningPkgs.remove(pkg);
                                        runingTimes.remove(pkg);
                                        startPkg = "";
                                        startProc = "";
                                        if(isStopRemveRecent){
                                            Intent intent = new Intent("com.click369.control.amsremoverecent");
                                            intent.putExtra("pkg",pkg);
                                            amsContext.sendBroadcast(intent);
                                        }
                                        if(gmsPkgs.containsKey(pkg)){
                                            HashSet<String> sets = gmsPkgs.get(pkg);
                                            for(String s:sets){
                                                try {
                                                    Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                                    m.invoke(amsObject, s, 0);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            gmsPkgs.get(pkg).clear();
                                            gmsPkgs.remove(pkg);
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            },800);
                        }
                        if(Common.PACKAGENAME.equals(pkg)){
                            notStopPkgs.remove(pkg);
                            Intent intent = new Intent("com.click369.control.startservice");
                            intent.putExtra("delay",1000);
                            amsContext.sendBroadcast(intent);
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(processRecordCls,XposedUtil.getParmsByName(processRecordCls,"kill"),"kill",hook);
        }

        if(amCls!=null){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        int uid = (int)param.args[1];
//                        if((SYSUI_UID!=0&&SYSUI_UID==uid)){//||(SYSUI_UID!=0&&SYSUI_UID==uid) (MUID!=0&&uid == MUID)||
//                            param.setResult(PackageManager.PERMISSION_GRANTED);
//                            return;
//                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        XposedBridge.log("checkComponentPermission err "+e.getMessage()+"^^^^^^^^^^^^^^^^^");
                    }

                }
            };
            XposedUtil.hookMethod(amCls, XposedUtil.getParmsByName(amCls, "checkComponentPermission"), "checkComponentPermission",hook);
            XposedUtil.hookMethod(amCls, XposedUtil.getParmsByName(amCls, "checkUidPermission"), "checkUidPermission",hook);
        }



//        isAppstart = settingPrefs.getBoolean(Common.ALLSWITCH_AUTOSTART_LOCK,true);

        //自启控制中自启动
        if(amsMethods.containsKey("startProcessLocked")){
            final int lenTemp = XposedUtil.hook_methodLen(amsCls,"startProcessLocked");
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                    try{
                        int len = lenTemp<10?6:lenTemp-1;
                        boolean cod1 = methodHookParam.args.length>len&&
                                (methodHookParam.args[1] instanceof ApplicationInfo)&&
                                (methodHookParam.args[4] instanceof String);
                        if(cod1&&methodHookParam.args[1]!=null){
                            String pkg = "";
                            String hostingType = "";
                            ComponentName cn = null;
                            ApplicationInfo applicationInfo = (ApplicationInfo) methodHookParam.args[1];
                            if(applicationInfo.packageName!=null){
                                pkg = applicationInfo.packageName;
                                hostingType = (String)methodHookParam.args[4];
                                if(methodHookParam.args[5] instanceof ComponentName){
                                    cn = (ComponentName) methodHookParam.args[5];
                                }else{
                                    cn = new ComponentName("","");
                                }
                                if(isAppstart){
                                    if(!"android".equals(pkg)){
                                        boolean isPrevent = false;
                                        boolean isAutoHM = appStartPrefHMs.containsKey(pkg+"/autostart")?(boolean)(appStartPrefHMs.get(pkg+"/autostart")):false;
                                        if(isAutoHM&&!pkg.equals(startPkg)){
                                            boolean isContainsPkgRuning = false;
                                            ActivityManager am = (ActivityManager) amsContext.getSystemService(Context.ACTIVITY_SERVICE);
                                            List<ActivityManager.RunningAppProcessInfo> rps =  am.getRunningAppProcesses();
                                            if (rps!=null&&rps.size()>0){
                                                for(ActivityManager.RunningAppProcessInfo ar:rps){
                                                    if(ar.pkgList!=null&&ar.pkgList.length>0){
                                                        for(String s:ar.pkgList){
                                                            if(s.equals(pkg)){
                                                                isContainsPkgRuning = true;
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
//                                            Field procListField = amsCls.getDeclaredField("mLruProcesses");
//                                            procListField.setAccessible(true);
//                                            ArrayList procsTemp = (ArrayList)procListField.get(methodHookParam.thisObject);
//                                            for(Object o:procsTemp){
//                                                Field infoField = o.getClass().getDeclaredField("info");
//                                                infoField.setAccessible(true);
//                                                ApplicationInfo info = (ApplicationInfo)infoField.get(o);
//                                                if(pkg.equals(info.packageName)){
//                                                    isContainsPkgRuning = true;
//                                                    break;
//                                                }
//                                            }
                                            if(!isContainsPkgRuning&&!"activity".equals(hostingType)){
                                                isPrevent = true;
                                            }else if(cn!=null&&!isContainsPkgRuning){
                                                if (!isContainsPkgRuning&&cn!=null&&
                                                        "activity".equals(hostingType)&&
                                                        (cn.getClassName().contains(".GActivity")||cn.getClassName().contains(".PushActivity")||cn.getClassName().contains(".PushGTActivity"))){
                                                    isPrevent = true;
                                                }else if(appStartPrefHMs.containsKey(pkg+"/checkautostart")){
                                                    Object jumpAct = appStartPrefHMs.get(pkg+"/jumpactivity");
                                                    Object homeAct = appStartPrefHMs.get(pkg+"/homeactivity");
                                                    if(((jumpAct!=null&&jumpAct.equals(cn.getClassName()))||(homeAct!=null&&!homeAct.equals(cn.getClassName())))){
                                                        isPrevent = true;
                                                    }
                                                }
                                            }
                                        }else if(appStartPrefHMs.containsKey(pkg+"/stopapp")&&
                                                (boolean)appStartPrefHMs.get(pkg+"/stopapp")){
                                            isPrevent = true;
                                        }
                                        String saveInfo = pkg+"|"+cn.getClassName()+"|"+methodHookParam.args[0]+"|"+hostingType;//+appStartPrefHMs.size()+test;//+isAutoHM+isPrevent+isContainsPkgRuning;
                                        //关联启动谷歌服务
                                        if(isPrevent&&
                                                System.currentTimeMillis()-lastOpenActivityTime<3000&&
                                                SystemClock.elapsedRealtime()>1000*60*2&&
                                                lastOpenActivityPkg.length()>0&&
                                                ("com.google.android.gms".equals(pkg)||
                                                "com.google.android.gsf".equals(pkg)||
                                                "com.google.android.tts".equals(pkg))){
                                            isPrevent = false;
                                            if(gmsPkgs.containsKey(lastOpenActivityPkg)){
                                                gmsPkgs.get(lastOpenActivityPkg).add(pkg);
                                            }else{
                                                HashSet<String> sets = new HashSet<String>();
                                                sets.add(pkg);
                                                gmsPkgs.put(lastOpenActivityPkg,sets);
                                            }
                                        }

                                        if(isPrevent){
                                            if(preventPkgs.size()>200||startPkgs.size()>200){
                                                preventPkgs.clear();
                                                startPkgs.clear();
                                                killPkgs.clear();
                                            }
//                                            XposedBridge.log(pkg+" "+saveInfo+"   "+preventInfo);
                                            if(("activity".equals(hostingType)||
                                                    "service".equals(hostingType))&&
                                                    saveInfo.equals(preventInfo)&&
                                                    System.currentTimeMillis()-lastPreventTime<800){
                                                preventPkgTime++;
                                                if(preventPkgTime>5){
//                                                    appStartPrefHMs.remove(pkg+"/autostart");
                                                    preventPkgTime = 0;
                                                    isPrevent = false;
                                                    Intent intent = new Intent("com.click369.control.amsalert");
                                                    intent.putExtra("pkg",pkg);
                                                    intent.putExtra("info","被频繁启动很可能其他APP需要依赖该应用，应用控制器本次取消对其阻止，请检查设置");
                                                    Context sysCxt = (Context) sysCxtField.get(methodHookParam.thisObject);
                                                    sysCxt.sendBroadcast(intent);
                                                }
                                            }else{
                                                preventPkgTime = 0;
                                            }
                                            if(isPrevent){
                                                preventInfo = saveInfo;
                                                //保存 包名:启动的组件的类名:进程名
                                                preventPkgs.put(System.currentTimeMillis(),saveInfo);
                                                lastPreventTime = System.currentTimeMillis();
                                                Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                                m.invoke(methodHookParam.thisObject, pkg,0);

                                                methodHookParam.setResult(null);
                                                return;
                                            }
                                        }
                                        if(methodHookParam.args[0]!=null&&!methodHookParam.args[0].equals(startProc)){
                                            startPkgs.put(System.currentTimeMillis(),saveInfo);
                                        }
                                        if(!notStopPkgs.contains(pkg)){
                                            boolean isNotStop = appStartPrefHMs.containsKey(pkg + "/notstop") ? (boolean) (appStartPrefHMs.get(pkg+ "/notstop")) : false;
                                            if(isNotStop){
                                                notStopPkgs.add(pkg);
                                            }
                                            if(Common.PACKAGENAME.equals(pkg)){
//                                                if(!isFlyme){
//                                                    notStopPkgs.add(Common.PACKAGENAME);
//                                                }
                                                XposedBridge.log("CONTROL_START_WDS_SUCCESS");
                                            }
                                        }
                                        startPkg = pkg;
                                        startProc = (String)methodHookParam.args[0];
                                    }
                                }
                                if("activity".equals(hostingType)){
                                    lastOpenActivityPkg = pkg;
                                    lastOpenActivityTime = System.currentTimeMillis();
                                }
                                startRuningPkgs.add(pkg);
                                if(!runingTimes.containsKey(pkg)){
                                    runingTimes.put(pkg,SystemClock.uptimeMillis());
                                }
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hook_methods(amsCls,"startProcessLocked",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^startProcessLocked  函数未找到^^^^^^^^^^^^^^^^^");
        }

//        if(amsMethods.containsKey("bindService")){
//            final Class clss[] = amsMethods.get("bindService").getParameterTypes();
//            XC_MethodHook hook = new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                try{
//                    if(isAppstart&&clss.length>2&&methodHookParam.args[2] instanceof Intent) {
//                        Intent intent = (Intent) methodHookParam.args[2];
//                        if(intent!=null&&intent.getComponent()!=null){
//                            String pkg = intent.getComponent().getPackageName();
//                            boolean isAutoHM = appStartPrefHMs.containsKey(pkg+"/autostart")?(Boolean)(appStartPrefHMs.get(pkg+"/autostart")):false;
//                            if(isAutoHM&&!startRuningPkgs.contains(pkg)){
//                                String saveInfo = pkg+"|"+intent.getComponent().getClassName()+"|bindservice";
//                                preventPkgs.put(System.currentTimeMillis(),saveInfo);
//                                if(saveInfo.equals(preventInfo)) {
//                                    preventPkgTime++;
//                                    if (preventPkgTime > 6) {
//                                        appStartPrefHMs.remove(pkg + "/autostart");
//                                        preventPkgTime = 0;
//                                        Intent intent1 = new Intent("com.click369.control.amsalert");
//                                        intent1.putExtra("pkg", pkg);
//                                        intent1.putExtra("info", "被频繁启动并且频繁阻止，应用控制器本次取消对其阻止，请检查设置");
//                                        Context sysCxt = (Context) sysCxtField.get(methodHookParam.thisObject);
//                                        sysCxt.sendBroadcast(intent);
//                                    }
//                                }else{
//                                    preventPkgTime = 0;
//                                }
//                                preventInfo = saveInfo;
//                                methodHookParam.setResult(0);
//                                return;
//                            }
//                        }
//                    }
//                } catch (Throwable e) {
//                    XposedBridge.log("^^^^^^^^^^^^^^AMS阻止服务出错 "+e+ "^^^^^^^^^^^^^^^^^");
//                }
//                }
//            };
//            XposedUtil.hookMethod(amsCls,clss,"bindService",hook);
//        }else{
//            XposedBridge.log("^^^^^^^^^^^^^^bindService  函数未找到^^^^^^^^^^^^^^^^^");
//        }

        if(ifwCls!=null&&!isFlyme()){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        ComponentName cn = (ComponentName)param.args[1];
                        int type = (int)param.args[2];
//                    Intent  intent = (Intent)param.args[3];
//                    String  type = (String)param.args[6];
                        if((type==1||type==2)&&cn!=null&&startRuningPkgs.size()>2){//0activity  1broad  2service
                            String pkg = cn.getPackageName();
                            String cls = cn.getClassName();
                            if(Common.PACKAGENAME.equals(pkg)){
                                param.setResult(true);
                                return;
                            }
                            if(isAppstart){
                                boolean isAutoHM = appStartPrefHMs.containsKey(pkg+"/autostart")?(boolean)(appStartPrefHMs.get(pkg+"/autostart")):false;
                                if(type==2&&("com.google.android.gms".equals(pkg)||
                                        "com.google.android.gsf".equals(pkg)||
                                        "com.google.android.tts".equals(pkg))){//排除谷歌play
                                }else{
                                    if(isAutoHM&&!startRuningPkgs.contains(pkg)){
                                        preventPkgs.put(System.currentTimeMillis(),pkg+"|"+cls+"|."+pkg+"|"+(type==1?"broadcast":"service"));
                                        if(preventPkgs.size()>200||startPkgs.size()>200){
                                            preventPkgs.clear();
                                            startPkgs.clear();
                                            killPkgs.clear();
                                        }
                                        param.setResult(false);
                                        return;
                                    }
                                }
                            }
                            if(type == 2&&!("com.google.android.gms".equals(pkg)||
                                    "com.google.android.gsf".equals(pkg)||
                                    "com.google.android.tts".equals(pkg))){
                                if ((controlHMs.containsKey(pkg + "/service")&&((boolean)controlHMs.get(pkg + "/service"))&& isOneOpen)||
                                        (muBeiHSs.contains(pkg) && isTwoOpen)) {//||(muBeiHSs.contains(pkg) && isTwoOpen)
                                    if (!controlHMs.containsKey(cls + "/service")) {
                                        param.setResult(false);
                                        return;
                                    }
                                }
                                if (SystemClock.elapsedRealtime() < 1000 * 60 * 2 && isStopScanMedia) {
                                    if (cls.endsWith("MediaScannerService")) {
                                        Method m = amsCls.getDeclaredMethod("forceStopPackage", String.class, int.class);
                                        m.invoke(amsObject, pkg, 0);
                                        param.setResult(false);
                                        return;
                                    }
                                }
                            }else if(type == 1){
                                if ((isOneOpen && controlHMs.containsKey(pkg + "/broad")&&((boolean)controlHMs.get(pkg + "/broad")))||
                                        (muBeiHSs.contains(pkg) && isTwoOpen)) {
                                    if(!pkg.equals("com.tencent.mm")){
                                        param.setResult(false);
                                        return;
                                    }
                                }
                            }
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(ifwCls,XposedUtil.getParmsByName(ifwCls,"checkIntent"),"checkIntent",hook);
        }else if(amsMethods.containsKey("startService")){//FLYME系统直接用意图过滤器过滤墓碑会导致wifi有问题，所以用startService来处理墓碑应用
            final Class clss[] = amsMethods.get("startService").getParameterTypes();
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try{
                        if(isTwoOpen) {
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
                            if ((controlHMs.containsKey(callingPkg + "/service")&&((boolean)controlHMs.get(callingPkg + "/service"))&& isOneOpen)||(muBeiHSs.contains(callingPkg) && isTwoOpen)) {
                                if (intent != null && intent.getComponent() != null && controlHMs.containsKey(intent.getComponent().getClassName() + "/service")) {
                                } else {
                                    methodHookParam.setResult(intent == null ? new ComponentName("", "") : intent.getComponent());
                                    return;
                                }
                            }
                        }
                    } catch (Throwable e) {
                        XposedBridge.log("CONTROL_AMS阻止服务出错 "+e);
                    }
                }
            };
            XposedUtil.hookMethod(amsCls,clss,"startService",hook);
        }else{
            XposedBridge.log("CONTROL_startService  函数未找到");
        }
        //阻止广播发送相关
//        if(amsMethods.containsKey("broadcastIntentLocked")){
//            XC_MethodHook hook = new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                try{
//                    if(isOneOpen||isTwoOpen) {
//                        //阻止往出发广播
//                        String callingPackage = ((String) methodHookParam.args[1]) + "";
//                        if (isOneOpen) {
//                            controlPrefs.reload();
//                        }
////                    if((isOneOpen&&controlHMs.containsKey(callingPackage+"/broad"))){
////                       (isMubeiStopOther && isTwoOpen && muBeiHSs.contains(callingPackage)
//                        if ((isOneOpen && controlHMs.containsKey(callingPackage + "/broad")&&
//                                controlHMs.get(callingPackage + "/broad")==(Boolean)true)) {
//                            boolean isSend = false;
//                            if (methodHookParam.args[2] != null) {
//                                Intent intent = (Intent) methodHookParam.args[2];
//                                isSend = (intent.getAction() + "").contains("click369");
//                            }
//                            if (!isSend) {
//                                methodHookParam.setResult(-1);
//                                return;
//                            }
//                        }
//                    }
//                } catch (Throwable e) {
//                    e.printStackTrace();
//                }
//                }
//            };
//            try{
//                final Class clss[] = amsMethods.get("broadcastIntentLocked").getParameterTypes();
//                XposedUtil.hookMethod(amsCls,clss,"broadcastIntentLocked",hook);
//            }catch (Throwable e){
//                e.printStackTrace();
//            }
//        }else{
//            XposedBridge.log("^^^^^^^^^^^^^^broadcastIntentLocked  函数未找到 ^^^^^^^^^^^^^^^^^");
//        }
//        if(true){
//            return;
//        }
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
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                }
            };
            XposedUtil.hookMethod(amsCls,amsMethods.get("checkBroadcastFromSystem").getParameterTypes(),"checkBroadcastFromSystem",hookBroadPerm);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^checkBroadcastFromSystem  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

//        isRecentOpen = settingPrefs.getBoolean(Common.ALLSWITCH_RECNETCARD,true);
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
                        String pkg = intent != null&& intent.getComponent()!=null?intent.getComponent().getPackageName():"";
                        if (recentPrefHMs.containsKey(pkg+ "/notshow")) {
                            Field isAvailableField = taskRecordCls.getDeclaredField("isAvailable");
                            isAvailableField.setAccessible(true);
//                            XposedBridge.log("CONTROL_changerecent_"+pkg+"  "+(!(Boolean)recentPrefHMs.get(pkg + "/notshow")));
                            isAvailableField.set(recentObj, !(Boolean)recentPrefHMs.get(pkg + "/notshow"));
                        }
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
                }
            };
            XposedUtil.hookMethod(amsCls, amsMethods.get("createRecentTaskInfoFromTaskRecord").getParameterTypes(),"createRecentTaskInfoFromTaskRecord",hook);
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^createRecentTaskInfoFromTaskRecord  函数未找到 ^^^^^^^^^^^^^^^^^");
        }

        //最近任务保留常驻内存
        if(processRecordCls!=null){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try{
                    if (isAppstart){
                        Field infoField = processRecordCls.getDeclaredField("info");
                        infoField.setAccessible(true);
                        ApplicationInfo info = (ApplicationInfo)infoField.get(methodHookParam.thisObject);
                        boolean isNotClean = appStartPrefHMs.containsKey(info.packageName+"/notstop")?(boolean)(appStartPrefHMs.get(info.packageName+"/notstop")):false;
//                        boolean isTen = info.packageName.equals("com.tencent.mm")&&info.processName.equals(info.packageName);
                        if (isNotClean||info.packageName.equals(Common.PACKAGENAME)){
                            if(info.packageName.equals(Common.PACKAGENAME)){
                                MUID = info.uid;
                            }
//                            else if(info.packageName.equals("com.android.systemui")){
//                                SYSUI_UID = info.uid;
//                            }
                            try {
                                Field systemNoUiField = processRecordCls.getDeclaredField("systemNoUi");
                                systemNoUiField.setAccessible(true);
                                systemNoUiField.set(methodHookParam.thisObject, true);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try {
                                Field hasTopUiField = processRecordCls.getDeclaredField("hasTopUi");
                                hasTopUiField.setAccessible(true);
                                hasTopUiField.set(methodHookParam.thisObject,true);
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            try {
                                Field foregroundActivitiesField = processRecordCls.getDeclaredField("foregroundActivities");
                                foregroundActivitiesField.setAccessible(true);
                                foregroundActivitiesField.set(methodHookParam.thisObject,true);

                                Field field = processListCls.getDeclaredField("SYSTEM_ADJ");
                                field.setAccessible(true);
                                sysadj = (int)field.get(null);

                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try {
                                Field setAdjField = processRecordCls.getDeclaredField("setAdj");
                                setAdjField.setAccessible(true);
                                setAdjField.set(methodHookParam.thisObject,sysadj);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try {
                                Field curAdjField = processRecordCls.getDeclaredField("curAdj");
                                curAdjField.setAccessible(true);
                                curAdjField.set(methodHookParam.thisObject,sysadj);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            try {
                                Field maxAdjField = processRecordCls.getDeclaredField("maxAdj");
                                maxAdjField.setAccessible(true);
                                maxAdjField.set(methodHookParam.thisObject,sysadj);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
                }
            };
            Constructor cs[] = processRecordCls.getDeclaredConstructors();
            if(cs!=null&&cs.length>0){
                XposedUtil.hookConstructorMethod(processRecordCls,cs[0].getParameterTypes(),hook);
            }
            XposedUtil.hookMethod(processRecordCls, XposedUtil.getParmsByName(processRecordCls, "modifyRawOomAdj"), "modifyRawOomAdj", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Field infoField = processRecordCls.getDeclaredField("info");
                        infoField.setAccessible(true);
                        ApplicationInfo info = (ApplicationInfo)infoField.get(param.thisObject);

                        if (isAppstart){
                            Field emptyField = processRecordCls.getDeclaredField("empty");
                            emptyField.setAccessible(true);
                            boolean isEmpty = (boolean)emptyField.get(param.thisObject);
                            if(!isEmpty){
                                boolean isNotClean = appStartPrefHMs.containsKey(info.packageName+"/notstop")?(boolean)(appStartPrefHMs.get(info.packageName+"/notstop")):false;
                                if (isNotClean){
                                    Field reportLowMemoryField = processRecordCls.getDeclaredField("reportLowMemory");
                                    reportLowMemoryField.setAccessible(true);
                                    reportLowMemoryField.set(param.thisObject,false);

                                    param.setResult(sysadj);
                                    return;
                                }
                            }
                        }
                        boolean isTen = info.packageName.equals("com.tencent.mm")&&info.processName.equals(info.packageName);
                        if(isTen){
                            //activities
                            Field activitiesField = processRecordCls.getDeclaredField("activities");
                            activitiesField.setAccessible(true);
                            ArrayList list = (ArrayList) activitiesField.get(param.thisObject);
                            Field field = processListCls.getDeclaredField("FOREGROUND_APP_ADJ");
                            field.setAccessible(true);
                            int myadj = (int)field.get(null);//
//                            XposedBridge.log("tenc .. pre");
                            if(list!=null&&list.size()>=2){
//                                XposedBridge.log("t nc .. pre1");
                                try {
                                    Field persistentField = processRecordCls.getDeclaredField("persistent");
                                    persistentField.setAccessible(true);
                                    persistentField.set(param.thisObject,true);

                                    Field setAdjField = processRecordCls.getDeclaredField("setAdj");
                                    setAdjField.setAccessible(true);
                                    setAdjField.set(param.thisObject,myadj);

                                    Field reportLowMemoryField = processRecordCls.getDeclaredField("reportLowMemory");
                                    reportLowMemoryField.setAccessible(true);
                                    reportLowMemoryField.set(param.thisObject,false);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                try {

                                    Field curAdjField = processRecordCls.getDeclaredField("curAdj");
                                    curAdjField.setAccessible(true);
                                    curAdjField.set(param.thisObject,myadj);
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                try {
                                    Field maxAdjField = processRecordCls.getDeclaredField("maxAdj");
                                    maxAdjField.setAccessible(true);
                                    maxAdjField.set(param.thisObject,myadj);
//                                    XposedBridge.log("t nc .. pre2");
                                    param.setResult(myadj);
                                    return;
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            });
            //****************常驻内存 防止低内存杀死
            if(amsMethods.containsKey("doLowMemReportIfNeededLocked")){
                XposedUtil.hookMethod(amsCls, amsMethods.get("doLowMemReportIfNeededLocked").getParameterTypes(), "doLowMemReportIfNeededLocked", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            Object proc = param.args[0];
                            if(proc!=null){
                                Field infoField = proc.getClass().getDeclaredField("info");
                                infoField.setAccessible(true);
                                ApplicationInfo info = (ApplicationInfo)infoField.get(param.thisObject);
                                if(notStopPkgs.contains(info.packageName)){
                                    param.setResult(null);
                                    return;
                                }else if(info.packageName.equals("com.tencent.mm")){
                                    Field activitiesField =  proc.getClass().getDeclaredField("activities");
                                    activitiesField.setAccessible(true);
                                    ArrayList list = (ArrayList) activitiesField.get(param.thisObject);
                                    if(list.size()>=2){
                                        param.setResult(null);
                                        return;
                                    }
                                }
                            }
                        }catch (Exception e){
                        }
                    }
                });
            }
        }else{
            XposedBridge.log("^^^^^^^^^^^^^^ProcessRecord1  构造函数未找到 ^^^^^^^^^^^^^^^^^");
        }
        //        final Method anyTaskForIdLockedMethod = XposedUtil.getMethodByName(activityStackSupervisorCls,"anyTaskForIdLocked");
        //最近任务保留或移除相关
//        if(amsMethods.containsKey("removeTaskByIdLocked")){
        if(activityStackSupervisorCls!=null&&amsMethods.containsKey("removeTask")){
            XC_MethodHook hook = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        if(isRecentOpen||isAppstart) {
                            Field mStackSupervisorField = amsCls.getDeclaredField("mStackSupervisor");
                            mStackSupervisorField.setAccessible(true);
                            Object mStackSupervisorObject = mStackSupervisorField.get(methodHookParam.thisObject);
                            final Method anyTaskForIdLockedMethod = activityStackSupervisorCls.getDeclaredMethod("anyTaskForIdLocked",int.class);
                            anyTaskForIdLockedMethod.setAccessible(true);
                            Object taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0]);
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
                                    if(pkg == null){
                                        Field affinityField = taskRecordCls.getDeclaredField("affinity");
                                        affinityField.setAccessible(true);
                                        pkg = (String)affinityField.get(taskRecordObject);
                                    }
                                }
                                boolean isForceClean = recentPrefHMs.containsKey(pkg + "/forceclean")&&(Boolean) recentPrefHMs.get(pkg + "/forceclean");
                                if (pkg != null && recentPrefHMs.containsKey(pkg + "/notclean")&&(Boolean) recentPrefHMs.get(pkg + "/notclean")) {

                                    if("com.tencent.mm".equals(pkg)&&!"com.tencent.mm.ui.LauncherUI".equals(cls)){
                                    }else {
                                        methodHookParam.setResult(false);
                                        return;
                                    }
                                } else if (pkg != null && isForceClean) {
                                    if ("com.tencent.mm".equals(pkg) && "com.tencent.mm.plugin.appbrand.ui.AppBrandUI".equals(cls)) {
                                    } else {
                                        Intent intent = new Intent("com.click369.control.removerecent");
                                        intent.putExtra("pkg", pkg);
                                        amsContext.sendBroadcast(intent);
                                    }
                                }
                                if(isAppstart&&
                                        methodHookParam.args.length>1&&
                                        (notStopPkgs.contains(pkg)||(!isForceClean&&"com.tencent.mm".equals(pkg)&&!"com.tencent.mm.plugin.appbrand.ui.AppBrandUI".equals(cls)))&&
                                        (methodHookParam.args[1] instanceof Boolean)){
                                    methodHookParam.args[1]=false;
                                }
                            } else {
                                XposedBridge.log("^^^^^^^^^^^^^^taskRecordObject removeTask 对象获取失败 ^^^^^^^^^^^^^^^^^");
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        XposedBridge.log("^^^^^^^^^^^^^^removeTask error0 "+e.getMessage()+" ^^^^^^^^^^^^^^^^^");
                    }
                }
            };
            XposedUtil.hookMethod(amsCls,amsMethods.get("removeTask").getParameterTypes(),"removeTask",hook);

            if(amsMethods.containsKey("removeTaskByIdLocked")){
                XposedUtil.hookMethod(amsCls,amsMethods.get("removeTaskByIdLocked").getParameterTypes(),"removeTaskByIdLocked",hook);
            }else{
                XC_MethodHook hook1 = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            if(isAppstart) {
                                final Method anyTaskForIdLockedMethod = activityStackSupervisorCls.getDeclaredMethod("anyTaskForIdLocked",int.class);
                                anyTaskForIdLockedMethod.setAccessible(true);
                                Object taskRecordObject = anyTaskForIdLockedMethod.invoke(methodHookParam.thisObject, methodHookParam.args[0]);
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
                                        if(pkg == null){
                                            Field affinityField = taskRecordCls.getDeclaredField("affinity");
                                            affinityField.setAccessible(true);
                                            pkg = (String)affinityField.get(taskRecordObject);
                                        }
                                    }
                                    boolean isForceClean = recentPrefHMs.containsKey(pkg + "/forceclean")&&(Boolean) recentPrefHMs.get(pkg + "/forceclean");

                                    if(notStopPkgs.contains(pkg)||(!isForceClean&&"com.tencent.mm".equals(pkg)&&!"com.tencent.mm.plugin.appbrand.ui.AppBrandUI".equals(cls))){
                                        methodHookParam.args[1]=false;
                                    }
                                } else {
                                    XposedBridge.log("CONTROL_taskRecordObject removeTaskByIdLocked 1 对象获取失败");
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            XposedBridge.log("CONTROL_removeTaskByIdLocked error1 "+e.getMessage());
                        }
                    }
                };
                XposedUtil.hookMethod(activityStackSupervisorCls,XposedUtil.getMaxLenParmsByName(activityStackSupervisorCls,"removeTaskByIdLocked"),"removeTaskByIdLocked",hook1);
            }
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

                        final Method anyTaskForIdLockedMethod = activityStackSupervisorCls.getDeclaredMethod("anyTaskForIdLocked",int.class);
                        anyTaskForIdLockedMethod.setAccessible(true);
                        Object taskRecordObject = anyTaskForIdLockedMethod.invoke(mStackSupervisorObject, methodHookParam.args[0]);

                        if (taskRecordObject != null) {
                            Field mAffiliatedTaskIdField = taskRecordCls.getDeclaredField("mAffiliatedTaskId");
                            Field intentField = taskRecordCls.getDeclaredField("intent");
                            mAffiliatedTaskIdField.setAccessible(true);
                            intentField.setAccessible(true);
                            final Object intentObject = intentField.get(taskRecordObject);
                            String pkg = null;
                            if (intentObject != null) {
                                pkg = ((Intent) intentObject).getComponent().getPackageName();
                                if(pkg == null){
                                    Field affinityField = taskRecordCls.getDeclaredField("affinity");
                                    affinityField.setAccessible(true);
                                    pkg = (String)affinityField.get(taskRecordObject);
                                }
                            }
                            final Object ams = methodHookParam.thisObject;
                            Field sysCxtField = amsCls.getDeclaredField("mContext");
                            if (sysCxtField != null) {
                                sysCxtField.setAccessible(true);
                                final Context sysCxt = (Context) sysCxtField.get(ams);//(Context)methodHookParam.args[0];
                                if (sysCxt != null) {
                                    boolean isLockApp = appStartPrefHMs.containsKey(pkg+"/lockapp")?(boolean)appStartPrefHMs.get(pkg+"/lockapp"):false;
                                    if (pkg != null && isLockApp) {
                                        autoStartPrefs.reload();
                                        if (!autoStartPrefs.getBoolean(pkg + "/lockok", false)) {
                                            Intent intent = new Intent(Intent.ACTION_MAIN);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
                                            intent.addCategory(Intent.CATEGORY_HOME);
                                            sysCxt.startActivity(intent);
                                            final String pkgTemp = pkg;
                                            final Intent broad = new Intent("com.click369.control.lockapp");
                                            broad.putExtra("pkg", pkgTemp);
//                                            broad.putExtra("isfromresume", true);
                                            broad.putExtra("intent", intentObject == null ? null : (Intent) intentObject);
//                                            handler.postDelayed(new Runnable() {
//                                                @Override
//                                                public void run() {
                                                    sysCxt.startActivity(broad);
//                                                }
//                                            },300);
                                            methodHookParam.setResult(0);
                                            return;
                                        }
                                    }
                                    //给启动判断时发送广播
                                    Intent broad1 = new Intent("com.click369.control.test");
                                    broad1.putExtra("pkg", pkg);
                                    broad1.putExtra("from", lpparam.packageName);
                                    broad1.putExtra("class", pkg == null ? null : ((Intent) intentObject).getComponent().getClassName().toString());
                                    broad1.putExtra("action", "");
                                    sysCxt.sendBroadcast(broad1);
                                }
                            }
                        } else {
                            XposedBridge.log("taskRecordObject FromRecents 对象获取失败 ^^^^^^^^^^^^^^^^^");
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                }
            };
            XposedUtil.hookMethod(amsCls,clss,"startActivityFromRecents",hook);
        }else if(isAppstart){
            XposedBridge.log("^^^^^^^^^^^^^^startActivityFromRecents  函数未找到 ^^^^^^^^^^^^^^^^^");
        }



//            try {
//                final Class brCls = XposedHelpers.findClass(" com.android.server.am.BroadcastRecord", lpparam.classLoader);
//                Constructor css[] = brCls.getDeclaredConstructors();
//                if(css!=null){
//                    Class clss[] = null;
//                    for(Constructor con:css){
//                        if(con.getParameterTypes().length>10){
//                            clss = con.getParameterTypes();
//                            break;
//                        }
//                    }
//                    XC_MethodHook hook1 = new XC_MethodHook() {
//                        @Override
//                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        try {
//                            if((isOneOpen||isTwoOpen)){
//                            int index = methodHookParam.args[10] instanceof List ?10:methodHookParam.args[11] instanceof List?11:-1;
//                            if(index!=-1){
//                                List receivers = (List)methodHookParam.args[index];
//                                if(receivers!=null&&receivers.size()>0){
////                                    XposedBridge.log("CONTROL -----BroadcastRecord "+receivers.get(0).getClass().getName());
//                                    Set removes = new HashSet();
//                                    for(Object o:receivers){
//                                        if (o.getClass().getName().contains("BroadcastFilter")){
//                                            Field nameFiled= o.getClass().getDeclaredField("packageName");
//                                            nameFiled.setAccessible(true);
//                                            String name = (String)nameFiled.get(o);
//                                            if ((isTwoOpen&&muBeiHSs.contains(name))||
//                                                    (isOneOpen&&controlHMs.containsKey(name+"/broad")&&controlHMs.get(name+"/broad")==(Boolean)true)){
////                                                    if ((isOneOpen&&controlHMs.containsKey(name+"/broad"))){
//                                                removes.add(o);
//                                            }
//                                        }else if(o instanceof ResolveInfo){
//                                            ActivityInfo info = ((ResolveInfo)o).activityInfo;
//                                            String name = info!=null?info.packageName:"";
//                                            if ((isTwoOpen&&muBeiHSs.contains(name))||
//                                                    (isOneOpen&&controlHMs.containsKey(name+"/broad")&&controlHMs.get(name+"/broad")==(Boolean)true)){
//                                                removes.add(o);
//                                            }
//                                        }
//                                    }
//                                    receivers.removeAll(removes);
//                                }
//                            }
//                            }
//                        }catch (Throwable e){
//                            e.printStackTrace();
//                        }
//                        }
//                    };
//                    if (clss!=null){
//                        //让7.0及以下生效  8.0强制杀死后不需要
//                        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.N ){
//                            XposedUtil.hookConstructorMethod(brCls,clss,hook1);
//                        }
//                    }else{
//                        XposedBridge.log("CONTROL BroadcastRecord 未找到0");
//                    }
//                }else{
//                    XposedBridge.log("CONTROL BroadcastRecord 未找到1");
//                }
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }

        try{
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final Class ussCls = ussClsTemp;
                if(ussCls!=null){
                    Class idleFilterParmsTemp[] = XposedUtil.getParmsByName(ussCls,"isAppIdleFilteredOrParoled");
                    Method getidlemethodTemp = XposedUtil.getMethodByName(ussCls,"isAppIdleFilteredOrParoled");
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
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                            try {
                                Context context = null;
                                if(methodHookParam.args[0] instanceof Context){
                                    context = (Context)methodHookParam.args[0];
                                }else{
                                    Field cxtFiled = methodHookParam.thisObject.getClass().getDeclaredField("mContext");
                                    cxtFiled.setAccessible(true);
                                    context = (Context) cxtFiled.get(methodHookParam.thisObject);
                                }
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
                                            } catch (Throwable e) {
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
                                            } catch (Throwable e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                };
                                try {
                                    IntentFilter filter = new IntentFilter();
                                    filter.addAction("com.click369.control.uss.setappidle");
                                    filter.addAction("com.click369.control.uss.getappidlestate");
                                    context.registerReceiver(br, filter);
                                }catch (Throwable e){

                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    };
                    if(android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1){
                        //Injector
                        Class injectorCls = XposedUtil.findClass("com.android.server.usage.AppStandbyController$Injector", lpparam.classLoader);
                        XposedHelpers.findAndHookConstructor(ussCls,injectorCls, hook);
                    }else{
                        XposedHelpers.findAndHookConstructor(ussCls, Context.class, hook);
                    }
                }
            }
        }catch (Throwable e) {
            e.printStackTrace();
            XposedBridge.log("CONTROL -----未找到UsageStatsService ClassNotFoundError "+e);
        }
        try {
            if(pwmServiceCls!=null) {
                XposedUtil.hookMethod(pwmServiceCls, XposedUtil.getParmsByName(pwmServiceCls, "checkAddPermission"), "checkAddPermission", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            WindowManager.LayoutParams attrs = (WindowManager.LayoutParams) param.args[0];
                            CharSequence title = attrs.getTitle();
                            if (isNeedFloadOnSys && Common.PACKAGENAME.equals(attrs.packageName) && ("控制器".equals(title))) {
                                param.setResult(0);
                                return;
                            }
                        } catch (Throwable e) {
                            isFloatOk = false;
                            e.printStackTrace();
                        }
                    }
                });
                isFloatOk = true;
            }
        }catch (Throwable e) {
            e.printStackTrace();
            isFloatOk = false;
            XposedBridge.log("CONTROL -----未找到PhoneWindowManager "+e);
        }
        try {
            if(nmsCls!=null){
                XC_MethodHook hook1 = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                String apppkg = (String)methodHookParam.args[0];
                                if(!methodHookParam.args[0].equals(methodHookParam.args[1])){
                                    if("android".equals(methodHookParam.args[1])||
                                            "com.android.systemui".equals(methodHookParam.args[1])){
                                        apppkg = (String)methodHookParam.args[0];
                                    }else{
                                        apppkg = (String)methodHookParam.args[1];
                                    }
                                }
                                Notification not = (Notification)methodHookParam.args[6];
                                //排除系统推送通知
                                if (not!=null&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
                                    String gp = not.getGroup();
                                    if(gp!=null&&!apppkg.equals(gp)){
                                        apppkg = gp;
                                    }
                                }
                                CharSequence title = (CharSequence) not.extras.get(Notification.EXTRA_TITLE);
                                CharSequence text = (CharSequence) not.extras.get(Notification.EXTRA_TEXT);
//                                ApplicationInfo info = null;
//                                try {
//                                    info = (ApplicationInfo) not.extras.get("android.appInfo");
//                                }catch (Throwable e){}
//                                XposedBridge.log("notifyPosted test "+not+"  "+title+"  "+text+"  "+autoStartAppNameMaps.containsKey(title.toString().trim()));
                                if(isAppstart&&isAutoStartNotNotify){
                                    boolean isAutoHM = appStartPrefHMs.containsKey(apppkg+"/autostart")?(Boolean)(appStartPrefHMs.get(apppkg+"/autostart")):false;
//                                    boolean isAutoHM1 = info!=null?(appStartPrefHMs.containsKey(info.packageName+"/autostart")?(Boolean)(appStartPrefHMs.get(info.packageName+"/autostart")):false):false;
//                                    if((!startRuningPkgs.contains(apppkg)||!startRuningPkgs.contains(autoStartAppNameMaps.get(title.toString())))&&(isAutoHM||isAutoHM1||autoStartAppNameMaps.containsKey(title.toString().trim()))){
//                                        methodHookParam.setResult(null);
//                                        return;
//                                    }
                                    String titleStr = title.toString().trim();
                                    if(autoStartAppNameMaps.containsKey(titleStr)&&!startRuningPkgs.contains(autoStartAppNameMaps.get(titleStr))){
                                        methodHookParam.setResult(null);
                                        return;
                                    }else if(!startRuningPkgs.contains(apppkg)&&isAutoHM){
                                        methodHookParam.setResult(null);
                                        return;
                                    }
                                }

                                if (isSkipAdOpen&&!Common.PACKAGENAME.equals(apppkg)) {
                                    if (notifySkipKeyWords.size()>0){
//                                        XposedBridge.log("notifyPosted title "+title+" text "+text);
                                        if (title != null && title.toString().contains("应用控制器") && !Common.PACKAGENAME.equals(apppkg)) {//title.toString().contains("可能有害")
                                            methodHookParam.setResult(null);
                                            return;
                                        } else if (text != null && text.toString().contains("应用控制器") && !Common.PACKAGENAME.equals(apppkg)) {
                                            methodHookParam.setResult(null);
                                            return;
                                        }
                                        Method cxtField = sysCls.getDeclaredMethod("getContext");
                                        cxtField.setAccessible(true);
                                        Context cxtObject = (Context)cxtField.invoke(methodHookParam.thisObject);
                                        String appName = apppkg;
                                        try {
                                            PackageManager pm = cxtObject.getPackageManager();
                                            PackageInfo packageInfo = pm.getPackageInfo(apppkg,PackageManager.GET_GIDS);
                                            if(packageInfo.applicationInfo!=null&&packageInfo.applicationInfo.loadLabel(pm)!=null){
                                                appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                                            }
                                        }catch (Exception e){
                                            appName = apppkg;
                                        }
                                        for(String s:notifySkipKeyWords){
                                            if (title != null && title.toString().contains(s)) {
                                                methodHookParam.setResult(null);
                                                return;
                                            } else if (text != null && text.toString().contains(s)) {
                                                methodHookParam.setResult(null);
                                                return;
                                            }else if (apppkg != null && apppkg.toString().equals(s)) {
                                                methodHookParam.setResult(null);
                                                return;
                                            }else if (appName != null && appName.equals(s)) {
                                                methodHookParam.setResult(null);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyPosted error "+e+"^^^^^^^^^^^^^^^^^");
                        }
                    }
                };
                XposedUtil.hookMethod(nmsCls, XposedUtil.getParmsByName(nmsCls,"enqueueNotificationInternal"), "enqueueNotificationInternal",hook1);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            try {
                final Class apperrorsCls = XposedUtil.findClass("com.android.server.am.AppErrors", lpparam.classLoader);
                if(apperrorsCls!=null){
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                Object proc = methodHookParam.args[0];
                                Field infoField = proc.getClass().getDeclaredField("info");
                                infoField.setAccessible(true);
                                ApplicationInfo info = (ApplicationInfo)infoField.get(proc);
                                XposedBridge.log("CONTROL_ANR_" + info.packageName );
                                Method method = proc.getClass().getDeclaredMethod("kill",String.class,boolean.class);
                                method.setAccessible(true);
                                method.invoke(proc,"stop "+info.packageName,false);
                                methodHookParam.setResult(null);
                                return;
                            } catch (RuntimeException e) {
                                XposedBridge.log("^^^^^^^^^^^^^^appNotResponding出错 " + e + "^^^^^^^^^^^^^^^^^");
                            }
                        }
                    };
                    XposedUtil.hookMethod(apperrorsCls,XposedUtil.getParmsByName(apperrorsCls,"appNotResponding"),"appNotResponding",hook);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
        //安卓6 需要下面权限
//        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            if(amsMethods.containsKey("checkCallingPermission")){
                XC_MethodHook  hook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        if ("android.permission.FORCE_STOP_PACKAGES".equals(methodHookParam.args[0])) {
                            try {
                                methodHookParam.setResult(PackageManager.PERMISSION_GRANTED);
                                return;
                            } catch (Throwable e) {
                                XposedBridge.log("CONTROL_hook AMS getpermission err " + e);
                            }
                        }
                    }
                };
                try {
                    XposedUtil.hookMethod(amsCls,amsMethods.get("checkCallingPermission").getParameterTypes(),"checkCallingPermission",hook);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }else{
                XposedBridge.log("CONTROL_checkCallingPermission  函数未找到");
            }

            if(amsMethods.containsKey("isGetTasksAllowed")){
                XC_MethodHook  hook = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            if(MUID!=0&&methodHookParam.args.length>2&&MUID==(Integer) methodHookParam.args[2]){
                                methodHookParam.setResult(true);
                                return;
                            }
                        } catch (Throwable e) {
                            XposedBridge.log("CONTROL_hook AM isGetTasksAllowed err "+e);
                        }
                    }
                };
                try {
                    XposedUtil.hookMethod(amsCls,amsMethods.get("isGetTasksAllowed").getParameterTypes(),"isGetTasksAllowed",hook);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }else{
                XposedBridge.log("CONTROL_isGetTasksAllowed  函数未找到");
            }
//        }

        /**
         *setFirewallEnabled
         *setFirewallChainEnabled
         *setFirewallChainState
         *getFirewallChainState
         *setFirewallUidRuleLocked
         */

        if(netServiceCls!=null) {
            try {
                final int FIREWALL_CHAIN_NONE = 0;
                final int FIREWALL_CHAIN_DOZABLE = 1;
                final int FIREWALL_CHAIN_STANDBY = 2;
                final int FIREWALL_CHAIN_POWERSAVE = 3;

                final String FIREWALL_CHAIN_NAME_NONE = "none";
                final String FIREWALL_CHAIN_NAME_DOZABLE = "dozable";
                final String FIREWALL_CHAIN_NAME_STANDBY = "standby";
                final String FIREWALL_CHAIN_NAME_POWERSAVE = "powersave";

                final int FIREWALL_RULE_DEFAULT = 0;
                final int FIREWALL_RULE_ALLOW = 1;
                final int FIREWALL_RULE_DENY = 2;
                XposedUtil.hookConstructorMethod(netServiceCls, new Class[]{Context.class, String.class}, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            final Object netObj = param.thisObject;
                            Context context = (Context)param.args[0];
                            final Method setFireWallMethod = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M?netServiceCls.getDeclaredMethod("setFirewallUidRule",int.class,int.class,int.class):netServiceCls.getDeclaredMethod("setFirewallUidRule",int.class,boolean.class);
                            setFireWallMethod.setAccessible(true);
                            final Method setFirewallEnabledMethod = netServiceCls.getDeclaredMethod("setFirewallEnabled",boolean.class);
                            setFirewallEnabledMethod.setAccessible(true);
                            final Method isFirewallEnabledMethod = netServiceCls.getDeclaredMethod("isFirewallEnabled");
                            isFirewallEnabledMethod.setAccessible(true);
                            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    try {
                                        if (!isPriOpen){
                                            return;
                                        }
                                        String action = intent.getAction();
                                        if("com.click369.control.ams.net.add".equals(action)){
                                            int uid = intent.getIntExtra("uid",-1);
                                            String type = intent.getStringExtra("type");
                                            int netType = getNetworkType(context);
                                            boolean isAdd = false;
                                            if("wifi".equals(type)){
                                                netWifiList.add(uid);
                                                isAdd = netType==2;
                                            }else if("mobile".equals(type)){
                                                netMobileList.add(uid);
                                                isAdd = netType==1;
                                            }
                                            if(isAdd){
                                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                    setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_DENY);
                                                }else{
                                                    setFireWallMethod.invoke(netObj,uid,false);
                                                }
                                            }
                                        }else if("com.click369.control.ams.net.remove".equals(action)){
                                            int uid = intent.getIntExtra("uid",-1);
                                            String type = intent.getStringExtra("type");
                                            int netType = getNetworkType(context);
                                            boolean isRemove = false;
                                            if("wifi".equals(type)){
                                                netWifiList.remove(uid);
                                                isRemove = netType==2;
                                            }else if("mobile".equals(type)){
                                                netMobileList.remove(uid);
                                                isRemove = netType==1;
                                            }
                                            if(isRemove){
                                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                    setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_ALLOW);
                                                }else{
                                                    setFireWallMethod.invoke(netObj,uid,true);
                                                }
                                            }
                                        }else if("com.click369.control.ams.net.set".equals(action)){
                                            boolean isEnable = intent.getBooleanExtra("isenable",false);
                                            setFirewallEnabledMethod.invoke(netObj,isEnable);
//                                        XposedBridge.log("CONTROL_NET_SET:"+isEnable);
                                        }else if("com.click369.control.ams.net.get".equals(action)){
                                            boolean isEnable = (Boolean) isFirewallEnabledMethod.invoke(netObj);
                                            XposedBridge.log("CONTROL_NET_ISENABLE:"+isEnable);
                                        }else if("com.click369.control.ams.net.init".equals(action)){
                                            HashSet<Integer> sets = new HashSet<Integer>();
                                            sets.addAll(netWifiList);
                                            sets.addAll(netMobileList);
                                            for(int uid:sets){
                                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                    setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_ALLOW);
                                                }else{
                                                    setFireWallMethod.invoke(netObj,uid,true);
                                                }
                                            }
                                            if(intent.hasExtra("wifilist")&&intent.hasExtra("mobilelist")){
                                                HashSet<Integer> netWifiListTemp = (HashSet<Integer>)intent.getSerializableExtra("wifilist");
                                                HashSet<Integer> netMobileListTemp = (HashSet<Integer>)intent.getSerializableExtra("mobilelist");
                                                netWifiList.clear();
                                                netMobileList.clear();
                                                netWifiList.addAll(netWifiListTemp);
                                                netMobileList.addAll(netMobileListTemp);
                                                XposedBridge.log("CONTROL_NETCONTROL_"+netWifiList.size()+" "+netMobileList.size());
                                            }
                                            int type = getNetworkType(context);
                                            if(type==1){
                                                for(int uid:netMobileList){
                                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                        setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_DENY);
                                                    }else{
                                                        setFireWallMethod.invoke(netObj,uid,false);
                                                    }
                                                }
                                            }else if(type ==2){
                                                for(int uid:netWifiList){
                                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                        setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_DENY);
                                                    }else{
                                                        setFireWallMethod.invoke(netObj,uid,false);
                                                    }
                                                }
                                            }
                                        }// 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
                                        else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                                            //获取联网状态的NetworkInfo对象
                                            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                                            if (info != null) {
                                                //如果当前的网络连接成功并且网络连接可用
                                                if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {//链接
                                                    if (info.getType() == ConnectivityManager.TYPE_WIFI ){
                                                        for(int uid:netWifiList){
                                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                                setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_DENY);
                                                            }else{
                                                                setFireWallMethod.invoke(netObj,uid,false);
                                                            }
                                                        }
                                                    }else if(info.getType() == ConnectivityManager.TYPE_MOBILE) {
                                                        for(int uid:netMobileList){
                                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                                setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_DENY);
                                                            }else{
                                                                setFireWallMethod.invoke(netObj,uid,false);
                                                            }
                                                        }
                                                    }
                                                } else {//断开
                                                    if (info.getType() == ConnectivityManager.TYPE_WIFI ){
                                                        for(int uid:netWifiList){
                                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                                setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_ALLOW);
                                                            }else{
                                                                setFireWallMethod.invoke(netObj,uid,true);
                                                            }
                                                        }
                                                    }else if(info.getType() == ConnectivityManager.TYPE_MOBILE) {
                                                        for(int uid:netMobileList){
                                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                                                setFireWallMethod.invoke(netObj,FIREWALL_CHAIN_NONE,uid,FIREWALL_RULE_ALLOW);
                                                            }else{
                                                                setFireWallMethod.invoke(netObj,uid,true);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            };
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.click369.control.ams.net.add");
                            intentFilter.addAction("com.click369.control.ams.net.remove");
                            intentFilter.addAction("com.click369.control.ams.net.init");
                            intentFilter.addAction("com.click369.control.ams.net.set");
                            intentFilter.addAction("com.click369.control.ams.net.get");
//                        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//                        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
                            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                            context.registerReceiver(broadcastReceiver,intentFilter);
//                        XposedBridge.log("CONTROL_NET_REG:"+isFirewallEnabledMethod.invoke(netObj));
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    public static int getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getApplicationContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return -1;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        if (networkinfo == null || !networkinfo.isAvailable()) {
            return -1;
        }
        if(networkinfo.getType() == ConnectivityManager.TYPE_MOBILE){
            return  1;
        }else if(networkinfo.getType() == ConnectivityManager.TYPE_WIFI){
            return  2;
        }
        return -1;
    }



//    private static boolean isProcessHasACTORSER(Object proc){
//        try {
//            Field activitiesField = proc.getClass().getDeclaredField("activities");
//            activitiesField.setAccessible(true);
//            Field servicesField = proc.getClass().getDeclaredField("services");
//            servicesField.setAccessible(true);
//            Field executingServicesField = proc.getClass().getDeclaredField("executingServices");
//            executingServicesField.setAccessible(true);
//            ArrayList activities = (ArrayList)activitiesField.get(proc);
//            ArraySet services = (ArraySet)servicesField.get(proc);
//            ArraySet executingServices = (ArraySet)executingServicesField.get(proc);
//            return activities.size()>0||services.size()>0||executingServices.size()>0;
//        }catch (Exception e){
//            e.printStackTrace();
//            return true;
//        }
//    }
}
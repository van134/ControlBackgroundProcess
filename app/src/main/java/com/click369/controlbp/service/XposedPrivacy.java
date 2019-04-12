package com.click369.controlbp.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoLte;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.DirBean;
import com.click369.controlbp.common.Common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
public class XposedPrivacy {
    private static String IMEI = (long)(Math.random()*10000000000L)+""+(long)(Math.random()*100000L);
    private static String IMSI = (long)(Math.random()*10000000000L)+""+(long)(Math.random()*100000L);
    private static String newDir = "zcache";
    private static String defaultDir = null;
    private static Context mContext;
    private static HashMap<Long,String> infos = new HashMap<Long,String>();
    private static long time = 0;
    private static int count = 0;
    private static String pkg = "";
    private static LocationListener ll;
//    private static double lat=39.916803,lon = 116.403766;//lat纬度  lon//经度
    private static double lat=0,lon = 0;//lat纬度  lon//经度
    private static Handler handler ;//不能再这里初始化  部分系统会出现问题
    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                HashMap<Long,String> minfos = new HashMap<Long,String>();
                minfos.putAll(infos);
                Intent intent = new Intent("com.click369.control.ams.sendprivacyinfo");
                intent.putExtra("pkg",pkg);
                intent.putExtra("infos",minfos);
                if(mContext!=null){
                    mContext.sendBroadcast(intent);
                    infos.clear();
                    count = 0;
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    };
    private static String lastInfo = "";
    private static long lastTime = 0;
    private static boolean isSelfGetTime = false;
    private static void sendBroad(String p,String info,boolean isSend){
        try {
            pkg = p;
            isSelfGetTime = true;
            if(info!=null&&!(info.equals(lastInfo)&& System.currentTimeMillis()-lastTime<800)){
                infos.put(System.currentTimeMillis(),info);
                lastInfo = info;
                lastTime = System.currentTimeMillis();
                count++;
            }
            isSelfGetTime = false;
            if(handler!=null&&count>0){
                if(count>40){
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable,10);
                }else{
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable,2000);
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private static boolean isContainsNotNewDirPkg(String pkg){
        return pkg.equals("com.android.storagemanager")||pkg.equals("com.android.externalstorage")||pkg.equals("com.sec.android.app.myfiles")||pkg.contains("bin.mt")||pkg.contains("fileexplorer")||pkg.equals("com.speedsoftware.rootexplorer")||pkg.equals("com.estrongs.android.pop");
    }
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences privacyPrefs){
        privacyPrefs.reload();
        if(!isContainsNotNewDirPkg(lpparam.packageName)&&privacyPrefs.getBoolean(Common.PREFS_PRIVATE_NEWDIR_ALLSWITCH,false)){
            defaultDir = privacyPrefs.getString( "defaultDir", null);
            final HashSet<String> sets = (HashSet<String>)privacyPrefs.getStringSet( Common.PREFS_PRIVATE_NEWDIR_KEYWORDS, new HashSet<String>());
            if(defaultDir!=null&&sets!=null&&sets.size()>0){
                final HashMap<String,String> newDirs = new HashMap<>();
                for(String s:sets){
                    String ss[] = s.split("\\|");
                    newDirs.put(ss[0],ss[1]);
                }
                final List<String> keys = new ArrayList<String>(newDirs.keySet());
                Collections.sort(keys, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        int flags = 0;
                        if (o1.length()<o2.length()) {
                            flags = 1;
                        }else if(o1.length()>o2.length()) {
                            flags = -1;
                        }else {
                            flags = 0;
                        }
                        return flags;
                    }
                });
                XC_MethodHook hookFileDirs = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        try {
                            Object obj = param.args[0];
                            boolean isNotFileCls = !param.thisObject.getClass().getName().endsWith(".File");
                            if(obj instanceof String){
                                String s1 = (String)obj;
                                String s2 = param.args.length==1||isNotFileCls?"":File.separator+(String)param.args[1];
                                String s = s1+s2;
                                if(s.startsWith("/storage")&&!s.equals(defaultDir)){
                                    String key = null;
                                    for(String set:keys){
                                        if(s.startsWith(defaultDir+File.separator+set)){
                                            key = set;
                                            break;
                                        }
                                    }
                                    if(key!=null){
                                        if(param.args.length == 1){
                                            param.args[0] = s.replace(defaultDir+File.separator+key,defaultDir+File.separator+newDirs.get(key));
                                        }else if(param.args.length == 2&&!isNotFileCls){
                                            if(s1.contains(defaultDir+File.separator+key)){
                                                param.args[0] = s1.replace(defaultDir+File.separator+key,defaultDir+File.separator+newDirs.get(key));
                                            }else if(s2.contains(key)){
                                                param.args[1] = s2.replace(key,newDirs.get(key));
                                            }
                                        }
                                    }
                                }
                            }else if(obj instanceof File){
                                File f = (File)obj;
                                String s1 = f.getAbsolutePath();
                                String s2 = param.args.length==1?"":File.separator+(String)param.args[1];
                                String s = s1+s2;
                                if(s.startsWith("/storage")&&!s.equals(defaultDir)) {
                                    String key = null;
                                    for(String set:keys){
                                        if(s.startsWith(defaultDir+File.separator+set)){
                                            key = set;
                                            break;
                                        }
                                    }
                                    if(key!=null){
                                        if(param.args.length == 1){
                                            param.args[0] = new File(s.replace(defaultDir+File.separator+key,defaultDir+File.separator+newDirs.get(key)));
                                        }else if(param.args.length == 2&&!isNotFileCls){
                                            if(s1.contains(defaultDir+File.separator+key)){
                                                param.args[0] = new File(s1.replace(defaultDir+File.separator+key,defaultDir+File.separator+newDirs.get(key)));
                                            }else if(s2.contains(key)){
                                                param.args[1] = s2.replace(key,newDirs.get(key));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                XposedUtil.hookConstructorMethod(File.class,new Class[]{File.class,String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(File.class,new Class[]{String.class,String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(File.class,new Class[]{String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(RandomAccessFile.class,new Class[]{String.class,String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(FileInputStream.class,new Class[]{String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(FileOutputStream.class,new Class[]{String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(FileOutputStream.class,new Class[]{String.class,boolean.class},hookFileDirs);
            }
        }


        if(privacyPrefs.getBoolean(lpparam.packageName+"/priwifi",false)||
                privacyPrefs.getBoolean(lpparam.packageName+"/primobile",false)){
            final Class netinfoCls = XposedUtil.findClass("android.net.NetworkInfo", lpparam.classLoader);
            XC_MethodHook hooknetinfo = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        privacyPrefs.reload();
                        boolean isPreventWifi = privacyPrefs.getBoolean(lpparam.packageName+"/priwifi",false);
                        boolean isPreventMobile = privacyPrefs.getBoolean(lpparam.packageName+"/primobile",false);
                        if(isPreventWifi||isPreventMobile){
                            if(param.method.getName().equals("isAvailable")||
                                    param.method.getName().equals("isConnected")){
                                NetworkInfo networkInfo = (NetworkInfo)param.thisObject;
                                if(networkInfo.getType()==ConnectivityManager.TYPE_WIFI&&isPreventWifi){
                                    param.setResult(false);
                                }else if(networkInfo.getType()==ConnectivityManager.TYPE_MOBILE&&isPreventMobile){
                                    param.setResult(false);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(netinfoCls, XposedUtil.getParmsByName(netinfoCls, "isAvailable"), "isAvailable",hooknetinfo);
            XposedUtil.hookMethod(netinfoCls, XposedUtil.getParmsByName(netinfoCls, "isConnected"), "isConnected",hooknetinfo);
        }

        if(privacyPrefs.getBoolean(lpparam.packageName+"/priswitch",false)){
            final boolean isDIDISIJI = "com.sdu.didi.gsui".equals(lpparam.packageName);//排除didi司机端
            final Set<String> switchs = privacyPrefs.getStringSet(lpparam.packageName+"/prilist",new HashSet<String>());
            final boolean isChangeLoc = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_CHANGELOC]);
            final boolean isPreventRunList = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_RUNLIST])||(isChangeLoc&&!isDIDISIJI);
            final boolean isPreventAppList = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_APPLIST]);//||(isChangeLoc&&!isDIDISIJI);
            final boolean isPreventGps = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_GPSINFO])||isChangeLoc;
            final boolean isPreventBaseStation = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_BASESTATION])||isChangeLoc;
            final boolean isPreventWifi = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_WIFIINFO])||isChangeLoc;
            final boolean isPreventDevInfo = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_DEVICEINFO]);//||(isChangeLoc&&!isDIDISIJI);
            final boolean isPreventDevIMEIInfo = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_DEVICEIMEIINFO]);//||(isChangeLoc&&!isDIDISIJI);
            final boolean isPreventDevIMSIInfo = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_DEVICEIMSIINFO]);//||(isChangeLoc&&!isDIDISIJI);
            final boolean isPreventTime = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_CHANGETIME]);
            final boolean isPreventFileDir = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_REDIRFIEDIR]);
            final boolean isPreventNetTypeWifi = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_NETTYPE_WIFI]);
            final boolean isPreventNetType4G = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_NETTYPE_4G]);
            if(privacyPrefs.contains(lpparam.packageName + "/changetime")){
                time = privacyPrefs.getLong(lpparam.packageName + "/changetime", 0);
            }
            if(privacyPrefs.contains(lpparam.packageName + "/newdir")){
                newDir = privacyPrefs.getString(lpparam.packageName + "/newdir", "zcache");
                if(newDir==null||newDir.trim().length()==0){
                    newDir = "zcache";
                }
            }
            if(isPreventFileDir){
                defaultDir = privacyPrefs.getString( "defaultDir", null);
            }
            if(isPreventDevIMEIInfo){
                String IMEITemp = privacyPrefs.getString(lpparam.packageName + "/imei", "");
                if(IMEITemp.length()==15){
                    IMEI = IMEITemp;
                }
            }
            if(isPreventDevIMSIInfo){
                String IMSITemp = privacyPrefs.getString(lpparam.packageName + "/imsi", "");
                if(IMSITemp.length()==15){
                    IMSI = IMSITemp;
                }
            }

            if(isChangeLoc){
                try {
                    String lonStr = privacyPrefs.getString(lpparam.packageName+"/lon","0");
                    String latStr = privacyPrefs.getString(lpparam.packageName+"/lat","0");
                    boolean isrechange = privacyPrefs.getBoolean(lpparam.packageName+"/isrechange",false);
                    lon = Double.parseDouble(lonStr);
                    lat = Double.parseDouble(latStr);
//                    XposedBridge.log("CONTROL_PRIVACY_lon:"+lon+" lat "+lat);
                    if(isrechange&&lat!=0){//GPS纠偏
                        lat = lat+0.002655;
                        lon = lon-0.004475;
                    }
                }catch (Exception e){
                    XposedBridge.log("CONTROL_PRIVACY_"+e);
//                    lat=39.916803;lon = 116.403766;
                    lat=0;lon = 0;
                }

            }

            final Class appCls = XposedUtil.findClass("android.app.Application", lpparam.classLoader);
            XposedUtil.hookMethod(appCls, XposedUtil.getParmsByName(appCls, "onCreate"), "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(param.thisObject!=null){
                            mContext = ((Application)(param.thisObject)).getApplicationContext();
                        }
                        if(mContext==null){
                            handler = new Handler();
                        }else{
                            handler = new Handler(mContext.getMainLooper());
                        }
                    }catch (Exception e){

                    }
                }
            });
            final Class actCls = XposedUtil.findClass("android.app.Activity", lpparam.classLoader);
            XposedUtil.hookMethod(actCls, XposedUtil.getParmsByName(actCls, "onCreate"), "onCreate", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(mContext==null&&param.thisObject!=null){
                            mContext = ((Activity)(param.thisObject)).getApplicationContext();
                        }
                        if(handler==null){
                            if(mContext==null){
                                handler = new Handler();
                            }else{
                                handler = new Handler(mContext.getMainLooper());
                            }
                        }
                    }catch (Exception e){

                    }
                }
            });

            final Class amCls = XposedUtil.findClass("android.app.ActivityManager", lpparam.classLoader);
            XC_MethodHook hookams = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(isPreventRunList){
                            List list = (List) param.getResult();
                            if(param.method.getName().equals("getRecentTasks")){
                                List mlist = new ArrayList();
                                for(Object o:list){
                                    ActivityManager.RecentTaskInfo rti = (ActivityManager.RecentTaskInfo)o;
                                    if(rti.baseIntent!=null&&
                                            rti.baseIntent.getComponent()!=null&&
                                            (rti.baseIntent.getComponent().getPackageName().equals(lpparam.packageName)||
                                                    rti.baseIntent.getComponent().getPackageName().startsWith("com.android"))){
                                        mlist.add(o);
                                    }
                                }
                                param.setResult(mlist);
                            }else if(param.method.getName().equals("getRunningAppProcesses")) {
                                List mlist = new ArrayList();
                                for (Object o : list) {
                                    ActivityManager.RunningAppProcessInfo rap = (ActivityManager.RunningAppProcessInfo) o;
                                    if (rap.processName != null &&
                                            (rap.processName.startsWith(lpparam.packageName)||
                                                    rap.processName.startsWith("com.android"))) {
                                        mlist.add(o);
                                    }
                                }
                                param.setResult(mlist);
                            }else if(param.method.getName().equals("getRunningServices")) {
                                List mlist = new ArrayList();
                                for (Object o : list) {
                                    ActivityManager.RunningServiceInfo rap = (ActivityManager.RunningServiceInfo) o;
                                    if (rap.clientPackage != null &&
                                            (rap.clientPackage.equals(lpparam.packageName)||
                                                    rap.clientPackage.startsWith("com.android"))) {
                                        mlist.add(o);
                                    }
                                }
                                param.setResult(mlist);
                            }else if(param.method.getName().equals("getRunningTasks")) {
                                List mlist = new ArrayList();
                                for (Object o : list) {
                                    ActivityManager.RunningTaskInfo rap = (ActivityManager.RunningTaskInfo) o;
                                    if (rap.baseActivity != null &&
                                            (rap.baseActivity.getPackageName().equals(lpparam.packageName)||
                                                    rap.baseActivity.getPackageName().equals("com.android"))) {
                                        mlist.add(o);
                                    }
                                }
                                param.setResult(mlist);
                            }
                        }
//                        infos.put(System.currentTimeMillis(),);
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_RUNLIST]+"|"+isPreventRunList,true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(amCls, XposedUtil.getParmsByName(amCls, "getRecentTasks"), "getRecentTasks",hookams);
            XposedUtil.hookMethod(amCls, XposedUtil.getParmsByName(amCls, "getRunningAppProcesses"), "getRunningAppProcesses",hookams);
            XposedUtil.hookMethod(amCls, XposedUtil.getParmsByName(amCls, "getRunningServices"), "getRunningServices",hookams);
            XposedUtil.hookMethod(amCls, XposedUtil.getParmsByName(amCls, "getRunningTasks"), "getRunningTasks",hookams);


            final Class apmCls = XposedUtil.findClass("android.app.ApplicationPackageManager", lpparam.classLoader);
            XC_MethodHook hookapm = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(isPreventAppList||isChangeLoc){
                            List list = (List) param.getResult();
                            List newList = new ArrayList();
//                            Object temp = null;
//                            String args = "";
//                            if(param.args!=null&&param.args.length>0){
//                                args+= param.args[0]+"  ";
//                                if(param.args.length==2){
//                                    args+= param.args[1]+"  ";
//                                }else if(param.args.length==3){
//                                    args+= param.args[1]+"  "+param.args[2];
//                                }else if(param.args.length==4){
//                                    args+= param.args[1]+"  "+param.args[2]+"  "+param.args[3];
//                                }
//                            }
//                            XposedBridge.log("DD_TEST_"+param.method.getName()+"  args "+args);
                            int type = 0;
                            for(Object o:list){
                                if(o instanceof ApplicationInfo){
                                    type = 1;
                                    ApplicationInfo applicationInfo = (ApplicationInfo)o;
                                    if(applicationInfo!=null&&applicationInfo.packageName!=null){
                                        if(isPreventAppList){
                                            if(applicationInfo.packageName.equals(lpparam.packageName)||applicationInfo.packageName.startsWith("com.android")){
                                                newList.add(applicationInfo);
                                            }
                                        }else{
                                            if(!applicationInfo.packageName.equals(Common.PACKAGENAME)&&!applicationInfo.packageName.toLowerCase().contains("xposed")){
                                                newList.add(applicationInfo);
                                            }
                                        }
                                    }
                                }else if(o instanceof PackageInfo){
                                    type = 2;
                                    PackageInfo packageInfo = (PackageInfo)o;
                                    if(packageInfo!=null&&packageInfo.packageName!=null){
                                        if(isPreventAppList){
                                            if(packageInfo.packageName.equals(lpparam.packageName)||packageInfo.packageName.startsWith("com.android")){
                                                newList.add(packageInfo);
                                            }
                                        }else{
                                            if(!packageInfo.packageName.equals(Common.PACKAGENAME)&&!packageInfo.packageName.toLowerCase().contains("xposed")){
                                                newList.add(packageInfo);
                                            }
                                        }
                                    }
                                }else if(o instanceof ResolveInfo){
                                    type = 3;
                                    ResolveInfo  resolveInfo = (ResolveInfo)o;
                                    if(resolveInfo!=null&&resolveInfo.resolvePackageName!=null){
                                        if(isPreventAppList){
                                            if(resolveInfo.resolvePackageName.equals(lpparam.packageName)||resolveInfo.resolvePackageName.startsWith("com.android")){
                                                newList.add(resolveInfo);
                                            }
                                        }else{
                                            if(!resolveInfo.resolvePackageName.equals(Common.PACKAGENAME)&&!resolveInfo.resolvePackageName.toLowerCase().contains("xposed")){
                                                newList.add(resolveInfo);
                                            }
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
                        }
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_APPLIST]+"|"+isPreventAppList,true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hookapmgetinfo = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(isPreventAppList){
                            if(param.args[0]!=null&&(param.args[0].equals(lpparam.packageName)||
                                    param.args[0].toString().startsWith("com.android"))){
                            }else{
                                param.args[0] = "haha.12345680e9";
                            }
                        }else if(isChangeLoc){
                            String info = param.args[0].toString();
                            if(param.args[0]!=null&&(info.toLowerCase().contains("xposed")||info.equals(Common.PACKAGENAME))){
                                param.args[0] = "haha.12345680e9";
                            }
                        }
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_APPLIST]+"|"+isPreventAppList,true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hookapmbacknull = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(isPreventAppList){
                            param.setResult("");
                        }
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_APPLIST]+"|"+isPreventAppList,true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getInstalledApplications"), "getInstalledApplications",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getInstalledPackages"), "getInstalledPackages",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getPackageInfo"), "getPackageInfo",hookapmgetinfo);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getInstallerPackageName"), "getInstallerPackageName",hookapmbacknull);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getPackagesHoldingPermissions"), "getPackagesHoldingPermissions",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "getPreferredPackages"), "getPreferredPackages",hookapm);
//            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "queryIntentActivities"), "queryIntentActivities",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "queryIntentContentProviders"), "queryIntentContentProviders",hookapm);
            XposedUtil.hookMethod(File.class, new Class[]{},"list", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if(isChangeLoc||isPreventAppList){
                        String ss[] = (String[])param.getResult();
                        if(ss!=null&&ss.length>0){
                            ArrayList<String> newList = new ArrayList<String>();
                            for(int i = 0;i<ss.length;i++){
                                String s = ss[i].toLowerCase();
                                if(!s.contains("xposed")&&!s.contains("click369")&&!s.contains("superuser")&&!s.contains("magisk")){
                                    newList.add(ss[i]);
                                }
                            }
                            String res[] = new String[newList.size()];
                            for(int i = 0;i<newList.size();i++){
                                res[i] = newList.get(i);
                            }
                            param.setResult(res);
                            return;
                        }
                    }
                }
            });


            final Class locCls = XposedUtil.findClass("android.location.Location", lpparam.classLoader);
            final Class locManagerCls = XposedUtil.findClass("android.location.LocationManager", lpparam.classLoader);
            final Class teleCls = XposedUtil.findClass("android.telephony.TelephonyManager", lpparam.classLoader);
            final Class sysSecCls = XposedUtil.findClass("android.provider.Settings$Secure", lpparam.classLoader);
            XC_MethodHook hookloc = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(isPreventGps){
                            Location location = (Location)param.getResult();
                            if(location!=null){
                                location.setTime(System.currentTimeMillis()-1000);
                                location.setLongitude(lon);
                                location.setLatitude(lat);
                                location.setAccuracy(3.0f);
                                location.setAltitude(Math.random()*lon);
                                param.setResult(location);
                            }

                        }
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_GPSINFO]+"|"+isPreventGps,true);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hookgetloc = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(isPreventGps){
                            if(param.method.getName().equals("getLongitude")){
                                param.setResult(lon);
                            }else{
                                param.setResult(lat);
                            }
                        }
//                        infos.put(System.currentTimeMillis(),Common.PRIVACY_KEYS[2]+"|"+isPrevent);
//                        sendBroad(lpparam.packageName);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hookaddlis = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_GPSINFO]+"|"+isPreventGps,true);
                        if(isPreventGps){
                            if(param.method.getName().equals("addGpsStatusListener")){
                                if(param.args[0]!=null&&isChangeLoc){
                                    XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 1);
                                    XposedHelpers.callMethod(param.args[0], "onGpsStatusChanged", 3);
                                }
                            }else{
                                if(param.args[1]!=null&&isChangeLoc){
                                    XposedHelpers.callMethod(param.args[1], "onProviderEnabled", LocationManager.GPS_PROVIDER);
//                                    Location location = new Location(LocationManager.GPS_PROVIDER);
//                                    location.setTime(System.currentTimeMillis()-1000);
//                                    location.setLongitude(lon);
//                                    location.setLatitude(lat);
//                                    location.setAccuracy(3.0f);
//                                    location.setAltitude(Math.random()*lon);
//                                    XposedHelpers.callMethod(param.args[1], "onLocationChanged", location);
                                    ll = (LocationListener)param.args[3];
                                    changeLoc();
                                }
                            }
                            return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hookaddnea = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_GPSINFO]+"|"+isPreventGps,true);
                        if(isPreventGps){
                           param.setResult(isChangeLoc?true:false);
                           return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hookisenable = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if(isPreventGps){
                            if(param.method.getName().equals("getInt")){
                                if(Settings.Secure.LOCATION_MODE.equals(param.args[1])){
                                    changeLoc();
                                    param.setResult(isChangeLoc?Settings.Secure.LOCATION_MODE_HIGH_ACCURACY:Settings.Secure.LOCATION_MODE_OFF);
                                }
                            }else{
                                changeLoc();
                                param.setResult(isChangeLoc?true:false);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hookbest = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_GPSINFO]+"|"+isPreventGps,true);
                        if(isPreventGps){
                            if(isChangeLoc){
                                param.setResult(LocationManager.GPS_PROVIDER);
                            }else{
                                param.setResult(LocationManager.NETWORK_PROVIDER);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            XC_MethodHook hookprovd = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_GPSINFO]+"|"+isPreventGps,true);
                        if(isPreventGps){
                            List list = (List) param.getResult();
                            list.clear();
                            if(isChangeLoc){
                                list.add(LocationManager.GPS_PROVIDER);
                            }
                            param.setResult(list);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            Class clss[] = XposedUtil.getParmsByName(locCls, "createFromParcel");
            if(clss==null){
                try {
                    Field field = locCls.getDeclaredField("CREATOR");
                    field.setAccessible(true);
                    Object CREATOR = field.get(null);
                    Class cls = CREATOR.getClass();
                    clss = XposedUtil.getParmsByName(cls, "createFromParcel");
                    XposedUtil.hookMethod(cls,clss , "createFromParcel",hookloc);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                XposedUtil.hookMethod(locCls,clss , "createFromParcel",hookloc);
            }
            XposedUtil.hookMethod(locCls,XposedUtil.getParmsByName(locCls, "getLatitude") , "getLatitude",hookgetloc);
            XposedUtil.hookMethod(locCls,XposedUtil.getParmsByName(locCls, "getLongitude") , "getLongitude",hookgetloc);

            XposedUtil.hookMethod(locManagerCls, XposedUtil.getParmsByName(locManagerCls, "addGpsStatusListener"), "addGpsStatusListener",hookaddlis);
            XposedUtil.hookMethod(locManagerCls, new Class[]{XposedUtil.findClass("android.location.LocationRequest",lpparam.classLoader), LocationListener.class, Looper.class, PendingIntent.class}, "requestLocationUpdates",hookaddlis);
            XposedUtil.hookMethod(locManagerCls, XposedUtil.getParmsByName(locManagerCls, "isProviderEnabled"), "isProviderEnabled",hookisenable);
            XposedUtil.hookMethod(sysSecCls, new Class[]{ContentResolver.class,String.class}, "getInt",hookisenable);
            XposedUtil.hookMethod(locManagerCls, XposedUtil.getParmsByName(locManagerCls, "addNmeaListener"), "addNmeaListener",hookaddnea);
            XposedUtil.hookMethod(locManagerCls, XposedUtil.getParmsByName(locManagerCls, "getBestProvider"), "getBestProvider",hookbest);
            XposedUtil.hookMethod(locManagerCls, XposedUtil.getParmsByName(locManagerCls, "getLastLocation"), "getLastLocation",hookloc);
            XposedUtil.hookMethod(locManagerCls, XposedUtil.getParmsByName(locManagerCls, "getLastKnownLocation"), "getLastKnownLocation",hookloc);
            XposedUtil.hookMethod(locManagerCls, XposedUtil.getParmsByName(locManagerCls, "getProviders"), "getProviders",hookprovd);


            XC_MethodHook hooktele = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_BASESTATION]+"|"+isPreventBaseStation,true);
                        if(isPreventBaseStation){
                            if(param.method.getName().equals("getCellLocation")){
                                CdmaCellLocation location = new CdmaCellLocation();
                                location.setCellLocationData(11201+((int)lat),(int)(lat*14400),(int)(lon*14400),1000+((int)lat),1000+((int)lon));
                                param.setResult(location);
                            }else if(param.method.getName().equals("getAllCellInfo")||param.method.getName().equals("getNeighboringCellInfo")){
                                List list = (List) param.getResult();
                                list.clear();
                                param.setResult(list);
                            }
//                            else if(param.method.getName().equals("getNeighboringCellInfo")){
//                                List list = (List) param.getResult();
//                                list.clear();
//                                NeighboringCellInfo neighboringCellInfo = new NeighboringCellInfo(0, "0", 0);
//                                list.add(neighboringCellInfo);
//                                param.setResult(list);
//                            }
//                            XposedBridge.log(lpparam.packageName+"  "+param.method.getName()+" ");
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XC_MethodHook hooktelelis = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_BASESTATION]+"|"+isPreventBaseStation,true);
                        if(isPreventBaseStation){
                            if(param.args[0]!=null){
                                CdmaCellLocation location = new CdmaCellLocation();
                                location.setCellLocationData(11201+((int)lat),(int)(lat*14400),(int)(lon*14400),1000+((int)lat),1000+((int)lon));
                                XposedHelpers.callMethod(param.args[0], "onCellLocationChanged", location);
                            }
                           return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            XC_MethodHook hooklte = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_BASESTATION]+"|"+isPreventBaseStation,true);
                        if(isPreventBaseStation){
                            int res = -1;
                            if("getBaseStationLatitude".equals(param.method.getName())){
                                res = (int)(lat*14400);
                            }else if("getBaseStationLongitude".equals(param.method.getName())){
                                res = (int)(lon*14400);
                            }else if("getSystemId".equals(param.method.getName())){
                                res = 1000+((int)lat);
                            }else if("getNetworkId".equals(param.method.getName())){
                                res = 1000+((int)lon);
                            }else if("getBaseStationId".equals(param.method.getName())){
                                res = 11201+((int)lat);
                            }else if("getNetworkType".equals(param.method.getName())){
                                res = TelephonyManager.NETWORK_TYPE_CDMA;
                            }
//                            XposedBridge.log(lpparam.packageName+"  "+param.method.getName()+" "+res);
                            param.setResult(res);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
//            XC_MethodHook reflactHook = new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                    try {
//                        if(isPreventBaseStation){
//                            Field field = (Field) methodHookParam.thisObject;
//                            if("mBaseStationLatitude".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------reflact:mBaseStationLatitude--------");
//                                methodHookParam.setResult((int)(lat*14400));
//                            }else if("mBaseStationLongitude".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------reflact:mBaseStationLongitude--------");
//                                methodHookParam.setResult((int)(lon*14400));
//                            }else if("mSystemId".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------reflact:mSystemId--------");
//                                methodHookParam.setResult(1000+((int)lat));
//                            }else if("mNetworkId".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------reflact:mNetworkId--------");
//                                methodHookParam.setResult(1000+((int)lon));
//                            }else if("mBaseStationId".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------reflact:mBaseStationId--------");
//                                methodHookParam.setResult(11201+((int)lat));
//                            }
//                        }
//                        if(isPreventGps){
//                            Field field = (Field) methodHookParam.thisObject;
//                            if("mLatitude".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------reflact:mLatitude--------");
//                                methodHookParam.setResult(lat);
//                            }else if("mLongitude".equals(field.getName())){
//                                XposedBridge.log(lpparam.packageName+"------reflact:mLongitude--------");
//                                methodHookParam.setResult(lon);
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            };
//            XposedHelpers.findAndHookMethod(Field.class,"get",Object.class,reflactHook);
            XposedUtil.hookMethod(teleCls, XposedUtil.getParmsByName(teleCls, "getNetworkType"), "getNetworkType",hooktele);
            XposedUtil.hookMethod(teleCls, XposedUtil.getParmsByName(teleCls, "getAllCellInfo"), "getAllCellInfo",hooktele);
            XposedUtil.hookMethod(teleCls, XposedUtil.getParmsByName(teleCls, "getCellLocation"), "getCellLocation",hooktele);
            XposedUtil.hookMethod(teleCls, XposedUtil.getParmsByName(teleCls, "getNeighboringCellInfo"), "getNeighboringCellInfo",hooktele);
            XposedUtil.hookMethod(teleCls, XposedUtil.getParmsByName(teleCls, "listen"), "listen",hooktelelis);
            XposedUtil.hookMethod(GsmCellLocation.class, XposedUtil.getParmsByName(GsmCellLocation.class, "getLac"), "getLac",hooklte);
            XposedUtil.hookMethod(GsmCellLocation.class, XposedUtil.getParmsByName(GsmCellLocation.class, "getCid"), "getCid",hooklte);
            XposedUtil.hookMethod(GsmCellLocation.class, XposedUtil.getParmsByName(GsmCellLocation.class, "getPsc"), "getPsc",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getBaseStationId"), "getBaseStationId",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getSystemId"), "getSystemId",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getNetworkId"), "getNetworkId",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getBaseStationLatitude"), "getBaseStationLatitude",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getBaseStationLongitude"), "getBaseStationLongitude",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getSystemId"), "getSystemId",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getNetworkId"), "getNetworkId",hooklte);
            XposedUtil.hookMethod(CdmaCellLocation.class, XposedUtil.getParmsByName(CdmaCellLocation.class, "getBaseStationId"), "getBaseStationId",hooklte);



            final Class netinfoCls = XposedUtil.findClass("android.net.NetworkInfo", lpparam.classLoader);
            final Class netinterCls = XposedUtil.findClass("java.net.NetworkInterface", lpparam.classLoader);
            final Class wifiinfoCls = XposedUtil.findClass("android.net.wifi.WifiInfo", lpparam.classLoader);
            final Class wifimanagerCls = XposedUtil.findClass("android.net.wifi.WifiManager", lpparam.classLoader);
            XC_MethodHook hooknetinfo = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_WIFIINFO]+"|"+isPreventWifi,true);
                        if(param.method.getName().equals("getTypeName")||param.method.getName().equals("getType")){
                            sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_NETTYPE_WIFI]+"|"+isPreventNetTypeWifi,false);
                            sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_NETTYPE_4G]+"|"+isPreventNetType4G,true);
                        }
                        if(isPreventWifi){
                            if(param.method.getName().equals("getTypeName")){
                                param.setResult("MOBILE");
                            }else if(param.method.getName().equals("getType")){
                                param.setResult(ConnectivityManager.TYPE_MOBILE);
                            }else if(param.method.getName().equals("getExtraInfo")){
                                param.setResult("");
                            }else if(param.method.getName().equals("isAvailable")||
                                param.method.getName().equals("isConnected")){
                                param.setResult(true);
                            }else if(param.method.getName().equals("getInterfaceAddresses")){
                                param.setResult("");
                            }
                        }else if(isPreventNetTypeWifi){
                            if(param.method.getName().equals("getTypeName")){
                                param.setResult("WIFI");
                            }else if(param.method.getName().equals("getType")){
                                param.setResult(ConnectivityManager.TYPE_WIFI);
                            }else if(param.method.getName().equals("isAvailable")||
                                    param.method.getName().equals("isConnected")){
                                param.setResult(true);
                            }
                        }else if(isPreventNetType4G){
                            if(param.method.getName().equals("getTypeName")){
                                param.setResult("MOBILE");
                            }else if(param.method.getName().equals("getType")){
                                param.setResult(ConnectivityManager.TYPE_MOBILE);
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };

            XC_MethodHook hookwifiinfo = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_WIFIINFO]+"|"+isPreventWifi,true);
                        if(isPreventWifi){
                            if(param.method.getName().equals("getBSSID")){
                                param.setResult("0"+getOneNum()+":0"+getOneNum()+":0"+getOneNum()+":0"+getOneNum()+":"+getOneNum()+"0:0"+getOneNum());
                            }else if(param.method.getName().equals("getIpAddress")){
                                param.setResult(0);
                            }else if(param.method.getName().equals("getMacAddress")){
                                param.setResult("0"+getOneNum()+":0"+getOneNum()+":0"+getOneNum()+":0"+getOneNum()+":"+getOneNum()+"0:0"+getOneNum());
                            }else if(param.method.getName().equals("getSSID")){
                                param.setResult("\" \"");
                            }else if(param.method.getName().equals("getConfiguredNetworks")||
                                    param.method.getName().equals("getScanResults")){
                                List list = (List) param.getResult();
                                list.clear();
                                param.setResult(list);
                            }else if(param.method.getName().equals("isWifiEnabled")){
                                param.setResult(false);
                            }else if(param.method.getName().equals("getWifiState")){
                                param.setResult(WifiManager.WIFI_STATE_DISABLED);
                            }
                        }else if(isPreventNetTypeWifi){
                            if(param.method.getName().equals("isWifiEnabled")){
                                param.setResult(true);
                            }else if(param.method.getName().equals("getWifiState")){
                                param.setResult(WifiManager.WIFI_STATE_ENABLED);
                            }
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(netinfoCls, XposedUtil.getParmsByName(netinfoCls, "getExtraInfo"), "getExtraInfo",hooknetinfo);
            XposedUtil.hookMethod(netinfoCls, XposedUtil.getParmsByName(netinfoCls, "getTypeName"), "getTypeName",hooknetinfo);
            XposedUtil.hookMethod(netinfoCls, XposedUtil.getParmsByName(netinfoCls, "getType"), "getType",hooknetinfo);
            XposedUtil.hookMethod(netinfoCls, XposedUtil.getParmsByName(netinfoCls, "isAvailable"), "isAvailable",hooknetinfo);
            XposedUtil.hookMethod(netinfoCls, XposedUtil.getParmsByName(netinfoCls, "isConnected"), "isConnected",hooknetinfo);
            XposedUtil.hookMethod(netinterCls, XposedUtil.getParmsByName(netinterCls, "getInterfaceAddresses"), "getInterfaceAddresses",hooknetinfo);
            XposedUtil.hookMethod(wifiinfoCls, XposedUtil.getParmsByName(wifiinfoCls, "getBSSID"), "getBSSID",hookwifiinfo);
            XposedUtil.hookMethod(wifiinfoCls, XposedUtil.getParmsByName(wifiinfoCls, "getIpAddress"), "getIpAddress",hookwifiinfo);
            XposedUtil.hookMethod(wifiinfoCls, XposedUtil.getParmsByName(wifiinfoCls, "getMacAddress"), "getMacAddress",hookwifiinfo);
            XposedUtil.hookMethod(wifiinfoCls, XposedUtil.getParmsByName(wifiinfoCls, "getSSID"), "getSSID",hookwifiinfo);
            XposedUtil.hookMethod(wifimanagerCls, XposedUtil.getParmsByName(wifimanagerCls, "getConfiguredNetworks"), "getConfiguredNetworks",hookwifiinfo);
            XposedUtil.hookMethod(wifimanagerCls, XposedUtil.getParmsByName(wifimanagerCls, "getScanResults"), "getScanResults",hookwifiinfo);
            XposedUtil.hookMethod(wifimanagerCls, XposedUtil.getParmsByName(wifimanagerCls, "isWifiEnabled"), "isWifiEnabled",hookwifiinfo);
            XposedUtil.hookMethod(wifimanagerCls, XposedUtil.getParmsByName(wifimanagerCls, "getWifiState"), "getWifiState",hookwifiinfo);



            XC_MethodHook hookteleinfo = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_DEVICEINFO]+"|"+isPreventDevInfo,true);
                        if(isPreventDevInfo){
                            if(param.method.getName().equals("getDeviceId")){
                                long i1 = (long)(Math.random()*10000000000L);
                                long i2 = (long)(Math.random()*100000L);
                                param.setResult(i1+""+i2);
                            }else if(param.method.getName().equals("getLine1Number")){
                                param.setResult("");
                            }else if(param.method.getName().equals("getMeid")){
                                long i1 = (long)(Math.random()*10000000000L);
                                long i2 = (long)(Math.random()*100000L);
                                param.setResult(i1+""+i2);
                            }else if(param.method.getName().equals("getSimSerialNumber")){//iccid  20位
                                long i1 = (long)(Math.random()*10000000000L);
                                long i2 = (long)(Math.random()*10000000000L);
                                param.setResult(i1+""+i2);
                            }else if(param.method.getName().equals("getInt")){//iccid  20位
                                if(Settings.Secure.ANDROID_ID.equals(param.args[1])){
                                    long i1 = (long)(Math.random()*10000000000L);
                                    long i2 = (long)(Math.random()*100000L);
                                    param.setResult(i1+""+i2);
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(teleCls, XposedUtil.getParmsByName(teleCls, "getDeviceId"), "getDeviceId",hookteleinfo);
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getLine1Number"), "getLine1Number",hookteleinfo);
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getMeid"), "getMeid",hookteleinfo);
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getSimSerialNumber"), "getSimSerialNumber",hookteleinfo);
            XposedUtil.hookMethod(sysSecCls, new Class[]{ContentResolver.class,String.class}, "getInt",hookteleinfo);


            XC_MethodHook hookImeiinfo = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_DEVICEIMEIINFO]+"|"+isPreventDevIMEIInfo,true);
                        if(isPreventDevIMEIInfo&&param.method.getName().equals("getImei")){//15位
                            param.setResult(IMEI);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getImei"), "getImei",hookImeiinfo);


            XC_MethodHook hookImsiinfo = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_DEVICEIMSIINFO]+"|"+isPreventDevIMSIInfo,true);
                        if(isPreventDevIMSIInfo&&param.method.getName().equals("getSubscriberId")){
                            param.setResult(IMSI);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getSubscriberId"), "getSubscriberId",hookImsiinfo);

            if (isPreventTime) {
                try {
                    final Long[] baseHolder = new Long[1];
                    XposedHelpers.findAndHookMethod("java.lang.System", lpparam.classLoader, "currentTimeMillis", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                if (!isSelfGetTime) {
//                                    if (isPreventTime) {
                                        if (baseHolder[0] == null) {
                                            baseHolder[0] = (Long) param.getResult();
                                            return;
                                        }
                                        long baseTime = baseHolder[0];
                                        long currTime = (Long) param.getResult();
                                        param.setResult(currTime - baseTime + time);
//                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    XposedHelpers.findAndHookMethod("android.text.format.Time", lpparam.classLoader, "setToNow", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                if (!isSelfGetTime) {
                                    ((android.text.format.Time) param.thisObject).set(time);
                                    param.setResult(null);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            XC_MethodHook hookFileDir = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                    try {
                        sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_REDIRFIEDIR]+"|"+isPreventFileDir,true);
//                        if (isPreventFileDir) {
//                            String s = param.getResult().toString() ;
//                            if(s!=null&&!isContainsKeyWord(s)&&!s.contains(newDir)){
//                                s = s.replace(defaultDir,defaultDir+ File.separator + newDir);
//                                param.setResult(new File(s));
//                            }else if(s!=null&&isContainsKeyWord(s)&&s.contains(newDir)){
//                                s = s.replace(File.separator+newDir,"");
//                                param.setResult(new File(s));
//                            }
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            if(isPreventFileDir&&defaultDir!=null){
//                XC_MethodHook hookFileDirs = new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//                        try {
//                            if(isPreventFileDir){
//                                Object obj = param.args[0];
//                                if(obj instanceof String){
//                                    String s = (String)obj;
//                                    if(s!=null&&!isContainsKeyWord(s)&&!s.contains(newDir)){
//                                        if(param.args.length==2){
//                                            String s1 = (String)param.args[1];
//                                            if(!isContainsKeyWord(s+File.separator+s1)&&!s1.contains(newDir)){
//                                                s = s.replace(defaultDir,defaultDir+ File.separator + newDir);
//                                                param.args[0] = s;
//                                            }else if(isContainsKeyWord(s)&&s.contains(newDir)){
//                                                s = s.replace(File.separator+newDir,"");
//                                                param.args[0] = s;
//                                            }
//                                        }else{
//                                            s = s.replace(defaultDir,defaultDir+ File.separator + newDir);
//                                            param.args[0] = s;
//                                        }
//                                    }else if(s!=null&&isContainsKeyWord(s)&&s.contains(newDir)){
//                                        s = s.replace(File.separator+newDir,"");
//                                        param.args[0] = s;
//                                    }
//                                }else if(obj instanceof File){
//                                    File f = (File)obj;
//                                    String s = f.getAbsolutePath();
//                                    if(s!=null&&!isContainsKeyWord(s)&&!s.contains(newDir)) {
//                                        if(param.args.length==2){
//                                            String s1 = (String)param.args[1];
//                                            if(!isContainsKeyWord(s+File.separator+s1)&&!s1.contains(newDir)){
//                                                s = s.replace(defaultDir,defaultDir+ File.separator + newDir);
//                                                param.args[0] = new File(s);
//                                            }else if(isContainsKeyWord(s)&&s.contains(newDir)){
//                                                s = s.replace(File.separator+newDir,"");
//                                                param.args[0] = new File(s);
//                                            }
//                                        }else{
//                                            s = s.replace(defaultDir, defaultDir + File.separator + newDir);
//                                            param.args[0] = new File(s);
//                                        }
//
//                                    }else if(s!=null&&isContainsKeyWord(s)&&s.contains(newDir)){
//                                        s = s.replace(File.separator+newDir,"");
//                                        param.args[0] = new File(s);
//                                    }
//                                }
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
//                XposedUtil.hookConstructorMethod(File.class,new Class[]{File.class,String.class},hookFileDirs);
//                XposedUtil.hookConstructorMethod(File.class,new Class[]{String.class,String.class},hookFileDirs);
//                XposedUtil.hookConstructorMethod(File.class,new Class[]{String.class},hookFileDirs);
                XC_MethodHook hookFileDirs = new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                        try {
                            Object obj = param.args[0];
                            boolean isNotFileCls = !param.thisObject.getClass().getName().endsWith(".File");
                            if(obj instanceof String){
                                String s1 = (String)obj;
                                String s2 = param.args.length==1||isNotFileCls?"":File.separator+(String)param.args[1];
                                String s = s1+s2;
                                if(s.startsWith("/storage")&&!s.equals(defaultDir)){
//                                    XposedBridge.log("FILE_TEST_1_"+s+"  "+isContainsKeyWord(s));
                                    if(!isContainsKeyWord(s)&&s.startsWith(defaultDir)&&!s.contains(newDir)){
                                        param.args[0] = s.replace(defaultDir,defaultDir+File.separator+newDir);
                                    }
                                }
                            }else if(obj instanceof File){
                                File f = (File)obj;
                                String s1 = f.getAbsolutePath();
                                String s2 = param.args.length==1?"":File.separator+(String)param.args[1];
                                String s = s1+s2;
                                if(s.startsWith("/storage")&&!s.equals(defaultDir)) {
//                                    XposedBridge.log("FILE_TEST_2_"+s+"  "+isContainsKeyWord(s));
                                    if(!isContainsKeyWord(s)&&s.startsWith(defaultDir)&&!s.contains(newDir)){
                                        param.args[0] = new File(s.replace(defaultDir,defaultDir+File.separator+newDir));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                XposedUtil.hookConstructorMethod(File.class,new Class[]{File.class,String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(File.class,new Class[]{String.class,String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(File.class,new Class[]{String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(RandomAccessFile.class,new Class[]{String.class,String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(FileInputStream.class,new Class[]{String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(FileOutputStream.class,new Class[]{String.class},hookFileDirs);
                XposedUtil.hookConstructorMethod(FileOutputStream.class,new Class[]{String.class,boolean.class},hookFileDirs);
            }
            XposedUtil.hookMethod(Environment.class, XposedUtil.getParmsByName(Environment.class, "getExternalStorageDirectory"),"getExternalStorageDirectory", hookFileDir);
            XposedUtil.hookMethod(Environment.class, XposedUtil.getParmsByName(Environment.class, "getExternalStoragePublicDirectory"),"getExternalStoragePublicDirectory", hookFileDir);
        }
    }

    private static int getOneNum(){
        return (int)(Math.random()*10);
    }

    private static long loctime = 0;
    private static void changeLoc(){
        try {
            if(ll==null||System.currentTimeMillis()-loctime<1000*5){
                return;
            }
            loctime = System.currentTimeMillis();
            Class<?> clazz = LocationListener.class;
            Method m = null;
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getName().equals("onLocationChanged")) {
                    m = method;
                    break;
                }
            }
            if (m != null) {
                //  mSettings.reload();
                Object[] args = new Object[1];
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setTime(System.currentTimeMillis()-1000);
                location.setLongitude(lon);
                location.setLatitude(lat);
                location.setAccuracy(3.0f);
                location.setAltitude(Math.random()*lon);
                args[0] = location;
                m.invoke(ll, args);
            }
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private static boolean isContainsKeyWord(String s){
        return s.contains(defaultDir+File.separator+"Android")||
                s.contains(newDir+File.separator+"Android")||
                s.contains(defaultDir+File.separator+"data")||
                s.contains(newDir+File.separator+"data")||
                s.contains(defaultDir+File.separator+"backup")||
                s.contains(newDir+File.separator+"backup")||
                s.contains(defaultDir+File.separator+"Pictures")||
                s.contains(newDir+File.separator+"Pictures")||
                s.contains(defaultDir+File.separator+"DCIM")||
                s.contains(newDir+File.separator+"DCIM")||
                s.contains(defaultDir+File.separator+"download")||
                s.contains(newDir+File.separator+"download")||
                s.contains(defaultDir+File.separator+"Download")||
                s.contains(newDir+File.separator+"Download");
    }
}
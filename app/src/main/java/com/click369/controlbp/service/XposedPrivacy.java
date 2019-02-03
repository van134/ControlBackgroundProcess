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
import com.click369.controlbp.common.Common;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
    private static Context mContext;
    private static HashMap<Long,String> infos = new HashMap<Long,String>();
    private static long time = 0;
    private static int count = 0;
    private static String pkg = "";
    private static LocationListener ll;
//    private static double lat=39.916803,lon = 116.403766;//lat纬度  lon//经度
    private static double lat=0,lon = 0;//lat纬度  lon//经度
    private static Handler handler = new Handler();
    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent("com.click369.control.ams.sendprivacyinfo");
            intent.putExtra("pkg",pkg);
            intent.putExtra("infos",infos);
            if(mContext!=null){
                mContext.sendBroadcast(intent);
                infos.clear();
                count = 0;
            }
        }
    };
    private static String lastInfo = "";
    private static long lastTime = 0;
    private static boolean isSelfGetTime = false;
    private static void sendBroad(String p,String info,boolean isSend){
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
            if(count>80){
                handler.removeCallbacks(runnable);
                runnable.run();
            }else{
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable,3000);
            }
        }
    }


    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences privacyPrefs){
        privacyPrefs.reload();
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
            final boolean isPreventAppList = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_APPLIST])||(isChangeLoc&&!isDIDISIJI);
            final boolean isPreventGps = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_GPSINFO])||isChangeLoc;
            final boolean isPreventBaseStation = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_BASESTATION])||isChangeLoc;
            final boolean isPreventWifi = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_WIFIINFO])||isChangeLoc;
            final boolean isPreventDevInfo = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_DEVICEINFO])||(isChangeLoc&&!isDIDISIJI);
            final boolean isPreventTime = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_CHANGETIME]);
//            final boolean isPreventContact = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_CONTACTINFO]);
//            XposedBridge.log("CONTROL_PRIVACY_PKG:"+lpparam.packageName+" isChangeLoc "+isChangeLoc);
            if(privacyPrefs.contains(lpparam.packageName + "/changetime")){
                time = privacyPrefs.getLong(lpparam.packageName + "/changetime", 0);
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
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mContext = ((Application)(param.thisObject)).getApplicationContext();
//                    privacyPrefs.reload();
//                    final Set<String> switchs1 = privacyPrefs.getStringSet(lpparam.packageName+"/prilist",new HashSet<String>());
//                    switchs.clear();
//                    switchs.addAll(switchs1);
//                    final boolean isChangeLoc = switchs.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_CHANGELOC]);
//                    if(isChangeLoc){
//                        try {
//                            String lonLat = privacyPrefs.getString(lpparam.packageName+"/changeloc","116.403766,39.916803");
//                            if(lonLat.contains(",")){
//                                String ss[] = lonLat.split(",");
//                                lon = Double.parseDouble(ss[0]);
//                                lat = Double.parseDouble(ss[1]);
//                            }
//                        }catch (Exception e){
//                            lat=39.916803;lon = 116.403766;
//                        }
//                        if("com.alibaba.android.rimet".equals(lpparam.packageName)){
//                            lat = lat+0.0012;
//                            lon = lon-0.0044;
//                        }
//                    }
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
                        if(isPreventAppList){
                            List list = (List) param.getResult();
                            List newList = new ArrayList();
                            for(Object o:list){
                                if(o instanceof ApplicationInfo){
                                    ApplicationInfo applicationInfo = (ApplicationInfo)o;
                                    if(applicationInfo.packageName.equals(lpparam.packageName)||applicationInfo.packageName.startsWith("com.android")){
                                        newList.add(o);
                                    }
                                }else if(o instanceof PackageInfo){
                                    PackageInfo packageInfo = (PackageInfo)o;
                                    if(packageInfo.packageName.equals(lpparam.packageName)||packageInfo.packageName.startsWith("com.android")){
                                        newList.add(o);
                                    }
                                }else if(o instanceof ResolveInfo){
                                    ResolveInfo resolveInfo = (ResolveInfo)o;
                                    if(resolveInfo.resolvePackageName.equals(lpparam.packageName)||resolveInfo.resolvePackageName.startsWith("com.android")){
                                        newList.add(o);
                                    }
                                }else if(o instanceof ResolveInfo){
                                    ResolveInfo resolveInfo = (ResolveInfo)o;
                                    if(resolveInfo.resolvePackageName.equals(lpparam.packageName)||resolveInfo.resolvePackageName.startsWith("com.android")){
                                        newList.add(o);
                                    }
                                }
                            }
                            param.setResult(newList);
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
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "queryIntentActivities"), "queryIntentActivities",hookapm);
            XposedUtil.hookMethod(apmCls, XposedUtil.getParmsByName(apmCls, "queryIntentContentProviders"), "queryIntentContentProviders",hookapm);


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
                            location.setTime(System.currentTimeMillis()-1000);
                            location.setLongitude(lon);
                            location.setLatitude(lat);
                            location.setAccuracy(3.0f);
                            location.setAltitude(Math.random()*lon);
                            param.setResult(location);
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
                            }else if(param.method.getName().equals("getImei")){//15位
                                long i1 = (long)(Math.random()*10000000000L);
                                long i2 = (long)(Math.random()*100000L);
                                param.setResult(i1+""+i2);
                            }else if(param.method.getName().equals("getMeid")){
                                long i1 = (long)(Math.random()*10000000000L);
                                long i2 = (long)(Math.random()*100000L);
                                param.setResult(i1+""+i2);
                            }else if(param.method.getName().equals("getSubscriberId")){
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
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getImei"), "getImei",hookteleinfo);
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getMeid"), "getMeid",hookteleinfo);
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getSubscriberId"), "getSubscriberId",hookteleinfo);
            XposedUtil.hookMethod(teleCls, XposedUtil.getMaxLenParmsByName(teleCls, "getSimSerialNumber"), "getSimSerialNumber",hookteleinfo);
            XposedUtil.hookMethod(sysSecCls, new Class[]{ContentResolver.class,String.class}, "getInt",hookteleinfo);
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
//            XC_MethodHook hookResolver = new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
//                    try {
//                        if(param.args.length>3&&param.args[0]!=null&&param.args[2] instanceof Bundle&&param.args[0] instanceof Uri){
//                            Uri uri = (Uri)param.args[0];
//                            XposedBridge.log("CONTACT_URI:"+uri.toString());
//                            if(ContactsContract.Contacts.CONTENT_URI.equals(uri)||ContactsContract.CommonDataKinds.Phone.CONTENT_URI.equals(uri)){
//                                sendBroad(lpparam.packageName,Common.PRIVACY_KEYS[Common.PRI_TYPE_CONTACTINFO]+"|"+isPreventContact,true);
//                                if(isPreventContact){
//                                    String url = uri.toString().replace("contacts","fuck");
//                                    param.args[0] = Uri.parse(url);
//                                }
//                            }
//                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
//                        }
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            };
//            XposedUtil.hook_methods(ContentResolver.class,"query",hookResolver);
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

}
package com.click369.controlbp.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.AttributeSet;
import android.widget.TextView;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedLocation {
//    public static  double lat = 0,lon = 0;
//    public static boolean ischangeloc = false;
//    public static LocationListener ll;
//    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences settingPrefs){
//        try {
//            ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//            if(ischangeloc){
//                String latStr = settingPrefs.getString(Common.PREFS_SETTING_OTHER_LOC_LAT,"0");
//                String lonStr = settingPrefs.getString(Common.PREFS_SETTING_OTHER_LOC_LON,"0");
//                if(latStr.length()>3){
//                    lat = Double.parseDouble(latStr);
//                    lon = Double.parseDouble(lonStr);
//                }else {
//                    ischangeloc = false;
//                    return;
//                }
//            }else{
//                return;
//            }
//            XposedUtil.hookMethod(WifiManager.class, XposedUtil.getParmsByName(WifiManager.class, "getScanResults"), "getScanResults", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    settingPrefs.reload();
//                    ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//                    if(ischangeloc){
//                        List<ScanResult> list = new ArrayList<ScanResult>();
//                        param.setResult(list);
//                        return;
//                    }
//                }
//            });
//
////            Class telecls = XposedHelpers.findClass("android.telephony.TelephonyManager", lpparam.classLoader);
//            XposedUtil.hookMethod(TelephonyManager.class, XposedUtil.getParmsByName(TelephonyManager.class, "getCellLocation"), "getCellLocation", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    settingPrefs.reload();
//                    ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//                    if(ischangeloc&&!lpparam.packageName.equals("com.tencent.mobileqq")) {
//                        GsmCellLocation gsmCellLocation = new GsmCellLocation();
//                        gsmCellLocation.setLacAndCid(0, 0);
//                        param.setResult(gsmCellLocation);
//                        return;
//                    }
//                }
//            });
//            XposedUtil.hookMethod(TelephonyManager.class, XposedUtil.getParmsByName(TelephonyManager.class, "getNeighboringCellInfo"), "getNeighboringCellInfo", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    settingPrefs.reload();
//                    ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//                    if(ischangeloc&&!lpparam.packageName.equals("com.tencent.mobileqq")) {
//                        List<NeighboringCellInfo> list = new ArrayList<NeighboringCellInfo>();
//                        NeighboringCellInfo neighboringCellInfo = new NeighboringCellInfo(0, "0", 0);
//                        list.add(neighboringCellInfo);
////                    XposedBridge.log(lpparam.packageName+" TelephonyManager getNeighboringCellInfo");
//                        param.setResult(list);
//                        return;
//                    }
//                }
//            });
//
//            Class loccls = XposedHelpers.findClass("android.location.LocationManager", lpparam.classLoader);
//            hook_methods("android.location.LocationManager","requestLocationUpdates", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    if (param.args.length == 4 && (param.args[0] instanceof String)) {//&&
//                        settingPrefs.reload();
//                        ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//                        if(ischangeloc){
//                            String latStr = settingPrefs.getString(Common.PREFS_SETTING_OTHER_LOC_LAT,"0");
//                            String lonStr = settingPrefs.getString(Common.PREFS_SETTING_OTHER_LOC_LON,"0");
//                            if(latStr.length()>3){
//                                lat = Double.parseDouble(latStr);
//                                lon = Double.parseDouble(lonStr);
//                            }
//                            XposedBridge.log(lpparam.packageName+" LocationManager requestLocationUpdates");
//                            //位置监听器,当位置改变时会触发onLocationChanged方法
//                            ll = (LocationListener)param.args[3];
////                            LocationListener ll = (LocationListener)param.args[3];
//                            changeLoc();
//                        }
//                    }
//                }
//            });
////            LocationManager
//            XposedUtil.hookMethod(loccls, XposedUtil.getParmsByName(loccls, "isProviderEnabled"), "isProviderEnabled", new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    settingPrefs.reload();
//                    ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//                    if(ischangeloc){
//                        if(LocationManager.GPS_PROVIDER.equals(param.args[0])){
//                            changeLoc();
//                            param.setResult(true);
//                        }
//                    }
//                }
//            });
//
//            XposedHelpers.findAndHookMethod(Location.class,  "getLatitude", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    settingPrefs.reload();
//                    ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//                    if(ischangeloc) {
//                        String latStr = settingPrefs.getString("change_lat", "0");
//                        String lonStr = settingPrefs.getString("change_lon", "0");
//                        if (latStr.length() > 3) {
//                            lat = Double.parseDouble(latStr);
//                            lon = Double.parseDouble(lonStr);
//                        }
//                        param.setResult(lat);
//                        return;
//                    }
//                }
//            });
//            XposedHelpers.findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                    settingPrefs.reload();
//                    ischangeloc = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_ISCHANGELOCK,false);
//                    if(ischangeloc) {
//                        String latStr = settingPrefs.getString("change_lat", "0");
//                        String lonStr = settingPrefs.getString("change_lon", "0");
//                        if (latStr.length() > 3) {
//                            lat = Double.parseDouble(latStr);
//                            lon = Double.parseDouble(lonStr);
//                        }
//                        param.setResult(lon);
//                        return;
//                    }
//                }
//            });
//        }catch (Throwable e){
//            e.printStackTrace();
//        }
//    }
//
//    private static long loctime = 0;
//    private static void changeLoc(){
//        try {
//            if(ll==null||System.currentTimeMillis()-loctime<1000*5){
//                return;
//            }
//            loctime = System.currentTimeMillis();
//            Class<?> clazz = LocationListener.class;
//            Method m = null;
//            for (Method method : clazz.getDeclaredMethods()) {
//                if (method.getName().equals("onLocationChanged")) {
//                    m = method;
//                    break;
//                }
//            }
//            if (m != null) {
//                //  mSettings.reload();
//                Object[] args = new Object[1];
//                Location l = new Location(LocationManager.GPS_PROVIDER);
//                double la=lat;//帝都的经纬度
//                double lo=lon;
//                l.setLatitude(la);
//                l.setLongitude(lo);
//                args[0] = l;
//                m.invoke(ll, args);
////                                    param.args[1] = null;
//            }
//        } catch (Throwable e) {
//            XposedBridge.log(e);
//        }
//    }
//    //带参数的方法拦截
//    private static void hook_methods(String className, String methodName, XC_MethodHook xmh)
//    {
//        try {
//            Class<?> clazz = Class.forName(className);
//
//            for (Method method : clazz.getDeclaredMethods())
//                if (method.getName().equals(methodName)
//                        && !Modifier.isAbstract(method.getModifiers())
//                        && Modifier.isPublic(method.getModifiers())) {
//                    XposedBridge.hookMethod(method, xmh);
//                }
//        } catch (Exception e) {
//            XposedBridge.log(e);
//        }
//    }
}
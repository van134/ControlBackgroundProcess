package com.click369.controlbp.util;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.receiver.AddAppReceiver;
import com.click369.controlbp.service.WatchDogService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by asus on 2017/10/17.
 */
public class PackageUtil {
    public static boolean isEnable(String pkg, String servicename, PackageManager packageManager) {
        return packageManager.getComponentEnabledSetting(new ComponentName(pkg, servicename)) <= 1;
    }

    public static ServiceInfo[] getServicesByPkg(Activity cxt, String pkg){
        ServiceInfo[] sifs = new ServiceInfo[0];
        if(cxt==null){return sifs;}
        synchronized (cxt) {
            PackageManager pm = cxt.getApplication().getPackageManager();
            try {
                sifs = pm.getPackageInfo(pkg, 516).services;
            } catch (Exception e) {
                try {
                    pm = cxt.getApplication().getPackageManager();
                    sifs = pm.getPackageInfo(pkg, 516).services;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sifs;
    }
    public static ActivityInfo[] getReceiverByPkg(Activity cxt, String pkg){
        ActivityInfo[] sifs = new ActivityInfo[0];
        if(cxt==null){
            return sifs;
        }
        synchronized (cxt) {

            PackageManager pm = cxt.getApplication().getPackageManager();
            try {
                sifs = pm.getPackageInfo(pkg, PackageManager.GET_RECEIVERS | PackageManager.GET_DISABLED_COMPONENTS).receivers;
            } catch (Exception e) {
                try {
                    pm = cxt.getApplication().getPackageManager();
                    sifs = pm.getPackageInfo(pkg, PackageManager.GET_RECEIVERS | PackageManager.GET_DISABLED_COMPONENTS).receivers;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sifs;
    }
    public static ActivityInfo[] getActivityByPkg(Activity cxt, String pkg){
        ActivityInfo[] sifs = new ActivityInfo[0];
        if(cxt==null){
            return sifs;
        }
        synchronized (cxt){
            PackageManager pm = cxt.getApplication().getPackageManager();
            try {
                sifs = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS).activities;
            } catch (Exception e) {
                try {
                    pm = cxt.getApplication().getPackageManager();
                    sifs = pm.getPackageInfo(pkg, PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS).activities;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return sifs;
    }
    public static HashSet<String> getServicesName(String pkg){
        HashSet<String> sers = new HashSet<String>();
        String serviceStrs = ShellUtils.execCommand("dumpsys activity services "+pkg,true,true).successMsg;
//        Log.i("DOZE","Services1  "+serviceStrs);
        if(serviceStrs.length()>0){
            String strs[] = serviceStrs.split("\n");
            for(String str:strs){
                if(str.trim().startsWith("* ServiceRecord")){
                    String sernames[] =  str.trim().split(" ");
                    if(sernames!=null&&sernames.length>1){
                        String sername =  sernames[sernames.length-1].replace("}","").trim();
                        Log.i("DOZE","sername  "+sername);
//                        sername = sername.replace(pkg+"/","");
                        sers.add(sername);
                    }
                }
            }
        }
        return sers;
    }

    public static void getIntentFliterByName(Context context){
        PackageManager pm = context.getPackageManager();// 查询条件
        // 可以把activity的信息赋值进去
        Intent launcher = new Intent();
        launcher.setComponent(new ComponentName(Common.PACKAGENAME, AddAppReceiver.class.getCanonicalName()));
        List<ResolveInfo> list = pm.queryBroadcastReceivers(launcher, PackageManager.MATCH_DEFAULT_ONLY);
        Log.i("CONTROL","  action  list " + list+ list.size());
        if (list!=null&&list.size()>0){
            IntentFilter intentFilter = list.get(0).filter;
//            Iterator<String> iterator = intentFilter.actionsIterator();
//            while (iterator.hasNext()){
                Log.i("CONTROL","  action  " +intentFilter);
//            }
        }
    }
//    public static HashMap<String,ArrayList<String>> getBroadCastNameAndFliter(String pkg){
//        HashSet<String> sers = new HashSet<String>();
//        String broadStrs = ShellUtils.execCommand("dumpsys activity broadcasts "+pkg,true,true).successMsg;
////        Log.i("DOZE","Services1  "+serviceStrs);
//        if(serviceStrs.length()>0){
//            String strs[] = serviceStrs.split("\n");
//            for(String str:strs){
//                if(str.trim().startsWith("* ServiceRecord")){
//                    String sernames[] =  str.trim().split(" ");
//                    if(sernames!=null&&sernames.length>1){
//                        String sername =  sernames[sernames.length-1].replace("}","").trim();
//                        Log.i("DOZE","sername  "+sername);
////                        sername = sername.replace(pkg+"/","");
//                        sers.add(sername);
//                    }
//                }
//            }
//        }
//        return sers;
//    }
    public static boolean isAppInIceRoom(Context c,String pkg){
        PackageManager packageManager = c.getPackageManager();
        try {
            return !packageManager.getPackageInfo(pkg,516).applicationInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean isEnableCompent(Context cxt, Class cls){
        PackageManager packageManager = cxt.getPackageManager();
        ComponentName componentName = new ComponentName(cxt, cls);
        int res = packageManager.getComponentEnabledSetting(componentName);
        if (res == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || res == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

            return true;
        } else {

            return false;
        }
    }

    public static String getRunngingApp(Context cxt){
        StringBuilder sb = new StringBuilder();
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> rps =  am.getRunningAppProcesses();
        if (rps!=null){
            for(ActivityManager.RunningAppProcessInfo ar:rps){
//            Log.i("CONTROL","rps   "+ar.pkgList[0]);
                if(ar.pkgList.length>0&&sb.indexOf(ar.pkgList[0])==-1){
                    for(String s:ar.pkgList){
                        if(sb.indexOf(s)==-1){
                            sb.append(s).append("\n");
                        }
                    }
                }
            }
//        Log.i("CONTROL","running  "+sb.toString());
            if(sb.length()<100){
                return "";
            }
        }
        return sb.toString();
    }

    public static HashSet<String> getRunngingAppList(Context cxt){
        HashSet<String> runLists = new HashSet<String>();
//        StringBuilder sb = new StringBuilder();
//        if(WatchDogService.TEST){
//            return new HashSet<String>();
//        }
        try {
            ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> rps =  am.getRunningAppProcesses();
            if (rps!=null){
                for(ActivityManager.RunningAppProcessInfo ar:rps){
                    if(ar.pkgList!=null&&ar.pkgList.length>0){
                        for(String s:ar.pkgList){
//                        if(sb.indexOf(s)==-1){
//                            sb.append(s).append("\n");
                            runLists.add(s);
//                        }
                        }
                    }
                }
//        Log.i("CONTROL","running  "+sb.toString());
            }
            if(runLists.size()<5&& WatchDogService.isRoot){
                runLists.clear();
                String info = ShellUtils.execCommand("ps",true,true).successMsg;
                if(info.length()>0){
                    info = info.replaceAll(" +"," ");
                    String lines[] = info.split("\n");
                    if(lines.length>0){
                        for(String line:lines){
                            String words[] = line.split(" ");
                            if(words.length>8){
                                if(words[0].startsWith("u0")||
                                        words[0].startsWith("sys")){
                                    runLists.add(words[8].trim());
//                                Log.i("CONTROL","words[8].trim()  "+words[8].trim()+"  "+words[8].trim().length());
                                }
                            }
                        }
                    }
                }
//            return new HashSet<String>();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return runLists;
    }

    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return versionName;
    }

    public static String getAppNameByPkg(Context context,String pkg) {
        try {
            String name = null;
            try {
                PackageManager pm = context.getPackageManager();
                name = pm.getApplicationLabel(
                        pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA)).toString();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return name;
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        return pkg;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
    public static Drawable getBitmap(Context context,String pkg) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext().getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
//        BitmapDrawable bd = (BitmapDrawable) d;
//        Bitmap bm = bd.getBitmap();
        return d;
    }

}

package com.click369.controlbp.util;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.service.NotificationService;

import java.util.List;

/**
 * Created by asus on 2017/10/16.
 */
public class OpenCloseUtil {
    static long lastStartCloaseOpenAccessTime = 0;
    public static void closeOpenAccessibilitySettingsOn(final Context mContext,boolean open) {
        if (System.currentTimeMillis()-lastStartCloaseOpenAccessTime<1000){
            return;
        }
        lastStartCloaseOpenAccessTime = System.currentTimeMillis();
//        final String service1 = Common.PACKAGENAME + "/" + NewWatchDogService.class.getCanonicalName();
        final String service = Common.PACKAGENAME + "/.service.NewWatchDogService";
        String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)+"";
        Log.i("DOZE","AccessListenerService0  "+settingValue);
        settingValue = settingValue.replace("%tmp","");
        Log.i("DOZE","AccessListenerService1  "+settingValue);
        if(settingValue.contains(":"+service)){
            Log.i("DOZE","toggleAccessListenerService1  ");
            ShellUtils.execCommand("settings put secure enabled_accessibility_services %tmp"+settingValue.replace(":"+service,""),true,false);
            if(open){
                ShellUtils.execCommand("settings put secure accessibility_enabled 0",true,false);
                ShellUtils.execCommand("settings put secure enabled_accessibility_services %tmp"+settingValue,true,false);
                ShellUtils.execCommand("settings put secure accessibility_enabled 1",true,false);
            }
        }else{
            Log.i("DOZE","toggleAccessListenerService2  ");
            if(open){
                if(settingValue.trim().length()>2&&!settingValue.contains("null")){
                    ShellUtils.execCommand("settings put secure enabled_accessibility_services %tmp:"+settingValue+":"+service,true,false);
                }else{
                    ShellUtils.execCommand("settings put secure enabled_accessibility_services %tmp:"+service,true,false);
                }
                ShellUtils.execCommand("settings put secure accessibility_enabled 1",true,false);
            }
        }
        lastStartCloaseOpenAccessTime = System.currentTimeMillis();
    }


    static long lastStartNotifyTime = 0;
    static String settingValue="";
    public static void startNotifyListener(Context cxt){
        if(System.currentTimeMillis() - lastStartNotifyTime<2*1000|| NotificationService.isNotifyRunning){
            return;
        }
        PackageManager pm = cxt.getPackageManager();
        settingValue = Settings.Secure.getString(cxt.getContentResolver(),"enabled_notification_listeners");
        Log.i("DOZE","notificationstring1  settingValue"+settingValue);
        if(settingValue!=null&&settingValue.length()>0&&settingValue.contains("%tmp:")){
            settingValue =settingValue.replaceAll("%tmp:","");
        }
        Log.i("DOZE","notificationstring2  settingValue"+settingValue);
        final String service = Common.PACKAGENAME+"/"+NotificationService.class.getCanonicalName();
        if (settingValue==null||settingValue.length()==0||!settingValue.contains(NotificationService.class.getName())) {
            ShellUtils.execCommand("settings put secure enabled_notification_listeners %tmp:"+service+":"+settingValue,true,false);
            Log.i("DOZE","notificationstring1  ");
        }else{
            toggleNotificationListenerService(cxt,pm);
            Log.i("DOZE","notificationstring2  ");
        }
        lastStartNotifyTime = System.currentTimeMillis();
    }
    public static void stopNotifyListener(Context cxt){
        if(System.currentTimeMillis() - lastStartNotifyTime<2*1000|| !NotificationService.isNotifyRunning){
            return;
        }
        settingValue = Settings.Secure.getString(cxt.getContentResolver(),"enabled_notification_listeners");

        if(settingValue!=null&&settingValue.length()>0&&settingValue.contains("%tmp:")) {
            settingValue = settingValue.replaceAll("%tmp:", "");
        }
        final String service1 = Common.PACKAGENAME+"/com.click369.controlbp.service.NotificationService";
        final String service2 = Common.PACKAGENAME+"/.service.NotificationService";
        if (settingValue==null||settingValue.length()==0||!settingValue.contains(NotificationService.class.getName())) {
        }else{
            settingValue = settingValue.replace(service1,"");
            settingValue = settingValue.replace(service2,"");
            if (settingValue.indexOf(":")==0){
                settingValue = settingValue.replaceFirst(":","");
            }
            settingValue = settingValue.replace("::",":");
            Log.i("DOZE","notificationstring1  settingValue  "+settingValue);
            ShellUtils.execCommand("settings put secure enabled_notification_listeners %tmp:"+settingValue,true,false);
        }
        lastStartNotifyTime = System.currentTimeMillis();
    }

    private static void toggleNotificationListenerService(final Context cxt,final PackageManager pm){// throws  InterruptedException
        new Thread(){
            public void run(){
                try{
                    pm.setComponentEnabledSetting(new ComponentName(cxt,NotificationService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    pm.setComponentEnabledSetting(new ComponentName(cxt,NotificationService.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }catch (Exception e){
                    e.printStackTrace();
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(cxt,"通知监听启动失败，请在设置中手动关闭再开启通知监听功能",Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }

//    public static void toggleAccessListenerService(final Context cxt,final PackageManager pm){// throws  InterruptedException

//        new Thread(){
//            public void run(){
//                try{
//                    pm.setComponentEnabledSetting(new ComponentName(cxt,NewWatchDogService.class), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    pm.setComponentEnabledSetting(new ComponentName(cxt,NewWatchDogService.class),PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//                }catch (Exception e){
//                    e.printStackTrace();
//                    new Handler().post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(cxt,"无障碍启动失败，请在设置中手动关闭再开启无障碍功能",Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            }
//        }.start();
//    }

    //键值对1  pkg+/ad  = 方式1/2/3
    //键值对2  pkg+/one = 第一个活动
    //键值对3  pkg+/two = 第二个活动
    //键值对4  pkg+/three = 第三个活动
    //如果有第一个键值对则判断剩下的三个有没有 没有则采集 有则根据方式进行跳过操作
    //获取第一个activity
    public static String getFirstActivity(String packagename, Context act){
        try {
            PackageInfo packageinfo = act.getPackageManager().getPackageInfo(packagename, 0);
            if (packageinfo == null) {
                return "";
            }
            // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
            Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
            resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            resolveIntent.setPackage(packageinfo.packageName);
            List<ResolveInfo> resolveinfoList = act.getPackageManager().queryIntentActivities(resolveIntent, 0);
            ResolveInfo resolveinfo = resolveinfoList.iterator().next();
            if (resolveinfo != null) {
                String className = resolveinfo.activityInfo.name;
                return className;
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void doStartApplicationWithPackageName(String packagename, Context act) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = act.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }

        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = act.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        if (resolveinfoList == null||resolveinfoList.size()==0) {
            return;
        }
        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packagename = 参数packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);

            intent.setComponent(cn);
            try{
                act.startActivity(intent);
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }



    public static void doStartApplicationWithPackageName(String packagename,String className, Context act) {
        if (className==null||className.length()<2){
            doStartApplicationWithPackageName(packagename,act);
        }else {
            // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
            PackageInfo packageinfo = null;
            try {
                packageinfo = act.getPackageManager().getPackageInfo(packagename, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packageinfo == null) {
                return;
            }
            // LAUNCHER Intent
            Intent intent = new Intent();
            // 设置ComponentName参数1:packagename参数2:MainActivity路径
            ComponentName cn = new ComponentName(packagename, className);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            try{
                act.startActivity(intent);
            }catch (RuntimeException e){
                e.printStackTrace();
            }

        }
    }
}

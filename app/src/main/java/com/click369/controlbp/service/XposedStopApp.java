package com.click369.controlbp.service;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.UserHandle;
import android.util.Log;

import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedStopApp {
//    public static  int tag = -1;

    public static void stopApk(String pkg,Context cxt){
        try {
            if(ContainsKeyWord.isContainsPkg(pkg)
                    ||pkg.toLowerCase().contains("clock")
                    ||pkg.toLowerCase().contains("stk"))
            {
                return;
            }
//            if(tag == -1){
//                tag = 0;
//            }
            if (WatchDogService.isSaveBackLog) {
                FileUtil.writeLog(FileUtil.getLog("杀死 " + PackageUtil.getAppNameByPkg(cxt, pkg)));
            }
            final AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(pkg)?AppLoaderUtil.allHMAppInfos.get(pkg):new AppInfo();
            ai.isRunning = false;
            AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(pkg)?AppLoaderUtil.allAppStateInfos.get(pkg):new AppStateInfo();
            AppStateInfo newAsi = new AppStateInfo();
            newAsi.isOpenFromIceRome = asi.isOpenFromIceRome;
            AppLoaderUtil.allAppStateInfos.put(pkg,newAsi);
            AppLoaderUtil.runLists.remove(pkg);
            if(ai.isLockApp){
                SharedPrefsUtil.getInstance(cxt).autoStartNetPrefs.edit().remove(pkg+"/lockok").commit();
            }
            if(ai.getPackageName().equals(WatchDogService.musicPlayPkg)){
                WatchDogService.musicPlayPkg = "";
            }
            if(ai.isNotStop){
                Log.i("CONTROL","changepersistent  "+pkg);
                Intent intent1 = new Intent("com.click369.control.ams.changepersistent");
                intent1.putExtra("persistent",false);
                intent1.putExtra("pkg",pkg);
                cxt.sendBroadcast(intent1);
                AppLoaderUtil.getInstance(cxt).notifyRuningStateChange();
                return;
            }
            if (!WatchDogService.isXPstop){
                ShellUtilNoBackData.kill(pkg);
                Log.i("CONTROL","ROOT 杀死进程"+pkg);
            }else{
                Log.i("CONTROL","AMS 杀死进程"+pkg);
                //部分系统无法使用
                ActivityManager am =(ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
                Method m = am.getClass().getDeclaredMethod("forceStopPackage",String.class);
                m.setAccessible(true);
                m.invoke(am,pkg);
            }
            AppLoaderUtil.getInstance(cxt).notifyRuningStateChange();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("CONTROL","AMS 杀死进程出错 改为root方式");
            ShellUtilNoBackData.kill(pkg);
        }
//        return false;
    }
}
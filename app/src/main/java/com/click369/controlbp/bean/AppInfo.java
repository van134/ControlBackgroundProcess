package com.click369.controlbp.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.BytesBitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by asus on 2017/5/29.
 */
public class AppInfo implements Serializable {
    public String appName;
    public String packageName;
    public boolean isUser;
    public boolean isDisable = false;
    public boolean isServiceStop;
    public boolean isSetTimeStopApp;//定时关闭
    public boolean isSetTimeStopOneTime;//是否是一次性的定时关闭
    public boolean isWakelockStop;
    public boolean isBroadStop;
    public boolean isAlarmStop;
    public boolean isBackForceStop;
    public boolean isBackMuBei;
    public boolean isInMuBei;
    public boolean isHomeMuBei;
    public boolean isHomeIdle;
    public boolean isOffscForceStop;
    public boolean isOffscMuBei;
    public boolean isNotifyNotExit;
    public boolean isAutoStart;
    public boolean isNotStop;
    public boolean isLockApp;
    public boolean isStopApp;
    public boolean isDozeOffsc;
    public boolean isDozeOnsc;
    public boolean isDozeOpenStop;
    public boolean isRecentNotClean;
    public boolean isRecentForceClean;
    public boolean isRecentBlur;
    public boolean isRecentNotShow;

    public boolean isblackAllXp;
    public boolean isblackControlXp;
    public boolean isNoCheckXp;
    public boolean isSetCanHookXp;

    public boolean isBarLockList;
    public boolean isBarColorList;
    public boolean isADJump;
    public boolean isPreventNotify;
    public boolean isNotUnstall;
    public boolean isPriSwitchOpen;
    public boolean isPriWifiPrevent;
    public boolean isPriMobilePrevent;
//    public boolean isStopWifiNet;
    public boolean isRunning;

    public int activityCount;
    public int serviceCount;
    public int broadCastCount;

    public int setTimeStopAppTime;
    public int activityDisableCount;
    public int serviceDisableCount;
    public int broadCastDisableCount;
    public int openCount = 0;
    public long lastOpenTime;
    public int uid;
    public long instanllTime;
    public long updateTime;
    public String versionName;
    public long versionCode;
//    public byte bits[];
    public File iconFile;
//    public AppStateInfo stateInfo;
    public AppInfo(){}
    public AppInfo(String appName){
        this.appName = appName;
    }
    public AppInfo(String appName, String packageName){
        this.appName = appName;
        this.packageName = packageName;
    }
    public AppInfo(String appName, String packageName, boolean isUser, boolean isDisable){
        this.appName = appName;
        this.packageName = packageName;
//        this.drawable = drawable;
//        bits = BytesBitmap.getBytes(bm);
        this.isUser = isUser;
        this.isDisable = isDisable;
//        stateInfo = new AppStateInfo();
        iconFile = new File(AppLoaderUtil.iconPath,packageName);
    }


    public String getAppName() {
        if(null == appName)
            return "";
        else
            return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        if(null == packageName)
            return "";
        else
            return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

//    public Bitmap getBitmap() {
//        return getRoundedCornerBitmap(BytesBitmap.getBitmap(bits));
////        return getRoundedCornerBitmap(BytesBitmap.getBitmap(bits));
//    }

//    public void setBitmap(Bitmap bitamp) {
//        this.bits = BytesBitmap.getBytes(bitamp);
//    }
//
//    public void recDradwable(){
//        if (bits != null&&bits.length>0) {
//            bits = null;
//        }
//    }
    public void resetSetting(){
        isServiceStop = false;
        isSetTimeStopApp = false;//定时关闭
        isSetTimeStopOneTime = false;//是否是一次性的定时关闭
        isWakelockStop = false;
        isBroadStop = false;
        isAlarmStop = false;
        isBackForceStop = false;
        isBackMuBei = false;
        isInMuBei = false;
        isHomeMuBei = false;
        isHomeIdle = false;
        isOffscForceStop = false;
        isOffscMuBei = false;
        isNotifyNotExit = false;
        isAutoStart = false;
        isNotStop = false;
        isLockApp = false;
        isStopApp = false;
        isDozeOffsc = false;
        isDozeOnsc = false;
        isDozeOpenStop = false;
        isRecentNotClean = false;
        isRecentForceClean = false;
        isRecentBlur = false;
        isRecentNotShow = false;

        isblackAllXp = false;
        isblackControlXp = false;
        isNoCheckXp = false;
        isSetCanHookXp = false;

        isBarLockList = false;
        isBarColorList = false;
        isADJump = false;
        isNotUnstall = false;
        isRunning = false;
        setTimeStopAppTime=0;
        openCount = 0;
        lastOpenTime=0;

    }

    public static void writeArrays(ArrayList<AppInfo> infos, Context cxt){
        try {
            File f = new File(cxt.getFilesDir(),"app.obj");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(infos);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static  ArrayList<AppInfo> readArrays(Context cxt){
        ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
        File f = new File(cxt.getFilesDir(),"app.obj");
        if(!f.exists()){
            Log.i("CONTROL","applist 不存在"+f.getAbsolutePath());
            return apps;
        }
        Log.i("CONTROL","applist 1存在");
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            apps = ((ArrayList<AppInfo>)ois.readObject());

            Log.i("CONTROL","applist 存在"+apps.size());
            ois.close();
        } catch (Exception e) {
            f.delete();
//            AppInfo.writeArrays(apps, cxt);
//            e.printStackTrace();
        }finally {
            return apps;
        }
    }


}

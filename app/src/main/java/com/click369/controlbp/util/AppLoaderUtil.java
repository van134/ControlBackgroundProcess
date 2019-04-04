package com.click369.controlbp.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Process;
import android.os.UserHandle;
import android.support.v7.widget.DrawableUtils;
import android.util.Log;

import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.service.WatchDogService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by asus on 2017/10/15.
 */
public class AppLoaderUtil {
    public static File iconPath;
    private static AppLoaderUtil instance;
    private SharedPrefsUtil sharedPrefs;
    private Context context;
//    private boolean isLocalChange = true;
    private boolean isLoadAppSettingThreadRun = false;
    private boolean isLoadAppThreadRun = false;
    public static boolean isIceAppChange = false;
    public boolean isAppChange = true;
    public boolean isSettingChange = true;
    private long lastUpdateTime = 0;
    private PackageManager pm = null;
    public static final HashMap<String,AppStateInfo> allAppStateInfos = new HashMap<String,AppStateInfo>();
    public static final HashMap<String,AppInfo> allHMAppInfos = new HashMap<String,AppInfo>();
    public static final HashMap<String,Bitmap> allHMAppIcons = new HashMap<String,Bitmap>();
    public static final ArrayList<AppInfo> allAppInfos = new ArrayList<AppInfo>();
    public static final HashSet<String> runLists = new HashSet<String>();
    public static AppLoaderUtil getInstance(Context context){
        if(instance==null){
            instance = new AppLoaderUtil(context);
        }
        return instance;
    }
    public void setIsAppChange(boolean isAppChange){
        this.isAppChange = isAppChange;
    }
    public void setIsPrefsChange(boolean isPrefsChange){
        sharedPrefs.isPrefsChange = isPrefsChange;
    }
    private AppLoaderUtil(Context context) {
        pm = context.getApplicationContext().getPackageManager();
        this.context = context;
        sharedPrefs = SharedPrefsUtil.getInstance(context);

        try {
            iconPath = new File(context.getFilesDir(),"icon");
            iconPath.mkdirs();
        }catch (Exception e){

        }

    }

    public void reloadRunList(){

        runLists.clear();
        runLists.addAll(PackageUtil.getRunngingAppList(context));
//        if(runLists.size()==0){
//            runLists.addAll(AppLoaderUtil.allHMAppInfos.keySet());
//        }
    }

    private void initLocalApp(){
        final ArrayList<AppInfo> apps = AppInfo.readArrays(context);
        if(apps==null||apps.size()==0){
//            isLocalChange = true;
        }else{
            allHMAppInfos.clear();
            allAppInfos.clear();
            allAppInfos.addAll(apps);
//            isLocalChange = false;
        }
        reloadRunList();
        for(AppInfo ai:allAppInfos){
            allHMAppInfos.put(ai.packageName,ai);
            ai.isRunning = runLists.contains(ai.packageName);
            if(!allAppStateInfos.containsKey(ai.packageName)){
                allAppStateInfos.put(ai.packageName,new AppStateInfo());
            }
        }
        if(isIceAppChange){
            loadAppSetting();
        }
    }

    final Set<LoadAppCallBack> listeners = new HashSet<LoadAppCallBack>();
    public void removeAppChangeListener(LoadAppCallBack loadAppCallBack){
        listeners.remove(loadAppCallBack);
    }
    public void addAppChangeListener(LoadAppCallBack loadAppCallBack){
        listeners.add(loadAppCallBack);
    };
    public void notifyRuningStateChange(){
        for (LoadAppCallBack lc : listeners) {
            if(lc!=null){
                lc.onRuningStateChange();
            }
        }
    }
    public void loadLocalApp(){
        synchronized (this) {
            final Handler handler = new Handler(Looper.getMainLooper());
            initLocalApp();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (LoadAppCallBack lc : listeners) {
                            if (lc != null) {
                                lc.onLoadLocalAppFinish();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            checkAppChange();
        }
    }

    public void checkAppChange(){
//        PackageManager pm = context.getPackageManager();
        List<PackageInfo> packgeInfos = pm.getInstalledPackages(0);
        long firTime = 0;
        for(PackageInfo packgeInfo : packgeInfos){
            if(packgeInfo.firstInstallTime>firTime){
                firTime = packgeInfo.firstInstallTime;
            }
        }
        long lastTime = sharedPrefs.settings.getLong("LASTAPPINSTALLTIME",0);
        int allappcount = sharedPrefs.settings.getInt("ALLAPPCOUNT",0);
        File fs[] = iconPath.listFiles();
        isAppChange = firTime != lastTime||(allappcount!=0&&allappcount!=packgeInfos.size())||allAppInfos.size()==0||fs==null||fs.length<10;
        if(isAppChange){
            Log.i("CONTROL","app发生变化  start loadApp");
            loadApp();
        }else{
            Log.i("CONTROL","app没有发生变化");
        }
    }

    public void loadApp(){
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(){
            @Override
            public void run() {
                if(isLoadAppThreadRun){
                    return;
                }
                isLoadAppThreadRun = true;
                synchronized (instance) {
                    Log.i("CONTROL","-----------start loadApp "+allHMAppInfos.size());
                    List<PackageInfo> packgeInfos = pm.getInstalledPackages(0);
                    ArrayList<AppInfo> tempallAppInfos = new ArrayList<AppInfo>();
                    HashMap<String, AppInfo> tempallHMAppInfos = new HashMap<String, AppInfo>();
                    reloadRunList();
                    long firTime = 0;
                    for (PackageInfo packgeInfo : packgeInfos) {
                        String packageName = packgeInfo.packageName;
                        if(packgeInfo.firstInstallTime>firTime){
                            firTime = packgeInfo.firstInstallTime;
                        }
                        if ("com.click369.controlbp".equals(packageName) ||
                                "android".equals(packageName) ||
                                "com.android.systemui".equals(packageName)) {
                            if("com.click369.controlbp".equals(packageName)){
                                loadAppImage(packgeInfo,pm,false);
                            }
                            continue;
                        }
                        AppInfo appInfo = getOneAppInfo(packgeInfo,pm,context);
                        if (appInfo.activityCount == 0 && appInfo.serviceCount == 0 && appInfo.broadCastCount == 0) {
                            continue;
                        }
                        if (appInfo != null) {
                            tempallAppInfos.add(appInfo);
                            tempallHMAppInfos.put(appInfo.packageName, appInfo);
                            if(!allAppStateInfos.containsKey(appInfo.packageName)){
                                allAppStateInfos.put(appInfo.packageName,new AppStateInfo());
                            }
                        }
                    }
                    if(!tempallHMAppInfos.containsKey("com.tencent.mm")){
                        try {
                            PackageInfo packageInfo = pm.getPackageInfo("com.tencent.mm",PackageManager.GET_GIDS);
                            AppInfo appInfo = getOneAppInfo(packageInfo,pm,context);
                            if(appInfo!=null){
                                tempallHMAppInfos.put(appInfo.getPackageName(),appInfo);
                                tempallAppInfos.add(appInfo);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    sharedPrefs.settings.edit().putLong("LASTAPPINSTALLTIME",firTime).commit();
                    sharedPrefs.settings.edit().putInt("ALLAPPCOUNT",packgeInfos.size()).commit();
                    PinyinCompare comparent = new PinyinCompare();
                    Collections.sort(tempallAppInfos, comparent);
                    lastUpdateTime = System.currentTimeMillis();
                    AppInfo.writeArrays(tempallAppInfos, context);
                    allAppInfos.clear();
                    allAppInfos.addAll(tempallAppInfos);
                    allHMAppInfos.clear();
                    allHMAppInfos.putAll(tempallHMAppInfos);
                    isAppChange = false;
                    sharedPrefs.isPrefsChange = false;
                    if (listeners.size()>0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                for(LoadAppCallBack lac:listeners) {
                                    if(lac!=null) {
                                        lac.onLoadAppFinish();
                                    }
                                }
                            }
                        });
                    }
                }
                isLoadAppThreadRun = false;
                Log.i("CONTROL","-----------stop loadApp "+allHMAppInfos.size());
            }
        }.start();
    }

    public static void loadAppImage(PackageInfo packageInfo,PackageManager pm,boolean isReload){
        File f = new File(iconPath,packageInfo.packageName);
        try {
            if(!f.exists()||isReload){
                Drawable d = packageInfo.applicationInfo.loadIcon(pm);
                Bitmap bm = zoomDrawable(d, 100, 100);
                bm.compress(Bitmap.CompressFormat.PNG,90,new FileOutputStream(f));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public AppInfo getOneAppInfo(PackageInfo packgeInfo,PackageManager pm,Context context){
        AppInfo appInfo = null;
        String appName = "";
        try {
            appName = packgeInfo.applicationInfo.loadLabel(pm).toString();
        } catch (Throwable e) {
            try {
                appName = packgeInfo.applicationInfo.loadLabel(pm).toString();
            } catch (Throwable e1) {
                e1.printStackTrace();
            }
        }
        try {
            loadAppImage(packgeInfo,pm,false);
            // zoomDrawable(d, 90, 90)
            appInfo = new AppInfo(appName, packgeInfo.packageName, (packgeInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0, !packgeInfo.applicationInfo.enabled);
            appInfo.iconFile = new File(iconPath,packgeInfo.packageName);
            appInfo.instanllTime = packgeInfo.firstInstallTime;
            appInfo.updateTime = packgeInfo.lastUpdateTime;
            appInfo.versionCode = packgeInfo.versionCode;
            appInfo.versionName = packgeInfo.versionName;
            appInfo.uid = packgeInfo.applicationInfo.uid;
            appInfo.isServiceStop = sharedPrefs.modPrefs.getBoolean(appInfo.getPackageName() + "/service", false);
            appInfo.isBroadStop = sharedPrefs.modPrefs.getBoolean(appInfo.getPackageName() + "/broad", false);
            appInfo.isWakelockStop = sharedPrefs.modPrefs.getBoolean(appInfo.getPackageName() + "/wakelock", false);
            appInfo.isAlarmStop = sharedPrefs.modPrefs.getBoolean(appInfo.getPackageName() + "/alarm", false);

            appInfo.isBackForceStop = sharedPrefs.forceStopPrefs.getBoolean(appInfo.getPackageName() + "/backstop", false);
            appInfo.isBackMuBei = sharedPrefs.forceStopPrefs.getBoolean(appInfo.getPackageName() + "/backmubei", false);
            appInfo.isOffscForceStop = sharedPrefs.forceStopPrefs.getBoolean(appInfo.getPackageName() + "/offstop", false);
            appInfo.isOffscMuBei = sharedPrefs.forceStopPrefs.getBoolean(appInfo.getPackageName() + "/offmubei", false);
//                            appInfo.isInMuBei = sharedPrefs.muBeiPrefs.getInt(appInfo.getPackageName(), -1) == 0;
            appInfo.isHomeMuBei = sharedPrefs.forceStopPrefs.getBoolean(appInfo.getPackageName() + "/homemubei", false);
            appInfo.isHomeIdle = sharedPrefs.forceStopPrefs.getBoolean(appInfo.getPackageName() + "/idle", false);
            appInfo.isNotifyNotExit = sharedPrefs.forceStopPrefs.getBoolean(appInfo.getPackageName() + "/notifynotexit", false);

            appInfo.isAutoStart = sharedPrefs.autoStartNetPrefs.getBoolean(appInfo.getPackageName() + "/autostart", false);
            appInfo.isStopApp = sharedPrefs.autoStartNetPrefs.getBoolean(appInfo.getPackageName() + "/stopapp", false);
            appInfo.isLockApp = sharedPrefs.autoStartNetPrefs.getBoolean(appInfo.getPackageName() + "/lockapp", false);
            appInfo.isNotStop = sharedPrefs.autoStartNetPrefs.getBoolean(appInfo.getPackageName() + "/notstop", false);

            appInfo.isDozeOffsc = sharedPrefs.dozePrefs.getBoolean(appInfo.getPackageName() + "/offsc", false);
            appInfo.isDozeOnsc = sharedPrefs.dozePrefs.getBoolean(appInfo.getPackageName() + "/onsc", false);
            appInfo.isDozeOpenStop = sharedPrefs.dozePrefs.getBoolean(appInfo.getPackageName() + "/openstop", false);

            appInfo.isRecentNotClean = sharedPrefs.recentPrefs.getBoolean(appInfo.getPackageName() + "/notclean", false);
            appInfo.isRecentForceClean = sharedPrefs.recentPrefs.getBoolean(appInfo.getPackageName() + "/forceclean", false);
            appInfo.isRecentBlur = sharedPrefs.recentPrefs.getBoolean(appInfo.getPackageName() + "/blur", false);
            appInfo.isRecentNotShow = sharedPrefs.recentPrefs.getBoolean(appInfo.getPackageName() + "/notshow", false);

            appInfo.isBarLockList = sharedPrefs.uiBarPrefs.getBoolean(appInfo.getPackageName() + "/locklist", false);
            appInfo.isBarColorList = sharedPrefs.uiBarPrefs.getBoolean(appInfo.getPackageName() + "/colorlist", false);

            appInfo.isNotUnstall = sharedPrefs.pmPrefs.getBoolean(appInfo.getPackageName() + "/notunstall", false);

            appInfo.serviceDisableCount = sharedPrefs.ifwCountPrefs.getInt(appInfo.getPackageName() + "/ifwservice", 0);
            appInfo.broadCastDisableCount = sharedPrefs.ifwCountPrefs.getInt(appInfo.getPackageName() + "/ifwreceiver", 0);
            appInfo.activityDisableCount = sharedPrefs.ifwCountPrefs.getInt(appInfo.getPackageName() + "/ifwactivity", 0);
            appInfo.isADJump = sharedPrefs.adPrefs.getInt(appInfo.getPackageName() + "/ad", 0) != 0;
            appInfo.isPreventNotify = sharedPrefs.adPrefs.getBoolean(appInfo.getPackageName() + "/preventnotify", false);

            appInfo.isblackAllXp = sharedPrefs.xpBlackListPrefs.getBoolean(appInfo.getPackageName() + "/allxpblack", false);
            appInfo.isblackControlXp = sharedPrefs.xpBlackListPrefs.getBoolean(appInfo.getPackageName() + "/contorlxpblack", false);
            appInfo.isNoCheckXp = sharedPrefs.xpBlackListPrefs.getBoolean(appInfo.getPackageName() + "/nocheckxp", false);
            appInfo.isSetCanHookXp = sharedPrefs.xpBlackListPrefs.getBoolean(appInfo.getPackageName() + "/setcanhook",false);

            appInfo.isPriWifiPrevent = sharedPrefs.privacyPrefs.getBoolean(appInfo.getPackageName() + "/priwifi",false);
            appInfo.isPriMobilePrevent = sharedPrefs.privacyPrefs.getBoolean(appInfo.getPackageName() + "/primobile",false);
            appInfo.isPriSwitchOpen = sharedPrefs.privacyPrefs.getBoolean(appInfo.getPackageName() + "/priswitch",false);

            appInfo.isSetTimeStopApp = sharedPrefs.setTimeStopPrefs.getInt(appInfo.getPackageName()+"/one", 0) != 0||sharedPrefs.setTimeStopPrefs.getInt(appInfo.getPackageName()+"/long", 0) != 0;
            appInfo.isSetTimeStopOneTime = sharedPrefs.setTimeStopPrefs.contains(appInfo.getPackageName()+"/one");
            appInfo.setTimeStopAppTime = appInfo.isSetTimeStopOneTime?sharedPrefs.setTimeStopPrefs.getInt(appInfo.getPackageName()+"/one", 0):sharedPrefs.setTimeStopPrefs.getInt(appInfo.getPackageName()+"/long", 0);
            appInfo.isRunning = runLists.contains(appInfo.getPackageName());
            PackageInfo piS = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_SERVICES | PackageManager.GET_DISABLED_COMPONENTS);
            appInfo.serviceCount = piS.services != null ? piS.services.length : 0;
            PackageInfo piB = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_RECEIVERS | PackageManager.GET_DISABLED_COMPONENTS);
            appInfo.broadCastCount = piB.receivers != null ? piB.receivers.length : 0;
            PackageInfo piA = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
            appInfo.activityCount = piA.activities != null ? piA.activities.length : 0;
        } catch (Throwable e) {
            pm = context.getPackageManager();
        }
        return appInfo;
    }
    public void loadAppSetting(){
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(){
            @Override
            public void run() {
                if(isLoadAppSettingThreadRun||(!sharedPrefs.isPrefsChange&&!isIceAppChange)){
                    if(!sharedPrefs.isPrefsChange){
                        try {
                            for(LoadAppCallBack lac:listeners) {
                                if(lac!=null) {
                                    lac.onLoadAppSettingFinish();
                                }
                            }
                        }catch (Throwable e){
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                isLoadAppSettingThreadRun = true;
                synchronized (instance) {
                    Log.i("CONTROL","loadAppSetting start");
                    try {
                        for (int i = 0; i < allAppInfos.size(); i++) {
                            AppInfo ai = allAppInfos.get(i);
                            ai.isServiceStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/service", false);
                            ai.isBroadStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/broad", false);
                            ai.isWakelockStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/wakelock", false);
                            ai.isAlarmStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/alarm", false);

                            ai.isBackForceStop = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/backstop", false);
                            ai.isBackMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/backmubei", false);
                            ai.isOffscForceStop = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/offstop", false);
                            ai.isOffscMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/offmubei", false);
//                            ai.isInMuBei = sharedPrefs.muBeiPrefs.getInt(ai.getPackageName(), -1) == 0;
                            ai.isHomeMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/homemubei", false);
                            ai.isHomeIdle = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/idle", false);
                            ai.isNotifyNotExit = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/notifynotexit", false);

                            ai.isAutoStart = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/autostart", false);
                            ai.isStopApp = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/stopapp", false);
                            ai.isLockApp = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/lockapp", false);
                            ai.isNotStop = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/notstop", false);

                            ai.isDozeOffsc = sharedPrefs.dozePrefs.getBoolean(ai.getPackageName() + "/offsc", false);
                            ai.isDozeOnsc = sharedPrefs.dozePrefs.getBoolean(ai.getPackageName() + "/onsc", false);
                            ai.isDozeOpenStop = sharedPrefs.dozePrefs.getBoolean(ai.getPackageName() + "/openstop", false);

                            ai.isRecentNotClean = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/notclean", false);
                            ai.isRecentForceClean = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/forceclean", false);
                            ai.isRecentBlur = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/blur", false);
                            ai.isRecentNotShow = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/notshow", false);

                            ai.isBarLockList = sharedPrefs.uiBarPrefs.getBoolean(ai.getPackageName() + "/locklist", false);
                            ai.isBarColorList = sharedPrefs.uiBarPrefs.getBoolean(ai.getPackageName() + "/colorlist", false);

                            ai.isNotUnstall = sharedPrefs.pmPrefs.getBoolean(ai.getPackageName() + "/notunstall", false);

                            ai.serviceDisableCount = sharedPrefs.ifwCountPrefs.getInt(ai.getPackageName() + "/ifwservice", 0);
                            ai.broadCastDisableCount = sharedPrefs.ifwCountPrefs.getInt(ai.getPackageName() + "/ifwreceiver", 0);
                            ai.activityDisableCount = sharedPrefs.ifwCountPrefs.getInt(ai.getPackageName() + "/ifwactivity", 0);
                            ai.isADJump = sharedPrefs.adPrefs.getInt(ai.getPackageName() + "/ad", 0) != 0;
                            ai.isPreventNotify = sharedPrefs.adPrefs.getBoolean(ai.getPackageName() + "/preventnotify", false);

                            ai.isblackAllXp = sharedPrefs.xpBlackListPrefs.getBoolean(ai.getPackageName() + "/allxpblack", false);
                            ai.isblackControlXp = sharedPrefs.xpBlackListPrefs.getBoolean(ai.getPackageName() + "/contorlxpblack", false);
                            ai.isNoCheckXp = sharedPrefs.xpBlackListPrefs.getBoolean(ai.getPackageName() + "/nocheckxp", false);
                            ai.isSetCanHookXp = sharedPrefs.xpBlackListPrefs.getBoolean(ai.getPackageName() + "/setcanhook",false);

                            ai.isPriWifiPrevent = sharedPrefs.privacyPrefs.getBoolean(ai.getPackageName() + "/priwifi",false);
                            ai.isPriMobilePrevent = sharedPrefs.privacyPrefs.getBoolean(ai.getPackageName() + "/primobile",false);
                            ai.isPriSwitchOpen = sharedPrefs.privacyPrefs.getBoolean(ai.getPackageName() + "/priswitch",false);

                            ai.isSetTimeStopApp = sharedPrefs.setTimeStopPrefs.getInt(ai.getPackageName()+"/one", 0) != 0||sharedPrefs.setTimeStopPrefs.getInt(ai.getPackageName()+"/long", 0) != 0;
                            ai.isSetTimeStopOneTime = sharedPrefs.setTimeStopPrefs.contains(ai.getPackageName()+"/one");
                            ai.setTimeStopAppTime = ai.isSetTimeStopOneTime?sharedPrefs.setTimeStopPrefs.getInt(ai.getPackageName()+"/one", 0):sharedPrefs.setTimeStopPrefs.getInt(ai.getPackageName()+"/long", 0);
                            ai.isRunning = runLists.contains(ai.getPackageName());
                            try {
                                ai.isDisable = !pm.getPackageInfo(ai.getPackageName(), PackageManager.GET_ACTIVITIES).applicationInfo.enabled;
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            allHMAppInfos.put(ai.packageName,ai);
                            if(!allAppStateInfos.containsKey(ai.packageName)){
                                allAppStateInfos.put(ai.packageName,new AppStateInfo());
                            }
                        }
                        AppInfo.writeArrays(allAppInfos, context);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    sharedPrefs.isPrefsChange = false;
                    isIceAppChange = false;
                    if (listeners.size()>0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                for(LoadAppCallBack lac:listeners){
                                    if(lac!=null) {
                                        lac.onLoadAppSettingFinish();
                                    }
                                }
                            }
                        });
                    }
                    isLoadAppSettingThreadRun = false;
                    Log.i("CONTROL","loadAppSetting end");
                }
            }
        }.start();
    }

    public static Bitmap zoomDrawable(Drawable drawable, int w, int h)
    {
        try {
            int width = drawable.getIntrinsicWidth();
            int height= drawable.getIntrinsicHeight();
            Bitmap oldbmp = drawableToBitmap(drawable); // drawable 转换成 bitmap
            if (oldbmp==null||width<=0||height<0){
                Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                return bitmap;
            }
            Matrix matrix = new Matrix();   // 创建操作图片用的 Matrix 对象
            float scaleWidth = ((float)w / width);   // 计算缩放比例
            float scaleHeight = ((float)h / height);
            matrix.postScale(scaleWidth, scaleHeight);         // 设置缩放比例
            Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
            return newbmp;       // 把 bitmap 转换成 drawable 并返回
        }catch (Exception e){
            return  null;
        }
    }
    static Bitmap drawableToBitmap(Drawable drawable) // drawable 转换成 bitmap
    {
        try {
            int width = drawable.getIntrinsicWidth();   // 取 drawable 的长宽
            int height = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;         // 取 drawable 的颜色格式
            if (width<=0||height<=0){
//            Bitmap bitmap = Bitmap.createBitmap(100, 100, config);
                return null;
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);     // 建立对应 bitmap
            Canvas canvas = new Canvas(bitmap);         // 建立对应 bitmap 的画布
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);      // 把 drawable 内容画到画布中
            return bitmap;
        }catch (Exception e){
            return  null;
        }
    }
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();

            final Rect rect = new Rect(0, 0, bitmap.getWidth(),bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),  bitmap.getHeight()));
            final float roundPx = bitmap.getWidth()>bitmap.getHeight()?bitmap.getWidth()/2:bitmap.getHeight()/2;
            paint.setAntiAlias(true);
//            canvas.drawRGB(0,0,0);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),  bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }


    public interface LoadAppCallBack{
//        void onInitAppFinish();
        void onLoadLocalAppFinish();
        void onLoadAppFinish();
        void onLoadAppSettingFinish();
        void onRuningStateChange();
    }
}

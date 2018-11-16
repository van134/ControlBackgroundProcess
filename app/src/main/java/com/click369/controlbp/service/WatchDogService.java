package com.click369.controlbp.service;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.click369.controlbp.activity.AppConfigActivity;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.RunningActivity;
import com.click369.controlbp.activity.ShowDialogActivity;
import com.click369.controlbp.activity.UIControlFragment;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.receiver.AddAppReceiver;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtilNoBackData;


import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by asus on 2017/6/3.
 */
public class WatchDogService extends Service {
    private HomeKeyReceiver hkr;
    private updateAppReceiver apr;
    private Handler handler = new Handler();
    private Handler handler1 = new Handler();

//    public static Set<String> launcherPkgs = new HashSet<String>();
    public static ArrayList<String> imePkgs = new ArrayList<String>();
    public static HashSet<String> backKillApp = new HashSet<String>();
    public static HashMap<String,Long> delayBackKillApp = new HashMap<String,Long>();
    public static HashSet<String> backMuBeiApp = new HashSet<String>();
    public static HashMap<String,Long> delayBackMuBeiApp = new HashMap<String,Long>();

    public static HashMap<String,Integer> setTimeStopApp = new HashMap<String,Integer>();
//    public static LinkedHashMap<String,Long> stopAppStartTime = new LinkedHashMap<String,Long>();
    public static ArrayList<String> stopAppName = new ArrayList<String>();
    public static boolean stopAppStartFlag = true;//是否可以开始进入定时强退  进入home或最近任务
//    public static HashMap<String,Long> cancelStopAppName = new HashMap<String,Long>();//取消强退的应用
    public static Set<String> setTimeStopkeys = new HashSet<String>();//永久性定时强退

    //临时存储
    public static HashMap<String,Long> delayHomeMuBeiApp = new HashMap<String,Long>();
    public static HashMap<String,Long> delayHomeIdleApp = new HashMap<String,Long>();
    public static HashSet<String> homeMuBeiApp = new HashSet<String>();
    public static HashSet<String> homeIdleApp = new HashSet<String>();

    public static HashSet<String> homeKeyPressApp = new HashSet<String>();
    public HashSet<String> openPkgs = new HashSet<String>();
//    public static ArrayList<String> wakeLockInfo = new ArrayList<String>();

    public static HashSet<String> notifs = new HashSet<>();
    public static HashSet<String> notifyNotColseList = new HashSet<String>();
    public static HashSet<String> notifyNotMuBeiList = new HashSet<String>();
    public static HashSet<String> notifyNotIdleList = new HashSet<String>();
    public static boolean isClockOpen = false;
    public static boolean isMusicOpen = false;
    public static long lastNotifyTime = 0;
    //常驻内存
    public static HashSet<String> notStops = new HashSet<String>();
    private static HashSet<String> backForceStops = new HashSet<String>();
    private HashSet<String> backForceMuBeis = new HashSet<String>();
    private static HashSet<String> offScreenStops = new HashSet<String>();
    private HashSet<String> offScreenMuBeis = new HashSet<String>();
//    private HashSet<String> homeKeyStops = new HashSet<String>();
    private HashSet<String> homeKeyMuBeis = new HashSet<String>();
    private HashSet<String> homeKeyIdles = new HashSet<String>();
    private HashSet<String> notifyNotExitList = new HashSet<String>();
    public HashSet<String> autoStartList = new HashSet<String>();
    public HashSet<String> lockAppList = new HashSet<String>();
    public HashSet<String> forceStopAppList = new HashSet<String>();

    public HashSet<String> removeAppList = new HashSet<String>();
    public static ArrayList<String> newInstallAppList = new ArrayList<String>();
    //要移除的最近任务卡片 有可能是打开了最近任务所以暂时没有移除
    public static HashSet<String> removeRecents = new HashSet<String>();
    public static HashSet<String> iceButOpenInfos = new HashSet<String>();

    public static String openPkgName = "";//当前打开的 比较准确
    public static String nowPkgName = "";//当前打开的 不一定准确
//    public static String lastPkgName = "";//上一个打开的
    private HashSet<String> oneTimeOpenPkgs = new HashSet<String>();//从桌面开始到回到桌面整个过程打开的应用
    private HashMap<String,String> openLinkPkgs = new HashMap<String,String>();//链式启动

    public static int delayOffTime = 0;
    public static int delayBackTime = 0;
    public static int delayHomeTime = 0;
    public static int delayCleanTime = 0;
    public static boolean  isOffClean = false;


    public static boolean  isAtuoRemoveIce = true;
    public static boolean  isAtuoStopIce = false;
    public static boolean  isSetTimeStopByZW = false;//取消强退是否需要指纹
    public static boolean  isSetTimeStopByPWD = false;//取消强退是否需要密码
    public static boolean  isAtuoOffScIce = false;
    public static boolean isNotNeedAccessibilityService = true;
    public static boolean isHomeClick = false;
    public static boolean isOpenCamera = false;
    public static boolean isRecentClick = false;
    public static boolean isKillRun = false;
    public static boolean isExitRemoveRecent = true;

    public  static boolean isCharging = false;
    public  static boolean isHookOff = false;
//    private HashSet<String> addADeadServicePkgs = new HashSet<String>();
    public static boolean  isSaveBackLog = false;

    public static boolean  isCameraMode = false;
    public static boolean  isLockUnlockCPU = false;
    public static boolean  isCameraModeOpen = false;
    public static boolean  isShowActInfo = false;
//    public static boolean  isCPU1 = false;
//    public static boolean  isCPU2 = false;
//    public static boolean  isCPU3 = false;
//    public static boolean  isCPU4 = false;
//    public static boolean  isCPU5 = false;
//    public static boolean  isCPU6 = false;
//    public static boolean  isCPU7 = false;
    public static boolean isCPUS[] = new boolean[8];
    public static int cpuNum = 8;
    public StringBuilder backLog = new StringBuilder();

    MyDozeService myDozeService;
    AppStartService appStartService;
    AdService adService;
    SharedPreferences settings;
    SharedPreferences forceStopPrefs;
    SharedPreferences autoStartPrefs;
    SharedPreferences recentPrefs;
    SharedPreferences muBeiPrefs;
    SharedPreferences muControlPrefs;
    SharedPreferences cpuPrefs;
    public static String homePkg = "";
    public static  long watchDogstartTime = 0;
    @Override
    public void onCreate() {
        isKillRun = true;
        watchDogstartTime = System.currentTimeMillis();
        Log.e("DOZE", "WatchDogService启动后台进程监听服务");
        forceStopPrefs = SharedPrefsUtil.getPreferences(getApplication(),Common.PREFS_FORCESTOPNAME);
        autoStartPrefs = SharedPrefsUtil.getPreferences(getApplication(),Common.PREFS_AUTOSTARTNAME);
        muBeiPrefs = SharedPrefsUtil.getPreferences(getApplication(),Common.IPREFS_MUBEILIST);
        muControlPrefs = SharedPrefsUtil.getPreferences(getApplication(),Common.PREFS_SETTINGNAME);
        recentPrefs = SharedPrefsUtil.getPreferences(getApplication(),Common.IPREFS_RECENTLIST);
        settings = SharedPrefsUtil.getPreferences(this,Common.PREFS_APPSETTINGS);//getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        cpuPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_SETCPU);//getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        muBeiPrefs.edit().clear().commit();
        IntentFilter ifliter = new IntentFilter();
        ifliter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        ifliter.addAction("com.click369.control.backkey");
        ifliter.addAction("com.click369.control.heart");
        ifliter.addAction("com.click369.control.accessclose");
        ifliter.addAction("com.click369.control.offsccloseapp");
        ifliter.addAction("com.click369.control.offcleancache");
        ifliter.addAction("com.click369.control.keylistener");
        ifliter.addAction("com.click369.control.loadapplist");
        ifliter.addAction("com.click369.control.startruuning");
        ifliter.addAction("com.click369.control.changerencetbysystemui");
        ifliter.addAction("com.click369.control.notify");
        ifliter.addAction("com.click369.control.test");
        ifliter.addAction("com.click369.control.exit");
        ifliter.addAction("com.click369.control.home");
        ifliter.addAction("com.click369.control.settimestopapp");
        ifliter.addAction("com.click369.control.canceltimestopapp");
        ifliter.addAction("com.click369.control.removeapp");
        ifliter.addAction("com.click369.control.addapp");
        ifliter.addAction("com.click369.control.appconfigclose");
        ifliter.addAction("com.click369.control.openmain");
        ifliter.addAction(Intent.ACTION_SCREEN_OFF);
        ifliter.addAction(Intent.ACTION_SCREEN_ON);
        ifliter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        ifliter.addAction(Intent.ACTION_POWER_CONNECTED);
        hkr = new HomeKeyReceiver();
        apr = new updateAppReceiver();
        this.registerReceiver(hkr, ifliter);
        IntentFilter ifliter1 = new IntentFilter();
        ifliter1.addAction(Intent.ACTION_PACKAGE_ADDED);
        ifliter1.addAction(Intent.ACTION_PACKAGE_REMOVED);
        ifliter1.addAction(Intent.ACTION_PACKAGE_REPLACED);
        ifliter1.addDataScheme("package");
        this.registerReceiver(apr, ifliter1);
        if (settings.getBoolean(Common.ALLSWITCH_SEVEN,true)) {
            myDozeService = new MyDozeService(this);
        }
        if (settings.getBoolean(Common.ALLSWITCH_FIVE,true)||
                settings.getBoolean(Common.ALLSWITCH_FOUR,true)) {
            appStartService = new AppStartService(this);
        }
        if (settings.getBoolean(Common.ALLSWITCH_NINE,true)) {
            adService = new AdService(this);
        }
        isCameraMode = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CAMERAMODE,false);
        isLockUnlockCPU = cpuPrefs.getBoolean(Common.PREFS_SETCPU_LOCKUNLOCK, false);
        isCPUS[0] = true;
        isCPUS[1] = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU1, true);
        isCPUS[2] = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU2, true);
        isCPUS[3] = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU3, true);
        isCPUS[4] = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU4, true);
        isCPUS[5] = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU5, true);
        isCPUS[6] = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU6, true);
        isCPUS[7] = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU7, true);
        new Thread(){
            @Override
            public void run() {
                try {
                    startIdleState();
                    Thread.sleep(1000*10);
                }catch (Exception e){
                }
                boolean isOpen = SELinuxUtil.isSELOpen();
                if(!settings.getBoolean(Common.PREFS_SETTING_SELOPEN,false)&&isOpen){
                    SELinuxUtil.closeSEL();
                }else if(!isOpen&&settings.contains(Common.PREFS_SETTING_SELOPEN)&&settings.getBoolean(Common.PREFS_SETTING_SELOPEN,false)){
                    SELinuxUtil.openSEL();
                }
                try {
                    if(cpuPrefs.getBoolean(Common.PREFS_SETCPU_AUTOSTART, false)) {
                        Thread.sleep(1000 * 10);
                        File file1 = new File(FileUtil.FILEPATH, "unlock_lowbatter_core");
                        File file2 = new File(FileUtil.FILEPATH, "lock_lowbatter_core");
                        if (!file1.exists() || !file2.exists()) {
                            FileUtil.init();
                            FileUtil.copyAssets(WatchDogService.this, "unlock_lowbatter_core", FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core");
                            FileUtil.copyAssets(WatchDogService.this, "lock_lowbatter_core", FileUtil.FILEPATH + File.separator + "lock_lowbatter_core");
                        }
                        String s = isLockUnlockCPU ? "sh " + FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core" : "sh " + FileUtil.FILEPATH + File.separator + "lock_lowbatter_core";
                        ShellUtilNoBackData.execCommand(s);
                        Thread.sleep(1000*20);
                        cpuNum = cpuPrefs.getInt(Common.PREFS_SETCPU_NUMBER,8);
//                        String names[] = {Common.PREFS_SETCPU_CPU0, Common.PREFS_SETCPU_CPU1, Common.PREFS_SETCPU_CPU2, Common.PREFS_SETCPU_CPU3,
//                                Common.PREFS_SETCPU_CPU4, Common.PREFS_SETCPU_CPU5, Common.PREFS_SETCPU_CPU6, Common.PREFS_SETCPU_CPU7};
                        for (int i = 0; i < cpuNum; i++) {
                            Thread.sleep(600);
                            try {
//                                boolean isCPU = cpuPrefs.getBoolean(names[i], true);
                                ShellUtilNoBackData.execCommand("echo -n " + (isCPUS[i] ? "1" : "0") + " > /sys/devices/system/cpu/cpu" + i + "/online");
                            }catch (Exception e){
                            }
                        }
                    }
                }catch (Exception e){
                }
            }
        }.start();
        super.onCreate();
    }

    private void startIdleState(){
        try {
            String runing = PackageUtil.getRunngingApp(WatchDogService.this);
            if (runing!=null&&runing.length()>20) {
                HashSet<String> pkgs = new HashSet<String>();
                String pkgs1[] = runing.split("\n");
                for (String s : pkgs1) {
                    if (forceStopPrefs.getBoolean(s + "/idle", false)) {
                        pkgs.add(s);
                    }
                }
                Intent intent = new Intent("com.click369.control.uss.setappidle");
                intent.putExtra("idle", true);
                intent.putExtra("pkgs", pkgs);
                sendBroadcast(intent);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("CONTROL","watchdog 加载数据");
        isKillRun = true;
        homePkg = WatchDogService.getDefaultHome(this);
        isNotNeedAccessibilityService = settings.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true);
        delayBackTime = settings.getInt(Common.PREFS_SETTING_BACKDELAYTIME,0);
        delayHomeTime = settings.getInt(Common.PREFS_SETTING_HOMEDELAYTIME,0);
        delayOffTime = settings.getInt(Common.PREFS_SETTING_OFFDELAYTIME,0);
        delayCleanTime = settings.getInt(Common.PREFS_SETTING_OTHER_CLEANDELAYTIME,0);
        isSaveBackLog = settings.getBoolean(Common.PREFS_SETTING_BACKLOGOPEN,false);
        isOffClean = settings.getBoolean(Common.PREFS_SETTING_OTHER_ISCLEAN,false);
        isAtuoRemoveIce = settings.getBoolean(Common.PREFS_SETTING_ICEBACKICE,true);
        isAtuoStopIce = settings.getBoolean(Common.PREFS_SETTING_ICESTOPICE,false);
        isAtuoOffScIce = settings.getBoolean(Common.PREFS_SETTING_ICEOFFICE,false);
        isExitRemoveRecent = settings.getBoolean(Common.PREFS_SETTING_EXITREMOVERECENT,true);
        isSetTimeStopByZW = settings.getBoolean(Common.PREFS_SETTING_SETTIMESTOPMODE,false);
        isSetTimeStopByPWD = settings.getBoolean(Common.PREFS_SETTING_SETTIMESTOPPWDMODE,false);
//        AppStartService.isStrongAutoStart = settings.getBoolean(Common.PREFS_SETTING_STRONGAUTOSTART,false);
//        Log.i("CONTROL","isSetTimeStopByPWD "+isSetTimeStopByPWD);
        AppStartService.isOffScLockApp = settings.getBoolean(Common.PREFS_SETTING_OFFSCLOCK,true);
        removeImp();
        if (settings.getBoolean(Common.ALLSWITCH_FIVE,true)) {
            Set<String> autoKeys = autoStartPrefs.getAll().keySet();
            autoStartList.clear();
            forceStopAppList.clear();
            lockAppList.clear();
            notStops.clear();
            for (String key : autoKeys) {
                if (key.endsWith("/autostart") && autoStartPrefs.getBoolean(key, false)) {
                    autoStartList.add(key.replace("/autostart", ""));
                }else if(key.endsWith("/stopapp") && autoStartPrefs.getBoolean(key, false)){
                    forceStopAppList.add(key.replace("/stopapp", ""));
                }else if(key.endsWith("/lockapp") && autoStartPrefs.getBoolean(key, false)){
                    lockAppList.add(key.replace("/lockapp", ""));
                }else if (key.endsWith("/notstop") && autoStartPrefs.getBoolean(key, false)) {
                    notStops.add(key.replace("/notstop", ""));
                }else if(key.endsWith("/lockok")){
                    autoStartPrefs.edit().remove(key).commit();
                }
            }
        }
        if (settings.getBoolean(Common.ALLSWITCH_SEVEN,true)){
            myDozeService.loadWhilteList();
        }
        if (settings.getBoolean(Common.ALLSWITCH_TWO,true)) {
            Set<String> keys = forceStopPrefs.getAll().keySet();
            backForceStops.clear();
            backForceMuBeis.clear();
            offScreenStops.clear();
            offScreenMuBeis.clear();
            homeKeyIdles.clear();
            homeKeyMuBeis.clear();
            notifyNotExitList.clear();
            SharedPreferences.Editor mubeiEditor = muBeiPrefs.edit();
            int defaultValue = 0;
            for (String key : keys) {
                if (key.endsWith("/backstop") && forceStopPrefs.getBoolean(key, false)) {
                    backForceStops.add(key.replace("/backstop", ""));
                }else if (key.endsWith("/backmubei") && forceStopPrefs.getBoolean(key, false)) {
                    String pkg = key.replace("/backmubei", "");
                    backForceMuBeis.add(pkg);
                    if (!muBeiPrefs.contains(pkg)) {
                        mubeiEditor.putInt(pkg, defaultValue).commit();
                    }
                } else if (key.endsWith("/offstop") && forceStopPrefs.getBoolean(key, false)) {
                    offScreenStops.add(key.replace("/offstop", ""));
                } else if (key.endsWith("/offmubei") && forceStopPrefs.getBoolean(key, false)) {
                    String pkg = key.replace("/offmubei", "");
                    offScreenMuBeis.add(pkg);
                    if (!muBeiPrefs.contains(pkg)) {
                        mubeiEditor.putInt(pkg , defaultValue).commit();
                    }
                }
//                else if (key.endsWith("/homestop") && forceStopPrefs.getBoolean(key, false)) {
//                    homeKeyStops.add(key.replace("/homestop", ""));
//                }
                else if (key.endsWith("/homemubei") && forceStopPrefs.getBoolean(key, false)) {
                    String pkg = key.replace("/homemubei", "");
                    homeKeyMuBeis.add(pkg);
                    if (!muBeiPrefs.contains(pkg)) {
                        mubeiEditor.putInt(pkg, defaultValue).commit();
                    }
                }else if (key.endsWith("/idle") && forceStopPrefs.getBoolean(key, false)) {
                    String pkg = key.replace("/idle", "");
                    homeKeyIdles.add(pkg);
                } else if (key.endsWith("/notifynotexit") && forceStopPrefs.getBoolean(key, false)) {
                    notifyNotExitList.add(key.replace("/notifynotexit", ""));
                }
            }
        }

        //定时关闭
        SharedPreferences setTimeStop = SharedPrefsUtil.getPreferences(this, Common.PREFS_SETTIMESTOP);
        setTimeStopkeys.clear();
        setTimeStopkeys.addAll(setTimeStop.getAll().keySet());
        for (String key : setTimeStopkeys) {
            setTimeStopApp.put(key,setTimeStop.getInt(key,999999));
        }
//        launcherPkgs.clear();
        imePkgs.clear();
//        launcherPkgs.addAll(getLauncherPackageName(this));
        imePkgs.addAll(getInputPackageName(this));

        settings.edit().remove("homeapk").commit();
        autoStartPrefs.edit().remove("homeapk").commit();
        settings.edit().putString("nowhomeapk",WatchDogService.getDefaultHome(this)).commit();
        autoStartPrefs.edit().putString("nowhomeapk",WatchDogService.getDefaultHome(this)).commit();

        if(!RoundedCornerService.isRoundRun){
            UIControlFragment.startRound(SharedPrefsUtil.getPreferences(this,Common.PREFS_UIBARLIST),this);
        }

        if (!NotificationService.isNotifyRunning&&!isNotNeedAccessibilityService&&settings.getBoolean(Common.ALLSWITCH_TWO,true)){
            new Thread() {
                @Override
                public void run() {
                    OpenCloseUtil.startNotifyListener(WatchDogService.this);
                }
            }.start();
        }else if(isNotNeedAccessibilityService&&NotificationService.isNotifyRunning){
            new Thread() {
                @Override
                public void run() {
                    OpenCloseUtil.stopNotifyListener(WatchDogService.this);
                }
            }.start();
        }
        if (!isNotNeedAccessibilityService&&!NewWatchDogService.isOpenNewDogService) {
            new Thread() {
                @Override
                public void run() {
                    OpenCloseUtil.closeOpenAccessibilitySettingsOn(WatchDogService.this, true);
                }
            }.start();
        }else if(isNotNeedAccessibilityService&&NewWatchDogService.isOpenNewDogService){
            new Thread() {
                @Override
                public void run() {
                    OpenCloseUtil.closeOpenAccessibilitySettingsOn(WatchDogService.this, false);
                }
            }.start();
        }
        return super.onStartCommand(intent, Service.START_FLAG_RETRY, startId);
    }

    private void removeImp(){
        muBeiPrefs.edit().remove(homePkg).commit();
        muBeiPrefs.edit().remove("com.fkzhang.wechatxposed").commit();

        SharedPreferences.Editor autoEdit = autoStartPrefs.edit();
        autoEdit.remove(homePkg+"/autostart");
        autoEdit.remove(homePkg+"/stopapp");
        autoEdit.remove(homePkg+"/lockok");
        autoEdit.commit();

        SharedPreferences.Editor forceEdit = muControlPrefs.edit();
        forceEdit.remove(homePkg+"/backstop");
        forceEdit.remove(homePkg+"/backmubei");
        forceEdit.remove(homePkg+"/offmubei");
        forceEdit.remove(homePkg+"/homemubei");
        forceEdit.remove("com.fkzhang.wechatxposed/homemubei");
        forceEdit.remove("com.fkzhang.wechatxposed/offmubei");
        forceEdit.remove("com.fkzhang.wechatxposed/backmubei");
        forceEdit.remove("com.fkzhang.wechatxposed/backstop");
        forceEdit.commit();

        SharedPreferences.Editor cEdit = muControlPrefs.edit();
        cEdit.remove(homePkg+"/service");
//        cEdit.remove(homePkg+"/broad");
//        cEdit.remove(homePkg+"/wakelock");
//        cEdit.remove(homePkg+"/alarm");
        cEdit.remove("com.fkzhang.wechatxposed/service");
        cEdit.remove("com.fkzhang.wechatxposed/broad");
        cEdit.remove("com.fkzhang.wechatxposed/wakelock");
        cEdit.remove("com.fkzhang.wechatxposed/alarm");
        cEdit.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("DOZE", "结束后台进程监听服务");
        if(hkr!=null){
            this.unregisterReceiver(hkr);
        }
        if(apr!=null){
            this.unregisterReceiver(apr);
        }
        if(myDozeService !=null){
            myDozeService.destory();
        }
        if(appStartService !=null){
            appStartService.destory();
        }
        if(adService !=null){
            adService.destory();
        }
        isKillRun = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
//    long lastExitTime = 0;
//    boolean isExitRun = false;
    Runnable homemubei = new Runnable() {
        @Override
        public void run() {
            homeStopMubeiApp();
        }
    };
    Runnable homeidle = new Runnable() {
        @Override
        public void run() {
            homeIdleApp();
        }
    };
    Runnable exit = new Runnable() {
        @Override
        public void run() {
            backStopMubeiApp();
        }
    };
    private void backStopMubeiApp(){
        if (delayBackKillApp.size()>0) {
            if (notifyNotExitList.size() > 0 && notifs.size() > 0) {
                for (String p : notifs) {
                    if (notifyNotExitList.contains(p)&&delayBackKillApp.containsKey(p)&&(System.currentTimeMillis()-delayBackKillApp.get(p)>=delayBackTime*950||(isScreenOff&&delayBackTime<7))) {
                        delayBackKillApp.remove(p);
                        notifyNotColseList.add(p);
                        Log.e("CONTROL", "返回——因通知未杀死：" + p);
                    }
                }
            }
            if (openPkgName.length() > 0 && delayBackKillApp.containsKey(openPkgName)) {
                delayBackKillApp.remove(openPkgName);
            }
            if (nowPkgName.length() > 0 && delayBackKillApp.containsKey(nowPkgName)) {
                delayBackKillApp.remove(nowPkgName);
            }
            Set<String> dels = new HashSet<>(delayBackKillApp.keySet());
            for (String p : dels) {
                if (System.currentTimeMillis()-delayBackKillApp.get(p)>=delayBackTime*950||(isScreenOff&&delayBackTime<7)) {
                    Log.e("CONTROL", "返回——杀死：" + p);
                    delayBackKillApp.remove(p);
                    XposedStopApp.stopApk(p, WatchDogService.this);
                    WatchDogService.sendRemoveRecent(p,WatchDogService.this);

//                    setTimeStopApp.remove(p);
//                    stopAppStartTime.remove(p);
//                    Log.e("CONTROL", "isAtuoBackIce：" + isAtuoBackIce+"   "+iceButOpenInfos.contains(p));
                    if (isAtuoStopIce&&iceButOpenInfos.contains(p)&&!notifs.contains(p)){
                        final List<String> lists = new ArrayList<String>();
                        lists.add("pm disable " + p);
                        iceButOpenInfos.remove(p);
                        new Thread(){
                            @Override
                            public void run() {
                                ShellUtilNoBackData.execCommand(lists);
                            }
                        }.start();
                    }
                }
            }
        }
        if (delayBackMuBeiApp.size()>0){
            if (openPkgName.length() > 0 && delayBackMuBeiApp.containsKey(openPkgName)) {
                delayBackMuBeiApp.remove(openPkgName);
            }
            if (nowPkgName.length() > 0 && delayBackMuBeiApp.containsKey(nowPkgName)) {
                delayBackMuBeiApp.remove(nowPkgName);
            }
            Set<String> mubeis = new HashSet<>(delayBackMuBeiApp.keySet());
            for(String p:mubeis){
                if (System.currentTimeMillis()-delayBackMuBeiApp.get(p)>=delayBackTime*950||(isScreenOff&&delayBackTime<7)) {
                    delayBackMuBeiApp.remove(p);
                    startMuBei(p);
                }
            }
        }
    }
    private void homeStopMubeiApp(){
        if (delayHomeMuBeiApp.size()>0){
            if (openPkgName.length() > 0 && delayHomeMuBeiApp.containsKey(openPkgName)) {
                delayHomeMuBeiApp.remove(openPkgName);
            }
            if (nowPkgName.length() > 0 && delayHomeMuBeiApp.containsKey(nowPkgName)) {
                delayHomeMuBeiApp.remove(nowPkgName);
            }
            Set<String> mubeis = new HashSet<>(delayHomeMuBeiApp.keySet());
            for(String p:mubeis){
                if (System.currentTimeMillis()-delayHomeMuBeiApp.get(p)>=delayHomeTime*950||(isScreenOff&&delayBackTime<7)) {
                    delayHomeMuBeiApp.remove(p);
                    startMuBei(p);
                }
            }
        }
    }
    private void homeIdleApp(){
        if (delayHomeIdleApp.size()>0){
            if (openPkgName.length() > 0 && delayHomeIdleApp.containsKey(openPkgName)) {
                delayHomeIdleApp.remove(openPkgName);
            }
            if (nowPkgName.length() > 0 && delayHomeIdleApp.containsKey(nowPkgName)) {
                delayHomeIdleApp.remove(nowPkgName);
            }
            Set<String> mubeis = new HashSet<>(delayHomeIdleApp.keySet());
            for(String p:mubeis){
                if (System.currentTimeMillis()-delayHomeIdleApp.get(p)>=delayHomeTime*950||(isScreenOff&&delayBackTime<7)) {
                    delayHomeIdleApp.remove(p);
                    startIdle(p);
                }
            }
        }
    }

    static final String SYSTEM_REASON = "reason";
    static final String SYSTEM_HOME_KEY = "homekey";
    static final String SYSTEM_RECENT_KEY = "recent";
    static final String SYSTEM_RECENTAPPS_KEY = "recentapps";
    private String ps = "";
    long offScTime = 0;
    boolean isScreenOff = false;
    public  static long lastPressedHome = 0;
//    public  static long lastInHome = 0;
    public  static long lastCleanTime = 0;
    public  static long lastHomeClick = 0,lastRecentClick = 0;
    class HomeKeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)||
                    "com.click369.control.keylistener".equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
//                Log.i("CONTROL", reason+" 键点击...."+action+"  "+intent);
                if (SYSTEM_HOME_KEY.equals(reason)||reason==null) {
                    lastHomeClick = System.currentTimeMillis();
                    if(!isRecentClick){
                        isHomeClick = true;
                        lastPressedHome = System.currentTimeMillis();
                    }else if(isRecentClick&&Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)){
                        isHomeClick = false;
                        isRecentClick = false;
                    }
                    stopAppStartFlag = true;
//                    if(!isRecentClick){
//                        handler.removeCallbacks(goHome);
//                        handler.postDelayed(goHome,200);
//                    }
                }else if(SYSTEM_RECENT_KEY.equals(reason)||SYSTEM_RECENTAPPS_KEY.equals(reason)){
                    lastRecentClick = System.currentTimeMillis();
                    isRecentClick = true;
                    stopAppStartFlag = true;
                    homeKeyPressApp.add(openPkgName);
                    backKillApp.remove(openPkgName);
                    backMuBeiApp.remove(openPkgName);
                    backKillApp.removeAll(oneTimeOpenPkgs);
                    backMuBeiApp.removeAll(oneTimeOpenPkgs);
                    oneTimeOpenPkgs.clear();
//                    openPkgName = "com.android.systemui";
//                    nowPkgName = "com.android.systemui";
                }
            }else if (Intent.ACTION_SCREEN_OFF.equals(action)&&!isHookOff) {
                isScreenOff = true;
                handler.removeCallbacks(exit);
                handler.removeCallbacks(homemubei);
                if(delayBackKillApp.size()>0||delayBackMuBeiApp.size()>0){
                    if(delayBackTime<7){
                        backStopMubeiApp();
                    }else{
                        setAlarm("com.click369.control.exit",delayBackTime);
                    }
                }
                if(delayHomeMuBeiApp.size()>0){
//                    setAlarm("com.click369.control.home",delayHomeTime);
                    if(delayHomeTime<7){
                        homeStopMubeiApp();
                    }else{
                        setAlarm("com.click369.control.home",delayHomeTime);
                    }
                }
                if (myDozeService !=null) {
                    myDozeService.screenOff();
                }
                if (lockAppList.size()>0){
                    for(String s:lockAppList){
                        if (autoStartPrefs.contains(s+"/lockok")){
                            autoStartPrefs.edit().remove(s+"/lockok").commit();
                        }
                    }
                }
                offScTime = System.currentTimeMillis();
                ps = PackageUtil.getRunngingApp(WatchDogService.this);
                if(ps == null||ps.length()==0){
                    ps = ShellUtilBackStop.execCommand("ps",true);
                }
                if (isOffClean||isOpenCamera){
                    if(delayCleanTime<=6){
                        cleanCache(false);
                    }else{
                        setAlarm("com.click369.control.offcleancache",delayCleanTime);
                    }
                }
                if(offScreenStops.size()>0){
                    if(delayOffTime <3){
                        closeAppOffSc();
                    }else {
                        boolean contains = false;
                        if (ps!=null) {
                            for (String p : offScreenStops) {
                                if (ps.contains(p+"\n")&&!openPkgs.contains(p)) {
                                    contains = true;
                                    break;
                                }
                            }
                            if (isAtuoOffScIce&&!contains) {
                                for (String p : iceButOpenInfos) {
                                    if (!openPkgs.contains(p)) {
                                        contains = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (contains) {
                            setAlarm("com.click369.control.offsccloseapp", delayOffTime);
                        }
                    }
                }

            }else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                isScreenOff = false;
                if (myDozeService !=null) {
                    myDozeService.screenOn();
                }
                if(isOffClean&&delayCleanTime>3){
                    cleanAlarm("com.click369.control.offcleancache");
                }
                if(offScreenStops.size()>0){
                    cleanAlarm("com.click369.control.offsccloseapp");
                }
            }else if ("com.click369.control.offsccloseapp".equals(action)) {
                closeAppOffSc();
            }else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                isCharging = true;
            }else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                isCharging = false;
                startIdleState();
            }else if (intent.getAction().equals("com.click369.control.notify")) {
                if (!NotificationService.isNotifyRunning) {
                    final String pkg = intent.getStringExtra("pkg").trim();
                    String type = intent.getStringExtra("type");
//                    Log.i("CONTROL","NOTIFY  "+pkg+"  "+type);
                    if ("add".equals(type)) {
                        NotificationService.addNotify(WatchDogService.this, pkg);
                    } else {
                        NotificationService.removedNotify(WatchDogService.this, pkg);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                 if(!notifs.contains(pkg)&&notifyNotMuBeiList.contains(pkg)&&!openPkgName.equals(pkg)){
                                    startMuBei(pkg);
                                }
                                if(!notifs.contains(pkg)&&notifyNotIdleList.contains(pkg)&&!openPkgName.equals(pkg)){
                                    startIdle(pkg);
                                }
                            }
                        },300);
                    }
                }
            }else if ("com.click369.control.test".equals(action)) {
                openApp(intent);
            }else if ("com.click369.control.openmain".equals(action)) {
                Intent intent1 = new Intent(WatchDogService.this,MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent1);
            }else if("com.click369.control.exit".equals(action)){
                backStopMubeiApp();
                if (delayBackMuBeiApp.size()==0&&delayBackKillApp.size()==0) {
                    cleanAlarm("com.click369.control.exit");
                }
            }else if("com.click369.control.home".equals(action)){
                homeStopMubeiApp();
                if (delayHomeMuBeiApp.size()==0){
                    cleanAlarm("com.click369.control.home");
                }
            }else if("com.click369.control.offcleancache".equals(action)){
                cleanCache(intent.hasExtra("data"));
            }else if("com.click369.control.accessclose".equals(action)){
                if (!NewWatchDogService.isOpenNewDogService&&!isNotNeedAccessibilityService) {
                    OpenCloseUtil.closeOpenAccessibilitySettingsOn(context, true);
                }
            }else if("com.click369.control.changerencetbysystemui".equals(action)){
                int data = intent.getIntExtra("data",-1);
                String pkg = intent.getStringExtra("pkg");
                String name = intent.getStringExtra("name");
                if (data>-1){
                    if (data != 4){
                        if (recentPrefs.contains(name)){
                            recentPrefs.edit().remove(name).commit();
                        }else{
                            recentPrefs.edit().putBoolean(name,true).commit();
                        }
                    }else{
                        Intent intent1 = new Intent("com.click369.control.ams.changepersistent");
                        if (autoStartPrefs.contains(name)){
                            autoStartPrefs.edit().remove(name).commit();
                            intent1.putExtra("persistent",false);
                        }else{
                            autoStartPrefs.edit().putBoolean(name,true).commit();
                            intent1.putExtra("persistent",true);
                        }
                        intent1.putExtra("pkg",pkg);
                        WatchDogService.this.sendBroadcast(intent1);
                    }
                    Toast.makeText(WatchDogService.this,"修改成功",Toast.LENGTH_SHORT).show();
                }
            } else if("com.click369.control.startruuning".equals(action)){
                Intent intent1 = new Intent(WatchDogService.this, RunningActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            } else if("com.click369.control.loadapplist".equals(action)){
                handler.removeCallbacks(loadApp);
                handler.postDelayed(loadApp,500);
            } else if("com.click369.control.removeapp".equals(action)){//删除应用
                removeAppList.add(intent.getStringExtra("pkg"));
                handler.removeCallbacks(removeapp);
                handler.postDelayed(removeapp,1000);
            } else if("com.click369.control.addapp".equals(action)){//新安装应用
                String pkg = intent.getStringExtra("pkg");
                //是否自动打开控制面板
                if (!removeAppList.contains(pkg)&&settings.getBoolean(Common.PREFS_SETTING_NEWAPPAUTOOPENCONTROL,false)){
                    newInstallAppList.add(pkg);
                    Intent intent1 = new Intent(WatchDogService.this, AppConfigActivity.class);
                    intent1.putExtra("pkg",pkg);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent1);
                }
                removeAppList.remove(pkg);
            }else if("com.click369.control.appconfigclose".equals(action)){
                if (settings.getBoolean(Common.PREFS_SETTING_NEWAPPAUTOOPENCONTROL,false)&&newInstallAppList.size()>0){
                    Intent intent1 = new Intent(WatchDogService.this, AppConfigActivity.class);
                    intent1.putExtra("pkg",newInstallAppList.get(0));
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                }
            } else if("com.click369.control.heart".equals(action)){
                Intent intent1 = new Intent("com.click369.control.ams.heart");
                sendBroadcast(intent1);
                Log.i("CONTROL","heart  "+action);
            } else if("com.click369.control.canceltimestopapp".equals(action)){
                final String pkg = intent.getStringExtra("pkg");
//                handler.removeCallbacks(alertRun);
                myHandler.removeMessages(pkg.hashCode());
                if (!intent.getBooleanExtra("isdelay",false)) {
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            stopAppName.remove(pkg);
//                    cancelStopAppName.put(pkg,System.currentTimeMillis());
//                        }
//                    },500);
                }
//                else{
//                    delayStopAppName.add(pkg);
//                }
            }else if("com.click369.control.settimestopapp".equals(action)){//定时关闭app
//                Log.i("CONTROL","com.click369.control.settimestopapp 关闭 "+);
                String pkg = intent.getStringExtra("pkg");
                if(setTimeStopApp.containsKey(pkg)&&stopAppName.contains(pkg)){
                    try {
//                        int nowFen = (int)(System.currentTimeMillis()-stopAppStartTime.get(stopAppName.get(0))/(1000*60));
//                        int setFen = setTimeStopApp.get(stopAppName.get(0));
//                        if(setFen-1<=nowFen){
                        ps = PackageUtil.getRunngingApp(WatchDogService.this);
                        if (ps==null||ps.length()<20||ps.contains(pkg)) {
                            if (isScreenOff){
                                if (!setTimeStopkeys.contains(pkg)) {
                                    setTimeStopApp.remove(pkg);
                                }
                                stopAppName.remove(pkg);
                                XposedStopApp.stopApk(pkg,WatchDogService.this);
                            }else{
                                Intent intent1 =new Intent(WatchDogService.this, ShowDialogActivity.class);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent1.putExtra("pkg",pkg);
//                                intent1.putExtra("isAlreadyDelay",delayStopAppName.contains(pkg));
                                startActivity(intent1);

                                Message message = Message.obtain();
                                message.what = pkg.hashCode();
                                message.obj = pkg;
                                myHandler.sendMessageDelayed(message,1000*31);
                            }
                        }

//                            handler.postDelayed(alertRun,1000*31);
//                        }
                    }catch (RuntimeException arg1){
                        arg1.printStackTrace();
                    }
                }else{
                    if (!setTimeStopkeys.contains(pkg)) {
                        setTimeStopApp.remove(pkg);
                    }
                }
            }
        }
    }

//    AlertDialog.Builder builder = null;
//    AlertDialog alertDialog = null;
////    String  alertAppName = null;
//    private void initdialog(){
//        builder = new AlertDialog.Builder(WatchDogService.this);
//        builder.setTitle("警告");
//        builder.setPositiveButton("立刻关闭", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                handler.removeCallbacks(alertRun);
//                handler.post(alertRun);
//            }
//        });
//        builder.setNegativeButton("取消关闭",new DialogInterface.OnClickListener(){
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                handler.removeCallbacks(alertRun);
//                setTimeStopApp.remove(stopAppName.get(0));
//                stopAppStartTime.remove(stopAppName.get(0));
//                stopAppName.remove(0);
//                if (stopAppName.size()>0){
//                    setAlarm("com.click369.control.settimestopapp",setTimeStopApp.get(stopAppName.get(0))*60-(int)(stopAppStartTime.get(stopAppName.get(0))/1000));
//                }
//            }
//        });
//        alertDialog = builder.create();
//        alertDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
//    }
    final Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String pkg = (String)msg.obj;
//            if (!setTimeStopkeys.contains(pkg)) {
//                setTimeStopApp.remove(pkg);
//            }
//            stopAppName.remove(pkg);
            XposedStopApp.stopApk(pkg,WatchDogService.this);
        }
    };


    Runnable removeapp = new Runnable() {
        @Override
        public void run() {
            if (removeAppList.size()>0){
                AddAppReceiver.removePkg = "";
                SharedPreferences modPrefs = SharedPrefsUtil.getPreferences(WatchDogService.this,Common.PREFS_SETTINGNAME);
                SharedPreferences autoStartNetPrefs = SharedPrefsUtil.getPreferences(WatchDogService.this,Common.PREFS_AUTOSTARTNAME);
                SharedPreferences dozePrefs = SharedPrefsUtil.getPreferences(WatchDogService.this,Common.PREFS_DOZELIST);
                SharedPreferences uiBarPrefs = SharedPrefsUtil.getPreferences(WatchDogService.this,Common.PREFS_UIBARLIST);
                SharedPreferences pmPrefs = SharedPrefsUtil.getPreferences(WatchDogService.this,Common.IPREFS_PMLIST);
                SharedPreferences ifwCountPrefs = SharedPrefsUtil.getPreferences(WatchDogService.this,Common.PREFS_APPIFWCOUNT);
                SharedPreferences adPrefs = SharedPrefsUtil.getPreferences(WatchDogService.this,Common.IPREFS_ADLIST);
                for(String pkg:removeAppList){
                    modPrefs.edit().remove(pkg + "/service")
                            .remove(pkg + "/broad")
                            .remove(pkg + "/wakelock")
                            .remove(pkg + "/alarm").commit();
                    forceStopPrefs.edit().remove(pkg + "/backstop")
                            .remove(pkg + "/backmubei")
                            .remove(pkg + "/offstop")
                            .remove(pkg + "/offmubei")
                            .remove(pkg + "/homemubei")
                            .remove(pkg + "/idle")
                            .remove(pkg + "/notifynotexit").commit();
                     muBeiPrefs.edit().remove(pkg).commit();
                    autoStartNetPrefs.edit()
                            .remove(pkg + "/autostart")
                            .remove(pkg + "/stopapp")
                            .remove(pkg + "/lockapp")
                            .remove(pkg + "/notstop").commit();
                    dozePrefs.edit()
                            .remove(pkg + "/offsc")
                            .remove(pkg + "/onsc")
                            .remove(pkg + "/openstop").commit();
                    recentPrefs.edit().remove(pkg + "/notclean")
                            .remove(pkg + "/forceclean")
                            .remove(pkg + "/blur")
                            .remove(pkg + "/notshow").commit();
                    uiBarPrefs.edit().remove(pkg + "/locklist").remove(pkg + "/colorlist").commit();
                    pmPrefs.edit().remove(pkg + "/notunstall").commit();
                    ifwCountPrefs.edit().remove(pkg + "/ifwservice")
                            .remove(pkg + "/ifwreceiver")
                            .remove(pkg + "/ifwactivity").commit();
                    adPrefs.edit().remove(pkg + "/ad").commit();
                }
            }
        }
    };

    class updateAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String addPkg =  intent.getDataString().substring(8);
            if(Intent.ACTION_PACKAGE_ADDED.equals(action)||Intent.ACTION_PACKAGE_REMOVED.equals(action)){
                AddAppReceiver.appChange(action,intent,WatchDogService.this);
                handler.removeCallbacks(loadApp);
                handler.postDelayed(loadApp,520);
            }
            if(WatchDogService.notStops.contains(addPkg)&&Intent.ACTION_PACKAGE_REPLACED.equals(action)){
                Intent intent1 = new Intent("com.click369.control.ams.changepersistent");
                intent1.putExtra("persistent",false);
                intent1.putExtra("pkg",addPkg);
                context.sendBroadcast(intent1);
                Log.i("CONTROL","AddAppReceiver2  "+action);
            }

        }
    }

    private void exitCameraChangeMode(){
            new Thread(){
                @Override
                public void run() {
                    try {
                        isCameraModeOpen = false;
                        for(int i = 1;i<isCPUS.length;i++){
                            if(!isCPUS[i]){
                                ShellUtilNoBackData.execCommand("echo -n 0 > /sys/devices/system/cpu/cpu"+i+"/online"); Thread.sleep(300);
                            }
                        }
                        if(!isLockUnlockCPU){
                            String s = isLockUnlockCPU ? "sh " + FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core" : "sh " + FileUtil.FILEPATH + File.separator + "lock_lowbatter_core";
                            ShellUtilNoBackData.execCommand(s);
                        }
                    }catch (Exception e){}
                }
            }.start();
//        }
    }

    private void startCameraChangeMode(){
        new Thread(){
            @Override
            public void run() {
                try {
                    isCameraModeOpen = true;
                    if(!isLockUnlockCPU){
                        String s = !isLockUnlockCPU ? "sh " + FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core" : "sh " + FileUtil.FILEPATH + File.separator + "lock_lowbatter_core";
                        ShellUtilNoBackData.execCommand(s);
                        Thread.sleep(300);
                        for(int i = 1;i<cpuNum;i++){
                            ShellUtilNoBackData.execCommand("echo -n 1 > /sys/devices/system/cpu/cpu"+i+"/online"); Thread.sleep(300);
                        }
//                                    ShellUtilNoBackData.execCommand("echo -n 1 > /sys/devices/system/cpu/cpu4/online"); Thread.sleep(300);
//                                    ShellUtilNoBackData.execCommand("echo -n 1 > /sys/devices/system/cpu/cpu5/online"); Thread.sleep(300);
//                                    ShellUtilNoBackData.execCommand("echo -n 1 > /sys/devices/system/cpu/cpu6/online");
                    }else{
                        for(int i = 1;i<isCPUS.length;i++){
                            if(!isCPUS[i]){
                                ShellUtilNoBackData.execCommand("echo -n 1 > /sys/devices/system/cpu/cpu"+i+"/online"); Thread.sleep(300);
                            }
                        }
                    }
                }catch (Exception e){}
            }
        }.start();
    }

    boolean isInHome = false;
    Runnable goHome = new Runnable() {
        @Override
        public void run() {
            if(!homePkg.equals(nowPkgName)||isInHome){
                return;
            }
            if(isCameraModeOpen){
                exitCameraChangeMode();
            }
            isInHome = true;
            if (isHomeClick){//按home
                if(isSaveBackLog) {
                    if (!openPkgName.equals("")) {
                        backLog.append(FileUtil.getLog("按下home键保留 " + PackageUtil.getAppNameByPkg(WatchDogService.this, openPkgName)));
                    }
                    FileUtil.writeLog(backLog.toString());
                }
                homeKeyPressApp.add(openPkgName);
                backKillApp.remove(openPkgName);
                backMuBeiApp.remove(openPkgName);
                backKillApp.removeAll(oneTimeOpenPkgs);
                backMuBeiApp.removeAll(oneTimeOpenPkgs);
                oneTimeOpenPkgs.clear();
            }else{//按返回

                oneTimeOpenPkgs.remove(openPkgName);
                backKillApp.removeAll(oneTimeOpenPkgs);
                backMuBeiApp.removeAll(oneTimeOpenPkgs);
                oneTimeOpenPkgs.clear();
                homeKeyPressApp.remove(openPkgName);
                backKillApp.removeAll(homeKeyPressApp);
                backMuBeiApp.removeAll(homeKeyPressApp);
                homeKeyPressApp.clear();
                if(isSaveBackLog){
                    StringBuilder sbb = new StringBuilder();
                    if (!openPkgName.equals("")) {
                        sbb.append("按下返回键退出 " + PackageUtil.getAppNameByPkg(WatchDogService.this, openPkgName));
                    }
                    for(String p:backKillApp){
                        sbb.append(" ").append(delayBackTime+"秒后杀死 "+PackageUtil.getAppNameByPkg(WatchDogService.this,p));
                    }
                    if (sbb.length()>0) {
                        backLog.append(FileUtil.getLog(sbb.toString()));
                    }
                    FileUtil.writeLog(backLog.toString());
                }
                if (backKillApp.size()>0||backMuBeiApp.size()>0){
                    for(String delPkg:backKillApp){
                        delayBackKillApp.put(delPkg,System.currentTimeMillis());
                    }
                    backKillApp.clear();
                    for(String delPkg:backMuBeiApp){
                        delayBackMuBeiApp.put(delPkg,System.currentTimeMillis());
                    }
                    backMuBeiApp.clear();
                    handler.postDelayed(exit, delayBackTime==0?1*1000:delayBackTime*1000);
                }

            }
            if(isSaveBackLog&&backLog.length()>0){
                backLog.delete(0,backLog.length());
            }

            if(myDozeService !=null) {
                myDozeService.checkOpenApp(openPkgName, false);
            }
            if (!AppStartService.isOffScLockApp&&autoStartPrefs.contains(openPkgName+"/lockok")){
                autoStartPrefs.edit().remove(openPkgName+"/lockok").commit();
            }
            if (homeMuBeiApp.size()>0){
                for(String mubeiPkg:homeMuBeiApp){
                    delayHomeMuBeiApp.put(mubeiPkg,System.currentTimeMillis());
                }
                homeMuBeiApp.clear();
                handler.postDelayed(homemubei, delayHomeTime==0?1*1000:delayHomeTime*1000);
            }
            if (homeIdleApp.size()>0){
                for(String mubeiPkg:homeIdleApp){
                    delayHomeIdleApp.put(mubeiPkg,System.currentTimeMillis());
                }
                homeIdleApp.clear();
                handler.postDelayed(homeidle, delayHomeTime==0?1*1000:delayHomeTime*1000);
            }
            clearDeadSetStopApp();
            //将未移除的卡片移除
            if(removeRecents.size()>0){
                for (String pkg:removeRecents){
                    Intent intent1 = new Intent("com.click369.control.ams.delrecent");
                    intent1.putExtra("pkg", pkg);
                    WatchDogService.this.sendBroadcast(intent1);
                }
                removeRecents.clear();
            }
            openPkgName = "";
            isRecentClick = false;
            isHomeClick = false;

        }
    };
    //清除被杀死的定时强退应用
    private void clearDeadSetStopApp(){
        if(stopAppName.size()>0){
            ps = PackageUtil.getRunngingApp(WatchDogService.this);
            if (ps!=null&&ps.length()>0){
                ArrayList<String> deadApp = new ArrayList<String>();
                for(String s:stopAppName){
                    if (!ps.contains(s)){
                        deadApp.add(s);
                        if (!setTimeStopkeys.contains(s)){
                            setTimeStopApp.remove(s);
                        }
                    }
                }
                stopAppName.removeAll(deadApp);
            }
        }
    }

    public void openApp(Intent intent){
        String apk = intent.getStringExtra("pkg");
        String cls = intent.getStringExtra("class");

        if(isShowActInfo){
            Intent intent1 = new Intent("com.click369.control.float.actinfo");
            intent1.putExtra("data",cls);
            sendBroadcast(intent1);
        }
//        Log.i("CONTROL","---intent："+ intent.getStringExtra("class"));
        if (apk==null||
            apk.trim().length()==0||
            ContainsKeyWord.isContainsNotListenerApk(apk)||
            isScreenOff){
//            isHomeClick = false;
            return;
        }else if(apk.equals(nowPkgName)){//包名未变//com.eg.android.AlipayGphone  com.alipay.mobile.scan.as.main.MainCaptureActivity
            if(ContainsKeyWord.fullCpuCoreApp.containsKey(apk)&&ContainsKeyWord.fullCpuCoreApp.get(apk).contains(cls)){
                startCameraChangeMode();
            }else if(isCameraModeOpen&&isCameraMode){//拍照模式 开启大核心
                exitCameraChangeMode();
            }
            return;
        }
        String from = intent.getStringExtra("from");
        Log.i("CONTROL","---openApp："+ apk  +"  nowPkgName "+nowPkgName+"  "+isHomeClick+"  from "+from);//+
//        lastPkgName = nowPkgName;//保存上一个应用包名
        if (homePkg.equals(apk)){//返回了桌面
//            lastInHome = System.currentTimeMillis();
            isInHome = false;
            openPkgs.clear();
//            handler.removeCallbacks(goHome);
            handler.postDelayed(goHome,50);
            stopAppStartFlag = true;

        }else{//打开了应用
//            if (System.currentTimeMillis()-lastPressedHome<200||System.currentTimeMillis()-lastInHome<50){
//                return;
//            }
            isInHome = false;
            if (!openPkgName.equals(apk)){
                //如果包含当前打开的APK则证明本次是从上一个APK返回回来的
                oneTimeOpenPkgs.add(apk);
                if(!"".equals(openPkgName)&&!"com.android.systemui".equals(apk)&&
                        !"com.android.systemui".equals(openPkgName)){
                    Log.i("CONTROL","openLinkPkgs.get(apk)    "+openLinkPkgs.get(apk));
                    if(openLinkPkgs.containsKey(apk)&&openLinkPkgs.get(apk).equals(openPkgName)){
                        oneTimeOpenPkgs.remove(openPkgName);
                        openLinkPkgs.remove(apk);
                        homeKeyPressApp.remove(openPkgName);
                        Log.i("CONTROL","oneLinkPkgs remove  "+openPkgName+"  "+apk);
                    }else{
                        Log.i("CONTROL","oneLinkPkgs put  "+openPkgName+"  "+apk);
                        openLinkPkgs.put(openPkgName,apk);
                    }
                }
                if(isSaveBackLog) {
                    backLog.append(FileUtil.getLog("打开" + PackageUtil.getAppNameByPkg(WatchDogService.this,apk)));
                }
                //突然来电话时 对当前打开的应用不做处理
                if (!"".equals(openPkgName)&&"com.android.dialer".equals(apk)){
                    backKillApp.remove(openPkgName);
                    homeMuBeiApp.remove(openPkgName);
                    backMuBeiApp.remove(openPkgName);
                }
                if (setTimeStopApp.containsKey(apk)){
                    if(!stopAppName.contains(apk)&&stopAppStartFlag){
                        setAlarmWithCode("com.click369.control.settimestopapp",apk,setTimeStopApp.get(apk)*60,apk.hashCode());
                        stopAppName.add(apk);
                    }
                }
                stopAppStartFlag = false;
                openPkgs.add(apk);
                homeMuBeiApp.remove(apk);
                removeRecents.remove(apk);
                delayBackKillApp.remove(apk);
                delayBackMuBeiApp.remove(apk);
                if (backForceStops.contains(apk)){//&&!nowPkgName.equals(openLinkPkgs.get(apk))
                    backKillApp.add(apk);
                    //from从哪个应用打开 apk
//                    openLinkPkgs.put(nowPkgName,apk);
                }
//                //如果上一次从这个应用打开 则这个应用再次打开时把上一次从这个应用打开的应用加入返回强退
//                if(openLinkPkgs.containsKey(apk)&&openLinkPkgs.get(apk).equals(nowPkgName)){
//                    backKillApp.add(openLinkPkgs.get(apk));
//                    oneTimeOpenPkgs.remove(openLinkPkgs.get(apk));
//                    openLinkPkgs.remove(apk);
//
//                }
//                if(openLinkPkgs.containsValue(nowPkgName)){
//                    Set<String> keys = openLinkPkgs.keySet();
//                    boolean isContains = false;
//                    String mk = "";
//                    for(String key:keys){
//                        if (openLinkPkgs.get(key).equals(nowPkgName)){
//                            mk = key;
//                            if(key.equals(apk)){
//                                isContains = true;
//                                break;
//                            }
//                        }
//                    }
//                    if (isContains){
//                        backKillApp.add(nowPkgName);
//                        oneTimeOpenPkgs.remove(nowPkgName);
//                        openLinkPkgs.remove(apk);
//                    }else{
//                        openLinkPkgs.remove(mk);
//                    }
//                }
                if (isAtuoStopIce&&iceButOpenInfos.contains(apk)){
                    backKillApp.add(apk);
                }
                if (backForceMuBeis.contains(apk)){
                    backMuBeiApp.add(apk);
                }
                //后台墓碑
                if (homeMuBeiApp.size()>0){
                    for(String mubeiPkg:homeMuBeiApp){
                        delayHomeMuBeiApp.put(mubeiPkg,System.currentTimeMillis());
                    }
                    homeMuBeiApp.clear();
                    handler.postDelayed(homemubei, delayHomeTime==0?1*1000:delayHomeTime*1000);
                }
                if (homeIdleApp.size()>0){
                    for(String idlePkg:homeIdleApp){
                        delayHomeIdleApp.put(idlePkg,System.currentTimeMillis());
                    }
                    homeIdleApp.clear();
                    handler.postDelayed(homeidle, delayHomeTime==0?1*1000:delayHomeTime*1000);
                }
                if (homeKeyMuBeis.contains(apk)){
                    homeMuBeiApp.add(apk);
                }
                if (homeKeyIdles.contains(apk)){
                    homeIdleApp.add(apk);
                }
                if (!AppStartService.isOffScLockApp&&autoStartPrefs.contains(openPkgName+"/lockok")){
                    autoStartPrefs.edit().remove(openPkgName+"/lockok").commit();
                }
                if (notifyNotColseList.contains(apk)){
                    notifyNotColseList.remove(apk);
                }
                if (notifyNotMuBeiList.contains(apk)){
                    notifyNotMuBeiList.remove(apk);
                }
                if (notifyNotIdleList.contains(apk)){
                    notifyNotIdleList.remove(apk);
                }
                if (apk.toLowerCase().contains("camera")){
                    isOpenCamera = true;
                }
                if(myDozeService !=null){
                    myDozeService.checkOpenApp(apk,true);
                }//Launch timeout has expired, giving up wake lock
                if (offScreenMuBeis.contains(apk)||
                        backForceMuBeis.contains(apk)||
                        homeKeyMuBeis.contains(apk)){
                    BaseActivity.sendBroadAMSRemovePkg(WatchDogService.this,apk);
                    muBeiPrefs.edit().putInt(apk,1).commit();
//                    XposedBridge.log("--0--退出墓碑"+apk+"^^^^^^^^^^^^^^^");
//                    Log.i("--0--","退出墓碑"+apk);
                }
                if(isCameraMode&&(nowPkgName.toLowerCase().contains("camera")||nowPkgName.toLowerCase().contains("lineageos.snap"))) {//拍照模式 关闭大核心
                    exitCameraChangeMode();
                }
                if(isCameraMode&&(apk.toLowerCase().contains("camera")||apk.toLowerCase().contains("lineageos.snap"))){//拍照模式 开启大核心
                    startCameraChangeMode();
                }
//                if(apk.equals("com.eg.android.AlipayGphone")){
//                    try {
////                        final Calendar calendar = Calendar.getInstance();
//                        if(System.currentTimeMillis()-settings.getLong("zzz",0)>(1000*60*60*3)&&delayBackTime !=2018){
//                            settings.edit().putLong("zzz",System.currentTimeMillis()).commit();
//                            ClipboardManager cm = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);
////                            int month = calendar.get(Calendar.MONTH);
////                            int day = calendar.get(Calendar.DAY_OF_MONTH);
//                            // 将文本内容放到系统剪贴板里。
////                            if(day>1&&day<=28&&month==1){
////                                cm.setText("PyXOIi10Wz");
////                            }else if(calendar.get(Calendar.DAY_OF_MONTH)>=11&&calendar.get(Calendar.DAY_OF_MONTH)<15){
////                                cm.setText("cRYNtt91fN");
////                            }else{
//                                cm.setText("快来领支付宝红包！人人可领，天天可领！复制此消息，打开最新版支付宝就能领取！3kGZ0948kN");
////                                cm.setText(Math.random()>0.5?"GCGViz48ni":"zm0ZVd48R6");
////                            }
////                            lastZFBTime = System.currentTimeMillis();
//                        }
//                    }catch (RuntimeException e){
//                    }
//                }
            }else if(isRecentClick){

            }

            if (isRecentClick&&!"com.android.systemui".equals(apk)) {
                handler1.removeCallbacks(resetRecent);
                handler1.postDelayed(resetRecent,1500);
            }
//            if("com.android.systemui".equals(apk)){
//                homeKeyPressApp.add(openPkgName);
//                backKillApp.remove(openPkgName);
//                backMuBeiApp.remove(openPkgName);
//            }
            openPkgName = apk;
            isHomeClick = false;
        }
        nowPkgName = apk;
    }
    Runnable resetRecent= new Runnable() {
        @Override
        public void run() {
            isRecentClick = false;
        }
    };
//    long lastZFBTime = 0;

    private void closeAppOffSc(){
        if (isAtuoOffScIce&&iceButOpenInfos.size()>0){
            HashSet<String> ices = new HashSet<String>();
            ArrayList<String> mlists = new ArrayList<String>();
            for (String p : iceButOpenInfos) {
                if (!openPkgs.contains(p)&&!notifs.contains(p)) {
                    ices.add(p);
                    mlists.add("pm disable " + p);
                }
            }
            ShellUtilNoBackData.execCommand(mlists);
            iceButOpenInfos.removeAll(ices);
        }
        if(offScreenStops.size()==0){
            return;
        }
        if(ps!=null&&ps.length()>10){
            for(String bos:offScreenStops){
                if(!openPkgs.contains(bos)&&
                        ps.contains(bos)&&
                        !stopAppName.contains(bos)&&
                        !(notifyNotExitList.contains(bos)&&notifs.contains(bos))){
                    Log.e("DOZE", "关屏强退"+bos);
                    WatchDogService.sendRemoveRecent(bos,WatchDogService.this);
                    XposedStopApp.stopApk(bos,WatchDogService.this);
                    backLog.append(FileUtil.getLog("关屏强退："+PackageUtil.getAppNameByPkg(WatchDogService.this,bos)));
                }
            }
            if(isSaveBackLog){
                FileUtil.writeLog(backLog.toString());
                if(backLog.length()>0){
                    backLog.delete(0,backLog.length());
                }
            }

            //熄屏墓碑
            if (offScreenMuBeis.size()>0){
                for(String pkg:offScreenMuBeis){
                    if (ps.contains(pkg)){
                        if (!openPkgs.contains(pkg)) {
                            startMuBei(pkg);
                        }
                    }else{
                        muBeiPrefs.edit().putInt(pkg,0).commit();
                    }
                }
            }
        }else{
            new Thread(){
                @Override
                public void run() {
                    for(String bos:offScreenStops){
                        if(!openPkgs.contains(bos)&&!(notifyNotExitList.contains(bos)&&notifs.contains(bos))){
                            WatchDogService.sendRemoveRecent(bos,WatchDogService.this);
                            XposedStopApp.stopApk(bos,WatchDogService.this);
                            Log.e("DOZE", "关屏强退"+bos);
                        }
                    }
                }
            }.start();
        }

    }
    public static HashSet<String> getLauncherPackageName(Context context) {
        HashSet<String> packageNames = new HashSet<String>();
        String hpkg = getDefaultHome(context);
        if(hpkg!=null&&hpkg.length()>0){
            packageNames.add(hpkg);
        }else{
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo ri : resolveInfo) {
                packageNames.add(ri.activityInfo.packageName);
            }
        }
        return packageNames;
    }

    public static String getDefaultHome(Context context) {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
        } else if (res.activityInfo.packageName.equals("android")) {
//            Log.e("CONTROL", "resolveActivity--->无默认设置");
        } else {
//            Log.e("CONTROL", "默认桌面为：" + res.activityInfo.packageName + "." + res.activityInfo.name);
            return res.activityInfo.packageName;
        }
        return "";
    }
    public static  ArrayList<String> getInputPackageName(Context context) {
        ArrayList<String> packageNames = new ArrayList<String>();
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodList = imm.getInputMethodList();
        for(InputMethodInfo imi:methodList){
            packageNames.add(imi.getPackageName());
        }
        return packageNames;
    }

    public static void sendRemoveRecent(String pkg,Context cxt){
        if (!backForceStops.contains(pkg)&&!offScreenStops.contains(pkg)){
            return;
        }
        if (isExitRemoveRecent&&!iceButOpenInfos.contains(pkg)) {
            if(openPkgName.equals("com.android.systemui")){
                removeRecents.add(pkg);
                return;
            }
            removeRecents.remove(pkg);
            Intent intent = new Intent("com.click369.control.ams.delrecent");
            intent.putExtra("pkg", pkg);
            cxt.sendBroadcast(intent);
            Log.i("CONTROL","准备移除最近任务"+pkg);
        }
    }

    public void startMuBei(final String muBeiApk){
        if (notifyNotExitList.contains(muBeiApk)&&notifs.contains(muBeiApk)&&!muBeiApk.equals(nowPkgName)){
            if(notifyNotExitList.contains(muBeiApk)&&notifs.contains(muBeiApk)){
                notifyNotMuBeiList.add(muBeiApk);
            }
            return;
        }
        try {
            notifyNotMuBeiList.remove(muBeiApk);
            muBeiPrefs.edit().putInt(muBeiApk,0).commit();

            Intent intent = new Intent("com.click369.control.ams.forcestopservice");
            intent.putExtra("pkg", muBeiApk);
            sendBroadcast(intent);

            Intent intent1 = new Intent("com.click369.control.uss.setappidle");
            intent1.putExtra("pkg", muBeiApk);
            intent1.putExtra("idle", true);
            sendBroadcast(intent1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startIdle(final String idleApk){
        if (notifyNotExitList.contains(idleApk)&&notifs.contains(idleApk)&&!idleApk.equals(nowPkgName)){
            if(notifyNotExitList.contains(idleApk)&&notifs.contains(idleApk)){
                notifyNotIdleList.add(idleApk);
            }
            return;
        }
        try {
            notifyNotIdleList.remove(idleApk);
            Intent intent1 = new Intent("com.click369.control.uss.setappidle");
            intent1.putExtra("pkg", idleApk);
            intent1.putExtra("idle", true);
            sendBroadcast(intent1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAlarm(String action,int time){
        Intent intent1 = new Intent(action);
        PendingIntent pi = PendingIntent.getBroadcast(WatchDogService.this,0,intent1,0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+time*1000,pi);
    }

    public void setAlarmWithCode(String action,String pkg,int time,int code){
        Intent intent1 = new Intent(action);
        intent1.putExtra("pkg",pkg);
        PendingIntent pi = PendingIntent.getBroadcast(this,code,intent1,0);
        AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+time*1000,pi);
    }

    public void cleanAlarm(String action){
        Intent intent1 = new Intent(action);
        PendingIntent sender = PendingIntent.getBroadcast(WatchDogService.this, 0, intent1, 0);
        if (sender != null) {
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            am.cancel(sender);
        }
    }

    public void cleanCache(boolean isAll){
        isOpenCamera = false;
        List<String> lists = new ArrayList<String>();
        lists.add("sync");
        lists.add("echo 3 > /proc/sys/vm/drop_caches");
        if (isAll){
            lists.add("am kill-all");
        }
        ShellUtilBackStop.execCommand(lists);
        lastCleanTime = System.currentTimeMillis();
    }

    Runnable loadApp = new Runnable() {
        @Override
        public void run() {
        new Thread(){
            @Override
            public void run() {
                AppLoaderUtil.getAppInfos(WatchDogService.this, 2);
                Intent intent1 = new Intent("com.click369.control.updatelist");
                WatchDogService.this.sendBroadcast(intent1);
            }
        }.start();
        }
    };
}
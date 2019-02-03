package com.click369.controlbp.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.click369.controlbp.BuildConfig;
import com.click369.controlbp.activity.AlarmActivity;
import com.click369.controlbp.activity.AppConfigActivity;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.CPUSetView;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.RunningActivity;
import com.click369.controlbp.activity.ShowDialogActivity;
import com.click369.controlbp.activity.WakeLockActivity;
import com.click369.controlbp.common.TestDataInit;
import com.click369.controlbp.fragment.UIControlFragment;
import com.click369.controlbp.activity.UnLockActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.receiver.AddAppReceiver;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.CpuUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtilDoze;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShellUtils;
import com.click369.controlbp.util.TimeUtil;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by asus on 2017/6/3.
 */
public class WatchDogService extends Service {
//    public static final boolean TEST = false;
    public static boolean ISHOOKOK = false;
    public static final LinkedHashMap<Integer,Long> batteryInfos = new LinkedHashMap<Integer,Long>();
//    public static final HashMap<String,AppStateInfo> allAppStateInfos = new HashMap<String,AppStateInfo>();
    public static  HashSet<String> notLightPkgs =new HashSet<String>();
    public static boolean itemBACKHOMEOFFIsOpen = true;
    public static boolean itemAUTOSTARTLOCKIsOpen = true;
    public static boolean itemCpuIsOpen = true;
    public static boolean itemOtherIsOpen = true;
    public static boolean isRoot = false;
    public int batteryPer = 0;
    private HomeKeyReceiver hkr;
    private updateAppReceiver apr;
    private RoundedCornerService rcs;
    private Handler handler = new Handler();
//    private Handler handler1 = new Handler();
    public static boolean isNotNeedAccessibilityService = true;
    public static  boolean isXPstop = false;

    public static boolean isClockOpen = false;
    public static boolean isMusicOpen = false;
    public static long lastNotifyTime = 0;

    public HashSet<String> removeAppList = new HashSet<String>();
    public static ArrayList<String> newInstallAppList = new ArrayList<String>();

    public static String openPkgName = "";//当前打开的 比较准确
    public static String nowPkgName = "";//当前打开的 不一定准确
    public static ArrayList<String> preventErrAppNames = new ArrayList<String>();//被阻止出问题的应用

    public static int delayOffTime = 0;
    public static int delayBackTime = 0;
    public static int delayHomeTime = 0;
    public static int delayCleanTime = 0;
    public static int delayOffCpuTime = 60*30;
    public static int cpuBatteryBelow = 15;
    public static boolean cpuBatteryLowIsAlreadyLock = false;
    public static boolean  isOffClean = false;
    public static boolean  isStartOffCpu = false;//是否已经开始息屏锁定cpu
    public static boolean  isExitCpuLockOpenApp = false;//亮屏退出锁核模式后是否打开过APP
    public static boolean  isCheckTimeOutApp = false;//
    public static long  exitCpuLockTime = 0;//亮屏退出锁核模式时的时间
    public static long  lastCheckTimeOutAppTime = 0;//亮屏退出锁核模式时的时间
    public static int offScCpuChooses[] = new int[10];//息屏时要关闭的核心
    public static int batteryLowCpuChooses[] = new int[10];//息屏时要关闭的核心
    public static int defaultCpuChooses[] = new int[10];//默认核心的开关


    public static boolean  isAtuoRemoveIce = true;
    public static boolean  isAtuoStopIce = false;
    public static boolean  isSetTimeStopByZW = false;//取消强退是否需要指纹
    public static boolean  isSetTimeStopByPWD = false;//取消强退是否需要密码
    public static boolean  isAtuoOffScIce = false;

    public static boolean isHomeClick = false;
    public static boolean isAlreadyOpenCamera = false;
    public static boolean isRecentClick = false;
    public static boolean isKillRun = false;
    public static boolean isExitRemoveRecent = true;
    public static boolean isNotExitAudioPlay = false;

    public  static boolean isCharging = false;
    public  static boolean isHookOff = false;//是否接电话中
    public static boolean  isSaveBackLog = false;

    public static boolean  isCameraMode = false;//拍照模式是否开启
    public static boolean  isOffScreenLockCpu = false;
    public static boolean  isBatteryLowLockCPU = false;
    public static boolean  isChargingNotLockCPU = false;
    public static boolean  isLockUnlockCPU = false;
    public static boolean  isOpenCamera = false;//拍照模式开启后是否执行了
    public static boolean  isShowActInfo = false;
    public static boolean  isAlwaysKillOff = false;
    public static boolean  isSetTimeStopNotShowDialog = false;


    public static boolean  isUICONTROLOPEN = false;
    public static boolean  isRoundCorOpen = false;
    public static boolean  isLightMsg = false;
    public static boolean  isLightCall = false;
    public static boolean  isLightRandomMode = false;
    public static boolean  isLightScOn = false;
    public static boolean  isLightMusic = false;
    public static boolean  isLightCharge = false;
    public static boolean  isLightAnimScale = false;
    public static boolean  isFlashNofity = false;
    public static boolean  isFlashCall = false;
    public static boolean  isFlashInOffSc = false;
    public static long lastScreenOnTime = 0;
    public static boolean  isNeedFloatOnSys = false;
    public static String musicPlayPkg = "";
    //是否有XP浮动权限?
    public static boolean  isHasXPFloatVewPermission = false;
    //是否有系统浮动权限?
    public static boolean  isHasSysFloatVewPermission = false;
    public static boolean isNeedGetFloatPremission = false;
    public static int  lightSize = 8;
    public static int  lightWidth = 100;
    public static int  lightSpeed = 1;
    public static int  lightOffset = 0;
    public static int  lightShowMode = 0;//0左右  1上下  2上下左右
    public static int  lightXiaoGuo = 0;//
    public static int cpuNum = 8;
    public static String lightColor = "#01d8ff";
    public StringBuilder backLog = new StringBuilder();

    long offScTime = 0;
    long onScTime = 0;
    public static long batteryOnScTime = 0;
    public static long batteryOffScTime = 0;
    public static boolean isScreenOff = false;

    MyDozeService myDozeService;
    AppStartService appStartService;
    AdService adService;
    SharedPreferences settings;
    SharedPreferences forceStopPrefs;
    SharedPreferences autoStartPrefs;
    SharedPreferences recentPrefs;
    SharedPreferences barPrefs;
    SharedPreferences muControlPrefs;
    SharedPreferences cpuPrefs;
    SharedPreferences skipDialogPrefs;
    SharedPreferences setTimeStopPrefs;
    public static String homePkg = "";
    public static  long watchDogstartTime = 0;
    private AudioManager audioManager;
    private AppLoaderUtil appLoader;
    public static boolean isNeedAMSReadLoad = false;
    SharedPreferences.OnSharedPreferenceChangeListener prefsListener =  new SharedPreferences.OnSharedPreferenceChangeListener(){
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(!key.endsWith("lockapp")&&!sharedPreferences.equals(settings)){
                SharedPrefsUtil.getInstance(WatchDogService.this).isPrefsChange = true;
                Log.i("CONTROL","shareprefs  变化了");

            }
            if(key.endsWith("broad")||
                    key.endsWith("service")||
                    key.endsWith("lockapp")||
                    key.endsWith("notstop")||
                    key.endsWith(Common.PREFS_SETTING_ISAUTOSTARTNOTNOTIFY)||
                    key.endsWith(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC)||
                    key.endsWith("autostart")){
                isNeedAMSReadLoad = true;
                Log.i("CONTROL","isNeedAMSReadLoad  变化了");
            }
        }
    };
    public static WatchDogService instence;
    public static WatchDogService getInstence(){
        return instence;
    };


    AppLoaderUtil.LoadAppCallBack appLoadListener = new AppLoaderUtil.LoadAppCallBack(){
        @Override
        public void onLoadAppFinish() {
            initSomeData();
        }
        @Override
        public void onRuningStateChange() {

        }
        @Override
        public void onLoadLocalAppFinish() {
            if(AppLoaderUtil.allAppInfos.size()>0&&!appLoader.isAppChange){
                appLoader.loadAppSetting();
            }else if(appLoader.isAppChange||AppLoaderUtil.allAppInfos.size()==0){
                appLoader.loadApp();
            }
        }
        @Override
        public void onLoadAppSettingFinish() {
            initSomeData();
        }
    };
    @Override
    public void onCreate() {
        super.onCreate();
//        if(TEST){
//            return;
//        }
        PowerManager pm =  (PowerManager) (getApplicationContext().getSystemService(Context.POWER_SERVICE));
        instence = this;
        isKillRun = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOff = !pm.isInteractive();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        AppLoaderUtil.allAppStateInfos.clear();
        settings = SharedPrefsUtil.getInstance(this).settings;//SharedPrefsUtil.getPreferences(this,Common.PREFS_APPSETTINGS);//getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        setTimeStopPrefs = SharedPrefsUtil.getInstance(this).setTimeStopPrefs;//SharedPrefsUtil.getPreferences(this,Common.PREFS_APPSETTINGS);//getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        watchDogstartTime = System.currentTimeMillis();
        Log.i("CONTROL", "WatchDogService启动后台进程监听服务");
        forceStopPrefs = SharedPrefsUtil.getInstance(this).forceStopPrefs;//SharedPrefsUtil.getPreferences(getApplication(),Common.PREFS_FORCESTOPNAME);
        autoStartPrefs = SharedPrefsUtil.getInstance(this).autoStartNetPrefs;//SharedPrefsUtil.getPreferences(getApplication(),Common.PREFS_AUTOSTARTNAME);
        barPrefs = SharedPrefsUtil.getInstance(this).uiBarPrefs;//SharedPrefsUtil.getPreferences(getApplication(),Common.IPREFS_MUBEILIST);
        muControlPrefs = SharedPrefsUtil.getInstance(this).modPrefs;//SharedPrefsUtil.getPreferences(getApplication(),Common.PREFS_SETTINGNAME);
        recentPrefs = SharedPrefsUtil.getInstance(this).recentPrefs;//SharedPrefsUtil.getPreferences(getApplication(),Common.IPREFS_RECENTLIST);
        cpuPrefs = SharedPrefsUtil.getInstance(this).cpuPrefs;//SharedPrefsUtil.getPreferences(this,Common.PREFS_SETCPU);//getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        skipDialogPrefs = SharedPrefsUtil.getInstance(this).skipDialogPrefs;//SharedPrefsUtil.getPreferences(this,Common.PREFS_SETCPU);//getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        muBeiPrefs.edit().clear().commit();
        MainActivity.THEME_COLOR = SharedPrefsUtil.getInstance(this).uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_COLOR,"#1a9dac");
        MainActivity.THEME_TEXT_COLOR = SharedPrefsUtil.getInstance(this).uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_TEXT_COLOR,"#1a9dac");
        MainActivity.THEME_BG_COLOR = SharedPrefsUtil.getInstance(this).uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_BG_COLOR,"#f4f4f4");
        MainActivity.isNightMode = SharedPrefsUtil.getInstance(this).uiBarPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_MODE,false);
        MainActivity.isAutoChange = SharedPrefsUtil.getInstance(this).uiBarPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_AUTOCHANGEMODE,false);
        if(!MainActivity.isNightMode&&MainActivity.isAutoChange){
            int hour = Integer.parseInt(TimeUtil.changeMils2String(System.currentTimeMillis(),"H"));
            if(hour>=22||hour<7){
                MainActivity.isNightMode = true;
            }
        }
        int saveCode = settings.getInt(Common.BUILDCODE,0);
        if(saveCode!= BuildConfig.VERSION_CODE) {
            TestDataInit.init(this);
        }
        removeImp();

        appLoader = AppLoaderUtil.getInstance(WatchDogService.this);
        appLoader.addAppChangeListener(appLoadListener);
        appLoader.loadLocalApp();

        muControlPrefs.registerOnSharedPreferenceChangeListener(prefsListener);
        forceStopPrefs.registerOnSharedPreferenceChangeListener(prefsListener);
        autoStartPrefs.registerOnSharedPreferenceChangeListener(prefsListener);
        setTimeStopPrefs.registerOnSharedPreferenceChangeListener(prefsListener);
        settings.registerOnSharedPreferenceChangeListener(prefsListener);

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
        ifliter.addAction("com.click369.control.offcpu");
        ifliter.addAction("com.click369.control.home");
        ifliter.addAction("com.click369.control.settimestopapp");
        ifliter.addAction("com.click369.control.canceltimestopapp");
        ifliter.addAction("com.click369.control.removeapp");
        ifliter.addAction("com.click369.control.addapp");
        ifliter.addAction("com.click369.control.appconfigclose");
        ifliter.addAction("com.click369.control.openmain");
        ifliter.addAction("com.click369.control.cpubatterychange");
        ifliter.addAction("com.click369.control.audiofocus");
        ifliter.addAction("com.click369.control.amsstoppkg");
        ifliter.addAction("com.click369.controlbp.zhendong");
        ifliter.addAction("com.click369.control.callflash.start");
        ifliter.addAction("com.click369.control.callflash.stop");
        ifliter.addAction("com.click369.control.clearcache");
        ifliter.addAction("com.click369.control.amsalert");
        ifliter.addAction("com.click369.control.hookok");
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

        if(!WatchDogService.ISHOOKOK){
            sendBroadcast(new Intent("com.click369.control.ams.checkhook"));
        }

        regBatteryBroad();
        if (settings.getBoolean(Common.ALLSWITCH_DOZE,true)) {
            myDozeService = new MyDozeService(this);

        }
        if (settings.getBoolean(Common.ALLSWITCH_AUTOSTART_LOCK,true)||
                settings.getBoolean(Common.ALLSWITCH_RECNETCARD,true)) {
            appStartService = new AppStartService(this);
        }
        if (settings.getBoolean(Common.ALLSWITCH_ADSKIP,true)) {
            adService = new AdService(this);
        }
        itemCpuIsOpen = settings.getBoolean(Common.ALLSWITCH_CPUSET,true);
        if (itemCpuIsOpen) {
            isLockUnlockCPU = cpuPrefs.getBoolean(Common.PREFS_SETCPU_LOCKUNLOCK, false);
            delayOffCpuTime =cpuPrefs.getInt(Common.PREFS_SETCPU_OFFSCREENDELAY,60)*60;
            isCameraMode = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CAMERAMODE,false);
            isBatteryLowLockCPU = cpuPrefs.getBoolean(Common.PREFS_SETCPU_BATTERYBELOWOPEN,false);
            isChargingNotLockCPU = cpuPrefs.getBoolean(Common.PREFS_SETCPU_CHARGINGNOTSTOP,false);
            cpuBatteryBelow = cpuPrefs.getInt(Common.PREFS_SETCPU_BATTERYBELOWCOUNT,15);
            isOffScreenLockCpu = cpuPrefs.getBoolean(Common.PREFS_SETCPU_OFFSCREENOPEN, false);
            offScCpuChooses = CPUSetView.getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_OFFSCREENCORECOUNT);
            defaultCpuChooses = CPUSetView.getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_DEFAULTCORECOUNT);
            batteryLowCpuChooses = CPUSetView.getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_BATTERYLOWCORECOUNT);
        }
        isUICONTROLOPEN = settings.getBoolean(Common.ALLSWITCH_UI,true);
        if(isUICONTROLOPEN){
            WatchDogService.isRoundCorOpen = barPrefs.getBoolean(Common.PREFS_SETTING_UI_ROUNDOPEN,false);
            WatchDogService.isLightMsg = barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTMSG,false);
            WatchDogService.isLightCall = barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTCALL,false);
            WatchDogService.isLightRandomMode = barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTMODE,false);
            WatchDogService.isLightScOn = barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTSCON,false);
            WatchDogService.isLightMusic = barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTMUSIC,false);
            WatchDogService.isLightCharge = barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTCHARGE,false);
            WatchDogService.isLightAnimScale = barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTANIMSCALE,false);
            WatchDogService.lightSize = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTSIZE,8);
            WatchDogService.lightWidth = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTWIDTH,100);
            WatchDogService.lightOffset = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTOFFSET,0);
            WatchDogService.lightSpeed = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTSPEED,1);
            WatchDogService.lightShowMode = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTWEIZHI,0);
            WatchDogService.lightColor = barPrefs.getString(Common.PREFS_SETTING_UI_LIGHTCOLOR,"#01d8ff");
            WatchDogService.lightXiaoGuo = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTXIAOGUO,0);

            WatchDogService.isFlashNofity = barPrefs.getBoolean(Common.PREFS_SETTING_UI_FLASHNOTIFY,false);
            WatchDogService.isFlashCall = barPrefs.getBoolean(Common.PREFS_SETTING_UI_FLASHNOTIFY,false);
            WatchDogService.isFlashInOffSc = barPrefs.getBoolean(Common.PREFS_SETTING_UI_FLASHSHOWINOFFSC,false);
            WatchDogService.isNeedFloatOnSys = barPrefs.getBoolean(Common.PREFS_SETTING_UI_ISNEEDFLOATONSYS,false);
        }
//        Log.i("CONTROL","create WatchDogService.isNeedFloatOnSys  "+ WatchDogService.isNeedFloatOnSys);
        BaseActivity.isZhenDong = settings.getBoolean(Common.PREFS_SETTING_ZHENDONG,true);
        isAlwaysKillOff = settings.getBoolean(Common.PREFS_SETTING_ISALWAYSKILLOFF,false);
        isSetTimeStopNotShowDialog = settings.getBoolean(Common.PREFS_SETTING_SETTIMESTOPNOTSHOWDIALOG,false);

        startIdleState();
        initCpuAndSel(true);
        File batteryFile = new File(getFilesDir(),"batteryinfo");
        if(batteryFile.exists()){
            try {
                HashMap<Integer,Long> temps = (HashMap<Integer,Long>)FileUtil.readObj(batteryFile.getAbsolutePath());
                batteryInfos.putAll(temps);
            }catch (Throwable e){
            }
        }
        offScTime = System.currentTimeMillis();
        onScTime = System.currentTimeMillis();
        batteryOffScTime = System.currentTimeMillis();
        batteryOnScTime = System.currentTimeMillis();
        lastScreenOnTime = System.currentTimeMillis();

        rcs = new RoundedCornerService(this);
        if(SystemClock.uptimeMillis()<1000*60*2){
            WakeLockActivity.delTempFiles(this);
            AlarmActivity.delTempFiles(this);
        }
        checkRoot();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        if(TEST){
//            return super.onStartCommand(intent, Service.START_FLAG_RETRY, startId);
//        }
        Log.i("CONTROL","watchdog 加载数据");
        //给系统发通知修改
        XposedUtil.reloadInfos(this,autoStartPrefs,muControlPrefs,settings,skipDialogPrefs,barPrefs);
        appLoader.reloadRunList();
        isKillRun = true;
        homePkg = WatchDogService.getDefaultHome(this);
        isNotNeedAccessibilityService = settings.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true);
        isXPstop = settings.getBoolean(Common.PREFS_SETTING_STOPAPPBYXP,true);

        if(!MainActivity.isModuleActive()){
            isNotNeedAccessibilityService = false;
            isXPstop = false;
        }
//        Log.i("CONTROL","MainActivity.isModuleActive() "+MainActivity.isModuleActive()+" isNotNeedAccessibilityService "+isNotNeedAccessibilityService);
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
        isNotExitAudioPlay = settings.getBoolean(Common.PREFS_SETTING_ISNOTEXITAUDIOPLAY,false);
        isCheckTimeOutApp = settings.getBoolean(Common.PREFS_SETTING_ISCHECKTIMEOUTAPP,false);
        AppStartService.isOffScLockApp = settings.getBoolean(Common.PREFS_SETTING_OFFSCLOCK,true);
        itemBACKHOMEOFFIsOpen = settings.getBoolean(Common.ALLSWITCH_BACKSTOP_MUBEI,true);
        itemAUTOSTARTLOCKIsOpen = settings.getBoolean(Common.ALLSWITCH_AUTOSTART_LOCK,true);
        itemCpuIsOpen = settings.getBoolean(Common.ALLSWITCH_CPUSET,true);
        itemOtherIsOpen = settings.getBoolean(Common.ALLSWITCH_OTHERS,true);
        if (itemAUTOSTARTLOCKIsOpen) {
            Set<String> autoKeys = autoStartPrefs.getAll().keySet();
            for (String key : autoKeys) {
                if(key.endsWith("/lockok")){
                    autoStartPrefs.edit().remove(key).commit();
                }
            }
        }
        if (settings.getBoolean(Common.ALLSWITCH_DOZE,true)){
            myDozeService.loadWhilteList();
        }

        settings.edit().remove("homeapk").commit();
        autoStartPrefs.edit().remove("homeapk").commit();
        settings.edit().putString("nowhomeapk",WatchDogService.getDefaultHome(this)).commit();
        autoStartPrefs.edit().putString("nowhomeapk",WatchDogService.getDefaultHome(this)).commit();

        if(itemBACKHOMEOFFIsOpen){
            if (!NotificationService.isNotifyRunning&&!isNotNeedAccessibilityService){
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
        }
        return super.onStartCommand(intent, Service.START_FLAG_RETRY, startId);
    }


    private void checkRoot(){
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    isRoot = ShellUtils.checkRootPermission();//runing==null||runing.length()==0?
                    if(isRoot){
                        Thread.sleep(50);
                        ShellUtilDoze.execCommand("dumpsys deviceidle whitelist +com.click369.controlbp");
                    }else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if(!MainActivity.isModuleActive()){
                                        Toast.makeText(WatchDogService.this,"没有获取到ROOT权限并且XP未激活,返回强退、熄屏强退、冷藏室等功能无法使用",Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(WatchDogService.this,"没有获取到ROOT权限,部分功能将无法使用",Toast.LENGTH_LONG).show();
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    boolean isAlreadInitCpu =false;
    private void initCpuAndSel(final boolean isneedwait){
        new Thread(){
            @Override
            public void run() {
                try {
                    Log.i("CONTROL","准备执行CPU核心控制");
                    if(isneedwait){
                        Thread.sleep(1000*10);
                    }
                    if(itemOtherIsOpen){
                        boolean isOpen = SELinuxUtil.isSELOpen();
                        if(!settings.getBoolean(Common.PREFS_SETTING_SELOPEN,false)&&isOpen){
                            SELinuxUtil.closeSEL();
                        }else if(!isOpen&&settings.contains(Common.PREFS_SETTING_SELOPEN)&&settings.getBoolean(Common.PREFS_SETTING_SELOPEN,false)){
                            SELinuxUtil.openSEL();
                        }
                    }
                    if(isLockUnlockCPU) {
                        if(isneedwait) {
                            Thread.sleep(1000 * 60);
                        }else{
                            Thread.sleep(500);
                        }
                        cpuNum = cpuPrefs.getInt(Common.PREFS_SETCPU_NUMBER,8);
                        File file1 = new File(FileUtil.FILEPATH, "unlock_lowbatter_core");
                        File file2 = new File(FileUtil.FILEPATH, "lock_lowbatter_core");
                        if (!file1.exists() || !file2.exists()) {
                            FileUtil.init();
                            FileUtil.copyAssets(WatchDogService.this, "unlock_lowbatter_core", FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core");
                            FileUtil.copyAssets(WatchDogService.this, "lock_lowbatter_core", FileUtil.FILEPATH + File.separator + "lock_lowbatter_core");
                        }
                        String s = isLockUnlockCPU ? "sh " + FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core" : "sh " + FileUtil.FILEPATH + File.separator + "lock_lowbatter_core";
                        ShellUtilNoBackData.execCommand(s);
                        if(isneedwait) {
                            Thread.sleep(1000 * 60);
                        }else{
                            Thread.sleep(500);
                        }
                        if(cpuPrefs.getBoolean(Common.PREFS_SETCPU_AUTOSTART, false)) {
//                            isBootStartLockCpu = true;
//                            changeCpu();
                            CpuUtil.changeCpu();
                        }
                    }
                    Log.i("CONTROL","结束执行CPU核心控制");
                }catch (Exception e){
                    e.printStackTrace();
                }
                isAlreadInitCpu = true;
            }
        }.start();
    }


    private void startIdleState(){
        try {
            HashSet<String> pkgs = new HashSet<String>();
            HashSet<String> runs = PackageUtil.getRunngingAppList(WatchDogService.this);
            for (String s : runs) {
                AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(s)?AppLoaderUtil.allHMAppInfos.get(s):null;
                if (ai!=null&&(ai.isHomeIdle)){
                    pkgs.add(s);
                }
            }
            Intent intent = new Intent("com.click369.control.uss.setappidle");
            intent.putExtra("pkgs", pkgs);
            intent.putExtra("idle", true);
            sendBroadcast(intent);
            updateUI();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initSomeData(){
        Set<String>  launcherPkgs = getLauncherPackageName(this);
        ArrayList<String> imePkgs = getInputPackageName(this);
        notLightPkgs.addAll(imePkgs);
        notLightPkgs.add("android");
        notLightPkgs.add("com.android.dialer");
        for(String l:launcherPkgs){
            if(AppLoaderUtil.allAppStateInfos.containsKey(l)){
                AppLoaderUtil.allAppStateInfos.get(l).isHomePkg = true;
            }
        }
        for(String i:imePkgs){
            if(AppLoaderUtil.allAppStateInfos.containsKey(i)) {
                AppLoaderUtil.allAppStateInfos.get(i).isImePkg = true;
            }
        }
        handler.removeCallbacks(initNetRunnable);
        handler.postDelayed(initNetRunnable,2000);

    }
    Runnable initNetRunnable = new Runnable() {
        @Override
        public void run() {
            sendInitNetData(WatchDogService.this);
        }
    };
    public static void sendInitNetData(Context c){
        HashSet<Integer> wifiList = new HashSet<Integer>();
        HashSet<Integer> mobileList = new HashSet<Integer>();
        for(AppInfo ai:AppLoaderUtil.allAppInfos){
            if(ai.isPriWifiPrevent){
                wifiList.add(ai.uid);
            }
            if(ai.isPriMobilePrevent){
                mobileList.add(ai.uid);
            }
        }
        Log.i("CONTROL","init net control "+wifiList.size()+"  "+mobileList.size());
        Intent intent = new Intent("com.click369.control.ams.net.init");
        intent.putExtra("wifilist",wifiList);
        intent.putExtra("mobilelist",mobileList);
        c.sendBroadcast(intent);
    }


    private void removeImp(){
        SharedPreferences.Editor autoEdit = autoStartPrefs.edit();
        autoEdit.remove(homePkg+"/autostart");
        autoEdit.remove(homePkg+"/stopapp");
        autoEdit.remove(homePkg+"/lockok");
        autoEdit.commit();

        SharedPreferences.Editor forceEdit = forceStopPrefs.edit();
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
        cEdit.remove("com.fkzhang.wechatxposed/service");
        cEdit.remove("com.fkzhang.wechatxposed/broad");
        cEdit.remove("com.fkzhang.wechatxposed/wakelock");
        cEdit.remove("com.fkzhang.wechatxposed/alarm");
        cEdit.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(TEST){
//            return;
//        }
        Log.e("DOZE", "结束后台进程监听服务");
        if(hkr!=null){
            this.unregisterReceiver(hkr);
        }
        if(apr!=null){
            this.unregisterReceiver(apr);
        }
        if(rcs!=null){
            rcs.destroy();
        }
        unRegBatteryBroad();
        appLoader.removeAppChangeListener(appLoadListener);
        if(myDozeService !=null){
            myDozeService.destory();
        }
        if(appStartService !=null){
            appStartService.destory();
        }
        if(adService !=null){
            adService.destory();
        }
        forceStopPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        autoStartPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        setTimeStopPrefs.unregisterOnSharedPreferenceChangeListener(prefsListener);
        isKillRun = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    Runnable exit = new Runnable() {
        @Override
        public void run() {
            stopMubeiIdleApp();
        }
    };
    private void stopMubeiIdleApp(){
        if(!itemBACKHOMEOFFIsOpen){
            return;
        }
        HashSet<String> confirmdels = new HashSet<>();
        HashSet<String> runs = new HashSet<String>(AppLoaderUtil.runLists);
        for(String key:runs){
            AppStateInfo asi = AppLoaderUtil.allAppStateInfos.get(key);
            AppInfo ai = AppLoaderUtil.allHMAppInfos.get(key);
            if(ai==null||ai.isblackControlXp){
                continue;
            }
            if(asi==null){
                AppLoaderUtil.allAppStateInfos.put(key,new AppStateInfo());
            }
            boolean backTimeOK = (System.currentTimeMillis()-asi.backStartTime>=delayBackTime*950||(isScreenOff&&delayBackTime<7));
            boolean homeTimeOK = (System.currentTimeMillis()-asi.homeStartTime>=delayHomeTime*950||(isScreenOff&&delayHomeTime<7));
            if(((isNotExitAudioPlay&&!asi.isHasAudioFocus)||!isNotExitAudioPlay)&&
              (!ai.isNotifyNotExit||(ai.isNotifyNotExit&&!asi.isHasNotify))&&
               !key.equals(nowPkgName)){
                boolean isInBack = false;
                if(isAtuoStopIce&&asi.isOpenFromIceRome&&backTimeOK&&asi.isPressKeyBack){
                    isInBack = true;
                    asi.isOpenFromIceRome = false;
                    ShellUtilNoBackData.execCommand("pm disable " + key);
                    Log.i("CONTROL","BACK ICE "+ai.packageName);
                }else if(ai.isBackForceStop&&backTimeOK){
                    if(asi.isPressKeyBack) {
                        isInBack = true;
                        XposedStopApp.stopApk(key, WatchDogService.this);
                        WatchDogService.sendRemoveRecent(key,WatchDogService.this);
                        if(isAlreadyOpenCamera&&ai.packageName.toLowerCase().contains("camera")){
                            isAlreadyOpenCamera = false;
                            cleanCache(false);
                        }
                        Log.i("CONTROL","BACK STOP IMMD "+ai.packageName);
                    }else if(!asi.isPressKeyHome){
                        isInBack = true;
                        if(!isXPstop&&!isNotNeedAccessibilityService){
                            XposedStopApp.stopApk(key, WatchDogService.this);
                            Log.i("CONTROL","BACK STOP IMMD "+ai.packageName);
                        }else{
//                            if(!"com.coolapk.market".equals(key)){
                                confirmdels.add(key);
//                            }
                        }
                        Log.i("CONTROL","BACK STOP CONFIRM "+ai.packageName);
                    }
                }else if(ai.isBackMuBei&&backTimeOK){
                    isInBack = true;
                    startMuBei(key);
                    Log.i("CONTROL","BACK MUBEI "+ai.packageName);
                }


                if((isScreenOff||isAlwaysKillOff)&&(asi.isReadyOffMuBei||asi.isReadyOffStop||asi.isReadyOffIce)&&
                        (System.currentTimeMillis()-asi.offScTime>delayOffTime*1000)){
                    if(isAtuoOffScIce&&asi.isOpenFromIceRome){
                        asi.isOpenFromIceRome = false;
                        asi.isReadyOffIce = false;
                        ShellUtilNoBackData.execCommand("pm disable " + key);
                        Log.i("CONTROL","OFF ICE "+ai.packageName);
                    }else if(ai.isOffscForceStop){
                        asi.isReadyOffStop = false;
                        XposedStopApp.stopApk(key, WatchDogService.this);
                        WatchDogService.sendRemoveRecent(key,WatchDogService.this);
                        Log.i("CONTROL","OFF STOPA "+ai.packageName);
                    }else if(!asi.isInMuBei&&ai.isOffscMuBei){
                        asi.isReadyOffMuBei = false;
                        startMuBei(key);
                        Log.i("CONTROL","OFF MUBEI "+ai.packageName);
                    }
                }

                if(!isInBack&&!asi.isInMuBei&&ai.isHomeMuBei&&homeTimeOK){
                    startMuBei(key);
                    Log.i("CONTROL","HOME MUBEI "+ai.packageName);
                }else if(!isInBack&&!asi.isInIdle&&ai.isHomeIdle&&homeTimeOK){
                    startIdle(key);
                    Log.i("CONTROL","HOME IDLE "+ai.packageName);
                }
            }
        }


        if(confirmdels.size()>0){
            Intent intentDel = new Intent("com.click369.control.ams.confirmforcestop");
            intentDel.putExtra("pkgs",confirmdels);
            sendBroadcast(intentDel);
            confirmdels.clear();
        }
    }


    Runnable flashRun = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent("com.click369.control.sysui.msgflash");
            sendBroadcast(intent);
        }
    };

    static final String SYSTEM_REASON = "reason";
    static final String SYSTEM_HOME_KEY = "homekey";
    static final String SYSTEM_RECENT_KEY = "recent";
    static final String SYSTEM_RECENTAPPS_KEY = "recentapps";
    public  static long lastPressedHome = 0;
    public  static long lastCleanTime = 0;
    public  static long lastHomeClick = 0,lastRecentClick = 0;
    class HomeKeyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)||
                    "com.click369.control.keylistener".equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_REASON);
                if (SYSTEM_HOME_KEY.equals(reason)||reason==null) {
                    lastHomeClick = System.currentTimeMillis();
                    Log.i("CONTROL","按下HOME "+openPkgName +" isRecentClick "+isRecentClick);
                    isHomeClick = true;
                    lastPressedHome = System.currentTimeMillis();
                }else if(SYSTEM_RECENT_KEY.equals(reason)||SYSTEM_RECENTAPPS_KEY.equals(reason)){
                    lastRecentClick = System.currentTimeMillis();
                    isRecentClick = true;
                    Log.i("CONTROL","按下最近任务 "+openPkgName);
                    if(AppLoaderUtil.allAppStateInfos.containsKey(openPkgName)){
                        AppLoaderUtil.allAppStateInfos.get(openPkgName).isPressKeyHome = true;
                        AppLoaderUtil.allAppStateInfos.get(openPkgName).isPressKeyBack = false;
                        AppLoaderUtil.allAppStateInfos.get(openPkgName).homeStartTime = System.currentTimeMillis();
                    }
                }
            }else if (Intent.ACTION_SCREEN_OFF.equals(action)&&!isHookOff) {
                batteryOffScTime = System.currentTimeMillis();
                offScTime = System.currentTimeMillis();
                if(!isScreenOff){
                    Long nowOn = batteryInfos.containsKey(101)?batteryInfos.get(101):0L;
                    nowOn+= System.currentTimeMillis()-batteryOnScTime;
                    batteryInfos.put(101,nowOn);
                }
                isScreenOff = true;
//                Log.i("CONTROL","息屏 "+AppLoaderUtil.allHMAppInfos.size()+"  "+AppLoaderUtil.allAppStateInfos.size());
                if(LightView.isStart){
                    ScreenLightServiceUtil.sendHideLight(WatchDogService.this);
                }
                if (itemBACKHOMEOFFIsOpen) {
                    handler.removeCallbacks(exit);
                    boolean isHasOffStopOrMuBei = false;
                    boolean isHasBackApp = false;
                    boolean isHasHomeApp = false;
                    HashSet<String> checkApps = new HashSet<String>();
                    appLoader.reloadRunList();
                    HashSet<String> runs = new HashSet<String>(AppLoaderUtil.runLists);

                    for (String key : runs) {
                        AppInfo ai = AppLoaderUtil.allHMAppInfos.get(key);
                        AppStateInfo asi = AppLoaderUtil.allAppStateInfos.get(key);
                        if (ai == null||nowPkgName.equals(key)) {
                            continue;
                        }
                        if (asi == null) {
                            AppLoaderUtil.allAppStateInfos.put(key, new AppStateInfo());
                        }
                        ai.isRunning = true;
                        if ((ai.isBackForceStop||ai.isOffscForceStop) &&
                                ((isNotExitAudioPlay && !asi.isHasAudioFocus) || !isNotExitAudioPlay) &&
                                !ai.isNotStop&&
                                (!ai.isNotifyNotExit || (ai.isNotifyNotExit && !asi.isHasNotify))) {
                            checkApps.add(key);
                        }
                        if (itemAUTOSTARTLOCKIsOpen&&ai.isLockApp) {
                            autoStartPrefs.edit().remove(key + "/lockok").commit();
                        }
//                        if(delayOffTime>120){
                            if(ai.isOffscForceStop&&(!asi.isReadyOffStop||isAlwaysKillOff)){//如果息屏  则设定要杀死的状态
                                asi.isReadyOffStop = true;
                                asi.offScTime = System.currentTimeMillis();
                            }else if(ai.isOffscMuBei&&(!asi.isReadyOffMuBei||isAlwaysKillOff)){//如果息屏  则设定要杀死的状态
                                asi.isReadyOffMuBei = true;
                                asi.offScTime = System.currentTimeMillis();
                            }else if(asi.isOpenFromIceRome&&((!asi.isReadyOffIce&&isAtuoOffScIce)||isAlwaysKillOff)){//如果息屏  则设定要杀死的状态
                                asi.isReadyOffIce = true;
                                asi.offScTime = System.currentTimeMillis();
                            }
//                        }

                        if (!isHasOffStopOrMuBei) {
                            isHasOffStopOrMuBei = ai.isRunning && (ai.isOffscMuBei || ai.isOffscForceStop|| (asi.isOpenFromIceRome&&isAtuoOffScIce));
                        }
                        if (!isHasBackApp) {
                            isHasBackApp = ai.isRunning && (ai.isBackForceStop || ai.isBackMuBei|| (asi.isOpenFromIceRome&&isAtuoStopIce&&ai.isBackForceStop))  && asi.isPressKeyBack;
                        }
                        if (!isHasHomeApp) {
                            isHasHomeApp = ai.isRunning && (ai.isHomeMuBei || ai.isHomeIdle) && asi.isPressKeyHome;
                        }
//                        Log.i("CONTROL","息屏   "+key+" isHasOffStopOrMuBei "+isHasOffStopOrMuBei+" isHasBackApp "+isHasBackApp+"  "+nowPkgName);
                    }
                    //检测超时强退的应用  间隔三小时执行一次
                    if (isCheckTimeOutApp && SystemClock.elapsedRealtime() - lastCheckTimeOutAppTime > 1000 * 60 * 60 * 2) {
                        lastCheckTimeOutAppTime = SystemClock.elapsedRealtime();
                        if (checkApps.size() > 0) {
                            Intent intent1 = new Intent("com.click369.control.ams.checktimeoutapp");
                            intent1.putExtra("pkgs", checkApps);
                            intent1.putExtra("timeout", 1000 * 60 * 60 * 11);
                            sendBroadcast(intent1);
                        }
                    }
                    if ((delayBackTime < 7 && isHasBackApp) || (delayHomeTime < 7 && isHasHomeApp)) {
                        stopMubeiIdleApp();
                        Log.i("CONTROL", "息屏立刻 处理返回相关的APP");
                    }
                    if (delayBackTime > 7 && isHasBackApp) {
                        setAlarm("com.click369.control.exit", delayBackTime);
                        Log.i("CONTROL", "息屏" + delayBackTime + "后 处理返回相关的APP");
                    }
                    if (delayHomeTime > 7 && isHasHomeApp) {
                        setAlarm("com.click369.control.exit", delayHomeTime);
                        Log.i("CONTROL", "息屏" + delayHomeTime + "后 处理HOME相关的APP");
                    }
                    if (isHasOffStopOrMuBei) {
                        if (delayOffTime < 3) {
                            stopMubeiIdleApp();
                            Log.i("CONTROL", "息屏 立即 处理息屏相关的APP");
                        } else {
                            setAlarm("com.click369.control.offsccloseapp", delayOffTime);
                            Log.i("CONTROL", "息屏" + delayHomeTime + "后 处理息屏相关的APP");
                        }
                    }
                }else if(itemAUTOSTARTLOCKIsOpen){
                    appLoader.reloadRunList();
                    HashSet<String> runs = new HashSet<String>(AppLoaderUtil.runLists);
                    for (String key : runs) {
                        if (autoStartPrefs.contains(key + "/lockok")) {
                            autoStartPrefs.edit().remove(key + "/lockok").commit();
                        }
                    }
                }
                if (isLockUnlockCPU&&
                        isOffScreenLockCpu&&
                        (!isChargingNotLockCPU||(isChargingNotLockCPU&&!isCharging))){//息屏锁定cpu
                    if(System.currentTimeMillis()-exitCpuLockTime<1000*60&&!isExitCpuLockOpenApp){//直接开启息屏锁定CPU
                        setAlarm("com.click369.control.offcpu",10);
                        Log.i("CONTROL","息屏立刻进入CPU锁核");
                    }else{
                        setAlarm("com.click369.control.offcpu",delayOffCpuTime);
                        Log.i("CONTROL","息屏"+(delayOffCpuTime)+"秒后进入CPU锁核");
                    }
                    isExitCpuLockOpenApp = false;
                }
                if (myDozeService !=null) {
                    myDozeService.screenOff();
                }
                if ((itemOtherIsOpen&&isOffClean)||isAlreadyOpenCamera){
                    if(delayCleanTime<=6||isAlreadyOpenCamera){
                        cleanCache(false);
                    }else{
                        setAlarm("com.click369.control.offcleancache",delayCleanTime);
                    }
                }
            }else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                batteryOnScTime = System.currentTimeMillis();
                lastScreenOnTime = System.currentTimeMillis();
                onScTime = System.currentTimeMillis();
               if(isScreenOff){
                   Long nowOff = batteryInfos.containsKey(102)?batteryInfos.get(102):0L;
                   nowOff+= System.currentTimeMillis()-batteryOffScTime;
                   batteryInfos.put(102,nowOff);
               }
               if(isLightMusic&&audioManager.isMusicActive()&& !TextUtils.isEmpty(musicPlayPkg)){
                   ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_MUSIC,WatchDogService.this);
               }else if(NotificationService.notifyLights.size()>0){
                   NotificationService.notifyLights.clear();
                   ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_MSG,WatchDogService.this);
               }else if(isLightScOn){
                   ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_SCON,WatchDogService.this);
               }
                isScreenOff = false;
                if(AppLoaderUtil.allHMAppInfos.size()==0){
                    appLoader.setIsAppChange(true);
                    appLoader.loadApp();
                }
                if(!isAlreadInitCpu&&SystemClock.uptimeMillis()>1000*60*3){
                    initCpuAndSel(false);
                }
                if (myDozeService !=null) {
                    myDozeService.screenOn();
                }
                if (itemBACKHOMEOFFIsOpen) {
                    if(!isAlwaysKillOff) {
                        cleanAlarm("com.click369.control.offsccloseapp");
                    }
                }
                if (itemOtherIsOpen&&isOffClean && delayCleanTime > 3) {
                    cleanAlarm("com.click369.control.offcleancache");
                }
                if(isLockUnlockCPU&&
                        isOffScreenLockCpu&&
                        (!isChargingNotLockCPU||(isChargingNotLockCPU&&!isCharging))){//关闭cpu
                    if(!isStartOffCpu){
                        cleanAlarm("com.click369.control.offcpu");
                        Log.i("CONTROL","亮屏取消定锁核时器");
                    }else{
                        exitCpuLockTime = System.currentTimeMillis();
                        CpuUtil.changeCpu();
                        Log.i("CONTROL","亮屏退出锁核");
                    }
                    isStartOffCpu = false;
                }
            }else if ("com.click369.control.offsccloseapp".equals(action)) {
                stopMubeiIdleApp();
            }else if ("com.click369.control.cpubatterychange".equals(action)) {
                if(isBatteryLowLockCPU){
//                    regBatteryBroad();
                    if (batteryPer<=cpuBatteryBelow&&!isCharging){
                        cpuBatteryLowIsAlreadyLock = true;
                    }else if(batteryPer>cpuBatteryBelow){
                        cpuBatteryLowIsAlreadyLock = false;
                    }
                    CpuUtil.changeCpu();
                }else if(!isBatteryLowLockCPU){
                    if(cpuBatteryLowIsAlreadyLock){
                        cpuBatteryLowIsAlreadyLock = false;
                        CpuUtil.changeCpu();
                    }
                }
            }else if (Intent.ACTION_POWER_CONNECTED.equals(action)) {
                isCharging = true;
                if(isLockUnlockCPU){
                    if(cpuBatteryLowIsAlreadyLock){
                        cpuBatteryLowIsAlreadyLock = false;
                    }
                    CpuUtil.changeCpu();
                }
                if(isLightCharge){
                    ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_CHARGE,WatchDogService.this);
                }

                updateUI();
            }else if (Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
                isCharging = false;
                batteryInfos.clear();
                batteryOnScTime = System.currentTimeMillis();
                batteryOffScTime = System.currentTimeMillis();
                File batteryFile = new File(getFilesDir(),"batteryinfo");
                if(batteryFile.exists()){
                    batteryFile.delete();
                }
                CpuUtil.changeCpu();
                lastBatteryPer = -1;
                startIdleState();
                if(isLightCharge){
                    ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_CHARGE,WatchDogService.this);
                }
            }else if (intent.getAction().equals("com.click369.control.notify")) {
                if (!NotificationService.isNotifyRunning) {
                    final String pkg = intent.getStringExtra("pkg").trim();
                    String type = intent.getStringExtra("type");
                    int flags = intent.getIntExtra("flags",-1);
                    boolean clearable = intent.getBooleanExtra("clearable",true);
                    Log.i("CONTROL","notify  "+type+"  "+pkg+"  flags "+flags+" clearable "+clearable);
                    if ("add".equals(type)) {
                        NotificationService.addNotify(WatchDogService.this, pkg,flags,clearable);
                    } else {
                        NotificationService.removedNotify(WatchDogService.this, pkg);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(pkg)?AppLoaderUtil.allAppStateInfos.get(pkg):new AppStateInfo();
                                 if(asi.isNotifyNotMubei&&asi.isHasNotify&&!openPkgName.equals(pkg)){
                                     asi.isNotifyNotMubei = false;
                                    startMuBei(pkg);
                                }
                                if(asi.isNotifyNotIdle&&asi.isHasNotify&&!openPkgName.equals(pkg)){
                                    asi.isNotifyNotIdle = false;
                                    startIdle(pkg);
                                }
                            }
                        },300);
                    }
                }
            }else if ("com.click369.control.test".equals(action)) {
                try {
                    openApp(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if ("com.click369.control.openmain".equals(action)) {
                Intent intent1 = new Intent(WatchDogService.this,MainActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent1);
            }else if ("com.click369.control.callflash.start".equals(action)) {
                handler.postDelayed(flashRun,1000);
                handler.postDelayed(flashRun,4000);
                handler.postDelayed(flashRun,9000);
                handler.postDelayed(flashRun,16000);
                handler.postDelayed(flashRun,22000);
            }else if ("com.click369.control.callflash.stop".equals(action)) {
                handler.removeCallbacks(flashRun);
            }else if("com.click369.control.exit".equals(action)||
                    "com.click369.control.home".equals(action)){
                stopMubeiIdleApp();
            }else if("com.click369.control.offcpu".equals(action)&&!isStartOffCpu){//息屏锁定cpu
                isStartOffCpu = true;
                cleanAlarm("com.click369.control.offcpu");
                Log.i("CONTROL","息屏定时已到准备进入CPU锁核");
                CpuUtil.changeCpu();
            }else if("com.click369.control.offcleancache".equals(action)){
                cleanCache(intent.hasExtra("data"));
            }else if("com.click369.control.hookok".equals(action)){
                ISHOOKOK = true;
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
                            if(data==0){//最近任务移除杀死取消
                                String mpkg = name.replace("/notclean", "");
                                if(AppLoaderUtil.allHMAppInfos.containsKey(mpkg)){
                                    AppLoaderUtil.allHMAppInfos.get(mpkg).isRecentNotClean = false;
                                }
                            }else if(data==1){//最近任务移除杀死取消
                                String mpkg = name.replace("/forceclean", "");
                                if(AppLoaderUtil.allHMAppInfos.containsKey(mpkg)){
                                    AppLoaderUtil.allHMAppInfos.get(mpkg).isRecentForceClean = false;
                                }
                            }
                        }else{
                            recentPrefs.edit().putBoolean(name,true).commit();
                            if(data==0){//最近任务移除杀死取消
                                String mpkg = name.replace("/notclean", "");
                                if(AppLoaderUtil.allHMAppInfos.containsKey(mpkg)){
                                    AppLoaderUtil.allHMAppInfos.get(mpkg).isRecentNotClean = true;
                                    AppLoaderUtil.allHMAppInfos.get(mpkg).isRecentForceClean = false;
                                }
                                recentPrefs.edit().remove(mpkg+"/forceclean").commit();
                            }else if(data==1){//最近任务移除杀死添加
                                String mpkg = name.replace("/forceclean", "");
                                if(AppLoaderUtil.allHMAppInfos.containsKey(mpkg)){
                                    AppLoaderUtil.allHMAppInfos.get(mpkg).isRecentForceClean = true;
                                    AppLoaderUtil.allHMAppInfos.get(mpkg).isRecentNotClean = false;
                                }
                                recentPrefs.edit().remove(mpkg+"/notclean").commit();
                            }
                        }
                    }else{
                        Intent intent1 = new Intent("com.click369.control.ams.changepersistent");
                        if (autoStartPrefs.contains(name)){
                            autoStartPrefs.edit().remove(name).commit();
                            intent1.putExtra("persistent",false);
                            String mpkg = name.replace("/notstop", "");
                            if(AppLoaderUtil.allHMAppInfos.containsKey(mpkg)){
                                AppLoaderUtil.allHMAppInfos.get(mpkg).isNotStop = false;
                            }
                        }else{
                            autoStartPrefs.edit().putBoolean(name,true).commit();
                            intent1.putExtra("persistent",true);
                            String mpkg = name.replace("/notstop", "");
                            if(AppLoaderUtil.allHMAppInfos.containsKey(mpkg)){
                                AppLoaderUtil.allHMAppInfos.get(mpkg).isNotStop = true;
                            }
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
            } else if("com.click369.control.removeapp".equals(action)){//删除应用
                removeAppList.add(intent.getStringExtra("pkg"));
                handler.removeCallbacks(removeapp);
                handler.postDelayed(removeapp,2000);
            } else if("com.click369.control.addapp".equals(action)){//新安装应用
                final String pkg = intent.getStringExtra("pkg");
                if(!removeAppList.contains(pkg)){
                    handler.removeCallbacks(loadApp);
                    handler.postDelayed(loadApp,500);
                }
                //刷新图标
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            final PackageManager pm = WatchDogService.this.getPackageManager();
                            final PackageInfo packageInfo = pm.getPackageInfo(pkg,PackageManager.GET_ACTIVITIES);
                            appLoader.loadAppImage(packageInfo,pm,true);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();
                //是否自动打开控制面板
                if (!removeAppList.contains(pkg)&&settings.getBoolean(Common.PREFS_SETTING_NEWAPPAUTOOPENCONTROL,false)){
                    newInstallAppList.add(pkg);
                    Intent intent1 = new Intent(WatchDogService.this, AppConfigActivity.class);
                    intent1.putExtra("pkg",pkg);
                    intent1.putExtra("from","newapp");
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
            }else if("com.click369.control.amsstoppkg".equals(action)){//从AMS杀死的应用 返回确认
                HashSet<String> stopPkgs = (HashSet<String>)intent.getSerializableExtra("pkgs");
                for(String pkg:stopPkgs){
                    AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(pkg)?AppLoaderUtil.allHMAppInfos.get(pkg):null;
                    if(ai==null){
                        continue;
                    }
                    AppLoaderUtil.runLists.remove(pkg);
                    AppLoaderUtil.allAppStateInfos.put(pkg,new AppStateInfo());
                    ai.isRunning = false;
                    sendRemoveRecent(pkg,WatchDogService.this);
                    if(ai.isLockApp){
                        SharedPrefsUtil.getInstance(WatchDogService.this).autoStartNetPrefs.edit().remove(pkg+"/lockok").commit();
                    }
                }
            } else if("com.click369.control.audiofocus".equals(action)){
                if(intent.hasExtra("pkgs")){
                    String pkg = intent.getStringExtra("pkg");
                    int state = intent.getIntExtra("state",2);
                    HashSet<String> pkgs = (HashSet<String>)intent.getSerializableExtra("pkgs");
                    AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(pkg)?AppLoaderUtil.allHMAppInfos.get(pkg):null;
                    if(ai==null){
                        return;
                    }
                    AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(pkg)?AppLoaderUtil.allAppStateInfos.get(pkg):new AppStateInfo();
                    if(pkgs.contains(pkg)&&state!=2){
                        asi.isHasAudioFocus = true;
                        if(isLightMusic&&ai.getAppName()!=null&&ai.getAppName().contains("音乐")){
                            musicPlayPkg = ai.getPackageName();
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_MUSIC,WatchDogService.this);
                        }
                    }else{
                        if(isLightMusic&&ai.getAppName()!=null&&ai.getAppName().contains("音乐")){
                            musicPlayPkg = "";
                            ScreenLightServiceUtil.sendHideLight(WatchDogService.this);
                        }
                        if(!asi.isHasAudioFocus){
                            return;
                        }
                        asi.isHasAudioFocus = false;
                        if((ai.isBackMuBei||ai.isBackForceStop)&&!asi.isPressKeyHome&&!nowPkgName.equals(pkg)){
                            if(isScreenOff){
                                cleanAlarm("com.click369.control.exit");
                                setAlarm("com.click369.control.exit", delayBackTime);
                            }else{
                                handler.removeCallbacks(exit);
                                handler.postDelayed(exit,delayBackTime*1000);
                            }
                        }else if(isScreenOff&&(ai.isOffscForceStop||ai.isOffscMuBei)&&!nowPkgName.equals(pkg)&&(System.currentTimeMillis()-offScTime<delayOffTime)){
                            cleanAlarm("com.click369.control.exit");
                            setAlarm("com.click369.control.exit", 30);
                        }else if((ai.isHomeMuBei||ai.isHomeIdle)&&!nowPkgName.equals(pkg)){
                            if(isScreenOff){
                                cleanAlarm("com.click369.control.exit");
                                setAlarm("com.click369.control.exit", delayHomeTime);
                            }else{
                                handler.removeCallbacks(exit);
                                handler.postDelayed(exit,delayHomeTime*1000);
                            }
                        }
                        Log.i("CONTROL",pkg+ " 丢失音频焦点");
                    }
                    Log.i("CONTROL","音频焦点个数："+pkgs.size());
                }
            } else if("com.click369.control.canceltimestopapp".equals(action)){
                final String pkg = intent.getStringExtra("pkg");
                myHandler.removeMessages(pkg.hashCode());
                AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(pkg)?AppLoaderUtil.allHMAppInfos.get(pkg):new AppInfo();
                if (!intent.getBooleanExtra("isdelay",false)) {
                    if(ai.isSetTimeStopOneTime){
                        ai.isSetTimeStopOneTime = false;
                        ai.isSetTimeStopApp = false;
                        ai.setTimeStopAppTime = 0;
                        SharedPrefsUtil.getInstance(WatchDogService.this).setTimeStopPrefs.edit().remove(pkg+"/one").commit();
                    }
                }
            }else if("com.click369.control.settimestopapp".equals(action)){//定时关闭app
                String pkg = intent.getStringExtra("pkg");
                AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(pkg)?AppLoaderUtil.allAppStateInfos.get(pkg):new AppStateInfo();
                AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(pkg)?AppLoaderUtil.allHMAppInfos.get(pkg):new AppInfo();
                if(ai.setTimeStopAppTime>0&&asi.isSetTimeStopAlreadStart&&ai.isSetTimeStopApp){
                    try {
                        if (appLoader.runLists.contains(pkg)) {
                            if (isScreenOff||isSetTimeStopNotShowDialog){
                                XposedStopApp.stopApk(pkg,WatchDogService.this);
                                if(ai.isSetTimeStopOneTime){
                                    ai.isSetTimeStopOneTime = false;
                                    ai.isSetTimeStopApp = false;
                                    ai.setTimeStopAppTime = 0;
                                    SharedPrefsUtil.getInstance(WatchDogService.this).setTimeStopPrefs.edit().remove(pkg+"/one").commit();
                                }
                            }else{
                                Intent intent1 =new Intent(WatchDogService.this, ShowDialogActivity.class);
                                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent1.putExtra("pkg",pkg);
                                startActivity(intent1);

                                Message message = Message.obtain();
                                message.what = pkg.hashCode();
                                message.obj = pkg;
                                myHandler.sendMessageDelayed(message,1000*31);
                            }
                        }
                    }catch (RuntimeException arg1){
                        arg1.printStackTrace();
                    }
                }else{
                    if (!ai.isSetTimeStopApp) {
                        asi.isSetTimeStopAlreadStart = false;
                    }
                }
            }else if("com.click369.controlbp.zhendong".equals(action)){
                BaseActivity.zhenDong(WatchDogService.this);
            }else if("com.click369.control.clearcache".equals(action)){
                Toast.makeText(context,intent.getStringExtra("info"),Toast.LENGTH_LONG).show();
            }else if("com.click369.control.amsalert".equals(action)){
                String appName = PackageUtil.getAppNameByPkg(WatchDogService.this,intent.getStringExtra("pkg"));
                if(!preventErrAppNames.contains(appName)){
                    preventErrAppNames.add(appName);
                    Toast.makeText(context,appName+intent.getStringExtra("info"),Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    final Handler myHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String pkg = (String)msg.obj;
            XposedStopApp.stopApk(pkg,WatchDogService.this);
            AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(pkg)?AppLoaderUtil.allHMAppInfos.get(pkg):new AppInfo();
            if(ai.isSetTimeStopOneTime){
                ai.isSetTimeStopOneTime = false;
                ai.isSetTimeStopApp = false;
                ai.setTimeStopAppTime = 0;
                SharedPrefsUtil.getInstance(WatchDogService.this).setTimeStopPrefs.edit().remove(pkg+"/one").commit();
            }
        }
    };


    Runnable removeapp = new Runnable() {
        @Override
        public void run() {
            if (removeAppList.size()>0){
                AddAppReceiver.removePkg = "";
                for(String pkg:removeAppList){
                    SharedPrefsUtil.getInstance(WatchDogService.this).clearAppSettings(new AppInfo(pkg,pkg),true);
                }
                removeAppList.clear();
                appLoader.setIsAppChange(true);
                appLoader.setIsPrefsChange(true);
                appLoader.loadApp();
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
//                handler.removeCallbacks(loadApp);
//                handler.postDelayed(loadApp,520);
            }
            AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(addPkg)?AppLoaderUtil.allHMAppInfos.get(addPkg):new AppInfo();
            if(ai.isNotStop&&(Intent.ACTION_PACKAGE_REPLACED.equals(action)||Intent.ACTION_PACKAGE_REMOVED.equals(action))){
                Intent intent1 = new Intent("com.click369.control.ams.changepersistent");
                intent1.putExtra("persistent",false);
                intent1.putExtra("pkg",addPkg);
                context.sendBroadcast(intent1);
                Log.i("CONTROL","AddAppReceiver2  "+action);
            }

        }
    }

    private void startCameraChangeMode(){
        isOpenCamera = true;
        CpuUtil.changeCpu();
    }

    private void exitCameraChangeMode(){
        isOpenCamera = false;
        CpuUtil.changeCpu();
    }

    boolean isInHome = false;
    Runnable goHome = new Runnable() {
        @Override
        public void run() {
            if(!homePkg.equals(nowPkgName)||isInHome){
                return;
            }
            exitCameraChangeMode();
            isInHome = true;
            AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(openPkgName)?AppLoaderUtil.allAppStateInfos.get(openPkgName):new AppStateInfo();
            if (isHomeClick){//按home
                if(isSaveBackLog) {
                    if (!openPkgName.equals("")) {
                        backLog.append(FileUtil.getLog("按下home键保留 " + PackageUtil.getAppNameByPkg(WatchDogService.this, openPkgName)));
                    }
                    FileUtil.writeLog(backLog.toString());
                }
                asi.isPressKeyHome = true;
                asi.homeStartTime = System.currentTimeMillis();
                asi.isPressKeyBack = false;
            }else{//按返回
                asi.isPressKeyHome = false;
                asi.backStartTime = System.currentTimeMillis();
                asi.isPressKeyBack = true;
                if(isSaveBackLog){
                    StringBuilder sbb = new StringBuilder();
                    if (!openPkgName.equals("")) {
                        sbb.append("按下返回键退出" + PackageUtil.getAppNameByPkg(WatchDogService.this, openPkgName));
                    }
                    AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(openPkgName)?AppLoaderUtil.allHMAppInfos.get(openPkgName):null;
                    if(ai!=null&&ai.isBackForceStop&&itemBACKHOMEOFFIsOpen){
                        sbb.append(" ").append(delayBackTime+"秒后杀死 "+PackageUtil.getAppNameByPkg(WatchDogService.this,openPkgName));
                    }
                    if (sbb.length()>0) {
                        backLog.append(FileUtil.getLog(sbb.toString()));
                    }
                    FileUtil.writeLog(backLog.toString());
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
            if(itemBACKHOMEOFFIsOpen){//总开关是否打开
                if(delayBackTime>delayHomeTime){
                    if(delayBackTime-delayHomeTime<10){
                        handler.postDelayed(exit, delayBackTime==0?1*1000:delayBackTime*1000);
                    }else{
                        handler.postDelayed(exit, delayHomeTime==0?1*1000:delayHomeTime*1000);
                        handler.postDelayed(exit, delayBackTime==0?1*1000:delayBackTime*1000);
                    }
                }else{
                    if(delayHomeTime-delayBackTime<10){
                        handler.postDelayed(exit, delayHomeTime==0?1*1000:delayHomeTime*1000);
                    }else{
                        handler.postDelayed(exit, delayHomeTime==0?1*1000:delayHomeTime*1000);
                        handler.postDelayed(exit, delayBackTime==0?1*1000:delayBackTime*1000);
                    }
                }
            }
            openPkgName = "";
            isRecentClick = false;
            isHomeClick = false;
        }
    };

    public void openApp(Intent intent) throws Exception{
        String apk = intent.getStringExtra("pkg");
        String cls = intent.getStringExtra("class");

        if(isShowActInfo){
            Intent intent1 = new Intent("com.click369.control.float.actinfo");
            intent1.putExtra("data",cls);
            sendBroadcast(intent1);
        }
        if (TextUtils.isEmpty(apk)||
            ContainsKeyWord.isContainsNotListenerApk(apk)||
            isScreenOff||
                (Common.PACKAGENAME.equals(apk)&&(AppConfigActivity.class.getName().equals(cls)||
                        UnLockActivity.class.getName().equals(cls)))){
            if ("com.topjohnwu.magisk".equals(apk)||"eu.chainfire.supersu".equals(apk)){
                nowPkgName = apk;
            }
            return;
        }else if(apk.equals(nowPkgName)){//包名未变//com.eg.android.AlipayGphone  com.alipay.mobile.scan.as.main.MainCaptureActivity
            if(ContainsKeyWord.fullCpuCoreApp.containsKey(apk)&&ContainsKeyWord.fullCpuCoreApp.get(apk).contains(cls)){
                startCameraChangeMode();
            }else if(isOpenCamera&&isCameraMode&&ContainsKeyWord.fullCpuCoreApp.containsKey(apk)){//拍照模式 开启大核心
                exitCameraChangeMode();
            }
            return;
        }
        String from = intent.getStringExtra("from");
        Log.i("CONTROL","---openApp："+ apk  +"  nowPkgName "+nowPkgName+"  "+isHomeClick+"  from "+from);//+
        AppLoaderUtil.runLists.add(apk);
        if (homePkg.equals(apk)){//返回了桌面
            isInHome = false;
//            openPkgs.clear();
            handler.postDelayed(goHome,50);
//            stopAppStartFlag = true;
        }else{//打开了应用
            isInHome = false;
            if (openPkgName!=null&&apk!=null&&!openPkgName.equals(apk)){
                if(itemBACKHOMEOFFIsOpen&&ISHOOKOK){
                    AppInfo oldai = AppLoaderUtil.allHMAppInfos.containsKey(openPkgName)?AppLoaderUtil.allHMAppInfos.get(openPkgName):new AppInfo();
                    if(oldai.isBackForceStop){
                        handler.postDelayed(exit, delayBackTime==0?1*1000:delayBackTime*1000);
                    }else if(oldai.isHomeIdle||oldai.isHomeMuBei){
                        handler.postDelayed(exit, delayHomeTime==0?1*1000:delayHomeTime*1000);
                    }
                }
                AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(apk)?AppLoaderUtil.allAppStateInfos.get(apk):new AppStateInfo();
                AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(apk)?AppLoaderUtil.allHMAppInfos.get(apk):new AppInfo();
                ai.openCount++;
                ai.lastOpenTime = System.currentTimeMillis();
                ai.isRunning = true;

                asi.isPressKeyBack = false;
                asi.isPressKeyHome = false;
                asi.isReadyOffMuBei = false;
                asi.isReadyOffStop = false;
                asi.isReadyOffIce = false;
                isExitCpuLockOpenApp = true;
                if(isSaveBackLog) {
                    backLog.append(FileUtil.getLog("打开" + PackageUtil.getAppNameByPkg(WatchDogService.this,apk)));
                }
                if (ai.isSetTimeStopApp){
//                    asi.isSetTimeStopApp = true;
//                    asi.setTimeStopTime = ai.setTimeStopAppTime;
                    Log.i("CONTROL","启动定时结束 "+ai.packageName);//+
                    if(!asi.isSetTimeStopAlreadStart){
                        setAlarmWithCode("com.click369.control.settimestopapp",apk,ai.setTimeStopAppTime*60,apk.hashCode());
                        asi.isSetTimeStopAlreadStart = true;
                    }
                }
//                stopAppStartFlag = false;
                if (apk.toLowerCase().contains("camera")){
                    isAlreadyOpenCamera = true;
                }
                if (ai.isLockApp&&!AppStartService.isOffScLockApp&&autoStartPrefs.contains(openPkgName+"/lockok")){
                    autoStartPrefs.edit().remove(openPkgName+"/lockok").commit();
                }
                if(myDozeService !=null){
                    myDozeService.checkOpenApp(apk,true);
                }
                if(itemBACKHOMEOFFIsOpen){
                    if (ai.isOffscMuBei||
                            ai.isBackMuBei||
                            ai.isHomeMuBei||
                            asi.isInMuBei||
                            ai.isInMuBei){
                        BaseActivity.sendBroadAMSRemovePkg(WatchDogService.this,ai.packageName);
//                        Log.i("CONTROL","remove mubei "+ai.packageName);
                        asi.isInMuBei = false;
                        ai.isInMuBei = false;
                    }
                    asi.isInIdle = false;
                }

                if(isCameraMode&&(nowPkgName.toLowerCase().contains("camera")||nowPkgName.toLowerCase().contains("lineageos.snap"))) {//拍照模式 关闭大核心
                    exitCameraChangeMode();
                }
                if(isCameraMode&&(apk.toLowerCase().contains("camera")||apk.toLowerCase().contains("lineageos.snap"))){//拍照模式 开启大核心
                    startCameraChangeMode();
                }
            }
            if("com.android.systemui".equals(openPkgName)){
                handler.postDelayed(resetHome,500);
            }
            openPkgName = apk;
            isHomeClick = false;
            isRecentClick = false;
        }
        nowPkgName = apk;
    }
    Runnable resetHome= new Runnable() {
        @Override
        public void run() {
            isHomeClick = false;
        }
    };

    public static HashSet<String> getLauncherPackageName(Context context) {
        HashSet<String> packageNames = new HashSet<String>();
        String hpkg = getDefaultHome(context);
        if(!TextUtils.isEmpty(hpkg)){
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
        if (isExitRemoveRecent) {
            if(openPkgName.equals("com.android.systemui")){
                return;
            }
            AppInfo ai = AppLoaderUtil.allHMAppInfos.containsKey(pkg)?AppLoaderUtil.allHMAppInfos.get(pkg):new AppInfo();
            if(ai.isRecentNotClean){
                return;
            }
            Intent intent = new Intent("com.click369.control.ams.delrecent");
            intent.putExtra("pkg", pkg);
            cxt.sendBroadcast(intent);
            Log.i("CONTROL","准备移除最近任务"+pkg);
        }
    }

    public void startMuBei(final String muBeiApk){
        try {
//            muBeiPrefs.edit().putInt(muBeiApk,0).commit();
            AppStateInfo asi = AppLoaderUtil.allAppStateInfos.get(muBeiApk);
            AppInfo ai = AppLoaderUtil.allHMAppInfos.get(muBeiApk);
            asi.isInMuBei = true;
            asi.isReadyOffMuBei = false;
            ai.isInMuBei = true;
            Intent intent = new Intent("com.click369.control.ams.forcestopservice");
            intent.putExtra("pkg", muBeiApk);
            sendBroadcast(intent);
//            if("com.tencent.mm".equals(muBeiApk)){
//                return;
//            }
            Intent intent1 = new Intent("com.click369.control.uss.setappidle");
            intent1.putExtra("pkg", muBeiApk);
            intent1.putExtra("idle", true);
            sendBroadcast(intent1);

            updateUI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startIdle(final String idleApk){
        try {
            AppStateInfo asi = AppLoaderUtil.allAppStateInfos.get(idleApk);
            asi.isInIdle = true;
//            Log.i("CONTROL","startIdle "+idleApk);
            Intent intent1 = new Intent("com.click369.control.uss.setappidle");
            intent1.putExtra("pkg", idleApk);
            intent1.putExtra("idle", true);
            sendBroadcast(intent1);

            updateUI();
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
        isAlreadyOpenCamera = false;
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
            appLoader.setIsAppChange(true);
            appLoader.setIsPrefsChange(true);
            appLoader.loadApp();
        }
    };

    int lastBatteryPer = -1;
    class BatteryReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(Intent.ACTION_BATTERY_CHANGED.equals(action)){
                int current = intent.getExtras().getInt("level");// 获得当前电量
                int total = intent.getExtras().getInt("scale");// 获得总电量
                batteryPer = current * 100 / total;
                if (isLockUnlockCPU&&isBatteryLowLockCPU&&!isCharging&&batteryPer<=100){
                    if (batteryPer<=cpuBatteryBelow&&!cpuBatteryLowIsAlreadyLock){
                        cpuBatteryLowIsAlreadyLock = true;
                        CpuUtil.changeCpu();
                    }else if(batteryPer>cpuBatteryBelow&&cpuBatteryLowIsAlreadyLock){
                        cpuBatteryLowIsAlreadyLock = false;
                        CpuUtil.changeCpu();
                    }
                }
                if(!isCharging&&lastBatteryPer!=batteryPer||lastBatteryPer==-1){
                    if(batteryPer==100){
                        batteryInfos.clear();
                        batteryOnScTime = System.currentTimeMillis();
                        batteryOffScTime = System.currentTimeMillis();
                        File batteryFile = new File(getFilesDir(),"batteryinfo");
                        if(batteryFile.exists()){
                            batteryFile.delete();
                        }
                    }
                    batteryInfos.put(batteryPer,System.currentTimeMillis());
                    if(!isScreenOff){
                        Long nowOn = batteryInfos.containsKey(101)?batteryInfos.get(101):0L;
                        nowOn+= System.currentTimeMillis()-batteryOnScTime;
                        batteryInfos.put(101,nowOn);//亮屏
                        batteryOnScTime = System.currentTimeMillis();
                    }else{
                        Long nowOff = batteryInfos.containsKey(102)?batteryInfos.get(102):0L;
                        nowOff+= System.currentTimeMillis()-batteryOffScTime;
                        batteryInfos.put(102,nowOff);
                        batteryOffScTime = System.currentTimeMillis();
                    }
                    if(batteryPer%2==1){
                        File batteryFile = new File(getFilesDir(),"batteryinfo");
                        FileUtil.writeObj(batteryInfos,batteryFile.getAbsolutePath());
                    }
                }
                lastBatteryPer = batteryPer;
            }
        }
    }
    BatteryReceiver batteryReceiver;
    public void regBatteryBroad(){
        if (batteryReceiver==null) {
            batteryReceiver = new BatteryReceiver();
            Intent intent = this.registerReceiver(batteryReceiver,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||status == BatteryManager.BATTERY_STATUS_FULL;
        }
    }
    public void unRegBatteryBroad(){
        if (batteryReceiver!=null) {
            this.unregisterReceiver(batteryReceiver);
        }
        batteryReceiver = null;
    }

    public void updateUI(){
        if(MainActivity.isShow){
            handler.removeCallbacks(updateUI);
            handler.postDelayed(updateUI,1000);
        }
    }
    Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent("com.click369.control.watchdogtellupdateui");
            sendBroadcast(intent);
        }
    };
}
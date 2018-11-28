package com.click369.controlbp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.click369.controlbp.BuildConfig;
import com.click369.controlbp.R;
import com.click369.controlbp.adapter.NavInfoAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.WhiteApp;
import com.click369.controlbp.common.TestDataInit;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.receiver.BootStartReceiver;
import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.BackupRestoreUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.GetPhoto;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtilDoze;
import com.click369.controlbp.util.ShellUtils;
import com.click369.controlbp.util.ShortCutUtil;
import com.click369.controlbp.util.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    public static boolean isUIRun = false;
    public static HashMap<String,WhiteApp> whiteApps = new HashMap<String,WhiteApp>();
    public RelativeLayout mainRL;
    private AppCompatTextView menuTv;
    public ControlFragment controlFragment;
    public ForceStopFragment forceStopFragment;
    public IFWFragment ifwFragment;
    public IceUnstallFragment iceRoomFragment;
    public JuanZengFragment juanZengFragment;
    public DozeFragment dozeFragment;
    public OtherFragment otherFragment;
    public SettingFragment settingFragment;
    public AppStartFragment appStartFragment;
    public UIControlFragment uiControlFragment;
    public RecentFragment recentFragment;
    public AdFragment adFragment;
    public CPUSetFragment cpuFragment;
    public QuestionFragment questionFragment;
    public Fragment chooseFragment;
//    public SharedPreferences modPrefs,wakeLockPrefs,alarmPrefs,forceStopPrefs,muBeiPrefs,settings,ifwCountPrefs,uiBarPrefs,autoStartNetPrefs,recentPrefs,dozePrefs,adPrefs,skipDialogPrefs,pmPrefs;
    private NavigationView navigationView;
    private int page = 0;
    public static String runing = "";
    public static String COLOR = "#00ccff";
    public static String COLOR_RUN = "#40d0b7";
    public static String COLOR_MUBEI = "#FF8C00";
    public static String COLOR_IDLE = "#6dcb21";
    public static boolean isNightMode = false;
    public static boolean isLinkStopAndAuto = true;
    public static boolean isLinkStopAndRemoveStop = true;
    public static boolean isLinkRecentAndAuto = true;
    public static boolean isLinkRecentAndNotStop = false;
    private Toolbar toolbar;
    private BackupRestoreUtil backupRestoreUtil;
    public DrawerLayout drawer;
    public LinearLayout navInfoLL;
    public TextView navInfoTitle,navInfoSamllTitle;
    public ListView navInfoListView;
    public NavInfoAdapter navAdapter;
    private MyUpdateListReceiver updateReceiver;
    public static boolean isRoot = true;
    private File muBeiPrefsFile = null;
    private long lastMuBeiUpdate = 0;
    public File bgFile,bgBlurFile;
    public GetPhoto getPhoto;
    public static HashSet<String> pkgIdleStates = new HashSet<String>();
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isUIRun = true;
//        ifwCountPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_APPIFWCOUNT);// getApplicationContext().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
//        modPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_SETTINGNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
//        wakeLockPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_WAKELOCKNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
//        alarmPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_ALARMNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
//        forceStopPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_FORCESTOPNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
//        muBeiPrefs = SharedPrefsUtil.getPreferences(this,Common.IPREFS_MUBEILIST);
//        autoStartNetPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_AUTOSTARTNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
//        uiBarPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_UIBARLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
//        settings = SharedPrefsUtil.getPreferences(this,Common.PREFS_APPSETTINGS);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        recentPrefs = SharedPrefsUtil.getPreferences(this,Common.IPREFS_RECENTLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        dozePrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_DOZELIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        adPrefs = SharedPrefsUtil.getPreferences(this,Common.IPREFS_ADLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        skipDialogPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_SKIPDIALOG);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        pmPrefs = SharedPrefsUtil.getPreferences(this,Common.IPREFS_PMLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        muBeiPrefsFile = new File(getFilesDir() + "/../shared_prefs/" + Common.IPREFS_MUBEILIST + ".xml");
        backupRestoreUtil = new BackupRestoreUtil(this);
        isNightMode = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_THEME_MODE,false);
        boolean isAutoChange = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_THEME_AUTOCHANGEMODE,false);
        if(!isNightMode&&isAutoChange){
            int hour = Integer.parseInt(TimeUtil.changeMils2String(System.currentTimeMillis(),"H"));
            if(hour>=22||hour<7){
                isNightMode = true;
            }
        }
        sharedPrefs.settings.edit().putBoolean("nowmode",isNightMode).commit();
        if(isNightMode){
            setTheme(R.style.AppTheme_NoActionBarDark);
        }
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mainRL =(RelativeLayout) findViewById(R.id.main_fragment_rl);
        mainRL.setBackgroundColor(isNightMode?Color.BLACK:Color.WHITE);
        menuTv = (AppCompatTextView) findViewById(R.id.main_menu_actv);
        menuTv.setVisibility(View.VISIBLE);
        menuClick();
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
//        DrawerAllScreen.setDrawerLeftEdgeSize(this,drawer,0.8f);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navInfoLL = (LinearLayout)navigationView.findViewById(R.id.nav_info_ll);
        navInfoTitle = (TextView)navigationView.findViewById(R.id.nav_info_title);
        navInfoSamllTitle = (TextView)navigationView.findViewById(R.id.nav_info_smalltitle);
        navInfoListView = (ListView) navigationView.findViewById(R.id.nav_info_listview);
        TextView subTitle = (TextView)navigationView.getHeaderView(0).findViewById(R.id.head_subtitle_tv);
        subTitle.setText("版本:"+PackageUtil.getAppVersionName(this)+"\n交流群:624055295");//+"\n朋友搞机公众号:MRYZSL"
        navAdapter = new NavInfoAdapter(this);
        navInfoListView.setAdapter(navAdapter);
        navInfoLL.setVisibility(View.GONE);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
            }
            @Override
            public void onDrawerClosed(View drawerView) {
                navInfoLL.setVisibility(View.GONE);
            }
            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        navInfoLL.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
        int[][] states = new int[][]{new int[]{ -android.R.attr.state_checked},new int[]{android.R.attr.state_checked} };
        int[] colors = new int[]{ getResources().getColor(R.color.uncheck_color),  getResources().getColor(R.color.checked_color) };
        if(isNightMode){
            toolbar.setBackgroundColor(getResources().getColor(R.color.darkblack));
//            navigationView.setBackgroundResource(R.drawable.saidbg);
            navigationView.setBackgroundColor(getResources().getColor(R.color.darkblack));
            navigationView.getHeaderView(0).setBackgroundColor(getResources().getColor(R.color.darkblack));
            navInfoLL.setBackgroundColor(getResources().getColor(R.color.darkblack));
//            navigationView.getHeaderView(0).setBackgroundResource(R.drawable.saidbg);
            colors = new int[]{ getResources().getColor(R.color.uncheck_colordark),  getResources().getColor(R.color.checked_color) };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.darkblack));
                window.setNavigationBarColor(Color.BLACK);
            }
        }else{
            navigationView.setBackgroundResource(R.drawable.slider_bg1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                if (new File(Environment.getExternalStorageDirectory(),"zroms").exists()){
                    //底部导航栏
                    window.setNavigationBarColor(Color.WHITE);
                }
            }
        }
        ColorStateList csl = new ColorStateList(states, colors);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        navigationView.setNavigationItemSelectedListener(this);
        initMenuView();
        toolbar.setTitle("");
        if (!isModuleActive()){
            menuTv.setAlpha(0.5f);
            menuTv.setEnabled(false);
            showNotActive();
        }else{
            int saveCode = sharedPrefs.settings.getInt(Common.BUILDCODE,0);
            if(saveCode!=BuildConfig.VERSION_CODE){
                sharedPrefs.settings.edit().putInt(Common.BUILDCODE,BuildConfig.VERSION_CODE).commit();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AlertUtil.showAlertMsg(MainActivity.this,getString(R.string.mainalert));
                    }
                },500);
            }
        }

        runing = PackageUtil.getRunngingApp(this);
        initView();
        new Thread(){
            @Override
            public void run() {
                checkRoot();
                TestDataInit.init(MainActivity.this);
                isZhenDong = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_ZHENDONG,true);
                sharedPrefs.settings.edit().remove("homeapk").commit();
                sharedPrefs.autoStartNetPrefs.edit().remove("homeapk").commit();
                sharedPrefs.settings.edit().putString("nowhomeapk",WatchDogService.getDefaultHome(MainActivity.this)).commit();
                sharedPrefs.autoStartNetPrefs.edit().putString("nowhomeapk",WatchDogService.getDefaultHome(MainActivity.this)).commit();
            }
        }.start();
        updateReceiver = new MyUpdateListReceiver();
        getPhoto = new GetPhoto(this);
        bgFile = new File(FileUtil.IMAGEPATH,"bg.jpg");
        bgBlurFile = new File(FileUtil.IMAGEPATH,"bg_blur.jpg");
        if (bgFile!=null&&bgFile.exists()){
            Drawable d = Drawable.createFromPath(bgBlurFile.exists()?bgBlurFile.getAbsolutePath():bgFile.getAbsolutePath());
            if(d!=null){
                d.setAlpha((int)(sharedPrefs.settings.getInt(Common.PREFS_SETTING_OTHER_BGBRIGHT,100)*2.55));
                mainRL.setBackground(d);
            }
        }
//        Intent intent11 = new Intent("com.click369.control.ams.removemubei");
//        Intent intent11 = new Intent("com.click369.control.ams.forcestopservice");
//        intent11.putExtra("apk",Common.PACKAGENAME);
//        intent11.putExtra("pkg",Common.PACKAGENAME);
//        sendBroadcast(intent11);
//        PackageUtil.getIntentFliterByName(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortCutUtil.initDynamicShortcuts(this);
        }

    }

    private void initView(){
        controlFragment = new ControlFragment();
        forceStopFragment = new ForceStopFragment();
        ifwFragment = new IFWFragment();
        recentFragment = new RecentFragment();
        appStartFragment = new AppStartFragment();
        iceRoomFragment = new IceUnstallFragment();
        dozeFragment = new DozeFragment();
        uiControlFragment = new UIControlFragment();
        adFragment = new AdFragment();
        cpuFragment = new CPUSetFragment();
        otherFragment = new OtherFragment();
        settingFragment = new SettingFragment();
        juanZengFragment = new JuanZengFragment();
        questionFragment = new QuestionFragment();
        Fragment fragments[] = {controlFragment,forceStopFragment,ifwFragment,recentFragment, appStartFragment,
                iceRoomFragment,dozeFragment,uiControlFragment,adFragment,cpuFragment,otherFragment,settingFragment,juanZengFragment,questionFragment};
        int index = initMenuView();
        chooseFragment = fragments[index];
        navigationView.getMenu().getItem(index).setChecked(true);
        this.setTitle(navigationView.getMenu().getItem(index).getTitle());
        uiControlFragment.startRound(sharedPrefs.uiBarPrefs,this);
        if(this.getIntent().hasExtra("from")&&this.getIntent().getStringExtra("from").equals("doze")){
            this.setTitle("打盹");
            navigationView.getMenu().getItem(4).setChecked(true);
            chooseFragment = dozeFragment;
        }
        showOrHidden(chooseFragment);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
            if(!PermissionUtils.checkPermissionAllGranted(this,permissions)){
                PermissionUtils.requestPermission(this,permissions);
            }
        }
        //判断开机启动广播是否可用  不可用启动
        if (!PackageUtil.isEnableCompent(this,BootStartReceiver.class)){
            PackageManager packageManager = this.getPackageManager();
            ComponentName componentName = new ComponentName(this, BootStartReceiver.class);
            packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
        }
        isLinkStopAndAuto = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_LINK_STOPANDAUTOSTART,true);
        isLinkStopAndRemoveStop = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_LINK_STOPANDREMOVERECENTSTOP,true);
        isLinkRecentAndNotStop = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_LINK_RECNETNOTCLEANANDNOTSTOP,false);
        isLinkRecentAndAuto = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_LINK_RECNETREMOVEANDAUTOSTART,true);
    }


    private int initMenuView(){
        boolean one = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_ONE,true);
        boolean two = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_TWO,true);
        boolean three = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_THREE,true);
        boolean four = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_FOUR,true);
        boolean five = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_FIVE,true);
        boolean six = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_SIX,true);
        boolean seven = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_SEVEN,true);
        boolean eight = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_EIGHT,true);
        boolean nine = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_NINE,true);
        boolean ten = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_TEN,true);
        boolean eleven = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_ELEVEN,true);
        navigationView.getMenu().getItem(0).setVisible(one);
        navigationView.getMenu().getItem(1).setVisible(two);
        navigationView.getMenu().getItem(2).setVisible(three);
        navigationView.getMenu().getItem(3).setVisible(four);
        navigationView.getMenu().getItem(4).setVisible(five);
        navigationView.getMenu().getItem(5).setVisible(six);
        navigationView.getMenu().getItem(6).setVisible(seven);
        navigationView.getMenu().getItem(7).setVisible(eight);
        navigationView.getMenu().getItem(8).setVisible(nine);
        navigationView.getMenu().getItem(9).setVisible(eleven);
        navigationView.getMenu().getItem(10).setVisible(ten);
        for(int i = 0;i<navigationView.getMenu().size();i++){
            if(navigationView.getMenu().getItem(i).isVisible()){
                return i;
            }
        }
        return 0;
    }


    private void checkRoot(){
        loadApp(false);
        if(!WatchDogService.isKillRun) {
            h.post(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MainActivity.this, WatchDogService.class);
                    MainActivity.this.startService(intent);
                    sharedPrefs.settings.edit().putBoolean(Common.PREFS_NAME_APPCHANGE, true).commit();
                }
            });
        }
        isRoot = ShellUtils.checkRootPermission();//runing==null||runing.length()==0?
        if(isRoot){
            try {
                Thread.sleep(50);
                ShellUtilDoze.execCommand("dumpsys deviceidle whitelist +com.click369.controlbp");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startAccess();
            whiteApps.putAll(FileUtil.getWhiteList(MainActivity.this));
        }else{
//            if (isModuleActive()) {
//                settings.edit().putBoolean(Common.PREFS_SETTING_STOPAPPBYXP, true).commit();
//            }
            h.post(new Runnable() {
                @Override
                public void run() {
                    showT("没有获取到ROOT权限");
                }
            });
        }
    }

    public void startAccess(){
        if(!isRoot&&!isModuleActive()){
            h.post(new Runnable() {
                @Override
                public void run() {
                    showT("没有获取到ROOT权限并且XP未激活,返回强退、熄屏强退、冷藏室等功能无法使用");
                }
            });
            return;
        }
        if (!sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true)) {
            h.postDelayed(st, 6000);
        }
    }

    Runnable st = new Runnable() {
        @Override
        public void run() {
            if(NewWatchDogService.isOpenNewDogService){
                return;
            }else if(!WatchDogService.isNotNeedAccessibilityService){
                    AlertUtil.showConfirmAlertMsg(MainActivity.this, "无障碍服务自动开启失败，很多功能需要该服务，部分手机显示开启但实际没开启，如果开启无效请尝试重启手机。是否设置？", new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if(tag == 1){
                                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                startActivity(accessibleIntent);
                            }
                        }
                    });
                }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public static boolean isModuleActive() {
        return false;
    }
    public static int getActivatedModuleVersion(){
        return  -1;
    }
    @SuppressLint("RestrictedApi")
    private void showOrHidden(Fragment fragOne){
        //Fragment的显示隐藏
        FragmentManager fm =MainActivity.this.getSupportFragmentManager();
        FragmentTransaction trans = fm.beginTransaction();
        if(fm.getFragments()!=null){
            for (Fragment frg : fm.getFragments()){//fm.getFragments()获取当前界面中所有的Fragment
                trans.hide(frg);
            }
        }
        if(fm.getFragments()==null||!fm.getFragments().contains(fragOne)){
            trans.add(R.id.main_fragment_fl, fragOne);
        }else{
            trans.show(fragOne);
        }
        chooseFragment = fragOne;
        trans.commit();
    }



    public static ArrayList<AppInfo> allAppInfos = new ArrayList<AppInfo>();
    private boolean isLoadAppFromSys = false;
    ProgressDialog pd;
    private void loadApp(boolean isReload){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        pd = ProgressDialog.show(MainActivity.this,"","正在加载应用列表...",true,true);
                    }catch (Exception e){e.printStackTrace();}
                }
            });

        synchronized (allAppInfos) {
            allAppInfos.clear();
            ArrayList<AppInfo> apps = AppInfo.readArrays(getApplicationContext());
            if (apps != null && apps.size() > 0&&!isReload) {
                allAppInfos.addAll(apps);
                isLoadAppFromSys = false;
            } else {
                isLoadAppFromSys = true;
                allAppInfos.addAll(AppLoaderUtil.getAppInfos(MainActivity.this.getApplication(), 2));
            }
            loadAppSetting();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        restartMethod();
        h.removeCallbacks(reUpdateR);
        h.postDelayed(reUpdateR,3000);
    }

    Runnable reUpdateR = new Runnable() {
        @Override
        public void run() {
            String mruning = PackageUtil.getRunngingApp(MainActivity.this);
            if((mruning==null||mruning.length()==0)){
                mruning = ShellUtilBackStop.execCommand("ps",true);
                if (runing==null){
                    runing = "";
                }
            }
            long lastMuBei = 0;
            if(muBeiPrefsFile!=null&&muBeiPrefsFile.exists()){
                lastMuBei = muBeiPrefsFile.lastModified();
            }
            if(mruning!=null&&!mruning.equals(runing)||lastMuBei!=lastMuBeiUpdate){
                loadAppSetting();
            }
        }
    };
    private void restartMethod(){
        if(muBeiPrefsFile!=null&&muBeiPrefsFile.exists()){
            lastMuBeiUpdate = muBeiPrefsFile.lastModified();
        }
        Log.i("CONTROL","WatchDogService.isKillRun  "+WatchDogService.isKillRun);
        if(!WatchDogService.isKillRun) {
            Intent intent = new Intent(MainActivity.this, WatchDogService.class);
            MainActivity.this.startService(intent);
        }
        synchronized (allAppInfos) {
            ArrayList<AppInfo> apps = AppInfo.readArrays(getApplicationContext());
            if (apps != null && apps.size() > 0) {
                allAppInfos.clear();
                allAppInfos.addAll(apps);
                loadAppSetting();
            }
        }
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onStart() {
        isShow = true;
        if(sharedPrefs.settings.getBoolean(Common.ALLSWITCH_SEVEN,true)){
            dozeFragment.reg();
        }
        isNightMode = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_THEME_MODE,false);
        boolean isAutoChange = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_THEME_AUTOCHANGEMODE,false);
        if(!isNightMode&&isAutoChange){
            int hour = Integer.parseInt(TimeUtil.changeMils2String(System.currentTimeMillis(),"H"));
            if(hour>=22||hour<7){
                isNightMode = true;
            }
        }
        showSideBar();
        if (sharedPrefs.settings.getBoolean("nowmode",false)!=isNightMode){
            restartSelf();
        }
//        showT("版本号："+getActivatedModuleVersion());
        super.onStart();
    }

    private void restartSelf(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(intent);
    }

    private void restartSelfConfirm(String msg){
        AlertUtil.showConfirmAlertMsg(this, msg, new AlertUtil.InputCallBack() {
            @Override
            public void backData(String txt, int tag) {
                if (tag == 1){
                    restartSelf();
                }
            }
        });
    }

    private void showSideBar(){
        if(BaseActivity.isPressBack&&sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_SHOWSIDEBAR,true)){
            BaseActivity.isPressBack = false;
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            }
        }
    }

    @Override
    protected void onStop() {
//        if(pd!=null&&pd.isShowing()){
//            pd.dismiss();
//        }
        isShow = false;
        if(sharedPrefs.settings.getBoolean(Common.ALLSWITCH_SEVEN,true)) {
            dozeFragment.destory();
        }
        if(ControlFragment.isClick||ForceStopFragment.isClick||AppStartFragment.isClickItem){
            Intent intent = new Intent(this, WatchDogService.class);
            this.startService(intent);
        }
        //更新AMS中的数据
        if(ControlFragment.isClick){
            sendBroadAMSChangeControl(this);
            ControlFragment.isClick = false;
        }
        if(ForceStopFragment.isClick){
            sendBroadAMSChangeMuBei(this);
            ForceStopFragment.isClick = false;
        }
        if (AppStartFragment.isClickItem) {
            sendBroadAMSChangeAutoStart(this);
            AppStartFragment.isClickItem = false;
        }
        h.removeCallbacks(reUpdateR);
        super.onStop();
    }

    long backTime= 0;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(System.currentTimeMillis()-backTime>1500){
                showT("再按一次退出程序！");
            }else{
//                super.onBackPressed();
//                sendBroadChangePrefs(this);
                BaseActivity.isPressBack = true;
                TopSearchView.searchText = "";
                moveTaskToBack(true);
            }
            backTime = System.currentTimeMillis();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
//            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }else{
                drawer.openDrawer(GravityCompat.START);
            }
        }
//        else if(event.getKeyCode() == KeyEvent.KEYCODE_HOME){
//            sendBroadChangePrefs(this);
//        }
        return super.onKeyDown(keyCode, event);
    }

//    public static void sendBroadChangePrefs(Context cxt){
//        Intent intent = new Intent("com.click369.control.ams.reloadprefs");
//        cxt.sendBroadcast(intent);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        this.setTitle(item.getTitle());
        menuTv.setVisibility(View.GONE);
        menuTv.setEnabled(true);
        menuTv.setAlpha(1.0f);
        if (id == R.id.nav_one) {
            showOrHidden(controlFragment);
            menuTv.setVisibility(View.VISIBLE);
            if (!isModuleActive()){
                menuTv.setEnabled(false);
                menuTv.setAlpha(0.5f);
            }
            page = 0;
        }else if (id == R.id.nav_two) {
            showOrHidden(forceStopFragment);
            menuTv.setVisibility(View.VISIBLE);
            page = 1;
        }else if (id == R.id.nav_three) {
            page = 2;
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(ifwFragment);
        }else if (id == R.id.nav_four) {
            page = 3;
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(iceRoomFragment);
        }
        else if (id == R.id.nav_five) {
            if( Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                page = 4;
                menuTv.setVisibility(View.VISIBLE);
                showOrHidden(dozeFragment);
            }else {
                navigationView.getMenu().getItem(page).setChecked(true);
                showT("该功能只有安卓6.0及以上系统才能使用");
            }
        }else if (id == R.id.nav_six) {
            page = 5;
            menuTv.setVisibility(View.INVISIBLE);
            showOrHidden(otherFragment);
        }else if (id == R.id.nav_seven) {
            page = 6;
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(settingFragment);
        }
        else if (id == R.id.nav_eight) {
            page = 7;
            menuTv.setVisibility(View.INVISIBLE);
            showOrHidden(juanZengFragment);
        }
        else if (id == R.id.nav_nine) {
            page = 8;
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(appStartFragment);
        }
        else if (id == R.id.nav_ten) {
            page = 9;
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(uiControlFragment);
        }
        else if (id == R.id.nav_eleven) {
            page = 10;
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(adFragment);
        }else if (id == R.id.nav_twelve) {
            page = 11;
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(recentFragment);
        }else if (id == R.id.nav_thridteen) {
            page = 12;
            menuTv.setVisibility(View.INVISIBLE);
            showOrHidden(questionFragment);
        }else if (id == R.id.nav_fourteen) {
            page = 13;
            menuTv.setVisibility(View.INVISIBLE);
            showOrHidden(cpuFragment);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void menuClick(){
        menuTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titles[] = {"备份设置","还原设置","清除所有","刷新应用"};
                if(page == 0){
                    titles = new String[]{"备份设置","还原设置","清除所有","刷新应用"};
                }else if(page == 2){
                    titles = new String[]{"备份IFW","还原IFW","一键阉割","恢复阉割","清空所有"};
                }else if(page == 6){
                    titles = new String[]{"备份所有项设置(IFW除外)","还原所有项设置(IFW除外)","清除所有项设置(IFW除外)","刷新应用"};
                }else if(page == 10){
                    titles = new String[]{"备份设置","还原设置","清除所有","刷新应用","载入部分APP预设"};
                }
                AlertUtil.showListAlert(MainActivity.this, "更多功能", titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, final int tag) {
//                        if (page == 0&&tag==4){
//                            Intent intent =new Intent(MainActivity.this,WakeLockActivity.class);
//                            startActivity(intent);
//                            return;
//                        }
                        AlertUtil.showConfirmAlertMsg(MainActivity.this, "请确认操作,防止误触。", new AlertUtil.InputCallBack() {
                            @Override
                            public void backData(String txt, int tag1) {
                                if (tag1 == 1){
                                    if(tag == 0){
                                        backupRestoreUtil.saveData(page);
                                    }else if(tag == 1){
                                        backupRestoreUtil.restoreData(page);
                                        loadAppSetting();
                                        if(page == 0){
                                            controlFragment.fresh();
                                        }else if(page == 1){
                                            sharedPrefs.muBeiPrefs.edit().clear().commit();
                                            forceStopFragment.fresh();
                                        }else if(page == 2){
                                            ifwFragment.loadDisableCount();
                                        }else if(page == 3){
                                            iceRoomFragment.fresh();
                                        }else if(page == 4){
                                            restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                        }else if(page == 6){
                                            restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                        }else if(page == 8){
                                            loadAppSetting();
                                        }else if(page == 9){
                                            restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                        }else if(page == 10){
                                            adFragment.fresh();
                                        }else if(page == 11){
                                            recentFragment.fresh();
                                        }
                                        Intent intent = new Intent(MainActivity.this,WatchDogService.class);
                                        startService(intent);
                                    }else if(tag == 2){
                                        if(page ==0||page == 1||page == 4||page == 6||page == 8||page == 9||page == 10||page == 11 ){
                                            AlertUtil.showConfirmAlertMsg(MainActivity.this, "是否移除所有选中项?", new AlertUtil.InputCallBack() {
                                                @Override
                                                public void backData(String txt, int tag) {
                                                    if(tag == 1){
                                                        loadAppSetting();
                                                        if(page == 0){
                                                            sharedPrefs.muBeiPrefs.edit().clear().commit();
                                                            sharedPrefs.modPrefs.edit().clear().commit();
                                                            sharedPrefs.wakeLockPrefs.edit().clear().commit();
                                                            sharedPrefs.alarmPrefs.edit().clear().commit();
                                                            controlFragment.fresh();
                                                        }else if(page == 1){
                                                            sharedPrefs.forceStopPrefs.edit().clear().commit();
                                                            forceStopFragment.fresh();
                                                        }else if(page == 4){
                                                            sharedPrefs.dozePrefs.edit().clear().commit();
                                                            showT("清除成功,重新打开生效");
                                                        }else if(page == 6){
                                                            sharedPrefs.modPrefs.edit().clear().commit();
                                                            sharedPrefs.forceStopPrefs.edit().clear().commit();
                                                            sharedPrefs.autoStartNetPrefs.edit().clear().commit();
                                                            sharedPrefs.recentPrefs.edit().clear().commit();
                                                            sharedPrefs.dozePrefs.edit().clear().commit();
                                                            sharedPrefs.uiBarPrefs.edit().clear().commit();
                                                            sharedPrefs.muBeiPrefs.edit().clear().commit();
                                                            sharedPrefs.adPrefs.edit().clear().commit();
                                                            sharedPrefs.skipDialogPrefs.edit().clear().commit();
                                                            sharedPrefs.pmPrefs.edit().clear().commit();
                                                            sharedPrefs.settings.edit().clear().commit();
                                                            showT("清除成功,重启手机生效");
                                                            restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                                        }else if(page == 8){
                                                            sharedPrefs.autoStartNetPrefs.edit().clear().commit();
                                                            appStartFragment.fresh();
                                                        }else if(page == 9){
                                                            sharedPrefs.uiBarPrefs.edit().clear().commit();
                                                            restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                                        }else if(page == 10){
                                                            sharedPrefs.adPrefs.edit().clear().commit();
                                                            adFragment.fresh();
                                                        }else if(page == 11){
                                                            sharedPrefs.recentPrefs.edit().clear().commit();
                                                            recentFragment.fresh();
                                                        }
                                                    }
                                                }
                                            });
                                            Intent intent = new Intent(MainActivity.this,WatchDogService.class);
                                            startService(intent);
                                        }else if(page==2){
                                            ifwFragment.startDis();
                                        }
                                    }else if(tag == 3){
                                        if (page == 2){
                                            ifwFragment.startBackDis();
                                        }else{
                                            new Thread(){
                                                @Override
                                                public void run() {
                                                    loadApp(true);
                                                }
                                            }.start();
                                        }
                                    }else if(tag == 4){
//                                        if(page == 0){
//                                            Intent intent =new Intent(MainActivity.this,WakeLockActivity.class);
//                                            startActivity(intent);
//                                        }else
                                        if (page == 2){
                                            ifwFragment.startClear();
                                        }else if (page == 10){
                                            TestDataInit.initAD(getApplicationContext());
                                            adFragment.fresh();
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void loadAppSetting(){
        final PackageManager pm = getPackageManager();
        new Thread(){
            @Override
            public void run() {
                runing = PackageUtil.getRunngingApp(MainActivity.this);
                if((runing==null||runing.length()==0)){
                    runing = ShellUtilBackStop.execCommand("ps",true);
                    if (runing==null){
                        runing = "";
                    }
                }else{
                    try {
                        HashMap<String, Boolean> pkgs = new HashMap<String, Boolean>();
                        String pkgs1[] = runing.split("\n");
                        for (String s : pkgs1) {
                            pkgs.put(s, false);
                        }
                        Intent intent = new Intent("com.click369.control.uss.getappidlestate");
                        intent.putExtra("pkgs", pkgs);
                        sendBroadcast(intent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                try {
                    for (int i =0;i<allAppInfos.size();i++) {
                        if (MainActivity.this.isFinishing()) {
                            return;
                        }
                        AppInfo ai = allAppInfos.get(i);
                        ai.isServiceStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/service", false);
                        ai.isBroadStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/broad", false);
                        ai.isWakelockStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/wakelock", false);
                        ai.isAlarmStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/alarm", false);

                        ai.isBackForceStop = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/backstop", false);
                        ai.isBackMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/backmubei", false);
                        ai.isOffscForceStop = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/offstop", false);
                        ai.isOffscMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/offmubei", false);
                        ai.isInMuBei = sharedPrefs.muBeiPrefs.getInt(ai.getPackageName(), -1)==0;
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
                        if (runing != null && runing.length() > 0) {
                            if (runing.contains(ai.getPackageName()+"\n")) {
                                ai.isRunning = true;
                            } else {
                                ai.isRunning = false;
                            }
                        }
                        if (!isLoadAppFromSys) {
                            try {
                                ai.isDisable = !pm.getPackageInfo(ai.getPackageName(), PackageManager.GET_ACTIVITIES).applicationInfo.enabled;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                isLoadAppFromSys= false;
                reloadAdapter();
            }
        }.start();
    }

    private void reloadAdapter(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chooseFragment==null){
                    return;
                }
                if(chooseFragment instanceof ControlFragment){
                    controlFragment.fresh();
                }else if(chooseFragment instanceof ForceStopFragment){
                    forceStopFragment.fresh();
                }else if(chooseFragment instanceof IFWFragment){
                    ifwFragment.fresh();
                }else if(chooseFragment instanceof RecentFragment){
                    recentFragment.fresh();
                }else if(chooseFragment instanceof AppStartFragment){
                    appStartFragment.fresh();
                }else if(chooseFragment instanceof IceUnstallFragment){
                    iceRoomFragment.fresh();
                }else if(chooseFragment instanceof AdFragment){
                    adFragment.fresh();
                }
                if(pd!=null&&pd.isShowing()){
                    pd.dismiss();
                    pd = null;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isUIRun = false;
        h.removeCallbacks(st);
        runing = "";
        if (sharedPrefs.settings.getBoolean(Common.ALLSWITCH_THREE,true)) {
            ifwFragment.destory();
        }
        if (updateReceiver!=null){
            MainActivity.this.unregisterReceiver(updateReceiver);
        }
    }

    long lastUpdateTime = 0;
    boolean isShow = true;
    class MyUpdateListReceiver extends BroadcastReceiver{
        public MyUpdateListReceiver(){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.click369.control.updatelist");
            intentFilter.addAction("com.click369.control.recappidlestate");
            MainActivity.this.registerReceiver(this,intentFilter);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("com.click369.control.updatelist".equals(action)) {
                if (System.currentTimeMillis() - lastUpdateTime > 2000 && isShow) {
                    restartMethod();
                    lastUpdateTime = System.currentTimeMillis();
                }
            }else if("com.click369.control.recappidlestate".equals(action)){
                pkgIdleStates.clear();
                HashSet<String> pkgs = ( HashSet<String>)intent.getSerializableExtra("pkgs");
                if (pkgs.size() == 0&&WatchDogService.homeIdleApp.size()>0&&!WatchDogService.isCharging){
                    pkgs.addAll(WatchDogService.homeIdleApp);
                }
                pkgIdleStates.addAll(pkgs);
                reloadAdapter();
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       File file = getPhoto.onActivityResult(requestCode,resultCode,data);
        if (file!=null&&file.exists()) {
            if(bgBlurFile.exists()){
               bgBlurFile.delete();
            }
            int blur  = sharedPrefs.settings.getInt(Common.PREFS_SETTING_OTHER_BGBLUR,0);
            int alpha = sharedPrefs.settings.getInt(Common.PREFS_SETTING_OTHER_BGBRIGHT,100);
            otherFragment.changeBgBlur(this,blur,alpha);
//            Drawable d = Drawable.createFromPath(file.getAbsolutePath());
//            if (d!=null) {
//                d.setAlpha(180);
//                mainRL.setBackgroundColor(Color.BLACK);
//                mainRL.setBackground(d);
//            }
        }
    }


    private void showNotActive() {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("模块未激活（更新后需要重新勾选一次模块），请先激活模块并重启手机")
                .setPositiveButton("激活", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openXposed();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void openXposed() {
        if (PackageUtil.isAppInstalled(this, "de.robv.android.xposed.installer")) {
            Intent intent = new Intent("de.robv.android.xposed.installer.OPEN_SECTION");
            if (getPackageManager().queryIntentActivities(intent, 0).isEmpty()) {
                intent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");
            }
            if (intent != null) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .putExtra("section", "modules")
                        .putExtra("fragment", 1)
                        .putExtra("module", BuildConfig.APPLICATION_ID);
                startActivity(intent);
            }
        } else {
           showT("xposed installer未安装");
        }
    }
}

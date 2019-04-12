package com.click369.controlbp.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.BuildConfig;
import com.click369.controlbp.R;
import com.click369.controlbp.adapter.NavInfoAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.WhiteApp;
import com.click369.controlbp.common.TestDataInit;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.fragment.AdFragment;
import com.click369.controlbp.fragment.AppStartFragment;
import com.click369.controlbp.fragment.BaseFragment;
import com.click369.controlbp.fragment.CPUSetFragment;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.fragment.DozeFragment;
import com.click369.controlbp.fragment.ForceStopFragment;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.fragment.IceUnstallFragment;
import com.click369.controlbp.fragment.JuanZengFragment;
import com.click369.controlbp.fragment.OtherFragment;
import com.click369.controlbp.fragment.PrivacyFragment;
import com.click369.controlbp.fragment.QuestionFragment;
import com.click369.controlbp.fragment.RecentFragment;
import com.click369.controlbp.fragment.SettingFragment;
import com.click369.controlbp.fragment.UIControlFragment;
import com.click369.controlbp.fragment.XpBlackListFragment;
import com.click369.controlbp.receiver.BootStartReceiver;
import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedAMS;
import com.click369.controlbp.service.XposedBroadCast;
import com.click369.controlbp.service.XposedUtil;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.BackupRestoreUtil;
import com.click369.controlbp.util.CpuUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.GCUtil;
import com.click369.controlbp.util.GetPhoto;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.ShortCutUtil;
import com.click369.controlbp.util.SoftKeyboardStateHelper;
import com.click369.controlbp.util.TimeUtil;
import com.githang.statusbar.StatusBarCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
//    public static final boolean TEST = false;
    public static String THEME_COLOR = "#1a9dac";
    public static String THEME_BG_COLOR = "#f4f4f4";
    public static String THEME_TEXT_COLOR = "#1a9dac";

    public static boolean isUIRun = false;
    public static boolean isShow = false;
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
    public PrivacyFragment privacyFragment;
    public AdFragment adFragment;
    public XpBlackListFragment xpBlackFragement;
    public CPUSetFragment cpuFragment;
    public QuestionFragment questionFragment;
    public BaseFragment chooseFragment;
    private NavigationView navigationView;
    private int page = 0;
//    public static String runing = "";
    public static String COLOR = "#00ccff";
    public static String COLOR_RUN = "#00cd50";//"#00cd50";
    public static String COLOR_MUBEI = "#e89437";
    public static String COLOR_IDLE = "#e04134";
    public static boolean isNightMode = false;
    public static boolean isAutoChange = false;

    private Toolbar toolbar;
    private BackupRestoreUtil backupRestoreUtil;
    public DrawerLayout drawer;
    public LinearLayout navInfoLL;
    public TextView navInfoTitle,navInfoSamllTitle;
    public ListView navInfoListView;
    public NavInfoAdapter navAdapter;
    private MyUpdateListReceiver updateReceiver;

    public File bgFile,bgBlurFile,sideBgFile,sideBgBlurFile;
    public GetPhoto getPhoto;
    BaseFragment fragments[];
    private AppLoaderUtil.LoadAppCallBack loadAppCallBack;
    ProgressDialog pd;
    TextView sysTimeTitle;
    public static HashSet<String> pkgIdleStates = new HashSet<String>();
    boolean isClickSysTime = false;
    public static boolean isModuleActive() {
        return false||WatchDogService.ISHOOKOK;
    }
    public static int getActivatedModuleVersion(){
        return  -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isUIRun = true;
        bgFile = new File(FileUtil.IMAGEPATH,"bg");
        bgBlurFile = new File(FileUtil.IMAGEPATH,"bg_blur");
        sideBgFile = new File(FileUtil.IMAGEPATH,"side");
        sideBgBlurFile = new File(FileUtil.IMAGEPATH,"side_blur");

        pd = ProgressDialog.show(MainActivity.this,"","正在加载应用列表...",true,true);
        loadAppCallBack = new LoadAppCallBackImp();
        appLoaderUtil.addAppChangeListener(loadAppCallBack);
        backupRestoreUtil = new BackupRestoreUtil(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M&&sharedPrefs.settings.getBoolean(Common.ALLSWITCH_DOZE,true)) {
            sharedPrefs.settings.edit().putBoolean(Common.ALLSWITCH_DOZE,false).commit();
        }
        File qualcpuFile = new File(getFilesDir(),"qualcomm");
        if(!qualcpuFile.exists()&&CpuUtil.isQualcommCpu()){
            try {
                qualcpuFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!qualcpuFile.exists()&&sharedPrefs.settings.getBoolean(Common.ALLSWITCH_CPUSET,true)){
            sharedPrefs.settings.edit().putBoolean(Common.ALLSWITCH_CPUSET,false).commit();
            WatchDogService.isLockUnlockCPU = false;
        }

        isNightMode = sharedPrefs.uiBarPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_MODE,false);
        THEME_COLOR = sharedPrefs.uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_COLOR,"#1a9dac");
        THEME_TEXT_COLOR = sharedPrefs.uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_TEXT_COLOR,"#1a9dac");
        THEME_BG_COLOR = sharedPrefs.uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_BG_COLOR,"#f4f4f4");
        isAutoChange = sharedPrefs.uiBarPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_AUTOCHANGEMODE,false);
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
        toolbar.setTitleMarginStart(0);
        toolbar.setContentInsetStartWithNavigation(0);
        mainRL =(RelativeLayout) findViewById(R.id.main_fragment_rl);
        mainRL.setBackgroundColor(isNightMode?Color.BLACK:Color.WHITE);
        menuTv = (AppCompatTextView) findViewById(R.id.main_menu_actv);
        menuTv.setVisibility(View.VISIBLE);
        menuClick();
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if(drawer!=null){
            drawer.setDrawerListener(toggle);
        }

        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navInfoLL = (LinearLayout)navigationView.findViewById(R.id.nav_info_ll);

        navInfoTitle = (TextView)navigationView.findViewById(R.id.nav_info_title);
        navInfoSamllTitle = (TextView)navigationView.findViewById(R.id.nav_info_smalltitle);
        navInfoListView = (ListView) navigationView.findViewById(R.id.nav_info_listview);
        initMenuView();
        initView();
        TextView head_bigtitle_tv = (TextView)navigationView.getHeaderView(0).findViewById(R.id.head_bigtitle_tv);
        TextView subTitle = (TextView)navigationView.getHeaderView(0).findViewById(R.id.head_subtitle_tv);
        sysTimeTitle = (TextView)navigationView.getHeaderView(0).findViewById(R.id.head_threetitle_tv);//+"\n微信号:Van418"
        subTitle.setText("版本:"+PackageUtil.getAppVersionName(this)+"\n交流群:624055295");//+"\n朋友搞机公众号:MRYZSL"
        isClickSysTime = sharedPrefs.settings.getBoolean("isclicksystime",false);
        sysTimeTitle.setText("系统启动时长:"+TimeUtil.changeMils2StringMin(SystemClock.elapsedRealtime())+(isClickSysTime?"":"(点我)"));//+"\n朋友搞机公众号:MRYZSL"
        sysTimeTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefs.settings.edit().putBoolean("isclicksystime",true).commit();
                isClickSysTime = true;
                if(WatchDogService.batteryInfos.size()==0){
                    AlertUtil.showAlertMsg(MainActivity.this,"还没有电量记录，待会再试哦");
                }else{
                    ArrayList<Integer> batteryInfo = new ArrayList<Integer>(WatchDogService.batteryInfos.keySet());
                    batteryInfo.remove((Integer)101);
                    batteryInfo.remove((Integer)102);
                    Collections.reverse(batteryInfo);
                    String titls[] = new String[batteryInfo.size()+(batteryInfo.size()>1?1:0)];
                    SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日HH:mm:ss 剩余:");
                    if(batteryInfo.size()>1){
                        Integer now = batteryInfo.get(0);
                        Integer first = batteryInfo.get(batteryInfo.size()-1);
                        long time = System.currentTimeMillis()-WatchDogService.batteryInfos.get(first);
                        String title = (SystemClock.elapsedRealtime()-time>1000*60*5?"从拔下电源使用时长:":"从开机使用时长:")+TimeUtil.changeMils2StringMin(time)+",消耗电量:"+(first-now)+"%,";
                        if(WatchDogService.batteryInfos.containsKey(101)&&WatchDogService.batteryInfos.containsKey(102)){
                            title+="亮屏时长:"+TimeUtil.changeMils2StringMin(WatchDogService.batteryInfos.get(101)+(System.currentTimeMillis()-WatchDogService.batteryOnScTime))+",熄屏时长:"+TimeUtil.changeMils2StringMin(WatchDogService.batteryInfos.get(102));
                        }
                        titls[0] = title;
                        for(int i = 1;i<batteryInfo.size()+1;i++){
                            Integer integer = batteryInfo.get(i-1);
                            titls[i] = sdf.format(new Date(WatchDogService.batteryInfos.get(integer)))+integer+"%";
                        }
                    }else{
                        for(int i = 0;i<batteryInfo.size();i++){
                            Integer integer = batteryInfo.get(i);
                            titls[i] = sdf.format(new Date(WatchDogService.batteryInfos.get(integer)))+integer+"%";
                        }
                    }
                    AlertUtil.showListAlert(MainActivity.this,"电池消耗时间点",titls,null);
                }
            }
        });
        head_bigtitle_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtherFragment.showRestartAlert(MainActivity.this);
            }
        });
        navAdapter = new NavInfoAdapter(this);
        navInfoListView.setAdapter(navAdapter);
        navInfoLL.setVisibility(View.GONE);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                sysTimeTitle.setText("系统启动时长:"+TimeUtil.changeMils2StringMin(SystemClock.elapsedRealtime())+(isClickSysTime?"":"(点我)"));
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
//            toolbar.setBackgroundColor(getResources().getColor(R.color.darkblack));
//            navigationView.setBackgroundColor(getResources().getColor(R.color.darkblack));
//            navigationView.getHeaderView(0).setBackgroundColor(getResources().getColor(R.color.darkblack));
//            navInfoLL.setBackgroundColor(getResources().getColor(R.color.darkblack));
            colors = new int[]{ getResources().getColor(R.color.uncheck_colordark),  getResources().getColor(R.color.checked_color_dark) };
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                Window window = this.getWindow();
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                window.setStatusBarColor(getResources().getColor(R.color.darkblack));
//                window.setNavigationBarColor(Color.BLACK);
//            }
        }
        initThemeColor();
        ColorStateList csl = new ColorStateList(states, colors);
        navigationView.setItemTextColor(csl);
        navigationView.setItemIconTintList(csl);
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setTitle("");
        isZhenDong = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_ZHENDONG,true);
        updateReceiver = new MyUpdateListReceiver();
        getPhoto = new GetPhoto(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortCutUtil.initDynamicShortcuts(this);
        }
        whiteApps.putAll(FileUtil.getWhiteList(MainActivity.this));
        h.postDelayed(showAlert,1000);
        initFileBg(0);
        if(!WatchDogService.isRoot){
            WatchDogService.isRoot = sharedPrefs.settings.getBoolean("ISROOT",false);
        }
        new Thread(){
            @Override
            public void run() {
                appLoaderUtil.loadLocalApp();
            }
        }.start();
    }

    public void initThemeColor(){
        navigationView.getHeaderView(0).setBackgroundColor(Color.TRANSPARENT);
        if(!isNightMode){
            toolbar.setBackgroundColor(Color.parseColor(THEME_COLOR));
            if(!sideBgFile.exists()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    navigationView.setBackground(null);
                    navInfoLL.setBackground(null);
                }else{
                    navigationView.setBackgroundDrawable(null);
                    navInfoLL.setBackgroundDrawable(null);
                }
                navInfoLL.setBackgroundColor(Color.parseColor(THEME_COLOR));
//                int color = Color.parseColor(THEME_COLOR);
//                int alpha = (color & 0xff000000) >>> 24;
//                int red = ((color & 0x00ff0000) >> 16)+10;
//                int green = ((color & 0x0000ff00) >> 8)+10;
//                int blue = (color & 0x000000ff)+10;
//                navigationView.setBackgroundColor(Color.argb(alpha,red>255?255:red,green>255?255:green,blue>255?255:blue));
                navigationView.setBackgroundColor(Color.parseColor(THEME_COLOR));
//            navigationView.setBackgroundResource(R.drawable.slider_bg1);

            }

        }else{
            toolbar.setBackgroundColor(Color.BLACK);
            if(!sideBgFile.exists()){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    navigationView.setBackground(null);
                    navInfoLL.setBackground(null);
                }else{
                    navigationView.setBackgroundDrawable(null);
                    navInfoLL.setBackgroundDrawable(null);
                }
                navInfoLL.setBackgroundColor(Color.BLACK);
                navigationView.getHeaderView(0).setBackgroundColor(Color.BLACK);
                navigationView.setBackgroundColor(Color.BLACK);
            }

        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarCompat.setStatusBarColor(this,isNightMode?Color.BLACK:Color.parseColor(THEME_COLOR),false);
//            Window window = this.getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
////                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
//            window.setStatusBarColor(isNightMode?Color.BLACK:Color.parseColor(THEME_COLOR));
//            if (new File(Environment.getExternalStorageDirectory(),"zroms").exists()){
//                //底部导航栏
//                window.setNavigationBarColor(Color.WHITE);
//            }
//        }
    }

    private void initFileBg(int type){//0所有  1列表背景  2侧栏背景
        try {
            if(type==1||type==0){

//                h.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                        if (bgFile.exists()){
                            Drawable d = Drawable.createFromPath(bgBlurFile.exists()?bgBlurFile.getAbsolutePath():bgFile.getAbsolutePath());
                            if(d!=null){
                                int alp = (int)(sharedPrefs.uiBarPrefs.getInt(Common.PREFS_SETTING_UI_BGBRIGHT,100)*2.55);
                                Log.i("CONTROL","ALP "+alp);
                                d.setAlpha(alp);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    mainRL.setBackground(d);
                                }else{
                                    mainRL.setBackgroundDrawable(d);
                                }
                            }else{
                                mainRL.setBackgroundColor(Color.TRANSPARENT);
                                setBgColor(Color.parseColor(THEME_BG_COLOR));
                            }
                        }else{
                            mainRL.setBackgroundColor(Color.TRANSPARENT);
                            setBgColor(Color.parseColor(THEME_BG_COLOR));
                        }
//                    }
//                },500);

            }
            if(type==2||type==0) {

//                h.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                        if (sideBgFile.exists()) {
                            Drawable d = Drawable.createFromPath(sideBgBlurFile.exists() ? sideBgBlurFile.getAbsolutePath() : sideBgFile.getAbsolutePath());
                            if (d != null) {

                                int alp = (int)(sharedPrefs.uiBarPrefs.getInt(Common.PREFS_SETTING_UI_SIDEBGBRIGHT,100)*2.55);
                                Log.i("CONTROL","ALP "+alp+"  "+Common.PREFS_SETTING_UI_SIDEBGBRIGHT);
                                d.setAlpha(alp);
                                navigationView.setBackgroundColor(Color.TRANSPARENT);
                                navInfoLL.setBackgroundColor(Color.TRANSPARENT);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    navigationView.setBackground(d);
                                    navInfoLL.setBackground(d);
                                } else {
                                    navigationView.setBackgroundDrawable(d);
                                    navInfoLL.setBackgroundDrawable(d);
                                }
                            } else {
                                initThemeColor();
                            }
                        } else {
                            initThemeColor();
                        }
//                    }
//                },500);
            }
            System.gc();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    boolean isShowAlert = false;
    Runnable showAlert  = new Runnable() {
        @Override
        public void run() {
            if (!isModuleActive()){
                menuTv.setAlpha(0.5f);
                menuTv.setEnabled(false);
                showNotActive();
                isShowAlert = true;
            }else{
                int saveCode = sharedPrefs.settings.getInt(Common.BUILDCODE,0);
                if(saveCode!=BuildConfig.VERSION_CODE){
                    isShowAlert = true;

                    sharedPrefs.settings.edit().putInt(Common.BUILDCODE,BuildConfig.VERSION_CODE).commit();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                AlertUtil.showAlertMsg(MainActivity.this,getString(R.string.mainalert));
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    },500);
                }
            }
        }
    };

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
        xpBlackFragement = new XpBlackListFragment();
        cpuFragment = new CPUSetFragment();
        privacyFragment = new PrivacyFragment();
        otherFragment = new OtherFragment();
        settingFragment = new SettingFragment();
        juanZengFragment = new JuanZengFragment();
        questionFragment = new QuestionFragment();
        fragments = new BaseFragment[]{controlFragment,forceStopFragment,ifwFragment,recentFragment, appStartFragment,
                iceRoomFragment,xpBlackFragement,privacyFragment,adFragment,dozeFragment,uiControlFragment,cpuFragment,otherFragment,
                settingFragment,juanZengFragment,questionFragment};
        int index = initMenuView();
        chooseFragment = fragments[index];
        navigationView.getMenu().getItem(index).setChecked(true);
        this.setTitle(navigationView.getMenu().getItem(index).getTitle());
        if(this.getIntent().hasExtra("from")&&this.getIntent().getStringExtra("from").equals("doze")){
            this.setTitle("打盹");
            navigationView.getMenu().getItem(4).setChecked(true);
            chooseFragment = dozeFragment;
            page = 4;
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
        isUpdateAppTime = sharedPrefs.settings.getBoolean(Common.PREFS_SETTING_ISUPDATEAPPTIME,false);
        listenerSoftInput();


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        sharedPrefs.settings.edit().putInt(Common.PREFS_SETTING_SCREENWIDTH,width).commit();
        sharedPrefs.settings.edit().putInt(Common.PREFS_SETTING_SCREENHEIGHT,height).commit();
    }


    private int initMenuView(){
        boolean one = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_SERVICE_BROAD,true);
        boolean two = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_BACKSTOP_MUBEI,true);
        boolean three = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_IFW,true);
        boolean four = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_RECNETCARD,true);
        boolean five = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_AUTOSTART_LOCK,true);
        boolean six = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_UNINSTALL_ICE,true);
        boolean seven = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_XPBLACKLIST,true);
        boolean eight = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_PRIVACY,true);
        boolean nine = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_ADSKIP,true);
        boolean ten = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_DOZE,true);
        boolean eleven = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_UI,true);
        boolean twelve = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_CPUSET,true);
        boolean thirteen = sharedPrefs.settings.getBoolean(Common.ALLSWITCH_OTHERS,true);
        navigationView.getMenu().getItem(0).setVisible(one);
        navigationView.getMenu().getItem(1).setVisible(two);
        navigationView.getMenu().getItem(2).setVisible(three);
        navigationView.getMenu().getItem(3).setVisible(four);
        navigationView.getMenu().getItem(4).setVisible(five);
        navigationView.getMenu().getItem(5).setVisible(six);
        navigationView.getMenu().getItem(6).setVisible(seven);
        navigationView.getMenu().getItem(7).setVisible(eight);
        navigationView.getMenu().getItem(8).setVisible(nine);
        navigationView.getMenu().getItem(9).setVisible(ten);
        navigationView.getMenu().getItem(10).setVisible(eleven);
        navigationView.getMenu().getItem(11).setVisible(twelve);
        navigationView.getMenu().getItem(12).setVisible(thirteen);
        for(int i = 0;i<navigationView.getMenu().size();i++){
            if(navigationView.getMenu().getItem(i).isVisible()){
                return i;
            }
        }
        return 0;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("RestrictedApi")
    private void showOrHidden(BaseFragment fragOne){
        try {
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

            if(isUpdateAppTime){
                h.removeCallbacks(updateTimeInfo);
                h.postDelayed(updateTimeInfo,1000);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(uiControlFragment.ischeck){
            uiControlFragment.ischeck = false;
            uiControlFragment.startRound(this);
        }
        restartMethod();
    }

    public static boolean isStart = false;
    public static boolean isFirst = true;
    @Override
    protected void onResume() {
        super.onResume();
        try {
           new Thread(){
               @Override
               public void run() {

                   if(!WatchDogService.isKillRun) {
                       Intent intent = new Intent(MainActivity.this, WatchDogService.class);
                       MainActivity.this.startService(intent);
                   }

                   h.removeCallbacks(updateTimeInfo);
                   h.postDelayed(updateTimeInfo,1);
                   HashMap<String, Boolean> pkgs = new HashMap<String, Boolean>();
                   appLoaderUtil.reloadRunList();
                   try {
                       for (String s : AppLoaderUtil.runLists) {
                           pkgs.put(s, false);
                       }
                       int len = appLoaderUtil.allAppInfos.size();
                       for(int i = 0;i<len;i++){
                           AppInfo ai = appLoaderUtil.allAppInfos.get(i);
                           ai.isRunning = AppLoaderUtil.runLists.contains(ai.getPackageName());
                       }
                   }catch (Exception e){
                   }
                   if(chooseFragment!=null){
                       chooseFragment.fresh();
                   }
                   appLoaderUtil.addAppChangeListener(loadAppCallBack);
                   appLoaderUtil.loadAppSetting();
                   sendBroadcast(new Intent(("com.click369.control.ams.getprocinfo")));
                   Intent intent = new Intent("com.click369.control.uss.getappidlestate");
                   intent.putExtra("pkgs", pkgs);
                   sendBroadcast(intent);

                   if(!isShowAlert){
                       update();
                   }else{
                       isShowAlert = false;
                   }
               }
           }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restartMethod(){
        appLoaderUtil.checkAppChange();
        if(!appLoaderUtil.isAppChange){
            appLoaderUtil.loadAppSetting();
        }
//        Log.i("CONTROL","restart  "+AppLoaderUtil.allHMAppIcons.size()+"  "+(AppLoaderUtil.allHMAppInfos.size()+1));
//        if(AppLoaderUtil.allHMAppIcons.size()==0||
//                AppLoaderUtil.allHMAppIcons.size()!=(AppLoaderUtil.allHMAppInfos.size()+1)){//+1因为包含应用控制器
            loadAppIcons();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        isShow = true;
//        if(TEST){
//            return;
//        }
        if (updateReceiver!=null){
            updateReceiver.reg();
            if(!WatchDogService.ISHOOKOK){
                sendBroadcast(new Intent("com.click369.control.ams.checkhook"));
            }
        }

        if(sharedPrefs.settings.getBoolean(Common.ALLSWITCH_DOZE,true)&&dozeFragment!=null){
            dozeFragment.reg();
        }
        isNightMode = sharedPrefs.uiBarPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_MODE,false);
        boolean isAutoChange = sharedPrefs.uiBarPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_AUTOCHANGEMODE,false);
        if(!isNightMode&&
                isAutoChange){
            int hour = Integer.parseInt(TimeUtil.changeMils2String(System.currentTimeMillis(),"H"));
            if(hour>=22||hour<7){
                isNightMode = true;
            }
        }
        if (sharedPrefs.settings.getBoolean("nowmode",false)!=isNightMode){
            restartSelf();
        }else{
            showSideBar();
            //异常阻止提示
            if(WatchDogService.preventErrAppNames.size()>0){
                StringBuilder sb = new StringBuilder();
                for(String app:WatchDogService.preventErrAppNames){
                    sb.append(app).append(" ");
                }
                AlertUtil.showAlertMsg(MainActivity.this,"系统检测到 "+sb.toString()+"被频繁启动很可能其他APP需要依赖该应用，应用控制器本次取消对其阻止（可以尝试取消对其的禁止自启或关闭该应用的通知读取权限）");
                WatchDogService.preventErrAppNames.clear();
            }
        }

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

    private void listenerSoftInput() {
        try {
            SoftKeyboardStateHelper softKeyboardStateHelper = new SoftKeyboardStateHelper(this);
            softKeyboardStateHelper.addSoftKeyboardStateListener(new SoftKeyboardStateHelper.SoftKeyboardStateListener() {
                @Override
                public void onSoftKeyboardOpened(int keyboardHeightInPx) {
                    h.removeCallbacks(updateTimeInfo);
                }
                @Override
                public void onSoftKeyboardClosed() {
                    h.removeCallbacks(updateTimeInfo);
                    if(isUpdateAppTime) {
                        h.postDelayed(updateTimeInfo, 1000);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    @Override
    protected void onStop() {
//        if(TEST){
//            return;
//        }
        if (updateReceiver!=null){
            updateReceiver.unReg();
        }
        appLoaderUtil.removeAppChangeListener(loadAppCallBack);
        h.removeCallbacks(updateInfo);
        h.removeCallbacks(updateTimeInfo);
        isShow = false;
        if(sharedPrefs.settings.getBoolean(Common.ALLSWITCH_DOZE,true)) {
            dozeFragment.destory();
        }
        //更新AMS中的数据
        if(WatchDogService.isNeedAMSReadLoad){
            XposedUtil.reloadInfos(this,
                    sharedPrefs.autoStartNetPrefs,
                    sharedPrefs.recentPrefs,
                    sharedPrefs.modPrefs,
                    sharedPrefs.settings,
                    sharedPrefs.skipDialogPrefs,
                    sharedPrefs.uiBarPrefs);
        }
        super.onStop();
    }
    @SuppressLint("RestrictedApi")
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if(TEST){
//            return;
//        }
        isUIRun = false;
//        if (sharedPrefs.settings.getBoolean(Common.ALLSWITCH_IFW,true)) {
//            ifwFragment.destory();
//        }
        if(fragments!=null){
            for(BaseFragment bf:fragments){
                if (bf==null){
                    continue;
                }
                bf.onDestroyView();
            }
            fragments = null;
        }
        MainActivity.this.getSupportFragmentManager().getFragments().clear();
        System.gc();
    }
    long backTime= 0;
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(pd!=null&&pd.isShowing()){
                pd.dismiss();
                pd = null;
                return;
            }
            if(System.currentTimeMillis()-backTime>1500){
                showT("再按一次退出程序！");
            }else{
                releaseAppIcons();
                BaseActivity.isPressBack = true;
                TopSearchView.searchText = "";
//                super.onBackPressed();
                moveTaskToBack(true);
                if(WatchDogService.isBackKillSelf){
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            WatchDogService.killAndRestartSelf(getApplicationContext());
                        }
                    },500);
                }
            }
            backTime = System.currentTimeMillis();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_MENU){
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }else{
                drawer.openDrawer(GravityCompat.START);
            }
        }else if(event.getKeyCode() == KeyEvent.KEYCODE_HOME){
            releaseAppIcons();
        }
        return super.onKeyDown(keyCode, event);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        this.setTitle(item.getTitle());
        menuTv.setVisibility(View.GONE);
        menuTv.setEnabled(true);
        menuTv.setAlpha(1.0f);
        page = id;
        if (id == R.id.nav_service_broad_control) {
            showOrHidden(controlFragment);
            menuTv.setVisibility(View.VISIBLE);
            if (!isModuleActive()){
                menuTv.setEnabled(false);
                menuTv.setAlpha(0.5f);
            }
        }else if (id == R.id.nav_backstop_mubei_control) {
            showOrHidden(forceStopFragment);
            menuTv.setVisibility(View.VISIBLE);
        }else if (id == R.id.nav_ifw_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(ifwFragment);
        }else if (id == R.id.nav_uninstall_ice_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(iceRoomFragment);
        }
        else if (id == R.id.nav_doze_control) {
            if( Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
                menuTv.setVisibility(View.VISIBLE);
                showOrHidden(dozeFragment);
            }else {
                navigationView.getMenu().getItem(9).setChecked(true);
                showT("打盹功能只有安卓6.0及以上系统才能使用");
            }
        }else if (id == R.id.nav_others_control) {
            menuTv.setVisibility(View.INVISIBLE);
            showOrHidden(otherFragment);
        }else if (id == R.id.nav_setting_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(settingFragment);
        }else if (id == R.id.nav_juanzeng_control) {
            menuTv.setVisibility(View.INVISIBLE);
            showOrHidden(juanZengFragment);
        }else if (id == R.id.nav_autostart_lock_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(appStartFragment);
        }else if (id == R.id.nav_ui_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(uiControlFragment);
        }else if (id == R.id.nav_adskip_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(adFragment);
        }else if (id == R.id.nav_recentcard_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(recentFragment);
        }else if (id == R.id.nav_question_log_control) {
            menuTv.setVisibility(View.INVISIBLE);
            showOrHidden(questionFragment);
        }else if (id == R.id.nav_cpuset_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(cpuFragment);
        }else if (id == R.id.nav_xpblacklist_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(xpBlackFragement);
        }else if (id == R.id.nav_privacy_control) {
            menuTv.setVisibility(View.VISIBLE);
            showOrHidden(privacyFragment);
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void menuClick(){
        menuTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                    if(!PermissionUtils.checkPermissionAllGranted(MainActivity.this,permissions)){
                        PermissionUtils.requestPermission(MainActivity.this,permissions);
                    }
                }

                String titles[] = {"备份设置","还原设置","清除所有","刷新应用"};
                if(page == R.id.nav_ifw_control){
                    titles = new String[]{"备份IFW","还原IFW","一键阉割","恢复阉割","清空所有"};
                }else if(page == R.id.nav_setting_control){
                    titles = new String[]{"备份所有项设置(IFW除外)","还原所有项设置(IFW除外)","清除所有项设置(IFW除外)","刷新应用"};
                }else if(page == R.id.nav_adskip_control){
                    titles = new String[]{"备份设置","还原设置","清除所有","刷新应用","载入部分APP预设"};
                }else if(page == R.id.nav_cpuset_control){
                    titles = new String[]{"备份设置","还原设置","清除所有"};
                }
                AlertUtil.showListAlert(MainActivity.this, "更多功能", titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, final int tag) {
                        AlertUtil.showConfirmAlertMsg(MainActivity.this, "请确认操作,防止误触。", new AlertUtil.InputCallBack() {
                            @Override
                            public void backData(String txt, int tag1) {
                                if (tag1 == 1){
                                    if(tag == 0){
                                        backupRestoreUtil.saveData(page);
                                    }else if(tag == 1){
                                        backupRestoreUtil.restoreData(page);
                                        appLoaderUtil.setIsPrefsChange(true);
                                        appLoaderUtil.loadAppSetting();
                                        if(page == R.id.nav_ifw_control){
                                            ifwFragment.loadDisableCount();
                                        }else if(page == R.id.nav_doze_control||
                                                page == R.id.nav_setting_control||
                                                page == R.id.nav_ui_control||
                                                page == R.id.nav_cpuset_control){
                                            restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                        }
                                        Intent intent = new Intent(MainActivity.this,WatchDogService.class);
                                        startService(intent);
                                    }else if(tag == 2){
                                        if(page ==R.id.nav_service_broad_control||
                                            page == R.id.nav_backstop_mubei_control||
                                            page == R.id.nav_doze_control||
                                            page == R.id.nav_setting_control||
                                            page == R.id.nav_autostart_lock_control||
                                            page == R.id.nav_ui_control||
                                            page == R.id.nav_adskip_control||
                                            page == R.id.nav_privacy_control||
                                            page == R.id.nav_xpblacklist_control||
                                            page == R.id.nav_cpuset_control||
                                            page == R.id.nav_recentcard_control ){
                                                if(page == R.id.nav_service_broad_control){
                                                    sharedPrefs.modPrefs.edit().clear().commit();
                                                    sharedPrefs.wakeLockPrefs.edit().clear().commit();
                                                    sharedPrefs.alarmPrefs.edit().clear().commit();
                                                }else if(page == R.id.nav_backstop_mubei_control){
                                                    sharedPrefs.forceStopPrefs.edit().clear().commit();
                                                }else if(page == R.id.nav_doze_control){
                                                    sharedPrefs.dozePrefs.edit().clear().commit();
                                                    showT("清除成功,重新打开生效");
                                                }else if(page == R.id.nav_setting_control){
                                                    sharedPrefs.modPrefs.edit().clear().commit();
                                                    sharedPrefs.forceStopPrefs.edit().clear().commit();
                                                    sharedPrefs.autoStartNetPrefs.edit().clear().commit();
                                                    sharedPrefs.recentPrefs.edit().clear().commit();
                                                    sharedPrefs.dozePrefs.edit().clear().commit();
                                                    sharedPrefs.uiBarPrefs.edit().clear().commit();
                                                    sharedPrefs.xpBlackListPrefs.edit().clear().commit();
                                                    sharedPrefs.adPrefs.edit().clear().commit();
                                                    sharedPrefs.skipDialogPrefs.edit().clear().commit();
                                                    sharedPrefs.pmPrefs.edit().clear().commit();
                                                    sharedPrefs.settings.edit().clear().commit();
                                                    sharedPrefs.setTimeStopPrefs.edit().clear().commit();
                                                    sharedPrefs.cpuPrefs.edit().clear().commit();
                                                    sharedPrefs.wakeLockPrefs.edit().clear().commit();
                                                    sharedPrefs.alarmPrefs.edit().clear().commit();
                                                    sharedPrefs.privacyPrefs.edit().clear().commit();
                                                    sharedPrefs.settings.edit().putInt(Common.PREFS_SETTING_SCREENWIDTH,0).commit();
                                                    XposedUtil.reloadInfos(MainActivity.this,
                                                            sharedPrefs.autoStartNetPrefs,
                                                            sharedPrefs.recentPrefs,
                                                            sharedPrefs.modPrefs,
                                                            sharedPrefs.settings,
                                                            sharedPrefs.skipDialogPrefs,
                                                            sharedPrefs.uiBarPrefs);
                                                    restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                                }else if(page == R.id.nav_autostart_lock_control){
                                                    sharedPrefs.autoStartNetPrefs.edit().clear().commit();
                                                }else if(page == R.id.nav_ui_control){
                                                    sharedPrefs.uiBarPrefs.edit().clear().commit();
                                                    restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                                }else if(page == R.id.nav_adskip_control){
                                                    sharedPrefs.adPrefs.edit().clear().commit();
                                                    sharedPrefs.skipDialogPrefs.edit().clear().commit();
                                                }else if(page == R.id.nav_recentcard_control){
                                                    sharedPrefs.recentPrefs.edit().clear().commit();
                                                }else if(page == R.id.nav_cpuset_control){
                                                    sharedPrefs.cpuPrefs.edit().clear().commit();
                                                    restartSelfConfirm("重新启动应用控制器生效，是否重启控制器？");
                                                }else if(page == R.id.nav_xpblacklist_control){
                                                    sharedPrefs.xpBlackListPrefs.edit().clear().commit();
                                                }else if(page == R.id.nav_privacy_control){
                                                    sharedPrefs.privacyPrefs.edit().clear().commit();
                                                }
                                                appLoaderUtil.setIsPrefsChange(true);
                                                appLoaderUtil.loadAppSetting();
                                        }else if(page== R.id.nav_ifw_control){
                                            ifwFragment.startDis();
                                        }
                                    }else if(tag == 3){
                                        if (page ==  R.id.nav_ifw_control){
                                            ifwFragment.startBackDis();
                                        }else{
                                            pd = ProgressDialog.show(MainActivity.this,"","正在加载应用列表...",true,true);
                                            appLoaderUtil.setIsAppChange(true);
                                            appLoaderUtil.loadApp();
                                        }
                                    }else if(tag == 4){
                                        if (page ==  R.id.nav_ifw_control){
                                            ifwFragment.startClear();
                                        }else if (page == R.id.nav_adskip_control){
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

    class MyUpdateListReceiver extends BroadcastReceiver{
        public void reg(){
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("com.click369.control.recappidlestate");
            intentFilter.addAction("com.click369.control.backprocinfo");
            intentFilter.addAction("com.click369.control.recpreventinfo");
            intentFilter.addAction("com.click369.control.watchdogtellupdateui");
            intentFilter.addAction("com.click369.control.hookok");
            MainActivity.this.registerReceiver(this,intentFilter);
        }
        public void unReg(){
            MainActivity.this.unregisterReceiver(this);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if("com.click369.control.recappidlestate".equals(action)){
                    pkgIdleStates.clear();
                    HashSet<String> pkgs = ( HashSet<String>)intent.getSerializableExtra("pkgs");
                    if(intent.hasExtra("mbpkgs")){
                        HashSet<String> mbpkgs = ( HashSet<String>)intent.getSerializableExtra("mbpkgs");
                        for(String p:mbpkgs){
                            if( appLoaderUtil.allHMAppInfos.containsKey(p)){
                                appLoaderUtil.allHMAppInfos.get(p).isInMuBei = true;
                            }
                        }
                    }

                    if(appLoaderUtil.allHMAppInfos.containsKey("com.tencent.mm")){
                        AppInfo ai = appLoaderUtil.allHMAppInfos.get("com.tencent.mm");
                        if(ai.isHomeIdle||ai.isHomeMuBei||ai.isBackMuBei){
                            AppLoaderUtil.allAppStateInfos.get("com.tencent.mm").isInIdle = true;
                            pkgIdleStates.add("com.tencent.mm");
                        }
                    }

                    pkgIdleStates.addAll(pkgs);
                    if(chooseFragment!=null){
                        chooseFragment.fresh();
                    }
                }else if("com.click369.control.backprocinfo".equals(action)){
                    HashMap<String,Long> procTimeInfos = (HashMap<String,Long>)intent.getSerializableExtra("infos");
                    setProcBgTimeInfos(procTimeInfos);
                    if(intent.hasExtra("runtimes")){
                        HashMap<String,Long> procRunTimes = (HashMap<String,Long>)intent.getSerializableExtra("runtimes");
                        setProcStartTimeInfos(procRunTimes);
                    }
                    if(chooseFragment!=null){
                        chooseFragment.fresh();
                    }
                }else if("com.click369.control.recpreventinfo".equals(action)){
                    questionFragment.onReceive(intent);
                }else if("com.click369.control.hookok".equals(action)){
                    WatchDogService.ISHOOKOK = true;
                    if(chooseFragment instanceof ControlFragment){
                        ((ControlFragment)chooseFragment).initAlert();
                    }
                }else if("com.click369.control.watchdogtellupdateui".equals(action)){
                    h.removeCallbacks(updateInfo);
                    h.postDelayed(updateInfo,200);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==0x12){
            if(data!=null&&data.hasExtra("color")){
                THEME_COLOR = data.getStringExtra("color");
                THEME_TEXT_COLOR = data.getStringExtra("textcolor");
                initThemeColor();
            }else if(data!=null&&data.hasExtra("bgcolor")){
                THEME_BG_COLOR = data.getStringExtra("bgcolor");
                setBgColor(Color.parseColor(THEME_BG_COLOR));
            }
            if(data!=null&&isNightMode){
                AlertUtil.showAlertMsg(this,"目前是处于夜间模式，请在界面控制中退出夜间模式查看效果!");
            }
        }else if(resultCode==0x15){
            if(data!=null&&data.hasExtra("key_fileName")){
                String key_fileName = data.getStringExtra("key_fileName");
                int blur = data.getIntExtra("blur",0);
                int bright = data.getIntExtra("bright",100);
                String key_blur = data.getStringExtra("key_blur");
                String key_bright = data.getStringExtra("key_bright");
                Log.i("CONTROL","blur "+blur+"  bright "+bright);
                Log.i("CONTROL","key_blur "+key_blur+"  key_bright "+key_bright);
                sharedPrefs.uiBarPrefs.edit().putInt(key_blur,blur).commit();
                sharedPrefs.uiBarPrefs.edit().putInt(key_bright,bright).commit();
                if(key_fileName.equals(bgFile.getAbsolutePath())){
                    initFileBg(1);
                }else if(key_fileName.equals(sideBgFile.getAbsolutePath())){
                    initFileBg(2);
                }
            }
        }else if(data!=null){
            File file = getPhoto.onActivityResult(requestCode,resultCode,data);
            if (file!=null&&file.exists()) {
                if(file.getName().equals("bg")){
                    if(bgBlurFile.exists()){
                        bgBlurFile.delete();
                    }
                    sharedPrefs.uiBarPrefs.edit().remove(Common.PREFS_SETTING_UI_BGBLUR).remove(Common.PREFS_SETTING_UI_BGBRIGHT).commit();
                    initFileBg(1);
                }else if(file.getName().equals("side")){
                    if(sideBgBlurFile.exists()){
                        sideBgBlurFile.delete();
                    }
                    sharedPrefs.uiBarPrefs.edit().remove(Common.PREFS_SETTING_UI_SIDEBGBLUR).remove(Common.PREFS_SETTING_UI_SIDEBGBRIGHT).commit();
                    initFileBg(2);
                }
            }
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

    class LoadAppCallBackImp implements AppLoaderUtil.LoadAppCallBack{
        @Override
        public void onLoadLocalAppFinish() {
            if(AppLoaderUtil.allHMAppInfos.size()>0){
                if(!appLoaderUtil.isAppChange){
                    appLoaderUtil.loadAppSetting();
                }
                loadAppIcons();
            }
        }
        @Override
        public void onLoadAppFinish() {
            loadAppIcons();
        }

        @Override
        public void onRuningStateChange() {
            if(chooseFragment!=null){
                chooseFragment.fresh();
            }
            h.removeCallbacks(updateInfo);
            h.postDelayed(updateInfo,2000);
//            Log.i("CONTROL","运行状态发生变化 MainActivity 更新");
        }

        @Override
        public void onLoadAppSettingFinish() {
            if(chooseFragment!=null){
                chooseFragment.fresh();
            }
        }
    }

    public static void releaseAppIcons(){
        try {
//            final Set<String> pkgs = AppLoaderUtil.allHMAppIcons.keySet();
//            for (String p : pkgs) {
//                AppLoaderUtil.allHMAppIcons.get(p).recycle();
//            }
//            AppLoaderUtil.allHMAppIcons.clear();
//            System.gc();
        }catch (Exception e){

        }
    }

    boolean isInLoadNewIcon = false;
    private void loadAppIcons(){
        h.removeCallbacks(loadIcons);
        h.postDelayed(loadIcons,400);
    }

    Runnable loadIcons = new Runnable() {
        @Override
        public void run() {
            new Thread(){
                @Override
                public void run() {
                    try {
                    synchronized (MainActivity.this) {
//                    Log.i("CONTROL","load icons");
                        final Set<String> pkgs = new HashSet<>();
                        pkgs.addAll(AppLoaderUtil.allHMAppInfos.keySet());
                        pkgs.add(Common.PACKAGENAME);
                        isInLoadNewIcon = false;
                        for (String p : pkgs) {
                            File f = new File(AppLoaderUtil.iconPath, p);
                            if (f.exists() && !AppLoaderUtil.allHMAppIcons.containsKey(p)) {
                                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                                AppLoaderUtil.allHMAppIcons.put(p, bitmap);
                                isInLoadNewIcon = true;
//                                Log.i("CONTROL","a==="+p);
                            }else if(!f.exists()){
                                PackageInfo packgeInfo = getPackageManager().getPackageInfo(p,PackageManager.GET_META_DATA);
                                AppLoaderUtil.loadAppImage(packgeInfo,getPackageManager(),true);
                                f = new File(AppLoaderUtil.iconPath, p);
//                                Log.i("CONTROL","b==="+p);
                                if (f.exists() && !AppLoaderUtil.allHMAppIcons.containsKey(p)) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                                    AppLoaderUtil.allHMAppIcons.put(p, bitmap);
                                }
                                isInLoadNewIcon = true;
                            }
                        }
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    if(chooseFragment!=null&&isInLoadNewIcon){
                                        chooseFragment.fresh();
                                    }
                                    if(pd!=null&&pd.isShowing()){
                                        pd.dismiss();
                                    }
                                    pd = null;
                                }catch (Throwable e){
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    };

    Runnable updateInfo = new Runnable() {
        @Override
        public void run() {
//            if(TEST){
//                return;
//            }
//            sendBroadcast(new Intent(("com.click369.control.ams.getprocinfo")));
            HashMap<String, Boolean> pkgs = new HashMap<String, Boolean>();
            appLoaderUtil.reloadRunList();
            for (String s : AppLoaderUtil.runLists) {
                pkgs.put(s, false);
            }
            Intent intent = new Intent("com.click369.control.uss.getappidlestate");
            intent.putExtra("pkgs", pkgs);
            sendBroadcast(intent);
        }
    };

    Runnable updateTimeInfo = new Runnable() {
        @Override
        public void run() {
//            if(TEST){
//                return;
//            }
//            sendBroadcast(new Intent(("com.click369.control.ams.getprocinfo")));
            if(chooseFragment!=null){
                chooseFragment.fresh();
            }
            if(isUpdateAppTime) {
                h.postDelayed(updateTimeInfo, 10000);
            }
        }
    };

    public void update(){
        long time = sharedPrefs.settings.getLong("UPDATE_CHECK_TIME",0);
        if(System.currentTimeMillis()-time<1000*60*60*4){
            return;
        }
        new Thread(){
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    String s = get("https://www.coolapk.com/apk/com.click369.controlbp");
                    String news = s.substring(s.indexOf("<title>应用控制器(com.click369.controlbp) - "),s.indexOf(" - 应用 - 酷安网</title>"));
                    news = news.replace("<title>应用控制器(com.click369.controlbp) - ","");
                    String not = sharedPrefs.settings.getString("NOT_ALERT_UPDATE","");;
                    if(!TextUtils.isEmpty(news)&&news.contains(".")&&!not.equals(news)){
                        String newss[] = news.split("\\.");
                        String n = "";
                        for(String a:newss){
                            n+= a;
                        }
                        String oldss[] = BuildConfig.VERSION_NAME.split("\\.");
                        String m = "";
                        for(String a:oldss){
                            m+= a;
                        }
                        final String v = news;
                        if(Integer.parseInt(m)<Integer.parseInt(n)){
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    AlertUtil.showUpdateAlertMsg(MainActivity.this, "检测到应用控制器有新版本" + v + ",请去下载更新。", new AlertUtil.InputCallBack() {
                                        @Override
                                        public void backData(String txt, int tag) {
                                            if(tag == 0){
                                                sharedPrefs.settings.edit().putString("NOT_ALERT_UPDATE",v).commit();
                                            }else if(tag == 1){

                                            }else if(tag == 2){
                                                Intent intent = new Intent();
                                                intent.setAction("android.intent.action.VIEW");
                                                Uri content_url = Uri.parse("https://www.coolapk.com/apk/com.click369.controlbp");
                                                intent.setData(content_url);
                                                startActivity(intent);
                                            }

                                        }
                                    });
                                    sharedPrefs.settings.edit().putLong("UPDATE_CHECK_TIME",System.currentTimeMillis()).commit();
                                }
                            });
                        }else{
                            Log.i("CONTROL","没有新版本");
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static String get(String ss){
        try{
            URL url = new URL(ss);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            InputStream in = connection.getInputStream();
            byte datas[] = new byte[1024];
            int len = 0;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            while((len=in.read(datas))!=-1){
                byteArrayOutputStream.write(datas,0,len);
            }
            in.close();
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            connection.disconnect();
            byte mDatas[] = byteArrayOutputStream.toByteArray();
            final String urlStr = new String(mDatas);
            return urlStr;
        }catch (Throwable e){

        }
        return null;
    }
}

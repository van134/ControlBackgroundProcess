package com.click369.controlbp.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.BuildConfig;
import com.click369.controlbp.R;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.service.XposedUtil;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;

public class AppConfigActivity extends BaseActivity {
    ImageView serviceImg,broadImg,wakeLockImg,alarmImg;
    ImageView backImg,homeImg,offScImg,notifyImg;
    ImageView keepImg,killImg,blurImg,hideImg;
    ImageView lockImg,notRunImg,notAutoStartImg,notStopImg;
    ImageView dongJieImg,notUninstallImg,unInstallImg;
    ImageView mode1Img,mode2Img,mode3Img;
    ImageView dozeQianTaiImg,dozeOffScImg,dozeOnScImg;
    ImageView ifwServceImg,ifwBroadImg,ifwActivityImg;
    TextView ifwServceTitle,ifwBroadTitle,ifwActivityTitle;
    ImageView barOpenImg,barLockImg;
    TextView subTitle;
    AppInfo ai;
//    public SharedPreferences modPrefs,wakeLockPrefs,alarmPrefs,forceStopPrefs,muBeiPrefs,settings,ifwCountPrefs,uiBarPrefs,autoStartNetPrefs,recentPrefs,dozePrefs,adPrefs,pmPrefs;

//    boolean isClick =false;
    String appName =null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MainActivity.isNightMode){
            setTheme(R.style.AppTheme_NoActionBarDarkFloat);
        }
//        isClick =false;
        setContentView(R.layout.activity_floatappconfig);
        setWindowStatusBarColor(this,R.color.transhalf);
        LinearLayout ll = (LinearLayout)this.findViewById(R.id.config_main_ll);
        FrameLayout fl = (FrameLayout)this.findViewById(R.id.config_title_fl);
        TextView title = (TextView)this.findViewById(R.id.config_title);
        subTitle = (TextView)this.findViewById(R.id.config_subtitle);
        TextView moreTitle = (TextView)this.findViewById(R.id.config_more_title);
        setClipViewCornerRadius(ll,30);
        if(MainActivity.isNightMode){
            ll.setBackgroundColor(Color.BLACK);
            fl.setBackgroundColor(Color.DKGRAY);
        }else{
            ll.setBackgroundColor(Color.WHITE);
            fl.setBackgroundColor(Color.parseColor("#1a9dac"));
        }
        ai = new AppInfo();
        final Intent intent = this.getIntent();
        String pkg = intent.getStringExtra("pkg");
        //从哪来  newapp 或nowapp
        String from = intent.getStringExtra("from");
        if(TextUtils.isEmpty(pkg)){
            pkg = WatchDogService.nowPkgName;
        }
        if(ContainsKeyWord.isContainsPkg(pkg)){
           finish();
           return;
        }
        if (BuildConfig.APPLICATION_ID.equals(pkg)){
            showT("检测到当前打开的应用为应用控制器，控制面板不会显示");
            finish();
            return;
        }
        if(TextUtils.isEmpty(pkg)){
            if(WatchDogService.isKillRun){
                showT("还未检测到你打开的应用");
            }else{
                showT("必须保证应用控制器的后台服务不被杀死才能使用");
            }
            finish();
        }else{
            appName = PackageUtil.getAppNameByPkg(this,pkg);
            if(WatchDogService.newInstallAppList.contains(pkg)){
                WatchDogService.newInstallAppList.remove(pkg);
            }
           title.setText("对<"+appName+">进行控制");
            ai.setAppName(appName);
            ai.setPackageName(pkg);
            if(AppLoaderUtil.allHMAppInfos.containsKey(ai.packageName)){
                ai = AppLoaderUtil.allHMAppInfos.get(ai.packageName);
            }else{
                PackageManager pm = getPackageManager();
                try {
                    PackageInfo packgeInfo = pm.getPackageInfo(ai.packageName,PackageManager.GET_ACTIVITIES);
                    ai = AppLoaderUtil.getInstance(this).getOneAppInfo(packgeInfo,pm,this);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        final AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(ai.packageName)?AppLoaderUtil.allAppStateInfos.get(ai.packageName):new AppStateInfo();
        moreTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity.zhenDong(AppConfigActivity.this);
                String titles[]={"结束"+appName+"进程","打开"+appName+"应用信息","重启"+appName+"进程","打开"+appName+"后定时结束","清除"+appName+"所有控制（IFW除外）","打开应用控制器"};
                if(ai.isSetTimeStopApp){
                    titles = new String[]{"结束"+appName+"进程","打开"+appName+"应用信息","重启"+appName+"进程","打开"+appName+"后定时结束","取消"+appName+"的定时结束设置","清除"+appName+"所有控制（IFW除外）","打开应用控制器"};
                }
                AlertUtil.showListAlert(AppConfigActivity.this, "请选择", titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            XposedStopApp.stopApk(ai.getPackageName(),AppConfigActivity.this);
                            finish();
                        }else if(tag==1){
                            getAppDetailSettingIntent();
                            finish();
                        }else if(tag==2){
                            XposedStopApp.stopApk(ai.getPackageName(),AppConfigActivity.this);
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    OpenCloseUtil.doStartApplicationWithPackageName(ai.packageName,AppConfigActivity.this);
                                    finish();
                                }
                            },300);
                        }else if(tag==3){
                            final String t = ai.isSetTimeStopApp? ai.setTimeStopAppTime+"":"";
                            String []titles1 = new String[]{"仅下次生效","永久生效"};
                            AlertUtil.showListAlert(AppConfigActivity.this, "请选择模式", titles1, new AlertUtil.InputCallBack() {
                                @Override
                                public void backData(String txt, int tag) {
                                    if (tag == 0){
                                        AlertUtil.inputAlertCustomer(AppConfigActivity.this,"请输入时长,仅生效一次（输入0可取消已设置的时间），单位：分钟","",t,new AlertUtil.InputCallBack(){
                                            @Override
                                            public void backData(String txt, int tag) {
                                                int time = Integer.parseInt(txt);
                                                if (time>0){
                                                    ai.setTimeStopAppTime = time;
                                                    ai.isSetTimeStopApp = true;
                                                    ai.isSetTimeStopOneTime = true;
                                                    sharedPrefs.setTimeStopPrefs.edit().putInt(ai.packageName+"/one",time).commit();
                                                    sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
//                                                    WatchDogService.setTimeStopApp.put(ai.packageName,time);
                                                    Toast.makeText(AppConfigActivity.this,"当该应用打开后将会在"+time+"分钟后强行关闭",Toast.LENGTH_LONG).show();
                                                }else{
//                                                    WatchDogService.setTimeStopApp.remove(ai.packageName);
//                                                    WatchDogService.stopAppName.remove(ai.packageName);
                                                    ai.setTimeStopAppTime = 0;
                                                    ai.isSetTimeStopApp = false;
                                                    ai.isSetTimeStopOneTime = false;
                                                    sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
                                                    sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();
                                                    Toast.makeText(AppConfigActivity.this,"没有设置有效的时间",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else if(tag==1){
                                        AlertUtil.inputAlertCustomer(AppConfigActivity.this,"请输入时长（输入0可取消已设置的时间），单位：分钟","",t,new AlertUtil.InputCallBack(){
                                            @Override
                                            public void backData(String txt, int tag) {
                                                int time = Integer.parseInt(txt);
                                                SharedPreferences setTimeStop = SharedPrefsUtil.getPreferences(AppConfigActivity.this, Common.PREFS_SETTIMESTOP);
                                                if (time>0){
                                                    setTimeStop.edit().putInt(ai.packageName,time).commit();
                                                    ai.setTimeStopAppTime = time;
                                                    ai.isSetTimeStopApp = true;
                                                    ai.isSetTimeStopOneTime = false;
                                                    sharedPrefs.setTimeStopPrefs.edit().putInt(ai.packageName+"/long",time).commit();
                                                    sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();
                                                    Toast.makeText(AppConfigActivity.this,"当该应用每次打开后将会在"+time+"分钟后强行关闭",Toast.LENGTH_LONG).show();
                                                }else{
                                                    setTimeStop.edit().remove(ai.packageName).commit();
                                                    ai.setTimeStopAppTime = 0;
                                                    ai.isSetTimeStopApp = false;
                                                    ai.isSetTimeStopOneTime = false;
                                                    sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
                                                    sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();
                                                    Toast.makeText(AppConfigActivity.this,"没有设置有效的时间",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }else if(tag==4){
                            if(ai.isSetTimeStopApp){
                                ai.isSetTimeStopApp = false;
                                ai.isSetTimeStopOneTime = false;
                                ai.setTimeStopAppTime = 0;
                                sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
                                sharedPrefs.setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();
                            }else{
                                AlertUtil.showConfirmAlertMsg(AppConfigActivity.this, "是否清除该应用所有设置（IFW除外）?", new AlertUtil.InputCallBack() {
                                    @Override
                                    public void backData(String txt, int tag) {
                                        if (tag==1){
                                            sharedPrefs.clearAppSettings(ai,false);
//                                            isClick = true;
                                            finish();
                                            showT("清除完成");
                                        }
                                    }
                                });
                            }
                        }else if(tag==5){
                            if(ai.isSetTimeStopApp){
                                AlertUtil.showConfirmAlertMsg(AppConfigActivity.this, "是否清除该应用所有设置（IFW除外）?", new AlertUtil.InputCallBack() {
                                    @Override
                                    public void backData(String txt, int tag) {
                                        if (tag==1){
                                            sharedPrefs.clearAppSettings(ai,false);
//                                            isClick = true;
                                            finish();
                                            showT("清除完成");
                                        }
                                    }
                                });
                            }else{
                                sendBroadcast(new Intent("com.click369.control.openmain"));
                                finish();
                            }
                        }else if(tag == 6){
                            sendBroadcast(new Intent("com.click369.control.openmain"));
                            finish();
                        }
                    }
                });
            }
        });
//        initData();
        initView();
        initState();
        initClickListener();
    }
    private void initView(){
        serviceImg = (ImageView) this.findViewById(R.id.item_service);
        broadImg = (ImageView) this.findViewById(R.id.item_broad);
        wakeLockImg = (ImageView) this.findViewById(R.id.item_wakelock);
        alarmImg = (ImageView) this.findViewById(R.id.item_alarm);

        backImg = (ImageView) this.findViewById(R.id.item_back);
        homeImg = (ImageView) this.findViewById(R.id.item_home);
        offScImg = (ImageView) this.findViewById(R.id.item_off);
        notifyImg = (ImageView) this.findViewById(R.id.item_notify);

        keepImg = (ImageView) this.findViewById(R.id.item_keep);
        killImg = (ImageView) this.findViewById(R.id.item_kill);
        blurImg = (ImageView) this.findViewById(R.id.item_blur);
        hideImg = (ImageView) this.findViewById(R.id.item_hide);

        lockImg = (ImageView) this.findViewById(R.id.item_lock);
        notRunImg = (ImageView) this.findViewById(R.id.item_stoprun);
        notAutoStartImg = (ImageView) this.findViewById(R.id.item_autostart);
        notStopImg = (ImageView) this.findViewById(R.id.item_notstop);

        dongJieImg = (ImageView) this.findViewById(R.id.item_dongjie);
        notUninstallImg = (ImageView) this.findViewById(R.id.item_notuninstall);
        unInstallImg = (ImageView) this.findViewById(R.id.item_uninstall);

        mode1Img = (ImageView) this.findViewById(R.id.item_ad_one);
        mode2Img = (ImageView) this.findViewById(R.id.item_ad_two);
        mode3Img = (ImageView) this.findViewById(R.id.item_ad_three);

        dozeQianTaiImg = (ImageView) this.findViewById(R.id.item_doze_qiantai);
        dozeOffScImg = (ImageView) this.findViewById(R.id.item_doze_off);
        dozeOnScImg = (ImageView) this.findViewById(R.id.item_doze_on);

        ifwServceImg = (ImageView) this.findViewById(R.id.item_ifw_service);
        ifwBroadImg = (ImageView) this.findViewById(R.id.item_ifw_broad);
        ifwActivityImg = (ImageView) this.findViewById(R.id.item_ifw_activity);
        ifwServceTitle = (TextView) this.findViewById(R.id.item_ifw_service_title);
        ifwBroadTitle = (TextView) this.findViewById(R.id.item_ifw_broad_title);
        ifwActivityTitle = (TextView) this.findViewById(R.id.item_ifw_activity_title);

        barOpenImg = (ImageView) this.findViewById(R.id.item_bar_on);
        barLockImg = (ImageView) this.findViewById(R.id.item_bar_lock);
    }

    private void initState(){
        serviceImg.setImageResource(ai.isServiceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
        broadImg.setImageResource(ai.isBroadStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
        wakeLockImg.setImageResource(ai.isWakelockStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
        alarmImg.setImageResource(ai.isAlarmStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);

        offScImg.setImageResource(ai.isOffscMuBei?R.mipmap.icon_dead:ai.isOffscForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
        backImg.setImageResource(ai.isBackMuBei?R.mipmap.icon_dead:ai.isBackForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
        homeImg.setImageResource(ai.isHomeMuBei?R.mipmap.icon_dead:ai.isHomeIdle?R.mipmap.icon_idle:R.mipmap.icon_notdisable);//data.isHomeForceStop?R.mipmap.icon_disable:
        notifyImg.setImageResource(ai.isNotifyNotExit?R.mipmap.icon_add:R.mipmap.icon_notdisable);

        keepImg.setImageResource(ai.isRecentNotClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        killImg.setImageResource(ai.isRecentForceClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        blurImg.setImageResource(ai.isRecentBlur?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        hideImg.setImageResource(ai.isRecentNotShow?R.mipmap.icon_add:R.mipmap.icon_notdisable);

        lockImg.setImageResource(ai.isLockApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        notRunImg.setImageResource(ai.isStopApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        notAutoStartImg.setImageResource(ai.isAutoStart?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        notStopImg.setImageResource(ai.isNotStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);

        dongJieImg.setImageResource(ai.isDisable?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        notUninstallImg.setImageResource(ai.isNotUnstall?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        unInstallImg.setImageResource(R.mipmap.icon_notdisable);

        mode1Img.setImageResource((sharedPrefs.adPrefs.getInt(ai.packageName+"/ad",0)==1)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
        mode2Img.setImageResource((sharedPrefs.adPrefs.getInt(ai.packageName+"/ad",0)==2)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
        mode3Img.setImageResource((sharedPrefs.adPrefs.getInt(ai.packageName+"/ad",0)==3)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);

        dozeOffScImg.setImageResource(ai.isDozeOffsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        dozeOnScImg.setImageResource(ai.isDozeOnsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        dozeQianTaiImg.setImageResource(ai.isDozeOpenStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);

        barOpenImg.setImageResource(ai.isBarColorList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
        barLockImg.setImageResource(ai.isBarLockList?R.mipmap.icon_add:R.mipmap.icon_notdisable);

        barLockImg.setEnabled(ai.isBarColorList);
        barLockImg.setAlpha(ai.isBarColorList?1.0f:0.6f);

        String serStr = ai.serviceDisableCount+"/"+ai.serviceCount;
        ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
        SpannableStringBuilder builder = new SpannableStringBuilder(serStr);
        builder.setSpan(redSpan,0, serStr.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ifwServceTitle.append(" ");
        ifwServceTitle.append(builder);

        serStr = ai.broadCastDisableCount+"/"+ai.broadCastCount;
        builder = new SpannableStringBuilder(serStr);
        builder.setSpan(redSpan,0, serStr.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ifwBroadTitle.append(" ");
        ifwBroadTitle.append(builder);

        serStr = ai.activityDisableCount+"/"+ai.activityCount;
        builder = new SpannableStringBuilder(serStr);
        builder.setSpan(redSpan,0, serStr.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ifwActivityTitle.append(" ");
        ifwActivityTitle.append(builder);
        subTitle.setText(ai.getPackageName()+"\n版本:"+ai.versionName+" 版本号:"+ai.versionCode);
        subTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(ai.getPackageName());
                Toast.makeText(AppConfigActivity.this,"包名已复制到粘贴板",Toast.LENGTH_LONG).show();

                return false;
            }
        });
    }

    private void initData(){


//        ifwCountPrefs = SharedPrefsUtil.getPreferences(this, Common.PREFS_APPIFWCOUNT);// getApplicationContext().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
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
//        pmPrefs = SharedPrefsUtil.getPreferences(this,Common.IPREFS_PMLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);


//        ai.isServiceStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/service", false);
//        ai.isBroadStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/broad", false);
//        ai.isWakelockStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/wakelock", false);
//        ai.isAlarmStop = sharedPrefs.modPrefs.getBoolean(ai.getPackageName() + "/alarm", false);
//
//        ai.isBackForceStop = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/backstop", false);
//        ai.isBackMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/backmubei", false);
//        ai.isOffscForceStop = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/offstop", false);
//        ai.isOffscMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/offmubei", false);
////        ai.isInMuBei = sharedPrefs.muBeiPrefs.getInt(ai.getPackageName(), -1)==0;
//        ai.isHomeMuBei = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/homemubei", false);
//        ai.isHomeIdle = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/idle", false);
//        ai.isNotifyNotExit = sharedPrefs.forceStopPrefs.getBoolean(ai.getPackageName() + "/notifynotexit", false);
//
//        ai.isAutoStart = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/autostart", false);
//        ai.isStopApp = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/stopapp", false);
//        ai.isLockApp = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/lockapp", false);
//        ai.isNotStop = sharedPrefs.autoStartNetPrefs.getBoolean(ai.getPackageName() + "/notstop", false);
//
//        ai.isDozeOffsc = sharedPrefs.dozePrefs.getBoolean(ai.getPackageName() + "/offsc", false);
//        ai.isDozeOnsc = sharedPrefs.dozePrefs.getBoolean(ai.getPackageName() + "/onsc", false);
//        ai.isDozeOpenStop = sharedPrefs.dozePrefs.getBoolean(ai.getPackageName() + "/openstop", false);
//
//        ai.isRecentNotClean = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/notclean", false);
//        ai.isRecentForceClean = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/forceclean", false);
//        ai.isRecentBlur = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/blur", false);
//        ai.isRecentNotShow = sharedPrefs.recentPrefs.getBoolean(ai.getPackageName() + "/notshow", false);
//
//        ai.isBarLockList = sharedPrefs.uiBarPrefs.getBoolean(ai.getPackageName() + "/locklist", false);
//        ai.isBarColorList = sharedPrefs.uiBarPrefs.getBoolean(ai.getPackageName() + "/colorlist", false);
//
//        ai.isNotUnstall = sharedPrefs.pmPrefs.getBoolean(ai.getPackageName() + "/notunstall", false);
//
//        ai.serviceDisableCount = sharedPrefs.ifwCountPrefs.getInt(ai.getPackageName() + "/ifwservice", 0);
//        ai.broadCastDisableCount = sharedPrefs.ifwCountPrefs.getInt(ai.getPackageName() + "/ifwreceiver", 0);
//        ai.activityDisableCount = sharedPrefs.ifwCountPrefs.getInt(ai.getPackageName() + "/ifwactivity", 0);
//        ai.isADJump = sharedPrefs.adPrefs.getInt(ai.getPackageName() + "/ad", 0) != 0;
//        PackageManager  pm = getPackageManager();
//        try{
//            PackageInfo piS = pm.getPackageInfo(ai.packageName, PackageManager.GET_SERVICES | PackageManager.GET_DISABLED_COMPONENTS);
//            PackageInfo piB = pm.getPackageInfo(ai.packageName, PackageManager.GET_RECEIVERS | PackageManager.GET_DISABLED_COMPONENTS);
//            PackageInfo piA = pm.getPackageInfo(ai.packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
//            ai.activityCount = piA.activities != null ? piA.activities.length : 0;
//            ai.serviceCount = piS.services != null ? piS.services.length : 0;
//            ai.broadCastCount = piB.receivers != null ? piB.receivers.length : 0;
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
    private void initClickListener(){
        class ForceStopListener implements View.OnClickListener{
            @Override
            public void onClick(View view) {
//                isClick = true;
                BaseActivity.zhenDong(AppConfigActivity.this);
                SharedPreferences.Editor ed = sharedPrefs.forceStopPrefs.edit();
                switch (view.getId()){
                    case R.id.item_back:
                        if (ai.isBackForceStop){
                            ed.remove(ai.getPackageName()+"/backstop").commit();
                            ai.isBackForceStop = false;
                            if(MainActivity.isLinkStopAndAuto&&ai.isAutoStart&&!ai.isOffscForceStop){
                                sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
                                ai.isAutoStart = false;
                            }
                            if (MainActivity.isLinkStopAndRemoveStop&&ai.isRecentForceClean&&!ai.isOffscForceStop) {
                                sharedPrefs.recentPrefs.edit().remove(ai.getPackageName() + "/forceclean").commit();
                                ai.isRecentForceClean = false;
                            }
                            if (MainActivity.isModuleActive()) {
                                if (ai.isServiceStop){
                                    AlertUtil.showAlertMsg(AppConfigActivity.this,"检测到你已经在第一项的禁用服务中禁用了该应用的服务，所以设置墓碑模式将不会生效，如果要使用墓碑模式请在第一项的禁用服务中取消勾选。");
                                }else{
                                    if (ai.isHomeMuBei){
                                        AlertUtil.showAlertMsg(AppConfigActivity.this,"检测到你已经设置了后台时墓碑，所以设置返回时墓碑将不会生效");
                                    }else {
                                        ed.putBoolean(ai.getPackageName() + "/backmubei", true).commit();
                                        ai.isBackMuBei = true;
                                    }
                                }
                            }else{
                                showT("墓碑模式需要XP支持");
                            }
                        }else if (ai.isBackMuBei){
                            ed.remove(ai.getPackageName()+"/backstop").commit();
                            ai.isBackForceStop = false;
                            if(MainActivity.isLinkStopAndAuto&&ai.isAutoStart&&!ai.isOffscForceStop){
                                sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
                                ai.isAutoStart = false;
                            }
                            if (MainActivity.isLinkStopAndRemoveStop&&ai.isRecentForceClean&&!ai.isOffscForceStop) {
                                sharedPrefs.recentPrefs.edit().remove(ai.getPackageName() + "/forceclean").commit();
                                ai.isRecentForceClean = false;
                            }
//                            sharedPrefs.muBeiPrefs.edit().remove(ai.getPackageName()).commit();
                            ed.remove(ai.getPackageName()+"/backmubei").commit();
                            ai.isBackMuBei = false;
                        }else{
                            ed.putBoolean(ai.getPackageName()+"/backstop",true).commit();
                            ai.isBackForceStop = true;
                            if (MainActivity.isLinkStopAndAuto&&!ai.isAutoStart) {
                                sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/autostart", true).commit();
                                ai.isAutoStart = true;
                            }
                            if (MainActivity.isLinkStopAndRemoveStop&&!ai.isRecentForceClean&&!ai.isRecentNotClean) {
                                sharedPrefs.recentPrefs.edit().putBoolean(ai.getPackageName() + "/forceclean", true).commit();
                                ai.isRecentForceClean = true;
                            }
//                            sharedPrefs.muBeiPrefs.edit().remove(ai.getPackageName()).commit();
                            ed.remove(ai.getPackageName()+"/backmubei").commit();
                            ai.isBackMuBei = false;
                        }
                        if(!ai.isHomeMuBei&&!ai.isBackMuBei&&!ai.isOffscMuBei){
                            BaseActivity.sendBroadAMSRemovePkg(AppConfigActivity.this,ai.getPackageName());
                        }
                        break;
                    case R.id.item_home:
                        if (ai.isHomeMuBei){
                            ed.remove(ai.getPackageName()+"/homestop").commit();
//                            sharedPrefs.muBeiPrefs.edit().remove(ai.getPackageName()).commit();
                            ed.remove(ai.getPackageName()+"/homemubei").commit();
                            ai.isHomeMuBei = false;
                        }else if(ai.isHomeIdle){
                            ed.remove(ai.getPackageName()+"/idle").commit();
                            ai.isHomeIdle = false;
                            ed.remove(ai.getPackageName()+"/homestop").commit();
                            if (MainActivity.isModuleActive()){
                                if (ai.isServiceStop){
                                    AlertUtil.showAlertMsg(AppConfigActivity.this,"检测到你已经在第一项的禁用服务中禁用了该应用的服务，所以设置墓碑模式将不会生效，如果要使用墓碑模式请在第一项的禁用服务中取消勾选。");
                                }else {
                                    ed.putBoolean(ai.getPackageName() + "/homemubei", true).commit();
                                    ai.isHomeMuBei = true;
                                    if(ai.isBackMuBei||ai.isOffscMuBei){
                                        AlertUtil.showAlertMsg(AppConfigActivity.this,"检测到你已经在设置了"+(ai.isBackMuBei?"返回时":"熄屏时")+"墓碑，选择后台墓碑后会自动取消"+(ai.isBackMuBei?"返回时":"熄屏时")+"墓碑设置。");
                                    }
                                    if(ai.isBackMuBei){
                                        ed.remove(ai.getPackageName() + "/backmubei").commit();
                                        ai.isBackMuBei = false;
                                    }
                                    if(ai.isOffscMuBei){
                                        ed.remove(ai.getPackageName() + "/offmubei").commit();
                                        ai.isOffscMuBei = false;
                                    }
                                }
                            }else{
                                showT("墓碑模式需要XP支持");
                            }
                        }else{
                            ed.putBoolean(ai.getPackageName()+"/idle",true).commit();
                            ai.isHomeIdle = true;
                        }
                        if(!ai.isHomeMuBei&&!ai.isBackMuBei&&!ai.isOffscMuBei){
                            BaseActivity.sendBroadAMSRemovePkg(AppConfigActivity.this,ai.getPackageName());
                        }
                       break;
                    case R.id.item_off:
                        if (ai.isOffscForceStop){
                            ed.remove(ai.getPackageName()+"/offstop").commit();
                            ai.isOffscForceStop = false;
                            if(MainActivity.isLinkStopAndAuto&&!ai.isRecentForceClean&&!ai.isBackForceStop){
                                sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
                                ai.isAutoStart = false;
                            }
                            if (MainActivity.isModuleActive()) {
                                if (ai.isServiceStop){
                                    AlertUtil.showAlertMsg(AppConfigActivity.this,"检测到你已经在第一项的禁用服务中禁用了该应用的服务，所以设置墓碑模式将不会生效，如果要使用墓碑模式请在第一项的禁用服务中取消勾选。");
                                }else {
                                    if (sharedPrefs.forceStopPrefs.contains(ai.getPackageName() + "/homemubei")){
                                        AlertUtil.showAlertMsg(AppConfigActivity.this,"检测到你已经设置了后台时墓碑，所以设置熄屏时墓碑将不会生效");
                                    }else{
                                        ed.putBoolean(ai.getPackageName() + "/offmubei", true).commit();
                                        ai.isOffscMuBei = true;
                                    }
                                }
                            }else{
                                showT("墓碑模式需要XP支持");
                            }
                        }else if (ai.isOffscMuBei){
                            ed.remove(ai.getPackageName()+"/offstop").commit();
                            ai.isOffscForceStop = false;
                            if(MainActivity.isLinkStopAndAuto&&ai.isAutoStart&&!ai.isBackForceStop){
                                sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
                                ai.isAutoStart = false;
                            }
                            if (MainActivity.isLinkStopAndRemoveStop&&ai.isRecentForceClean&&!ai.isBackForceStop) {
                                sharedPrefs.recentPrefs.edit().remove(ai.getPackageName() + "/forceclean").commit();
                                ai.isRecentForceClean = false;
                            }
//                            sharedPrefs.muBeiPrefs.edit().remove(ai.getPackageName()).commit();
                            ed.remove(ai.getPackageName()+"/offmubei").commit();
                            ai.isOffscMuBei = false;

                        }else{
                            ed.putBoolean(ai.getPackageName()+"/offstop",true).commit();
                            ai.isOffscForceStop = true;
//                            sharedPrefs.muBeiPrefs.edit().remove(ai.getPackageName()).commit();
                            ed.remove(ai.getPackageName()+"/offmubei").commit();
                            ai.isOffscMuBei = false;
                            if (MainActivity.isLinkStopAndAuto&&!ai.isAutoStart) {
                                sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/autostart", true).commit();
                                ai.isAutoStart = true;
                            }
                            if (MainActivity.isLinkStopAndRemoveStop&&!ai.isRecentForceClean&&!ai.isRecentNotClean) {
                                sharedPrefs.recentPrefs.edit().putBoolean(ai.getPackageName() + "/forceclean", true).commit();
                                ai.isRecentForceClean = true;
                            }
                        }
                        if(!ai.isHomeMuBei&&!ai.isBackMuBei&&!ai.isOffscMuBei){
                            BaseActivity.sendBroadAMSRemovePkg(AppConfigActivity.this,ai.getPackageName());
                        }
                        break;
                    case R.id.item_notify:
                        ai.isNotifyNotExit = !ai.isNotifyNotExit;
                        sharedPrefs.forceStopPrefs.edit().putBoolean(ai.getPackageName() + "/notifynotexit",ai.isNotifyNotExit).commit();
                        notifyImg.setImageResource(ai.isNotifyNotExit?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                }
                backImg.setImageResource(ai.isBackMuBei?R.mipmap.icon_dead:ai.isBackForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                homeImg.setImageResource(ai.isHomeMuBei?R.mipmap.icon_dead:ai.isHomeIdle?R.mipmap.icon_idle:R.mipmap.icon_notdisable);
                offScImg.setImageResource(ai.isOffscMuBei?R.mipmap.icon_dead:ai.isOffscForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                notAutoStartImg.setImageResource(ai.isAutoStart?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                killImg.setImageResource(ai.isRecentForceClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
            }
        }
        class MyClickListener implements View.OnClickListener{
            @Override
            public void onClick(View view) {
//                isClick = true;
                BaseActivity.zhenDong(AppConfigActivity.this);
                switch (view.getId()){
                    case R.id.item_service:
                        ai.isServiceStop = !ai.isServiceStop;
                        sharedPrefs.modPrefs.edit().putBoolean(ai.getPackageName() + "/service",ai.isServiceStop).commit();
                        serviceImg.setImageResource(ai.isServiceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_broad:
                        ai.isBroadStop = !ai.isBroadStop;
                        sharedPrefs.modPrefs.edit().putBoolean(ai.getPackageName() + "/broad",ai.isBroadStop).commit();
                        broadImg.setImageResource(ai.isBroadStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_wakelock:
                        ai.isWakelockStop = !ai.isWakelockStop;
                        sharedPrefs.modPrefs.edit().putBoolean(ai.getPackageName() + "/wakelock",ai.isWakelockStop).commit();
                        wakeLockImg.setImageResource(ai.isWakelockStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_alarm:
                        ai.isAlarmStop = !ai.isAlarmStop;
                        sharedPrefs.modPrefs.edit().putBoolean(ai.getPackageName() + "/alarm",ai.isAlarmStop).commit();
                        alarmImg.setImageResource(ai.isAlarmStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_keep:
                        if(ai.isRecentNotClean){
                            ai.isRecentNotClean = false;
                            sharedPrefs.recentPrefs.edit().remove(ai.getPackageName()+"/notclean").commit();
                            if (MainActivity.isLinkRecentAndNotStop&&ai.isNotStop){
                                sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName() + "/notstop").commit();
                                ai.isNotStop = false;
                            }
                        }else{
                            ai.isRecentNotClean = true;
                            sharedPrefs.recentPrefs.edit().putBoolean(ai.getPackageName()+"/notclean",ai.isRecentNotClean).commit();
                            if (MainActivity.isLinkRecentAndNotStop&&!ai.isNotStop){
                                sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/notstop", true).commit();
                                ai.isNotStop = true;
                            }
                            if (sharedPrefs.recentPrefs.contains(ai.getPackageName()+"/forceclean")){
                                ai.isRecentForceClean = false;
                                sharedPrefs.recentPrefs.edit().remove(ai.getPackageName()+"/forceclean").commit();
                            }
                        }
                        keepImg.setImageResource(ai.isRecentNotClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notStopImg.setImageResource(ai.isNotStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        killImg.setImageResource(ai.isRecentForceClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_kill:
                        if(ai.isRecentForceClean){
                            ai.isRecentForceClean = false;
                            sharedPrefs.recentPrefs.edit().remove(ai.getPackageName()+"/forceclean").commit();
                            if(MainActivity.isLinkRecentAndAuto&&!ai.isBackForceStop&&!ai.isOffscForceStop){
                                sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
                                ai.isAutoStart = false;
                            }
                        }else{
                            ai.isRecentForceClean = true;
                            sharedPrefs.recentPrefs.edit().putBoolean(ai.getPackageName()+"/forceclean",ai.isRecentForceClean).commit();
                            if (sharedPrefs.recentPrefs.contains(ai.getPackageName()+"/notclean")){
                                ai.isRecentNotClean = false;
                                sharedPrefs.recentPrefs.edit().remove(ai.getPackageName()+"/notclean").commit();
                            }
                            if(MainActivity.isLinkRecentAndAuto&&!ai.isAutoStart) {
                                sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/autostart", true).commit();
                                ai.isAutoStart = true;
                            }
                        }
                        killImg.setImageResource(ai.isRecentForceClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notAutoStartImg.setImageResource(ai.isAutoStart?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        keepImg.setImageResource(ai.isRecentNotClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_blur:
                        ai.isRecentBlur = !ai.isRecentBlur;
                        sharedPrefs.recentPrefs.edit().putBoolean(ai.getPackageName() + "/blur",ai.isRecentBlur).commit();
                        blurImg.setImageResource(ai.isRecentBlur?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_hide:
                        ai.isRecentNotShow = !ai.isRecentNotShow;
                        sharedPrefs.recentPrefs.edit().putBoolean(ai.getPackageName() + "/notshow",ai.isRecentNotShow).commit();
                        hideImg.setImageResource(ai.isRecentNotShow?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_dongjie:
                        AlertUtil.showConfirmAlertMsg(AppConfigActivity.this, "该功能需要谨慎操作，选择是否冻结？(冻结后去应用控制器内解冻)", new AlertUtil.InputCallBack() {
                            @Override
                            public void backData(String txt, int tag) {
                                if (tag == 1){
                                    ShellUtilNoBackData.execCommand("pm disable "+ai.packageName);
                                    finish();
                                }
                            }
                        });
                        break;
                    case R.id.item_uninstall:
                        AlertUtil.showConfirmAlertMsg(AppConfigActivity.this, "该功能需要谨慎操作，选择是否卸载？", new AlertUtil.InputCallBack() {
                            @Override
                            public void backData(String txt, int tag) {
                                if (tag == 1){
                                    Uri uri = Uri.fromParts("package", ai.packageName, null);
                                    Intent intentdel = new Intent(Intent.ACTION_DELETE, uri);
                                    AppConfigActivity.this.startActivity(intentdel);
                                    finish();
                                }
                            }
                        });
                        break;
                    case R.id.item_notuninstall:
                        ai.isNotUnstall = !ai.isNotUnstall;
                        sharedPrefs.pmPrefs.edit().putBoolean(ai.getPackageName() + "/notunstall",ai.isNotUnstall).commit();
                        notUninstallImg.setImageResource(ai.isNotUnstall?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_ad_one:
                    case R.id.item_ad_two:
                    case R.id.item_ad_three:
                        int mode = view.getId()==R.id.item_ad_one?1:(view.getId()==R.id.item_ad_two?2:3);
                        if (sharedPrefs.adPrefs.getInt(ai.packageName+"/ad",0)==mode){
                            sharedPrefs.adPrefs.edit().remove(ai.packageName+"/ad").commit();
                            ai.isADJump = false;
                        }else{
                            sharedPrefs.adPrefs.edit().putInt(ai.packageName+"/ad",mode).putString(ai.packageName+"/one", OpenCloseUtil.getFirstActivity(ai.packageName,AppConfigActivity.this)).commit();
                            ai.isADJump = true;
                            if(ai.packageName.equals("so.ofo.labofo")){
                                sharedPrefs.adPrefs.edit().putString(ai.packageName+"/two", "so.ofo.labofo.activities.journey.MainActivity").commit();
                            }
                        }
                        mode1Img.setImageResource((sharedPrefs.adPrefs.getInt(ai.packageName+"/ad",0)==1)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                        mode2Img.setImageResource((sharedPrefs.adPrefs.getInt(ai.packageName+"/ad",0)==2)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                        mode3Img.setImageResource((sharedPrefs.adPrefs.getInt(ai.packageName+"/ad",0)==3)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_doze_qiantai:
                        ai.isDozeOpenStop = !ai.isDozeOpenStop;
                        sharedPrefs.dozePrefs.edit().putBoolean(ai.getPackageName() + "/openstop",ai.isDozeOpenStop).commit();
                        dozeQianTaiImg.setImageResource(ai.isDozeOpenStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_doze_on:
                        ai.isDozeOnsc = !ai.isDozeOnsc;
                        sharedPrefs.dozePrefs.edit().putBoolean(ai.getPackageName() + "/onsc",ai.isDozeOnsc).commit();
                        dozeOnScImg.setImageResource(ai.isDozeOnsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_doze_off:
                        ai.isDozeOffsc = !ai.isDozeOffsc;
                        sharedPrefs.dozePrefs.edit().putBoolean(ai.getPackageName() + "/offsc",ai.isDozeOffsc).commit();
                        dozeOffScImg.setImageResource(ai.isDozeOffsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_bar_on:
                        ai.isBarColorList = !ai.isBarColorList;
                        barLockImg.setEnabled(ai.isBarColorList);
                        barLockImg.setAlpha(ai.isBarColorList?1.0f:0.6f);
                        sharedPrefs.uiBarPrefs.edit().putBoolean(ai.getPackageName() + "/colorlist",ai.isBarColorList).commit();
                        if(!ai.isBarColorList){
                            sharedPrefs.uiBarPrefs.edit().remove(ai.getPackageName() + "/locklist").commit();
                            ai.isBarLockList = false;
                        }
                        barOpenImg.setImageResource(ai.isBarColorList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        barLockImg.setImageResource(ai.isBarLockList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_bar_lock:
                        ai.isBarLockList = !ai.isBarLockList;
                        sharedPrefs.uiBarPrefs.edit().putBoolean(ai.getPackageName() + "/locklist",ai.isBarLockList).commit();
                        barLockImg.setImageResource(ai.isBarLockList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_lock:
                        ai.isLockApp = !ai.isLockApp;
                        sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/lockapp",ai.isLockApp).commit();
                        if(ai.isLockApp&&ai.isStopApp){
                            ai.isStopApp = false;
                            sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName() + "/stopapp").commit();
                        }
                        lockImg.setImageResource(ai.isLockApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notRunImg.setImageResource(ai.isStopApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_stoprun:
                        ai.isStopApp = !ai.isStopApp;
                        sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/stopapp",ai.isStopApp).commit();
                        sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName() + "/lockapp").commit();
                        sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName() + "/autostart").commit();
                        sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName() + "/notstop").commit();
                        ai.isLockApp = false;
                        ai.isAutoStart = false;
                        ai.isNotStop = false;
                        lockImg.setImageResource(ai.isLockApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notRunImg.setImageResource(ai.isStopApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notAutoStartImg.setImageResource(ai.isAutoStart?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notStopImg.setImageResource(ai.isNotStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_autostart:
                        ai.isAutoStart = !ai.isAutoStart;
                        sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/autostart",ai.isAutoStart).commit();
                        sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName() + "/stopapp").commit();
                        ai.isStopApp = false;
                        notAutoStartImg.setImageResource(ai.isAutoStart?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notRunImg.setImageResource(ai.isStopApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_notstop:
                        ai.isNotStop = !ai.isNotStop;
                        sharedPrefs.autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/notstop",ai.isNotStop).commit();
                        sharedPrefs.autoStartNetPrefs.edit().remove(ai.getPackageName() + "/stopapp").commit();
                        ai.isStopApp = false;
                        notStopImg.setImageResource(ai.isNotStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        notRunImg.setImageResource(ai.isStopApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
                        break;
                    case R.id.item_ifw_service:
                        startAct(ai,0);
                        break;
                    case R.id.item_ifw_broad:
                        startAct(ai,1);
                        break;
                    case R.id.item_ifw_activity:
                        startAct(ai,2);
                        break;

                }
            }
        };
        MyClickListener listener = new MyClickListener();
        serviceImg.setOnClickListener(listener);
        broadImg.setOnClickListener(listener);
        wakeLockImg.setOnClickListener(listener);
        alarmImg.setOnClickListener(listener);
        ForceStopListener forceStopListener = new ForceStopListener();
        backImg.setOnClickListener(forceStopListener);
        homeImg.setOnClickListener(forceStopListener);
        offScImg.setOnClickListener(forceStopListener);
        notifyImg.setOnClickListener(forceStopListener);

        keepImg.setOnClickListener(listener);
        killImg.setOnClickListener(listener);
        blurImg.setOnClickListener(listener);
        hideImg.setOnClickListener(listener);

        lockImg.setOnClickListener(listener);
        notRunImg.setOnClickListener(listener);
        notAutoStartImg.setOnClickListener(listener);
        notStopImg.setOnClickListener(listener);

        dongJieImg.setOnClickListener(listener);
        notUninstallImg.setOnClickListener(listener);
        unInstallImg.setOnClickListener(listener);

        mode1Img.setOnClickListener(listener);
        mode2Img.setOnClickListener(listener);
        mode3Img.setOnClickListener(listener);

        dozeQianTaiImg.setOnClickListener(listener);
        dozeOffScImg.setOnClickListener(listener);
        dozeOnScImg.setOnClickListener(listener);

        ifwServceImg.setOnClickListener(listener);
        ifwBroadImg.setOnClickListener(listener);
        ifwActivityImg.setOnClickListener(listener);

        barOpenImg.setOnClickListener(listener);
        barLockImg.setOnClickListener(listener);
    }
    private void startAct(AppInfo ai,int type){
        IFWFragment.ai = ai;
        Intent intent = new Intent(this, IFWCompActivity.class);
        intent.putExtra("name",ai.appName);
        intent.putExtra("pkg",ai.packageName);
        intent.putExtra("type",type);
        startActivity(intent);
    }
    public static void setWindowStatusBarColor(Activity activity, int colorResId) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = activity.getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(activity.getResources().getColor(colorResId));

//                底部导航栏
                window.setNavigationBarColor(activity.getResources().getColor(colorResId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onRestart() {
    	super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (isClick){
//            isClick = false;
            if(WatchDogService.isNeedAMSReadLoad){
                XposedUtil.reloadInfos(this,sharedPrefs.autoStartNetPrefs,sharedPrefs.modPrefs,sharedPrefs.settings,sharedPrefs.skipDialogPrefs);
                WatchDogService.isNeedAMSReadLoad= false;
                Log.i("CONTROL","更新AMS中的数据....");
            }
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent("com.click369.control.appconfigclose");
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        finish();
    }
//    public void clearAppSettings(AppInfo ai){
//        SharedPreferences.Editor modEd = sharedPrefs.modPrefs.edit();
//        modEd.remove(ai.getPackageName() + "/service");
//        modEd.remove(ai.getPackageName() + "/broad");
//        modEd.remove(ai.getPackageName() + "/wakelock");
//        modEd.remove(ai.getPackageName() + "/alarm");
//        modEd.commit();
//
//        SharedPreferences.Editor forceEd = sharedPrefs.forceStopPrefs.edit();
//        forceEd.remove(ai.getPackageName() + "/backstop");
//        forceEd.remove(ai.getPackageName() + "/backmubei");
//        forceEd.remove(ai.getPackageName() + "/offstop");
//        forceEd.remove(ai.getPackageName() + "/offmubei");
//        sharedPrefs.muBeiPrefs.edit().remove(ai.getPackageName()).commit();
//        forceEd.remove(ai.getPackageName() + "/homemubei");
//        forceEd.remove(ai.getPackageName() + "/notifynotexit");
//        forceEd.commit();
//
//        SharedPreferences.Editor appstartEd = sharedPrefs.autoStartNetPrefs.edit();
//        appstartEd.remove(ai.getPackageName() + "/autostart");
//        appstartEd.remove(ai.getPackageName() + "/stopapp");
//        appstartEd.remove(ai.getPackageName() + "/lockapp");
//        appstartEd.remove(ai.getPackageName() + "/notstop");
//        appstartEd.commit();
//
//        SharedPreferences.Editor dozeEd = sharedPrefs.dozePrefs.edit();
//        dozeEd.remove(ai.getPackageName() + "/offsc");
//        dozeEd.remove(ai.getPackageName() + "/onsc");
//        dozeEd.remove(ai.getPackageName() + "/openstop");
//        dozeEd.commit();
//
//        SharedPreferences.Editor recentEd = sharedPrefs.recentPrefs.edit();
//        recentEd.remove(ai.getPackageName() + "/notclean");
//        recentEd.remove(ai.getPackageName() + "/forceclean");
//        recentEd.remove(ai.getPackageName() + "/blur");
//        recentEd.remove(ai.getPackageName() + "/notshow");
//        recentEd.commit();
//
//        SharedPreferences.Editor barEd = sharedPrefs.uiBarPrefs.edit();
//        barEd.remove(ai.getPackageName() + "/locklist");
//        barEd.remove(ai.getPackageName() + "/colorlist");
//        barEd.commit();
//
//        sharedPrefs.pmPrefs.edit().remove(ai.getPackageName() + "/notunstall").commit();
//        sharedPrefs.adPrefs.edit().remove(ai.getPackageName() + "/ad").commit();
//    }
    /**
     * 设置视图裁剪的圆角半径
     * @param radius
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setClipViewCornerRadius(View view, final int radius){

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            //不支持5.0版本以下的系统
            return;
        }

        if(view == null) return;

        if(radius <= 0){
            return;
        }

        /*view.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom(), radius);
            }
        });*/
        view.setOutlineProvider(new ViewOutlineProvider() {

            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(),   view.getHeight(), radius);
            }
        });
        view.setClipToOutline(true);
    }
    private void getAppDetailSettingIntent() {
//        Activity activity = this;
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", ai.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", ai.getPackageName());
        }
        startActivity(localIntent);
    }
}

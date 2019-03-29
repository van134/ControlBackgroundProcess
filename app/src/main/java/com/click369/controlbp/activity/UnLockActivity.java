package com.click369.controlbp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.GCUtil;
import com.click369.controlbp.util.GetPhoto;
import com.click369.controlbp.util.MyFingerUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.view.WaveProgressView;

import net.qiujuer.genius.blur.StackBlur;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UnLockActivity extends Activity {
    private Handler h = new Handler();
    private int errorCount = 0;
    private SharedPreferences autoStartPrefs;
    private TextView msgTv,pwdTv;
    private ImageView imge;
    private LinearLayout numberLL,mumBackLL;
    private Intent mIntent;
    private String pkg,cls;
    private boolean isNotShowUI = true,isShowANIM = true,isShowKey = true;
    private SharedPreferences settingPrefs;
    private RelativeLayout mainRl;
    private GetPhoto getPhoto;
    private File imgFile = null;
    private boolean isUsePwd = false,isneedbroad = false;//,isfromresume=false;
    private String bgColor = MainActivity.THEME_COLOR;

    private WaveProgressView wpv;
    private int max_progress = 60;
    private int progress = 0;
    private boolean isAdd = true;
    private static final int FLAG_ONE = 0X0001;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(wpv==null||!wpv.isShown()){
                return;
            }
            if (progress >= wpv.getScHeight()*0.1){
                isAdd = false;
            }else if(progress<=1){
                isAdd = true;
            }
            if(isAdd){
                progress+=1;
            }else{
                progress-=1;
            }

            switch (msg.what) {
                case FLAG_ONE:

                    if(wpv!=null&&wpv.isShown()){
//                        wpv.setCurrent(progress, "");
                        wpv.setProgress(progress);
                        wpv.invalidate();
//                        Log.i("WAVE","progress  "+progress);
//                        waveView.setCurrent(progress, progress + "%");
                        sendEmptyMessageDelayed(FLAG_ONE, 40);
                    }
                    break;
            }
        }
    };
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        pkg = this.getIntent().getStringExtra("pkg");
        cls = this.getIntent().getStringExtra("class");
        isneedbroad = this.getIntent().getBooleanExtra("isneedbroad",false)&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
//        isfromresume = this.getIntent().getBooleanExtra("isfromresume",false);
        mIntent = this.getIntent().getParcelableExtra("intent");
        autoStartPrefs =  SharedPrefsUtil.getInstance(this.getApplicationContext()).autoStartNetPrefs;//SharedPrefsUtil.getPreferences(this, Common.PREFS_AUTOSTARTNAME);
        settingPrefs = SharedPrefsUtil.getInstance(this.getApplicationContext()).settings;//SharedPrefsUtil.getPreferences(this, Common.PREFS_APPSETTINGS);
        bgColor = SharedPrefsUtil.getInstance(getApplicationContext()).uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_UNLOCK_BG_COLOR,MainActivity.THEME_COLOR);
        WaveProgressView.WAVE_PAINT_COLOR = SharedPrefsUtil.getInstance(getApplicationContext()).uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_UNLOCK_ANIM_COLOR,WaveProgressView.WAVE_PAINT_COLOR);

        Log.i("CONTROL","准备解锁：pkg"+pkg+"  intent "+mIntent);
        if(pkg==null||pkg.length()==0){
            this.finish();
            return;
        }
        isNotShowUI = settingPrefs.getBoolean(Common.PREFS_APPSTART_ISSHOWUI,false);
        isShowANIM = settingPrefs.getBoolean(Common.PREFS_SETTING_LOCKISSHOWANIM,true);
        if (isNotShowUI){
            Window window = this.getWindow();
            window.setGravity(Gravity.TOP| Gravity.LEFT);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.x = 0;
            lp.y = 0;
            lp.width = 1;
            lp.height = 1;
            window.setAttributes(lp);
            setTheme(R.style.liveactivity);
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveTaskToBack(false);
                }
            },500);
        }else{
            setContentView(R.layout.activity_unlock);
            initView();
            mainRl = (RelativeLayout) findViewById(R.id.unlock_main_rl);
            msgTv = (TextView)this.findViewById(R.id.unlock_msg_tv);
            pwdTv = (TextView)this.findViewById(R.id.unlock_pwd_tv);
            numberLL = (LinearLayout) this.findViewById(R.id.unlock_number_ll);
            mumBackLL = (LinearLayout) this.findViewById(R.id.unlock_numberbackground_ll);
            imge = (ImageView) this.findViewById(R.id.unlock_numberbackground_img);


            wpv = (WaveProgressView) this.findViewById(R.id.loadingImageView);
            if(!isShowANIM){
                wpv.setVisibility(View.GONE);
                imge.setVisibility(View.VISIBLE);
            }else{
                imge.setVisibility(View.INVISIBLE);
                mumBackLL.setBackgroundColor(Color.TRANSPARENT);
                progress = 0;
                wpv.setAlpha(0.8f);
//            wpv.setWaveColor("#FFFFFF");
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                wpv.setScHeight(dm.heightPixels);
                handler.sendEmptyMessageDelayed(FLAG_ONE, 10);
            }


            isShowKey= settingPrefs.getBoolean(Common.PREFS_APPSTART_ISSHOWNUMBERLOCK,true);
            isUsePwd = settingPrefs.getBoolean(Common.PREFS_SETTING_USEPWDLOCK,false);
            numberLL.setVisibility(isShowKey?View.VISIBLE:View.GONE);
            PackageManager pm = this.getPackageManager();
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
                msgTv.setText(pm.getApplicationLabel(ai).toString()+"已锁定");
                pwdTv.setText(isShowKey?"请输入4位密码或指纹解锁":"请指纹解锁");
                if (settingPrefs.getBoolean("isfirstshowlock",true)){
                    pwdTv.append("\n\n长按空白处修改背景图片");
                }
                settingPrefs.edit().putBoolean("isfirstshowlock",false).commit();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            imgFile = new File(FileUtil.IMAGEPATH,"lock.jpg");
            getPhoto = new GetPhoto(this);
            getPhoto.setIsNeedCrop(true);
            getPhoto.setPhotofile(imgFile);
            if (imgFile!=null&&imgFile.exists()){
                Drawable d = Drawable.createFromPath(imgFile.getAbsolutePath());
                mainRl.setBackground(d);
//                wpv.setBackground(d);

            }else{
                mainRl.setBackgroundColor(Color.parseColor(bgColor));
//                wpv.setBackgroundColor(Color.parseColor(bgColor));
            }

            mainRl.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    boolean isOk = true;
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                        if(!PermissionUtils.checkPermissionAllGranted(getApplicationContext(),permissions)){
                            isOk = false;
                        }
                    }
                    if(isOk){
                        AlertUtil.showListAlert(UnLockActivity.this, "请选择", new String[]{"选择解锁背景图片","对图片进行模糊处理","清除解锁背景图片"}, new AlertUtil.InputCallBack() {
                            @Override
                            public void backData(String txt, int tag) {
                                if (tag == 0){
                                    getPhoto.photoWithGrelly();
                                }else if(tag == 1){
                                    if(imgFile.exists()){
                                        try {
                                            Bitmap bitmap = StackBlur.blurNativelyPixels(BitmapFactory.decodeFile(imgFile.getAbsolutePath()),50, false);
                                            FileOutputStream fos = new FileOutputStream(imgFile);
                                            bitmap.compress(Bitmap.CompressFormat.JPEG,95,fos);
                                            mainRl.setBackground(Drawable.createFromPath(imgFile.getAbsolutePath()));
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }else{
                                        Toast.makeText(getApplicationContext(),"还未选择图片",Toast.LENGTH_SHORT).show();
                                    }
                                }else if(tag == 2){
                                    if(imgFile.exists()){
                                        imgFile.delete();
                                    }
                                    mainRl.setBackground(null);
                                    mainRl.setBackgroundColor(Color.parseColor(bgColor));
                                }
                            }
                        });
                    }else{
                        Toast.makeText(getApplicationContext(),"没有文件读写权限",Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            });
        }
//        Toast.makeText(UnLockActivity.this,"请在验证指纹进入程序",Toast.LENGTH_LONG).show();
        if(isNotShowUI){
            h.removeCallbacks(cancelFinger);
            h.postDelayed(cancelFinger, 10000);
//            h.postDelayed(cancelFinger, isNotShowUI?10000:15000);
        }
//        AppStartService.waitUnlockApp = pkg;
        errorCount = 0;
        initFrg();
    }
    Runnable cancelFinger = new Runnable() {
        @Override
        public void run() {
            MyFingerUtil.cancel();
            UnLockActivity.this.finish();
        }
    };

    private void initFrg(){
        try {
//            Class.forName("android.hardware.fingerprint.FingerprintManager"); // 通过反射判断是否存在该类
            MyFingerUtil.callFingerPrint(UnLockActivity.this.getApplicationContext(), new MyFingerUtil.OnCallBackListenr() {
                @Override
                public void onSupportFailed() {
                    h.removeCallbacks(cancelFinger);
//                    Toast.makeText(UnLockActivity.this,"设备不支持指纹，请用密码验证",Toast.LENGTH_SHORT).show();
                    if (numberLL!=null) {
                        numberLL.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(UnLockActivity.this,"设备不支持指纹，直接进入。",Toast.LENGTH_LONG).show();
                        autoStartPrefs.edit().putBoolean(pkg+"/lockok",true).commit();

                        startact();
                    }
                }
                @Override
                public void onInsecurity() {
                    h.removeCallbacks(cancelFinger);
                    Toast.makeText(UnLockActivity.this,"当前设备未处于安全保护中，请用密码解锁(系统设置了指纹但无密码)",Toast.LENGTH_SHORT).show();
//                    startact();
                }
                @Override
                public void onEnrollFailed() {
                    h.removeCallbacks(cancelFinger);
                    if (numberLL!=null) {
                        Toast.makeText(UnLockActivity.this,"还未设置指纹，请用密码进入。",Toast.LENGTH_LONG).show();
                        numberLL.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(UnLockActivity.this,"还未设置指纹，直接进入。",Toast.LENGTH_LONG).show();
                        autoStartPrefs.edit().putBoolean(pkg+"/lockok",true).commit();
                        UnLockActivity.this.finish();
                        OpenCloseUtil.doStartApplicationWithPackageName(pkg,UnLockActivity.this);
                    }
                }
                @Override
                public void onAuthenticationStart() {
//                        Toast.makeText(service,"开始解锁",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onAuthenticationError(int errMsgId, CharSequence errString) {
//                    h.removeCallbacks(cancelFinger);
//                    Toast.makeText(UnLockActivity.this,errString,Toast.LENGTH_SHORT).show();
                    errorCount++;
                }

                @Override
                public void onAuthenticationFailed() {
                    h.removeCallbacks(cancelFinger);
                    errorCount++;
                    if (pwdTv!=null){
                        pwdTv.setTextColor(Color.RED);
                        pwdTv.setText("解锁失败"+errorCount+"次");
                    }else{
                        Toast.makeText(UnLockActivity.this,"解锁失败"+errorCount+"次",Toast.LENGTH_SHORT).show();
                    }
                    if (errorCount>=3){
                        MyFingerUtil.cancel();
//                        AppStartService.waitUnlockApp = "";
                        UnLockActivity.this.finish();
                    }
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {

                }
                @Override
//                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                    h.removeCallbacks(cancelFinger);

//                    AppStartService.waitUnlockApp = "";
//                    Toast.makeText(UnLockActivity.this,"解锁成功",Toast.LENGTH_SHORT).show();
                    if (pwdTv!=null) {
                        pwdTv.setText("解锁成功");
                    }
                    autoStartPrefs.edit().putBoolean(pkg+"/lockok",true).commit();
                    errorCount = 0;

                    startact();

//                        autoStartPrefs.edit().remove(pkg+"/lockok").commit();
                }
            });
        } catch (Exception e) {
            Toast.makeText(UnLockActivity.this,"设备不支持指纹，请用密码验证",Toast.LENGTH_SHORT).show();
            if (numberLL!=null) {
                numberLL.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(UnLockActivity.this,"设备不支持指纹，直接进入。",Toast.LENGTH_LONG).show();
                autoStartPrefs.edit().putBoolean(pkg+"/lockok",true).commit();

                startact();
            }
        }
    }

    private void startact(){
        try {
            if(isneedbroad){
                Intent intent = new Intent("com.click369.lock");
                intent.putExtra("islockok",true);
                intent.putExtra("pkg",pkg);
                sendBroadcast(intent);
            }else{
//                if(isfromresume){
//                   return;
//                }
                if(mIntent!=null){
                    if("com.android.settings".equals(mIntent.getPackage())){
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }else{
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        UnLockActivity.this.startActivity(mIntent);
                    }
                }else{
                    if("com.android.settings".equals(pkg)){
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }else {
                        OpenCloseUtil.doStartApplicationWithPackageName(pkg, cls, UnLockActivity.this);
                    }
                }
            }
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    UnLockActivity.this.finish();
                }
            },500);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(!isNotShowUI) {
            MyFingerUtil.cancel();
        }
        handler.removeMessages(FLAG_ONE);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(!isNotShowUI){
            initFrg();
        }
        handler.sendEmptyMessage(FLAG_ONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (isNotShowUI){
//            Intent broad = new Intent("com.click369.control.lockappbackground");
//            broad.putExtra("pkg", pkg);
//            broad.putExtra("class", cls);
//            broad.putExtra("intent",mIntent);
//            this.sendBroadcast(broad);
//            this.finish();
//            return;
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveTaskToBack(false);
                }
            },500);
        }
        if (autoStartPrefs.getBoolean(pkg+"/lockok",false)){
            this.finish();
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File file = getPhoto.onActivityResult(requestCode,resultCode,data);
        if(file!=null&&file.exists()){
            mainRl.setBackground(Drawable.createFromPath(file.getAbsolutePath()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        if(pkg==null||pkg.length()==0){
            this.finish();
            return;
        }
    }

    @Override
    public void onBackPressed() {
//        moveTaskToBack(false);
//        if(AppLoaderUtil.allAppStateInfos!=null&&AppLoaderUtil.allAppStateInfos.containsKey(pkg)){
//            AppLoaderUtil.allAppStateInfos.get(pkg).isPressKeyHome = true;
//            AppLoaderUtil.allAppStateInfos.get(pkg).isPressKeyBack = false;
//        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);// 注意
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);

        Intent intent1 = new Intent("com.click369.control.unlockback");
        intent1.putExtra("pkg",pkg);
        sendBroadcast(intent1);

        h.removeCallbacks(cancelFinger);
        MyFingerUtil.cancel();
        if(isneedbroad){
            Intent intent11 = new Intent("com.click369.lock");
            intent11.putExtra("islockok",false);
            intent11.putExtra("pkg",pkg);
            sendBroadcast(intent11);
        }
        UnLockActivity.this.finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyFingerUtil.cancel();
        pkg = "";
        mIntent = null;
        cls = "";
        GCUtil.releaseBitMap(mainRl);
        GCUtil.releaseBitMap(wpv);
        setContentView(R.layout.view_null);
        System.gc();
    }

    StringBuilder passWord = new StringBuilder();
    Button bt0,bt1,bt2,bt3,bt4,bt5,bt6,bt7,bt8,bt9;
    int wrongTime = 0;
    private void initView(){
        bt0 = (Button)this.findViewById(R.id.bt0);
        bt1 = (Button)this.findViewById(R.id.bt1);
        bt2 = (Button)this.findViewById(R.id.bt2);
        bt3 = (Button)this.findViewById(R.id.bt3);
        bt4 = (Button)this.findViewById(R.id.bt4);
        bt5 = (Button)this.findViewById(R.id.bt5);
        bt6 = (Button)this.findViewById(R.id.bt6);
        bt7 = (Button)this.findViewById(R.id.bt7);
        bt8 = (Button)this.findViewById(R.id.bt8);
        bt9 = (Button)this.findViewById(R.id.bt9);
//        Button btr = (Button)mFloatLayout.findViewById(R.id.btr);
        BtClick btClick = new BtClick();
        bt0.setOnClickListener(btClick);
        bt1.setOnClickListener(btClick);
        bt2.setOnClickListener(btClick);
        bt3.setOnClickListener(btClick);
        bt4.setOnClickListener(btClick);
        bt5.setOnClickListener(btClick);
        bt6.setOnClickListener(btClick);
        bt7.setOnClickListener(btClick);
        bt8.setOnClickListener(btClick);
        bt9.setOnClickListener(btClick);
//        btr.setOnClickListener(btClick);
    }
    private void resetBtBg()
    {
        bt0.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt1.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt2.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt3.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt4.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt5.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt6.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt7.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt8.setBackgroundResource(R.mipmap.home_btn_bg_s);
        bt9.setBackgroundResource(R.mipmap.home_btn_bg_s);
        passWord.delete(0,passWord.length());
    }

    class BtClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            BaseActivity.zhenDong(UnLockActivity.this);
//            vib.vibrate(20);
            String title = ((Button)v).getText().toString();
            passWord.append(title);
            if(passWord.length()==5){
                passWord.delete(0,4);
            }
            pwdTv.setText(passWord);
            pwdTv.setTextColor(Color.WHITE);
            if(passWord.length()==4){
//                Date d = new Date( System.currentTimeMillis()-1000*60*1);

                Date d = new Date( System.currentTimeMillis()-1000*60);
                SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
                String mpass = sdf.format(d);
                if(isUsePwd){
                    mpass = settingPrefs.getString(Common.PREFS_SETTING_APPPWD,"");
                    if(mpass.length()!=4){
                        msgTv.setText("密码还未设置，请到设置中设置解锁密码");
                    }
                }
                if(mpass.equals(passWord.toString())){
//                    unlockScreen(LockService.this);

//                    AppStartService.waitUnlockApp = "";
//                    Toast.makeText(UnLockActivity.this,"解锁成功",Toast.LENGTH_SHORT).show();
                    pwdTv.setText("解锁成功");
                    autoStartPrefs.edit().putBoolean(pkg+"/lockok",true).commit();
                    errorCount = 0;
                    wrongTime=0;
                    h.removeCallbacks(cancelFinger);
                    MyFingerUtil.cancel();

                    startact();
                }else{
//                    Date d1 = new Date( System.currentTimeMillis());
//                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    FileUtil.writeLog(sdf1.format(d1)+"输入错误,错误密码为："+passWord.toString());
                    wrongTime++;
                    pwdTv.setTextColor(Color.RED);
//                    resetBtBg();
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            vib.vibrate(wrongTime*1000);
//                        }
//                    },300);
                }
            }
        }
    }

    public void cleanClick(View v){
        passWord = new StringBuilder();
        pwdTv.setText(isShowKey?"请输入4位密码或指纹解锁":"请指纹解锁");
        BaseActivity.zhenDong(UnLockActivity.this);
    }
    public void delClick(View v){
        if (passWord.length()>0){
            passWord.deleteCharAt(passWord.length()-1);
            pwdTv.setText(passWord);
            if(passWord.length()==0){
                pwdTv.setText(isShowKey?"请输入4位密码或指纹解锁":"请指纹解锁");
            }
        }else{
            pwdTv.setText(isShowKey?"请输入4位密码或指纹解锁":"请指纹解锁");
        }
        BaseActivity.zhenDong(UnLockActivity.this);
    }

}

package com.click369.controlbp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.GetPhoto;
import com.click369.controlbp.util.MyFingerUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.SharedPrefsUtil;

import net.qiujuer.genius.blur.StackBlur;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SetPWDActivity extends BaseActivity {
    private Handler h = new Handler();
    private TextView msgTv,pwdTv;
    private LinearLayout numberLL;
    private SharedPreferences settingPrefs;
    private RelativeLayout mainRl;
    private GetPhoto getPhoto;
    private File imgFile = null;
    boolean isSet = false;//是设置还是验证
    boolean isClosePWD = false;
    String bgColor = MainActivity.THEME_COLOR;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         settingPrefs =sharedPrefs.settings;// SharedPrefsUtil.getPreferences(this, Common.PREFS_APPSETTINGS);
//            requestWindowFeature(Window.FEATURE_NO_TITLE);
            setContentView(R.layout.activity_unlock);
        bgColor = SharedPrefsUtil.getInstance(getApplicationContext()).uiBarPrefs.getString(Common.PREFS_SETTING_UI_THEME_UNLOCK_BG_COLOR,MainActivity.THEME_COLOR);

        initView();
            mainRl = (RelativeLayout) findViewById(R.id.unlock_main_rl);
            msgTv = (TextView)this.findViewById(R.id.unlock_msg_tv);
            pwdTv = (TextView)this.findViewById(R.id.unlock_pwd_tv);
            numberLL = (LinearLayout) this.findViewById(R.id.unlock_number_ll);
            numberLL.setVisibility(View.VISIBLE);
            isSet = settingPrefs.getString(Common.PREFS_SETTING_APPPWD,"").length()!=4;
        isClosePWD = this.getIntent().hasExtra("isclose");
            msgTv.setText(isSet?"请设置密码（4位）":isClosePWD?"请输入密码才可关闭":"请输入旧密码");
            pwdTv.setText("");
            imgFile = new File(FileUtil.IMAGEPATH,"lock.jpg");
            getPhoto = new GetPhoto(this);
            getPhoto.setIsNeedCrop(true);
            getPhoto.setPhotofile(imgFile);
            if (imgFile!=null&&imgFile.exists()){
                mainRl.setBackground(Drawable.createFromPath(imgFile.getAbsolutePath()));
            }else{
                mainRl.setBackgroundColor(Color.parseColor(bgColor));
            }
//            mainRl.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View view) {
//                    boolean isOk = true;
//                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
//                        String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//                        if(!PermissionUtils.checkPermissionAllGranted(getApplicationContext(),permissions)){
//                            isOk = false;
//                        }
//                    }
//                    if(isOk){
//                        settingPrefs.edit().putBoolean("isfirstshowlock",false).commit();
//                        AlertUtil.showListAlert(SetPWDActivity.this, "请选择", new String[]{"选择解锁背景图片","对图片进行模糊处理","清除解锁背景图片"}, new AlertUtil.InputCallBack() {
//                            @Override
//                            public void backData(String txt, int tag) {
//                                if (tag == 0){
//                                    getPhoto.photoWithGrelly();
//                                }else if(tag == 1){
//                                    if(imgFile.exists()){
//                                        try {
//                                            Bitmap bitmap = StackBlur.blurNativelyPixels(BitmapFactory.decodeFile(imgFile.getAbsolutePath()),50, false);
//                                            FileOutputStream fos = new FileOutputStream(imgFile);
//                                            bitmap.compress(Bitmap.CompressFormat.JPEG,95,fos);
//                                            mainRl.setBackground(Drawable.createFromPath(imgFile.getAbsolutePath()));
//                                        }catch (Exception e){
//                                            e.printStackTrace();
//                                        }
//                                    }else{
//                                        Toast.makeText(getApplicationContext(),"还未选择图片",Toast.LENGTH_SHORT).show();
//                                    }
//                                }else if(tag == 2){
//                                    if(imgFile.exists()){
//                                        imgFile.delete();
//                                    }
//                                    mainRl.setBackground(null);
//                                    mainRl.setBackgroundColor(Color.parseColor("#ff1cadfb"));
//                                }
//                            }
//                        });
//                    }else{
//                        Toast.makeText(getApplicationContext(),"没有文件读写权限",Toast.LENGTH_LONG).show();
//                    }
//                    return true;
//                }
//            });
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        File file = getPhoto.onActivityResult(requestCode,resultCode,data);
        if(file!=null&&file.exists()){
            mainRl.setBackground(Drawable.createFromPath(file.getAbsolutePath()));
        }
//        AlertUtil.showConfirmAlertMsg(this, "是否对图片进行模糊处理？", new AlertUtil.InputCallBack() {
//            @Override
//            public void backData(String txt, int tag) {
//                if(tag == 1){
//                    try {
//                        Bitmap bitmap = StackBlur.blurNativelyPixels(BitmapFactory.decodeFile(imgFile.getAbsolutePath()),50, false);
//                        FileOutputStream fos = new FileOutputStream(imgFile);
//                        bitmap.compress(Bitmap.CompressFormat.JPEG,95,fos);
//                        mainRl.setBackground(Drawable.createFromPath(imgFile.getAbsolutePath()));
//                    }catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
    }

    @Override
    public void onBackPressed() {
        SetPWDActivity.this.finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    String pwd1 = "";
    class BtClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            BaseActivity.zhenDong(SetPWDActivity.this);
            String title = ((Button)v).getText().toString();
            passWord.append(title);
            if(passWord.length()==5){
                passWord.delete(0,4);
            }
            pwdTv.setText(passWord);
            pwdTv.setTextColor(Color.WHITE);
            if(passWord.length()==4){
                if(isSet&&!isClosePWD){
                    if(pwd1.length()>0){
                        if(pwd1.equals(passWord.toString())){
                            settingPrefs.edit().putString(Common.PREFS_SETTING_APPPWD,pwd1).commit();
                            msgTv.setText("密码设置成功");
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },1000);
                        }else{
                            passWord = new StringBuilder();
                            msgTv.setText("和第一次次输入的密码不一致，请重新输入");
                        }
                    }else{
                        pwd1 = passWord.toString();
                        msgTv.setText("请再输入一次确保无误");
                    }
                }else{
                    String s = settingPrefs.getString(Common.PREFS_SETTING_APPPWD,"");
                    if(s.equals(passWord.toString())){
                        msgTv.setText(isClosePWD?"验证成功":"验证成功，请设置新密码");
                        pwdTv.setText("");
                        wrongTime=0;
                        isSet = true;
                        passWord = new StringBuilder();
                        if(isClosePWD){
                            LockAppSwitchView.isCloseOk = true;
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            },600);
                        }
                    }else{
                        wrongTime++;
                        pwdTv.setTextColor(Color.RED);
                        if(wrongTime>=3){
                            finish();
                        }
                    }
                }
            }
        }
    }

    public void cleanClick(View v){
        passWord = new StringBuilder();
        msgTv.setText(isSet?"请设置密码（4位）":"请输入旧的密码");
        BaseActivity.zhenDong(SetPWDActivity.this);
    }
    public void delClick(View v){
        if (passWord.length()>0){
            passWord.deleteCharAt(passWord.length()-1);
            pwdTv.setText(passWord);
            if(passWord.length()==0){
                msgTv.setText(isSet?"请设置密码（4位）":"请输入旧的密码");
                pwdTv.setText("");
            }
        }else{
            msgTv.setText(isSet?"请设置密码（4位）":"请输入旧的密码");
            pwdTv.setText("");
        }
        BaseActivity.zhenDong(SetPWDActivity.this);
    }
}

package com.click369.controlbp.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.GetPhoto;
import com.click369.controlbp.util.MyFingerUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtils;

import net.qiujuer.genius.blur.StackBlur;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowDialogActivity extends Activity {
    private String pkg = "";
    private boolean isAlreadyDelay = false;
    private Handler handler = new Handler();
    private int time = 30;
    private int delayTime = 10;
    private String name = "";
    private Handler h = new Handler();
    private int errorCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Material_Dialog);
        super.onCreate(savedInstanceState);
        pkg = this.getIntent().getStringExtra("pkg");
//        isAlreadyDelay = this.getIntent().getBooleanExtra("isAlreadyDelay",false);
        initdialog();
        if (WatchDogService.isSetTimeStopByZW) {
            initFrg();
        }
            try {
                name = PackageUtil.getAppNameByPkg(this,pkg);
                if(alertDialog==null){
                    initdialog();
                }
                alertDialog.setMessage(name+"正在运行，即将在"+time+"秒内强制关闭，"+(WatchDogService.isSetTimeStopByZW?"进行指纹验证后取消强退":WatchDogService.isSetTimeStopByPWD?"进行密码验证后取消强退":"点击取消按钮取消强退")+"，否则将会自动强制关闭\n\n");
                handler.post(showAlert);
            }catch (RuntimeException arg1){
                arg1.printStackTrace();
            }
//        }
    }
    AlertDialog.Builder builder = null;
    AlertDialog alertDialog = null;
    private void showDelay(){
        if (WatchDogService.isSetTimeStopByZW||WatchDogService.isSetTimeStopByPWD){
            alertDialog.setTitle(WatchDogService.isSetTimeStopByZW?"请验证指纹":"请输入密码");
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setVisibility(View.GONE);
            isAlreadyDelay = true;
            Toast.makeText(ShowDialogActivity.this,WatchDogService.isSetTimeStopByZW?"请验证指纹进行延迟":"请输入密码进行延迟",Toast.LENGTH_LONG).show();
            handler.removeCallbacks(showTime);
            handler.post(showAlert);
            if (WatchDogService.isSetTimeStopByPWD){
                et.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        et.setFocusableInTouchMode(true);
                        et.requestFocus();
                        et.setSelection(et.getText().length());
                        InputMethodManager inputManager = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.showSoftInput(et, 0);

                    }
                },400);
            }
        }else{
            cancelClose(true);
        }
    }
    private void setButton(){
        builder.setNegativeButton("延迟30分钟", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delayTime = 30;
                showDelay();
            }
        });
        builder.setPositiveButton("延迟10分钟", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delayTime = 10;
                showDelay();
            }
        });
        builder.setNeutralButton("延迟60分钟", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                delayTime = 60;
                showDelay();
            }
        });
    }
    EditText et = null;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void initdialog(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle("警告");

        if (WatchDogService.isSetTimeStopByZW&&!isAlreadyDelay) {
            setButton();
        }else if(WatchDogService.isSetTimeStopByPWD){
            et = new EditText(this);
            et.setHint("请输入四位密码");
            et.setText("");
            et.setSelection(et.getText().length());
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
            et.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
            et.setBackgroundColor(Color.argb(20,0,0,0));
            et.setPadding(0,30,0,30);
            et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.postDelayed(new Runnable() {
                @Override
                public void run() {
                    et.setFocusableInTouchMode(true);
                    et.requestFocus();
                    et.setSelection(et.getText().length());
                    InputMethodManager inputManager = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(et, 0);
                }
            },200);
            et.postDelayed(new Runnable() {
                @Override
                public void run() {
                    et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            },400);
            builder.setView(et);
            if(!isAlreadyDelay){
                setButton();
            }
            final SharedPreferences settingPrefs = SharedPrefsUtil.getPreferences(this, Common.PREFS_APPSETTINGS);
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String pwd = et.getText().toString().trim();
                    if(pwd.length()==4){
                        String mpass = settingPrefs.getString(Common.PREFS_SETTING_APPPWD,"");
                        boolean isUsePwd = settingPrefs.getBoolean(Common.PREFS_SETTING_USEPWDLOCK,false);
                        Date d = new Date( System.currentTimeMillis()-1000*60);
                        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
                        if(isUsePwd){
                            if(mpass.length() != 4){
                                mpass = sdf.format(d);
                            }
                        }else{
                            mpass = sdf.format(d);
                        }
                        if (pwd.equals(mpass)){
                            Toast.makeText(ShowDialogActivity.this,"验证成功"+(isAlreadyDelay?"延迟成功":"取消强退"),Toast.LENGTH_LONG).show();
                            h.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    cancelClose(isAlreadyDelay);
                                }
                            },500);
                        }else{
                            et.setText("");
                            Toast.makeText(ShowDialogActivity.this,"密码错误",Toast.LENGTH_LONG).show();
                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                }
            });

//            builder.setPositiveButton("验证密码", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                }
//            });
        }else if(!WatchDogService.isSetTimeStopByZW&&!WatchDogService.isSetTimeStopByPWD){
            setButton();
            builder.setPositiveButton("取消强退", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    cancelClose(false);
                }
            });
        }
        dialogCreate();
    }

    private void dialogCreate(){
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Toast.makeText(ShowDialogActivity.this,"程序将在设定的时间强制退出",Toast.LENGTH_SHORT).show();
                handler.removeCallbacks(showAlert);
                handler.removeCallbacks(showTime);
                if (WatchDogService.isSetTimeStopByZW) {
                    MyFingerUtil.cancel();
                }
                finish();
            }
        });
    }

    private void confrim(){
        Intent intent = new Intent("com.click369.control.canceltimestopapp");
        intent.putExtra("pkg",pkg);
        intent.putExtra("isdelay",false);
        sendBroadcast(intent);
        handler.removeCallbacks(showTime);
        XposedStopApp.stopApk(pkg,ShowDialogActivity.this);
        if (!WatchDogService.setTimeStopkeys.contains(pkg)) {
            WatchDogService.setTimeStopApp.remove(pkg);
        }
        WatchDogService.stopAppName.remove(pkg);
//        alertDialog.cancel();
        finish();
    }

    private void cancelClose(boolean isDelay){
        Intent intent = new Intent("com.click369.control.canceltimestopapp");
        intent.putExtra("pkg",pkg);
        intent.putExtra("isdelay",isDelay);
        sendBroadcast(intent);
        handler.removeCallbacks(showTime);
        if (isDelay){
            setAlarmWithCode("com.click369.control.settimestopapp",pkg,delayTime*60,pkg.hashCode());
        }else{
            if (!WatchDogService.setTimeStopkeys.contains(pkg)) {
                WatchDogService.setTimeStopApp.remove(pkg);
            }
        }
        finish();
    }

    Runnable showAlert = new Runnable() {
        @Override
        public void run() {
            try {
                alertDialog.show();
                handler.postDelayed(showTime,1000);
            }catch (RuntimeException e){
                e.printStackTrace();
            }

        }
    };
    Runnable showTime = new Runnable() {
        @Override
        public void run() {
        try {
            time--;
            if (time>0){
                String mm = isAlreadyDelay?"进行延迟":"取消强退";
                alertDialog.setMessage(name+"正在运行，即将在"+time+"秒内强制关闭，"+(WatchDogService.isSetTimeStopByZW?"进行指纹验证后"+mm:WatchDogService.isSetTimeStopByPWD?"进行密码验证后"+mm:"点击取消按钮"+mm)+"，否则将会自动强制关闭\n\n");
                handler.postDelayed(showTime,1000);
            }else{
                finish();
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        }
    };

//    public void setAlarm(String action,int time){
//        Intent intent1 = new Intent(action);
//        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent1,0);
//        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
//        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+time*1000,pi);
//    }
    public void setAlarmWithCode(String action,String pkg,int time,int code){
        Intent intent1 = new Intent(action);
        intent1.putExtra("pkg",pkg);
        PendingIntent pi = PendingIntent.getBroadcast(this,code,intent1,0);
        AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+time*1000,pi);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(showAlert);
        handler.removeCallbacks(showTime);
        if (WatchDogService.isSetTimeStopByZW) {
            MyFingerUtil.cancel();
        }
        finish();
    }


    private void initFrg(){
        try {
//            Class.forName("android.hardware.fingerprint.FingerprintManager"); // 通过反射判断是否存在该类
            MyFingerUtil.callFingerPrint(ShowDialogActivity.this.getApplicationContext(), new MyFingerUtil.OnCallBackListenr() {
                @Override
                public void onSupportFailed() {
                    Toast.makeText(ShowDialogActivity.this,"设备不支持指纹",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onInsecurity() {
                    Toast.makeText(ShowDialogActivity.this,"当前设备未处于安全保护中，请用密码解锁(系统设置了指纹但无密码)",Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onEnrollFailed() {
                    Toast.makeText(ShowDialogActivity.this,"还未设置指纹。",Toast.LENGTH_LONG).show();
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
                    errorCount++;
                    Toast.makeText(ShowDialogActivity.this,"验证失败"+errorCount+"次，三次错误后无法再验证",Toast.LENGTH_SHORT).show();
                    if (errorCount>=3){
                        MyFingerUtil.cancel();
                        ShowDialogActivity.this.finish();
                    }
                }

                @Override
                public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {

                }
                @Override
//                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                    Toast.makeText(ShowDialogActivity.this,"验证成功，取消强退",Toast.LENGTH_SHORT).show();
                    cancelClose(isAlreadyDelay);
                }
            });
        } catch (Exception e) {
            Toast.makeText(ShowDialogActivity.this,"设备不支持指纹，请用密码验证",Toast.LENGTH_SHORT).show();
        }
    }

}

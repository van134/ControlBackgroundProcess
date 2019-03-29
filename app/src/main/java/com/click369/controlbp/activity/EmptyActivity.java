package com.click369.controlbp.activity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.ShellUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/5/27.
 */
public class EmptyActivity extends AppCompatActivity {
    private Handler h = new Handler();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.setGravity(Gravity.TOP| Gravity.LEFT);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = 0;
        lp.width = 1;
        lp.height = 1;

        window.setAttributes(lp);

        Log.i("CONTROL","start empty");
        if(this.getIntent().hasExtra("data")&&this.getIntent().getStringExtra("data").contains("启动服务")){
            Log.i("CONTROL","start service  启动服务");
            if (!WatchDogService.isKillRun){
                Intent intent = new Intent(this,WatchDogService.class);
                startService(intent);
            }
            this.finish();
        }else if(this.getIntent().hasExtra("data")&&this.getIntent().getStringExtra("data").contains("锁屏")){
            this.finish();
        }else if(this.getIntent().hasExtra("data")&&this.getIntent().getStringExtra("data").contains("异常")){
            AlertUtil.showAlertMsgBack(this, "应用控制器出现异常(请截屏发给开发者)", this.getIntent().getStringExtra("content"), new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                   finish();
                }
            });
        }else if(this.getIntent().hasExtra("data")&&this.getIntent().getStringExtra("data").contains("清理")){
            clearMomery();
        }else if(this.getIntent().hasExtra("data")&&this.getIntent().getStringExtra("data").contains("重启")){
            String titles[] = {"重启系统界面","重启到RECOVER","重启手机","关机","自杀(仅前台界面)"};
            AlertUtil.showListNotCancelAlert(this, "重启菜单", titles, new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if (tag ==0){
                        Intent intent1 = new Intent("com.click369.control.rebootsystemui");
                        sendBroadcast(intent1);
                    }else if (tag ==1){
                        ShellUtils.execCommand("reboot recovery",true);
                    }else if (tag ==2){
                        ShellUtils.execCommand("reboot",true);
                    }else if (tag ==3){
                        ShellUtils.execCommand("reboot -p",true);
                    }else if (tag == 4){
                        Intent intent1 = new Intent("com.click369.control.ams.killself");
                        sendBroadcast(intent1);
                    }
                }
            });
        }else if(this.getIntent().hasExtra("name")&&this.getIntent().hasExtra("pkg")){
            String name = this.getIntent().getStringExtra("name");
            String pkg = this.getIntent().getStringExtra("pkg");
            this.pkg = pkg;
            pd = ProgressDialog.show(this, null, "正在解冻并启动"+name+"，请稍等...", true, false);
            Intent intent = new Intent("com.click369.control.pms.enablepkg");
            intent.putExtra("pkg",pkg);
            sendBroadcast(intent);
            ShellUtilNoBackData.execCommand("pm enable "+pkg);
//            if(!NewWatchDogService.isOpenNewDogService){
//                OpenCloseUtil.closeOpenAccessibilitySettingsOn(this,true);
//            }
            AppStateInfo asi =AppLoaderUtil.allAppStateInfos.containsKey(pkg)?AppLoaderUtil.allAppStateInfos.get(pkg):new AppStateInfo();
            asi.isOpenFromIceRome = true;
            AppLoaderUtil.allAppStateInfos.put(pkg,asi);
            h.postDelayed(r,100);
            cout = 0;
        }


////        getPower(null);
////        this.finish();
//        thisAct = this;
//
//        Log.i("DOZE","empty start");
    }
    String pkg = null;
    int cout = 0;
    Runnable r=  new Runnable() {
        @Override
        public void run() {
            try {
                OpenCloseUtil.doStartApplicationWithPackageName(pkg,EmptyActivity.this);
                if(pd!=null&&pd.isShowing()){
                    try {
                        pd.dismiss();
                    }catch (Exception e){
                    }
                }
                EmptyActivity.this.finish();
            }catch (Exception e){
                cout++;
                if(cout>20){
                    cout = 0;
                    if(pd!=null&&pd.isShowing()){
                        try {
                            pd.dismiss();
                        }catch (Exception e1){
                        }
                    }
                    EmptyActivity.this.finish();
                    Toast.makeText(EmptyActivity.this,"该应用无法启动",Toast.LENGTH_LONG).show();
                }else{
                    h.postDelayed(r,300);
                }
            }
        }
    };
//    public void getPower(View v){
//        DevicePolicyManager mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
//        ComponentName devAdminReceiver = new ComponentName(this, DeviceManagerBC.class);
//        boolean admin = mDPM.isAdminActive(devAdminReceiver);
//        if (!admin) {
//            Intent i = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);//激活系统设备管理器
//            i.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, devAdminReceiver);//注册系统组件
//            startActivity(i);
//            this.finish();
//        }
//    }

    ProgressDialog pd= null;
    public void clearMomery(){
        pd= ProgressDialog.show(this, "", "开始整理内存，请稍后...", false, true);
        final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        final ActivityManager.MemoryInfo memoryInfo1 = new ActivityManager.MemoryInfo();
        final ActivityManager.MemoryInfo memoryInfo2 = new ActivityManager.MemoryInfo();
        new Thread(){
            public void run(){
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityManager.getMemoryInfo(memoryInfo1);
                            pd.show();
//                        showT("开始整理内存，准备清理!");
                        }
                    });
//                    String psRes = ShellUtilBackStop.execCommand("ps",true);
//                    ArrayList<AppInfo> apps = AppInfo.readArrays(getApplicationContext());
//                    if(apps==null||apps.size()==0){
//                        apps = new ArrayList<AppInfo>();
//                        apps.addAll(AppLoaderUtil.getAppInfos(EmptyActivity.this,2));
//                    }
                    List<String> lists = new ArrayList<String>();
                    lists.add("am kill-all");
                    lists.add("sync");
                    lists.add("echo 3 > /proc/sys/vm/drop_caches");
//                    for(AppInfo ai:apps) {
//                        if (psRes.contains(ai.getPackageName())) {
//                            if(ai.getPackageName().startsWith("com.click369")){
//                                continue;
//                            }
//                            lists.add("am force-stop "+ai.getPackageName());
//                        }
//                    }
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pd.setMessage("整理完毕，开始清理...");
                                pd.show();
                            } catch (Exception e) {
                                EmptyActivity.this.finish();
                            }
                        }
                    });
                    Thread.sleep(1000);
//                    ShellUtils.execCommand(lists,true,true);
                    ShellUtilNoBackData.execCommand(lists);
                    Thread.sleep(2500);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            activityManager.getMemoryInfo(memoryInfo2);
                            long bit = memoryInfo2.availMem - memoryInfo1.availMem;
                            try {
                                if (pd!=null&&pd.isShowing()){pd.cancel();} } catch (Exception e) {
                            }
                            Toast.makeText(EmptyActivity.this,"内存已释放完毕,共释放"+(bit/(1024*1024))+"M", Toast.LENGTH_LONG).show();
                            EmptyActivity.this.finish();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    EmptyActivity.this.finish();
                }
            }
        }.start();

    }
}

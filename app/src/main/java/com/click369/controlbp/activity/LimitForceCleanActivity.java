package com.click369.controlbp.activity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.service.XposedUtil;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShellUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/5/27.
 */
public class LimitForceCleanActivity extends BaseActivity {
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
        clearMomery();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    ProgressDialog pd= null;
    public void clearMomery(){
        pd= ProgressDialog.show(this, "", "开始整理内存，请稍后...", false, true);
        final ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        final ActivityManager.MemoryInfo memoryInfo1 = new ActivityManager.MemoryInfo();
        final ActivityManager.MemoryInfo memoryInfo2 = new ActivityManager.MemoryInfo();
        new Thread(){
            public void run(){
                try {
                    LimitForceCleanActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pd.show();
                        }
                    });
                    activityManager.getMemoryInfo(memoryInfo1);
                    String psRes = ShellUtilBackStop.execCommand("ps",true);
                    ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
                    apps.addAll(appLoaderUtil.allAppInfos);
                    List<String> lists = new ArrayList<String>();
                    lists.add("sync");
                    lists.add("echo 3 > /proc/sys/vm/drop_caches");
                    lists.add("am kill-all");
                    for(AppInfo ai:apps) {
                        if (psRes.contains(ai.getPackageName())) {
                            if(ai.getPackageName().startsWith("com.click369")||
                                    ai.getPackageName().contains("navbarapps")){
                                continue;
                            }

                            lists.add("am force-stop "+ai.getPackageName());
                            WatchDogService.sendRemoveRecent(ai.getPackageName(),LimitForceCleanActivity.this);
                            if(ai.isNotStop){
                                XposedStopApp.stopApk(ai.getPackageName(),LimitForceCleanActivity.this);
                            }
                        }
                    }
                    Thread.sleep(1000);
                    LimitForceCleanActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            try {
                                pd.setMessage("整理完毕，开始清理...");
                                pd.show();
                            } catch (Exception e) {
                            }
                        }
                    });
//                    ShellUtils.execCommand(lists,true,true);
                    ShellUtilNoBackData.execCommand(lists);
                    Thread.sleep(2500);
                    LimitForceCleanActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            activityManager.getMemoryInfo(memoryInfo2);
                            long bit = memoryInfo2.availMem - memoryInfo1.availMem;
                            try {
                                if (pd != null && pd.isShowing()) {
                                    pd.cancel();
                                }
                            } catch (Exception e) {
                            }
                            finish();
                            Toast.makeText(LimitForceCleanActivity.this, "内存已释放完毕,共释放" + (bit / (1024 * 1024)) + "M", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

package com.click369.controlbp.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.util.OpenCloseUtil;

public class RunningActivity extends BaseActivity {
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.setGravity(Gravity.TOP| Gravity.LEFT);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = 0;
        lp.width = 1;
        lp.height = 1;
        window.setAttributes(lp);
        final Handler h = new Handler();
        if (!NewWatchDogService.isOpenNewDogService){
            new Thread(){
                @Override
                public void run() {
                    OpenCloseUtil.closeOpenAccessibilitySettingsOn(RunningActivity.this,true);
                }
            }.start();
            final Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (NewWatchDogService.isOpenNewDogService){
                        try{
                            NewWatchDogService.run_from = 0;
                            start("com.android.settings.DevelopmentSettings" );
                        }catch (Exception e){
                            NewWatchDogService.run_from = 1;
                            start("com.android.settings.Settings$SystemDashboardActivity" );
                        }
                    }else{
                        count++;
                        if (count<20) {
                            h.postDelayed(this, 200);
                        }
                    }
                }
            };
            h.postDelayed(r,1000);
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    RunningActivity.this.finish();
                }
            },1500);
//            moveTaskToBack(false);
        }else{
            try{
                NewWatchDogService.run_from = 0;
                start("com.android.settings.DevelopmentSettings" );
            }catch (Exception e){
                NewWatchDogService.run_from = 1;
                start("com.android.settings.Settings$SystemDashboardActivity" );
            }
        }
        //SavePerfrence.savePerfrence(this, SavePerfrence.PERF_SETTING_FILE, SavePerfrence.PERF_RUNNING_STATE, "ok");
    }

    private void start(String name){
        NewWatchDogService.run_state = 1;
        Intent mIntent = new Intent();//"com.android.settings.DevelopmentSettings"  "com.android.settings.Settings$SystemDashboardActivity"
        ComponentName comp = new ComponentName("com.android.settings",name);
        mIntent.setComponent(comp);
        mIntent.setAction("android.intent.action.VIEW");
        startActivity(mIntent);
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
    }

    @Override
    protected void onRestart() {
    	this.finish();
    	super.onRestart();
    }

}


package com.click369.controlbp.service;
import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.click369.controlbp.activity.AppConfigActivity;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.io.IOException;
import java.lang.reflect.Method;


/**
 * Created by asus on 2017/5/20.
 */
@TargetApi(24)
public class AppConfigFromStatusService extends TileService{
    android.os.Handler handler = new Handler();
    @Override
    public void onTileAdded() {
        super.onTileAdded();
       // updateTile();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        updateTile();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }


    @Override
    public void onClick() {
        super.onClick();
        Log.i("CONTROL","当前打开的应用  "+WatchDogService.nowPkgName);

        collapseStatusBar(this);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                Intent intent = new Intent(AppConfigFromStatusService.this, AppConfigActivity.class);
                intent.putExtra("pkg",WatchDogService.nowPkgName);
        intent.putExtra("from","nowapp");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
//            }
//        },500);

//        this.getQsTile().updateTile();
    }
    public static void collapseStatusBar(Context context) {
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }
    @Override
    public void onStartListening() {
        super.onStartListening();
//        updateTile();
    }
//    private void updateTile(){
//        SharedPreferences sp =  SharedPrefsUtil.getPreferences(this, Common.PREFS_DOZELIST);
//        SharedPreferences setting =  SharedPrefsUtil.getPreferences(this, Common.PREFS_APPSETTINGS);
//        if(sp.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false)&&setting.getBoolean(Common.ALLSWITCH_SEVEN,true)){
//            this.getQsTile().setState(Tile.STATE_ACTIVE);
//            this.getQsTile().setLabel("打盹打开");
//        }else{
//            this.getQsTile().setState(Tile.STATE_INACTIVE);
//            this.getQsTile().setLabel("打盹关闭");
//        }
//        this.getQsTile().updateTile();
//    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }
}

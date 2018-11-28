
package com.click369.controlbp.service;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.SharedPrefsUtil;

/**
 * Created by asus on 2017/5/20.
 */
@TargetApi(24)
public class MyTileService extends TileService{
    @Override
    public void onTileAdded() {
        super.onTileAdded();
       // updateTile();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateTile();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }


    @Override
    public void onClick() {
        super.onClick();
        SharedPreferences sp =  SharedPrefsUtil.getInstance(this).dozePrefs;//SharedPrefsUtil.getPreferences(this, Common.PREFS_DOZELIST);
        SharedPreferences setting =  SharedPrefsUtil.getInstance(this).settings;//SharedPrefsUtil.getPreferences(this, Common.PREFS_APPSETTINGS);
        if(!setting.getBoolean(Common.ALLSWITCH_SEVEN,true)){
            return;
        }
        if(sp.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false)){
            MyDozeService.allSwitch = false;
            sp.edit().putBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false).commit();
            Intent intent = new Intent(MyDozeService.STATE_CLOSE);
            this.sendBroadcast(intent);
            this.getQsTile().setLabel("打盹关闭");
            this.getQsTile().setState(Tile.STATE_INACTIVE);
//            SavePerfrence.savePerfrence(MyTileService.this,SavePerfrence.PERF_SETTING_FILE,SavePerfrence.PERF_ISSELFSTOP,"true");
        }else{
            MyDozeService.allSwitch = true;
            sp.edit().putBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,true).commit();
            Intent intent = new Intent(MyDozeService.STATE_CLOSE);
            this.sendBroadcast(intent);
            this.getQsTile().setLabel("打盹打开");
            this.getQsTile().setState(Tile.STATE_ACTIVE);

//            SavePerfrence.savePerfrence(MyTileService.this,SavePerfrence.PERF_SETTING_FILE,SavePerfrence.PERF_ISSELFSTOP,"false");
        }
        this.getQsTile().updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }
    private void updateTile(){
        SharedPreferences sp =  SharedPrefsUtil.getPreferences(this, Common.PREFS_DOZELIST);
        SharedPreferences setting =  SharedPrefsUtil.getPreferences(this, Common.PREFS_APPSETTINGS);
        if(sp.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false)&&setting.getBoolean(Common.ALLSWITCH_SEVEN,true)){
            this.getQsTile().setState(Tile.STATE_ACTIVE);
            this.getQsTile().setLabel("打盹打开");
        }else{
            this.getQsTile().setState(Tile.STATE_INACTIVE);
            this.getQsTile().setLabel("打盹关闭");
        }
        this.getQsTile().updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }
}

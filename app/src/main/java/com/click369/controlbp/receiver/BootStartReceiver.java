package com.click369.controlbp.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.ColorNavBarService;
import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

/**
 * Created by asus on 2017/5/19.
 */
public class BootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (WatchDogService.isKillRun){
            return;
        }
//        SharedPrefsUtil sp = SharedPrefsUtil.getInstance(context.getApplicationContext());
//        XposedUtil.reloadInfos(context,sp.autoStartNetPrefs,sp.modPrefs,sp.settings,sp.skipDialogPrefs,sp.uiBarPrefs);
        String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)||
                "com.click369.control.test".equals(action)){

            if (!WatchDogService.isKillRun) {
                Log.i("CONTROL","启动.................."+action);
                if (!PackageUtil.isEnableCompent(context,WatchDogService.class)){
                    PackageManager packageManager = context.getPackageManager();
                    ComponentName componentName = new ComponentName(context, WatchDogService.class);
                    packageManager.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
                }
                Intent intent1 = new Intent(context, WatchDogService.class);
                context.startService(intent1);
                WatchDogService.isKillRun  = true;
            }
        }
    }
}

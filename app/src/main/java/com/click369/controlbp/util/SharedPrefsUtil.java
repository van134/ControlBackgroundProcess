package com.click369.controlbp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.XposedStopApp;

import java.io.File;

/**
 * Created by asus on 2017/11/4.
 */
public class SharedPrefsUtil {
    public SharedPreferences
            modPrefs,
            wakeLockPrefs,
            alarmPrefs,
            forceStopPrefs,
            muBeiPrefs,
            settings,
            ifwCountPrefs,
            uiBarPrefs,
            autoStartNetPrefs,
            recentPrefs,
            dozePrefs,
//            whiteListPrefs,
            adPrefs,
            skipDialogPrefs,
            pmPrefs,
            tvPrefs,
            cpuPrefs;

    private static SharedPrefsUtil instance;
    private Context context;
    public static SharedPrefsUtil getInstance(Context context){
        if(instance==null){
            instance = new SharedPrefsUtil(context);
            //            muBeiPrefsFile = new File(getFilesDir() + "/../shared_prefs/" + Common.IPREFS_MUBEILIST + ".xml");
        }
        return instance;
    }
    private SharedPrefsUtil(Context context){
        this.context = context;
        ifwCountPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_APPIFWCOUNT);// getApplicationContext().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
        modPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_SETTINGNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
        wakeLockPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_WAKELOCKNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
        alarmPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_ALARMNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
        forceStopPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
        muBeiPrefs = SharedPrefsUtil.getPreferences(context,Common.IPREFS_MUBEILIST);
        autoStartNetPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_AUTOSTARTNAME);// getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        uiBarPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_UIBARLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        settings = SharedPrefsUtil.getPreferences(context,Common.PREFS_APPSETTINGS);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        recentPrefs = SharedPrefsUtil.getPreferences(context,Common.IPREFS_RECENTLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        dozePrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_DOZELIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        whiteListPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_DOZELIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        adPrefs = SharedPrefsUtil.getPreferences(context,Common.IPREFS_ADLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        skipDialogPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_SKIPDIALOG);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        pmPrefs = SharedPrefsUtil.getPreferences(context,Common.IPREFS_PMLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        tvPrefs = SharedPrefsUtil.getPreferences(context,Common.IPREFS_TVLIST);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        cpuPrefs = SharedPrefsUtil.getPreferences(context,Common.PREFS_SETCPU);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
    }
    public static SharedPreferences getPreferences(Context ctx, String prefName) {
        SharedPreferences prefs = ctx.getSharedPreferences(prefName, Context.MODE_WORLD_READABLE);
//        final File prefsFile = new File(ctx.getFilesDir() + "/../shared_prefs/" + prefName + ".xml");
//        Log.i("CONTROL","prefsFile exists"+prefsFile.exists());
//        boolean isOk  = prefsFile.setReadable(true, false);
//        Log.i("CONTROL","prefsFile isOk"+isOk);
//        prefsFile.setReasable(true, false);
        return prefs;
    }
//    public static void changeQx(SharedPreferences sp){
//    }

    public void clearAppSettings(AppInfo ai){
        SharedPreferences.Editor modEd = modPrefs.edit();
        modEd.remove(ai.getPackageName() + "/service");
        modEd.remove(ai.getPackageName() + "/broad");
        modEd.remove(ai.getPackageName() + "/wakelock");
        modEd.remove(ai.getPackageName() + "/alarm");
        modEd.commit();

        SharedPreferences.Editor forceEd = forceStopPrefs.edit();
        forceEd.remove(ai.getPackageName() + "/backstop");
        forceEd.remove(ai.getPackageName() + "/backmubei");
        forceEd.remove(ai.getPackageName() + "/offstop");
        forceEd.remove(ai.getPackageName() + "/offmubei");
        muBeiPrefs.edit().remove(ai.getPackageName()).commit();
        forceEd.remove(ai.getPackageName() + "/homemubei");
        forceEd.remove(ai.getPackageName() + "/notifynotexit");
        forceEd.commit();

        SharedPreferences.Editor appstartEd = autoStartNetPrefs.edit();
        appstartEd.remove(ai.getPackageName() + "/autostart");
        appstartEd.remove(ai.getPackageName() + "/stopapp");
        appstartEd.remove(ai.getPackageName() + "/lockapp");
        appstartEd.remove(ai.getPackageName() + "/notstop");
        appstartEd.commit();

        SharedPreferences.Editor dozeEd = dozePrefs.edit();
        dozeEd.remove(ai.getPackageName() + "/offsc");
        dozeEd.remove(ai.getPackageName() + "/onsc");
        dozeEd.remove(ai.getPackageName() + "/openstop");
        dozeEd.commit();

        SharedPreferences.Editor recentEd = recentPrefs.edit();
        recentEd.remove(ai.getPackageName() + "/notclean");
        recentEd.remove(ai.getPackageName() + "/forceclean");
        recentEd.remove(ai.getPackageName() + "/blur");
        recentEd.remove(ai.getPackageName() + "/notshow");
        recentEd.commit();

        SharedPreferences.Editor barEd = uiBarPrefs.edit();
        barEd.remove(ai.getPackageName() + "/locklist");
        barEd.remove(ai.getPackageName() + "/colorlist");
        barEd.commit();

        pmPrefs.edit().remove(ai.getPackageName() + "/notunstall").commit();
        adPrefs.edit().remove(ai.getPackageName() + "/ad").commit();

        ai.isRunning = false;
        XposedStopApp.stopApk(ai.packageName,context);
    }
}

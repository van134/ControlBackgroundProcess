package com.click369.controlbp.receiver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.SharedPrefsUtil;


/**
 * Created by asus on 2017/5/19.
 */
public class AddAppReceiver extends BroadcastReceiver {
    public static String removePkg = "";
    public static Handler h = new Handler();
    public Context cxt;
    @Override
    public void onReceive(final Context context, final Intent intent) {
        String action = intent.getAction();
        this.cxt = context;
        if (!WatchDogService.isKillRun) {
            appChange(action, intent, context);
            h.removeCallbacks(r);
            h.postDelayed(r,500);

            String addPkg =  intent.getDataString().substring(8);
            if(WatchDogService.notStops.contains(addPkg)&&Intent.ACTION_PACKAGE_REPLACED.equals(action)){
                Intent intent1 = new Intent("com.click369.control.ams.changepersistent");
                intent1.putExtra("persistent",false);
                intent1.putExtra("pkg",addPkg);
                context.sendBroadcast(intent1);
            }
            Log.i("CONTROL","AddAppReceiver1  "+action);
        }
    }
    Runnable r = new Runnable() {
        @Override
        public void run() {
            if (cxt!=null) {
                //通知后台服务刷新应用列表
                Intent intent1 = new Intent("com.click369.control.loadapplist");
                cxt.sendBroadcast(intent1);
            }
        }
    };
    public static void appChange(String action,Intent intent,Context context){
        if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
            String addPkg =  intent.getDataString().substring(8);
            SharedPreferences settings =  SharedPrefsUtil.getPreferences(context,Common.PREFS_APPSETTINGS);//context.getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
            if(addPkg.equals(removePkg)){
//                settings.edit().putBoolean(Common.PREFS_NAME_APPCHANGE,false).commit();
//                settings.edit().putBoolean(Common.PREFS_NAME_IFWCHANGE,false).commit();
                removePkg = "";
            }else{
                removePkg = "";
                if (ContainsKeyWord.isContainsPkg(addPkg)){
                    return;
                }

                settings.edit().putBoolean(Common.PREFS_NAME_APPCHANGE,true).commit();
                settings.edit().putBoolean(Common.PREFS_NAME_IFWCHANGE,true).commit();
                if(settings.getBoolean(Common.PREFS_SETTING_BACKAPPAUTOADD,false)){
                    SharedPreferences forceSet = SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);//context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                    forceSet.edit().putBoolean(addPkg+"/backstop",true).commit();
                }
                if(settings.getBoolean(Common.PREFS_SETTING_OFFAPPAUTOADD,false)){
                    SharedPreferences forceSet =  SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);//context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                    forceSet.edit().putBoolean(addPkg+"/offstop",true).commit();
                }
                if(settings.getBoolean(Common.PREFS_SETTING_AUTOSTARTAPPAUTOADD,false)){
                    SharedPreferences autoSet =SharedPrefsUtil.getPreferences(context,Common.PREFS_AUTOSTARTNAME);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                    autoSet.edit().putBoolean(addPkg+"/autostart",true).commit();
                }
                if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDREMOVERECENTEXIT,false)){
                    SharedPreferences recentSet =SharedPrefsUtil.getPreferences(context,Common.IPREFS_RECENTLIST);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                    recentSet.edit().putBoolean(addPkg+"/forceclean",true).commit();
                }
                if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDNOTIFY,false)){
                    SharedPreferences forceSet =SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                    forceSet.edit().putBoolean(addPkg+"/notifynotexit",true).commit();
                }
                if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDBLUR,false)){
                    SharedPreferences recentSet =SharedPrefsUtil.getPreferences(context,Common.IPREFS_RECENTLIST);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                    recentSet.edit().putBoolean(addPkg+"/blur",true).commit();
                }
//                Log.i("CONTROL","AddApp BAR1  "+settings.getBoolean(Common.ALLSWITCH_EIGHT,true)+addPkg);
                if(settings.getBoolean(Common.ALLSWITCH_EIGHT,true)){
                    SharedPreferences barSet =SharedPrefsUtil.getPreferences(context,Common.PREFS_UIBARLIST);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
//                    Log.i("CONTROL","AddApp BAR2  "+(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDCOLORBAR,false)&&(barSet.getBoolean(Common.PREFS_SETTING_UI_TOPBAR,false)||barSet.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR,false))));
                    if (settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDCOLORBAR,false)&&(barSet.getBoolean(Common.PREFS_SETTING_UI_TOPBAR,false)||barSet.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR,false))){
                        barSet.edit().putBoolean(addPkg + "/colorlist", true).commit();
                        barSet.edit().putBoolean(addPkg + "/locklist", true).commit();
                    }
                }
                if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPHOMEMUBEI,false)){
                    SharedPreferences foceStopPrefs =SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                    foceStopPrefs.edit().putBoolean(addPkg+"/homemubei",true).commit();
                }else  {
                    if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPBACKMUBEI,false)){
                        SharedPreferences foceStopPrefs =SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                        foceStopPrefs.edit().putBoolean(addPkg+"/backmubei",true).commit();
                    }
                    if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPOFFMUBEI,false)){
                        SharedPreferences foceStopPrefs =SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                        foceStopPrefs.edit().putBoolean(addPkg+"/offmubei",true).commit();
                    }
                    if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPHOMEIDLE,false)){
                        SharedPreferences foceStopPrefs =SharedPrefsUtil.getPreferences(context,Common.PREFS_FORCESTOPNAME);// context.getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
                        foceStopPrefs.edit().putBoolean(addPkg+"/idle",true).commit();
                    }
                }
                Intent intent1 = new Intent(context,WatchDogService.class);
                context.startService(intent1);
            }
            Intent intent1 = new Intent("com.click369.control.addapp");
            intent1.putExtra("pkg",addPkg);
            context.sendBroadcast(intent1);
        }else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
            removePkg = intent.getDataString().substring(8);
            SharedPreferences settings = SharedPrefsUtil.getPreferences(context,Common.PREFS_APPSETTINGS);
            settings.edit().putBoolean(Common.PREFS_NAME_APPCHANGE,true).commit();
            settings.edit().putBoolean(Common.PREFS_NAME_IFWCHANGE,true).commit();

            Intent intent1 = new Intent("com.click369.control.removeapp");
            intent1.putExtra("pkg",removePkg);
            context.sendBroadcast(intent1);
        }
    }
}

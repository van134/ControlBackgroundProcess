package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.click369.controlbp.fragment.DozeFragment;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.DozeUtil;
import com.click369.controlbp.util.DozeWhiteListUtil;
import com.click369.controlbp.util.Notify;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtilDoze;
import com.click369.controlbp.util.ShellUtils;
import com.click369.controlbp.util.TimeUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by asus on 2017/10/25.
 */
public class MyDozeService {
    public static final String STATE_OFF = "com.click369.offscreen.control.doze.off";
    public static final String STATE_ON = "com.click369.offscreen.control.doze.on";
    public static final String STATE_CLOSE = "com.click369.offscreen.control.doze.close";
    public static final String STATE_CHECKON = "com.click369.offscreen.control.doze.checkon";
    public static final ArrayList<String> logs = new ArrayList<String>();
    private Set<String> onWhiteList = new HashSet<String>();
    private Set<String> offWhiteList = new HashSet<String>();
    private  WatchDogService service;
    private MyDozeReceiver mdr;
//    private BettryReceiver br;
    private PowerManager pm;
    private boolean isDozeOpen;
    public static boolean lastDozeOpenScreenIsOn = false;
    public static boolean lastDozeClaseScreenIsOn = false;
    public static long lastDozeOpenTime = 0;
    public static long lastDozeCloseTime = 0;
    public static int scOnDozeTime = 0;
    public static int scOffDozeTime = 0;
    public static int scOffDozeDelayTime = 0;
    public static  int stopCount = 0;
    public static boolean issconDoze = false;
    public static boolean isscoffDoze = false;
    public static boolean isNotify = false;
    public static boolean allSwitch = true;
    public static boolean nightNotStop = false;
    private boolean isSelfStop = false;
    public static boolean isRoot = false;
    private Handler handler;
    private int openDozeBattery = 0;//battery = 0,
    public SharedPreferences dozePrefs;
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public MyDozeService(WatchDogService service){
        this.service = service;
        dozePrefs = SharedPrefsUtil.getInstance(service).dozePrefs;// SharedPrefsUtil.getPreferences(service,Common.PREFS_DOZELIST);//service.getSharedPreferences(Common.PREFS_DOZEWHITELIST,Context.MODE_WORLD_READABLE);
        IntentFilter ifliter = new IntentFilter();
        ifliter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        ifliter.addAction(STATE_OFF);
        ifliter.addAction(STATE_ON);
        ifliter.addAction(STATE_CLOSE);
        ifliter.addAction(STATE_CHECKON);
        mdr = new MyDozeReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
//        br = new BettryReceiver();
        service.registerReceiver(mdr, ifliter);
//        service.registerReceiver(br, intentFilter);
        handler = new Handler();
        pm = (PowerManager) (service.getApplicationContext().getSystemService(Context.POWER_SERVICE));
        ShellUtilDoze.execCommand("dumpsys deviceidle whitelist +com.click369.controlbp");
        isDozeOpen = DozeUtil.isDozeOpen(pm);
        lastDozeOpenTime = dozePrefs.getLong(Common.PREFS_SETTING_DOZE_LASTOPENTIME,0);
        lastDozeCloseTime = dozePrefs.getLong(Common.PREFS_SETTING_DOZE_LASTCLOSETIME,0);
        scOnDozeTime = dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCONTIME, DozeFragment.MINTIME+120);
        scOffDozeTime = dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFTIME, DozeFragment.MINTIME+240);
        scOffDozeDelayTime = dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFDELAYTIME, 5);
        issconDoze = dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCON, false);
        isscoffDoze = dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCOFF, false);
        isNotify = dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_NOTIFY, true);
        allSwitch = dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH, false);
        nightNotStop = dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_NIGHTNOTSTOP, false);
        DozeUtil.sendBroadCast(service);
        if(allSwitch){
            if (isDozeOpen) {
                if (issconDoze && pm.isInteractive()) {
                    setAlarm(STATE_OFF, scOnDozeTime);
                } else if (isscoffDoze && !pm.isInteractive()) {
                    setAlarm(STATE_OFF, scOffDozeTime);
                }else{
                    DozeUtil.closeDoze();
                }
            }else{
                if ((issconDoze && pm.isInteractive())|| (isscoffDoze && !pm.isInteractive())){
                    setAlarm(STATE_ON, 1);
                }
            }
            if(isNotify){
                Notify.sendNotify(service,isDozeOpen?1:0,false);
            }
        }else{
            if (isDozeOpen) {
                DozeUtil.closeDoze();
            }
        }
    }
//    class BettryReceiver extends BroadcastReceiver{
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if(Intent.ACTION_BATTERY_CHANGED.equals(action)){
//                int current = intent.getExtras().getInt("level");// 获得当前电量
//                int total = intent.getExtras().getInt("scale");// 获得总电量
//                battery = current * 100 / total;
//            }
//        }
//    }
    class  MyDozeReceiver extends BroadcastReceiver {
        @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(STATE_ON.equals(action)){
                if (intent.hasExtra("data")&&onWhiteList.size()>0){
                    DozeWhiteListUtil.putWhiteList(onWhiteList);
                }
                if ((!issconDoze&&pm.isInteractive())||(!isscoffDoze&&!pm.isInteractive())||!allSwitch){
                    cancaleAlarm();
                }else{
                    isSelfStop = false;
                    if(isNotify) {
                        Notify.sendNotify(service,DozeUtil.isDozeOpen(pm)?1:0, false);
                    }
                    DozeUtil.openDoze();
                    setAlarm(STATE_CHECKON,2);
                    setAlarm(STATE_OFF,pm.isInteractive()?scOnDozeTime:scOffDozeTime);
                }
            }else if(STATE_OFF.equals(action)){
                if (intent.hasExtra("data")&&onWhiteList.size()>0){
                    DozeWhiteListUtil.removeWhiteList(onWhiteList);
                }
                isSelfStop = true;
                if ((!issconDoze&&pm.isInteractive())||(!isscoffDoze&&!pm.isInteractive())||!allSwitch){
                    cancaleAlarm();
                }else{
                    setAlarm(STATE_ON,20);
                }
                DozeUtil.closeDoze();
            }else if(STATE_CHECKON.equals(action)){
                if (!DozeUtil.isDozeOpen(pm)){
                    ShellUtilDoze.close();
                    StringBuilder sb = new StringBuilder();
                    sb.append(TimeUtil.changeMils2String(System.currentTimeMillis(),"HH:mm:ss"));
                    isRoot = ShellUtilBackStop.execCommand("ps",true)!=null;
                    if (!isRoot){
                        isRoot = ShellUtils.checkRootPermission();
                    }
                    if (isRoot){
                        sb.append("检测到打开启动失败，一小时后尝试进入（原因见提示第5条）");
                        setAlarm(STATE_ON,60*60);
                    }else{
                        sb.append("检测到打开启动失败,没有赋予ROOT权限。");
                        cancaleAlarm();
                    }
                    logs.add(0,sb.toString());
                    if (pm.isInteractive()) {
                        DozeUtil.sendBroadCast(service);
                    }
                }
            }else if(STATE_CLOSE.equals(action)){
//                boolean isClose = intent.getBooleanExtra("close",false);
                if (!allSwitch){
//                    if (DozeUtil.isDozeOpen(pm)) {
                        clearWhiteList();
                        isSelfStop = true;
                        cancaleAlarm();
                    if(DozeUtil.isDozeOpen(pm)) {
                        DozeUtil.closeDoze();
                    }
                    Notify.cancelNotify(service);
//                    }
                }else{
//                    Log.i("CONTROL","0000000000000000000000000000000000000000000000000000000"+pm.isInteractive());
                    pm = (PowerManager) (service.getApplicationContext().getSystemService(Context.POWER_SERVICE));
                    if ((issconDoze && pm.isInteractive())|| (isscoffDoze && !pm.isInteractive())){
//                        Log.i("CONTROL","11111111111111111111111111111111111111111111111111111");
                        isSelfStop = false;
                        if(DozeUtil.isDozeOpen(pm)) {
                            DozeUtil.closeDoze();
                        }
                        if(isNotify) {
                            Notify.sendNotify(service,DozeUtil.isDozeOpen(pm)?1:0, false);
                        }
                        new Thread(){
                            @Override
                            public void run() {
                                try {
                                    DozeWhiteListUtil.putWhiteList(pm.isInteractive()?onWhiteList:offWhiteList);
                                    Thread.sleep(500);
                                    DozeUtil.openDoze();
                                    setAlarm(STATE_CHECKON,2);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                        setAlarm(STATE_OFF,pm.isInteractive()?scOnDozeTime:scOffDozeTime);
                    }
                }
            }else if("android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action)){
                isDozeOpen = DozeUtil.isDozeOpen(pm);
                if(isNotify){
                    if (!allSwitch&&!dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false)){

                    }else{
                        Notify.sendNotify(service,isDozeOpen?1:0,false);
                    }
                }
                Log.i("CONTROL","DOZE状态改变"+isDozeOpen);
                if(isDozeOpen){
                    cancaleCheckAlarm();
                    openDozeBattery = service.batteryPer;
                    lastDozeOpenScreenIsOn = pm.isInteractive();
                    lastDozeOpenTime = System.currentTimeMillis();
                    dozePrefs.edit().putLong(Common.PREFS_SETTING_DOZE_LASTOPENTIME,lastDozeOpenTime).commit();
                }else{
                    if(lastDozeOpenTime>0){
                        if(logs.size()>500){
                            logs.clear();
                        }
                        lastDozeClaseScreenIsOn = pm.isInteractive();
                        lastDozeCloseTime = System.currentTimeMillis();
                        StringBuilder sb = new StringBuilder();
                        sb.append(TimeUtil.changeMils2String(lastDozeOpenTime,"HH:mm:ss"));
                        sb.append(lastDozeOpenScreenIsOn?"亮":"熄");
                        sb.append("--");
                        sb.append(TimeUtil.changeMils2String(lastDozeCloseTime,"HH:mm:ss"));
                        sb.append(lastDozeClaseScreenIsOn?"亮":"熄");
                        sb.append(" 打盹时长");
                        sb.append(TimeUtil.changeMils2StringZero(lastDozeCloseTime-lastDozeOpenTime,"HH:mm:ss"));
                        sb.append(" 耗电");
                        sb.append((openDozeBattery-service.batteryPer)>0?(openDozeBattery-service.batteryPer):0).append("%");
                        sb.append(" 剩余");
                        sb.append(service.batteryPer).append("%");
                        logs.add(0,sb.toString());
//                        Log.i("CONTROL","battery  "+battery);
                    }
                    dozePrefs.edit().putLong(Common.PREFS_SETTING_DOZE_LASTCLOSETIME,lastDozeCloseTime).commit();
                    if(MainActivity.isModuleActive()&&!isSelfStop&&(isscoffDoze||issconDoze)&&!allSwitch){
                        if(pm.isInteractive()&&issconDoze){
                            if(stopCount<30){
                                stopCount++;
                                setAlarm(STATE_ON,30*stopCount);
                            }
                        }else if(!pm.isInteractive()&&isscoffDoze){
                            if(stopCount<30){
                                stopCount++;
                                setAlarm(STATE_ON,30*stopCount);
                            }
                        }
                    }else{
                        stopCount = 0;
                    }
                }
                if (pm.isInteractive()) {
                    DozeUtil.sendBroadCast(service);
                }
            }
        }
    }

    public void screenOff(){
        if (!allSwitch||(!isscoffDoze&&!issconDoze)){
            return;
        }
        if(onWhiteList.size()>0){
            DozeWhiteListUtil.removeWhiteList(onWhiteList);
        }
        if(isscoffDoze){
            if (nightNotStop){
                int hour = Integer.parseInt(TimeUtil.changeMils2String(System.currentTimeMillis(),"H"));
                if(hour>=22||hour<=5){
                    scOffDozeTime = 3600;
                }else{
                    scOffDozeTime = dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFTIME, DozeFragment.MINTIME+240);
                }
            }else{
                scOffDozeTime = dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFTIME, DozeFragment.MINTIME+240);
            }
            if(offWhiteList.size()>0){
                DozeWhiteListUtil.putWhiteList(offWhiteList);
            }
            if (DozeUtil.isDozeOpen(pm)){
                isSelfStop = true;
                DozeUtil.closeDoze();
            }
            setAlarm(STATE_ON,scOffDozeDelayTime);
        }else{
            isSelfStop = true;
            DozeUtil.closeDoze();
            cancaleAlarm();
        }
    }
    public void screenOn(){
        if (!allSwitch||(!isscoffDoze&&!issconDoze)){
            return;
        }
        if(offWhiteList.size()>0){
            DozeWhiteListUtil.removeWhiteList(offWhiteList);
        }
        if(issconDoze){
            if(onWhiteList.size()>0){
                DozeWhiteListUtil.putWhiteList(onWhiteList);
            }
            if (DozeUtil.isDozeOpen(pm)){
                isSelfStop = true;
                DozeUtil.closeDoze();
            }
            setAlarm(STATE_ON,2);
        }else{
            isSelfStop = true;
            DozeUtil.closeDoze();
            cancaleAlarm();
        }

    }

    public void clearWhiteList(){
        if(onWhiteList.size()>0){
            DozeWhiteListUtil.removeWhiteList(onWhiteList);
        }
        if(offWhiteList.size()>0){
            DozeWhiteListUtil.removeWhiteList(offWhiteList);
        }
    }

    public void loadWhilteList(){
        Map<String,Boolean> all = (Map<String,Boolean>)dozePrefs.getAll();
        if(all.size()>0){
            onWhiteList = DozeWhiteListUtil.getWhiteList(all.keySet(),1);
            offWhiteList = DozeWhiteListUtil.getWhiteList(all.keySet(),0);
        }
    }

    private void setAlarm(String action,int time){
        if(STATE_OFF.equals(action)||STATE_ON.equals(action)){
            cancaleAlarm();
        }
        Intent intent = new Intent(action);
        PendingIntent pi = PendingIntent.getBroadcast(service,0,intent,0);
        AlarmManager am = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+time*1000,pi);
    }
    private void cancaleAlarm(){
        Intent intent = new Intent(STATE_OFF);
        PendingIntent sender = PendingIntent.getBroadcast(service, 0, intent, 0);
        if (sender != null){
            AlarmManager am = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);
        }
        Intent intent1 = new Intent(STATE_ON);
        PendingIntent sender1 = PendingIntent.getBroadcast(service, 0, intent1, 0);
        if (sender1 != null){
            AlarmManager am = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender1);
        }
    }
    private void cancaleCheckAlarm(){
        Intent intent = new Intent(STATE_CHECKON);
        PendingIntent sender = PendingIntent.getBroadcast(service, 0, intent, 0);
        if (sender != null){
            AlarmManager am = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
            am.cancel(sender);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public void checkOpenApp(String pkg, boolean open){
        if(!dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH, false)){
            return;
        }
        if(dozePrefs.getBoolean(pkg+"/openstop",false)){
            if(open){//关闭DOZE
                allSwitch = false;
                isSelfStop = true;
                cancaleAlarm();
                DozeUtil.closeDoze();
            }else{//关闭启动DOZE
                allSwitch = true;
                if ((!issconDoze&&pm.isInteractive())||
                        (!isscoffDoze&&!pm.isInteractive())||
                        !allSwitch){
                    cancaleAlarm();
                }else{
                    isSelfStop = false;
                    DozeUtil.openDoze();
                    setAlarm(STATE_OFF,pm.isInteractive()?scOnDozeTime:scOffDozeTime);
                }
            }
        }else if(!allSwitch){
            allSwitch = true;
            if ((!issconDoze&&pm.isInteractive())||(!isscoffDoze&&!pm.isInteractive())){
                cancaleAlarm();
            }else{
                isSelfStop = false;
                DozeUtil.openDoze();
                setAlarm(STATE_OFF,pm.isInteractive()?scOnDozeTime:scOffDozeTime);
            }
        }
    }

    public void destory(){
        if(mdr==null){
            return;
        }
        Notify.cancelNotify(service);
        service.unregisterReceiver(mdr);
//        service.unregisterReceiver(br);
    }
}

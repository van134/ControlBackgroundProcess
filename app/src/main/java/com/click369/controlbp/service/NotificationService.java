package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.util.AppLoaderUtil;

/**
 * Created by asus on 2017/7/5.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class NotificationService extends NotificationListenerService {
    public static boolean isNotifyRunning = false;
//    public static boolean isClockOpen = false;
//    public static boolean isMusicOpen = false;
//    public static HashSet<String> notifs = new HashSet<>();
//    public static HashSet<String> notifyNotColseList = new HashSet<String>();
//    public long lastTime = 0;
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        addNotify(this,sbn.getPackageName());
    }

    @Override
    public void onNotificationRemoved(final StatusBarNotification sbn) {
        StatusBarNotification sbns[] = getActiveNotifications();
        boolean isRemove = true;
        if (sbns!=null){
            for (StatusBarNotification s:sbns){
                if (sbn.getPackageName().equals(s.getPackageName())){
                    isRemove = false;
                }
            }
        }
        if (isRemove) {
            removedNotify(this, sbn.getPackageName());
        }
    }

    public static void removedNotify(final Context cxt,final String pkg){
        final AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(pkg)?AppLoaderUtil.allAppStateInfos.get(pkg):new AppStateInfo();
        if(!asi.isHasNotify){//System.currentTimeMillis()-WatchDogService.lastNotifyTime<100||
            return;
        }
        asi.isHasNotify = false;
//        WatchDogService.allAppStateInfos.put(pkg,asi);
//        WatchDogService.notifs.remove(pkg);
        String mpkg = pkg.toLowerCase()+"";
        Log.i("CONTROL", "remove notify" + "-----" + pkg);
        if(mpkg.contains("android.deskclock")){
            WatchDogService.isClockOpen = false;
            Intent intent = new Intent("com.click369.offscreen.changestate.on");
            cxt.sendBroadcast(intent);

        }else{
            if(asi.isNotifyNotStop){
//            if(WatchDogService.notifyNotColseList.contains(pkg)){
                new Thread(){
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(500);
                            if(!WatchDogService.openPkgName.equals(pkg)){
                                XposedStopApp.stopApk(pkg,cxt);
                                asi.isNotifyNotStop = false;
                            }
                        } catch (Exception e) {
                        }
                    }
                }.start();
            }
//            else if(!WatchDogService.openPkgName.equals(pkg)&&isNotifyRunning){
//                Intent intent = new Intent("com.click369.control.notify");
//                intent.putExtra("type","remove");
//                intent.putExtra("pkg",pkg);
//                cxt.sendBroadcast(intent);
//            }
        }
        WatchDogService.lastNotifyTime = System.currentTimeMillis();
    }

    public static void addNotify(Context cxt,String pkg){
        final AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(pkg)?AppLoaderUtil.allAppStateInfos.get(pkg):new AppStateInfo();
        if(asi.isHasNotify){//System.currentTimeMillis()-WatchDogService.lastNotifyTime<100||
            return;
        }
//        WatchDogService.notifs.add(pkg);
        asi.isHasNotify = true;
//        WatchDogService.allAppStateInfos.put(pkg,asi);
        if(pkg.toLowerCase().contains("android.deskclock")){
            WatchDogService.isClockOpen = true;
        }else if(pkg.toLowerCase().contains("music")||pkg.equals("com.cyanogenmod.eleven")){
            WatchDogService.isMusicOpen = ((AudioManager)cxt.getApplicationContext().getSystemService(Context.AUDIO_SERVICE)).isMusicActive();
        }
        Log.i("CONTROL", "open notify" + "-----" + pkg);
        WatchDogService.lastNotifyTime = System.currentTimeMillis();
    }
//    private boolean isContainsNotify(String pkg){
//        StatusBarNotification[] nos=  getActiveNotifications();
//        if(nos!=null&&nos.length>0){
//            for(StatusBarNotification sbn : nos){
//                if(sbn.getPackageName().equals(pkg)){
////                    Log.i("DOZE",pkg+"还有通知");
//                   return true;
//                }
//            }
//        }
////        Log.i("DOZE",pkg+"没有通知");
//        return  false;
//    }

    @Override
    public void onCreate() {
        isNotifyRunning = true;
        Log.i("DOZEX","通知监听开启...");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        isNotifyRunning = false;
        super.onDestroy();
    }
}

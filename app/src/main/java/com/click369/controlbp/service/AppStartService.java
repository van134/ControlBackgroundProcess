package com.click369.controlbp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.click369.controlbp.util.ShellUtilNoBackData;

import java.util.HashMap;

/**
 * Created by asus on 2017/10/31.
 */
public class AppStartService {
    private WatchDogService service;
    private MyAutoStartReceiver receiver;
    public HashMap<String,Integer> startApps = new HashMap<String,Integer>();
    private String action =  null;
//    private SharedPreferences autoStartPrefs;
    private Handler h ;
//    public static String waitUnlockApp = "";
//    public static boolean isStrongAutoStart = false;
    public static boolean isOffScLockApp = false;
//    private long lastUnlockTime =0;
    public AppStartService(WatchDogService service){
        this.service = service;
        IntentFilter ifliter = new IntentFilter();
//        ifliter.addAction("com.click369.control.startapp");
//        ifliter.addAction("com.click369.control.startservice");
//        ifliter.addAction("com.click369.control.startactivity");
        ifliter.addAction("com.click369.control.removerecent");
//        ifliter.addAction("com.click369.control.lockappbackground");
        receiver = new MyAutoStartReceiver();
        service.registerReceiver(receiver,ifliter);
        h = new Handler();
//        autoStartPrefs = SharedPrefsUtil.getPreferences(service,Common.PREFS_AUTOSTARTNAME);
    }
    int errorCount = 0;
    class  MyAutoStartReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.click369.control.startapp")){
//                if(intent.hasExtra("appstart")){
//                    String pkg = intent.getStringExtra("appstart");
//                    String type = intent.getStringExtra("type");
////                    Log.e("CONTROL","appstart "+pkg+"  "+type);
//                    if ("stopapp".equals(type)&&service.forceStopAppList.contains(pkg)){
////                        Log.e("CONTROL","应用禁止启动 杀死 "+pkg);
//                        stopApp(pkg,true);
//                        return;
//                    }
//                   if (AppStartService.this.action!=null&&
//                           AppStartService.this.action.contains(pkg)||
//                           System.currentTimeMillis()-WatchDogService.lastPressedRecent<600||
//                           System.currentTimeMillis()-lastChooseTime<5000){//从最近任务中打开的临时处理办法
//                       lastChooseTime = 0;
//                       AppStartService.this.action = null;
//                       startApps.put(pkg,2);
//                       return;
//                   }
//                    AppStartService.this.action = null;
//                    if((!startApps.containsKey(pkg)||startApps.get(pkg)!=2)&&
//                            service.autoStartList.contains(pkg)){
//                        Log.e("CONTROL","应用自启动 杀死 "+pkg);
//                        stopApp(pkg,false);
//                    }
//                }
            }else if(intent.getAction().equals("com.click369.control.startservice")){
//                if(intent.hasExtra("servicestart")){
//                    String pkg = intent.getStringExtra("servicestart");
//                    String type = intent.getStringExtra("type");
////                    Log.e("CONTROL","servicestart "+pkg+"  "+type);
//                    if ("stopapp".equals(type)&&service.forceStopAppList.contains(pkg)){
//                        Log.e("CONTROL","应用禁止启动 杀死 "+pkg);
//                        stopApp(pkg,true);
//                        return;
//                    }
//                    if (AppStartService.this.action!=null&&AppStartService.this.action.contains(pkg)||
//                            System.currentTimeMillis()-WatchDogService.lastPressedRecent<600||
//                            System.currentTimeMillis()-lastChooseTime<5000){//从最近任务中打开的临时处理办法
//                        AppStartService.this.action = null;
//                        lastChooseTime = 0;
//                        startApps.put(pkg,2);
//                        Log.e("CONTROL","servicestart 不杀 "+pkg);
//                        return;
//                    }
//                    AppStartService.this.action = null;
//                    if((!startApps.containsKey(pkg)||startApps.get(pkg)!=2)&&
//                            service.autoStartList.contains(pkg)){
////                        Log.e("CONTROL","服务自启动 杀死 "+pkg);
//                        stopApp(pkg,false);
//                    }
//                }
            }
//            else if(intent.getAction().equals("com.click369.control.test")){
//                String from = intent.getStringExtra("from");
//                String pkg = intent.getStringExtra("pkg");
//                String cls = intent.getStringExtra("class");
//                String action = intent.getStringExtra("action");
//                if (pkg!=null&&!pkg.equals("null")){
//                    if (service.autoStartList.contains(pkg)||
//                            service.forceStopAppList.contains(pkg)) {
//                        startApps.put(pkg, 2);
//                    }
//                }else{
//                    AppStartService.this.action = action;
//                }
//                Log.i("CONTROL","启动 从 "+from+" 打开 "+pkg+" 中的 "+cls+" action "+action+" ");
//            }
            else if(intent.getAction().equals("com.click369.control.removerecent")){
                String pkg = intent.getStringExtra("pkg");
                if(!pkg.equals("com.click369.control")){

                    if(service.isAtuoRemoveIce&& service.iceButOpenInfos.size()>0&&service.iceButOpenInfos.contains(pkg)){
                        ShellUtilNoBackData.execCommand("pm disable "+pkg);
                        Log.e("CONTROL", "划掉 冻结"+pkg);
                        service.iceButOpenInfos.remove(pkg);
                    }
                }
                Log.e("CONTROL","从最近任务列表移除 "+pkg+"  "+intent.getStringExtra("killfail"));
                if (intent!=null&&WatchDogService.removeRecents.contains(pkg)){
                    XposedStopApp.stopApk(pkg,service);
                }
                if (!isOffScLockApp&&service.autoStartPrefs.contains(pkg+"/lockok")){
                    service.autoStartPrefs.edit().remove(pkg+"/lockok").commit();
                }
            }
        }
    }

//    Runnable cancelFinger = new Runnable() {
//        @Override
//        public void run() {
//            MyFingerUtil.cancel();
//            waitUnlockApp = "";
//        }
//    };

//    public void backClean(String pkg){
//        if(startApps.containsKey(pkg)){
//            startApps.remove(pkg);
//        }
//    }
//
//    long lastChooseTime = 0;
//    public void openApp(Intent intent){
//        String from = intent.getStringExtra("from");
//        String pkg = intent.getStringExtra("pkg");
//        String cls = intent.getStringExtra("class");
//        String action = intent.getStringExtra("action");
//        if (pkg!=null&&!pkg.equals("null")){
//            if (service.autoStartList.contains(pkg)) {
//                startApps.put(pkg, 2);
//            }else if(service.forceStopAppList.contains(pkg)){
//                Log.e("CONTROL","应用禁止启动 杀死 "+pkg);
//                stopApp(pkg,true);
//            }else if("com.android.internal.app.ChooserActivity".equals(cls)){//进入选择界面
//                lastChooseTime = System.currentTimeMillis();
//            }
//        }else{
//            AppStartService.this.action = action;
//        }
//        Log.i("CONTROL","启动 从 "+from+" 打开 "+pkg+" 中的 "+cls+" action "+action+" ");
//    }
//
//    public void clean(String ps){
//        if (ps==null||ps.length()<50){
//            return;
//        }
//        Set<String> keys =startApps.keySet();
//        if(keys.size()>0){
//            HashSet<String> notRun = new HashSet<String>();
//            for(String key:keys){
//                if(!ps.contains(key)){
//                    notRun.add(key);
//                }
//            }
//            for(String s:notRun){
//                startApps.remove(s);
//            }
//        }
//    }

    public void destory(){
        service.unregisterReceiver(receiver);
    }

//    private void stopApp(final String pkg,final boolean isNotStart){
//            if(isNotStart){
//                if((pkg!=null&&
//                        !pkg.equals(Common.PACKAGENAME)&&
//                        !WatchDogService.homePkg.equals(pkg)&&
//                        !WatchDogService.imePkgs.contains(pkg))||
//                        ContainsKeyWord.isContainsBlackPkg(pkg)){
//                    XposedStopApp.stopApk(pkg,service);
//                    Log.e("CONTROL","禁止启动杀死 "+pkg);
//                    startApps.remove(pkg);
//                }
//            }else {
//                if(isStrongAutoStart){
//                    if ((pkg != null &&
//                            !pkg.equals(WatchDogService.openPkgName) &&
//                            !pkg.equals(WatchDogService.nowPkgName) &&
//                            !pkg.equals(Common.PACKAGENAME) &&
//                            !WatchDogService.homePkg.equals(pkg) &&
//                            !WatchDogService.imePkgs.contains(pkg))||
//                            ContainsKeyWord.isContainsBlackPkg(pkg)) {
//                        XposedStopApp.stopApk(pkg, service);
//                        Log.e("CONTROL", "自启动杀死1 " + pkg);
//                        startApps.remove(pkg);
//                    }
//                }else {
//                    new Thread() {
//                        @Override
//                        public void run() {
//                            try {
//                                Thread.sleep(600);
//                                if ((pkg != null &&
//                                        !pkg.equals(WatchDogService.openPkgName) &&
//                                        !pkg.equals(WatchDogService.nowPkgName) &&
//                                        !pkg.equals(Common.PACKAGENAME) &&
//                                        !WatchDogService.homePkg.equals(pkg) &&
//                                        !WatchDogService.imePkgs.contains(pkg))||
//                                        ContainsKeyWord.isContainsBlackPkg(pkg)) {
//                                    XposedStopApp.stopApk(pkg, service);
//                                    Log.e("CONTROL", "自启动杀死2 " + pkg);
//                                    startApps.remove(pkg);
//                                }
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }.start();
//                }
//        }
//    }
}

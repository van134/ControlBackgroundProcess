package com.click369.controlbp.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

//import com.click369.dozex.IceRoomActivity;
//import com.click369.dozex.util.DozeAbout;
//import com.click369.dozex.util.SavePerfrence;

import com.click369.controlbp.activity.ColorNavBarActivity;
import com.click369.controlbp.activity.IceRoomView;
import com.click369.controlbp.activity.UIControlFragment;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by asus on 2017/6/3.
 */
public class NewWatchDogService extends AccessibilityService {
//    performGlobalAction(GLOBAL_ACTION_HOME);
    public static int run_state = 0;
    public static int run_from = 0;
//    public static ArrayList<String> launcherPkgs = new ArrayList<String>();
//    public static ArrayList<String> imePkgs = new ArrayList<String>();
//    public static HashMap<String,HashSet<String>> openOtherPkgNames = new HashMap<String,HashSet<String>>();
//    public static String openPkgName = "";//当前打开的 比较准确
//    public static String nowPkgName = "";//当前打开的 不一定准确
    private Handler handler = new Handler();
//    public static boolean isOpenApp = false;
    public static boolean isOpenNewDogService = false;
//    public static boolean isOpenCamera = false;
//    private boolean isOpenInstall = false;
//    private HashMap<String,Long> launcherTimes = new HashMap<String,Long>();
    @Override
    public void onAccessibilityEvent(final AccessibilityEvent accessibilityEvent) {
        if (!WatchDogService.isNotNeedAccessibilityService&&
                accessibilityEvent != null &&
                accessibilityEvent.getPackageName() != null &&
                run_state!=1&&
                !"android".equals(String.valueOf(accessibilityEvent.getPackageName()))) {
            final String pkgName = String.valueOf(accessibilityEvent.getPackageName());
            Log.i("CONTROL","========"+pkgName+"  "+String.valueOf(accessibilityEvent.getClassName()));
            if (WatchDogService.homePkg.equals(pkgName)&&
                    !"com.android.settings".equals(pkgName)){
                if(accessibilityEvent.getClassName()==null||accessibilityEvent.getClassName().toString().startsWith("android.widget")){
                    return;
                }
                String cls = accessibilityEvent.getClassName().toString();
                if (Common.SHOWDIALOGCLS.equals(cls)){
                    return;
                }
                Intent intent = new Intent("com.click369.control.test");
                intent.putExtra("pkg",pkgName);
                intent.putExtra("from","");
                intent.putExtra("class",cls);
                intent.putExtra("action", "");
                sendBroadcast(intent);
            }else {
                String cls = accessibilityEvent.getClassName().toString();
                if (Common.SHOWDIALOGCLS.equals(cls)){
                    return;
                }
                Intent intent = new Intent("com.click369.control.test");
                intent.putExtra("pkg",pkgName);
                intent.putExtra("from","");
                intent.putExtra("class",cls);
                intent.putExtra("action", "");
                sendBroadcast(intent);
            }
        }else if(run_state == 1&& String.valueOf(accessibilityEvent.getPackageName()).startsWith("com.android.settings")){
            openRunning(accessibilityEvent);
            if (WatchDogService.isNotNeedAccessibilityService) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (WatchDogService.isNotNeedAccessibilityService) {
                            OpenCloseUtil.closeOpenAccessibilitySettingsOn(NewWatchDogService.this, false);
                        }
                    }
                }, 1500);
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    protected void onServiceConnected() {
        Log.i("DOZE", "辅助服务开启");
//        WatchDogService.launcherPkgs.clear();
        WatchDogService.imePkgs.clear();
        WatchDogService.homePkg = WatchDogService.getDefaultHome(this);
//        WatchDogService.launcherPkgs.addAll(WatchDogService.getLauncherPackageName(this));
        WatchDogService.imePkgs.addAll(WatchDogService.getInputPackageName(this));
        isOpenNewDogService = true;
        run_state = 0;

        SharedPreferences settings = SharedPrefsUtil.getPreferences(this,Common.PREFS_APPSETTINGS);
        WatchDogService.isNotNeedAccessibilityService = settings.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true);
        if (!WatchDogService.isKillRun) {
            Intent intent = new Intent(NewWatchDogService.this, WatchDogService.class);
            startService(intent);
        }
        super.onServiceConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOpenNewDogService = false;
//        handler.removeCallbacks(backR);
//        handler.removeCallbacks(openR);
        if (!WatchDogService.isNotNeedAccessibilityService ) {
            Intent intent1 = new Intent("com.click369.control.accessclose");
            sendBroadcast(intent1);
        }
//        SharedPreferences settings = getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        if (settings.getBoolean(Common.ISCOLORBAROPEN,false)){
//            Intent intent = new Intent(this,ColorNavBarService.class);
//            stopService(intent);
//        }
        Log.e("DOZE", "结束辅助服务");
    }

//    public static ArrayList<String> getLauncherPackageName(Context context) {
//        ArrayList<String> packageNames = new ArrayList<String>();
//        final Intent intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        for (ResolveInfo ri : resolveInfo) {
//            packageNames.add(ri.activityInfo.packageName);
//        }
//        return packageNames;
//    }
//
//    public static  ArrayList<String> getInputPackageName(Context context) {
//        ArrayList<String> packageNames = new ArrayList<String>();
//        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        List<InputMethodInfo> methodList = imm.getInputMethodList();
//        for(InputMethodInfo imi:methodList){
//            packageNames.add(imi.getPackageName());
//        }
//        return packageNames;
//    }

    /*
    * +++++++++++++++++ com.netease.newsreader.activity  com.netease.nr.biz.ad.AdActivity
10-31 16:12:23.048 2108-2108/com.click369.controlbp I/DOZE: +++++++++++++++++ com.teslacoilsw.launcher  android.widget.LinearLayout
10-31 16:12:26.006 2108-2108/com.click369.controlbp E/CONTROL: actstart com.netease.newsreader.activity  com.netease.nr.phone.main.MainActivity
10-31 16:12:26.573 2108-2108/com.click369.controlbp I/DOZE: +++++++++++++++++ com.netease.newsreader.activity  com.netease.nr.phone.main.MainActivity
    *
    * */
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void jumpAD(){
//        handler.postDelayed(adJump,10);
//        handler.postDelayed(adJump,200);
//        handler.postDelayed(adJump,800);
//    }
//    Runnable adJump = new Runnable() {
//        @Override
//        public void run() {
//            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
//                AccessibilityNodeInfo an = getRootInActiveWindow();
//                if (an == null){
//                    return;
//                }
//                List<AccessibilityNodeInfo> list1 = an.findAccessibilityNodeInfosByText("跳过");
//                Log.i("CONTROL", "点击跳过111"+list1.size());
//                if (list1.size() > 0) {
//                    Log.i("CONTROL", "点击跳过");
//                    list1.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                }
//            }
//        }
//    };

    static final String TEXT_FIND_KEY1 = "OEM";//"";
    static final String TEXT_FIND_KEY2 = "不锁定";//"";
    static final String TEXT_FIND_KEY3 = "bug";//"";
    static final String TEXT_KEY = "运行";//"正在运行的服务";
    static final String TEXT_KEYDEV = "开发者选项";//"正在运行的服务";
    static final String TEXT_KEYDEV1 = "Developer options";//"正在运行的服务";
    static final String TEXT_KEY1= "Running";//"正在运行的服务";
    AccessibilityNodeInfo nodeInfo = null;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void openRunning(AccessibilityEvent event) {
        Log.i("CONTROL", "点击跳过  "+event.getClassName());
        if ("com.android.settings.Settings$DevelopmentSettingsActivity".equals(event.getClassName())||(run_from==1&&"com.android.settings.SubSettings".equals(event.getClassName()))) {
            run_state = 0;
            nodeInfo = getRootInActiveWindow();
            if (nodeInfo==null){
                Toast.makeText(this,"无法找到正在运行的服务", Toast.LENGTH_LONG).show();
                return;
            }
            List<AccessibilityNodeInfo> list1 = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEY);
            if(list1.size()==0){
                list1 = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEY1);
                if(list1.size()==0) {
                    if (nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY1) != null && nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY1).size() > 0) {
                        nodeInfo = nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY1).get(0).getParent().getParent();
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        handler.postDelayed(r, 250);
                    } else if (nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY2) != null && nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY2).size() > 0) {
                        nodeInfo = nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY2).get(0).getParent().getParent();
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        handler.postDelayed(r, 250);
                    }else if (nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY3) != null && nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY3).size() > 0) {
                        nodeInfo = nodeInfo.findAccessibilityNodeInfosByText(TEXT_FIND_KEY3).get(0).getParent().getParent();
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                        handler.postDelayed(r, 250);
                    } else {
                        Toast.makeText(this, "无法找到正在运行的服务", Toast.LENGTH_LONG).show();
                    }
                }else{
                    for(AccessibilityNodeInfo nf:list1){
                        AccessibilityNodeInfo parent = nf.getParent();
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        run_state = 0;
                        break;
                    }
                }
            }else{
                for(AccessibilityNodeInfo nf:list1){
                    AccessibilityNodeInfo parent = nf.getParent();
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    run_state = 0;
                    break;
                }
            }
        }else if ("com.android.settings.Settings$SystemDashboardActivity".equals(event.getClassName())) {
           // run_state = 0;
            nodeInfo = getRootInActiveWindow();
            if (nodeInfo==null){
                Toast.makeText(this,"无法找到正在运行的服务", Toast.LENGTH_LONG).show();
                return;
            }
            List<AccessibilityNodeInfo> list1 = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEYDEV);
            if(list1.size()==0){
                list1 = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEYDEV1);
                if(list1.size()!=0) {
                    for(AccessibilityNodeInfo nf:list1){
                        AccessibilityNodeInfo parent = nf.getParent();
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                        run_state = 0;
                        break;
                    }
                }
            }else{
                for(AccessibilityNodeInfo nf:list1){
                    AccessibilityNodeInfo parent = nf.getParent();
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                    run_state = 0;
                    break;
                }
            }
        }
    }
    int c = 0;
    Runnable r = new Runnable(){
        public void run() {
            c++;
            if (nodeInfo==null){
                Toast.makeText(NewWatchDogService.this,"无法找到正在运行的服务", Toast.LENGTH_LONG).show();
                return;
            }
            List<AccessibilityNodeInfo> list1 = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEY);
            if(list1.size()==0){
                list1 = nodeInfo.findAccessibilityNodeInfosByText(TEXT_KEY1);
                if(list1.size()==0) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    if (c < 4) {
                        handler.postDelayed(r, 250);
                    } else {
                        run_state = 0;
                        Toast.makeText(NewWatchDogService.this, "无法找到正在运行的服务", Toast.LENGTH_LONG).show();
                    }
                }else{
                    for(AccessibilityNodeInfo nf:list1){
                        AccessibilityNodeInfo parent = nf.getParent();
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        run_state = 0;
                        break;
                    }
                }
            }else{
                for(AccessibilityNodeInfo nf:list1){
                    AccessibilityNodeInfo parent = nf.getParent();
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    run_state = 0;
                    break;
                }
            }
        }
    };
}
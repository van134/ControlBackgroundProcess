package com.click369.controlbp.service;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.job.JobInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.view.KeyEvent;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedAppStartNotifyListener {
    static HashSet<String> mmnotifys = null;

    public static void sendBroad(final Intent intent,final Context context,final String callingPkg,final String pkg){
        new Thread(){
            @Override
            public void run() {
                try {
//                    String pkg = intent.getComponent()==null?null:intent.getComponent().getPackageName();
                    if (context!=null) {
//                        autoStartPrefs.reload();
                        String action = intent.getAction();
                        if (action!=null&&"com.click369.control.lockapp".equals(action)){
                            return;
                        }
                        String cls =  pkg==null?null:intent.getComponent().getClassName();
                        Intent broad = new Intent("com.click369.control.test");
                        broad.putExtra("pkg", pkg);
                        broad.putExtra("class",cls);
                        broad.putExtra("from", callingPkg);
                        broad.putExtra("action",action);
                        if (context!=null){
                            if (Common.SHOWDIALOGCLS.equals(cls)){
                                return;
                            }
                            context.sendBroadcast(broad);
                        }
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static Intent mintent;
    private static int mcode;
    private static Bundle mbundle;
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,
                                   final XSharedPreferences autoStartPrefs,
                                   final boolean isAutoStartOpen,
                                   final boolean isNotNeedAccess){
        final Class actCls = XposedUtil.findClass("android.app.Activity",lpparam.classLoader);
        try {
            XposedHelpers.findAndHookMethod(actCls, "startActivityForResult", Intent.class, int.class, Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Intent intent = (Intent) methodHookParam.args[0];
                        if (intent != null){
                            final String pkg = intent.getComponent() == null ? null : intent.getComponent().getPackageName();
                            if (!lpparam.packageName.equals(pkg)) {
                                autoStartPrefs.reload();
                                if (isAutoStartOpen&&pkg != null&&autoStartPrefs.getBoolean(pkg + "/lockapp", false) &&
                                        !autoStartPrefs.getBoolean(pkg + "/lockok", false)) {
                                    String cls = pkg == null ? null : intent.getComponent().getClassName();
//                                    if (!(Common.PACKAGENAME + ".activity.UnLockActivity").equals(cls) &&
//                                            !(Common.PACKAGENAME + ".activity.RunningActivity").equals(cls)&&
//                                            !"com.android.webview".equals(lpparam.packageName)&&
//                                            !("com.eg.android.AlipayGphone".equals(lpparam.packageName)&&("com.alipay.mobile.nebulacore.ui.H5Activity".equals(cls)||"com.alipay.mobile.quinox.LauncherActivity".equals(cls)))) {
                                    if(!ContainsKeyWord.isContainsNotLockApk(cls)&&
                                            !ContainsKeyWord.isContainsNotLockApk(lpparam.packageName)){
                                        final Activity cxt = ((Activity) methodHookParam.thisObject);
                                        final Intent broad = new Intent("com.click369.control.lockapp");
                                        broad.putExtra("pkg", pkg);
                                        broad.putExtra("class", cls);
                                        broad.putExtra("intent", intent);
                                        broad.putExtra("isneedbroad", true);
//                                        broad.putExtra("bundle", (Bundle) methodHookParam.args[2]);
                                        mintent = (Intent) methodHookParam.args[0];
                                        mcode = (int) methodHookParam.args[1];
                                        mbundle = (Bundle) methodHookParam.args[2];
                                        if("com.coolapk.searchbox".equals(lpparam.packageName)){
                                            Handler h = new Handler();
                                            h.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    cxt.startActivity(broad);
//                                                    cxt.startActivityForResult(broad,0x1111);
                                                }
                                            },800);
                                        }else{
//                                            cxt.startActivityForResult(broad,0x1111);
                                            cxt.startActivity(broad);
                                        }
                                        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                                            @Override
                                            public void onReceive(Context context, Intent intent1) {
//                                                XposedBridge.log("hook onActivityResult "+intent1);
                                                if(intent1.getBooleanExtra("islockok",false)&&
                                                        pkg.equals(intent1.getStringExtra("pkg"))){
                                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                                        cxt.startActivityForResult(mintent,mcode,mbundle);
                                                    }
                                                }
                                                cxt.unregisterReceiver(this);
                                            }
                                        };
                                        cxt.registerReceiver(broadcastReceiver,new IntentFilter("com.click369.lock"));
                                        methodHookParam.setResult(null);
                                        return;
                                    }
                                }
                                if(isNotNeedAccess){
                                    sendBroad(intent, (Context) methodHookParam.thisObject, lpparam.packageName,pkg);
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            });
//            XposedUtil.hookMethod(actCls, XposedUtil.getParmsByName(actCls, "onActivityResult"), "onActivityResult", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//
//                    if(param.args.length>2&&param.args[2]!=null){
//                        Intent intent1 = (Intent) param.args[2];
//                        XposedBridge.log("hook onActivityResult "+intent1);
//                        if(intent1.getBooleanExtra("islockok",false)){
//                            Activity activity = (Activity) param.thisObject;
//                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                                activity.startActivityForResult(intent,code,bundle);
//                            }
//                        }
//                        if("click369.lock".equals(intent1.getAction())){
//                            param.setResult(null);
//                            return;
//                        }
//                    }
//                }
//            });
//            if (lpparam.packageName.equals("com.coolapk.market")){
//                XposedHelpers.findAndHookMethod(actCls, "onCreate", Bundle.class, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onCreate before ^^^^^^^^^^^^^^^^^");
//                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onCreate after ^^^^^^^^^^^^^^^^^");
//                    }
//                });
//                XposedHelpers.findAndHookMethod(actCls, "onStart", new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onStart before ^^^^^^^^^^^^^^^^^");
//                    }
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        XposedBridge.log("^^^^^^^^^^^^^^酷安 onStart after ^^^^^^^^^^^^^^^^^");
//                    }
//                });
//            }

        }catch (Throwable e){
            XposedBridge.log("^^^^^^^^^^^^^^autostart error "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
            e.printStackTrace();
        }
//        autoStartPrefs.reload();
//        if (autoStartPrefs.getBoolean(lpparam.packageName + "/lockapp", false)) {
            try {
                XposedUtil.hookMethod(actCls,XposedUtil.getParmsByName(actCls,"onResume"),"onResume", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                        try {
                            String pkg = lpparam.packageName;
                            String cls = methodHookParam.thisObject.getClass().getName();
                            final Activity act = ((Activity) methodHookParam.thisObject);
                            autoStartPrefs.reload();
                            if (isAutoStartOpen&&autoStartPrefs.getBoolean(pkg + "/lockapp", false)&&!ContainsKeyWord.isContainsNotLockApk(cls)&&
                                    !ContainsKeyWord.isContainsNotLockApk(lpparam.packageName)) {
//                                autoStartPrefs.reload();
                                if (!autoStartPrefs.getBoolean(pkg + "/lockok", false)) {
                                    if (!(Common.PACKAGENAME + ".activity.UnLockActivity").equals(cls)) {
//                                        if (!Common.PACKAGENAME.equals(pkg)) {
//                                            act.moveTaskToBack(false);
//                                        }
                                        final Intent broad = new Intent("com.click369.control.lockapp");
                                        broad.putExtra("pkg", pkg);
                                        broad.putExtra("class", cls);
                                        act.startActivity(broad);
                                        if (Common.PACKAGENAME.equals(pkg)) {
                                            act.finish();
                                        }
                                    }
                                }
                            }
                            if(isNotNeedAccess) {
                                Intent broad = new Intent("com.click369.control.test");
                                broad.putExtra("pkg", pkg);
                                broad.putExtra("from", pkg);
                                broad.putExtra("class", cls);
                                broad.putExtra("action", "");
                                if (!Common.SHOWDIALOGCLS.equals(cls)) {
                                    act.sendBroadcast(broad);
                                }
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });
            }catch (Throwable e){
                e.printStackTrace();
            }
//        }
        try {
            if(lpparam.packageName.equals("android")) {
                try {

                    final Class notifyCls = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService$NotificationListeners",lpparam.classLoader);
                    final Class managerCls = XposedHelpers.findClass("com.android.server.notification.ManagedServices",lpparam.classLoader);

                    if (isNotNeedAccess&&notifyCls!=null&&managerCls!=null){
                        mmnotifys = new HashSet<String>();
                        Method ms[] = notifyCls.getDeclaredMethods();
                        Method temp1 = null,temp2 = null;
                        for (Method mm:ms){
                            if (temp1==null&&mm.getName().equals("notifyRemoved")){
                                temp1 = mm;
                            }else if (temp2==null&&mm.getName().equals("notifyPosted")){
                                temp2 = mm;
                            }
                        }
                        final Method notifyRemoved = temp1;
                        final Method notifyPosted = temp2;
                        if(notifyRemoved!=null){
                            Class clss[] = notifyRemoved.getParameterTypes();
                            XC_MethodHook hook = new XC_MethodHook() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                                @Override
                                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    try {
                                        StatusBarNotification sbn = (StatusBarNotification) methodHookParam.args[1];
                                        boolean isRemove = false;
                                        int id = sbn.getId();
                                        String pkg = sbn.getPackageName();
                                        String key = pkg+"/"+id;
                                        if (mmnotifys.contains(key)){
                                            mmnotifys.remove(key);
                                            isRemove = true;
                                        }
                                        if(isRemove){
                                            for (String s:mmnotifys){
                                                if (s.startsWith(pkg+"/")){
                                                    isRemove = false;
                                                }
                                            }
                                        }
                                        if (isRemove) {
                                            Intent broad = new Intent("com.click369.control.notify");
                                            broad.putExtra("type", "remove");
                                            broad.putExtra("pkg", pkg);
                                            Field cxtField = managerCls.getDeclaredField("mContext");
                                            cxtField.setAccessible(true);
                                            Object cxtObject = cxtField.get(methodHookParam.thisObject);
                                            try {
                                                ((Context) cxtObject).sendBroadcast(broad);
                                            } catch (Throwable e) {
                                            }
                                        }
                                    } catch (Exception e) {
                                        XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyRemoved error "+e+"^^^^^^^^^^^^^^^^^");
                                    }
                                }
                            };
                            if(clss!=null){
                                XposedUtil.hookMethod(notifyCls,clss,"notifyRemoved",hook);
                            }else{
                                XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyRemoved null 未找到^^^^^^^^^^^^^^^^^");
                            }
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyRemoved clss null 未找到^^^^^^^^^^^^^^^^^");
                        }
                        if(notifyPosted!=null){
                            Class clss[] = notifyPosted.getParameterTypes();
                            XC_MethodHook hook = new XC_MethodHook() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
                                @Override
                                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    try {
                                        StatusBarNotification sbn = (StatusBarNotification)methodHookParam.args[1];
                                        int id = sbn.getId();
                                        String pkg = sbn.getPackageName();
                                        String key = pkg+"/"+id;
                                        if (!mmnotifys.contains(key)){
                                            mmnotifys.add(key);
                                            Intent broad = new Intent("com.click369.control.notify");
                                            broad.putExtra("type","add");
                                            broad.putExtra("pkg", pkg);
                                            broad.putExtra("flags", sbn.getNotification().flags);
                                            broad.putExtra("clearable", sbn.isClearable());
                                            Field cxtField = managerCls.getDeclaredField("mContext");
                                            cxtField.setAccessible(true);
                                            Object cxtObject = cxtField.get(methodHookParam.thisObject);
                                            try {
                                                ((Context) cxtObject).sendBroadcast(broad);
                                            }catch (Throwable e){
                                            }
                                        }
                                    } catch (Exception e) {
                                        XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyPosted error "+e+"^^^^^^^^^^^^^^^^^");
                                    }
                                }
                            };
                            if(clss!=null){
                                XposedUtil.hookMethod(notifyCls,clss,"notifyPosted",hook);
                            }else{
                                XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyPosted null 未找到^^^^^^^^^^^^^^^^^");
                            }

                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyPosted clss null 未找到^^^^^^^^^^^^^^^^^");
                        }
                    }
                    final Class windowManagerCls = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",lpparam.classLoader);
                    if(windowManagerCls!=null){
                        Class clss[] = XposedUtil.getParmsByName(windowManagerCls,"interceptKeyBeforeDispatching");
                        if (clss!=null){
                            XC_MethodHook hook = new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    try {
                                        KeyEvent event = null;
                                        if (methodHookParam.args[1] instanceof KeyEvent){
                                            event = (KeyEvent)methodHookParam.args[1];
                                        }else if (methodHookParam.args[2] instanceof KeyEvent){
                                            event = (KeyEvent)methodHookParam.args[2];
                                        }else if (methodHookParam.args[0] instanceof KeyEvent){
                                            event = (KeyEvent)methodHookParam.args[0];
                                        }
//                                    AlertDialog
                                        if (event!=null) {
                                            int keyCode = event.getKeyCode();
                                            if (keyCode == KeyEvent.KEYCODE_HOME&&event.getAction() == KeyEvent.ACTION_UP&&!event.isLongPress()) {
                                                Field contextField = windowManagerCls.getDeclaredField("mContext");
                                                contextField.setAccessible(true);
                                                Context context = (Context) contextField.get(methodHookParam.thisObject);
                                                Intent intent1 = new Intent("com.click369.control.keylistener");
                                                intent1.putExtra("reason", "homekey");
                                                context.sendBroadcast(intent1);
                                            }
                                        }else{
                                            XposedBridge.log("^^^^^^^^^^^^^^goHome event null ^^^^^^^^^^^^^^^^^");
                                        }
                                    }catch (RuntimeException e){
                                        XposedBridge.log("^^^^^^^^^^^^^^goHome err"+e+"^^^^^^^^^^^^^^^^^");
                                    }
                                }
                            };
                            XposedUtil.hookMethod(windowManagerCls,clss,"interceptKeyBeforeDispatching",hook);
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^interceptKeyBeforeDispatching null 未找到^^^^^^^^^^^^^^^^^");
                        }
                    }
                } catch (XposedHelpers.ClassNotFoundError e) {
                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify error1 "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
                }
            }
        }catch (Throwable e){
            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify error 2"+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
        }
    }
}

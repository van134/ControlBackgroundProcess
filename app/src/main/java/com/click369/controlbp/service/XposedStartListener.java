package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedStartListener {
    final static HashSet<String> mmnotifys = new HashSet<String>();
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam){
        try {
            Class actCls = XposedHelpers.findClass("android.app.Activity",lpparam.classLoader);
            if (actCls!=null){
                XposedHelpers.findAndHookMethod(actCls, "onResume", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                    try {
                        final Context context = ((Context) methodHookParam.thisObject);
                        String cls = methodHookParam.thisObject.getClass().getName();
                            Intent broad = new Intent("com.click369.control.test");
                            broad.putExtra("pkg", lpparam.packageName);
                            broad.putExtra("from", lpparam.packageName);
                            broad.putExtra("class", cls);
                            broad.putExtra("action", "");
                            if(context!=null) {
                                if (Common.SHOWDIALOGCLS.equals(cls)){
                                    return;
                                }
                                context.sendBroadcast(broad);
                            }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    }
                });
            }
            if(lpparam.packageName.equals("android")) {
                try {
                    final  Class notSerCls = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService",lpparam.classLoader);
                    final Class notifyCls = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService$NotificationListeners",lpparam.classLoader);
                    final Class managerCls = XposedHelpers.findClass("com.android.server.notification.ManagedServices",lpparam.classLoader);

                    if (notifyCls!=null&&managerCls!=null){
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
//                                    Field mServiceField = notSerCls.getDeclaredField("mService");
//                                    mServiceField.setAccessible(true);
//                                    Field this$0Field = notifyCls.getDeclaredField("this$0");
//                                    this$0Field.setAccessible(true);
//                                    Object mServiceObj = mServiceField.get(this$0Field.get(methodHookParam.thisObject));
//                                    Method getMethod = mServiceObj.getClass().getDeclaredMethod("getActiveNotifications",String.class);
//                                    getMethod.setAccessible(true);
//                                    StatusBarNotification[] sbns = (StatusBarNotification[])getMethod.invoke(mServiceObj,"android");
                                    boolean isRemove = false;
//                                    if (sbns!=null){
//                                        for (StatusBarNotification s:sbns){
//                                            if (sbn.getPackageName().equals(s.getPackageName())){
//                                                isRemove = false;
//                                            }
//                                        }
//                                    }
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
                                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyRemoved error "+e+"^^^^^^^^^^^^^^^^^");
                                }
                                }
                            };
                            if(clss!=null){
                                if(clss.length == 3){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyRemoved", clss[0], clss[1], clss[2],hook );
                                }else if(clss.length == 4){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyRemoved", clss[0], clss[1], clss[2], clss[3],hook );
                                }else if(clss.length == 2){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyRemoved", clss[0], clss[1],hook );
                                }else if(clss.length == 5){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyRemoved", clss[0], clss[1], clss[2], clss[3], clss[4],hook );
                                }else{
                                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyRemoved clss else 未找到^^^^^^^^^^^^^^^^^");
                                }
                            }else{
                                XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyRemoved null 未找到^^^^^^^^^^^^^^^^^");
                            }
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyRemoved clss null 未找到^^^^^^^^^^^^^^^^^");
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
                                        Field cxtField = managerCls.getDeclaredField("mContext");
                                        cxtField.setAccessible(true);
                                        Object cxtObject = cxtField.get(methodHookParam.thisObject);
                                        try {
                                            ((Context) cxtObject).sendBroadcast(broad);
                                        }catch (Throwable e){
                                        }
                                    }
                                } catch (Exception e) {
                                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyPosted error "+e+"^^^^^^^^^^^^^^^^^");
                                }
                                }
                            };
                            if(clss!=null){
                                if(clss.length == 3){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyPosted", clss[0], clss[1], clss[2],hook );
                                }else if(clss.length == 4){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyPosted", clss[0], clss[1], clss[2], clss[3],hook );
                                }else if(clss.length == 2){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyPosted", clss[0], clss[1],hook );
                                }else if(clss.length == 5){
                                    XposedHelpers.findAndHookMethod(notifyCls, "notifyPosted", clss[0], clss[1], clss[2], clss[3], clss[4],hook );
                                }else{
                                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyPosted clss else 未找到^^^^^^^^^^^^^^^^^");
                                }
                            }else{
                                XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyPosted null 未找到^^^^^^^^^^^^^^^^^");
                            }

                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener notifyPosted clss null 未找到^^^^^^^^^^^^^^^^^");
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
                            if (clss.length == 3) {
                                XposedHelpers.findAndHookMethod(windowManagerCls, "interceptKeyBeforeDispatching",clss[0],clss[1],clss[2], hook);
                            }else if (clss.length == 4) {
                                XposedHelpers.findAndHookMethod(windowManagerCls, "interceptKeyBeforeDispatching",clss[0],clss[1],clss[2],clss[3], hook);
                            }else{
                                XposedBridge.log("^^^^^^^^^^^^^^interceptKeyBeforeDispatching else "+clss.length+"^^^^^^^^^^^^^^^^^");
                            }
                        }else{
                            XposedBridge.log("^^^^^^^^^^^^^^interceptKeyBeforeDispatching null 未找到^^^^^^^^^^^^^^^^^");
                        }
                    }
                } catch (XposedHelpers.ClassNotFoundError e) {
                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener error1 "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
                }
            }
        }catch (XposedHelpers.ClassNotFoundError e){
            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener error 2"+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
        }catch (RuntimeException e){
            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListener error 3"+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
        }
    }


}

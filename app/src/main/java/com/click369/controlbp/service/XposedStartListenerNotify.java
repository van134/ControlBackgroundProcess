package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
public class XposedStartListenerNotify {
//    final static HashSet<String> mmnotifys = new HashSet<String>();
//    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam){
//        try {
////             Class actCls = XposedHelpers.findClass("android.app.Activity",lpparam.classLoader);
////            if (actCls!=null){
////                XposedHelpers.findAndHookMethod(actCls, "onResume", new XC_MethodHook() {
////                    @Override
////                    protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
////                    try {
////                        final Context context = ((Context) methodHookParam.thisObject);
////                        String cls = methodHookParam.thisObject.getClass().getName();
////                            Intent broad = new Intent("com.click369.control.test");
////                            broad.putExtra("pkg", context.getApplicationInfo().packageName);
////                            broad.putExtra("from", context.getApplicationInfo().packageName);
////                            broad.putExtra("class", cls);
////                            broad.putExtra("action", "");
////                            if(context!=null) {
////                                if (Common.SHOWDIALOGCLS.equals(cls)){
////                                    return;
////                                }
////                                context.sendBroadcast(broad);
////                            }
////                    } catch (RuntimeException e) {
////                        e.printStackTrace();
////                    }
////                    }
////                });
////            }
//            if(lpparam.packageName.equals("android")) {
//                try {
//                    final Class notifyCls = XposedHelpers.findClass("com.android.server.notification.NotificationManagerService$NotificationListeners",lpparam.classLoader);
//                    final Class managerCls = XposedHelpers.findClass("com.android.server.notification.ManagedServices",lpparam.classLoader);
//
//                    if (notifyCls!=null&&managerCls!=null){
//                        Method ms[] = notifyCls.getDeclaredMethods();
//                        Method temp1 = null,temp2 = null;
//                        for (Method mm:ms){
//                            if (temp1==null&&mm.getName().equals("notifyRemoved")){
//                                temp1 = mm;
//                            }else if (temp2==null&&mm.getName().equals("notifyPosted")){
//                                temp2 = mm;
//                            }
//                        }
//                        final Method notifyRemoved = temp1;
//                        final Method notifyPosted = temp2;
//                        if(notifyRemoved!=null){
//                            Class clss[] = notifyRemoved.getParameterTypes();
//                            XC_MethodHook hook = new XC_MethodHook() {
//                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//                                @Override
//                                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                try {
//                                    StatusBarNotification sbn = (StatusBarNotification) methodHookParam.args[1];
//                                    boolean isRemove = false;
//                                    int id = sbn.getId();
//                                    String pkg = sbn.getPackageName();
//                                    String key = pkg+"/"+id;
//                                    if (mmnotifys.contains(key)){
//                                        mmnotifys.remove(key);
//                                        isRemove = true;
//                                    }
//                                    if(isRemove){
//                                        for (String s:mmnotifys){
//                                            if (s.startsWith(pkg+"/")){
//                                                isRemove = false;
//                                            }
//                                        }
//                                    }
//                                    if (isRemove) {
//                                        Intent broad = new Intent("com.click369.control.notify");
//                                        broad.putExtra("type", "remove");
//                                        broad.putExtra("pkg", pkg);
//                                        Field cxtField = managerCls.getDeclaredField("mContext");
//                                        cxtField.setAccessible(true);
//                                        Object cxtObject = cxtField.get(methodHookParam.thisObject);
//                                        try {
//                                            ((Context) cxtObject).sendBroadcast(broad);
//                                        } catch (Throwable e) {
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyRemoved error "+e+"^^^^^^^^^^^^^^^^^");
//                                }
//                                }
//                            };
//                            if(clss!=null){
//                                XposedUtil.hookMethod(notifyCls,clss,"notifyRemoved",hook);
//                            }else{
//                                XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyRemoved null 未找到^^^^^^^^^^^^^^^^^");
//                            }
//                        }else{
//                            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyRemoved clss null 未找到^^^^^^^^^^^^^^^^^");
//                        }
//                        if(notifyPosted!=null){
//                            Class clss[] = notifyPosted.getParameterTypes();
//                            XC_MethodHook hook = new XC_MethodHook() {
//                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//                                @Override
//                                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                try {
//                                    StatusBarNotification sbn = (StatusBarNotification)methodHookParam.args[1];
//                                    int id = sbn.getId();
//                                    String pkg = sbn.getPackageName();
//                                    String key = pkg+"/"+id;
//                                    if (!mmnotifys.contains(key)){
//                                        mmnotifys.add(key);
//                                        Intent broad = new Intent("com.click369.control.notify");
//                                        broad.putExtra("type","add");
//                                        broad.putExtra("pkg", pkg);
//                                        broad.putExtra("flags", sbn.getNotification().flags);
//                                        broad.putExtra("clearable", sbn.isClearable());
//                                        Field cxtField = managerCls.getDeclaredField("mContext");
//                                        cxtField.setAccessible(true);
//                                        Object cxtObject = cxtField.get(methodHookParam.thisObject);
//                                        try {
//                                            ((Context) cxtObject).sendBroadcast(broad);
//                                        }catch (Throwable e){
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyPosted error "+e+"^^^^^^^^^^^^^^^^^");
//                                }
//                                }
//                            };
//                            if(clss!=null){
//                                XposedUtil.hookMethod(notifyCls,clss,"notifyPosted",hook);
//                            }else{
//                                XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyPosted null 未找到^^^^^^^^^^^^^^^^^");
//                            }
//
//                        }else{
//                            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify notifyPosted clss null 未找到^^^^^^^^^^^^^^^^^");
//                        }
//                    }
//                    final Class windowManagerCls = XposedHelpers.findClass("com.android.server.policy.PhoneWindowManager",lpparam.classLoader);
//                    if(windowManagerCls!=null){
//                        Class clss[] = XposedUtil.getParmsByName(windowManagerCls,"interceptKeyBeforeDispatching");
//                        if (clss!=null){
//                            XC_MethodHook hook = new XC_MethodHook() {
//                                @Override
//                                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                try {
//                                    KeyEvent event = null;
//                                    if (methodHookParam.args[1] instanceof KeyEvent){
//                                        event = (KeyEvent)methodHookParam.args[1];
//                                    }else if (methodHookParam.args[2] instanceof KeyEvent){
//                                        event = (KeyEvent)methodHookParam.args[2];
//                                    }else if (methodHookParam.args[0] instanceof KeyEvent){
//                                        event = (KeyEvent)methodHookParam.args[0];
//                                    }
////                                    AlertDialog
//                                    if (event!=null) {
//                                        int keyCode = event.getKeyCode();
//                                        if (keyCode == KeyEvent.KEYCODE_HOME&&event.getAction() == KeyEvent.ACTION_UP&&!event.isLongPress()) {
//                                            Field contextField = windowManagerCls.getDeclaredField("mContext");
//                                            contextField.setAccessible(true);
//                                            Context context = (Context) contextField.get(methodHookParam.thisObject);
//                                            Intent intent1 = new Intent("com.click369.control.keylistener");
//                                            intent1.putExtra("reason", "homekey");
//                                            context.sendBroadcast(intent1);
//                                        }
//                                    }else{
//                                        XposedBridge.log("^^^^^^^^^^^^^^goHome event null ^^^^^^^^^^^^^^^^^");
//                                    }
//                                }catch (RuntimeException e){
//                                    XposedBridge.log("^^^^^^^^^^^^^^goHome err"+e+"^^^^^^^^^^^^^^^^^");
//                                }
//                                }
//                            };
//                            XposedUtil.hookMethod(windowManagerCls,clss,"interceptKeyBeforeDispatching",hook);
//                        }else{
//                            XposedBridge.log("^^^^^^^^^^^^^^interceptKeyBeforeDispatching null 未找到^^^^^^^^^^^^^^^^^");
//                        }
//                    }
//                } catch (XposedHelpers.ClassNotFoundError e) {
//                    XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify error1 "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
//                }
//            }
//        }catch (Throwable e){
//            XposedBridge.log("^^^^^^^^^^^^^^XposedStartListenerNotify error 2"+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
//        }
//    }


}

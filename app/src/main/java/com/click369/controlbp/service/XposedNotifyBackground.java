package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.view.ViewParent;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedNotifyBackground {
//    private static HashMap<String,Integer> colorHMs = new HashMap<String,Integer>();
//    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
//        if(!lpparam.packageName.equals("com.android.systemui")){
//            return;
//        }
//        final String colors[] = {"#ff0000","#00ff00","#ffff00","#00ffff","#ff00ff","#23e2fe","#aa46ff","#7bff11",
//                "#e011ff","#fa0093","#f7fa00","#fac200","#3af1bb","#9a74f2","#fd00ad","#90014f"};
//        final Class ncvClass = XposedHelpers.findClass("com.android.systemui.statusbar.NotificationContentView", lpparam.classLoader);
////        final Class enrClass = XposedHelpers.findClass("com.android.systemui.statusbar.ExpandableNotificationRow", lpparam.classLoader);
//        final Class nbvClass = XposedHelpers.findClass("com.android.systemui.statusbar.NotificationBackgroundView", lpparam.classLoader);
//        final Class nsslClass = XposedHelpers.findClass("com.android.systemui.statusbar.stack.NotificationStackScrollLayout", lpparam.classLoader);
//        Class clss[] = XposedUtil.getParmsByName(nsslClass,"onDraw");
//        Class clss1[] = XposedUtil.getParmsByName(ncvClass,"getBackgroundColor");
////        Class clss2[] = XposedUtil.getParmsByName(enrClass,"setContentBackground");
//        Class clss3[] = XposedUtil.getParmsByName(nbvClass,"onDraw");
//        //通知滑动后背景修改
//        XC_MethodHook hook = new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                try {
////                    String c = colors[(int)(Math.random()*colors.length)];
//                    Field field = nsslClass.getDeclaredField("mBackgroundPaint");
//                    field.setAccessible(true);
//                    Paint p = (Paint) field.get(param.thisObject);
//                    p.setColor(Color.argb(150,255,255,255));
//                }catch (Throwable e){
//                    e.printStackTrace();
//                }
//            }
//        };
//        //通知背景修改
//        XposedUtil.hookMethod(nsslClass,clss,"onDraw",hook);
//         XC_MethodHook hook1 = new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                try {
//                    Field mStatusBarNotificationField = ncvClass.getDeclaredField("mStatusBarNotification");
//                    mStatusBarNotificationField.setAccessible(true);
//                    StatusBarNotification sbn = (StatusBarNotification) mStatusBarNotificationField.get(param.thisObject);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                        int color = Color.WHITE;
//                        String key = sbn.getPackageName()+sbn.getNotification().extras.get(Notification.EXTRA_TEXT);
//                        if(!colorHMs.containsKey(key)){
//                            String c = colors[(int)(Math.random()*colors.length)];
//                            color = Color.parseColor(c);
//                            colorHMs.put(key,color);
//                        }else{
//                            color = colorHMs.get(key);
//                        }
//                        param.setResult(color);
//                        return;
//                    }
//                }catch (Throwable e){
//                    e.printStackTrace();
//                }
//            }
//        };
//        XposedUtil.hookMethod(ncvClass,clss1,"getBackgroundColor",hook1);
//
//        //通知背景透明度修改
//        XC_MethodHook hook3 = new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                View v = (View)(param.thisObject);
//                v.setAlpha(0.9f);
//            }
//        };
//        XposedUtil.hookMethod(nbvClass,clss3,"onDraw",hook3);
//    }
}
package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.view.View;
import android.widget.Toast;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Stack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedMedia {
    //    private final static int MMSG_POLICY_STATUS_CHANGE = 0;
//    private final static int MMSG_FOCUS_GRANT = 1;
//    private final static int MMSG_FOCUS_LOSS = 2;
//    private final static int MMSG_MIX_STATE_UPDATE = 3;
//    private final static int MMSG_FOCUS_REQUEST = 4;
//    private final static int MMSG_FOCUS_ABANDON = 5;
    private static boolean isListener = true;
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam){
        try {
//                XposedBridge.log("==================media1==================");
            if("android".equals(lpparam.packageName)) {
//                final Class frcls = XposedHelpers.findClass("com.android.server.audio.FocusRequester", lpparam.classLoader);
//                Class clss1[] = XposedUtil.getParmsByName(frcls, "handleFocusGain");
//                Class clss2[] = XposedUtil.getParmsByName(frcls, "handleFocusLoss");
//                Class clss3[] = XposedUtil.getParmsByName(frcls, "dispatchFocusChange");
//                XposedUtil.hookMethod(frcls, clss1, "handleFocusGain", new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Object fr = param.thisObject;
//                        Field field = fr.getClass().getDeclaredField("mPackageName");
//                        field.setAccessible(true);
//                        String pkg = (String)field.get(fr);
//                        XposedBridge.log("==================handleFocusGain ----- pkg " + pkg+"  "+param.args[0]);
//                    }
//                });
//                XposedUtil.hookMethod(frcls, clss2, "handleFocusLoss", new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Object fr = param.thisObject;
//                        Field field = fr.getClass().getDeclaredField("mPackageName");
//                        field.setAccessible(true);
//                        String pkg = (String)field.get(fr);
//                        XposedBridge.log("==================handleFocusLoss ----- pkg " + pkg+"  "+param.args[0]);
//                    }
//                });
//                XposedUtil.hookMethod(frcls, clss3, "dispatchFocusChange", new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                        Object fr = param.thisObject;
//                        Field field = fr.getClass().getDeclaredField("mPackageName");
//                        field.setAccessible(true);
//                        String pkg = (String)field.get(fr);
//                        XposedBridge.log("==================dispatchFocusChange ----- pkg " + pkg+"  "+param.args[0]);
//                    }
//                });
                final Class audiocls = XposedHelpers.findClass("com.android.server.audio.MediaFocusControl", lpparam.classLoader);
                Class clss[] = XposedUtil.getParmsByName(audiocls, "abandonAudioFocus");
//                Class clss1[] = XposedUtil.getParmsByName(audiocls, "removeFocusStackEntry");
                Class clss2[] = XposedUtil.getParmsByName(audiocls, "requestAudioFocus");
//                Class clss3[] = XposedUtil.getParmsByName(audiocls, "notifyExtPolicyFocusLoss_syncAf");
                XC_MethodHook hook =  new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            if(isListener) {
                                Field mContextField = audiocls.getDeclaredField("mContext");
                                mContextField.setAccessible(true);
                                Context mContext = (Context) mContextField.get(methodHookParam.thisObject);
                                String pkg = (String) methodHookParam.args[3];


                                HashSet<String> pkgs = new HashSet<String>();
                                Field mFocusStackField = audiocls.getDeclaredField("mFocusStack");
                                mFocusStackField.setAccessible(true);
                                Stack mFocusStack = (Stack) mFocusStackField.get(methodHookParam.thisObject);
                                Iterator iterator = mFocusStack.iterator();
                                while (iterator.hasNext()){
                                    Object obj = iterator.next();
                                    Field mpField = obj.getClass().getDeclaredField("mPackageName");
                                    mpField.setAccessible(true);
                                    String p = (String)mpField.get(obj);
                                    pkgs.add(p);
//                                    XposedBridge.log("==================media3 丢失音频焦点abandonAudioFocus ----- pkg " + p);
                                }
                                Method m = audiocls.getDeclaredMethod("getCurrentAudioFocus");
                                m.setAccessible(true);
                                int state = (Integer) m.invoke(methodHookParam.thisObject);
                                Intent intent = new Intent("com.click369.control.audiofocus");
                                intent.putExtra("pkgs", pkgs);
                                intent.putExtra("pkg", pkg);
                                intent.putExtra("state", state);
                                mContext.sendBroadcast(intent);

//                                XposedBridge.log("==================media3 丢失音频焦点abandonAudioFocus  pkg " + pkg+"  "+pkgs.size()+"  "+n);
                            }
                            //取消

                        } catch (Throwable e) {
                            e.printStackTrace();
                            isListener = false;
                        }
                    }
                };
                XposedUtil.hookMethod(audiocls,clss,"abandonAudioFocus",hook);
                XC_MethodHook hook1 = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            if(isListener) {
//                                Field mContextField = audiocls.getDeclaredField("mContext");
//                                mContextField.setAccessible(true);
//                                Context mContext = (Context) mContextField.get(methodHookParam.thisObject);
                                String pkg = (String) methodHookParam.args[5];
//                                Intent intent = new Intent("com.click369.control.audiofocus");
//                                intent.putExtra("pkg", pkg);
//                                intent.putExtra("state", 1);
//                                mContext.sendBroadcast(intent);
//
//                                Field mFocusStackField = audiocls.getDeclaredField("mFocusStack");
//                                mFocusStackField.setAccessible(true);
//                                Stack mFocusStack = (Stack) mFocusStackField.get(methodHookParam.thisObject);
                                Field mContextField = audiocls.getDeclaredField("mContext");
                                mContextField.setAccessible(true);
                                Context mContext = (Context) mContextField.get(methodHookParam.thisObject);
//                                String pkg = (String) methodHookParam.args[3];


                                HashSet<String> pkgs = new HashSet<String>();
                                Field mFocusStackField = audiocls.getDeclaredField("mFocusStack");
                                mFocusStackField.setAccessible(true);
                                Stack mFocusStack = (Stack) mFocusStackField.get(methodHookParam.thisObject);
                                Iterator iterator = mFocusStack.iterator();
                                while (iterator.hasNext()){
                                    Object obj = iterator.next();
                                    Field mpField = obj.getClass().getDeclaredField("mPackageName");
                                    mpField.setAccessible(true);
                                    String p = (String)mpField.get(obj);
                                    pkgs.add(p);
//                                    XposedBridge.log("==================media3 获取音频焦点requestAudioFocus ----- pkg " + p);
                                }
                                Method m = audiocls.getDeclaredMethod("getCurrentAudioFocus");
                                m.setAccessible(true);
                                int state = (Integer) m.invoke(methodHookParam.thisObject);
                                Intent intent = new Intent("com.click369.control.audiofocus");
                                intent.putExtra("pkgs",pkgs);
                                intent.putExtra("pkg", pkg);
                                intent.putExtra("state", state);
                                mContext.sendBroadcast(intent);
                                //isMusicActiveRemotely
//                                XposedBridge.log("==================media3 获取音频焦点  pkg " + pkg+"  "+pkgs.size()+"  "+n);
                            }
                            //请求
//                          XposedBridge.log("==================media3  requestAudioFocus id " + methodHookParam.args[4]+ " pkg  " + methodHookParam.args[5]);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            isListener = false;
                        }
                    }
                };
                XposedUtil.hookMethod(audiocls,clss2,"requestAudioFocus",hook1);
////                XposedHelpers.findAndHookMethod(audiocls, "requestAudioFocus", clss2[0], clss2[1], clss2[2], clss2[3], clss2[4], clss2[5], clss2[6], clss2[7], );
////                XposedHelpers.findAndHookMethod(audiocls, "removeFocusStackEntry", clss1[0], clss1[1], clss1[2], new XC_MethodHook() {
////                    @Override
////                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
////                        try {
////                            if(isListener) {
////                                Field mContextField = audiocls.getDeclaredField("mContext");
////                                mContextField.setAccessible(true);
////                                Context mContext = (Context) mContextField.get(methodHookParam.thisObject);
//////                                Object afi =  methodHookParam.args[0];
//////                                Field field = afi.getClass().getDeclaredField("mPackageName");
//////                                field.setAccessible(true);
//////                                String pkg = (String)field.get(afi);
////                                String pkg = (String)methodHookParam.args[0];
////                                Intent intent = new Intent("com.click369.control.audiofocus");
////                                intent.putExtra("pkg", pkg);
////                                intent.putExtra("state", 1);
////                                mContext.sendBroadcast(intent);
////
////                                Field mFocusStackField = audiocls.getDeclaredField("mFocusStack");
////                                mFocusStackField.setAccessible(true);
////                                Stack mFocusStack = (Stack) mFocusStackField.get(methodHookParam.thisObject);
////                                XposedBridge.log("==================media3 丢失音频焦点removeFocusStackEntry  pkg " + pkg+"  "+mFocusStack.size());
////                            }
////                            //请求
//////                          XposedBridge.log("==================media3  requestAudioFocus id " + methodHookParam.args[4]+ " pkg  " + methodHookParam.args[5]);
////                        } catch (RuntimeException e) {
////                            e.printStackTrace();
////                            isListener = false;
////                        }
////                    }
////                });
//                XposedHelpers.findAndHookMethod(audiocls, "notifyExtPolicyFocusLoss_syncAf", clss3[0], clss3[1], new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        try {
//                            if(isListener) {
//                                Field mContextField = audiocls.getDeclaredField("mContext");
//                                mContextField.setAccessible(true);
//                                Context mContext = (Context) mContextField.get(methodHookParam.thisObject);
//
//                                Object afi =  methodHookParam.args[0];
//                                Field field = afi.getClass().getDeclaredField("mPackageName");
//                                field.setAccessible(true);
//                                String pkg = (String)field.get(afi);
//                                HashSet<String> pkgs = new HashSet<String>();
//                                Field mFocusStackField = audiocls.getDeclaredField("mFocusStack");
//                                mFocusStackField.setAccessible(true);
//                                Stack mFocusStack = (Stack) mFocusStackField.get(methodHookParam.thisObject);
//                                Iterator iterator = mFocusStack.iterator();
//                                while (iterator.hasNext()){
//                                    Object obj = iterator.next();
//                                    Field mpField = obj.getClass().getDeclaredField("mPackageName");
//                                    mpField.setAccessible(true);
//                                    String p = (String)mpField.get(obj);
//                                    pkgs.add(p);
//                                    XposedBridge.log("==================media3 丢失音频焦点notifyExtPolicyFocusLoss_syncAf ----- pkg " + p);
//                                }
//
//                                Intent intent = new Intent("com.click369.control.audiofocus");
//                                intent.putExtra("pkgs",pkgs);
//                                intent.putExtra("pkg", pkg);
//                                intent.putExtra("state", 0);
//                                mContext.sendBroadcast(intent);
//                                XposedBridge.log("==================media3 丢失音频焦点notifyExtPolicyFocusLoss_syncAf  pkg " + pkg+"  "+pkgs.size());
//                            }
//                            //请求
////                          XposedBridge.log("==================media3  requestAudioFocus id " + methodHookParam.args[4]+ " pkg  " + methodHookParam.args[5]);
//                        } catch (RuntimeException e) {
//                            e.printStackTrace();
//                            isListener = false;
//                        }
//                    }
//                });
////              XposedBridge.log("==================media2==================");
            }
        }catch (Throwable e){
            e.printStackTrace();
            isListener = false;
        }
    }
}
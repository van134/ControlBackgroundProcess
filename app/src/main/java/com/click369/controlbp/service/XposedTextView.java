package com.click369.controlbp.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedTextView {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences tvPrefs){
        try {
//            Class filecls = XposedHelpers.findClass("java.io.File",lpparam.classLoader);
//            Class filesyscls = XposedHelpers.findClass("java.io.FileSystem",lpparam.classLoader);
//            final Field fsField = XposedHelpers.findFirstFieldByExactType(filecls,filesyscls);
//            XposedHelpers.findAndHookMethod(filecls, "list", new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                    try {
//                        SecurityManager security = System.getSecurityManager();
//                        File f = (File)methodHookParam.thisObject;
//                        Field pathField = f.getClass().getDeclaredField("path");
////                    Field pathField = f.getClass().getDeclaredField("fs");
//                        Method isInvalidMethod = f.getClass().getDeclaredMethod("isInvalid");
//                        if (security != null) {
//                            pathField.setAccessible(true);
//                            String path = (String)pathField.get(f);
//                            security.checkRead(path);
//                        }
//                        isInvalidMethod.setAccessible(true);
//                        Boolean isOk = (Boolean) isInvalidMethod.invoke(f);
//                        if (isOk) {
//                            methodHookParam.setResult(null);
//                            return;
//                        }
//                        fsField.setAccessible(true);
//                        Object fsObject= fsField.get(f);
//                        Method listMethod = fsObject.getClass().getDeclaredMethod("list",File.class);
//                        listMethod.setAccessible(true);
//                        String lists[] = (String[])listMethod.invoke(fsObject,f);
//                        if(lists!=null&&lists.length>0){
//                            String p = Environment.getExternalStorageDirectory()+File.separator+"xunlei";
//                            for(String s:lists){
//                                if (s.equals(p)){
//                                    s = Environment.getExternalStorageDirectory()+File.separator+"xxxxunlei";
//                                }
//                            }
//                            methodHookParam.setResult(lists);
//                            return;
//                        }
//                    }catch (RuntimeException e){
//                        e.printStackTrace();
//                    }
//                }
//            });
            if ("android".equals(lpparam.packageName) ||
                    "com.android.phone".equals(lpparam.packageName) ||
                    "com.android.systemui".equals(lpparam.packageName)) {
                return;
            }
            tvPrefs.reload();
            if(tvPrefs.contains(lpparam.packageName)){
                Class tvcls = XposedHelpers.findClass("android.widget.TextView",lpparam.classLoader);
                Class canvascls = XposedHelpers.findClass("android.graphics.Canvas",lpparam.classLoader);
                XposedHelpers.findAndHookMethod(canvascls,"drawText",char[].class, int.class,int.class,float.class,float.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        char datas[] = (char[])methodHookParam.args[0];
                        if(datas!=null){
                            String tet = new String(datas);
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr.toCharArray();
                                methodHookParam.args[2] = replaceStr.length();
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(canvascls,"drawText",String.class, int.class,int.class,float.class,float.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        String tet= (String)methodHookParam.args[0];
                        if(tet!=null){
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr;
                                methodHookParam.args[2] = replaceStr.length();
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(canvascls,"drawText",CharSequence.class, int.class,int.class,float.class,float.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        CharSequence data = (CharSequence) methodHookParam.args[0];
                        if (data != null) {
                            String tet = data.toString();
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr;
                                methodHookParam.args[2] = replaceStr.length();
                            }
                        }
                    }
                });

                XposedHelpers.findAndHookMethod(canvascls,"drawText",String.class,float.class,float.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        String tet= (String)methodHookParam.args[0];
                        if(tet!=null){
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr;
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(canvascls,"drawTextOnPath",char[].class,int.class,int.class,Path.class,float.class,float.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        char datas[] = (char[])methodHookParam.args[0];
                        if(datas!=null){
                            String tet = new String(datas);
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr.toCharArray();
                                methodHookParam.args[1] = replaceStr.length();
                                methodHookParam.args[2] = replaceStr.length();
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(canvascls,"drawTextOnPath",String.class,Path.class,float.class,float.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        String tet= (String)methodHookParam.args[0];
                        if(tet!=null){
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr;
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(canvascls,"drawTextRun",char[].class,int.class,int.class,int.class,int.class,float.class,float.class,boolean.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        char datas[] = (char[])methodHookParam.args[0];
                        if(datas!=null){
                            String tet = new String(datas);
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr.toCharArray();
                                methodHookParam.args[2] = replaceStr.length();
                                methodHookParam.args[4] = replaceStr.length();
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(canvascls,"drawTextRun",CharSequence.class,int.class,int.class,int.class,int.class,float.class,float.class,boolean.class,Paint.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        CharSequence data = (CharSequence) methodHookParam.args[0];
                        if (data != null) {
                            String tet = data.toString();
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr;
                                methodHookParam.args[2] = replaceStr.length();
                                methodHookParam.args[4] = replaceStr.length();
                            }
                        }
                    }
                });

                XposedHelpers.findAndHookConstructor(tvcls,Context.class, AttributeSet.class,int.class,int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        final TextView tv = (TextView) methodHookParam.thisObject;
                        if (tv.getText() != null) {
                            String tet = tv.getText().toString();
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                tv.setText(replaceStr);
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(tvcls,"setText",CharSequence.class ,new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        if(methodHookParam.args[0]!=null){
                            String tet = methodHookParam.args[0].toString();
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr;
                            }
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(tvcls,"setText",CharSequence.class, TextView.BufferType.class ,new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        final TextView tv = (TextView)methodHookParam.thisObject;
                        if(methodHookParam.args[0]!=null){
                            String tet = methodHookParam.args[0].toString();
                            String replaceStr = getText(tvPrefs,lpparam.packageName,tet);
                            if(replaceStr!=null){
                                methodHookParam.args[0] = replaceStr;
                            }
                        }
                    }
                });
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }

    public static String getText(XSharedPreferences tvPrefs,String pkg,String text){
        String s = tvPrefs.getString(pkg+"/"+text,null);
        return s;
    }
}
package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedAD {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences adPrefs){
        try {
            if ("android".equals(lpparam.packageName) ||
                    "com.android.phone".equals(lpparam.packageName) ||
                    "com.android.systemui".equals(lpparam.packageName)) {
                return;
            }
            adPrefs.reload();
            int type = adPrefs.getInt(lpparam.packageName + "/ad",0);
            if (type == 0){
                return;
            }
            Class actCls = XposedHelpers.findClass("android.app.Activity",lpparam.classLoader);
//            if (adPrefs.contains(lpparam.packageName + "/one")&&adPrefs.contains(lpparam.packageName + "/two")) {
            if (actCls!=null) {
//                Class actCls = XposedHelpers.findClass(adPrefs.getString(lpparam.packageName + "/one",""), lpparam.classLoader);
                if(type!=3){
//                    XposedBridge.log("++++++++++++++1"+lpparam.packageName+"  oneclass "+actCls);
                    XposedHelpers.findAndHookMethod(actCls, "onCreate", Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                final Activity act = (Activity) (methodHookParam.thisObject);
                                int type = adPrefs.getInt(lpparam.packageName+"/ad",0);
                                Class nextCls = null;
                                if(type == 1){//从第一个跳第二个
                                    if (adPrefs.getString(lpparam.packageName + "/one","").equals(act.getClass().getName())) {
                                        String three = adPrefs.getString(lpparam.packageName + "/two", "");
                                        if(three.length()>0){
                                            nextCls = XposedHelpers.findClass(three, lpparam.classLoader);
//                                            XposedBridge.log("++++++++++++++1"+lpparam.packageName+"从第一个跳第二个"+nextCls);
                                        }
                                    }else if(adPrefs.getString(lpparam.packageName + "/two","").equals(act.getClass().getName())){
                                        if(act.getClass().getName().toLowerCase().contains(".ad")){
                                            act.finish();
                                        }
                                    }else if(adPrefs.getString(lpparam.packageName + "/three","").equals(act.getClass().getName())){
                                        if(act.getClass().getName().toLowerCase().contains(".ad")){
                                            act.finish();
                                        }
                                    }
                                }else if(type == 2){//从第二个跳第三个
                                    if (adPrefs.getString(lpparam.packageName + "/two","").equals(act.getClass().getName())) {
                                        String three = adPrefs.getString(lpparam.packageName + "/three", "");
                                        if(three.length()>0){
                                            nextCls = XposedHelpers.findClass(three, lpparam.classLoader);
//                                            XposedBridge.log("++++++++++++++1"+lpparam.packageName+"从第二个跳第三个"+nextCls);
                                        }
                                    }else if(adPrefs.getString(lpparam.packageName + "/three","").equals(act.getClass().getName())){
                                        if(act.getClass().getName().toLowerCase().contains(".ad")){
                                            act.finish();
                                        }
                                    }
                                }
//                                else if(type == 3){//从第一个跳第三个
//                                    if (adPrefs.getString(lpparam.packageName + "/one","").equals(act.getClass().getName())) {
//                                        String three = adPrefs.getString(lpparam.packageName + "/three", "");
//                                        if(three.length()>0){
//                                            nextCls = XposedHelpers.findClass(three, lpparam.classLoader);
////                                            XposedBridge.log("++++++++++++++1"+lpparam.packageName+"从第一个跳第三个"+nextCls);
//                                        }
//                                    }else if(adPrefs.getString(lpparam.packageName + "/two","").equals(act.getClass().getName())){
//                                        if(act.getClass().getName().toLowerCase().contains(".ad")){
//                                            act.finish();
//                                        }
//                                    }else if(adPrefs.getString(lpparam.packageName + "/three","").equals(act.getClass().getName())){
//                                        if(act.getClass().getName().toLowerCase().contains(".ad")){
//                                            act.finish();
//                                        }
//                                    }
//                                }
                                if (nextCls != null) {
                                    Intent intent = new Intent(act, nextCls);
                                    act.startActivity(intent);
                                    act.finish();
                                }
//                                if (adPrefs.getString(lpparam.packageName + "/one","").equals(act.getClass().getName())) {
//                                    Class nextCls = XposedHelpers.findClass(adPrefs.getString(lpparam.packageName + "/two", ""), lpparam.classLoader);
//                                    if (nextCls != null) {
////                                        XposedBridge.log("++++++++++++++2" + lpparam.packageName + "  twoclass " + nextCls);
//                                        Intent intent = new Intent(act, nextCls);
//                                        act.startActivity(intent);
//                                        act.finish();
//                                    }
//                                }
                            }catch (RuntimeException e){
                                e.printStackTrace();
                            }
                        }
                    });
                }else if (type==3){

                    Class tvcls = XposedHelpers.findClass("android.widget.TextView",lpparam.classLoader);
                    if(tvcls!=null){
                        XposedHelpers.findAndHookConstructor(tvcls,Context.class, AttributeSet.class,int.class,int.class, new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                final TextView tv = (TextView)methodHookParam.thisObject;
//                                XposedBridge.log("++++++++++++++TEXTVIEW11" + lpparam.packageName + " " + tv.getText());
                                String tet = tv.getText().toString();
                                if((tet.contains("跳过")||tet.toLowerCase().contains("skip"))&&tet.length()<8||tet.length()==0){
                                    Runnable r = new Runnable() {
                                        @Override
                                        public void run() {
                                            String tet1 = tv.getText().toString();
                                            if(!tet1.contains("跳过")&&!tet1.toLowerCase().contains("skip")){
                                                return;
                                            }
                                            tv.performClick();
                                            ViewParent vp = tv.getParent();
                                            if(vp!=null){
                                                View v = ((View)vp);
                                                v.performClick();
//                                                ViewParent vpp = v.getParent();
//                                                XposedBridge.log("++++++++++++++TEXTVIEW22" + lpparam.packageName + " " +  v.getPivotX()+"  "+v.getWidth());
//                                                if(vpp!=null){
//                                                    View v1= ((View)vpp);
//                                                    XposedBridge.log("++++++++++++++TEXTVIEW33" + lpparam.packageName + " " +  v.getPivotX()+"  "+v1.getWidth());
//                                                    v1.performClick();
////                                                        ((View)vpp).setVisibility(View.GONE);
//                                                }
//                                                    ((View)vp).setVisibility(View.GONE);

                                            }
                                        }
                                    };
                                    if (tet.length() == 0){
                                        tv.postDelayed(r,100);
                                    }else{
                                        tv.postDelayed(r,500);
                                    }
                                }
                            }
                        });
                    }
                }
                try {
                    Class dialogcls = XposedHelpers.findClass("android.app.Dialog",lpparam.classLoader);
                    XposedHelpers.findAndHookMethod(dialogcls, "show", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                if(methodHookParam.thisObject.getClass().getName().toLowerCase().contains(".ad")){
                                    methodHookParam.setResult(null);
                                    return;
                                }
                            }catch (RuntimeException e){
                                e.printStackTrace();
                            }
                        }
                    });
                }catch (RuntimeException e){
                    e.printStackTrace();
                }
            }
            XposedHelpers.findAndHookMethod(actCls, "startActivityForResult", Intent.class, int.class, Bundle.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
//                new Thread(){
//                    @Override
//                    public void run() {
                        try {
                            Intent intent = (Intent)methodHookParam.args[0];
                            String pkg = intent.getComponent()==null?null:intent.getComponent().getPackageName();
                            int type = adPrefs.getInt(pkg+"/ad",0);
                            if (type!=0){
                                Context cxt= ((Context) methodHookParam.thisObject);
                                String cls = methodHookParam.thisObject.getClass().getName();
                                if (!"com.android.webview".equals(cls)){
                                    if(type==1&&!adPrefs.contains(pkg+"/two")){
                                        adPrefs.reload();
                                        if(adPrefs.getString(pkg+"/one","").equals(cls)){
                                            Intent broad = new Intent("com.click369.control.ad");
                                            String mcls = pkg==null?null:intent.getComponent().getClassName();
                                            broad.putExtra("pkg",pkg);
                                            broad.putExtra("two",mcls);
                                            broad.putExtra("three","");
                                            cxt.sendBroadcast(broad);
//                                    XposedBridge.log("++++++++++++++0"+pkg+"采集到第二个"+mcls);
                                        }
                                    }else if(type==2&&!adPrefs.contains(pkg+"/three")){
                                        adPrefs.reload();
                                        if(adPrefs.getString(pkg+"/two","").equals(cls)){
                                            Intent broad = new Intent("com.click369.control.ad");
                                            String mcls = pkg==null?null:intent.getComponent().getClassName();
                                            broad.putExtra("pkg",pkg);
                                            broad.putExtra("two","");
                                            broad.putExtra("three",mcls);
                                            cxt.sendBroadcast(broad);
//                                    XposedBridge.log("++++++++++++++0"+pkg+"采集到第三个"+mcls);
                                        }
                                    }
                                }
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
//                    }
//                }.start();

                }
            });

        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }
}
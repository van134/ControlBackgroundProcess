package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.app.Fragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.PackageUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedToast {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences barPrefs){
        try {
            barPrefs.reload();
            if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOASTCHANGE,false)&&
                    !lpparam.packageName.equals("com.cyberlink.photodirector")&&
                    !lpparam.packageName.equals("com.android.phone")){
                final Class toastCls = XposedHelpers.findClass("android.widget.Toast", lpparam.classLoader);
                XposedHelpers.findAndHookConstructor(toastCls, Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            Context context = (Context)methodHookParam.args[0];
                            Toast result = (Toast)methodHookParam.thisObject;
                            int dur = Toast.LENGTH_LONG;
                            barPrefs.reload();
                            int grivity = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTGRIVITY,Gravity.BOTTOM);
                            int bgcolor = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTBGCOLOR,Color.BLACK);
                            int textclor = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTTEXTCOLOR,Color.WHITE);
                            int postion = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTPOSTION,0);
                            makeToast(result,context,"",dur,grivity,bgcolor,textclor,postion);
                        }catch (Throwable e){
                           XposedBridge.log("修改Toast失败1 "+e);
                        }

                    }
                });
                XposedHelpers.findAndHookMethod(toastCls, "makeText", Context.class,CharSequence.class,int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try {
                            Context context = (Context)methodHookParam.args[0];
                            CharSequence text = (CharSequence)methodHookParam.args[1];
                            int dur = (Integer) methodHookParam.args[2];
                            Toast t = new Toast(context);
                            View v = t.getView();
                            if(v!=null){
                                View textView =  v.findViewWithTag(10);
                                if(textView!=null){
                                    ((TextView)textView).setText(PackageUtil.getAppNameByPkg(context,context.getPackageName())+":"+text);
                                }
                                t.setDuration(dur==1?Toast.LENGTH_LONG:Toast.LENGTH_SHORT);
                                methodHookParam.setResult(t);
                                return;
                            }
                        }catch (Throwable e){
                            XposedBridge.log("修改Toast失败2 "+e);
                        }
                    }
                });
                XposedHelpers.findAndHookMethod(toastCls, "setText", CharSequence.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try{
                           Toast t  = (Toast)methodHookParam.thisObject;
                           View v = t.getView();
                            if(v!=null){
                               View textView =  v.findViewWithTag(10);
                                if(textView!=null){
                                    ((TextView)textView).setText(PackageUtil.getAppNameByPkg(v.getContext(),v.getContext().getPackageName())+":"+(CharSequence) methodHookParam.args[0]);
                                }
                                methodHookParam.setResult(null);
                                return;
                            }
                        }catch (Throwable e){
                            XposedBridge.log("修改Toast失败3 "+e);
                        }
                    }
                });
//                XposedHelpers.findAndHookMethod(toastCls, "setView", View.class, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        return;
//                    }
//                });
         }
        }catch (RuntimeException e){
            XposedBridge.log("^^^^^^^^^^^^^^toast error "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
        }
    }


    public static Toast makeToast(Context context, CharSequence text, int dur,int grivity,int bgColor,int textColor,int postion){
        Toast result = new Toast(context);
        return makeToast(result,context,text,dur,grivity,bgColor,textColor,postion);
    }

    public static Toast makeToast(Toast result,Context context, CharSequence text, int dur,int grivity,int bgColor,int textColor,int position){
        try {
            FrameLayout mainFl = new FrameLayout(context);
            mainFl.setBackgroundColor(bgColor);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
            mainFl.setLayoutParams(params);
            TextView tv = new TextView(context);
            tv.setTag(10);
            tv.setText(PackageUtil.getAppNameByPkg(context,context.getPackageName())+":"+text);
            tv.setTextColor(textColor);
            WindowManager mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
            Point p = new Point();
            Display d = mWindowManager.getDefaultDisplay();//.getRealSize(p);
            tv.setGravity(Gravity.CENTER);
            mainFl.addView(tv,new FrameLayout.LayoutParams(d.getWidth()+20, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            PackageManager pm  =context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            Drawable drawable = pi.applicationInfo.loadIcon(pm);
            ImageView img = new ImageView(context);
            img.setImageDrawable(drawable);
            mainFl.setPadding(d.getWidth()/80,d.getWidth()/80,d.getWidth()/80,d.getWidth()/80);
            mainFl.addView(img,new FrameLayout.LayoutParams(d.getHeight()/24, d.getHeight()/24, Gravity.LEFT|Gravity.CENTER));
            tv.setPadding(d.getHeight()/20,0,d.getHeight()/20,0);
            result.setView(mainFl);
            Field f = Toast.class.getDeclaredField("mNextView");
            f.setAccessible(true);
            f.set(result,mainFl);
            result.setGravity(grivity,0,position);
            result.setDuration(dur==1?Toast.LENGTH_LONG:Toast.LENGTH_SHORT);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return  result;
    }

}
/*
*
* Class fragmentCls = XposedHelpers.findClassIfExists("android.support.v4.app.Fragment",lpparam.classLoader);
                if(fragmentCls!=null){
                    XposedHelpers.findAndHookMethod(fragmentCls, "onHiddenChanged", boolean.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            boolean isHidden = (Boolean)(methodHookParam.args[0]);
                            Fragment fragment = (Fragment)methodHookParam.thisObject;
                            Activity act= fragment.getActivity();
                            if(!isHidden){
                                changeBar(settings,methodHookParam,lpparam,act);
                                if(lpparam.packageName.startsWith("com.cool")){
                                    XposedBridge.log("^^^^^^^^^^^^^^fragment onHiddenChanged "+lpparam.packageName+"  "+act.getClass().getName()+"^^^^^^^^^^^^^^^^^");
                                }
                            }
                        }
                    });
                    XposedHelpers.findAndHookMethod(fragmentCls, "onCreateView", LayoutInflater.class,ViewGroup.class,Bundle.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            Fragment fragment = (Fragment)methodHookParam.thisObject;
                            Activity act= fragment.getActivity();
                            changeBar(settings,methodHookParam,lpparam,act);
                            if(lpparam.packageName.startsWith("com.cool")){
                                XposedBridge.log("^^^^^^^^^^^^^^fragment onCreateView "+lpparam.packageName+"  "+act.getClass().getName()+"^^^^^^^^^^^^^^^^^");
                            }
                        }
                    });
                }
* */
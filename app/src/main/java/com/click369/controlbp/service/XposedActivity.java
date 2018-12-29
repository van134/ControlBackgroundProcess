package com.click369.controlbp.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;

import com.click369.controlbp.activity.AppConfigActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedActivity {
    private static long downtime = 0;
//    private static long sddowntime = 0;
    private static Activity act = null;
    private static Handler handler = new Handler();
    private static boolean isDown = false;
    private static Runnable r = new Runnable() {
        @Override
        public void run() {
            if(act!=null&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1&&!act.isDestroyed())){
                Intent intent1 = new Intent("com.click369.controlbp.zhendong");
                act.sendBroadcast(intent1);
                Intent intent = new Intent("com.click369.controlbp.activity.AppConfigActivity");
                intent.putExtra("pkg",act.getPackageName());
                intent.putExtra("from","nowapp");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(intent);
            }
        }
    };
    private static Runnable rsd = new Runnable() {
        @Override
        public void run() {
            if(act!=null&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1&&!act.isDestroyed())){
                Intent intent1 = new Intent("com.click369.control.sysui.changeflash");
                act.sendBroadcast(intent1);
            }
        }
    };
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final boolean isListener,final int w,final int h){
        try {
            final Class actcls = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
            Class clss[] = XposedUtil.getParmsByName(actcls, "dispatchTouchEvent");
            XC_MethodHook hook =  new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        if(isListener) {
                            MotionEvent me =  (MotionEvent)methodHookParam.args[0];
                            float x = me.getX();
                            float y = me.getY();
                            int width = w;
                            int height = h;
                            act = (Activity)methodHookParam.thisObject;
                            if(width==0||height==0){
                                DisplayMetrics dm = new DisplayMetrics();
                                act.getWindowManager().getDefaultDisplay().getMetrics(dm);
                                height = dm.heightPixels;
                                width = dm.widthPixels;
                            }
                            int action = me.getAction();
//                            XposedBridge.log(act.getPackageName()+" x "+x+"  y "+y+"  "+me.getAction());
                            if(x>(width*3/7)&&x<(width*4/7)&&y<(height*1/10)&&(action==MotionEvent.ACTION_DOWN||isDown==true)) {
                                Configuration config = act.getResources().getConfiguration();
                                // 如果当前是横屏
                                if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
                                    switch (me.getAction()) {
                                        case MotionEvent.ACTION_DOWN:
                                            isDown = true;
                                            handler.postDelayed(r,600);
                                            downtime = System.currentTimeMillis();
                                            break;
                                        case MotionEvent.ACTION_UP:
                                            handler.removeCallbacks(r);
                                            if(System.currentTimeMillis()-downtime>=600&&isDown){
                                                isDown = false;
                                                methodHookParam.setResult(true);
                                                return;
                                            }
                                            break;
                                        case MotionEvent.ACTION_CANCEL:
                                        case MotionEvent.ACTION_OUTSIDE:
                                            handler.removeCallbacks(r);
                                            isDown = false;
                                            break;
                                    }
                                }
                            }else if(x>(width*22/23)&&y<(height*1/8)&&(action==MotionEvent.ACTION_DOWN||isDown==true)) {
                                Configuration config = act.getResources().getConfiguration();
                                // 如果当前是横屏
                                if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
                                    File f = new File(Environment.getExternalStorageDirectory(),"zroms");
                                    if(f.exists()){
                                        switch (me.getAction()) {
                                            case MotionEvent.ACTION_DOWN:
                                                isDown = true;
                                                handler.postDelayed(rsd,400);
//                                                sddowntime = System.currentTimeMillis();
                                                methodHookParam.setResult(true);
                                                return;
                                            case MotionEvent.ACTION_UP:
                                                handler.removeCallbacks(rsd);
                                                if(isDown){
                                                    isDown = false;
                                                    methodHookParam.setResult(true);
                                                    return;
                                                }
                                                break;
                                            case MotionEvent.ACTION_CANCEL:
                                            case MotionEvent.ACTION_OUTSIDE:
                                                handler.removeCallbacks(rsd);
                                                isDown = false;
                                                break;
                                        }
                                    }
                                }
                            }else if(isDown){
                                handler.removeCallbacks(rsd);
                                handler.removeCallbacks(r);
                                isDown = false;
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            };
            XposedUtil.hookMethod(actcls,clss,"dispatchTouchEvent",hook);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
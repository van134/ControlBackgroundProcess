package com.click369.controlbp.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.click369.controlbp.activity.AppConfigActivity;
import com.click369.controlbp.common.Common;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
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
    private static Handler handler;
    private static boolean isDown = false;
    private static BroadcastReceiver broadcastReceiver;
    private static Runnable r = new Runnable() {
        @Override
        public void run() {
            try {
                if(act!=null&&(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1&&!act.isDestroyed())){
//                    Toast.makeText(act,"打开控制面板",Toast.LENGTH_LONG).show();

                    Intent intent1 = new Intent("com.click369.controlbp.zhendong");
                    act.sendBroadcast(intent1);

                    Intent intent = new Intent("com.click369.controlbp.activity.AppConfigActivity");
                    intent.putExtra("pkg",act.getPackageName());
                    intent.putExtra("from","nowapp");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    act.startActivity(intent);
                }
            }catch (Exception e){
                e.printStackTrace();
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
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam, final boolean isGetCodetoEdit,final boolean isListener, final int w, final int h){
        try {
            final Class actcls = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);

            XC_MethodHook hook =  new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        if(isListener) {
                            if(handler==null){
                                handler = new Handler();
                            }
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
            if(isListener){
                XposedUtil.hookMethod(actcls,XposedUtil.getParmsByName(actcls, "dispatchTouchEvent"),"dispatchTouchEvent",hook);
            }
            if(isGetCodetoEdit){
                XposedUtil.hookMethod(actcls, XposedUtil.getParmsByName(actcls, "onStart"), "onStart", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if(isGetCodetoEdit){
                            try{
                                final Activity activity = (Activity) param.thisObject;
                                broadcastReceiver = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        String code = intent.getStringExtra("code");
                                        if(!TextUtils.isEmpty(code)){
                                            ViewGroup doc = (ViewGroup)activity.getWindow().getDecorView();
                                            if (doc.isShown()) {
                                                ArrayList<View> views = new ArrayList<View>();
                                                doc.findViewsWithText(views, "edittext", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);
                                                boolean isOk = false;
                                                for (View v : views) {
                                                    if ((v instanceof EditText) && v.isShown() && TextUtils.isEmpty(((EditText) v).getText())) {
                                                        ((EditText) v).setText(code);
                                                        isOk = true;
                                                        break;
                                                    }
                                                }
                                                if(!isOk){
                                                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                    // 创建普通字符型ClipData
                                                    ClipData mClipData = ClipData.newPlainText("Label", code);
                                                    // 将ClipData内容放到系统剪贴板里。
                                                    cm.setPrimaryClip(mClipData);
                                                    Toast.makeText(context, "未找到文本框,验证码已复制到粘贴板", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }
                                };
                                IntentFilter intentFilter = new IntentFilter("com.click369.control.setmsgcode");
                                activity.registerReceiver(broadcastReceiver,intentFilter);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
                XposedUtil.hookMethod(actcls,XposedUtil.getParmsByName(actcls, "onPause"),"onPause",new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if(isGetCodetoEdit) {
                            try {
                                Activity activity = (Activity) param.thisObject;
                                if (broadcastReceiver != null) {
                                    activity.unregisterReceiver(broadcastReceiver);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                });
                final Class edcls = XposedHelpers.findClass("android.widget.EditText", lpparam.classLoader);
                XposedUtil.hookConstructorMethod(edcls,new Class[]{Context.class, AttributeSet.class, int.class, int.class},new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if(isGetCodetoEdit) {
                            EditText editText = (EditText) param.thisObject;
                            editText.setContentDescription("edittext");
                        }
                    }
                });
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
//    private static void setMsgCode(ViewGroup doc,String text){
//        if(!doc.isShown()){
//            return;
//        }
//        int count = doc.getChildCount();
//        for(int i = 0;i<count;i++){
//            View v = doc.getChildAt(i);
//            if(v.isShown()){
//                if((v instanceof EditText)&&v.isShown()&& TextUtils.isEmpty(((EditText) v).getText())){
//                    ((EditText) v).setText(text);
//                    break;
//                }else if(v instanceof ViewGroup){
//                    setMsgCode(doc,text);
//                }
//            }
//
////            XposedBridge.log(lpparam.packageName+"  "+activity.getClass().getSimpleName()+"  "+v.getClass().getName());
//        }
//    }
}
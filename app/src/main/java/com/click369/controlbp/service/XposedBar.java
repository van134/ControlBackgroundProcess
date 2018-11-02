package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AndroidRuntimeException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.adapter.IFWCompActBroadAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.FileUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedBar {
    private static boolean isQianRGB(int r,int g,int b){
        int grayLevel = (int) (r * 0.299 + g * 0.587 + b* 0.114);
        if(grayLevel>=200){
            return true;
        }
        return false;
    }
//    private static boolean isQianRGB(int r,int g,int b){
//        return r>175&&g>175&&b>175;
//    }
    public static void setDarkStatusIcon(Window window,boolean bDark) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            View decorView = window.getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                if(bDark){
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                } else{
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }
    }
//    public static void setDarkStatusIcon(Activity act,boolean bDark) {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            View decorView = act.getWindow().getDecorView();
//            if(decorView != null){
//                if(bDark){
//                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//                } else{
//                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
//                }
//            }
//        }
//    }
    public static void setDarkNavIcon(Window window,boolean bDark) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            View decorView = window.getDecorView();
            if(decorView != null){
                int vis = decorView.getSystemUiVisibility();
                if(bDark){
                    vis |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                } else{
                    vis &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
                decorView.setSystemUiVisibility(vis);
            }
        }
    }
    public static  void setColor(final XC_LoadPackage.LoadPackageParam lpparam,final Activity act,final XSharedPreferences barPrefs,final XSharedPreferences colorTestPrefs,final Window window,final int colortop,final int colorbottom){
        if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOPBAR, false)
                &&!colorTestPrefs.getBoolean(lpparam.packageName +"/nottopbar",false)) {
//            final int red = (colortop & 0xff0000) >> 16;
//            final int green = (colortop & 0x00ff00) >> 8;
//            final int blue = (colortop & 0x0000ff);
            act.runOnUiThread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    try {
//                        setDarkStatusIcon(window,true);
                        window.setStatusBarColor(colortop);

//                        setDarkStatusIcon(window, isQianRGB(red,green,blue) || colorTestPrefs.contains(lpparam.packageName + "/dark"));
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR, false)) {
            act.runOnUiThread(new Runnable() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                try {
//                    int bottom = 0;
                    if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP, false)) {
                        window.setNavigationBarColor(colortop);
//                        bottom = colortop;
                    } else {
                        window.setNavigationBarColor(colorbottom);
//                        bottom = colorbottom;
                    }
//                    final int red = (bottom & 0xff0000) >> 16;
//                    final int green = (bottom & 0x00ff00) >> 8;
//                    final int blue = (bottom & 0x0000ff);
//                    setDarkNavIcon(window,isQianRGB(red,green,blue));
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                }
            });
        }
    }

    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences barPrefs,final XSharedPreferences colorTestPrefs,final XSharedPreferences settingPrefs){
        try {
            try {
                if(lpparam.packageName.equals("com.android.systemui")) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOPBAR, false)) {
                            Class barBackClass = XposedHelpers.findClass("com.android.systemui.statusbar.phone.BarTransitions$BarBackgroundDrawable", lpparam.classLoader);
                            if (barBackClass != null) {
                                try {
                                    Constructor ms[] = barBackClass.getDeclaredConstructors();
                                    Class clss[] = null;
//                                    XposedBridge.log("++++++++++++++HOOK  ms " + ms);
//                                    XposedBridge.log("++++++++++++++HOOK  ms.length" + ms.length);
                                    for (Constructor m : ms) {
                                        if (m.getParameterTypes()!=null) {
                                            clss = m.getParameterTypes();
                                            break;
                                        }
                                    }
                                    XC_MethodHook hook = new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                                                    barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOPBAR, false)) {
                                                try {
                                                    Class cls = methodHookParam.thisObject.getClass();
                                                    Field f = cls.getDeclaredField("mSemiTransparent");
                                                    Field f1 = cls.getDeclaredField("mOpaque");
                                                    Field f2 = cls.getDeclaredField("mTransparent");
                                                    if (f != null) {
                                                        f.setAccessible(true);
                                                        f1.setAccessible(true);
                                                        f2.setAccessible(true);
                                                        f.set(methodHookParam.thisObject, 0x00000000);
                                                        f1.set(methodHookParam.thisObject, 0x00000000);
                                                        f2.set(methodHookParam.thisObject, 0x00000000);
                                                    }
                                                } catch (RuntimeException e) {
                                                    XposedBridge.log("++++++++++++++HOOK  key1" + e);
                                                }
                                            }
                                        }
                                    };
                                    if (clss.length == 2) {
                                        XposedHelpers.findAndHookConstructor(barBackClass, clss[0], clss[1], hook);
                                    } else if (clss.length == 1) {
                                        XposedHelpers.findAndHookConstructor(barBackClass, clss[0], hook);
                                    } else if (clss.length == 3) {
                                        XposedHelpers.findAndHookConstructor(barBackClass, clss[0],clss[1],clss[2], hook);
                                    } else if (clss.length == 6) {
                                        XposedHelpers.findAndHookConstructor(barBackClass, clss[0],clss[1],clss[2],clss[3],clss[4],clss[5], hook);
                                    }else if (clss.length == 0) {
                                        XposedHelpers.findAndHookConstructor(barBackClass, hook);
                                    }else if (clss.length == 4) {
                                        XposedHelpers.findAndHookConstructor(barBackClass, clss[0],clss[1],clss[2],clss[3], hook);
                                    } else {
                                        XposedBridge.log("^^^^^^^^^^^^^^barBackClass else " + clss.length + "构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                    }
                                } catch (RuntimeException e) {
                                    XposedBridge.log("++++++++++++++HOOK  11barBackClass err " + e);
                                }
                            }
                        }
                        if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_KEYCOLOROPEN, false)) {
//                            final Class nbvClass = XposedHelpers.findClass("com.android.systemui.statusbar.phone.NavigationBarView", lpparam.classLoader);
                            Class keyDrawClass = XposedHelpers.findClass("com.android.systemui.statusbar.policy.KeyButtonDrawable", lpparam.classLoader);
//                            if (nbvClass != null) {
//                                XposedHelpers.findAndHookMethod(nbvClass, "onFinishInflate", new XC_MethodHook() {
//                                    @Override
//                                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                        XposedBridge.log("++++++++++++++HOOK  onFinishInflate");
//                                    }
//                                });
//                            }
                            if (keyDrawClass != null) {
                                XposedHelpers.findAndHookConstructor(keyDrawClass, Drawable[].class, new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP &&
                                                barPrefs.getBoolean(Common.PREFS_SETTING_UI_KEYCOLOROPEN, false)) {
                                            try {
                                                if (methodHookParam.thisObject instanceof LayerDrawable) {
                                                    LayerDrawable ld = (LayerDrawable) (methodHookParam.thisObject);
                                                    int[][] states = new int[][]{new int[]{-android.R.attr.state_active}, new int[]{android.R.attr.state_activated}};
                                                    int c = barPrefs.getInt(Common.PREFS_SETTING_UI_KEYCOLOR, Color.CYAN);
                                                    int[] colors = new int[]{c, c, c};
                                                    ColorStateList csl = new ColorStateList(states, colors);
                                                    ld.setTintList(csl);
                                                }
                                            } catch (RuntimeException e) {
                                                XposedBridge.log("++++++++++++++HOOK  key1" + e);
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }catch (XposedHelpers.ClassNotFoundError e){
                        XposedBridge.log("^^^^^^^^^^^^^^HOOK barBackClass err "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
                    } catch (Exception e) {
                        XposedBridge.log("++++++++++++++HOOK barBackClass err" + e);
                    }
                }
            }catch (XposedHelpers.ClassNotFoundError e){
                XposedBridge.log("^^^^^^^^^^^^^^HOOK error "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
            }catch (NoSuchMethodError e){
                XposedBridge.log("^^^^^^^^^^^^^^HOOK error "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
            }catch (Exception e){
                XposedBridge.log("^^^^^^^^^^^^^^HOOK error "+lpparam.packageName+"  "+e+"^^^^^^^^^^^^^^^^^");
            }

            if("android".equals(lpparam.packageName)||
                    "com.click369.controlbp".equals(lpparam.packageName)||
                    "com.android.phone".equals(lpparam.packageName)||
                    "system".equals(lpparam.packageName)||
                    lpparam.packageName.startsWith("com.android.internal.")||
                    "com.android.systemui".equals(lpparam.packageName)){
                return;
            }
            barPrefs.reload();
            final boolean isTopBarOpen = barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOPBAR,false);
            final boolean isBottomBarOpen = barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR,false);
            if(!isTopBarOpen&&!isBottomBarOpen) {
                return;
            }
            final boolean isBottomBarDengTop = barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP,false);
            colorTestPrefs.reload();
            if (lpparam.packageName.toLowerCase().contains("camera")||
                    lpparam.packageName.toLowerCase().contains(".snap")||
                    colorTestPrefs.getBoolean(lpparam.packageName+"/blacklist",false)||
                    !barPrefs.getBoolean(lpparam.packageName+"/colorlist",false)){
                return;
            }

            if(colorTestPrefs.contains(lpparam.packageName+"/ime")){
                Class inputCls = XposedHelpers.findClass("android.inputmethodservice.InputMethodService",lpparam.classLoader);
                XposedHelpers.findAndHookMethod(inputCls, "onWindowShown", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        InputMethodService ims = (InputMethodService)methodHookParam.thisObject;
                        SharedPreferences sp = ims.getSharedPreferences("color_ime",Context.MODE_PRIVATE);
                        Intent  imeOpen = new Intent("com.click369.control.imeopen");
                        if(sp.contains("ime_color")&&barPrefs.contains(lpparam.packageName+"/locklist")){
                            imeOpen.putExtra("color",sp.getInt("ime_color",Color.BLACK));
                        }else{
                            View v = ims.getWindow().getWindow().getDecorView();
                            if (v!=null&&v.getWidth()>0) {
                                Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
                                if (bitmap != null && bitmap.getWidth() > 100) {
                                    Canvas canvas = new Canvas();
                                    canvas.setBitmap(bitmap);
                                    v.draw(canvas);
                                    int colorbottom = bitmap.getPixel(bitmap.getWidth() - 1, bitmap.getHeight() - ColorNavBarService.getDaoHangHeight(ims) - 1);
                                    bitmap.recycle();
                                    sp.edit().putInt("ime_color", colorbottom).commit();
                                    imeOpen.putExtra("color", colorbottom);
                                } else {
                                    imeOpen.putExtra("color", sp.getInt("ime_color", Color.WHITE));
                                }
                            }
                        }
                        ims.sendBroadcast(imeOpen);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    }
                });
                XposedHelpers.findAndHookMethod(inputCls, "onFinishInputView",boolean.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        try{
                            InputMethodService ims = (InputMethodService)methodHookParam.thisObject;
                            Intent  imeClose = new Intent("com.click369.control.imeclose");
                            ims.sendBroadcast(imeClose);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }


            final Class actCls = XposedHelpers.findClass("android.app.Activity",lpparam.classLoader);
            XposedHelpers.findAndHookMethod(actCls, "onCreate",Bundle.class, new XC_MethodHook() {
                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                try {
                    final Activity act = (Activity) (methodHookParam.thisObject);
                    final Handler h = new Handler();
                    final Window window = act.getWindow();
                    final SharedPreferences bar = act.getSharedPreferences(lpparam.packageName + "_bar", Context.MODE_PRIVATE);
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    if (colorTestPrefs.getBoolean(lpparam.packageName + "/trans", false)) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    }
                    final Runnable r = new Runnable() {
                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public void run() {
                        if (window != null && !act.isFinishing() && window.getDecorView() != null) {
                            new Thread() {
                                @Override
                                public void run() {
                                try {
                                    synchronized (actCls) {
//                                                        if(System.currentTimeMillis()-(Long)XposedHelpers.getAdditionalStaticField(actCls,"lasttime")<400){
//                                                            return;
//                                                        }
                                        final View v = (View) act.getWindow().getDecorView();
                                        if (v != null && v.isShown() && v.getWidth() > 100 && v.getHeight() > 100) {
                                            Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
                                            if (bitmap != null) {
                                                final Canvas canvas = new Canvas();
                                                canvas.setBitmap(bitmap);
                                                act.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            v.draw(canvas);
                                                            synchronized (v) {
                                                                v.notifyAll();
                                                            }
                                                        } catch (RuntimeException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                                synchronized (v) {
                                                    try {
                                                        v.wait(500);
                                                    } catch (InterruptedException e) {
                                                        //                                                                    e.printStackTrace();
                                                    }
                                                }
                                                int zt = RoundedCornerService.getZhuangTaiHeight(act);
                                                final int colortop = bitmap.getPixel(bitmap.getWidth() - 1, zt + 1);
                                                final int colorbottom = bitmap.getPixel(bitmap.getWidth()-1, bitmap.getHeight() - RoundedCornerService.getVirtualBarHeigh(act) - 2);
                                                bitmap.recycle();
//                                                                if ((Integer)XposedHelpers.getAdditionalStaticField(actCls,"lasttop") == colortop &&
//                                                                        (Integer)XposedHelpers.getAdditionalStaticField(actCls,"lastbottom") == colorbottom) {
//                                                                    if ((isTopBarOpen&&window.getStatusBarColor() == colortop)&&
//                                                                            ((isBottomBarOpen&&!isBottomBarDengTop&&window.getNavigationBarColor() == colorbottom)||
//                                                                                    (isBottomBarOpen&&isBottomBarDengTop&&window.getNavigationBarColor() == colortop))){
//                                                                        return;
//                                                                    }else if (isTopBarOpen&&window.getStatusBarColor() == colortop&&!isBottomBarOpen){
//                                                                        return;
//                                                                    }else if (isBottomBarOpen&&!isBottomBarDengTop&&window.getNavigationBarColor() == colorbottom&&!isTopBarOpen){
//                                                                        return;
//                                                                    }else if (isBottomBarOpen&&isBottomBarDengTop&&window.getNavigationBarColor() == colortop&&!isTopBarOpen){
//                                                                        return;
//                                                                    }
//                                                                }
//                                                                XposedBridge.log("+++++++++++++changecolor...  "+lpparam.packageName+" ^^^^^^^^^^^^^^^^^");
                                                if (colortop!=0) {
                                                    bar.edit().putInt(act.getClass().getName() + "/top", colortop).commit();
                                                    XposedHelpers.setAdditionalStaticField(actCls, "lasttop", colortop);
                                                }
                                                if (colorbottom!=0) {
                                                    bar.edit().putInt(act.getClass().getName() + "/bottom", colorbottom).commit();
                                                    XposedHelpers.setAdditionalStaticField(actCls, "lastbottom", colorbottom);
                                                }
                                                setColor(lpparam,act,barPrefs,colorTestPrefs,window,colortop,colorbottom);
                                            }
                                        }
                                    }
                                } catch (RuntimeException e) {
                                    e.printStackTrace();
                                }
                                }
                            }.start();
                        }
                        }
                    };
                    final int deyTime1 = colorTestPrefs.getInt("time1", 300);
                    int deyTime2 = colorTestPrefs.getInt("time2", 3000);
                    if ("com.tencent.mm.ui.LauncherUI".equals(act.getClass().getName())) {
                        deyTime2 = 10000;
                    }else if ("com.eg.android.AlipayGphone.AlipayLogin".equals(act.getClass().getName())) {
                        deyTime2 = 6000;
                    }
                    final long nTime = System.currentTimeMillis();
                    final ViewTreeObserver.OnGlobalLayoutListener lis = new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if(System.currentTimeMillis()-(Long)XposedHelpers.getAdditionalStaticField(actCls,"lasttime")<200){
                                return;
                            }
                            XposedHelpers.setAdditionalStaticField(actCls,"lasttime",System.currentTimeMillis());
                            h.post(r);
//                                    XposedHelpers.setAdditionalStaticField(actCls,"changetime369",System.currentTimeMillis());
//                                    new Thread(r).start();
//                                    }
//                                    if (deyTime1 > 0) {
//                                        if(System.currentTimeMillis()-nTime>deyTime1){
//                                            h.removeCallbacks(r);
//                                        }
//                                        h.postDelayed(r, deyTime1);
//                                    }

                        }
                    };
                    if (((bar.contains(act.getClass().getName() + "/top") &&
                            bar.contains(act.getClass().getName() + "/bottom")) &&
                            barPrefs.getBoolean(lpparam.packageName + "/locklist", false))) {
                        int colortop = bar.getInt(act.getClass().getName() + "/top", 0);
                        int colorbottom = 0;
                        if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP, false)) {
                            colorbottom = colortop;
                        } else {
                            colorbottom = bar.getInt(act.getClass().getName() + "/bottom", 0);
                        }
                        setColor(lpparam,act,barPrefs,colorTestPrefs,window,colortop,colorbottom);
                    } else if (!act.getWindow().isFloating()) {
                        ViewTreeObserver vto = act.getWindow().getDecorView().getViewTreeObserver();
                        if (vto.isAlive()) {
//                                    bar.edit().remove(act.getClass().getName() + "/top").commit();
//                                    bar.edit().remove(act.getClass().getName() + "/bottom").commit();
                            XposedHelpers.setAdditionalStaticField(actCls,"lasttop",0);
                            XposedHelpers.setAdditionalStaticField(actCls,"lastbottom",0);
//                                    XposedHelpers.setAdditionalStaticField(actCls,"changetime369",0L);
                            XposedHelpers.setAdditionalStaticField(actCls,"lasttime",0L);
                            vto.addOnGlobalLayoutListener(lis);//.addOnLayoutChangeListener(lis);

                        }
                        if (!barPrefs.getBoolean(Common.PREFS_SETTING_UI_ALWAYSCOLORBAR,false)) {
                            h.postDelayed(new Runnable() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                public void run() {
                                    try {
                                        synchronized (act) {
                                            if (act != null && act.getWindow() != null && act.getWindow().getDecorView() != null &&
                                                    act.getWindow().getDecorView().getViewTreeObserver().isAlive()) {
                                                act.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(lis);
                                            }
                                        }
                                    } catch (RuntimeException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, deyTime2);
                        }
                    }

                }catch (RuntimeException e){
                    e.printStackTrace();
                }
                }
            });
//            XposedHelpers.findAndHookMethod(actCls, "onResume",new XC_MethodHook() {
//                @Override
//                protected void beforeHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
//                        new Thread() {
//                            @Override
//                            public void run() {
//                                try {
//                                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
//                                        final Activity act = (Activity) (methodHookParam.thisObject);
//                                        final Window window = act.getWindow();
//                                        final SharedPreferences bar = act.getSharedPreferences(lpparam.packageName + "_bar", Context.MODE_PRIVATE);
//                                        if ((bar.contains(act.getClass().getName() + "/top") &&
//                                                bar.contains(act.getClass().getName() + "/bottom")) &&
//                                                !act.getWindow().isFloating()) {
//                                            int colortop = bar.getInt(act.getClass().getName() + "/top", 0);
//                                            int colorbottom = 0;
//                                            if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP, false)) {
//                                                colorbottom = colortop;
//                                            } else {
//                                                colorbottom = bar.getInt(act.getClass().getName() + "/bottom", 0);
//                                            }
//                                            setColor(lpparam,act,barPrefs,colorTestPrefs,window,colortop,colorbottom);
                                            //                                final  Handler h = new Handler();
//                                            if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOPBAR, false)
//                                                    &&!colorTestPrefs.getBoolean(lpparam.packageName +"/nottopbar",false)) {
//                                                final int colortop = bar.getInt(act.getClass().getName() + "/top", 0);
//                                                int red = (colortop & 0xff0000) >> 16;
//                                                int green = (colortop & 0x00ff00) >> 8;
//                                                int blue = (colortop & 0x0000ff);
//                                                final boolean isDark = (red > 175 && green > 175 && blue > 175) || colorTestPrefs.contains(lpparam.packageName + "/dark");
//                                                act.runOnUiThread(new Runnable() {
//                                                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//                                                    @Override
//                                                    public void run() {
//                                                        try {
//                                                            setDarkStatusIcon(act, isDark);
//                                                            window.setStatusBarColor(colortop);
//                                                        } catch (RuntimeException e) {
//                                                            XposedBridge.log("^^^^^^^^^^^^^^start error " + lpparam.packageName + "  " + e + "^^^^^^^^^^^^^^^^^");
//                                                        }
//                                                    }
//                                                });
//                                            }
//                                            if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR, false)) {
//                                                act.runOnUiThread(new Runnable() {
//                                                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//                                                    @Override
//                                                    public void run() {
//                                                        try {
//                                                            if (barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP, false)) {
//                                                                window.setNavigationBarColor(bar.getInt(act.getClass().getName() + "/top", 0));
//                                                            } else {
//                                                                window.setNavigationBarColor(bar.getInt(act.getClass().getName() + "/bottom", 0));
//                                                            }
//                                                        } catch (RuntimeException e) {
//                                                            e.printStackTrace();
//                                                        }
//                                                    }
//                                                });
//                                            }
//                                        }
//                                    }
//                                } catch (RuntimeException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }.start();
//                }
//            });

            try {
                final Class windowCls = XposedHelpers.findClass("com.android.internal.policy.PhoneWindow",lpparam.classLoader);
                XposedHelpers.findAndHookMethod(windowCls, "setNavigationBarColor", int.class, new XC_MethodHook() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Window window = (Window) methodHookParam.thisObject;
                        Context context = window.getContext();
                        final SharedPreferences bar = context.getSharedPreferences(lpparam.packageName + "_bar", Context.MODE_PRIVATE);
                        int bottom = bar.getInt(context.getClass().getName() + "/bottom", 0);
                        if (isBottomBarDengTop) {
                            bottom = bar.getInt(context.getClass().getName() + "/top", 0);
                        }
                        methodHookParam.args[0] = bottom;

                        if (window.getNavigationBarColor() == bottom) {
                            methodHookParam.setResult(null);
                            return;
                        }
//                        if (!colorTestPrefs.contains(lpparam.packageName+"/notchangeicon")) {
                            final int red = (bottom & 0xff0000) >> 16;
                            final int green = (bottom & 0x00ff00) >> 8;
                            final int blue = (bottom & 0x0000ff);
                            setDarkNavIcon(window, isQianRGB(red, green, blue));
//                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    }
                });
                XposedHelpers.findAndHookMethod(windowCls, "setStatusBarColor", int.class, new XC_MethodHook() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Window window = (Window)methodHookParam.thisObject;
                        Context context = window.getContext();
                        final SharedPreferences bar = context.getSharedPreferences(lpparam.packageName + "_bar", Context.MODE_PRIVATE);
                        int top =bar.getInt(context.getClass().getName() + "/top",0);
                        methodHookParam.args[0] = top;
                        final int red = (top & 0xff0000) >> 16;
                        final int green = (top & 0x00ff00) >> 8;
                        final int blue = (top & 0x0000ff);
                        setDarkStatusIcon(window, isQianRGB(red, green, blue) );//|| colorTestPrefs.contains(lpparam.packageName + "/dark")
                        if (window.getStatusBarColor() == top){
                            methodHookParam.setResult(null);
                            return;
                        }
//                        if (!colorTestPrefs.contains(lpparam.packageName+"/notchangeicon")) {

//                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    }
                });
//                final Class viewCls = XposedHelpers.findClass("android.view.View",lpparam.classLoader);
//                XposedHelpers.findAndHookMethod(viewCls, "setSystemUiVisibility", int.class, new XC_MethodHook() {
//                    @Override
//                    protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        try {
//                            View docView = (View) methodHookParam.thisObject;
////                            final SharedPreferences bar = context.getSharedPreferences(lpparam.packageName + "_bar", Context.MODE_PRIVATE);
////                            int bottom = bar.getInt(context.getClass().getName() + "/bottom", 0);
////                            if (isBottomBarDengTop) {
////                                bottom = bar.getInt(context.getClass().getName() + "/top", 0);
////                            }
//                            methodHookParam.args[0] = docView.getSystemUiVisibility()|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
//                            Field mvField = methodHookParam.thisObject.getClass().getDeclaredField("mSystemUiVisibility");
//                            mvField.setAccessible(true);
//                            mvField.set(methodHookParam.thisObject,methodHookParam.args[0]);
//                            Field field = methodHookParam.thisObject.getClass().getDeclaredField("mParent");
//                            field.setAccessible(true);
//                            Object parent = field.get(methodHookParam.thisObject);
//                            Method method = parent.getClass().getDeclaredMethod("recomputeViewAttributes",View.class);
//                            method.setAccessible(true);
//                            method.invoke(parent,methodHookParam.thisObject);
//                            methodHookParam.setResult(null);
//                            return;
//                        }catch (Exception e){
//                            e.printStackTrace();
//                        }
//                    }
//                });
            }catch (XposedHelpers.ClassNotFoundError e){
                e.printStackTrace();
            }catch (NoSuchMethodError e){
                e.printStackTrace();
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }
}
package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.InputEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.common.Common;


import org.w3c.dom.Text;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedRencent {
    public static  String getPkgByTask(Class cls,String taskName,Object obj){
        try {
            Field mTaskField = cls.getDeclaredField(taskName);
            mTaskField.setAccessible(true);
            Object mTask = mTaskField.get(obj);
            return getPkgByTask(mTask);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static  String getPkgByTask(Object task){
        try {
            Object mTask = task;
            Field keyField = mTask.getClass().getDeclaredField("key");
            keyField.setAccessible(true);
            Object key = keyField.get(mTask);
            Field baseIntentField = key.getClass().getDeclaredField("baseIntent");
            baseIntentField.setAccessible(true);
            Intent intentm = (Intent) (baseIntentField.get(key));
            String pkg = intentm.getComponent().getPackageName();
            return pkg;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getInfoByPkg(XSharedPreferences recentPrefs,
                                      XSharedPreferences appStartiPrefs,
//                                      XSharedPreferences muBeiPrefs,
                                      String pkg){
//        muBeiPrefs.reload();
        recentPrefs.reload();
        appStartiPrefs.reload();
        boolean isNotClean = recentPrefs.getBoolean(pkg + "/notclean", false);
        boolean isForceClean = recentPrefs.getBoolean(pkg+"/forceclean",false);
        boolean isBlur = recentPrefs.getBoolean(pkg+"/blur",false);
//        boolean isInMuBei = muBeiPrefs.getInt(pkg, -1) == 0;
        boolean isNotStop = appStartiPrefs.getBoolean(pkg + "/notstop", false);
        if (isNotClean ||isForceClean || isBlur || isNotStop) {
            String msgs[] = {"模糊", "保留", "常驻", "杀死"};
            boolean chooses[] = {isBlur, isNotClean, isNotStop, isForceClean};
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            for (int i = 0; i < msgs.length; i++) {
                if (chooses[i]) {
                    sb.append(msgs[i]).append(",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append(")");
            return sb.toString();
        }
        return "";
    }
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,
                                   final XSharedPreferences recentPrefs,
                                   final XSharedPreferences barPrefs,
//                                   final XSharedPreferences muBeiPrefs,
                                   final XSharedPreferences appStartiPrefs,
                                   final boolean isRecentOpen,final boolean isUIChangeOpen){
        try {
            if(lpparam.packageName.equals("com.android.systemui")&&isRecentOpen&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){//&&Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                final Class tvtCls = XposedHelpers.findClass("com.android.systemui.recents.views.TaskViewThumbnail", lpparam.classLoader);
                final Class recentActCls = XposedHelpers.findClass("com.android.systemui.recents.RecentsActivity", lpparam.classLoader);
                if (tvtCls != null) {
//                    final int roundNumber = barPrefs.getInt(Common.PREFS_SETTING_UI_RECENTBARROUNDNUM, 10);
                    Class clss[] = XposedUtil.getParmsByName(tvtCls, "setThumbnail");
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                if (methodHookParam.args[0] != null) {
                                   String pkg = getPkgByTask(tvtCls,"mTask",methodHookParam.thisObject);
                                    recentPrefs.reload();
                                    if (recentPrefs.getBoolean(pkg + "/blur", false)) {
                                        Map thbs = (HashMap)XposedHelpers.getAdditionalStaticField(recentActCls,"thbs");
                                        if (thbs==null){
                                            thbs = new HashMap<String,Object>();
                                            XposedHelpers.setAdditionalStaticField(recentActCls,"thbs",thbs);
                                        }
                                        Object thumbnailData = methodHookParam.args[0];
                                        Object newData = thbs.get(pkg);
                                        if (newData==null) {
                                            if (thumbnailData instanceof Bitmap) {
                                                methodHookParam.args[0] = fastblur((Bitmap) thumbnailData, 8);
                                            } else {

                                                Field thumbnailField = thumbnailData.getClass().getDeclaredField("thumbnail");
                                                Field scaleField = thumbnailData.getClass().getDeclaredField("scale");
                                                thumbnailField.setAccessible(true);
                                                scaleField.setAccessible(true);
                                                Object bmObj = thumbnailField.get(thumbnailData);
                                                if (bmObj != null) {
                                                    Bitmap newBm = fastblur((Bitmap) bmObj, 8);
                                                    thumbnailField.set(thumbnailData, newBm);
                                                }
                                            }
                                            thbs.put(pkg,methodHookParam.args[0]);
                                        }else{
                                            methodHookParam.args[0] = newData;
                                        }
                                    }

//                                    Field mTaskBarField = tvtCls.getDeclaredField("mTaskBar");
//                                    mTaskBarField.setAccessible(true);
//                                    View mTaskBar = (View) mTaskBarField.get(methodHookParam.thisObject);
//                                    Object thumbnailData = methodHookParam.args[0];
//                                    Bitmap bitmap = null;
//                                    if (thumbnailData instanceof Bitmap) {
//                                        bitmap = (Bitmap) thumbnailData;
//                                    } else {
//                                        Field thumbnailField = thumbnailData.getClass().getDeclaredField("thumbnail");
//                                        Field scaleField = thumbnailData.getClass().getDeclaredField("scale");
//                                        thumbnailField.setAccessible(true);
//                                        scaleField.setAccessible(true);
//                                        Object bmObj = thumbnailField.get(thumbnailData);
//                                        bitmap = (Bitmap) bmObj;
//                                    }
//                                    int pixel = bitmap.getPixel(1, bitmap.getHeight()/2);
//                                    mTaskBar.setBackgroundColor(pixel);
//                                    XposedBridge.log("【颜色值】  bitmap "+bitmap+ Integer.toHexString(pixel).toUpperCase());
                                }
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }

//                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                            if (param.args[0] != null) {
////                                View v = (View) param.thisObject;
////                                Map thbcolors = (HashMap) XposedHelpers.getAdditionalStaticField(recentActCls, "thbcolors");
////                                if (thbcolors == null) {
////                                    thbcolors = new HashMap<String, Integer>();
////                                    XposedHelpers.setAdditionalStaticField(recentActCls, "thbcolors", thbcolors);
////                                }
//                                Field mTaskBarField = tvtCls.getDeclaredField("mTaskBar");
//                                mTaskBarField.setAccessible(true);
//                                View mTaskBar = (View) mTaskBarField.get(param.thisObject);
//                                Object thumbnailData = param.args[0];
//                                Bitmap bitmap = null;
//                                if (thumbnailData instanceof Bitmap) {
//                                    bitmap = (Bitmap) thumbnailData;
//                                } else {
//                                    Field thumbnailField = thumbnailData.getClass().getDeclaredField("thumbnail");
//                                    Field scaleField = thumbnailData.getClass().getDeclaredField("scale");
//                                    thumbnailField.setAccessible(true);
//                                    scaleField.setAccessible(true);
//                                    Object bmObj = thumbnailField.get(thumbnailData);
//                                    bitmap = (Bitmap) bmObj;
//                                }
//                               int pixel = bitmap.getPixel(1, bitmap.getHeight()/2);
//                                mTaskBar.setBackgroundColor(pixel);
//                                XposedBridge.log("【颜色值】  bitmap "+bitmap+ Integer.toHexString(pixel).toUpperCase());
//
//                            }
//                        }
                    };
                    if (clss != null) {
                        XposedUtil.hookMethod(tvtCls,clss,"setThumbnail",hook);
//                        if (clss.length == 1) {
//                            XposedHelpers.findAndHookMethod(tvtCls, "setThumbnail", clss[0], hook);
//                        } else if (clss.length == 2) {
//                            XposedHelpers.findAndHookMethod(tvtCls, "setThumbnail", clss[0], clss[1], hook);
//                        } else if (clss.length == 4) {
//                            XposedHelpers.findAndHookMethod(tvtCls, "setThumbnail", clss[0], clss[1], clss[2], clss[3], hook);
//                        }  else if (clss.length == 3) {
//                            XposedHelpers.findAndHookMethod(tvtCls, "setThumbnail", clss[0], clss[1], clss[2], hook);
//                        } else {
//                            XposedBridge.log("^^^^^^^^^^^^^^setThumbnail else " + clss.length + "函数未找到 ^^^^^^^^^^^^^^^^^");
//                            for (Class c : clss) {
//                                XposedBridge.log("^^^^^^^^^^^^^^setThumbnail canshu " + c.getName() + " ^^^^^^^^^^^^^^^^^");
//                            }
//                        }
                    } else {
                        XposedBridge.log("^^^^^^^^^^^^^^setThumbnail 函数未找到 ^^^^^^^^^^^^^^^^^");
                    }
                    Class clss1[] = XposedUtil.getParmsByName(recentActCls, "onStop");
                    if (clss1 != null) {
                        XC_MethodHook hook1 = new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                Map thbs = (HashMap)XposedHelpers.getAdditionalStaticField(recentActCls,"thbs");
                                Map thbcolors = (HashMap)XposedHelpers.getAdditionalStaticField(recentActCls,"thbcolors");
                                if (thbs!=null){
                                    thbs.clear();
                                }
                                if (thbcolors!=null){
                                    thbcolors.clear();
                                }
                            }
                        };
                        XposedUtil.hookMethod(recentActCls,clss1,"onStop",hook1);
//                        if (clss1.length == 0) {
//                            XposedHelpers.findAndHookMethod(recentActCls, "onStop", hook1);
//                        } else if (clss1.length == 2) {
//                            XposedHelpers.findAndHookMethod(recentActCls, "onStop", clss1[0], clss1[1], hook1);
//                        } else {
//                            XposedBridge.log("^^^^^^^^^^^^^^onStop else " + clss1.length + "函数未找到 ^^^^^^^^^^^^^^^^^");
//                        }
                    } else {
                        XposedBridge.log("^^^^^^^^^^^^^^onStop 函数未找到 ^^^^^^^^^^^^^^^^^");
                    }
                }
            }else if(lpparam.packageName.equals("android")&&isRecentOpen&&Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                final Class arCls = XposedHelpers.findClass("com.android.server.am.ActivityRecord", lpparam.classLoader);
                if (arCls != null) {
                    Class clss[] = XposedUtil.getParmsByName(arCls,"updateThumbnailLocked");
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                if (methodHookParam.args[0] != null) {
                                    Field packageNameField = arCls.getDeclaredField("packageName");
                                    packageNameField.setAccessible(true);
                                    String pkg = (String) packageNameField.get(methodHookParam.thisObject);
                                    recentPrefs.reload();
                                    if (recentPrefs.getBoolean(pkg + "/blur", false)) {
                                        Bitmap bm = (Bitmap) methodHookParam.args[0];
                                        methodHookParam.args[0] = fastblur(bm, 8);//blurBitmap((Context) mContextObject,bm);
                                        // XposedBridge.log("^^^^^^^^^^^^^^最近任务强制模糊" +pkg+ " ^^^^^^^^^^^^^^^^^");
                                    }
                                }
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    if (clss != null) {
                        XposedUtil.hookMethod(arCls,clss,"updateThumbnailLocked",hook);
//                        if (clss.length == 2) {
//                            XposedHelpers.findAndHookMethod(arCls, "updateThumbnailLocked", clss[0], clss[1], hook);
//                        } else if (clss.length == 1) {
//                            XposedHelpers.findAndHookMethod(arCls, "updateThumbnailLocked", clss[0], hook);
//                        } else if (clss.length == 3) {
//                            XposedHelpers.findAndHookMethod(arCls, "updateThumbnailLocked", clss[0], clss[1], clss[2], hook);
//                        } else {
//                            XposedBridge.log("^^^^^^^^^^^^^^updateThumbnailLocked else " + clss.length + "函数未找到 ^^^^^^^^^^^^^^^^^");
//                        }
                    } else {
                        XposedBridge.log("^^^^^^^^^^^^^^updateThumbnailLocked 函数未找到 ^^^^^^^^^^^^^^^^^");
                    }
                }
            }
            if(isUIChangeOpen&&"com.android.systemui".equals(lpparam.packageName)) {
                barPrefs.reload();
                final boolean isColorBar = barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTBARCOLOR, false);
                final boolean isHideBar = barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTBARHIDE, false);
                final boolean isShowMem = barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTMEMSHOW, false);
                final boolean isShowInfo = barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTIFNO, false);
                final int roundNumber = barPrefs.getInt(Common.PREFS_SETTING_UI_RECENTBARROUNDNUM, 10);
                final float alphaNumber = barPrefs.getInt(Common.PREFS_SETTING_UI_RECENTBARALPHANUM, 100) / 100.0f;
                final Class recentViewCls = XposedHelpers.findClass("com.android.systemui.recents.views.TaskView", lpparam.classLoader);
                final Class recentHeaderCls = XposedHelpers.findClass("com.android.systemui.recents.views.TaskViewHeader", lpparam.classLoader);
                try {
                    if(isShowInfo&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final Class taskCls = XposedHelpers.findClass("com.android.systemui.recents.model.Task", lpparam.classLoader);
                        if (taskCls != null) {
                            Constructor cons[] = taskCls.getDeclaredConstructors();
                            Class clss[] = null;
                            for (Constructor c : cons) {
                                if (c.getParameterTypes().length > 1) {
                                    clss = c.getParameterTypes();
                                }
                            }
                            if (clss != null) {
                                XC_MethodHook hook = new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                        try {
                                            String pkg = getPkgByTask(methodHookParam.thisObject);
                                            String title = getInfoByPkg(recentPrefs,appStartiPrefs,pkg);
                                            if (title!=null&&title.length()>0) {
                                                Field field = taskCls.getDeclaredField(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? "title" : "activityLabel");
                                                field.setAccessible(true);
                                                String oldTitle = (String) field.get(methodHookParam.thisObject);
                                                if (oldTitle.indexOf("(")!=-1){
                                                    oldTitle = oldTitle.substring(0,oldTitle.indexOf("("));
                                                }
                                                field.set(methodHookParam.thisObject, oldTitle + title);
                                            }
                                        } catch (RuntimeException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                XposedUtil.hookConstructorMethod(taskCls,clss,hook);
//                                if (clss.length == 11) {
//                                    XposedHelpers.findAndHookConstructor(taskCls, clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10], hook);
//                                } else if (clss.length == 20) {
//                                    XposedHelpers.findAndHookConstructor(taskCls, clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10], clss[11], clss[12], clss[13], clss[14], clss[15], clss[16], clss[17], clss[18], clss[19], hook);
//                                }else if (clss.length == 21) {
//                                    XposedHelpers.findAndHookConstructor(taskCls, clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10], clss[11], clss[12], clss[13], clss[14], clss[15], clss[16], clss[17], clss[18], clss[19],  clss[20], hook);
//                                }else if (clss.length == 13) {
//                                    XposedHelpers.findAndHookConstructor(taskCls, clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10], clss[11], clss[12], hook);
//                                } else {
//                                    XposedBridge.log("^^^^^^^^^^^^^^Task else " + clss.length + "函数未找到 ^^^^^^^^^^^^^^^^^");
//                                }
                                Class clss1[] = XposedUtil.getParmsByName(taskCls,"notifyTaskDataLoaded");
                                XposedUtil.hookMethod(taskCls,clss1,"notifyTaskDataLoaded",hook);
//                                if (clss1.length == 2) {
//                                    XposedHelpers.findAndHookMethod(taskCls, "notifyTaskDataLoaded", clss1[0],clss1[1], hook);
//                                }else if (clss1.length == 3) {
//                                    XposedHelpers.findAndHookMethod(taskCls, "notifyTaskDataLoaded", clss1[0],clss1[1],clss1[2], hook);
//                                } else {
//                                    XposedBridge.log("^^^^^^^^^^^^^^notifyTaskDataLoaded else " + clss1.length + " ^^^^^^^^^^^^^^^^^");
//                                }
                            } else {
                                XposedBridge.log("^^^^^^^^^^^^^^Task 构造函数未找到 ^^^^^^^^^^^^^^^^^");
                            }
                        }
                        Class clss[] = XposedUtil.getParmsByName(recentHeaderCls,"onFinishInflate");
                        if (clss!=null&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            XC_MethodHook hook = new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                                    Field field = recentHeaderCls.getDeclaredField(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? "mTitleView" : "mActivityDescription");
                                    field.setAccessible(true);
                                    final TextView tv = (TextView) field.get(methodHookParam.thisObject);
//                                    ((View)(tv.getParent())).setBackgroundColor(Color.TRANSPARENT);
//                                            LinearLayout
                                    tv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            try {
                                                final String pkg = getPkgByTask(recentHeaderCls,"mTask",methodHookParam.thisObject);
                                                if (pkg == null||pkg.length()==0){
                                                    Toast.makeText(tv.getContext(),"无法获取被点击的应用，证明系统代码改动较大没有适配",Toast.LENGTH_LONG).show();
                                                    return;
                                                }
//                                                muBeiPrefs.reload();
                                                recentPrefs.reload();
                                                appStartiPrefs.reload();
                                                final boolean isNotClean = recentPrefs.getBoolean(pkg + "/notclean", false);
                                                final boolean isForceClean = recentPrefs.getBoolean(pkg+"/forceclean",false);
                                                final boolean isBlur = recentPrefs.getBoolean(pkg+"/blur",false);
                                                final boolean isNotShow = recentPrefs.getBoolean(pkg+"/notshow",false);
                                                final boolean isNotStop = appStartiPrefs.getBoolean(pkg + "/notstop", false);
                                                String titles[] = new String[]{
                                                        isNotClean?"取消卡片保留":"添加卡片保留",
                                                        isForceClean?"取消移除杀死":"添加移除杀死",
                                                        isBlur?"取消卡片模糊":"添加卡片模糊",
                                                        isNotShow?"取消卡片隐藏":"添加卡片隐藏",
                                                        isNotStop?"取消内存常驻(慎重)":"添加内存常驻(慎重)"
                                                };
                                                final String names[] = {pkg + "/notclean",pkg+"/forceclean",pkg+"/blur",pkg+"/notshow",pkg + "/notstop"};
                                                AlertDialog.Builder builder = new AlertDialog.Builder(tv.getContext());
                                                builder.setTitle("请选择(应用控制器后台如果被杀则不生效)");
//                                            builder.setMessage("设置后重新打开最近任务生效，如果不生效则证明应用控制器后台被杀。");
                                                builder.setItems(titles,new DialogInterface.OnClickListener(){
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which){
                                                        Intent intent = new Intent("com.click369.control.changerencetbysystemui");
                                                        intent.putExtra("data",which);
                                                        intent.putExtra("pkg",pkg);
                                                        intent.putExtra("name",names[which]);
                                                        tv.getContext().sendBroadcast(intent);
                                                        tv.postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    String title = getInfoByPkg(recentPrefs,appStartiPrefs,pkg);
                                                                    String oldTitle = tv.getText().toString();
                                                                    if (oldTitle.indexOf("(")!=-1){
                                                                        oldTitle = oldTitle.substring(0,oldTitle.indexOf("("));
                                                                    }
                                                                    tv.setText(oldTitle+title);
                                                                }catch (Exception e){
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        },500);
                                                    }
                                                });
                                                builder.create().show();
                                            }catch (Exception e){
                                                e.printStackTrace();
                                                Toast.makeText(tv.getContext(),"出错了，请把XP日志提交给开发者",Toast.LENGTH_LONG).show();
                                                XposedBridge.log("^^^^^^^^^^^^^^点击标最近任务题出错"+e+"^^^^^^^^^^^^^^^^^");
                                            }
                                        }
                                    });
                                }
                            };
                            XposedHelpers.findAndHookMethod(recentHeaderCls,"onFinishInflate",hook);
                        }
                    }
                }catch (XposedHelpers.ClassNotFoundError e){
                    e.printStackTrace();
                }catch (NoSuchMethodError e){
                    e.printStackTrace();
                }
                try {
                    if (isShowMem) {
                        final Class recentActCls = XposedHelpers.findClass("com.android.systemui.recents.RecentsActivity", lpparam.classLoader);
                        if (recentActCls != null) {
                            XC_MethodHook hook = new XC_MethodHook() {
                                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                                @Override
                                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    try {
                                        final Activity act = (Activity) methodHookParam.thisObject;
                                        View rootView = act.getWindow().getDecorView();
                                        ViewGroup fl = null;
                                        if (rootView instanceof  FrameLayout){
                                            fl = (FrameLayout)rootView;
                                        }else if (rootView instanceof RelativeLayout){
                                            fl = (RelativeLayout)rootView;
                                        }else if (rootView instanceof LinearLayout){
                                            fl = (LinearLayout)rootView;
                                        }
                                        final ActivityManager.MemoryInfo memoryInfo1 = new ActivityManager.MemoryInfo();
                                        final ActivityManager activityManager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
                                        activityManager.getMemoryInfo(memoryInfo1);
                                        final long mmm = memoryInfo1.availMem;
                                        String m = "可用" + (memoryInfo1.availMem / (1024 * 1024)) + "M,共" + (memoryInfo1.totalMem / (1024 * 1024)) + "M";
                                        if (fl.findViewWithTag("mem") != null) {
                                            TextView tv = (TextView) fl.findViewWithTag("mem");
                                            tv.setText(m);
                                        } else {
                                            final TextView memeTv = new TextView(fl.getContext());
                                            memeTv.setText(m);
                                            memeTv.setPadding(58, 80, 10, 10);
                                            memeTv.setTextColor(Color.WHITE);
                                            memeTv.setTextSize(14);
                                            memeTv.setTag("mem");
                                            memeTv.setVisibility(View.VISIBLE);
                                            ViewGroup.LayoutParams flp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                            fl.addView(memeTv, flp);
                                            memeTv.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                                                    builder.setTitle("请选择");
                                                    builder.setItems(new String[]{"释放系统缓存","释放缓存并杀死缓存进程","查看正在运行的服务"}, new DialogInterface.OnClickListener(){
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which){
                                                            if(which == 2){
                                                                Intent intent = new Intent("com.click369.control.startruuning");
                                                                act.sendBroadcast(intent);
                                                            }else{
                                                                Intent intent = new Intent("com.click369.control.offcleancache");
                                                                if (which == 1){
                                                                    intent.putExtra("data","all");
                                                                }
                                                                act.sendBroadcast(intent);
                                                                Toast.makeText(act,"开始清理...",Toast.LENGTH_SHORT).show();
                                                                memeTv.postDelayed(new Runnable() {
                                                                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
                                                                    @Override
                                                                    public void run() {
                                                                        if (!act.isDestroyed()) {
                                                                            ActivityManager activityManager = (ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE);
                                                                            activityManager.getMemoryInfo(memoryInfo1);
                                                                            String m = "可用" + (memoryInfo1.availMem / (1024 * 1024)) + "M,共" + (memoryInfo1.totalMem / (1024 * 1024)) + "M";
                                                                            memeTv.setText(m);
                                                                            Toast.makeText(act,"清理完成，共清理"+(Math.abs(memoryInfo1.availMem-mmm) / (1024 * 1024))+"M内存",Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                },2000);
                                                            }
                                                        }
                                                    });
                                                    builder.show();
                                                }
                                            });
                                        }
                                    }catch (RuntimeException e){
                                        e.printStackTrace();
                                    }
                                }
                            };
                            Class clss1[] = XposedUtil.getParmsByName(recentActCls, "onCreate");
                            if (clss1 != null) {
                                if (clss1.length == 1) {
                                    XposedHelpers.findAndHookMethod(recentActCls, "onCreate", clss1[0], hook);
                                } else {
                                    XposedBridge.log("^^^^^^^^^^^^^^onCreate else " + clss1.length + " ^^^^^^^^^^^^^^^^^");
                                }
                            } else {
                                XposedBridge.log("^^^^^^^^^^^^^^onCreate null ^^^^^^^^^^^^^^^^^");
                            }
                            Class clss2[] = XposedUtil.getParmsByName(recentActCls, "onStart");
                            if (clss2 != null) {
                                XposedHelpers.findAndHookMethod(recentActCls, "onStart", hook);
                            } else {
                                XposedBridge.log("^^^^^^^^^^^^^^onStart null ^^^^^^^^^^^^^^^^^");
                            }
                        }
                    }
                }catch (XposedHelpers.ClassNotFoundError e){
                    e.printStackTrace();
                }
                if (!isColorBar && !isHideBar && roundNumber == 10 && alphaNumber == 1.0f) {
                    return;
                }
               if (recentViewCls != null) {
                    Class clss[] = XposedUtil.getParmsByName(recentViewCls,"onFinishInflate");;
                    if (clss != null) {
                        XC_MethodHook hook = new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
//                                ((View)methodHookParam.thisObject).setBackgroundColor(Color.TRANSPARENT);
                                Field mThumbnailViewfield = recentViewCls.getDeclaredField("mThumbnailView");
                                mThumbnailViewfield.setAccessible(true);
                                final View mThumbnailView = (View) mThumbnailViewfield.get(methodHookParam.thisObject);
                                mThumbnailView.setAlpha(alphaNumber);

                                Field field = recentViewCls.getDeclaredField("mHeaderView");
                                field.setAccessible(true);
                                final View v = (View) field.get(methodHookParam.thisObject);
                                if (isHideBar) {
                                    v.setVisibility(View.INVISIBLE);
//                                    v.setBackgroundColor(Color.TRANSPARENT);
                                } else {
                                    if (isColorBar) {
                                        final String colors[] = {"#c84848", "#c75241", "#c58c47", "#c7ba45", "#86c442", "#5fc745", "#47c278", "#43c29c", "#46c6c2", "#419ec7", "#435cc0", "#6b46c1", "#8546c4", "#b745c0", "#c54367"};
                                        int color = Color.parseColor(colors[(int) (Math.random() * colors.length)]);
                                        v.setBackgroundColor(color);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            GradientDrawable gd = new GradientDrawable();//创建drawable
                                            gd.setColor(color);
                                            gd.setCornerRadii(new float[]{roundNumber, roundNumber, roundNumber, roundNumber, 0, 0, 0, 0});
                                            gd.setStroke(0, color);
                                            v.setBackground(gd);
                                        } else {
                                            v.setBackgroundColor(color);
                                        }
                                    }
                                }



                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    try {
                                        Field field1 = recentHeaderCls.getDeclaredField("mCornerRadius");
                                        field1.setAccessible(true);
                                        field1.set(v, roundNumber);
                                    }catch (NoSuchFieldException e){
                                    }
                                }
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                            }
                        };
                        XposedUtil.hookMethod(recentViewCls,clss,"onFinishInflate",hook);
//                        if (clss.length == 0) {
//                            XposedHelpers.findAndHookMethod(recentViewCls, "onFinishInflate", hook);
//                        } else {
//                            XposedBridge.log("^^^^^^^^^^^^^^onFinishInflate else " + clss.length + "函数未找到 ^^^^^^^^^^^^^^^^^");
//                        }
                    } else {
                        XposedBridge.log("^^^^^^^^^^^^^^onFinishInflate  函数未找到 ^^^^^^^^^^^^^^^^^");
                    }
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    final Class thumbViewCls = XposedHelpers.findClass("com.android.systemui.recents.views.TaskViewThumbnail", lpparam.classLoader);
                    if (thumbViewCls != null) {
                        Constructor ms[] = thumbViewCls.getDeclaredConstructors();
                        Class clss[] = null;
                        for (Constructor m : ms) {
                            if (m.getParameterTypes().length > 3) {
                                clss = m.getParameterTypes();
                                break;
                            }
                        }
                        if (clss != null) {
                            XC_MethodHook hook = new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                try {
                                    if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                                        Field field = thumbViewCls.getDeclaredField("mConfig");
                                        field.setAccessible(true);
                                        Object mConfig = field.get(methodHookParam.thisObject);
                                        Field rcField = mConfig.getClass().getDeclaredField("taskViewRoundedCornerRadiusPx");
                                        rcField.setAccessible(true);
                                        rcField.set(mConfig,roundNumber);
                                    }else{
                                        Field field = thumbViewCls.getDeclaredField("mCornerRadius");
                                        field.setAccessible(true);
                                        field.set(methodHookParam.thisObject, roundNumber);
                                    }
                                } catch (RuntimeException e) {
                                    e.printStackTrace();
                                }
                                }
                            };
                            XposedUtil.hookConstructorMethod(thumbViewCls,clss,hook);
//                            if (clss.length == 4) {
//                                XposedHelpers.findAndHookConstructor(thumbViewCls, clss[0], clss[1], clss[2], clss[3], hook);
//                            } else if (clss.length == 5) {
//                                XposedHelpers.findAndHookConstructor(thumbViewCls, clss[0], clss[1], clss[2], clss[3], clss[4], hook);
//                            } else {
//                                XposedBridge.log("^^^^^^^^^^^^^^yhumbnail else " + clss.length + "构造函数未找到 ^^^^^^^^^^^^^^^^^");
//                            }
                        } else {
                            XposedBridge.log("^^^^^^^^^^^^^^yhumbnail  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                        }
                        Class clsss[] = XposedUtil.getParmsByName(thumbViewCls,"updateClipToTaskBar");
                        if (clsss != null) {
                            XC_MethodHook hook = new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                    try{
                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
//                                            methodHookParam.args[0] = null;
                                        }
                                    } catch (RuntimeException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            XposedUtil.hookMethod(thumbViewCls,clsss,"updateClipToTaskBar",hook);
//                            if (clsss.length == 1) {
//                                XposedHelpers.findAndHookMethod(thumbViewCls, "updateClipToTaskBar", clsss[0], hook);
//                            } else if (clsss.length == 2) {
//                                XposedHelpers.findAndHookMethod(thumbViewCls, "updateClipToTaskBar", clsss[0], clsss[1], hook);
//                            } else {
//                                XposedBridge.log("^^^^^^^^^^^^^^updateClipToTaskBar else " + clsss.length + "函数未找到 ^^^^^^^^^^^^^^^^^");
//                            }
                        } else {
                            XposedBridge.log("^^^^^^^^^^^^^^updateClipToTaskBar  函数未找到 ^^^^^^^^^^^^^^^^^");
                        }
                    }

                    if (roundNumber != 10&&roundNumber != 0) {
                        final Class avbCls = XposedHelpers.findClass("com.android.systemui.recents.views.AnimateableViewBounds", lpparam.classLoader);
                        if (avbCls != null) {
                            Constructor ms[] = avbCls.getDeclaredConstructors();
                            Class clss[] = null;
                            for (Constructor m : ms) {
                                if (m.getParameterTypes().length > 1) {
                                    clss = m.getParameterTypes();
                                    break;
                                }
                            }
                            if (clss != null) {
                                XC_MethodHook hook = new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                        try {
                                            Field field = avbCls.getDeclaredField("mCornerRadius");
                                            field.setAccessible(true);
                                            field.set(methodHookParam.thisObject, roundNumber);
                                        } catch (RuntimeException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                XposedUtil.hookConstructorMethod(avbCls,clss,hook);
//                                if (clss.length == 2) {
//                                    XposedHelpers.findAndHookConstructor(avbCls, clss[0], clss[1], hook);
//                                } else if (clss.length == 3) {
//                                    XposedHelpers.findAndHookConstructor(avbCls, clss[0], clss[1], clss[2], hook);
//                                } else {
//                                    XposedBridge.log("^^^^^^^^^^^^^^avbCls else " + clss.length + "构造函数未找到 ^^^^^^^^^^^^^^^^^");
//                                }
                            } else {
                                XposedBridge.log("^^^^^^^^^^^^^^avbCls  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                            }
                        }
                        try {
                            final Class fsdCls = XposedHelpers.findClass("com.android.systemui.recents.views.FakeShadowDrawable", lpparam.classLoader);
                            if (fsdCls != null) {
                                Constructor ms[] = fsdCls.getDeclaredConstructors();
                                Class clss[] = null;
                                for (Constructor m : ms) {
                                    if (m.getParameterTypes().length > 1) {
                                        clss = m.getParameterTypes();
                                        break;
                                    }
                                }
                                if (clss != null) {
                                    XC_MethodHook hook = new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                            try {
                                                Field field = fsdCls.getDeclaredField("mCornerRadius");
                                                field.setAccessible(true);
                                                field.set(methodHookParam.thisObject, roundNumber);
                                            } catch (RuntimeException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    };
                                    if (clss.length == 2) {
                                        XposedHelpers.findAndHookConstructor(fsdCls, clss[0], clss[1], hook);
                                    } else if (clss.length == 3) {
                                        XposedHelpers.findAndHookConstructor(fsdCls, clss[0], clss[1], clss[2], hook);
                                    } else {
                                        XposedBridge.log("^^^^^^^^^^^^^^fsdCls else " + clss.length + "构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                    }
                                } else {
                                    XposedBridge.log("^^^^^^^^^^^^^^fsdCls  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                }
                            }
                        }catch (RuntimeException e){
                            XposedBridge.log("^^^^^^^^^^^^^FakeShadowDrawable 未找到 ^^^^^^^^^^^^^^^^^");
                        }catch (NoSuchMethodError e){
                            XposedBridge.log("^^^^^^^^^^^^^FakeShadowDrawable 未找到 ^^^^^^^^^^^^^^^^^");
                        }catch (XposedHelpers.ClassNotFoundError e){
                            XposedBridge.log("^^^^^^^^^^^^^FakeShadowDrawable1 未找到 ^^^^^^^^^^^^^^^^^");
                        }
                        try {
                            final Class tsvCls = XposedHelpers.findClass("com.android.systemui.recents.views.TaskStackView", lpparam.classLoader);
                            if (tsvCls != null) {
                                Constructor ms[] = tsvCls.getDeclaredConstructors();
                                Class clss[] = null;
                                for (Constructor m : ms) {
                                    if (m.getParameterTypes().length > 0) {
                                        clss = m.getParameterTypes();
                                        break;
                                    }
                                }
                                if (clss != null) {
                                    XC_MethodHook hook = new XC_MethodHook() {
                                        @Override
                                        protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                            try {
                                                if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                                                    Field field = tsvCls.getDeclaredField("mConfig");
                                                    field.setAccessible(true);
                                                    Object mConfig = field.get(methodHookParam.thisObject);
                                                    Field rcField = mConfig.getClass().getDeclaredField("taskViewRoundedCornerRadiusPx");
                                                    rcField.setAccessible(true);
                                                    rcField.set(mConfig,roundNumber);
                                                }else {
                                                    Field field = tsvCls.getDeclaredField("mTaskCornerRadiusPx");
                                                    field.setAccessible(true);
                                                    field.set(methodHookParam.thisObject, roundNumber);
                                                }
                                            } catch (NoSuchFieldException e) {
                                                XposedBridge.log("^^^^^^^^^^^^^^mTaskCornerRadiusPx 未找到 ^^^^^^^^^^^^^^^^^");
                                            }
                                        }
                                    };
                                    if (clss.length == 1) {
                                        XposedHelpers.findAndHookConstructor(tsvCls, clss[0], hook);
                                    } else if (clss.length == 2) {
                                        XposedHelpers.findAndHookConstructor(tsvCls, clss[0], clss[1], hook);
                                    } else {
                                        XposedBridge.log("^^^^^^^^^^^^^^tsvCls else " + clss.length + "构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                    }
                                } else {
                                    XposedBridge.log("^^^^^^^^^^^^^^tsvCls  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                }
                            }
                        }catch (RuntimeException e){
                            XposedBridge.log("^^^^^^^^^^^^^TaskStackView 未找到 ^^^^^^^^^^^^^^^^^");
                        }catch (NoSuchMethodError e){
                            XposedBridge.log("^^^^^^^^^^^^^TaskStackView 未找到 ^^^^^^^^^^^^^^^^^");
                        }catch (XposedHelpers.ClassNotFoundError e){
                            XposedBridge.log("^^^^^^^^^^^^^TaskStackView1 未找到 ^^^^^^^^^^^^^^^^^");
                        }
                    }
                }
            }
        }catch (RuntimeException e){
            e.printStackTrace();
        }catch (XposedHelpers.ClassNotFoundError e){
            e.printStackTrace();
        }
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//    public static Bitmap blurBitmap(Context context, Bitmap image) {
//        // 计算图片缩小后的长宽
//        int width = Math.round(image.getWidth() * 0.4f);
//        int height = Math.round(image.getHeight() * 0.4f);
//        // 将缩小后的图片做为预渲染的图片
//        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
//        // 创建一张渲染后的输出图片
//        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);
//        // 创建RenderScript内核对象
//        RenderScript rs = RenderScript.create(context);
//        // 创建一个模糊效果的RenderScript的工具对象
//        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
//        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间
//        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去
//        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
//        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
//        // 设置渲染的模糊程度, 25f是最大模糊度
//        blurScript.setRadius(15f);
//        // 设置blurScript对象的输入内存
//        blurScript.setInput(tmpIn);
//        // 将输出数据保存到输出内存中
//        blurScript.forEach(tmpOut);
//        // 将数据填充到Allocation中
//        tmpOut.copyTo(outputBitmap);
//        return outputBitmap;
//    }

    //模糊图片
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = getbitmap(sentBitmap,0.15f);
        if (radius < 1) {
            return (null);
        }
        try {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int[] pix = new int[w * h];
            bitmap.getPixels(pix, 0, w, 0, 0, w, h);

            int wm = w - 1;
            int hm = h - 1;
            int wh = w * h;
            int div = radius + radius + 1;

            int r[] = new int[wh];
            int g[] = new int[wh];
            int b[] = new int[wh];
            int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
            int vmin[] = new int[Math.max(w, h)];

            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int dv[] = new int[256 * divsum];
            for (i = 0; i < 256 * divsum; i++) {
                dv[i] = (i / divsum);
            }

            yw = yi = 0;

            int[][] stack = new int[div][3];
            int stackpointer;
            int stackstart;
            int[] sir;
            int rbs;
            int r1 = radius + 1;
            int routsum, goutsum, boutsum;
            int rinsum, ginsum, binsum;

            for (y = 0; y < h; y++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = pix[yi + Math.min(wm, Math.max(i, 0))];
                    sir = stack[i + radius];
                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;

                for (x = 0; x < w; x++) {

                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (y == 0) {
                        vmin[x] = Math.min(x + radius + 1, wm);
                    }
                    p = pix[yw + vmin[x]];

                    sir[0] = (p & 0xff0000) >> 16;
                    sir[1] = (p & 0x00ff00) >> 8;
                    sir[2] = (p & 0x0000ff);

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];

                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;

                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[(stackpointer) % div];

                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];

                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];

                    yi++;
                }
                yw += w;
            }
            for (x = 0; x < w; x++) {
                rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
                yp = -radius * w;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;

                    sir = stack[i + radius];

                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];

                    rbs = r1 - Math.abs(i);

                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;

                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }

                    if (i < hm) {
                        yp += w;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < h; y++) {
                    // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                    pix[yi] = ( 0xff000000 & pix[yi] ) | ( dv[rsum] << 16 ) | ( dv[gsum] << 8 ) | dv[bsum];

                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;

                    stackstart = stackpointer - radius + div;
                    sir = stack[stackstart % div];

                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];

                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * w;
                    }
                    p = x + vmin[y];

                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];

                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi += w;
                }
            }
            bitmap.setPixels(pix, 0, w, 0, 0, w, h);
            int[] argb = new int[bitmap.getWidth() * bitmap.getHeight()];

            bitmap.getPixels(argb, 0, bitmap.getWidth(), 0, 0,bitmap.getWidth(), bitmap.getHeight());
            int alpha = 100;
            // 获得图片的ARGB值
            alpha = alpha * 255 / 100;
            for (int a = 0; a < argb.length; a++) {
                argb[a] = (alpha << 24) | (argb[a] & 0x00FFFFFF);
            }
//            bitmap = Bitmap.createBitmap(argb,ow, oh, Bitmap.Config.ARGB_8888);
            bitmap = Bitmap.createBitmap(argb, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            return (getbitmap(bitmap,1.0f/0.145f));
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //缩放图片
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Bitmap getbitmap(Bitmap bitmap,float scale){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 取得想要缩放的matrix參數
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的圖片
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix,false);
    }

    public static Bitmap getBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(b);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        // Draw background
        Drawable bgDrawable = v.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(c);
        } else {
            c.drawColor(Color.WHITE);
        }
        // Draw view to canvas
        v.draw(c);
        return b;
    }

}
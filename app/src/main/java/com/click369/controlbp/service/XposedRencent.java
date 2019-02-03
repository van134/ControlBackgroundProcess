package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.InputEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.activity.ColorSetActivity;
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

    private static final String colors[] = {"#ff0000","#00ff00","#ffff00","#00ffff","#ff00ff","#23e2fe","#aa46ff","#7bff11",
            "#e011ff","#fa0093","#f7fa00","#fac200","#3af1bb","#9a74f2","#fd00ad","#90014f"};
    private static boolean isOpenNotifyColor = false;
    private static boolean isNotifyUseImgFile = false;
    private static boolean isRandomNotifyColor = false;
    private static String notifyColor = "#FFFFFF";
    private static int notifyAlpha = 100;
    private static long lastFlashTime = 0;
    private static boolean flashEnable = false;
    private static boolean flashEnableException = false;
    private static Context recentContext;
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
    public static  String getClsByTask(Object task){
        try {
            Object mTask = task;
            Field keyField = mTask.getClass().getDeclaredField("key");
            keyField.setAccessible(true);
            Object key = keyField.get(mTask);
            Field baseIntentField = key.getClass().getDeclaredField("baseIntent");
            baseIntentField.setAccessible(true);
            Intent intentm = (Intent) (baseIntentField.get(key));
            String cls = intentm.getComponent().getClassName();
            return cls;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getInfoByPkg(XSharedPreferences recentPrefs,
                                      XSharedPreferences appStartiPrefs,
                                      String pkg){
        recentPrefs.reload();
        appStartiPrefs.reload();
        boolean isNotClean = recentPrefs.getBoolean(pkg + "/notclean", false);
        boolean isForceClean = recentPrefs.getBoolean(pkg+"/forceclean",false);
        boolean isBlur = recentPrefs.getBoolean(pkg+"/blur",false);
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
                                   final XSharedPreferences appStartiPrefs,
                                   final boolean isRecentOpen,final boolean isUIChangeOpen){
        try {
            if(lpparam.packageName.equals("com.android.systemui")&&isRecentOpen&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){//&&Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                final Class tvtCls = XposedUtil.findClass("com.android.systemui.recents.views.TaskViewThumbnail", lpparam.classLoader);
                final Class recentActCls = XposedUtil.findClass("com.android.systemui.recents.RecentsActivity", lpparam.classLoader);
                final Class deleTaskCls = XposedUtil.findClass("com.android.systemui.recents.events.ui.DeleteTaskDataEvent", lpparam.classLoader);
                //尝试适配部分机型的最近任务保留功能  如果没有作用可以删除
                if(deleTaskCls!=null){
                    XposedUtil.hookMethod(recentActCls, new Class[]{deleTaskCls}, "onBusEvent", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                Object object =  param.args[0];
                                Field field = object.getClass().getDeclaredField("task");
                                field.setAccessible(true);
                                Object task = field.get(object);
                                String pkg = getPkgByTask(task);
                                recentPrefs.reload();
                                if (pkg != null && recentPrefs.getBoolean(pkg + "/notclean", false)) {
                                    String cls = getClsByTask(task);
                                    if("com.tencent.mm".equals(pkg)&&!"com.tencent.mm.ui.LauncherUI".equals(cls)){
                                    }else {
                                        param.setResult(false);
                                        return;
                                    }
                                }else if (recentPrefs.getBoolean(pkg + "/forceclean", false)) {
                                    if("com.tencent.mm".equals(pkg)){
                                        String cls = getClsByTask(task);
                                        if(!"com.tencent.mm.ui.LauncherUI".equals(cls)){
                                        }else{
                                            Activity activity = (Activity)param.thisObject;
                                            Intent intent = new Intent("com.click369.control.removerecent");
                                            intent.putExtra("pkg", pkg);
                                            activity.sendBroadcast(intent);
                                        }
                                    }else{
                                        Activity activity = (Activity)param.thisObject;
                                        Intent intent = new Intent("com.click369.control.removerecent");
                                        intent.putExtra("pkg", pkg);
                                        activity.sendBroadcast(intent);
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }


                if (tvtCls != null) {
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

                                }
                            } catch (Throwable e) {
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
                    XposedUtil.hookMethod(tvtCls,clss,"setThumbnail",hook);
                    Class clss1[] = XposedUtil.getParmsByName(recentActCls, "onStop");
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
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    XposedUtil.hookMethod(arCls,clss,"updateThumbnailLocked",hook);
                }
            }
            if("com.android.systemui".equals(lpparam.packageName)){//
                try {
                    Class flashCls = XposedUtil.findClass("com.android.systemui.statusbar.policy.FlashlightControllerImpl", lpparam.classLoader);
                    if(flashCls == null){
                        flashCls = XposedUtil.findClass("com.android.systemui.statusbar.policy.FlashlightController", lpparam.classLoader);
                    }
                    if(flashCls!=null){
                        final Class mFlashCls = flashCls;
                        XC_MethodHook hook = new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
//                                Field field = mFlashCls.getDeclaredField("mContext");
//                                field.setAccessible(true);
                                Context context = (Context)(param.args[0]);
                                final Method setFlashlightMethod = mFlashCls.getDeclaredMethod("setFlashlight",boolean.class);
                                setFlashlightMethod.setAccessible(true);
                                BroadcastReceiver br = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        try {
                                            String action = intent.getAction();
                                            if("com.click369.control.sysui.changeflash".equals(action)){
                                                if(System.currentTimeMillis()-lastFlashTime<200){
                                                    return;
                                                }
                                                try {
                                                    final Field isEnabledField = mFlashCls.getDeclaredField("mFlashlightEnabled");
                                                    isEnabledField.setAccessible(true);
                                                    flashEnable = (Boolean)(isEnabledField.get(param.thisObject));
                                                }catch (Exception e){
                                                    flashEnable = false;
                                                }

                                                setFlashlightMethod.invoke(param.thisObject,!flashEnable);
                                                flashEnable = !flashEnable;
                                                lastFlashTime = System.currentTimeMillis();
                                            }else if("com.click369.control.sysui.msgflash".equals(action)){
                                                new Thread(){
                                                    @Override
                                                    public void run() {
                                                        try {
                                                            try {
                                                                final Field isEnabledField = mFlashCls.getDeclaredField("mFlashlightEnabled");
                                                                isEnabledField.setAccessible(true);
                                                                flashEnable = (Boolean)(isEnabledField.get(param.thisObject));
                                                            }catch (Exception e){
                                                                if(!flashEnableException){
                                                                    flashEnable = false;
                                                                }
                                                                flashEnableException = true;
                                                            }
                                                            for(int i = 0;i<6;i++){
                                                                Thread.sleep(i%2==0?150:50);
                                                                setFlashlightMethod.invoke(param.thisObject,!flashEnable);
                                                                flashEnable = !flashEnable;
                                                            }
                                                        }catch (Throwable e){

                                                        }
                                                    }
                                                }.start();
                                            }
                                        }catch (Throwable e){

                                        }
                                    }
                                };
                                IntentFilter intentFilter = new IntentFilter();
                                intentFilter.addAction("com.click369.control.sysui.changeflash");
                                intentFilter.addAction("com.click369.control.sysui.msgflash");
                                context.registerReceiver(br,intentFilter);
//                                XposedBridge.log("FlashlightControllerImpl  注册完成 ...");
                            }
                        };
                        XposedUtil.hookConstructorMethod(mFlashCls,new Class[]{Context.class},hook);
                    }
                }catch (Throwable e){
                    XposedBridge.log("FlashlightControllerImpl  hook出错 ..."+e);
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
                final Class recentViewCls = XposedUtil.findClass("com.android.systemui.recents.views.TaskView", lpparam.classLoader);
                final Class recentHeaderCls = XposedUtil.findClass("com.android.systemui.recents.views.TaskViewHeader", lpparam.classLoader);
                try {
                    if(isShowInfo&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        final Class taskCls = XposedUtil.findClass("com.android.systemui.recents.model.Task", lpparam.classLoader);
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
                                        } catch (Throwable e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                XposedUtil.hookConstructorMethod(taskCls,clss,hook);
                                XposedUtil.hookMethod(taskCls,XposedUtil.getParmsByName(taskCls,"notifyTaskDataLoaded"),"notifyTaskDataLoaded",hook);
                            } else {
                                XposedBridge.log("^^^^^^^^^^^^^^Task 构造函数未找到 ^^^^^^^^^^^^^^^^^");
                            }
                        }
                        Class clss[] = XposedUtil.getParmsByName(recentHeaderCls,"onFinishInflate");
                        if (clss!=null&&Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                            XC_MethodHook hook = new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                                    try {
                                        if(recentContext!=null){
                                            Field field = recentHeaderCls.getDeclaredField(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ? "mTitleView" : "mActivityDescription");
                                            field.setAccessible(true);
                                            final TextView tv = (TextView) field.get(methodHookParam.thisObject);
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(recentContext);
                                            builder.setTitle("请选择(应用控制器后台如果被杀则不生效)");
                                            tv.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    try {
                                                        final String pkg = getPkgByTask(recentHeaderCls, "mTask", methodHookParam.thisObject);
                                                        if (pkg == null || pkg.length() == 0) {
                                                            Toast.makeText(tv.getContext(), "无法获取被点击的应用，证明系统代码改动较大没有适配", Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        recentPrefs.reload();
                                                        appStartiPrefs.reload();
                                                        final boolean isNotClean = recentPrefs.getBoolean(pkg + "/notclean", false);
                                                        final boolean isForceClean = recentPrefs.getBoolean(pkg + "/forceclean", false);
                                                        final boolean isBlur = recentPrefs.getBoolean(pkg + "/blur", false);
                                                        final boolean isNotShow = recentPrefs.getBoolean(pkg + "/notshow", false);
                                                        final boolean isNotStop = appStartiPrefs.getBoolean(pkg + "/notstop", false);
                                                        String titles[] = new String[]{
                                                                isNotClean ? "取消卡片保留" : "添加卡片保留",
                                                                isForceClean ? "取消移除杀死" : "添加移除杀死",
                                                                isBlur ? "取消卡片模糊" : "添加卡片模糊",
                                                                isNotShow ? "取消卡片隐藏" : "添加卡片隐藏",
                                                                isNotStop ? "取消内存常驻(慎重)" : "添加内存常驻(慎重)"
                                                        };
                                                        final String names[] = {pkg + "/notclean", pkg + "/forceclean", pkg + "/blur", pkg + "/notshow", pkg + "/notstop"};

                                                        builder.setItems(titles, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Intent intent = new Intent("com.click369.control.changerencetbysystemui");
                                                                intent.putExtra("data", which);
                                                                intent.putExtra("pkg", pkg);
                                                                intent.putExtra("name", names[which]);
                                                                tv.getContext().sendBroadcast(intent);
                                                                tv.postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        try {
                                                                            String title = getInfoByPkg(recentPrefs, appStartiPrefs, pkg);
                                                                            String oldTitle = tv.getText().toString();
                                                                            if (oldTitle.indexOf("(") != -1) {
                                                                                oldTitle = oldTitle.substring(0, oldTitle.indexOf("("));
                                                                            }
                                                                            tv.setText(oldTitle + title);
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                }, 500);
                                                            }
                                                        });
                                                        AlertDialog ad =  builder.create();
                                                        ad.show();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        Toast.makeText(recentContext, "出错了，请把XP日志提交给开发者", Toast.LENGTH_LONG).show();
                                                        XposedBridge.log("^^^^^^^^^^^^^^点击最近任务标题出错" + e + "^^^^^^^^^^^^^^^^^");
                                                    }
                                                }
                                            });
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                        XposedBridge.log("^^^^^^^^^^^^^^最近任务标题出错1 " + e + "^^^^^^^^^^^^^^^^^");
                                    }
                                }
                            };
                            XposedUtil.hookMethod(recentHeaderCls,clss,"onFinishInflate",hook);
                        }
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
                try {
//                    final Class recentActCls = XposedHelpers.findClass("android.app.Activity", lpparam.classLoader);
                    final Class recentActCls = XposedUtil.findClass("com.android.systemui.recents.RecentsActivity", lpparam.classLoader);
                    if (recentActCls != null) {
                        XC_MethodHook hook = new XC_MethodHook() {
//                            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                                try {
                                    final Activity recentAct = (Activity) methodHookParam.thisObject;
                                    if (isShowMem) {
                                        View rootView = recentAct.getWindow().getDecorView();
                                        ViewGroup fl = null;
                                        if (rootView instanceof FrameLayout) {
                                            fl = (FrameLayout) rootView;
                                        } else if (rootView instanceof RelativeLayout) {
                                            fl = (RelativeLayout) rootView;
                                        } else if (rootView instanceof LinearLayout) {
                                            fl = (LinearLayout) rootView;
                                        }
                                        recentContext = fl.getContext();
                                        final ActivityManager.MemoryInfo memoryInfo1 = new ActivityManager.MemoryInfo();
                                        final ActivityManager activityManager = (ActivityManager) recentAct.getSystemService(Context.ACTIVITY_SERVICE);
                                        activityManager.getMemoryInfo(memoryInfo1);
                                        final long mmm = memoryInfo1.availMem;
                                        String m = "可用" + (memoryInfo1.availMem / (1024 * 1024)) + "M,共" + (memoryInfo1.totalMem / (1024 * 1024)) + "M";
                                        if (fl.findViewWithTag("mem") != null) {
                                            TextView tv = (TextView) fl.findViewWithTag("mem");
                                            tv.setText(m);
                                        } else {
                                            final TextView memeTv = new TextView(recentContext);
                                            memeTv.setText(m);
                                            memeTv.setPadding(58, 80, 10, 10);
                                            memeTv.setTextColor(Color.WHITE);
                                            memeTv.setTextSize(14);
                                            memeTv.setTag("mem");
                                            memeTv.setVisibility(View.VISIBLE);
                                            ViewGroup.LayoutParams flp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                            fl.addView(memeTv, flp);

                                            final AlertDialog.Builder builder = new AlertDialog.Builder(recentContext);
                                            builder.setTitle("请选择");
                                            builder.setItems(new CharSequence[]{"释放系统缓存", "释放缓存并杀死缓存进程", "查看正在运行的服务"}, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 2) {
                                                        Intent intent = new Intent("com.click369.control.startruuning");
                                                        recentContext.sendBroadcast(intent);
                                                    } else {
                                                        Intent intent = new Intent("com.click369.control.offcleancache");
                                                        if (which == 1) {
                                                            intent.putExtra("data", "all");
                                                        }
                                                        recentContext.sendBroadcast(intent);
                                                        Toast.makeText(recentContext, "开始清理...", Toast.LENGTH_SHORT).show();
                                                        memeTv.postDelayed(new Runnable() {
                                                            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
                                                            @Override
                                                            public void run() {
                                                                try{
                                                                    if (!recentAct.isDestroyed()) {
                                                                        ActivityManager activityManager = (ActivityManager) recentAct.getSystemService(Context.ACTIVITY_SERVICE);
                                                                        activityManager.getMemoryInfo(memoryInfo1);
                                                                        String m = "可用" + (memoryInfo1.availMem / (1024 * 1024)) + "M,共" + (memoryInfo1.totalMem / (1024 * 1024)) + "M";
                                                                        memeTv.setText(m);
                                                                        Toast.makeText(recentAct, "清理完成，共清理" + (Math.abs(memoryInfo1.availMem - mmm) / (1024 * 1024)) + "M内存", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }catch (Exception e){
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }, 2000);
                                                    }
                                                }
                                            });
                                            final AlertDialog ad =  builder.create();
                                            memeTv.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    try {
                                                        ad.show();
                                                    }catch (Exception e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }catch (Throwable e){
                                    e.printStackTrace();
                                }
                            }
                        };
                        XposedUtil.hookMethod(recentActCls,XposedUtil.getParmsByName(recentActCls, "onCreate"), "onCreate",  hook);
                        XposedUtil.hookMethod(recentActCls,XposedUtil.getParmsByName(recentActCls, "onStart"), "onStart",  hook);
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
//                if (!isColorBar && !isHideBar && roundNumber == 10 && alphaNumber == 1.0f) {
//                    return;
//                }
               if (recentViewCls != null&&!(!isColorBar && !isHideBar && roundNumber == 10 && alphaNumber == 1.0f)) {
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
                                    }catch (Throwable e){
                                        e.printStackTrace();
                                    }
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                            }
                        };
                        XposedUtil.hookMethod(recentViewCls,clss,"onFinishInflate",hook);
                    } else {
                        XposedBridge.log("^^^^^^^^^^^^^^onFinishInflate  函数未找到 ^^^^^^^^^^^^^^^^^");
                    }
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    try {
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
                            } else {
                                XposedBridge.log("^^^^^^^^^^^^^^yhumbnail  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                            }
//                        Class clsss[] = XposedUtil.getParmsByName(thumbViewCls,"updateClipToTaskBar");
//                        if (clsss != null) {
//                            XC_MethodHook hook = new XC_MethodHook() {
//                                @Override
//                                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                                    try{
//                                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
////                                            methodHookParam.args[0] = null;
//                                        }
//                                    } catch (RuntimeException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//                            };
//                            XposedUtil.hookMethod(thumbViewCls,clsss,"updateClipToTaskBar",hook);
//                        } else {
//                            XposedBridge.log("^^^^^^^^^^^^^^updateClipToTaskBar  函数未找到 ^^^^^^^^^^^^^^^^^");
//                        }
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }

                    if (roundNumber != 10&&roundNumber != 0) {
                        try {
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
                                } else {
                                    XposedBridge.log("^^^^^^^^^^^^^^avbCls  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                }
                            }
                        }catch (Throwable e){
                            e.printStackTrace();
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
                                    XposedUtil.hookConstructorMethod(fsdCls,clss,hook);
                                } else {
                                    XposedBridge.log("^^^^^^^^^^^^^^fsdCls  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                }
                            }
                        }catch (Throwable e){
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
                                            } catch (Throwable e) {
                                                XposedBridge.log("^^^^^^^^^^^^^^mTaskCornerRadiusPx 未找到 ^^^^^^^^^^^^^^^^^");
                                            }
                                        }
                                    };
                                    XposedUtil.hookConstructorMethod(tsvCls,clss,hook);
                                } else {
                                    XposedBridge.log("^^^^^^^^^^^^^^tsvCls  构造函数未找到 ^^^^^^^^^^^^^^^^^");
                                }
                            }
                        }catch (Throwable e){
                            XposedBridge.log("^^^^^^^^^^^^^TaskStackView1 未找到 ^^^^^^^^^^^^^^^^^");
                        }
                    }
                }
            }

            if (lpparam.packageName.equals("com.android.systemui")) {
                try {
                    barPrefs.reload();
                    isOpenNotifyColor = barPrefs.getBoolean(Common.PREFS_SETTING_UI_NOTIFY_COLOROPEN,false);
                    isNotifyUseImgFile = barPrefs.getBoolean(Common.PREFS_SETTING_UI_NOTIFY_ISUSEIMGFILE,false);
                    isRandomNotifyColor = barPrefs.getBoolean(Common.PREFS_SETTING_UI_NOTIFY_RANDOMCOLOR,false);
                    notifyColor = barPrefs.getString(Common.PREFS_SETTING_UI_NOTIFY_SETCOLOR,"#FFFFFF");
                    notifyAlpha = barPrefs.getInt(Common.PREFS_SETTING_UI_NOTIFY_ALPHA,100);
                    final Class eovClass = XposedHelpers.findClass("com.android.systemui.statusbar.ExpandableOutlineView", lpparam.classLoader);
                    final Class anvClass = XposedHelpers.findClass("com.android.systemui.statusbar.ActivatableNotificationView", lpparam.classLoader);
//                    final Class enrClass = XposedHelpers.findClass("com.android.systemui.statusbar.ExpandableNotificationRow", lpparam.classLoader);
                    final Class nbvClass = XposedHelpers.findClass("com.android.systemui.statusbar.NotificationBackgroundView", lpparam.classLoader);
                    final Class nsslClass = XposedHelpers.findClass("com.android.systemui.statusbar.stack.NotificationStackScrollLayout", lpparam.classLoader);
                    //通知滑动后背景修改
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                if(isOpenNotifyColor){
                                    if (notifyAlpha>95){
                                        param.setResult(null);
                                        return;
                                    }
                                    Field mBackgroundPaintField = nsslClass.getDeclaredField("mBackgroundPaint");
                                    mBackgroundPaintField.setAccessible(true);
                                    Paint p = (Paint)mBackgroundPaintField.get(param.thisObject);
//                                    Canvas canvas = (Canvas)param.args[0];
                                    p.setColor(Color.argb(255-((int)((notifyAlpha*1.0f/100)*255)),255,255,255));
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    };
                    //通知背景修改
                    XposedUtil.hookMethod(nsslClass,XposedUtil.getParmsByName(nsslClass,"onDraw"),"onDraw",hook);
                    XC_MethodHook hook1 = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                if(isOpenNotifyColor) {
                                    Field mShadowAlphaField = anvClass.getDeclaredField("mShadowAlpha");
                                    mShadowAlphaField.setAccessible(true);
                                    mShadowAlphaField.set(param.thisObject,0f);
//                                    Class cls = Class.forName("com.android.systemui.statusbar.ExpandableOutlineView");
                                    Method m = eovClass.getDeclaredMethod("setOutlineAlpha",float.class);
                                    m.setAccessible(true);
                                    m.invoke(param.thisObject,0f);
                                    param.setResult(null);
                                    return;
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    };
                    XposedUtil.hookMethod(anvClass,XposedUtil.getParmsByName(anvClass,"updateOutlineAlpha"),"updateOutlineAlpha",hook1);
                    //通知背景透明度修改
                    XC_MethodHook hook3 = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if(isOpenNotifyColor) {
                                View v = (View) (param.thisObject);
                                try {
                                    if(isNotifyUseImgFile){
                                        Object o = v.getTag();
                                        if("OK".equals(o)){
                                            v.invalidate();
                                            param.setResult(null);
                                            return;
                                        }else{
                                            File f = new File(Environment.getExternalStorageDirectory()+File.separator+"processcontrol","nb.png");
                                            if(!f.exists()){
                                                f =  new File(Environment.getExternalStorageDirectory()+File.separator+"processcontrol","nb.jpg");
                                            }
                                            if(f.exists()){
                                                Field field = nbvClass.getDeclaredField("mBackground");
                                                field.setAccessible(true);
                                                Drawable mDrawable = Drawable.createFromPath(f.getAbsolutePath());
//                                                mDrawable.setAlpha((int)((notifyAlpha*1.0f/100)*255));
                                                field.set(param.thisObject,mDrawable);
                                                v.setTag("OK");
                                                v.invalidate();
                                                param.setResult(null);
                                                return;
                                            }
                                        }
                                    }
                                    if(v.getTag()==null||!(v.getTag() instanceof Integer)){
                                        int c = Color.WHITE;
                                        if(isRandomNotifyColor){
                                            String cs = colors[(int) (Math.random() * colors.length)];
                                            c = Color.parseColor(cs);
                                        }else{
                                            c = Color.parseColor(notifyColor);
                                        }
                                        v.setTag(c);
                                    }
                                    int c = (Integer) v.getTag();
                                    param.args[0] = c;
                                }catch (Throwable e){
                                    e.printStackTrace();
                                    XposedBridge.log(e);
                                }
                            }
                        }
                    };
                    String name = "setTint";
                    Class clss1[] = XposedUtil.getParmsByName(nbvClass,name);
                    if(clss1 ==null){
                        name = "setBackgroundColor";
                        clss1 = XposedUtil.getParmsByName(nbvClass,name);
                    }
                    XposedUtil.hookMethod(nbvClass,clss1,name,hook3);
                    XC_MethodHook hook4 = new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            try {
                                if(isOpenNotifyColor) {
                                    View v = (View) (param.thisObject);
                                    v.setAlpha(notifyAlpha*1.0f/100);
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    };
                    XposedUtil.hookMethod(nbvClass,XposedUtil.getParmsByName(nbvClass,"onDraw"),"onDraw",hook4);
                }catch (Throwable e){
                    e.printStackTrace();
                }
                try {
//                    final Class appSysuiCls = XposedHelpers.findClass("com.android.systemui.SystemUIApplication", lpparam.classLoader);
                    final Class appSysuiCls = XposedHelpers.findClass("android.app.Application", lpparam.classLoader);
                    XC_MethodHook hook = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {

                                BroadcastReceiver br = new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        barPrefs.reload();
                                        String action = intent.getAction();
                                        if(action.equals("com.click369.control.rebootsystemui")){
                                            System.exit(0);
                                        }else if(action.equals("com.click369.control.sysui.loadconfig")){
                                            if(intent.hasExtra("notifyalpha")){
                                                notifyAlpha = intent.getIntExtra("notifyalpha",100);
                                            }
                                            if(intent.hasExtra("israndomnotifycolor")){
                                                isRandomNotifyColor = intent.getBooleanExtra("israndomnotifycolor",false);
                                            }
                                            if(intent.hasExtra("isnotifycoloropen")){
                                                isOpenNotifyColor = intent.getBooleanExtra("isnotifycoloropen",false);
                                            }
                                            if(intent.hasExtra("notifycolor")){
                                                notifyColor = intent.getStringExtra("notifycolor");
                                            }
                                            if(intent.hasExtra("notifyuseimgfile")){
                                                isNotifyUseImgFile = intent.getBooleanExtra("notifyuseimgfile",false);
                                            }
                                        }
                                    }
                                };
                                IntentFilter intentFilter = new IntentFilter();
                                intentFilter.addAction("com.click369.control.rebootsystemui");
                                intentFilter.addAction("com.click369.control.sysui.loadconfig");
                                if (methodHookParam.thisObject != null && methodHookParam.thisObject instanceof Application) {
                                    Application app = ((Application) methodHookParam.thisObject);
                                    app.registerReceiver(br, intentFilter);
                                }
                            } catch (RuntimeException e) {
                                XposedBridge.log("^^^^^^^^^^^^^^SystemUIApplication " + e + "^^^^^^^^^^^^^^^^^");
                            }
                        }
                    };
                    XposedUtil.hookMethod(appSysuiCls,XposedUtil.getParmsByName(appSysuiCls,"onCreate"),"onCreate",hook);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }


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
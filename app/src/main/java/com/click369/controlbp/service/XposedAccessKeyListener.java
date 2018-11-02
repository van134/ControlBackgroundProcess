package com.click369.controlbp.service;

import android.accessibilityservice.AccessibilityService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Field;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedAccessKeyListener {

    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam, XSharedPreferences colorPrefs){
        if(colorPrefs.hasFileChanged()){
            colorPrefs.reload();
        }
        if (!colorPrefs.contains(lpparam.packageName+"/keylistener")){
            return;
        }
        try{
            Class contextWrapperClass = XposedHelpers.findClass("android.content.ContextWrapper", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(contextWrapperClass, "startActivity", Intent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Intent intent = (Intent)param.args[0];
                        String action = intent.getAction();
                        Set<String> sets = intent.getCategories();
                        if (sets!=null&&sets.contains(Intent.CATEGORY_HOME)){
                            if (Intent.ACTION_MAIN.equals(action)&&intent.getComponent()==null){
                                Intent intent1 = new Intent("com.click369.control.keylistener");
                                intent1.putExtra("reason","homekey");
                                ((Context)(param.thisObject)).sendBroadcast(intent1);
                                //                        XposedBridge.log("^^^^^^^^^^^^^HOOK keylistener1 homekey00  "+action+"^^^^^^^^^^^^^^^");
                            }
                        }
                    }catch (RuntimeException e){
                        XposedBridge.log("^^^^^^^^^^^^^HOOK keylistener 出错"+e+"^^^^^^^^^^^^^^^");
                    }
//                XposedBridge.log("^^^^^^^^^^^^^HOOK keylistener1 homekey11  "+action+"^^^^^^^^^^^^^^^");
                }
            });
            final Class accessCls = XposedHelpers.findClass("android.accessibilityservice.AccessibilityService",lpparam.classLoader);
            XposedHelpers.findAndHookMethod(accessCls, "performGlobalAction", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        int action = (Integer) methodHookParam.args[0];
                        Intent intent = new Intent("com.click369.control.keylistener");
                        if(action == AccessibilityService.GLOBAL_ACTION_HOME){
                            intent.putExtra("reason","homekey");
                            ((Context)(methodHookParam.thisObject)).sendBroadcast(intent);
                        }else if (action == AccessibilityService.GLOBAL_ACTION_RECENTS){
                            intent.putExtra("reason","recent");
                            ((Context)(methodHookParam.thisObject)).sendBroadcast(intent);
                        }
//                    XposedBridge.log("^^^^^^^^^^^^^HOOK keylistener2 "+intent.getStringExtra("reason")+action+"^^^^^^^^^^^^^^^");
                    }catch (RuntimeException e){
                        XposedBridge.log("^^^^^^^^^^^^^HOOK keylistener 出错"+e+"^^^^^^^^^^^^^^^");
                    }
                }
            });
        }catch (RuntimeException e){
            e.printStackTrace();
        }
    }
}
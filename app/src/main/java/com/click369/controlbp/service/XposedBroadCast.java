package com.click369.controlbp.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedBroadCast {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences controlPrefs,final XSharedPreferences muBeiPrefs,final boolean isOneOpen,final boolean isTwoOpen,final boolean isMubeiStopBroad) {
        if (lpparam.packageName.equals("android")) {
            try {
                final Class brCls = XposedHelpers.findClass(" com.android.server.am.BroadcastRecord", lpparam.classLoader);
                Constructor css[] = brCls.getDeclaredConstructors();
                if(css!=null){
                    Class clss[] = null;
                    for(Constructor con:css){
                        if(con.getParameterTypes().length>10){
                            clss = con.getParameterTypes();
                            break;
                        }
                    }
                    XC_MethodHook hook1 = new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                            try {
                                int index = methodHookParam.args[10] instanceof List ?10:methodHookParam.args[11] instanceof List?11:-1;
                                if(index!=-1){
                                    if(isMubeiStopBroad&&isTwoOpen) {
                                        muBeiPrefs.reload();
                                    }
                                    if(isOneOpen) {
                                        controlPrefs.reload();
                                    }
                                    List receivers = (List)methodHookParam.args[index];
                                    if(receivers!=null&&receivers.size()>0){
//                                    XposedBridge.log("CONTROL -----BroadcastRecord "+receivers.get(0).getClass().getName());
                                        Set removes = new HashSet();
                                        for(Object o:receivers){
                                            if (o.getClass().getName().contains("BroadcastFilter")){
                                                Field nameFiled= o.getClass().getDeclaredField("packageName");
                                                nameFiled.setAccessible(true);
                                                String name = (String)nameFiled.get(o);
                                                if ((isTwoOpen&&muBeiPrefs.getInt(name + "/service", -1)==0&&isMubeiStopBroad)||
                                                        (isOneOpen&&controlPrefs.getBoolean(name+"/broad",false))){
                                                    removes.add(o);
                                                }
                                            }else if(o instanceof ResolveInfo ){
                                                ActivityInfo info = ((ResolveInfo)o).activityInfo;
                                                String name = info!=null?info.packageName:"";
                                                if ((isTwoOpen&&muBeiPrefs.getInt(name + "/service", -1)==0&&isMubeiStopBroad)||
                                                        (isOneOpen&&controlPrefs.getBoolean(name+"/broad",false))){
                                                    removes.add(o);
                                                }
                                            }
                                        }
                                        receivers.removeAll(removes);
                                    }
                                }
                            }catch (RuntimeException e){
                                e.printStackTrace();
                            }
                        }
                    };
                    if (clss!=null){
                        if(clss.length == 19){
                            XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],hook1);
                        }else if (clss.length == 20){
                            XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],hook1);
                        }else if(clss.length == 21){
                            XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],clss[18],clss[19],clss[20],hook1);
                        }else if(clss.length == 18){
                            XposedHelpers.findAndHookConstructor(brCls,clss[0], clss[1], clss[2], clss[3], clss[4], clss[5], clss[6], clss[7], clss[8], clss[9], clss[10],clss[11],clss[12],clss[13],clss[14],clss[15],clss[16],clss[17],hook1);
                        }else {
                            XposedBridge.log("CONTROL BroadcastRecord "+clss.length+"未找到");
                        }
                    }else{
                        XposedBridge.log("CONTROL BroadcastRecord 未找到0");
                    }
                }else{
                    XposedBridge.log("CONTROL BroadcastRecord 未找到1");
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
}

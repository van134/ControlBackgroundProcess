package com.click369.controlbp.service;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedDialog {
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences dialogPrefs){
        try {
            try {
                final Class dialogcls = XposedHelpers.findClass("android.app.Dialog",lpparam.classLoader);
                XposedHelpers.findAndHookMethod(dialogcls, "show", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    try {
                        Dialog dialog =(Dialog)(methodHookParam.thisObject);
                        Field mDecorField = dialogcls.getDeclaredField("mDecor");
                        Field mContextField = dialogcls.getDeclaredField("mContext");
                        mDecorField.setAccessible(true);
                        mContextField.setAccessible(true);
                        Context context = (Context)(mContextField.get(methodHookParam.thisObject));
                        View mDecor = (View)(mDecorField.get(methodHookParam.thisObject));
                        String pkg = context.getPackageName();
                        if (!Common.PACKAGENAME.equals(pkg)&&mDecor!=null){//&&!"com.tencent.mm".equals(pkg)
                            dialogPrefs.reload();
                            Set<String> keywords = dialogPrefs.getStringSet(Common.PREFS_SKIPDIALOG_KEYWORDS,new LinkedHashSet<String>());
                            if (keywords.size()>0){
                                boolean isContains = false;

                                for(String s:keywords){
                                    ArrayList<View> views = new ArrayList<View>();
                                    String text = "";
                                    String apkName = "";
                                    if(s.contains("@")){
                                        String ss[] = s.split("@");
                                        apkName = ss[0];
                                        text = ss[1];
                                    }else{
                                        text = s;
                                    }
                                    mDecor.findViewsWithText(views,text,View.FIND_VIEWS_WITH_TEXT);
                                    if(views.size()>0){
                                        isContains = true;
                                        if(apkName.length()>0){
                                            String appName = null;
                                            try {
                                                PackageManager pm = context.getPackageManager();
                                                PackageInfo packageInfo = pm.getPackageInfo(pkg,PackageManager.GET_GIDS);
                                                if(packageInfo.applicationInfo!=null&&packageInfo.applicationInfo.loadLabel(pm)!=null){
                                                    appName = packageInfo.applicationInfo.loadLabel(pm).toString();
                                                }
                                            }catch (Exception e){
                                                appName = null;
                                            }
                                            if(apkName.equals(appName)){
                                                break;
                                            }else{
                                                isContains = false;
                                            }
                                        }else{
                                            break;
                                        }
                                    }
                                }
                                if(isContains){
                                    ArrayList<View> views = new ArrayList<View>();
                                    mDecor.findViewsWithText(views,"wxposed",View.FIND_VIEWS_WITH_TEXT);
                                    if( views.size()>0){
                                        isContains = false;
                                    }
                                    if(isContains) {
                                        mDecor.findViewsWithText(views, "qxposed", View.FIND_VIEWS_WITH_TEXT);
                                        if( views.size()>0){
                                            isContains = false;
                                        }
                                    }
                                }

                                if(isContains){
                                    dialog.setOnCancelListener(null);
                                    dialog.setOnDismissListener(null);
                                    dialog.setCanceledOnTouchOutside(true);
                                    dialog.setCancelable(true);
                                    dialog.dismiss();
                                    boolean isShow = dialogPrefs.getBoolean(Common.PREFS_SKIPDIALOG_ISSHOWTOAST,true);
                                    if(isShow){
                                        Toast.makeText(context,"对话框被拦截，若要显示请到应用控制器中设置",Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        }
                    }catch (Throwable e){
                        e.printStackTrace();
                    }
                    }
                });
            }catch (Throwable e){
                e.printStackTrace();
            }
        } catch (Throwable e){
            e.printStackTrace();
        }
    }
}
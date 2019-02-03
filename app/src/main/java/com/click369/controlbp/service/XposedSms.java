package com.click369.controlbp.service;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.telephony.NeighboringCellInfo;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.click369.controlbp.common.Common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by asus on 2017/10/30.
 */
public class XposedSms {
    private static final String TARGET_PACKAGE = "com.android.mms.service";
    private static final String TARGET_PACKAGE1 = "com.android.mms";
    private static Context cxt;
    private static boolean isGetCode = false;
    private static boolean isGetCodetoEdit = false;
    private static boolean isControlMsg = false;
    private static HashMap<String,String> config = new HashMap<String,String>();
    public static void loadPackage(final XC_LoadPackage.LoadPackageParam lpparam,final XSharedPreferences settingPrefs){
        if (TARGET_PACKAGE.equals(lpparam.packageName)||TARGET_PACKAGE1.equals(lpparam.packageName)) {
            try {
                isGetCode = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_MMS_ISGETCODE,false);
                isGetCodetoEdit = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_MMS_ISGETCODETOEDIT,false);
                isControlMsg = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_MMS_ISCONTROLMSG,false);
                if (!isGetCode&&!isGetCodetoEdit&&!isControlMsg){
                    return;
                }
                final Class mmsCls = XposedHelpers.findClass("com.android.mms.service.MmsService",lpparam.classLoader);
                final Class smsBaseCls = XposedHelpers.findClass("com.android.internal.telephony.SmsMessageBase",lpparam.classLoader);
                XposedUtil.hookMethod(mmsCls, XposedUtil.getParmsByName(mmsCls,"onCreate"),"onCreate", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            cxt = (Context)param.thisObject;
                            BroadcastReceiver br = new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    try {
                                        String ac = intent.getAction();
                                        if("com.click369.control.mms.changeconfig".equals(ac)){
                                            if(intent.hasExtra("isGetCode")){
                                                isGetCode = intent.getBooleanExtra("isGetCode",false);
                                            }
                                            if(intent.hasExtra("isGetCodetoEdit")){
                                                isGetCodetoEdit = intent.getBooleanExtra("isGetCodetoEdit",false);
                                            }
                                            if(intent.hasExtra("isControlMsg")){
                                                isControlMsg = intent.getBooleanExtra("isControlMsg",false);
                                            }
                                            if(intent.hasExtra("msgconfig")){
                                                Object o = intent.getSerializableExtra("msgconfig");
                                                if(o instanceof HashMap){
                                                    HashMap<String,String> hm = (HashMap<String,String>)o;
                                                    config.clear();
                                                    config.putAll(hm);
                                                }
                                            }
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            };
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction("com.click369.control.mms.changeconfig");
                            if(cxt!=null&&cxt instanceof Service){
                                cxt.registerReceiver(br,intentFilter);
                            }
//                            XposedBridge.log(lpparam.packageName+"=========MmsService  cxt"+cxt);
                        }catch (Throwable e){
                            e.printStackTrace();
                        }
                    }
                });
                XposedUtil.hookMethod(smsBaseCls, XposedUtil.getParmsByName(smsBaseCls, "getMessageBody"), "getMessageBody", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            if(isControlMsg||isGetCode||isGetCodetoEdit){
                                Field f = smsBaseCls.getDeclaredField("mMessageBody");
                                f.setAccessible(true);
                                String msg = (String)f.get(param.thisObject);

                                Field f1 = smsBaseCls.getDeclaredField("mScTimeMillis");
                                f1.setAccessible(true);
                                long time = (long)f1.get(param.thisObject);

                                Field f2 = smsBaseCls.getDeclaredField("mStatusOnIcc");
                                f2.setAccessible(true);
                                int mStatusOnIcc = (int)f2.get(param.thisObject);

                                Method methodNum = smsBaseCls.getDeclaredMethod("getOriginatingAddress");
                                methodNum.setAccessible(true);
                                String num = (String)methodNum.invoke(param.thisObject);
//                                XposedBridge.log("time "+time+"  mStatusOnIcc "+mStatusOnIcc+"  msg "+msg);
                                if(msg!=null){
                                    if((isGetCode||isGetCodetoEdit)&&(mStatusOnIcc!= SmsManager.STATUS_ON_ICC_READ)){
                                        getCode(cxt,msg);
                                    }
                                    if(isControlMsg){
                                        if(config.containsKey(msg)){
                                            String cmd = config.get(msg);
                                            if(cxt!=null){
                                                Intent intent = new Intent("com.click369.control.msgcmd");
                                                intent.putExtra("cmd",cmd);
                                                cxt.sendBroadcast(intent);
                                            }
                                            Field fpdu = smsBaseCls.getDeclaredField("mPdu");
                                            fpdu.setAccessible(true);
                                            fpdu.set(param.thisObject,new byte[]{});

                                            f.set(param.thisObject,"");
                                            param.setResult("");
                                            return;
                                        }
                                    }
                                }
                            }


//                            XposedBridge.log(lpparam.packageName+"=========SmsMessageBase  msg"+msg+"   num "+num+" cxt "+cxt);
//                            if(msg!=null&&msg.contains("您的通用余额")){
//                                if(cxt!=null){
//                                    Intent intent = new Intent("com.click369.control.getmsg");
//                                    intent.putExtra("msg",msg);
//                                    cxt.sendBroadcast(intent);
//                                }
//                                Field fpdu = smsBaseCls.getDeclaredField("mPdu");
//                                fpdu.setAccessible(true);
//                                fpdu.set(param.thisObject,null);
//
//                                f.set(param.thisObject,null);
//                                param.setResult(null);
//                                return;
//                            }
                        } catch (Throwable e) {
                            XposedBridge.log(e);
                        }
                    }
                });
            }catch (Throwable e){
                e.printStackTrace();
                XposedBridge.log(lpparam.packageName+"=========msg  error"+e.getMessage());
            }
        }
    }

//    public static void test(Context context){
//        String body = "【滴滴出行】您正在登录，短信验证码(604759)，5分钟内有效。转发可能导致帐号被盗，请勿泄露给他人";
//        Pattern pattern1 = Pattern.compile("(\\d{6})");//提取六位数字
//        Matcher matcher1 = pattern1.matcher(body);//进行匹配
//
//        Pattern pattern2 = Pattern.compile("(\\d{4})");//提取四位数字
//        Matcher matcher2 = pattern2.matcher(body);//进行匹配
//        String code = "";
//        if (matcher1.find()) {//匹配成功
//            code = matcher1.group(0);
//                Toast.makeText(context, "验证码已复制到粘贴板"+code, Toast.LENGTH_SHORT).show();
//        } else if (matcher2.find()) {
//            code = matcher2.group(0);
//                Toast.makeText(context, "验证码已复制到粘贴板"+code, Toast.LENGTH_SHORT).show();
//        }
//    }
    private static HashSet<String> msgs = new HashSet<String>();
    private static void getCode(Context context, String body) {
//        XposedBridge.log("=========SmsMessageBase  context "+context+"   body "+body);
        if(context==null||body==null||msgs.contains(body)||!(body.contains("验证码")||body.toLowerCase().contains("verification"))){
            return;
        }
        if(msgs.size()>100){
            msgs.clear();
        }
        msgs.add(body);
        try {
            //获取剪贴板管理器：
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);

            Pattern pattern1 = Pattern.compile("(\\d{6})");//提取六位数字
            Matcher matcher1 = pattern1.matcher(body);//进行匹配

            Pattern pattern2 = Pattern.compile("(\\d{4})");//提取四位数字
            Matcher matcher2 = pattern2.matcher(body);//进行匹配
            String code = "";
            if (matcher1.find()) {//匹配成功
                code = matcher1.group(0);
                if(!isGetCodetoEdit){
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", code);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(context, "验证码已复制到粘贴板", Toast.LENGTH_SHORT).show();
                }
            } else if (matcher2.find()) {
                code = matcher2.group(0);
                if(!isGetCodetoEdit) {
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", code);
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);

                    Toast.makeText(context, "验证码已复制到粘贴板", Toast.LENGTH_SHORT).show();
                }
            }
//            XposedBridge.log("=========SmsMessageBase  body "+body+"   code "+code);
            if(isGetCodetoEdit&&code!=null&&code.length()>0){
                Intent intentFilter = new Intent("com.click369.control.setmsgcode");
                intentFilter.putExtra("code",code);
                context.sendBroadcast(intentFilter);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
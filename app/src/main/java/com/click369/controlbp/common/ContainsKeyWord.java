package com.click369.controlbp.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by asus on 2017/10/12.
 */
public class ContainsKeyWord {
    static HashSet<String> blackStopApp = new HashSet<String>();
    static HashSet<String> notStopApp = new HashSet<String>();
    static HashSet<String> notListenerApp = new HashSet<String>();
    static HashSet<String> notLockAppPage = new HashSet<String>();
//    static HashSet<String> fullCpuCoreClass = new HashSet<String>();
    public static HashMap<String,ArrayList<String>> fullCpuCoreApp = new HashMap<String,ArrayList<String>>();
    public final static HashSet<String> notWakeLock = new HashSet<String>();
    static ArrayList<String> words = new ArrayList<String>();
    static {
        words.clear();
        notStopApp.clear();
        notListenerApp.clear();
        notWakeLock.clear();
        notLockAppPage.clear();
        words.add("pushservice");
        words.add("messagehandle");
        words.add("getui");
        words.add("gexin");
        words.add("mipush");
        words.add("xmpush");
        words.add("xmjobservice");
        words.add("daemonservice");
        words.add("hwpush");
        words.add("channel");
        words.add("pushgt");
        words.add("xmpushservice");
        words.add("xgpushservice");
        words.add("xgremoteservice");
        words.add("umengintentservice");
        words.add("umenglocalnotificationservice");
        words.add("umengmessagecallbackhandlerservice");
        words.add("umengmessageintentreceiverservice");
        words.add("umengservice");
        words.add("pollservice");
        words.add("debugservice");
        words.add("bugreportservice");
        words.add("getuiextservice");
        words.add("yolanda");
        words.add("analyticeservice");
        words.add("defaultservice");
        words.add("nbcacheservice");
        words.add("xstateservice");
        words.add("openudid");
        words.add("moplusservice");
        words.add("rollfloatservice");
        words.add("electionservice");
        words.add("ixintui");
        words.add("agoo");
        words.add(".ads.");

        notStopApp.add("com.fkzhang.wechatxposed");
        notStopApp.add("com.cyanogenmod.lockclock");
        notStopApp.add("com.google.android.deskclock");
        notStopApp.add("com.click369.controlbp");
        notStopApp.add("com.fkzhang.qqxposed");
        notStopApp.add("com.android.webview");
        notStopApp.add("com.google.android.webview");
        notStopApp.add("com.android.providers.media");

        notListenerApp.add("com.fkzhang.wechatxposed");
        notListenerApp.add("com.fkzhang.qqxposed");
        notListenerApp.add("com.blanke.mdwechat");
        notListenerApp.add("eu.chainfire.supersu");
        notListenerApp.add("com.topjohnwu.magisk");
        notListenerApp.add("com.android.webview");
        notListenerApp.add("com.google.android.webview");
        notListenerApp.add("com.google.android.gms");
        notListenerApp.add("com.miui.contentcatcher");
        notListenerApp.add("com.android.keyguard");
        notListenerApp.add("com.yaerin.xposed.hide");
        notListenerApp.add("android");

        blackStopApp.add("com.qihoo.appstore");

        notWakeLock.add("*alarm*");
        notWakeLock.add("*dexopt*");
        notWakeLock.add("*launch*");
        notWakeLock.add("*vibrator*");
        notWakeLock.add("AlarmAlertWakeLock");

        notLockAppPage.add("com.click369.controlbp.activity.UnLockActivity");
        notLockAppPage.add("com.click369.controlbp.activity.RunningActivity");
        notLockAppPage.add("com.alipay.android.phone.wallet.buscode.BusCodeActivity");
        notLockAppPage.add("com.alipay.mobile.quinox.LauncherActivity");
        notLockAppPage.add("com.android.webview");
        notLockAppPage.add("com.google.android.webview");

        ArrayList<String> full1 = new ArrayList<String>();
        full1.add("com.tencent.mm.plugin.scanner.ui.BaseScanUI");
        full1.add("com.tencent.mm.plugin.mmsight.ui.SightCaptureUI");
        full1.add("com.tencent.mm.plugin.scanner.ui.SingleTopScanUI");
        fullCpuCoreApp.put("com.tencent.mm",full1);
        ArrayList<String> full2 = new ArrayList<String>();
        full2.add("com.alipay.mobile.scan.as.main.MainCaptureActivity");
        full2.add("com.alipay.mobile.chatapp.ui.video.VideoRecordActivity_");
        fullCpuCoreApp.put("om.eg.android.AlipayGphone",full2);
        ArrayList<String> full3 = new ArrayList<String>();
        full3.add("com.ss.android.ugc.aweme.shortvideo.ui.VideoRecordNewActivity");
        fullCpuCoreApp.put("com.ss.android.ugc.aweme",full3);
        ArrayList<String> full4 = new ArrayList<String>();
        full4.add("com.tencent.mobileqq.richmedia.capture.activity.CameraCaptureActivity");
        fullCpuCoreApp.put("com.tencent.mobileqq",full4);
    }

    public static boolean isContainsWord(String str){
        str = str.toLowerCase();
        for(String s:words){
            if(str.contains(s)){
                return true;
            }
        }
        return false;
    }

    public static boolean isContainsPkg(String str){
        str = str.toLowerCase();
        for(String s:notStopApp){
            if(str.contains(s)){
                return true;
            }
        }
        return false;
    }

    public static boolean isContainsBlackPkg(String str){
        str = str.toLowerCase();
        for(String s:blackStopApp){
            if(str.contains(s)){
                return true;
            }
        }
        return false;
    }

    public static boolean isContainsNotListenerApk(String apk){
        return notListenerApp.contains(apk);
    }

    public static boolean isContainsNotLockApk(String apk){
        return notLockAppPage.contains(apk);
    }
}

package com.click369.controlbp.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by asus on 2017/10/31.
 */
public class AllWhiteList {
    public static HashMap<String,String> whiteList = new HashMap<String,String>();
    static {
        whiteList.put("com.tencent.mm","处理该应用后可能消息会有延迟，或收不到消息，如果出现问题请恢复处理");
        whiteList.put("com.fkzhang.wechatxposed","处理该应用后可能微信消息会有延迟，或收不到消息，如果出现问题请恢复处理");
        whiteList.put("com.tencent.mobileqq","处理该应用后可能消息会有延迟，或收不到消息，如果出现问题请恢复处理");
        whiteList.put("com.tencent.qqlite","处理该应用后可能消息会有延迟，或收不到消息，如果出现问题请恢复处理");
        whiteList.put("com.android.webview","处理该应用后会影响到所有依赖网页的应用，建议不要处理");
        whiteList.put("org.cyanogenmod.audiofx","处理该应用后会影响到声音播放功能，建议不要处理");
        whiteList.put("com.cyanogenmod.lockclock","处理该应用后会影响时间显示和闹钟，建议不要处理");
        whiteList.put("com.android.messaging","处理该应用后会影响短信接收发送，建议不要处理");
        whiteList.put("com.android.systemui","处理该应用后会影响整个系统界面，建议不要处理");
        whiteList.put("com.android.setting","处理该应用后会影系统设置功能，建议不要处理");
        whiteList.put("cyanogenmod.platform","很重要，建议不要处理");
        whiteList.put("org.cyanogenmod.cmsettings","很重要，建议不要处理");
        whiteList.put("com.android.location.fused","处理该应用后会导致定位失败，建议不要处理");
        whiteList.put("com.android.providers.media","处理该应用后会导致系统媒体无法读取，建议不要处理");
        whiteList.put("com.google.android.deskclock","处理该应用后会影响时间显示和闹钟，建议不要处理");
        whiteList.put("com.android.dialer","处理该应用后会影响电话呼叫，建议不要处理");
        whiteList.put("com.android.phone","处理该应用后会导致无限重启，请不要处理");
        whiteList.put("com.android.server.telecom","处理该应用后会导电话呼叫异常，请不要处理");
    }
}

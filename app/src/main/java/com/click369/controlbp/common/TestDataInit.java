package com.click369.controlbp.common;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by asus on 2017/11/2.
 */
public class TestDataInit {
    public static void  init(Context cxt){
//        WatchDogService.launcherPkgs.clear();
//        WatchDogService.launcherPkgs.addAll(WatchDogService.getLauncherPackageName(cxt));
        SharedPreferences sp = SharedPrefsUtil.getPreferences(cxt,Common.IPREFS_COLORBARTEST);// cxt.getSharedPreferences(Common.IPREFS_COLORBARTEST,Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor e = sp.edit();
        e.clear().commit();
        e.putInt("time1",200);
        e.putInt("time2",3000);
        e.putInt("time3",800);
//        e.putInt("time4",1500);
        e.putBoolean("com.ruanmei.ithome/trans",true);
        e.putBoolean("com.netease.cloudmusic/trans",true);
        e.putBoolean("com.ss.android.article.news/trans",true);
        e.putBoolean("com.baidu.tieba/trans",true);
        e.putBoolean("com.tencent.tim/trans",true);
        e.putBoolean("com.taobao.taobao/trans",true);

//        e.putBoolean("com.baidu.BaiduMap/dark",true);
//        e.putBoolean("com.taobao.idlefish/dark",true);
//        e.putBoolean("com.tencent.qqlive/dark",true);

        //悬浮助手 简悬浮
        e.putBoolean("com.bs.smarttouch/keylistener",true);
        e.putBoolean("com.bs.smarttouchpro/keylistener",true);
        e.putBoolean("com.hardwork.fg607.relaxfinger/keylistener",true);
        e.putBoolean("com.hardwork.fg607.floatassistant/keylistener",true);
        e.putBoolean("com.jozein.xedgepro/keylistener",true);
        e.putBoolean("com.jozein.xedge/keylistener",true);
        //悬浮菜单
        e.putBoolean("com.ksxkq.floating/keylistener",true);

        e.putBoolean("com.android.settings/nottopbar",true);
        e.putBoolean("com.tencent.mobileqq/nottopbar",true);
        e.putBoolean("com.taobao.idlefish/nottopbar",true);

//        e.putInt("com.tencent.qqmobile/topcolor", Color.TRANSPARENT);
//        e.putInt("com.tencent.qqmobile/bottomcolor", Color.WHITE);

        HashSet<String> list = PackageUtil.getLauncherPackageName(cxt);
        for(String p:list){
            e.putBoolean(p+"/blacklist",true);
        }
        ArrayList<String> ime = PackageUtil.getInputPackageName(cxt);
        for(String p:ime){
            e.putBoolean(p+"/ime",true);
        }
        e.putBoolean("com.android.settings/blacklist",true);
        e.putBoolean("com.android.systemui/blacklist",true);
        e.putBoolean("com.android.webview/blacklist",true);
        e.putBoolean("com.google.android.webview/blacklist",true);
        e.putBoolean("com.lbe.security.miui/blacklist",true);
        e.putBoolean("com.fkzhang.wechatxposed/blacklist",true);
        e.putBoolean("android/blacklist",true);
        e.putBoolean("com.android.packageinstaller/blacklist",true);
//        e.putBoolean("com.tencent.mobileqq/notchangeicon",true);
//        e.putBoolean("com.netease.cloudmusic/notchangeicon",true);
//        e.putBoolean("com.taobao.idlefish/notchangeicon",true);
//        e.putBoolean("com.baidu.BaiduMap/notchangeicon",true);
//        e.putBoolean("com.tencent.qqlive/notchangeicon",true);


//        e.putBoolean("com.netease.cloudmusic.service.PlayService/service",true);
//        e.putBoolean("com.netease.cloudmusic.service.PlayNannyService/service",true);

//        e.putBoolean("com.smile.gifmaker/checkautostart",true);
//        e.putString("com.smile.gifmaker/homeactivity","com.yxcorp.gifshow.HomeActivity");

        e.commit();
        SharedPreferences autoStartPrefs = SharedPrefsUtil.getPreferences(cxt,Common.PREFS_AUTOSTARTNAME);// cxt.getSharedPreferences(Common.IPREFS_COLORBARTEST,Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor autostart = autoStartPrefs.edit();
        autostart.putBoolean("com.tencent.android.qqdownloader/checkautostart",true);
        autostart.putString("com.tencent.android.qqdownloader/jumpactivity","com.tencent.pangu.link.LinkProxyActivity");
        autostart.putBoolean("com.tencent.mtt/checkautostart",true);
        autostart.putString("com.tencent.mtt/jumpactivity","com.tencent.mtt.external.reader.thirdcall.ThirdCallDispatchActivity");
        autostart.putBoolean("com.taobao.trip/checkautostart",true);
        autostart.putString("com.taobao.trip/homeactivity","com.alipay.mobile.quinox.LauncherActivity");
        autostart.putBoolean("cn.goapk.market/checkautostart",true);
        autostart.putString("cn.goapk.market/jumpactivity","cn.goapk.market.AnZhiGeTuiActivity");
        autostart.commit();

        SharedPreferences controlPrefs = SharedPrefsUtil.getPreferences(cxt,Common.PREFS_SETTINGNAME);// cxt.getSharedPreferences(Common.IPREFS_COLORBARTEST,Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor controls = controlPrefs.edit();
        controls.putBoolean("com.netease.cloudmusic.service.PlayService/service",true);
        controls.putBoolean("com.netease.cloudmusic.service.PlayNannyService/service",true);
        controls.putBoolean("com.tencent.mm.booter.NotifyReceiver$NotifyService/service",true);
        controls.commit();

//        SharedPreferences tvPrefs = SharedPrefsUtil.getPreferences(cxt,Common.IPREFS_TVLIST);// cxt.getSharedPreferences(Common.IPREFS_COLORBARTEST,Context.MODE_WORLD_READABLE);
//        SharedPreferences.Editor tvEdit = tvPrefs.edit();
//        tvEdit.clear();
//        tvEdit.putBoolean("com.tencent.mm",true);
//        tvEdit.putString("com.tencent.mm/van","我");
//        tvEdit.putString("com.tencent.mm/微信","WECHAT");
//        tvEdit.putString("com.tencent.mm/朋友圈","圈子");
//        tvEdit.commit();

//        SharedPreferences spTest = SharedPrefsUtil.getPreferences(cxt,"retest");// cxt.getSharedPreferences(Common.IPREFS_COLORBARTEST,Context.MODE_WORLD_READABLE);
//        SharedPreferences.Editor eTest = spTest.edit();
//        SharedPreferences pmTest = SharedPrefsUtil.getPreferences(cxt,Common.IPREFS_PMLIST);// cxt.getSharedPreferences(Common.IPREFS_COLORBARTEST,Context.MODE_WORLD_READABLE);
//        SharedPreferences.Editor pmEd = pmTest.edit();
//        pmEd.clear().commit();
//        pmEd.putBoolean("com.One.WoodenLetter/notunstall",true).commit();
//        try {
//            ActivityManager am =(ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
//            Class cls = am.getClass();
//            Method ms[] = cls.getDeclaredMethods();
//            for(Method m:ms){
//                LinkedHashSet<String> ls = new LinkedHashSet<String>();
//                Class clss[] = m.getParameterTypes();
//                if (clss.length>0){
//                    for (Class c:clss){
//                        ls.add(c.getName());
//                    }
//                }
//                eTest.putStringSet(m.getName(),ls).commit();
//            }
//        } catch (Exception e1) {
//            e1.printStackTrace();
//        }
    }

    public static void initAD(Context cxt){
        SharedPreferences adPrefs = SharedPrefsUtil.getPreferences(cxt,Common.IPREFS_ADLIST);// cxt.getSharedPreferences(Common.IPREFS_COLORBARTEST,Context.MODE_WORLD_READABLE);
        SharedPreferences.Editor adEdit = adPrefs.edit();
        //网易新闻
        adEdit.putInt("com.netease.newsreader.activity/ad",1);
        adEdit.putString("com.netease.newsreader.activity/one","com.netease.nr.biz.ad.AdActivity");
        adEdit.putString("com.netease.newsreader.activity/two","com.netease.nr.phone.main.MainActivity");
        //同花顺
        adEdit.putInt("com.hexin.plat.android/ad",1);
        adEdit.putString("com.hexin.plat.android/one","com.hexin.plat.android.AndroidLogoActivity");
        adEdit.putString("com.hexin.plat.android/two","com.hexin.plat.android.Hexin");
        //网易云
        adEdit.putInt("com.netease.cloudmusic/ad",1);
        adEdit.putString("com.netease.cloudmusic/one","com.netease.cloudmusic.activity.LoadingActivity");
        adEdit.putString("com.netease.cloudmusic/two","com.netease.cloudmusic.activity.MainActivity");
        //斗鱼
        adEdit.putInt("air.tv.douyu.android/ad",1);
        adEdit.putString("air.tv.douyu.android/one","tv.douyu.view.activity.SplashActivity");
        adEdit.putString("air.tv.douyu.android/two","tv.douyu.view.activity.MainActivity");
        //手机营业厅（联通）
        adEdit.putInt("com.sinovatech.unicom.ui/ad",1);
        adEdit.putString("com.sinovatech.unicom.ui/one","com.sinovatech.unicom.ui.WelcomeClient");
        adEdit.putString("com.sinovatech.unicom.ui/two","com.sinovatech.unicom.basic.ui.MainActivity");
        //美团
        adEdit.putInt("com.sankuai.meituan/ad",1);
        adEdit.putString("com.sankuai.meituan/one","com.sankuai.meituan.startup.StartupActivity");
        adEdit.putString("com.sankuai.meituan/two","com.sankuai.meituan.activity.MainActivity");
        //追书神器
        adEdit.putInt("com.ushaqi.zhuishushenqi/ad",1);
        adEdit.putString("com.ushaqi.zhuishushenqi/one","com.ushaqi.zhuishushenqi.ui.SplashActivity");
        adEdit.putString("com.ushaqi.zhuishushenqi/two","com.ushaqi.zhuishushenqi.ui.home.HomeActivity");
        //饿了么
        adEdit.putInt("me.ele/ad",1);
        adEdit.putString("me.ele/one","me.ele.Launcher");
        adEdit.putString("me.ele/two","me.ele.wq");
        //招商银行
        adEdit.putInt("cmb.pb/ad",1);
        adEdit.putString("cmb.pb/one","cmb.pb.ui.PBInitActivity");
        adEdit.putString("cmb.pb/two","cmb.pb.mainframe.PBEntryActivity");
        //掌上公交
        adEdit.putInt("com.mygolbs.mybus/ad",1);
        adEdit.putString("com.mygolbs.mybus/one","com.mygolbs.mybus.LoginActivity");
        adEdit.putString("com.mygolbs.mybus/two","com.mygolbs.mybus.MainTabHostActivity");
        //咸鱼
        adEdit.putInt("com.taobao.idlefish/ad",1);
        adEdit.putString("com.taobao.idlefish/one","com.taobao.fleamarket.home.activity.AdvertActivity");
        adEdit.putString("com.taobao.idlefish/two","com.taobao.fleamarket.home.activity.MainActivity");
        //电影天堂
        adEdit.putInt("com.ghost.movieheaven/ad",1);
        adEdit.putString("com.ghost.movieheaven/one","com.dianping.movieheaven.activity.SplashActivity");
        adEdit.putString("com.ghost.movieheaven/two","com.dianping.movieheaven.activity.MainActivity");
        //腾讯视频
        adEdit.putString("com.tencent.qqlive/one","com.tencent.qqlive.ona.activity.WelcomeActivity");
        adEdit.putString("com.tencent.qqlive/two","com.tencent.qqlive.ona.activity.HomeActivity");
        //美图秀秀
        adEdit.putInt("com.mt.mtxx.mtxx/ad",1);
        adEdit.putString("com.mt.mtxx.mtxx/one","com.meitu.business.ads.core.activity.AdActivity");
        adEdit.putString("com.mt.mtxx.mtxx/two","com.meitu.mtxx.MainActivity");
        //中国联通手机营业厅
        adEdit.putInt("com.sinovatech.unicom.ui/ad",1);
        adEdit.putString("com.sinovatech.unicom.ui/one","com.sinovatech.unicom.ui.WelcomeClient");
        adEdit.putString("com.sinovatech.unicom.ui/two","com.sinovatech.unicom.basic.ui.MainActivity");
        //NGA
        adEdit.putInt("gov.pianzong.androidnga/ad",1);
        adEdit.putString("gov.pianzong.androidnga/one","gov.pianzong.androidnga.activity.LoadingActivity");
        adEdit.putString("gov.pianzong.androidnga/two","gov.pianzong.androidnga.activity.home.HomeActivity");
        //邮储银行
        adEdit.putInt("com.yitong.mbank.psbc/ad",1);
        adEdit.putString("com.yitong.mbank.psbc/one","com.yitong.mbank.psbc.android.activity.SplashActivity");
        adEdit.putString("com.yitong.mbank.psbc/two","com.yitong.mbank.psbc.android.activity.MainActivity");
        //今日头条
        adEdit.putInt("com.ss.android.article.news/ad",1);
        adEdit.putString("com.ss.android.article.news/one","com.ss.android.article.news.activity.SplashBadgeActivity");
        adEdit.putString("com.ss.android.article.news/two","com.ss.android.article.news.activity.MainActivity");
        //京东
        adEdit.putInt("com.jingdong.app.mall/ad",3);
        adEdit.commit();
    }
}

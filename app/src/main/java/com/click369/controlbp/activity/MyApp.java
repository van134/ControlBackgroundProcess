package com.click369.controlbp.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.click369.controlbp.util.MyCrashHandler;
import com.click369.controlbp.util.SharedPrefsUtil;

/**
 * Created by asus on 2017/10/27.
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyCrashHandler crashHandler = MyCrashHandler.getInstance();
        crashHandler.init(this);
//        SharedPrefsUtil.getInstance(this);
    }
}
/*
新增新应用自动加入待机
适配更多系统待机功能
长按设置定时关闭功能新增永久性设置
显示时钟图标
设置中加入定时强退的应用取消时必须用指纹验证
修复全选为待机后再选择取消选择失效问题
杂项以及长按图标shortcut增加重启选项快捷方式

修复无法还原界面控制和增强唤醒锁定时器的数据
*/
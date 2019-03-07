package com.click369.controlbp.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.MyCrashHandler;
import com.click369.controlbp.util.SharedPrefsUtil;

/**
 * Created by asus on 2017/10/27.
 */
public class MyApp extends BaseApp {
    @Override
    public void onCreate() {
        super.onCreate();
        SharedPrefsUtil.getInstance(this);
        AppLoaderUtil.getInstance(this);
        MyCrashHandler crashHandler = MyCrashHandler.getInstance();
        crashHandler.init(this);
//        SharedPrefsUtil.getInstance(this);
    }
}

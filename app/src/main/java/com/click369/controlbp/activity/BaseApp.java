package com.click369.controlbp.activity;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.MyCrashHandler;
import com.click369.controlbp.util.SharedPrefsUtil;

import dalvik.system.DexClassLoader;

/**
 * Created by asus on 2017/10/27.
 */
public class BaseApp extends MultiDexApplication {
    public BaseApp() {
    }
    @Override
    protected void attachBaseContext(Context context){
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}

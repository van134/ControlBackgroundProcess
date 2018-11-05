package com.click369.controlbp.service;

import android.app.Activity;
import android.app.Application;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Environment;
import android.os.Handler;

import com.click369.controlbp.common.Common;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class ControlService implements IXposedHookZygoteInit, IXposedHookLoadPackage{
//	private static final String TAG = ControlService.class.getSimpleName();
	private XSharedPreferences controlPrefs,wakeLockPrefs,alarmPrefs,settingPrefs,autoStartPrefs,barPrefs,recentPrefs,dozePrefs;
	private XSharedPreferences pmPrefs,testPrefs,adPrefs,tvPrefs,muBeiPrefs;
	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam paramStartupParam) throws Throwable {
//		loadPrefs();
		initData();
//		SharedPreferences modPrefs = getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_PRIVATE);
	}

	private void initData(){
		settingPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_APPSETTINGS);
		controlPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_SETTINGNAME);
		wakeLockPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_WAKELOCKNAME);
		alarmPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_ALARMNAME);
//		autoStartPrefs = new XSharedPreferences(Environment.getExternalStorageDirectory()+ File.separator+"processcontrol"+File.separator+"files"+File.separator+Common.PREFS_AUTOSTARTNAME+".xml");
		autoStartPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_AUTOSTARTNAME);
		barPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_UIBARLIST);
		testPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_COLORBARTEST);
		recentPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_RECENTLIST);
		dozePrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_DOZELIST);
		adPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_ADLIST);
		tvPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_TVLIST);
		pmPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_PMLIST);
		muBeiPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_MUBEILIST);
		controlPrefs.makeWorldReadable();
		wakeLockPrefs.makeWorldReadable();
		alarmPrefs.makeWorldReadable();
		autoStartPrefs.makeWorldReadable();
		barPrefs.makeWorldReadable();
		testPrefs.makeWorldReadable();
		settingPrefs.makeWorldReadable();
		recentPrefs.makeWorldReadable();
		dozePrefs.makeWorldReadable();
		adPrefs.makeWorldReadable();
		tvPrefs.makeWorldReadable();
		pmPrefs.makeWorldReadable();
		muBeiPrefs.makeWorldReadable();
	}

    @Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		try {

			if (lpparam.packageName.equals("com.click369.controlbp")) {
				XposedHelpers.findAndHookMethod("com.click369.controlbp.activity.MainActivity", lpparam.classLoader,
						"isModuleActive", XC_MethodReplacement.returnConstant(true));
			}

			settingPrefs.reload();
			XposedAMS.loadPackage(lpparam,settingPrefs,controlPrefs,autoStartPrefs,muBeiPrefs,recentPrefs);

			if (settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true)){
				XposedAppStart.loadPackage(lpparam, autoStartPrefs);
			}
			boolean isOneOpen = settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
			boolean isTwoOpen = settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
			if (isOneOpen||isTwoOpen){
//				boolean isMubeiStopBroad = settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPRECEIVER,false);
//				XposedService.loadPackage(lpparam, controlPrefs,wakeLockPrefs,muBeiPrefs,isOneOpen,isTwoOpen,isMubeiStopBroad);
				if(wakeLockPrefs.getBoolean(Common.PREFS_SETTING_WAKELOCK_LOOK, false)){
					XposedWakeLock.loadPackage(lpparam, controlPrefs,wakeLockPrefs,muBeiPrefs,isOneOpen,isTwoOpen);
				}
				if(alarmPrefs.getBoolean(Common.PREFS_SETTING_ALARM_LOOK,false)){
					XposedAlarm.loadPackage(lpparam, controlPrefs,alarmPrefs,muBeiPrefs,isOneOpen,isTwoOpen);
				}
//				XposedBroadCast.loadPackage(lpparam, controlPrefs, muBeiPrefs, isOneOpen, isTwoOpen, isMubeiStopBroad);
			}
			if (settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true)){
				XposedAccessKeyListener.loadPackage(lpparam,testPrefs);
			}
			boolean isUIChangeOpen = settingPrefs.getBoolean(Common.ALLSWITCH_EIGHT,true);
			boolean isRecentOpen = settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
			XposedRencent.loadPackage(lpparam, recentPrefs,barPrefs,muBeiPrefs,autoStartPrefs,isRecentOpen,isUIChangeOpen);

			if (settingPrefs.getBoolean(Common.ALLSWITCH_SIX,true)){
				XposedPackageManager.loadPackage(lpparam, pmPrefs);
			}
			if (settingPrefs.getBoolean(Common.ALLSWITCH_SEVEN,true)){
				XposedDoze.loadPackage(lpparam, dozePrefs);
			}
			//如果不用辅助服务 则用hook形式处理
			if (settingPrefs.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true)){
				XposedStartListener.loadPackage(lpparam);
			}

			if (isUIChangeOpen){
				XposedBar.loadPackage(lpparam, barPrefs, testPrefs,settingPrefs);
				XposedToast.loadPackage(lpparam, barPrefs);
			}
			if(settingPrefs.getBoolean(Common.ALLSWITCH_NINE,true)){
				XposedAD.loadPackage(lpparam,adPrefs);
			}
			if(tvPrefs!=null){
				XposedTextView.loadPackage(lpparam,tvPrefs);
			}

			XposedEnd.loadPackage(lpparam,settingPrefs,autoStartPrefs,controlPrefs,muBeiPrefs);
		}catch (RuntimeException e){
			XposedBridge.log("^^^^^^^^^^^^^重要！！！ MAIN  HOOK出错"+e+"^^^^^^^^^^^^^^^");
		}
	}

}
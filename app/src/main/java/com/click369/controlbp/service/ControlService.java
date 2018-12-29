package com.click369.controlbp.service;

import com.click369.controlbp.common.Common;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class ControlService implements IXposedHookZygoteInit, IXposedHookLoadPackage{//,IXposedHookInitPackageResources
	private XSharedPreferences controlPrefs,
		wakeLockPrefs,alarmPrefs,settingPrefs,
		autoStartPrefs,barPrefs,recentPrefs,dozePrefs,
	    pmPrefs,testPrefs,adPrefs,tvPrefs,dialogPrefs;
	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam paramStartupParam) throws Throwable {
		initData();
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
		dialogPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_SKIPDIALOG);
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
//		muBeiPrefs.makeWorldReadable();
		dialogPrefs.makeWorldReadable();
	}

//	@Override
//	public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
//		if (resparam.packageName.equals("com.click369.xlivepaper")) {
//			XposedBridge.log("CONTROL UPDATE string color");
//			resparam.res.setReplacement("com.click369.xlivepaper", "string", "app_name", "APP CONTROL");
//			resparam.res.setReplacement("com.click369.xlivepaper", "color", "colorPrimary", "#FF0000");
//			resparam.res.setReplacement("com.click369.xlivepaper", "color", "colorPrimary", "#FF0000");
//		}
//	}

    @Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		try {

			if (lpparam.packageName.equals("com.click369.controlbp")) {
				XposedHelpers.findAndHookMethod("com.click369.controlbp.activity.MainActivity", lpparam.classLoader,
						"isModuleActive", XC_MethodReplacement.returnConstant(true));
//				XposedHelpers.findAndHookMethod("com.click369.controlbp.activity.MainActivity", lpparam.classLoader,
//						"getActivatedModuleVersion", XC_MethodReplacement.returnConstant(BuildConfig.VERSION_CODE));
			}

			settingPrefs.reload();
			XposedAMS.loadPackage(lpparam,settingPrefs,controlPrefs,autoStartPrefs,recentPrefs,barPrefs,dialogPrefs);
			XposedMedia.loadPackage(lpparam);
			if (settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true)){
				XposedAppStart.loadPackage(lpparam, autoStartPrefs);
			}
			boolean isOneOpen = settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true);
			boolean isTwoOpen = settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true);
			if (isOneOpen||isTwoOpen){
				boolean isMubeiStopOther = settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false);
				XposedWakeLock.loadPackage(lpparam, controlPrefs,wakeLockPrefs,isOneOpen,isTwoOpen,isMubeiStopOther);
				XposedAlarm.loadPackage(lpparam, controlPrefs,alarmPrefs,isOneOpen,isTwoOpen,isMubeiStopOther);
			}
			if (settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true)){
				XposedAccessKeyListener.loadPackage(lpparam,testPrefs);
			}
			boolean isUIChangeOpen = settingPrefs.getBoolean(Common.ALLSWITCH_EIGHT,true);
			boolean isRecentOpen = settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true);
			XposedRencent.loadPackage(lpparam, recentPrefs,barPrefs,autoStartPrefs,isRecentOpen,isUIChangeOpen);

			if (settingPrefs.getBoolean(Common.ALLSWITCH_SIX,true)){
				XposedPackageManager.loadPackage(lpparam, pmPrefs);
			}
			if (settingPrefs.getBoolean(Common.ALLSWITCH_SEVEN,true)){
				XposedDoze.loadPackage(lpparam, dozePrefs);
			}
			//如果不用辅助服务 则用hook形式处理
			if (settingPrefs.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true)){
				XposedStartListenerNotify.loadPackage(lpparam);
			}

			if (isUIChangeOpen){
				XposedBar.loadPackage(lpparam, barPrefs, testPrefs,settingPrefs);
				XposedToast.loadPackage(lpparam, barPrefs);
			}
			if(settingPrefs.getBoolean(Common.ALLSWITCH_NINE,true)){
				XposedAD.loadPackage(lpparam,adPrefs);
				XposedDialog.loadPackage(lpparam,dialogPrefs);
			}
			if(tvPrefs!=null){
				XposedTextView.loadPackage(lpparam,tvPrefs);
			}
			boolean isLongClickOpenConfig = settingPrefs.getBoolean(Common.PREFS_SETTING_ISLONGCLICKOPENCONFIG,true);
			if(isLongClickOpenConfig){
				int w = settingPrefs.getInt(Common.PREFS_SETTING_SCREENWIDTH,0);
				int h = settingPrefs.getInt(Common.PREFS_SETTING_SCREENHEIGHT,0);
				XposedActivity.loadPackage(lpparam,isLongClickOpenConfig,w,h);
			}
			XposedEnd.loadPackage(lpparam,settingPrefs,autoStartPrefs,controlPrefs);
//			XposedNotifyBackground.loadPackage(lpparam);
		}catch (Throwable e){
			XposedBridge.log(lpparam.packageName+"^^^^^^^^^^^^^重要！！！ MAIN  HOOK出错"+e+"^^^^^^^^^^^^^^^");
		}
	}

}
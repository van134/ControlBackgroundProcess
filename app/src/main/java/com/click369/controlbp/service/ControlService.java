package com.click369.controlbp.service;

import com.click369.controlbp.common.Common;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;


public class ControlService implements IXposedHookZygoteInit, IXposedHookLoadPackage {//,IXposedHookInitPackageResources
	private XSharedPreferences controlPrefs,
		wakeLockPrefs,alarmPrefs,settingPrefs,
		autoStartPrefs,barPrefs,recentPrefs,dozePrefs,
	    pmPrefs,testPrefs,adPrefs,tvPrefs,dialogPrefs,
			xpBlackListPrefs,privacyPrefs;
	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam paramStartupParam) throws Throwable {
		initData();
	}

	private void initData(){
		settingPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_APPSETTINGS);
		controlPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_SETTINGNAME);
		wakeLockPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_WAKELOCKNAME);
		alarmPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_ALARMNAME);
		autoStartPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_AUTOSTARTNAME);
		barPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_UIBARLIST);
		testPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_COLORBARTEST);
		recentPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_RECENTLIST);
		dozePrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_DOZELIST);
		adPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_ADLIST);
		tvPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_TVLIST);
		pmPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_PMLIST);
		dialogPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_SKIPDIALOG);
		xpBlackListPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_XPBLACKLIST);
		privacyPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_PRIVACY);
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
		dialogPrefs.makeWorldReadable();
		xpBlackListPrefs.makeWorldReadable();
		privacyPrefs.makeWorldReadable();
	}

//	@Override
//	public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
//		if (resparam.packageName.equals("com.android.systemui")) {
//			XposedBridge.log("CONTROL UPDATE notification_divider_height");
//			resparam.res.setReplacement("com.android.systemui", "dimen", "notification_divider_height", "0dp");
//			resparam.res.setReplacement("com.android.systemui", "dimen", "notification_divider_height_increased", "0dp");
//			resparam.res.setReplacement("com.android.systemui", "dimen", "notification_panel_margin_bottom", "0dp");
//			resparam.res.setReplacement("com.android.systemui", "dimen", "notification_panel_margin_bottom", "0dp");
////			resparam.res.setReplacement("com.click369.xlivepaper", "color", "colorPrimary", "#FF0000");
////			resparam.res.setReplacement("com.click369.xlivepaper", "color", "colorPrimary", "#FF0000");
//		}
//	}

    @Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
		try {
			if(lpparam.packageName.startsWith("com.fkzhang")){
				return;
			}
			if (lpparam.packageName.equals("com.click369.controlbp")) {
				XposedHelpers.findAndHookMethod("com.click369.controlbp.activity.MainActivity", lpparam.classLoader,
						"isModuleActive", XC_MethodReplacement.returnConstant(true));
//				settingPrefs.reload();
//				boolean isUIChangeOpen = settingPrefs.getBoolean(Common.ALLSWITCH_UI,true);
//				if(isUIChangeOpen){
//					XposedToast.loadPackage(lpparam, barPrefs);
//				}
//				return;
			}
			settingPrefs.reload();
			xpBlackListPrefs.reload();
			boolean isBlackXpOpen = settingPrefs.getBoolean(Common.ALLSWITCH_XPBLACKLIST,true);
			if(isBlackXpOpen&&xpBlackListPrefs.getBoolean(lpparam.packageName+"/contorlxpblack",false)){
				return;
			}

			XposedAMS.loadPackage(lpparam,settingPrefs,controlPrefs,autoStartPrefs,recentPrefs,barPrefs,dialogPrefs);
			XposedMedia.loadPackage(lpparam);
			if (settingPrefs.getBoolean(Common.ALLSWITCH_AUTOSTART_LOCK,true)){
				XposedAppStart.loadPackage(lpparam, autoStartPrefs);
			}

			boolean isOneOpen = settingPrefs.getBoolean(Common.ALLSWITCH_SERVICE_BROAD,true);
			if (isOneOpen){
				XposedWakeLock.loadPackage(lpparam, controlPrefs,wakeLockPrefs,isOneOpen);
				XposedAlarm.loadPackage(lpparam, controlPrefs,alarmPrefs,isOneOpen);
			}

			if (settingPrefs.getBoolean(Common.ALLSWITCH_BACKSTOP_MUBEI,true)){
				XposedAccessKeyListener.loadPackage(lpparam,testPrefs);
			}

			boolean isUIChangeOpen = settingPrefs.getBoolean(Common.ALLSWITCH_UI,true);
			boolean isRecentOpen = settingPrefs.getBoolean(Common.ALLSWITCH_RECNETCARD,true);
			XposedRencent.loadPackage(lpparam, recentPrefs,barPrefs,autoStartPrefs,isRecentOpen,isUIChangeOpen);

			if (settingPrefs.getBoolean(Common.ALLSWITCH_UNINSTALL_ICE,true)){
				XposedPackageManager.loadPackage(lpparam, pmPrefs);
			}
			if (settingPrefs.getBoolean(Common.ALLSWITCH_DOZE,true)){
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
			if(settingPrefs.getBoolean(Common.ALLSWITCH_ADSKIP,true)){
				XposedAD.loadPackage(lpparam,adPrefs);
				XposedDialog.loadPackage(lpparam,dialogPrefs);
			}
			if(settingPrefs.getBoolean(Common.ALLSWITCH_PRIVACY,true)){
				XposedPrivacy.loadPackage(lpparam,privacyPrefs);
			}
			if(tvPrefs!=null){
				XposedTextView.loadPackage(lpparam,tvPrefs);
			}
			boolean isLongClickOpenConfig = settingPrefs.getBoolean(Common.PREFS_SETTING_ISLONGCLICKOPENCONFIG,false);
			boolean isGetCodetoEdit = settingPrefs.getBoolean(Common.PREFS_SETTING_OTHER_MMS_ISGETCODETOEDIT,false);
			if(isLongClickOpenConfig||isGetCodetoEdit){
				int w = settingPrefs.getInt(Common.PREFS_SETTING_SCREENWIDTH,0);
				int h = settingPrefs.getInt(Common.PREFS_SETTING_SCREENHEIGHT,0);
				XposedActivity.loadPackage(lpparam,isGetCodetoEdit,isLongClickOpenConfig,w,h);
			}


			boolean isOtherOpen = settingPrefs.getBoolean(Common.ALLSWITCH_OTHERS,true);
//			if(isOtherOpen&&(lpparam.packageName.equals("com.tencent.mm")||
//					lpparam.packageName.equals("com.autonavi.minimap")||
//					lpparam.packageName.equals("com.tencent.qqlite")||
//					lpparam.packageName.equals("com.sdu.didi.gsui")||
//					lpparam.packageName.equals("com.sdu.didi.psnger")||
//					lpparam.packageName.equals("com.tencent.mobileqq"))){
//				XposedLocation.loadPackage(lpparam,settingPrefs);
//			}
			if(isOtherOpen){
				XposedSms.loadPackage(lpparam,settingPrefs);
			}

			if(isBlackXpOpen){
				XposedBlackList.loadPackage(lpparam,xpBlackListPrefs);
			}
			XposedEnd.loadPackage(lpparam,settingPrefs);
		}catch (Throwable e){
			XposedBridge.log(lpparam.packageName+"^^^^^^^^^^^^^重要！！！ MAIN  HOOK出错"+e+"^^^^^^^^^^^^^^^");
		}
	}

}
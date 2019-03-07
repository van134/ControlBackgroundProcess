package com.click369.controlbp.service;

import com.click369.controlbp.common.Common;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
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
		try {
			if(settingPrefs==null){
				settingPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_APPSETTINGS);
				settingPrefs.makeWorldReadable();
			}
			if(controlPrefs==null) {
				controlPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_SETTINGNAME);
				controlPrefs.makeWorldReadable();
			}
			if(wakeLockPrefs==null) {
				wakeLockPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_WAKELOCKNAME);
				wakeLockPrefs.makeWorldReadable();
			}
			if(alarmPrefs == null){
				alarmPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.PREFS_ALARMNAME);
				alarmPrefs.makeWorldReadable();
			}
			if(autoStartPrefs == null) {
				autoStartPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_AUTOSTARTNAME);
				autoStartPrefs.makeWorldReadable();
			}
			if(barPrefs == null) {
				barPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_UIBARLIST);
				barPrefs.makeWorldReadable();
			}
			if(testPrefs == null){
				testPrefs = new XSharedPreferences(Common.PACKAGENAME,Common.IPREFS_COLORBARTEST);
				testPrefs.makeWorldReadable();
			}
			if(recentPrefs == null) {
				recentPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.IPREFS_RECENTLIST);
				recentPrefs.makeWorldReadable();
			}
			if(dozePrefs == null) {
				dozePrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_DOZELIST);
				dozePrefs.makeWorldReadable();
			}
			if(adPrefs == null) {
				adPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.IPREFS_ADLIST);
				adPrefs.makeWorldReadable();
			}
			if(tvPrefs == null) {
				tvPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.IPREFS_TVLIST);
				tvPrefs.makeWorldReadable();
			}
			if(pmPrefs == null) {
				pmPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.IPREFS_PMLIST);
				pmPrefs.makeWorldReadable();
			}
			if(dialogPrefs == null) {
				dialogPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_SKIPDIALOG);
				dialogPrefs.makeWorldReadable();
			}
			if(xpBlackListPrefs == null) {
				xpBlackListPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_XPBLACKLIST);
				xpBlackListPrefs.makeWorldReadable();
			}
			if(privacyPrefs == null) {
				privacyPrefs = new XSharedPreferences(Common.PACKAGENAME, Common.PREFS_PRIVACY);
				privacyPrefs.makeWorldReadable();
			}
		}catch (Throwable arg1){
			arg1.printStackTrace();
			XposedBridge.log("重要！！！CONTROL_初始化共享参数出错 "+XposedUtil.getErroInfo(arg1));
		}
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
			if (lpparam == null || lpparam.packageName == null || lpparam.packageName.startsWith("com.fkzhang")) {
				return;
			}
//			initData();
			if ("com.click369.controlbp".equals(lpparam.packageName)) {
				XposedHelpers.findAndHookMethod("com.click369.controlbp.activity.MainActivity", lpparam.classLoader,
						"isModuleActive", XC_MethodReplacement.returnConstant(true));
			}
			XposedBroadCast.loadPackage(lpparam);
			if (settingPrefs != null) {
				settingPrefs.reload();
			} else {
				XposedBridge.log("重要！！！CONTROL_共享参数settingPrefs为空");
			}
			if (xpBlackListPrefs != null) {
				xpBlackListPrefs.reload();
			} else {
				XposedBridge.log("重要！！！CONTROL_共享参数xpBlackListPrefs为空");
			}
			boolean isBlackXpOpen = settingPrefs.getBoolean(Common.ALLSWITCH_XPBLACKLIST, true);
			if (isBlackXpOpen&&xpBlackListPrefs!=null && xpBlackListPrefs.getBoolean(lpparam.packageName + "/contorlxpblack", false)) {
				return;
			}
			if ("com.android.systemui".equals(lpparam.packageName)||
					"android".equals(lpparam.packageName)) {

				XposedAMS.loadPackage(lpparam, settingPrefs, controlPrefs, autoStartPrefs, recentPrefs, barPrefs, dialogPrefs);

				boolean isUIChangeOpen = settingPrefs.getBoolean(Common.ALLSWITCH_UI,true);
				boolean isRecentOpen = settingPrefs.getBoolean(Common.ALLSWITCH_RECNETCARD,true);
				if(recentPrefs!=null){
					XposedRencent.loadPackage(lpparam, recentPrefs,barPrefs,autoStartPrefs,isRecentOpen,isUIChangeOpen);
				}

				if (settingPrefs.getBoolean(Common.ALLSWITCH_UNINSTALL_ICE,true)&&pmPrefs!=null){
					XposedPackageManager.loadPackage(lpparam, pmPrefs);
				}

				if (settingPrefs.getBoolean(Common.ALLSWITCH_DOZE,true)&&dozePrefs!=null){
					XposedDoze.loadPackage(lpparam, dozePrefs);
				}

				XposedMedia.loadPackage(lpparam);
			}

			boolean isAutoStartOpen = settingPrefs.getBoolean(Common.ALLSWITCH_AUTOSTART_LOCK,true);
			//如果不用辅助服务 则用hook形式处理
			boolean isNotNeedAccess = settingPrefs.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true);
//			boolean isBackStopOpen = settingPrefs.getBoolean(Common.ALLSWITCH_BACKSTOP_MUBEI,true);
//			if (isAutoStartOpen||isBackStopOpen){
				XposedAppStartNotifyListener.loadPackage(lpparam, autoStartPrefs,isAutoStartOpen,isNotNeedAccess);
//			}

			boolean isOneOpen = settingPrefs.getBoolean(Common.ALLSWITCH_SERVICE_BROAD,true);
			if (isOneOpen&&controlPrefs!=null){
				XposedWakeLock.loadPackage(lpparam, controlPrefs,wakeLockPrefs,isOneOpen);
				XposedAlarm.loadPackage(lpparam, controlPrefs,alarmPrefs,isOneOpen);
			}

			if (settingPrefs.getBoolean(Common.ALLSWITCH_BACKSTOP_MUBEI,true)){
				XposedAccessKeyListener.loadPackage(lpparam,testPrefs);
			}

			//如果不用辅助服务 则用hook形式处理
//			if (settingPrefs.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true)){
//				XposedStartListenerNotify.loadPackage(lpparam);
//			}

			if (settingPrefs.getBoolean(Common.ALLSWITCH_UI,true)){
				if(barPrefs!=null){
					XposedBar.loadPackage(lpparam, barPrefs, testPrefs,settingPrefs);
				}
				if(barPrefs!=null){
					XposedToast.loadPackage(lpparam, barPrefs);
				}
			}
			if(settingPrefs.getBoolean(Common.ALLSWITCH_ADSKIP,true)){
				if(adPrefs!=null){
					XposedAD.loadPackage(lpparam,adPrefs);
				}
				if(dialogPrefs!=null){
					XposedDialog.loadPackage(lpparam,dialogPrefs);
				}
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
			if(isOtherOpen){
				XposedSms.loadPackage(lpparam,settingPrefs);
			}
			if(isBlackXpOpen&&xpBlackListPrefs!=null){
				XposedBlackList.loadPackage(lpparam,xpBlackListPrefs);
			}
			if(settingPrefs.getBoolean(Common.ALLSWITCH_PRIVACY,true)&&privacyPrefs!=null){
				XposedPrivacy.loadPackage(lpparam,privacyPrefs);
			}
			XposedEnd.loadPackage(lpparam,settingPrefs);
		}catch (Throwable arg1){
			arg1.printStackTrace();
			XposedBridge.log("重要！！！CONTROL_出错"+lpparam.packageName+"  "+XposedUtil.getErroInfo(arg1));
		}
	}

}
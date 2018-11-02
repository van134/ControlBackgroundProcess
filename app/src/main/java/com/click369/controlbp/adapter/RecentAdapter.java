package com.click369.controlbp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.ControlFragment;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.RecentFragment;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.PinyinCompare;

import java.util.ArrayList;
import java.util.Collections;

public class RecentAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Activity c;
	private SharedPreferences modPrefs,appStartPrefs;
	public String fliterName = "u";
	private AppInfo myAi =null;

//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public RecentAdapter(Activity context, SharedPreferences modPrefs, SharedPreferences appStartPrefs) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.modPrefs = modPrefs;
		this.appStartPrefs = appStartPrefs;
		try {
			PackageInfo packgeInfo = context.getPackageManager().getPackageInfo(Common.PACKAGENAME, PackageManager.GET_ACTIVITIES);
			String appName = packgeInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
			Drawable d = packgeInfo.applicationInfo.loadIcon(context.getPackageManager());
			myAi = new AppInfo(appName, Common.PACKAGENAME,AppLoaderUtil.zoomDrawable(d,60,60),(packgeInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0,!packgeInfo.applicationInfo.enabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setData(ArrayList<AppInfo> datas){
		bjdatas.clear();
		bjdatas.addAll(datas);
//		this.notifyDataSetChanged();
		freshList();
	}

	public void fliterList(String name,ArrayList<AppInfo> apps){
		Log.i("CONTROL","name   "+name);
		fliterName =name;
		if(name.length()>0){
			bjdatas.clear();
			if(name.toLowerCase().equals("u")){
				for(AppInfo ai :apps){
					if(ai.isUser){
						bjdatas.add(ai);
					}
				}
			}else if(name.toLowerCase().equals("s")){
				for(AppInfo ai :apps){
					if(!ai.isUser){
						bjdatas.add(ai);
					}
				}
			}else{
				for(AppInfo ai :apps){
					if(ai.getAppName().toLowerCase().contains(name.trim().toLowerCase())
							||ai.getPackageName().toLowerCase().contains(name.trim().toLowerCase())){
						bjdatas.add(ai);
					}
				}
			}
		}else{
			bjdatas.clear();
			bjdatas.addAll(apps);
		}
		freshList();
	}
	public void setSortType(int sortType){
		this.sortType = sortType;
		freshList();
	}
	public void freshList(){
		if (this.bjdatas.size()>0&&this.bjdatas.get(0).getPackageName().equals(Common.PACKAGENAME)){
			this.bjdatas.remove(0);
		}else if(this.bjdatas.contains(myAi)){
			this.bjdatas.remove(myAi);
		}

		PinyinCompare comparent = new PinyinCompare();
		Collections.sort(this.bjdatas, comparent);
//		if (sortType == -1){
//			this.notifyDataSetChanged();
//			return;
//		}
//		String exs[] = {"/service","/wakelock","/alarm"};
////		Collections.sort(this.choosedatas, comparent);
		ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNoChoose = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNewApp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempRun = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.isRecentNotClean){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isRecentForceClean){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&ai.isRecentBlur){
				this.bjdatas.add(ai);
			}else if(sortType == 3&&ai.isRecentNotShow){
				this.bjdatas.add(ai);
			}else{
				if(ai.isRunning){
					tempRun.add(ai);
				}else{
					if (System.currentTimeMillis() - ai.instanllTime<1000*60*60*12&&System.currentTimeMillis() - ai.instanllTime>1000){
						tempNewApp.add(ai);
					}else{
						tempNoChoose.add(ai);
					}
				}
			}
		}
		this.bjdatas.addAll(tempNewApp);
		this.bjdatas.addAll(tempRun);
		this.bjdatas.addAll(tempNoChoose);
		this.bjdatas.add(0,myAi);
		myAi.isRunning = true;
		myAi.isRecentNotClean = modPrefs.getBoolean(myAi.packageName+"/notclean",false);
		myAi.isRecentBlur = modPrefs.getBoolean(myAi.packageName+"/blur",false);
		myAi.isRecentNotShow = modPrefs.getBoolean(myAi.packageName+"/notshow",false);
		this.notifyDataSetChanged();
	}
	public void chooseAll(){
//		choosedatas.clear();
//		choosedatas.addAll(this.bjdatas);
		this.notifyDataSetChanged();
	}
	public void cancelchooseAll(){
//		choosedatas.clear();
//		ActivityTZZF.chooseInfo.addAll(studatas);
		this.notifyDataSetChanged();
	}

	public int getCount() {
		return bjdatas.size();
	}

	
	public Object getItem(int position) {
		return bjdatas.get(position);
	}

	
	public long getItemId(int position) {
		return position;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		AppInfo data = bjdatas.get(position);
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_recent, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_recent_appname);
			viewHolder.notCleanIv = (ImageView) convertView.findViewById(R.id.item_recent_notclean);
			viewHolder.forceCleanIv = (ImageView)convertView.findViewById(R.id.item_recent_forcestop);
			viewHolder.blurImgIv = (ImageView)convertView.findViewById(R.id.item_recent_blur);
			viewHolder.notshowIv = (ImageView)convertView.findViewById(R.id.item_recent_notshow);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_recent_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_recent_iceicon);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.appNameTv.setText(data.appName);
		viewHolder.appNameTv.setTextColor(data.isRunning?(data.isInMuBei?Color.parseColor(MainActivity.COLOR_MUBEI):(MainActivity.pkgIdleStates.contains(data.packageName)?Color.parseColor(MainActivity.COLOR_IDLE):Color.parseColor(MainActivity.COLOR_RUN))):(data.isDisable?Color.LTGRAY: ControlFragment.curColor));
		viewHolder.appIcon.setImageBitmap(data.getBitmap());
		viewHolder.iceIv.setImageResource(data.isDisable?R.mipmap.ice: WatchDogService.setTimeStopApp.containsKey(data.packageName)?R.mipmap.icon_clock:R.mipmap.empty);
		viewHolder.appNameTv.setTag(position);
		viewHolder.notCleanIv.setTag(position);
		viewHolder.forceCleanIv.setTag(position);
		viewHolder.blurImgIv.setTag(position);
		viewHolder.notshowIv.setTag(position);
		if (data.equals(myAi)) {
			viewHolder.forceCleanIv.setEnabled(false);
			viewHolder.forceCleanIv.setAlpha(0.3f);
		}else {
			viewHolder.forceCleanIv.setEnabled(true);
			viewHolder.forceCleanIv.setAlpha(1.0f);
		}
		viewHolder.notCleanIv.setImageResource(data.isRecentNotClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.forceCleanIv.setImageResource(data.isRecentForceClean?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.blurImgIv.setImageResource(data.isRecentBlur?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.notshowIv.setImageResource(data.isRecentNotShow?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		if(!MainActivity.isModuleActive()){
			convertView.setAlpha(0.5f);
		}
		viewHolder.notCleanIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/notclean",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ai.isRecentNotClean = false;
					ed.remove(ai.getPackageName()+"/notclean");
					if (MainActivity.isLinkRecentAndNotStop&&ai.isNotStop){
						SharedPreferences.Editor ed1 = appStartPrefs.edit();
						ed1.remove(ai.getPackageName() + "/notstop").commit();
						ai.isNotStop = false;
					}
				}else{
					ed.putBoolean(ai.getPackageName()+"/notclean",!isStop);
					ai.isRecentNotClean = true;
					if (MainActivity.isLinkRecentAndNotStop&&!ai.isNotStop){
						SharedPreferences.Editor ed1 = appStartPrefs.edit();
						ed1.putBoolean(ai.getPackageName() + "/notstop", true).commit();
						ai.isNotStop = true;
					}
					if (modPrefs.contains(ai.getPackageName()+"/forceclean")){
						ai.isRecentForceClean = false;
						ed.remove(ai.getPackageName()+"/forceclean");
						notifyDataSetChanged();
					}
				}
				ed.commit();
				buttonView.setImageResource(!isStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.forceCleanIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/forceclean",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ai.isRecentForceClean = false;
					ed.remove(ai.getPackageName()+"/forceclean");
					if(MainActivity.isLinkRecentAndAuto&&!ai.isBackForceStop&&!ai.isOffscForceStop){
						SharedPreferences.Editor ed1 = appStartPrefs.edit();
						ed1.remove(ai.getPackageName()+"/autostart").commit();
						ai.isAutoStart = false;
					}
				}else{
					ai.isRecentForceClean = true;
					ed.putBoolean(ai.getPackageName()+"/forceclean",!isStop);
					if (modPrefs.contains(ai.getPackageName()+"/notclean")){
						ai.isRecentNotClean = false;
						ed.remove(ai.getPackageName()+"/notclean");
						notifyDataSetChanged();
					}
					if(MainActivity.isLinkRecentAndAuto&&!ai.isAutoStart) {
						SharedPreferences.Editor ed1 = appStartPrefs.edit();
						ed1.putBoolean(ai.getPackageName() + "/autostart", true).commit();
						ai.isAutoStart = true;
					}
				}
				ed.commit();
				buttonView.setImageResource(!isStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.blurImgIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/blur",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ed.remove(ai.getPackageName()+"/blur");
				}else{
					ed.putBoolean(ai.getPackageName()+"/blur",!isStop);
				}
				ai.isRecentBlur = !isStop;
				ed.commit();
				buttonView.setImageResource(!isStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.notshowIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/notshow",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ed.putBoolean(ai.getPackageName()+"/notshow",false);
					if (ai.getPackageName().equals(Common.PACKAGENAME)) {
						Intent intent = new Intent("com.click369.control.ams.changerecent");
						intent.putExtra("pkg",ai.getPackageName());
						c.sendBroadcast(intent);
					}else{
						XposedStopApp.stopApk(ai.getPackageName(),c);
					}
				}else{
					ed.putBoolean(ai.getPackageName()+"/notshow",!isStop);
				}
				ai.isRecentNotShow = !isStop;
				ed.commit();
				buttonView.setImageResource(!isStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,notCleanIv,forceCleanIv,blurImgIv,notshowIv,iceIv;
	}


}

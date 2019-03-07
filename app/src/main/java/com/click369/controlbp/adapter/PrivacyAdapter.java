package com.click369.controlbp.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.EmptyActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.PrivacyControlActivity;
import com.click369.controlbp.activity.PrivacyLogActivity;
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.bean.WhiteApp;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.fragment.AppStartFragment;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShortCutUtil;

import java.util.ArrayList;
import java.util.Collections;

public class PrivacyAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Activity c;
	private SharedPreferences modPrefs;
	public String fliterName = "u";
//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public PrivacyAdapter(Activity context, SharedPreferences modPrefs) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.modPrefs = modPrefs;
	}

	public void setData(ArrayList<AppInfo> datas){
		bjdatas.clear();
		bjdatas.addAll(datas);
//		this.notifyDataSetChanged();
		freshList();
	}

	public void fliterList(String name,ArrayList<AppInfo> apps){
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
						if (TopSearchView.appType==2||(TopSearchView.appType==0&&ai.isUser)||(TopSearchView.appType==1&&!ai.isUser)) {
							bjdatas.add(ai);
						}
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
		ArrayList<AppInfo> tempDisableApp = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.isPriWifiPrevent){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isPriMobilePrevent){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&ai.isPriSwitchOpen){
				this.bjdatas.add(ai);
			}else{
				if(ai.isRunning){
					tempRun.add(ai);
				}else{
					if(ai.isDisable){
						tempDisableApp.add(ai);
						continue;
					}
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
		this.bjdatas.addAll(tempDisableApp);
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
			convertView= inflater.inflate(R.layout.item_privacy, null);
			viewHolder = new ViewHolder();

			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_main_appname);
			viewHolder.appTimeTv = (TextView)convertView.findViewById(R.id.item_main_apptime);
			viewHolder.priLogIv = (ImageView)convertView.findViewById(R.id.item_main_alarm);
			viewHolder.priControlIv = (ImageView)convertView.findViewById(R.id.item_provd_broad);
			viewHolder.priSwitchIv = (ImageView) convertView.findViewById(R.id.item_main_service);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_main_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_main_iceicon);
			viewHolder.wifiIv= (ImageView) convertView.findViewById(R.id.item_main_wifi);
			viewHolder.mobileIv= (ImageView) convertView.findViewById(R.id.item_main_mobile);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		int color = data.isRunning?(data.isInMuBei?Color.parseColor(MainActivity.COLOR_MUBEI):(MainActivity.pkgIdleStates.contains(data.packageName)?Color.parseColor(MainActivity.COLOR_IDLE):Color.parseColor(MainActivity.COLOR_RUN))):(data.isDisable?Color.LTGRAY: ControlFragment.curColor);
		viewHolder.appNameTv.setText(data.appName);
		viewHolder.appTimeTv.setText((data.isRunning?BaseActivity.getProcStartTimeStr(data.packageName)+"\n"+BaseActivity.getProcTimeStr(data.packageName):""));
		viewHolder.appTimeTv.setVisibility(data.isRunning?View.VISIBLE:View.GONE);
		viewHolder.appTimeTv.setTextColor(color);
		viewHolder.appNameTv.setTextColor(color);
		viewHolder.appIcon.setImageBitmap(AppLoaderUtil.allHMAppIcons.get(data.packageName));
//		File file = null;
//		if(BaseActivity.isLoadIcon||BaseActivity.loadeds.contains(data.packageName)){
//			BaseActivity.loadeds.add(data.packageName);
//			file = data.iconFile;
//		}
//		Glide.with(c).load(file).into(viewHolder.appIcon);
		viewHolder.iceIv.setImageResource(data.isDisable?R.mipmap.ice: data.isSetTimeStopApp?R.mipmap.icon_clock:R.mipmap.empty);
		viewHolder.appNameTv.setTag(position);
		viewHolder.priLogIv.setTag(position);
		viewHolder.priControlIv.setTag(position);
		viewHolder.priSwitchIv.setTag(position);
		viewHolder.wifiIv.setTag(position);
		viewHolder.mobileIv.setTag(position);
		if(data.isPriSwitchOpen){
			viewHolder.priControlIv.setEnabled(true);
			viewHolder.priLogIv.setEnabled(true);
			viewHolder.priLogIv.setAlpha(1.0f);
			viewHolder.priControlIv.setAlpha(1.0f);
		}else{
			viewHolder.priControlIv.setEnabled(false);
			viewHolder.priLogIv.setEnabled(false);
			viewHolder.priLogIv.setAlpha(0.5f);
			viewHolder.priControlIv.setAlpha(0.5f);
		}
		viewHolder.priSwitchIv.setImageResource(data.isPriSwitchOpen?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.wifiIv.setImageResource(data.isPriWifiPrevent?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		viewHolder.mobileIv.setImageResource(data.isPriMobilePrevent?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		if(!MainActivity.isModuleActive()){
			viewHolder.priControlIv.setEnabled(false);
			viewHolder.priLogIv.setEnabled(false);
			viewHolder.priSwitchIv.setEnabled(false);
			viewHolder.priLogIv.setAlpha(0.3f);
			viewHolder.priControlIv.setAlpha(0.3f);
			viewHolder.priSwitchIv.setAlpha(0.3f);
		}
		viewHolder.wifiIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				SharedPreferences.Editor ed = modPrefs.edit();
				if (ai.isPriWifiPrevent){
					ai.isPriWifiPrevent = false;
					ed.remove(ai.packageName+"/priwifi");
					Intent intent = new Intent("com.click369.control.ams.net.remove");
					intent.putExtra("uid",ai.uid);
					intent.putExtra("type","wifi");
					c.sendBroadcast(intent);
				}else {
					ai.isPriWifiPrevent = true;
					ed.putBoolean(ai.packageName+"/priwifi",true);
					Intent intent = new Intent("com.click369.control.ams.net.add");
					intent.putExtra("uid",ai.uid);
					intent.putExtra("type","wifi");
					c.sendBroadcast(intent);
				}
				ed.commit();
				buttonView.setImageResource(ai.isPriWifiPrevent?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.mobileIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				SharedPreferences.Editor ed = modPrefs.edit();
				if (ai.isPriMobilePrevent){
					ai.isPriMobilePrevent = false;
					ed.remove(ai.packageName+"/primobile");
					Intent intent = new Intent("com.click369.control.ams.net.remove");
					intent.putExtra("uid",ai.uid);
					intent.putExtra("type","mobile");
					c.sendBroadcast(intent);
				}else {
					ai.isPriMobilePrevent = true;
					ed.putBoolean(ai.packageName+"/primobile",true);
					Intent intent = new Intent("com.click369.control.ams.net.add");
					intent.putExtra("uid",ai.uid);
					intent.putExtra("type","mobile");
					c.sendBroadcast(intent);
				}
				ed.commit();
				buttonView.setImageResource(ai.isPriMobilePrevent?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.priSwitchIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(view);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				final SharedPreferences.Editor ed = modPrefs.edit();
				if(ai.isPriSwitchOpen){
					ai.isPriSwitchOpen = false;
					ed.remove(ai.getPackageName()+"/priswitch");
					ed.commit();
				}else{
					ai.isPriSwitchOpen = true;
					ed.putBoolean(ai.getPackageName()+"/priswitch",true);
					ed.commit();
				}
				XposedStopApp.onlyStopApk(ai.packageName,c);
				notifyDataSetChanged();
			}
		});
		viewHolder.priLogIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				BaseActivity.zhenDong(c);
				if(!ai.isPriSwitchOpen){
					AlertUtil.showAlertMsg(c,"请先打开总开关，打开后重启该应用才能看到记录");
					return;
				}
				Intent intent = new Intent(c, PrivacyLogActivity.class);
				intent.putExtra("pkg",ai.packageName);
				intent.putExtra("name",ai.appName);
				c.startActivity(intent);
			}
		});
		viewHolder.priControlIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				BaseActivity.zhenDong(c);
				if(!ai.isPriSwitchOpen){
					AlertUtil.showAlertMsg(c,"请先打开该应用总开关");
					return;
				}
				Intent intent = new Intent(c, PrivacyControlActivity.class);
				intent.putExtra("pkg",ai.packageName);
				intent.putExtra("name",ai.appName);
				c.startActivity(intent);
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv,appTimeTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,priLogIv,priControlIv,iceIv,priSwitchIv,wifiIv,mobileIv;

	}
}

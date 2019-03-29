package com.click369.controlbp.adapter;

import android.app.Activity;
import android.content.Context;
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
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.service.XposedUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.Collections;

public class XpBlackListAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Activity c;
	private SharedPreferences xpBlackListPrefs;
	public String fliterName = "u";

//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public XpBlackListAdapter(Activity context, SharedPreferences xpBlackListPrefs) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.xpBlackListPrefs = xpBlackListPrefs;
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
		ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNoChoose = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNewApp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempRun = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempDisableApp = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.isblackAllXp){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isblackControlXp){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&ai.isNoCheckXp){
				this.bjdatas.add(ai);
			}else if(sortType == 3&&ai.isSetCanHookXp){
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
			convertView= inflater.inflate(R.layout.item_recent, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_recent_appname);
			viewHolder.appTimeTv = (TextView)convertView.findViewById(R.id.item_main_apptime);
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
		viewHolder.notCleanIv.setTag(position);
		viewHolder.forceCleanIv.setTag(position);
		viewHolder.blurImgIv.setTag(position);
		viewHolder.notshowIv.setTag(position);
		viewHolder.notCleanIv.setImageResource(data.isblackAllXp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.forceCleanIv.setImageResource(data.isblackControlXp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.blurImgIv.setImageResource(data.isNoCheckXp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.notshowIv.setImageResource(data.isSetCanHookXp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
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
				boolean isOk = ai.isblackAllXp;
				reset(ai,xpBlackListPrefs,c);
				SharedPreferences.Editor ed = xpBlackListPrefs.edit();
				if(isOk){
					ai.isblackAllXp = false;
					ed.remove(ai.getPackageName()+"/allxpblack");
				}else{
					ed.remove(ai.getPackageName()+"/nocheckxp");
					ed.remove(ai.getPackageName()+"/setcanhook");
					ai.isNoCheckXp = false;
					ai.isSetCanHookXp = false;

					ed.putBoolean(ai.getPackageName()+"/allxpblack",true);
					ai.isblackAllXp = true;
				}
				ed.commit();
				XposedStopApp.onlyStopApk(ai.packageName,c);
				notifyDataSetChanged();
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
				boolean isOk = ai.isblackControlXp;
				reset(ai,xpBlackListPrefs,c);
				SharedPreferences.Editor ed = xpBlackListPrefs.edit();
				if(isOk){
					ai.isblackControlXp = false;
					ed.remove(ai.getPackageName()+"/contorlxpblack");
				}else{
					ed.remove(ai.getPackageName()+"/nocheckxp");
					ed.remove(ai.getPackageName()+"/setcanhook");
					ai.isNoCheckXp = false;
					ai.isSetCanHookXp = false;

					ed.putBoolean(ai.getPackageName()+"/contorlxpblack",true);
					ai.isblackControlXp = true;
				}
				ed.commit();
				XposedStopApp.onlyStopApk(ai.packageName,c);
				notifyDataSetChanged();
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
				boolean isOk = ai.isNoCheckXp;
//				reset(ai,xpBlackListPrefs,c);
				SharedPreferences.Editor ed = xpBlackListPrefs.edit();
				if(isOk){
					ai.isNoCheckXp = false;
					ed.remove(ai.getPackageName()+"/nocheckxp");
				}else{
					ed.remove(ai.getPackageName()+"/contorlxpblack");
					ed.remove(ai.getPackageName()+"/allxpblack");
					ai.isblackControlXp = false;
					ai.isblackAllXp = false;

					ed.putBoolean(ai.getPackageName()+"/nocheckxp",true);
					ai.isNoCheckXp = true;
				}
				ed.commit();
				XposedStopApp.onlyStopApk(ai.packageName,c);
				notifyDataSetChanged();
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
				boolean isOk = ai.isSetCanHookXp;
				SharedPreferences.Editor ed = xpBlackListPrefs.edit();
				if(isOk){
					ai.isSetCanHookXp = false;
					ed.remove(ai.getPackageName()+"/setcanhook");
				}else{
					ed.remove(ai.getPackageName()+"/contorlxpblack");
					ed.remove(ai.getPackageName()+"/allxpblack");
					ai.isblackControlXp = false;
					ai.isblackAllXp = false;
					ed.putBoolean(ai.getPackageName()+"/setcanhook",true);
					ai.isSetCanHookXp = true;
				}
				ed.commit();
				XposedStopApp.onlyStopApk(ai.packageName,c);
				notifyDataSetChanged();
			}
		});
		return convertView;
	}
	public static void reset(AppInfo ai, SharedPreferences xpBlackListPrefs, Context c){
		String pkg = ai.packageName;
		SharedPreferences.Editor ed = xpBlackListPrefs.edit();
		ed.remove(pkg+"/allxpblack");
		ed.remove(pkg+"/contorlxpblack");
//		ed.remove(pkg+"/nocheckxp");
//		ed.remove(pkg+"/setcanhook");
		ed.commit();
		ai.isblackAllXp = false;
		ai.isblackControlXp = false;
//		ai.isNoCheckXp = false;
//		ai.isSetCanHookXp = false;

	}
	static class ViewHolder{
		public TextView appNameTv,appTimeTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,notCleanIv,forceCleanIv,blurImgIv,notshowIv,iceIv;
	}


}

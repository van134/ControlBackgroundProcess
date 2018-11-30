package com.click369.controlbp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PinyinCompare;

import java.util.ArrayList;
import java.util.Collections;

public class AdAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Context c;
	private SharedPreferences modPrefs;
	public String fliterName = "u";
//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public AdAdapter(Context context, SharedPreferences modPrefs) {
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
		PinyinCompare comparent = new PinyinCompare();
		Collections.sort(this.bjdatas, comparent);
		ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNoChoose = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempRun = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNewApp = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&modPrefs.getInt(ai.packageName+"/ad",0)==1){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&modPrefs.getInt(ai.packageName+"/ad",0)==2){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&modPrefs.getInt(ai.packageName+"/ad",0)==3){
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
			convertView= inflater.inflate(R.layout.item_mainapp, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_main_appname);
			viewHolder.serviceIv = (ImageView) convertView.findViewById(R.id.item_main_service);
			viewHolder.wakelockIv = (ImageView)convertView.findViewById(R.id.item_main_wakelock);
			viewHolder.alarmIv = (ImageView)convertView.findViewById(R.id.item_main_alarm);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_main_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_main_iceicon);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.appNameTv.setText(data.appName+BaseActivity.getProcTimeStr(data.packageName));
		viewHolder.appNameTv.setTextColor(data.isRunning?(data.isInMuBei?Color.parseColor(MainActivity.COLOR_MUBEI):(MainActivity.pkgIdleStates.contains(data.packageName)?Color.parseColor(MainActivity.COLOR_IDLE):Color.parseColor(MainActivity.COLOR_RUN))):(data.isDisable?Color.LTGRAY: ControlFragment.curColor));
		viewHolder.appIcon.setImageBitmap(data.getBitmap());
		viewHolder.iceIv.setImageResource(data.isDisable?R.mipmap.ice: WatchDogService.setTimeStopApp.containsKey(data.packageName)?R.mipmap.icon_clock:R.mipmap.empty);
		viewHolder.appNameTv.setTag(position);
		viewHolder.serviceIv.setTag(position);
		viewHolder.wakelockIv.setTag(position);
		viewHolder.alarmIv.setTag(position);
		viewHolder.serviceIv.setImageResource((modPrefs.getInt(data.packageName+"/ad",0)==1)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		viewHolder.wakelockIv.setImageResource((modPrefs.getInt(data.packageName+"/ad",0)==2)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		viewHolder.alarmIv.setImageResource((modPrefs.getInt(data.packageName+"/ad",0)==3)?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		if (data.isDisable){
			convertView.setAlpha(0.5f);
			convertView.setEnabled(false);
		}else{
			convertView.setAlpha(1.0f);
			convertView.setEnabled(true);
		}
		if(!MainActivity.isModuleActive()){
			convertView.setAlpha(0.5f);
		}
		viewHolder.serviceIv.setOnClickListener(new View.OnClickListener() {
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
				if (ai.isDisable){
					return;
				}
				final boolean isStop = (modPrefs.getInt(ai.packageName+"/ad",0)==1);
				if (isStop){
					modPrefs.edit().remove(ai.packageName+"/ad").commit();
					ai.isADJump = false;
				}else{
					modPrefs.edit().putInt(ai.packageName+"/ad",1).putString(ai.packageName+"/one", OpenCloseUtil.getFirstActivity(ai.packageName,c)).commit();
					ai.isADJump = true;
					if(ai.packageName.equals("so.ofo.labofo")){
						modPrefs.edit().putString(ai.packageName+"/two", "so.ofo.labofo.activities.journey.MainActivity").commit();
					}
				}
				XposedStopApp.stopApk(ai.getPackageName(),c);
				notifyDataSetChanged();
			}
		});
		viewHolder.wakelockIv.setOnClickListener(new View.OnClickListener() {
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
				if (ai.isDisable){
					return;
				}
				final boolean isStop = (modPrefs.getInt(ai.packageName+"/ad",0)==2);
				if (isStop){
					modPrefs.edit().remove(ai.packageName+"/ad").commit();
					ai.isADJump = false;
				}else{
					modPrefs.edit().putInt(ai.packageName+"/ad",2).putString(ai.packageName+"/one", OpenCloseUtil.getFirstActivity(ai.packageName,c)).commit();
					ai.isADJump = true;
				}
				XposedStopApp.stopApk(ai.getPackageName(),c);
				notifyDataSetChanged();
			}
		});
		viewHolder.alarmIv.setOnClickListener(new View.OnClickListener() {
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
				if (ai.isDisable){
					return;
				}
				final boolean isStop = (modPrefs.getInt(ai.packageName+"/ad",0)==3);
				if (isStop){
					modPrefs.edit().remove(ai.packageName+"/ad").remove(ai.packageName+"/one").remove(ai.packageName+"/two").remove(ai.packageName+"/three").commit();
					ai.isADJump = false;
				}else{
					modPrefs.edit().putInt(ai.packageName+"/ad",3).putString(ai.packageName+"/one", OpenCloseUtil.getFirstActivity(ai.packageName,c)).commit();
					ai.isADJump = true;
				}
				XposedStopApp.stopApk(ai.getPackageName(),c);
				notifyDataSetChanged();
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,serviceIv,wakelockIv,alarmIv,iceIv;
	}
}

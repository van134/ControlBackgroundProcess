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
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.WhiteApp;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.ShellUtilBackStop;

import java.util.ArrayList;
import java.util.Collections;

public class ControlAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Context c;
	private SharedPreferences modPrefs;
	public String fliterName = "u";
	private boolean isMubeiStopBroad = false;
	//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public ControlAdapter(Context context, SharedPreferences modPrefs) {
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
			if(sortType == 0&&(ai.isServiceStop)){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isBroadStop){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&ai.isWakelockStop){
				this.bjdatas.add(ai);
			}else if(sortType == 3&&ai.isAlarmStop){
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
			convertView= inflater.inflate(R.layout.item_controlapp, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_main_appname);
			viewHolder.serviceIv = (ImageView) convertView.findViewById(R.id.item_main_service);
			viewHolder.broadIv = (ImageView) convertView.findViewById(R.id.item_main_broad);
			viewHolder.wakelockIv = (ImageView)convertView.findViewById(R.id.item_main_wakelock);
			viewHolder.alarmIv = (ImageView)convertView.findViewById(R.id.item_main_alarm);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_main_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_main_iceicon);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.appNameTv.setText(data.appName+(data.isRunning?BaseActivity.getProcTimeStr(data.packageName):""));
		viewHolder.appNameTv.setTextColor(data.isRunning?(data.isInMuBei?Color.parseColor(MainActivity.COLOR_MUBEI):(MainActivity.pkgIdleStates.contains(data.packageName)?Color.parseColor(MainActivity.COLOR_IDLE):Color.parseColor(MainActivity.COLOR_RUN))):(data.isDisable?Color.LTGRAY: ControlFragment.curColor));
//		viewHolder.appIcon.setImageBitmap();
//		viewHolder.appIcon.setImageDrawable(PackageUtil.getBitmap(c,data.packageName));
		viewHolder.appIcon.setImageBitmap(data.getBitmap());

//		viewHolder.iceIv.setVisibility(data.isDisable?View.VISIBLE:View.GONE);
		viewHolder.iceIv.setImageResource(data.isDisable?R.mipmap.ice: data.isSetTimeStopApp?R.mipmap.icon_clock:R.mipmap.empty);
		viewHolder.appNameTv.setTag(position);
		viewHolder.broadIv.setTag(position);
		viewHolder.serviceIv.setTag(position);
		viewHolder.wakelockIv.setTag(position);
		viewHolder.alarmIv.setTag(position);
		viewHolder.serviceIv.setImageResource(data.isServiceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		viewHolder.broadIv.setImageResource(data.isBroadStop? R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		viewHolder.wakelockIv.setImageResource(data.isWakelockStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
//		viewHolder.wakelockTv.setTextColor(data.isWakelockStop? Color.RED:Color.BLACK);
		viewHolder.alarmIv.setImageResource(data.isAlarmStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
//		viewHolder.alarmTv.setTextColor(data.isAlarmStop? Color.RED:Color.BLACK);
//		convertView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				TextView tv = (TextView)(v.findViewById(R.id.item_main_appname));
//				int g = (Integer)tv.getTag();
//
//			}
//		});
//		if (!ControlFragment.isBroadStop){
//			viewHolder.broadIv.setAlpha(0.5f);
//		}else{
//			viewHolder.broadIv.setAlpha(1.0f);
//		}
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
				ControlFragment.isClick = true;
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				final boolean isSerStop = modPrefs.getBoolean(ai.getPackageName()+"/service",false);
				if (!isSerStop){
					if (ai.isHomeMuBei||ai.isOffscMuBei||ai.isBackMuBei){
						AlertUtil.showAlertMsg(c,"检测到你已经在第二项的墓碑模式中添加了该应用，所以设置禁用服务将不会生效，如果要使用禁用请在第二项的墓碑模式中取消勾选。");
						return;
					}
					if (MainActivity.whiteApps.containsKey(ai.packageName)){
						WhiteApp wa = MainActivity.whiteApps.get(ai.packageName);
						AlertUtil.showConfirmAlertMsg(c, wa.content+" 是否处理?", new AlertUtil.InputCallBack() {
							@Override
							public void backData(String txt, int tag) {
								if(tag == 1){
									ai.isServiceStop = true;
									modPrefs.edit().putBoolean(ai.getPackageName()+"/service",true).commit();
									XposedStopApp.stopApk(ai.getPackageName(),c);
									buttonView.setImageResource(ai.isServiceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
								}
							}
						});
					}else{
						modPrefs.edit().putBoolean(ai.getPackageName()+"/service",true).commit();
						ai.isServiceStop = true;
					}
				}else{
					modPrefs.edit().remove(ai.getPackageName()+"/service").commit();
					ai.isServiceStop = false;
				}
				XposedStopApp.stopApk(ai.getPackageName(),c);
				notifyDataSetChanged();
//				buttonView.setImageResource(ai.isServiceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
////				buttonView.setTextColor(ai.isServiceStop? Color.RED:Color.BLACK);
			}
		});
		viewHolder.broadIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				ControlFragment.isClick = true;
//				else if(!ControlFragment.isBroadStop){
//					AlertUtil.showAlertMsg(c,"由于部分系统对广播处理后会导致卡顿或唤醒异常，所以该功能默认关闭，如果需要使用请到设置中打开开关并重启手机，如果使用有问题请关闭广播开关。");
//					return;
//				}
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				final boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/broad",false);
				if (!isStop&&MainActivity.whiteApps.containsKey(ai.packageName)){
					WhiteApp wa = MainActivity.whiteApps.get(ai.packageName);
					AlertUtil.showConfirmAlertMsg(c, wa.content+" 是否处理?", new AlertUtil.InputCallBack() {
						@Override
						public void backData(String txt, int tag) {
							if(tag == 1){
								ai.isBroadStop = !isStop;
								modPrefs.edit().putBoolean(ai.getPackageName()+"/broad",!isStop).commit();
//								ShellUtilBackStop.kill(ai.getPackageName());
								XposedStopApp.stopApk(ai.getPackageName(),c);
//								buttonView.setImageResource(ai.isBroadStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
								notifyDataSetChanged();
							}
						}
					});
				}else {
					SharedPreferences.Editor ed = modPrefs.edit();
					if(isStop){
						ed.remove(ai.getPackageName()+"/broad");
					}else{
						ed.putBoolean(ai.getPackageName()+"/broad",!isStop);
					}
					ed.commit();
					ai.isBroadStop = !isStop;
					XposedStopApp.stopApk(ai.getPackageName(),c);
//					buttonView.setImageResource(ai.isBroadStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
					notifyDataSetChanged();
				}
			}
		});
		viewHolder.wakelockIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				ControlFragment.isClick = true;
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				final boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/wakelock",false);
				if (!isStop&&MainActivity.whiteApps.containsKey(ai.packageName)){
					WhiteApp wa = MainActivity.whiteApps.get(ai.packageName);
					AlertUtil.showConfirmAlertMsg(c, wa.content+" 是否处理?", new AlertUtil.InputCallBack() {
						@Override
						public void backData(String txt, int tag) {
							if(tag == 1){
								ai.isWakelockStop = !isStop;
								modPrefs.edit().putBoolean(ai.getPackageName()+"/wakelock",!isStop).commit();
								XposedStopApp.stopApk(ai.getPackageName(),c);
								notifyDataSetChanged();
//								ShellUtilBackStop.kill(ai.getPackageName());
//								buttonView.setImageResource(ai.isWakelockStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
							}
						}
					});
				}else {
					SharedPreferences.Editor ed = modPrefs.edit();
					if(isStop){
						ed.remove(ai.getPackageName()+"/wakelock");
					}else{
						ed.putBoolean(ai.getPackageName()+"/wakelock",!isStop);
					}
					ed.commit();
					ai.isWakelockStop = !isStop;
					XposedStopApp.stopApk(ai.getPackageName(),c);
//					buttonView.setImageResource(ai.isWakelockStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
					notifyDataSetChanged();
				}
			}
		});
		viewHolder.alarmIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				ControlFragment.isClick = true;
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				final boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/alarm",false);
				if (!isStop&&MainActivity.whiteApps.containsKey(ai.packageName)){
					WhiteApp wa = MainActivity.whiteApps.get(ai.packageName);
					AlertUtil.showConfirmAlertMsg(c, wa.content+" 是否处理?", new AlertUtil.InputCallBack() {
						@Override
						public void backData(String txt, int tag) {
							if(tag == 1){
								ai.isAlarmStop = !isStop;
								modPrefs.edit().putBoolean(ai.getPackageName()+"/alarm",!isStop).commit();
								XposedStopApp.stopApk(ai.getPackageName(),c);
								notifyDataSetChanged();
//								ShellUtilBackStop.kill(ai.getPackageName());
//								buttonView.setImageResource(ai.isAlarmStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
							}
						}
					});
				}else {
					SharedPreferences.Editor ed = modPrefs.edit();
					if (isStop) {
						ed.remove(ai.getPackageName() + "/alarm");
					} else {
						ed.putBoolean(ai.getPackageName() + "/alarm", !isStop);
					}
					ed.commit();
					ai.isAlarmStop = !isStop;
					XposedStopApp.stopApk(ai.getPackageName(),c);
//					buttonView.setImageResource(ai.isAlarmStop ? R.mipmap.icon_disable : R.mipmap.icon_notdisable);
					notifyDataSetChanged();
				}
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,serviceIv,broadIv,wakelockIv,alarmIv,iceIv;
	}
}

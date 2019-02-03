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
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.fragment.AppStartFragment;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.PinyinCompare;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class AppStartAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Activity c;
	private SharedPreferences modPrefs;
	public String fliterName = "u";
	private AppInfo myAi =null;
//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public AppStartAdapter(Activity context, SharedPreferences modPrefs) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.modPrefs = modPrefs;
		try {
			PackageInfo packgeInfo = context.getPackageManager().getPackageInfo(Common.PACKAGENAME, PackageManager.GET_ACTIVITIES);
			String appName = packgeInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
			Drawable d = packgeInfo.applicationInfo.loadIcon(context.getPackageManager());
			myAi = new AppInfo(appName, Common.PACKAGENAME,(packgeInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0,!packgeInfo.applicationInfo.enabled);
		} catch (PackageManager.NameNotFoundException e) {
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
		ArrayList<AppInfo> tempDisableApp = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.isLockApp){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isStopApp){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&ai.isAutoStart){
				this.bjdatas.add(ai);
			}else if(sortType == 3&&ai.isNotStop){
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
		this.bjdatas.add(0,myAi);
		myAi.isRunning = true;
		myAi.isLockApp = modPrefs.getBoolean(myAi.packageName+"/lockapp",false);
		myAi.isNotStop = modPrefs.getBoolean(myAi.packageName+"/notstop",false);
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
			convertView= inflater.inflate(R.layout.item_appstart, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView) convertView.findViewById(R.id.item_appstart_appname);
			viewHolder.appTimeTv = (TextView) convertView.findViewById(R.id.item_main_apptime);
			viewHolder.lockAppIv = (ImageView) convertView.findViewById(R.id.item_appstart_lock);
			viewHolder.stopAppIv = (ImageView)convertView.findViewById(R.id.item_appstart_notstart);
			viewHolder.autoStartIv = (ImageView)convertView.findViewById(R.id.item_appstart_autostart);
			viewHolder.notStoptIv = (ImageView)convertView.findViewById(R.id.item_appstart_notstop);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_appstart_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_appstart_iceicon);
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
		viewHolder.lockAppIv.setTag(position);
		viewHolder.stopAppIv.setTag(position);
		viewHolder.autoStartIv.setTag(position);
		viewHolder.notStoptIv.setTag(position);

		viewHolder.lockAppIv.setImageResource(data.isLockApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//		viewHolder.serviceTv.setTextColor(data.isServiceStop? Color.RED:Color.BLACK);
		viewHolder.stopAppIv.setImageResource(data.isStopApp?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//		viewHolder.wakelockTv.setTextColor(data.isWakelockStop? Color.RED:Color.BLACK);
		viewHolder.autoStartIv.setImageResource(data.isAutoStart?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.notStoptIv.setImageResource(data.isNotStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//		viewHolder.alarmTv.setTextColor(data.isAlarmStop? Color.RED:Color.BLACK);
//		convertView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				TextView tv = (TextView)(v.findViewById(R.id.item_main_appname));
//				int g = (Integer)tv.getTag();
//
//			}
//		});
//		viewHolder.moblieNetIv.setVisibility(View.INVISIBLE);
//		viewHolder.stopAppIv.setVisibility(View.INVISIBLE);
		if (data.equals(myAi)) {
			viewHolder.stopAppIv.setEnabled(false);
			viewHolder.stopAppIv.setAlpha(0.3f);
			viewHolder.autoStartIv.setEnabled(false);
			viewHolder.autoStartIv.setAlpha(0.3f);
		}else {
			viewHolder.stopAppIv.setEnabled(true);
			viewHolder.stopAppIv.setAlpha(1.0f);
			viewHolder.autoStartIv.setEnabled(true);
			viewHolder.autoStartIv.setAlpha(1.0f);
		}
		if(!MainActivity.isModuleActive()){
			convertView.setAlpha(0.5f);
		}
		viewHolder.lockAppIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				AppStartFragment.isClickItem = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/lockapp",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ai.isLockApp = false;
					ed.remove(ai.getPackageName()+"/lockapp");
				}else{
					ed.putBoolean(ai.getPackageName()+"/lockapp",!isStop);
					ai.isLockApp = true;
					if (modPrefs.contains(ai.getPackageName()+"/stopapp")) {
						ai.isStopApp = false;
						ed.remove(ai.getPackageName() + "/stopapp");
						ed.commit();
						notifyDataSetChanged();
					}
				}
				ed.commit();
				XposedStopApp.stopApk(ai.getPackageName(),c);
				buttonView.setImageResource(!isStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//				buttonView.setTextColor(ai.isServiceStop? Color.RED:Color.BLACK);
			}
		});
		viewHolder.stopAppIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AppStartFragment.isClickItem = true;
				BaseActivity.zhenDong(c);
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/stopapp",false);
				final SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ai.isStopApp = false;
					ed.remove(ai.getPackageName()+"/stopapp");
					ed.commit();
				}else{
					AlertUtil.showConfirmAlertMsg(c, "该功能需要谨慎操作，不确定的请不要乱禁用(小心手机无法启动)，选择是否禁用？", new AlertUtil.InputCallBack() {
						@Override
						public void backData(String txt, int tag) {
							if (tag == 1){
								ed.putBoolean(ai.getPackageName()+"/stopapp",true);
								ai.isStopApp = true;
								if (ai.isLockApp) {
									ai.isLockApp = false;
									ed.remove(ai.getPackageName() + "/lockapp").commit();
								}
								if (ai.isAutoStart) {
									ai.isAutoStart = false;
									ed.remove(ai.getPackageName() + "/autostart").commit();
								}
								if (ai.isNotStop) {
									ai.isNotStop = false;
									ed.remove(ai.getPackageName() + "/notstop").commit();
								}
								ed.commit();
								notifyDataSetChanged();
								XposedStopApp.stopApk(ai.getPackageName(),c);
								buttonView.setImageResource(modPrefs.getBoolean(ai.getPackageName()+"/stopapp",false)?R.mipmap.icon_add:R.mipmap.icon_notdisable);
							}
						}
					});
				}
				XposedStopApp.stopApk(ai.getPackageName(),c);
				buttonView.setImageResource(modPrefs.getBoolean(ai.getPackageName()+"/stopapp",false)?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//				buttonView.setText(ai.isWakelockStop?"已禁用":"未禁用");
//				buttonView.setTextColor(ai.isWakelockStop? Color.RED:Color.BLACK);
			}
		});
		viewHolder.autoStartIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				AppStartFragment.isClickItem = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/autostart",false);
				ai.isAutoStart = !isStop;
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ed.remove(ai.getPackageName()+"/autostart").commit();
				}else{
					ed.putBoolean(ai.getPackageName()+"/autostart",true).commit();
					if (ai.isStopApp){
						ed.remove(ai.getPackageName()+"/stopapp").commit();
						ai.isStopApp = false;
						notifyDataSetChanged();
					}
				}
				XposedStopApp.stopApk(ai.getPackageName(),c);
				buttonView.setImageResource(ai.isAutoStart?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//				buttonView.setText(ai.isAlarmStop?"已禁用":"未禁用");
//				buttonView.setTextColor(ai.isAlarmStop? Color.RED:Color.BLACK);
			}
		});
		viewHolder.notStoptIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				AppStartFragment.isClickItem = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/notstop",false);
				ai.isNotStop = !isStop;
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ed.remove(ai.getPackageName()+"/notstop").commit();
					sendBroadChangePersistent(c,ai.getPackageName(),false);
				}else{
					ed.putBoolean(ai.getPackageName()+"/notstop",true).commit();
					if (ai.isStopApp){
						ed.remove(ai.getPackageName()+"/stopapp").commit();
						ai.isStopApp = false;
						notifyDataSetChanged();
					}
					sendBroadChangePersistent(c,ai.getPackageName(),true);
					if(ai.isBackForceStop||ai.isOffscForceStop){
						AlertUtil.showAlertMsg(c,"检测到你已把该应用加入到强退功能中，应用控制器的强退功能还会将其杀死，但系统或其他管理类应用将不会杀死该应用。");
					}
				}

//				XposedStopApp.onlyStopApk(ai.getPackageName(),c);
				buttonView.setImageResource(ai.isNotStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//				buttonView.setText(ai.isAlarmStop?"已禁用":"未禁用");
//				buttonView.setTextColor(ai.isAlarmStop? Color.RED:Color.BLACK);
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv,appTimeTv;
		public ImageView appIcon,lockAppIv,stopAppIv,autoStartIv,notStoptIv,iceIv;

	}

	public static void sendBroadChangePersistent(Context c,String pkg, boolean persistent){
		Intent intent1 = new Intent("com.click369.control.ams.changepersistent");
		intent1.putExtra("persistent",persistent);
		intent1.putExtra("pkg",pkg);
		c.sendBroadcast(intent1);
	}
}

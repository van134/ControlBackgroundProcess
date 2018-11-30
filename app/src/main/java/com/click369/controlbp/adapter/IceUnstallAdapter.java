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
import com.click369.controlbp.activity.AppStartFragment;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.ControlFragment;
import com.click369.controlbp.activity.EmptyActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShortCutUtil;

import java.util.ArrayList;
import java.util.Collections;

public class IceUnstallAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Activity c;
	private SharedPreferences modPrefs;
	public String fliterName = "u";
	private AppInfo myAi =null;
//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public IceUnstallAdapter(Activity context, SharedPreferences modPrefs) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.modPrefs = modPrefs;
		try {
			PackageInfo packgeInfo = context.getPackageManager().getPackageInfo(Common.PACKAGENAME, PackageManager.GET_ACTIVITIES);
			String appName = packgeInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
			Drawable d = packgeInfo.applicationInfo.loadIcon(context.getPackageManager());
			myAi = new AppInfo(appName, Common.PACKAGENAME, AppLoaderUtil.zoomDrawable(d,60,60),(packgeInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0,!packgeInfo.applicationInfo.enabled);
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
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.isDisable){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isNotUnstall){
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
		myAi.isNotUnstall = modPrefs.getBoolean(myAi.packageName+"/notunstall",false);
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
			convertView= inflater.inflate(R.layout.item_mainapp, null);
			viewHolder = new ViewHolder();

			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_main_appname);
			viewHolder.iceAppIv = (ImageView) convertView.findViewById(R.id.item_main_service);
			viewHolder.notUnstallAppIv = (ImageView)convertView.findViewById(R.id.item_main_wakelock);
			viewHolder.unstallIv = (ImageView)convertView.findViewById(R.id.item_main_alarm);
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
		viewHolder.iceAppIv.setTag(position);
		viewHolder.notUnstallAppIv.setTag(position);
		viewHolder.unstallIv.setTag(position);

		viewHolder.iceAppIv.setImageResource(data.isDisable?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//		viewHolder.serviceTv.setTextColor(data.isServiceStop? Color.RED:Color.BLACK);
		viewHolder.notUnstallAppIv.setImageResource(data.isNotUnstall?R.mipmap.icon_add:R.mipmap.icon_notdisable);
//		viewHolder.wakelockTv.setTextColor(data.isWakelockStop? Color.RED:Color.BLACK);
		viewHolder.unstallIv.setImageResource(R.mipmap.icon_notdisable);

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
			viewHolder.iceAppIv.setEnabled(false);
			viewHolder.iceAppIv.setAlpha(0.3f);
			viewHolder.unstallIv.setEnabled(false);
			viewHolder.unstallIv.setAlpha(0.3f);
		}else {
			viewHolder.iceAppIv.setEnabled(true);
			viewHolder.iceAppIv.setAlpha(1.0f);
			if (data.isNotUnstall){
				viewHolder.unstallIv.setEnabled(false);
				viewHolder.unstallIv.setAlpha(0.3f);
			}else{
				viewHolder.unstallIv.setEnabled(true);
				viewHolder.unstallIv.setAlpha(1.0f);
			}
		}
		if(!MainActivity.isModuleActive()){
			viewHolder.notUnstallAppIv.setEnabled(false);
			viewHolder.notUnstallAppIv.setAlpha(0.3f);
		}
		viewHolder.iceAppIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				AppStartFragment.isClickItem = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = ai.isDisable;
				ShellUtilNoBackData.execCommand("pm "+(ai.isDisable?"enable":"disable")+" "+ai.packageName);
				ai.isDisable = !isStop;
				ai.isRunning = false;
				notifyDataSetChanged();
				buttonView.setImageResource(!isStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.iceAppIv.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(view);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				if (ai.isDisable){
					String titles[] = {"运行该程序","创建快捷方式"};
					AlertUtil.showListAlert(c,"请选择",titles,new AlertUtil.InputCallBack(){
						@Override
						public void backData(String txt, int tag) {
							if(tag == 0){
								runIceApp(ai);
							}else if(tag == 1){
								try {
									Drawable d = c.getPackageManager().getPackageInfo(ai.getPackageName(), PackageManager.GET_ACTIVITIES).applicationInfo.loadIcon(c.getPackageManager());
									BitmapDrawable bd = (BitmapDrawable) d ;
									ShortCutUtil.addShortcutDrawable(ai.getPackageName(),ai.appName,c,EmptyActivity.class, bd.getBitmap());
									Toast.makeText(c,"快捷方式创建成功",Toast.LENGTH_LONG).show();
								} catch (PackageManager.NameNotFoundException e) {
									e.printStackTrace();
								}

							}
						}
					});
				}
				return true;
			}
		});
		viewHolder.notUnstallAppIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!MainActivity.isModuleActive()){
					Toast.makeText(c,"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
					return;
				}
				BaseActivity.zhenDong(c);
				AppStartFragment.isClickItem = true;
				final ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/notunstall",false);
				final SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ai.isNotUnstall = false;
					ed.remove(ai.getPackageName()+"/notunstall");
					ed.commit();
				}else{
					ai.isNotUnstall = true;
					ed.putBoolean(ai.getPackageName()+"/notunstall",true);
					ed.commit();
				}
				notifyDataSetChanged();
				buttonView.setImageResource(!isStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.unstallIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				final AppInfo ai = bjdatas.get(g);
				AlertUtil.showConfirmAlertMsg(c, "该功能需要谨慎操作，选择是否卸载？", new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						if (tag == 1){
							RemoveApp(ai.packageName);
						}
					}
				});
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,iceAppIv,notUnstallAppIv,unstallIv,iceIv;

	}

	ProgressDialog pd = null;
	AppInfo ai = null;
	int cout = 0;
	Handler h = new Handler();
	public void runIceApp(final AppInfo ai){
		if(ai.isDisable){
			pd = ProgressDialog.show(c, null, "正在解冻并启动，请稍等...", true, false);
			WatchDogService.iceButOpenInfos.add(ai.getPackageName());
			Intent intent = new Intent("com.click369.control.pms.enablepkg");
			intent.putExtra("pkg",ai.getPackageName());
			c.sendBroadcast(intent);
			ShellUtilNoBackData.execCommand("pm "+(ai.isDisable?"enable":"disable")+" "+ai.packageName);
			this.ai = ai;
			h.postDelayed(r,100);
			cout = 0;
		}else{
			OpenCloseUtil.doStartApplicationWithPackageName(ai.packageName,c);

		}
	}
	Runnable r=  new Runnable() {
		@Override
		public void run() {
			try {
				OpenCloseUtil.doStartApplicationWithPackageName(ai.packageName,c);
				if(pd!=null&&pd.isShowing()){
					try {
						pd.dismiss();
					}catch (Exception e){
					}
				}
			}catch (Exception e){
				cout++;
				if(cout>20){
					cout = 0;
					if(pd!=null&&pd.isShowing()){
						try {
							pd.dismiss();
						}catch (Exception e1){
						}
					}
					Toast.makeText(c,"该应用无法启动",Toast.LENGTH_LONG).show();
				}else{
					h.postDelayed(r,300);
				}
			}
		}
	};

	public void RemoveApp(String packageName){
		Uri uri = Uri.fromParts("package", packageName, null);
		//也可以这样写：Uri uri=Uri.parse("package:"+packageName);
		Intent intentdel = new Intent(Intent.ACTION_DELETE, uri);
		c.startActivity(intentdel);
	}
}

package com.click369.controlbp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.activity.IFWCompActivity;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.ShellUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class IFWAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Context c;
//	private SharedPreferences modPrefs;
	public String fliterName = "u";
//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public IFWAdapter(Context context) {
		c = context;
		inflater = LayoutInflater.from(context);
//		this.modPrefs = modPrefs;
	}

	public void setData(ArrayList<AppInfo> datas){
//		bjdatas.clear();
//		bjdatas.addAll(datas);
//		this.notifyDataSetChanged();
//		freshList();
		fliterList(fliterName,datas);
	}
	public void setSortType(int sortType){
		this.sortType = sortType;
		freshList();
	}
	public void fliterList(String name,ArrayList<AppInfo> apps){
		fliterName = name;
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
//	public void setSortType(int sortType){
//		this.sortType = sortType;
//		freshList();
//	}
	public void freshList(){
		PinyinCompare comparent = new PinyinCompare();
		Collections.sort(this.bjdatas, comparent);

//		if (sortType == -1){
//			this.notifyDataSetChanged();
//			return;
//		}
////		String exs[] = {"/service","/wakelock","/alarm"};
//////		Collections.sort(this.choosedatas, comparent);
		ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNoChoose = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNewApp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempRun = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempDisableApp = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.serviceDisableCount>0){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.broadCastDisableCount>0){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&ai.activityDisableCount>0){
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
			convertView= inflater.inflate(R.layout.item_ifwapp, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_main_appname);
			viewHolder.appTimeTv = (TextView)convertView.findViewById(R.id.item_main_apptime);
			viewHolder.serviceTv = (TextView)convertView.findViewById(R.id.item_main_service);
			viewHolder.broadTv = (TextView)convertView.findViewById(R.id.item_main_wakelock);
			viewHolder.activiyTv = (TextView)convertView.findViewById(R.id.item_main_alarm);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_main_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_main_iceicon);
			viewHolder.serFl= (LinearLayout) convertView.findViewById(R.id.item_main_service_fl);
			viewHolder.broFl = (LinearLayout) convertView.findViewById(R.id.item_main_wakelock_fl);
			viewHolder.actFl= (LinearLayout) convertView.findViewById(R.id.item_main_alarm_fl);
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
		viewHolder.iceIv.setImageResource(data.isDisable?R.mipmap.ice: data.isSetTimeStopApp?R.mipmap.icon_clock:R.mipmap.empty);
		viewHolder.appIcon.setImageBitmap(AppLoaderUtil.allHMAppIcons.get(data.packageName));
//		File file = null;
//		if(BaseActivity.isLoadIcon||BaseActivity.loadeds.contains(data.packageName)){
//			BaseActivity.loadeds.add(data.packageName);
//			file = data.iconFile;
//		}
//		Glide.with(c).load(file).into(viewHolder.appIcon);
		viewHolder.appNameTv.setTag(position);
		viewHolder.serFl.setTag(position);
		viewHolder.broFl.setTag(position);
		viewHolder.actFl.setTag(position);
		ForegroundColorSpan redSpan = new ForegroundColorSpan(Color.RED);
		String serStr = data.serviceDisableCount+"/"+data.serviceCount;
		SpannableStringBuilder builder = new SpannableStringBuilder(serStr);
		builder.setSpan(redSpan,0, serStr.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		viewHolder.serviceTv.setText(builder);

		String broadStr = data.broadCastDisableCount+"/"+data.broadCastCount;
		SpannableStringBuilder builder1 = new SpannableStringBuilder(broadStr);
		builder1.setSpan(redSpan,0, broadStr.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		viewHolder.broadTv.setText(builder1);

		String actStr = data.activityDisableCount+"/"+data.activityCount;
		SpannableStringBuilder builder2 = new SpannableStringBuilder(actStr);
		builder2.setSpan(redSpan,0, actStr.indexOf("/"), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		viewHolder.activiyTv.setText(builder2);

//		viewHolder.serviceTv.setText("服务\n"+data.serviceDisableCount+"/"+data.serviceCount);
//		viewHolder.serviceTv.setTextColor(data.isServiceStop? Color.RED:Color.BLACK);

//		viewHolder.wakelockTv.setTextColor(data.isWakelockStop? Color.RED:Color.BLACK);
//		viewHolder.broadTv.setText("广播\n"+data.broadCastDisableCount+"/"+data.broadCastCount);
//		viewHolder.activiyTv.setText("活动\n"+data.activityDisableCount+"/"+data.activityCount);
//		viewHolder.alarmTv.setTextColor(data.isAlarmStop? Color.RED:Color.BLACK);
//		convertView.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				TextView tv = (TextView)(v.findViewById(R.id.item_main_appname));
//				int g = (Integer)tv.getTag();
//
//			}
//		});
		viewHolder.serFl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!WatchDogService.isRoot){
					Toast.makeText(c,"请给予ROOT权限",Toast.LENGTH_SHORT).show();
					WatchDogService.isRoot = ShellUtils.checkRootPermission();
				}
				if(!WatchDogService.isRoot){
					Toast.makeText(c,"未获取ROOT权限，无法使用",Toast.LENGTH_SHORT).show();
					return;
				}
				LinearLayout buttonView = (LinearLayout)(v);
				int g = (Integer)buttonView.getTag();
				IFWFragment.ai = bjdatas.get(g);
				startAct(IFWFragment.ai,0);
			}
		});
		viewHolder.broFl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!WatchDogService.isRoot){
					Toast.makeText(c,"请给予ROOT权限",Toast.LENGTH_SHORT).show();
					WatchDogService.isRoot = ShellUtils.checkRootPermission();
				}
				if(!WatchDogService.isRoot){
					Toast.makeText(c,"未获取ROOT权限，无法使用",Toast.LENGTH_SHORT).show();
					return;
				}
				LinearLayout buttonView = (LinearLayout)(v);
				int g = (Integer)buttonView.getTag();
				IFWFragment.ai = bjdatas.get(g);
				startAct(IFWFragment.ai,1);
			}
		});
		viewHolder.actFl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!WatchDogService.isRoot){
					Toast.makeText(c,"请给予ROOT权限",Toast.LENGTH_SHORT).show();
					WatchDogService.isRoot = ShellUtils.checkRootPermission();
				}
				if(!WatchDogService.isRoot){
					Toast.makeText(c,"未获取ROOT权限，无法使用",Toast.LENGTH_SHORT).show();
					return;
				}
				LinearLayout buttonView = (LinearLayout)(v);
				int g = (Integer)buttonView.getTag();
				IFWFragment.ai = bjdatas.get(g);
				startAct(IFWFragment.ai,2);
			}
		});

		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv,appTimeTv,serviceTv,activiyTv,broadTv;
		public ImageView appIcon,iceIv;
		public LinearLayout serFl,actFl,broFl;
	}
	private void startAct(AppInfo ai,int type){
		Intent intent = new Intent(c, IFWCompActivity.class);
		intent.putExtra("name",ai.appName);
		intent.putExtra("pkg",ai.packageName);
		intent.putExtra("type",type);
		c.startActivity(intent);
	}
}

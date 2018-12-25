package com.click369.controlbp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.DozeWhiteListActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.ShellUtilBackStop;

import java.util.ArrayList;
import java.util.Collections;

public class DozeWhiteListAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Context c;
	private SharedPreferences modPrefs;
	public String fliterName = "";
	public DozeWhiteListAdapter(Context context, SharedPreferences modPrefs) {
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
		ArrayList<AppInfo> tempNewApp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempRun = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.isDozeOffsc){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isDozeOnsc){
				this.bjdatas.add(ai);
			}else if(sortType == 2&&ai.isDozeOpenStop){
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
			convertView= inflater.inflate(R.layout.item_dozewhitelistapp, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_doze_appname);
			viewHolder.openStopIv = (ImageView) convertView.findViewById(R.id.item_doze_openstop_tv);
			viewHolder.offIv = (ImageView) convertView.findViewById(R.id.item_doze_off_tv);
			viewHolder.onIv = (ImageView)convertView.findViewById(R.id.item_doze_on_tv);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_doze_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_doze_iceicon);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.appNameTv.setText(data.appName);
		viewHolder.appNameTv.setTextColor(data.isRunning?data.isInMuBei?Color.parseColor(MainActivity.COLOR_MUBEI):Color.parseColor(MainActivity.COLOR_RUN):(data.isDisable?Color.LTGRAY:DozeWhiteListActivity.curColor));
//		viewHolder.appIcon.setImageBitmap(data.getBitmap());
		Glide.with( c ).load( Uri.fromFile(data.iconFile ) ).into(viewHolder.appIcon );
		viewHolder.iceIv.setVisibility(data.isDisable?View.VISIBLE:View.GONE);
		viewHolder.appNameTv.setTag(position);
		viewHolder.openStopIv.setTag(position);
		viewHolder.offIv.setTag(position);
		viewHolder.onIv.setTag(position);
		viewHolder.offIv.setImageResource(data.isDozeOffsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.onIv.setImageResource(data.isDozeOnsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.openStopIv.setImageResource(data.isDozeOpenStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		if(!MainActivity.isModuleActive()){
			convertView.setAlpha(0.5f);
		}
		viewHolder.offIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				DozeWhiteListActivity.isClick = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/offsc",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				ai.isDozeOffsc = !isStop;
				if(isStop){
					ed.remove(ai.getPackageName()+"/offsc");
				}else{
					ed.putBoolean(ai.getPackageName()+"/offsc",!isStop);
				}
				ed.commit();
				buttonView.setImageResource(ai.isDozeOffsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.onIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				DozeWhiteListActivity.isClick = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/onsc",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				ai.isDozeOnsc = !isStop;
				if(isStop){
					ed.remove(ai.getPackageName()+"/onsc");
				}else{
					ed.putBoolean(ai.getPackageName()+"/onsc",!isStop);
				}
				ed.commit();
				buttonView.setImageResource(ai.isDozeOnsc?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		viewHolder.openStopIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				DozeWhiteListActivity.isClick = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/openstop",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				ai.isDozeOpenStop = !isStop;
				if(isStop){
					ed.remove(ai.getPackageName()+"/openstop");
				}else{
					ed.putBoolean(ai.getPackageName()+"/openstop",!isStop);
				}
				ed.commit();
				buttonView.setImageResource(ai.isDozeOpenStop?R.mipmap.icon_add:R.mipmap.icon_notdisable);
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,offIv,onIv,iceIv,openStopIv;
	}
}

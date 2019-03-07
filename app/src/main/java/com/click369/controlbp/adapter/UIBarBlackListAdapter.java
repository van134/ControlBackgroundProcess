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

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.activity.DozeWhiteListActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.PinyinCompare;

import java.util.ArrayList;
import java.util.Collections;

public class UIBarBlackListAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Context c;
	private SharedPreferences modPrefs;
	public String fliterName = "";
	public UIBarBlackListAdapter(Context context, SharedPreferences modPrefs) {
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
		ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNoChoose = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNewApp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempRun = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempDisableApp = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&ai.isBarLockList){
				this.bjdatas.add(ai);
			}else if(sortType == 1&&ai.isBarColorList){
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
			convertView= inflater.inflate(R.layout.item_uiblacklistapp, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_ui_appname);
			viewHolder.topBarIv = (ImageView) convertView.findViewById(R.id.item_ui_topbar_tv);
			viewHolder.bottomBarIv = (ImageView)convertView.findViewById(R.id.item_ui_bottombar_tv);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_ui_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_ui_iceicon);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.appNameTv.setText(data.appName);
		viewHolder.appNameTv.setTextColor(data.isRunning?(data.isInMuBei?Color.parseColor(MainActivity.COLOR_MUBEI):(MainActivity.pkgIdleStates.contains(data.packageName)?Color.parseColor(MainActivity.COLOR_IDLE):Color.parseColor(MainActivity.COLOR_RUN))):(data.isDisable?Color.LTGRAY: ControlFragment.curColor));
		viewHolder.appIcon.setImageBitmap(AppLoaderUtil.allHMAppIcons.get(data.packageName));
//		Glide.with( c ).load( Uri.fromFile(data.iconFile ) ).into(viewHolder.appIcon );
		viewHolder.iceIv.setImageResource(data.isDisable?R.mipmap.ice: data.isSetTimeStopApp?R.mipmap.icon_clock:R.mipmap.empty);
		viewHolder.appNameTv.setTag(position);
		viewHolder.bottomBarIv.setTag(position);
		viewHolder.topBarIv.setTag(position);
		viewHolder.bottomBarIv.setImageResource(data.isBarColorList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		viewHolder.topBarIv.setImageResource(data.isBarLockList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		if(!MainActivity.isModuleActive()){
			convertView.setAlpha(0.5f);
		}
		if(!data.isBarColorList){
			viewHolder.topBarIv.setAlpha(0.5f);
		}else{
			viewHolder.topBarIv.setAlpha(1.0f);
		}
		viewHolder.topBarIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				if(!ai.isBarColorList){
					return;
				}
				DozeWhiteListActivity.isClick = true;
				BaseActivity.zhenDong(c);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/locklist",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ed.remove(ai.getPackageName()+"/locklist");
				}else{
					ed.putBoolean(ai.getPackageName()+"/locklist",true);
				}
				ai.isBarLockList = !isStop;
				ed.commit();
				buttonView.setImageResource(ai.isBarLockList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
				XposedStopApp.stopApk(ai.getPackageName(),c);
				}
		});
		viewHolder.bottomBarIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				DozeWhiteListActivity.isClick = true;
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isStop = modPrefs.getBoolean(ai.getPackageName()+"/colorlist",false);
				SharedPreferences.Editor ed = modPrefs.edit();
				if(isStop){
					ed.remove(ai.getPackageName()+"/colorlist");
					ed.remove(ai.getPackageName()+"/locklist");
					ai.isBarLockList = false;
				}else{
					ed.putBoolean(ai.getPackageName()+"/colorlist",true);
				}
				ed.commit();
				ai.isBarColorList = !isStop;
				if (!ai.isBarColorList){
					notifyDataSetChanged();
				}
				buttonView.setImageResource(ai.isBarColorList?R.mipmap.icon_add:R.mipmap.icon_notdisable);
				XposedStopApp.stopApk(ai.getPackageName(),c);
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView appIcon,topBarIv,bottomBarIv,iceIv;
	}
}

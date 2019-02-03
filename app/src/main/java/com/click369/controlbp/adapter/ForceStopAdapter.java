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

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.fragment.ForceStopFragment;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class ForceStopAdapter extends BaseAdapter{
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public int sortType = -1;
	private Context c;
//	private SharedPreferences modPrefs;
//	private SharedPreferences muBeiPrefs;
//	private SharedPreferences appStartPrefs;
	public String fliterName = "u";
//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public ForceStopAdapter(Context context) {
		c = context;
		inflater = LayoutInflater.from(context);
//		this.modPrefs = modPrefs;
//		this.muBeiPrefs = muBeiPrefs;
//		this.appStartPrefs = appStartPrefs;
	}

	public void setData(ArrayList<AppInfo> datas){
		bjdatas.clear();
		bjdatas.addAll(datas);
//		this.notifyDataSetChanged();
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
		ArrayList<AppInfo> tempDisableApp = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(sortType == 0&&(ai.isBackForceStop)){//||ai.isBackMuBei
				this.bjdatas.add(ai);
			}else if(sortType == 1&&(ai.isHomeMuBei)){//ai.isHomeForceStop||
				this.bjdatas.add(ai);
			}else if(sortType == 2&&(ai.isOffscForceStop)){//||ai.isOffscMuBei
				this.bjdatas.add(ai);
			}else if(sortType == 3&&ai.isNotifyNotExit){
				this.bjdatas.add(ai);
			}else if(sortType == 4&&ai.isBackMuBei){
				this.bjdatas.add(ai);
			}else if(sortType == 5&&ai.isOffscMuBei){
				this.bjdatas.add(ai);
			}else if(sortType == 6&&ai.isHomeIdle){
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
			convertView= inflater.inflate(R.layout.item_forcestopapp, null);
			viewHolder = new ViewHolder();
			viewHolder.appNameTv = (TextView)convertView.findViewById(R.id.item_force_appname);
			viewHolder.appTimeTv = (TextView)convertView.findViewById(R.id.item_main_apptime);
			viewHolder.backIv = (ImageView) convertView.findViewById(R.id.item_force_back);
			viewHolder.homeIv = (ImageView) convertView.findViewById(R.id.item_force_home);
			viewHolder.offIv = (ImageView) convertView.findViewById(R.id.item_force_offsc);
			viewHolder.nofityIv = (ImageView) convertView.findViewById(R.id.item_force_notify);
			viewHolder.appIcon= (ImageView) convertView.findViewById(R.id.item_force_appicon);
			viewHolder.iceIv= (ImageView) convertView.findViewById(R.id.item_force_iceicon);
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
		viewHolder.offIv.setTag(position);
		viewHolder.homeIv.setTag(position);
		viewHolder.backIv.setTag(position);
		viewHolder.nofityIv.setTag(position);
		viewHolder.offIv.setImageResource(data.isOffscMuBei?R.mipmap.icon_dead:data.isOffscForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		viewHolder.backIv.setImageResource(data.isBackMuBei?R.mipmap.icon_dead:data.isBackForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
		viewHolder.homeIv.setImageResource(data.isHomeMuBei?R.mipmap.icon_dead:data.isHomeIdle?R.mipmap.icon_idle:R.mipmap.icon_notdisable);//data.isHomeForceStop?R.mipmap.icon_disable:
		viewHolder.nofityIv.setImageResource(data.isNotifyNotExit?R.mipmap.icon_add:R.mipmap.icon_notdisable);
		if (MainActivity.isModuleActive()){
			viewHolder.homeIv.setEnabled(true);
			viewHolder.homeIv.setAlpha(1.0f);
		}else{
			viewHolder.homeIv.setEnabled(false);
			viewHolder.homeIv.setAlpha(0.5f);
		}
		viewHolder.backIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				SharedPreferences.Editor ed = SharedPrefsUtil.getInstance(c).forceStopPrefs.edit();
				boolean isStop = SharedPrefsUtil.getInstance(c).forceStopPrefs.getBoolean(ai.getPackageName()+"/backstop",false);
				boolean isMubei = SharedPrefsUtil.getInstance(c).forceStopPrefs.getBoolean(ai.getPackageName()+"/backmubei",false);
				if (isStop){
					ed.remove(ai.getPackageName()+"/backstop").commit();
					ai.isBackForceStop = false;
					if(MainActivity.isLinkStopAndAuto&&ai.isAutoStart&&!ai.isOffscForceStop){
						SharedPrefsUtil.getInstance(c).autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
						ai.isAutoStart = false;
					}
					if (MainActivity.isLinkStopAndRemoveStop&&ai.isRecentForceClean&&!ai.isOffscForceStop) {
						SharedPrefsUtil.getInstance(c).recentPrefs.edit().remove(ai.getPackageName() + "/forceclean").commit();
						ai.isRecentForceClean = false;
					}
					if (MainActivity.isModuleActive()) {
						if (ai.isServiceStop){
							AlertUtil.showAlertMsg(c,"检测到你已经在第一项的禁用服务中禁用了该应用的服务，所以设置墓碑模式将不会生效，如果要使用墓碑模式请在第一项的禁用服务中取消勾选。");
						}else{
							if (ai.isHomeMuBei){
								AlertUtil.showAlertMsg(c,"检测到你已经设置了后台时墓碑，所以设置返回时墓碑将不会生效");
							}else {
								ed.putBoolean(ai.getPackageName() + "/backmubei", true).commit();
								ai.isBackMuBei = true;
							}
						}
					}else{
						Toast.makeText(c,"墓碑模式需要XP支持",Toast.LENGTH_SHORT).show();
					}
				}else if (isMubei){
					ed.remove(ai.getPackageName()+"/backstop").commit();
					ai.isBackForceStop = false;
					if(MainActivity.isLinkStopAndAuto&&ai.isAutoStart&&!ai.isOffscForceStop){
						SharedPrefsUtil.getInstance(c).autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
						ai.isAutoStart = false;
					}
					if (MainActivity.isLinkStopAndRemoveStop&&ai.isRecentForceClean&&!ai.isOffscForceStop) {
						SharedPrefsUtil.getInstance(c).recentPrefs.edit().remove(ai.getPackageName() + "/forceclean").commit();
						ai.isRecentForceClean = false;
					}
					ed.remove(ai.getPackageName()+"/backmubei").commit();
					ai.isBackMuBei = false;
					if(!ai.isHomeMuBei&&!ai.isBackMuBei&&!ai.isOffscMuBei){
						BaseActivity.sendBroadAMSRemovePkg(c,ai.getPackageName());
					}
				}else{
					ed.putBoolean(ai.getPackageName()+"/backstop",true).commit();
					ai.isBackForceStop = true;
					if (MainActivity.isLinkStopAndAuto&&!ai.isAutoStart) {
						SharedPrefsUtil.getInstance(c).autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/autostart", true).commit();
						ai.isAutoStart = true;
					}
					if (MainActivity.isLinkStopAndRemoveStop&&!ai.isRecentForceClean&&!ai.isRecentNotClean) {
						SharedPrefsUtil.getInstance(c).recentPrefs.edit().putBoolean(ai.getPackageName() + "/forceclean", true).commit();
						ai.isRecentForceClean = true;
					}
					ed.remove(ai.getPackageName()+"/backmubei").commit();
					ai.isBackMuBei = false;
					if(!ai.isHomeMuBei&&!ai.isBackMuBei&&!ai.isOffscMuBei){
						BaseActivity.sendBroadAMSRemovePkg(c,ai.getPackageName());
						ai.isInMuBei = false;
					}
				}
				buttonView.setImageResource(ai.isBackMuBei?R.mipmap.icon_dead:ai.isBackForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
				ForceStopFragment.isClick = true;
			}
		});

		viewHolder.homeIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				SharedPreferences.Editor ed = SharedPrefsUtil.getInstance(c).forceStopPrefs.edit();
//				boolean isMubei = c.forceStopPrefs.getBoolean(ai.getPackageName()+"/homemubei",false);
				if (ai.isHomeMuBei){
					ed.remove(ai.getPackageName()+"/homestop").commit();
					ed.remove(ai.getPackageName()+"/homemubei").commit();
					ai.isHomeMuBei = false;
					if(!ai.isHomeMuBei&&!ai.isBackMuBei&&!ai.isOffscMuBei){
						BaseActivity.sendBroadAMSRemovePkg(c,ai.getPackageName());
						ai.isInMuBei = false;
					}
				}else if(ai.isHomeIdle){
					ed.remove(ai.getPackageName()+"/idle").commit();
					ai.isHomeIdle = false;

					ed.remove(ai.getPackageName()+"/homestop").commit();
					if (MainActivity.isModuleActive()){
						if (ai.isServiceStop){
							AlertUtil.showAlertMsg(c,"检测到你已经在第一项的禁用服务中禁用了该应用的服务，所以设置墓碑模式将不会生效，如果要使用墓碑模式请在第一项的禁用服务中取消勾选。");
						}else {
							ed.putBoolean(ai.getPackageName() + "/homemubei", true).commit();
							ai.isHomeMuBei = true;
							boolean isContains = false;
							if(ai.isBackMuBei||ai.isOffscMuBei){
								AlertUtil.showAlertMsg(c,"检测到你已经在设置了"+(ai.isBackMuBei?"返回时":"熄屏时")+"墓碑，选择后台墓碑后会自动取消"+(ai.isBackMuBei?"返回时":"熄屏时")+"墓碑设置。");
							}
							if(ai.isBackMuBei){
								ed.remove(ai.getPackageName() + "/backmubei").commit();
								ai.isBackMuBei = false;
								isContains = true;
							}
							if(ai.isOffscMuBei){
								ed.remove(ai.getPackageName() + "/offmubei").commit();
								ai.isOffscMuBei = false;
								isContains = true;
							}
							if(isContains){
								notifyDataSetChanged();
							}
						}
					}else{
						Toast.makeText(c,"墓碑模式需要XP支持",Toast.LENGTH_SHORT).show();
					}
				}else{
					ed.putBoolean(ai.getPackageName()+"/idle",true).commit();
					ai.isHomeIdle = true;
				}
				buttonView.setImageResource(ai.isHomeMuBei?R.mipmap.icon_dead:ai.isHomeIdle?R.mipmap.icon_idle:R.mipmap.icon_notdisable);
				ForceStopFragment.isClick = true;
			}
		});
		viewHolder.offIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				SharedPreferences.Editor ed =SharedPrefsUtil.getInstance(c).forceStopPrefs.edit();
				boolean isStop = SharedPrefsUtil.getInstance(c).forceStopPrefs.getBoolean(ai.getPackageName()+"/offstop",false);
				boolean isMubei =SharedPrefsUtil.getInstance(c).forceStopPrefs.getBoolean(ai.getPackageName()+"/offmubei",false);
				if (isStop){
					ed.remove(ai.getPackageName()+"/offstop").commit();
					ai.isOffscForceStop = false;
					if(MainActivity.isLinkStopAndAuto&&!ai.isRecentForceClean&&!ai.isBackForceStop){
						SharedPrefsUtil.getInstance(c).autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
						ai.isAutoStart = false;
					}
					if (MainActivity.isModuleActive()) {
						if (ai.isServiceStop){
							AlertUtil.showAlertMsg(c,"检测到你已经在第一项的禁用服务中禁用了该应用的服务，所以设置墓碑模式将不会生效，如果要使用墓碑模式请在第一项的禁用服务中取消勾选。");
						}else {
							if (SharedPrefsUtil.getInstance(c).forceStopPrefs.contains(ai.getPackageName() + "/homemubei")){
								AlertUtil.showAlertMsg(c,"检测到你已经设置了后台时墓碑，所以设置熄屏时墓碑将不会生效");
							}else{
								ed.putBoolean(ai.getPackageName() + "/offmubei", true).commit();
								ai.isOffscMuBei = true;
							}
						}
					}else{
						Toast.makeText(c,"墓碑模式需要XP支持",Toast.LENGTH_SHORT).show();
					}
				}else if (isMubei){
					ed.remove(ai.getPackageName()+"/offstop").commit();
					ai.isOffscForceStop = false;
					if(MainActivity.isLinkStopAndAuto&&ai.isAutoStart&&!ai.isBackForceStop){
						SharedPrefsUtil.getInstance(c).autoStartNetPrefs.edit().remove(ai.getPackageName()+"/autostart").commit();
						ai.isAutoStart = false;
					}
					if (MainActivity.isLinkStopAndRemoveStop&&ai.isRecentForceClean&&!ai.isBackForceStop) {
						SharedPrefsUtil.getInstance(c).recentPrefs.edit().remove(ai.getPackageName() + "/forceclean").commit();
						ai.isRecentForceClean = false;
					}
					ed.remove(ai.getPackageName()+"/offmubei").commit();
					ai.isOffscMuBei = false;
					if(!ai.isHomeMuBei&&!ai.isBackMuBei&&!ai.isOffscMuBei){
						BaseActivity.sendBroadAMSRemovePkg(c,ai.getPackageName());
						ai.isInMuBei = false;
					}
				}else{
					ed.putBoolean(ai.getPackageName()+"/offstop",true).commit();
					ai.isOffscForceStop = true;
					ed.remove(ai.getPackageName()+"/offmubei").commit();
					ai.isOffscMuBei = false;
					if (MainActivity.isLinkStopAndAuto&&!ai.isAutoStart) {
						SharedPrefsUtil.getInstance(c).autoStartNetPrefs.edit().putBoolean(ai.getPackageName() + "/autostart", true).commit();
						ai.isAutoStart = true;
					}
					if (MainActivity.isLinkStopAndRemoveStop&&!ai.isRecentForceClean&&!ai.isRecentNotClean) {
						SharedPrefsUtil.getInstance(c).recentPrefs.edit().putBoolean(ai.getPackageName() + "/forceclean", true).commit();
						ai.isRecentForceClean = true;
					}
				}
				buttonView.setImageResource(ai.isOffscMuBei?R.mipmap.icon_dead:ai.isOffscForceStop?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
				ForceStopFragment.isClick = true;
			}
		});
		viewHolder.nofityIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				ImageView buttonView = (ImageView)(v);
				int g = (Integer)buttonView.getTag();
				AppInfo ai = bjdatas.get(g);
				boolean isAuto = SharedPrefsUtil.getInstance(c).forceStopPrefs.getBoolean(ai.getPackageName()+"/notifynotexit",false);
				ai.isNotifyNotExit = !isAuto;
				SharedPreferences.Editor ed = SharedPrefsUtil.getInstance(c).forceStopPrefs.edit();
				if(isAuto){
					ed.remove(ai.getPackageName()+"/notifynotexit");
				}else{
					ed.putBoolean(ai.getPackageName()+"/notifynotexit",!isAuto);
				}
				ed.commit();
				buttonView.setImageResource(ai.isNotifyNotExit?R.mipmap.icon_add:R.mipmap.icon_notdisable);
				ForceStopFragment.isClick = true;
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView appNameTv,appTimeTv;
		public ImageView appIcon,backIv,homeIv,offIv,nofityIv,iceIv;
	}
}

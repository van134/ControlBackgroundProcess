package com.click369.controlbp.adapter;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.bean.PkgAndName;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.PinyinStringCompare;
import com.click369.controlbp.util.TimeUtil;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class WakeLockAdapter extends BaseExpandableListAdapter{
	public final HashMap<String, ArrayList<String>> setWakeLocks = new HashMap<String, ArrayList<String>>();

	public final HashMap<String, Integer> wakeLockAllowCounts = new HashMap<String, Integer>();
	public final HashMap<String, Integer> wakeLockNotAllowCounts = new HashMap<String, Integer>();
	public final HashMap<String, Long> wakeLockAllowTimes = new HashMap<String, Long>();
	public final HashMap<String, Long> wakeLockNotAllowTimes = new HashMap<String, Long>();
	public final HashMap<String, ArrayList<String>> wakeLocks = new HashMap<String, ArrayList<String>>();
	public final HashMap<String, ArrayList<Long>> wakeLockAllTimes = new HashMap<String, ArrayList<Long>>();
	public final HashMap<String, ArrayList<Long>> wakeLockNotAllowAllTimes = new HashMap<String, ArrayList<Long>>();

	public final HashMap<String, ArrayList<String>> wakeLockTemps = new HashMap<String, ArrayList<String>>();
	public final ArrayList<PkgAndName> bjdatas = new ArrayList<PkgAndName>();
	private LayoutInflater inflater;
	private Activity act;
	private SharedPreferences wakeLockPrefs;
	private NumberFormat  nf = NumberFormat.getInstance();

	public WakeLockAdapter(Activity context, SharedPreferences wakeLockPrefs) {
		inflater = LayoutInflater.from(context);
		this.act = context;
		this.wakeLockPrefs = wakeLockPrefs;
		nf.setMaximumFractionDigits(2);
	}
	public void appendData(HashMap<String, ArrayList<String>> wakeLocks,
						   HashMap<String, Integer> wakeLockAllowCounts,
						   HashMap<String, Integer> wakeLockNotAllowCounts,
						   HashMap<String, Long> wakeLockAllowTimes,
						   HashMap<String, Long> wakeLockNotAllowTimes,
						   HashMap<String, ArrayList<Long>> wakeLockAllTimes,
						   HashMap<String, ArrayList<Long>> wakeLockNotAllowAllTimes){
		if (wakeLockAllowCounts!=null) {
			this.wakeLockAllowCounts.putAll(wakeLockAllowCounts);
		}
		if (wakeLockNotAllowCounts!=null) {
			this.wakeLockNotAllowCounts.putAll(wakeLockNotAllowCounts);
		}
		if (wakeLockAllowTimes!=null) {
			this.wakeLockAllowTimes.putAll(wakeLockAllowTimes);
		}
		if (wakeLockNotAllowTimes!=null) {
			this.wakeLockNotAllowTimes.putAll(wakeLockNotAllowTimes);
		}
		if (wakeLockAllTimes!=null) {
			this.wakeLockAllTimes.putAll(wakeLockAllTimes);
		}
		if (wakeLockNotAllowAllTimes!=null) {
			this.wakeLockNotAllowAllTimes.putAll(wakeLockNotAllowAllTimes);
		}
//		this.wakeLocks.putAll(wakeLocks);
		if(wakeLocks!=null) {
			for (String key : wakeLocks.keySet()) {
				if (!this.wakeLocks.containsKey(key)) {
					this.wakeLocks.put(key, wakeLocks.get(key));
				} else {
					ArrayList<String> names = this.wakeLocks.get(key);
					HashSet<String> sets = new HashSet<>(names);
					sets.addAll(wakeLocks.get(key));
					this.wakeLocks.put(key, new ArrayList<String>(sets));
				}
			}
		}
		wakeLockTemps.clear();
		wakeLockTemps.putAll(this.wakeLocks);
//		bjdatas.clear();
//		bjdatas.addAll(this.wakeLocks.keySet());
	}

	public void reload(){
		synchronized (bjdatas) {
			bjdatas.clear();
			wakeLockTemps.clear();
			wakeLockTemps.putAll(this.wakeLocks);
			initSetWakeLocks();
			for (String key : setWakeLocks.keySet()) {
				if (!this.wakeLockTemps.containsKey(key)) {
					this.wakeLockTemps.put(key, setWakeLocks.get(key));
				} else {
//				Log.i("CONTROL",key+"  key  "+setWakeLocks.get(key).size());
					ArrayList<String> names = this.wakeLockTemps.get(key);
					LinkedHashSet<String> sets = new LinkedHashSet<>(setWakeLocks.get(key));
					sets.addAll(names);
//				names.removeAll(setWakeLocks.get(key));
//				names.addAll(0,setWakeLocks.get(key));
					this.wakeLockTemps.put(key, new ArrayList<String>(sets));
				}
				ArrayList<String> lists = this.wakeLockTemps.get(key);
				ArrayList<String> temps1 = new ArrayList<String>();
				ArrayList<String> temps2 = new ArrayList<String>();
				for (String n : lists) {
					if (wakeLockPrefs.contains(key + "+" + n) || wakeLockPrefs.contains(key + "/startname")) {
						temps1.add(n);
					} else {
						temps2.add(n);
					}
				}
				this.wakeLockTemps.get(key).clear();
				this.wakeLockTemps.get(key).addAll(temps1);
				this.wakeLockTemps.get(key).addAll(temps2);
			}
			Set<String> keys = this.wakeLockTemps.keySet();
			PackageManager pm = act.getPackageManager();
			for (String key : keys) {
				PkgAndName pan = new PkgAndName();
				pan.pkg = key;
				PackageInfo packageInfo = null;
				try {
					packageInfo = pm.getPackageInfo(key, PackageManager.GET_META_DATA);
					if (packageInfo != null&&pm!=null) {
						CharSequence cs = pm.getApplicationLabel(packageInfo.applicationInfo);
						String appName = cs == null ? key : cs.toString();
						pan.name = appName;
					} else {
						pan.name = key;
					}
				} catch (Exception e) {
					pan.name = key;
				}

				for (String allowKey : wakeLockAllowTimes.keySet()) {
					if (allowKey!=null&&allowKey.startsWith(key + "/")) {
						pan.allowTime += wakeLockAllowTimes.get(allowKey);
					}
				}
				for (String notallowKey : wakeLockNotAllowTimes.keySet()) {
					if (notallowKey!=null&&notallowKey.startsWith(key + "/")) {
						pan.notAllowTime += wakeLockNotAllowTimes.get(notallowKey);
					}
				}
				bjdatas.add(pan);
			}
			PinyinStringCompare comparent = new PinyinStringCompare();
			Collections.sort(this.bjdatas, comparent);
			ArrayList<PkgAndName> tempDatas1 = new ArrayList<PkgAndName>();
			ArrayList<PkgAndName> tempDatas2 = new ArrayList<PkgAndName>();
			for (PkgAndName pan : this.bjdatas) {
				if (setWakeLocks.containsKey(pan.pkg)) {
					tempDatas1.add(pan);
				} else {
					tempDatas2.add(pan);
				}
			}
			this.bjdatas.clear();
			this.bjdatas.addAll(tempDatas1);
			this.bjdatas.addAll(tempDatas2);
			notifyDataSetChanged();
		}
	}

	private void initSetWakeLocks(){
		setWakeLocks.clear();
		Set<String> prefKeys = wakeLockPrefs.getAll().keySet();
		for(String prefKey:prefKeys){
//			Log.i("CONTROL",prefKey+"  prefKey");
			int index = prefKey.indexOf("+");
			if(index!=-1){
				String ss[] = prefKey.split("\\+");
				if(ss!=null&&ss.length>1){
					if(setWakeLocks.containsKey(ss[0])){
						setWakeLocks.get(ss[0]).add(ss[1]);
					}else{
						ArrayList<String> names = new ArrayList<String>();
						names.add(ss[1]);
						setWakeLocks.put(ss[0],names);
					}
				}
			}else{
				if(prefKey.endsWith("/startname")){
					String pkg = prefKey.replace("/startname","");
					if(setWakeLocks.containsKey(pkg)){
						setWakeLocks.get(pkg).add(0,"以'"+wakeLockPrefs.getString(prefKey,"null")+"'开头");
//						Log.i("CONTROL",wakeLockPrefs.getString(prefKey,"null")+"  001");
					}else{
						ArrayList<String> names = new ArrayList<String>();
						names.add("以'"+wakeLockPrefs.getString(prefKey,"null")+"'开头");
						setWakeLocks.put(pkg,names);
//						Log.i("CONTROL",wakeLockPrefs.getString(prefKey,"null")+"  002");
					}
				}
			}
		}
	}

	public void clear(){
		bjdatas.clear();
		this.wakeLockAllowCounts.clear();
		this.wakeLockNotAllowCounts.clear();
		this.wakeLockNotAllowAllTimes.clear();
		this.wakeLockAllTimes.clear();
		this.wakeLockNotAllowTimes.clear();
		this.wakeLockAllowTimes.clear();
		this.wakeLocks.clear();
		this.wakeLockTemps.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return bjdatas.size();
	}

	@Override
	public int getChildrenCount(int i) {
		return wakeLockTemps.get(bjdatas.get(i).pkg).size();
	}

	@Override
	public Object getGroup(int i) {
		return bjdatas.get(i);
	}

	@Override
	public Object getChild(int i, int i1) {
		return wakeLockTemps.get(bjdatas.get(i).pkg).get(i1);
	}

	@Override
	public long getGroupId(int i) {
		return i;
	}

	@Override
	public long getChildId(int i, int i1) {
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
		ViewHolder viewHolder;
		PkgAndName data = bjdatas.get(i);
		if(view == null){
			view= inflater.inflate(R.layout.item_wakelockone, null);
			viewHolder = new ViewHolder();
			viewHolder.nameTv = (TextView)view.findViewById(R.id.wakelock_name);
			viewHolder.addRoleTv = (TextView)view.findViewById(R.id.wakelock_addrole_tv);
//			viewHolder.setTv = (TextView)view.findViewById(R.id.wakelock_set);
			view.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)view.getTag();
		}

		viewHolder.nameTv.setText(data.name+"("+wakeLockTemps.get(data.pkg).size()+"个 允许"+getTimeByMils(data.allowTime)+",拒绝"+getTimeByMils(data.notAllowTime)+")");
//		viewHolder.setTv.setText("▼");
		viewHolder.nameTv.setTextColor(setWakeLocks.containsKey(data.pkg)? Color.RED: ControlFragment.curColor);
		viewHolder.addRoleTv.setTextColor(wakeLockPrefs.contains(data.pkg+"/startname")? Color.RED: ControlFragment.curColor);
		viewHolder.addRoleTv.setTag(i);
		viewHolder.addRoleTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int tag = (Integer) view.getTag();
				final PkgAndName data = bjdatas.get(tag);
				String titles[] = {"设置规则","删除规则"};
				AlertUtil.showListAlert(act, "请选择对"+data.name+"规则操作\n该功能主要用来处理随机唤醒锁", titles, new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						if (tag == 0){
							AlertUtil.inputAlert(act, new AlertUtil.InputCallBack() {
								@Override
								public void backData(String txt, int tag) {
									wakeLockPrefs.edit().putString(data.pkg+"/startname",txt).commit();
									reload();
								}
							});
						}else if(tag == 1){
							wakeLockPrefs.edit().remove(data.pkg+"/startname").commit();
							wakeLockPrefs.edit().remove(data.pkg+"/starttime").commit();
							reload();
						}
					}
				});

			}
		});
		return view;
	}

	@Override
	public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
		ViewHolder1 viewHolder;
		String pkg = bjdatas.get(i).pkg;
		String name = wakeLockTemps.get(pkg).get(i1);
		if(view == null){
			view= inflater.inflate(R.layout.item_wakelocktwo, null);
			viewHolder = new ViewHolder1();
			viewHolder.nameTv = (TextView)view.findViewById(R.id.wakelock_name);
			viewHolder.allowcountTv = (TextView)view.findViewById(R.id.wakelock_allowcount);
			viewHolder.notallowcountTv = (TextView)view.findViewById(R.id.wakelock_notallowcount);
			viewHolder.timeTv = (TextView)view.findViewById(R.id.wakelock_time);
			viewHolder.setTv = (TextView)view.findViewById(R.id.wakelock_set);
			view.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder1)view.getTag();
		}
		view.setBackgroundColor(Color.parseColor("#331a9dac"));
		viewHolder.nameTv.setText(name);
		boolean isRole = ("以'"+wakeLockPrefs.getString(pkg+"/startname","")+"'开头").equals(name);
		int allowCount = (wakeLockAllowCounts.containsKey(pkg+"/"+name)?wakeLockAllowCounts.get(pkg+"/"+name):0);
		viewHolder.allowcountTv.setText(isRole?"":"允许\n"+allowCount+"次\n"+getTimeByMils(pkg,name,true));
		int notAllowCount = (wakeLockNotAllowCounts.containsKey(pkg+"/"+name)?wakeLockNotAllowCounts.get(pkg+"/"+name):0);
		viewHolder.notallowcountTv.setText(isRole?"":"阻止\n"+notAllowCount+"次\n"+getTimeByMils(pkg,name,false));
		viewHolder.notallowcountTv.setTextColor(notAllowCount>0?Color.RED:ControlFragment.curColor);
		long lastTime =wakeLockAllTimes.containsKey(pkg+"/"+name)? wakeLockAllTimes.get(pkg+"/"+name).get(0):0;
		viewHolder.timeTv.setText(isRole?"":"上次\n"+TimeUtil.changeMils2String(lastTime));
		int time = wakeLockPrefs.getInt(pkg+"+"+name,0);
		int roleTime = wakeLockPrefs.getInt(pkg+"/starttime",0);
		viewHolder.allowcountTv.setTextColor(allowCount>0&&time>0?Color.parseColor("#38cd27"):ControlFragment.curColor);
		viewHolder.setTv.setText(time!=0?"已控制\n"+time+"s":isRole&&roleTime>0?"已控制\n"+roleTime+"s":("未控制"+ (ContainsKeyWord.notWakeLock.contains(name)?"\n不建议":"")));
		viewHolder.setTv.setTextColor(time>0?Color.RED:isRole&&roleTime>0?Color.RED:ControlFragment.curColor);
		viewHolder.setTv.setTag(i+","+i1);
		viewHolder.allowcountTv.setTag(i+","+i1);
		viewHolder.notallowcountTv.setTag(i+","+i1);
		viewHolder.nameTv.setTag(name);
		viewHolder.setTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				String tag = (String)view.getTag();
				int one = Integer.parseInt(tag.split(",")[0]);
				int two = Integer.parseInt(tag.split(",")[1]);
				final String pkg = bjdatas.get(one).pkg;
				final String wkname = wakeLockTemps.get(pkg).get(two);
				final boolean isRole = ("以'"+wakeLockPrefs.getString(pkg+"/startname","")+"'开头").equals(wkname);
				int time = isRole? wakeLockPrefs.getInt(pkg+"/starttime",0):wakeLockPrefs.getInt(pkg+"+"+wkname,0);
				AlertUtil.inputAlert(act, "0为不控制", time>0?time+"":"", new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						int v = Integer.parseInt(txt);
						if (v <= 0){
							if (isRole){
								wakeLockPrefs.edit().remove(pkg+"/starttime").commit();
							}else{
								wakeLockPrefs.edit().remove(pkg+"+"+wkname).commit();
							}
						}else{
							if (isRole){
								wakeLockPrefs.edit().putInt(pkg+"/starttime",v).commit();
							}else{
								wakeLockPrefs.edit().putInt(pkg+"+"+wkname,v).commit();
							}
						}
						notifyDataSetChanged();
					}
				});
			}
		});
		viewHolder.allowcountTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				String tag = (String)view.getTag();
				int one = Integer.parseInt(tag.split(",")[0]);
				int two = Integer.parseInt(tag.split(",")[1]);
				final String pkg = bjdatas.get(one).pkg;
				final String wkname = wakeLockTemps.get(pkg).get(two);
				ArrayList<Long> times = wakeLockAllTimes.get(pkg+"/"+wkname);
				if(times!=null){
					String titls[] = new String[times.size()];
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss_SSS");
					for(int i = 0;i<times.size();i++){
						titls[i] = sdf.format(new Date(times.get(i)));
					}
					AlertUtil.showListAlert(act,"允许唤醒的时间点(最多100条)",titls,null);
				}else{
					Toast.makeText(act,"还未获取到历史记录",Toast.LENGTH_SHORT).show();
				}
			}
		});
		viewHolder.notallowcountTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				String tag = (String)view.getTag();
				int one = Integer.parseInt(tag.split(",")[0]);
				int two = Integer.parseInt(tag.split(",")[1]);
				final String pkg = bjdatas.get(one).pkg;
				final String wkname = wakeLockTemps.get(pkg).get(two);
				ArrayList<Long> times = wakeLockNotAllowAllTimes.get(pkg+"/"+wkname);
				if(times!=null){
					String titls[] = new String[times.size()];
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss_SSS");
					for(int i = 0;i<times.size();i++){
						titls[i] = sdf.format(new Date(times.get(i)));
					}
					AlertUtil.showListAlert(act,"阻止唤醒的时间点(最多100条)",titls,null);
				}else{
					Toast.makeText(act,"还未获取到历史记录",Toast.LENGTH_SHORT).show();
				}
			}
		});
		viewHolder.nameTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				AlertUtil.showConfirmAlertMsg(act, ""+view.getTag(),"唤醒锁全名","复制名称","取消", new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						if(tag == 1){
							ClipboardManager cm = (ClipboardManager)act.getSystemService(Context.CLIPBOARD_SERVICE);
							cm.setText(view.getTag()+"");
						}
					}
				});
			}
		});
		return view;
	}

	@Override
	public boolean isChildSelectable(int i, int i1) {
		return false;
	}


	static class ViewHolder{
		public TextView nameTv, addRoleTv;//,setTv;
	}

	static class ViewHolder1{
		public TextView nameTv,allowcountTv,notallowcountTv,timeTv,setTv;
	}

	String getTimeByMils(String pkg,String tag,boolean isAllow){
		long time = 0;
		if (isAllow){
			time = (wakeLockAllowTimes.containsKey(pkg+"/"+tag)?wakeLockAllowTimes.get(pkg+"/"+tag):0);
		}else {
			time = (wakeLockNotAllowTimes.containsKey(pkg+"/"+tag)?wakeLockNotAllowTimes.get(pkg+"/"+tag):0);
		}
		return getTimeByMils(time);
	}
	String getTimeByMils(long t){
		float time = t/1000.0f;
		if (time<=60){
			return nf.format(time)+"s";
		}else if (time<=60*60){
			int min = (int)time/60;
			int sec = (int)time%60;
			return min+"m"+sec+"s";
		}else{
			int hour = (int)time/(60*60);
			int min = (int)time%(60*60)/60;
			int sec = (int)time%60;
			return hour+"h"+ min+"m"+sec+"s";
		}
	}
}

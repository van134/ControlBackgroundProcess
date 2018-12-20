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
import java.util.LinkedHashSet;
import java.util.Set;

public class AlarmAdapter extends BaseExpandableListAdapter{
	public final HashMap<String, ArrayList<String>> setAlarms = new HashMap<String, ArrayList<String>>();

	public final HashMap<String, Integer> alarmAllowCounts = new HashMap<String, Integer>();
	public final HashMap<String, Integer> alarmNotAllowCounts = new HashMap<String, Integer>();
	public final HashMap<String, ArrayList<String>> alarms = new HashMap<String, ArrayList<String>>();
	public final HashMap<String, ArrayList<Long[]>> alarmAllTimes = new HashMap<String, ArrayList<Long[]>>();
	public final HashMap<String, ArrayList<Long[]>> alarmNotAllowAllTimes = new HashMap<String, ArrayList<Long[]>>();

	public final HashMap<String, ArrayList<String>> alarmTemps = new HashMap<String, ArrayList<String>>();
	public final ArrayList<PkgAndName> bjdatas = new ArrayList<PkgAndName>();
	private LayoutInflater inflater;
	private Activity act;
	private SharedPreferences alarmPrefs;
	private NumberFormat  nf = NumberFormat.getInstance();

	public AlarmAdapter(Activity context, SharedPreferences alarmPrefs) {
		inflater = LayoutInflater.from(context);
		this.act = context;
		this.alarmPrefs = alarmPrefs;
		nf.setMaximumFractionDigits(2);
	}
	public void appendData(HashMap<String, ArrayList<String>> alarms,
						   HashMap<String, Integer> alarmAllowCounts,
						   HashMap<String, Integer> alarmNotAllowCounts,
						   HashMap<String, ArrayList<Long[]>> alarmAllTimes,
						   HashMap<String, ArrayList<Long[]>> alarmNotAllowAllTimes){
		if (alarmAllowCounts!=null) {
			this.alarmAllowCounts.putAll(alarmAllowCounts);
		}
		if (alarmNotAllowCounts!=null) {
			this.alarmNotAllowCounts.putAll(alarmNotAllowCounts);
		}
		if (alarmAllTimes!=null) {
			this.alarmAllTimes.putAll(alarmAllTimes);
		}
		if (alarmNotAllowAllTimes!=null) {
			this.alarmNotAllowAllTimes.putAll(alarmNotAllowAllTimes);
		}
		if(alarms!=null) {
			for (String key : alarms.keySet()) {
				if (!this.alarms.containsKey(key)) {
					this.alarms.put(key, alarms.get(key));
				} else {
					ArrayList<String> names = this.alarms.get(key);
					LinkedHashSet<String> sets = new LinkedHashSet<>(names);
					sets.addAll(alarms.get(key));
					this.alarms.put(key, new ArrayList<String>(sets));
				}
			}
		}
		alarmTemps.clear();
		alarmTemps.putAll(this.alarms);
	}

	public void reload(){
		synchronized (bjdatas) {
			bjdatas.clear();
			alarmTemps.clear();
			alarmTemps.putAll(this.alarms);
			initSetAlarms();
			for (String key : setAlarms.keySet()) {
				if (!this.alarmTemps.containsKey(key)) {
					this.alarmTemps.put(key, setAlarms.get(key));
				} else {
					ArrayList<String> names = this.alarmTemps.get(key);
					LinkedHashSet<String> sets = new LinkedHashSet<>(setAlarms.get(key));
					sets.addAll(names);
					this.alarmTemps.put(key, new ArrayList<String>(sets));
				}
				ArrayList<String> lists = this.alarmTemps.get(key);
				ArrayList<String> temps1 = new ArrayList<String>();
				ArrayList<String> temps2 = new ArrayList<String>();
				for (String n : lists) {
					if (alarmPrefs.contains(key + "+" + n) || alarmPrefs.contains(key + "/startname")) {
						temps1.add(n);
					} else {
						temps2.add(n);
					}
				}
				this.alarmTemps.get(key).clear();
				this.alarmTemps.get(key).addAll(temps1);
				this.alarmTemps.get(key).addAll(temps2);
			}
			Set<String> keys = this.alarmTemps.keySet();
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
				for (String name : alarmAllowCounts.keySet()) {
					if (name!=null&&name.startsWith(key + "/")) {
						pan.allowTime += alarmAllowCounts.get(name);
					}
				}
				for (String name : alarmNotAllowCounts.keySet()) {
					if (name!=null&&name.startsWith(key + "/")) {
						pan.notAllowTime += alarmNotAllowCounts.get(name);
					}
				}
				bjdatas.add(pan);
			}
			PinyinStringCompare comparent = new PinyinStringCompare();
			Collections.sort(this.bjdatas, comparent);
			ArrayList<PkgAndName> tempDatas1 = new ArrayList<PkgAndName>();
			ArrayList<PkgAndName> tempDatas2 = new ArrayList<PkgAndName>();
			for (PkgAndName pan : this.bjdatas) {
				if (setAlarms.containsKey(pan.pkg)) {
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

	private void initSetAlarms(){
		setAlarms.clear();
		Set<String> prefKeys = alarmPrefs.getAll().keySet();
		for(String prefKey:prefKeys){
//			Log.i("CONTROL",prefKey+"  prefKey");
			int index = prefKey.indexOf("+");
			if(index!=-1){
				String ss[] = prefKey.split("\\+");
				if(setAlarms.containsKey(ss[0])){
					setAlarms.get(ss[0]).add(ss[1]);
				}else{
					ArrayList<String> names = new ArrayList<String>();
					names.add(ss[1]);
					setAlarms.put(ss[0],names);
				}
			}else{
				if(prefKey.endsWith("/startname")){
					String pkg = prefKey.replace("/startname","");
					if(setAlarms.containsKey(pkg)){
						setAlarms.get(pkg).add(0,"以'"+alarmPrefs.getString(prefKey,"null")+"'开头");
					}else{
						ArrayList<String> names = new ArrayList<String>();
						names.add("以'"+alarmPrefs.getString(prefKey,"null")+"'开头");
						setAlarms.put(pkg,names);
					}
				}
			}
		}
	}

	public void clear(){
		bjdatas.clear();
		this.alarmAllowCounts.clear();
		this.alarmNotAllowCounts.clear();
		this.alarmNotAllowAllTimes.clear();
		this.alarmAllTimes.clear();
		this.alarms.clear();
		this.alarmTemps.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return bjdatas.size();
	}

	@Override
	public int getChildrenCount(int i) {
		return alarmTemps.get(bjdatas.get(i).pkg).size();
	}

	@Override
	public Object getGroup(int i) {
		return bjdatas.get(i);
	}

	@Override
	public Object getChild(int i, int i1) {
		return alarmTemps.get(bjdatas.get(i).pkg).get(i1);
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

		viewHolder.nameTv.setText(data.name+"("+alarmTemps.get(data.pkg).size()+"个 允许"+data.allowTime+"次,拒绝"+data.notAllowTime+"次)");
//		viewHolder.setTv.setText("▼");
		viewHolder.nameTv.setTextColor(setAlarms.containsKey(data.pkg)? Color.RED: ControlFragment.curColor);
		viewHolder.addRoleTv.setTextColor(alarmPrefs.contains(data.pkg+"/startname")? Color.RED: ControlFragment.curColor);
		viewHolder.addRoleTv.setTag(i);
		viewHolder.addRoleTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				int tag = (Integer) view.getTag();
				final PkgAndName data = bjdatas.get(tag);
				String titles[] = {"设置规则","删除规则"};
				AlertUtil.showListAlert(act, "请选择对"+data.name+"规则操作\n该功能主要用来处理随机定时器", titles, new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						if (tag == 0){
							AlertUtil.inputAlert(act, new AlertUtil.InputCallBack() {
								@Override
								public void backData(String txt, int tag) {
									alarmPrefs.edit().putString(data.pkg+"/startname",txt).commit();
									reload();
								}
							});
						}else if(tag == 1){
							alarmPrefs.edit().remove(data.pkg+"/startname").commit();
							alarmPrefs.edit().remove(data.pkg+"/starttime").commit();
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
		String name = alarmTemps.get(pkg).get(i1);
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
		boolean isRole = ("以'"+alarmPrefs.getString(pkg+"/startname","")+"'开头").equals(name);
		int allowCount = (alarmAllowCounts.containsKey(pkg+"/"+name)?alarmAllowCounts.get(pkg+"/"+name):0);
		viewHolder.allowcountTv.setText(isRole?"":"允许\n"+allowCount+"次");
		int notAllowCount = (alarmNotAllowCounts.containsKey(pkg+"/"+name)?alarmNotAllowCounts.get(pkg+"/"+name):0);
		viewHolder.notallowcountTv.setText(isRole?"":"阻止\n"+notAllowCount+"次");
		viewHolder.notallowcountTv.setTextColor(notAllowCount>0?Color.RED:ControlFragment.curColor);
		long lastTime = 0;
		try {
			lastTime =alarmAllTimes.containsKey(pkg+"/"+name)?alarmAllTimes.get(pkg+"/"+name).get(0)[0]:0;
		}catch (Exception e){
			e.printStackTrace();
		}
		viewHolder.timeTv.setText(isRole?"":"上次\n"+TimeUtil.changeMils2String(lastTime));
		int time = alarmPrefs.getInt(pkg+"+"+name,0);
		int roleTime = alarmPrefs.getInt(pkg+"/starttime",0);
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
				final String wkname = alarmTemps.get(pkg).get(two);
				final boolean isRole = ("以'"+alarmPrefs.getString(pkg+"/startname","")+"'开头").equals(wkname);
				int time = isRole? alarmPrefs.getInt(pkg+"/starttime",0):alarmPrefs.getInt(pkg+"+"+wkname,0);
				AlertUtil.inputAlert(act, "0为不控制", time>0?time+"":"", new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						int v = Integer.parseInt(txt);
						if (v <= 0){
							if (isRole){
								alarmPrefs.edit().remove(pkg+"/starttime").commit();
							}else{
								alarmPrefs.edit().remove(pkg+"+"+wkname).commit();
							}
						}else{
							if (isRole){
								alarmPrefs.edit().putInt(pkg+"/starttime",v).commit();
							}else{
								alarmPrefs.edit().putInt(pkg+"+"+wkname,v).commit();
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
				final String wkname = alarmTemps.get(pkg).get(two);
				ArrayList<Long[]> times = alarmAllTimes.get(pkg+"/"+wkname);
				if(times!=null){
					String titls[] = new String[times.size()];
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss_SSS");
					for(int i = 0;i<times.size();i++){
						titls[i] = "设定时间:"+sdf.format(new Date(times.get(i)[0]))+"\n执行时间:"+sdf.format(new Date(times.get(i)[1]));
					}
					AlertUtil.showListAlert(act,"允许定时器的时间点(最多100条)",titls,null);
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
				final String wkname = alarmTemps.get(pkg).get(two);
				ArrayList<Long[]> times =alarmNotAllowAllTimes.get(pkg+"/"+wkname);
				if(times!=null){
					String titls[] = new String[times.size()];
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss_SSS");
					for(int i = 0;i<times.size();i++){
						titls[i] = "设定时间:"+sdf.format(new Date(times.get(i)[0]))+"\n执行时间:"+sdf.format(new Date(times.get(i)[1]));
					}
					AlertUtil.showListAlert(act,"阻止定时器的时间点(最多100条)",titls,null);
				}else{
					Toast.makeText(act,"还未获取到历史记录",Toast.LENGTH_SHORT).show();
				}
			}
		});
		viewHolder.nameTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View view) {
				AlertUtil.showConfirmAlertMsg(act, ""+view.getTag(),"定时器全名","复制名称","取消", new AlertUtil.InputCallBack() {
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

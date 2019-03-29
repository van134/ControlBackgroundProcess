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
import com.click369.controlbp.bean.PkgAndName;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.fragment.ControlFragment;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public class PrivacyLogAdapter extends BaseExpandableListAdapter{
	public final LinkedHashMap<String,ArrayList<PriLog>> datas = new LinkedHashMap<String,ArrayList<PriLog>>();
	private LayoutInflater inflater;
	private Activity act;
	public PrivacyLogAdapter(Activity context) {
		inflater = LayoutInflater.from(context);
		this.act = context;
	}
	public void setData(LinkedHashMap<String,ArrayList<PriLog>> datas){
		this.datas.clear();
		this.datas.putAll(datas);
		notifyDataSetChanged();
	}

	public void clear(){
		datas.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getGroupCount() {
		return Common.PRIVACY_TITLES.length-2;
	}

	@Override
	public int getChildrenCount(int i) {
		return datas.containsKey(Common.PRIVACY_KEYS[i+2])?datas.get(Common.PRIVACY_KEYS[i+2]).size():0;
	}

	@Override
	public Object getGroup(int i) {
		return Common.PRIVACY_TITLES[i];
	}

	@Override
	public Object getChild(int i, int i1) {
		return datas.containsKey(Common.PRIVACY_KEYS[i+2])?datas.get(Common.PRIVACY_KEYS[i+2]).get(i1):null;
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
		String name = Common.PRIVACY_LOG_TITLES[i+2];
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

		String count = "";
		Object o = getChild(i,0);
		if(o ==null){
			viewHolder.addRoleTv.setText("暂无");
		}else{
			PriLog pl = (PriLog)o;
			viewHolder.addRoleTv.setText(pl.time);
			count = " "+getChildrenCount(i)+"次";
		}
		viewHolder.nameTv.setText(name+count);
		return view;
	}

	@Override
	public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
		ViewHolder1 viewHolder;
		Object o = getChild(i,i1);
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
		if(o!=null){
			PriLog pl = (PriLog)o;
			viewHolder.nameTv.setText(pl.time);
			viewHolder.timeTv.setText("");
			viewHolder.notallowcountTv.setText("");
			viewHolder.allowcountTv.setText("");
			viewHolder.setTv.setText(pl.isPrevent?"已处理":"未处理");
		}else{
			viewHolder.nameTv.setText("");
			viewHolder.timeTv.setText("");
			viewHolder.notallowcountTv.setText("");
			viewHolder.allowcountTv.setText("");
			viewHolder.setTv.setText("");
		}
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
	public static class PriLog{
		public String time;
		public boolean isPrevent;
		public String name;
	}
}

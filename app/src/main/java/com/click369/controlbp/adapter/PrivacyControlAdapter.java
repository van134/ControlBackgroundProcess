package com.click369.controlbp.adapter;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.IFWCompActivity;
import com.click369.controlbp.activity.PrivacyControlActivity;
import com.click369.controlbp.bean.PrivacyInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PinyinCompareDisableReceiver;
import com.click369.controlbp.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PrivacyControlAdapter extends BaseAdapter{
	public Set<String> bjdatas = new HashSet<String>();
	private LayoutInflater inflater;
	private PrivacyControlActivity c;
//	private String ifw = "";
	public PrivacyControlAdapter(PrivacyControlActivity context) {
		c = context;
		inflater = LayoutInflater.from(context);

	}

	public void setData(Set<String> infos){
		bjdatas.clear();
		bjdatas.addAll(infos);
		freshList();
	}

	public void freshList(){

		this.notifyDataSetChanged();
	}
	public void chooseAll(){
		this.notifyDataSetChanged();
	}
	public void cancelchooseAll(){
		this.notifyDataSetChanged();
	}

	public int getCount() {
		return Common.PRIVACY_TITLES.length;
	}

	
	public Object getItem(int position) {
		return Common.PRIVACY_TITLES[position];
	}

	
	public long getItemId(int position) {
		return position;
	}

	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		String name = Common.PRIVACY_TITLES[position];
		String data = Common.PRIVACY_KEYS[position];
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_ifwcomp, null);
			viewHolder = new ViewHolder();
			viewHolder.pb = (ProgressBar) convertView.findViewById(R.id.item_ifw_pb);
			viewHolder.nameTv = (TextView)convertView.findViewById(R.id.item_ifw_name);
			viewHolder.stopTv = (TextView)convertView.findViewById(R.id.item_ifw_stop);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}

		boolean isDiable = bjdatas.contains(data);
		if(position==Common.PRI_TYPE_CHANGELOC){
			viewHolder.stopTv.setText(isDiable?"已开启\n(长按设置位置)":"未开启");
			viewHolder.nameTv.setText(name+"\n"+Common.PRIVACY_ALERT[position]);
		}else if(position==Common.PRI_TYPE_CHANGETIME){
			viewHolder.stopTv.setText(isDiable?"已开启\n(长按设置时间)":"未开启");
			viewHolder.nameTv.setText(name+(isDiable? TimeUtil.changeMils2String(PrivacyControlActivity.setTime,"\n(当前时间起点为: yyyy-MM-dd HH:mm)"):""));
		}else if(position==Common.PRI_TYPE_DEVICEIMEIINFO){
			viewHolder.stopTv.setText(isDiable?"已开启\n(长按自定义IMEI)":"未开启");
			viewHolder.nameTv.setText(name+"\n"+(isDiable?(PrivacyControlActivity.IMEI.length()>0?("(当前的IMEI为:"+PrivacyControlActivity.IMEI+")"):Common.PRIVACY_ALERT[position]):Common.PRIVACY_ALERT[position]));
		}else if(position==Common.PRI_TYPE_DEVICEIMSIINFO){
			viewHolder.stopTv.setText(isDiable?"已开启\n(长按自定义IMSI)":"未开启");
			viewHolder.nameTv.setText(name+"\n"+(isDiable?(PrivacyControlActivity.IMSI.length()>0?("(当前的IMSI为:"+PrivacyControlActivity.IMSI+")"):Common.PRIVACY_ALERT[position]):Common.PRIVACY_ALERT[position]));
		}else if(position==Common.PRI_TYPE_REDIRFIEDIR){
			viewHolder.stopTv.setText(isDiable?"已重定向\n(长按自定义文件夹)":"未重定向");
			viewHolder.nameTv.setText(name+"\n"+(isDiable?(PrivacyControlActivity.newDir.length()>0?("(当前文件夹为:"+PrivacyControlActivity.newDir+")"):Common.PRIVACY_ALERT[position]):Common.PRIVACY_ALERT[position]));
		}else if(position==Common.PRI_TYPE_NETTYPE_WIFI||position==Common.PRI_TYPE_NETTYPE_4G){
			viewHolder.stopTv.setText(isDiable?"已开启":"未开启");
			viewHolder.nameTv.setText(name+"\n"+Common.PRIVACY_ALERT[position]);
		}else{
			viewHolder.stopTv.setText(isDiable?"已禁用":"未禁用");
			viewHolder.nameTv.setText(name+"\n"+Common.PRIVACY_ALERT[position]);
		}

		viewHolder.stopTv.setTextColor(isDiable? Color.RED:PrivacyControlActivity.curColor);
//		viewHolder.stopTv.setTag(position);
//		viewHolder.stopTv.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				int tag = (Integer) v.getTag();
//				String data = Common.PRIVACY_KEYS[tag];
//				boolean isDiable = bjdatas.contains(data);
//				if(isDiable){
//					bjdatas.remove(data);
//				}else{
//					bjdatas.add(data);
//				}
//				priPrefs.edit().putStringSet(pkg+"/prilist",bjdatas).commit();
//				notifyDataSetChanged();
//			}
//		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView nameTv,stopTv;
		public ProgressBar pb;
	}
}

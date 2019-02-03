package com.click369.controlbp.adapter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.IFWCompActivity;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PinyinCompareDisable;
import com.click369.controlbp.util.PinyinCompareDisableReceiver;

import java.util.ArrayList;
import java.util.Collections;

public class IFWCompActBroadAdapter extends BaseAdapter{
	public ArrayList<ActivityInfo> bjdatas = new ArrayList<ActivityInfo>();
	private LayoutInflater inflater;
	private IFWCompActivity c;
	private PackageManager pm;
//	private String ifw = "";
	public IFWCompActBroadAdapter(IFWCompActivity context, PackageManager pm) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.pm = pm;
	}

	public void setData(ArrayList<ActivityInfo> activityInfos){
		bjdatas.clear();
		for(ActivityInfo si1:activityInfos){
			bjdatas.add(si1);
		}
		freshList();
	}

//	public void setIFW(String ifw){
//		this.ifw = ifw;
//		freshList();
//	}

	public void freshList(){
		ArrayList<ActivityInfo> disTemp = new ArrayList<ActivityInfo>();
		ArrayList<ActivityInfo> enTemp = new ArrayList<ActivityInfo>();
		PinyinCompareDisableReceiver comparent = new PinyinCompareDisableReceiver();
		Collections.sort(this.bjdatas, comparent);
		for(ActivityInfo data :bjdatas){
			String dataName = data.name.trim();//.replaceAll("\\$","/\\$");
			boolean isDisable = c.ifwString.contains(dataName)||!PackageUtil.isEnable(data.packageName,dataName,pm);
			if(!isDisable){
				enTemp.add(data);
			}else{
				disTemp.add(data);
			}
		}
		this.bjdatas.clear();
		this.bjdatas.addAll(disTemp);
		this.bjdatas.addAll(enTemp);
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
		ActivityInfo data = bjdatas.get(position);
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
		if(data.name.indexOf(".")>-1&&!c.isShowAllName){
			String s = data.name.substring(data.name.lastIndexOf(".")+1).trim();
			viewHolder.nameTv.setText(s.length()>1?s:data.name);
		}else{
			viewHolder.nameTv.setText(data.name);
		}
//		String runs = "";
//		if(data.name.indexOf(".")>-1){
//			runs=  data.name.substring(data.name.lastIndexOf(".")+1).trim();
//		}else{
//			runs=data.name;
//		}

		String dataName = data.name.trim();//.replaceAll("\\$","/\\$");
		boolean isDiable = c.ifwString.contains(dataName)||!PackageUtil.isEnable(data.packageName,dataName,pm);
		viewHolder.stopTv.setText(isDiable?"已禁用": (ContainsKeyWord.isContainsWord(dataName)?"建议禁用":"未禁用"));
		viewHolder.stopTv.setTextColor(isDiable? Color.RED:(ContainsKeyWord.isContainsWord(dataName)?Color.BLUE:IFWCompActivity.curColor));
		return convertView;
	}
	
	static class ViewHolder{
		public TextView nameTv,stopTv;
		public ProgressBar pb;
	}
}

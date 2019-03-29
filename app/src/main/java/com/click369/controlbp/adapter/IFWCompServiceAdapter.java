package com.click369.controlbp.adapter;

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
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PinyinCompareDisable;

import java.util.ArrayList;
import java.util.Collections;

public class IFWCompServiceAdapter extends BaseAdapter{
	public ArrayList<ServiceInfo> bjdatas = new ArrayList<ServiceInfo>();
	private LayoutInflater inflater;
	private IFWCompActivity c;
	private PackageManager pm;
//	private String ifw = "";
	public IFWCompServiceAdapter(IFWCompActivity context,PackageManager pm) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.pm = pm;
	}

	public void setData(ArrayList<ServiceInfo> serviceInfos){
//		if(datas==null||datas.length==0){
//			return;
//		}
		bjdatas.clear();
		for(ServiceInfo si1:serviceInfos){
			bjdatas.add(si1);
		}
		freshList();
	}

//	public void setIFW(String ifw){
//		this.ifw = ifw;
//		freshList();
//	}

	public void freshList(){
		ArrayList<ServiceInfo> disTemp = new ArrayList<ServiceInfo>();
		ArrayList<ServiceInfo> enTemp = new ArrayList<ServiceInfo>();
		PinyinCompareDisable comparent = new PinyinCompareDisable();
		Collections.sort(this.bjdatas, comparent);
		for(ServiceInfo data :bjdatas){
			String dataName = data.name;//.replaceAll("\\$","/\\$");
			boolean isDisable = c.ifwString.contains(dataName);//!PackageUtil.isEnable(data.packageName,dataName,pm)||
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
		ServiceInfo data = bjdatas.get(position);
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_ifwcomp, null);
			viewHolder = new ViewHolder();
			viewHolder.nameTv = (TextView)convertView.findViewById(R.id.item_ifw_name);
			viewHolder.stopTv = (TextView)convertView.findViewById(R.id.item_ifw_stop);
			viewHolder.pb = (ProgressBar) convertView.findViewById(R.id.item_ifw_pb);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		String s ="",runs="";
		if(data.name.indexOf(".")>-1&&!IFWFragment.isShowAllName){
			s = data.name.substring(data.name.lastIndexOf(".")+1).trim();
			viewHolder.nameTv.setText(s.length()>4?s:data.name);
		}else{
			s = data.name;
			viewHolder.nameTv.setText(data.name);
		}
		if(data.name.indexOf(".")>-1){
			runs=  data.name.substring(data.name.lastIndexOf(".")+1).trim();
		}else{
			runs=data.name;
		}
		String dataName = data.name;//.replaceAll("\\$","/\\$");
		boolean isDiable = c.ifwString.contains(dataName)||!PackageUtil.isEnable(data.packageName, dataName, pm);//!PackageUtil.isEnable(data.packageName,dataName,pm)||
		viewHolder.stopTv.setText(isDiable?"已禁用": (ContainsKeyWord.isContainsWord(dataName)?"建议禁用":"未禁用"));
//		viewHolder.stopTv.setTextColor(isDiable?Color.RED: isContains(runs)?Color.GREEN:IFWCompActivity.curColor);
		viewHolder.stopTv.setTextColor(isDiable?Color.RED:(ContainsKeyWord.isContainsWord(dataName)?Color.BLUE:IFWCompActivity.curColor));
		viewHolder.nameTv.setTextColor(isContains(runs)?Color.parseColor(MainActivity.COLOR_RUN):IFWCompActivity.curColor);
//		viewHolder.stopTv.setTextColor(isDiable? Color.RED:isContains(runs)? Color.GREEN:IFWCompActivity.curColor);
		return convertView;
	}

	private boolean isContains(String name){
		for(String s:IFWCompActivity.runServices){
			if(s.contains(name)){
				return true;
			}
		}
		return false;
	}
	
	static class ViewHolder{
		public TextView nameTv,stopTv;
		public ProgressBar pb;
	}
}

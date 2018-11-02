package com.click369.controlbp.adapter;

import android.app.Activity;
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
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PinyinCompareDisableReceiver;

import java.util.ArrayList;
import java.util.Collections;

public class DozeLogAdapter extends BaseAdapter{
	public ArrayList<String> bjdatas = new ArrayList<String>();
	private LayoutInflater inflater;
	public DozeLogAdapter(Activity context) {
		inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<String> datas){
		bjdatas.clear();
		bjdatas.addAll(datas);
		notifyDataSetChanged();
	}

	public void appendData(ArrayList<String> datas){
		bjdatas.addAll(datas);
		notifyDataSetChanged();
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
		String data = bjdatas.get(position);
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_dozelog, null);
			viewHolder = new ViewHolder();
			viewHolder.logTv = (TextView)convertView.findViewById(R.id.item_dozelog_tv);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.logTv.setText(data);
		return convertView;
	}
	
	static class ViewHolder{
		public TextView logTv;
	}
}

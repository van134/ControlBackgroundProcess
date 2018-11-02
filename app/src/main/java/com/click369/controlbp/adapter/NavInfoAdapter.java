package com.click369.controlbp.adapter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.bean.NavInfo;

import java.util.ArrayList;

public class NavInfoAdapter extends BaseAdapter{
	public ArrayList<NavInfo> bjdatas = new ArrayList<NavInfo>();
	private LayoutInflater inflater;
	private Activity act;
	public NavInfoAdapter(Activity context) {

		this.act = context;
		inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<NavInfo> datas){
		bjdatas.clear();
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

	
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		NavInfo data = bjdatas.get(position);
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_navinfo, null);
			viewHolder = new ViewHolder();
			viewHolder.titleTv = (TextView)convertView.findViewById(R.id.item_navinfo_title_tv);
			viewHolder.countTv = (TextView)convertView.findViewById(R.id.item_navinfo_content_tv);
			viewHolder.sw = (Switch) convertView.findViewById(R.id.item_navinfo_sw);
			viewHolder.fl = (FrameLayout) convertView.findViewById(R.id.item_navinfo_fl);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.fl.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				return true;
			}
		});
		viewHolder.titleTv.setText(data.titleName);
		if(data.type == 0){
			viewHolder.countTv.setVisibility(View.GONE);
			viewHolder.sw.setVisibility(View.VISIBLE);
			viewHolder.sw.setChecked(data.isOn);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//				int[][] states = new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}};
//				int[] colors = new int[]{act.getResources().getColor(R.color.uncheck_color), act.getResources().getColor(R.color.checked_color)};
//				ColorStateList csl = new ColorStateList(states, colors);
//				viewHolder.sw.setButtonTintList(csl);
//			}
		}else{
			viewHolder.countTv.setVisibility(View.VISIBLE);
			viewHolder.sw.setVisibility(View.GONE);
			viewHolder.countTv.setText(data.content);
		}
		return convertView;
	}
	
	static class ViewHolder{
		public TextView titleTv,countTv;
		public Switch sw;
		public FrameLayout fl;
	}
}

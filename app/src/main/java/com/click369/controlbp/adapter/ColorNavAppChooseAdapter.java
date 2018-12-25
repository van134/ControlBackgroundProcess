package com.click369.controlbp.adapter;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.click369.controlbp.R;
import com.click369.controlbp.activity.ColorNavBarActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.service.ColorNavBarService;
import com.click369.controlbp.util.PinyinCompare;

import java.util.ArrayList;
import java.util.Collections;

public class ColorNavAppChooseAdapter extends BaseAdapter {
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<AppInfo> rdatas = new ArrayList<AppInfo>();
	public ArrayList<String> choosedatas = new ArrayList<String>();

	private LayoutInflater inflater;
	public int chooseposition = -1;
	private ColorNavBarActivity c;
//	public ArrayList<StudentInfo> chooseInfo = new ArrayList<StudentInfo>();
	public ColorNavAppChooseAdapter(ColorNavBarActivity context) {
		c = context;
		inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<AppInfo> datas){
		bjdatas.clear();
		bjdatas.addAll(datas);
//		this.notifyDataSetChanged();
		freshList();
	}

//	public void setRunningData(ArrayList<AppInfo> rdatas){
//		this.rdatas.clear();
//		this.rdatas.addAll(rdatas);
//		freshList();
//	}

	public void fliterList(String name, ArrayList<AppInfo> apps){
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
					if(ai.getAppName().toLowerCase().contains(name.trim().toLowerCase())){
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

	public void freshList(){
		PinyinCompare comparent = new PinyinCompare();
		Collections.sort(this.bjdatas, comparent);
//		Collections.sort(this.choosedatas, comparent);
		ArrayList<AppInfo> temp = new ArrayList<AppInfo>();
		ArrayList<AppInfo> tempNoChoose = new ArrayList<AppInfo>();
		temp.addAll(this.bjdatas);
		this.bjdatas.clear();
		for(AppInfo ai:temp){
			if(ai.isDisable){
				this.bjdatas.add(ai);
			}else{
				tempNoChoose.add(ai);
			}
		}
		this.bjdatas.addAll(tempNoChoose);
		this.notifyDataSetChanged();
	}
	public void chooseAll(){
		choosedatas.clear();
		for(AppInfo ai:bjdatas){
			choosedatas.add(ai.getPackageName());
		}
		freshList();
	}
	public void cancelchooseAll(){
		choosedatas.clear();

		freshList();
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
			convertView= inflater.inflate(R.layout.item_colorbarchooseapp, null);
			viewHolder = new ViewHolder();
			viewHolder.titleTv = (TextView)convertView.findViewById(R.id.item_chooseapp_tv);
			viewHolder.run = (ImageView)convertView.findViewById(R.id.item_chooseapp_run);
			viewHolder.iv= (ImageView) convertView.findViewById(R.id.item_chooseapp_img);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
//		Util.isEnable()
		viewHolder.titleTv.setText(data.appName);
		viewHolder.run.setVisibility(View.VISIBLE);
		Glide.with( c ).load( Uri.fromFile(data.iconFile ) ).into(viewHolder.iv );
//		viewHolder.iv.setImageBitmap(data.getBitmap());
		viewHolder.run.setImageBitmap(null);
		if(ColorNavBarService.appColors.containsKey(data.getPackageName())&&ColorNavBarService.appColors.get(data.getPackageName())!=null){
			viewHolder.run.setBackgroundColor(Color.parseColor("#"+ColorNavBarService.appColors.get(data.getPackageName())));
		}else{
			viewHolder.run.setBackgroundColor(Color.parseColor("#ffffffff"));
		}
		return convertView;
	}
	
	static class ViewHolder{
		public TextView titleTv;
		public ImageView iv,run;
	}
}

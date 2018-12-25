package com.click369.controlbp.adapter;

import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.click369.controlbp.R;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.PinyinCompare;

import java.util.ArrayList;
import java.util.Collections;

public class IceRoomAdapter extends BaseAdapter {
	public ArrayList<AppInfo> bjdatas = new ArrayList<AppInfo>();
//	public ArrayList<AppInfo> rdatas = new ArrayList<AppInfo>();
	private LayoutInflater inflater;
	private Activity c;
	public String fliterName= "u";
	public IceRoomAdapter(Activity context) {
		c = context;
		inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<AppInfo> datas){
		bjdatas.clear();
		bjdatas.addAll(datas);
		freshList();
	}


	public void fliterList(String name, ArrayList<AppInfo> apps){
		this.fliterName = name;
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
		for(AppInfo ai :bjdatas){
			ai.isDisable = PackageUtil.isAppInIceRoom(c,ai.getPackageName());
		}
		freshList();
	}

	public void freshList(){
		PinyinCompare comparent = new PinyinCompare();
		Collections.sort(this.bjdatas, comparent);
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
			convertView= inflater.inflate(R.layout.item_iceroom, null);
			viewHolder = new ViewHolder();
			viewHolder.titleTv = (TextView)convertView.findViewById(R.id.item_chooseapp_tv);
			viewHolder.iv = (ImageView)convertView.findViewById(R.id.item_chooseapp_img);
			viewHolder.ice= (ImageView) convertView.findViewById(R.id.item_chooseice_img);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.titleTv.setText(data.appName);
		viewHolder.ice.setVisibility(data.isDisable? View.VISIBLE: View.INVISIBLE);
//		viewHolder.iv.setImageBitmap(data.getBitmap());
		Glide.with( c ).load( Uri.fromFile(data.iconFile ) ).into(viewHolder.iv );
		return convertView;
	}
	
	static class ViewHolder{
		public TextView titleTv;
		public ImageView iv,ice;
	}
}

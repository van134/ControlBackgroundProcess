package com.click369.controlbp.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.DozeWhiteListActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.SkipDialogActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.PinyinCompare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;

public class SkipDialogListAdapter extends BaseAdapter{
	public ArrayList<String> bjdatas = new ArrayList<String>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	private Context c;
	private SharedPreferences skipDialogPrefs;
	private int type = 0;
	public SkipDialogListAdapter(Context context, SharedPreferences skipDialogPrefs,int type) {
		c = context;
		this.type = type;
		inflater = LayoutInflater.from(context);
		this.skipDialogPrefs = skipDialogPrefs;
	}

	public void setData(ArrayList<String> datas){
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

	
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		String data = bjdatas.get(position);
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_skipdialog, null);
			viewHolder = new ViewHolder();
			viewHolder.nametv = (TextView)convertView.findViewById(R.id.item_skipdialog_name);
			viewHolder.disableIv = (ImageView) convertView.findViewById(R.id.item_skipdialog_iv);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.nametv.setText(data);
		viewHolder.disableIv.setTag(position);
		viewHolder.disableIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				BaseActivity.zhenDong(c);
				AlertUtil.showConfirmAlertMsg(c, "是否确定删除该规则？", new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						if(tag ==1){
							int tag1 = (Integer)(v.getTag());
							bjdatas.remove(tag1);
							skipDialogPrefs.edit().putStringSet(type==0?Common.PREFS_SKIPDIALOG_KEYWORDS:Common.PREFS_SKIPNOTIFY_KEYWORDS,new LinkedHashSet<String>(bjdatas)).commit();
							notifyDataSetChanged();
							SkipDialogActivity.isClick = true;
						}
					}
				});
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView nametv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView disableIv;
	}
}

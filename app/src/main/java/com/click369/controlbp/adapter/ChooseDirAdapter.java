package com.click369.controlbp.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.NewDirActivity;
import com.click369.controlbp.bean.DirBean;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;

public class ChooseDirAdapter extends BaseAdapter{
	private Handler handler = new Handler();
	public ArrayList<DirBean> bjdatas = new ArrayList<DirBean>();
	private LayoutInflater inflater;
	private Activity c;
	public ChooseDirAdapter(Activity context) {
		c = context;
		inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<DirBean> datas){
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

	
	public View getView(int position, View convertView, final ViewGroup parent) {
		ViewHolder viewHolder;
		DirBean data = bjdatas.get(position);
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_choosedir, null);
			viewHolder = new ViewHolder();
			viewHolder.nametv = (TextView)convertView.findViewById(R.id.item_name);
			viewHolder.addIv = (ImageView) convertView.findViewById(R.id.item_add_iv);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}

		viewHolder.nametv.setText(data.shortName);

		String ss = data.allName.replace(Environment.getExternalStorageDirectory().getAbsolutePath(),"");
		if(ss.startsWith(File.separator)){
			ss = ss.substring(1);
		}
		boolean contains = false;
		for(String key:NewDirListAdapter.bjdatas){
			if(key.startsWith(ss+"|")){
				contains = true;
				break;
			}
		}
		viewHolder.addIv.setTag(position+"|"+contains);
		viewHolder.addIv.setImageResource(contains?R.mipmap.icon_disable:R.mipmap.icon_add);
		viewHolder.addIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			BaseActivity.zhenDong(c);
			String ss = (String)(v.getTag());
			String sss[] = ss.split("\\|");
			final boolean iscontains = Boolean.parseBoolean(sss[1]);
			final int tag1 = Integer.parseInt(sss[0]);
			final DirBean content = bjdatas.get(tag1);
			String path = content.allName.replace(Environment.getExternalStorageDirectory().getAbsolutePath(),"");
			if(path.startsWith(File.separator)){
				path = path.substring(1);
			}
			Intent intent = new Intent("com.click369.newdir.send");
			intent.putExtra("index",c.getIntent().getIntExtra("index",-1));
			if(iscontains){
				intent.putExtra("remove",true);
			}else{
				intent.putExtra("add",true);
			}
			intent.putExtra("path",path);
			c.sendBroadcast(intent);
//			c.setResult(0x10,intent);
//			c.finish();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				},300);
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView nametv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView addIv;
	}
}

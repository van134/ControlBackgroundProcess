package com.click369.controlbp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
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
import com.click369.controlbp.activity.ChooseDirActivity;
import com.click369.controlbp.activity.NewDirActivity;
import com.click369.controlbp.activity.SkipDialogActivity;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.PinyinCompare;
import com.click369.controlbp.util.PinyinCompareDisable;
import com.click369.controlbp.util.PinyinStringCompare;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;

public class NewDirListAdapter extends BaseAdapter{
	public static ArrayList<String> bjdatas = new ArrayList<String>();
//	public ArrayList<String> choosedatas = new ArrayList<String>();
	private LayoutInflater inflater;
	private Activity c;
	private SharedPreferences priPrefs;
	public NewDirListAdapter(Activity context, SharedPreferences priPrefs) {
		c = context;
		inflater = LayoutInflater.from(context);
		this.priPrefs = priPrefs;
	}

	public void setData(ArrayList<String> datas){
		bjdatas.clear();
		Collections.sort(datas, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				Collator ca = Collator.getInstance(Locale.CHINA);
				int flags = 0;
				if (ca.compare( o1,o2) < 0) {
					flags = -1;
				}
				else if(ca.compare(o1, o2) > 0) {
					flags = 1;
				}
				else {
					flags = 0;
				}
				return flags;
			}
		});
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
			convertView= inflater.inflate(R.layout.item_newdir, null);
			viewHolder = new ViewHolder();
			viewHolder.nametv = (TextView)convertView.findViewById(R.id.item_newdir_old_name);
			viewHolder.newNameTv = (TextView)convertView.findViewById(R.id.item_newdir_new_name);
			viewHolder.disableIv = (ImageView) convertView.findViewById(R.id.item_del_iv);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		String names[] = data.split("\\|");
		viewHolder.nametv.setText(names[0]);
		viewHolder.newNameTv.setText(names[1]);
		viewHolder.newNameTv.setTag(position);
		viewHolder.nametv.setTag(position);
		viewHolder.disableIv.setTag(position);
		viewHolder.nametv.setEnabled(NewDirActivity.newDirAllSw);
		viewHolder.newNameTv.setEnabled(NewDirActivity.newDirAllSw);
		viewHolder.disableIv.setEnabled(NewDirActivity.newDirAllSw);
		viewHolder.nametv.setAlpha(NewDirActivity.newDirAllSw?1.0f:0.5f);
		viewHolder.newNameTv.setAlpha(NewDirActivity.newDirAllSw?1.0f:0.5f);
		viewHolder.disableIv.setAlpha(NewDirActivity.newDirAllSw?1.0f:0.5f);

		viewHolder.nametv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				final int tag1 = (Integer)(v.getTag());
				final String content = bjdatas.get(tag1);
				final String ss[] =  content.split("\\|");
				AlertUtil.showConfirmAlertMsg(c, "是否修改或重选"+ss[0]+"文件夹名称?", new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						if(tag==1){
							Intent intent = new Intent(c,ChooseDirActivity.class);
							intent.putExtra("index",tag1);
							c.startActivity(intent);
//							c.startActivityForResult(intent,0x10);
//							AlertUtil.inputTextAlert(c, 1, new AlertUtil.InputCallBack() {
//								@Override
//								public void backData(String txt, int tag) {
//									if(!TextUtils.isEmpty(txt)){
//										bjdatas.remove(tag1);
//										String newStr = txt+"|"+ss[1];
//										bjdatas.add(tag1,newStr);
//										priPrefs.edit().putStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>(bjdatas)).commit();
//										notifyDataSetChanged();
//									}else if(NewDirActivity.isContainsKeyWord(txt)){
//										Toast.makeText(c,"不能包含系统公共文件夹，请重新命名",Toast.LENGTH_LONG).show();
//									}else{
//										Toast.makeText(c,"名称不能为空",Toast.LENGTH_LONG).show();
//									}
//								}
//							});
						}
					}
				});
			}
		});
		viewHolder.newNameTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.zhenDong(c);
				final int tag1 = (Integer)(v.getTag());
				final String content = bjdatas.get(tag1);
				final String ss[] =  content.split("\\|");
				AlertUtil.showConfirmAlertMsg(c, "是否修改"+ss[1]+"文件夹名称?", new AlertUtil.InputCallBack() {
					@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
					@Override
					public void backData(String txt, int tag) {
						if(tag==1){
							AlertUtil.inputTextAlert(c, ss[1], new AlertUtil.InputCallBack() {
								@Override
								public void backData(String txt, int tag) {
									if(!TextUtils.isEmpty(txt)){
										bjdatas.remove(tag1);
										String newStr = ss[0]+"|"+txt;
										bjdatas.add(tag1,newStr);
										priPrefs.edit().putStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>(bjdatas)).commit();
										notifyDataSetChanged();
									}else{
										Toast.makeText(c,"名称不能为空",Toast.LENGTH_LONG).show();
									}
								}
							});
						}
					}
				});
			}
		});
		viewHolder.disableIv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				BaseActivity.zhenDong(c);
				AlertUtil.showConfirmAlertMsg(c, "是否确定删除该重定向内容？", new AlertUtil.InputCallBack() {
					@Override
					public void backData(String txt, int tag) {
						if(tag ==1){
							int tag1 = (Integer)(v.getTag());
							bjdatas.remove(tag1);
							priPrefs.edit().putStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>(bjdatas)).commit();
							notifyDataSetChanged();
						}
					}
				});
			}
		});
		return convertView;
	}
	
	static class ViewHolder{
		public TextView nametv,newNameTv;//,serviceTv,wakelockTv,alarmTv;
		public ImageView disableIv;
	}
}

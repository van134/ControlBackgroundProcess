package com.click369.controlbp.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.fragment.QuestionFragment;
import com.click369.controlbp.bean.Question;

import java.util.ArrayList;

public class QuestionAdapter extends BaseAdapter{
	public ArrayList<Question> bjdatas = new ArrayList<Question>();
	private LayoutInflater inflater;
	private Activity c;
	public QuestionAdapter(Activity context) {
		c = context;
		inflater = LayoutInflater.from(context);
	}

	public void setData(ArrayList<Question> datas){
		bjdatas.clear();
		bjdatas.addAll(datas);
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
		Question data = bjdatas.get(position);
		if(convertView == null){
			convertView= inflater.inflate(R.layout.item_question, null);
			viewHolder = new ViewHolder();
			viewHolder.nameTv = (TextView)convertView.findViewById(R.id.item_question_name);
			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder)convertView.getTag();
		}
		viewHolder.nameTv.setText(data.title);
		if (QuestionFragment.chooseIndex==position){
			viewHolder.nameTv.setText(data.title+"\n\n"+data.content);
		}
		return convertView;
	}
	
	static class ViewHolder{
		public TextView nameTv;
	}
}

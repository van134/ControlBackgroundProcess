package com.click369.controlbp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.DozeWhiteListAdapter;
import com.click369.controlbp.adapter.SkipDialogListAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.LightView;
import com.click369.controlbp.service.ScreenLightServiceUtil;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Created by asus on 2017/5/27.
 */
public class SkipDialogActivity extends BaseActivity {
    private ListView listView;
    private ImageView add;
    private TextView alert;
    private SkipDialogListAdapter adapter;
    private FrameLayout swFl;
    private Switch isShowToastSw;
//    private TopSearchView topView;
    private SharedPreferences skipDialogPrefs;
    public static  boolean isClick = false;
    public static int curColor = Color.BLACK;
    public int type = 0;//0  对话框 1通知
//    public ArrayList<String> lists = new ArrayList<String>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = getLayoutInflater().inflate(R.layout.activity_skipdialoglist,null);
        setContentView(v);
        type = this.getIntent().getIntExtra("type",0);
        skipDialogPrefs =sharedPrefs.skipDialogPrefs;// SharedPrefsUtil.getPreferences(this,Common.PREFS_SKIPDIALOG);

        swFl = (FrameLayout) this.findViewById(R.id.skip_dialog_fl);
        isShowToastSw = (Switch) this.findViewById(R.id.skip_dialog_sw);
        swFl.setVisibility(type==0?View.VISIBLE:View.GONE);
        isShowToastSw.setChecked(skipDialogPrefs.getBoolean(Common.PREFS_SKIPDIALOG_ISSHOWTOAST,true));

        isShowToastSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                skipDialogPrefs.edit().putBoolean(Common.PREFS_SKIPDIALOG_ISSHOWTOAST,isChecked).commit();
            }
        });
        alert = (TextView) this.findViewById(R.id.skipdialog_alert);

        isShowToastSw.setTextColor(alert.getCurrentTextColor());
        listView = (ListView)this.findViewById(R.id.skipdialog_listview);
        add = (ImageView) this.findViewById(R.id.skipdialog_add);
        add.setOnClickListener(new AddClickListener());
//        curColor = onTitle.getCurrentTextColor();
        adapter = new SkipDialogListAdapter(this,skipDialogPrefs,type);
        adapter.bjdatas.clear();
        adapter.bjdatas.addAll(skipDialogPrefs.getStringSet(type==0?Common.PREFS_SKIPDIALOG_KEYWORDS:Common.PREFS_SKIPNOTIFY_KEYWORDS,new LinkedHashSet<String>()));
        listView.setAdapter(adapter);
        setTitle(type==0?"对话框跳过":"通知屏蔽");
        String t = type == 0?"请添加要跳过的对话框中显示的关键文字(如果屏蔽指定应用的某对话框输入格式为 应用名称@关键字)，添加后重启对应的进程生效。(关键文字可以是对话框的标题、内容或按钮标题,三种中的一种，内容尽可能精确)":"请添加屏蔽的通知中显示的关键文字或要屏蔽的应用名称，添加后立即生效。(关键文字必须是通知内容的一部分，不能以标题为关键文字，内容尽可能精确";
        alert.setText(t);
//        ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_MSG,this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isClick){
            isClick = false;
            Intent intentb = new Intent("com.click369.control.ams.reloadskipnotify");
            intentb.putExtra("skipDialogPrefs", (Serializable) skipDialogPrefs.getAll());
            sendBroadcast(intentb);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }
    class AddClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            AlertUtil.inputTextAlert(SkipDialogActivity.this,2, new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if(!TextUtils.isEmpty(txt)){
                        if(txt.length()<2){
                            showT("内容长度不能小于两位");
                            return;
                        }
                        boolean isc = false;
                        for(String s:adapter.bjdatas){
                            if(s.contains(txt)){
                                isc = true;
                                break;
                            }
                        }
                        if(isc){
                            showT("该内容已经被包含");
                        }else{
                            SkipDialogActivity.isClick = true;
                            adapter.bjdatas.clear();
                            adapter.bjdatas.add(txt);
                            adapter.bjdatas.addAll(skipDialogPrefs.getStringSet(type==0?Common.PREFS_SKIPDIALOG_KEYWORDS:Common.PREFS_SKIPNOTIFY_KEYWORDS,new LinkedHashSet<String>()));
                            skipDialogPrefs.edit().putStringSet(type==0?Common.PREFS_SKIPDIALOG_KEYWORDS:Common.PREFS_SKIPNOTIFY_KEYWORDS,new LinkedHashSet<String>(adapter.bjdatas)).commit();
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

}

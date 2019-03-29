package com.click369.controlbp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.PrivacyControlAdapter;
import com.click369.controlbp.adapter.PrivacyLogAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.TimeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class PrivacyLogActivity extends BaseActivity {
    private ExpandableListView list;
    private PrivacyLogAdapter adapter;
    private TextView logTv,notDisableTv,showAlertTv,alertTv,changeModeTv;
    private FrameLayout alertFl;
    private ScrollView scview;
    private String appName,pkg;
//    private PrivacyControlAdapter adapter;
    public static int curColor = Color.BLACK;
    public SharedPreferences priPrefs;
    public int mode = 0;//0列表 1时间线
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacylog);
        priPrefs = sharedPrefs.privacyPrefs;//SharedPrefsUtil.getPreferences(this,Common.PREFS_APPIFWCOUNT);// getApplicationContext().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
        Intent intent= this.getIntent();
        appName = intent.getStringExtra("name");
        pkg = intent.getStringExtra("pkg");
        setTitle(appName+"的权限访问记录");
        list = (ExpandableListView) this.findViewById(R.id.prilog_listview);
        notDisableTv = (TextView)this.findViewById(R.id.ifwcomp_notdisableall);
        changeModeTv = (TextView)this.findViewById(R.id.prilog_changemode);
        logTv = (TextView)this.findViewById(R.id.prilog_alert_tv);
        showAlertTv = (TextView)this.findViewById(R.id.ifwcomp_showalert);
        alertTv = (TextView)this.findViewById(R.id.ifwcomp_alert_tv);
        alertFl = (FrameLayout) this.findViewById(R.id.ifwcomp_alert_fl);
        scview = (ScrollView) this.findViewById(R.id.prilog_scview);
        curColor = notDisableTv.getCurrentTextColor();
        alertFl.setVisibility(View.GONE);
        showAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
            }
        });
        changeModeTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode == 0){
                    mode = 1;
                    scview.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                }else{
                    mode = 0;
                    scview.setVisibility(View.GONE);
                    list.setVisibility(View.VISIBLE);
                }
                Intent intent1 = new Intent("com.click369.control.ams.getprivacyinfo");
                intent1.putExtra("pkg",pkg);
                sendBroadcast(intent1);
            }
        });
        notDisableTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent("com.click369.control.ams.clearprivacyinfo");
                intent1.putExtra("pkg",pkg);
                sendBroadcast(intent1);
                logTv.setText("暂无访问记录");
                adapter.clear();
            }
        });
        adapter = new PrivacyLogAdapter(this);
        if(mode == 0){
            scview.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }else{
            scview.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        }
        list.setAdapter(adapter);
        IntentFilter intentFilter = new IntentFilter("com.click369.control.recprivacyinfo");
        registerReceiver(broadcastReceiver,intentFilter);
        Intent intent1 = new Intent("com.click369.control.ams.getprivacyinfo");
        intent1.putExtra("pkg",pkg);
        sendBroadcast(intent1);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.click369.control.recprivacyinfo")){
                try {
                    HashMap<Long,String> minfos = (HashMap<Long,String>)intent.getSerializableExtra("infos");
                    Set<Long> time = minfos.keySet();
                    if(time.size()==0){
                        logTv.setText("暂无访问记录");
                        return;
                    }
                    TreeSet<Long> times = new TreeSet<Long>();
                    times.addAll(time);
                    StringBuilder stringBuilder = new StringBuilder();
                    LinkedHashMap<String,ArrayList<PrivacyLogAdapter.PriLog>> exDatas = new LinkedHashMap<String,ArrayList<PrivacyLogAdapter.PriLog>>();
                    for(Long t:times){
                        String value = minfos.get(t);
                        String ps[] = value.split("\\|");
                        int index = getIndexInArr(ps[0]);
                        if(index!=-1){
                            if(mode==0){
                                if(exDatas.containsKey(Common.PRIVACY_KEYS[index])){
                                    PrivacyLogAdapter.PriLog log = new PrivacyLogAdapter.PriLog();
                                    log.isPrevent = Boolean.parseBoolean(ps[1]);
                                    log.name = Common.PRIVACY_LOG_TITLES[index];
                                    log.time = TimeUtil.changeMils2String(t, "MM-dd HH:mm:ss");
                                    exDatas.get(Common.PRIVACY_KEYS[index]).add(0,log);
                                }else {
                                    ArrayList<PrivacyLogAdapter.PriLog> logs = new ArrayList<PrivacyLogAdapter.PriLog>();
                                    PrivacyLogAdapter.PriLog log = new PrivacyLogAdapter.PriLog();
                                    log.isPrevent = Boolean.parseBoolean(ps[1]);
                                    log.name = Common.PRIVACY_LOG_TITLES[index];
                                    log.time = TimeUtil.changeMils2String(t, "MM-dd HH:mm:ss");
                                    logs.add(log);
                                    exDatas.put(Common.PRIVACY_KEYS[index],logs);
                                }

                            }else {
                                stringBuilder.append(TimeUtil.changeMils2String(t, "MM-dd HH:mm:ss"))
                                        .append(" 访问 ")
                                        .append(Common.PRIVACY_TITLES[index])
                                        .append(Boolean.parseBoolean(ps[1]) ? " 被阻止" : " 已允许")
                                        .append("\n\n");
                            }
                        }
                    }
                    if(mode==0){
                        adapter.setData(exDatas);
                    }else{
                        logTv.setText(stringBuilder.toString());
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                scview.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    };

    private int getIndexInArr(String s){
        for(int i = 0;i<Common.PRIVACY_KEYS.length;i++){
            if(Common.PRIVACY_KEYS[i].equals(s)){
                return i;
            }
        }
        return -1;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}

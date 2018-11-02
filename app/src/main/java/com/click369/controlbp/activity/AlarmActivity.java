package com.click369.controlbp.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.AlarmAdapter;
import com.click369.controlbp.adapter.WakeLockAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AlarmActivity extends BaseActivity {
    private Switch alarmSw;
    private TextView alertTv,resetTv,cleanRoleTv,cleanSetTv,showAlertTv;
    private FrameLayout alertFl;
    private ExpandableListView listView;
    private AlarmAdapter adapter;
    private SharedPreferences alarmPrefs;
    private int curColor = Color.BLACK;
    private MyWakeLockReceiver receiver;
    int count = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_alarm);
        initView();
        setTitle("增强定时器");
    }

    private void initView() {
        alarmPrefs = SharedPrefsUtil.getPreferences(this, Common.PREFS_ALARMNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        listView = (ExpandableListView) findViewById(R.id.alarm_log_listview);
        alarmSw = (Switch) findViewById(R.id.alarm_sw);
        alertFl = (FrameLayout) findViewById(R.id.wakelock_alert_fl);
        alertFl.setVisibility(View.GONE);
        alertTv = (TextView) findViewById(R.id.alarm_alert_tv);
        resetTv = (TextView) findViewById(R.id.alarm_reset_tv);
        cleanRoleTv = (TextView) findViewById(R.id.alarm_cleanrole_tv);
        cleanSetTv = (TextView) findViewById(R.id.alarm_cleanset_tv);
        showAlertTv = (TextView) findViewById(R.id.alarm_showalert_tv);
        curColor = alertTv.getCurrentTextColor();
        adapter = new AlarmAdapter(this,alarmPrefs);
        listView.setAdapter(adapter);
        alarmSw.setTextColor(curColor);
        alarmSw.setChecked(alarmPrefs.getBoolean(Common.PREFS_SETTING_ALARM_LOOK, false));
        alarmSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                alarmPrefs.edit().putBoolean(Common.PREFS_SETTING_ALARM_LOOK, isChecked).commit();
                adapter.clear();
                if (isChecked) {
                    sendAndRead();
                }
            }
        });
        showAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
            }
        });
        resetTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertUtil.showConfirmAlertMsg(AlarmActivity.this, "是否清除所有定时器信息？(已设置信息保留)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if (tag ==1){
                            adapter.clear();
                            FileUtil.writeObj(adapter.alarmAllowCounts,getFilesDir()+ File.separator+"alarmAllowCounts");
                            FileUtil.writeObj(adapter.alarmNotAllowCounts,getFilesDir()+ File.separator+"alarmNotAllowCounts");
                            FileUtil.writeObj(adapter.alarmAllTimes,getFilesDir()+ File.separator+"alarmAllTimes");
                            FileUtil.writeObj(adapter.alarmNotAllowAllTimes,getFilesDir()+ File.separator+"alarmNotAllowAllTimes");
                            FileUtil.writeObj(adapter.alarms,getFilesDir()+ File.separator+"alarms");
                            Intent intent = new Intent("com.click369.alarm.clearinfo");
                            sendBroadcast(intent);
                            adapter.reload();
                        }
                    }
                });
            }
        });
        cleanRoleTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            AlertUtil.showConfirmAlertMsg(AlarmActivity.this, "是否清除所有已设置的自定义规则？", new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                if (tag ==1){
                    SharedPreferences.Editor ed = alarmPrefs.edit();
                    Set<String> keys = new HashSet<String>(alarmPrefs.getAll().keySet());
                    for(String key:keys){
                        if(key.endsWith("/startname")){
                            ed.remove(key);
                        }else if(key.endsWith("/starttime")){
                            ed.remove(key);
                        }
                    }
                    ed.commit();
                    adapter.reload();
                }
                }
            });
            }
        });
        cleanSetTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertUtil.showConfirmAlertMsg(AlarmActivity.this, "是否清除所有定时器设置？", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                    if (tag ==1){
                        SharedPreferences.Editor ed = alarmPrefs.edit();
                        ed.clear();
                        ed.putBoolean(Common.PREFS_SETTING_ALARM_LOOK, alarmSw.isChecked());
                        ed.commit();
                        adapter.reload();
                    }
                    }
                });
            }
        });
        receiver = new MyWakeLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.click369.alarm.getinfo");
        this.registerReceiver(receiver,intentFilter);
        sendAndRead();
    }

    private void sendAndRead(){
        new Thread(){
            @Override
            public void run() {
                Object o1 = FileUtil.readObj(getFilesDir()+ File.separator+"alarmAllTimes");
                Object o7 = FileUtil.readObj(getFilesDir()+ File.separator+"alarmNotAllowAllTimes");
                Object o2 = FileUtil.readObj(getFilesDir()+ File.separator+"alarmAllowCounts");
                Object o3 = FileUtil.readObj(getFilesDir()+ File.separator+"alarmNotAllowCounts");
                Object o5 = FileUtil.readObj(getFilesDir()+ File.separator+"alarms");
                if (o1!=null&&o1 instanceof HashMap){
                    adapter.alarmAllTimes.putAll((HashMap<String,ArrayList<Long[]>>)o1);
                }
                if (o7!=null&&o7 instanceof HashMap){
                    adapter.alarmNotAllowAllTimes.putAll((HashMap<String,ArrayList<Long[]>>)o7);
                }
                if (o2!=null&&o2 instanceof HashMap){
                    adapter.alarmAllowCounts.putAll((HashMap<String,Integer>)o2);
                }
                if (o3!=null&&o3 instanceof HashMap){
                    adapter.alarmNotAllowCounts.putAll((HashMap<String,Integer>)o3);
                }
                if (o5!=null&&o5 instanceof HashMap){
                    adapter.alarms.putAll((HashMap<String,ArrayList<String>>)o5);
                }
                if (alarmSw.isChecked()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle("增强定时器，正在加载...");
                            Intent intent = new Intent("com.click369.alarm.giveinfo");
                            sendBroadcast(intent);
                        }
                    });
                    h.postDelayed(showInfo,5000);
                }
            }
        }.start();

//        ActivityManager am = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE);
//        try {
//            Field f= am.getClass().getDeclaredField("IActivityManagerSingleton");
//            Log.i("CONTROL",f.getName());
////            Field field = am.getClass().getDeclaredField("IActivityManagerSingleton");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    Runnable showInfo = new Runnable() {
        @Override
        public void run() {
            if (count==0){
                setTitle("增强定时器，没有数据"+count);
                showT("未读取到定时器信息，确保XP模块生效或重启手机再试。");
            }
        }
    };
    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacks(showInfo);
        count = 0;
        this.unregisterReceiver(receiver);
    }

    class MyWakeLockReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if ("com.click369.alarm.getinfo".equals(action)) {
                    if (intent.hasExtra("alarms")) {
                        final HashMap<String, ArrayList<String>> alarms = intent.hasExtra("alarms")?(HashMap<String, ArrayList<String>>) intent.getSerializableExtra("alarms"):null;

                        final HashMap<String, Integer> alarmAllowCounts = intent.hasExtra("alarmAllowCounts")?(HashMap<String, Integer>) intent.getSerializableExtra("alarmAllowCounts"):null;
                        final HashMap<String, Integer> alarmNotAllowCounts = intent.hasExtra("alarmNotAllowCounts")?(HashMap<String, Integer>) intent.getSerializableExtra("alarmNotAllowCounts"):null;

                        final HashMap<String, ArrayList<Long[]>> alarmAllTimes = intent.hasExtra("alarmAllTimes")?(HashMap<String, ArrayList<Long[]>>) intent.getSerializableExtra("alarmAllTimes"):null;
                        final HashMap<String, ArrayList<Long[]>> alarmNotAllowAllTimes = intent.hasExtra("alarmNotAllowAllTimes")?(HashMap<String, ArrayList<Long[]>>) intent.getSerializableExtra("alarmNotAllowAllTimes"):null;
//                        Log.i("CONTROL","alarmAllTimes  "+alarmAllTimes.size());
                        adapter.appendData(alarms,alarmAllowCounts, alarmNotAllowCounts,alarmAllTimes,alarmNotAllowAllTimes);
                        h.removeCallbacks(r);
                        h.postDelayed(r, 500);
                    }
                    count++;
                    setTitle("增强定时器，正在加载"+count);
                }
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }
    Runnable r = new Runnable() {
        @Override
        public void run() {
            adapter.reload();
            setTitle("增强定时器");
            new Thread(){
                @Override
                public void run() {
                    FileUtil.writeObj(adapter.alarmAllowCounts,getFilesDir()+ File.separator+"alarmAllowCounts");
                    FileUtil.writeObj(adapter.alarmNotAllowCounts,getFilesDir()+ File.separator+"alarmNotAllowCounts");
                    FileUtil.writeObj(adapter.alarmAllTimes,getFilesDir()+ File.separator+"alarmAllTimes");
                    FileUtil.writeObj(adapter.alarmNotAllowAllTimes,getFilesDir()+ File.separator+"alarmNotAllowAllTimes");
                    FileUtil.writeObj(adapter.alarms,getFilesDir()+ File.separator+"alarms");
                }
            }.start();
        }
    };
}

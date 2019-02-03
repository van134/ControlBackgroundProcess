package com.click369.controlbp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.WakeLockAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.githang.statusbar.StatusBarCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class WakeLockActivity extends BaseActivity {
    private Switch wakelockSw;
    private TextView alertTv,resetTv,cleanRoleTv,cleanSetTv,showAlertTv;
    private FrameLayout alertFl;
    private ExpandableListView listView;
    private WakeLockAdapter adapter;
    private SharedPreferences wakeLockPrefs;
    private int curColor = Color.BLACK;
    private MyWakeLockReceiver receiver;
    int count = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_wakelock);
        initView();
        setTitle("增强唤醒锁");
    }

    private void initView() {
        wakeLockPrefs = sharedPrefs.wakeLockPrefs;//SharedPrefsUtil.getPreferences(this, Common.PREFS_WAKELOCKNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        listView = (ExpandableListView) findViewById(R.id.wakelock_log_listview);
        wakelockSw = (Switch) findViewById(R.id.wakelock_sw);
        alertFl = (FrameLayout) findViewById(R.id.wakelock_alert_fl);
        alertFl.setVisibility(View.GONE);
        alertTv = (TextView) findViewById(R.id.wakelock_alert_tv);
        resetTv = (TextView) findViewById(R.id.wakelock_reset_tv);
        cleanRoleTv = (TextView) findViewById(R.id.wakelock_cleanrole_tv);
        cleanSetTv = (TextView) findViewById(R.id.wakelock_cleanset_tv);
        showAlertTv = (TextView) findViewById(R.id.wakelock_showalert_tv);
        curColor = alertTv.getCurrentTextColor();
        adapter = new WakeLockAdapter(this,wakeLockPrefs);
        listView.setAdapter(adapter);
        wakelockSw.setTextColor(curColor);
        wakelockSw.setChecked(wakeLockPrefs.getBoolean(Common.PREFS_SETTING_WAKELOCK_LOOK, false));
        wakelockSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wakeLockPrefs.edit().putBoolean(Common.PREFS_SETTING_WAKELOCK_LOOK, isChecked).commit();
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
                AlertUtil.showConfirmAlertMsg(WakeLockActivity.this, "是否清除所有唤醒锁信息？(已设置信息保留)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if (tag ==1){
                            adapter.clear();
                            FileUtil.writeObj(adapter.wakeLockAllowCounts,getFilesDir()+ File.separator+"wakeLockAllowCounts");
                            FileUtil.writeObj(adapter.wakeLockNotAllowCounts,getFilesDir()+ File.separator+"wakeLockNotAllowCounts");
                            FileUtil.writeObj(adapter.wakeLockAllowTimes,getFilesDir()+ File.separator+"wakeLockAllowTimes");
                            FileUtil.writeObj(adapter.wakeLockNotAllowTimes,getFilesDir()+ File.separator+"wakeLockNotAllowTimes");
                            FileUtil.writeObj(adapter.wakeLockAllTimes,getFilesDir()+ File.separator+"wakeLockAllTimes");
                            FileUtil.writeObj(adapter.wakeLockNotAllowAllTimes,getFilesDir()+ File.separator+"wakeLockNotAllowAllTimes");
                            FileUtil.writeObj(adapter.wakeLocks,getFilesDir()+ File.separator+"wakeLocks");
                            Intent intent = new Intent("com.click369.wakelock.clearinfo");
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
            AlertUtil.showConfirmAlertMsg(WakeLockActivity.this, "是否清除所有已设置的自定义规则？", new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                if (tag ==1){
                    SharedPreferences.Editor ed = wakeLockPrefs.edit();
                    Set<String> keys = new HashSet<String>(wakeLockPrefs.getAll().keySet());
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
                AlertUtil.showConfirmAlertMsg(WakeLockActivity.this, "是否清除所有唤醒锁设置？", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                    if (tag ==1){
                        SharedPreferences.Editor ed = wakeLockPrefs.edit();
                        ed.clear();
                        ed.putBoolean(Common.PREFS_SETTING_WAKELOCK_LOOK, wakelockSw.isChecked());
                        ed.commit();
                        adapter.reload();
                    }
                    }
                });
            }
        });
        receiver = new MyWakeLockReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.click369.wakelock.getinfo");
        this.registerReceiver(receiver,intentFilter);
        sendAndRead();
    }

    private void sendAndRead(){
        new Thread(){
            @Override
            public void run() {
                Object o1 = FileUtil.readObj(getFilesDir()+ File.separator+"wakeLockAllTimes");
                Object o7 = FileUtil.readObj(getFilesDir()+ File.separator+"wakeLockNotAllowAllTimes");
                Object o2 = FileUtil.readObj(getFilesDir()+ File.separator+"wakeLockAllowCounts");
                Object o3 = FileUtil.readObj(getFilesDir()+ File.separator+"wakeLockNotAllowCounts");
                Object o4 = FileUtil.readObj(getFilesDir()+ File.separator+"wakeLockAllowTimes");
                Object o6 = FileUtil.readObj(getFilesDir()+ File.separator+"wakeLockNotAllowTimes");
                Object o5 = FileUtil.readObj(getFilesDir()+ File.separator+"wakeLocks");
                if (o1!=null&&o1 instanceof HashMap){
                    adapter.wakeLockAllTimes.putAll((HashMap<String,ArrayList<Long>>)o1);
                }
                if (o7!=null&&o7 instanceof HashMap){
                    adapter.wakeLockNotAllowAllTimes.putAll((HashMap<String,ArrayList<Long>>)o7);
                }
                if (o2!=null&&o2 instanceof HashMap){
                    adapter.wakeLockAllowCounts.putAll((HashMap<String,Integer>)o2);
                }
                if (o3!=null&&o3 instanceof HashMap){
                    adapter.wakeLockNotAllowCounts.putAll((HashMap<String,Integer>)o3);
                }
                if (o4!=null&&o4 instanceof HashMap){
                    adapter.wakeLockAllowTimes.putAll((HashMap<String,Long>)o4);
                }
                if (o6!=null&&o6 instanceof HashMap){
                    adapter.wakeLockNotAllowTimes.putAll((HashMap<String,Long>)o6);
                }
                if (o5!=null&&o5 instanceof HashMap){
                    adapter.wakeLocks.putAll((HashMap<String,ArrayList<String>>)o5);
                }
                if (wakelockSw.isChecked()){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTitle("增强唤醒锁，正在加载...");
                            Intent intent = new Intent("com.click369.wakelock.giveinfo");
                            sendBroadcast(intent);
                        }
                    });
                    h.postDelayed(showInfo,5000);
                }
            }
        }.start();
    }

    Runnable showInfo = new Runnable() {
        @Override
        public void run() {
            if (count==0){
                setTitle("增强唤醒锁，没有数据");
                showT("未读取到唤醒锁信息，确保XP模块生效或重启手机再试。");
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
                if ("com.click369.wakelock.getinfo".equals(action)) {
                    if (intent.hasExtra("wakeLocks")) {
                        final HashMap<String, ArrayList<String>> wakeLocks = intent.hasExtra("wakeLocks")?(HashMap<String, ArrayList<String>>) intent.getSerializableExtra("wakeLocks"):null;

                        final HashMap<String, Integer> wakeLockAllowCounts = intent.hasExtra("wakeLockAllowCounts")?(HashMap<String, Integer>) intent.getSerializableExtra("wakeLockAllowCounts"):null;
                        final HashMap<String, Integer> wakeLockNotAllowCounts = intent.hasExtra("wakeLockNotAllowCounts")?(HashMap<String, Integer>) intent.getSerializableExtra("wakeLockNotAllowCounts"):null;

                        final HashMap<String, Long> wakeLockAllowTimes = intent.hasExtra("wakeLockAllowTimes")?(HashMap<String, Long>) intent.getSerializableExtra("wakeLockAllowTimes"):null;
                        final HashMap<String, Long> wakeLockNotAllowTimes = intent.hasExtra("wakeLockNotAllowTimes")?(HashMap<String, Long>) intent.getSerializableExtra("wakeLockNotAllowTimes"):null;

                        final HashMap<String, ArrayList<Long>> wakeLockAllTimes = intent.hasExtra("wakeLockAllTimes")?(HashMap<String, ArrayList<Long>>) intent.getSerializableExtra("wakeLockAllTimes"):null;
                        final HashMap<String, ArrayList<Long>> wakeLockNotAllowAllTimes = intent.hasExtra("wakeLockNotAllowAllTimes")?(HashMap<String, ArrayList<Long>>) intent.getSerializableExtra("wakeLockNotAllowAllTimes"):null;

                        adapter.appendData(wakeLocks,wakeLockAllowCounts, wakeLockNotAllowCounts, wakeLockAllowTimes,wakeLockNotAllowTimes,wakeLockAllTimes,wakeLockNotAllowAllTimes);
                        h.removeCallbacks(r);
                        h.postDelayed(r, 500);
                    }
                    count++;
                    setTitle("增强唤醒锁，正在加载"+count);
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
            setTitle("增强唤醒锁");
            new Thread(){
                @Override
                public void run() {
                    FileUtil.writeObj(adapter.wakeLockAllowCounts,getFilesDir()+ File.separator+"wakeLockAllowCounts");
                    FileUtil.writeObj(adapter.wakeLockNotAllowCounts,getFilesDir()+ File.separator+"wakeLockNotAllowCounts");
                    FileUtil.writeObj(adapter.wakeLockAllowTimes,getFilesDir()+ File.separator+"wakeLockAllowTimes");
                    FileUtil.writeObj(adapter.wakeLockNotAllowTimes,getFilesDir()+ File.separator+"wakeLockNotAllowTimes");
                    FileUtil.writeObj(adapter.wakeLockAllTimes,getFilesDir()+ File.separator+"wakeLockAllTimes");
                    FileUtil.writeObj(adapter.wakeLockNotAllowAllTimes,getFilesDir()+ File.separator+"wakeLockNotAllowAllTimes");
                    FileUtil.writeObj(adapter.wakeLocks,getFilesDir()+ File.separator+"wakeLocks");
                }
            }.start();
        }
    };

    public static void delTempFiles(Context c){
        try {
            File file1 = new File(c.getFilesDir()+ File.separator+"wakeLockAllowCounts");
            File file2 = new File(c.getFilesDir()+ File.separator+"wakeLockNotAllowCounts");
            File file3 = new File(c.getFilesDir()+ File.separator+"wakeLockAllowTimes");
            File file4 = new File(c.getFilesDir()+ File.separator+"wakeLockNotAllowTimes");
            File file5 = new File(c.getFilesDir()+ File.separator+"wakeLockAllTimes");
            File file6 = new File(c.getFilesDir()+ File.separator+"wakeLockNotAllowAllTimes");
            File file7 = new File(c.getFilesDir()+ File.separator+"wakeLocks");
            file1.delete();
            file2.delete();
            file3.delete();
            file4.delete();
            file5.delete();
            file6.delete();
            file7.delete();
        }catch (Exception e){

        }
    }
}

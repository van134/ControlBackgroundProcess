package com.click369.controlbp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.WakeLockAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CPUSetActivity extends BaseActivity {
    private Switch cpulockSw,cameraSw,autoStartSw,cpu0,cpu1,cpu2,cpu3,cpu4,cpu5,cpu6,cpu7;
    private SharedPreferences cpuPrefs;
    private int curColor = Color.BLACK;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_cpu);
        initView();
        setTitle("CPU设置");
    }

    private void initView() {
        cpuPrefs = SharedPrefsUtil.getPreferences(this, Common.PREFS_SETCPU);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        TextView topTitle = (TextView) findViewById(R.id.cpu_top_title);
        FrameLayout fls[] = {(FrameLayout) findViewById(R.id.cpu_fl0),(FrameLayout) findViewById(R.id.cpu_fl1),(FrameLayout) findViewById(R.id.cpu_fl2)
        ,(FrameLayout) findViewById(R.id.cpu_fl3),(FrameLayout) findViewById(R.id.cpu_fl4),(FrameLayout) findViewById(R.id.cpu_fl5),(FrameLayout) findViewById(R.id.cpu_fl6),
                (FrameLayout) findViewById(R.id.cpu_fl7)};
        cpu0 = (Switch) findViewById(R.id.cpu_sw0);
        cpu0.setEnabled(false);
        cpu1 = (Switch) findViewById(R.id.cpu_sw1);
        cpu2 = (Switch) findViewById(R.id.cpu_sw2);
        cpu3 = (Switch) findViewById(R.id.cpu_sw3);
        cpu4 = (Switch) findViewById(R.id.cpu_sw4);
        cpu5 = (Switch) findViewById(R.id.cpu_sw5);
        cpu6 = (Switch) findViewById(R.id.cpu_sw6);
        cpu7 = (Switch) findViewById(R.id.cpu_sw7);
        cameraSw = (Switch) findViewById(R.id.cpu_camera_sw);
        cpulockSw = (Switch) findViewById(R.id.cpu_lockunlock_sw);
        autoStartSw = (Switch) findViewById(R.id.cpu_autostart_sw);
        curColor = topTitle.getCurrentTextColor();
        cpulockSw.setTextColor(curColor);
        autoStartSw.setTextColor(curColor);
        cameraSw.setTextColor(curColor);
        cpu0.setTextColor(curColor);
        cpu1.setTextColor(curColor);
        cpu2.setTextColor(curColor);
        cpu3.setTextColor(curColor);
        cpu4.setTextColor(curColor);
        cpu5.setTextColor(curColor);
        cpu6.setTextColor(curColor);
        cpu7.setTextColor(curColor);
        cpulockSw.setTag(8);
        autoStartSw.setTag(9);
        cameraSw.setTag(10);
        cpu0.setTag(0);
        cpu1.setTag(1);
        cpu2.setTag(2);
        cpu3.setTag(3);
        cpu4.setTag(4);
        cpu5.setTag(5);
        cpu6.setTag(6);
        cpu7.setTag(7);
        cpulockSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_LOCKUNLOCK, false));
        autoStartSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_AUTOSTART, false));
        cameraSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CAMERAMODE, false));
        cpu0.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU0, true));
        cpu1.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU1, true));
        cpu2.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU2, true));
        cpu3.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU3, true));
        cpu4.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU4, true));
        cpu5.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU5, true));
        cpu6.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU6, true));
        cpu7.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CPU7, true));
        ChangeListener changeListener = new ChangeListener();
        cpulockSw.setOnCheckedChangeListener(changeListener);
        autoStartSw.setOnCheckedChangeListener(changeListener);
        cameraSw.setOnCheckedChangeListener(changeListener);
        cpu0.setOnCheckedChangeListener(changeListener);
        cpu1.setOnCheckedChangeListener(changeListener);
        cpu2.setOnCheckedChangeListener(changeListener);
        cpu3.setOnCheckedChangeListener(changeListener);
        cpu4.setOnCheckedChangeListener(changeListener);
        cpu5.setOnCheckedChangeListener(changeListener);
        cpu6.setOnCheckedChangeListener(changeListener);
        cpu7.setOnCheckedChangeListener(changeListener);
        int coreNum =  getNumberOfCPUCores();
        for(int i = 8;i>coreNum;i--){
            fls[i-1].setVisibility(View.GONE);
        }
//        Log.i("CONTROL","cpunum  "+coreNum);
        cpuPrefs.edit().putInt(Common.PREFS_SETCPU_NUMBER,coreNum).commit();
        new Thread(){
            @Override
            public void run() {
                try {
                    boolean isLockUnlockCPU = cpuPrefs.getBoolean(Common.PREFS_SETCPU_LOCKUNLOCK, false);
                    File file1 = new File(FileUtil.FILEPATH, "unlock_lowbatter_core");
                    File file2 = new File(FileUtil.FILEPATH, "lock_lowbatter_core");
                    if (!file1.exists() || !file2.exists()) {
                        FileUtil.init();
                        FileUtil.copyAssets(CPUSetActivity.this, "unlock_lowbatter_core", FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core");
                        FileUtil.copyAssets(CPUSetActivity.this, "lock_lowbatter_core", FileUtil.FILEPATH + File.separator + "lock_lowbatter_core");
                    }
                    String s = isLockUnlockCPU ? "sh " + FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core" : "sh " + FileUtil.FILEPATH + File.separator + "lock_lowbatter_core";
                    ShellUtilNoBackData.execCommand(s);
                    String names[] = {Common.PREFS_SETCPU_CPU0, Common.PREFS_SETCPU_CPU1, Common.PREFS_SETCPU_CPU2, Common.PREFS_SETCPU_CPU3,
                            Common.PREFS_SETCPU_CPU4, Common.PREFS_SETCPU_CPU5, Common.PREFS_SETCPU_CPU6, Common.PREFS_SETCPU_CPU7};
                    for (int i = 0; i < names.length; i++) {
                        Thread.sleep(600);
                        boolean isCPU = cpuPrefs.getBoolean(names[i], true);
                        ShellUtilNoBackData.execCommand("echo -n " + (isCPU ? "1" : "0") + " > /sys/devices/system/cpu/cpu" + i + "/online");
                    }
                }catch (Exception e){
                }
            }
        }.start();
    }

    class  ChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int tag = (Integer) buttonView.getTag();
            String names[] = {Common.PREFS_SETCPU_CPU0,Common.PREFS_SETCPU_CPU1,Common.PREFS_SETCPU_CPU2,Common.PREFS_SETCPU_CPU3,
                    Common.PREFS_SETCPU_CPU4,Common.PREFS_SETCPU_CPU5,Common.PREFS_SETCPU_CPU6,Common.PREFS_SETCPU_CPU7,
                    Common.PREFS_SETCPU_LOCKUNLOCK,Common.PREFS_SETCPU_AUTOSTART,Common.PREFS_SETCPU_CAMERAMODE};
            cpuPrefs.edit().putBoolean(names[tag],isChecked).commit();
            if(tag == 8){
                //sh system/etc/init.d/lock_lowbatter_core
                //sh system/etc/init.d/unlock_lowbatter_core
                File file1 = new File(FileUtil.FILEPATH,"unlock_lowbatter_core");
                File file2 = new File(FileUtil.FILEPATH,"lock_lowbatter_core");
                if(!file1.exists()||!file2.exists()){
                    FileUtil.init();
                    FileUtil.copyAssets(CPUSetActivity.this,"unlock_lowbatter_core",FileUtil.FILEPATH+File.separator+"unlock_lowbatter_core");
                    FileUtil.copyAssets(CPUSetActivity.this,"lock_lowbatter_core",FileUtil.FILEPATH+File.separator+"lock_lowbatter_core");
                }
                String s = isChecked?"sh "+FileUtil.FILEPATH+File.separator+"unlock_lowbatter_core":"sh "+FileUtil.FILEPATH+File.separator+"lock_lowbatter_core";
//                String s = isChecked?"sh system/etc/init.d/unlock_lowbatter_core":"sh system/etc/init.d/lock_lowbatter_core";
                ShellUtilNoBackData.execCommand(s);
            }else if(tag<8){
                String s = "echo -n "+(isChecked?"1":"0")+" > /sys/devices/system/cpu/cpu"+tag+"/online";
                ShellUtilNoBackData.execCommand(s);
                WatchDogService.isCPUS[tag] = isChecked;
            }else if(tag == 10){
                WatchDogService.isCameraMode = isChecked;
            }
        }
    }


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
    }


    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            // Gingerbread doesn't support giving a single application access to both cores, but a
            // handful of devices (Atrix 4G and Droid X2 for example) were released with a dual-core
            // chipset and Gingerbread; that can let an app in the background run without impacting
            // the foreground application. But for our purposes, it makes them single core.
            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = 0;
        } catch (NullPointerException e) {
            cores = 0;
        }
        return cores;
    }
    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            //regex is slow, so checking char by char.
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };
}

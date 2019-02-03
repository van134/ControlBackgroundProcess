package com.click369.controlbp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.CpuUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShellUtils;

import java.io.File;
import java.io.FileFilter;

public class CPUSetView{
    private Switch cpulockSw,cameraSw,autoStartSw,offScSw,batterySw,cpuCharingSw;//,cpu0,cpu1,cpu2,cpu3,cpu4,cpu5,cpu6,cpu7;
    private SeekBar delaySb,batterySb;
    private SharedPreferences cpuPrefs;
    private int curColor = Color.BLACK;
    private FrameLayout cpuOffScFl,cpuDefaultFl,cpuOffDelayFl,cpuBatteryFl,cpuBatteryChangeFl;
    private int coreNum = 8;
    private Context context;
    public View init(Context context) {
        this.context = context;
        View v = LayoutInflater.from(context).inflate(R.layout.fragment_cpu, null);
        initView(v);
        return v;
    }
    private void initView(View v) {
        cpuPrefs =  SharedPrefsUtil.getInstance(context).cpuPrefs;//SharedPrefsUtil.getPreferences(context, Common.PREFS_SETCPU);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        if (!WatchDogService.isRoot){
            WatchDogService.isRoot = ShellUtils.checkRootPermission();
        }
        if(!WatchDogService.isRoot){
            cpuPrefs.edit().putBoolean(Common.PREFS_SETCPU_LOCKUNLOCK, false);
            WatchDogService.isLockUnlockCPU = false;
            AlertUtil.showAlertMsgBack(context, "提示", "检测到没有root权限，无法使用CPU相关设置", new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {

                }
            });
        }
        coreNum =  getNumberOfCPUCores();
        WatchDogService.defaultCpuChooses = getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_DEFAULTCORECOUNT);
        WatchDogService.offScCpuChooses = getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_OFFSCREENCORECOUNT);
        WatchDogService.batteryLowCpuChooses = getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_BATTERYLOWCORECOUNT);
//        WatchDogService.delayOffCpuTime =cpuPrefs.getInt(Common.PREFS_SETCPU_OFFSCREENDELAY,60)*60;
//        WatchDogService.cpuBatteryBelow =cpuPrefs.getInt(Common.PREFS_SETCPU_BATTERYBELOWCOUNT,15);

        TextView topTitle = (TextView)v.findViewById(R.id.cpu_top_title);
        delaySb = (SeekBar) v.findViewById(R.id.cpu_off_delay_bar);
        batterySb = (SeekBar) v.findViewById(R.id.cpu_battery_bar);
        delaySb.setProgress(cpuPrefs.getInt(Common.PREFS_SETCPU_OFFSCREENDELAY,60));
        batterySb.setProgress(cpuPrefs.getInt(Common.PREFS_SETCPU_BATTERYBELOWCOUNT,15));
        cameraSw = (Switch) v.findViewById(R.id.cpu_camera_sw);
        cpulockSw = (Switch) v.findViewById(R.id.cpu_lockunlock_sw);
        autoStartSw = (Switch) v.findViewById(R.id.cpu_autostart_sw);
        offScSw = (Switch) v.findViewById(R.id.cpu_offsc_sw);
        batterySw = (Switch) v.findViewById(R.id.cpu_batterysc_sw);
        cpuCharingSw = (Switch) v.findViewById(R.id.cpu_charing_sw);
        offScSw.setText("熄屏"+delaySb.getProgress()+"分钟后关闭核心(充电时不关闭)");
        batterySw.setText("电量低于"+batterySb.getProgress()+"%时关闭核心(充电时不关闭)");
        curColor = topTitle.getCurrentTextColor();
        cpulockSw.setTextColor(curColor);
        autoStartSw.setTextColor(curColor);
        cameraSw.setTextColor(curColor);
        offScSw.setTextColor(curColor);
        batterySw.setTextColor(curColor);
        cpuCharingSw.setTextColor(curColor);
        cpulockSw.setTag(0);
        autoStartSw.setTag(1);
        cameraSw.setTag(2);
        offScSw.setTag(3);
        batterySw.setTag(4);
        cpuCharingSw.setTag(5);
        cpulockSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_LOCKUNLOCK, false));
        if(coreNum==0){
            cpulockSw.setChecked(false);
            Toast.makeText(context,"未检测到您的cpu核心数",Toast.LENGTH_LONG).show();
        }else if(!WatchDogService.isRoot){
            cpulockSw.setChecked(false);
            Toast.makeText(context,"未检测到有ROOT权限，请赋予ROOT权限后再使用",Toast.LENGTH_LONG).show();
        }
        autoStartSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_AUTOSTART, false));
        cameraSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CAMERAMODE, false));
        offScSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_OFFSCREENOPEN, false));
        batterySw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_BATTERYBELOWOPEN, false));
        cpuCharingSw.setChecked(cpuPrefs.getBoolean(Common.PREFS_SETCPU_CHARGINGNOTSTOP, false));
        switchCpuSet();
        CPUSetView.ChangeListener changeListener = new CPUSetView.ChangeListener();
        cpulockSw.setOnCheckedChangeListener(changeListener);
        autoStartSw.setOnCheckedChangeListener(changeListener);
        cameraSw.setOnCheckedChangeListener(changeListener);
        offScSw.setOnCheckedChangeListener(changeListener);
        batterySw.setOnCheckedChangeListener(changeListener);
        cpuCharingSw.setOnCheckedChangeListener(changeListener);

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
                        FileUtil.copyAssets(context, "unlock_lowbatter_core", FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core");
                        FileUtil.copyAssets(context, "lock_lowbatter_core", FileUtil.FILEPATH + File.separator + "lock_lowbatter_core");
                    }
                    String s = isLockUnlockCPU ? "sh " + FileUtil.FILEPATH + File.separator + "unlock_lowbatter_core" : "sh " + FileUtil.FILEPATH + File.separator + "lock_lowbatter_core";
                    ShellUtilNoBackData.execCommand(s);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <coreNum; i++) {
//                        Thread.sleep(600);
                        boolean isCPU = WatchDogService.defaultCpuChooses[i]==1;//cpuPrefs.getBoolean(names[i], true);
                        sb.append("echo -n " + (isCPU ? "1" : "0") + " > /sys/devices/system/cpu/cpu" + i + "/online").append("\n");
                    }
                    ShellUtilNoBackData.execCommand(sb.toString());
                }catch (Exception e){
                }
            }
        }.start();
        cpuDefaultFl = (FrameLayout) v.findViewById(R.id.cpu_default_fl);
        cpuOffDelayFl = (FrameLayout) v.findViewById(R.id.cpu_off_delay_fl);
        cpuOffScFl = (FrameLayout) v.findViewById(R.id.cpu_offsc_fl);
        cpuBatteryFl = (FrameLayout) v.findViewById(R.id.cpu_battery_fl);
        cpuBatteryChangeFl = (FrameLayout) v.findViewById(R.id.cpu_battery_change_fl);
        cpuOffScFl.setVisibility(offScSw.isChecked()?View.VISIBLE:View.GONE);
        cpuDefaultFl.setVisibility(cpulockSw.isChecked()?View.VISIBLE:View.GONE);
        cpuOffDelayFl.setVisibility(offScSw.isChecked()?View.VISIBLE:View.GONE);
        cpuBatteryFl.setVisibility(batterySw.isChecked()?View.VISIBLE:View.GONE);
        cpuBatteryChangeFl.setVisibility(batterySw.isChecked()?View.VISIBLE:View.GONE);
        initItem(cpuDefaultFl,Common.PREFS_SETCPU_DEFAULTCORECOUNT);
        initItem(cpuOffScFl,Common.PREFS_SETCPU_OFFSCREENCORECOUNT);
        initItem(cpuBatteryFl,Common.PREFS_SETCPU_BATTERYLOWCORECOUNT);
        delaySb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                offScSw.setText("熄屏"+seekBar.getProgress()+"分钟后关闭核心(充电时不关闭)");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cpuPrefs.edit().putInt(Common.PREFS_SETCPU_OFFSCREENDELAY,seekBar.getProgress()).commit();
                WatchDogService.delayOffCpuTime = seekBar.getProgress()*60;
            }
        });
        batterySb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                batterySw.setText("电量低于"+(seekBar.getProgress()>99?99:seekBar.getProgress())+"%时关闭核心(充电时不关闭)");
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cpuPrefs.edit().putInt(Common.PREFS_SETCPU_BATTERYBELOWCOUNT,(seekBar.getProgress()>99?99:seekBar.getProgress())).commit();
                WatchDogService.cpuBatteryBelow = (seekBar.getProgress()>99?99:seekBar.getProgress());
                Intent intent = new Intent("com.click369.control.cpubatterychange");
                context.sendBroadcast(intent);
            }
        });
    }
    private void initItem(final FrameLayout flview, final String key){
        int layoutids[] = {R.id.cpu_item0_ll,R.id.cpu_item1_ll,R.id.cpu_item2_ll,R.id.cpu_item3_ll,R.id.cpu_item4_ll,
                R.id.cpu_item5_ll,R.id.cpu_item6_ll,R.id.cpu_item7_ll,R.id.cpu_item8_ll,R.id.cpu_item9_ll};
        LinearLayout layouts[] = new LinearLayout[10];
        int ivids[] = {R.id.cpu_item0_iv,R.id.cpu_item1_iv,R.id.cpu_item2_iv,R.id.cpu_item3_iv,R.id.cpu_item4_iv,
                R.id.cpu_item5_iv,R.id.cpu_item6_iv,R.id.cpu_item7_iv,R.id.cpu_item8_iv,R.id.cpu_item9_iv};
        final int chooses[] = getCpuCoreInfo(cpuPrefs,key);
        final ImageView imgs[] = new ImageView[10];
        class CpuItemClickListener implements View.OnClickListener{
            @Override
            public void onClick(View view) {
                if (!cpulockSw.isChecked()){return;}
                BaseActivity.zhenDong(context);
                int tag = (Integer)(view.getTag());
                chooses[tag] = (chooses[tag]==0)?1:0;
                imgs[tag].setImageResource(chooses[tag]==0?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
                saveCpuCoreInfo(cpuPrefs,key,chooses);
                if(flview.equals(cpuDefaultFl)){
                    WatchDogService.defaultCpuChooses[tag] = chooses[tag];
                    String s = "echo -n "+(chooses[tag]==1?"1":"0")+" > /sys/devices/system/cpu/cpu"+tag+"/online";
                    ShellUtilNoBackData.execCommand(s);
                }
            }
        }
        CpuItemClickListener cpuItemClickListener = new CpuItemClickListener();
        for(int i = 0;i<layoutids.length;i++){
            layouts[i] = (LinearLayout)flview.findViewById(layoutids[i]);
            layouts[i].setVisibility(View.GONE);
            imgs[i] = (ImageView)flview.findViewById(ivids[i]);
            imgs[i].setTag(i);
            imgs[i].setOnClickListener(cpuItemClickListener);
            if(i<chooses.length){
//                chooses[i] = states.length>i&&states[i].length()>0?Integer.parseInt(states[i]):1;
                imgs[i].setImageResource(chooses[i]==0?R.mipmap.icon_disable:R.mipmap.icon_notdisable);
            }
        }
        layouts[0].setEnabled(false);
        layouts[0].setAlpha(0.7f);
        imgs[0].setEnabled(false);
        for(int i = 0;i<coreNum;i++){
            layouts[i].setVisibility(View.VISIBLE);
        }
        saveCpuCoreInfo(cpuPrefs,key,chooses);
    }


    class  ChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int tag = (Integer) buttonView.getTag();
            String names[] = {Common.PREFS_SETCPU_LOCKUNLOCK,Common.PREFS_SETCPU_AUTOSTART,
                    Common.PREFS_SETCPU_CAMERAMODE,Common.PREFS_SETCPU_OFFSCREENOPEN,
                    Common.PREFS_SETCPU_BATTERYBELOWOPEN,Common.PREFS_SETCPU_CHARGINGNOTSTOP};
            cpuPrefs.edit().putBoolean(names[tag],isChecked).commit();
            if(tag == 0){
                //sh system/etc/init.d/lock_lowbatter_core
                //sh system/etc/init.d/unlock_lowbatter_core
                File file1 = new File(FileUtil.FILEPATH,"unlock_lowbatter_core");
                File file2 = new File(FileUtil.FILEPATH,"lock_lowbatter_core");
                if(!file1.exists()||!file2.exists()){
                    FileUtil.init();
                    FileUtil.copyAssets(context,"unlock_lowbatter_core",FileUtil.FILEPATH+File.separator+"unlock_lowbatter_core");
                    FileUtil.copyAssets(context,"lock_lowbatter_core",FileUtil.FILEPATH+File.separator+"lock_lowbatter_core");
                }
                String s = isChecked?"sh "+FileUtil.FILEPATH+File.separator+"unlock_lowbatter_core":"sh "+FileUtil.FILEPATH+File.separator+"lock_lowbatter_core";
//                String s = isChecked?"sh system/etc/init.d/unlock_lowbatter_core":"sh system/etc/init.d/lock_lowbatter_core";
                ShellUtilNoBackData.execCommand(s);
                switchCpuSet();
                WatchDogService.isLockUnlockCPU = isChecked;
                cpuDefaultFl.setVisibility(isChecked?View.VISIBLE:View.GONE);
                WatchDogService.defaultCpuChooses = getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_DEFAULTCORECOUNT);
                resetCpuLock();
            }else if(tag == 3){
                WatchDogService.isOffScreenLockCpu = isChecked;
                cpuOffScFl.setVisibility(isChecked?View.VISIBLE:View.GONE);
                cpuOffDelayFl.setVisibility(offScSw.isChecked()?View.VISIBLE:View.GONE);
                WatchDogService.offScCpuChooses = getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_OFFSCREENCORECOUNT);
            }else if(tag == 4){
                WatchDogService.isBatteryLowLockCPU = isChecked;
                cpuBatteryChangeFl.setVisibility(isChecked?View.VISIBLE:View.GONE);
                cpuBatteryFl.setVisibility(batterySw.isChecked()?View.VISIBLE:View.GONE);
                WatchDogService.batteryLowCpuChooses = getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_BATTERYLOWCORECOUNT);
                Intent intent = new Intent("com.click369.control.cpubatterychange");
                context.sendBroadcast(intent);
            }else if(tag == 5){
                WatchDogService.isChargingNotLockCPU = isChecked;
                CpuUtil.changeCpu();
            }
        }

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
        if (cores>10){
            cores = 10;
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

    private void switchCpuSet(){
        cameraSw.setEnabled(cpulockSw.isChecked());
        autoStartSw.setEnabled(cpulockSw.isChecked());
        offScSw.setEnabled(cpulockSw.isChecked());
        batterySw.setEnabled(cpulockSw.isChecked());
        delaySb.setEnabled(cpulockSw.isChecked());
        batterySb.setEnabled(cpulockSw.isChecked());
//        cpu0.setEnabled(cpulockSw.isChecked());
//        cpu1.setEnabled(cpulockSw.isChecked());
//        cpu2.setEnabled(cpulockSw.isChecked());
//        cpu3.setEnabled(cpulockSw.isChecked());
//        cpu4.setEnabled(cpulockSw.isChecked());
//        cpu5.setEnabled(cpulockSw.isChecked());
//        cpu6.setEnabled(cpulockSw.isChecked());
//        cpu7.setEnabled(cpulockSw.isChecked());
    }

    public static int[] getCpuCoreInfo(SharedPreferences preferences,String key){
        String offs = preferences.getString(key,"");
        String states[] = offs.split(",");
        int choosesTemp[] = new int[getNumberOfCPUCores()];
        for(int i = 0;i<choosesTemp.length;i++){
            choosesTemp[i] = states.length>i&&states[i].length()>0?Integer.parseInt(states[i]):1;
        }
        return choosesTemp;
    }
    public static void saveCpuCoreInfo(SharedPreferences preferences,String key,int chooses[]){
        StringBuilder sb = new StringBuilder();
        for(int i:chooses){
            sb.append(i).append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        preferences.edit().putString(key,sb.toString()).commit();
    }
    private void resetCpuLock(){
        if (!cpulockSw.isChecked()){
//            WatchDogService.defaultCpuChooses = getCpuCoreInfo(cpuPrefs,Common.PREFS_SETCPU_DEFAULTCORECOUNT);
//        }else{
            for(int i = 0;i<WatchDogService.defaultCpuChooses.length;i++){
                WatchDogService.defaultCpuChooses[i] = 1;
            }
        }
        new Thread(){
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 1;i<coreNum;i++){
                        if(WatchDogService.defaultCpuChooses[i]==0){
                            sb.append("echo -n 0 > /sys/devices/system/cpu/cpu"+i+"/online").append("\n");
                        }else{
                            sb.append("echo -n 1 > /sys/devices/system/cpu/cpu"+i+"/online").append("\n");
                        }
                    }
                    ShellUtilNoBackData.execCommand(sb.toString());
                }catch (Exception e){}
            }
        }.start();
    }
}

package com.click369.controlbp.util;

import android.util.Log;

import com.click369.controlbp.service.WatchDogService;

/**
 * Created by 41856 on 2019/2/3.
 */

public class CpuUtil {
    private static int nowtype = -1;//-1还没设置  0默认  1低电量  2熄屏  100所有核心开
    public static void changeCpu(){
        if(!WatchDogService.isLockUnlockCPU){
            return;
        }
        if((!WatchDogService.isChargingNotLockCPU||(WatchDogService.isChargingNotLockCPU&&!WatchDogService.isCharging))&&
                ((WatchDogService.isCameraMode&&!WatchDogService.isOpenCamera)||!WatchDogService.isCameraMode)){
            if (WatchDogService.cpuBatteryLowIsAlreadyLock&&nowtype!=1) {
                nowtype = 1;
                lockCpu(WatchDogService.batteryLowCpuChooses);
                Log.i("CONTROL","CPU 低电量核心设置");
            }else if (WatchDogService.isOffScreenLockCpu&&WatchDogService.isScreenOff&&nowtype!=2) {
                nowtype = 2;
                lockCpu(WatchDogService.offScCpuChooses);
                Log.i("CONTROL","CPU 熄屏核心设置");
            } else if(nowtype!=0){
                nowtype = 0;
                lockCpu(WatchDogService.defaultCpuChooses);
                Log.i("CONTROL","CPU 默认核心设置");
            }
        }else if(nowtype!=100){
            nowtype =100;
            openAllCpuCore();
            Log.i("CONTROL","CPU 所有核心开启");
        }
    }
    private static void openAllCpuCore(){
        new Thread(){
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 1;i<WatchDogService.cpuNum;i++){
                        sb.append("echo -n 1 > /sys/devices/system/cpu/cpu"+i+"/online").append("\n");
                    }
                    ShellUtilNoBackData.execCommand(sb.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void lockCpu(final int cpuChooses[]){
        if(WatchDogService.isChargingNotLockCPU&&WatchDogService.isCharging){
            return;
        }
        new Thread(){
            @Override
            public void run() {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < WatchDogService.cpuNum; i++) {
                        sb.append("echo -n " + (cpuChooses[i] == 1 ? 1 : 0) + " > /sys/devices/system/cpu/cpu" + i + "/online").append("\n");
                    }
                    ShellUtilNoBackData.execCommand(sb.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

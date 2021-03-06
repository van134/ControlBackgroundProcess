package com.click369.controlbp.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.click369.controlbp.service.LightView;
import com.click369.controlbp.service.ScreenLightServiceUtil;
import com.click369.controlbp.service.WatchDogService;

/**
 * Created by asus on 2017/5/31.
 */
public class PhoneStateReciver  extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if("android.intent.action.PHONE_STATE".equals(intent.getAction())){
            //获得相应的系统服务
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if(tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                Log.i("DOZE", "call state idle...");
                WatchDogService.isHookOff = false;
                ScreenLightServiceUtil.sendHideLight(context);

                if(WatchDogService.isFlashCall){
                    if(!WatchDogService.isFlashInOffSc||(WatchDogService.isFlashInOffSc&&
                            (System.currentTimeMillis()-WatchDogService.lastScreenOnTime<500||WatchDogService.isScreenOff))) {
                        Intent intent1 = new Intent("com.click369.control.callflash.stop");
                        context.sendBroadcast(intent1);
                    }
                }
            }else if(tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                Log.i("DOZE", "call state offhook...");
                WatchDogService.isHookOff = true;
                ScreenLightServiceUtil.sendHideLight(context);
                if(WatchDogService.isFlashCall){
                    Intent intent1 = new Intent("com.click369.control.callflash.stop");
                    context.sendBroadcast(intent1);
                }
            }else if(tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                Log.i("DOZE", "call state ringing...");
                WatchDogService.isHookOff = true;
                ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_CALL,context);
                if(WatchDogService.isFlashCall){
                    if(!WatchDogService.isFlashInOffSc||(WatchDogService.isFlashInOffSc&&
                            (System.currentTimeMillis()-WatchDogService.lastScreenOnTime<500||WatchDogService.isScreenOff))) {
                        Intent intent1 = new Intent("com.click369.control.callflash.start");
                        context.sendBroadcast(intent1);
                    }
                }
            }
        }else if("android.intent.action.NEW_OUTGOING_CALL".equals(intent.getAction())){
            TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            if(tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                Log.i("DOZE", "call state idle...");
                WatchDogService.isHookOff = false;
            }else if(tm.getCallState() == TelephonyManager.CALL_STATE_OFFHOOK) {
                Log.i("DOZE", "call state offhook...");
                WatchDogService.isHookOff = true;
            }else if(tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                Log.i("DOZE", "call state ringing...");
                WatchDogService.isHookOff = true;
            }
        }
    }
}

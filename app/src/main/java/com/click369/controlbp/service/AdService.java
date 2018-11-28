package com.click369.controlbp.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.SharedPrefsUtil;

/**
 * Created by asus on 2017/12/1.
 */
public class AdService {
    private WatchDogService service;
    private MyAdReceiver adReceiver;
    public AdService(WatchDogService service){
        this.service = service;
        IntentFilter ifliter = new IntentFilter();
        ifliter.addAction("com.click369.control.ad");
        adReceiver = new MyAdReceiver();
        service.registerReceiver(adReceiver,ifliter);
    }

    class MyAdReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.click369.control.ad")){
                String pkg = intent.getStringExtra("pkg");
                String two = intent.getStringExtra("two");
                String three = intent.getStringExtra("three");
                SharedPreferences adPrefs = SharedPrefsUtil.getInstance(context).adPrefs;//SharedPrefsUtil.getPreferences(service, Common.IPREFS_ADLIST);
                Log.i("CONTROL","AD TWO"+two+"  THREE "+three);
                if(two.length()>0){
                    adPrefs.edit().putString(pkg+"/two",two).commit();
                }else if(three.length()>0){
                    adPrefs.edit().putString(pkg+"/three",three).commit();
                }
            }
        }
    }
    public void destory(){
        if (adReceiver!=null){
            service.unregisterReceiver(adReceiver);
        }
    }
}

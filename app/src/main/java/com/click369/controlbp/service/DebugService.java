package com.click369.controlbp.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.click369.controlbp.activity.DebugActivity;
import com.click369.controlbp.activity.EmptyActivity;

/**
 * Created by 41856 on 2018/11/26.
 */

public class DebugService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instence = this;
    }

    public static DebugService instence;
    public static DebugService getInstence(){
        return instence;
    };
    public void sendError(String msg){
        Intent intent = new Intent(this, DebugActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.putExtra("data", "异常");
        intent.putExtra("content", msg);
        startActivity(intent);
    }
}

package com.click369.controlbp.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.click369.controlbp.R;
import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.util.OpenCloseUtil;

public class IceRoomActivity extends BaseActivity {
    private IceRoomView irv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MainActivity.isNightMode){
            setTheme(R.style.AppTheme_NoActionBarDark);
        }
        irv = new IceRoomView();
        setContentView(R.layout.activity_iceroom);

        FrameLayout fl = (FrameLayout)this.findViewById(R.id.ice_room_mainfl);
        if(MainActivity.isNightMode){
            fl.setBackgroundColor(Color.BLACK);
        }else{
            fl.setBackgroundColor(Color.WHITE);
        }
        fl.addView(irv.onCreate(getLayoutInflater(),this));
    }

    @Override
    protected void onRestart() {
    	this.finish();
    	super.onRestart();
    }

}

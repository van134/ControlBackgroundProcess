package com.click369.controlbp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;

/**
 * Created by asus on 2017/10/19.
 */
public class LockAppSwitchView {
    public static boolean isCloseOk = false;
    private SharedPreferences settings;
    public TextView titleTv,setPwdTv;
    private FrameLayout setPwdFl;
    public Switch isNotShowLockUISw,unlockNumberSw,offscLockSw,usePWDsw;
    private View v;
    private Context cxt;
    private int curColor = Color.BLACK;
    public LockAppSwitchView(SharedPreferences settingPrefs, View v, Context cxt){
        this.settings = settingPrefs;
        this.v = v;
        this.cxt =cxt;
    }
    public void initView() {
//        View v = act.getLayoutInflater().inflate(R.layout.layout_topsearch,null);
        titleTv = (TextView) v.findViewById(R.id.setting_lockapp_title);
        curColor = titleTv.getCurrentTextColor();
        setPwdFl = (FrameLayout) v.findViewById(R.id.setting_lock_setpwd_fl);
        setPwdTv = (TextView) v.findViewById(R.id.setting_lock_setpwd_tv);
        isNotShowLockUISw = (Switch) v.findViewById(R.id.setting_showui_sw);
        offscLockSw = (Switch) v.findViewById(R.id.setting_offsclock_sw);
        unlockNumberSw = (Switch) v.findViewById(R.id.setting_showunlocknumber_sw);
        usePWDsw = (Switch) v.findViewById(R.id.setting_usepwd_sw);
        isNotShowLockUISw.setTextColor(curColor);
        unlockNumberSw.setTextColor(curColor);
        offscLockSw.setTextColor(curColor);
        usePWDsw.setTextColor(curColor);
        isNotShowLockUISw.setTag(0);
        offscLockSw.setTag(1);
        unlockNumberSw.setTag(2);
        usePWDsw.setTag(3);


        isNotShowLockUISw.setChecked(settings.getBoolean(Common.PREFS_APPSTART_ISSHOWUI,false));
        offscLockSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_OFFSCLOCK,true));
        unlockNumberSw.setChecked(settings.getBoolean(Common.PREFS_APPSTART_ISSHOWNUMBERLOCK,true));
        usePWDsw.setChecked(settings.getBoolean(Common.PREFS_SETTING_USEPWDLOCK,false));

        SwitchClick sc = new SwitchClick();
        isNotShowLockUISw.setOnCheckedChangeListener(sc);
        offscLockSw.setOnCheckedChangeListener(sc);
        unlockNumberSw.setOnCheckedChangeListener(sc);
        usePWDsw.setOnCheckedChangeListener(sc);

        setPwdTv.setEnabled(usePWDsw.isChecked());
        setPwdTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(cxt,SetPWDActivity.class);
                cxt.startActivity(intent);
            }
        });
    }

    class SwitchClick implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BaseActivity.zhenDong(cxt);
            int tag= (Integer) buttonView.getTag();
            String names[] = {Common.PREFS_APPSTART_ISSHOWUI,Common.PREFS_SETTING_OFFSCLOCK,
                    Common.PREFS_APPSTART_ISSHOWNUMBERLOCK,Common.PREFS_SETTING_USEPWDLOCK};
            if(tag ==1){
                WatchDogService.isOffScLockApp = isChecked;
            }if (tag == 3){
                if (isChecked==false&&settings.getString(Common.PREFS_SETTING_APPPWD,"").length()==4&&!isCloseOk){
                    Intent intent = new Intent(cxt,SetPWDActivity.class);
                    intent.putExtra("isclose",true);
                    cxt.startActivity(intent);
                    buttonView.setChecked(!isChecked);
                    return;
                }else{
                    setPwdTv.setEnabled(isChecked);
                }
            }
            settings.edit().putBoolean(names[tag],isChecked).commit();
        }
    }


}

package com.click369.controlbp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;

/**
 * Created by asus on 2017/10/19.
 */
public class LinkAppSwitchView {
    private SharedPreferences settings;
    public TextView titleTv;
    public Switch stopAndAutoSw,stopAndRemoveStopSw,recentNotCleanAndNotstopSw,recentRemoveAndAutoSw;
    private View v;
    private Context cxt;
    private int curColor = Color.BLACK;
    public LinkAppSwitchView(SharedPreferences settingPrefs, View v, Context cxt){
        this.settings = settingPrefs;
        this.v = v;
        this.cxt =cxt;
    }
    public void initView() {
//        View v = act.getLayoutInflater().inflate(R.layout.layout_topsearch,null);
        titleTv = (TextView) v.findViewById(R.id.setting_link_title);
        curColor = titleTv.getCurrentTextColor();
        stopAndAutoSw = (Switch) v.findViewById(R.id.setting_linkstopandautostart_sw);
        stopAndRemoveStopSw = (Switch) v.findViewById(R.id.setting_linkstopandremovestop_sw);
        recentNotCleanAndNotstopSw = (Switch) v.findViewById(R.id.setting_linkrecentnotcleanandnotstop_sw);
        recentRemoveAndAutoSw = (Switch) v.findViewById(R.id.setting_linkrecentremoveandautostart_sw);

        stopAndAutoSw.setTextColor(curColor);
        stopAndRemoveStopSw.setTextColor(curColor);
        recentNotCleanAndNotstopSw.setTextColor(curColor);
        recentRemoveAndAutoSw.setTextColor(curColor);
        stopAndAutoSw.setTag(0);
        stopAndRemoveStopSw.setTag(1);
        recentNotCleanAndNotstopSw.setTag(2);
        recentRemoveAndAutoSw.setTag(3);
        stopAndAutoSw.setChecked(WatchDogService.isLinkStopAndAuto);
        stopAndRemoveStopSw.setChecked(WatchDogService.isLinkStopAndRemoveStop);
        recentNotCleanAndNotstopSw.setChecked(WatchDogService.isLinkRecentAndNotStop);
        recentRemoveAndAutoSw.setChecked(WatchDogService.isLinkRecentAndAuto);
        SwitchClick sc = new SwitchClick();
        stopAndAutoSw.setOnCheckedChangeListener(sc);
        stopAndRemoveStopSw.setOnCheckedChangeListener(sc);
        recentNotCleanAndNotstopSw.setOnCheckedChangeListener(sc);
        recentRemoveAndAutoSw.setOnCheckedChangeListener(sc);
    }

    class SwitchClick implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BaseActivity.zhenDong(cxt);
            int tag= (Integer) buttonView.getTag();
            String names[] = {Common.PREFS_SETTING_LINK_STOPANDAUTOSTART,
                    Common.PREFS_SETTING_LINK_STOPANDREMOVERECENTSTOP,
                    Common.PREFS_SETTING_LINK_RECNETNOTCLEANANDNOTSTOP,
                    Common.PREFS_SETTING_LINK_RECNETREMOVEANDAUTOSTART};
            settings.edit().putBoolean(names[tag],isChecked).commit();
            if (tag == 0){
                WatchDogService.isLinkStopAndAuto = isChecked;
            }else if(tag == 1){
                WatchDogService.isLinkStopAndRemoveStop = isChecked;
            }else if(tag == 2){
                WatchDogService.isLinkRecentAndNotStop = isChecked;
            }else if(tag == 3){
                WatchDogService.isLinkRecentAndAuto = isChecked;
            }
        }
    }
}

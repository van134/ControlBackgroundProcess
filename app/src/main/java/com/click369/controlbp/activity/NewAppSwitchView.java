package com.click369.controlbp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;

/**
 * Created by asus on 2017/10/19.
 */
public class NewAppSwitchView {
    private SharedPreferences settings;
    public TextView titleTv;
    public Switch newAppAutoOpenControlSw,backmubeSw,homemubeSw,homeIdleSw,offmubeSw,backSw,offSw,autoStartSw,newAppAddRemoveRecentExit,notifySw,blurSw,colorBarSw;
    private View v;
    private Context cxt;
    private int curColor = Color.BLACK;
    public NewAppSwitchView(SharedPreferences settingPrefs, View v, Context cxt){
        this.settings = settingPrefs;
        this.v = v;
        this.cxt =cxt;
    }
    public void initView() {
//        View v = act.getLayoutInflater().inflate(R.layout.layout_topsearch,null);
        titleTv = (TextView) v.findViewById(R.id.setting_newapp_title);
        curColor = titleTv.getCurrentTextColor();
        newAppAutoOpenControlSw = (Switch) v.findViewById(R.id.setting_newappautoopencontrol_sw);
        backmubeSw = (Switch) v.findViewById(R.id.setting_newappbackmubei_sw);
        homemubeSw = (Switch) v.findViewById(R.id.setting_newapphomemubei_sw);
        homeIdleSw = (Switch) v.findViewById(R.id.setting_newapphomeidle_sw);
        offmubeSw = (Switch) v.findViewById(R.id.setting_newappoffmubei_sw);
        backSw = (Switch) v.findViewById(R.id.setting_backautoadd_sw);
        offSw = (Switch) v.findViewById(R.id.setting_offautoadd_sw);
        autoStartSw = (Switch) v.findViewById(R.id.setting_autostartadd_sw);
        newAppAddRemoveRecentExit = (Switch) v.findViewById(R.id.setting_newappaddrecentmovestop_sw);
        notifySw = (Switch) v.findViewById(R.id.setting_newappnotifyadd_sw);
        blurSw = (Switch) v.findViewById(R.id.setting_newapprecentbluradd_sw);
        colorBarSw = (Switch) v.findViewById(R.id.setting_newappaddcolorbar_sw);
        newAppAutoOpenControlSw.setTextColor(curColor);
        backmubeSw.setTextColor(curColor);
        homemubeSw.setTextColor(curColor);
        homeIdleSw.setTextColor(curColor);
        offmubeSw.setTextColor(curColor);
        backSw.setTextColor(curColor);
        offSw.setTextColor(curColor);
        autoStartSw.setTextColor(curColor);
        newAppAddRemoveRecentExit.setTextColor(curColor);
        notifySw.setTextColor(curColor);
        blurSw.setTextColor(curColor);
        colorBarSw.setTextColor(curColor);
        backSw.setTag(0);
        offSw.setTag(1);
        autoStartSw.setTag(2);
        newAppAddRemoveRecentExit.setTag(3);
        backmubeSw.setTag(4);
        homemubeSw.setTag(5);
        offmubeSw.setTag(6);
        notifySw.setTag(7);
        blurSw.setTag(8);
        colorBarSw.setTag(9);
        homeIdleSw.setTag(10);
        newAppAutoOpenControlSw.setTag(11);
        if(settings.getBoolean(Common.PREFS_SETTING_NEWAPPHOMEMUBEI,false)){
            settings.edit().remove(Common.PREFS_SETTING_NEWAPPBACKMUBEI).commit();
            settings.edit().remove(Common.PREFS_SETTING_NEWAPPOFFMUBEI).commit();
            settings.edit().remove(Common.PREFS_SETTING_NEWAPPHOMEIDLE).commit();
            backmubeSw.setEnabled(false);
            offmubeSw.setEnabled(false);
            homeIdleSw.setEnabled(false);
        }
        backmubeSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPBACKMUBEI,false));
        homemubeSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPHOMEMUBEI,false));
        homeIdleSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPHOMEIDLE,false));
        offmubeSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPOFFMUBEI,false));
        backSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_BACKAPPAUTOADD,false));
        offSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_OFFAPPAUTOADD,false));
        autoStartSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_AUTOSTARTAPPAUTOADD,false));
        newAppAddRemoveRecentExit.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDREMOVERECENTEXIT,false));
        notifySw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDNOTIFY,false));
        blurSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDBLUR,false));
        colorBarSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPADDCOLORBAR,false));
        newAppAutoOpenControlSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NEWAPPAUTOOPENCONTROL,false));
        if(backSw.isChecked()){
            backmubeSw.setChecked(false);
            settings.edit().remove(Common.PREFS_SETTING_NEWAPPBACKMUBEI).commit();
        }
        if(offSw.isChecked()){
            offmubeSw.setChecked(false);
            settings.edit().remove(Common.PREFS_SETTING_NEWAPPOFFMUBEI).commit();
        }
        SwitchClick sc = new SwitchClick();
        newAppAutoOpenControlSw.setOnCheckedChangeListener(sc);
        backSw.setOnCheckedChangeListener(sc);
        homeIdleSw.setOnCheckedChangeListener(sc);
        offSw.setOnCheckedChangeListener(sc);
        autoStartSw.setOnCheckedChangeListener(sc);
        newAppAddRemoveRecentExit.setOnCheckedChangeListener(sc);
        backmubeSw.setOnCheckedChangeListener(sc);
        homemubeSw.setOnCheckedChangeListener(sc);
        offmubeSw.setOnCheckedChangeListener(sc);
        notifySw.setOnCheckedChangeListener(sc);
        blurSw.setOnCheckedChangeListener(sc);
        colorBarSw.setOnCheckedChangeListener(sc);
    }

    class SwitchClick implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BaseActivity.zhenDong(cxt);
            int tag= (Integer) buttonView.getTag();
            String names[] = {Common.PREFS_SETTING_BACKAPPAUTOADD,Common.PREFS_SETTING_OFFAPPAUTOADD,
                    Common.PREFS_SETTING_AUTOSTARTAPPAUTOADD,Common.PREFS_SETTING_NEWAPPADDREMOVERECENTEXIT,
                    Common.PREFS_SETTING_NEWAPPBACKMUBEI,Common.PREFS_SETTING_NEWAPPHOMEMUBEI,Common.PREFS_SETTING_NEWAPPOFFMUBEI,
            Common.PREFS_SETTING_NEWAPPADDNOTIFY,Common.PREFS_SETTING_NEWAPPADDBLUR,Common.PREFS_SETTING_NEWAPPADDCOLORBAR,
                    Common.PREFS_SETTING_NEWAPPHOMEIDLE,Common.PREFS_SETTING_NEWAPPAUTOOPENCONTROL};
            settings.edit().putBoolean(names[tag],isChecked).commit();
            if(tag == 0){
                if(isChecked){
                    backmubeSw.setChecked(false);
                    settings.edit().remove(Common.PREFS_SETTING_NEWAPPBACKMUBEI).commit();
                }
            }else if(tag == 4){
                if(isChecked){
                    backSw.setChecked(false);
                    settings.edit().remove(Common.PREFS_SETTING_BACKAPPAUTOADD).commit();
                }
            }else if(tag == 1){
                if(isChecked){
                    offmubeSw.setChecked(false);
                    settings.edit().remove(Common.PREFS_SETTING_NEWAPPOFFMUBEI).commit();
                }
            }else if(tag == 6){
                if(isChecked){
                    offSw.setChecked(false);
                    settings.edit().remove(Common.PREFS_SETTING_OFFAPPAUTOADD).commit();
                }
            }
            if (tag == 5){
                if (isChecked){
                    settings.edit().remove(Common.PREFS_SETTING_NEWAPPBACKMUBEI).commit();
                    settings.edit().remove(Common.PREFS_SETTING_NEWAPPOFFMUBEI).commit();
                    settings.edit().remove(Common.PREFS_SETTING_NEWAPPHOMEIDLE).commit();
                    backmubeSw.setChecked(false);
                    offmubeSw.setChecked(false);
                    homeIdleSw.setChecked(false);
                }else{
                    backmubeSw.setEnabled(true);
                    offmubeSw.setEnabled(true);
                    homeIdleSw.setEnabled(true);
                }
            }
        }
    }
}

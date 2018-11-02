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
public class FunctionSwitchView {
    private SharedPreferences settingPrefs;
    public TextView titleTv;
    public Switch oneSW,twoSW,threeSW,fourSW,fiveSW,sixSW,sevenSW,eightSW,nineSW,tenSw;
    private View v;
    private Context cxt;
    private int curColor = Color.BLACK;
    public FunctionSwitchView(SharedPreferences settingPrefs, View v, Context cxt){
        this.settingPrefs = settingPrefs;
        this.v = v;
        this.cxt =cxt;
    }
    public void initView() {
//        View v = act.getLayoutInflater().inflate(R.layout.layout_topsearch,null);
        titleTv = (TextView) v.findViewById(R.id.setting_all_title);
        curColor = titleTv.getCurrentTextColor();
        oneSW = (Switch) v.findViewById(R.id.setting_allsw_onesw);
        twoSW = (Switch) v.findViewById(R.id.setting_allsw_twosw);
        threeSW = (Switch) v.findViewById(R.id.setting_allsw_threesw);
        fourSW = (Switch) v.findViewById(R.id.setting_allsw_foursw);
        fiveSW = (Switch) v.findViewById(R.id.setting_allsw_fivesw);
        sixSW = (Switch) v.findViewById(R.id.setting_allsw_sixsw);
        sevenSW = (Switch) v.findViewById(R.id.setting_allsw_sevensw);
        eightSW = (Switch) v.findViewById(R.id.setting_allsw_eightsw);
        nineSW = (Switch) v.findViewById(R.id.setting_allsw_ninesw);
        tenSw = (Switch) v.findViewById(R.id.setting_allsw_tensw);
        oneSW.setTextColor(curColor);
        twoSW.setTextColor(curColor);
        threeSW.setTextColor(curColor);
        fourSW.setTextColor(curColor);
        fiveSW.setTextColor(curColor);
        sixSW.setTextColor(curColor);
        sevenSW.setTextColor(curColor);
        eightSW.setTextColor(curColor);
        nineSW.setTextColor(curColor);
        tenSw.setTextColor(curColor);
        oneSW.setTag(0);
        twoSW.setTag(1);
        threeSW.setTag(2);
        fourSW.setTag(3);
        fiveSW.setTag(4);
        sixSW.setTag(5);
        sevenSW.setTag(6);
        eightSW.setTag(7);
        nineSW.setTag(8);
        tenSw.setTag(9);
        oneSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_ONE,true));
        twoSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_TWO,true));
        threeSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_THREE,true));
        fourSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_FOUR,true));
        fiveSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_FIVE,true));
        sixSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_SIX,true));
        sevenSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_SEVEN,true));
        eightSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_EIGHT,true));
        nineSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_NINE,true));
        tenSw.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_TEN,true));
        SwitchClick sc = new SwitchClick();
        oneSW.setOnCheckedChangeListener(sc);
        twoSW.setOnCheckedChangeListener(sc);
        threeSW.setOnCheckedChangeListener(sc);
        fourSW.setOnCheckedChangeListener(sc);
        fiveSW.setOnCheckedChangeListener(sc);
        sixSW.setOnCheckedChangeListener(sc);
        sevenSW.setOnCheckedChangeListener(sc);
        eightSW.setOnCheckedChangeListener(sc);
        nineSW.setOnCheckedChangeListener(sc);
        tenSw.setOnCheckedChangeListener(sc);
    }

    class SwitchClick implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int tag= (Integer) buttonView.getTag();
            String names[] = {Common.ALLSWITCH_ONE,Common.ALLSWITCH_TWO,Common.ALLSWITCH_THREE,
                    Common.ALLSWITCH_FOUR,Common.ALLSWITCH_FIVE,Common.ALLSWITCH_SIX,
                    Common.ALLSWITCH_SEVEN,Common.ALLSWITCH_EIGHT,Common.ALLSWITCH_NINE,
                    Common.ALLSWITCH_TEN};
            if(settingPrefs.getBoolean(names[tag],true)!=isChecked){
                Toast.makeText(cxt,"重启生效",Toast.LENGTH_LONG).show();
            }
            settingPrefs.edit().putBoolean(names[tag],isChecked).commit();
        }
    }
}

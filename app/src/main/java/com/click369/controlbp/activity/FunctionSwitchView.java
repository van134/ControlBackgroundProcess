package com.click369.controlbp.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;

import java.io.File;

/**
 * Created by asus on 2017/10/19.
 */
public class FunctionSwitchView {
    private SharedPreferences settingPrefs;
    public TextView titleTv;
    public Switch oneSW,twoSW,threeSW,fourSW,fiveSW,sixSW,sevenSW,eightSW,nineSW,tenSw,elevenSw,twleveSw,thirteenSw;
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
        oneSW = (Switch) v.findViewById(R.id.setting_allsw_service_broad_control);
        twoSW = (Switch) v.findViewById(R.id.setting_allsw_backstop_mubei_control);
        threeSW = (Switch) v.findViewById(R.id.setting_allsw_ifw_control);
        fourSW = (Switch) v.findViewById(R.id.setting_allsw_recentcard_control);
        fiveSW = (Switch) v.findViewById(R.id.setting_allsw_autostart_lock_control);
        sixSW = (Switch) v.findViewById(R.id.setting_allsw_uninstall_ice_control);
        sevenSW = (Switch) v.findViewById(R.id.setting_allsw_xpblacklist_control);
        eightSW = (Switch) v.findViewById(R.id.setting_allsw_privacy_control);
        nineSW = (Switch) v.findViewById(R.id.setting_allsw_adskip_control);
        tenSw = (Switch) v.findViewById(R.id.setting_allsw_doze_control);
        elevenSw = (Switch) v.findViewById(R.id.setting_allsw_ui_control);
        twleveSw = (Switch) v.findViewById(R.id.setting_allsw_cpuset_control);
        thirteenSw = (Switch) v.findViewById(R.id.setting_allsw_others_control);
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
        elevenSw.setTextColor(curColor);
        twleveSw.setTextColor(curColor);
        thirteenSw.setTextColor(curColor);
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
        elevenSw.setTag(10);
        twleveSw.setTag(11);
        thirteenSw.setTag(12);
        oneSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_SERVICE_BROAD,true));
        twoSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_BACKSTOP_MUBEI,true));
        threeSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_IFW,true));
        fourSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_RECNETCARD,true));
        fiveSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_AUTOSTART_LOCK,true));
        sixSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_UNINSTALL_ICE,true));
        sevenSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_XPBLACKLIST,true));
        eightSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_PRIVACY,true));
        nineSW.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_ADSKIP,true));
        tenSw.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_DOZE,true));
        elevenSw.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_UI,true));
        twleveSw.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_CPUSET,true));
        thirteenSw.setChecked(settingPrefs.getBoolean(Common.ALLSWITCH_OTHERS,true));

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
        elevenSw.setOnCheckedChangeListener(sc);
        twleveSw.setOnCheckedChangeListener(sc);
        thirteenSw.setOnCheckedChangeListener(sc);
    }

    class SwitchClick implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int tag= (Integer) buttonView.getTag();
            String names[] = {Common.ALLSWITCH_SERVICE_BROAD,Common.ALLSWITCH_BACKSTOP_MUBEI,
                    Common.ALLSWITCH_IFW,Common.ALLSWITCH_RECNETCARD,Common.ALLSWITCH_AUTOSTART_LOCK,
                    Common.ALLSWITCH_UNINSTALL_ICE,Common.ALLSWITCH_XPBLACKLIST,Common.ALLSWITCH_PRIVACY,
                    Common.ALLSWITCH_ADSKIP,Common.ALLSWITCH_DOZE,Common.ALLSWITCH_UI,
                    Common.ALLSWITCH_CPUSET,Common.ALLSWITCH_OTHERS};
            if(settingPrefs.getBoolean(names[tag],true)!=isChecked){
                Toast.makeText(cxt,"重启生效",Toast.LENGTH_LONG).show();
            }
            if (tag==9&&Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                AlertUtil.showAlertMsg(cxt,"您的手机版本不支持打盹功能,必须Android M(6.0)及以上才支持打盹！");
                return;
            }else if(tag==11){
                File qualcpuFile = new File(cxt.getFilesDir(),"qualcomm");
                if(!qualcpuFile.exists()){
                    AlertUtil.showAlertMsg(cxt,"检测到您的手机不是高通CPU，本功能目前只支持高通CPU！");
                    return;
                }
            }
            settingPrefs.edit().putBoolean(names[tag],isChecked).commit();
        }
    }
}

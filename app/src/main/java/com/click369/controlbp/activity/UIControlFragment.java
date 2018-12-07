package com.click369.controlbp.activity;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.RoundedCornerService;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedToast;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtils;
import com.click369.controlbp.util.ShortCutUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


public class UIControlFragment extends BaseFragment {
    private TextView blackListTv,toastGrvityTv,toastBgColorTv,toastFontColorTv,keyColorTv,toastPositionTv,corTv,topCorTv,roundSizeTv,recentRoundSizeTv,recentAlphaTv;
    private SeekBar toastPositionSb,corOffsetSb,corTopOffsetSb,roundSizeSb,recentRoundSizeSb,recentAlphaSb;
    private Switch topBarSw,bottomBarSw,alwaysColorSw,bottomDengTopSw,toastSw,keyColorSw,roundSw,recentBarColorSw,recentBarHideSw,recentMemSw,recentInfoSw,actInfoSw;
//    private ActivityManager activityManager;
    private int curColor = Color.BLACK;
    private int toastBgColor = Color.BLACK,toastTextColor = Color.WHITE,toastGrivity,toastPostion = 0;
    private SharedPreferences barPrefs;
    public UIControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_uicontrol, container, false);
        recentBarColorSw = (Switch) v.findViewById(R.id.ui_recentbar_color_sw);
        recentBarHideSw = (Switch) v.findViewById(R.id.ui_recentbar_hide_sw);
        recentMemSw = (Switch) v.findViewById(R.id.ui_recentbar_mem_sw);
        recentInfoSw = (Switch) v.findViewById(R.id.ui_recentbar_info_sw);
        topBarSw = (Switch) v.findViewById(R.id.ui_topbar_sw);
        toastSw = (Switch) v.findViewById(R.id.ui_toast_sw);
        bottomBarSw = (Switch) v.findViewById(R.id.ui_bottombar_sw);
        alwaysColorSw = (Switch) v.findViewById(R.id.ui_always_sw);
        bottomDengTopSw = (Switch) v.findViewById(R.id.ui_bottom_deng_top_sw);
        keyColorSw = (Switch) v.findViewById(R.id.ui_key_color_sw);
        roundSw = (Switch) v.findViewById(R.id.ui_round_sw);
        actInfoSw = (Switch) v.findViewById(R.id.ui_actinfo_sw);

        keyColorTv = (TextView) v.findViewById(R.id.ui_key_color_tv);
        blackListTv = (TextView) v.findViewById(R.id.ui_blacklist_tv);
        toastGrvityTv = (TextView) v.findViewById(R.id.ui_toast_grivaty);
        toastBgColorTv = (TextView) v.findViewById(R.id.ui_toast_bgcolor_tv);
        toastFontColorTv = (TextView) v.findViewById(R.id.ui_toast_fontcolor_tv);
        toastPositionTv = (TextView) v.findViewById(R.id.ui_toast_position_tv);
        corTv = (TextView) v.findViewById(R.id.ui_round_offset_tv);
        topCorTv = (TextView) v.findViewById(R.id.ui_round_topoffset_tv);
        roundSizeTv = (TextView) v.findViewById(R.id.ui_round_size_tv);
        recentRoundSizeTv = (TextView) v.findViewById(R.id.ui_recentround_size_tv);
        recentAlphaTv = (TextView) v.findViewById(R.id.ui_recentalpha_size_tv);
        toastPositionSb = (SeekBar) v.findViewById(R.id.ui_toast_position_sb);
        corOffsetSb = (SeekBar) v.findViewById(R.id.ui_round_offset_sb);
        corTopOffsetSb = (SeekBar) v.findViewById(R.id.ui_round_topoffset_sb);
        roundSizeSb = (SeekBar) v.findViewById(R.id.ui_round_size_sb);
        recentRoundSizeSb = (SeekBar) v.findViewById(R.id.ui_recentround_size_sb);
        recentAlphaSb = (SeekBar) v.findViewById(R.id.ui_recentalpha_size_sb);
        curColor = blackListTv.getCurrentTextColor();
        recentBarColorSw.setTextColor(curColor);
        recentBarHideSw.setTextColor(curColor);
        recentMemSw.setTextColor(curColor);
        recentInfoSw.setTextColor(curColor);
        topBarSw.setTextColor(curColor);
        toastSw.setTextColor(curColor);
        bottomBarSw.setTextColor(curColor);
        alwaysColorSw.setTextColor(curColor);
        bottomDengTopSw.setTextColor(curColor);
        keyColorSw.setTextColor(curColor);
        roundSw.setTextColor(curColor);
        actInfoSw.setTextColor(curColor);
        ItemClick itemClick = new ItemClick();
        blackListTv.setOnClickListener(itemClick);
        toastGrvityTv.setOnClickListener(itemClick);
        toastFontColorTv.setOnClickListener(itemClick);
        toastBgColorTv.setOnClickListener(itemClick);
        keyColorTv.setOnClickListener(itemClick);
        SwCheckListener swLis = new SwCheckListener();
        topBarSw.setOnCheckedChangeListener(swLis);
        bottomBarSw.setOnCheckedChangeListener(swLis);
        alwaysColorSw.setOnCheckedChangeListener(swLis);
        bottomDengTopSw.setOnCheckedChangeListener(swLis);
        keyColorSw.setOnCheckedChangeListener(swLis);
        toastSw.setOnCheckedChangeListener(swLis);
        roundSw.setOnCheckedChangeListener(swLis);
        recentBarColorSw.setOnCheckedChangeListener(swLis);
        recentBarHideSw.setOnCheckedChangeListener(swLis);
        recentMemSw.setOnCheckedChangeListener(swLis);
        recentInfoSw.setOnCheckedChangeListener(swLis);
        topBarSw.setTag(0);
        bottomBarSw.setTag(1);
        bottomDengTopSw.setTag(2);
        toastSw.setTag(3);
        keyColorSw.setTag(4);
        roundSw.setTag(5);
        recentBarColorSw.setTag(6);
        recentBarHideSw.setTag(7);
        recentMemSw.setTag(8);
        recentInfoSw.setTag(9);
        alwaysColorSw.setTag(10);
//        actInfoSw.setTag(11);
//        activityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        barPrefs = SharedPrefsUtil.getInstance(getActivity()).uiBarPrefs;//;SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_UIBARLIST);//getActivity().getSharedPreferences(Common.PREFS_APPSETTINGS,Context.MODE_WORLD_READABLE);
        recentBarColorSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTBARCOLOR,false));
        recentBarHideSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTBARHIDE,false));
        recentMemSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTMEMSHOW,false));
        recentInfoSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_RECENTIFNO,false));
        topBarSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOPBAR,false));
        toastSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_TOASTCHANGE,false));
        bottomBarSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR,false));
        alwaysColorSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_ALWAYSCOLORBAR,false));
        bottomDengTopSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP,false));
        keyColorSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_KEYCOLOROPEN,false));
        if ((Build.VERSION.SDK_INT >=Build.VERSION_CODES.M&&Settings.canDrawOverlays(getActivity()))||Build.VERSION.SDK_INT <Build.VERSION_CODES.M){
            roundSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_ROUNDOPEN,false));
            startRound(barPrefs,getActivity());
        }else{
            barPrefs.edit().putBoolean(Common.PREFS_SETTING_UI_ROUNDOPEN,false).commit();
            roundSw.setChecked(false);
            actInfoSw.setChecked(false);
        }
        if(!roundSw.isChecked()){
            corOffsetSb.setEnabled(false);
            corOffsetSb.setAlpha(0.5f);
            actInfoSw.setAlpha(0.5f);
            actInfoSw.setEnabled(false);
        }
        toastGrivity = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTGRIVITY, Gravity.BOTTOM);
        toastBgColor = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTBGCOLOR, Color.BLACK);
        toastTextColor = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTTEXTCOLOR, Color.WHITE);
        toastPostion = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTPOSTION, 0);
        toastPositionTv.setText("Toast位置偏移:"+toastPostion);
        toastPositionSb.setProgress(toastPostion);
        int offset = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDOFFSET,RoundedCornerService.getVirtualBarHeigh(getActivity()));
        int topoffset = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDTOPOFFSET,RoundedCornerService.getZhuangTaiHeight(getContext()));
        int roundsize = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDSIZE,35);
        int recentroundsize = barPrefs.getInt(Common.PREFS_SETTING_UI_RECENTBARROUNDNUM,10);
        int recentalpha = barPrefs.getInt(Common.PREFS_SETTING_UI_RECENTBARALPHANUM,100);
        corOffsetSb.setProgress(offset);
        corTopOffsetSb.setProgress(topoffset);
        topCorTv.setText("顶部圆角偏移:"+topoffset);
        corTv.setText("底部圆角偏移:"+offset);
        roundSizeTv.setText("圆角半径:"+roundsize);
        recentRoundSizeTv.setText("最近任务卡片圆角半径:"+recentroundsize);
        recentAlphaTv.setText("最近任务卡片不透明度:"+recentalpha);
        roundSizeSb.setProgress(roundsize);
        recentRoundSizeSb.setProgress(recentroundsize);
        recentAlphaSb.setProgress(recentalpha);
        toastGrvityTv.setText("Toast位置（点击切换）:"+(toastGrivity==Gravity.BOTTOM?"下":(toastGrivity==Gravity.CENTER?"中":"上")));
        SeekBarListener sbl = new SeekBarListener();
        toastPositionSb.setOnSeekBarChangeListener(sbl);
        corOffsetSb.setOnSeekBarChangeListener(sbl);
        corTopOffsetSb.setOnSeekBarChangeListener(sbl);
        roundSizeSb.setOnSeekBarChangeListener(sbl);
        recentRoundSizeSb.setOnSeekBarChangeListener(sbl);
        recentAlphaSb.setOnSeekBarChangeListener(sbl);
        toastPositionSb.setTag(0);
        corOffsetSb.setTag(1);
        roundSizeSb.setTag(2);
        recentRoundSizeSb.setTag(3);
        recentAlphaSb.setTag(4);
        corTopOffsetSb.setTag(5);
        if (!keyColorSw.isChecked()){
            keyColorTv.setEnabled(false);
            keyColorTv.setAlpha(0.4f);
        }
        actInfoSw.setChecked(WatchDogService.isShowActInfo);
        actInfoSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                WatchDogService.isShowActInfo = b;
                Intent intent1 = new Intent("com.click369.control.float.infouishow");
                intent1.putExtra("isShow",b);
                getActivity().sendBroadcast(intent1);
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        actInfoSw.setChecked(WatchDogService.isShowActInfo);
        super.onResume();
    }

    class SwCheckListener implements CompoundButton.OnCheckedChangeListener{
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int index = (Integer) buttonView.getTag();
                String keys[] = {Common.PREFS_SETTING_UI_TOPBAR, Common.PREFS_SETTING_UI_BOTTOMBAR,
                        Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP,  Common.PREFS_SETTING_UI_TOASTCHANGE,
                        Common.PREFS_SETTING_UI_KEYCOLOROPEN,Common.PREFS_SETTING_UI_ROUNDOPEN,
                        Common.PREFS_SETTING_UI_RECENTBARCOLOR, Common.PREFS_SETTING_UI_RECENTBARHIDE,
                        Common.PREFS_SETTING_UI_RECENTMEMSHOW,Common.PREFS_SETTING_UI_RECENTIFNO,
                        Common.PREFS_SETTING_UI_ALWAYSCOLORBAR};
                if (buttonView.equals(keyColorSw)){
                    if (isChecked){
                        keyColorTv.setEnabled(true);
                        keyColorTv.setAlpha(1.0f);
                        if (isChecked != barPrefs.getBoolean(keys[index], false)) {
                            AlertUtil.showAlertMsg(getActivity(), "部分系统开启后可能会导致重启后系统报错（比如小米系统），如果出现异常请关闭。");
                        }
                    }else{
                        keyColorTv.setEnabled(false);
                        keyColorTv.setAlpha(0.4f);
                    }
                }
                if (isChecked != barPrefs.getBoolean(keys[index], false)) {
                    if(index == 5){
                        if(isChecked){
                            if ((Build.VERSION.SDK_INT >=Build.VERSION_CODES.M&&!Settings.canDrawOverlays(getActivity()))) {
                                buttonView.setChecked(false);
                                barPrefs.edit().putBoolean(keys[index],false);
                                AlertUtil.showConfirmAlertMsg(getActivity(), "该功能需要打开允许应用控制器在上层显示选项，是否去打开？", new AlertUtil.InputCallBack() {
                                    @Override
                                    public void backData(String txt, int tag) {
                                        if(tag == 1){
                                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivityForResult(intent, 1);
                                        }
                                    }
                                });
                                return;
                            }else if(Build.VERSION.SDK_INT <Build.VERSION_CODES.M){
                                Toast.makeText(getActivity(),"请确保允许应用控制器在上层显示",Toast.LENGTH_SHORT).show();
                            }
                            corOffsetSb.setEnabled(true);
                            actInfoSw.setEnabled(true);
                            corOffsetSb.setAlpha(1.0f);
                            actInfoSw.setAlpha(1.0f);
                        }else{
                            corOffsetSb.setEnabled(false);
                            actInfoSw.setEnabled(false);
                            corOffsetSb.setAlpha(0.5f);
                            actInfoSw.setAlpha(0.5f);
                        }
                        barPrefs.edit().putBoolean(keys[index], isChecked).commit();
                        startRound(barPrefs,getActivity());
                    }else if(index == 6||index == 7||index == 8||index == 9){
                        if (index== 6 &&isChecked&&recentBarHideSw.isChecked()){
                            recentBarHideSw.setChecked(false);
                            barPrefs.edit().putBoolean(Common.PREFS_SETTING_UI_RECENTBARHIDE, false).commit();
                        }else if(index== 7 &&isChecked&&recentBarColorSw.isChecked()){
                            recentBarColorSw.setChecked(false);
                            barPrefs.edit().putBoolean(Common.PREFS_SETTING_UI_RECENTBARCOLOR, false).commit();
                        }
                        if(isChecked){
                            AlertUtil.showAlertMsg(getActivity(),"重启系统界面或重启手机生效");
                        }
                    }else{
                        XposedToast.makeToast(getContext(), "重启手机或杀死当前运行的其他应用后生效", Toast.LENGTH_LONG,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
                    }
                }
                barPrefs.edit().putBoolean(keys[index], isChecked).commit();
            }else{
                buttonView.setChecked(!isChecked);
                XposedToast.makeToast(getContext(), "支持安卓5.0以上", Toast.LENGTH_LONG,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
            }
        }
    }

    public static void startRound(SharedPreferences barPrefs,Context cxt){
//        if(Build.VERSION.SDK_INT <Build.VERSION_CODES.M){
//            return;
//        }
        if((Build.VERSION.SDK_INT >=Build.VERSION_CODES.M&&Settings.canDrawOverlays(cxt))||Build.VERSION.SDK_INT <Build.VERSION_CODES.M){
        }else{
            return;
        }
        Intent intent = new Intent(cxt, RoundedCornerService.class);

        if(barPrefs.getBoolean(Common.PREFS_SETTING_UI_ROUNDOPEN,false)&&
                SharedPrefsUtil.getPreferences(cxt,Common.PREFS_APPSETTINGS).getBoolean(Common.ALLSWITCH_EIGHT,true)){
            cxt.startService(intent);
        }else{
            cxt.stopService(intent);
        }
    }

    class ItemClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(v.equals(blackListTv)){
                   Intent intent = new Intent(getActivity(),UIBarBlackListActivity.class);
                    startActivity(intent);
                }else if(v.equals(toastGrvityTv)){
                    String s = "下";
                    int gri = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTGRIVITY, Gravity.BOTTOM);
                    if(gri == Gravity.BOTTOM){
                        barPrefs.edit().putInt(Common.PREFS_SETTING_UI_TOASTGRIVITY,Gravity.CENTER).commit();
                        s = "中";
                    }else if(gri == Gravity.CENTER){
                        barPrefs.edit().putInt(Common.PREFS_SETTING_UI_TOASTGRIVITY,Gravity.TOP).commit();
                        s = "上";
                    }else if(gri == Gravity.TOP){
                        barPrefs.edit().putInt(Common.PREFS_SETTING_UI_TOASTGRIVITY,Gravity.BOTTOM).commit();
                        s = "下";
                    }
                    toastGrivity = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTGRIVITY,Gravity.BOTTOM);
                    toastGrvityTv.setText("Toast位置（点击切换）:"+s);
                    XposedToast.makeToast(getContext(), "重启手机生效", Toast.LENGTH_SHORT,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
                }else if(v.equals(toastFontColorTv)){
                    Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                    intent.putExtra("data","Toast字体颜色");
                    intent.putExtra("key",Common.PREFS_SETTING_UI_TOASTTEXTCOLOR);
                    startActivity(intent);
                }else if(v.equals(toastBgColorTv)){
                    Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                    intent.putExtra("data","Toast背景颜色");
                    intent.putExtra("key",Common.PREFS_SETTING_UI_TOASTBGCOLOR);
                    startActivity(intent);
                }else if(v.equals(keyColorTv)){
                    Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                    intent.putExtra("data","虚拟键颜色");
                    intent.putExtra("key",Common.PREFS_SETTING_UI_KEYCOLOR);
                    startActivity(intent);
                }
            }else{
                XposedToast.makeToast(getContext(), "支持安卓5.0以上", Toast.LENGTH_LONG,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
            }
        }
    }

    Handler h = new Handler();
    Runnable r = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent("com.click369.control.corchangeposition");
            getActivity().sendBroadcast(intent);
        }
    };
    class SeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int tag= (Integer) seekBar.getTag();
            String names[] = {"Toast位置偏移:","底部圆角偏移:","圆角半径:","最近任务卡片圆角半径:","最近任务卡片不透明度:","顶部圆角偏移:"};
            if (tag == 1||tag == 2||tag == 5){
                h.removeCallbacks(r);
                h.postDelayed(r,100);
                if(tag == 1){
                    corTv.setText(names[tag]+seekBar.getProgress());
                }else if(tag == 5){
                    topCorTv.setText(names[tag]+seekBar.getProgress());
                }else{
                    roundSizeTv.setText(names[tag]+seekBar.getProgress());
                }
            }else if (tag==0){
                toastPositionTv.setText(names[tag]+seekBar.getProgress());
            }else if (tag==3){
                recentRoundSizeTv.setText(names[tag]+seekBar.getProgress());
            }else if (tag==4){
                recentAlphaTv.setText(names[tag]+seekBar.getProgress());
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int tag= (Integer) seekBar.getTag();
            String keys[] = {Common.PREFS_SETTING_UI_TOASTPOSTION,Common.PREFS_SETTING_UI_ROUNDOFFSET,Common.PREFS_SETTING_UI_ROUNDSIZE,Common.PREFS_SETTING_UI_RECENTBARROUNDNUM,Common.PREFS_SETTING_UI_RECENTBARALPHANUM,Common.PREFS_SETTING_UI_ROUNDTOPOFFSET};
            barPrefs.edit().putInt(keys[tag],seekBar.getProgress()).commit();
            if(tag == 0){
                toastPostion = seekBar.getProgress();
                XposedToast.makeToast(getContext(),"重启生效",Toast.LENGTH_SHORT,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
            }else if(tag == 3||tag == 4){
                XposedToast.makeToast(getContext(),"重启系统或重启系统界面生效",Toast.LENGTH_SHORT,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
            }
        }
    }
}

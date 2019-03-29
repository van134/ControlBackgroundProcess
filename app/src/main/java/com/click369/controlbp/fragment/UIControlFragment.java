package com.click369.controlbp.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.ChangePhotoActivity;
import com.click369.controlbp.activity.ColorSetActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.UIBarBlackListActivity;
import com.click369.controlbp.activity.UnLockActivity;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.LightView;
import com.click369.controlbp.service.RoundedCornerService;
import com.click369.controlbp.service.ScreenLightServiceUtil;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedToast;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.Notify;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.view.WaveProgressView;

import net.qiujuer.genius.blur.StackBlur;

import java.io.File;
import java.io.FileOutputStream;


public class UIControlFragment extends BaseFragment {
    private  MainActivity mainActivity = null;
    private TextView blackListTv,toastGrvityTv,toastBgColorTv,
            toastFontColorTv,keyColorTv,toastPositionTv,
            corTv,topCorTv,roundSizeTv,recentRoundSizeTv,
            recentAlphaTv,lightSizeTv,lightWidthTv,lightOffsetTv,
            lightSpeedTv,lightWeiZhiTv,lightColorTv,lightXiaoGuoTv,
            notifyAlphaTv,notifySetColorTv,changeThemeTv,changeBgTv;
    private String lightSpeedNames[] = {"快速","中速","慢速"};
    private String lightWeiZhiNames[] = {"左右","上下","上下左右"};
    private String lightXiaoGuoNames[] = {"小弧度","直线","大弧度"};
    private SeekBar toastPositionSb,corOffsetSb,corTopOffsetSb,
            roundSizeSb,recentRoundSizeSb,recentAlphaSb,lightSizeSb,lightWidthSb,
            lightOffsetSb,notifyAlphaSb;
    private Switch topBarSw,bottomBarSw,alwaysColorSw,bottomDengTopSw,
            toastSw,keyColorSw,roundSw,recentBarColorSw,recentBarHideSw,
            recentMemSw,recentInfoSw,actInfoSw,lightMsgSw,lightCallSw,
            lightModeSw,lightScOnSw,lightMusicSw,lightChargeSw,lightScaleSw,
            flashNotifySw,flashCallSw,floatOnSysSw,notifyColorSw,
            notifyRandomColorSw,notifyImgFileSw,autoNightSw,nightModeSw,flashOffScSw;
    private FrameLayout lightColorFl,lightWidthFl,notifyColorFl;
    private int curColor = Color.BLACK;
    private int toastBgColor = Color.BLACK,toastTextColor = Color.WHITE,
            toastGrivity,toastPostion = 0;
    private int lightSize = 8,lightWidth = 100,lightOffset = 0,lightSpeed = 1,lightWeiZhi=0,lightXiaoGuo = 0,notifyAlpha=100;

    private SharedPreferences barPrefs;
    public UIControlFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity)getActivity();
        View v = inflater.inflate(R.layout.fragment_uicontrol, container, false);
        lightColorFl = (FrameLayout) v.findViewById(R.id.ui_light_color_fl);
        lightWidthFl = (FrameLayout) v.findViewById(R.id.ui_light_width_fl);
        notifyColorFl = (FrameLayout) v.findViewById(R.id.ui_notify_setcolor_fl);
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
        lightMsgSw = (Switch) v.findViewById(R.id.ui_light_msg_sw);
        lightCallSw = (Switch) v.findViewById(R.id.ui_light_call_sw);
        lightScOnSw = (Switch) v.findViewById(R.id.ui_light_scon_sw);
        lightModeSw = (Switch) v.findViewById(R.id.ui_light_mode_sw);
        lightMusicSw = (Switch) v.findViewById(R.id.ui_light_music_sw);
        lightChargeSw = (Switch) v.findViewById(R.id.ui_light_charge_sw);
        lightScaleSw = (Switch) v.findViewById(R.id.ui_light_animscale_sw);
        flashNotifySw = (Switch) v.findViewById(R.id.ui_flash_notify_sw);
        flashCallSw = (Switch) v.findViewById(R.id.ui_flash_call_sw);
        floatOnSysSw = (Switch) v.findViewById(R.id.ui_light_floatonsys_sw);
        notifyColorSw = (Switch) v.findViewById(R.id.ui_notify_color_sw);
        notifyRandomColorSw = (Switch) v.findViewById(R.id.ui_notify_colorrandom_sw);
        notifyImgFileSw = (Switch) v.findViewById(R.id.ui_notify_imgbg_sw);
        autoNightSw = (Switch) v.findViewById(R.id.ui_auto_night_sw);
        nightModeSw = (Switch) v.findViewById(R.id.ui_nightmode_sw);
        flashOffScSw = (Switch) v.findViewById(R.id.ui_flash_offsc_sw);

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
        lightSizeTv = (TextView) v.findViewById(R.id.ui_light_size_tv);
        lightWidthTv = (TextView) v.findViewById(R.id.ui_light_width_tv);
        lightOffsetTv = (TextView) v.findViewById(R.id.ui_light_offset_tv);
        lightSpeedTv = (TextView) v.findViewById(R.id.ui_light_speed_tv);
        lightWeiZhiTv = (TextView) v.findViewById(R.id.ui_light_weizhi_tv);
        lightColorTv = (TextView) v.findViewById(R.id.ui_light_color_tv);
        lightXiaoGuoTv = (TextView) v.findViewById(R.id.ui_light_xiaoguo_tv);
        notifySetColorTv = (TextView) v.findViewById(R.id.ui_notify_setcolor_tv);
        notifyAlphaTv = (TextView) v.findViewById(R.id.ui_notify_alpha_tv);
        changeThemeTv = (TextView) v.findViewById(R.id.ui_chang_theme_color_tv);
        changeBgTv = (TextView) v.findViewById(R.id.ui_changbg_tv);

        toastPositionSb = (SeekBar) v.findViewById(R.id.ui_toast_position_sb);
        corOffsetSb = (SeekBar) v.findViewById(R.id.ui_round_offset_sb);
        corTopOffsetSb = (SeekBar) v.findViewById(R.id.ui_round_topoffset_sb);
        roundSizeSb = (SeekBar) v.findViewById(R.id.ui_round_size_sb);
        recentRoundSizeSb = (SeekBar) v.findViewById(R.id.ui_recentround_size_sb);
        recentAlphaSb = (SeekBar) v.findViewById(R.id.ui_recentalpha_size_sb);
        lightSizeSb = (SeekBar) v.findViewById(R.id.ui_light_size_sb);
        lightWidthSb = (SeekBar) v.findViewById(R.id.ui_light_width_sb);
        lightOffsetSb = (SeekBar) v.findViewById(R.id.ui_light_offset_sb);
        notifyAlphaSb = (SeekBar) v.findViewById(R.id.ui_notify_alpha_sb);

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
        lightMsgSw.setTextColor(curColor);
        lightCallSw.setTextColor(curColor);
        lightModeSw.setTextColor(curColor);
        lightScOnSw.setTextColor(curColor);
        lightMusicSw.setTextColor(curColor);
        lightChargeSw.setTextColor(curColor);
        lightScaleSw.setTextColor(curColor);
        flashNotifySw.setTextColor(curColor);
        flashCallSw.setTextColor(curColor);
        floatOnSysSw.setTextColor(curColor);
        notifyColorSw.setTextColor(curColor);
        notifyImgFileSw.setTextColor(curColor);
        notifyRandomColorSw.setTextColor(curColor);
        autoNightSw.setTextColor(curColor);
        nightModeSw.setTextColor(curColor);
        flashOffScSw.setTextColor(curColor);
        ItemClick itemClick = new ItemClick();
        blackListTv.setOnClickListener(itemClick);
        toastGrvityTv.setOnClickListener(itemClick);
        toastFontColorTv.setOnClickListener(itemClick);
        toastBgColorTv.setOnClickListener(itemClick);
        keyColorTv.setOnClickListener(itemClick);
        lightSpeedTv.setOnClickListener(itemClick);
        lightWeiZhiTv.setOnClickListener(itemClick);
        lightColorTv.setOnClickListener(itemClick);
        lightXiaoGuoTv.setOnClickListener(itemClick);
        notifySetColorTv.setOnClickListener(itemClick);
        changeThemeTv.setOnClickListener(itemClick);
        changeBgTv.setOnClickListener(itemClick);
//        changeThemeTv.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                barPrefs.edit().putString(Common.PREFS_SETTING_UI_THEME_COLOR,"#1a9dac").commit();
//                barPrefs.edit().putString(Common.PREFS_SETTING_UI_THEME_TEXT_COLOR,"#1a9dac").commit();
//                MainActivity.THEME_COLOR = "#1a9dac";
//                MainActivity.THEME_TEXT_COLOR = "#1a9dac";
//                ((MainActivity)getActivity()).initThemeColor();
//                return true;
//            }
//        });

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
        lightMsgSw.setTag(11);
        lightCallSw.setTag(12);
        lightModeSw.setTag(13);
        lightScOnSw.setTag(14);
        lightMusicSw.setTag(15);
        lightChargeSw.setTag(16);
        lightScaleSw.setTag(17);
        flashNotifySw.setTag(18);
        flashCallSw.setTag(19);
        floatOnSysSw.setTag(20);
        notifyColorSw.setTag(21);
        notifyRandomColorSw.setTag(22);
        notifyImgFileSw.setTag(23);
        autoNightSw.setTag(24);
        nightModeSw.setTag(25);
        flashOffScSw.setTag(26);
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
        lightMsgSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTMSG,false));
        lightCallSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTCALL,false));
        lightModeSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTMODE,false));
        lightScOnSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTSCON,false));
        lightMusicSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTMUSIC,false));
        lightChargeSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTCHARGE,false));
        lightScaleSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_LIGHTANIMSCALE,false));
        flashNotifySw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_FLASHNOTIFY,false));
        flashCallSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_FLASHCALL,false));
        floatOnSysSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_ISNEEDFLOATONSYS,false));
        roundSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_ROUNDOPEN,false));
        notifyColorSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_NOTIFY_COLOROPEN,false));
        notifyRandomColorSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_NOTIFY_RANDOMCOLOR,false));
        notifyImgFileSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_NOTIFY_ISUSEIMGFILE,false));
        lightColorFl.setVisibility(lightModeSw.isChecked()?View.GONE:View.VISIBLE);
        notifyColorFl.setVisibility((notifyImgFileSw.isChecked()||notifyRandomColorSw.isChecked())?View.GONE:View.VISIBLE);
        notifyRandomColorSw.setEnabled(notifyImgFileSw.isChecked()?false:true);
        autoNightSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_AUTOCHANGEMODE,false));
        nightModeSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_THEME_MODE,false));
        flashOffScSw.setChecked(barPrefs.getBoolean(Common.PREFS_SETTING_UI_FLASHSHOWINOFFSC,false));

        if(!roundSw.isChecked()){
            roundSizeSb.setEnabled(false);
            corOffsetSb.setEnabled(false);
            corTopOffsetSb.setEnabled(false);
            actInfoSw.setEnabled(false);
            corOffsetSb.setAlpha(0.5f);
            corTopOffsetSb.setAlpha(0.5f);
            roundSizeSb.setAlpha(0.5f);
            actInfoSw.setAlpha(0.5f);
        }
        if (!notifyColorSw.isChecked()){
            notifyRandomColorSw.setEnabled(false);
            notifySetColorTv.setEnabled(false);
            notifyAlphaSb.setEnabled(false);
            notifyImgFileSw.setEnabled(false);
        }

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
        lightMsgSw.setOnCheckedChangeListener(swLis);
        lightCallSw.setOnCheckedChangeListener(swLis);
        lightModeSw.setOnCheckedChangeListener(swLis);
        lightScOnSw.setOnCheckedChangeListener(swLis);
        lightMusicSw.setOnCheckedChangeListener(swLis);
        lightChargeSw.setOnCheckedChangeListener(swLis);
        lightScaleSw.setOnCheckedChangeListener(swLis);
        flashNotifySw.setOnCheckedChangeListener(swLis);
        flashCallSw.setOnCheckedChangeListener(swLis);
        floatOnSysSw.setOnCheckedChangeListener(swLis);
        notifyColorSw.setOnCheckedChangeListener(swLis);
        notifyRandomColorSw.setOnCheckedChangeListener(swLis);
        notifyImgFileSw.setOnCheckedChangeListener(swLis);
        autoNightSw.setOnCheckedChangeListener(swLis);
        nightModeSw.setOnCheckedChangeListener(swLis);
        flashOffScSw.setOnCheckedChangeListener(swLis);



        lightWidth = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTWIDTH, 100);
        lightSize = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTSIZE, 8);
        lightOffset = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTOFFSET, 0);
        lightSpeed = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTSPEED, 1);
        lightWeiZhi = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTWEIZHI, 0);
        lightXiaoGuo = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTXIAOGUO, 0);
        notifyAlpha = barPrefs.getInt(Common.PREFS_SETTING_UI_NOTIFY_ALPHA, 100);
        lightWidthTv.setText("边沿呼吸距离:"+lightWidth+"%");
        lightSizeTv.setText("边沿呼吸粗细:"+lightSize+"%");
        lightOffsetTv.setText("边沿呼吸纵向偏移:"+lightOffset);
        lightSpeedTv.setText("边沿呼吸速度（点击切换）:"+lightSpeedNames[lightSpeed]);
        lightWeiZhiTv.setText("边沿呼吸位置（点击切换）:"+lightWeiZhiNames[lightWeiZhi]);
        lightXiaoGuoTv.setText("边沿呼吸效果（点击切换）:"+lightXiaoGuoNames[lightXiaoGuo]);
        notifyAlphaTv.setText("消息通知背景不透明度:"+notifyAlpha+"%");
        lightWidthFl.setVisibility(lightXiaoGuo==1?View.GONE:View.VISIBLE);


        lightWidthSb.setProgress(lightWidth);
        lightSizeSb.setProgress(lightSize);
        lightOffsetSb.setProgress(lightOffset);
        notifyAlphaSb.setProgress(notifyAlpha);
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
        lightWidthSb.setOnSeekBarChangeListener(sbl);
        lightSizeSb.setOnSeekBarChangeListener(sbl);
        lightOffsetSb.setOnSeekBarChangeListener(sbl);
        notifyAlphaSb.setOnSeekBarChangeListener(sbl);

        toastPositionSb.setTag(0);
        corOffsetSb.setTag(1);
        roundSizeSb.setTag(2);
        recentRoundSizeSb.setTag(3);
        recentAlphaSb.setTag(4);
        corTopOffsetSb.setTag(5);
        lightSizeSb.setTag(6);
        lightWidthSb.setTag(7);
        lightOffsetSb.setTag(8);
        notifyAlphaSb.setTag(9);

        if (!keyColorSw.isChecked()){
            keyColorTv.setEnabled(false);
            keyColorTv.setAlpha(0.4f);
        }
        actInfoSw.setChecked(WatchDogService.isShowActInfo);
        actInfoSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                BaseActivity.zhenDong(getContext());
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
            BaseActivity.zhenDong(getContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int index = (Integer) buttonView.getTag();
                String keys[] = {Common.PREFS_SETTING_UI_TOPBAR, Common.PREFS_SETTING_UI_BOTTOMBAR,
                        Common.PREFS_SETTING_UI_BOTTOM_DENG_TOP,  Common.PREFS_SETTING_UI_TOASTCHANGE,
                        Common.PREFS_SETTING_UI_KEYCOLOROPEN,Common.PREFS_SETTING_UI_ROUNDOPEN,
                        Common.PREFS_SETTING_UI_RECENTBARCOLOR, Common.PREFS_SETTING_UI_RECENTBARHIDE,
                        Common.PREFS_SETTING_UI_RECENTMEMSHOW,Common.PREFS_SETTING_UI_RECENTIFNO,
                        Common.PREFS_SETTING_UI_ALWAYSCOLORBAR,Common.PREFS_SETTING_UI_LIGHTMSG,
                        Common.PREFS_SETTING_UI_LIGHTCALL,Common.PREFS_SETTING_UI_LIGHTMODE,
                        Common.PREFS_SETTING_UI_LIGHTSCON,Common.PREFS_SETTING_UI_LIGHTMUSIC,
                        Common.PREFS_SETTING_UI_LIGHTCHARGE,Common.PREFS_SETTING_UI_LIGHTANIMSCALE,
                        Common.PREFS_SETTING_UI_FLASHNOTIFY,Common.PREFS_SETTING_UI_FLASHCALL,
                        Common.PREFS_SETTING_UI_ISNEEDFLOATONSYS,Common.PREFS_SETTING_UI_NOTIFY_COLOROPEN,
                        Common.PREFS_SETTING_UI_NOTIFY_RANDOMCOLOR,Common.PREFS_SETTING_UI_NOTIFY_ISUSEIMGFILE,
                        Common.PREFS_SETTING_UI_THEME_AUTOCHANGEMODE,Common.PREFS_SETTING_UI_THEME_MODE,Common.PREFS_SETTING_UI_FLASHSHOWINOFFSC};
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
                    if(index == 5){
                        if(isChecked){
                            corOffsetSb.setEnabled(true);
                            roundSizeSb.setEnabled(true);
                            corTopOffsetSb.setEnabled(true);
                            actInfoSw.setEnabled(true);
                            corOffsetSb.setAlpha(1.0f);
                            roundSizeSb.setAlpha(1.0f);
                            corTopOffsetSb.setAlpha(1.0f);
                            actInfoSw.setAlpha(1.0f);
                            Toast.makeText(getActivity(),"如果没有生效，请重启手机再试",Toast.LENGTH_SHORT).show();
                        }else{
                            roundSizeSb.setEnabled(false);
                            corOffsetSb.setEnabled(false);
                            corTopOffsetSb.setEnabled(false);
                            actInfoSw.setEnabled(false);
                            corOffsetSb.setAlpha(0.5f);
                            corTopOffsetSb.setAlpha(0.5f);
                            roundSizeSb.setAlpha(0.5f);
                            actInfoSw.setAlpha(0.5f);
                        }
                        WatchDogService.isRoundCorOpen = isChecked;
//                        barPrefs.edit().putBoolean(keys[index], isChecked).commit();
                        checkAndStartRound(getActivity(),isChecked);
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
                    }else if(index == 11){
                        WatchDogService.isLightMsg = isChecked;
                        if(isChecked){
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                        }else{
                            ScreenLightServiceUtil.sendHideLight(getContext());
                        }
                        checkAndStartRound(getActivity(),isChecked);
                    }else if(index == 12){
                        WatchDogService.isLightCall = isChecked;
                        if(isChecked){
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                        }else{
                            ScreenLightServiceUtil.sendHideLight(getContext());
                        }
                        checkAndStartRound(getActivity(),isChecked);
                    }else if(index == 13){
                        WatchDogService.isLightRandomMode = isChecked;
                        if(lightCallSw.isChecked()||lightMsgSw.isChecked()){
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                        }
                        lightColorFl.setVisibility(lightModeSw.isChecked()?View.GONE:View.VISIBLE);
                        checkAndStartRound(getActivity(),isChecked);
                    }else if(index == 14){
                        WatchDogService.isLightScOn = isChecked;
                        if(isChecked){
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                        }else{
                            ScreenLightServiceUtil.sendHideLight(getContext());
                        }
                        checkAndStartRound(getActivity(),isChecked);
                    }else if(index == 15){
                        WatchDogService.isLightMusic = isChecked;
                        if(isChecked){
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                        }else{
                            ScreenLightServiceUtil.sendHideLight(getContext());
                        }
                        checkAndStartRound(getActivity(),isChecked);
                    }else if(index == 16){
                        WatchDogService.isLightCharge = isChecked;
                        if(isChecked){
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                        }else{
                            ScreenLightServiceUtil.sendHideLight(getContext());
                        }
                        checkAndStartRound(getActivity(),isChecked);
                    }else if(index == 17){
                        WatchDogService.isLightAnimScale = isChecked;
                        if(isChecked){
                            ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                        }else{
                            ScreenLightServiceUtil.sendHideLight(getContext());
                        }
                        checkAndStartRound(getActivity(),isChecked);
                    }else if(index == 18){
                        WatchDogService.isFlashNofity = isChecked;
                    }else if(index == 19){
                        WatchDogService.isFlashCall = isChecked;
                    }else if(index == 20){
                        WatchDogService.isNeedFloatOnSys = isChecked;
                        if(!isChecked&&!WatchDogService.isHasSysFloatVewPermission){
                            WatchDogService.isNeedGetFloatPremission = true;
                        }else{
                            WatchDogService.isNeedGetFloatPremission = false;
                        }
                        if(isChecked){
                            AlertUtil.showAlertMsg(getActivity(),"如果打开后出现无法进入应用控制器就证明系统不支持（白屏或黑屏）出现此问题请到xp框架中去掉应用控制器的勾选并重启手机，再次进入应用控制器后关闭该选项。");
                        }
                        Intent intent = new Intent("com.click369.control.ams.float.checkxp");
                        intent.putExtra("isNeedFloadOnSys",WatchDogService.isNeedFloatOnSys);
                        getActivity().sendBroadcast(intent);
//                        Log.i("CONTROL----1","WatchDogService.isNeedFloatOnSys  "+WatchDogService.isNeedFloatOnSys);
                        checkAndStartRound(getActivity(),true);
                    }else if(index == 21){
                        Intent sysIntent = new Intent("com.click369.control.sysui.loadconfig");
                        sysIntent.putExtra("isnotifycoloropen",isChecked);
                        getActivity().sendBroadcast(sysIntent);
                        if (!notifyColorSw.isChecked()){
                            notifyRandomColorSw.setEnabled(false);
                            notifySetColorTv.setEnabled(false);
                            notifyAlphaSb.setEnabled(false);
                            notifyImgFileSw.setEnabled(false);
                        }else{
                            notifyRandomColorSw.setEnabled(true);
                            notifySetColorTv.setEnabled(true);
                            notifyAlphaSb.setEnabled(true);
                            notifyImgFileSw.setEnabled(true);
                        }
                        Notify.testNotify(getContext());
                    }else if(index == 22){
                        Intent sysIntent = new Intent("com.click369.control.sysui.loadconfig");
                        sysIntent.putExtra("israndomnotifycolor",isChecked);
                        getActivity().sendBroadcast(sysIntent);
                        notifyColorFl.setVisibility((notifyImgFileSw.isChecked()||notifyRandomColorSw.isChecked())?View.GONE:View.VISIBLE);
                        Notify.testNotify(getContext());
                    }else if(index == 23){
                        Intent sysIntent = new Intent("com.click369.control.sysui.loadconfig");
                        sysIntent.putExtra("notifyuseimgfile",isChecked);
                        getActivity().sendBroadcast(sysIntent);
                       if(isChecked){
                           File f = new File(Environment.getExternalStorageDirectory()+File.separator+"processcontrol","nb.jpg");
                           if(!f.exists()){
                               f =  new File(Environment.getExternalStorageDirectory()+File.separator+"processcontrol","nb.png");
                           }
//                           Log.i("CONTROL",f.getAbsolutePath());
                           if(!f.exists()){
                               FileUtil.copyAssets(getActivity(),"nb.png",f.getAbsolutePath());
                           }
                       }
                        notifyColorFl.setVisibility((notifyImgFileSw.isChecked()||notifyRandomColorSw.isChecked())?View.GONE:View.VISIBLE);
                        notifyRandomColorSw.setEnabled(notifyImgFileSw.isChecked()?false:true);
                        Notify.testNotify(getContext());
                    }else if(index == 24){
                        MainActivity.isAutoChange = isChecked;
                    }else if(index == 25){
                        MainActivity.isNightMode = isChecked;
                        AlertUtil.showConfirmAlertMsg(getActivity(), "重启应用生效，是否重启?", new AlertUtil.InputCallBack() {
                            @Override
                            public void backData(String txt, int tag) {
                                if (tag==1){
                                    getActivity().finish();
                                    Intent intent = new Intent(getActivity(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT| Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                    startActivity(intent);
                                }
                            }
                        });
                    }else if(index == 26){
                        WatchDogService.isFlashInOffSc = isChecked;
                    }else{
                        XposedToast.makeToast(getContext(), "重启手机或杀死当前运行的其他应用后生效", Toast.LENGTH_LONG,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
                    }

                barPrefs.edit().putBoolean(keys[index], isChecked).commit();
            }else{
                buttonView.setChecked(!isChecked);
                XposedToast.makeToast(getContext(), "支持安卓5.0以上", Toast.LENGTH_LONG,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        startRound(getActivity());
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean ischeck = false;
    public void checkAndStartRound(Context cxt,boolean isChecked){
        if(WatchDogService.isNeedGetFloatPremission&&isChecked){
            AlertUtil.showConfirmAlertMsg(getActivity(), "该功能需要打开允许应用控制器在上层显示选项，是否去打开？", new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if(tag == 1){
                        ischeck = true;
                        WatchDogService.isNeedGetFloatPremission = false;
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivityForResult(intent, 1);
                    }
                }
            });
        }else if(WatchDogService.isHasXPFloatVewPermission||WatchDogService.isHasSysFloatVewPermission){
            startRound(cxt);
        }
    }

    public static void startRound(Context cxt){
//        if(WatchDogService.isRoundCorOpen||
//                WatchDogService.isLightCall||
//                WatchDogService.isLightScOn||
//                WatchDogService.isLightMusic||
//                WatchDogService.isLightCharge||
//                WatchDogService.isLightMsg){
            Intent intent = new Intent("com.click369.control.float.checkxp");
            cxt.sendBroadcast(intent);
//            Log.i("CONTROL----","WatchDogService.isNeedFloatOnSys  "+WatchDogService.isNeedFloatOnSys);
//        }
    }

    class ItemClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            BaseActivity.zhenDong(getContext());
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
                }else if(v.equals(lightSpeedTv)){
                    int speed = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTSPEED, 1);
                    int save  = 0;
                    if(speed == 0){
                        save = 1;
                    }else if(speed == 1){
                        save = 2;
                    }else if(speed == 2){
                        save = 0;
                    }
                    lightSpeed = save;
                    WatchDogService.lightSpeed = save;
                    barPrefs.edit().putInt(Common.PREFS_SETTING_UI_LIGHTSPEED,save).commit();
                    lightSpeedTv.setText("边沿呼吸速度（点击切换）:"+lightSpeedNames[save]);
                    ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                }else if(v.equals(lightWeiZhiTv)){
                    int weizhi = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTWEIZHI, 0);
                    int save  = 0;
                    if(weizhi == 0){
                        save = 1;
                    }else if(weizhi == 1){
                        save = 2;
                    }else if(weizhi == 2){
                        save = 0;
                    }
                    lightWeiZhi = save;
                    WatchDogService.lightShowMode = save;
                    barPrefs.edit().putInt(Common.PREFS_SETTING_UI_LIGHTWEIZHI,save).commit();
                    lightWeiZhiTv.setText("边沿呼吸位置（点击切换）:"+lightWeiZhiNames[save]);
                    ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                }else if(v.equals(lightXiaoGuoTv)){
                    int xiaoguo = barPrefs.getInt(Common.PREFS_SETTING_UI_LIGHTXIAOGUO, 0);
                    int save  = 0;
                    if(xiaoguo == 0){
                        save = 1;
                    }else if(xiaoguo == 1){
                        save = 2;
                    }else if(xiaoguo == 2){
                        save = 0;
                    }
                    lightXiaoGuo = save;
                    WatchDogService.lightXiaoGuo = save;
                    barPrefs.edit().putInt(Common.PREFS_SETTING_UI_LIGHTXIAOGUO,save).commit();
                    lightXiaoGuoTv.setText("边沿呼吸效果（点击切换）:"+lightXiaoGuoNames[save]);
                    lightWidthFl.setVisibility(lightXiaoGuo==1?View.GONE:View.VISIBLE);
                    ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
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
                }else if(v.equals(lightColorTv)){
                    Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                    intent.putExtra("data","边缘呼吸颜色");
                    intent.putExtra("key",Common.PREFS_SETTING_UI_LIGHTCOLOR);
                    startActivity(intent);
                }else if(v.equals(notifySetColorTv)){
                    Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                    intent.putExtra("data","消息通知颜色");
                    intent.putExtra("key",Common.PREFS_SETTING_UI_NOTIFY_SETCOLOR);
                    startActivity(intent);
                }else if(v.equals(changeThemeTv)){
                    AlertUtil.showListAlert(mainActivity, "请选择", new String[]{"自定义标题栏/侧栏主题色","恢复标题栏/侧栏默认主题色","自定义列表背景主题色","恢复列表默认主题色","自定义应用锁背景主题色","恢复应用锁默认主题色","自定义应用锁动画波浪颜色","恢复应用锁动画波浪颜色"}, new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if (tag == 0){
                                Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                                intent.putExtra("data","应用控制器标题栏/侧栏主题色");
                                intent.putExtra("key",Common.PREFS_SETTING_UI_THEME_COLOR);
                                startActivityForResult(intent,0x12);
                            }else if(tag == 1){
                                barPrefs.edit().putString(Common.PREFS_SETTING_UI_THEME_COLOR,"#1a9dac").commit();
                                barPrefs.edit().putString(Common.PREFS_SETTING_UI_THEME_TEXT_COLOR,"#1a9dac").commit();
                                MainActivity.THEME_COLOR = "#1a9dac";
                                MainActivity.THEME_TEXT_COLOR = "#1a9dac";
                                ((MainActivity)getActivity()).initThemeColor();
                            }else if(tag == 2){
                                Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                                intent.putExtra("data","应用控制器列表背景主题色");
                                intent.putExtra("key",Common.PREFS_SETTING_UI_THEME_BG_COLOR);
                                startActivityForResult(intent,0x12);
                            }else if(tag == 3){
                                barPrefs.edit().putString(Common.PREFS_SETTING_UI_THEME_BG_COLOR,"#f4f4f4").commit();
                                MainActivity.THEME_BG_COLOR = "#f4f4f4";
                                ((MainActivity)getActivity()).setBgColor(Color.parseColor(MainActivity.THEME_BG_COLOR));
                            }else if(tag == 4){
                                Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                                intent.putExtra("data","应用锁背景主题色");
                                intent.putExtra("key",Common.PREFS_SETTING_UI_THEME_UNLOCK_BG_COLOR);
                                startActivityForResult(intent,0x12);
                            }else if(tag == 5){
                                barPrefs.edit().putString(Common.PREFS_SETTING_UI_THEME_UNLOCK_BG_COLOR,MainActivity.THEME_COLOR).commit();
                            }else if(tag == 6){
                                Intent intent = new Intent(getActivity(),ColorSetActivity.class);
                                intent.putExtra("data","应用锁背波浪动画颜色");
                                intent.putExtra("key",Common.PREFS_SETTING_UI_THEME_UNLOCK_ANIM_COLOR);
                                startActivityForResult(intent,0x12);
                            }else if(tag == 7){
                                barPrefs.edit().putString(Common.PREFS_SETTING_UI_THEME_UNLOCK_ANIM_COLOR, "#88eeeeee").commit();
                            }
                        }
                    });
                }else if(v.equals(changeBgTv)){
                    mainActivity.getPhoto.setIsNeedCrop(true);
                    boolean isOk = true;
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                        String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                        if(!PermissionUtils.checkPermissionAllGranted(mainActivity,permissions)){
                            isOk = false;
                        }
                    }
                    if(isOk){
                        AlertUtil.showListAlert(mainActivity, "请选择", new String[]{"选择列表背景图片","处理列表背景图片","清除列表背景图片","","选择侧栏背景图片","处理侧栏背景图片","清除侧栏背景图片"}, new AlertUtil.InputCallBack() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void backData(String txt, int tag) {
                                if (tag == 0){
                                    mainActivity.getPhoto.setPhotofile(mainActivity.bgFile);
                                    mainActivity.getPhoto.setScale(0);
                                    mainActivity.getPhoto.photoWithGrelly();
                                }else if(tag == 2){
                                    if(mainActivity.bgFile.exists()){
                                        mainActivity.bgFile.delete();
                                    }
                                    if(mainActivity.bgBlurFile.exists()){
                                        mainActivity.bgBlurFile.delete();
                                    }
                                    barPrefs.edit().remove(Common.PREFS_SETTING_UI_BGBLUR).remove(Common.PREFS_SETTING_UI_BGBRIGHT).commit();
                                    mainActivity.mainRL.setBackground(null);
                                    mainActivity.mainRL.setBackgroundColor(MainActivity.isNightMode?Color.BLACK:Color.parseColor(MainActivity.THEME_BG_COLOR));
                                }else if (tag == 4){
                                    mainActivity.getPhoto.setPhotofile(mainActivity.sideBgFile);
                                    mainActivity.getPhoto.setScale(1);
                                    mainActivity.getPhoto.photoWithGrelly();
                                }else if(tag == 6){
                                    if(mainActivity.sideBgFile.exists()){
                                        mainActivity.sideBgFile.delete();
                                    }
                                    if(mainActivity.sideBgBlurFile.exists()){
                                        mainActivity.sideBgBlurFile.delete();
                                    }
                                    barPrefs.edit().remove(Common.PREFS_SETTING_UI_SIDEBGBLUR).remove(Common.PREFS_SETTING_UI_SIDEBGBRIGHT).commit();
                                    mainActivity.initThemeColor();
                                }else if(tag ==1||tag == 5){
                                    String key_blur = null;
                                    String key_bright = null;
                                    String key_fileName = null;
                                    int blur = 0;
                                    int bright = 100;
                                    if (tag == 1){
                                        if(!mainActivity.bgFile.exists()){
                                            Toast.makeText(getActivity(),"背景图片不存在，请选择后再处理",Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        key_blur = Common.PREFS_SETTING_UI_BGBLUR;
                                        key_bright = Common.PREFS_SETTING_UI_BGBRIGHT;
                                        key_fileName = FileUtil.IMAGEPATH+File.separator+"bg";
                                    }else if(tag == 5){
                                        if(!mainActivity.sideBgFile.exists()){
                                            Toast.makeText(getActivity(),"背景图片不存在，请选择后再处理",Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        key_blur = Common.PREFS_SETTING_UI_SIDEBGBLUR;
                                        key_bright = Common.PREFS_SETTING_UI_SIDEBGBRIGHT;
                                        key_fileName = FileUtil.IMAGEPATH+File.separator+"side";
                                    }
                                    blur = barPrefs.getInt(key_blur,0);
                                    bright = barPrefs.getInt(key_bright,100);
                                    Log.i("CONTROL","blur "+blur+"  bright "+bright);
                                    Log.i("CONTROL","key_blur "+key_blur+"  key_bright "+key_bright);
                                    Intent intent = new Intent(getActivity(), ChangePhotoActivity.class);
                                    intent.putExtra("blur",blur);
                                    intent.putExtra("bright",bright);
                                    intent.putExtra("key_blur",key_blur);
                                    intent.putExtra("key_bright",key_bright);
                                    intent.putExtra("key_fileName",key_fileName);
                                    startActivityForResult(intent,0x15);
                                }
                            }
                        });
                    }else{
                        Toast.makeText(mainActivity,"没有文件读写权限",Toast.LENGTH_LONG).show();
                    }
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
            String names[] = {"Toast位置偏移:","底部圆角偏移:","圆角半径:","最近任务卡片圆角半径:",
                    "最近任务卡片不透明度:","顶部圆角偏移:","边沿呼吸粗细:","边沿呼吸距离:",
                    "边沿呼吸纵向偏移:","消息通知背景不透明度:","应用控制器背景图片模糊度:","应用控制器背景图片亮度:"};
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
            }else if (tag==6){
                lightSizeTv.setText(names[tag]+seekBar.getProgress()+"%");
            }else if (tag==7){
                lightWidthTv.setText(names[tag]+seekBar.getProgress()+"%");
            }else if (tag==8){
                lightOffsetTv.setText(names[tag]+seekBar.getProgress());
            }else if (tag==9){
                notifyAlphaTv.setText(names[tag]+seekBar.getProgress()+"%");
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int tag= (Integer) seekBar.getTag();
            String keys[] = {Common.PREFS_SETTING_UI_TOASTPOSTION,Common.PREFS_SETTING_UI_ROUNDOFFSET,
                    Common.PREFS_SETTING_UI_ROUNDSIZE,Common.PREFS_SETTING_UI_RECENTBARROUNDNUM,
                    Common.PREFS_SETTING_UI_RECENTBARALPHANUM,Common.PREFS_SETTING_UI_ROUNDTOPOFFSET,
            Common.PREFS_SETTING_UI_LIGHTSIZE,Common.PREFS_SETTING_UI_LIGHTWIDTH,
                    Common.PREFS_SETTING_UI_LIGHTOFFSET,Common.PREFS_SETTING_UI_NOTIFY_ALPHA,
                    Common.PREFS_SETTING_UI_BGBLUR,Common.PREFS_SETTING_UI_BGBRIGHT};
            barPrefs.edit().putInt(keys[tag],seekBar.getProgress()).commit();
            BaseActivity.zhenDong(getContext());
            if(tag == 0){
                toastPostion = seekBar.getProgress();
                XposedToast.makeToast(getContext(),"重启生效",Toast.LENGTH_SHORT,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
            }else if(tag == 3||tag == 4){
                XposedToast.makeToast(getContext(),"重启系统或重启系统界面生效",Toast.LENGTH_SHORT,toastGrivity,toastBgColor,toastTextColor,toastPostion).show();
            }else if(tag == 6||tag == 7||tag == 8){
                if(tag==6){
                    WatchDogService.lightSize = seekBar.getProgress();
                    ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                }else if(tag==7){
                    WatchDogService.lightWidth = seekBar.getProgress();
                    ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,getContext());
                }else if(tag==8){
                    WatchDogService.lightOffset = seekBar.getProgress();
                    ScreenLightServiceUtil.sendReloadLight(getContext());
                }
            }else if(tag == 9){
                Intent sysIntent = new Intent("com.click369.control.sysui.loadconfig");
                sysIntent.putExtra("notifyalpha",seekBar.getProgress());
                getActivity().sendBroadcast(sysIntent);
                Notify.testNotify(getContext());
            }
        }
    }
}

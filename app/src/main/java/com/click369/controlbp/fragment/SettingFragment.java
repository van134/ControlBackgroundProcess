package com.click369.controlbp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.FunctionSwitchView;
import com.click369.controlbp.activity.LinkAppSwitchView;
import com.click369.controlbp.activity.LockAppSwitchView;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.NewAppSwitchView;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;


public class SettingFragment extends BaseFragment {
    private Switch iceRemoveSw,iceOffSw,iceStopSw,isShowSideBarSw,//autoNightSw,nightModeSw,
            isOpenConfigSw,isAlwaysKillOffSw,
            updateTimeSw,autoStartNotNofifySw,
            notExitAudioPlaySw,checkTimeOutSw,
            setTimeStopPwdModeSw,setTimeStopZWModeSw,setTimeStopNotShowDialogSw,
            stopAppModeSw,exitRemoveRecentSw,muBeiStopReceiverSw,
            notneedAccessSw,zhendongSw,backSw,notCleanDataSw;//,offSw,autoStartSw,newAppAddRemoveRecentExit,,recentRemoveSw
    private TextView backDelayTimeTv,homeDelayTimeTv,offDelayTimeTv;
    private SeekBar backSb,homeSb,offSb;
    private SharedPreferences settings;
    private int curColor = Color.BLACK;
    private FunctionSwitchView funcitonSWView;
    private NewAppSwitchView newAppSwitchView;
    private LockAppSwitchView lockAppSwitchView;
    private LinkAppSwitchView linkAppSwitchView;
    private ImageView penImg1,penImg2,penImg3;

    TextView tvs[] = null;
    public SettingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        settings = SharedPrefsUtil.getInstance(getActivity()).settings;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_APPSETTINGS);//  getActivity().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        View v = inflater.inflate(R.layout.fragment_setting, container, false);
        funcitonSWView = new FunctionSwitchView(settings,v,getActivity());
        funcitonSWView.initView();
        newAppSwitchView = new NewAppSwitchView(settings,v,getActivity());
        newAppSwitchView.initView();
        lockAppSwitchView = new LockAppSwitchView(settings,v,getActivity());
        lockAppSwitchView.initView();
        linkAppSwitchView = new LinkAppSwitchView(settings,v,getActivity());
        linkAppSwitchView.initView();
        curColor = ((TextView)v.findViewById(R.id.setting_title1_tv)).getCurrentTextColor();
        isShowSideBarSw = (Switch) v.findViewById(R.id.setting_showsidebar_sw);
        backSw = (Switch) v.findViewById(R.id.setting_backkillself_sw);
        iceStopSw = (Switch) v.findViewById(R.id.setting_ice_stop_sw);
        notCleanDataSw = (Switch) v.findViewById(R.id.setting_notunstall_notclear_sw);
        setTimeStopZWModeSw = (Switch) v.findViewById(R.id.setting_settimestopzwmode_sw);
        setTimeStopPwdModeSw = (Switch) v.findViewById(R.id.setting_settimestoppwdmode_sw);
        setTimeStopNotShowDialogSw = (Switch) v.findViewById(R.id.setting_settimestopnotshowdialog_sw);
        notExitAudioPlaySw = (Switch) v.findViewById(R.id.setting_notexitaudio_sw);
        checkTimeOutSw = (Switch) v.findViewById(R.id.setting_checktimeout_sw);
        iceRemoveSw = (Switch) v.findViewById(R.id.setting_ice_remove_sw);
        iceOffSw = (Switch) v.findViewById(R.id.setting_ice_off_sw);

        updateTimeSw = (Switch) v.findViewById(R.id.setting_updatetime_sw);
        isOpenConfigSw = (Switch) v.findViewById(R.id.setting_openconfig_sw);
        isAlwaysKillOffSw = (Switch) v.findViewById(R.id.setting_alwayskilloff_sw);
        autoStartNotNofifySw = (Switch) v.findViewById(R.id.setting_autostartnotnotify_sw);

        stopAppModeSw = (Switch) v.findViewById(R.id.setting_stopmode_sw);
        muBeiStopReceiverSw = (Switch) v.findViewById(R.id.setting_mubestopreceiver_sw);
        exitRemoveRecentSw = (Switch) v.findViewById(R.id.setting_exitremoverecent_sw);
        notneedAccessSw = (Switch) v.findViewById(R.id.setting_isnotneedaccess_sw);
        zhendongSw = (Switch) v.findViewById(R.id.setting_zhendong_sw);
        setTimeStopZWModeSw.setTextColor(curColor);
        setTimeStopPwdModeSw.setTextColor(curColor);
        setTimeStopNotShowDialogSw.setTextColor(curColor);
        backSw.setTextColor(curColor);
        isShowSideBarSw.setTextColor(curColor);
        iceRemoveSw.setTextColor(curColor);
        iceOffSw.setTextColor(curColor);
        notCleanDataSw.setTextColor(curColor);
//        nightModeSw.setTextColor(curColor);
        iceStopSw.setTextColor(curColor);
        stopAppModeSw.setTextColor(curColor);
        notExitAudioPlaySw.setTextColor(curColor);
        muBeiStopReceiverSw.setTextColor(curColor);
        exitRemoveRecentSw.setTextColor(curColor);
        zhendongSw.setTextColor(curColor);
        checkTimeOutSw.setTextColor(curColor);
        notneedAccessSw.setTextColor(curColor);
        updateTimeSw.setTextColor(curColor);
        isOpenConfigSw.setTextColor(curColor);
        isAlwaysKillOffSw.setTextColor(curColor);
        autoStartNotNofifySw.setTextColor(curColor);
        penImg1 = (ImageView) v.findViewById(R.id.setting_pen1_iv);
        penImg2 = (ImageView) v.findViewById(R.id.setting_pen2_iv);
        penImg3 = (ImageView) v.findViewById(R.id.setting_pen3_iv);
        penImg1.setTag(0);
        penImg2.setTag(1);
        penImg3.setTag(2);
        PenClickListener penClick = new PenClickListener();
        penImg1.setOnClickListener(penClick);
        penImg2.setOnClickListener(penClick);
        penImg3.setOnClickListener(penClick);
        backDelayTimeTv = (TextView) v.findViewById(R.id.setting_backdelay_tv);
        homeDelayTimeTv = (TextView) v.findViewById(R.id.setting_homedelay_tv);
        offDelayTimeTv = (TextView) v.findViewById(R.id.setting_offdelay_tv);
        backSb = (SeekBar) v.findViewById(R.id.setting_backdelay_sb);
        homeSb = (SeekBar) v.findViewById(R.id.setting_homedelay_sb);
        offSb = (SeekBar) v.findViewById(R.id.setting_offdelay_sb);
        isShowSideBarSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_SHOWSIDEBAR,true));
        backSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_BACKKILLSELF,false));
        notExitAudioPlaySw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISNOTEXITAUDIOPLAY,false));
        iceStopSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ICESTOPICE,false));
        iceRemoveSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ICEBACKICE,true));
        iceOffSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ICEOFFICE,false));
        notCleanDataSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_NOTUNSTALLNOTCLEAN,true));

        setTimeStopZWModeSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_SETTIMESTOPMODE,false));
        setTimeStopPwdModeSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_SETTIMESTOPPWDMODE,false));
        setTimeStopNotShowDialogSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_SETTIMESTOPNOTSHOWDIALOG,false));
        setTimeStopZWModeSw.setEnabled(!setTimeStopNotShowDialogSw.isChecked());
        setTimeStopPwdModeSw.setEnabled(!setTimeStopNotShowDialogSw.isChecked());
        stopAppModeSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_STOPAPPBYXP,true));
        muBeiStopReceiverSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,false));
        exitRemoveRecentSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_EXITREMOVERECENT,true));
        checkTimeOutSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISCHECKTIMEOUTAPP,false));
        notneedAccessSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISNOTNEEDACCESS,true));
        zhendongSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ZHENDONG,true));
        updateTimeSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISUPDATEAPPTIME,false));
        isOpenConfigSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISLONGCLICKOPENCONFIG,false));
        isAlwaysKillOffSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISALWAYSKILLOFF,false));
        autoStartNotNofifySw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ISAUTOSTARTNOTNOTIFY,false));
        backDelayTimeTv.setText("返回时强退、墓碑和冻结延迟:"+settings.getInt(Common.PREFS_SETTING_BACKDELAYTIME,0)+"秒");
        homeDelayTimeTv.setText("后台时墓碑、待机延迟:"+settings.getInt(Common.PREFS_SETTING_HOMEDELAYTIME,0)+"秒");
        offDelayTimeTv.setText("熄屏时强退、墓碑和冻结延迟:"+settings.getInt(Common.PREFS_SETTING_OFFDELAYTIME,0)+"秒");
        backSb.setProgress(settings.getInt(Common.PREFS_SETTING_BACKDELAYTIME,0));
        homeSb.setProgress(settings.getInt(Common.PREFS_SETTING_HOMEDELAYTIME,0));
        offSb.setProgress(settings.getInt(Common.PREFS_SETTING_OFFDELAYTIME,0));
        backSw.setTag(0);
        isShowSideBarSw.setTag(1);
        iceRemoveSw.setTag(2);
        iceOffSw.setTag(3);
        notCleanDataSw.setTag(4);
//        nightModeSw.setTag(5);
        setTimeStopZWModeSw.setTag(6);
        iceStopSw.setTag(7);
        stopAppModeSw.setTag(8);
        setTimeStopPwdModeSw.setTag(9);
        notExitAudioPlaySw.setTag(10);
        muBeiStopReceiverSw.setTag(11);
        exitRemoveRecentSw.setTag(12);
        checkTimeOutSw.setTag(13);
        notneedAccessSw.setTag(14);
        zhendongSw.setTag(15);
        updateTimeSw.setTag(16);
        isOpenConfigSw.setTag(17);
        isAlwaysKillOffSw.setTag(18);
        setTimeStopNotShowDialogSw.setTag(19);
        autoStartNotNofifySw.setTag(20);
        SwitchClick sc = new SwitchClick();
        backSw.setOnCheckedChangeListener(sc);
        isShowSideBarSw.setOnCheckedChangeListener(sc);
        iceRemoveSw.setOnCheckedChangeListener(sc);
        iceOffSw.setOnCheckedChangeListener(sc);
        notCleanDataSw.setOnCheckedChangeListener(sc);

        setTimeStopZWModeSw.setOnCheckedChangeListener(sc);
        setTimeStopPwdModeSw.setOnCheckedChangeListener(sc);
        setTimeStopNotShowDialogSw.setOnCheckedChangeListener(sc);
        iceStopSw.setOnCheckedChangeListener(sc);
        stopAppModeSw.setOnCheckedChangeListener(sc);
        notExitAudioPlaySw.setOnCheckedChangeListener(sc);
        checkTimeOutSw.setOnCheckedChangeListener(sc);
        muBeiStopReceiverSw.setOnCheckedChangeListener(sc);
        exitRemoveRecentSw.setOnCheckedChangeListener(sc);
        updateTimeSw.setOnCheckedChangeListener(sc);
        notneedAccessSw.setOnCheckedChangeListener(sc);
        zhendongSw.setOnCheckedChangeListener(sc);
        isAlwaysKillOffSw.setOnCheckedChangeListener(sc);
        isOpenConfigSw.setOnCheckedChangeListener(sc);
        autoStartNotNofifySw.setOnCheckedChangeListener(sc);
        SeekBarListener sbl = new SeekBarListener();
        backSb.setTag(0);
        homeSb.setTag(1);
        offSb.setTag(2);
        backSb.setOnSeekBarChangeListener(sbl);
        homeSb.setOnSeekBarChangeListener(sbl);
        offSb.setOnSeekBarChangeListener(sbl);
        tvs= new TextView[]{backDelayTimeTv,homeDelayTimeTv,offDelayTimeTv};
        return v;
    }

    class SwitchClick implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BaseActivity.zhenDong(getContext());
            int tag= (Integer) buttonView.getTag();
            String names[] = {Common.PREFS_SETTING_BACKKILLSELF,Common.PREFS_SETTING_SHOWSIDEBAR,Common.PREFS_SETTING_ICEBACKICE,
                    Common.PREFS_SETTING_ICEOFFICE,Common.PREFS_SETTING_NOTUNSTALLNOTCLEAN,"",
                    Common.PREFS_SETTING_SETTIMESTOPMODE,Common.PREFS_SETTING_ICESTOPICE,Common.PREFS_SETTING_STOPAPPBYXP,
                    Common.PREFS_SETTING_SETTIMESTOPPWDMODE,Common.PREFS_SETTING_ISNOTEXITAUDIOPLAY,Common.PREFS_SETTING_ISMUBEISTOPOTHERPROC,
                    Common.PREFS_SETTING_EXITREMOVERECENT,Common.PREFS_SETTING_ISCHECKTIMEOUTAPP,Common.PREFS_SETTING_ISNOTNEEDACCESS,
                    Common.PREFS_SETTING_ZHENDONG,Common.PREFS_SETTING_ISUPDATEAPPTIME,Common.PREFS_SETTING_ISLONGCLICKOPENCONFIG,
                    Common.PREFS_SETTING_ISALWAYSKILLOFF,Common.PREFS_SETTING_SETTIMESTOPNOTSHOWDIALOG,Common.PREFS_SETTING_ISAUTOSTARTNOTNOTIFY};
            if (tag == 0){
                WatchDogService.isBackKillSelf = isChecked;
            }else if (tag == 2){
                WatchDogService.isAtuoRemoveIce = isChecked;
            }else if(tag == 3){
                WatchDogService.isAtuoOffScIce = isChecked;
            }else if(tag ==4){
                WatchDogService.isNotUntallNotclean = isChecked;
                Intent intent = new Intent("com.click369.control.pms.changeunstallcleanstate");
                intent.putExtra("isNotUntallNotclean",isChecked);
                getContext().sendBroadcast(intent);
            }else if((tag ==7)){
                WatchDogService.isAtuoStopIce = isChecked;
            }else if((tag ==6)){
                WatchDogService.isSetTimeStopByZW = isChecked;
                if (isChecked){
                    setTimeStopPwdModeSw.setChecked(false);
                    WatchDogService.isSetTimeStopByPWD = false;
                    settings.edit().putBoolean(Common.PREFS_SETTING_SETTIMESTOPPWDMODE,false).commit();
                }
            }else if(tag ==8){
                WatchDogService.isXPstop= isChecked;
            }else if(tag ==9){
                WatchDogService.isSetTimeStopByPWD= isChecked;
                if (isChecked){
                    setTimeStopZWModeSw.setChecked(false);
                    WatchDogService.isSetTimeStopByZW = false;
                    settings.edit().putBoolean(Common.PREFS_SETTING_SETTIMESTOPMODE,false).commit();
                }
            }else if(tag ==10){
                WatchDogService.isNotExitAudioPlay = isChecked;
//                Log.i("CONTROL","WatchDogService.isNotExitAudioPlay  "+WatchDogService.isNotExitAudioPlay);
            }else if(tag ==11){
                AlertUtil.showAlertMsg(getActivity(),"改变模式后重启生效，请手动重启手机，部分系统处理广播会导致异常，如果出现异常请关闭并重启。");
            }else if(tag ==12){
                WatchDogService.isExitRemoveRecent= isChecked;
            }else if(tag ==13){
                WatchDogService.isCheckTimeOutApp= isChecked;
            }else if(tag ==14){
                settings.edit().putBoolean(names[tag],isChecked).commit();
                WatchDogService.isNotNeedAccessibilityService= isChecked;
                AlertUtil.showAlertMsg(getActivity(),"改变模式后重启生效，请手动重启手机");
                Intent intent = new Intent(getActivity(),WatchDogService.class);
                getActivity().startService(intent);
            }else if(tag ==15){
                BaseActivity.isZhenDong = isChecked;
            }else if(tag ==16){
                BaseActivity.isUpdateAppTime = isChecked;
            }else if(tag ==18){
                WatchDogService.isAlwaysKillOff = isChecked;
            }else if(tag ==19){
                WatchDogService.isSetTimeStopNotShowDialog = isChecked;
                setTimeStopZWModeSw.setEnabled(!isChecked);
                setTimeStopPwdModeSw.setEnabled(!isChecked);
            }else if(tag ==20){
                WatchDogService.isAutoStartNotNotify = isChecked;
            }
            settings.edit().putBoolean(names[tag],isChecked).commit();
        }
    }

    class SeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int tag= (Integer) seekBar.getTag();
            String names[] = {"返回时强退、墓碑和冻结","后台时墓碑、待机","熄屏时强退、墓碑和冻结"};
            tvs[tag].setText(names[tag]+"延迟:"+seekBar.getProgress()+"秒");
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            BaseActivity.zhenDong(getContext());
            int tag= (Integer) seekBar.getTag();
//            if(seekBar.getProgress()>20){
            int offset = seekBar.getProgress()%10;
            seekBar.setProgress(seekBar.getProgress()+(offset==0?0:(offset>5?(10-offset):-1*offset)));
//            }
            String names[] = {Common.PREFS_SETTING_BACKDELAYTIME,Common.PREFS_SETTING_HOMEDELAYTIME,Common.PREFS_SETTING_OFFDELAYTIME};
            settings.edit().putInt(names[tag],seekBar.getProgress()).commit();
            if(tag == 0){
                WatchDogService.delayBackTime = seekBar.getProgress();
            }else if(tag == 1){
                WatchDogService.delayHomeTime = seekBar.getProgress();
            }else if(tag == 2){
                WatchDogService.delayOffTime = seekBar.getProgress();
            }
        }
    }
    class PenClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            BaseActivity.zhenDong(getContext());
            final int tag= (Integer) view.getTag();
            AlertUtil.inputAlert(getActivity(), tag, new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if(txt!=null&&txt.length()>0){
                        int value= Integer.parseInt(txt);
                        String names[] = {Common.PREFS_SETTING_BACKDELAYTIME,Common.PREFS_SETTING_HOMEDELAYTIME,Common.PREFS_SETTING_OFFDELAYTIME};
                        settings.edit().putInt(names[tag],value).commit();
                        if(tag == 0){
                            WatchDogService.delayBackTime = value;
                            backSb.setProgress(value);
                        }else if(tag == 1){
                            WatchDogService.delayHomeTime = value;
                            homeSb.setProgress(value);
                        }else if(tag == 2){
                            WatchDogService.delayOffTime = value;
                            offSb.setProgress(value);
                        }
                        String titlenames[] = {"返回时强退、墓碑和冻结","后台时墓碑、待机","熄屏时强退、墓碑和冻结"};
                        tvs[tag].setText(titlenames[tag]+"延迟:"+value+"秒");
                    }
                }
            });
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LockAppSwitchView.isCloseOk = false;
    }
}

package com.click369.controlbp.fragment;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.DozeWhiteListActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.adapter.DozeLogAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.MyDozeService;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.DozeUtil;
import com.click369.controlbp.util.Notify;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilDoze;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShellUtils;
import com.click369.controlbp.util.TimeUtil;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ForceStopFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForceStopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DozeFragment extends BaseFragment {
//    private TopSearchView topView;
    private MainActivity mainActivity;
    private Switch updateTimeSw,notifySw,allSwitchSw,nightSw;
    private SeekBar onSb,offSb,offDelaySb;
    private FrameLayout alertFl,sconTimeFl,scoffTimeFl;
    private TextView sconTv,scoffTv,sconTimeTv,scoffTimeTv,scoffDelayTv,alertTv,showAlertTv,stateTv,dozeLogTv,whitelistTv;
    private ImageView sconPen,scoffPen,offDelayPen;
    private ListView listView;
    private DozeLogAdapter adapter;
    private PowerManager pm;
    private MyDozeReceiver mdr;
    private SharedPreferences dozePrefs;
    private Handler handler;
    private TextView tvs[] = null;
    public static final int MINTIME= 60;
    private int curColor = Color.BLACK;
    private boolean isShow = false;
    public DozeFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_doze, container, false);
        initView(v);
        return v;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    private void initView(View v){
        pm = (PowerManager) (this.getActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE));
        dozePrefs = SharedPrefsUtil.getInstance(getContext()).dozePrefs;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_DOZELIST);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        listView = (ListView) v.findViewById(R.id.doze_log_listview);
        updateTimeSw = (Switch) v.findViewById(R.id.doze_updatetime_sw);
        notifySw = (Switch) v.findViewById(R.id.doze_notify_sw);
        allSwitchSw = (Switch) v.findViewById(R.id.doze_allswitch_sw);
        nightSw = (Switch) v.findViewById(R.id.doze_night_sw);
        alertFl = (FrameLayout) v.findViewById(R.id.doze_alert_fl);
        sconTimeFl = (FrameLayout) v.findViewById(R.id.doze_scon_fl);
        scoffTimeFl = (FrameLayout) v.findViewById(R.id.doze_scoff_fl);
        onSb = (SeekBar) v.findViewById(R.id.doze_scon_sb);
        offSb = (SeekBar) v.findViewById(R.id.doze_scoff_sb);
        offDelaySb = (SeekBar) v.findViewById(R.id.doze_scoffdelay_sb);
        stateTv = (TextView) v.findViewById(R.id.doze_state_tv);
        sconTv = (TextView) v.findViewById(R.id.doze_scon_tv);
        sconTimeTv = (TextView) v.findViewById(R.id.doze_scon_time_tv);
        scoffTv = (TextView) v.findViewById(R.id.doze_scoff_tv);
        scoffTimeTv = (TextView) v.findViewById(R.id.doze_scoff_time_tv);
        scoffDelayTv = (TextView) v.findViewById(R.id.doze_scoff_delaytime_tv);
        showAlertTv = (TextView) v.findViewById(R.id.doze_showalert_tv);
        dozeLogTv = (TextView) v.findViewById(R.id.doze_log_tv);
        alertTv = (TextView) v.findViewById(R.id.doze_alert_tv);
        curColor = sconTv.getCurrentTextColor();
        whitelistTv = (TextView) v.findViewById(R.id.doze_whitelist_tv);
        sconPen = (ImageView) v.findViewById(R.id.doze_scon_pen);
        scoffPen = (ImageView) v.findViewById(R.id.doze_scoff_pen);
        offDelayPen = (ImageView) v.findViewById(R.id.doze_scoffdelay_pen);
        sconPen.setTag(4);
        scoffPen.setTag(5);
        offDelayPen.setTag(6);
        PenClickListener penClickListener = new PenClickListener();
        sconPen.setOnClickListener(penClickListener);
        scoffPen.setOnClickListener(penClickListener);
        offDelayPen.setOnClickListener(penClickListener);
        alertFl.setVisibility(View.VISIBLE);
        tvs= new TextView[]{sconTimeTv,scoffTimeTv,scoffDelayTv};
        String msg = "1.打盹需要保留后台，进入DOZE后所有应用后台均进入暂停状态，网络、定时器、唤醒锁、GPS均不再运行。\n2.有XP框架则进入DOZE之后移动时不会退出。\n3.时钟更新只支持类原生ROM，遇到不更新有秒钟的调出秒钟否则时间会停止。\n4.若你的手机已经足够省电，该功能对你的系统作用不大。\n5.打盹失败一般由闹钟快要响起引起(响起前一小时开始)或某些系统级别的任务的导致也有可能手机中由于没有安装GMS导致无法使用打盹功能，如果出现失败并且手机本来可以打盹一般等系统任务处理完成就可以重新打盹。";
        alertTv.setText(msg);
//        topView.setAlertText("安卓6.0以上才有此功能",Color.RED,false);
        adapter = new DozeLogAdapter(getActivity());
        listView.setAdapter(adapter);
        SeekBarListener sbl = new SeekBarListener();
        onSb.setTag(0);
        offSb.setTag(1);
        offDelaySb.setTag(2);
        onSb.setOnSeekBarChangeListener(sbl);
        offSb.setOnSeekBarChangeListener(sbl);
        offDelaySb.setOnSeekBarChangeListener(sbl);
        sconTimeTv.setText("亮屏每次打盹时长:"+dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCONTIME,MINTIME+120)+"秒");
        scoffTimeTv.setText("熄屏每次打盹时长:"+dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFTIME,MINTIME+240)+"秒");
        scoffDelayTv.setText("熄屏打盹延迟:"+dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFDELAYTIME,MINTIME+5)+"秒");
        onSb.setProgress(dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCONTIME,MINTIME+120)-MINTIME);
        offSb.setProgress(dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFTIME,MINTIME+240)-MINTIME);
        offDelaySb.setProgress(dozePrefs.getInt(Common.PREFS_SETTING_DOZE_SCOFFDELAYTIME,5)-5);
        updateTimeSw.setChecked(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_UPDATETIME,false));
        notifySw.setChecked(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_NOTIFY,true));
        allSwitchSw.setChecked(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false));
        nightSw.setChecked(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_NIGHTNOTSTOP,false));
        updateTimeSw.setTextColor(curColor);
        notifySw.setTextColor(curColor);
        allSwitchSw.setTextColor(curColor);
        nightSw.setTextColor(curColor);
        handler = new Handler();

        if(!dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCON,false)&&DozeUtil.isDozeOpen(pm)){
            DozeUtil.closeDoze();
        }
        if (!allSwitchSw.isChecked()){
            sconTv.setAlpha(0.5f);
            scoffTv.setAlpha(0.5f);
        }
        sconTv.setTextColor(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCON,false)?Color.parseColor(MainActivity.THEME_TEXT_COLOR):curColor);
        scoffTv.setTextColor(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCOFF,false)?Color.parseColor(MainActivity.THEME_TEXT_COLOR):curColor);
        sconTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    Toast.makeText(getActivity(),"安卓6.0及以上才可以使用打盹",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!allSwitchSw.isChecked()){
                    Toast.makeText(getContext(),"请打开打盹总开关",Toast.LENGTH_LONG).show();
                    return;
                }
                if (!WatchDogService.isRoot){
                    WatchDogService.isRoot = ShellUtils.checkRootPermission();
                }
                if (!WatchDogService.isRoot){
                    Toast.makeText(getActivity(),"请给予ROOT权限",Toast.LENGTH_SHORT).show();
                    WatchDogService.isRoot = ShellUtils.checkRootPermission();
                }
                if(!WatchDogService.isRoot){
                    Toast.makeText(getActivity(),"未获取ROOT权限，无法使用",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCON,false)){
                    dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_SCON,false).commit();
                    MyDozeService.issconDoze = false;
                    sconTv.setTextColor(curColor);
                    Intent in1 = new Intent(MyDozeService.STATE_OFF);
                    in1.putExtra("data","ui");
                    getActivity().sendBroadcast(in1);
                }else{
                    dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_SCON,true).commit();
                    MyDozeService.issconDoze = true;
                    sconTv.setTextColor(Color.parseColor(MainActivity.THEME_TEXT_COLOR));
                    if(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_UPDATETIME,false)){
                        Intent in = new Intent("com.click369.control.updatetime");
                        getActivity().sendBroadcast(in);
                    }
                    Intent in1 = new Intent(MyDozeService.STATE_ON);
                    in1.putExtra("data","ui");
                    getActivity().sendBroadcast(in1);
                    AlertUtil.showAlertMsg(getActivity(),"亮屏打盹时有可能会导致状态栏时钟不更新或前台应用断网（即使加入白名单部分ROM也会断网）");

                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stateTv.setText(getInfo());
                    }
                },1000);
            }
        });
        scoffTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    Toast.makeText(getActivity(),"安卓6.0及以上才可以使用打盹",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!allSwitchSw.isChecked()){
                    Toast.makeText(getContext(),"请打开打盹总开关",Toast.LENGTH_LONG).show();
                    return;
                }
                if (!WatchDogService.isRoot){
                    WatchDogService.isRoot = ShellUtils.checkRootPermission();
                }
                if (!WatchDogService.isRoot){
                    Toast.makeText(getActivity(),"请给予ROOT权限",Toast.LENGTH_SHORT).show();
                    WatchDogService.isRoot = ShellUtils.checkRootPermission();
                }
                if(!WatchDogService.isRoot){
                    Toast.makeText(getActivity(),"未获取ROOT权限，无法使用",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_SCOFF,false)){
                    dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_SCOFF,false).commit();
                    scoffTv.setTextColor(curColor);
                    MyDozeService.isscoffDoze = false;
                }else{
                    dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_SCOFF,true).commit();
                    scoffTv.setTextColor(Color.parseColor(MainActivity.THEME_TEXT_COLOR));
                    MyDozeService.isscoffDoze = true;
                }
            }
        });
        updateTimeSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    Toast.makeText(getActivity(),"安卓6.0及以上才可以使用打盹",Toast.LENGTH_SHORT).show();
                    return;
                }
                dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_UPDATETIME,isChecked).commit();
                if(isChecked){
                    Intent in = new Intent("com.click369.control.updatetime");
                    getActivity().sendBroadcast(in);
                }
            }
        });
        notifySw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                    Toast.makeText(getActivity(),"安卓6.0及以上才可以使用打盹",Toast.LENGTH_SHORT).show();
                    return;
                }
                dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_NOTIFY,isChecked).commit();
                MyDozeService.isNotify = isChecked;
                if(isChecked){
                    Notify.sendNotify(getContext(),DozeUtil.isDozeOpen(pm)?1:0,false);
                }else{
                    Notify.cancelNotify(getContext());
                }
            }
        });
        allSwitchSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
                        Toast.makeText(getActivity(),"安卓6.0及以上才可以使用打盹",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!WatchDogService.isRoot){
                        WatchDogService.isRoot = ShellUtils.checkRootPermission();
                    }
                    if (!WatchDogService.isRoot){
                        Toast.makeText(getActivity(),"请给予ROOT权限",Toast.LENGTH_SHORT).show();
                        WatchDogService.isRoot = ShellUtils.checkRootPermission();
                    }
                    if(!WatchDogService.isRoot){
                        Toast.makeText(getActivity(),"未获取ROOT权限，无法使用",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                ShellUtilDoze.close();
                ShellUtilNoBackData.close();
                dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,b).commit();
                MyDozeService.allSwitch = b;

                Log.i("CONTROL","MyDozeService.allSwitch  "+ MyDozeService.allSwitch);
                Intent intent = new Intent(MyDozeService.STATE_CLOSE);
                getActivity().sendBroadcast(intent);
                Log.i("CONTROL","MyDozeService.allSwitch  "+ MyDozeService.allSwitch);
                sconTv.setAlpha(b?1.0f:0.5f);
                scoffTv.setAlpha(b?1.0f:0.5f);
            }
        });
        nightSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                dozePrefs.edit().putBoolean(Common.PREFS_SETTING_DOZE_NIGHTNOTSTOP,b).commit();
                MyDozeService.nightNotStop = b;
            }
        });
        alertFl.setVisibility(View.GONE);
        showAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
            }
        });
        whitelistTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent intent = new Intent(getActivity(),DozeWhiteListActivity.class);
                getActivity().startActivity(intent);
            }
        });
        dozeLogTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listView.isShown()){
                    listView.setVisibility(View.GONE);
                    dozeLogTv.setTextColor(curColor);
                }else{
                    if (MyDozeService.logs.size()==0){
                        Toast.makeText(getContext(),"当前还没有日志",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    listView.setVisibility(View.VISIBLE);
                    adapter.setData(MyDozeService.logs);
                    dozeLogTv.setTextColor(Color.parseColor(MainActivity.THEME_TEXT_COLOR));
                }
            }
        });

        dozeLogTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listView.isShown()){
                    MyDozeService.logs.clear();
                    adapter.setData(MyDozeService.logs);
                    listView.setVisibility(View.GONE);
                    dozeLogTv.setTextColor(curColor);
                }
                return true;
            }
        });
        reg();
        stateTv.setText(getInfo());
        if(!WatchDogService.isKillRun){
            AlertUtil.showAlertMsg(getActivity(),"后台服务(WatchDogService)被杀死,保持后台服务不被杀才能正常使用Doze，否则程序无法控制而且也无法检测到打盹状态。");
        }
        isShow = true;
    }

    class  MyDozeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if("com.click369.control.doze.ui".equals(action)){
                stateTv.setText(getInfo());
                if (listView.isShown()) {
                    adapter.setData(MyDozeService.logs);
                }
                if (isShow&& MyDozeService.logs.size()>0&& MyDozeService.logs.get(0).contains("第5条")){
                    Toast.makeText(getActivity(), MyDozeService.logs.get(0),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        stateTv.setText(getInfo());
        if (listView.isShown()) {
            adapter.setData(MyDozeService.logs);
        }
        isShow = true;
    }

    class SeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int tag= (Integer) seekBar.getTag();
            if (tag == 2){
                tvs[tag].setText("熄屏打盹延迟:"+(seekBar.getProgress()+5)+"秒");
            }else{
                String names[] = {"亮屏","熄屏"};
                tvs[tag].setText(names[tag]+"每次打盹时长:"+(seekBar.getProgress()+MINTIME)+"秒");
            }

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int tag= (Integer) seekBar.getTag();
            int offset = seekBar.getProgress()%10;
            seekBar.setProgress(seekBar.getProgress()+(offset==0?0:(offset>5?(10-offset):-1*offset)));
            String names[] = {Common.PREFS_SETTING_DOZE_SCONTIME,Common.PREFS_SETTING_DOZE_SCOFFTIME,Common.PREFS_SETTING_DOZE_SCOFFDELAYTIME};
            if(tag == 0){
                MyDozeService.scOnDozeTime = seekBar.getProgress()+MINTIME;
                dozePrefs.edit().putInt(names[tag],seekBar.getProgress()+MINTIME).commit();
            }else if(tag == 1){
                MyDozeService.scOffDozeTime = seekBar.getProgress()+MINTIME;
                dozePrefs.edit().putInt(names[tag],seekBar.getProgress()+MINTIME).commit();
            }else{
                MyDozeService.scOffDozeDelayTime = seekBar.getProgress()+5;
                dozePrefs.edit().putInt(names[tag],seekBar.getProgress()+5).commit();
            }
        }
    }
   @Override
    public void onStart() {
       stateTv.setText(getInfo());
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        isShow = false;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        stateTv.setText(getInfo());
        allSwitchSw.setChecked(dozePrefs.getBoolean(Common.PREFS_SETTING_DOZE_ALLSWITCH,false));
        isShow = !hidden;
        super.onHiddenChanged(hidden);

    }

    @Override
    public void onStop() {

        super.onStop();
    }

    public void reg(){
        if(this.getActivity()==null){
            return;
        }
        IntentFilter ifliter = new IntentFilter();
        ifliter.addAction("com.click369.control.doze.ui");
        mdr = new MyDozeReceiver();
        this.getActivity().registerReceiver(mdr, ifliter);
    }
    public void destory(){
        if(mdr==null){
            return;
        }
        this.getActivity().unregisterReceiver(mdr);
    }

    private String getInfo(){
        StringBuilder sb = new StringBuilder("状态："+(DozeUtil.isDozeOpen(pm)?"开 ":"关 "));
        if(MyDozeService.lastDozeOpenTime>0){
            sb.append("上次开").append(TimeUtil.changeMils2String(MyDozeService.lastDozeOpenTime,"HH:mm:ss"));
        }
        if(MyDozeService.lastDozeCloseTime>0){
            sb.append(" 上次关").append(TimeUtil.changeMils2String(MyDozeService.lastDozeCloseTime,"HH:mm:ss"));
        }
        if(Math.abs(MyDozeService.lastDozeCloseTime- MyDozeService.lastDozeOpenTime)<1000&& MyDozeService.stopCount>0){
            sb.append("\n被动关闭了"+ MyDozeService.stopCount+"次");
        }
        return sb.toString();
    }

    class PenClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            final int tag= (Integer) view.getTag();
            AlertUtil.inputAlert(getActivity(), tag, new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if(txt!=null&&txt.length()>0){
                        int value= Integer.parseInt(txt);
                        if (tag == 6){
                            if (value<5){
                                value= 5;
                            }
                        }else{
                            if (value<60){
                                value= 60;
                            }
                        }
                        String names[] = {Common.PREFS_SETTING_DOZE_SCONTIME,Common.PREFS_SETTING_DOZE_SCOFFTIME,Common.PREFS_SETTING_DOZE_SCOFFDELAYTIME};
                        dozePrefs.edit().putInt(names[tag-4],value).commit();
                        if(tag == 4){
                            MyDozeService.scOnDozeTime = value;
                            onSb.setProgress(value);
                        }else if(tag == 5){
                            MyDozeService.scOffDozeTime = value;
                            offSb.setProgress(value);
                        }else if(tag ==6){
                            MyDozeService.scOffDozeDelayTime = value;
                            offDelaySb.setProgress(value);
                        }
                        if (tag == 6){
                            tvs[tag-4].setText("熄屏打盹延迟:"+value+"秒");
                        }else{
                            String titlenames[] = {"亮屏每次打盹时长:","熄屏每次打盹时长:"};
                            tvs[tag-4].setText(titlenames[tag-4]+value+"秒");
                        }

                    }
                }
            });
        }
    }
}

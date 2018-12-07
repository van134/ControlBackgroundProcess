package com.click369.controlbp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.ControlAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.util.ArrayList;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ControlFragment extends BaseFragment {
    private Handler h = new Handler();
    public ControlAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
//    private EditText editText;
//    private FrameLayout alertFl;
    public static int curColor = Color.BLACK;
    private TextView serviceTv,broadTv,wakelockTv,alarmTv,strongWakeLockTv,strongAlarmTv;//alertTv,closeTv;
    private SharedPreferences modPrefs;//,settingPrefs;
    public static boolean isClick = false;
    public ControlFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_control, container, false);
        initView(v);
        return v;
    }

    @SuppressLint("WorldReadableFiles")
    private void initView(View v){
//        settingPrefs = SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_APPSETTINGS);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
        modPrefs =  SharedPrefsUtil.getInstance(getContext()).modPrefs;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_SETTINGNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
        modPrefs.edit().remove(Common.PACKAGENAME+"/broad").commit();
//        isBroadStop = settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPRECEIVER,false);
//        modPrefs.edit().putBoolean(Common.PACKAGENAME+"/broad",true).commit();
        listView = (ListView)v.findViewById(R.id.main_listview);
        strongWakeLockTv = (TextView)v.findViewById(R.id.main_control_waklock_tv);
        strongAlarmTv = (TextView)v.findViewById(R.id.main_control_alarm_tv);
        serviceTv = (TextView) v.findViewById(R.id.main_service_tv);
        broadTv = (TextView)v.findViewById(R.id.main_broad_tv);
        wakelockTv = (TextView)v.findViewById(R.id.main_wakelock_tv);
        alarmTv = (TextView)v.findViewById(R.id.main_alarm_tv);
        curColor = alarmTv.getCurrentTextColor();
        adapter = new ControlAdapter(this.getContext(),modPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        if(MainActivity.isModuleActive()){
            String msg = "1.部分应用禁用后会出现无响应或无法打开，甚至禁用某些系统应用会导致无法开机，如果出现请取消禁用，如果无法开机禁用框架后清除本应用数据重新安装即可。\n2.服务禁用后如果需要下载或后台的软件将会出现异常，所以请慎重处理。选择或取消后应用会被杀死。\n3.长按顶部的服务、唤醒锁、定时器可以一键全选,单击可以按对应的项目排序。\n4.说明：1.服务主要是用来保持后台运行的，如果你觉得某些应用不需要下载音乐播放等后台则可以禁止。2.广播用来监听一些系统动作和拉起服务的，比如熄屏亮屏开机启动等，禁用广播后不仅不能接收并且也不能发送。3.唤醒锁和定时器主要是在熄屏时用来执行任务的，所以待机耗电很大一部分来自与唤醒锁定时器耗电，所以如果你不需要让某应用在待机时执行任务则可禁止。";
            topView.setAlertText(msg,0,false);
        }else{
            String msg = "检测到xposed框架未生效，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可。本功能需要框架支持，其他功能只需root即可。";
            topView.setAlertText(msg,Color.RED,true);
            listView.setEnabled(false);
            topView.sysAppTv.setEnabled(false);
            topView.userAppTv.setEnabled(false);
            topView.editText.setEnabled(false);
            serviceTv.setEnabled(false);
            wakelockTv.setEnabled(false);
            broadTv.setEnabled(false);
            alarmTv.setEnabled(false);
        }
        serviceTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ControlFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(ControlFragment.this.getActivity(), "请选择对后台服务的操作(禁用后部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                if (ai.isHomeMuBei||ai.isOffscMuBei||ai.isBackMuBei){
                                    continue;
                                }
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/service",true);
                                ed.commit();
                                ai.isServiceStop = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.remove(ai.getPackageName()+"/service").commit();
                                ai.isServiceStop = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        broadTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ControlFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(ControlFragment.this.getActivity(), "请选择对广播接收器的操作(部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/broad",true);
                                ed.commit();
                                ai.isBroadStop = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else  if(tag==2){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.remove(ai.getPackageName()+"/broad").commit();
                                ai.isBroadStop = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        wakelockTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ControlFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(ControlFragment.this.getActivity(), "请选择对唤醒锁的操作(部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/wakelock",true);
                                ed.commit();
                                ai.isWakelockStop = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else  if(tag==2){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.remove(ai.getPackageName()+"/wakelock").commit();
                                ai.isWakelockStop = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        alarmTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ControlFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(ControlFragment.this.getActivity(), "请选择对定时器的操作(部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/alarm",true);
                                ed.commit();
                                ai.isAlarmStop = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            isClick = true;
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.remove(ai.getPackageName()+"/alarm").commit();
                                ai.isAlarmStop = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        strongWakeLockTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), WakeLockActivity.class);
                getActivity().startActivity(intent);
            }
        });
        strongAlarmTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AlarmActivity.class);
                getActivity().startActivity(intent);
            }
        });
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        TitleClickListener listener = new TitleClickListener();
        serviceTv.setOnClickListener(listener);
        broadTv.setOnClickListener(listener);
        wakelockTv.setOnClickListener(listener);
        alarmTv.setOnClickListener(listener);
//        loadApp();
        fresh();
    }

    @Override
    public void onStop() {
//        if (isClickItem){
//            isClickItem = false;
//            Intent intent = new Intent(getActivity(), WatchDogService.class);
//            getActivity().startService(intent);
//        }
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
//        if (hidden&&isClickItem){
//            isClickItem = false;
//            Intent intent = new Intent(getActivity(), WatchDogService.class);
//            getActivity().startService(intent);
//        }else
        if (!hidden){
            fresh();
        }
        super.onHiddenChanged(hidden);
    }

    public void fresh(){
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                topView.showText();
//                adapter.fliterName = TopSearchView.searchText;
////                isBroadStop = settingPrefs.getBoolean(Common.PREFS_SETTING_ISMUBEISTOPRECEIVER,false);
//                adapter.fliterList(adapter.fliterName,MainActivity.allAppInfos);
            }
        },250);
    }

    class TitleClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            ArrayList<TextView> tvs = new ArrayList<TextView>();
            tvs.add(serviceTv);
            tvs.add(broadTv);
            tvs.add(wakelockTv);
            tvs.add(alarmTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.COLOR));
        }
    }
}

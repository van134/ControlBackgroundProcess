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
import com.click369.controlbp.adapter.ForceStopAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.util.ArrayList;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ForceStopFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForceStopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForceStopFragment extends Fragment {
    private Handler h = new Handler();
    public ForceStopAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
//    private EditText editText;
//    private FrameLayout alertFl;
    private TextView backTv,homeTv,offScTv,notifyTv;//alertTv,closeTv;
    private SharedPreferences forcePrefs,muBeiPrefs,appStartPrefs;
    private MainActivity mainActivity;
    public static  boolean isClick = false;
    public static int curColor = Color.BLACK;
    public ForceStopFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forcestop, container, false);
        initView(v);
        return v;
    }

    private void initView(View v){
        appStartPrefs = SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_AUTOSTARTNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        muBeiPrefs = SharedPrefsUtil.getPreferences(this.getActivity(),Common.IPREFS_MUBEILIST);
        forcePrefs = SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_FORCESTOPNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
//        controlPrefs = SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_SETTINGNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_FORCESTOPNAME, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.main_listview);
        backTv = (TextView) v.findViewById(R.id.main_back_tv);
        homeTv = (TextView) v.findViewById(R.id.main_home_tv);
        offScTv = (TextView)v.findViewById(R.id.main_offsc_tv);
        notifyTv = (TextView)v.findViewById(R.id.main_notify_tv);
        curColor = offScTv.getCurrentTextColor();
        adapter = new ForceStopAdapter((MainActivity) this.getActivity());
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        String msg = "1.返回时、后台时、熄屏时分别可设置两种模式(点击多次进行切换)。返回时、熄屏时第一种是强退模式第二种是墓碑模式，后台时第一种是墓碑模式(十字架图标)第二种是待机模式（AppStandby暂停图标），而且当设置了后台墓碑熄屏和返回墓碑会自动取消，强退为直接杀死进程墓碑则为进入缓存停止执行任务（个别应用可能失效），如果在第一项的服务中设置了禁用则本功能的墓碑无法生效。\n2.选择通知排除的应用在有通知时不会执行设置的任何操作。\n3.返回强退和熄屏强退需要保证本应用后台不被杀死。\n4.添加返回强退的时同时会添加到禁止自启列表中。";
        topView.setAlertText(msg,0,false);
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,MainActivity.allAppInfos);
            }
        });
        final String titles [] =new String[]{"全选为强退模式","全选为墓碑模式","清除所有选择"};
        backTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ForceStopFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showListAlert(ForceStopFragment.this.getActivity(), "请选择对返回键的操作",titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            SharedPreferences.Editor edStart = appStartPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                ed.putBoolean(ai.getPackageName()+"/backstop",true).commit();
                                ed.remove(ai.getPackageName()+"/backmubei").commit();
                                mbed.remove(ai.getPackageName()).commit();
                                ai.isBackForceStop = true;
                                ai.isBackMuBei = false;

                                edStart.putBoolean(ai.getPackageName()+"/autostart",true).commit();
                                ai.isAutoStart = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==1){
                            if(!MainActivity.isModuleActive()){
                                Toast.makeText(getContext(),"墓碑模式需要XP支持",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            SharedPreferences.Editor edStart = appStartPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                if(!ai.isHomeMuBei&&!ai.isServiceStop){
                                    ed.putBoolean(ai.getPackageName()+"/backmubei",true).commit();
                                    ed.remove(ai.getPackageName()+"/backstop").commit();
                                    mbed.putInt(ai.getPackageName(),0).commit();
                                    ai.isBackMuBei = true;
                                    ai.isBackForceStop = false;
                                    if(!ai.isRecentForceClean){
                                        edStart.remove(ai.getPackageName()+"/autostart").commit();
                                        ai.isAutoStart = false;
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                ed.remove(ai.getPackageName()+"/backstop").commit();
                                ed.remove(ai.getPackageName()+"/backmubei").commit();
                                mbed.remove(ai.getPackageName()).commit();
                                ai.isBackForceStop = false;
                                ai.isBackMuBei = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                        isClick = true;
                    }
                });
                return true;
            }
        });
        homeTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ForceStopFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                final String titles [] =new String[]{"全选为待机模式","全选为墓碑模式","清除所有选择"};
                AlertUtil.showListAlert(ForceStopFragment.this.getActivity(), "请选择对后台时的操作",titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            if(!MainActivity.isModuleActive()){
                                Toast.makeText(getContext(),"墓碑模式需要XP支持",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                ed.remove(ai.getPackageName()+"/homestop").putBoolean(ai.getPackageName()+"/idle",true).commit();
                                mbed.remove(ai.getPackageName()+"/homemubei").remove(ai.getPackageName()).commit();
                                ai.isHomeMuBei = false;
                                ai.isHomeIdle = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==1){
                            if(!MainActivity.isModuleActive()){
                                Toast.makeText(getContext(),"墓碑模式需要XP支持",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                if (!ai.isOffscMuBei&&!ai.isBackMuBei&&!ai.isServiceStop) {
                                    ed.remove(ai.getPackageName() + "/homestop").commit();
                                    ed.putBoolean(ai.getPackageName() + "/homemubei", true).commit();
                                    mbed.putInt(ai.getPackageName(),0).commit();
                                    ai.isHomeMuBei = true;
//                                ai.isHomeForceStop = false;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                ed.remove(ai.getPackageName()+"/idle").remove(ai.getPackageName()+"/homemubei").commit();
                                mbed.remove(ai.getPackageName()).commit();
                                ai.isHomeIdle = false;
                                ai.isHomeMuBei = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                        isClick = true;
                    }
                });
                return true;
            }
        });
        offScTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ForceStopFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showListAlert(ForceStopFragment.this.getActivity(), "请选择对熄屏时的操作",titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                ed.putBoolean(ai.getPackageName()+"/offstop",true).commit();
                                ed.remove(ai.getPackageName()+"/offmubei").commit();
                                mbed.remove(ai.getPackageName()).commit();
                                ai.isOffscForceStop = true;
                                ai.isOffscMuBei = false;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==1){
                            if(!MainActivity.isModuleActive()){
                                Toast.makeText(getContext(),"墓碑模式需要XP支持",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                if(!ai.isHomeMuBei&&!ai.isServiceStop) {
                                    ed.putBoolean(ai.getPackageName() + "/offmubei", true).commit();
                                    ed.remove(ai.getPackageName() + "/offstop").commit();
                                    mbed.putInt(ai.getPackageName() , 0).commit();
                                    ai.isOffscMuBei = true;
                                    ai.isOffscForceStop = false;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            isClick = true;
                            SharedPreferences.Editor ed = forcePrefs.edit();
                            SharedPreferences.Editor mbed = muBeiPrefs.edit();
                            for(AppInfo ai:adapter.bjdatas){
                                ed.remove(ai.getPackageName()+"/offstop").commit();
                                ed.remove(ai.getPackageName()+"/offmubei").commit();
                                mbed.remove(ai.getPackageName()).commit();
                                ai.isOffscForceStop = false;
                                ai.isOffscMuBei = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                        isClick = true;
                    }
                });
                return true;
            }
        });
        notifyTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
//                if(!MainActivity.isModuleActive()){
//                    Toast.makeText(getActivity(),"该功能需要XP框架支持",Toast.LENGTH_LONG).show();
//                    return true;
//                }
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(ForceStopFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(ForceStopFragment.this.getActivity(), "请选择对通知排除的操作", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = forcePrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/notifynotexit",true);
                                ed.commit();
                                ai.isNotifyNotExit = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = forcePrefs.edit();
                                ed.remove(ai.getPackageName()+"/notifynotexit");
                                ed.commit();
                                ai.isNotifyNotExit = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                        isClick = true;
                    }
                });
                return true;
            }
        });
        TitleClickListener listener = new TitleClickListener();
        backTv.setOnClickListener(listener);
        homeTv.setOnClickListener(listener);
        offScTv.setOnClickListener(listener);
        notifyTv.setOnClickListener(listener);
//        loadApp();
        fresh();
    }

    public void fresh(){
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                topView.showText();
//                adapter.fliterName = TopSearchView.searchText;
//                adapter.fliterList(adapter.fliterName,MainActivity.allAppInfos);
            }
        },250);
    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        if (!hidden){
//            adapter.notifyDataSetChanged();
            fresh();
        }
        Log.i("CONTROL","返回force");
        ((MainActivity)getActivity()).startAccess();
    }

    @Override
    public void onStop() {
        if (ForceStopFragment.isClick) {
            Intent intent = new Intent(this.getActivity(), WatchDogService.class);
            this.getActivity().startService(intent);
            ForceStopFragment.isClick = false;
            Log.i("CONTROL","返回force");
            ((MainActivity)getActivity()).startAccess();
        }
        super.onStop();
    }

    //
//    final ArrayList<AppInfo> allAppInfos = new ArrayList<AppInfo>();
//    ProgressDialog pd;
//    private void loadApp(){
//        pd = ProgressDialog.show(ControlFragment.this.getActivity(),"","正在加载应用列表...",true,true);
//        allAppInfos.clear();
//        new Thread(){
//            @Override
//            public void run() {
//                ArrayList<AppInfo> apps = AppInfo.readArrays(getApplicationContext());
//                if(apps!=null&&apps.size()>0){
//                    allAppInfos.addAll(apps);
//                }else{
//                    allAppInfos.addAll(AppLoaderUtil.getAppInfos(MainActivity.this,2));
//                }
//                reloadList();
//                if(apps!=null&&apps.size()>0){
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    ArrayList<AppInfo> allapps = AppLoaderUtil.getAppInfos(MainActivity.this,2);
//                    allAppInfos.clear();
//                    allAppInfos.addAll(allapps);
//                    reloadList();
//                }
//            }
//        }.start();
//    }
//
//    private void reloadList(){
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(allAppInfos.size()>0){
//                    for(AppInfo ai:allAppInfos){
//                        ai.isServiceStop = modPrefs.getBoolean(ai.getPackageName()+"/service",false);
//                        ai.isWakelockStop = modPrefs.getBoolean(ai.getPackageName()+"/wakelock",false);
//                        ai.isAlarmStop = modPrefs.getBoolean(ai.getPackageName()+"/alarm",false);
//                    }
//                    adapter.fliterList(editText.getText().toString().trim(),allAppInfos);
//                }else{
//                    showT("无法获取你的应用列表，请查看是否有限制读取。");
//                }
//                if(pd!=null&&pd.isShowing()){
//                    pd.dismiss();
//                }
//            }
//        });
//    }
    class TitleClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            ArrayList<TextView> tvs = new ArrayList<TextView>();
            tvs.add(backTv);
            tvs.add(homeTv);
            tvs.add(offScTv);
            tvs.add(notifyTv);
            int index = tvs.indexOf(v);
            if(index == 0&&adapter.sortType==index){
                index = 4;
            }else if(index == 0&&adapter.sortType==4){
                index = -1;
            }else if(index == 2&&adapter.sortType==index){
                index = 5;
            }else if(index == 2&&adapter.sortType==5){
                index = -1;
            }else if(index == 1&&adapter.sortType==index){
                index = 6;
            }else if(index == 1&&adapter.sortType==6){
                index = -1;
            }
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.COLOR));
        }
    }
}

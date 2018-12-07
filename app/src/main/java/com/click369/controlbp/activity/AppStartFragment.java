package com.click369.controlbp.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.AppStartAdapter;
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
public class AppStartFragment extends BaseFragment {
    private Handler h = new Handler();
    public AppStartAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
    public static int curColor = Color.BLACK;
    private TextView lockAppTv,stopAppTv,autoStartTv,notStopTv;//alertTv,closeTv;
    private SharedPreferences modPrefs;
    public static boolean isClickItem = false;
    public AppStartFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_appstart, container, false);
        initView(v);
        isClickItem = false;
        return v;
    }

    @SuppressLint("WorldReadableFiles")
    private void initView(View v){
        modPrefs =  SharedPrefsUtil.getInstance(getContext()).autoStartNetPrefs;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_AUTOSTARTNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);

        listView = (ListView)v.findViewById(R.id.main_listview);
        lockAppTv = (TextView) v.findViewById(R.id.main_service_tv);
        stopAppTv = (TextView)v.findViewById(R.id.main_wakelock_tv);
        autoStartTv = (TextView)v.findViewById(R.id.main_alarm_tv);
        notStopTv = (TextView)v.findViewById(R.id.main_notstop_tv);
        curColor = autoStartTv.getCurrentTextColor();
        adapter = new AppStartAdapter(this.getActivity(),modPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter, getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        if(MainActivity.isModuleActive()){
            String msg = "1.加入应用锁的应用在打开时需要校验指纹（前提是手机支持指纹并已经设置指纹）\n2.加入禁止运行的程序点击应用图标无法开启程序（不要禁止系统应用）。\n3.加入禁止自启的应用无法开机启动并且无法关联启动除非手动开启应用。\n4.加入内存常驻的应用启动后系统或其他管理软件无法杀死该应用，只有应用控制器的强退功能能杀死，注意部分应用加入后可能会出现不可预料的异常，比如微信更新微X模块后无法重启微信。";
            topView.setAlertText(msg,0,false);
        }else{
            String msg = "检测到xposed框架未生效，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可。本功能需要框架支持，其他功能只需root即可。";
            topView.setAlertText(msg,Color.RED,true);
            listView.setEnabled(false);
            topView.sysAppTv.setEnabled(false);
            topView.userAppTv.setEnabled(false);
            topView.editText.setEnabled(false);
            lockAppTv.setEnabled(false);
            stopAppTv.setEnabled(false);
            autoStartTv.setEnabled(false);
        }
//        mobileNetTv.setVisibility(View.INVISIBLE);
//        stopAppTv.setVisibility(View.INVISIBLE);
//        wifiNetTv.setAlpha(0.5f);
        lockAppTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(AppStartFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(AppStartFragment.this.getActivity(), "请选择对应用锁的操作(只对有界面的应用有效)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            for(AppInfo ai:adapter.bjdatas){
                                if (ai.isStopApp){
                                    continue;
                                }
                                ai.isLockApp = true;
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/lockapp",true);
                                ed.commit();
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            for(AppInfo ai:adapter.bjdatas){
                                ai.isLockApp = false;
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.remove(ai.getPackageName()+"/lockapp").commit();
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        stopAppTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(true){
                    Toast.makeText(getActivity(),"该功能不能全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                return true;
            }
        });
        autoStartTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(AppStartFragment.this.getActivity(),"为了安全起见只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(AppStartFragment.this.getActivity(), "请选择禁止自启的操作(部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            for(AppInfo ai:adapter.bjdatas){
                                if (ai.getPackageName().equals(Common.PACKAGENAME)||ai.isStopApp) {
                                    continue;
                                }
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/autostart",true);
                                ed.commit();
                                ai.isAutoStart = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = modPrefs.edit();
                                ed.remove(ai.getPackageName()+"/autostart").commit();
                                ai.isAutoStart = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        notStopTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertUtil.showAlertMsg(getActivity(),"内存常驻不支持全选，因为保留的应用将常驻内存无法杀死，全选后可能导致手机内存不足，所以请单独选择。");
                return true;
            }
        });
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        TitleClickListener listener = new TitleClickListener();
        lockAppTv.setOnClickListener(listener);
        stopAppTv.setOnClickListener(listener);
        autoStartTv.setOnClickListener(listener);
        notStopTv.setOnClickListener(listener);
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

    class TitleClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            ArrayList<TextView> tvs = new ArrayList<TextView>();
            tvs.add(lockAppTv);
            tvs.add(stopAppTv);
            tvs.add(autoStartTv);
            tvs.add(notStopTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.COLOR));
        }
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden){
            fresh();
        }
        super.onHiddenChanged(hidden);
    }
}

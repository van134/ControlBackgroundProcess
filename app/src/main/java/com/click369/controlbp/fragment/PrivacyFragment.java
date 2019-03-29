package com.click369.controlbp.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.NewDirActivity;
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.adapter.IceUnstallAdapter;
import com.click369.controlbp.adapter.PrivacyAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.util.ArrayList;
import java.util.HashSet;

/*
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ForceStopFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForceStopFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrivacyFragment extends BaseFragment {
    private Handler h = new Handler();
    public PrivacyAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
    public static int curColor = Color.BLACK;
    private TextView wifiTv,mobileTv,logTv,controlTv,switchTv,main_newdir_tv;//alertTv,closeTv;
    private SharedPreferences modPrefs;
    public PrivacyFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_privacy, container, false);
        initView(v);
        return v;
    }

    private void initView(View v){
        modPrefs =SharedPrefsUtil.getInstance(getActivity()).privacyPrefs;// SharedPrefsUtil.getPreferences(this.getActivity(),Common.IPREFS_PMLIST);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.main_listview);
        logTv = (TextView) v.findViewById(R.id.main_service_tv);
        wifiTv = (TextView)v.findViewById(R.id.main_wifi_tv);
        mobileTv = (TextView)v.findViewById(R.id.main_mobile_tv);
        controlTv = (TextView)v.findViewById(R.id.main_wakelock_tv);
        switchTv = (TextView)v.findViewById(R.id.main_prov_tv);
        main_newdir_tv = (TextView)v.findViewById(R.id.main_newdir_tv);
        main_newdir_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NewDirActivity.class);
                startActivity(intent);
            }
        });
        curColor = switchTv.getCurrentTextColor();
        adapter = new PrivacyAdapter(this.getActivity(),modPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        if(MainActivity.isModuleActive()){
            String msg = "1.网络控制可以控制WIFI和移动数据两种，添加后立即生效，如果重启失效，请检查应用控制器后台是否存活。\n2.如果要对应用进行权限控制或自定义位置必须打开权限开关，打开权限开关后就开始记录该应用的访问情况。\n3.权限记录只保存本次开机后的，并且每次达到600条会自动清除，加入自定义位置的应用所有权限都会被阻止。\n4.权限控制目前支持：自定义位置、自定义时间、获取GPS位置、获取基站信息、获取WIFI及MAC信息、获取正在运行程序列表、获取已安装应用列表、获取手机识别码";
            topView.setAlertText(msg,0,false);
        }else{
            String msg = "检测到xposed框架未生效,阻止卸载功能无法使用冻结和卸载功能可以正常使用，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可,本功能需要框架支持。";
            topView.setAlertText(msg,Color.RED,true);
            switchTv.setEnabled(false);
        }
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        TitleClickListener listener = new TitleClickListener();
        switchTv.setOnClickListener(listener);
        wifiTv.setOnClickListener(listener);
        mobileTv.setOnClickListener(listener);
        switchTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertUtil.showConfirmAlertMsg(getActivity(), "确定清除当前列表中所有应用的权限控制开关？", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==1){
                            for(AppInfo ai:adapter.bjdatas){
                                ai.isPriSwitchOpen = false;
                                modPrefs.edit().remove(ai.packageName+"/priswitch").commit();
                            }

                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        wifiTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertUtil.showConfirmAlertMsg(getActivity(), "确定清除当前列表中所有应用的WIFI数据开关？", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==1){
                            for(AppInfo ai:adapter.bjdatas){
                                ai.isPriWifiPrevent = false;
                                modPrefs.edit().remove(ai.packageName+"/priwifi").commit();
                            }
                            WatchDogService.sendInitNetData(getContext());
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        mobileTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertUtil.showConfirmAlertMsg(getActivity(), "确定清除当前列表中所有应用的移动数据开关？", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==1){
                            for(AppInfo ai:adapter.bjdatas){
                                ai.isPriMobilePrevent = false;
                                modPrefs.edit().remove(ai.packageName+"/primobile").commit();
                            }
                            WatchDogService.sendInitNetData(getContext());
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        fresh();
        loadY(listView,this.getClass(),adapter.sortType);
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
            tvs.add(wifiTv);
            tvs.add(mobileTv);
            tvs.add(switchTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
            loadY(listView,PrivacyFragment.this.getClass(),adapter.sortType);
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
            loadY(listView,this.getClass(),adapter.sortType);
        }
        super.onHiddenChanged(hidden);
    }
}

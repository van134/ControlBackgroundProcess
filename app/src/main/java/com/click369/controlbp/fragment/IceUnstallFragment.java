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
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.adapter.IceUnstallAdapter;
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
public class IceUnstallFragment extends BaseFragment {
    private Handler h = new Handler();
    public IceUnstallAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
    public static int curColor = Color.BLACK;
    private TextView iceAppTv,notUnstallAppTv,unstallTv,clearCacheTv;//alertTv,closeTv;
    private SharedPreferences modPrefs;
    public static boolean isClickItem = false;
    public IceUnstallFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_iceunstall, container, false);
        initView(v);
        isClickItem = false;
        return v;
    }

    private void initView(View v){
        modPrefs =SharedPrefsUtil.getInstance(getActivity()).pmPrefs;// SharedPrefsUtil.getPreferences(this.getActivity(),Common.IPREFS_PMLIST);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.main_listview);
        iceAppTv = (TextView) v.findViewById(R.id.main_service_tv);
        notUnstallAppTv = (TextView)v.findViewById(R.id.main_wakelock_tv);
        unstallTv = (TextView)v.findViewById(R.id.main_alarm_tv);
        clearCacheTv = (TextView)v.findViewById(R.id.main_broad_tv);
        curColor = unstallTv.getCurrentTextColor();
        adapter = new IceUnstallAdapter(this.getActivity(),modPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        if(MainActivity.isModuleActive()){
            String msg = "1.冻结后长按冻结按钮弹出菜单选择创建快捷方式或运行冻结应用，杂项中的创建冷藏室快捷方式保持原来的九宫格显示。\n2.禁止卸载功能加入的程序在卸载时会出现崩溃界面，这个是故意为之。\n3.清除数据功能是清除该应用的所有数据（设置账户等）。\n4.清除缓存功能是清除该应用的缓存数据（并非用户数据）\n5.卸载功能请谨慎使用，系统或用户应用都可以卸载。";
            topView.setAlertText(msg,0,false);
        }else{
            String msg = "检测到xposed框架未生效,阻止卸载功能无法使用冻结和卸载功能可以正常使用，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可,本功能需要框架支持。";
            topView.setAlertText(msg,Color.RED,true);
            notUnstallAppTv.setEnabled(false);
        }
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        TitleClickListener listener = new TitleClickListener();
        iceAppTv.setOnClickListener(listener);
        notUnstallAppTv.setOnClickListener(listener);
        clearCacheTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertUtil.showConfirmAlertMsg(getActivity(), "确定清除当前列表中所有应用的缓存,(缓存数据并非用户数据)？确认后无需等待，后台自动执行。", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==1){
                            HashSet<String> pkgs = new HashSet<String>();
                            for(AppInfo ai:adapter.bjdatas){
                                pkgs.add(ai.packageName);
                            }
                            Intent intent1 = new Intent("com.click369.control.pms.clearcache");
                            intent1.putExtra("pkgs",pkgs);
                            getActivity().sendBroadcast(intent1);
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
            tvs.add(iceAppTv);
            tvs.add(notUnstallAppTv);
//            tvs.add(unstallTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
            loadY(listView,IceUnstallFragment.this.getClass(),adapter.sortType);
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

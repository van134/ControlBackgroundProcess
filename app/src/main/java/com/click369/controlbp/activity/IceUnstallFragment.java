package com.click369.controlbp.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.AppStartAdapter;
import com.click369.controlbp.adapter.ForceStopAdapter;
import com.click369.controlbp.adapter.IceUnstallAdapter;
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
public class IceUnstallFragment extends BaseFragment {
    private Handler h = new Handler();
    public IceUnstallAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
    public static int curColor = Color.BLACK;
    private TextView iceAppTv,notUnstallAppTv,unstallTv;//alertTv,closeTv;
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

    @SuppressLint("WorldReadableFiles")
    private void initView(View v){
        modPrefs =SharedPrefsUtil.getInstance(getActivity()).pmPrefs;// SharedPrefsUtil.getPreferences(this.getActivity(),Common.IPREFS_PMLIST);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.main_listview);
        iceAppTv = (TextView) v.findViewById(R.id.main_service_tv);
        notUnstallAppTv = (TextView)v.findViewById(R.id.main_wakelock_tv);
        unstallTv = (TextView)v.findViewById(R.id.main_alarm_tv);
        curColor = unstallTv.getCurrentTextColor();
        adapter = new IceUnstallAdapter(this.getActivity(),modPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        if(MainActivity.isModuleActive()){
            String msg = "1.冻结后长按冻结按钮弹出菜单选择创建快捷方式或运行冻结应用，杂项中的创建冷藏室快捷方式保持原来的九宫格显示。\n2.禁止卸载功能加入的程序在卸载时会出现崩溃界面，这个是故意为之。\n3.卸载功能请谨慎使用。";
            topView.setAlertText(msg,0,false);
        }else{
            String msg = "检测到xposed框架未生效,阻止卸载功能无法使用冻结和卸载功能可以正常使用，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可。本功能需要框架支持，其他功能只需root即可。";
            topView.setAlertText(msg,Color.RED,true);
//            listView.setEnabled(false);
//            topView.sysAppTv.setEnabled(false);
//            topView.userAppTv.setEnabled(false);
//            topView.editText.setEnabled(false);
//            iceAppTv.setEnabled(false);
            notUnstallAppTv.setEnabled(false);
//            unstallTv.setEnabled(false);
        }
//        mobileNetTv.setVisibility(View.INVISIBLE);
//        stopAppTv.setVisibility(View.INVISIBLE);
//        wifiNetTv.setAlpha(0.5f);
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        TitleClickListener listener = new TitleClickListener();
        iceAppTv.setOnClickListener(listener);
        notUnstallAppTv.setOnClickListener(listener);
        unstallTv.setOnClickListener(listener);
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
            tvs.add(iceAppTv);
            tvs.add(notUnstallAppTv);
            tvs.add(unstallTv);
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
        if (isClickItem){
            isClickItem = false;
            Intent intent = new Intent(getActivity(), WatchDogService.class);
            getActivity().startService(intent);
        }
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden&&isClickItem){
            isClickItem = false;
            Intent intent = new Intent(getActivity(), WatchDogService.class);
            getActivity().startService(intent);
        }else if (!hidden){
            fresh();
        }
        super.onHiddenChanged(hidden);
    }
}

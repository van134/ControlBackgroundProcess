package com.click369.controlbp.fragment;

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
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.SkipDialogActivity;
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.adapter.AdAdapter;
import com.click369.controlbp.adapter.RecentAdapter;
import com.click369.controlbp.adapter.XpBlackListAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.PackageUtil;
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
public class XpBlackListFragment extends BaseFragment {
    private Handler h = new Handler();
    public XpBlackListAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
    public static int curColor = Color.BLACK;
    private TextView allXpTv,controlXpTv,backHookTv,setHookTv;//alertTv,closeTv;
    private SharedPreferences xpBlackListPrefs;
    public XpBlackListFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_xpblacklist, container, false);
        initView(v);
        return v;
    }

    private void initView(View v){
        xpBlackListPrefs = SharedPrefsUtil.getInstance(getActivity()).xpBlackListPrefs;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.IPREFS_RECENTLIST);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.main_listview);
        allXpTv = (TextView) v.findViewById(R.id.main_service_tv);
        controlXpTv = (TextView)v.findViewById(R.id.main_wakelock_tv);
        backHookTv = (TextView)v.findViewById(R.id.main_alarm_tv);
        setHookTv = (TextView)v.findViewById(R.id.main_broad_tv);
        curColor = setHookTv.getCurrentTextColor();
        adapter = new XpBlackListAdapter(this.getActivity(),xpBlackListPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        if(MainActivity.isModuleActive()){
            String msg = "本功能是为了让部分检测XP模块或框架的应用可以正常使用(例如抖音)或者部分反XP的可以使用XP(例如酷安)。\n1.让所有模块失效：让所有的XP模块对该应用失效，这样部分检测XP的应用就可以正常使用但是XP功能对其无效(例如抖音),注意仅仅是部分应用，大多应用只要检测到有xp框架无论你是否对它做处理它都不会让你正常使用，而这种应用得使用阻止XP检测，目前应用控制器的阻止XP检测非常不成熟。\n2.让控制器失效：只禁止应用控制器对该应用的控制，即使设置了也不会生效。\n3.尝试阻止XP检测：该功能目前非常不成熟，在测试阶段，建议先用让模块失效的方式来让其可运行当然你也可以选择该项尝试下。该功能主要是防止应用检测到XP框架或模块从而能正常使用。\n4.防止反Xposed：部分应用加入了反HOOK功能，这样会导致所有XP模块对其失效，加入防止反HOOK是为了让模块对该应用生效。(例如酷安)";
            topView.setAlertText(msg,0,false);
        }else{
            String msg = "检测到xposed框架未生效，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可,本功能需要框架支持。";
            topView.setAlertText(msg,Color.RED,true);
            listView.setEnabled(false);
            topView.sysAppTv.setEnabled(false);
            topView.userAppTv.setEnabled(false);
            topView.editText.setEnabled(false);
            allXpTv.setEnabled(false);
            controlXpTv.setEnabled(false);
            backHookTv.setEnabled(false);
            setHookTv.setEnabled(false);
        }
//
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        XpBlackListFragment.TitleClickListener listener = new XpBlackListFragment.TitleClickListener();
        allXpTv.setOnClickListener(listener);
        controlXpTv.setOnClickListener(listener);
        backHookTv.setOnClickListener(listener);
        setHookTv.setOnClickListener(listener);
        fresh();
        loadY(listView,this.getClass(),adapter.sortType);
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
//            adapter.notifyDataSetChanged();
            fresh();
            loadY(listView,this.getClass(),adapter.sortType);
        }
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
            tvs.add(allXpTv);
            tvs.add(controlXpTv);
            tvs.add(backHookTv);
            tvs.add(setHookTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
            loadY(listView,XpBlackListFragment.this.getClass(),adapter.sortType);
        }
    }
}

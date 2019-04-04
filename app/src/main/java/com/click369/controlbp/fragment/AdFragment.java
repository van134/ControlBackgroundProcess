package com.click369.controlbp.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
public class AdFragment extends BaseFragment {
    private Handler h = new Handler();
    public AdAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
    public static int curColor = Color.BLACK;
    private TextView oneTv,twoTv,threeTv;//alertTv,closeTv;
    private SharedPreferences adPrefs;
    public AdFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ad, container, false);
        initView(v);
        return v;
    }

    private void initView(View v){
        adPrefs = SharedPrefsUtil.getInstance(getContext()).adPrefs;//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_SETTINGNAME, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.main_listview);
        oneTv = (TextView) v.findViewById(R.id.main_service_tv);
        twoTv = (TextView)v.findViewById(R.id.main_wakelock_tv);
        threeTv = (TextView)v.findViewById(R.id.main_alarm_tv);
        TextView main_skipdialog_tv = (TextView)v.findViewById(R.id.main_skipdialog_tv);
        TextView main_skipnotify_tv = (TextView)v.findViewById(R.id.main_skipnotify_tv);
        main_skipdialog_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SkipDialogActivity.class);
                intent.putExtra("type",0);
                startActivity(intent);
            }
        });
        main_skipnotify_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SkipDialogActivity.class);
                intent.putExtra("type",1);
                startActivity(intent);
            }
        });
        curColor = oneTv.getCurrentTextColor();
        adapter = new AdAdapter(this.getActivity(),adPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter, getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        String msg = "";
        if(MainActivity.isModuleActive()){
            msg = "设置后第一次运行对应的程序为采集数据广告还会显示，应用杀死或重启手机后运行程序才会生效，右上角菜单可设置预设值。\n1.模式A可以跳过大多应用的启动页广告,其次是模式B，\n2.请不要给没有启动页广告的应用设置该功能，不然可能导致闪退跳页等问题。\n3.加入跳过后出现问题切换跳过模式还无法解决请取消该应用的广告跳过功能。";
        }else{
            msg = "检测到xposed框架未生效，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可。本功能需要框架支持，其他功能只需root即可。";
            topView.setAlertText(msg,Color.RED,true);
            listView.setEnabled(false);
            topView.sysAppTv.setEnabled(false);
            topView.userAppTv.setEnabled(false);
            topView.editText.setEnabled(false);
            oneTv.setEnabled(false);
            twoTv.setEnabled(false);
            threeTv.setEnabled(false);
        }
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        TitleClickListener listener = new TitleClickListener();
        oneTv.setOnClickListener(listener);
        twoTv.setOnClickListener(listener);
        threeTv.setOnClickListener(listener);
        fresh();
        loadY(listView,this.getClass(),adapter.sortType);
        String versave = adPrefs.getString("version", "0");
        String verread = PackageUtil.getAppVersionName(getContext());
        if(!versave.equals(verread)){
            if (MainActivity.isModuleActive()){
                topView.setAlertText(msg,0,false);
            }
            Toast.makeText(getActivity(),"点击右上角更多按钮可以载入部分app预设",Toast.LENGTH_LONG).show();
//            AlertUtil.showConfirmAlertMsg(getActivity(), "是否载入部分应用程序的预设值？(如果取消以后右上角菜单中也可找到该选项)", new AlertUtil.InputCallBack() {
//                @Override
//                public void backData(String txt, int tag) {
//                    if (tag == 1){
//                        TestDataInit.initAD(getActivity());
//                        fresh();
//                    }
//                }
//            });
        }else{
            topView.setAlertText(msg,0,false);
        }
        adPrefs.edit().putString("version",verread).commit();
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            fresh();
            loadY(listView,this.getClass(),adapter.sortType);
        }
    }
    public void fresh(){
        listView.setTag(this.getClass().getName());
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                topView.showText();
//                BaseActivity.scrollyTag = AdFragment.this.getClass().getName();
            }
        },250);
    }

    class TitleClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            ArrayList<TextView> tvs = new ArrayList<TextView>();
            tvs.add(oneTv);
            tvs.add(twoTv);
            tvs.add(threeTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
            loadY(listView,AdFragment.this.getClass(),adapter.sortType);
        }
    }
}

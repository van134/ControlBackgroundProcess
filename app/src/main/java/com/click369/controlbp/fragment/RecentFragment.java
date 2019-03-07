package com.click369.controlbp.fragment;

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
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.adapter.RecentAdapter;
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
public class RecentFragment extends BaseFragment {
    private Handler h = new Handler();
    public RecentAdapter adapter;
    private ListView listView;
    private TopSearchView topView;
    public static int curColor = Color.BLACK;
    private TextView notCleanTv,forceCleanTv,blurImgTv,notShowTv;//alertTv,closeTv;
    private SharedPreferences recentPrefs,appStartPrefs;
    public RecentFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recent, container, false);
        initView(v);
        return v;
    }

    private void initView(View v){
        recentPrefs = SharedPrefsUtil.getInstance(getActivity()).recentPrefs;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.IPREFS_RECENTLIST);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        appStartPrefs = SharedPrefsUtil.getInstance(getActivity()).autoStartNetPrefs;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_AUTOSTARTNAME);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_AUTOSTARTNAME, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.recent_listview);
        notCleanTv = (TextView) v.findViewById(R.id.recent_notclean_tv);
        forceCleanTv = (TextView)v.findViewById(R.id.recent_forceclean_tv);
        blurImgTv = (TextView)v.findViewById(R.id.recent_blur_tv);
        notShowTv = (TextView)v.findViewById(R.id.rrecent_notshow_tv);
        curColor = blurImgTv.getCurrentTextColor();
        adapter = new RecentAdapter(this.getActivity(),recentPrefs,appStartPrefs);
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        if(MainActivity.isModuleActive()){
            String msg = "1.保留为强制保留程序不从最近任务中移除,即使被杀掉卡片也不会丢失。\n2.杀死为从最近任务中移除便结束进程,添加杀死的时同时会添加到禁止自启列表中\n3.模糊为最近任务中的列表为模糊状态看不到app中的内容\n4.隐藏为不在最近任务中显示应用的预览,部分软件在设置后需要重启手机才能生效。";//移动网络、无线网络、
            topView.setAlertText(msg,0,false);
        }else{
            String msg = "检测到xposed框架未生效，请勾选后重启,如果已勾选并重启过请反复勾选一次再重启即可,本功能需要框架支持。";
            topView.setAlertText(msg,Color.RED,true);
            listView.setEnabled(false);
            topView.sysAppTv.setEnabled(false);
            topView.userAppTv.setEnabled(false);
            topView.editText.setEnabled(false);
            notCleanTv.setEnabled(false);
            forceCleanTv.setEnabled(false);
            blurImgTv.setEnabled(false);
            notShowTv.setEnabled(false);
        }
//        wifiNetTv.setAlpha(0.5f);
        notCleanTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(RecentFragment.this.getActivity(),"为了安全期间只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(RecentFragment.this.getActivity(), "请选择对强制保留的操作，处理后重启生效", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            for(AppInfo ai:adapter.bjdatas){
                                if(ai.isRecentForceClean){
                                    continue;
                                }
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/notclean",true);
                                ed.commit();
                                ai.isRecentNotClean = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.remove(ai.getPackageName()+"/notclean").commit();
                                ai.isRecentNotClean = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        forceCleanTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(RecentFragment.this.getActivity(),"为了安全期间只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(RecentFragment.this.getActivity(), "请选择对移出杀死的操作(部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            for(AppInfo ai:adapter.bjdatas){
                                if (ai.getPackageName().equals(Common.PACKAGENAME)||ai.isRecentNotClean){
                                    continue;
                                }
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/forceclean",true);
                                ed.commit();
                                ai.isRecentForceClean = true;
                                if (WatchDogService.isLinkRecentAndNotStop) {
                                    SharedPreferences.Editor ed1 = appStartPrefs.edit();
                                    ed1.putBoolean(ai.getPackageName() + "/autostart", true).commit();
                                    ai.isAutoStart = true;
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }else  if(tag==2){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.remove(ai.getPackageName()+"/forceclean").commit();
                                ai.isRecentForceClean = false;
                                if(WatchDogService.isLinkRecentAndNotStop&&!ai.isBackForceStop){
                                    SharedPreferences.Editor ed1 = appStartPrefs.edit();
                                    ed1.remove(ai.getPackageName()+"/autostart").commit();
                                    ai.isAutoStart = false;
                                }
                            }
                            adapter.notifyDataSetChanged();

                        }
                    }
                });
                return true;
            }
        });
        blurImgTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(RecentFragment.this.getActivity(),"为了安全期间只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(RecentFragment.this.getActivity(), "请选择对模糊预览的操作(部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/blur",true);
                                ed.commit();
                                ai.isRecentBlur = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.remove(ai.getPackageName()+"/blur").commit();
                                ai.isRecentBlur = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        notShowTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(adapter.fliterName.toLowerCase().equals("s")||adapter.fliterName.length()==0){
                    Toast.makeText(RecentFragment.this.getActivity(),"为了安全期间只有切换到用户应用才可以全选",Toast.LENGTH_LONG).show();
                    return true;
                }
                AlertUtil.showThreeButtonAlertMsg(RecentFragment.this.getActivity(), "请选择对隐藏预览的操作(部分应用可能会出现异常)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag==0){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.putBoolean(ai.getPackageName()+"/notshow",true);
                                ed.commit();
                                ai.isRecentNotShow = true;
                            }
                            adapter.notifyDataSetChanged();
                        }else if(tag==2){
                            for(AppInfo ai:adapter.bjdatas){
                                SharedPreferences.Editor ed = recentPrefs.edit();
                                ed.remove(ai.getPackageName()+"/notshow").commit();
                                ai.isRecentNotShow = false;
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
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
        notCleanTv.setOnClickListener(listener);
        forceCleanTv.setOnClickListener(listener);
        blurImgTv.setOnClickListener(listener);
        notShowTv.setOnClickListener(listener);
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
            tvs.add(notCleanTv);
            tvs.add(forceCleanTv);
            tvs.add(blurImgTv);
            tvs.add(notShowTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
            loadY(listView,RecentFragment.this.getClass(),adapter.sortType);
        }
    }
}

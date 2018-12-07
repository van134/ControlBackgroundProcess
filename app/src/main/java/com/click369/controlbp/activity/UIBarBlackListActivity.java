package com.click369.controlbp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.DozeWhiteListAdapter;
import com.click369.controlbp.adapter.UIBarBlackListAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.util.ArrayList;

/**
 * Created by asus on 2017/5/27.
 */
public class UIBarBlackListActivity extends BaseActivity {
    private ListView list;
    private UIBarBlackListAdapter adapter;
    private TopSearchView topView;
    private TextView topTitle,bottomTitle;//alertTv,closeTv,//serviceTv,wakelockTv,alarmTv,
    private SharedPreferences barPrefs;
    public static  boolean isClick = false;
    public static int curColor = Color.BLACK;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.activity_uibarblacklist,null);
        setContentView(v);
        barPrefs = sharedPrefs.uiBarPrefs;SharedPrefsUtil.getPreferences(this,Common.PREFS_UIBARLIST);// this.getApplicationContext().getSharedPreferences(Common.PREFS_UIBARBLACKLIST, Context.MODE_WORLD_READABLE);
//        et = (EditText)this.findViewById(R.id.main_edittext);
        list = (ListView)this.findViewById(R.id.ui_blacklist_listview);
        topTitle = (TextView)this.findViewById(R.id.ui_title_topbar_tv);
        bottomTitle = (TextView)this.findViewById(R.id.ui_title_bottombar_tv);
        curColor = topTitle.getCurrentTextColor();
        adapter = new UIBarBlackListAdapter(this,barPrefs);
        list.setAdapter(adapter);
        adapter.fliterList("u",appLoaderUtil.allAppInfos);
        topView = new TopSearchView(this,v);
        topView.initView();
        String msg = "1.染色锁定是用来锁定该应用颜色，锁定后打开应用时如果该界面已经取过色就不再取色从而达到省电效果,几乎不耗电（如果颜色经常变色的应用不合适锁定）。\n2.开启染色设置需要染色的应用，如果某些软件需要染色则加入开启染色。";
        if(!MainActivity.isModuleActive()){
            msg = "框架未生效，请重启手机";
            topView.setAlertText(msg,Color.RED,true);
         }else{
            topView.setAlertText(msg,0,true);
        }
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoaderUtil.allAppInfos);
            }
        });
        topTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                    AlertUtil.showThreeButtonAlertMsg(UIBarBlackListActivity.this, "请选择对染色锁定的操作", new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if (tag == 0) {
                                SharedPreferences.Editor ed = barPrefs.edit();
                                for (AppInfo ai : adapter.bjdatas) {
                                    if (!ai.isBarColorList){
                                        continue;
                                    }
                                    ed.putBoolean(ai.getPackageName() + "/locklist", true);
                                    ai.isBarLockList = true;
                                }
                                ed.commit();
                                adapter.notifyDataSetChanged();
                                showT("重启生效");
                            } else if (tag == 2) {
                                SharedPreferences.Editor ed = barPrefs.edit();
                                for (AppInfo ai : adapter.bjdatas) {
                                    ed.remove(ai.getPackageName() + "/locklist");
                                    ai.isBarLockList = false;
                                }
                                ed.commit();
                                adapter.notifyDataSetChanged();
                                showT("重启生效");
                            }
                        }
                    });
                return true;
            }
        });
        bottomTitle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertUtil.showThreeButtonAlertMsg(UIBarBlackListActivity.this, "请选择对开启染色的操作", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if (tag == 0) {
                            SharedPreferences.Editor ed = barPrefs.edit();
                            for (AppInfo ai : adapter.bjdatas) {
                                ed.putBoolean(ai.getPackageName() + "/colorlist", true);
                                ai.isBarColorList = true;
                            }
                            ed.commit();
                            adapter.notifyDataSetChanged();
                            showT("重启生效");
                        } else if (tag == 2) {
                            SharedPreferences.Editor ed = barPrefs.edit();
                            for (AppInfo ai : adapter.bjdatas) {
                                ed.remove(ai.getPackageName() + "/colorlist");
                                ai.isBarColorList = false;
                            }
                            ed.commit();
                            showT("重启生效");
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                return true;
            }
        });
        TitleClickListener tcl = new TitleClickListener();
        topTitle.setTag(0);
        bottomTitle.setTag(1);
        topTitle.setOnClickListener(tcl);
        bottomTitle.setOnClickListener(tcl);
        setTitle("颜色锁定及不染色名单");

//        FrameLayout fl = (FrameLayout)this.findViewById(R.id.barfl);
//        TextView memeTv = new TextView(this);
//        memeTv.setText("hello");
//        memeTv.setPadding(10, 10, 10, 10);
//        memeTv.setTextColor(Color.WHITE);
//        memeTv.setTextSize(40);
//        memeTv.setVisibility(View.VISIBLE);
//        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
//        fl.addView(memeTv,flp);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }
    class TitleClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            ArrayList<TextView> tvs = new ArrayList<TextView>();
            tvs.add(topTitle);
            tvs.add(bottomTitle);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.COLOR));
        }
    }
}

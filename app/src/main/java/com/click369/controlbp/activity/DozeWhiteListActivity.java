package com.click369.controlbp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.DozeWhiteListAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.util.ArrayList;

/**
 * Created by asus on 2017/5/27.
 */
public class DozeWhiteListActivity extends BaseActivity {
    private ListView list;
    private DozeWhiteListAdapter adapter;
    private TopSearchView topView;
    private TextView onTitle,offTitle,openStopTitle;//alertTv,closeTv,//serviceTv,wakelockTv,alarmTv,
    private SharedPreferences whiteListPrefs;
    public static  boolean isClick = false;
    public static int curColor = Color.BLACK;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = getLayoutInflater().inflate(R.layout.activity_dozewhitelist,null);
        setContentView(v);

        whiteListPrefs = SharedPrefsUtil.getPreferences(this,Common.PREFS_DOZELIST);//this.getApplicationContext().getSharedPreferences(Common.PREFS_DOZEWHITELIST, Context.MODE_WORLD_READABLE);
//        et = (EditText)this.findViewById(R.id.main_edittext);
        list = (ListView)this.findViewById(R.id.doze_whitelist_listview);
        onTitle = (TextView)this.findViewById(R.id.doze_title_on_tv);
        offTitle = (TextView)this.findViewById(R.id.doze_title_off_tv);
        openStopTitle = (TextView)this.findViewById(R.id.doze_title_openstop_tv);
//        AppCompatTextView menuTv = (AppCompatTextView) findViewById(R.id.main_menu_actv);
//        menuTv.setVisibility(View.VISIBLE);
        curColor = onTitle.getCurrentTextColor();
        adapter = new DozeWhiteListAdapter(this,whiteListPrefs);
        list.setAdapter(adapter);
        adapter.fliterList("u",MainActivity.allAppInfos);
        topView = new TopSearchView(this,v);
        topView.initView();
        String msg = "打盹白名单，不建议设置过多APP到白名单(最好不要超过5个)，否则就失去了打盹的意义，甚至还会导致更加耗电。加入前台运行的程序当打开该程序时暂停打盹退出或后台时重新进入打盹";
        topView.setAlertText(msg,0,true);
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,MainActivity.allAppInfos);
            }
        });
        TitleClickListener tcl = new TitleClickListener();
        offTitle.setTag(0);
        onTitle.setTag(1);
        openStopTitle.setTag(2);
        onTitle.setOnClickListener(tcl);
        offTitle.setOnClickListener(tcl);
        openStopTitle.setOnClickListener(tcl);
        setTitle("打盹白名单");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isClick){
            Intent intent = new Intent(this, WatchDogService.class);
            startService(intent);
            isClick = false;
        }
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
            tvs.add(offTitle);
            tvs.add(onTitle);
            tvs.add(openStopTitle);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.COLOR));
        }
    }
}

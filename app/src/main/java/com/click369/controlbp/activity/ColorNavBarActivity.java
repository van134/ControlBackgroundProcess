package com.click369.controlbp.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.click369.controlbp.R;
import com.click369.controlbp.adapter.ColorNavAppChooseAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.ColorNavBarService;
import com.click369.controlbp.service.NewWatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by asus on 2017/5/27.
 */
public class ColorNavBarActivity extends BaseActivity {
    private ListView list;
    private ColorNavAppChooseAdapter adapter;
    private TopSearchView topView;
//    private EditText et;
//    private FrameLayout alertFl;
    private TextView colorCloseTv;//alertTv,closeTv,//serviceTv,wakelockTv,alarmTv,
    private SharedPreferences settings;
    private int curColor = Color.BLACK;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View v = getLayoutInflater().inflate(R.layout.activity_colorbar,null);
        setContentView(v);
        settings =  SharedPrefsUtil.getInstance(this).settings;//SharedPrefsUtil.getPreferences(this,Common.PREFS_APPSETTINGS);//this.getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);

//        et = (EditText)this.findViewById(R.id.main_edittext);
        list = (ListView)this.findViewById(R.id.main_listview);
        colorCloseTv = (TextView)this.findViewById(R.id.main_colorclose_tv);
//        alertFl = (FrameLayout)this.findViewById(R.id.main_alert_fl);
//        alertTv = (TextView)this.findViewById(R.id.main_alert_tv);
//        closeTv = (TextView)this.findViewById(R.id.main_alert_closetv);
//        alertFl.setTag("colorbarmsg");
//        long time = settings.getLong(alertFl.getTag().toString(),0);
//        if(time==0||System.currentTimeMillis()-time>1000*60*60*24){
//            alertFl.setVisibility(View.VISIBLE);
//        }else{
//            alertFl.setVisibility(View.GONE);
//        }
        adapter = new ColorNavAppChooseAdapter(this);
        list.setAdapter(adapter);
        adapter.fliterList("u",appLoaderUtil.allAppInfos);
        topView = new TopSearchView(this,v);
        topView.initView();
        String msg = "不需要染色的应用透明度设置为0即可，部分应用无法染色。";
        curColor = ((TextView)findViewById(R.id.colorbar_title)).getCurrentTextColor();
        topView.setAlertText(msg,curColor,false);
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoaderUtil.allAppInfos);
            }
        });
        if(!settings.getBoolean(Common.ISCOLORBAROPEN,false)){
            list.setEnabled(false);
            colorCloseTv.setText("打开变色");
            showT("请点击打开变色才可使用");
        }
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppInfo ai = (AppInfo) adapter.getItem(position);
                Intent intent = new Intent(ColorNavBarActivity.this,ColorSetActivity.class);
                intent.putExtra("appname",ai.getAppName());
                intent.putExtra("apppkg",ai.getPackageName());
                startActivityForResult(intent,0x3);
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return true;
            }
        });

//        closeTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertFl.setVisibility(View.GONE);
//                SharedPreferences.Editor ed = settings.edit();
//                ed.putLong(alertFl.getTag().toString(),System.currentTimeMillis());
//                ed.commit();
//            }
//        });
        colorCloseTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtil.showConfirmAlertMsg(ColorNavBarActivity.this,settings.getBoolean(Common.ISCOLORBAROPEN,false)?"是否关闭导航栏染色？":"是否打开导航栏染色？", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag == 1){
                            if(settings.getBoolean(Common.ISCOLORBAROPEN,false)){
                                SharedPreferences.Editor ed = settings.edit();
                                ed.putBoolean(Common.ISCOLORBAROPEN,!settings.getBoolean(Common.ISCOLORBAROPEN,false));
                                ed.commit();
                                Intent intent = new Intent(ColorNavBarActivity.this,ColorNavBarService.class);
                                stopService(intent);
                                list.setEnabled(false);
                            }else{
                                SharedPreferences.Editor ed = settings.edit();
                                ed.putBoolean(Common.ISCOLORBAROPEN,!settings.getBoolean(Common.ISCOLORBAROPEN,false));
                                ed.commit();
                                Intent intent = new Intent(ColorNavBarActivity.this,ColorNavBarService.class);
                                startService(intent);
                                list.setEnabled(true);
                                if(!NewWatchDogService.isOpenNewDogService) {
//                                    OpenCloseUtil.closeOpenAccessibilitySettingsOn(ColorNavBarActivity.this,true);
                                }
                            }

                        }
                    }
                });
            }
        });

        this.setTitle("导航栏变色");
        File file = new File(FileUtil.FILEPATH,"navcolor");
        if(file.exists()){
            Object o = FileUtil.readObj(file.getAbsolutePath());
            if(o!=null){
                ColorNavBarService.appColors.clear();
                ColorNavBarService.appColors.putAll((HashMap<String,String>)o);
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if(settings.getBoolean(Common.ISCOLORBAROPEN,false)&&!NewWatchDogService.isOpenNewDogService){
            AlertUtil.showConfirmAlertMsg(this, "检测到无障碍服务没有自动开启成功，如果要导航栏变色生效，必须开启无障碍服务，如果进入后已开启则重复关闭开启一次，是否开启？", new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if(tag == 1){
                        Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        startActivity(accessibleIntent);
                    }else{
                        finish();
                    }
                }
            });
        }else{
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        adapter.notifyDataSetChanged();
        super.onActivityResult(requestCode, resultCode, data);
    }

}

package com.click369.controlbp.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;


import com.click369.controlbp.R;
import com.click369.controlbp.adapter.IceRoomAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.ShortCutUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by asus on 2017/5/27.
 */
public class IceRoomView{
    private GridView list;
    public IceRoomAdapter adapter;
//    private EditText et;
//    private FrameLayout alertFl;
//    private TextView alertTv,closeTv;
    private Handler h = new Handler();
    private BaseActivity act = null;
    private TopSearchView topView;
    public View  onCreate(LayoutInflater inflater,final BaseActivity cxt) {
        this.act = cxt;
        View v = inflater.inflate(R.layout.view_iceroom,null);
//        et = (EditText)v.findViewById(R.id.main_edittext);
//        alertFl = (FrameLayout)v.findViewById(R.id.main_alert_fl);
//        alertTv = (TextView)v.findViewById(R.id.main_alert_tv);
//        closeTv = (TextView)v.findViewById(R.id.main_alert_closetv);
        list = (GridView)v.findViewById(R.id.main_listview);
        adapter = new IceRoomAdapter(cxt);
        list.setAdapter(adapter);
        refresh(cxt);
        topView = new TopSearchView(cxt,v);
        topView.initView();
        String msg = "加入冷藏的应用无法启动，并且从桌面上消失，长按冷藏的应用显示操作菜单（移出冷藏，创建快捷方式），点击冷藏的应用则为打开该应用。打开冷藏的应用按返回键退出自动冷藏，HOME键保留后台。";
        topView.setAlertText(msg,0,false);
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,act.appLoaderUtil.allAppInfos);
            }
        });
//        alertFl.setTag("iceroommsg");
//        settings = act.getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
//        long time = settings.getLong(alertFl.getTag().toString(),0);
//        if(time==0||System.currentTimeMillis()-time>1000*60*60*24){
//            alertFl.setVisibility(View.VISIBLE);
//        }else{
//            alertFl.setVisibility(View.GONE);
//        }
//        closeTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertFl.setVisibility(View.GONE);
//                SharedPreferences.Editor ed = settings.edit();
//                ed.putLong(alertFl.getTag().toString(),System.currentTimeMillis());
//                ed.commit();
//            }
//        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AppInfo ai = (AppInfo)adapter.getItem(position);
                if(ai.isDisable){
                    if(ai.activityCount == 0){
                        Toast.makeText(cxt,"本应用没有可视化界面，所以无法打开",Toast.LENGTH_LONG).show();
                        return;
                    }
                    runIceApp(ai);
                }else{
                    AlertUtil.showConfirmAlertMsg(cxt, ai.isDisable ? "是否把" + ai.getAppName() + "移出冷藏室?" : "是否把" + ai.getAppName() + "加入冷藏室?", new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if(tag == 1){
                                ShellUtilNoBackData.execCommand("pm "+(ai.isDisable?"enable":"disable")+" "+ai.packageName);
                                ai.isDisable = !ai.isDisable;
                                adapter.freshList();
                            }
                        }
                    });
                }
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final AppInfo ai = (AppInfo)adapter.getItem(position);
                if(!ai.isDisable){
                    runIceApp(ai);
                }else{
                    String titles[] = {"移出冷藏室","运行该程序","创建快捷方式"};
                    AlertUtil.showListAlert(cxt,"请选择",titles,new AlertUtil.InputCallBack(){
                        @Override
                        public void backData(String txt, int tag) {
                            if(tag == 0){
                                ShellUtilNoBackData.execCommand("pm "+(ai.isDisable?"enable":"disable")+" "+ai.packageName);
                                ai.isDisable = !ai.isDisable;
                                adapter.freshList();
                            }else if(tag == 1){
                                runIceApp(ai);
                            }else if(tag == 2){
                                try {
                                    Drawable d = cxt.getPackageManager().getPackageInfo(ai.getPackageName(), PackageManager.GET_ACTIVITIES).applicationInfo.loadIcon(cxt.getPackageManager());

                                    BitmapDrawable bd = null ;
                                    if(d instanceof BitmapDrawable){
                                        bd = (BitmapDrawable) d ;
                                    }else if(d instanceof AdaptiveIconDrawable){
                                        AdaptiveIconDrawable ad = (AdaptiveIconDrawable) d ;
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            bd = (BitmapDrawable) (ad.getForeground());
                                        }
                                    }
                                    if (bd==null){
                                        Toast.makeText(cxt,"快捷方式创建失败",Toast.LENGTH_LONG).show();
                                    }else{
                                        ShortCutUtil.addShortcutDrawable(ai.getPackageName(),ai.appName,cxt,EmptyActivity.class, bd.getBitmap());
                                        Toast.makeText(cxt,"快捷方式创建成功",Toast.LENGTH_LONG).show();
                                    }
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });
                }

                return true;
            }
        });

//        et.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                String appName = et.getText().toString();
//                adapter.fliterList(appName,MainActivity.allAppInfos);
//            }
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
        return v;
    }

    public void refresh(Context cxt){
        if(act.appLoaderUtil.allAppInfos.size()==0){
            ArrayList<AppInfo> apps = AppInfo.readArrays(cxt);
            act.appLoaderUtil.allAppInfos.addAll(apps);
        }
        if(AppLoaderUtil.allHMAppIcons.size()<10){
            loadAppIcons();
        }
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.fliterList(adapter.fliterName,act.appLoaderUtil.allAppInfos);
            }
        },250);
    }

    ProgressDialog pd = null;
    AppInfo ai = null;
    int cout = 0;
    public void runIceApp(final AppInfo ai){
        if(ai.isDisable){
            pd = ProgressDialog.show(act, null, "正在解冻并启动，请稍等...", true, false);
//            WatchDogService.iceButOpenInfos.add(ai.getPackageName());
            AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(ai.getPackageName())?AppLoaderUtil.allAppStateInfos.get(ai.getPackageName()):new AppStateInfo();
            asi.isOpenFromIceRome = true;
            AppLoaderUtil.allAppStateInfos.put(ai.getPackageName(),asi);
            Intent intent = new Intent("com.click369.control.pms.enablepkg");
            intent.putExtra("pkg",ai.packageName);
            act.sendBroadcast(intent);
            ShellUtilNoBackData.execCommand("pm "+(ai.isDisable?"enable":"disable")+" "+ai.packageName);
            this.ai = ai;
//            if(!NewWatchDogService.isOpenNewDogService){
//                OpenCloseUtil.closeOpenAccessibilitySettingsOn(act,true);
//            }
            h.postDelayed(r,100);
            cout = 0;
        }else{
            OpenCloseUtil.doStartApplicationWithPackageName(ai.packageName,act);
            act.finish();
        }
    }
    Runnable r=  new Runnable() {
        @Override
        public void run() {
            try {
                OpenCloseUtil.doStartApplicationWithPackageName(ai.packageName,act);
                if(pd!=null&&pd.isShowing()){
                    try {
                        pd.dismiss();
                    }catch (Exception e){
                    }
                }
                act.finish();

            }catch (Exception e){
                cout++;
                if(cout>20){
                    cout = 0;
                    if(pd!=null&&pd.isShowing()){
                        try {
                            pd.dismiss();
                        }catch (Exception e1){
                        }
                    }
                    Toast.makeText(act,"该应用无法启动",Toast.LENGTH_LONG).show();
                }else{
                    h.postDelayed(r,300);
                }
            }
        }
    };

    private void loadAppIcons(){
        new Thread(){
            @Override
            public void run() {
                try {
//                    synchronized (MainActivity.this) {
                    final Set<String> pkgs = new HashSet<>();
                    pkgs.addAll(AppLoaderUtil.allHMAppInfos.keySet());
                    pkgs.add(Common.PACKAGENAME);
                    for (String p : pkgs) {
                        File f = new File(AppLoaderUtil.iconPath, p);
                        if (f.exists() && !AppLoaderUtil.allHMAppIcons.containsKey(p)) {
                            Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                            AppLoaderUtil.allHMAppIcons.put(p, bitmap);
                        }if(!f.exists()){
                            PackageInfo packgeInfo = act.getPackageManager().getPackageInfo(p,PackageManager.GET_META_DATA);
                            AppLoaderUtil.loadAppImage(packgeInfo,act.getPackageManager(),true);
                            f = new File(AppLoaderUtil.iconPath, p);
                            if (f.exists() && !AppLoaderUtil.allHMAppIcons.containsKey(p)) {
                                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                                AppLoaderUtil.allHMAppIcons.put(p, bitmap);
                            }
                        }
                    }
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(adapter!=null){
                                    adapter.notifyDataSetChanged();
                                }
                            }catch (Throwable e){
                                e.printStackTrace();
                            }
                        }
                    });
//                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}

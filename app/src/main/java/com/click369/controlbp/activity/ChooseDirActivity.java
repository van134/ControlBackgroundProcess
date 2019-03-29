package com.click369.controlbp.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.BuildConfig;
import com.click369.controlbp.R;
import com.click369.controlbp.adapter.AppStartAdapter;
import com.click369.controlbp.adapter.ChooseDirAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.bean.DirBean;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.service.XposedUtil;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import static com.click369.controlbp.activity.AppConfigActivity.setClipViewCornerRadius;
import static com.click369.controlbp.activity.AppConfigActivity.setWindowStatusBarColor;

public class ChooseDirActivity extends BaseActivity {
    ListView listView;
    TextView title,subtitle,backtitle,moreTitle;
    ChooseDirAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(MainActivity.isNightMode){
            setTheme(R.style.AppTheme_NoActionBarDarkFloat);
        }
        setContentView(R.layout.activity_floatchoosedir);
        setWindowStatusBarColor(this,R.color.transhalf);
        LinearLayout ll = (LinearLayout)this.findViewById(R.id.choosedir_main_ll);
        FrameLayout fl = (FrameLayout)this.findViewById(R.id.choosedir_title_fl);
        title = (TextView)this.findViewById(R.id.choosedir_title);
        subtitle = (TextView)this.findViewById(R.id.choosedir_sub_title);
        backtitle = (TextView)this.findViewById(R.id.choosedir_back_title);
        moreTitle = (TextView)this.findViewById(R.id.choosedir_input_title);
        moreTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            AlertUtil.inputTextAlert(ChooseDirActivity.this,2, new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if(!TextUtils.isEmpty(txt)){
                        String path = txt.replace(Environment.getExternalStorageDirectory().getAbsolutePath(),"");
                        if(path.startsWith(File.separator)){
                            path = path.substring(1);
                        }
                        Intent intent = new Intent("com.click369.newdir.send");
                        intent.putExtra("index",getIntent().getIntExtra("index",-1));
                        intent.putExtra("path",path);
                        intent.putExtra("add",true);
                        sendBroadcast(intent);
//                        setResult(0x10,intent);
//                        finish();
                    }else{
                        showT("内容不能为空");
                    }
                }
            });
            }
        });
        backtitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subtitleStr = subtitle.getText().toString();
                if(subtitleStr.equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
                    setResult(0x10,null);
                    finish();
                }else{
                    File f = new File(subtitleStr).getParentFile();
                    subtitle.setText(f.getAbsolutePath());
                    subtitleStr = subtitle.getText().toString();
                    if(subtitleStr.equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
                        backtitle.setText("关闭");
                    }else{
                        backtitle.setText("返回上一级");
                    }
                    adapter.setData(initList(f.getAbsolutePath()));
                }
            }
        });
        listView = (ListView)this.findViewById(R.id.choosedir_listview);
        setClipViewCornerRadius(ll,30);
        if(MainActivity.isNightMode){
            ll.setBackgroundColor(Color.BLACK);
            fl.setBackgroundColor(Color.DKGRAY);
        }else{
            ll.setBackgroundColor(Color.parseColor(MainActivity.THEME_BG_COLOR));
            fl.setBackgroundColor(Color.parseColor(MainActivity.THEME_COLOR));
        }
        adapter = new ChooseDirAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DirBean dirBean = (DirBean) adapter.getItem(position);
                if(!dirBean.isFile) {
                    backtitle.setText("返回上一级");
                    ArrayList<DirBean> list = initList(dirBean.allName);
                    subtitle.setText(dirBean.allName);
                    adapter.setData(list);
                }
            }
        });
        subtitle.setText(Environment.getExternalStorageDirectory().getAbsolutePath());
        backtitle.setText("关闭");
        adapter.setData(initList(Environment.getExternalStorageDirectory().getAbsolutePath()));
    }

    private ArrayList<DirBean> initList(String path){
        ArrayList<DirBean> dirs = new ArrayList<>();
        File file = new File(path);
        if(file.exists()&&file.isDirectory()){
            File files[] = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(pathname.getParent().equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
                        return !NewDirActivity.isContainsKeyWord(pathname.getName());
                    }else{
                        return true;
                    }
                }
            });
            ArrayList<DirBean> dirsTemp = new ArrayList<>();
            ArrayList<DirBean> filesTemp = new ArrayList<>();
            for(File f:files){
                if(f.isDirectory()){
                    DirBean dirBean = new DirBean();
                    dirBean.isFile = false;
                    dirBean.shortName = f.getName();
                    dirBean.allName = f.getAbsolutePath();
                    dirBean.file = f;
                    dirsTemp.add(dirBean);
                }else{
                    DirBean dirBean = new DirBean();
                    dirBean.isFile = true;
                    dirBean.shortName = f.getName();
                    dirBean.allName = f.getAbsolutePath();
                    dirBean.file = f;
                    filesTemp.add(dirBean);
                }
            }
            Collections.sort(dirsTemp, new Comparator<DirBean>() {
                @Override
                public int compare(DirBean o1, DirBean o2) {
                    Collator ca = Collator.getInstance(Locale.CHINA);
                    int flags = 0;
                    if (ca.compare( o1.shortName,o2.shortName) < 0) {
                        flags = -1;
                    }
                    else if(ca.compare(o1.shortName,o2.shortName) > 0) {
                        flags = 1;
                    }
                    else {
                        flags = 0;
                    }
                    return flags;
                }
            });
            Collections.sort(filesTemp, new Comparator<DirBean>() {
                @Override
                public int compare(DirBean o1, DirBean o2) {
                    Collator ca = Collator.getInstance(Locale.CHINA);
                    int flags = 0;
                    if (ca.compare( o1.shortName,o2.shortName) < 0) {
                        flags = -1;
                    }
                    else if(ca.compare(o1.shortName,o2.shortName) > 0) {
                        flags = 1;
                    }
                    else {
                        flags = 0;
                    }
                    return flags;
                }
            });
            dirs.addAll(dirsTemp);
            dirs.addAll(filesTemp);
        }
        return dirs;
    }

}

package com.click369.controlbp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.NewDirListAdapter;
import com.click369.controlbp.adapter.SkipDialogListAdapter;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.XposedUtil;
import com.click369.controlbp.util.AlertUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Created by asus on 2017/5/27.
 */
public class NewDirActivity extends BaseActivity {
    private ListView listView;
    private ImageView add,newdir_edit;
    private TextView alert,newdir_input;
    private NewDirListAdapter adapter;
    private FrameLayout swFl;
    private Switch isShowToastSw;
//    private TopSearchView topView;
    private SharedPreferences priPrefs;
    public static  boolean newDirAllSw = false;
    public static int curColor = Color.BLACK;
    public static String defaultNewDir = "zcache";

//    public ArrayList<String> lists = new ArrayList<String>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = getLayoutInflater().inflate(R.layout.activity_newdir,null);
        setContentView(v);
        priPrefs =sharedPrefs.privacyPrefs;// SharedPrefsUtil.getPreferences(this,Common.PREFS_SKIPDIALOG);
        newDirAllSw = priPrefs.getBoolean(Common.PREFS_PRIVATE_NEWDIR_ALLSWITCH,false);
        swFl = (FrameLayout) this.findViewById(R.id.skip_dialog_fl);
        isShowToastSw = (Switch) this.findViewById(R.id.newdir_allsw_sw);
        swFl.setVisibility(View.VISIBLE);
        isShowToastSw.setChecked(newDirAllSw);

        isShowToastSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                priPrefs.edit().putBoolean(Common.PREFS_PRIVATE_NEWDIR_ALLSWITCH,isChecked).commit();
                newDirAllSw = isChecked;
                adapter.notifyDataSetChanged();
                add.setEnabled(newDirAllSw);
                newdir_edit.setEnabled(newDirAllSw);
                add.setAlpha(newDirAllSw?1.0f:0.5f);
                newdir_edit.setAlpha(newDirAllSw?1.0f:0.5f);
            }
        });
        alert = (TextView) this.findViewById(R.id.skipdialog_alert);
        isShowToastSw.setTextColor(alert.getCurrentTextColor());
        listView = (ListView)this.findViewById(R.id.newdir_listview);
        newdir_edit = (ImageView) this.findViewById(R.id.newdir_edit);
        newdir_input = (TextView) this.findViewById(R.id.newdir_input);
        defaultNewDir =  priPrefs.getString("newDir","zcache");
        newdir_input.setText("编辑默认重定向文件夹，当前为"+defaultNewDir);
        newdir_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtil.inputAlertCustomer(NewDirActivity.this,"请输入默认重定向文件夹名(英文)","文件夹名称",defaultNewDir, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(!TextUtils.isEmpty(txt)){
                            if(isContainsKeyWord(txt)){
                                showT("不能包含系统公共文件夹，请重新命名");
                            }else{
                                defaultNewDir = txt;
                                priPrefs.edit().putString("newDir",txt).commit();
                                newdir_input.setText("编辑默认重定向文件夹，当前为"+txt);
                            }
                        }
                    }
                });
            }
        });
        add = (ImageView) this.findViewById(R.id.skipdialog_add);
        add.setOnClickListener(new AddClickListener());
        add.setEnabled(newDirAllSw);
        newdir_edit.setEnabled(newDirAllSw);
        add.setAlpha(newDirAllSw?1.0f:0.5f);
        newdir_edit.setAlpha(newDirAllSw?1.0f:0.5f);
//        curColor = onTitle.getCurrentTextColor();
        adapter = new NewDirListAdapter(this,priPrefs);
        adapter.bjdatas.clear();
        listView.setAdapter(adapter);
        HashSet<String> sets = (HashSet<String>)priPrefs.getStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>());
//        List<String> keys = new ArrayList<>(sets);

//        Collections.sort(keys, new Comparator<String>() {
//            @Override
//            public int compare(String o1, String o2) {
//                int flags = 0;
//                if (o1.length()<o2.length()) {
//                    flags = 1;
//                }
//                else if(o1.length()>o2.length()) {
//                    flags = -1;
//                }
//                else {
//                    flags = 0;
//                }
//                return flags;
//            }
//        });
//        for(String s:keys){
//            Log.i("FILE_TEST_",s);
//        }
        adapter.setData(new ArrayList<String>(sets));
        setTitle("全局存储重定向");
        IntentFilter intentFilter = new IntentFilter("com.click369.newdir.send");
        this.registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(broadcastReceiver);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent data) {
            if(data!=null&&data.hasExtra("add")){
                int index = data.getIntExtra("index",-1);
                String path = data.getStringExtra("path");
                if(!TextUtils.isEmpty(path)){
                    boolean isc = false;
                    for(String s:adapter.bjdatas){
                        if(s.startsWith(path+"|")||s.endsWith("|"+path)){
                            isc = true;
                            break;
                        }
                    }
                    if(isc){
                        showT("该内容已经被包含或重定向目标文件夹已存在，请重新选择");
                    }else if(isContainsKeyWord(path)){
                        showT("不能包含系统公共文件夹，请重新命名");
                    }else{
                        if(index==-1){
                            adapter.bjdatas.add(path+"|"+defaultNewDir+File.separator+path);
                        }else{
                            adapter.bjdatas.remove(index);
                            adapter.bjdatas.add(index,path+"|"+defaultNewDir+File.separator+path);
                        }
//                    adapter.bjdatas.addAll(priPrefs.getStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>()));
                        priPrefs.edit().putStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>(adapter.bjdatas)).commit();
                        adapter.notifyDataSetChanged();
                        showT("添加成功");
                    }
                }
                adapter.notifyDataSetChanged();
            }else if(data!=null&&data.hasExtra("remove")){
                String path = data.getStringExtra("path");
                if(!TextUtils.isEmpty(path)){
                    String searchPath = null;
                    for(String s:adapter.bjdatas){
                        if(s.startsWith(path+"|")){
                            searchPath = s;
                            break;
                        }
                    }
                    if(searchPath!=null){
                        adapter.bjdatas.remove(searchPath);
                        priPrefs.edit().putStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>(adapter.bjdatas)).commit();
                        adapter.notifyDataSetChanged();
                        showT("移除成功");
                    }
                }
            }
        }
    };
    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(data!=null){
//            int index = data.getIntExtra("index",-1);
//            String path = data.getStringExtra("path");
//            if(!TextUtils.isEmpty(path)){
//                boolean isc = false;
//                for(String s:adapter.bjdatas){
//                    if(s.contains(path)){
//                        isc = true;
//                        break;
//                    }
//                }
//                if(isc){
//                    showT("该内容已经被包含");
//                }else if(isContainsKeyWord(path)){
//                    showT("不能包含系统公共文件夹，请重新命名");
//                }else{
////                    adapter.bjdatas.clear();
//                    if(index==-1){
//                        adapter.bjdatas.add(path+"|"+defaultNewDir+File.separator+path);
//                    }else{
//                        adapter.bjdatas.remove(index);
//                        adapter.bjdatas.add(index,path+"|"+defaultNewDir+File.separator+path);
//                    }
////                    adapter.bjdatas.addAll(priPrefs.getStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>()));
//                    priPrefs.edit().putStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>(adapter.bjdatas)).commit();
//                    adapter.notifyDataSetChanged();
//                    showT("添加成功");
//                }
//            }
//
//            adapter.notifyDataSetChanged();
//        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    class AddClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(NewDirActivity.this,ChooseDirActivity.class);
            intent.putExtra("index",-1);
            startActivity(intent);
//            startActivityForResult(intent,0x10);
//            AlertUtil.inputTextAlert(NewDirActivity.this,2, new AlertUtil.InputCallBack() {
//                @Override
//                public void backData(String txt, int tag) {
//                    if(!TextUtils.isEmpty(txt)){
//                        boolean isc = false;
//                        for(String s:adapter.bjdatas){
//                            if(s.contains(txt)){
//                                isc = true;
//                                break;
//                            }
//                        }
//                        if(isc){
//                            showT("该内容已经被包含");
//                        }else if(isContainsKeyWord(txt)){
//                            showT("不能包含系统公共文件夹，请重新命名");
//                        }else{
//                            adapter.bjdatas.clear();
//                            adapter.bjdatas.add(txt+"|zcache"+File.separator+txt);
//                            adapter.bjdatas.addAll(priPrefs.getStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>()));
//                            priPrefs.edit().putStringSet(Common.PREFS_PRIVATE_NEWDIR_KEYWORDS,new LinkedHashSet<String>(adapter.bjdatas)).commit();
//                            adapter.notifyDataSetChanged();
//                        }
//                    }
//                }
//            });
        }
    }

    public static boolean isContainsKeyWord(String s){
        return s.equals("Android")||
                s.equals("data")||
                s.equals("Pictures")||
                s.equals("DCIM")||
                s.equals("Download")||
                s.equals("processcontrol")||
                s.equals("backup");
    }


}

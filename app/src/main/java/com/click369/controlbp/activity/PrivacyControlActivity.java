package com.click369.controlbp.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.IFWCompActBroadAdapter;
import com.click369.controlbp.adapter.IFWCompServiceAdapter;
import com.click369.controlbp.adapter.PrivacyControlAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.bean.PrivacyInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.ShellUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PrivacyControlActivity extends BaseActivity {
    private ListView list;
    private TextView disableAllTv,notDisableTv,showAllNameTv,showAlertTv,alertTv;
    private EditText et;
    private FrameLayout alertFl;
    private String appName = "",pkg= "";
    private PrivacyControlAdapter adapter;
    public static int curColor = Color.BLACK;
    public static long setTime = 0;
    public SharedPreferences priPrefs;
    public boolean isChange = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacycontrol);
        priPrefs = sharedPrefs.privacyPrefs;//SharedPrefsUtil.getPreferences(this,Common.PREFS_APPIFWCOUNT);// getApplicationContext().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
        Intent intent= this.getIntent();
        appName = intent.getStringExtra("name");
        pkg = intent.getStringExtra("pkg");
        setTitle(appName+"的权限控制");
        list = (ListView)this.findViewById(R.id.ifwcom_listview);
        disableAllTv = (TextView)this.findViewById(R.id.ifwcomp_disableall);
        notDisableTv = (TextView)this.findViewById(R.id.ifwcomp_notdisableall);
        showAllNameTv = (TextView)this.findViewById(R.id.ifwcomp_showallname);
        showAlertTv = (TextView)this.findViewById(R.id.ifwcomp_showalert);
        alertTv = (TextView)this.findViewById(R.id.ifwcomp_alert_tv);
        alertFl = (FrameLayout) this.findViewById(R.id.ifwcomp_alert_fl);
        curColor = showAllNameTv.getCurrentTextColor();
        et = (EditText) this.findViewById(R.id.ifwcomp_et);
        et.setTextColor(curColor);
        alertFl.setVisibility(View.GONE);
        setTime =  priPrefs.getLong(pkg+"/changetime",0);
        adapter = new PrivacyControlAdapter(this);
        list.setAdapter(adapter);
        list.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = motionEvent.getRawX();
                return false;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                if (x<p.x*3/4){
                    showT("请点击列表右侧");
                    return;
                }
                String data = Common.PRIVACY_KEYS[position];
                boolean isDiable = adapter.bjdatas.contains(data);
                if(isDiable){
                    adapter.bjdatas.remove(data);
                }else{
                    adapter.bjdatas.add(data);
                }
                isChange = true;
                priPrefs.edit().putStringSet(pkg+"/prilist",adapter.bjdatas).commit();
                adapter.notifyDataSetChanged();
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(position == Common.PRI_TYPE_CHANGELOC){
                    if(!adapter.bjdatas.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_CHANGELOC])){
                        showT("请先开启开关");
                        return true;
                    }
                    Intent intent1 = new Intent(PrivacyControlActivity.this,ChangeLocActivity.class);
                    intent1.putExtra("pkg",pkg);
                    String lat = priPrefs.getString(pkg+"/lat","39.916803");
                    String lon = priPrefs.getString(pkg+"/lon","116.403766");
                    boolean isrechange = priPrefs.getBoolean(pkg+"/isrechange",false);
                    intent1.putExtra("lon",lon);
                    intent1.putExtra("lat",lat);
                    intent1.putExtra("isrechange",isrechange);
                    intent1.putExtra("name",appName);
                    PrivacyControlActivity.this.startActivityForResult(intent1,0x11);
                }else if(position == Common.PRI_TYPE_CHANGETIME){
                    if(!adapter.bjdatas.contains(Common.PRIVACY_KEYS[Common.PRI_TYPE_CHANGETIME])){
                        showT("请先开启开关");
                        return true;
                    }
                    String value = setTime==0?"":new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(setTime));
                    AlertUtil.inputAlertCustomer(PrivacyControlActivity.this, "输入时间", "格式:年-月-日 时:分", value, new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if (tag==0){
                                if(!TextUtils.isEmpty(txt)){
                                    try {
                                        Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(txt);
                                        setTime = date.getTime();
                                        priPrefs.edit().putLong(pkg+"/changetime",setTime).commit();
                                        adapter.notifyDataSetChanged();
                                        showT("设置成功");
                                    } catch (Exception e) {
                                        showT("格式错误，请重新设置");
                                    }
                                }else{
                                    showT("输入为空");
                                }
                            }
                        }
                    });
                }
                return true;
            }
        });
        alertTv.setText("1.自定义位置：可以通过修改各种定位方式（基站、AGPS、WIFI、GPS）来控制应用获取位置，把位置替换为自己定义的位置。开启后默认位置为北京故宫。\n" +
                "2.自定义时间：开启后当应用获取手机时间时给其返回自己设置的时间，如果未设置则以1970年开始。\n" +
                "3.获取GPS位置：仅仅禁止GPS和AGPS的数据获取但是应用可以使用其他方式的定位。\n" +
                "4.获取基站信息：仅仅禁止基站的数据，可防止通过基站定位。\n" +
                "5.获取WIFI及MAC信息：禁止获取WIFI信息及MAC地址，WIFI信息可以定位MAC地址是手机的唯一标志。\n" +
                "6.获取正在运行程序列表：防止应用检测运行程序。\n" +
                "7.获取已安装应用列表：防止应用检测安装程序，但是一些重要的应用他们总有办法检测出来。\n" +
                "8.获取手机识别码：防止应用获取各种识别码来对手机进行标识。");
        showAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
            }
        });
        showAllNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        disableAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtil.showConfirmAlertMsg(PrivacyControlActivity.this, "禁用后如果本应用出现问题请取消禁用，是否禁用全部？重启进程生效", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag == 1){
                            for(String key:Common.PRIVACY_KEYS){
                                adapter.bjdatas.add(key);
                            }
                            priPrefs.edit().putStringSet(pkg+"/prilist",adapter.bjdatas).commit();
                            adapter.notifyDataSetChanged();
                            isChange = true;
                        }
                    }
                });
            }
        });
        notDisableTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtil.showConfirmAlertMsg(PrivacyControlActivity.this, "是否启用全部？重启进程生效", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag == 1){
                            adapter.bjdatas.clear();
                            priPrefs.edit().putStringSet(pkg+"/prilist",adapter.bjdatas).commit();
                            adapter.notifyDataSetChanged();
                            isChange = true;
                        }
                    }
                });
            }
        });
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Set<String> priList = priPrefs.getStringSet(pkg+"/prilist",new HashSet<String>());
        adapter.setData(priList);
    }
    boolean isActStop = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data!=null&&data.hasExtra("lat")){
            String lat = data.getStringExtra("lat");//,"116.403766,39.916803"
            String lon = data.getStringExtra("lon");//,"116.403766,39.916803"
            boolean isrechange = data.getBooleanExtra("isrechange",false);//,"116.403766,39.916803"
            priPrefs.edit().putString(pkg+"/lon",lon).commit();
            priPrefs.edit().putString(pkg+"/lat",lat).commit();
            priPrefs.edit().putBoolean(pkg+"/isrechange",isrechange).commit();
            showT("位置修改成功");
            isChange = true;
        }else{
            showT("位置未修改");
        }
    }

    @Override
    public void onBackPressed() {
        if(isChange){
            isChange =false;
            AppInfo ai = AppLoaderUtil.allHMAppInfos.get(pkg);
            if(ai!=null&&ai.isRunning){
                AlertUtil.showConfirmAlertMsg(PrivacyControlActivity.this, "设置改变后重启应用生效，是否杀死" + appName + "？", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag == 1){
                            XposedStopApp.stopApk(pkg,PrivacyControlActivity.this);
                        }
                        finish();
                    }
                });
                return;
            }

        }
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}

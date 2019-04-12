package com.click369.controlbp.activity;

import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.IFWCompActBroadAdapter;
import com.click369.controlbp.adapter.IFWCompServiceAdapter;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.ShellUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class IFWCompActivity extends BaseActivity {
    public static final String EXT_SERVICE = "_service";
    public static final String EXT_BROADCASE = "_broadcast";
    public static final String EXT_ACTIVITY = "_activity";
    private ListView list;
    private TextView disableAllTv,notDisableTv,showAllNameTv,showAlertTv,alertTv;
    private EditText et;
    private FrameLayout alertFl;
    private String appName,pkg;
    private BaseAdapter adapter;
    private int type = 0;//服务 活动 广播
    private String titles[] = {"服务","广播","活动"};
    public String ifwString = "";
    public static HashSet<String> runServices = new HashSet<String>();
    private boolean isStop = false;
    private boolean isSelOpen = false;

    public ArrayList<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();
    public ArrayList<ActivityInfo> activityInfos = new ArrayList<ActivityInfo>();
    private SharedPreferences ifwCountPrefs;
    public static int curColor = Color.BLACK;
    private   PowerManager powerManager;
    PowerManager.WakeLock mWakeLock = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ifwcomp);
        isSelOpen = SELinuxUtil.isSELOpen();

        if(isSelOpen){
            SELinuxUtil.closeSEL();
        }
        ifwCountPrefs = sharedPrefs.ifwCountPrefs;//SharedPrefsUtil.getPreferences(this,Common.PREFS_APPIFWCOUNT);// getApplicationContext().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
        Intent intent= this.getIntent();
        appName = intent.getStringExtra("name");
        pkg = intent.getStringExtra("pkg");
        type = intent.getIntExtra("type",0);
        isStop = false;
        setTitle(appName+"的"+titles[type]+"列表");
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
        powerManager =(PowerManager)getSystemService(POWER_SERVICE);

        if(type == 0){
            alertTv.append("如果设置并重启后还出现已禁用但正在运行，请重启，禁用服务功能不仅使用了IFW防火墙还使用了XPOSED的HOOK压制，30个以上的组件不提供禁用全部功能。");
            adapter = new IFWCompServiceAdapter(this,getPackageManager());
            new Thread(){
                @Override
                public void run() {
                    ServiceInfo sis[] = PackageUtil.getServicesByPkg(IFWCompActivity.this,pkg);
                    if(sis!=null&&sis.length>0) {
                        for (ServiceInfo si : sis) {
                            serviceInfos.add(si);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((IFWCompServiceAdapter)adapter).setData(serviceInfos);
//                            if (serviceInfos.size()>=50){
//                                disableAllTv.setEnabled(false);
//                            }
                        }
                    });
                    ifwString = FileUtil.readIFWList(pkg+EXT_SERVICE);
                    runServices.clear();
                    runServices.addAll(PackageUtil.getServicesName(pkg));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((IFWCompServiceAdapter)adapter).setData(serviceInfos);
                        }
                    });
                }
            }.start();

        }else {
            adapter = new IFWCompActBroadAdapter(this,getPackageManager());
            new Thread(){
                @Override
                public void run() {

                    ActivityInfo acts[] = (type==1?PackageUtil.getReceiverByPkg(IFWCompActivity.this,pkg):PackageUtil.getActivityByPkg(IFWCompActivity.this,pkg));
                    if(acts!=null&&acts.length>0){
                        for(ActivityInfo si:acts){
                            activityInfos.add(si);
//                Log.i("CONTROL","si.permission "+);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((IFWCompActBroadAdapter)adapter).setData(activityInfos);
//                            if (activityInfos.size()>30){
//                                disableAllTv.setEnabled(false);
//                            }
                        }
                    });
                    ifwString = FileUtil.readIFWList(pkg+(type==1?EXT_BROADCASE:EXT_ACTIVITY));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((IFWCompActBroadAdapter)adapter).setData(activityInfos);
                        }
                    });
                }
            }.start();
//            ((IFWCompActBroadAdapter)adapter).setIFW(ifwString);
        }
        Log.i("CONTROL","ifwString   "+ifwString);
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
                if (IFWFragment.ai==null){
                    showT("对象为空，请退出重新进入该界面");
                    return;
                }

                ((ProgressBar)view.findViewById(R.id.item_ifw_pb)).setVisibility(View.VISIBLE);
                new Thread(){
                    @Override
                    public void run() {
                        synchronized (IFWFragment.ai) {
                            String dataName = null;
                            String pkgName = null;
                            String ext = null;
                            if (isFinishing()){
                                return;
                            }
                            if (position>=adapter.getCount()){
                                return;
                            }
                            if (type == 0) {
                                ServiceInfo si = (ServiceInfo) adapter.getItem(position);
                                dataName = si.name;//.replaceAll("\\$","/\\$");
                                pkgName = si.packageName;
                                ext = EXT_SERVICE;
                            } else {
                                ActivityInfo si = (ActivityInfo) adapter.getItem(position);
                                dataName = si.name;//.replaceAll("\\$","/\\$");
                                pkgName = si.packageName;
                                ext = type == 1 ? EXT_BROADCASE : EXT_ACTIVITY;
                            }
                            if(!isStop){
                                isStop = true;
                                XposedStopApp.stopApk(pkgName,IFWCompActivity.this);
                            }

                            boolean isDisable = ifwString.contains(dataName) || !PackageUtil.isEnable(pkgName, dataName, getPackageManager());
                            ShellUtils.execCommand("pm " + (isDisable ? "enable" : "disable") + " " + pkgName + "/" + dataName, true, true);

//                            TextView tv = (TextView) (view.findViewById(R.id.item_ifw_stop));
                            if (isDisable) {
                                if (ifwString.contains(dataName)) {
                                    FileUtil.writeIFWList(pkgName + ext, pkgName, dataName, type);
                                    ifwString = ifwString.replace(dataName, "");
                                }
                                if (type == 0) {
//                                    SharedPreferences sp = getSharedPreferences(pkgName + IFWCompActivity.EXT_SERVICE, Context.MODE_WORLD_READABLE);
//                                    SharedPreferences.Editor se = sp.edit();
//                                    se.remove(dataName);
//                                    se.commit();
                                    IFWFragment.ai.serviceDisableCount--;
                                    if(IFWFragment.ai.serviceDisableCount < 0){
                                        IFWFragment.ai.serviceDisableCount = 0;
                                    }
                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwservice",IFWFragment.ai.serviceDisableCount).commit();
                                } else if (type == 1) {
                                    IFWFragment.ai.broadCastDisableCount--;
                                    if(IFWFragment.ai.broadCastDisableCount < 0){
                                        IFWFragment.ai.broadCastDisableCount = 0;
                                    }
                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwreceiver",IFWFragment.ai.broadCastDisableCount).commit();
                                } else if (type == 2) {
                                    IFWFragment.ai.activityDisableCount--;
                                    if(IFWFragment.ai.activityDisableCount < 0){
                                        IFWFragment.ai.activityDisableCount = 0;
                                    }
                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwactivity",IFWFragment.ai.activityDisableCount).commit();
                                }
                            } else {
                                if (!ifwString.contains(dataName)) {
                                    FileUtil.writeIFWList(pkgName + ext, pkgName, dataName, type);
                                    ifwString += dataName;
                                }
                                if (type == 0) {
                                    String runName = getContainsName(dataName);
                                    Log.i("CONTROL","runName   "+runName);
                                    if(runName!=null){
                                        runServices.remove(runName);
                                    }
                                    IFWFragment.ai.serviceDisableCount++;
                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwservice",IFWFragment.ai.serviceDisableCount).commit();
                                } else if (type == 1) {
                                    IFWFragment.ai.broadCastDisableCount++;
                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwreceiver",IFWFragment.ai.broadCastDisableCount).commit();
                                } else if (type == 2) {
                                    IFWFragment.ai.activityDisableCount++;
                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwactivity",IFWFragment.ai.activityDisableCount).commit();
                                }
                            }
//                            if (isDisable) {
                                //                            }
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    view.findViewById(R.id.item_ifw_pb).setVisibility(View.GONE);
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }.start();
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String name = null;
                if(type == 0) {
                    ServiceInfo si = (ServiceInfo) adapter.getItem(position);
                    name = si.name;
                }else{
                    ActivityInfo si = (ActivityInfo)adapter.getItem(position);
                    name = si.name;
                }
//                final String copy = pkg+"/"+name;
//                AlertUtil.showAllNameAlertMsg(IFWCompActivity.this, "完整名称\n" + name, new AlertUtil.InputCallBack() {
//                    @Override
//                    public void backData(String txt, int tag) {
//                        if(tag==1){
                            ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                            // 将文本内容放到系统剪贴板里。
                            cm.setText(pkg+"/"+name);
                            Toast.makeText(IFWCompActivity.this,"完整包名及组件名已复制到你的粘贴板",Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
                return true;
            }
        });
        showAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
            }
        });
        showAllNameTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IFWFragment.isShowAllName = !IFWFragment.isShowAllName;
                showAllNameTv.setTextColor(IFWFragment.isShowAllName?Color.parseColor("#40d0b7"):curColor);
                adapter.notifyDataSetChanged();
            }
        });
        showAllNameTv.setTextColor(IFWFragment.isShowAllName?Color.parseColor("#40d0b7"):curColor);
        disableAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtil.showConfirmAlertMsg(IFWCompActivity.this, "禁用后如果本应用出现问题请选择启用全部，禁用后部分组件重启手机生效", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag == 1){
                            final ProgressDialog pd = ProgressDialog.show(IFWCompActivity.this,"","正在禁用，请稍后",true,false);
                            new Thread(){
                                @Override
                                public void run() {
                                    try{
                                        if(powerManager!=null){
                                            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"WakeLock");
                                            mWakeLock.acquire();
                                        }
                                        if(type == 0){
                                            ArrayList<ServiceInfo> sis = ((IFWCompServiceAdapter)adapter).bjdatas;
                                            int i = 0;
                                            for(final ServiceInfo si:sis){
                                                if (isActStop){
                                                    break;
                                                }
                                                if(!ifwString.contains(si.name)){
                                                    h.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                           pd.setMessage("正在禁用"+si.name);
                                                        }
                                                    });
                                                    FileUtil.writeIFWList(si.packageName+EXT_SERVICE,si.packageName,si.name,type);
                                                    ShellUtils.execCommand("pm disable " + si.packageName + "/" + si.name, true, true);
                                                    ifwString+= si.name;
                                                    i++;
                                                }
                                            }
                                            IFWFragment.ai.serviceDisableCount +=i;
    //                                        IFWFragment.ai.serviceDisableCount =  IFWFragment.ai.serviceCount;
                                            ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwservice",IFWFragment.ai.serviceDisableCount).commit();
                                        }else{
                                            int i = 0;
                                            ArrayList<ActivityInfo> sis = ((IFWCompActBroadAdapter)adapter).bjdatas;
                                            for(final ActivityInfo si:sis){
                                                if (isActStop){
                                                    break;
                                                }
                                                if(!ifwString.contains(si.name)){
                                                    h.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            pd.setMessage("正在禁用"+si.name);
                                                        }
                                                    });
                                                    ShellUtils.execCommand("pm disable " + si.packageName + "/" + si.name, true, true);
                                                    FileUtil.writeIFWList(si.packageName+(type==1?EXT_BROADCASE:EXT_ACTIVITY),si.packageName,si.name,type);
                                                    ifwString+= si.name;
                                                    i++;
                                                }
                                            }
                                            if(type == 1){
    //                                            IFWFragment.ai.broadCastDisableCount =  IFWFragment.ai.broadCastCount;
                                                IFWFragment.ai.broadCastDisableCount +=i;
                                                ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwreceiver",IFWFragment.ai.broadCastDisableCount).commit();
                                            }else{
    //                                            IFWFragment.ai.activityDisableCount =  IFWFragment.ai.activityCount;
                                                IFWFragment.ai.activityDisableCount +=i;
                                                ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwactivity",IFWFragment.ai.activityDisableCount).commit();
                                            }
                                        }
                                        h.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showT("禁用完成，请重启手机");
                                                if(pd!=null&&pd.isShowing()){
                                                    pd.dismiss();
                                                }
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                        if(mWakeLock!=null){
                                            mWakeLock.release();
                                        }

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    }
                });
            }
        });
        notDisableTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtil.showConfirmAlertMsg(IFWCompActivity.this, "是否启用全部？重启手机生效", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag == 1){

                            final ProgressDialog pd = ProgressDialog.show(IFWCompActivity.this,"","正在启用，请稍候",true,false);
                            new Thread(){
                                @Override
                                public void run() {
                                    try{
                                        if(powerManager!=null){
                                            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK,"WakeLock");
                                            mWakeLock.acquire();
                                        }
                                        PackageManager pm = getPackageManager();
                                        if(type == 0){
                                            FileUtil.delIFWList(pkg+EXT_SERVICE);
                                            ArrayList<ServiceInfo> sis = ((IFWCompServiceAdapter)adapter).bjdatas;
                                            for(ServiceInfo si:sis){
                                                if (isActStop){
                                                    break;
                                                }
                                                if(ifwString.contains(si.name)||!PackageUtil.isEnable(si.packageName,si.name,pm)){
                                                    ShellUtils.execCommand("pm enable " + si.packageName + "/" + si.name, true, true);
                                                    IFWFragment.ai.serviceDisableCount--;
                                                }
                                            }
                                            if(isActStop){
                                                if(IFWFragment.ai.serviceDisableCount < 0){
                                                    IFWFragment.ai.serviceDisableCount = 0;
                                                }
                                                ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwservice",IFWFragment.ai.serviceDisableCount).commit();
                                            }else{
                                                IFWFragment.ai.serviceDisableCount = 0;
                                                ifwCountPrefs.edit().remove(IFWFragment.ai.getPackageName()+"/ifwservice").commit();
                                            }

                                        }else{
                                            FileUtil.delIFWList(pkg+(type==1?EXT_BROADCASE:EXT_ACTIVITY));
                                            ArrayList<ActivityInfo> sis = ((IFWCompActBroadAdapter)adapter).bjdatas;
                                            for(ActivityInfo si:sis){
                                                if (isActStop){
                                                    break;
                                                }
                                                if(ifwString.contains(si.name)||!PackageUtil.isEnable(si.packageName,si.name,pm)){
                                                    ShellUtils.execCommand("pm enable " + si.packageName + "/" + si.name, true, true);
                                                    if(type == 1){
                                                        IFWFragment.ai.broadCastDisableCount--;
                                                    }else{
                                                        IFWFragment.ai.activityDisableCount--;
                                                    }
                                                }
                                            }
                                            if(!isActStop){
                                                if(type == 1){
                                                    IFWFragment.ai.broadCastDisableCount = 0;
                                                    ifwCountPrefs.edit().remove(IFWFragment.ai.getPackageName()+"/ifwreceiver").commit();
                                                }else{
                                                    IFWFragment.ai.activityDisableCount = 0;
                                                    ifwCountPrefs.edit().remove(IFWFragment.ai.getPackageName()+"/ifwactivity").commit();
                                                }
                                            }else{
                                                if(type == 1){
                                                    if(IFWFragment.ai.broadCastDisableCount<0){
                                                        IFWFragment.ai.broadCastDisableCount = 0;
                                                    }
                                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwreceiver",IFWFragment.ai.broadCastDisableCount).commit();
                                                }else{
                                                    if(IFWFragment.ai.activityDisableCount<0){
                                                        IFWFragment.ai.activityDisableCount = 0;
                                                    }
                                                    ifwCountPrefs.edit().putInt(IFWFragment.ai.getPackageName()+"/ifwactivity",IFWFragment.ai.activityDisableCount).commit();
                                                }
                                            }
                                        }
                                        ifwString = "";
                                        h.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                showT("启用完成");
                                                if(pd!=null&&pd.isShowing()){
                                                    pd.dismiss();
                                                }
                                                adapter.notifyDataSetChanged();
                                            }
                                        });
                                        if(mWakeLock!=null){
                                            mWakeLock.release();
                                        }

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }.start();
                        }
                    }
                });
            }
        });
        final ImageView clearIv = (ImageView)findViewById(R.id.top_clear_iv);
        clearIv.setVisibility(View.INVISIBLE);
        clearIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearIv.setVisibility(View.INVISIBLE);
                et.setText("");
            }
        });
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String appName = et.getText().toString();

                    if(appName.trim().length()==0){
                        IFWFragment.isShowAllName = false;
                        showAllNameTv.setTextColor(IFWFragment.isShowAllName?Color.parseColor("#40d0b7"):curColor);
                        if(type == 0) {
                            ((IFWCompServiceAdapter) adapter).setData(serviceInfos);
                        }else{
                            ((IFWCompActBroadAdapter) adapter).setData(activityInfos);
                        }
                    }else{
                        IFWFragment.isShowAllName = true;
                        showAllNameTv.setTextColor(IFWFragment.isShowAllName?Color.parseColor("#40d0b7"):Color.BLACK);
                        if(type == 0) {
                            ArrayList<ServiceInfo> sis = new ArrayList<ServiceInfo>();
                            for (ServiceInfo si : serviceInfos) {
                                if (si.name.toLowerCase().contains(appName.trim().toLowerCase())) {
                                    sis.add(si);
                                }
                            }
                            ((IFWCompServiceAdapter) adapter).setData(sis);
                        }else{
                            ArrayList<ActivityInfo> acts = new ArrayList<ActivityInfo>();
                            for (ActivityInfo si : activityInfos) {
                                if (si.name.toLowerCase().contains(appName.trim().toLowerCase())) {
                                    acts.add(si);
                                }
                            }
                            ((IFWCompActBroadAdapter) adapter).setData(acts);
                        }
                    }
            }
            @Override
            public void afterTextChanged(Editable s) {
                clearIv.setVisibility(et.getText().toString().length()>0?View.VISIBLE:View.INVISIBLE);
            }
        });
    }
    boolean isActStop = false;

    @Override
    protected void onStart() {
        super.onStart();
        isActStop = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActStop = true;
        try{
            if(mWakeLock!=null){
                mWakeLock.release();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isSelOpen){
            SELinuxUtil.openSEL();
        }
    }
    private String getContainsName(String name){
        String s ="";
        if(name.indexOf(".")>-1){
            s = name.substring(name.lastIndexOf(".")+1).trim();
        }else{
            s = name;
        }
        for(String s1:IFWCompActivity.runServices){
            if(s1.contains(s)){
                return s1;
            }
        }
        return null;
    }
}

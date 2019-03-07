package com.click369.controlbp.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
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
import com.click369.controlbp.activity.IFWCompActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.TopSearchView;
import com.click369.controlbp.adapter.IFWAdapter;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.common.ContainsKeyWord;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShellUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IFWFragment extends BaseFragment {
    public IFWAdapter adapter;
    private ListView listView;
    private TextView serviceTv,broadTv,actTv;
    private TopSearchView topView;
    private SharedPreferences settings,ifwCountPrefs;//modPrefs,
    public static int curColor = Color.BLACK;
    private Handler h = new Handler();
    public static AppInfo ai;
    private boolean isOpen;
    public IFWFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ifw, container, false);

        initView(v);
        return v;
    }

    public void onDestroyView(){
        super.onDestroyView();
        if(isOpen){
            new Thread(){
                @Override
                public void run() {
                    SELinuxUtil.openSEL();
                }
            }.start();
        }
    }

    @SuppressLint("WorldReadableFiles")
    private void initView(View v){
        ifwCountPrefs = SharedPrefsUtil.getInstance(getActivity()).ifwCountPrefs;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_APPIFWCOUNT);//this.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
        settings = SharedPrefsUtil.getInstance(getActivity()).settings;//SharedPrefsUtil.getPreferences(this.getActivity(),Common.PREFS_APPSETTINGS);//tthis.getActivity().getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        listView = (ListView)v.findViewById(R.id.main_listview);
        serviceTv = (TextView) v.findViewById(R.id.main_service_tv);
        broadTv = (TextView)v.findViewById(R.id.main_broad_tv);
        actTv = (TextView)v.findViewById(R.id.main_act_tv);
        curColor = actTv.getCurrentTextColor();
        adapter = new IFWAdapter(this.getContext());
        listView.setAdapter(adapter);
        BaseActivity.addListClickListener(listView,adapter,getActivity());
        topView = new TopSearchView(this.getActivity(),v);
        topView.initView();
        String msg = "1.该功能和功能一的区别在于功能一选择后被禁应用的所有的服务唤醒锁定时器都无法使用而且依赖XPOSED，而本功能是可选择的禁用，并且加入IFW即使卸载应用也不会失效,操作后最好重启一次手机。\n2.IFW禁用后如果该应用出现问题请启用被禁用的组件并重启手机。\n3.部分应用的组件是应用自己禁用的使用的时候才会打开，即使你没有操作也会显示某个组件被禁用，这是正常的。";
        topView.setAlertText(msg, 0,false);
        topView.setListener(new TopSearchView.CallBack() {
            @Override
            public void backAppType(String appName) {
                adapter.fliterList(appName,appLoader.allAppInfos);
            }
        });
        TitleClickListener listener = new TitleClickListener();
        serviceTv.setOnClickListener(listener);
        broadTv.setOnClickListener(listener);
        actTv.setOnClickListener(listener);
        fresh();
        loadY(listView,this.getClass(),adapter.sortType);
        loadDisableCount();
//        setAlarmWithCode("com.click369.control.settimestopapp","mm",5,1);
//        setAlarmWithCode("com.click369.control.settimestopapp","dd",20,2);
//        setAlarmWithCode("com.click369.control.settimestopapp","cc",45,30);
//        setAlarmWithCode("com.click369.control.settimestopapp","gg",105,4);
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
//        loadDisableCount();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(!hidden){
           fresh();
            loadY(listView,this.getClass(),adapter.sortType);
        }
    }

    @Override
    public void onStop() {
//        if(pd!=null&&pd.isShowing()){
//            pd.dismiss();
//        }
        super.onStop();
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        adapter.notifyDataSetChanged();
//    }

    private boolean isStrart = false;
    public void loadDisableCount(){
        if(isStrart||!settings.getBoolean(Common.PREFS_NAME_IFWCHANGE,true)){
            return;
        }
        new Thread() {
            @Override
            public void run() {
                try {
                    isStrart = true;
                    isOpen = SELinuxUtil.isSELOpen();
                    if(isOpen){
                        SELinuxUtil.closeSEL();
                    }

                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setData(appLoader.allAppInfos);
                        }
                    });
//                    Log.i("CONTROL","start get disable count1");
                    PackageManager pm = getActivity().getPackageManager();
                    int count = 0;
                    FileUtil.changeQX(777,"/data/system/ifw");
                    File ifwFiles[]  =  new File("/data/system/ifw").listFiles();
                    if (ifwFiles==null||ifwFiles.length==0){
                        List<String> lists2 = new ArrayList<String>();
                        lists2.add("chmod 700 /data/system/ifw");
                        ShellUtils.execCommand(lists2,true,false);
                        isStrart = false;
                        return;
                    }
                    List<String> lists = new ArrayList<String>();
                    for(File f:ifwFiles){
                        lists.add("chmod 777 "+f.getAbsolutePath());
                    }
                    SharedPreferences prefsCount = getActivity().getSharedPreferences(Common.PREFS_APPIFWCOUNT, Context.MODE_WORLD_READABLE);
                    ShellUtils.execCommand(lists,true,true);
                    synchronized (appLoader.allAppInfos) {
                        try {
                            for (AppInfo ai : appLoader.allAppInfos) {
                                if (ai.serviceCount > 0) {
                                    if (getActivity()==null||getActivity().isFinishing()){
                                        return;
                                    }
                                    ServiceInfo sis[] = PackageUtil.getServicesByPkg(getActivity(), ai.getPackageName());
                                    String ifw = "";
                                    File file = new File("/data/system/ifw/"+ai.getPackageName()+ IFWCompActivity.EXT_SERVICE+".xml");
                                    if(file.exists()){
    //                                FileUtil.changeQX(777,file.getAbsolutePath());
                                        byte datas[] = FileUtil.readFile(file.getAbsolutePath());
                                        if(datas!=null&&datas.length>0){
                                            ifw = new String(datas);
                                        }
                                    }
                                    ai.serviceDisableCount = 0;
                                    if (sis!=null) {
                                        for (ServiceInfo si : sis) {
                                            String dataName = si.name;//.replaceAll("\\$", "/\\$");
                                            if (ifw.contains(dataName) || !PackageUtil.isEnable(si.packageName, dataName, pm)) {//||ifw.contains(dataName)!PackageUtil.isEnable(si.packageName, dataName, pm)||
                                                ai.serviceDisableCount++;
                                            }
                                        }
                                    }
                                    prefsCount.edit().putInt(ai.getPackageName()+"/ifwservice",ai.serviceDisableCount).commit();
                                }
                                if (ai.broadCastCount > 0) {
                                    String ifw = "";
                                    File file = new File("/data/system/ifw/"+ai.getPackageName()+IFWCompActivity.EXT_BROADCASE+".xml");
                                    if(file.exists()){
    //                                FileUtil.changeQX(777,file.getAbsolutePath());
                                        byte datas[] = FileUtil.readFile(file.getAbsolutePath());
                                        if(datas!=null&&datas.length>0){
                                            ifw = new String(datas);
                                        }
                                    }
    //                            String ifw = FileUtil.readIFWList(ai.getPackageName()+IFWCompActivity.EXT_BROADCASE);
                                    ActivityInfo ais[] = PackageUtil.getReceiverByPkg(getActivity(), ai.getPackageName());
                                    ai.broadCastDisableCount = 0;
                                    if (ais!=null) {
                                        for (ActivityInfo si : ais) {
                                            String dataName = si.name;//.replaceAll("\\$", "/\\$");
                                            if (ifw.contains(dataName) || !PackageUtil.isEnable(si.packageName, dataName, pm)) {//||ifw.contains(dataName)!PackageUtil.isEnable(si.packageName, dataName, pm)||
                                                ai.broadCastDisableCount++;
                                            }
                                        }
                                    }
                                    prefsCount.edit().putInt(ai.getPackageName()+"/ifwreceiver",ai.broadCastDisableCount).commit();
                                }
                                if (ai.activityCount > 0) {
                                    String ifw = "";
                                    File file = new File("/data/system/ifw/"+ai.getPackageName()+IFWCompActivity.EXT_ACTIVITY+".xml");
                                    if(file.exists()){
    //                                FileUtil.changeQX(777,file.getAbsolutePath());
                                        byte datas[] = FileUtil.readFile(file.getAbsolutePath());
                                        if(datas!=null&&datas.length>0){
                                            ifw = new String(datas);
                                        }
                                    }
    //                            String ifw = FileUtil.readIFWList(ai.getPackageName()+IFWCompActivity.EXT_ACTIVITY);
                                    ActivityInfo ais[] = PackageUtil.getActivityByPkg(getActivity(), ai.getPackageName());
                                    ai.activityDisableCount = 0;
                                    if (ais!=null) {
                                        for (ActivityInfo si : ais) {
                                            String dataName = si.name;//.replaceAll("\\$", "/\\$");
                                            if (ifw.contains(dataName) || !PackageUtil.isEnable(si.packageName, dataName, pm)) {//||ifw.contains(dataName)!PackageUtil.isEnable(si.packageName, dataName, pm)||
                                                ai.activityDisableCount++;
                                            }
                                        }
                                    }
                                    prefsCount.edit().putInt(ai.getPackageName()+"/ifwactivity",ai.activityDisableCount).commit();
                                }
                                count++;
                                if(count%20==0){
                                    h.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                            List<String> lists2 = new ArrayList<String>();
                            for(File f:ifwFiles){
                                lists2.add("chmod 644 "+f.getAbsolutePath());
                            }
                            lists2.add("chmod 700 /data/system/ifw");
                            ShellUtils.execCommand(lists2,true,false);
                            //                AppInfo.writeArrays(MainActivity.allAppInfos,getActivity());
                            h.post(new Runnable() {
                                @Override
                                public void run() {
    //                            adapter.notifyDataSetChanged();
                                    fresh();
    //                            adapter.fliterList(adapter.fliterName, MainActivity.allAppInfos);
                                }
                            });
                            isStrart = false;
                            settings.edit().putBoolean(Common.PREFS_NAME_IFWCHANGE,false);
                        }catch (RuntimeException e){
                            e.printStackTrace();
                        }
                    }
                }catch (Exception e){

                }
            }
        }.start();

    }

    public void startDis(){
        AlertUtil.showConfirmAlertMsg(this.getActivity(), "阉割后如果某个应用运行有问题请在服务中开启所有已禁用的服务并重启手机。", new AlertUtil.InputCallBack() {
            @Override
            public void backData(String txt, int tag) {
                if(tag == 1){
                    startDis1();
                }
            }
        });
    }
    ProgressDialog pd = null;
    public void startDis1(){
        if(adapter.bjdatas.size()==appLoader.allAppInfos.size()){
            adapter.fliterList("u",appLoader.allAppInfos);
        }
        if(pd==null){
            pd= ProgressDialog.show(this.getActivity(),"正在阉割","准备阉割...",true,false);

        }
        new Thread(){
            @Override
            public void run() {
                synchronized (appLoader.allAppInfos) {
//                    boolean isSEL = SELinuxUtil.isSELOpen();
//                    if (isSEL) {
//                        SELinuxUtil.closeSEL();
//                    }
//                    PackageManager pm = IFWFragment.this.getActivity().getApplication().getPackageManager();
                    ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
                    apps.addAll(adapter.bjdatas);
                    for (final AppInfo ai : apps) {
                        try{
                            Log.i("CONTROL", "正在阉割000" + ai.getAppName());
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    Log.i("CONTROL", "正在阉割111");
                                    try {
                                        pd.setMessage("正在阉割" + ai.appName + "...");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            ServiceInfo[] infos = PackageUtil.getServicesByPkg(IFWFragment.this.getActivity(), ai.getPackageName());
                            String ifw = FileUtil.readIFWList(ai.getPackageName() + IFWCompActivity.EXT_SERVICE);
                            if (infos == null || infos.length == 0) {
                                continue;
                            }

    //                    ArrayList<String> lists = new ArrayList<String>();
                            for (ServiceInfo data : infos) {
                                final String dataName = data.name;//.replaceAll("\\$", "/\\$");
                                boolean isEnable = !ifw.contains(dataName);//PackageUtil.isEnable(data.packageName, dataName, pm) &&
                                if (isEnable && ContainsKeyWord.isContainsWord(data.name)) {
                                    ai.serviceDisableCount++;
                                    FileUtil.writeIFWList(data.packageName + IFWCompActivity.EXT_SERVICE, data.packageName, dataName, FileUtil.IFWTYPE_SERVICE);
    //                            lists.add("pm disable "+data.packageName+"/"+dataName);
                                    h.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                Log.i("CONTROL", "正在阉割2");
                                                pd.setMessage("正在阉割" + ai.appName + "中的\n" + dataName + "\n请等待...");
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });
                                }
                            }
                            ifwCountPrefs.edit().putInt(ai.getPackageName()+"/ifwserivce",ai.serviceDisableCount).commit();
    //                    ShellUtilBackStop.execCommand(lists);
//                            Thread.sleep(50);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.i("DOZEX", "阉割完成1");
                                if (pd != null && pd.isShowing()) {
                                    pd.dismiss();
                                }
                                loadDisableCount();
                                AlertUtil.showAlertMsg(IFWFragment.this.getActivity(), "阉割完成，建议重启!");
                                pd = null;
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }
        }.start();
    }

    public void startBackDis(){
//        AlertUtil.showConfirmAlertMsg(this.getActivity(), "是否恢复阉割？", new AlertUtil.InputCallBack() {
//            @Override
//            public void backData(String txt, int tag) {
//                if(tag == 1){
                    startBackDis1();
//                }
//            }
//        });
    }
    private void startBackDis1(){
        if(pd==null){
            pd= ProgressDialog.show(this.getActivity(),"正在恢复","准备恢复...",true,false);
        }
        new Thread(){
            @Override
            public void run() {
//                boolean isSEL = SELinuxUtil.isSELOpen();
//                if(isSEL){
//                    SELinuxUtil.closeSEL();
//                }
                PackageManager pm = IFWFragment.this.getActivity().getApplication().getPackageManager();
                ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
                apps.addAll(adapter.bjdatas);
                for(final AppInfo ai:apps){
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            pd.setMessage("正在恢复"+ai.appName+"...");
                        }
                    });

                    ServiceInfo[] infos = PackageUtil.getServicesByPkg(IFWFragment.this.getActivity(), ai.getPackageName());
                    if(infos==null||infos.length==0){continue;}
                    FileUtil.delIFWList(ai.getPackageName()+IFWCompActivity.EXT_SERVICE);
                    ArrayList<String> lists = new ArrayList<String>();
                    for (ServiceInfo data : infos) {
                        final String dataName = data.name;//.replaceAll("\\$", "/\\$");
                        boolean isEnable = PackageUtil.isEnable(data.packageName, dataName, pm);
                        if (!isEnable&& ContainsKeyWord.isContainsWord(dataName)) {
                            lists.add("pm enable "+data.packageName+"/"+dataName);
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    try{
                                        pd.setMessage("正在恢复"+ai.appName+"中的\n"+dataName+"\n请等待...");
                                    } catch (Exception e) {
                                    }
                                }
                            });
                        }
                    }
                    ai.serviceDisableCount = 0;
                    if(lists.size()>0){
                        ShellUtilNoBackData.execCommand(lists);
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            if(pd!=null&&pd.isShowing()){
                                pd.dismiss();
                            }
                            loadDisableCount();
                            AlertUtil.showAlertMsg(IFWFragment.this.getActivity(),"恢复完成，建议重启!");
                            pd = null;
                        }catch (Exception e){
                        }
                    }
                });
            }
        }.start();
    }

    public void backup(){
        if(pd==null){
            pd= ProgressDialog.show(this.getActivity(),"备份","正在备份...",true,false);
        }
        new Thread(){
            @Override
            public void run() {
                FileUtil.changeQX(777,"/data/system/ifw");
                File ifwFiles[] =   new File("/data/system/ifw").listFiles();
//                List<String> lists = new ArrayList<String>();
//                for(File f:ifwFiles){
//                    lists.add("chmod 777 "+f.getAbsolutePath());
//                }
//                ShellUtils.execCommand(lists,true,false);
                if(ifwFiles.length>0){
                    File oldfiles[] = new File(FileUtil.IFWPATH).listFiles();
                    for(File of:oldfiles){
                        if(of.getName().endsWith(IFWCompActivity.EXT_ACTIVITY+".xml")||
                                of.getName().endsWith(IFWCompActivity.EXT_SERVICE+".xml")||
                                of.getName().endsWith(IFWCompActivity.EXT_BROADCASE+".xml")){
                            of.delete();
                        }
                    }
                    for(final File f:ifwFiles){
                        File newFile = new File(FileUtil.IFWPATH,f.getName());
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    pd.setMessage("正在备份\n"+f.getName());
                                }catch (Exception e){
                                }
                            }
                        });
                        FileUtil.changeQX(777,newFile.getAbsolutePath());
                        FileUtil.writeFile(newFile.getAbsolutePath(),FileUtil.readFile(f.getAbsolutePath()));
                        FileUtil.changeQX(600,newFile.getAbsolutePath());
                    }
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(pd!=null&&pd.isShowing()){
                                    pd.dismiss();
                                }
                                Toast.makeText(getActivity(),"备份完成",Toast.LENGTH_LONG).show();
                            }catch (Exception e){
                            }
                        }
                    });
                }else{
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(pd!=null&&pd.isShowing()){
                                    pd.dismiss();
                                }
                                Toast.makeText(getActivity(),"没有IFW数据可备份",Toast.LENGTH_LONG).show();
                            }catch (Exception e){
                            }
                        }
                    });
                }
                FileUtil.changeQX(700,"/data/system/ifw");
            }
        }.start();
    }

    public void restroe(){
//        AlertUtil.showConfirmAlertMsg(this.getActivity(), "是否恢IFW？", new AlertUtil.InputCallBack() {
//            @Override
//            public void backData(String txt, int tag) {
//                if(tag == 1){
                    startRestroe();
//                }
//            }
//        });
    }
    boolean isok = false;
    public void startRestroe(){
        if(pd==null){
            pd= ProgressDialog.show(this.getActivity(),"还原","正在还原...",true,false);
        }
        final File ifwFiles[] = new File(FileUtil.IFWPATH).listFiles();
        Log.i("CONTROL", "ifwFiles " + ifwFiles.length);
        if (ifwFiles.length > 0) {
            new Thread(){
                @Override
                public void run() {
                    File ifwFile = new File("/data/system/ifw");
                    if (!ifwFile.exists()) {
                        FileUtil.changeQX(777, "/data/system");
                        ifwFile.mkdirs();
                        FileUtil.changeQX(771, "/data/system");
                    }
                    FileUtil.changeQX(777, "/data/system/ifw");

                    for (final File f : ifwFiles) {
                        if (f.getAbsolutePath().toLowerCase().endsWith("xml")) {
                            h.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        pd.setMessage("正在还原\n"+f.getName());
                                    } catch (Exception e) {
                                    }
                                }
                            });
                            isok = true;
                            File newFile = new File("/data/system/ifw", f.getName());
                            try {
                                newFile.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            FileUtil.changeQX(777, newFile.getAbsolutePath());
                            FileUtil.writeFile(newFile.getAbsolutePath(), FileUtil.readFile(f.getAbsolutePath()));
                        }
                    }
                    List<String> lists2 = new ArrayList<String>();
                    File allFiles[] = new File("/data/system/ifw").listFiles();
                    if(allFiles!=null){
                        for (File f : allFiles) {
                            lists2.add("chmod 600 " + f.getAbsolutePath());
                        }
                    }
                    lists2.add("chmod 700 /data/system/ifw");
                    ShellUtils.execCommand(lists2, true, false);
                    loadDisableCount();
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(pd!=null&&pd.isShowing()){
                                    pd.dismiss();
                                }
                                Toast.makeText(getActivity(),isok ? "恢复完成，重启生效" : "没有备份数据，所以无法恢复",Toast.LENGTH_LONG).show();
                            }catch (Exception e) {
                            }
                        }
                    });
                }
            }.start();
        } else {
            Toast.makeText(getActivity(), "没有备份数据，所以无法恢复", Toast.LENGTH_LONG).show();
        }
    }

    public void startClear(){
        AlertUtil.showConfirmAlertMsg(this.getActivity(), "是清除所有已设置的IFW（仅仅是IFW文件，清除后部分组件还会处于禁用状态）？", new AlertUtil.InputCallBack() {
            @Override
            public void backData(String txt, int tag) {
                if(tag == 1){
                    File ifwFile = new File("/data/system/ifw");
                    if(!ifwFile.exists()){
                       return;
                    }
                    FileUtil.changeQX(777,"/data/system/ifw");
                    File ifwFiles[] =   new File("/data/system/ifw").listFiles();
                    if(ifwFiles.length>0){
                        for(File f:ifwFiles){
                            f.delete();
                        }
                    }
                    FileUtil.changeQX(700,"/data/system/ifw");
                    Toast.makeText(getActivity(),"清除完成",Toast.LENGTH_LONG).show();
                    for(AppInfo ai:appLoader.allAppInfos){
                        ai.activityDisableCount = 0;
                        ai.serviceDisableCount = 0;
                        ai.broadCastDisableCount = 0;
                    }
                    ifwCountPrefs.edit().clear().apply();
                    fresh();
                }
            }
        });
    }

    class TitleClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            TextView tv = (TextView)v;
            ArrayList<TextView> tvs = new ArrayList<TextView>();
            tvs.add(serviceTv);
            tvs.add(broadTv);
            tvs.add(actTv);
            int index = tvs.indexOf(v);
            adapter.setSortType(adapter.sortType==index?-1:index);
            for(TextView t:tvs){
                t.setTextColor(curColor);
            }
            tv.setTextColor(adapter.sortType==-1?curColor:Color.parseColor(MainActivity.COLOR));
            loadY(listView,IFWFragment.this.getClass(),adapter.sortType);
        }
    }
}

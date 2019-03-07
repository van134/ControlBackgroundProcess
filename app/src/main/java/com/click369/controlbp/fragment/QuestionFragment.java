package com.click369.controlbp.fragment;


import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.adapter.QuestionAdapter;
import com.click369.controlbp.bean.Question;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedSms;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class QuestionFragment extends BaseFragment {
    private ListView list;
    private ScrollView scview;
    private Switch logSw;
    private TextView showAlertTv,showLogAlertTv,showAppLogAlertTv,showAmsLogTv,alertTv;
    private EditText et;
    private LinearLayout alertFl,logLL;
    private QuestionAdapter adapter;
    public static int curColor = Color.BLACK;
    public static int chooseIndex = -1;
    private int chooseType = 0;
    private ArrayList<Question> questions = new ArrayList<Question>();
    private String msg = "目前还不完善，这是个长期的工作，如果你有异常和解决办法请到酷安评论区发表并@我，可行的话我可以在更新时加入到问题记录中";
    private  String logMSG = "";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_question, container, false);
        initView(v);
//        intentFilter = new IntentFilter("com.click369.control.recpreventinfo");
//        getActivity().registerReceiver(broadcastReceiver,intentFilter);
        return v;
    }
//    IntentFilter intentFilter;
//    public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
        public void onReceive(Intent intent) {
        if(!QuestionFragment.this.isVisible()){
            return;
        }
            String action = intent.getAction();
            if("com.click369.control.recpreventinfo".equals(action)){
                if(intent.hasExtra("preventPkgs")){
                    Map<Long,String> preventPkgsTemp = (Map<Long,String>)intent.getSerializableExtra("preventPkgs");
                    Map<Long,String> killPkgsTemp = (Map<Long,String>)intent.getSerializableExtra("killPkgs");
                    Map<Long,String> startPkgsTemp = (Map<Long,String>)intent.getSerializableExtra("startPkgs");
                    TreeMap<Long,String> preventPkgs = new TreeMap<Long,String>();
                    TreeMap<Long,String> killPkgs = new TreeMap<Long,String>();
                    TreeMap<Long,String> startPkgs = new TreeMap<Long,String>();
                    if(preventPkgsTemp!=null){
                        preventPkgs.putAll(preventPkgsTemp);
                    }
                    if(killPkgsTemp!=null){
                        killPkgs.putAll(killPkgsTemp);
                    }
                    if(startPkgsTemp!=null){
                        startPkgs.putAll(startPkgsTemp);
                    }
                    TreeSet<Long> sets = new TreeSet<Long>();
                    sets.addAll(preventPkgs.keySet());
                    sets.addAll(killPkgs.keySet());
                    sets.addAll(startPkgs.keySet());
                    StringBuilder stringBuilder = new StringBuilder();
                    long time = 0;
                    for(Long l:sets){
                        try{
                            if(preventPkgs.containsKey(l)){
                                String pkginfo =  preventPkgs.get(l);
                                String pkg = "";
                                String cls = "";
                                String proName="";
                                String type="";
                                if(pkginfo.contains("|")){
                                    String ps[] = pkginfo.split("\\|");
                                    if(ps.length==3){
                                        pkg = ps[0];
                                        cls = ps[1];
                                        proName = ps[2];
                                    }else if(ps.length==4){
                                        pkg = ps[0];
                                        cls = ps[1];
                                        proName = ps[2];
                                        type = " 的 "+ps[3];
                                    }else{
                                        pkg = pkginfo;
                                    }
                                }else{
                                    pkg = pkginfo;
                                }
                                String t = TimeUtil.changeMils2String(l,"MM-dd HH:mm:ss阻止 ");
                                String name = PackageUtil.getAppNameByPkg(getActivity(),pkg);
                                if(name==null){
                                    name = pkg;
                                }
                                stringBuilder.append(t).append(name).append(type).append("\n").append("进程:").append(proName).append("\n").append("组件:").append(cls).append("\n\n");
                            }else if(killPkgs.containsKey(l)){
                                String pkg =  killPkgs.get(l);
                                String t = TimeUtil.changeMils2String(l,"MM-dd HH:mm:ss杀死 ");
                                String name = PackageUtil.getAppNameByPkg(getActivity(),pkg);
                                if(name==null){
                                    name = pkg;
                                }
                                stringBuilder.append(t).append(name).append("\n\n");
                            }else if(startPkgs.containsKey(l)){
                                String pkginfo =  startPkgs.get(l);
                                String pkg = "";
                                String cls = "";
                                String proName="";
                                String type="";
                                if(pkginfo.contains("|")){
                                    String ps[] = pkginfo.split("\\|");
                                    if(ps.length==3){
                                        pkg = ps[0];
                                        cls = ps[1];
                                        proName = ps[2];
                                    }else if(ps.length==4){
                                        pkg = ps[0];
                                        cls = ps[1];
                                        proName = ps[2];
                                        type = " 的 "+ps[3];
                                    }else{
                                        pkg = pkginfo;
                                    }
                                }else{
                                    pkg = pkginfo;
                                }
                                String t = TimeUtil.changeMils2String(l,"MM-dd HH:mm:ss启动 ");
                                String name = PackageUtil.getAppNameByPkg(getActivity(),pkg);
                                if(name==null){
                                    name = pkg;
                                }
                                stringBuilder.append(t).append(name).append(type).append("\n").append("进程:").append(proName).append("\n").append("组件:").append(cls).append("\n\n");
                            }
                            if(time!=0&&l-time>=1000*60){
                                stringBuilder.append("\n");
                            }
                            time = l;
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                    logMSG = stringBuilder.toString();
                    if(logMSG.equals("暂无系统日志")||logMSG.length()<5){
                        alertTv.setText("暂无系统日志");
                    }else{
                        alertTv.setText("这是系统启动、阻止和杀死应用运行的日志，该日志可以帮助你分析系统阻止和杀死应用运行的情况,你可以看到被尝试唤醒的组件名，如果你是老鸟你也可以根据组件名称去用IFW禁止该组件,注意：如果你的手机因为禁止自启卡顿，可以参考该日志来排除是否禁止了不该禁止的应用，系统级别或XP应用如果在一两秒内被启动或阻止几十次则证明该应用对系统非常重要不能禁止。\n\n"+logMSG);
                    }
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            scview.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
            }
        }
//    };

    protected void initView(View v) {
        list = (ListView)v.findViewById(R.id.question_listview);
        scview = (ScrollView) v.findViewById(R.id.question_scview);
        showAlertTv = (TextView)v.findViewById(R.id.question_showupdatelogalert);
        showAppLogAlertTv = (TextView)v.findViewById(R.id.question_showappstartlogalert);
        showLogAlertTv = (TextView)v.findViewById(R.id.question_showlogalert);
        alertTv = (TextView)v.findViewById(R.id.question_alert_tv);
        showAmsLogTv = (TextView)v.findViewById(R.id.question_showamslogalert);
        alertFl = (LinearLayout) v.findViewById(R.id.question_alert_fl);
        logLL = (LinearLayout) v.findViewById(R.id.question_long_ll);
        logSw = (Switch) v.findViewById(R.id.setting_backlog_sw);
        logLL.setVisibility(View.GONE);
        logSw.setChecked(SharedPrefsUtil.getInstance(getContext()).settings.getBoolean(Common.PREFS_SETTING_BACKLOGOPEN,false));
        logSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WatchDogService.isSaveBackLog = isChecked;
                SharedPrefsUtil.getInstance(getContext()).settings.edit().putBoolean(Common.PREFS_SETTING_BACKLOGOPEN,isChecked).commit();
            }
        });
        curColor = showAlertTv.getCurrentTextColor();
        logSw.setTextColor(curColor);
        et = (EditText) v.findViewById(R.id.question_et);
        et.setTextColor(curColor);
        et.setHintTextColor(curColor);
        alertFl.setVisibility(View.GONE);
        adapter = new QuestionAdapter(getActivity());
        questions.addAll(FileUtil.getQuestions(getActivity()));
        adapter.setData(questions);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                BaseActivity.zhenDong(getContext());
                chooseIndex = chooseIndex==position?-1:position;
                adapter.notifyDataSetChanged();
//                if (chooseIndex!=-1){
//                    Question question = (Question)adapter.getItem(chooseIndex);
//                }
            }
        });
        showAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity.zhenDong(getContext());
                logLL.setVisibility(View.GONE);
                logMSG = FileUtil.getAssetsString(getContext(),"update.txt");
                if(logMSG.equals("暂无更新日志")||logMSG.length()<5){
                    alertTv.setText("暂无更新日志");
                }else{
                    alertTv.setText(logMSG);
                }
                if (chooseType!=0){
                    alertFl.setVisibility(View.VISIBLE);
                }else{
                    alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
                }
                showAlertTv.setTextColor(alertFl.isShown()?Color.parseColor(MainActivity.THEME_TEXT_COLOR):curColor);
                showAppLogAlertTv.setTextColor(curColor);
                showAmsLogTv.setTextColor(curColor);
                showLogAlertTv.setTextColor(curColor);
                chooseType = 0;

            }
        });
        showLogAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                XposedSms.test(getActivity());
                BaseActivity.zhenDong(getContext());
                logLL.setVisibility(View.GONE);
                byte data[] = FileUtil.readFile(FileUtil.ROOTPATH+ File.separator+"errorlog.txt");
                logMSG = new String((data==null||data.length==0)?"暂无出错日志".getBytes():data);
                if(logMSG.equals("暂无出错日志")||logMSG.length()<5){
                    alertTv.setText("暂无出错日志");
                }else{
                    alertTv.setText("这是应用控制器自身的错误日志（其他应用或系统有问题从本日志中无法看出，需要XP日志），把日志截图或长按复制发送给开发者\n\n"+logMSG);
                }
                if (chooseType!=1){
                    alertFl.setVisibility(View.VISIBLE);
                }else{
                    alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
                }

                showLogAlertTv.setTextColor(alertFl.isShown()?Color.parseColor(MainActivity.THEME_TEXT_COLOR):curColor);
                showAppLogAlertTv.setTextColor(curColor);
                showAmsLogTv.setTextColor(curColor);
                showAlertTv.setTextColor(curColor);
                chooseType = 1;
//                scview.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        showAppLogAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity.zhenDong(getContext());
                logLL.setVisibility(View.VISIBLE);
                byte data[] = FileUtil.readFile(FileUtil.ROOTPATH+ File.separator+"backlog.txt");
                logMSG = new String((data==null||data.length==0)?"暂无动作日志,动作日志需要在设置中打开记录开关".getBytes():data);
                if(logMSG.startsWith("暂无动作日志")||logMSG.length()<5){
                    alertTv.setText("暂无动作日志,启动日志需要在设置中打开记录开关");
                }else{
                    alertTv.setText("这是应用打开及关闭日志,主要是检测应用打开后按home或back键的执行情况，该日志可以帮助你分析应用的打开及关闭情况,注意：显示退出并不代表程序被杀死，杀死则会显示xx秒后杀死xx。\n\n"+logMSG);
                }
                if (chooseType!=2){
                    alertFl.setVisibility(View.VISIBLE);
                }else{
                    alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
                }
                showAppLogAlertTv.setTextColor(alertFl.isShown()?Color.parseColor(MainActivity.THEME_TEXT_COLOR):curColor);
                showLogAlertTv.setTextColor(curColor);
                showAmsLogTv.setTextColor(curColor);
                showAlertTv.setTextColor(curColor);
                chooseType =2;
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        scview.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
        showAmsLogTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity.zhenDong(getContext());
                logLL.setVisibility(View.GONE);
                if (chooseType!=3){
                    alertTv.setText("两秒内如果没有数据则证明没有任何系统记录，或者您更新应用后没有重启");
                    alertFl.setVisibility(View.VISIBLE);
                }else{
                    alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
                }
                if(alertFl.isShown()){
                    Intent intent = new Intent("com.click369.control.ams.getpreventinfo");
                    getActivity().sendBroadcast(intent);
                }
                showAmsLogTv.setTextColor(alertFl.isShown()?Color.parseColor(MainActivity.THEME_TEXT_COLOR):curColor);
                showLogAlertTv.setTextColor(curColor);
                showAppLogAlertTv.setTextColor(curColor);
                showAlertTv.setTextColor(curColor);
                chooseType =3;
            }
        });
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                chooseIndex = -1;
                String search = et.getText().toString();
                ArrayList<Question> temps = new ArrayList<Question>();
                for(Question q:questions){
                    if (q.title.contains(search)){
                        temps.add(q);
                    }
                }
                adapter.setData(temps);
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        alertTv.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                    String ttt[] = {"取消","清空错误日志","清空动作日志","清空系统日志"};
                    AlertUtil.showListAlert(getActivity(), "请选择", new String[]{"复制日志",ttt[chooseType]}, new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if (tag == 0){
                                ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(logMSG);
                                Toast.makeText(getActivity(),"内容已复制到粘贴板",Toast.LENGTH_LONG).show();
                            }else {
                                if (chooseType == 1&&logMSG.length()>10) {
                                    alertTv.setText("暂无错误日志");
                                    File file = new File(FileUtil.ROOTPATH + File.separator + "errorlog.txt");
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }else if (chooseType == 2&&logMSG.length()>10) {
                                    alertTv.setText("暂无动作日志,动作日志需要在设置中打开记录开关");
                                    File file = new File(FileUtil.ROOTPATH + File.separator + "backlog.txt");
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }else if (chooseType == 3&&logMSG.length()>10) {
                                    alertTv.setText("暂无系统日志");
                                    Intent intent = new Intent("com.click369.control.ams.getpreventinfo");
                                    intent.putExtra("isclear",true);
                                    getActivity().sendBroadcast(intent);
                                }
                            }
                        }
                    });
                return true;
            }
        });
    }

//
//    private String autoSplitText(final TextView tv) {
//        final String rawText = tv.getText().toString(); //原始文本
//        final Paint tvPaint = tv.getPaint(); //paint，包含字体等信息
//        final float tvWidth = tv.getWidth() - tv.getPaddingLeft() - tv.getPaddingRight(); //控件可用宽度
//        //将原始文本按行拆分
//        String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
//        StringBuilder sbNewText = new StringBuilder();
//        for (String rawTextLine : rawTextLines) {
//            if (tvPaint.measureText(rawTextLine) <= tvWidth) {
//                //如果整行宽度在控件可用宽度之内，就不处理了
//                sbNewText.append(rawTextLine);
//            } else {
//                //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
//                float lineWidth = 0;
//                for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
//                    char ch = rawTextLine.charAt(cnt);
//                    lineWidth += tvPaint.measureText(String.valueOf(ch));
//                    if (lineWidth <= tvWidth) {
//                        sbNewText.append(ch);
//                    } else {
//                        sbNewText.append("\n");
//                        lineWidth = 0;
//                        --cnt;
//                    }
//                }
//            }
//            sbNewText.append("\n");
//        }
//        //把结尾多余的\n去掉
//        if (!rawText.endsWith("\n")) {
//            sbNewText.deleteCharAt(sbNewText.length() - 1);
//        }
//        return sbNewText.toString();
//    }
}

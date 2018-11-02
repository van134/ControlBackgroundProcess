package com.click369.controlbp.activity;


import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.adapter.QuestionAdapter;
import com.click369.controlbp.bean.Question;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class QuestionFragment extends Fragment {
    private ListView list;
    private ScrollView scview;
    private TextView showAlertTv,showLogAlertTv,showAppLogAlertTv,alertTv;
    private EditText et;
    private FrameLayout alertFl;
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
        return v;
    }

    protected void initView(View v) {
        list = (ListView)v.findViewById(R.id.question_listview);
        scview = (ScrollView) v.findViewById(R.id.question_scview);
        showAlertTv = (TextView)v.findViewById(R.id.question_showalert);
        showAppLogAlertTv = (TextView)v.findViewById(R.id.question_showappstartlogalert);
        showLogAlertTv = (TextView)v.findViewById(R.id.question_showlogalert);
        alertTv = (TextView)v.findViewById(R.id.question_alert_tv);
        alertFl = (FrameLayout) v.findViewById(R.id.question_alert_fl);

        curColor = showAlertTv.getCurrentTextColor();
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
                alertTv.setText(msg);
                if (chooseType==0){
                    alertFl.setVisibility(View.VISIBLE);
                }else{
                    alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
                }
                showAlertTv.setTextColor(alertFl.isShown()?Color.parseColor("#40d0b7"):curColor);
                showLogAlertTv.setTextColor(curColor);
                chooseType = 0;
            }
        });
        showLogAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte data[] = FileUtil.readFile(FileUtil.ROOTPATH+ File.separator+"errorlog.txt");
                logMSG = new String((data==null||data.length==0)?"暂无异常日志".getBytes():data);
                if(logMSG.equals("暂无异常日志")||logMSG.length()<5){
                    alertTv.setText("暂无异常日志");
                }else{
                    alertTv.setText("这是应用控制器自身的错误日志（其他应用或系统有问题从本日志中无法看出，需要XP日志），把日志截图或长按复制发送给开发者\n\n"+logMSG);

                }
                if (chooseType!=1){
                    alertFl.setVisibility(View.VISIBLE);
                }else{
                    alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
                }

                showLogAlertTv.setTextColor(alertFl.isShown()?Color.parseColor("#40d0b7"):curColor);
                showAppLogAlertTv.setTextColor(curColor);
                showAlertTv.setTextColor(curColor);
                chooseType = 1;
//                scview.fullScroll(ScrollView.FOCUS_UP);
            }
        });
        showAppLogAlertTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte data[] = FileUtil.readFile(FileUtil.ROOTPATH+ File.separator+"backlog.txt");
                logMSG = new String((data==null||data.length==0)?"暂无应用启动日志".getBytes():data);
                if(logMSG.equals("暂无应用启动日志")||logMSG.length()<5){
                    alertTv.setText("暂无应用启动日志");
                }else{
                    alertTv.setText("这是应用启动及关闭日志，该日志可以帮助你分析应用的打开及关闭情况,注意：显示退出并不代表程序被杀死，杀死则会显示xx秒后杀死xx。\n\n"+logMSG);

                }
                if (chooseType!=2){
                    alertFl.setVisibility(View.VISIBLE);
                }else{
                    alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
                }
                showAppLogAlertTv.setTextColor(alertFl.isShown()?Color.parseColor("#40d0b7"):curColor);
                showLogAlertTv.setTextColor(curColor);
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

                    AlertUtil.showListAlert(getActivity(), "请选择", new String[]{"复制日志",chooseType == 1?"清空异常日志":"清空应用启动日志"}, new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if (tag == 0){
                                ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                cm.setText(logMSG);
                                Toast.makeText(getActivity(),"内容已复制到粘贴板",Toast.LENGTH_LONG).show();
                            }else {
                                if (chooseType == 1&&logMSG.length()>10) {
                                    alertTv.setText("暂无异常日志");
                                    File file = new File(FileUtil.ROOTPATH + File.separator + "errorlog.txt");
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }else if (chooseType == 2&&logMSG.length()>10) {
                                    alertTv.setText("暂无应用启动日志");
                                    File file = new File(FileUtil.ROOTPATH + File.separator + "backlog.txt");
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }
                            }
                        }
                    });
                return true;
            }
        });
    }
}

package com.click369.controlbp.activity;

import android.app.Activity;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.fragment.ControlFragment;
import com.click369.controlbp.fragment.IFWFragment;
import com.click369.controlbp.fragment.IceUnstallFragment;
import com.click369.controlbp.util.AlertUtil;

/**
 * Created by asus on 2017/10/19.
 */
public class TopSearchView {
    private Activity act;
    public EditText editText;
    public FrameLayout alertFl;
    public TextView alertTv,closeOpenTv,sysAppTv,userAppTv;
    public static int appType = 0;//0用户  1系统  2用户系统
    private CallBack cb;
    private View v;
    private int curColor = Color.BLACK;
    public static String searchText = "u";
    public TopSearchView(Activity act,View v){
        this.act = act;
        this.v = v;
    }
    public void setListener(CallBack cb){
        this.cb = cb;
    }
    public void setAlertText(String text,int color,boolean isShow){
        alertTv.setText(text);
        if (color!=0){
            alertTv.setTextColor(color);
        }
        alertFl.setVisibility(isShow?View.VISIBLE:View.GONE);
    }

    public void initView(){
//        View v = act.getLayoutInflater().inflate(R.layout.layout_topsearch,null);
        editText = (EditText) v.findViewById(R.id.main_edittext);
        userAppTv = (TextView)v.findViewById(R.id.main_userapp_tv);
        curColor = userAppTv.getCurrentTextColor();
        if (MainActivity.isNightMode){
            editText.setTextColor(curColor);
            editText.setHintTextColor(Color.GRAY);
        }
//        userAppTv.setTextColor(Color.parseColor("#40d0b7"));
        sysAppTv = (TextView)v.findViewById(R.id.main_sysapp_tv);
        alertFl = (FrameLayout)v.findViewById(R.id.main_alert_fl);
        alertFl.setVisibility(View.GONE);
        alertTv = (TextView)v.findViewById(R.id.main_alert_tv);
        closeOpenTv = (TextView)v.findViewById(R.id.main_alert_closetv);
        closeOpenTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertFl.setVisibility(alertFl.isShown()?View.GONE:View.VISIBLE);
//                XposedStopApp.stopApk("com.tencent.mm",(ActivityManager) act.getSystemService(Context.ACTIVITY_SERVICE));
            }
        });

        userAppTv.setTextColor(appType==1?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
        sysAppTv.setTextColor(appType==0?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));

        sysAppTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appType == 0){
                    if (act instanceof MainActivity&&(((MainActivity) act).chooseFragment instanceof ControlFragment ||((MainActivity) act).chooseFragment instanceof IFWFragment ||((MainActivity) act).chooseFragment instanceof IceUnstallFragment)) {
                        AlertUtil.showConfirmAlertMsg(act, "处理系统应用时最好你十分确定处理了不会影响系统功能，否则请不要乱禁用，部分系统应用禁用后会导致无法开机及各种问题", new AlertUtil.InputCallBack() {
                            @Override
                            public void backData(String txt, int tag) {
                                if (tag == 1) {
                                    appType = 2;
                                    sysAppTv.setTextColor(Color.parseColor(MainActivity.THEME_TEXT_COLOR));
                                    if(cb!=null){
                                        searchText = editText.getText().toString().trim().length()>0?editText.getText().toString().trim():(appType==0?"u":(appType==1?"s":""));
                                        cb.backAppType(searchText);
                                    }
                                }
                            }
                        });
                    }else{
                        appType = 2;
                        sysAppTv.setTextColor(Color.parseColor(MainActivity.THEME_TEXT_COLOR));
                    }
                }else if(appType == 2){
                    appType = 0;
                    sysAppTv.setTextColor(curColor);
                }
                if(cb!=null){
                    searchText = editText.getText().toString().trim().length()>0?editText.getText().toString().trim():(appType==0?"u":(appType==1?"s":""));
                    cb.backAppType(searchText);
                }
            }
        });
        userAppTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appType == 1){
                    appType = 2;
                    userAppTv.setTextColor(Color.parseColor(MainActivity.THEME_TEXT_COLOR));
                }else if(appType == 2){
                    appType = 1;
                    userAppTv.setTextColor(curColor);
                }
                if(cb!=null){
                    searchText = editText.getText().toString().trim().length()>0?editText.getText().toString().trim():(appType==0?"u":(appType==1?"s":""));
                    cb.backAppType(searchText);
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String appName = editText.getText().toString();
                if(cb!=null){
                    searchText = editText.getText().toString().trim().length()>0?editText.getText().toString().trim():(appType==0?"u":(appType==1?"s":""));
                    cb.backAppType(searchText);
                }

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                editText.setSelection(editText.getText().length());
            }
        });
//        return v;
    }

    public void showText(){
        if("u".equals(searchText)||"U".equals(searchText)){
            searchText = "";
        }else if("s".equals(searchText)||"S".equals(searchText)){
            searchText = "";
        }
        editText.setText(searchText);
        editText.setSelection(editText.getText().length());
        userAppTv.setTextColor(appType==1?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
        sysAppTv.setTextColor(appType==0?curColor:Color.parseColor(MainActivity.THEME_TEXT_COLOR));
    }

    public interface CallBack{
        int APP_USER = 0;
        int APP_SYS = 1;
        int APP_ALL = 2;
        void backAppType(String appName);
    }
}

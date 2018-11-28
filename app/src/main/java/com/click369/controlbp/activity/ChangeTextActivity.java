package com.click369.controlbp.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

/**
 * Created by asus on 2017/5/27.
 */
public class ChangeTextActivity extends BaseActivity{
    private EditText nameEt,oldEt,newEt;
    private TextView alertTv,okTv,cancelTv;
    private int curColor= Color.BLACK;
    String pkg = null;
    private SharedPreferences tvPrefs;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changetext);
        nameEt = (EditText)this.findViewById(R.id.changetext_name_et);
        oldEt = (EditText)this.findViewById(R.id.changetext_old_et);
        newEt = (EditText)this.findViewById(R.id.changetext_new_et);
        alertTv = (TextView) this.findViewById(R.id.changetext_alerttv);
        okTv = (TextView) this.findViewById(R.id.changetext_okbt);
        cancelTv = (TextView) this.findViewById(R.id.changetext_clearbt);
        curColor = alertTv.getCurrentTextColor();
        nameEt.setTextColor(curColor);
        oldEt.setTextColor(curColor);
        newEt.setTextColor(curColor);
        if (MainActivity.isNightMode){
            nameEt.setHintTextColor(Color.GRAY);
            oldEt.setHintTextColor(Color.GRAY);
            newEt.setHintTextColor(Color.GRAY);
        }
        this.setTitle("替换文字");
        tvPrefs =  SharedPrefsUtil.getInstance(this).tvPrefs;//SharedPrefsUtil.getPreferences(getApplicationContext(), Common.IPREFS_TVLIST);
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameEt.getText().toString().trim().length()>0){
                    final String appName = nameEt.getText().toString().trim();
                    for(AppInfo ai:MainActivity.allAppInfos){
                        if (ai.appName.equals(appName)){
                            pkg = ai.packageName;
                            break;
                        }
                    }
                    if(pkg!=null){
                        final String oldText = oldEt.getText().toString().trim();
                        final String newText = newEt.getText().toString().trim();
                        if(oldText.length()>0){
                            AlertUtil.showConfirmAlertMsg(ChangeTextActivity.this, "是否确定把" + appName + "中所有的" + oldText + "替换为" + newText + "?\n(如果替换后出问题请清除所有并重启对应的应用或重启手机)", new AlertUtil.InputCallBack() {
                                @Override
                                public void backData(String txt, int tag) {
                                    if (tag == 1){
                                        if (oldText.equals(newText)){
                                            tvPrefs.edit().remove(pkg+"/"+oldText).commit();
                                        }else{
                                            tvPrefs.edit().putBoolean(pkg,true).putString(pkg+"/"+oldText,newText).commit();
                                        }
//                                        XposedStopApp.stopApk(pkg,getApplicationContext());
                                        showT("数据已添加，强制杀死应用"+appName+"或重启手机生效。");
                                        oldEt.setText("");
                                        newEt.setText("");
                                    }
                                }
                            });
                        }
                    }else{
                        showT("本机应用中未找到"+appName);
                    }
                }else{
                    showT("内容为不能为空");
                }
            }
        });
        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertUtil.showConfirmAlertMsg(ChangeTextActivity.this, "是否确定把清空所有的文字替换数据?(清空后重启系统)", new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if (tag == 1){
                            tvPrefs.edit().clear().commit();
                            showT("数据已清空，重启生效。");
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}

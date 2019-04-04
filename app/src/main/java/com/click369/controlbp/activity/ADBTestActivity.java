package com.click369.controlbp.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.ColorNavBarService;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.ShellUtils;

import java.io.File;
import java.util.HashMap;

/**
 * Created by van on 2017/5/27.
 */
public class ADBTestActivity extends BaseActivity{
    private EditText et;
    private TextView okTv,infoTv;
    private int curColor= Color.BLACK;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adbtest);
        et = (EditText)this.findViewById(R.id.adbtest_et);
        okTv = (TextView) this.findViewById(R.id.adbtest_ok_tv);
//        cleanTv = (TextView)this.findViewById(R.id.adbtest_clean_tv);
        infoTv = (TextView)this.findViewById(R.id.adbtest_info_tv);
        curColor = infoTv.getCurrentTextColor();
        et.setTextColor(curColor);
        this.setTitle("ADB命令");
        okTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et.getText().toString().trim().length()>0){
                    final String content = et.getText().toString().trim();

                    new Thread(){
                        @Override
                        public void run() {
                            String c =content;
                            if (content.startsWith("adb")){
                                c = content.replace("adb","");
                            }
                            final String info = ShellUtils.execCommand(c,true,true).successMsg;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    infoTv.setText(info.trim().length()>0?"执行完毕："+content+"\n"+info:"执行完毕："+content+"\n无返回结果");
                                }
                            });
                        }
                    }.start();
                }else{
                    showT("内容为空");
                }
            }
        });
//        cleanTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                infoTv.setText("...");
//            }
//        });
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
            }
            @Override
            public void afterTextChanged(Editable s) {
                clearIv.setVisibility(et.getText().toString().length()>0?View.VISIBLE:View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}

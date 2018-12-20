package com.click369.controlbp.activity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShellUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus on 2017/5/27.
 */
public class DebugActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        window.setGravity(Gravity.TOP| Gravity.LEFT);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 0;
        lp.y = 0;
        lp.width = 1;
        lp.height = 1;
        window.setAttributes(lp);
        Log.i("CONTROL","start DebugActivity");
        AlertUtil.showAlertMsgBack(this, "应用控制器出现异常(请截屏发给开发者)", this.getIntent().getStringExtra("content"), new AlertUtil.InputCallBack() {
            @Override
            public void backData(String txt, int tag) {
               finish();
            }
        });
    }
}

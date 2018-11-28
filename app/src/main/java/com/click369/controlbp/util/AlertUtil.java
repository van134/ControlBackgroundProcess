package com.click369.controlbp.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Created by asus on 2017/5/23.
 */
public class AlertUtil {


    public static void showListAlert(Context cxt,final String title,final String titles[],final  InputCallBack icb){
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(title);
        //    指定下拉列表的显示数据
//        final String[] cities = {"广州", "上海", "北京", "香港", "澳门"};
        //    设置一个下拉的列表选择项
        builder.setItems(titles, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (icb==null){
                    return;
                }
                icb.backData("",which);
               // Toast.makeText(MainActivity.this, "选择的城市为：" + cities[which], Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    public static void showListNotCancelAlert(final Activity cxt,final String title,final String titles[],final  InputCallBack icb){
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(title);
        builder.setItems(titles, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (icb==null){
                    return;
                }
                icb.backData("",which);
                cxt.finish();
            }
        });
        Dialog d = builder.create();
        d.setCanceledOnTouchOutside(false);
        d.show();
        d.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cxt.finish();
            }
        }); d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                cxt.finish();
            }
        });
    }


    public static void showConfirmAlertMsg(final Context cxt,final String msg,final InputCallBack ic){
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle("提示！");
        builder.setMessage(msg);
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",1);
            }
        });
        builder.setNegativeButton("否",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",0);
            }
        });
        builder.create().show();
    }

    public static void showConfirmAlertMsg(final Context cxt,final String msg,final String title,final String comTitle,final String cancelTitle,final InputCallBack ic){
        AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton(comTitle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",1);
            }
        });
        builder.setNegativeButton(cancelTitle,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",0);
            }
        });
        builder.create().show();
    }

    public static void showThreeButtonAlertMsg(final Context cxt,final String msg,final InputCallBack ic){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(cxt);
        builder.setTitle("提示！");
        builder.setMessage(msg);
        builder.setNeutralButton("选择所有",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",0);
            }
        });
        builder.setPositiveButton("不操作", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",1);
            }
        });
        builder.setNegativeButton("取消所有",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",2);
            }
        });
        builder.create().show();
    }

    public static void showAlertMsg(final Context cxt,final String msg){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(cxt);
        builder.setTitle("提示！");
        builder.setMessage(msg);
        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }
    public static void showAlertMsgBack(final Context cxt,final String title,final String msg,final InputCallBack ic){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(cxt);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ic.backData("",0);
            }
        });
        builder.create().show();
    }
    public static void showAlertMsg(final Context cxt,final String title,final String msg){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(cxt);
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setPositiveButton("知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void inputAlert(final Activity cxt, final int tag, final InputCallBack ic){
        final String holders[] = {"0-3600之间","0-3600之间","0-3600之间","0-3600之间","0-3600之间","60-3600之间","60-3600之间","60-3600之间","0-100之间","0-100之间"};
        final String titles[] = {"返回时强退和墓碑延迟时长","后台时墓碑延迟时长","熄屏时强退和墓碑延迟时长","清除冗余数据","亮屏时打盹时长","熄屏时打盹时长","熄屏打盹延迟","应用控制器背景图片模糊度","应用控制器背景图片透明度"};
        final EditText et = new EditText(cxt);
        et.setHint(holders[tag]);
        et.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
        et.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
        et.setBackgroundColor(Color.argb(20,0,0,0));
        et.setPadding(0,30,0,30);
        et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        final AlertDialog ad = new AlertDialog.Builder(cxt).setTitle("输入"+ titles[tag])
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(cxt.getApplicationContext(), "不能为空！" + input, Toast.LENGTH_LONG).show();
                        }else {
                            ic.backData(input,tag);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // 先隐藏键盘
                    ((InputMethodManager) et.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(cxt.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    String input = et.getText().toString();
                    if (input.equals("")) {
                        Toast.makeText(cxt.getApplicationContext(), "不能为空！" + input, Toast.LENGTH_LONG).show();
                    }else {
                        ic.backData(input,tag);
                        ad.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        et.postDelayed(new Runnable() {
            @Override
            public void run() {
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                et.setSelection(et.getText().length());
                InputMethodManager inputManager = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);

            }
        },200);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void inputAlert(final Activity cxt, final String hit, final String value, final InputCallBack ic){
        final EditText et = new EditText(cxt);
        et.setHint(hit);
        et.setText(value);
        et.setSelection(et.getText().length());
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        et.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
        et.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
        et.setBackgroundColor(Color.argb(20,0,0,0));
        et.setPadding(0,30,0,30);
        et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        final AlertDialog ad = new AlertDialog.Builder(cxt).setTitle("输入该唤醒锁间隔时长（单位:秒）")
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(cxt.getApplicationContext(), "不能为空！" + input, Toast.LENGTH_LONG).show();
                        }else {
                            ic.backData(input,0);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // 先隐藏键盘
                    ((InputMethodManager) et.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(cxt.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    String input = et.getText().toString();
                    if (input.equals("")) {
                        Toast.makeText(cxt.getApplicationContext(), "不能为空！" + input, Toast.LENGTH_LONG).show();
                    }else {
                        ic.backData(input,0);
                        ad.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        et.postDelayed(new Runnable() {
            @Override
            public void run() {
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                et.setSelection(et.getText().length());
                InputMethodManager inputManager = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);

            }
        },200);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void inputAlertCustomer(final Activity cxt, final String title,  final String hit, final String value, final InputCallBack ic){
        final EditText et = new EditText(cxt);
        et.setHint(hit);
        et.setText(value);
        et.setSelection(et.getText().length());
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        et.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
        et.setKeyListener(DigitsKeyListener.getInstance("1234567890"));
        et.setBackgroundColor(Color.argb(20,0,0,0));
        et.setPadding(0,30,0,30);
        et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        final AlertDialog ad = new AlertDialog.Builder(cxt).setTitle(title)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.equals("")) {
                            Toast.makeText(cxt.getApplicationContext(), "不能为空！" + input, Toast.LENGTH_LONG).show();
                        }else {
                            ic.backData(input,0);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // 先隐藏键盘
                    ((InputMethodManager) et.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(cxt.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    String input = et.getText().toString();
                    if (input.equals("")) {
                        Toast.makeText(cxt.getApplicationContext(), "不能为空！" + input, Toast.LENGTH_LONG).show();
                    }else {
                        ic.backData(input,0);
                        ad.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        et.postDelayed(new Runnable() {
            @Override
            public void run() {
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                et.setSelection(et.getText().length());
                InputMethodManager inputManager = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);

            }
        },200);

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void inputAlert(final Activity cxt, final InputCallBack ic){
        final EditText et = new EditText(cxt);
        et.setHint("至少4个字符");
        et.setText("");
        et.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
        et.setBackgroundColor(Color.argb(20,0,0,0));
        et.setPadding(0,30,0,30);
        et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        final AlertDialog ad = new AlertDialog.Builder(cxt).setTitle("输入以xxx开头的名称，设置后以此开头的唤醒锁会被阻止")
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.length()<4) {
                            Toast.makeText(cxt.getApplicationContext(), "至少输入4个字符" + input, Toast.LENGTH_LONG).show();
                        }else {
                            ic.backData(input,0);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // 先隐藏键盘
                    ((InputMethodManager) et.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(cxt.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    String input = et.getText().toString();
                    if (input.length()<4) {
                        Toast.makeText(cxt.getApplicationContext(), "至少输入4个字符" + input, Toast.LENGTH_LONG).show();
                    }else {
                        ic.backData(input,0);
                        ad.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        et.postDelayed(new Runnable() {
            @Override
            public void run() {
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                InputMethodManager inputManager = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);

            }
        },200);
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static void inputTextAlert(final Activity cxt,final int minlength, final InputCallBack ic){
        final EditText et = new EditText(cxt);
        et.setHint("至少"+minlength+"个字符");
        et.setText("");
        et.setInputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE);
        et.setBackgroundColor(Color.argb(20,0,0,0));
        et.setPadding(0,30,0,30);
        et.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        final AlertDialog ad = new AlertDialog.Builder(cxt).setTitle("请输入内容")
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String input = et.getText().toString();
                        if (input.length()<minlength) {
                            Toast.makeText(cxt.getApplicationContext(), "至少输入"+minlength+"个字符" + input, Toast.LENGTH_LONG).show();
                        }else {
                            ic.backData(input,0);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // 先隐藏键盘
                    ((InputMethodManager) et.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(cxt.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    String input = et.getText().toString();
                    if (input.length()<minlength) {
                        Toast.makeText(cxt.getApplicationContext(), "至少输入"+minlength+"个字符" + input, Toast.LENGTH_LONG).show();
                    }else {
                        ic.backData(input,0);
                        ad.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });
        et.postDelayed(new Runnable() {
            @Override
            public void run() {
                et.setFocusableInTouchMode(true);
                et.requestFocus();
                InputMethodManager inputManager = (InputMethodManager)et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(et, 0);

            }
        },200);
    }

    public interface InputCallBack{
        void backData(String txt, int tag);
    }
}

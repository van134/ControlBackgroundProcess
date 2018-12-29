package com.click369.controlbp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.ColorNavBarService;
import com.click369.controlbp.service.LightView;
import com.click369.controlbp.service.ScreenLightServiceUtil;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.Notify;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.io.File;
import java.util.HashMap;

/**
 * Created by asus on 2017/5/27.
 */
public class ColorSetActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener{
    public static final String COLOR_DEFAULT = "#ffffff";
    private EditText editText;
    private SeekBar sbAlpa,redSb,greenSb,blueSb;
//    private int color = 0;
    private String apppkg,colorStr="ffffffff";
    private String title="";
    private String key = "";
    private SharedPreferences barPrefs;
    private int bgcolor,textcolor,keyColor,lightColor,notifyColor;
    private boolean isbg;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setcolor);
        barPrefs =  SharedPrefsUtil.getInstance(this).uiBarPrefs;//SharedPrefsUtil.getPreferences(this,Common.PREFS_UIBARLIST);//getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
        if(this.getIntent().hasExtra("apppkg")){
            apppkg = this.getIntent().getStringExtra("apppkg");
            this.setTitle("颜色设置");
        }

        if(this.getIntent().hasExtra("data")){
            title = this.getIntent().getStringExtra("data");
            setTitle(title);
        }
        if(this.getIntent().hasExtra("key")){
            key = this.getIntent().getStringExtra("key");
        }
        editText = (EditText) this.findViewById(R.id.color_fliter_et);
        sbAlpa = (SeekBar)this.findViewById(R.id.color_alphasb);
        redSb = (SeekBar)this.findViewById(R.id.color_red_sb);
        greenSb = (SeekBar)this.findViewById(R.id.color_green_sb);
        blueSb = (SeekBar)this.findViewById(R.id.color_blue_sb);
        sbAlpa.setOnSeekBarChangeListener(this);
        redSb.setOnSeekBarChangeListener(this);
        greenSb.setOnSeekBarChangeListener(this);
        blueSb.setOnSeekBarChangeListener(this);
        if(apppkg!=null&&ColorNavBarService.appColors.containsKey(apppkg)){
            colorStr = ColorNavBarService.appColors.get(apppkg);
            String colors[] = colorStr.split("");
            Log.i("DOZEX","colors.length  "+colors.length);
            sbAlpa.setProgress(Integer.parseInt(colors[1]+colors[2],16));
            redSb.setProgress(Integer.parseInt(colors[3]+colors[4],16));
            greenSb.setProgress(Integer.parseInt(colors[5]+colors[6],16));
            blueSb.setProgress(Integer.parseInt(colors[7]+colors[8],16));
            editText.setText("#"+colorStr);
            editText.setBackgroundColor(Color.parseColor("#"+colorStr));
        }else{
            bgcolor =  barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTBGCOLOR,Color.BLACK);
            textcolor = barPrefs.getInt(Common.PREFS_SETTING_UI_TOASTTEXTCOLOR,Color.WHITE);
            keyColor = barPrefs.getInt(Common.PREFS_SETTING_UI_KEYCOLOR,Color.WHITE);
            lightColor = Color.parseColor(barPrefs.getString(Common.PREFS_SETTING_UI_LIGHTCOLOR,"#01d8ff"));
            notifyColor = Color.parseColor(barPrefs.getString(Common.PREFS_SETTING_UI_NOTIFY_SETCOLOR,"#FFFFFF"));
            int color = Color.WHITE;
            if(key.equals(Common.PREFS_SETTING_UI_KEYCOLOR)){
                editText.setBackgroundColor(keyColor);
                editText.setTextColor(Color.CYAN);
                color = keyColor;
            }else  if(key.equals(Common.PREFS_SETTING_UI_LIGHTCOLOR)){
                editText.setBackgroundColor(lightColor);
                editText.setTextColor(Color.WHITE);
                color = lightColor;
            }else  if(key.equals(Common.PREFS_SETTING_UI_NOTIFY_SETCOLOR)){
                editText.setBackgroundColor(notifyColor);
                editText.setTextColor(Color.WHITE);
                color = notifyColor;
            }else{
                editText.setBackgroundColor(bgcolor);
                editText.setTextColor(textcolor);
                isbg = Common.PREFS_SETTING_UI_TOASTBGCOLOR.equals(key);
                color = (isbg?bgcolor:textcolor);
            }
            int alpha = (color & 0xff000000) >>> 24;;
            int red = (color & 0x00ff0000) >> 16;
            int green = (color & 0x0000ff00) >> 8;
            int blue = (color & 0x000000ff);
            sbAlpa.setProgress(alpha);
            redSb.setProgress(red);
            greenSb.setProgress(green);
            blueSb.setProgress(blue);
            editText.setSelection(editText.getText().toString().length());
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String c = s.toString();
                    if(c.startsWith("#")&&c.length()==9){
                        try {
                            int cint = Color.parseColor(c);
                            int alpha = (cint & 0xff000000) >>> 24;;
                            int red = (cint & 0x00ff0000) >> 16;
                            int green = (cint & 0x0000ff00) >> 8;
                            int blue = (cint & 0x000000ff);
                            sbAlpa.setProgress(alpha);
                            redSb.setProgress(red);
                            greenSb.setProgress(green);
                            blueSb.setProgress(blue);
                        }catch (Exception e){

                        }
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        colorStr = changeHex(Integer.toHexString(sbAlpa.getProgress()))+changeHex(Integer.toHexString(redSb.getProgress()))+changeHex(Integer.toHexString(greenSb.getProgress()))+changeHex(Integer.toHexString(blueSb.getProgress()));
        Log.i("CONTROL","colorStr  "+colorStr);
        if(key.equals(Common.PREFS_SETTING_UI_TOASTBGCOLOR)||
                key.equals(Common.PREFS_SETTING_UI_KEYCOLOR)||
                key.equals(Common.PREFS_SETTING_UI_NOTIFY_SETCOLOR)||
                key.equals(Common.PREFS_SETTING_UI_LIGHTCOLOR)){
            editText.setBackgroundColor(Color.parseColor("#"+colorStr));
            editText.setText("#"+colorStr);
        }else{
            editText.setTextColor(Color.parseColor("#"+colorStr));
            editText.setText("#"+colorStr);
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
//        color = Color.argb(sbAlpa.getProgress(),redSb.getProgress(),greenSb.getProgress(),blueSb.getProgress());
        colorStr = changeHex(Integer.toHexString(sbAlpa.getProgress()))+changeHex(Integer.toHexString(redSb.getProgress()))+changeHex(Integer.toHexString(greenSb.getProgress()))+changeHex(Integer.toHexString(blueSb.getProgress()));
        if(key.equals(Common.PREFS_SETTING_UI_TOASTBGCOLOR)||
                key.equals(Common.PREFS_SETTING_UI_KEYCOLOR)||
                key.equals(Common.PREFS_SETTING_UI_NOTIFY_SETCOLOR)||
                key.equals(Common.PREFS_SETTING_UI_LIGHTCOLOR)){
            editText.setBackgroundColor(Color.parseColor("#"+colorStr));
            editText.setText("#"+colorStr);
        }else{
            editText.setTextColor(Color.parseColor("#"+colorStr));
            editText.setText("#"+colorStr);
        }
        editText.setSelection(editText.getText().toString().length());
    }

    public void okClick(View v){
        if(apppkg!=null){
            FileUtil.init();
            File file = new File(FileUtil.FILEPATH,"navcolor");
            if(file.exists()){
                Object o = FileUtil.readObj(file.getAbsolutePath());
                if(o!=null){
                    ColorNavBarService.appColors.clear();
                    ColorNavBarService.appColors.putAll((HashMap<String,String>)o);
                }
            }
            ColorNavBarService.appColors.put(apppkg,colorStr);
            FileUtil.writeObj(ColorNavBarService.appColors,file.getAbsolutePath());
            Intent intent = new Intent();
//        intent.putExtra("data",colorStr);
            this.setResult(0x1,intent);
            this.finish();
        }else{
            if(key.equals(Common.PREFS_SETTING_UI_KEYCOLOR)){
                barPrefs.edit().putInt(key,Color.parseColor("#"+colorStr.substring(2))).commit();
            }else if(key.equals(Common.PREFS_SETTING_UI_LIGHTCOLOR)){
                barPrefs.edit().putString(key,"#"+colorStr.substring(2)).commit();
                WatchDogService.lightColor = "#"+colorStr.substring(2);
                ScreenLightServiceUtil.sendShowLight(LightView.LIGHT_TYPE_TEST,this);
            }else if(key.equals(Common.PREFS_SETTING_UI_NOTIFY_SETCOLOR)){
                barPrefs.edit().putString(key,"#"+colorStr.substring(2)).commit();
                Intent sysIntent = new Intent("com.click369.control.sysui.loadconfig");
                sysIntent.putExtra("notifycolor","#"+colorStr.substring(2));
                sendBroadcast(sysIntent);
                Notify.testNotify(ColorSetActivity.this);
            }else{
                barPrefs.edit().putInt(key,Color.parseColor("#"+colorStr)).commit();
            }
            Log.i("CONTROL",key+colorStr.substring(2));
            showT("设置成功");
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            },1000);
        }
    }

    @Override
    public void onBackPressed() {
        nokClick(null);
    }

    private String changeHex(String hex){
        if(hex.length()==1){
            return "0"+hex;
        }
        return hex;
    }

    public void nokClick(View v){
        this.setResult(0x2,null);
        this.finish();
    }
}

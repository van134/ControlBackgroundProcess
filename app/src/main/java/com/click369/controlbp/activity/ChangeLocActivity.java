package com.click369.controlbp.activity;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.click369.controlbp.R;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

/**
 * Created by asus on 2017/5/27.
 */
public class ChangeLocActivity extends BaseActivity{
    private EditText latEt,lonEt;
    private Switch changeLocSw;
    private LinearLayout webViewLay;
//    private FrameLayout swFl;
    private TextView changeloc_btn;
    private WebView webView;
    private TextView alertTv;
    private int curColor= Color.BLACK;
    private SharedPreferences settings;
    private String lat,lon;
    private ClipboardManager cm;
    String pkg,name;
    boolean isrechange = false,isChange = false;
    private int mapType = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeloc);
        lonEt = (EditText)this.findViewById(R.id.changeloc_jing_et);
        latEt = (EditText)this.findViewById(R.id.changeloc_wei_et);
        changeLocSw = (Switch)this.findViewById(R.id.changeloc_sw);
        changeloc_btn = (TextView) this.findViewById(R.id.changeloc_btn);
//        swFl = (FrameLayout) this.findViewById(R.id.skip_dialog_fl);
        webViewLay = (LinearLayout)this.findViewById(R.id.changeloc_web);
        alertTv = (TextView) this.findViewById(R.id.changeloc_alerttv);
        curColor = alertTv.getCurrentTextColor();
        pkg = getIntent().getStringExtra("pkg");
        name = getIntent().getStringExtra("name");
        lon = getIntent().getStringExtra("lon");
        lat = getIntent().getStringExtra("lat");
        isrechange = getIntent().getBooleanExtra("isrechange",false);
        webView=new WebView(this);

        webView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        webViewLay.addView(webView);
        if (MainActivity.isNightMode){
            curColor = Color.GRAY;
        }
        lonEt.setTextColor(curColor);
        latEt.setTextColor(curColor);
        alertTv.setTextColor(curColor);
        changeLocSw.setTextColor(curColor);
        this.setTitle("虚拟定位");
        if(!TextUtils.isEmpty(pkg)){
            this.setTitle("自定义"+name+"的位置");
        }
        settings =  SharedPrefsUtil.getInstance(this).settings;//SharedPrefsUtil.getPreferences(getApplicationContext(), Common.IPREFS_TVLIST);
        changeLocSw.setChecked(isrechange);
        if(TextUtils.isEmpty(lat)){
            try{
                lat = settings.getString(Common.PREFS_SETTING_OTHER_LOC_LAT,"39.916803");
                lon = settings.getString(Common.PREFS_SETTING_OTHER_LOC_LON,"116.403766");
            }catch (Exception e){
                lat = "39.916803";
                lon = "116.403766";
            }
        }
        latEt.setText(lat);
        lonEt.setText(lon);
        changeLocSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isrechange = isChecked;
                isChange = true;
            }
        });
        lonEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String string = lonEt.getText().toString().trim();
                if(!TextUtils.isEmpty(string)){
                   try {
                       double lond = Double.parseDouble(string);
                       lon = lond+"";
                       settings.edit().putString(Common.PREFS_SETTING_OTHER_LOC_LON,lon).commit();
                       isChange = true;
                   }catch (Throwable e){
                       e.printStackTrace();
                       lonEt.setText("");
                       showT("定位切换失败，包含非数字");
                       lon = "";
                   }
                }
            }
        });
        latEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String string = latEt.getText().toString().trim();
                if(!TextUtils.isEmpty(string)){
                   try {
                       double latd = Double.parseDouble(string);
                       lat = latd+"";
                       settings.edit().putString(Common.PREFS_SETTING_OTHER_LOC_LAT,lat).commit();
                       isChange = true;
                   }catch (Throwable e){
                       e.printStackTrace();
                       latEt.setText("");
                       showT("定位切换失败，包含非数字");
                       lat = "";
                   }
                }
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setDomStorageEnabled(true);//主要是这句
        webSettings.setJavaScriptEnabled(true);//启用js
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.supportMultipleWindows();
        webSettings.setAllowContentAccess(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSavePassword(true);
        webSettings.setSaveFormData(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        // 设置可以支持缩放
        webSettings.setSupportZoom(false);
        // 设置出现缩放工具
        webSettings.setBuiltInZoomControls(false);
        //扩大比例的缩放
        webSettings.setUseWideViewPort(false);
        //自适应屏幕
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webView.setWebChromeClient(new WebChromeClient());//这行最好不要丢掉
        webView.setWebViewClient(new WebViewClient());
        mapType = settings.getInt("MAP_TYPE",mapType);
        if(mapType==1){
            webView.clearCache(true);
            webView.setInitialScale(200);
            webView.loadUrl("http://www.gpsspg.com/maps.htm");
            changeloc_btn.setText("地图二");
        }else{
            webView.clearCache(true);
            webView.setInitialScale(140);
            webView.loadUrl("https://lbs.amap.com/console/show/picker");
            changeloc_btn.setText("地图一");
        }

//        webView.setScaleX(1.2f);
//        webView.setScaleY(1.2f);
//        webView.loadUrl("http://www.gpsspg.com/maps.htm");
        cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);


    }
    ClipboardManager.OnPrimaryClipChangedListener lis = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            if(cm.getPrimaryClip().getItemCount()>0){
                CharSequence t = cm.getPrimaryClip().getItemAt(cm.getPrimaryClip().getItemCount()-1).getText();
                String s = t.toString();
                if(mapType == 0){
                    if(s.contains(",")){
                        String ss[] = s.split(",");
                        lonEt.setText(ss[0]);
                        latEt.setText(ss[1]);
                    }
                }else{
                    if(s.contains(",")){
                        String ss[] = s.split(",");
                        lonEt.setText(ss[1]);
                        latEt.setText(ss[0]);
                    }
                }
            }
        }
    };

    public void changeMapClick(View v){
        if(mapType==0){
            mapType=1;
            webView.clearCache(true);
            webView.setInitialScale(200);
            webView.loadUrl("http://www.gpsspg.com/maps.htm");
            changeloc_btn.setText("地图二");
        }else{
            webView.clearCache(true);
            mapType = 0;
            webView.setInitialScale(140);
            webView.loadUrl("https://lbs.amap.com/console/show/picker");
            changeloc_btn.setText("地图一");
        }
        settings.edit().putInt("MAP_TYPE",mapType).commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cm.removePrimaryClipChangedListener(lis);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        if(!TextUtils.isEmpty(lat)&&!TextUtils.isEmpty(lon)&&isChange){
            intent.putExtra("lon",lon);
            intent.putExtra("lat",lat);
            intent.putExtra("isrechange",isrechange);
        }
        setResult(0x11,intent);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webViewLay.removeView(webView);
        webView.stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        webView.getSettings().setJavaScriptEnabled(false);
        webView.clearHistory();
        webView.clearView();
        webView.removeAllViews();
        webView.destroy();
        System.exit(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        cm.addPrimaryClipChangedListener(lis);
    }

}

package com.click369.controlbp.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.ADBTestActivity;
import com.click369.controlbp.activity.BaseActivity;
import com.click369.controlbp.activity.CPUSetActivity;
import com.click369.controlbp.activity.ChangeTextActivity;
import com.click369.controlbp.activity.EmptyActivity;
import com.click369.controlbp.activity.IceRoomActivity;
import com.click369.controlbp.activity.LimitForceCleanActivity;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.activity.RunningActivity;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.PermissionUtils;
import com.click369.controlbp.util.SELinuxUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilBackStop;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.ShellUtils;
import com.click369.controlbp.util.ShortCutUtil;

import net.qiujuer.genius.blur.StackBlur;

import org.xmlpull.v1.XmlPullParser;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


public class OtherFragment extends BaseFragment {
    private TextView jxCleanTv,cleanSysTv,cleanProcessTv,kjRunTv,kjIceTv,kjCleanTv,kjjxCleanTv,kjRestartTv,adbTv,wifiTv,killSelfTv,changeTextTv,cleanTv,changeBgTv,bgBlurTv,bgBrightTv,kjCPUTv;
    private Switch selSw,homeErrorSw,cleanSw,stopScanMeidaSw;//toastSw;
    private SeekBar cleanSb,bgBlurSb,bgBrightSb;
    private ActivityManager activityManager;
    private MainActivity mainActivity;
    private int curColor = Color.BLACK;
    private SharedPreferences settings;
    private int alpha = 100,blur = 0;
    private TextView tvs[] = null;
    private SeekBar sbs[] = null;
    public OtherFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity)getActivity();
        View v = inflater.inflate(R.layout.fragment_other, container, false);
        settings = SharedPrefsUtil.getInstance(getActivity()).settings;//SharedPrefsUtil.getPreferences(getActivity(),Common.PREFS_APPSETTINGS);
        selSw = (Switch) v.findViewById(R.id.other_sel_sw);
        stopScanMeidaSw = (Switch) v.findViewById(R.id.other_scanmedia_sw);
        homeErrorSw = (Switch) v.findViewById(R.id.other_homeerr_sw);
        cleanSw = (Switch) v.findViewById(R.id.other_clean_sw);
        cleanSb = (SeekBar) v.findViewById(R.id.other_clean_sb);
        bgBlurSb = (SeekBar) v.findViewById(R.id.other_bg_blursb);
        bgBrightSb = (SeekBar) v.findViewById(R.id.other_bg_brightsb);
        cleanTv = (TextView) v.findViewById(R.id.other_clean_title);
        adbTv = (TextView) v.findViewById(R.id.other_adb_tv);
        curColor = adbTv.getCurrentTextColor();
        selSw.setTextColor(curColor);
        stopScanMeidaSw.setTextColor(curColor);
        homeErrorSw.setTextColor(curColor);
        cleanSw.setTextColor(curColor);
//        colorBarTv = (TextView) v.findViewById(R.id.other_colorbar_tv);
        kjCPUTv = (TextView) v.findViewById(R.id.other_kj_cpu_tv);
        jxCleanTv = (TextView) v.findViewById(R.id.other_jixian_tv);
        cleanSysTv = (TextView) v.findViewById(R.id.other_cleansys_tv);
        cleanProcessTv = (TextView) v.findViewById(R.id.other_cleanprocess_tv);
        kjRunTv = (TextView) v.findViewById(R.id.other_kj_run_tv);
        kjIceTv = (TextView) v.findViewById(R.id.other_kj_ice_tv);
        kjCleanTv = (TextView) v.findViewById(R.id.other_kj_clean_tv);
        kjjxCleanTv = (TextView) v.findViewById(R.id.other_kj_jxclean_tv);
        kjRestartTv = (TextView) v.findViewById(R.id.other_kj_restart_tv);
        wifiTv = (TextView) v.findViewById(R.id.other_wifipwd_tv);
        killSelfTv = (TextView) v.findViewById(R.id.other_killself_tv);
        changeTextTv = (TextView) v.findViewById(R.id.other_changetext_tv);
        changeBgTv = (TextView) v.findViewById(R.id.other_changbg_tv);
        bgBlurTv = (TextView) v.findViewById(R.id.other_bg_blurvalue);
        bgBrightTv = (TextView) v.findViewById(R.id.other_bg_brightvalue);
        ImageView pen = (ImageView) v.findViewById(R.id.other_pen1_iv);
        ImageView pen2 = (ImageView) v.findViewById(R.id.other_pen2_iv);
        ImageView pen3 = (ImageView) v.findViewById(R.id.other_pen3_iv);
        pen.setTag(3);
        pen2.setTag(7);
        pen3.setTag(8);
        PenClickListener penClickListener = new PenClickListener();
        pen.setOnClickListener(penClickListener);
        pen2.setOnClickListener(penClickListener);
        pen3.setOnClickListener(penClickListener);
        ItemClick itemClick = new ItemClick();
//        colorBarTv.setOnClickListener(itemClick);
        jxCleanTv.setOnClickListener(itemClick);
        cleanSysTv.setOnClickListener(itemClick);
        cleanProcessTv.setOnClickListener(itemClick);
        kjRunTv.setOnClickListener(itemClick);
        kjIceTv.setOnClickListener(itemClick);
        kjCleanTv.setOnClickListener(itemClick);
        kjjxCleanTv.setOnClickListener(itemClick);
        kjRestartTv.setOnClickListener(itemClick);
        adbTv.setOnClickListener(itemClick);
        wifiTv.setOnClickListener(itemClick);
        killSelfTv.setOnClickListener(itemClick);
        changeTextTv.setOnClickListener(itemClick);
        changeBgTv.setOnClickListener(itemClick);
        kjCPUTv.setOnClickListener(itemClick);
        SwCheckListener swLis = new SwCheckListener();

//        toastSw.setOnCheckedChangeListener(swLis);
        activityManager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        try {
            new Thread(){
                @Override
                public void run() {
                    final boolean isOn = SELinuxUtil.isSELOpen();
                    if (getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                selSw.setChecked(isOn);
                            }
                        });
                    }
                }
            }.start();
        }catch (Exception e){
            e.printStackTrace();
        }
//        adSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_ADJUMP,false));
        int time = settings.getInt(Common.PREFS_SETTING_OTHER_CLEANDELAYTIME,0);
        cleanSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_OTHER_ISCLEAN,false));
        homeErrorSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_OTHER_HOMEERROR,false));
        stopScanMeidaSw.setChecked(settings.getBoolean(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA,false));
        cleanSb.setProgress(time);

        selSw.setTag(0);
        cleanSw.setTag(1);
        homeErrorSw.setTag(2);
        stopScanMeidaSw.setTag(3);
        selSw.setOnCheckedChangeListener(swLis);
        cleanSw.setOnCheckedChangeListener(swLis);
        homeErrorSw.setOnCheckedChangeListener(swLis);
        stopScanMeidaSw.setOnCheckedChangeListener(swLis);


        blur  = settings.getInt(Common.PREFS_SETTING_OTHER_BGBLUR,0);
        alpha = settings.getInt(Common.PREFS_SETTING_OTHER_BGBRIGHT,100);
        bgBlurSb.setProgress(blur);
        bgBrightSb.setProgress(alpha);
        bgBlurTv.setText("应用控制器背景图片模糊度:"+blur);
        bgBrightTv.setText("应用控制器背景图片透明度:"+alpha);

        cleanTv.setText("熄屏清理内存冗余数据延迟:"+time+"秒");
        SeekBarListener sbl = new SeekBarListener();
        cleanSb.setTag(0);
        bgBlurSb.setTag(1);
        bgBrightSb.setTag(2);
        cleanSb.setOnSeekBarChangeListener(sbl);
        bgBlurSb.setOnSeekBarChangeListener(sbl);
        bgBrightSb.setOnSeekBarChangeListener(sbl);
        if (!cleanSw.isChecked()){
            cleanTv.setAlpha(0.7f);
            cleanSb.setAlpha(0.7f);
            cleanSb.setEnabled(false);
        }
        tvs = new TextView[]{cleanTv,bgBlurTv,bgBrightTv};
        sbs = new SeekBar[]{cleanSb,bgBlurSb,bgBrightSb};
        return v;
    }
    class SwCheckListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            BaseActivity.zhenDong(getContext());
            if (buttonView.equals(selSw)){
                if (!isChecked){
                    SELinuxUtil.closeSEL();
                }else{
                    SELinuxUtil.openSEL();
                }
                settings.edit().putBoolean(Common.PREFS_SETTING_SELOPEN,isChecked).commit();
            } else if (buttonView.equals(cleanSw)){
                settings.edit().putBoolean(Common.PREFS_SETTING_OTHER_ISCLEAN,isChecked).commit();
                WatchDogService.isOffClean = isChecked;
                if (isChecked){
                    cleanTv.setAlpha(1.0f);
                    cleanSb.setAlpha(1.0f);
                    cleanSb.setEnabled(true);
                }else{
                    cleanTv.setAlpha(0.7f);
                    cleanSb.setAlpha(0.7f);
                    cleanSb.setEnabled(false);
                }
            }else if(buttonView.equals(homeErrorSw)){
                settings.edit().putBoolean(Common.PREFS_SETTING_OTHER_HOMEERROR,isChecked).commit();
            }else if(buttonView.equals(stopScanMeidaSw)){
                settings.edit().putBoolean(Common.PREFS_SETTING_OTHER_STOPSCANMEDIA,isChecked).commit();
            }

        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    class ItemClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            BaseActivity.zhenDong(getContext());
            if(v.equals(adbTv)){
               Intent intent = new Intent(getActivity(),ADBTestActivity.class);
                startActivity(intent);
            }else if(v.equals(wifiTv)){
                showWifiPwd();
            }
//            else if(v.equals(colorBarTv)){
//                if (!Settings.canDrawOverlays(getActivity())) {
//                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivityForResult(intent, 1);
//                }else{
//                    Intent itent = new Intent(getActivity(),ColorNavBarActivity.class);
//                    startActivity(itent);
//                }
//            }
            else if(v.equals(jxCleanTv)){
                AlertUtil.showConfirmAlertMsg(getActivity(),"清理时间较长，个别系统可能会卡死，是否清理？",new AlertUtil.InputCallBack(){
                    @Override
                    public void backData(String txt, int tag) {
                        if(tag == 1){
                            clearMomery();
                        }
                    }
                });
            }else if(v.equals(cleanSysTv)){
                clearHuanCun(0);
            }else if(v.equals(cleanProcessTv)){
                clearHuanCun(1);
            }else if(v.equals(kjRunTv)){
                ShortCutUtil.addShortcut("正在运行",getActivity().getApplicationContext(),RunningActivity.class,R.drawable.icon_run);
                Toast.makeText(getActivity(),"正在运行 快捷方式创建成功",Toast.LENGTH_LONG).show();
            }else if(v.equals(kjIceTv)){
                ShortCutUtil.addShortcut("冷藏室",getActivity().getApplicationContext(),IceRoomActivity.class,R.drawable.icon_iceroom);
                Toast.makeText(getActivity(),"冷藏室 快捷方式创建成功",Toast.LENGTH_LONG).show();
            }else if(v.equals(kjCleanTv)){
                ShortCutUtil.addShortcut("缓存清理",getActivity().getApplicationContext(),EmptyActivity.class,R.drawable.icon_clean);
                Toast.makeText(getActivity(),"缓存清理 快捷方式创建成功",Toast.LENGTH_LONG).show();
            }else if(v.equals(kjjxCleanTv)){
                ShortCutUtil.addShortcut("极限清理",getActivity().getApplicationContext(),LimitForceCleanActivity.class,R.drawable.icon_clean);
                Toast.makeText(getActivity(),"极限清理 快捷方式创建成功",Toast.LENGTH_LONG).show();
            }else if(v.equals(kjCPUTv)){
                ShortCutUtil.addShortcut("CPU设置",getActivity().getApplicationContext(),CPUSetActivity.class,R.drawable.icon_cpu);
                Toast.makeText(getActivity(),"CPU设置 快捷方式创建成功",Toast.LENGTH_LONG).show();
            }else if(v.equals(kjRestartTv)){
                ShortCutUtil.addShortcut("重启选项",getActivity().getApplicationContext(),EmptyActivity.class,R.drawable.icon_restart);
                Toast.makeText(getActivity(),"重启选项 快捷方式创建成功",Toast.LENGTH_LONG).show();
            }else if(v.equals(killSelfTv)){
                //System.exit(0);
                String titles[] = {"重启系统界面","重启到RECOVER","重启手机","关机","自杀(仅前台界面)"};
                AlertUtil.showListAlert(getActivity(), "重启菜单", titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if (tag ==0){
                            Intent intent1 = new Intent("com.click369.control.rebootsystemui");
                            getActivity().sendBroadcast(intent1);
                        }else if (tag ==1){
                            ShellUtils.execCommand("reboot recovery",true);
                        }else if (tag ==2){
                            ShellUtils.execCommand("reboot",true);
                        }else if (tag ==3){
                            ShellUtils.execCommand("reboot -p",true);
                        }else if (tag == 4){
                            Intent intent1 = new Intent("com.click369.control.ams.killself");
                            getActivity().sendBroadcast(intent1);
                        }
                    }
                });
            }else if(v.equals(changeTextTv)){
                Intent itent = new Intent(getActivity(),ChangeTextActivity.class);
                startActivity(itent);
            }else if(v.equals(changeBgTv)){
                mainActivity.getPhoto.setIsNeedCrop(true);
                mainActivity.getPhoto.setPhotofile(mainActivity.bgFile);
                boolean isOk = true;
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
                    if(!PermissionUtils.checkPermissionAllGranted(mainActivity,permissions)){
                        isOk = false;
                    }
                }
                if(isOk){
                    AlertUtil.showListAlert(mainActivity, "请选择", new String[]{"选择背景图片","清除背景图片"}, new AlertUtil.InputCallBack() {
                        @Override
                        public void backData(String txt, int tag) {
                            if (tag == 0){
                                mainActivity.getPhoto.setScale();
                                mainActivity.getPhoto.photoWithGrelly();
                            }else if(tag == 1){
                                if(mainActivity.bgFile.exists()){
                                    mainActivity.bgFile.delete();
                                }
                                if(mainActivity.bgBlurFile.exists()){
                                    mainActivity.bgBlurFile.delete();
                                }
                                mainActivity.mainRL.setBackground(null);
                                mainActivity.mainRL.setBackgroundColor(MainActivity.isNightMode?Color.BLACK:Color.WHITE);
                            }
                        }
                    });
                }else{
                    Toast.makeText(mainActivity,"没有文件读写权限",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    String s = null;
    private void showWifiPwd(){
       new Thread(){
           @Override
           public void run() {
               try {

                   if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
                       s =  ShellUtils.execCommand("cat /data/misc/wifi/*.conf",true,true).successMsg;
                       s = URLDecoder.decode(s,"UTF-8");
                       if (s != null && s.contains("network={")) {
                           s = s.substring(s.indexOf("network={"));
                           s = s.replaceAll("network=\\{", "");
                           s = s.replaceAll("\\}", "");
                           s = s.replaceAll("bssid", "MAC");
                           s = s.replaceAll("priority", "优先级");
                           s = s.replaceAll("key_mgmt", "加密方式");
                           s = s.replaceAll("id_str", "参数");
                           s = s.replaceAll("ssid", "名称");
                           s = s.replaceAll("psk", "密码");
                       }
                   }else{
                       try {
                           StringBuilder sb = new StringBuilder();
                           String msgs =  ShellUtils.execCommand("cat /data/misc/wifi/WifiConfigStore.xml",true,true).successMsg;
                           msgs = URLDecoder.decode(msgs,"UTF-8");
                           XmlPullParser pullParser = Xml.newPullParser();
                           CharArrayWriter cw = new CharArrayWriter();
                           cw.write(msgs);
                           pullParser.setInput(new CharArrayReader(cw.toCharArray()));
                           int event = pullParser.getEventType();// 觸發第一個事件
                           while (event != XmlPullParser.END_DOCUMENT) {
                               switch (event) {
                                   case XmlPullParser.START_DOCUMENT:
//                                       persons = new ArrayList<Person>();
                                       break;
                                   case XmlPullParser.START_TAG:
                                      if(pullParser.getAttributeCount() >0) {
                                          String value = pullParser.getAttributeValue(0);
                                          if ("SSID".equals(value)) {
                                              String wifiName = pullParser.nextText();
                                              sb.append("名称:").append(wifiName.replaceAll("\"","")).append("\n");
                                          } else if ("PreSharedKey".equals(value)) {
                                              String wifiPwd = pullParser.nextText();
                                              sb.append("密码:").append(wifiPwd.replaceAll("\"","")).append("\n\n");
                                          }
                                      }
                                       break;
                                   case XmlPullParser.END_TAG:
                                       break;
                               }
                               event = pullParser.next();
                           }
                           s = sb.toString();
                       }catch (Exception e){
                           e.printStackTrace();
                       }
                   }
               } catch (Exception e) {
                   e.printStackTrace();
               }
               if (getActivity()!=null) {
                   getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           AlertUtil.showAlertMsg(getActivity(), s == null ? "获取数据失败" : s);
                       }
                   });
               }
           }
       }.start();

   }
    ProgressDialog pd= null;
    final ActivityManager.MemoryInfo memoryInfo1 = new ActivityManager.MemoryInfo();
    final ActivityManager.MemoryInfo memoryInfo2 = new ActivityManager.MemoryInfo();
    public void clearMomery(){
        pd= ProgressDialog.show(getContext(), "", "开始整理内存，请稍后...", false, true);
        new Thread(){
            public void run(){
                try {
                    if(getActivity()!=null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pd.show();
                            }
                        });
                    }
                    activityManager.getMemoryInfo(memoryInfo1);
                    String psRes = ShellUtilBackStop.execCommand("ps",true);
                    ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
                    apps.addAll(appLoader.allAppInfos);
                    List<String> lists = new ArrayList<String>();
                    lists.add("sync");
                    lists.add("echo 3 > /proc/sys/vm/drop_caches");
                    lists.add("am kill-all");
                    for(AppInfo ai:apps) {
                        if (psRes.contains(ai.getPackageName())) {
                            if(ai.getPackageName().startsWith("com.click369")||
                                    ai.getPackageName().contains("navbarapps")){
                                continue;
                            }
                            lists.add("am force-stop "+ai.getPackageName());
                            WatchDogService.sendRemoveRecent(ai.getPackageName(),getActivity());
                            if(ai.isNotStop){
                                XposedStopApp.stopApk(ai.getPackageName(),getContext());
                            }
                        }
                    }
                    Thread.sleep(1000);
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    pd.setMessage("整理完毕，开始清理...");
                                    pd.show();
                                } catch (Exception e) {
                                }
                            }
                        });
                    }
//                    ShellUtils.execCommand(lists,true,true);
                    ShellUtilNoBackData.execCommand(lists);
                    Thread.sleep(2500);
                    if(getActivity()!=null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activityManager.getMemoryInfo(memoryInfo2);
                                long bit = memoryInfo2.availMem - memoryInfo1.availMem;
                                try {
                                    if (pd != null && pd.isShowing()) {
                                        pd.cancel();
                                    }
                                } catch (Exception e) {
                                }
                                Toast.makeText(getActivity(), "内存已释放完毕,共释放" + (bit / (1024 * 1024)) + "M", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void clearHuanCun(final int type){//0系统 1进程
        pd= ProgressDialog.show(getContext(), "", "正在清理内存，请稍后...", true, true);
        new Thread(){
            @Override
            public void run() {
                activityManager.getMemoryInfo(memoryInfo1);
                List<String> lists = new ArrayList<String>();
                lists.add("sync");
                lists.add("echo 3 > /proc/sys/vm/drop_caches");
                if(type == 1){
                    lists.add("am kill-all");
                }
                ShellUtilNoBackData.execCommand(lists);
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                activityManager.getMemoryInfo(memoryInfo2);
                final long bit = memoryInfo2.availMem - memoryInfo1.availMem;
                if(getActivity()!=null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        try {
                            if (pd != null && pd.isShowing()) {
                                pd.cancel();
                            }
                        } catch (Exception e) {
                        }
                        Toast.makeText(getActivity(), "内存已释放完毕,共释放" + (bit / (1024 * 1024)) + "M", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }

    class SeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int tag= (Integer) seekBar.getTag();
            String names[] = {"熄屏清理内存冗余数据延迟:","应用控制器背景图片模糊度:","应用控制器背景图片亮度:"};
            tvs[tag].setText(names[tag]+seekBar.getProgress()+(tag==0?"秒":""));

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            BaseActivity.zhenDong(getContext());
            int tag= (Integer) seekBar.getTag();
            int offset = seekBar.getProgress()%10;
            if(tag ==0){
                seekBar.setProgress(seekBar.getProgress()+(offset==0?0:(offset>5?(10-offset):-1*offset)));
            }
            String names[] = {Common.PREFS_SETTING_OTHER_CLEANDELAYTIME,Common.PREFS_SETTING_OTHER_BGBLUR,Common.PREFS_SETTING_OTHER_BGBRIGHT};
            settings.edit().putInt(names[tag],seekBar.getProgress()).commit();
            if(tag == 0){
                WatchDogService.delayCleanTime = seekBar.getProgress();
            }else if(tag == 1){
                blur = seekBar.getProgress();
                changeBgBlur(mainActivity,seekBar.getProgress(),alpha);
            }else if(tag == 2){
                alpha = seekBar.getProgress();
                changeBgBlur(mainActivity,blur,alpha);
            }
        }
    }

    class PenClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            final int tag= (Integer) view.getTag();
            AlertUtil.inputAlert(getActivity(), tag, new AlertUtil.InputCallBack() {
                @Override
                public void backData(String txt, int tag) {
                    if(txt!=null&&txt.length()>0){
                        int value= Integer.parseInt(txt);
//                        settings.edit().putInt(Common.PREFS_SETTING_OTHER_CLEANDELAYTIME,value).commit();
                        if(tag == 3){
                            WatchDogService.delayCleanTime = value;
                            tag = 0;
                        }else{
                            tag = tag- 6;
                            if(tag == 1){
                                blur = value;
                            }else{
                                alpha = value;
                            }
                            changeBgBlur(mainActivity,blur,alpha);
                        }
                        String names[] = {Common.PREFS_SETTING_OTHER_CLEANDELAYTIME,Common.PREFS_SETTING_OTHER_BGBLUR,Common.PREFS_SETTING_OTHER_BGBRIGHT};
                        settings.edit().putInt(names[tag],value).commit();
                        String names1[] = {"熄屏清理内存冗余数据延迟:","应用控制器背景图片模糊度:","应用控制器背景图片透明度:"};
                        tvs[tag].setText(names1[tag]+value+(tag==0?"秒":""));
                        sbs[tag].setProgress(value);
                    }
                }
            });
        }
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void changeBgBlur(MainActivity mainActivity,final int blur,final int alpha){
        if(mainActivity.bgFile.exists()){
            try {
                Drawable d = null;
                if(blur>0){
                    Bitmap bitmap = StackBlur.blurNativelyPixels(BitmapFactory.decodeFile(mainActivity.bgFile.getAbsolutePath()),blur, false);
                    FileOutputStream fos = new FileOutputStream(mainActivity.bgBlurFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,95,fos);
                    d = Drawable.createFromPath(mainActivity.bgBlurFile.getAbsolutePath());
                }else{
                    d = Drawable.createFromPath(mainActivity.bgFile.getAbsolutePath());
                }
                d.setAlpha((int)(alpha*2.55));
                mainActivity.mainRL.setBackgroundColor(Color.BLACK);
                mainActivity.mainRL.setBackground(d);
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            Toast.makeText(mainActivity,"还未选择图片",Toast.LENGTH_SHORT).show();
        }
    }
}

package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.lang.reflect.Method;


/**
 * Created by asus on 2017/5/27.
 */
public class RoundedCornerService{
    public static int floatLeve = 2003;

//    public static boolean isRoundRun = false;
//    public PixelView pv;
    boolean isShow = false,isShowKeyBar;
    //定义浮动窗口布局
    FrameLayout mFloatLayoutTop,mFloatLayoutTop1,mFloatLayoutBottom,imeFloatLayout,infoFloatLayout;
    boolean infoFloatIsShow = false;
    LinearLayout actInfoLL;
    TextView actInfoTV,actInfoShouSuo,actInfoQingKong,actInfoGuanBi;
    WindowManager.LayoutParams wmParamsBottom,wmParamsTop,wmParamsTop1,wmParamsInfo;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;
    Handler hander = new Handler();
    MyReciver rec;
    SharedPreferences barPrefs;
//    ScreenLightServiceUtilNew screenLightServiceUtil;
    ScreenLightServiceUtil screenLightServiceUtil;
    public RoundedCornerService(){}
    Context context;
    public  RoundedCornerService(Context context) {
        this.context = context;
        barPrefs = SharedPrefsUtil.getInstance(context).uiBarPrefs;//SharedPrefsUtil.getPreferences(this, Common.PREFS_UIBARLIST);
        mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
//        screenLightServiceUtil = new ScreenLightServiceUtilNew(this,mWindowManager);
        screenLightServiceUtil = new ScreenLightServiceUtil(this,mWindowManager);
        isShowKeyBar = barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR,false);
        rec = new MyReciver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.click369.control.openinstall");
        filter.addAction("com.click369.control.closeinstall");
        filter.addAction("com.click369.control.restartcor");
        filter.addAction("com.click369.control.imeclose");
        filter.addAction("com.click369.control.imeopen");
        filter.addAction("com.click369.control.corchangeposition");
        filter.addAction("com.click369.control.float.actinfo");
        filter.addAction("com.click369.control.float.infouishow");
        filter.addAction("com.click369.control.light.show");
        filter.addAction("com.click369.control.light.hide");
        filter.addAction("com.click369.control.light.changeposition");
        filter.addAction("com.click369.control.float.checkxp");
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        context.registerReceiver(rec,filter);
        Intent check = new Intent("com.click369.control.ams.float.checkxp");
        check.putExtra("isNeedFloadOnSys",WatchDogService.isNeedFloatOnSys);
        context.sendBroadcast(check);
        hander.postDelayed(checkRunable,1000);

    }
    Runnable checkRunable = new Runnable() {
        @Override
        public void run() {
            Intent check = new Intent("com.click369.control.float.checkxp");
            check.putExtra("isfloatok",false);
            context.sendBroadcast(check);
        }
    };


    boolean isAllReadyInit = false;
    private void init(){
        if(isAllReadyInit){
            return;
        }
        Log.i("CONTROL","start float view");
        isAllReadyInit = true;
        screenLightServiceUtil.init();
//        pv = new PixelView(context);
//        pv.setWidowManager(mWindowManager,context);
        if(WatchDogService.isRoundCorOpen){
            Configuration config = context.getResources().getConfiguration();
            // 如果当前是横屏
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE){
                createFloatView(1);
            }else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
                createFloatView(0);
            }
        }
    }
    int dhheight = 0,statusheight = 0;
    int offset = 0;
    int topoffset = 0;
    int roundSize = 35;
    long clickTime = 0;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void createFloatView(int type)//0竖 1横
    {
//        if ((Build.VERSION.SDK_INT >=Build.VERSION_CODES.M&&Settings.canDrawOverlays(this))||Build.VERSION.SDK_INT <Build.VERSION_CODES.M) {
//            isRoundRun = true;
            wmParamsBottom = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    floatLeve,
                    536, -3);
            wmParamsTop = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    2003,
                    536, -3);
            wmParamsTop1 = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    floatLeve,
                    536, -3);
            wmParamsInfo = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            //设置window type
            wmParamsInfo.type = 2003;
            //设置图片格式，效果为背景透明
            wmParamsInfo.format = PixelFormat.RGBA_8888;
            //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
            wmParamsInfo.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //调整悬浮窗显示的停靠位置为左侧置顶
            wmParamsInfo.gravity = Gravity.LEFT | Gravity.TOP;
            wmParamsInfo.height = WindowManager.LayoutParams.WRAP_CONTENT;//height-dhheight+offset;
            wmParamsInfo.width = WindowManager.LayoutParams.WRAP_CONTENT;
            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            wmParamsInfo.x = 0;
            wmParamsInfo.y = 0;
//                2003, 536, -3);
            //获取的是WindowManagerImpl.CompatModeWrapper
//            WindowManager.LayoutParams.TYPE_PHONE

            //设置图片格式，效果为背景透明
            wmParamsBottom.format = PixelFormat.RGBA_8888;
            wmParamsTop.format = PixelFormat.RGBA_8888;
            wmParamsTop1.format = PixelFormat.RGBA_8888;
            //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //调整悬浮窗显示的停靠位置为左侧置顶
            wmParamsBottom.gravity = Gravity.LEFT | Gravity.BOTTOM;
            wmParamsTop.gravity = Gravity.LEFT | Gravity.TOP;
            wmParamsTop1.gravity = Gravity.LEFT | Gravity.TOP;
//        wmParams.gravity = 53;//Gravity.LEFT | Gravity.BOTTOM;
            Point p = new Point();
            mWindowManager.getDefaultDisplay().getRealSize(p);
            int height = p.y;
            //设置悬浮窗口长宽数据
            wmParamsBottom.width = WindowManager.LayoutParams.MATCH_PARENT;
            wmParamsTop.width = WindowManager.LayoutParams.MATCH_PARENT;
            wmParamsTop1.width = WindowManager.LayoutParams.MATCH_PARENT;
            dhheight = getVirtualBarHeigh(context);
            statusheight = getZhuangTaiHeight(context);
            topoffset = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDTOPOFFSET,statusheight);
            offset = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDOFFSET,dhheight);
            roundSize = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDSIZE,35);
            wmParamsTop.height = WindowManager.LayoutParams.WRAP_CONTENT;//height-dhheight+offset;
            wmParamsTop1.height = WindowManager.LayoutParams.WRAP_CONTENT;//height-dhheight+offset;
            wmParamsBottom.height = WindowManager.LayoutParams.WRAP_CONTENT;//height-dhheight+offset;

//            if(setting.getBoolean(Common.PREFS_SETTING_UI_ROUNDALLOPEN,false)){
//                wmParams.height = height;
//            }
            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            wmParamsTop.x = 0;
            wmParamsTop1.x = 0;
            wmParamsBottom.x = 0;
            wmParamsTop.y = topoffset * -1;
            wmParamsTop1.y = topoffset * -1;
            wmParamsBottom.y = offset*-1;
            if (type == 1) {
                wmParamsBottom.y = 0;
                wmParamsTop.width = p.x;
                wmParamsTop1.width = p.x;
                wmParamsBottom.width = p.x;
            }
            LayoutInflater inflater = LayoutInflater.from(context);
            //获取浮动窗口视图所在布局
            mFloatLayoutBottom = (FrameLayout) inflater.inflate(R.layout.float_roundlayout_bottom, null);
            mFloatLayoutTop = (FrameLayout) inflater.inflate(R.layout.float_roundlayout_top, null);
            mFloatLayoutTop1 = (FrameLayout) inflater.inflate(R.layout.float_roundlayout_top, null);
            infoFloatLayout = (FrameLayout) inflater.inflate(R.layout.float_actinfo, null);
            infoFloatLayout.setVisibility(View.GONE);
            imeFloatLayout = (FrameLayout)mFloatLayoutBottom.findViewById(R.id.float_ime_fl);
            imeFloatLayout.setLayoutParams(new FrameLayout.LayoutParams(p.x,dhheight+2,Gravity.BOTTOM));
//            imeFloatLayout.setLayoutParams(new FrameLayout.LayoutParams(p.x,(int)(dhheight*0.72),Gravity.BOTTOM));
            imeFloatLayout.setBackgroundColor(Color.TRANSPARENT);
            imeFloatLayout.setVisibility(View.GONE);
            actInfoLL = (LinearLayout) infoFloatLayout.findViewById(R.id.float_show_act_info_ll);
            actInfoTV = (TextView) infoFloatLayout.findViewById(R.id.float_show_act_info_tv);
            actInfoShouSuo = (TextView) infoFloatLayout.findViewById(R.id.float_show_act_info_btn_ss);
            actInfoQingKong = (TextView) infoFloatLayout.findViewById(R.id.float_show_act_info_btn_qk);
            actInfoGuanBi = (TextView) infoFloatLayout.findViewById(R.id.float_show_act_info_btn_gb);
            actInfoTV.setText("长按复制");
            ImageView ltImg = (ImageView)mFloatLayoutTop.findViewById(R.id.round_lt_img);
            ImageView ltImg1 = (ImageView)mFloatLayoutTop1.findViewById(R.id.round_lt_img);
            ltImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.LEFT));
            ltImg1.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.LEFT));
            ImageView rtImg = (ImageView)mFloatLayoutTop.findViewById(R.id.round_rt_img);
            ImageView rtImg1= (ImageView)mFloatLayoutTop1.findViewById(R.id.round_rt_img);
            rtImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.RIGHT));
            rtImg1.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.RIGHT));
            ImageView rbImg = (ImageView)mFloatLayoutBottom.findViewById(R.id.round_rb_img);
            rbImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.RIGHT|Gravity.BOTTOM));
            ImageView lbImg = (ImageView)mFloatLayoutBottom.findViewById(R.id.round_lb_img);
            lbImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.LEFT|Gravity.BOTTOM));
            //添加mFloatLayout
            try {
                mWindowManager.addView(mFloatLayoutTop, wmParamsTop);
                mWindowManager.addView(mFloatLayoutTop1, wmParamsTop1);
                mWindowManager.addView(mFloatLayoutBottom, wmParamsBottom);
                isShow  =true;
                if(WatchDogService.isShowActInfo){
                    infoFloatIsShow = true;
                    mWindowManager.addView(infoFloatLayout,wmParamsInfo);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
            actInfoQingKong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actInfoTV.setText("");
                    actInfoSB.delete(0,actInfoSB.length());
                }
            });
            actInfoShouSuo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actInfoShouSuo.setText(actInfoTV.isShown()?"显示":"隐藏");
                    actInfoQingKong.setVisibility(actInfoTV.isShown()?View.GONE:View.VISIBLE);
                    actInfoGuanBi.setVisibility(actInfoTV.isShown()?View.GONE:View.VISIBLE);
                    actInfoTV.setVisibility(actInfoTV.isShown()?View.GONE:View.VISIBLE);

                }
            });
            actInfoGuanBi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    infoFloatIsShow = false;
                    mWindowManager.removeView(infoFloatLayout);
                    actInfoTV.setText("");
                    actInfoSB.delete(0,actInfoSB.length());
                    WatchDogService.isShowActInfo = false;
                }
            });
            infoFloatLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ClipboardManager cm = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
                    // 将文本内容放到系统剪贴板里。
                    cm.setText(actInfoSB.toString());
                    Toast.makeText(context,"已复制到粘贴板",Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
//            infoFloatLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    actInfoTV.removeCallbacks(r);
//                    if(System.currentTimeMillis()-clickTime<300){
//                        actInfoTV.setText("");
//                        actInfoSB.delete(0,actInfoSB.length());
//                        infoFloatLayout.setVisibility(View.GONE);
//                    }else{
//                        actInfoTV.postDelayed(r,400);
//                    }
//                    clickTime = System.currentTimeMillis();
//                }
//            });
//        }else{
//            this.stopSelf();
//        }
    }

//    Runnable r = new Runnable() {
//        @Override
//        public void run() {
//            ClipboardManager cm = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
//            // 将文本内容放到系统剪贴板里。
//            cm.setText(actInfoSB.toString());
//            Toast.makeText(context,"已复制到粘贴板",Toast.LENGTH_SHORT).show();
//        }
//    };


    public void destroy() {
        try{
            if(isAllReadyInit&&WatchDogService.isRoundCorOpen&&isShow){
                addOrRemoveCor(false);
                if(infoFloatIsShow){
                    mWindowManager.removeView(infoFloatLayout);
                }
            }
            screenLightServiceUtil.remove();
            context.unregisterReceiver(rec);
//            isRoundRun = false;
        }catch (Throwable e){
            e.printStackTrace();
        }
    }


//    public static int getDaoHangHeight(Context context) {
//        int result = 0;
//        int resourceId=0;
//        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
//        if (rid!=0){
//            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
////            CMLog.show("高度："+resourceId);
////            CMLog.show("高度："+context.getResources().getDimensionPixelSize(resourceId) +"");
//            return context.getResources().getDimensionPixelSize(resourceId);
//        }else
//            return 0;
//    }
    public static int getZhuangTaiHeight(Context context){
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
    public static int getVirtualBarHeigh(Context context) {
        int vh = 0;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("rawtypes")
            Class c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            vh = dm.heightPixels - windowManager.getDefaultDisplay().getHeight();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return vh;
    }

    private void addOrRemoveCor(boolean isAdd){
        try {
            if(WatchDogService.isRoundCorOpen&&
                    (WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)&&
                    isAdd){
                if(!isShow){
                    if(mFloatLayoutBottom!=null){
                        wmParamsBottom.type = floatLeve;
//                        wmParamsTop.type = floatLeve;
                        wmParamsTop1.type = floatLeve;
                        mWindowManager.addView(mFloatLayoutBottom, wmParamsBottom);
                        mWindowManager.addView(mFloatLayoutTop, wmParamsTop);
                        mWindowManager.addView(mFloatLayoutTop1, wmParamsTop1);
                    }else {
                        createFloatView(0);
                    }
                    isShow = true;
                }
            }else if(!isAdd){
                if(isShow){
                    if(mFloatLayoutTop!=null){
                        mWindowManager.removeViewImmediate(mFloatLayoutTop);
                        mWindowManager.removeViewImmediate(mFloatLayoutTop1);
                        mWindowManager.removeViewImmediate(mFloatLayoutBottom);
                    }
                    isShow = false;
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
    StringBuilder actInfoSB = new StringBuilder();
    class  MyReciver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            try{
                if(!WatchDogService.isUICONTROLOPEN){
                    return;
                }
                if(intent.getAction().equals("com.click369.control.openinstall")){
                    addOrRemoveCor(false);
//                    if(isShow){
//                        mWindowManager.removeView(mFloatLayoutBottom);
//                        mWindowManager.removeView(mFloatLayoutTop);
//                        mWindowManager.removeView(mFloatLayoutTop1);
//                        isShow = false;
//                    }
                }else if(intent.getAction().equals("com.click369.control.closeinstall")){
                    addOrRemoveCor(true);
//                    if(!isShow){
//                        mWindowManager.addView(mFloatLayoutBottom, wmParamsBottom);
//                        mWindowManager.addView(mFloatLayoutTop, wmParamsTop);
//                        mWindowManager.addView(mFloatLayoutTop1, wmParamsTop1);
//                        isShow = true;
//                    }
                }else if(intent.getAction().equals("com.click369.control.restartcor")){
                    addOrRemoveCor(false);
                    addOrRemoveCor(true);
//                    if(isShow){
//                        mWindowManager.removeView(mFloatLayoutTop);
//                        mWindowManager.removeView(mFloatLayoutTop1);
//                        mWindowManager.removeView(mFloatLayoutBottom);
//                        mWindowManager.addView(mFloatLayoutBottom, wmParamsBottom);
//                        mWindowManager.addView(mFloatLayoutTop, wmParamsTop);
//                        mWindowManager.addView(mFloatLayoutTop1, wmParamsTop1);
//                    }
                }else if(intent.getAction().equals("com.click369.control.imeopen")){
                    Log.i("CONTROL","imeopen");
                    if(WatchDogService.isRoundCorOpen&&isAllReadyInit){
                        isShowKeyBar = barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR,false);
                        if(isShow&&offset+10>=dhheight&&isShowKeyBar){
                            imeFloatLayout.setBackgroundColor(Color.WHITE);
                            if(intent.hasExtra("color")){
                                imeFloatLayout.setBackgroundColor(intent.getIntExtra("color",Color.BLACK));
                            }
                            imeFloatLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }else if(intent.getAction().equals("com.click369.control.imeclose")){
                    Log.i("CONTROL","imeclose");
                    if(WatchDogService.isRoundCorOpen&&isAllReadyInit&&isShow){
                        hander.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    imeFloatLayout.setBackgroundColor(Color.TRANSPARENT);
                                    imeFloatLayout.setVisibility(View.GONE);
                                }catch (Throwable e){
                                    e.printStackTrace();
                                }
                            }
                        },200);
                    }
                }else if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)||
                        intent.getAction().equals("com.click369.control.corchangeposition")){
                    if(isShow&&(WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)){
                        Configuration config = context.getResources().getConfiguration();
                        // 如果当前是横屏
                        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE){
                            Log.i("wall","横屏");
                            mWindowManager.removeView(mFloatLayoutBottom);
                            mWindowManager.removeView(mFloatLayoutTop);
                            mWindowManager.removeView(mFloatLayoutTop1);
                            createFloatView(1);
                        }else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
                            mWindowManager.removeView(mFloatLayoutBottom);
                            mWindowManager.removeView(mFloatLayoutTop);
                            mWindowManager.removeView(mFloatLayoutTop1);
                            createFloatView(0);
                            Log.i("wall","竖屏");
                        }
                    }
                    if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)){
                        screenLightServiceUtil.changDir();
//                        screenLightServiceUtil.init();
//                        screenLightServiceUtil.showLight(LightView.LIGHT_TYPE_TEST);
                    }
                }else if(intent.getAction().equals("com.click369.control.float.infouishow")){
                    boolean isShowInfo = intent.getBooleanExtra("isShow",false);
                    if(isShowInfo){
    //                    actInfoLL.setVisibility(View.VISIBLE);
                        if(infoFloatIsShow){
                            return;
                        }
                        infoFloatIsShow = true;
                        mWindowManager.addView(infoFloatLayout,wmParamsInfo);
                    }else{
    //                    infoFloatLayout.setVisibility(View.GONE);
                        if(!infoFloatIsShow){
                            return;
                        }
                        infoFloatIsShow = false;
                        mWindowManager.removeView(infoFloatLayout);
                        actInfoTV.setText("");
                        actInfoSB.delete(0,actInfoSB.length());
                    }
                }else if(intent.getAction().equals("com.click369.control.float.actinfo")){
                    String info = intent.getStringExtra("data");
                    if(actInfoSB.indexOf(info)!=0){
                        if (actInfoSB.length()>0){
                            if(actInfoSB.length()>600){
                                actInfoSB.delete(0,actInfoSB.length());
                            }
                            actInfoSB.insert(0,info+"\r\n");

                        }else{
                            infoFloatLayout.setVisibility(View.VISIBLE);
                            actInfoSB.insert(0,info);
                        }
                        actInfoTV.setText(actInfoSB.toString());
                    }
                }else if(intent.getAction().equals("com.click369.control.light.show")){
                    int type = intent.getIntExtra("type",0);
                    screenLightServiceUtil.showLight(type);
                }else if(intent.getAction().equals("com.click369.control.light.hide")){
                    screenLightServiceUtil.hideLight();
                }else if(intent.getAction().equals("com.click369.control.light.changeposition")){
                    screenLightServiceUtil.remove();
                    screenLightServiceUtil.init();
                    screenLightServiceUtil.showLight(LightView.LIGHT_TYPE_TEST);
                }else if(intent.getAction().equals("com.click369.control.float.checkxp")){
                    hander.removeCallbacks(checkRunable);
                    if(intent.hasExtra("isfloatok")){
                        WatchDogService.isHasXPFloatVewPermission = intent.getBooleanExtra("isfloatok",false);
                    }
                   WatchDogService.isHasSysFloatVewPermission = (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M&&Settings.canDrawOverlays(context))||Build.VERSION.SDK_INT <Build.VERSION_CODES.M;
                   if((!WatchDogService.isLightCharge&&
                           !WatchDogService.isLightScOn&&
                           !WatchDogService.isLightMusic&&
                           !WatchDogService.isLightMsg&&
                           !WatchDogService.isLightCall&&
                           !WatchDogService.isRoundCorOpen)){
                       if(isAllReadyInit){
//                           pv.remove();
                           addOrRemoveCor(false);
                       }
                        return;
                   }
                    if(WatchDogService.isHasXPFloatVewPermission){
                       floatLeve = 2015;
                   }else if(!WatchDogService.isHasXPFloatVewPermission&&WatchDogService.isHasSysFloatVewPermission){
                       floatLeve = 2003;
                   }else if(!WatchDogService.isHasXPFloatVewPermission&&!WatchDogService.isHasSysFloatVewPermission){
                       WatchDogService.isNeedGetFloatPremission = true;
                       Toast.makeText(context,"圆角或边缘呼吸效果需要浮动权限，请重启手机或打开界面控制进行设置",Toast.LENGTH_LONG).show();
                   }
                    Log.i("CONTROL","checkxp float view  isHasXPFloat "+WatchDogService.isHasXPFloatVewPermission+" isHasSysFloat "+WatchDogService.isHasSysFloatVewPermission);
                   if(WatchDogService.isHasSysFloatVewPermission||
                           WatchDogService.isHasXPFloatVewPermission){
                       if (!isAllReadyInit){
                           init();
                       }else{
                           if(WatchDogService.isRoundCorOpen){
                               addOrRemoveCor(false);
                               hander.postDelayed(new Runnable() {
                                   @Override
                                   public void run() {
                                       addOrRemoveCor(true);
                                   }
                               },300);
                           }else{
                               addOrRemoveCor(false);
                           }
                           screenLightServiceUtil.remove();
                       }
                   }else{
                       if (isAllReadyInit){
                           if(WatchDogService.isRoundCorOpen){
                               addOrRemoveCor(false);
                           }
                           screenLightServiceUtil.remove();
                       }
                   }
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }



}

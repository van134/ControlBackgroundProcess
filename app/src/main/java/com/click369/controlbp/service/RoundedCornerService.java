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

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.lang.reflect.Method;


/**
 * Created by asus on 2017/5/27.
 */
public class RoundedCornerService extends Service {
    public static boolean isRoundRun = false;
    boolean isShow = true,isShowKeyBar;
    //定义浮动窗口布局
    FrameLayout mFloatLayoutTop,mFloatLayoutBottom,imeFloatLayout,infoFloatLayout;
    LinearLayout actInfoLL;
    TextView actInfoTV;
    WindowManager.LayoutParams wmParamsBottom,wmParamsTop,wmParamsInfo;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;
    Handler hander = new Handler();
    MyReciver rec;
    SharedPreferences barPrefs;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        barPrefs = SharedPrefsUtil.getPreferences(this, Common.PREFS_UIBARLIST);
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
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        this.registerReceiver(rec,filter);
        Configuration config = getResources().getConfiguration();
        // 如果当前是横屏
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE){
            Log.i("wall","横屏");
            createFloatView(1);
        }else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
            createFloatView(0);
            Log.i("wall","竖屏");
        }

    }
    int dhheight = 0,statusheight = 0;
    int offset = 0;
    int roundSize = 35;
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void createFloatView(int type)//0竖 1横
    {
        if ((Build.VERSION.SDK_INT >=Build.VERSION_CODES.M&& Settings.canDrawOverlays(this))||Build.VERSION.SDK_INT <Build.VERSION_CODES.M) {
            isRoundRun = true;
            wmParamsBottom = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    2003,
                    536, -3);
            wmParamsTop = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    2003,
                    536, -3);
            wmParamsInfo = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
            //设置window type
            wmParamsInfo.type = WindowManager.LayoutParams.TYPE_PHONE;
            //设置图片格式，效果为背景透明
            wmParamsInfo.format = PixelFormat.RGBA_8888;
            //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
            wmParamsInfo.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //调整悬浮窗显示的停靠位置为左侧置顶
            wmParamsInfo.gravity = Gravity.LEFT | Gravity.TOP;
            wmParamsInfo.height = WindowManager.LayoutParams.WRAP_CONTENT;//height-dhheight+offset;
            wmParamsInfo.height = WindowManager.LayoutParams.WRAP_CONTENT;
            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            wmParamsInfo.x = 0;
            wmParamsInfo.y = 0;
//                2003, 536, -3);
            //获取的是WindowManagerImpl.CompatModeWrapper
//            WindowManager.LayoutParams.TYPE_PHONE
            mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
            //设置图片格式，效果为背景透明
            wmParamsBottom.format = PixelFormat.RGBA_8888;
            wmParamsTop.format = PixelFormat.RGBA_8888;
            //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            //调整悬浮窗显示的停靠位置为左侧置顶
            wmParamsBottom.gravity = Gravity.LEFT | Gravity.BOTTOM;
            wmParamsTop.gravity = Gravity.LEFT | Gravity.TOP;
//        wmParams.gravity = 53;//Gravity.LEFT | Gravity.BOTTOM;
            Point p = new Point();
            mWindowManager.getDefaultDisplay().getRealSize(p);
            int height = p.y;
            //设置悬浮窗口长宽数据
            wmParamsBottom.width = WindowManager.LayoutParams.MATCH_PARENT;
            wmParamsTop.width = WindowManager.LayoutParams.MATCH_PARENT;
            dhheight = getVirtualBarHeigh(this);
            statusheight = getZhuangTaiHeight(this);
            int topoffset = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDTOPOFFSET,statusheight);
            offset = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDOFFSET,dhheight);
            roundSize = barPrefs.getInt(Common.PREFS_SETTING_UI_ROUNDSIZE,35);
            wmParamsTop.height = WindowManager.LayoutParams.WRAP_CONTENT;//height-dhheight+offset;
            wmParamsBottom.height = WindowManager.LayoutParams.WRAP_CONTENT;//height-dhheight+offset;

//            if(setting.getBoolean(Common.PREFS_SETTING_UI_ROUNDALLOPEN,false)){
//                wmParams.height = height;
//            }
            // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
            wmParamsTop.x = 0;
            wmParamsBottom.x = 0;
            wmParamsTop.y = topoffset * -1;
            wmParamsBottom.y = offset*-1;
            if (type == 1) {
                wmParamsBottom.y = 0;
                wmParamsTop.width = p.x;
                wmParamsBottom.width = p.x;
//                wmParamsTop.height = p.y;
//                wmParamsBottom.height = p.y;
            }
            LayoutInflater inflater = LayoutInflater.from(getApplication());
            //获取浮动窗口视图所在布局
            mFloatLayoutBottom = (FrameLayout) inflater.inflate(R.layout.float_roundlayout_bottom, null);
            mFloatLayoutTop = (FrameLayout) inflater.inflate(R.layout.float_roundlayout_top, null);
            infoFloatLayout = (FrameLayout) inflater.inflate(R.layout.float_actinfo, null);
            infoFloatLayout.setVisibility(View.GONE);
            imeFloatLayout = (FrameLayout)mFloatLayoutBottom.findViewById(R.id.float_ime_fl);
            imeFloatLayout.setLayoutParams(new FrameLayout.LayoutParams(p.x,dhheight+2,Gravity.BOTTOM));
//            imeFloatLayout.setLayoutParams(new FrameLayout.LayoutParams(p.x,(int)(dhheight*0.72),Gravity.BOTTOM));
            imeFloatLayout.setBackgroundColor(Color.TRANSPARENT);
            imeFloatLayout.setVisibility(View.GONE);
            actInfoLL = (LinearLayout) infoFloatLayout.findViewById(R.id.float_show_act_info_ll);
            actInfoTV = (TextView) infoFloatLayout.findViewById(R.id.float_show_act_info_tv);
            ImageView ltImg = (ImageView)mFloatLayoutTop.findViewById(R.id.round_lt_img);
            ltImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.LEFT));
            ImageView rtImg = (ImageView)mFloatLayoutTop.findViewById(R.id.round_rt_img);
            rtImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.RIGHT));
            ImageView rbImg = (ImageView)mFloatLayoutBottom.findViewById(R.id.round_rb_img);
            rbImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.RIGHT|Gravity.BOTTOM));
            ImageView lbImg = (ImageView)mFloatLayoutBottom.findViewById(R.id.round_lb_img);
            lbImg.setLayoutParams(new FrameLayout.LayoutParams(roundSize,roundSize,Gravity.LEFT|Gravity.BOTTOM));
            //添加mFloatLayout
            mWindowManager.addView(mFloatLayoutTop, wmParamsTop);
            mWindowManager.addView(mFloatLayoutBottom, wmParamsBottom);
            mWindowManager.addView(infoFloatLayout,wmParamsInfo);
            infoFloatLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    actInfoTV.setText("");
                    actInfoSB.delete(0,actInfoSB.length());
                    infoFloatLayout.setVisibility(View.GONE);
                    return true;
                }
            });
            infoFloatLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager cm = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    // 将文本内容放到系统剪贴板里。
                    cm.setText(actInfoSB.toString());
                }
            });
//            infoFloatLayout.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View view, MotionEvent motionEvent) {
//                    switch (motionEvent.getAction()){
//                        case MotionEvent.ACTION_UP:
//                            actInfoTV.setText("");
//                            actInfoSB.delete(0,actInfoSB.length());
//                            break;
//                    }
//                    return false;
//                }
//            });
        }else{
            this.stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        mWindowManager.removeView(mFloatLayoutBottom);
        mWindowManager.removeView(mFloatLayoutTop);
        this.unregisterReceiver(rec);
        isRoundRun = false;
        super.onDestroy();
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
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
    StringBuilder actInfoSB = new StringBuilder();
    class  MyReciver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.click369.control.openinstall")){
                if(isShow){
                    mWindowManager.removeView(mFloatLayoutBottom);
                    mWindowManager.removeView(mFloatLayoutTop);
                    isShow = false;
                }
            }else if(intent.getAction().equals("com.click369.control.closeinstall")){
                if(!isShow){
                    mWindowManager.addView(mFloatLayoutBottom, wmParamsBottom);
                    mWindowManager.addView(mFloatLayoutTop, wmParamsTop);
                    isShow = true;
                }
            }else if(intent.getAction().equals("com.click369.control.restartcor")){
                if(isShow){
                    mWindowManager.removeView(mFloatLayoutTop);
                    mWindowManager.removeView(mFloatLayoutBottom);
                    mWindowManager.addView(mFloatLayoutBottom, wmParamsBottom);
                    mWindowManager.addView(mFloatLayoutTop, wmParamsTop);
                }
            }else if(intent.getAction().equals("com.click369.control.imeopen")){
                Log.i("CONTROL","imeopen");
                isShowKeyBar = barPrefs.getBoolean(Common.PREFS_SETTING_UI_BOTTOMBAR,false);
                if(isShow&&offset+10>=dhheight&&isShowKeyBar){
                    imeFloatLayout.setBackgroundColor(Color.WHITE);
                    if(intent.hasExtra("color")){
                        imeFloatLayout.setBackgroundColor(intent.getIntExtra("color",Color.BLACK));
                    }
                    imeFloatLayout.setVisibility(View.VISIBLE);
                }
            }else if(intent.getAction().equals("com.click369.control.imeclose")){
                Log.i("CONTROL","imeclose");
                if(isShow){
                    hander.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imeFloatLayout.setBackgroundColor(Color.TRANSPARENT);
                            imeFloatLayout.setVisibility(View.GONE);
                        }
                    },200);
                }
            }else if(intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)||
                    intent.getAction().equals("com.click369.control.corchangeposition")){
                if(isShow){
                    Configuration config = getResources().getConfiguration();
                    // 如果当前是横屏
                    if (config.orientation == Configuration.ORIENTATION_LANDSCAPE){
                        Log.i("wall","横屏");
                        mWindowManager.removeView(mFloatLayoutBottom);
                        mWindowManager.removeView(mFloatLayoutTop);
                        createFloatView(1);
                    }else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
                        mWindowManager.removeView(mFloatLayoutBottom);
                        mWindowManager.removeView(mFloatLayoutTop);
                        createFloatView(0);
                        Log.i("wall","竖屏");
                    }
                }
            }else if(intent.getAction().equals("com.click369.control.float.infouishow")){
                boolean isShowInfo = intent.getBooleanExtra("isShow",false);
                if(isShowInfo){
//                    actInfoLL.setVisibility(View.VISIBLE);
                }else{
                    infoFloatLayout.setVisibility(View.GONE);
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
            }
        }
    }

}

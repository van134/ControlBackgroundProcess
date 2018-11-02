package com.click369.controlbp.service;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;


import com.click369.controlbp.R;
import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.FileUtil;

import java.io.File;
import java.util.HashMap;

/**
 * Created by asus on 2017/5/27.
 */
public class ColorNavBarService extends Service {
//    public static final String  ISCOLORBAROPEN = "iscolorbaropen";
    public static HashMap<String,String> appColors = new HashMap<String,String>();
    public static boolean isNavColorRun = false;
    //定义浮动窗口布局
    FrameLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;
    //创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;
//    Handler hander = new Handler();
    MyReciver rec;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ColorNavBarService.this.startActivity(intent);
            Toast.makeText(this,"请赋予本应用显示在其他窗口上面的权限",Toast.LENGTH_LONG).show();
            stopSelf();
            return;
        }
        wmParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);
        createFloatView();
        rec = new MyReciver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction("com.click369.control.navcolor.change");
        filter.addAction("com.click369.control.openinstall");
        filter.addAction("com.click369.control.closeinstall");
        isNavColorRun = true;
        FileUtil.init();
        File file = new File(FileUtil.FILEPATH,"navcolor");
        if(file.exists()){
            Object o = FileUtil.readObj(file.getAbsolutePath());
            if(o!=null){
                appColors.clear();
                appColors.putAll((HashMap<String,String>)o);
            }
        }
//        appColors.put("com.click369.dozex","ff000000");
        this.registerReceiver(rec,filter);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void createFloatView()
    {
        wmParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                2003,536, -3);
//                2003, 536, -3);
        //获取的是WindowManagerImpl.CompatModeWrapper
        mWindowManager = (WindowManager)getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        //设置图片格式，效果为背景透明
        wmParams.format = PixelFormat.RGBA_8888;
        //设置浮动窗口不可聚焦（实现操作除浮动窗口外的其他可见窗口的操作）
//        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        //调整悬浮窗显示的停靠位置为左侧置顶
        wmParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
//        wmParams.gravity = 53;//Gravity.LEFT | Gravity.BOTTOM;
        Point p = new Point();
        mWindowManager.getDefaultDisplay().getRealSize(p);
        //设置悬浮窗口长宽数据
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = getDaoHangHeight(this);
        // 以屏幕左上角为原点，设置x、y初始值，相对于gravity
        wmParams.x = 0;
        wmParams.y = getDaoHangHeight(this)*-1;// p.y - getDaoHangHeight(this);
         /*// 设置悬浮窗口长宽数据
        wmParams.width = 200;
        wmParams.height = 80;*/
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局
        mFloatLayout = (FrameLayout) inflater.inflate(R.layout.float_layout, null);
        SharedPreferences settings = this.getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);

        if(!settings.getBoolean(Common.ISCOLORBAROPEN,false)){
            isShow = false;
        }else{
            isShow = true;
            mWindowManager.addView(mFloatLayout, wmParams);
            sendBroad();
        }
    }

    private void sendBroad(){
        Intent intent = new Intent("com.click369.dozex.restartcor");
        sendBroadcast(intent);
    }

    public static int getZhuangTaiHeight(Context context){
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }else{
            statusBarHeight = 200;
        }
        Log.i("DOZEX","statusBarHeight "+statusBarHeight);
        return statusBarHeight;
    }
    public static int getDaoHangHeight(Context context){
        int resourceId=0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid!=0){
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            Log.i("DOZEX","getDaoHangHeight "+context.getResources().getDimensionPixelSize(resourceId));
            return context.getResources().getDimensionPixelSize(resourceId);
        }else
            return 0;
    }

    boolean isShow = false;
    class  MyReciver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("com.click369.doze.openinstall")){
                if(isShow){
                    mWindowManager.removeView(mFloatLayout);
                    isShow = false;
                }
            }else if(intent.getAction().equals("com.click369.doze.closeinstall")){
                if(!isShow){
                    mWindowManager.addView(mFloatLayout, wmParams);
                    sendBroad();
                    isShow = true;
                }
            }else if(intent.getAction().equals("com.click369.control.navcolor.change")){
                String pkg = intent.getStringExtra("data").trim();
                Log.i("DOZEX","修改 "+pkg+ "  "+appColors.get(pkg));
                if(pkg.length()==0){
                    mFloatLayout.setAlpha(0);
                }else{
                    mFloatLayout.setAlpha(1);
                    if (pkg.equals(Common.PACKAGENAME)){
                        if(MainActivity.isNightMode){
                            mFloatLayout.setBackgroundColor(Color.BLACK);
                        }else{
                            mFloatLayout.setBackgroundColor(Color.WHITE);
                        }
                        return;
                    }
                    if(appColors.containsKey(pkg)&&appColors.get(pkg)!=null){
                        mFloatLayout.setBackgroundColor(Color.parseColor("#"+appColors.get(pkg)));
                    }else{
                        mFloatLayout.setBackgroundColor(Color.parseColor("#ffffffff"));
                    }
                }
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if(isShow){
            mWindowManager.removeView(mFloatLayout);
            isShow = false;
        }
        isNavColorRun = false;
        if(rec!=null){
            this.unregisterReceiver(rec);
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }
}

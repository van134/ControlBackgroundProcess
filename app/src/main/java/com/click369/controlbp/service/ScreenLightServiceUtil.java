package com.click369.controlbp.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.click369.controlbp.R;
import com.click369.controlbp.fragment.ControlFragment;

import java.lang.reflect.Method;

/**
 * Created by 41856 on 2018/12/19.
 */

public class ScreenLightServiceUtil {
    private RoundedCornerService service;
    private WindowManager windowManager;
    private WindowManager.LayoutParams wmParams;
    private FrameLayout fl;
    private boolean isInit =false;
    public ScreenLightServiceUtil(RoundedCornerService service,WindowManager windowManager){
        this.service = service;
        this.windowManager = windowManager;
    }

    public void init(){
        try {
            if(!WatchDogService.isHasXPFloatVewPermission&&
                    !WatchDogService.isHasSysFloatVewPermission){
                return;
            }
            isInit = true;
           wmParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    RoundedCornerService.floatLeve,
                    536, -3);
            wmParams.format = PixelFormat.RGBA_8888;
            wmParams.gravity = Gravity.BOTTOM;
            wmParams.setTitle("控制器");
            wmParams.x = 0;
            wmParams.width = service.context.getResources().getDisplayMetrics().widthPixels;
            wmParams.height = getHasVirtualKey(windowManager);
            wmParams.y = isNavigationBarShow()?-1*RoundedCornerService.getVirtualBarHeigh(service.context):0;
//            wmParams.y = -1 * RoundedCornerService.getZhuangTaiHeight(service.context);//-1*RoundedCornerService.getZhuangTaiHeight(service)+WatchDogService.lightOffset;
            LayoutInflater inflater = LayoutInflater.from(service.context);
            //获取浮动窗口视图所在布局
            fl = (FrameLayout) inflater.inflate(R.layout.float_lightlayout, null);
            lv = (LightView) fl.findViewById(R.id.float_light_fl);
            lv.setWidowManager(windowManager,wmParams,fl);
//            windowManager.addView(fl,wmParams);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void remove(){
        try {
            if(isInit&&windowManager!=null&&LightView.isStart){
                if(lv!=null) {
                    lv.stopBl();
                }
                windowManager.removeView(fl);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
    LightView lv =null;
    public void showLight(final int type){
        if(isInit&&(WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)&&lv!=null){
            int w = service.context.getResources().getDisplayMetrics().widthPixels;
            int bar  =RoundedCornerService.getVirtualBarHeigh(service.context);
            if(w!=wmParams.width){
                wmParams.width = w;
                wmParams.height = getHasVirtualKey(windowManager);
                lv.setWidowManager(windowManager,wmParams,fl);
            }
//            if(bar*-1!=wmParams.y){
                wmParams.y = isNavigationBarShow()?-1*RoundedCornerService.getVirtualBarHeigh(service.context):0;
                lv.setWidowManager(windowManager,wmParams,fl);
//            }
            lv.startBl(type);
        }
    }

    public void changDir(){
        if(isInit){
            wmParams.width = service.context.getResources().getDisplayMetrics().widthPixels;
            wmParams.height = getHasVirtualKey(windowManager);
            if((WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)&&lv!=null) {
                lv.setWidowManager(windowManager,wmParams,fl);
                lv.setVisibility(View.GONE);
                if(lv.isStart){
                    lv.isNeedTest = true;
                }
            }
        }

    }
    public void hideLight(){
        if(isInit&&(WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)&&lv!=null) {
//            windowManager.removeViewImmediate(fl);
            lv.stopBl();
        }
    }

//    private boolean isHashVirtualKey(){
//        return false;
//    }

    public static int getHasVirtualKey(WindowManager windowManager) {
        int dpi = 0;
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        @SuppressWarnings("rawtypes")
        Class c;
        try {
            c = Class.forName("android.view.Display");
            @SuppressWarnings("unchecked")
            Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
            dpi = dm.heightPixels;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dpi;
    }

    public static void sendShowLight(int type, Context context){
        if((!WatchDogService.isLightCall&&type==LightView.LIGHT_TYPE_CALL)||
                (!WatchDogService.isLightScOn&&type==LightView.LIGHT_TYPE_SCON)||
                (!WatchDogService.isLightMusic&&type==LightView.LIGHT_TYPE_MUSIC)||
                (!WatchDogService.isLightMsg&&type==LightView.LIGHT_TYPE_MSG)&type!=LightView.LIGHT_TYPE_TEST){
            return;
        }
        Intent intent = new Intent("com.click369.control.light.show");
        intent.putExtra("type",type);
        context.sendBroadcast(intent);
    }
    public static void sendHideLight(Context context){
        if(!LightView.isStart){
            return;
        }
        Intent intent = new Intent("com.click369.control.light.hide");
        context.sendBroadcast(intent);
    }
    public static void sendReloadLight(Context context){
        Intent intent = new Intent("com.click369.control.light.changeposition");
        context.sendBroadcast(intent);
    }















    public boolean isNavigationBarShow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = windowManager.getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y!=size.y;
        }else {
            boolean menu = ViewConfiguration.get(service.context).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if(menu || back) {
                return false;
            }else {
                return true;
            }
        }
    }
}

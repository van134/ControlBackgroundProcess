package com.click369.controlbp.service;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.click369.controlbp.R;

import java.lang.reflect.Method;

/**
 * Created by 41856 on 2018/12/19.
 */

public class ScreenLightServiceUtilNew {
    private RoundedCornerService service;
    private WindowManager windowManager;
    private WindowManager.LayoutParams wmParams;//Left,wmParamsTop,wmParamsRight,wmParamsBottom;
    private FrameLayout flleft,fltop,flright,flbottom;
    private boolean isInit =false;
    public ScreenLightServiceUtilNew(RoundedCornerService service, WindowManager windowManager){
        this.service = service;
        this.windowManager = windowManager;
    }

    public void initparms(){
        int h = getHasVirtualKey();
        wmParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                RoundedCornerService.floatLeve,
                536, -3);
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.gravity = Gravity.BOTTOM;
        wmParams.x = 0;
        wmParams.width = service.context.getResources().getDisplayMetrics().widthPixels;
        wmParams.height = getHasVirtualKey();
        lvleft.setWidowManager(windowManager,wmParams,flleft,0);
        lvtop.setWidowManager(windowManager,wmParams,fltop,1);
        lvright.setWidowManager(windowManager,wmParams,flright,2);
        lvbottom.setWidowManager(windowManager,wmParams,flbottom,3);
    }

    public void init(){
        try {
            if(!WatchDogService.isHasXPFloatVewPermission&&
                    !WatchDogService.isHasSysFloatVewPermission){
                return;
            }
            isInit = true;

            LayoutInflater inflater = LayoutInflater.from(service.context);
            //获取浮动窗口视图所在布局
            flleft = (FrameLayout) inflater.inflate(R.layout.float_lightlayout_new, null);
            fltop = (FrameLayout) inflater.inflate(R.layout.float_lightlayout_new, null);
            flright = (FrameLayout) inflater.inflate(R.layout.float_lightlayout_new, null);
            flbottom = (FrameLayout) inflater.inflate(R.layout.float_lightlayout_new, null);
            lvleft = (LightViewNew) flleft.findViewById(R.id.float_light_fl);
            lvtop = (LightViewNew) fltop.findViewById(R.id.float_light_fl);
            lvright = (LightViewNew) flright.findViewById(R.id.float_light_fl);
            lvbottom = (LightViewNew) flbottom.findViewById(R.id.float_light_fl);
            initparms();

        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void remove(){
        try {
            if(isInit&&
                    (WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)&&
                    windowManager!=null){
                windowManager.removeView(flleft);
                windowManager.removeView(fltop);
                windowManager.removeView(flright);
                windowManager.removeView(flbottom);
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
    LightViewNew lvleft,lvtop,lvright,lvbottom;
    public void showLight(final int type){
        if(isInit&&(WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)){
//            windowManager.addView(flleft,wmParamsLeft);
//            windowManager.addView(flright,wmParamsRight);
//            windowManager.addView(fltop,wmParamsTop);
//            windowManager.addView(flbottom,wmParamsBottom);
//            lv.checkFullScreen(service.pv);
            lvleft.startBl(type);
            lvtop.startBl(type);
            lvright.startBl(type);
            lvbottom.startBl(type);
        }
    }

    public void changDir(){
        if(isInit&&(WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)) {
            initparms();
            if(lvleft.isStart){
                lvleft.isNeedTest = true;
                lvright.isNeedTest = true;
                lvtop.isNeedTest = true;
                lvbottom.isNeedTest = true;
            }
        }
    }
    public void hideLight(){
        if(isInit&&(WatchDogService.isHasSysFloatVewPermission||WatchDogService.isHasXPFloatVewPermission)) {
//            windowManager.removeViewImmediate(flleft);
//            windowManager.removeViewImmediate(fltop);
//            windowManager.removeViewImmediate(flright);
//            windowManager.removeViewImmediate(flbottom);
            lvleft.stopBl();
            lvtop.stopBl();
            lvright.stopBl();
            lvbottom.stopBl();
        }
    }

//    private boolean isHashVirtualKey(){
//        return false;
//    }

    private int getHasVirtualKey() {
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
}

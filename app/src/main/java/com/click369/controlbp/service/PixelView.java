package com.click369.controlbp.service;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

/**
 * Created by 41856 on 2018/12/21.
 */

public class PixelView extends View{
    private WindowManager widowManager;
    private WindowManager.LayoutParams wmParams;
    public PixelView(Context context) {
        super(context);
    }
    private boolean isShow = false;
    public void setWidowManager(WindowManager widowManager,Context context){
        try {
            this.widowManager = widowManager;
            this.wmParams = wmParams;
//        PixelView mFullScreenCheckView = new PixelView(context);
//        WindowManager windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            //创建非模态、不可碰触
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            //放在左上角
            params.gravity = Gravity.START | Gravity.TOP;
            params.height = 1;
            params.width = 1;
            //设置弹出View类型，
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
            params.format = PixelFormat.TRANSLUCENT;
            widowManager.addView(this, params);
            isShow = true;
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void remove(){
        if(isShow){
            try{
                widowManager.removeView(this);
                isShow = false;
            }catch (Throwable e){
                e.printStackTrace();
            }
        }
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        checkFullScreen();//通过监控View的位置改变重新测量布局的回调的时候去判断当前是否全屏
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkFullScreen();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        checkFullScreen();
    }

    private void checkFullScreen(){
        int[] location = new int[2];
        this.getLocationOnScreen(location);

        Log.i("CONTROL","LightView   "+location[1]+"  wmParams.y ");
        if(location[1] == 0){
            //全屏显示
        }else if(location[1] != 0){
            //不是全屏
        }
    }
}

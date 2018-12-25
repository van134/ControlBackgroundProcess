package com.click369.controlbp.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;

import com.click369.controlbp.R;

/**
 * Created by 41856 on 2018/12/19.
 */

//@SuppressLint("AppCompatCustomView")
public class LightViewNew extends View {
    public int dir = 0;//0左 1上 2右 3下
    private WindowManager widowManager;
    private WindowManager.LayoutParams wmParams;
    private  FrameLayout fl;
    public static final int durs[] ={500,1000,1500};
    public static final int LIGHT_TYPE_TEST= 0;
    public static final int LIGHT_TYPE_SCON= 1;
    public static final int LIGHT_TYPE_CHARGE= 2;
    public static final int LIGHT_TYPE_MSG= 3;
    public static final int LIGHT_TYPE_CALL= 4;
    public static final int LIGHT_TYPE_MUSIC= 5;
    public static boolean isStart = false;
//    boolean isSuiJi =true;
    int index = 0;
    final int MAX_COUNT = 100;
    final int MSG_COUNT = 4;
    final int CALL_COUNT = 50;
    final int MUSIC_COUNT = Animation.INFINITE;
    final int SCON_COUNT = 2;
    final int CHARGE_COUNT = 3;
    final int TEST_COUNT = 2;
    int type = 0;//0 消息  1 电话
    int count = 0;
//    int pics[] = {R.drawable.light_6,R.drawable.light_1,R.drawable.light_2,R.drawable.light_3,R.drawable.light_4,R.drawable.light_5};
    String colors[] = {"#ff0000","#00ff00","#0000ff","#ffff00","#00ffff","#ff00ff","#23e2fe","#aa46ff","#7bff11",
        "#e011ff","#fa0093","#f7fa00","#fac200","#3af1bb","#9a74f2","#fd00ad","#9001ff"};
    Bitmap bm,topBm,rightBm,bottomBm;
    Rect mSrcRect,mDestRect;
    Rect mSrcRectTop,mDestRectTop;
    Rect mSrcRectRight,mDestRectRight;
    Rect mSrcRectBottom,mDestRectBottom;
    Paint paint;
    public LightViewNew(Context context) {
        super(context);
    }

    public LightViewNew(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LightViewNew(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setWidowManager(WindowManager widowManager,
                                WindowManager.LayoutParams wmParams,
                                FrameLayout fl,
                                int dir){
        this.widowManager = widowManager;
        this.wmParams = wmParams;
        this.fl = fl;
        this.dir = dir;
        initBm(true);
    }

    private float[] getRandomColor(){
       String c = colors[(int)(Math.random()*colors.length)];
       return getfloatColorByCode(c);
    }
    private float[] getfloatColorByCode(String colorStr){
        int color = Color.parseColor(colorStr);
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        return new float[]{red*1.0f/255.0f,green*1.0f/255.0f,blue*1.0f/255.0f};
    }
    private float[] getAfloatColor(){
        float f1 = (float)(Math.random());
        float f2 = (float)(Math.random());
        float f3 = (float)(Math.random());
        if(f1<0.2&&f2<0.2&&f3<0.2){
            f1+=0.7f;
        }else if(f1<0.3&&f2<0.3&&f3<0.3){
            f2+=0.6f;
        }else if(f1<0.4&&f2<0.4&&f3<0.4){
            f3+=0.5f;
        }else if(f1>0.8&&f2>0.8&&f3>0.8){
            f3-=0.6f;
        }else if(f1>0.7&&f2>0.7&&f3>0.7){
            f2-=0.5f;
        }
        return new float[]{f1,f2,f3};
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setVisibility(View.INVISIBLE);
        initBm(false);
        float rgb[] = WatchDogService.isLightRandomMode?getRandomColor():getfloatColorByCode(WatchDogService.lightColor);
        // 根据SeekBar定义RGBA的矩阵
        float[] src = new float[]{
                rgb[0], 0, 0, 0, 0,
                 0, rgb[1], 0, 0, 0,
                 0, 0, rgb[2], 0, 0,
                 0, 0, 0, 0.98f, 0};
         // 定义ColorMatrix，并指定RGBA矩阵
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.set(src);
        // 设置Paint的颜色
        paint.setColorFilter(new ColorMatrixColorFilter(src));
        if(WatchDogService.lightShowMode == 0||WatchDogService.lightShowMode == 2){
            if(dir==0){
                canvas.drawBitmap(bm,mSrcRect, mDestRect, paint);
            }else if(dir==2){
                canvas.drawBitmap(rightBm,mSrcRectRight, mDestRectRight, paint);
            }
        }
        if(WatchDogService.lightShowMode == 1||WatchDogService.lightShowMode == 2){
            if(dir==1){
                canvas.drawBitmap(topBm,mSrcRectTop, mDestRectTop, paint);
            }else if(dir == 3){
                canvas.drawBitmap(bottomBm,mSrcRectBottom, mDestRectBottom, paint);
            }
        }
    }


    private int lastXiaoGuo = 0;
    public void initBm(boolean isNeedload){
        int width = wmParams.width;
        int height = wmParams.height;
        int pics[] = {R.drawable.light,R.drawable.light_1,R.drawable.light_2};
        if(bm==null||bm.isRecycled()||(lastXiaoGuo!=WatchDogService.lightXiaoGuo)){
            bm = BitmapFactory.decodeResource(this.getContext().getResources(),pics[WatchDogService.lightXiaoGuo]);
            paint = new Paint();
        }else if(type!=LIGHT_TYPE_TEST&&!isNeedload){
            return;
        }
        lastXiaoGuo = WatchDogService.lightXiaoGuo;
        int tempWidthPercent = WatchDogService.lightWidth;
        if(WatchDogService.lightXiaoGuo ==1){
            tempWidthPercent = 100;
        }
        int size = (WatchDogService.lightSize*width/200);
        int voffset = (height-(tempWidthPercent*height/100))/2;
        int hoffset = (width-(tempWidthPercent*width/100))/2;
        if(dir == 0){
            mSrcRect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
            mDestRect = new Rect(0, voffset, size, height-voffset);
        }else if(dir == 1){
            Matrix mxtop = new Matrix();
            mxtop.setSinCos(1,0,bm.getWidth()/2,bm.getHeight()/2);
            topBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mxtop, true);
            mSrcRectTop = new Rect(0, 0, topBm.getWidth(), topBm.getHeight());
            mDestRectTop = new Rect(hoffset, 0, width-hoffset, size);
        }else if(dir == 2){
            mSrcRectRight = new Rect(0, 0, bm.getWidth(), bm.getHeight());
            mDestRectRight = new Rect(width-size, voffset, width, height-voffset);
            Matrix mx = new Matrix();
            mx.setScale(-1, 1);
            rightBm = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(), bm.getHeight(), mx, true);
        }else if(dir == 3){
            Matrix mxbottom = new Matrix();
            mxbottom.setSinCos(-1,0,bm.getWidth()/2,bm.getHeight()/2);
            bottomBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mxbottom, true);
            mSrcRectBottom = new Rect(0, 0, bottomBm.getWidth(), bottomBm.getHeight());
            mDestRectBottom = new Rect(hoffset, height-size, width-hoffset, height);
        }
    }

    public void unInitAnimation(){
        clearAnimation();
        if(animationset!=null){
            animationset.cancel();
            animationset.reset();
            animationset = null;
        }
    }
    public void initAnimation(){
        unInitAnimation();
        animationset = new AnimationSet(false);
        final AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);//第一个参数开始的透明度，第二个参数结束的透明度
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        animationset.addAnimation(alphaAnimation);

        if(WatchDogService.isLightAnimScale&&(WatchDogService.lightShowMode==0||WatchDogService.lightShowMode==1)&&WatchDogService.lightXiaoGuo!=1){
            ScaleAnimation scaleAnimation = new ScaleAnimation(1f,1,0.2f,1,ScaleAnimation.RELATIVE_TO_PARENT,0.5f,ScaleAnimation.RELATIVE_TO_PARENT,0.5f);
            if(WatchDogService.lightShowMode==1){
                scaleAnimation = new ScaleAnimation(0f,1f,1f,1,ScaleAnimation.RELATIVE_TO_PARENT,0.5f,ScaleAnimation.RELATIVE_TO_PARENT,0.5f);
            }
//            scaleAnimation.setDuration(durs[WatchDogService.lightSpeed]);
            scaleAnimation.setRepeatCount(1);
            scaleAnimation.setRepeatMode(Animation.REVERSE);
//            scaleAnimation.setFillAfter(true);
            animationset.addAnimation(scaleAnimation);
        }
        animationset.setDuration(type == LIGHT_TYPE_MUSIC ? 1500 : durs[WatchDogService.lightSpeed]);//多长时间完成这个动作
        setAnimation(animationset);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(View.VISIBLE);
                setAlpha(1.0f);
                count++;
                Log.i("CONTROL","start anima   "+animation);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if ((type == LIGHT_TYPE_MSG && count >= MSG_COUNT) ||
                        (type == LIGHT_TYPE_CALL && count >= CALL_COUNT) ||
                        (type == LIGHT_TYPE_CHARGE && count >= CHARGE_COUNT) ||
                        (type == LIGHT_TYPE_TEST && count >= TEST_COUNT) ||
                        (type == LIGHT_TYPE_SCON && count >= SCON_COUNT)||
                        isNeedTest) {
//                    Log.i("CONTROL","stop anima   "+animation);

                    stopBl();
                    if(isNeedTest){
                        isNeedTest = false;
                        startBl(type);

                    }
                    return;
                }
                Log.i("CONTROL","restart anima   "+animation);
                if (isStart) {
                    if (WatchDogService.isLightRandomMode || lastXiaoGuo != WatchDogService.lightXiaoGuo) {
                        invalidate();
                    }
                    animationset.start();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void stopBl(){
        try {
//            synchronized (this) {
            if (!isStart) {
                return;
            }
//            Log.i("CONTROL ", "stop bl  type "+type+"  亮屏  "+!WatchDogService.isScreenOff);
            if(WatchDogService.isLightMusic&&type==LIGHT_TYPE_MUSIC&&!WatchDogService.isScreenOff){
                Log.i("CONTROL ", "stop remove bl   11111");
                final AudioManager audioManager = (AudioManager)this.getContext().getSystemService(Context.AUDIO_SERVICE);
                if(audioManager.isMusicActive()){
                    Log.i("CONTROL ", "stop remove bl   111112");
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(audioManager.isMusicActive()) {
                                Log.i("CONTROL ", "stop remove bl   111113");
                                startBl(LIGHT_TYPE_MUSIC);
                            }
                        }
                    },1000);
                }
            }
            isStart = false;
            this.type = 0;
            setAlpha(0.0f);
            unInitAnimation();
            setVisibility(View.GONE);
            if (bm != null) {
                bm.recycle();
            }
            if (rightBm != null) {
                rightBm.recycle();
            }
            if (topBm != null) {
                topBm.recycle();
            }
            if (bottomBm != null) {
                bottomBm.recycle();
            }
            bm = null;
            rightBm = null;
            topBm = null;
            bottomBm = null;
            destroyDrawingCache();
            System.gc();

            post(new Runnable() {
                @Override
                public void run() {
                    Log.i("CONTROL ", "stop remove bl");
                    widowManager.removeView(fl);
                }
            });
//            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    boolean isNeedTest = false;
    AnimationSet animationset;
//    AlphaAnimation alphaAnimation;
    public void startBl(int type){
        try {
            int dur = 100;
            if(type == LIGHT_TYPE_TEST&&isStart){
                if(WatchDogService.isLightAnimScale){
                    isNeedTest = true;
                    return;
                }else{
                    stopBl();
                    dur = 200;
                }
            }
            if (this.type <= type) {
                this.type = type;
            }
            Handler h = new Handler();
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    count = 0;
                    initAnimation();
                    if (isStart) {
                        animationset.start();
                        return;
                    }
                    initBm(true);
                    isStart = true;
                    widowManager.addView(fl, wmParams);
                    setAlpha(0.0f);
                    setVisibility(View.VISIBLE);
                    animationset.start();
                }
            };
            h.removeCallbacks(r);
            h.postDelayed(r,dur);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
    public void checkFullScreen(View v){
        try {
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            wmParams.y = location[1]*-1;
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

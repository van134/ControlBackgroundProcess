package com.click369.controlbp.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.media.Image;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.click369.controlbp.R;

/**
 * Created by 41856 on 2018/12/19.
 */

//@SuppressLint("AppCompatCustomView")
public class LightView extends View {
    public static final int durs[] ={500,1000,1500};
    public static final int LIGHT_TYPE_TEST= 0;
    public static final int LIGHT_TYPE_SCON= 1;
    public static final int LIGHT_TYPE_MSG= 2;
    public static final int LIGHT_TYPE_CALL= 3;
    public static final int LIGHT_TYPE_MUSIC= 4;
    public static boolean isStart = false;
//    boolean isSuiJi =true;
    int index = 0;
    final int MAX_COUNT = 100;
    final int MSG_COUNT = 4;
    final int CALL_COUNT = 50;
    final int MUSIC_COUNT = Animation.INFINITE;
    final int SCON_COUNT = 2;
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
    public LightView(Context context) {
        super(context);
    }

    public LightView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LightView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        //获取View的宽高
        int width = getWidth()/2;
        int height = getHeight();
        initBm(width,height);
//        int size = (WatchDogService.lightSize*width/100);
//        int voffset = (height-(WatchDogService.lightWidth*height/100))/2;
//        int hoffset = (width*2-(WatchDogService.lightWidth*width*2/100))/2;
        Paint paint = new Paint();
        float rgb[] = WatchDogService.isLightRandomMode?getRandomColor():getfloatColorByCode(WatchDogService.lightColor);
//        float rgb[] = getAfloatColor();
//        float r = rgb[0];
//        float g = rgb[1];
//        float b = rgb[2];
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
//        Rect mSrcRect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
//        Rect mDestRect = new Rect(0, voffset, size, height-voffset);
        if(WatchDogService.lightShowMode == 0||WatchDogService.lightShowMode == 2){
            canvas.drawBitmap(bm,mSrcRect, mDestRect, paint);
//            mDestRect = new Rect(width*2-size, voffset, width*2, height-voffset);
//            Matrix mx = new Matrix();
//            mx.setScale(-1, 1);
//            Bitmap rightBm = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(), bm.getHeight(), mx, true);
            canvas.drawBitmap(rightBm,mSrcRectRight, mDestRectRight, paint);
        }
        if(WatchDogService.lightShowMode == 1||WatchDogService.lightShowMode == 2){
//            Matrix mxtop = new Matrix();
//            mxtop.setSinCos(1,0,bm.getWidth()/2,bm.getHeight()/2);
//            Bitmap topBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mxtop, true);
//            mSrcRect = new Rect(0, 0, topBm.getWidth(), topBm.getHeight());
//            mDestRect = new Rect(hoffset, 0, width*2-hoffset, size);
            canvas.drawBitmap(topBm,mSrcRectTop, mDestRectTop, paint);

//            Matrix mxbottom = new Matrix();
//            mxbottom.setSinCos(-1,0,bm.getWidth()/2,bm.getHeight()/2);
//            Bitmap bottomBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mxbottom, true);
//            mSrcRect = new Rect(0, 0, bottomBm.getWidth(), bottomBm.getHeight());
//            mDestRect = new Rect(hoffset, height-size, width*2-hoffset, height);
            canvas.drawBitmap(bottomBm,mSrcRectBottom, mDestRectBottom, paint);
        }

    }

    private int lastXiaoGuo = 0;
    public void initBm(int width,int height){
        int pics[] = {R.drawable.light,R.drawable.light_1};
        if(bm==null||bm.isRecycled()||(lastXiaoGuo!=WatchDogService.lightXiaoGuo)){
            bm = BitmapFactory.decodeResource(this.getContext().getResources(),pics[WatchDogService.lightXiaoGuo]);
        }else if(type!=LIGHT_TYPE_TEST){
            return;
        }
        lastXiaoGuo = WatchDogService.lightXiaoGuo;
        int tempWidthPercent = WatchDogService.lightWidth;
        if(WatchDogService.lightXiaoGuo ==1){
            tempWidthPercent = 100;
        }
        int size = (WatchDogService.lightSize*width/100);
        int voffset = (height-(tempWidthPercent*height/100))/2;
        int hoffset = (width*2-(tempWidthPercent*width*2/100))/2;
        mSrcRect = new Rect(0, 0, bm.getWidth(), bm.getHeight());
        mDestRect = new Rect(0, voffset, size, height-voffset);

        mSrcRectRight = new Rect(0, 0, bm.getWidth(), bm.getHeight());
        mDestRectRight = new Rect(width*2-size, voffset, width*2, height-voffset);
        Matrix mx = new Matrix();
        mx.setScale(-1, 1);
        rightBm = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(), bm.getHeight(), mx, true);

        Matrix mxtop = new Matrix();
        mxtop.setSinCos(1,0,bm.getWidth()/2,bm.getHeight()/2);
        topBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mxtop, true);
        mSrcRectTop = new Rect(0, 0, topBm.getWidth(), topBm.getHeight());
        mDestRectTop = new Rect(hoffset, 0, width*2-hoffset, size);


        Matrix mxbottom = new Matrix();
        mxbottom.setSinCos(-1,0,bm.getWidth()/2,bm.getHeight()/2);
        bottomBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), mxbottom, true);
        mSrcRectBottom = new Rect(0, 0, bottomBm.getWidth(), bottomBm.getHeight());
        mDestRectBottom = new Rect(hoffset, height-size, width*2-hoffset, height);
    }

    public void init(){
        alphaAnimation = new AlphaAnimation(0.0f, 1.0f);//第一个参数开始的透明度，第二个参数结束的透明度
        alphaAnimation.setDuration(durs[WatchDogService.lightSpeed]);//多长时间完成这个动作
        alphaAnimation.setRepeatCount(1);
//        alphaAnimation.setFillAfter(true);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setVisibility(View.VISIBLE);
                setAlpha(1.0f);
                count++;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if ((type==LIGHT_TYPE_MSG&&count>=MSG_COUNT)||
                    (type==LIGHT_TYPE_CALL&&count>=CALL_COUNT)||
                    (type==LIGHT_TYPE_TEST&&count>=TEST_COUNT)||
                    (type==LIGHT_TYPE_SCON&&count>=SCON_COUNT)){
                    stopBl();
                    return;
                }
                if(isStart){
                    if(WatchDogService.isLightRandomMode||lastXiaoGuo!=WatchDogService.lightXiaoGuo) {
                        invalidate();
                    }
                    alphaAnimation.reset();
                    startAnimation(alphaAnimation);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void stopBl(){
        if(!isStart){
            return;
        }
        this.type = 0;
        isStart = false;
        setVisibility(View.GONE);
        setAlpha(0.0f);
        if(alphaAnimation!=null){
            alphaAnimation.cancel();
        }

    }

    AlphaAnimation alphaAnimation;
    public void startBl(int type){
        if(this.type<=type){
            this.type = type;
        }
        count = 0;
        alphaAnimation.setDuration(type==LIGHT_TYPE_MUSIC?1500:durs[WatchDogService.lightSpeed]);//多长时间完成这个动作
        if(isStart){
            return;
        }
        isStart = true;
        setAlpha(0.0f);
        setVisibility(View.VISIBLE);
        alphaAnimation.reset();
        startAnimation(alphaAnimation);
    }

}

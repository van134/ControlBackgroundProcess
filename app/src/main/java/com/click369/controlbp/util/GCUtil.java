package com.click369.controlbp.util;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

/**
 * Created by 41856 on 2019/2/2.
 */

public class GCUtil {
    public static void releaseBitMap(View v){
        if(v!=null && v instanceof ImageView){
            ImageView imageView = (ImageView)v;
            if(imageView.getDrawable()!=null){
                BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
                Bitmap bmp = drawable.getBitmap();
                if (null != bmp && !bmp.isRecycled()) {
                    bmp.recycle();
                }
            }
        }else if(v!=null){
            if(v.getBackground()!=null&&v.getBackground() instanceof BitmapDrawable){
                BitmapDrawable drawable = (BitmapDrawable)v.getBackground();
                Bitmap bmp = drawable.getBitmap();
                if (null != bmp && !bmp.isRecycled()) {
                    bmp.recycle();
                }
            }
        }
    }


    public static void unbindDrawables(View view)
    {
        if(view==null){
            return;
        }
        if (view.getBackground() != null)
        {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView))
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }




    /*是否释放背景图  true:释放;false:不释放*/
//    private static boolean flagWithBackgroud = false;
    /**
     *
     */
    public static void startGC(View layout) {
        if(layout!=null&&layout instanceof ViewGroup){
            recycle((ViewGroup) layout);
            ((ViewGroup) layout).removeAllViews();
        }
    }

    /**
     * 释放Imageview占用的图片资源
     * 用于退出时释放资源，调用完成后，请不要刷新界面
     * @param layout 需要释放图片的布局     *
     */
    private static void recycle(ViewGroup layout) {
        releaseBitMap(layout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            //获得该布局的所有子布局
            View subView = layout.getChildAt(i);
            //判断子布局属性，如果还是ViewGroup类型，递归回收
            if (subView instanceof ViewGroup) {
                //递归回收
                recycle((ViewGroup)subView);
            } else {
                releaseBitMap(subView);
            }
        }
    }
//
//    private static void recycleBackgroundBitMap(ImageView view) {
//        if (view != null&&view.getBackground()!=null&&view.getBackground() instanceof BitmapDrawable) {
//            BitmapDrawable bd = (BitmapDrawable) view.getBackground();
//            rceycleBitmapDrawable(bd);
//        }
//    }
//
//    private static void recycleImageViewBitMap(ImageView imageView) {
//        if (imageView != null&&imageView.getDrawable()!=null&&imageView.getDrawable() instanceof BitmapDrawable) {
//            BitmapDrawable bd = (BitmapDrawable) imageView.getDrawable();
//            rceycleBitmapDrawable(bd);
//        }
//    }
//
//    private static void rceycleBitmapDrawable(BitmapDrawable bd) {
//        if (bd != null) {
//            Bitmap bitmap = bd.getBitmap();
//            rceycleBitmap(bitmap);
//        }
//        bd = null;
//    }
//
//    private static void rceycleBitmap(Bitmap bitmap) {
//        if (bitmap != null && !bitmap.isRecycled()) {
//            bitmap.recycle();
//            bitmap = null;
//        }
//    }

}

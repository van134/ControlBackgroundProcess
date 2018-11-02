package com.click369.controlbp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by asus on 2017/7/19.
 */
public class BytesBitmap {
    public static Bitmap getBitmap(byte[] data) {
        if (data == null||data.length==0){
            return null;
        }
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static  byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream baops = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, baops);
        return baops.toByteArray();
    }
}

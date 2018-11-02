package com.click369.controlbp.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.click369.controlbp.util.BytesBitmap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by asus on 2017/5/29.
 */
public class AppInfo implements Serializable {
    public String appName;
    public String packageName;
    public boolean isUser;
    public boolean isDisable = false;
    public boolean isServiceStop;
//    public boolean isServiceDeadStone;//墓碑
    public boolean isWakelockStop;
    public boolean isBroadStop;
    public boolean isAlarmStop;
    public boolean isBackForceStop;
    public boolean isBackMuBei;
    public boolean isInMuBei;
    public boolean isHomeMuBei;
    public boolean isHomeIdle;
    public boolean isOffscForceStop;
    public boolean isOffscMuBei;
    public boolean isNotifyNotExit;
    public boolean isAutoStart;
    public boolean isNotStop;
    public boolean isLockApp;
    public boolean isStopApp;
    public boolean isDozeOffsc;
    public boolean isDozeOnsc;
    public boolean isDozeOpenStop;
    public boolean isRecentNotClean;
    public boolean isRecentForceClean;
    public boolean isRecentBlur;
    public boolean isRecentNotShow;

    public boolean isBarLockList;
    public boolean isBarColorList;
    public boolean isADJump;
    public boolean isNotUnstall;
//    public boolean isStopMoblieNet;
//    public boolean isStopWifiNet;
    public boolean isRunning;

    public int activityCount;
    public int serviceCount;
    public int broadCastCount;

    public int activityDisableCount;
    public int serviceDisableCount;
    public int broadCastDisableCount;
    public int uid;
    public long instanllTime;
    public byte bits[];
    public AppInfo(){}
    public AppInfo(String appName){
        this.appName = appName;
    }
    public AppInfo(String appName, String packageName){
        this.appName = appName;
        this.packageName = packageName;
    }
    public AppInfo(String appName, String packageName, Bitmap bm, boolean isUser, boolean isDisable){
        this.appName = appName;
        this.packageName = packageName;
//        this.drawable = drawable;
        bits = BytesBitmap.getBytes(bm);
        this.isUser = isUser;
        this.isDisable = isDisable;
    }


    public String getAppName() {
        if(null == appName)
            return "";
        else
            return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        if(null == packageName)
            return "";
        else
            return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Bitmap getBitmap() {
        return getRoundedCornerBitmap(BytesBitmap.getBitmap(bits));
//        return getRoundedCornerBitmap(BytesBitmap.getBitmap(bits));
    }

    public void setBitmap(Bitmap bitamp) {
        this.bits = BytesBitmap.getBytes(bitamp);
    }

    public void recDradwable(){
        if (bits != null&&bits.length>0) {
            bits = null;
        }
    }

    public static void writeArrays(ArrayList<AppInfo> infos, Context cxt){
        try {
            File f = new File(cxt.getFilesDir(),"app.obj");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(infos);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static  ArrayList<AppInfo> readArrays(Context cxt){
        ArrayList<AppInfo> apps = new ArrayList<AppInfo>();
        File f = new File(cxt.getFilesDir(),"app.obj");
        if(!f.exists()){
            Log.i("CONTROL","applist 不存在"+f.getAbsolutePath());
            return apps;
        }
        Log.i("CONTROL","applist 1存在");
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            apps = ((ArrayList<AppInfo>)ois.readObject());

            Log.i("CONTROL","applist 存在"+apps.size());
            ois.close();
        } catch (Exception e) {
            f.delete();
//            AppInfo.writeArrays(apps, cxt);
//            e.printStackTrace();
        }finally {
            return apps;
        }
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        try {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();

            final Rect rect = new Rect(0, 0, bitmap.getWidth(),bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),  bitmap.getHeight()));
            final float roundPx = bitmap.getWidth()>bitmap.getHeight()?bitmap.getWidth()/2:bitmap.getHeight()/2;
            paint.setAntiAlias(true);
//            canvas.drawRGB(0,0,0);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            final Rect src = new Rect(0, 0, bitmap.getWidth(),  bitmap.getHeight());
            canvas.drawBitmap(bitmap, src, rect, paint);
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

}

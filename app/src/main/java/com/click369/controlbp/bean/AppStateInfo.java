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
public class AppStateInfo implements Serializable {
//    public String appName="";
//    public String packageName="";
//    public boolean isRunning;
    public boolean isImePkg;
    public boolean isHomePkg;
    public boolean isHasNotify;
    public boolean isNotifyNotMubei;
    public boolean isNotifyNotIdle;
    public boolean isNotifyNotStop;
    public boolean isPressKeyBack;
    public boolean isPressKeyHome;
    public boolean isHasAudioFocus;
    public boolean isOpenFromIceRome;
    public boolean isReadyOffStop;
    public boolean isReadyOffMuBei;
    public boolean isReadyOffIce;
    public boolean isSetTimeStopAlreadStart;
    public boolean isInMuBei;
    public boolean isInIdle;
    public long backStartTime;
    public long homeStartTime;
    public long offScTime;
}

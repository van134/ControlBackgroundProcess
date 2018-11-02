package com.click369.controlbp.util;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.UserHandle;
import android.util.Log;

import com.click369.controlbp.activity.MainActivity;
import com.click369.controlbp.bean.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by asus on 2017/10/15.
 */
public class AppLoaderUtil {
    public static ArrayList<AppInfo> getAppInfos(Context cxt, int type) {//0用户 1系统  2所有
        ArrayList<AppInfo> appInfos = new ArrayList<AppInfo>();
        synchronized (MainActivity.class) {
        PackageManager  pm = cxt.getPackageManager();
        List<PackageInfo> packgeInfos = pm.getInstalledPackages(0);

        /* 获取应用程序的名称，不是包名，而是清单文件中的labelname
            String str_name = packageInfo.applicationInfo.loadLabel(pm).toString();
            appInfo.setAppName(str_name);
         */
        for(PackageInfo packgeInfo : packgeInfos){

            String packageName = packgeInfo.packageName;
            if("com.click369.controlbp".equals(packageName)||
                    "android".equals(packageName)||
//                    "com.fkzhang.wechatxposed".equals(packageName)||
                "com.android.systemui".equals(packageName)){
                continue;
            }
                String appName = "";
                try {
                    appName = packgeInfo.applicationInfo.loadLabel(pm).toString();
                }catch (Exception e){
                    pm = cxt.getPackageManager();
                    try {
                        appName = packgeInfo.applicationInfo.loadLabel(pm).toString();
                    }catch (Exception e1){
                        e1.printStackTrace();
                    }
                }


            AppInfo appInfo = null;
            try {
                Drawable d = packgeInfo.applicationInfo.loadIcon(pm);
                appInfo = new AppInfo(appName, packageName,zoomDrawable(d,100,100),(packgeInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0,!packgeInfo.applicationInfo.enabled);
                appInfo.instanllTime = packgeInfo.firstInstallTime;
                appInfo.uid = packgeInfo.applicationInfo.uid;
                PackageInfo piS = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_SERVICES | PackageManager.GET_DISABLED_COMPONENTS);
                PackageInfo piB = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_RECEIVERS | PackageManager.GET_DISABLED_COMPONENTS);
                PackageInfo piA = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
                appInfo.activityCount = piA.activities != null ? piA.activities.length : 0;
                appInfo.serviceCount = piS.services != null ? piS.services.length : 0;
                appInfo.broadCastCount = piB.receivers != null ? piB.receivers.length : 0;
                if (appInfo.activityCount == 0 && appInfo.serviceCount == 0 && appInfo.broadCastCount == 0) {
                    continue;
                }
//                else{
//                    PackageInfo piSDis = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_SERVICES);
//                    PackageInfo piBDis = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_RECEIVERS);
//                    PackageInfo piADis = pm.getPackageInfo(packgeInfo.packageName, PackageManager.GET_ACTIVITIES );
//                    appInfo.serviceDisableCount = appInfo.serviceCount- (piSDis.services != null ? piSDis.services.length : 0);
//                    appInfo.activityDisableCount = appInfo.activityCount - (piADis.activities != null ? piADis.activities.length : 0);
//                    appInfo.broadCastDisableCount = appInfo.broadCastCount- ( piBDis.receivers != null ? piBDis.receivers.length : 0);
//                }
            } catch (Exception e) {
                e.printStackTrace();
//                if(cxt instanceof Activity){
//                    pm = ((Activity)cxt).getApplication().getPackageManager();
//                }else{
//                    pm = ((Service)cxt).getApplication().getPackageManager();
//                }
                pm = cxt.getPackageManager();
            }
            if (appInfo!=null) {
                if (type == 0) {
                    if ((packgeInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        appInfos.add(appInfo);
                    }
                } else if (type == 1) {
                    if ((packgeInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                        appInfos.add(appInfo);
                    }
                } else {
                    appInfos.add(appInfo);
                }
            }
        }

        if(type == 2){
//            if(cxt instanceof Activity){
//                AppInfo.writeArrays(appInfos,((Activity)cxt));
//            }else{
//                AppInfo.writeArrays(appInfos,((Service)cxt));
//            }
            AppInfo.writeArrays(appInfos,cxt);
        }
        PinyinCompare comparent = new PinyinCompare();
        Collections.sort(appInfos, comparent);
        }
        return appInfos;
    }

    public static Bitmap zoomDrawable(Drawable drawable, int w, int h)
    {
        try {
            int width = drawable.getIntrinsicWidth();
            int height= drawable.getIntrinsicHeight();
            Bitmap oldbmp = drawableToBitmap(drawable); // drawable 转换成 bitmap
            if (oldbmp==null||width<=0||height<0){
                Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                return bitmap;
            }
            Matrix matrix = new Matrix();   // 创建操作图片用的 Matrix 对象
            float scaleWidth = ((float)w / width);   // 计算缩放比例
            float scaleHeight = ((float)h / height);
            matrix.postScale(scaleWidth, scaleHeight);         // 设置缩放比例
            Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);       // 建立新的 bitmap ，其内容是对原 bitmap 的缩放后的图
            return newbmp;       // 把 bitmap 转换成 drawable 并返回
        }catch (Exception e){
            return  null;
        }
    }
    static Bitmap drawableToBitmap(Drawable drawable) // drawable 转换成 bitmap
    {
        try {
            int width = drawable.getIntrinsicWidth();   // 取 drawable 的长宽
            int height = drawable.getIntrinsicHeight();
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;         // 取 drawable 的颜色格式
            if (width<=0||height<=0){
//            Bitmap bitmap = Bitmap.createBitmap(100, 100, config);
                return null;
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, config);     // 建立对应 bitmap
            Canvas canvas = new Canvas(bitmap);         // 建立对应 bitmap 的画布
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);      // 把 drawable 内容画到画布中
            return bitmap;
        }catch (Exception e){
            return  null;
        }

    }

}

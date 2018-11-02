package com.click369.controlbp.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by asus on 2017/9/29.
 */
public class PermissionUtils {
    public static boolean checkPermissionAllGranted(Context cxt,String[]permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(cxt, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /*
    *   new String[] {
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
    * */
    public static void requestPermission(Activity cxt, String[]permissions){
        ActivityCompat.requestPermissions(
                cxt,
                permissions,
                0X1
        );
    }

    public static boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 0X1) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            return isAllGranted;
        }
        return false;
    }


}

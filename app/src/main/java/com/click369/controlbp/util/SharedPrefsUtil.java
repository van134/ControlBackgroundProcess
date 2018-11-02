package com.click369.controlbp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.click369.controlbp.common.Common;

import java.io.File;

/**
 * Created by asus on 2017/11/4.
 */
public class SharedPrefsUtil {
    public static SharedPreferences getPreferences(Context ctx, String prefName) {
        SharedPreferences prefs = ctx.getSharedPreferences(prefName, Context.MODE_WORLD_READABLE);
//        final File prefsFile = new File(ctx.getFilesDir() + "/../shared_prefs/" + prefName + ".xml");
//        Log.i("CONTROL","prefsFile exists"+prefsFile.exists());
//        boolean isOk  = prefsFile.setReadable(true, false);
//        Log.i("CONTROL","prefsFile isOk"+isOk);
//        prefsFile.setReasable(true, false);
        return prefs;
    }
//    public static void changeQx(SharedPreferences sp){
//    }
}

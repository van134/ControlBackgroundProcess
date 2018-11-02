package com.click369.controlbp.util;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.EmptyActivity;
import com.click369.controlbp.activity.IceRoomActivity;
import com.click369.controlbp.activity.RunningActivity;
import com.click369.controlbp.receiver.PhoneStateReciver;

import java.util.Arrays;

/**
 * Created by asus on 2017/5/29.
 */
public class ShortCutUtil {
    public static final String ACTION_REMOVE_SHORTCUT = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";
    public static void addShortcut(String name, Context cxt, Class cls, int icid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addShortCut(name,cxt,cls,icid);
        }else{
            Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
            // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
            // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
            // 屏幕上没有空间时会提示
            // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式
            // 名字
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            // 图标
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(cxt,icid));
            // 设置关联程序
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.putExtra("data",name);
            launcherIntent.setClass(cxt, cls);
            launcherIntent.addCategory("controlbp");//Intent.CATEGORY_LAUNCHER
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            // 发送广播
            cxt.sendBroadcast(addShortcutIntent);
        }
    }

    public static void addShortcutDrawable(String pkg, String name, Context cxt, Class cls, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addShortCutDrawable(pkg,name,cxt,cls,bitmap);
        }else {
            Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);
            // 不允许重复创建
            addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
            // 名字
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            // 图标
            bitmap = Bitmap.createScaledBitmap(bitmap, 130, 130, true);
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
            // 设置关联程序
            Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
            launcherIntent.putExtra("pkg", pkg);
            launcherIntent.putExtra("name", name);
            launcherIntent.setClass(cxt, cls);
            launcherIntent.addCategory("controlbp");//Intent.CATEGORY_LAUNCHER
            addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
            // 发送广播
            cxt.sendBroadcast(addShortcutIntent);
        }
    }

    @TargetApi(26)
    public static void addShortCut(String name, Context context, Class cls, int icid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            if (shortcutManager.isRequestPinShortcutSupported()) {
                Intent shortcutInfoIntent = new Intent(context, cls);
                shortcutInfoIntent.putExtra("data", name);
                shortcutInfoIntent.setAction(""); //action必须设置，不然报错
                ShortcutInfo info = new ShortcutInfo.Builder(context, name)
                        .setIcon(Icon.createWithResource(context, icid))
                        .setShortLabel(name)
                        .setIntent(shortcutInfoIntent)
                        .build();
                //当添加快捷方式的确认弹框弹出来时，将被回调
                PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, PhoneStateReciver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.getIntentSender());
            }
        }
    }
    @TargetApi(26)
    public static void addShortCutDrawable(String pkg, String name, Context context, Class cls, Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);
            if (shortcutManager.isRequestPinShortcutSupported()) {
                Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
                launcherIntent.putExtra("pkg",pkg);
                launcherIntent.putExtra("name",name);
                launcherIntent.setClass(context, cls);
                launcherIntent.addCategory("controlbp");
                launcherIntent.setAction(""); //action必须设置，不然报错
                ShortcutInfo info = new ShortcutInfo.Builder(context, name)
                        .setIcon(Icon.createWithBitmap(bitmap))
                        .setShortLabel(name)
                        .setIntent(launcherIntent)
                        .build();
                //当添加快捷方式的确认弹框弹出来时，将被回调
                PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, PhoneStateReciver.class), PendingIntent.FLAG_UPDATE_CURRENT);
                shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.getIntentSender());
            }
        }
    }

//    public static void removeShortcut(String name,Context cxt) {
//        // remove shortcut的方法在小米系统上不管用，在三星上可以移除
//        Intent intent = new Intent(ACTION_REMOVE_SHORTCUT);
//        // 名字
//        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
//        // 设置关联程序
//        Intent launcherIntent = new Intent(cxt,EmptyActivity.class).setAction(Intent.ACTION_MAIN);
//        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
//        // 发送广播
//        cxt.sendBroadcast(intent);
//    }

    /**
     * 为App创建动态Shortcuts
     */
//    @TargetApi(25)
    public static void initDynamicShortcuts(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            //①、创建动态快捷方式的第一步，创建ShortcutManager
            ShortcutManager scManager = context.getSystemService(ShortcutManager.class);
            Intent intent1 = new Intent(context, EmptyActivity.class);
            Intent intent2 = new Intent(context, RunningActivity.class);
            Intent intent3 = new Intent(context, IceRoomActivity.class);
            Intent intent4 = new Intent(context, EmptyActivity.class);
            intent1.setAction("");
            intent1.putExtra("data", "清理");
            intent4.setAction("");
            intent4.putExtra("data", "重启");
            intent2.setAction("");
            intent3.setAction("");
            //②、构建动态快捷方式的详细信息
            ShortcutInfo scInfoOne = new ShortcutInfo.Builder(context, "dynamic_one")
                    .setShortLabel("清理缓存")
                    .setLongLabel("清理缓存")
                    .setIcon(Icon.createWithResource(context, R.drawable.icon_clean))
                    .setIntent(intent1)
                    .build();
            ShortcutInfo scInfoTwo = new ShortcutInfo.Builder(context, "dynamic_two")
                    .setShortLabel("正在运行的程序")
                    .setLongLabel("正在运行的程序")
                    .setIcon(Icon.createWithResource(context, R.drawable.icon_run))
                    .setIntent(intent2)
                    .build();
            ShortcutInfo scInfoThree = new ShortcutInfo.Builder(context, "dynamic_three")
                    .setShortLabel("冷藏室")
                    .setLongLabel("冷藏室")
                    .setIcon(Icon.createWithResource(context, R.drawable.icon_iceroom))
                    .setIntent(intent3)
                    .build();
            ShortcutInfo scInfoFour = new ShortcutInfo.Builder(context, "dynamic_four")
                    .setShortLabel("重启选项")
                    .setLongLabel("重启选项")
                    .setIcon(Icon.createWithResource(context, R.drawable.icon_restart))
                    .setIntent(intent4)
                    .build();
            //③、为ShortcutManager设置动态快捷方式集合
            scManager.setDynamicShortcuts(Arrays.asList(scInfoOne, scInfoTwo, scInfoThree,scInfoFour));

//        //如果想为两个动态快捷方式进行排序，可执行下面的代码
//        ShortcutInfo dynamicWebShortcut = new ShortcutInfo.Builder(this, "dynamic_one")
//                .setRank(0)
//                .build();
//        ShortcutInfo dynamicActivityShortcut = new ShortcutInfo.Builder(this, "dynamic_two")
//                .setRank(1)
//                .build();

            //④、更新快捷方式集合
            scManager.updateShortcuts(Arrays.asList(scInfoOne, scInfoTwo, scInfoThree,scInfoFour));
//        scManager.updateShortcuts(Arrays.asList(dynamicWebShortcut, dynamicActivityShortcut));
        }
    }
}

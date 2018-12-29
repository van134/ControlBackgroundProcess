package com.click369.controlbp.util;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

import com.click369.controlbp.R;
import com.click369.controlbp.activity.MainActivity;


/**
 * Created by asus on 2017/5/25.
 */
public class Notify {
    // 为发送通知的按钮的点击事件定义事件处理方法
    @TargetApi(24)
    public static void sendNotify(Context context, int state, boolean isCanCanel) {//0stop 1start 2pause
        int ids[] = {R.drawable.icon_notdoze,R.drawable.icon_doze};
//        String title = "强制打盹正在运行";
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 创建一个启动其他Activity的Intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("from","doze");
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        // 单击Notification 通知时将会启动Intent 对应的程序，实现页面的跳转
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        RemoteViews mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.doze_notify);
        Notification notify = new Notification.Builder(context)
                // 设置打开该通知，该通知自动消失
                .setAutoCancel(false)
                // 设置显示在状态栏的通知提示信息
//                .setTicker("强制打盹运行")
                // 设置通知的图标
                .setSmallIcon(ids[state])
                // 设置通知内容的标题
                .setContentTitle(state==0?"没有打盹":"正在打盹")
                // 设置通知内容
//                .setContentText("恭喜你，您加薪了，工资增加20%!")
                // // 设置使用系统默认的声音、默认LED灯
                 .setDefaults( isCanCanel? Notification.DEFAULT_ALL: Notification.FLAG_NO_CLEAR)
//                 |Notification.DEFAULT_LIGHTS)
                // 设改通知将要启动程序的Intent
                .setContentIntent(pi)
//                .setCustomContentView(mRemoteViews)
                .getNotification();
        if(!isCanCanel){
            notify.flags = Notification.FLAG_ONGOING_EVENT;
        }
        // 发送通知
        nm.notify(1, notify);
    }

    // 为删除通知的按钮的点击事件定义事件处理方法
    public static void cancelNotify(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // 取消通知
        nm.cancel(1);
    }



    public static void testNotify(Context context) {

        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(0);
        // 创建一个启动其他Activity的Intent
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        final Notification notify = new Notification.Builder(context)
                // 设置打开该通知，该通知自动消失
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.icon)
                // 设置显示在状态栏的通知提示信息
                // 设置通知内容的标题
                .setContentTitle("这是一条测试通知")
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(pi)
                .setFullScreenIntent(pi, true)
                .getNotification();
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 发送通知
                nm.notify(0, notify);
            }
        },500);
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    // 发送通知
                    nm.cancel(0);
                }catch (Exception e){
                }
            }
        },3500);
    }
}

package com.click369.controlbp.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Created by asus on 2017/6/1.
 */
public class TimeUtil {
    public static String changeMils2String(long mils){
        if(mils == 0){
            return "无";
        }
        Date date = new Date(mils);
        Calendar get = Calendar.getInstance();
        get.setTimeInMillis(mils);
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        if(get.get(Calendar.DAY_OF_MONTH)!=now.get(Calendar.DAY_OF_MONTH)&&System.currentTimeMillis()-mils>1000*60*60*10){
            SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd");
            return sdf.format(date);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(date);
    }

    public static String changeMils2String(long mils,String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(mils));
    }
    public static String changeMils2StringZero(long mils,String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
        return sdf.format(new Date(mils));
    }
    //"20:00-01:00", "01:00"
    public static boolean isInTime(String sourceTime, String curTime) {
        if (sourceTime == null || !sourceTime.contains("-") || !sourceTime.contains(":")) {
            throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
        }
        if (curTime == null || !curTime.contains(":")) {
            throw new IllegalArgumentException("Illegal Argument arg:" + curTime);
        }
        String[] args = sourceTime.split("-");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            long now = sdf.parse(curTime).getTime();
            long start = sdf.parse(args[0]).getTime();
            long end = sdf.parse(args[1]).getTime();
            if (args[1].equals("00:00")) {
                args[1] = "24:00";
            }
            if (end < start) {
                if (now >= end && now < start) {
                    return false;
                } else {
                    return true;
                }
            }
            else {
                if (now >= start && now < end) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
        }

    }
}


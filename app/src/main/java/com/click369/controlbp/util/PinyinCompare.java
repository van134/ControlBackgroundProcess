package com.click369.controlbp.util;

import com.click369.controlbp.bean.AppInfo;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by asus on 2017/5/30.
 */
public class PinyinCompare implements  Comparator<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object arg0, Object arg1) {
        AppInfo name1 = (AppInfo)arg0;
        AppInfo name2 = (AppInfo)arg1;
        Collator ca = Collator.getInstance(Locale.CHINA);
        int flags = 0;
        if (ca.compare( name1.appName, name2.appName) < 0) {
            flags = -1;
        }
        else if(ca.compare( name1.appName, name2.appName) > 0) {
            flags = 1;
        }
        else {
            flags = 0;
        }
        return flags;
    }
}
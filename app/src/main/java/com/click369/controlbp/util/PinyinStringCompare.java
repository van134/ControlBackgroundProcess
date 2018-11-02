package com.click369.controlbp.util;

import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.PkgAndName;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by asus on 2017/5/30.
 */
public class PinyinStringCompare implements  Comparator<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object arg0, Object arg1) {
        PkgAndName name1 = (PkgAndName)arg0;
        PkgAndName name2 = (PkgAndName)arg1;
        Collator ca = Collator.getInstance(Locale.CHINA);
        int flags = 0;
        if (ca.compare( name1.name, name2.name) < 0) {
            flags = -1;
        }
        else if(ca.compare(name1.name, name2.name) > 0) {
            flags = 1;
        }
        else {
            flags = 0;
        }
        return flags;
    }
}
package com.click369.controlbp.util;

import android.content.pm.ActivityInfo;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by asus on 2017/5/30.
 */
public class PinyinCompareDisableReceiver implements Comparator<Object> {

    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object arg0, Object arg1) {
        ActivityInfo name1 = (ActivityInfo)arg0;
        ActivityInfo name2 = (ActivityInfo)arg1;
        Collator ca = Collator.getInstance(Locale.ENGLISH);
        String n1 = "";
        String n2 = "";
        if(name1.name.indexOf(".")>-1){
            String s = name1.name.substring(name1.name.lastIndexOf(".")+1).trim();
            n1 =  s.length()>1?s:name1.name;
        }else{
            n1 = name1.name;
        }
        if(name2.name.indexOf(".")>-1){
            String s = name2.name.substring(name2.name.lastIndexOf(".")+1).trim();
            n2 =  s.length()>1?s:name2.name;
        }else{
            n2 = name2.name;
        }
        int flags = 0;
        if (ca.compare(n1,n2) < 0) {
            flags = -1;
        }
        else if(ca.compare( n1,n2) > 0) {
            flags = 1;
        }
        else {
            flags = 0;
        }
        return flags;
    }
}
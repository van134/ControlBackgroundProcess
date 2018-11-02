package com.click369.controlbp.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by asus on 2017/10/26.
 */
public class DozeWhiteListUtil {
    public static HashSet<String> getWhiteList(Set<String> all, int type){
        HashSet<String> list = new HashSet<String>();
        for(String s:all){
            if(type == 0&&s.endsWith("/offsc")){
                list.add(s.replace("/offsc",""));
            }else if(type == 1&&s.endsWith("/onsc")){
                list.add(s.replace("/onsc",""));
            }
        }
        return  list;
    }

    public static void putWhiteList(Set<String> list){
        List<String> ls = new ArrayList<String>();
        for(String s:list){
            ls.add("dumpsys deviceidle whitelist +"+s);
        }
        ShellUtilDoze.execCommand(ls);
    }

    public static void removeWhiteList(Set<String> list){
        List<String> ls = new ArrayList<String>();
        for(String s:list){
            ls.add("dumpsys deviceidle whitelist -"+s);
        }
        ShellUtilDoze.execCommand(ls);
    }
}

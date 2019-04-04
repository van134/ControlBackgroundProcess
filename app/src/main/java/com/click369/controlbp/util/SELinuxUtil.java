package com.click369.controlbp.util;

/**
 * Created by asus on 2017/10/17.
 */
public class SELinuxUtil {
    public static boolean isSELOpen(){
        String selinuxState = ShellUtils.execCommand("getenforce",true,true).successMsg;
        if (selinuxState!=null&&!selinuxState.toLowerCase().contains("permissive")) {
            return true;
        }
        return false;
    }

    public static void closeSEL(){
//        ShellUtilNoBackData.execCommand("setenforce 0");
        ShellUtils.execCommand("setenforce 0",true,false);
    }

    public static void openSEL(){
        ShellUtils.execCommand("setenforce 1",true,false);
//        ShellUtilNoBackData.execCommand("setenforce 1");
    }
}

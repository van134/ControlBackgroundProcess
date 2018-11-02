package com.click369.controlbp.bean;

/**
 * Created by asus on 2017/11/30.
 */
public class NavInfo {
    public int type;//0开关型 1数字型
    public String titleName;
    public String content;
    public boolean isOn;
    public NavInfo(int type,String titleName,String content,boolean isOn){
        this.type = type;
        this.titleName = titleName;
        this.content = content;
        this.isOn = isOn;
    }
}

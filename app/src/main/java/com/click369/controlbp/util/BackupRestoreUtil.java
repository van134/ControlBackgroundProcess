package com.click369.controlbp.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Build;

import com.click369.controlbp.activity.MainActivity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by asus on 2017/11/11.
 */
public class BackupRestoreUtil {
    private MainActivity act;
    public BackupRestoreUtil(MainActivity act){
        this.act = act;
    }
    public void saveData(int type){
        boolean isOk = true;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            String[] permissions= {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            if(!PermissionUtils.checkPermissionAllGranted(act,permissions)){
                isOk = false;
            }
        }
        if(isOk){
            FileUtil.init();
            if(type == 0){
                Map<String,Boolean> allChoose = new HashMap<String,Boolean>();
                Map<String,?> temp =  act.modPrefs.getAll();
                for(String s:temp.keySet()){
                    allChoose.put(s,Boolean.parseBoolean(temp.get(s).toString()));
                }
                FileUtil.writeObj(allChoose,FileUtil.FILEPATH+ File.separator+"choose");

                Map allWakeLock = new HashMap();
                Map temp1 =  act.wakeLockPrefs.getAll();
                for(Object s:temp1.keySet()){
                    allWakeLock.put(s,temp1.get(s));
                }
                FileUtil.writeObj(allWakeLock,FileUtil.FILEPATH+ File.separator+"wakelock");

                Map allAlarm = new HashMap();
                Map temp2 =  act.alarmPrefs.getAll();
                for(Object s:temp2.keySet()){
                    allAlarm.put(s,temp2.get(s));
                }
                FileUtil.writeObj(allAlarm,FileUtil.FILEPATH+ File.separator+"alarm");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"choose");
            }else if(type == 1){
                Map<String,Boolean> allForce = new HashMap<String,Boolean>();
                Map<String,?> temp1 =  act.forceStopPrefs.getAll();
                for(String s:temp1.keySet()){
                    allForce.put(s,Boolean.parseBoolean(temp1.get(s).toString()));
                }
                FileUtil.writeObj(allForce,FileUtil.FILEPATH+File.separator+"force");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"force");
            }else if(type == 2){
                act.ifwFragment.backup();
            }else if(type == 3){
                Map<String,Boolean> allForce = new HashMap<String,Boolean>();
                Map<String,?> temp1 =  act.pmPrefs.getAll();
                for(String s:temp1.keySet()){
                    allForce.put(s,Boolean.parseBoolean(temp1.get(s).toString()));
                }
                FileUtil.writeObj(allForce,FileUtil.FILEPATH+File.separator+"package");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"package");
            }else if(type == 4){
                Map<String,Boolean> allForce = new HashMap<String,Boolean>();
                Map<String,?> temp1 =  act.dozePrefs.getAll();
                for(String s:temp1.keySet()){
                    allForce.put(s,Boolean.parseBoolean(temp1.get(s).toString()));
                }
                FileUtil.writeObj(allForce,FileUtil.FILEPATH+File.separator+"doze");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"doze");
            }else if(type == 6){
                for(int i = 0;i<12;i++){
                    if(i == 2||i == 6){
                        continue;
                    }
                    saveData(i);
                }
                Map<String,Object> allForce = new HashMap<String,Object>();
                Map<String,?> temp1 =  act.settings.getAll();
                for(String s:temp1.keySet()){
                    allForce.put(s,temp1.get(s));
                }
                FileUtil.writeObj(allForce,FileUtil.FILEPATH+File.separator+"setting");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"setting");
            }else if(type == 8){
                Map<String,Boolean> allForce = new HashMap<String,Boolean>();
                Map<String,?> temp1 = act.autoStartNetPrefs.getAll();
                for(String s:temp1.keySet()){
                    allForce.put(s,Boolean.parseBoolean(temp1.get(s).toString()));
                }
                FileUtil.writeObj(allForce,FileUtil.FILEPATH+File.separator+"autostartnet");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"autostartnet");
            }else if(type == 9){
                Map<String,Boolean> allForce = new HashMap<String,Boolean>();
                Map<String,?> temp1 =  act.uiBarPrefs.getAll();
                for(String s:temp1.keySet()){
                    allForce.put(s,Boolean.parseBoolean(temp1.get(s).toString()));
                }
                FileUtil.writeObj(allForce,FileUtil.FILEPATH+File.separator+"uicontrol");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"uicontrol");
            }else if(type == 10){
                Map<String,Object> allForce = new HashMap<String,Object>();
                Map<String,?> temp1 =  act.adPrefs.getAll();
                for(String s:temp1.keySet()){
                    allForce.put(s,temp1.get(s));
                }
                FileUtil.writeObj(allForce,FileUtil.FILEPATH+File.separator+"adjump");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"adjump");
            }else if(type == 11){
                Map<String,Boolean> allRecents = new HashMap<String,Boolean>();
                Map<String,?> temp1 =  act.recentPrefs.getAll();
                for(String s:temp1.keySet()){
                    allRecents.put(s,Boolean.parseBoolean(temp1.get(s).toString()));
                }
                FileUtil.writeObj(allRecents,FileUtil.FILEPATH+File.separator+"recents");
                act.showT("保存成功，保存在"+FileUtil.FILEPATH+File.separator+"recents");
            }
        }else{
            act.showT("保存失败，请授予文件读写权限");
        }
    }


    public void restoreData(int type){
        if(type == 0){
            File file = new File(FileUtil.FILEPATH+ File.separator+"choose");
            if(file.exists()){
                Object o = FileUtil.readObj(FileUtil.FILEPATH+ File.separator+"choose");
                if(o!=null){
                    Map<String,Boolean>  datas = (Map<String,Boolean>)o;
                    SharedPreferences.Editor ed = act.modPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p)){
                            ed.putBoolean(p,datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
                Object o1 = FileUtil.readObj(FileUtil.FILEPATH+ File.separator+"wakelock");
                if(o1!=null){
                    Map  datas = (Map)o1;
                    SharedPreferences.Editor ed = act.wakeLockPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(Object p:datas.keySet()){
                        if(datas.get(p) instanceof Boolean){
                            ed.putBoolean((String)p,(Boolean)datas.get(p));
                        }else if(datas.get(p) instanceof String){
                            ed.putString((String)p,(String)datas.get(p));
                        }else if(datas.get(p) instanceof Integer){
                            ed.putInt((String)p,(Integer)datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
                Object o2 = FileUtil.readObj(FileUtil.FILEPATH+ File.separator+"alarm");
                if(o2!=null){
                    Map  datas = (Map)o2;
                    SharedPreferences.Editor ed = act.alarmPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(Object p:datas.keySet()){
                        if(datas.get(p) instanceof Boolean){
                            ed.putBoolean((String)p,(Boolean)datas.get(p));
                        }else if(datas.get(p) instanceof String){
                            ed.putString((String)p,(String)datas.get(p));
                        }else if(datas.get(p) instanceof Integer){
                            ed.putInt((String)p,(Integer)datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 1){
            File file = new File(FileUtil.FILEPATH+ File.separator+"force");
            if(file.exists()){
                Object o = FileUtil.readObj(file.getAbsolutePath());
                if(o!=null){
                    Map<String,Boolean>  datas = (Map<String,Boolean>)o;
                    SharedPreferences.Editor ed = act.forceStopPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p)){
                            ed.putBoolean(p,datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 2){
            act.ifwFragment.restroe();
        }else if(type == 3){
            File file = new File(FileUtil.FILEPATH+ File.separator+"package");
            if(file.exists()){
                Object o = FileUtil.readObj(file.getAbsolutePath());
                if(o!=null){
                    Map<String,Boolean>  datas = (Map<String,Boolean>)o;
                    SharedPreferences.Editor ed = act.pmPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p)){
                            ed.putBoolean(p,datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 4){
            File file = new File(FileUtil.FILEPATH+ File.separator+"doze");
            if(file.exists()){
                Object o = FileUtil.readObj(file.getAbsolutePath());
                if(o!=null){
                    Map<String,Boolean>  datas = (Map<String,Boolean>)o;
                    SharedPreferences.Editor ed = act.dozePrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p)){
                            ed.putBoolean(p,datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 6){
            for(int i = 0;i<12;i++){
                if(i == 2||i == 6){
                    continue;
                }
                restoreData(i);
            }
            File file = new File(FileUtil.FILEPATH+ File.separator+"setting");
            if(file.exists()){
                Object o = FileUtil.readObj(file.getAbsolutePath());
                if(o!=null){
                    Map<String,Object>  datas = (Map<String,Object>)o;
                    SharedPreferences.Editor ed = act.settings.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p) instanceof Boolean){
                            ed.putBoolean(p,(Boolean) datas.get(p));
                        }else if(datas.get(p) instanceof Integer){
                            ed.putInt(p,(Integer) datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 8){
            File file = new File(FileUtil.FILEPATH+ File.separator+"autostartnet");
            if(file.exists()){
                Object o = FileUtil.readObj(FileUtil.FILEPATH+ File.separator+"autostartnet");
                if(o!=null){
                    Map<String,Boolean>  datas = (Map<String,Boolean>)o;
                    SharedPreferences.Editor ed = act.autoStartNetPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p)){
                            ed.putBoolean(p,datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 9){
            File file = new File(FileUtil.FILEPATH+ File.separator+"uicontrol");
            if(file.exists()){
                Object o = FileUtil.readObj(FileUtil.FILEPATH+ File.separator+"uicontrol");
                if(o!=null){
                    Map<String,Object>  datas = (Map<String,Object>)o;
                    SharedPreferences.Editor ed = act.uiBarPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
//                        if(datas.get(p)){
//                            ed.putBoolean(p,datas.get(p));
//                        }
                        if(datas.get(p) instanceof Boolean){
                            ed.putBoolean(p,(Boolean)datas.get(p));
                        }else if(datas.get(p) instanceof String){
                            ed.putString(p,(String)datas.get(p));
                        }else if(datas.get(p) instanceof Integer){
                            ed.putInt(p,(Integer)datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 10){
            File file = new File(FileUtil.FILEPATH+ File.separator+"adjump");
            if(file.exists()){
                Object o = FileUtil.readObj(FileUtil.FILEPATH+ File.separator+"adjump");
                if(o!=null){
                    Map<String,Object>  datas = (Map<String,Object>)o;
                    SharedPreferences.Editor ed = act.adPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p) instanceof Integer){
                            ed.putInt(p,(Integer) datas.get(p));
                        }else if(datas.get(p) instanceof String){
                            ed.putString(p,(String) datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }else if(type == 11){
            File file = new File(FileUtil.FILEPATH+ File.separator+"recents");
            if(file.exists()){
                Object o = FileUtil.readObj(FileUtil.FILEPATH+ File.separator+"recents");
                if(o!=null){
                    Map<String,Boolean>  datas = (Map<String,Boolean>)o;
                    SharedPreferences.Editor ed = act.recentPrefs.edit();
                    if (datas.size()>0){
                        ed.clear().commit();
                    }
                    for(String p:datas.keySet()){
                        if(datas.get(p)){
                            ed.putBoolean(p,datas.get(p));
                        }
                    }
                    ed.commit();
                    act.showT("还原成功");
                }else{
                    act.showT("备份文件损坏");
                }
            }else{
                act.showT("备份文件不存在");
            }
        }
    }
}

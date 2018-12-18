package com.click369.controlbp.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.click369.controlbp.BuildConfig;
import com.click369.controlbp.R;
import com.click369.controlbp.bean.AppInfo;
import com.click369.controlbp.bean.AppStateInfo;
import com.click369.controlbp.bean.NavInfo;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedStopApp;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.AppLoaderUtil;
import com.click369.controlbp.util.OpenCloseUtil;
import com.click369.controlbp.util.PackageUtil;
import com.click369.controlbp.util.SharedPrefsUtil;
import com.click369.controlbp.util.ShellUtilNoBackData;
import com.click369.controlbp.util.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;


public class BaseActivity extends AppCompatActivity {
    public static boolean isPressBack = true;
    private Toast t;
    public Handler h = new Handler();
    public static Point p = new Point();
    public static boolean isZhenDong = true;
    public static boolean isUpdateAppTime = false;
    public SharedPrefsUtil sharedPrefs;
    public AppLoaderUtil appLoaderUtil;
    private static HashMap<String,Long> procTimeInfos = new HashMap<String,Long>();
    //    @TargetApi(Build.VERSION_CODES.M)
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appLoaderUtil = AppLoaderUtil.getInstance(this.getApplicationContext());
        sharedPrefs = SharedPrefsUtil.getInstance(this.getApplicationContext());
        if (!MainActivity.isUIRun){
            SharedPreferences settings = SharedPrefsUtil.getPreferences(this,Common.PREFS_APPSETTINGS);// getApplicationContext().getSharedPreferences(Common.PREFS_APPSETTINGS, Context.MODE_WORLD_READABLE);
            long lastTime = settings.getLong(Common.PREFS_SETTING_LASTUISTARTTIME,0);
            if (System.currentTimeMillis()-lastTime<500){
                showT("检测到应用控制器频繁的开启，可能由于出错导致异常");
            }
            settings.edit().putLong(Common.PREFS_SETTING_LASTUISTARTTIME,System.currentTimeMillis()).commit();
        }
        WindowManager mWindowManager = (WindowManager) getApplication().getSystemService(getApplication().WINDOW_SERVICE);
        mWindowManager.getDefaultDisplay().getRealSize(p);
        t = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        if(MainActivity.isNightMode&&!(this instanceof MainActivity)){
            setTheme(R.style.AppTheme_NoActionBarDarkAct);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if(MainActivity.isNightMode) {
                window.setStatusBarColor(getResources().getColor(R.color.darkblack));
                //底部导航栏
                window.setNavigationBarColor(Color.BLACK);
            }else{
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
                if (new File(Environment.getExternalStorageDirectory(),"zroms").exists()){
                    //底部导航栏
                    window.setNavigationBarColor(Color.WHITE);
                }
            }
        }
        super.onCreate(savedInstanceState);
    }

    public static void setProcTimeInfos(HashMap<String,Long> procTimeInfos){
        if(procTimeInfos!=null){
            BaseActivity.procTimeInfos.clear();
            BaseActivity.procTimeInfos.putAll(procTimeInfos);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public static long getProcTime(String packageName){
        if(BaseActivity.procTimeInfos.containsKey(packageName)){
            return  BaseActivity.procTimeInfos.get(packageName);
        }
        return -1;
    }
    public static String getProcTimeStr(String packageName){
        long t = getProcTime(packageName);
        if(t==-1||packageName.equals(BuildConfig.APPLICATION_ID)){
            return  "";
        }
        t =SystemClock.uptimeMillis()- t;
        return "\n后台:"+TimeUtil.changeMils2StringMin(t);
    }

    public void showT(String msg){
        if(t == null){
            t = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        }
        t.setText(msg);
        t.show();
//        startActivityFromFragment();
    }
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public void changeColor(String color){
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                Window window = this.getWindow();
//                getSupportActionBar().hide();//隐藏掉整个ActionBar，包括下面的Tabs
//                //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
//                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//                //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
//                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//                //设置状态栏颜色
//                window.setStatusBarColor(Color.parseColor(color));
//            }
//
//        }catch (Exception e){
//
//        }
//    }

//    public void changeColor(View mainView){
//        try {
//            String color = "#000000";
//            mainView.setBackgroundColor(Color.parseColor(color));
//            Window window = this.getWindow();
//            getSupportActionBar().hide();
//            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            //设置状态栏颜色
//            window.setStatusBarColor(Color.parseColor(color));
//        }catch (Exception e){
//        }
//    }

    public static float x = 0;
    public  static  int choose = 0;
    public static void addListClickListener(final ListView listView, final BaseAdapter adapter, final Activity cxt){
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                x = motionEvent.getRawX();
                return false;
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final AppInfo ai = (AppInfo)(adapter.getItem(i));
                final AppStateInfo asi = AppLoaderUtil.allAppStateInfos.containsKey(ai.packageName)?AppLoaderUtil.allAppStateInfos.get(ai.packageName):new AppStateInfo();

                String titles[] = {"启动该应用","清空该应用的所有设置","打开后定时结束该程序"};
                final boolean isRun = ai.isRunning;
                if(isRun&&(ai.isSetTimeStopApp)){
                    titles = new String[]{"启动该应用","清空该应用的所有设置","打开后定时结束该程序","取消已设置的定时结束设置","杀死该进程"};
                    choose = 0;
                }else if(isRun&&(!ai.isSetTimeStopApp)){
                    titles = new String[]{"启动该应用","清空该应用的所有设置","打开后定时结束该程序","杀死该进程"};
                    choose = 1;
                }else if(!isRun&&(ai.isSetTimeStopApp)){
                    titles = new String[]{"启动该应用","清空该应用的所有设置","打开后定时结束该程序","取消已设置的定时结束设置"};
                    choose = 2;
                }else if(!isRun&&(!ai.isSetTimeStopApp)){
                    titles = new String[]{"启动该应用","清空该应用的所有设置","打开后定时结束该程序"};
                    choose = 3;
                }
                final String t = ai.isSetTimeStopApp? ai.setTimeStopAppTime+"":"";
                AlertUtil.showListAlert(cxt, "请选择对"+ai.appName+"的操作", titles, new AlertUtil.InputCallBack() {
                    @Override
                    public void backData(String txt, int tag) {
                        if (tag == 0){
                            if(ai.activityCount>0){
                                cxt.moveTaskToBack(true);
                                if(ai.isDisable){
                                    asi.isOpenFromIceRome = true;
                                    Intent intent = new Intent("com.click369.control.pms.enablepkg");
                                    intent.putExtra("pkg",ai.getPackageName());
                                    cxt.sendBroadcast(intent);
                                    ShellUtilNoBackData.execCommand("pm "+(ai.isDisable?"enable":"disable")+" "+ai.packageName);
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        OpenCloseUtil.doStartApplicationWithPackageName(ai.packageName,cxt);
                                    }
                                },200);
                            }else{
                                Toast.makeText(cxt,"该应用没有可视界面，无法启动",Toast.LENGTH_LONG).show();
                            }
                        }else if (tag == 1){
                           AlertUtil.showConfirmAlertMsg(cxt, "确定要清空该应用的所有设置（IFW设置除外）？", new AlertUtil.InputCallBack() {
                               @Override
                               public void backData(String txt, int tag) {
                               if (tag == 1) {
                                   ((BaseActivity)cxt).sharedPrefs.clearAppSettings(ai,false);
                               }
                               }
                           });
                        }else{
                           if(tag==2){
                               String []titles1 = new String[]{"仅下次生效","永久生效"};
                               AlertUtil.showListAlert(cxt, "请选择模式", titles1, new AlertUtil.InputCallBack() {
                                   @Override
                                   public void backData(String txt, int tag) {
                                       if (tag == 0){
                                           AlertUtil.inputAlertCustomer(cxt,"请输入时长,仅生效一次（输入0可取消已设置的时间），单位：分钟","",t,new AlertUtil.InputCallBack(){
                                               @Override
                                               public void backData(String txt, int tag) {
                                                   int time = Integer.parseInt(txt);
                                                   if (time>0){
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().putInt(ai.packageName+"/one",time).commit();
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
                                                       ai.isSetTimeStopOneTime = true;
                                                       ai.setTimeStopAppTime = time;
                                                       ai.isSetTimeStopApp = true;
                                                       Toast.makeText(cxt,"当该应用打开后将会在"+time+"分钟后强行关闭",Toast.LENGTH_LONG).show();
                                                   }else{
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
                                                       ai.isSetTimeStopOneTime = false;
                                                       ai.setTimeStopAppTime = 0;
                                                       ai.isSetTimeStopApp = false;
                                                       Toast.makeText(cxt,"没有设置有效的时间",Toast.LENGTH_SHORT).show();
                                                       adapter.notifyDataSetChanged();
                                                   }
                                               }
                                           });
                                       }else{
                                           AlertUtil.inputAlertCustomer(cxt,"请输入时长（输入0可取消已设置的时间），单位：分钟","",t,new AlertUtil.InputCallBack(){
                                               @Override
                                               public void backData(String txt, int tag) {
                                                   int time = Integer.parseInt(txt);
                                                   if (time>0){
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().putInt(ai.packageName+"/long",time).commit();
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();
                                                       ai.isSetTimeStopOneTime = false;
                                                       ai.setTimeStopAppTime = time;
                                                       ai.isSetTimeStopApp = true;
                                                       Toast.makeText(cxt,"当该应用每次打开后将会在"+time+"分钟后强行关闭",Toast.LENGTH_LONG).show();
                                                   }else{
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
                                                       SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();
                                                       ai.isSetTimeStopOneTime = false;
                                                       ai.setTimeStopAppTime = 0;
                                                       ai.isSetTimeStopApp = false;
                                                       Toast.makeText(cxt,"没有设置有效的时间",Toast.LENGTH_SHORT).show();
                                                       adapter.notifyDataSetChanged();
                                                   }
                                               }
                                           });
                                       }
                                   }
                               });
                            }else  if ((tag==3&&choose == 1)||(tag==4&&choose == 0)){
                               XposedStopApp.stopApk(ai.packageName, cxt);
                               ai.isRunning = false;
                               adapter.notifyDataSetChanged();
                           }else if(tag==3&&(choose == 0||choose == 2)){
                               ai.setTimeStopAppTime = 0;
                               ai.isSetTimeStopApp = false;
                               ai.isSetTimeStopOneTime= false;
                               SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/long").commit();
                               SharedPrefsUtil.getInstance(cxt).setTimeStopPrefs.edit().remove(ai.packageName+"/one").commit();

                               adapter.notifyDataSetChanged();
                           }
                        }
                    }
                });
                return true;
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (x>p.x/4){
                    return;
                }
                AppInfo ai = (AppInfo)(adapter.getItem(i));
                ArrayList<NavInfo> navInfos = new ArrayList<NavInfo>();
                navInfos.add(new NavInfo(0,"禁用服务",null,ai.isServiceStop));
                navInfos.add(new NavInfo(0,"禁用广播",null,ai.isBroadStop));
                navInfos.add(new NavInfo(0,"禁唤醒锁",null,ai.isWakelockStop));
                navInfos.add(new NavInfo(0,"禁定时器",null,ai.isAlarmStop));

                navInfos.add(new NavInfo(1,"返回时动作",ai.isBackForceStop?"强退":ai.isBackMuBei?"墓碑":"无",false));
                navInfos.add(new NavInfo(1,"后台时动作",ai.isHomeMuBei?"墓碑":ai.isHomeIdle?"待机":"无",false));
                navInfos.add(new NavInfo(1,"熄屏时动作",ai.isOffscForceStop?"强退":ai.isOffscMuBei?"墓碑":"无",false));
                navInfos.add(new NavInfo(0,"通知排除",null,ai.isNotifyNotExit));

                navInfos.add(new NavInfo(1,"IFW服务禁用个数",ai.serviceDisableCount+"",false));
                navInfos.add(new NavInfo(1,"IFW广播禁用个数",ai.broadCastDisableCount+"",false));
                navInfos.add(new NavInfo(1,"IFW活动禁用个数",ai.activityDisableCount+"",false));

                navInfos.add(new NavInfo(0,"最近任务保留",null,ai.isRecentNotClean));
                navInfos.add(new NavInfo(0,"卡片移除杀死",null,ai.isRecentForceClean));
                navInfos.add(new NavInfo(0,"最近任务模糊",null,ai.isRecentBlur));
                navInfos.add(new NavInfo(0,"最近任务隐藏",null,ai.isRecentNotShow));

                navInfos.add(new NavInfo(0,"启动时指纹验证",null,ai.isLockApp));
                navInfos.add(new NavInfo(0,"不允许启动程序",null,ai.isStopApp));
                navInfos.add(new NavInfo(0,"不允许自动启动",null,ai.isAutoStart));
                navInfos.add(new NavInfo(0,"启动后常驻内存",null,ai.isNotStop));

                navInfos.add(new NavInfo(0,"打盹亮屏白名单",null,ai.isDozeOnsc));
                navInfos.add(new NavInfo(0,"打盹熄屏白名单",null,ai.isDozeOffsc));
                navInfos.add(new NavInfo(0,"启动时暂停打盹",null,ai.isDozeOpenStop));

                navInfos.add(new NavInfo(0,"是否冻结",null,ai.isDisable));
                navInfos.add(new NavInfo(0,"阻止卸载",null,ai.isNotUnstall));

                navInfos.add(new NavInfo(0,"是否染色",null,ai.isBarColorList));
                navInfos.add(new NavInfo(0,"锁定染色",null,ai.isBarLockList));

                navInfos.add(new NavInfo(0,"跳过广告",null,ai.isADJump));
                if(ai.instanllTime>0){
                    navInfos.add(new NavInfo(1,"安装时间",TimeUtil.changeMils2String(ai.instanllTime,"yyyy-MM-dd HH:mm"),false));
                    navInfos.add(new NavInfo(1,"更新时间",TimeUtil.changeMils2String(ai.updateTime,"yyyy-MM-dd HH:mm"),false));
                }
                if(ai.lastOpenTime>0){
                    navInfos.add(new NavInfo(1,"上次打开",TimeUtil.changeMils2String(ai.lastOpenTime,"yyyy-MM-dd HH:mm"),false));
                    navInfos.add(new NavInfo(1,"打开次数",ai.openCount+"",false));
                }
                MainActivity ma = (MainActivity)cxt;
                ma.navInfoLL.setVisibility(View.VISIBLE);

                ma.navInfoTitle.setText("对<"+ai.appName+">的控制详情");
                ma.navInfoSamllTitle.setText(ai.packageName+"\n版本:"+ai.versionName+" 版本号:"+ai.versionCode);
//                ma.navInfoContent.setText(sb.toString());
                ma.navAdapter.setData(navInfos);
                if (!ma.drawer.isDrawerOpen(GravityCompat.START)) {
                    ma.drawer.openDrawer(GravityCompat.START);
                }
//                Intent intent = new Intent("com.click369.control.ams.getprocinfo");
//                intent.putExtra("pkg",ai.packageName);
//                cxt.sendBroadcast(intent);
            }
        });
    }

    public static void zhenDong(Context c){
        if (isZhenDong) {
            Vibrator vibrator = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            long [] pattern = {20,20};   // 停止 开启 停止 开启
            vibrator.vibrate(pattern,-1);
        }
    }

    public static void sendBroadAMSChangeControl(Context cxt){
        Intent intent = new Intent("com.click369.control.ams.reloadcontrol");
        cxt.sendBroadcast(intent);
    }

    public static void sendBroadAMSChangeMuBei(Context cxt){
        Intent intent = new Intent("com.click369.control.ams.reloadmubei");
        cxt.sendBroadcast(intent);
    }

    public static void sendBroadAMSChangeAutoStart(Context cxt){
        Intent intent = new Intent("com.click369.control.ams.reloadautostart");
        cxt.sendBroadcast(intent);
    }

    public static void sendBroadAMSRemovePkg(Context cxt,String pkg){
        Intent intent = new Intent("com.click369.control.ams.removemubei");
        intent.putExtra("apk",pkg);
        cxt.sendBroadcast(intent);
    }


}

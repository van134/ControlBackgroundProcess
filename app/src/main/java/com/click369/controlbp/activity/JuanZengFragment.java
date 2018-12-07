package com.click369.controlbp.activity;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.didikee.donate.AlipayDonate;
import android.didikee.donate.WeiXinDonate;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.util.AlertUtil;
import com.click369.controlbp.util.FileUtil;
import com.click369.controlbp.util.SharedPrefsUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class JuanZengFragment extends BaseFragment {
    private TextView zfbTv,wxTv,zklTv;
    private TextView arQuaTv;
    private LinearLayout arFL;
    private Switch arSw;
    private SharedPreferences settingPrefs;
    private AlertDialog dialog;
    public JuanZengFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_jz, container, false);
        zfbTv = (TextView)v.findViewById(R.id.jz_zfb);
        wxTv = (TextView)v.findViewById(R.id.jz_wx);
        zklTv = (TextView)v.findViewById(R.id.jz_zkl);
        arSw = (Switch) v.findViewById(R.id.ar_change_sw);
        arQuaTv = (TextView)v.findViewById(R.id.ar_qua_grivaty);
        arFL = (LinearLayout) v.findViewById(R.id.ar_fl);
        arSw.setTextColor(arQuaTv.getCurrentTextColor());
        zfbTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager)getActivity(). getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText("418560128@qq.com");
               Toast.makeText(getContext(),"支付宝账号已复制到你的粘贴板",Toast.LENGTH_LONG).show();
            }
        });
        wxTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText("Van418");
                Toast.makeText(getContext(),"微信账号已复制到你的粘贴板",Toast.LENGTH_LONG).show();
            }
        });
//        zklTv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ClipboardManager cm = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                // 将文本内容放到系统剪贴板里。
//                cm.setText("3kGZ0948kN");
//                Toast.makeText(getContext(),"支口令已复制到你的粘贴板,请打开支付宝领取红包",Toast.LENGTH_LONG).show();
//            }
//        });

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N){
            arFL.setVisibility(View.GONE);
        }
        settingPrefs = SharedPrefsUtil.getInstance(getActivity()).settings;//SharedPrefsUtil.getPreferences(this.getActivity(), Common.PREFS_APPSETTINGS);//getActivity().getSharedPreferences(Common.PREFS_APPSETTINGS,Context.MODE_WORLD_READABLE);
        arSw.setChecked(settingPrefs.getBoolean("archange",false));
        arQuaTv.setEnabled(arSw.isChecked());
        arQuaTv.setAlpha(arSw.isChecked()?1.0f:0.6f);
        arSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                settingPrefs.edit().putBoolean("archange",b).commit();
                arQuaTv.setEnabled(b);
                arQuaTv.setAlpha(arSw.isChecked()?1.0f:0.6f);
            }
        });
        arQuaTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = "AR图像质量:低(360*640)\n(性能差的调低不然很卡)";
                int size = settingPrefs.getInt("arsize", 1);
                if(size == 1){
                    settingPrefs.edit().putInt("arsize",2).commit();
                    s = "AR图像质量:中(720*1080)\n(性能差的调低不然很卡)";
                }else if(size == 2){
                    settingPrefs.edit().putInt("arsize",3).commit();
                    s = "AR图像质量:高(1080*1920)\n(性能差的调低不然很卡)";
                }else if(size == 3){
                    settingPrefs.edit().putInt("arsize",1).commit();
                    s = "AR图像质量:低(360*640)\n(性能差的调低不然很卡)";
                }
                arQuaTv.setText(s);
            }
        });
        int size = settingPrefs.getInt("arsize", 1);
        String s ="";
        if(size ==2){
            s = "AR图像质量:中(720*1080)\n(性能差的调低不然很卡)";
        }else if(size == 3){
            s = "AR图像质量:高(1080*1920)\n(性能差的调低不然很卡)";
        }else if(size == 1){
            s = "AR图像质量:低(360*640)\n(性能差的调低不然很卡)";
        }
        arQuaTv.setText(s);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setTitle("提示！");
        builder.setMessage("如果喜欢我的作品可以选择捐赠");
        builder.setNeutralButton("不捐赠",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton("支付宝捐赠", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                donateAlipay("a6x02254itpf8g4rys71ta1");
            }
        });
        builder.setNegativeButton("微信捐赠",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlertUtil.showAlertMsgBack(getActivity(), "提示","进入扫一扫界面，点击右上角菜单按钮选择从相册选取二维码，选择相册的第一张照片即可",new AlertUtil.InputCallBack(){
                    @Override
                    public void backData(String txt, int tag) {
                        String qrPath = FileUtil.ROOTPATH + File.separator+"wx.jpg";
                        File f = new File(qrPath);
                        if (f.exists()){
                            f.delete();
                        }
                        donateWeixin();
                    }
                });
            }
        });
        dialog = builder.create();
        dialog.show();
        return v;
    }
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            dialog.show();
        }
    }
    /**
     * 需要提前准备好 微信收款码 照片，可通过微信客户端生成
     */
    private void donateWeixin() {
        try {
            InputStream weixinQrIs = getResources().getAssets().open("wx.jpg");
            String qrPath = FileUtil.ROOTPATH + File.separator+"wx.jpg";
            WeiXinDonate.saveDonateQrImage2SDCard(qrPath, BitmapFactory.decodeStream(weixinQrIs));
            WeiXinDonate.donateViaWeiXin(getActivity(), qrPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 支付宝支付
     * @param payCode 收款码后面的字符串；例如：收款二维码里面的字符串为 https://qr.alipay.com/stx00187oxldjvyo3ofaw60 ，则
     *                payCode = stx00187oxldjvyo3ofaw60
     *                注：不区分大小写
     */
    private void donateAlipay(String payCode) {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(getActivity());
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(getActivity(), payCode);
        }
    }
}

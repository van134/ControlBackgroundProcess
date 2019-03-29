package com.click369.controlbp.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.click369.controlbp.R;
import com.click369.controlbp.common.Common;
import com.click369.controlbp.service.LightView;
import com.click369.controlbp.service.ScreenLightServiceUtil;
import com.click369.controlbp.service.WatchDogService;
import com.click369.controlbp.service.XposedToast;
import com.click369.controlbp.util.Notify;
import com.click369.controlbp.util.ShellUtils;

import net.qiujuer.genius.blur.StackBlur;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;

/**
 * Created by van on 2017/5/27.
 */
public class ChangePhotoActivity extends BaseActivity{
    private TextView bgBlurTv,bgBrightTv;
    private SeekBar bgBlurSb,bgBrightSb;
    private ImageView imageView;
    private int curColor= Color.BLACK;
    private int alpha = 100,blur = 0;
    private File file,blurFile;
    private String key_blur;
    private String key_bright;
    private String key_fileName;
    private boolean isChange;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changeimg);
        key_blur = getIntent().getStringExtra("key_blur");
        key_bright = getIntent().getStringExtra("key_bright");
        key_fileName = getIntent().getStringExtra("key_fileName");
        blur = getIntent().getIntExtra("blur",0);
        alpha = getIntent().getIntExtra("bright",100);
        file = new File(key_fileName);
        blurFile = new File(key_fileName+"_blur");

        bgBlurTv = (TextView) findViewById(R.id.ui_bg_blurvalue);
        bgBrightTv = (TextView) findViewById(R.id.ui_bg_brightvalue);
        bgBlurSb = (SeekBar) findViewById(R.id.ui_bg_blursb);
        bgBrightSb = (SeekBar) findViewById(R.id.ui_bg_brightsb);
        imageView = (ImageView) findViewById(R.id.ui_img);
//        blur  = sharedPrefs.uiBarPrefs.getInt(key_blur,0);
//        alpha = sharedPrefs.uiBarPrefs.getInt(key_bright,100);
        bgBlurSb.setProgress(blur);
        bgBrightSb.setProgress(alpha);
        bgBlurTv.setText("图片模糊度:"+blur+"%");
        bgBrightTv.setText("图片不透明度:"+alpha+"%");
        this.setTitle("背景图片处理");
        bgBlurSb.setTag(0);
        bgBrightSb.setTag(1);
        SeekBarListener sbl = new SeekBarListener();
        bgBrightSb.setOnSeekBarChangeListener(sbl);
        bgBlurSb.setOnSeekBarChangeListener(sbl);

        if(!file.exists()){
            showT("图片不存在,请选择后再进行处理");
            finish();
        }else{
            File tempFile = null;
            if(blurFile.exists()){
                tempFile = blurFile;
            }else{
                tempFile = file;
            }
            Drawable d = Drawable.createFromPath(tempFile.getAbsolutePath());
            d.setAlpha((int)(alpha*2.55));
            imageView.setImageDrawable(d);
        }
    }
    class SeekBarListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int tag= (Integer) seekBar.getTag();
            String names[] = {"图片模糊度:","图片透明度:"};
            if (tag==0){
                bgBlurTv.setText(names[tag]+seekBar.getProgress()+"%");
            }else if (tag==1){
                bgBrightTv.setText(names[tag]+seekBar.getProgress()+"%");
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int tag= (Integer) seekBar.getTag();
//            String keys[] = {key_blur,key_bright};
//            sharedPrefs.uiBarPrefs.edit().putInt(keys[tag],seekBar.getProgress()).commit();
            BaseActivity.zhenDong(ChangePhotoActivity.this);
            if(tag == 0){
                blur = seekBar.getProgress();
            }else if(tag == 1){
                alpha = seekBar.getProgress();
            }
            isChange = true;
            Drawable d = changeBgBlur(file,blurFile,blur,alpha);
            imageView.setImageDrawable(d);
        }
    }

    @Override
    public void backClick() {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        if(isChange){
            Intent intent = new Intent();
            intent.putExtra("key_fileName",key_fileName);
            intent.putExtra("blur",blur);
            intent.putExtra("bright",alpha);
            intent.putExtra("key_blur",key_blur);
            intent.putExtra("key_bright",key_bright);
            setResult(0x15,intent);
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    public static Drawable changeBgBlur(final File file,final File blurFile,final int blur,final int alpha){
        if(file.exists()){
            try {
                Drawable d = null;
                if(blur>0){
                    Bitmap bitmap = StackBlur.blurNativelyPixels(BitmapFactory.decodeFile(file.getAbsolutePath()),blur, false);
                    FileOutputStream fos = new FileOutputStream(blurFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG,95,fos);
                    d = Drawable.createFromPath(blurFile.getAbsolutePath());
                }else{
                    if(blurFile.exists()){
                        blurFile.delete();
                    }
                    d = Drawable.createFromPath(file.getAbsolutePath());
                }
                d.setAlpha((int)(alpha*2.55));
                return d;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
    public static Bitmap changeBgBlurBM(final File file,final File blurFile,final int blur,final int alpha){
        if(file.exists()){
            try {
                Drawable d = null;
                if(blur>0){
                    Bitmap bitmap = StackBlur.blurNativelyPixels(BitmapFactory.decodeFile(file.getAbsolutePath()),blur, false);
                    return bitmap;
                }else{
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    return bitmap;
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }


}

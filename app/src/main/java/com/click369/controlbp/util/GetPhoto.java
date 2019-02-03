package com.click369.controlbp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class GetPhoto {
	public static final int CODE_GALLERY_REQUEST = 0xa0;// 从SD中得到照片
	public static final int CODE_CAMERA_REQUEST = 0xa1;// 拍摄照片
	public static final int CODE_RESULT_REQUEST = 0xa2;//裁剪
	public File photofile;//头像要保存的文件
	public boolean isNeedCrop = true;//是否需要裁剪
	private Activity act;
	private int x = 9,y = 16;
	public GetPhoto(Activity act){
		this.act = act;
	}
	
	//用户名  用来设定图片路径
	public void setPhotofile(File photofile){
		this.photofile = photofile;
//		if(photofile.exists()){
//			photofile.delete();
//		}
	}
	public void setScale(int type){
		if(type == 0){
			x = 9;
			y = 14;
		}else if(type ==1){
			x = 6;
			y = 17;
		}else if(type ==2){
			x = 5;
			y = 1;
		}

	}
	//是否需要裁剪
	public void setIsNeedCrop(boolean isNeedCrop){
		this.isNeedCrop = isNeedCrop;
	}
	
	//用来选择图片的对话框
	public void showPhotoDialog(){
		Builder dialog = new Builder(act);
		dialog.setTitle("选择设定头像方式");
		String items[]= new String[] {"相机","相册"};
		dialog.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==0){
					photoWithCamera();
				}else{
					photoWithGrelly();
				}
			}
		});
		dialog.show();
	}
	
	//相机
	public void photoWithCamera(){
		if(photofile==null){
			Toast.makeText(act, "没有设置头像文件", Toast.LENGTH_SHORT).show();
			return;
		}
		//创建打开相机意图
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//创建一个空文件用来存放生成的照片
		i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photofile)); 
		//启动隐式意图
		act.startActivityForResult(i, CODE_CAMERA_REQUEST);
	}

	//相册
	public void photoWithGrelly(){
		if(photofile==null){
			Toast.makeText(act, "没有设置头像文件", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent();
		/* 开启Pictures画面Type设定为image */
		intent.setType("image/*");
		/* 使用Intent.ACTION_GET_CONTENT这个Action */
		intent.setAction(Intent.ACTION_GET_CONTENT);
		/* 取得相片后返回本画面 */
		act.startActivityForResult(intent, CODE_GALLERY_REQUEST);
	}

//	Uri uri;
	/**
	 * 裁剪原始的图片
	 */
	public void cropRawPhoto(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		intent.putExtra("scale", true);
		intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		// aspectX , aspectY :宽高的比例
		intent.putExtra("aspectX", x);
		intent.putExtra("aspectY", y);
		// outputX , outputY : 裁剪图片宽高
//		intent.putExtra("outputX", 900);
//		intent.putExtra("outputY", 1600);
//		intent.putExtra("outputX", 450);
//		intent.putExtra("outputY", 800);
		intent.putExtra("return-data", false);
//		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photofile));
		act.startActivityForResult(intent, CODE_RESULT_REQUEST);
	}
	
	//返回结果  在所在的Activity中的onActivityResult函数中调用
	public File onActivityResult(int arg0, int arg1, Intent intent) {
		if(arg1 == Activity.RESULT_OK){
			if(arg0 == CODE_CAMERA_REQUEST){//相机返回数据
				if(isNeedCrop){
					cropRawPhoto(Uri.fromFile(photofile));
				}else{
//					FileUtil.writeFile(photofile.getAbsolutePath(), Bitmap2Bytes(decodeFile(photofile)));
				}
			}else if(arg0 == CODE_GALLERY_REQUEST){//相册返回数据
				if (intent.getData()!= null){
					Log.i("wall","1 ");
					if(isNeedCrop){
						cropRawPhoto(intent.getData());
					}else{
						Bitmap bitmap = getBitmapFormUri(act, intent.getData());
						FileUtil.writeFile(photofile.getAbsolutePath(), Bitmap2Bytes(bitmap));
					}
				}else if (intent.getExtras() != null){
					Log.i("wall","2 ");
					Bitmap bitmap = intent.getExtras().getParcelable("data");
					if(isNeedCrop){
						cropRawPhoto(Uri.parse(MediaStore.Images.Media.insertImage(act.getContentResolver(), bitmap, null,null)));
					}else{
						FileUtil.writeFile(photofile.getAbsolutePath(), Bitmap2Bytes(bitmap));
					}
				}
			}else if(arg0 == CODE_RESULT_REQUEST){//裁剪返回数据
				Log.i("wall",intent.toString());
				if (intent != null) {
					try {
						if(!photofile.exists()||photofile.length()==0){
//							Bitmap bm = BitmapFactory.decodeStream(act.getContentResolver().openInputStream(uri));
							Bitmap bm = intent.getExtras().getParcelable("data");
							Log.i("wall","1 "+bm.getWidth()+"   "+photofile.length());
							FileUtil.writeFile(photofile.getAbsolutePath(),Bitmap2Bytes(bm));
							Log.i("wall","1 "+bm.getWidth()+"   "+photofile.length());
						}else{
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			return photofile;
		}else{
			Toast.makeText(act, "获取失败", Toast.LENGTH_SHORT).show();
			return null;
		}
	}
	
	//从uri中获取图片的bitmap
	public  Bitmap getBitmapFormUri(Activity ac, Uri uri) {
		Bitmap bitmap = null;
		try {
			InputStream input = ac.getContentResolver().openInputStream(uri);
			Options onlyBoundsOptions = new Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			onlyBoundsOptions.inDither = true;// optional
			onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
			input.close();

			int originalWidth = onlyBoundsOptions.outWidth;
			int originalHeight = onlyBoundsOptions.outHeight;
			Log.i("wall",originalWidth+"  "+originalHeight);
			if ((originalWidth == -1) || (originalHeight == -1))
				return null;
			float hh = 800f;//
			float ww = 480f;//
			int be = 1;//
//			if (originalWidth > originalHeight && originalWidth > ww) {
//				be = (int) (originalWidth / ww);
//			} else if (originalWidth < originalHeight && originalHeight > hh) {
//				be = (int) (originalHeight / hh);
//			}
//			if (be <= 0)
//				be = 1;
			Options bitmapOptions = new Options();
			bitmapOptions.inSampleSize = be;
			bitmapOptions.inDither = true;
			bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// optional
			input = ac.getContentResolver().openInputStream(uri);
			bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		return compressImage(bitmap);
		return  bitmap;
	}

	//改变Bitmap大小
//	public Bitmap compressImage(Bitmap image) {
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//		int options = 100;
//		while (baos.toByteArray().length / 1024 > 100) {
//			baos.reset();
//			image.compress(Bitmap.CompressFormat.JPEG, options, baos);
//			options -= 10;
//		}
//		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
//		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
//		return bitmap;
//	}
	
	
	//改变图片文件大小
//	public  Bitmap decodeFile(File f) {
//	    Bitmap b = null;
//	    try {
//	       Bitmap bb = BitmapFactory.decodeFile(f.getAbsolutePath());
//	       int width = bb.getWidth();
//	       int height = bb.getHeight();
//	       if (width > 300 || height > 300) {
//	           double bi = ((double) height / (double) width);
//	           b = Bitmap.createScaledBitmap(bb, 300, (int) (bi * 300), true);
//	           }
//	        } catch (Exception e) {
//	            e.printStackTrace();
//	        }
//	        return b;
//	}
	
	//把bitmap转为字节数组
	public  byte[] Bitmap2Bytes(Bitmap bm) {
	    //字节数组流：
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
	    return baos.toByteArray();
	}
}

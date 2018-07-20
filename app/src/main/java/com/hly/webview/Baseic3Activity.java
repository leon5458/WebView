package com.hly.webview;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Setting;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * ~~~~~~文件描述:~~~~~~
 * ~~~~~~作者:huleiyang~~~~~~
 * ~~~~~~创建时间:2018/7/20~~~~~~
 * ~~~~~~更改时间:2018/7/20~~~~~~
 * ~~~~~~版本号:1~~~~~~
 */
public class Baseic3Activity extends AppCompatActivity {
    WebView mWebView;

    // 拍照回传码
    public final static int CAMERA_REQUEST_CODE = 0;
    // 相册选择回传吗
    public final static int GALLERY_REQUEST_CODE = 1;
    //Uri获取类型判断
    public final int TYPE_TAKE_PHOTO = 1;


    // 拍照的照片的存储位置
    private String mTempPhotoPath;
    // 照片所在的Uri地址
    private Uri imageUri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.basic_layout);

        mWebView = findViewById(R.id.webview);

        WebSettings webSettings = mWebView.getSettings();

        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);

        // 通过addJavascriptInterface()将Java对象映射到JS对象
        //参数1：Javascript对象名
        //参数2：Java对象名
        mWebView.addJavascriptInterface(new AndroidtoJs(), "test");//AndroidtoJS类对象映射到js的test对象

        // 加载JS代码
        // 格式规定为:file:///android_asset/文件名.html
        mWebView.loadUrl("file:///android_asset/javascript.html");

    }


    public class AndroidtoJs {
        // 定义JS需要调用的方法
        // 被JS调用的方法必须加入@JavascriptInterface注解
        @JavascriptInterface
        public void hello(String msg) {
//            System.out.println("JS调用了");

            AndPermission.with(Baseic3Activity.this)
                    .runtime()
                    .permission(Manifest.permission.CAMERA)
                    .rationale(new RuntimeRationale())
                    .onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            if (Build.VERSION.SDK_INT >= 24) {
                                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                Uri photoUri = get24MediaFileUri(TYPE_TAKE_PHOTO);
                                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(takeIntent, CAMERA_REQUEST_CODE);
                            } else {
                                Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                Uri photoUri = getMediaFileUri(TYPE_TAKE_PHOTO);
                                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                startActivityForResult(takeIntent, CAMERA_REQUEST_CODE);
                            }
                        }
                    })
                    .onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            if (AndPermission.hasAlwaysDeniedPermission(Baseic3Activity.this, data)) {
                                showSettingDialog(Baseic3Activity.this, data);
                            }
                        }
                    }).start();


        }
    }


    private Uri get24MediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "external_files");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        } else {
            return null;
        }
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", mediaFile);
    }

    private Uri getMediaFileUri(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "external_files");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        //创建Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == TYPE_TAKE_PHOTO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".png");
        } else {
            return null;
        }
        return Uri.fromFile(mediaFile);

    }

    //当拍摄照片完成时会回调到onActivityResult 在这里处理照片的裁剪
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_REQUEST_CODE: {
                    if (data != null) {
                        if (data.hasExtra("data")) {
                            Bitmap bitmap = data.getParcelableExtra("data");
//                            imageView.setImageBitmap(bitmap);//imageView即为当前页面需要展示照片的控件，可替换
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= 24) {
                            Bitmap bitmap = null;
                            try {
                                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
//                            imageView.setImageBitmap(bitmap);
                        } else {
                            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.getPath());
//                            imageView.setImageBitmap(bitmap);
                        }
                    }

                    break;

                }
                case GALLERY_REQUEST_CODE: {
                    // 获取图片
                    try {
                        imageUri = data.getData();
                        if (imageUri != null) {
                            Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
//                            imageView.setImageBitmap(bit);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showSettingDialog(Baseic3Activity basic3Activiy, List<String> data) {
        List<String> permissionNames = com.yanzhenjie.permission.Permission.transformText(basic3Activiy, data);
        String message = basic3Activiy.getString(R.string.message_permission_always_failed, TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(basic3Activiy)
                .setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    private void setPermission() {
        AndPermission.with(this)
                .runtime()
                .setting()
                .onComeback(new Setting.Action() {
                    @Override
                    public void onAction() {
                        Toast.makeText(Baseic3Activity.this, R.string.message_setting_comeback, Toast.LENGTH_SHORT).show();
                    }
                })
                .start();
    }

}

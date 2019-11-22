package com.example.mycamera;


import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MainActivity extends AppCompatActivity {
    /*宣告*/
                /*Intent*/
    Intent galleryIntent;
                /*static int */
    private static final int REQUEST_CAMERA_PERMISSION = 200;
                /*按鈕*/
    ImageButton cameraBtn = null;
    ImageButton galleryBtn = null;
    /*全螢幕*/
    @Override
    public void onWindowFocusChanged(boolean hasFocas){
        super.onWindowFocusChanged(hasFocas);
        View decorView= getWindow().getDecorView();
        if(hasFocas){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

    }
    @Override
    /*授權*/
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CAMERA_PERMISSION){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"在沒有授權之前你不能使用相機",Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override



    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**********詢問授權**********/

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },REQUEST_CAMERA_PERMISSION);
            return;
        }

        /**********View ID**********/

        cameraBtn=findViewById(R.id.cameraBtn);
        galleryBtn=findViewById(R.id.galleryBtn);

        /**********相機按鈕**********/

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CameraActivity.class);
                startActivity(intent);
            }
        });

        /**********圖庫按鈕**********/

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                galleryIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(galleryIntent,10);
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode){

            case 10 :
                if (resultCode==RESULT_OK)
                {
                    if(data!=null){

                        Uri uri =data.getData();
                        String path =getPathFromUri.getPathFromUri(this,uri);/*from class getPath form Uri*/
                        Log.i("gallery get uri","Uri="+uri.toString());
                        Log.i("gallery get path ","Path="+ path);
                        Toast.makeText(MainActivity.this, "real path " + path, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ImageSettingForGallery.class);
                        intent.putExtra("dataPath", path);
                        if (Build.VERSION.SDK_INT < 19) {
                            this.getWindow().getDecorView().setSystemUiVisibility(View.GONE);
                        } else {
                            View decorView = getWindow().getDecorView();
                            int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                            decorView.setSystemUiVisibility(uiOptions);
                        }
                        startActivity(intent);
                    }
                }
                break;
        }
    }

}

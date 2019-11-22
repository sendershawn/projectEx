package com.example.mycamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ImageSettingForGallery extends AppCompatActivity implements AsyncResponse {
    String dataPath;
    String message;

    ImageView myPhoto;

    ImageButton uploadBtn;
    ImageButton rotationBtn;
    int rotation=0;


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
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_setting_for_gallery);

        Bundle bundle = this.getIntent().getExtras();
        dataPath = bundle.getString("dataPath");

        myPhoto=findViewById(R.id.myPhoto);
        rotationBtn =findViewById(R.id.turnBtn);
        uploadBtn=findViewById(R.id.uploadBtn);
        myPhoto.setImageBitmap(getBitmap(dataPath));



        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });


        rotationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               rotation+=90;
               if(rotation==360)rotation=0;
                myPhoto.setRotation(rotation);

            }
        });

        Log.i("dataGallery",dataPath);
    }

    private static Bitmap getBitmap(String file)
    {
        try {
            Bitmap temp = BitmapFactory.decodeFile(file);
            return temp;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /************接口**********/

    public void processFinish(String output ) {
        message=output;
        Log.d("正確信息:message",message);
        Intent intent = new Intent(ImageSettingForGallery.this, emotionDetect.class);
        intent.putExtra("dataPath", dataPath);
        intent.putExtra("message", message);

        startActivity(intent);
    }

    private void upload() {

        UploadAsycTask uploadAsyncTask = new UploadAsycTask(ImageSettingForGallery.this, dataPath);
        uploadAsyncTask.delegate = this;
        uploadAsyncTask.execute();
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
    }
}

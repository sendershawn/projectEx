package com.example.mycamera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

/*****縮放套件*****/

import com.example.mycamera.GestureViewBinder.GestureViewBinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


public class emotionDetect extends AppCompatActivity {

    /**********全螢幕**********/

    @Override
    public void onWindowFocusChanged(boolean hasFocas){
        super.onWindowFocusChanged(hasFocas);
        View decorView= getWindow().getDecorView();
        myPhotoHeight = myPhoto.getHeight();
        myPhotoWidth = myPhoto.getWidth();
        if(hasFocas){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    /***********資料傳值變數**********/

    String dataPath;
    String message;

    /**********View類**********/

    Context mContext;
    ImageView myPhoto;
    ImageView textImage;
    ImageButton savePhoto;
    ImageButton setSticker;
    FrameLayout groupView;
    Drawable frontPic;

    GestureViewBinder bind;


    /*********位置及大小參數**********/
    /*********** textImageView (Sticker) ***********/
    int positionX;
    int positionY;
    float textImageScale=1;//比例尺
    int textImageHeight;
    int textImageWidth;
    /*********** myPhoto (照片) ***********/
    int myPhotoHeight;
    int myPhotoWidth;
    float myPhotoHeightScale=1;//比例尺
    float myPhotoWidthScale=1;//比例尺

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);

        /***********圖片及message**********/
        Bundle bundle = this.getIntent().getExtras();
        dataPath = bundle.getString("dataPath");
        message = bundle.getString("message");

        /*********按鈕***********/
        textImage=(ImageView) findViewById(R.id.TextImage);
        setSticker= (ImageButton) findViewById(R.id.setSticker);
        savePhoto = (ImageButton)findViewById(R.id.saveBtn);

        /**********image and view**********/

        mContext=this;
        myPhoto = findViewById(R.id.myPhoto);
        groupView=findViewById(R.id.groupView);
        /**********initial**********/

        myPhoto.setImageBitmap(getBitmap(dataPath));
        textImage.setVisibility(View.GONE);




        Toast.makeText(emotionDetect.this, dataPath +"----------"+message, Toast.LENGTH_SHORT).show();

        /**********繪圖按鈕**********/

        savePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**********計算textImage 與 螢幕比例 如果跑版把 這行註解 並且 把myPhoto 的ScaleType 設為 matrix**********/

                setScreenScale();// 如果跑版註解這行

                /*************合圖實作**********/

                Bitmap bigImage = ((BitmapDrawable)myPhoto.getDrawable()).getBitmap();
                Bitmap smallImage =getResizedBitmap(((BitmapDrawable)frontPic).getBitmap(),textImageWidth,textImageHeight);
                Bitmap mergedImages = createSingleImageFromMultipleImages(bigImage, smallImage);
                myPhoto.setImageBitmap(mergedImages);

                saveFile(mergedImages);//存檔

                Toast.makeText(emotionDetect.this,"saved", Toast.LENGTH_LONG).show();

                Log.i("ScaleForScreen", myPhotoHeight + "");
                Log.i("ScaleForScreen", myPhotoWidth + "");
                Log.i("ScaleForScreen", myPhotoHeightScale + "");
                Log.i("ScaleForScreen", myPhotoWidthScale + "");

            }
        });


        /***********貼圖按鈕**********/

        setSticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textImage.setVisibility(View.VISIBLE);

                /**********根據表情設定貼圖**********/

                setSticker();

                /**********初始化長寬**********/

                initTextImageViewSize();

                /************縮放套件 啟動**********/

                bind = GestureViewBinder.bind(mContext, groupView, textImage);
                bind.setFullGroup(false);

                /**********縮放監聽器************/

                bind.setOnScaleListener(new GestureViewBinder.OnScaleListener() {
                    @Override
                    public void onScale(float scale) {

                        textImageScale=scale;//存比例

                        /*********存改變時的textImageView大小********/

                        textImage.post(new Runnable() {
                            @Override
                            public void run() {
                                textImageHeight=textImage.getHeight();
                                textImageWidth=textImage.getWidth();

                            }
                        });

                     /*   Log.i("動態比例", scale + "");
                        Log.i("動態比例", textImageHeight*scale + "");
                        Log.i("動態比例", textImageWidth*scale + "");*/

                    }
                });

              /*  Log.i("myPhotoScale(X,Y)", myPhotoHeight+ "");
                Log.i("myPhotoScale(X,Y)", myPhotoWidth+ "");
                Log.i("初始TextImage 長寬", textImageHeight + "");
                Log.i("初始TextImage 長寬", textImageWidth + "");*/

            }
        });
    }

    /**********合圖**********/

    private Bitmap createSingleImageFromMultipleImages(Bitmap firstImage, Bitmap secondImage){

        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);

        /**********設定 textImage (Sticker)  XY*********/

        int[] locations = new int[2];
        textImage.getLocationOnScreen(locations);
        positionX = locations[0];
        positionY= locations[1];
        Log.i("TAG(X,Y)", positionX + "");
        Log.i("TAG(X,Y)", positionY + "");;
        Matrix matrix = new Matrix();
        matrix.postRotate(textImage.getRotation());
        matrix.postTranslate(positionX*myPhotoHeightScale,positionY*myPhotoHeightScale);//根據螢幕比設定

        /*********繪圖**********/

        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImage,matrix, null);

        return result;
    }

    /**********調整Bitmap大小***********/


    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth*textImageScale)/width *myPhotoHeightScale;//根據螢幕比設定
        float scaleHeight = ((float) newHeight*textImageScale) /height*myPhotoHeightScale;//根據螢幕比設定

        /*********矩陣for resize**********/

        Matrix matrix = new Matrix();

        /***********根據矩陣resize**********/

        matrix.postScale(scaleWidth, scaleHeight);

        /***********產生新BITMAP**********/
        Bitmap resizedBitmap = Bitmap.createBitmap(bm,0,0,width, height ,matrix, false);

       // bm.recycle(); /*********清除Sticker*********/

        Log.i("Resized", resizedBitmap.getHeight() + "");
        Log.i("Resized", resizedBitmap.getWidth() + "");

        return resizedBitmap;
    }

    /**********根據表情選取貼圖**********/

    private void setSticker(){
        switch (message){
            case "disgust" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.disgust));
                frontPic = getResources().getDrawable(R.drawable.disgust);
                break;
            case "angry" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.angry));
                frontPic = getResources().getDrawable(R.drawable.angry);
                break;

            case "happy" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.happy));
                frontPic = getResources().getDrawable(R.drawable.happy);
                break;
            case "neutral" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.netrl));
                frontPic = getResources().getDrawable(R.drawable.netrl);
                break;
            case "sad" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.sad));
                frontPic = getResources().getDrawable(R.drawable.sad);
                break;
            case "scared" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.scared));
                frontPic = getResources().getDrawable(R.drawable.scared);
                break;
            case "surprised" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.surprise));
                frontPic = getResources().getDrawable(R.drawable.surprise);
                break;
            case  "Failed Uploaded!":
                Toast.makeText(emotionDetect.this,"Failed Uploaded!", Toast.LENGTH_LONG).show();
                frontPic = getResources().getDrawable(R.drawable.surprise);
                break;
            case "NoFace":
                Toast.makeText(emotionDetect.this,"NO FACE", Toast.LENGTH_LONG).show();
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.angry));
                frontPic = getResources().getDrawable(R.drawable.angry);
                break;
        }
    }

    /***********存檔***********/

    private void saveFile(Bitmap combineImages){
        /*********建立資料夾**********/
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "Pictures2");
        try{
            if(folder.mkdir()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        /**********儲存位置***********/

        String path = Environment.getExternalStorageDirectory() + "/Pictures2/" + UUID.randomUUID().toString() + ".JPEG";
        File file = new File((path));

        try {
            FileOutputStream out = new FileOutputStream(file);
            combineImages.compress(Bitmap.CompressFormat.JPEG,90,out);
            out.flush();
            out.close();
            Uri uri = Uri.fromFile(file);
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*********算 textImageView 與 Screen 的比值**********/

    public void setScreenScale(){
        int DSI_height;
        int  DSI_width;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;
        myPhotoHeightScale=(float)((float)DSI_height/(float)myPhotoHeight);
        myPhotoWidthScale=(float)((float)DSI_width/(float)myPhotoWidth);

    }

    /*********初始化貼圖長寬**********/

    public void initTextImageViewSize() {
        ViewTreeObserver vto = textImage.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                textImage.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                textImageWidth=textImage.getWidth();

                textImageHeight=textImage.getHeight();
                Log.i("監聽器:初始 textImage Size", "width: " + textImageWidth);
                Log.i("監聽器:初始 textImage Size", "height: " + textImageHeight);
            }
        });
    }

    /*********獲取bitmap form file**********/

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
}

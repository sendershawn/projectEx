package com.example.mycamera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;


public class emotionDetect extends AppCompatActivity implements removeResponse{

    /**********全螢幕**********/

    @Override
    public void onWindowFocusChanged(boolean hasFocas){
        super.onWindowFocusChanged(hasFocas);
        View decorView= getWindow().getDecorView();
        myPhotoHeight = myPhoto.getHeight();
        myPhotoWidth = myPhoto.getWidth();
        Window window = emotionDetect.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(hasFocas){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    |View.SYSTEM_UI_FLAG_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    |View.KEEP_SCREEN_ON
                    |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    /***********資料傳值變數**********/

    String dataPath;
    String message;
    String resultPath;
    /**********View類**********/

    TextView instructionView;

    ImageView myPhoto;
    ImageView textImage ;
    ImageView stringImage;

    ImageButton savePhotoBtn;
    ImageButton setStickerBtn;
    ImageButton setTextStingBtn;
    ImageButton removeBackBtn;
    ImageButton checkBtn;
    ImageButton cancelBtn;
    ImageButton penColorBtn;


    FrameLayout groupView;

    Bitmap resultBitmap;

    ZoomView zoomViewImage;
    ZoomView zoomViewText;
    PaintBoard paintBoard;

    /*********位置及大小參數**********/
    int checkEvent=0;
    /*********** textImageView (Sticker) ***********/
    int positionX;
    int positionY;
    int textImageHeight;
    int textImageWidth;
    /*********** myPhoto (照片) ***********/
    int myPhotoHeight;
    int myPhotoWidth;
    float myPhotoHeightScale=1;//比例尺
    float myPhotoWidthScale=1;//比例尺
    /*************浮水印*************/
    EditText inputText;
    int stringImageHeight =0;
    int stringImageWidth =0;
    /**********畫板**********/
    Bitmap maskForRemove=null;
    String maskPath;
    int penColor=1; //1==red 2==green

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);

        /*********** 圖片及message **********/

        Bundle bundle = this.getIntent().getExtras();
        dataPath = bundle.getString("dataPath");
        message = bundle.getString("message");

        /*********按鈕***********/

        setStickerBtn = (ImageButton) findViewById(R.id.setSticker);
        savePhotoBtn = (ImageButton)findViewById(R.id.saveBtn);
        setTextStingBtn=(ImageButton)findViewById(R.id.textStringBtn);
        removeBackBtn=(ImageButton)findViewById(R.id.removeBackBtn);
        penColorBtn=(ImageButton)findViewById(R.id.setPenColorBtn);
        checkBtn=(ImageButton)findViewById(R.id.checkBtn);
        cancelBtn =(ImageButton)findViewById(R.id.cancelBtn);

        /********** Image And View**********/

        myPhoto = findViewById(R.id.myPhoto);
        groupView=findViewById(R.id.groupView);
        textImage=(ImageView) findViewById(R.id.TextImage);
        stringImage = (ImageView)findViewById(R.id.StringView);
        instructionView=findViewById(R.id.instructionView);
        zoomViewImage=(ZoomView)findViewById(R.id.zoomViewImage);
        zoomViewText=(ZoomView)findViewById(R.id.zoomViewText);
        paintBoard =(PaintBoard)findViewById(R.id.paint_board);

        /**********初始化**********/

        myPhoto.setImageBitmap(getBitmap(dataPath));

        resultBitmap=((BitmapDrawable)myPhoto.getDrawable()).getBitmap();


        VisibleController(true);

        Toast.makeText(emotionDetect.this, dataPath +"----------"+message, Toast.LENGTH_SHORT).show();

        /**********繪圖按鈕**********/

        checkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**********計算textImage 與 螢幕比例 如果跑版把 這行註解 並且 把myPhoto 的ScaleType 設為 matrix**********/

                setScreenScale();// 如果跑版註解這行

                Bitmap bigImage = ((BitmapDrawable)myPhoto.getDrawable()).getBitmap();
                Bitmap mergedImages;
                Bitmap smallImage;
                switch (checkEvent){
                    case 0:
                        smallImage =getResizedBitmap(((BitmapDrawable)textImage.getDrawable()).getBitmap(),textImageWidth,textImageHeight,zoomViewImage.getScale());
                        mergedImages = createSingleImageFromMultipleImages(bigImage, smallImage,textImage,zoomViewImage);
                        break;
                    case 1:
                        smallImage =getResizedBitmap(((BitmapDrawable) stringImage.getDrawable()).getBitmap(), stringImageWidth, stringImageHeight,zoomViewText.getScale());
                        mergedImages = createSingleImageFromMultipleImages(bigImage, smallImage,stringImage,zoomViewText);
                        break;
                    case 2:
                        maskForRemove=paintBoard.getBitmap();
                        mergedImages=null;
                        saveFile(maskForRemove);
                        removeBackground();

                        break;
                    default:mergedImages=null;
                }
                myPhoto.setImageBitmap(mergedImages);
                resultBitmap=mergedImages;

                /*按鈕設置*/
                textImage.setImageBitmap(null);
                VisibleController(true);

            }
        });

        /**********取消按鈕*********/

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VisibleController(true);
            }
        });

        /*********t儲存按鈕**********/

        savePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveFile(resultBitmap);/*********存檔**********/

                Toast.makeText(emotionDetect.this,"saved", Toast.LENGTH_LONG).show();

                Log.i("ScaleForScreen", myPhotoHeight + "");
                Log.i("ScaleForScreen", myPhotoWidth + "");
                Log.i("ScaleForScreen", myPhotoHeightScale + "");
                Log.i("ScaleForScreen", myPhotoWidthScale + "");
                Log.i("path", resultPath + "");


                shareImg(resultPath);


            }
        });

        /***********貼圖按鈕**********/

        setStickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEvent=0;
                textImage.setVisibility(View.VISIBLE);
                VisibleController(false);
                /**********根據表情設定貼圖**********/
                setSticker();
                textImage.post(new Runnable() {
                    @Override
                    public void run() {
                        textImageHeight=textImage.getHeight();
                        textImageWidth=textImage.getWidth();
                    }
                });


            }
        });

        /*******文字編輯按鈕********/
        setTextStingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkEvent=1;
                stringImage.setVisibility(View.VISIBLE);


                /***********宣告dialog************/

                AlertDialog.Builder builder = new AlertDialog.Builder(emotionDetect.this);
                LayoutInflater inflater = LayoutInflater.from(emotionDetect.this);
                View alert_view = inflater.inflate(R.layout.textstringview,null);//alert為另外做給alert用的layout
                builder.setView(alert_view);
                builder.setCancelable(true);

                /***********抓取TEXT裡輸入的東西*********/

                inputText = (EditText)alert_view.findViewById(R.id.inputtext);

                /*******生成YES.NO按鈕以及點下去相對應發生的事******/

                final AlertDialog dialog = builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                               WaterMaker();
                               VisibleController(false);
                            }
                        }).setNegativeButton("NO",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        VisibleController(true);
                    }
                }).create();
                dialog.show();//把dialog秀出來
            }
        });

        /**********去背功能按鈕*********/
        removeBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VisibleController(false);
                instructionView.setVisibility(View.VISIBLE);
                paintBoard.setSize(myPhotoWidth,myPhotoHeight);
                paintBoard.setVisibility(View.VISIBLE);
                penColorBtn.setVisibility(View.VISIBLE);
                checkEvent=2;
            }
        });
        /**********筆顏色切換*********/
        penColorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(penColor==1)
                {
                    penColorBtn.setBackgroundColor(Color.BLACK);
                    penColor=2;
                }else {
                    penColor=1;
                    penColorBtn.setBackgroundColor(Color.WHITE);
                }
                paintBoard.switchColor(penColor);
            }
        });


    }
    /**********文字浮水印*********/

    private  void WaterMaker(){
        Paint paint = new Paint();
        String text= inputText.getText().toString().trim();//讀取EditText內容
        Log.d("test",text);
        String[] arr = text.split("\n");
        for (int i =0;i<arr.length;i++) {
            Log.d("test", arr[i]);
        }
        DisplayMetrics displayMetrics =this.getResources().getDisplayMetrics();

        float dips = 50.0f;//設定字體 不然不同解析度的字體大小差異極大
      //  final float scale = getResources().getDisplayMetrics().density;//獲取螢幕解析度(dpi)
        int ps = Math.round(dips * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        //int ps = (int) (dips * scale + 0.5f);
        String type = "宋體";
        Typeface typeface =Typeface.create(type,Typeface.BOLD);
        paint.setColor(Color.RED);
        paint.setTextSize(ps);//設定字體大小
        paint.setTypeface(typeface);//設定字形
        stringImageHeight =arr.length;
        stringImageWidth =0;

        /***選出最大寬度以及最大長度***/
        for (int i = 0;i<arr.length;i++){
            if (arr[i].length()> stringImageWidth){
                stringImageWidth =arr[i].length();
            }
        }
        stringImageHeight *=ps+10;
        stringImageWidth *=ps;

        Bitmap textBitmap = Bitmap.createBitmap(stringImageWidth, stringImageHeight,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(textBitmap);

        for (int i =0;i<arr.length;i++) {
            Log.d("test", arr[i]);
            canvas.drawText(arr[i], 0, ps+ps*i, paint);
        }
        stringImage.setImageBitmap(textBitmap);//將文字畫上去bitmap

    }

    /**********合圖**********/
    private Bitmap createSingleImageFromMultipleImages(Bitmap firstImage, Bitmap secondImage, ImageView smallImage ,ZoomView zoomView){

        Bitmap result = Bitmap.createBitmap(firstImage.getWidth(), firstImage.getHeight(), firstImage.getConfig());
        Canvas canvas = new Canvas(result);

        /**********設定 textImage (Sticker)  XY*********/

        int[] locations = new int[2];
        smallImage.getLocationOnScreen(locations);
        positionX = locations[0];
        positionY= locations[1];
        Log.i("TAG(X,Y)", positionX + "");
        Log.i("TAG(X,Y)", positionY + "");
        Matrix matrix = new Matrix();
        matrix.postRotate(zoomView.getRotation());
        matrix.postTranslate(positionX*myPhotoHeightScale,positionY*myPhotoHeightScale);//根據螢幕比設定

        /*********繪圖**********/

        canvas.drawBitmap(firstImage, 0f, 0f, null);
        canvas.drawBitmap(secondImage,matrix, null);

        return result;
    }

    /**********調整Bitmap大小***********/

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight,float scale) {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth*scale)/width *myPhotoHeightScale;//根據螢幕比設定
        float scaleHeight = ((float) newHeight*scale) /height*myPhotoHeightScale;//根據螢幕比設定

        /*********矩陣for resize**********/

        Matrix matrix = new Matrix();

        /***********根據矩陣resize**********/

        matrix.postScale(scaleWidth, scaleHeight);

        /***********產生新BITMAP**********/
        Bitmap resizedBitmap = Bitmap.createBitmap(bm,0,0,width, height ,matrix, false);

        //bm.recycle(); /*********清除Sticker*********/
        Log.i("Resized", resizedBitmap.getWidth() + "");
        Log.i("Resized", resizedBitmap.getHeight() + "");

        return resizedBitmap;
    }

    /**********根據表情選取貼圖**********/

    private void setSticker(){
        switch (message){
            case "disgust" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.disgust));
                break;
            case "angry" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.angry));
                break;
            case "happy" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.happy));
                break;
            case "neutral" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.netrl));
                break;
            case "sad" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.sad));
                break;
            case "scared" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.scared));
                break;
            case "surprised" :
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.surprise));
                break;
            case  "Failed Uploaded!":
                Toast.makeText(emotionDetect.this,"Failed Uploaded!", Toast.LENGTH_LONG).show();
                break;
            case "NoFace":
                Toast.makeText(emotionDetect.this,"NO FACE", Toast.LENGTH_LONG).show();
                textImage.setImageDrawable(getResources().getDrawable(R.drawable.angry));
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

        resultPath = Environment.getExternalStorageDirectory() + "/Pictures2/" + UUID.randomUUID().toString() + ".png";
        File file = new File((resultPath));
        maskPath=resultPath;
        Uri uri = Uri.fromFile(file);
        try {
            FileOutputStream output = new FileOutputStream(file);
            combineImages.compress(Bitmap.CompressFormat.PNG,90,output);
            output.flush();
            output.close();
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri));
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

    /*********按鈕&View 之 Visible設置器 *********/

    public void VisibleController(boolean checked){
        if (checked){
            instructionView.setVisibility(View.GONE);
            stringImage.setVisibility(View.GONE);
            textImage.setVisibility(View.GONE);
            paintBoard.setVisibility(View.GONE);
            penColorBtn.setVisibility(View.GONE);
            checkBtn.setVisibility(View.GONE);
            cancelBtn.setVisibility(View.GONE);
            setStickerBtn.setVisibility(View.VISIBLE);
            setTextStingBtn.setVisibility(View.VISIBLE);
            savePhotoBtn.setVisibility(View.VISIBLE);
            removeBackBtn.setVisibility(View.VISIBLE);
        }else{
            checkBtn.setVisibility(View.VISIBLE);
            cancelBtn.setVisibility(View.VISIBLE);
            setStickerBtn.setVisibility(View.GONE);
            setTextStingBtn.setVisibility(View.GONE);
            savePhotoBtn.setVisibility(View.GONE);
            removeBackBtn.setVisibility(View.GONE);

        }
    }

    /*****去背結果*****/
    @Override
    public void processRemoveFinish(Bitmap output) {
        myPhoto.setImageBitmap(output);
        deletePic(maskPath);

    }
    /*********刪除mask**********/
    private void deletePic(String path){
        if(!TextUtils.isEmpty(path)){
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver contentResolver = emotionDetect.this.getContentResolver();//cutPic.this是一个上下文
            String url =  MediaStore.Images.Media.DATA + "='" + path + "'";
            contentResolver.delete(uri, url, null);
        }
    }

    /********去背連線*********/

    private void removeBackground() {
        RemoveBackgroundAsyncTask removeBackgroundAsyncTask = new RemoveBackgroundAsyncTask (emotionDetect.this, maskPath);
        removeBackgroundAsyncTask.delegate = this;
        removeBackgroundAsyncTask.execute();
    }
    /*********分享**********/
    private void shareImg(String imagePath) {
        if (imagePath == null) {
            return;
        }
        File file= new File(imagePath);
        Uri uri = FileProvider.getUriForFile(this,getPackageName()+".provider",file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent,"分享至"));
    }

}

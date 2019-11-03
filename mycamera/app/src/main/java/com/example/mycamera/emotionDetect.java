package com.example.mycamera;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class emotionDetect extends AppCompatActivity {
    String datapath;
    ImageView imageView;
    GestureImageView Textimage;
    ImageButton savephoto;
    Button get;
    String message;
    Drawable frontpic;
    int[] values = new int[2];
    PointF llocation;
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
        setContentView(R.layout.activity_page2);
        Textimage = (GestureImageView)findViewById(R.id.imageView2);
        imageView = (ImageView)findViewById(R.id.imageView3);
        get = (Button)findViewById(R.id.button4);
        Bundle bundle = this.getIntent().getExtras();
        datapath = bundle.getString("datapath");
        message = bundle.getString("message");
        savephoto = (ImageButton)findViewById(R.id.imageButton);

        Toast.makeText(emotionDetect.this,datapath+"++++++"+message, Toast.LENGTH_SHORT).show();
        imageView.setImageBitmap(getBitmap(datapath));
        Textimage.setVisibility(View.GONE);
        savephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values[0] = (int) Textimage.getTop();
                values[1] = (int) Textimage.getLeft();
                combine();
                Toast.makeText(emotionDetect.this,"saved", Toast.LENGTH_LONG).show();
            }
        });
        //Textimage.setImageDrawable(getResources().getDrawable(R.drawable.angry));

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (message){
                    case "disgust" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.disgust));
                        frontpic = getResources().getDrawable(R.drawable.disgust);
                        break;
                    case "angry" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.angry));
                        frontpic = getResources().getDrawable(R.drawable.angry);
                        break;

                    case "happy" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.happy));
                        frontpic = getResources().getDrawable(R.drawable.happy);
                        break;
                    case "neutral" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.netrl));
                        frontpic = getResources().getDrawable(R.drawable.netrl);
                        break;
                    case "sad" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.sad));
                        frontpic = getResources().getDrawable(R.drawable.sad);
                        break;
                    case "scared" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.scared));
                        frontpic = getResources().getDrawable(R.drawable.scared);
                        break;
                    case "surprised" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.surprise));
                        frontpic = getResources().getDrawable(R.drawable.surprise);
                        break;
                    case  "Faild Uploaded!":
                        Toast.makeText(emotionDetect.this,"Faild Uploaded!", Toast.LENGTH_LONG).show();
                        frontpic = getResources().getDrawable(R.drawable.surprise);
                        break;
                    default:
                        Toast.makeText(emotionDetect.this,"NO FACE", Toast.LENGTH_LONG).show();
                        frontpic = getResources().getDrawable(R.drawable.angry);
                        break;
                }
                Textimage.setVisibility(View.VISIBLE);
            }
        });

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

    private void combine(){
        Bitmap backgrond = getBitmap(datapath);
        Bitmap front = ((BitmapDrawable)frontpic).getBitmap();

        int backgrondWidthWidth = (backgrond.getWidth());
        int backgrondHeightheigth = (backgrond.getHeight());
        PointF size = Textimage.sizex();


        Bitmap scaledBitmapFore = Bitmap.createScaledBitmap(front,(int) size.x,(int)size.y, true);//帶條
        Bitmap scaledBitmapBack = Bitmap.createScaledBitmap(backgrond, backgrondWidthWidth, backgrondHeightheigth, true);//帶條

        //Log.d("test X1:",String.valueOf(values[0]));
        //Log.d("test Y1:",String.valueOf(values[1]));
        //Bitmap combineImages = overlay(scaledBitmapBack, scaledBitmapFore,values[0],values[1]);

        llocation = Textimage.locationx();
        Bitmap combineImages = overlay(scaledBitmapBack, scaledBitmapFore,(int) llocation.x,(int)llocation.y);

        String path = Environment.getExternalStorageDirectory() + "/Pictures/" + UUID.randomUUID().toString() + ".JPEG";
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
    public static Bitmap overlay(Bitmap bmp1,Bitmap bmp2,int x,int y){

        try
        {
            int maxWidth = (bmp1.getWidth());
            int maxHeight = (bmp1.getHeight());
            Bitmap bmOverlay = Bitmap.createBitmap(maxWidth, maxHeight,  bmp1.getConfig());
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawBitmap(bmp1, 0, 0, null);
            canvas.drawBitmap(bmp2,x, y, null);
            Log.d("test X2:",String.valueOf(x));
            Log.d("test Y2:",String.valueOf(y));
            return bmOverlay;

        } catch (Exception e)
        {
            // TODO: handle exception
            e.printStackTrace();
            return null;
        }
    }

}

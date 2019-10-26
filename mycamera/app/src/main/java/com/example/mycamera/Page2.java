package com.example.mycamera;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class Page2 extends AppCompatActivity {
String datapath;
ImageView imageView;
ImageView Textimage;
ImageButton savephoto;
Button get;
String message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);
        Textimage = (ImageView)findViewById(R.id.imageView2);
        imageView = (ImageView)findViewById(R.id.imageView3);

        get = (Button)findViewById(R.id.button4);
        Bundle bundle = this.getIntent().getExtras();
        datapath = bundle.getString("datapath");
        message = bundle.getString("message");
        savephoto = (ImageButton)findViewById(R.id.imageButton);

        Toast.makeText(Page2.this,datapath+"++++++"+message, Toast.LENGTH_SHORT).show();
        imageView.setImageBitmap(getBitmap(datapath));
        Textimage.setVisibility(View.GONE);
        savephoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        //Textimage.setImageDrawable(getResources().getDrawable(R.drawable.angry));

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (message){
                    case "disgust" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.disgust));
                        break;
                    case "angry" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.angry));
                        break;

                    case "happy" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.happy));
                        break;
                    case "neutral" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.netrl));
                        break;
                    case "sad" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.sad));
                        break;
                    case "scared" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.scared));
                        break;
                    case "surprised" :
                        Textimage.setImageDrawable(getResources().getDrawable(R.drawable.surprise));
                        break;
                    case  "Faild Uploaded!":
                        Toast.makeText(Page2.this,"Faild Uploaded!", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(Page2.this,"NO FACE", Toast.LENGTH_LONG).show();
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
}

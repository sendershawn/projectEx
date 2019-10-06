package com.example.mycamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class Page2 extends AppCompatActivity {
String datapath;
ImageView imageView;
Button get;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page2);

        imageView = (ImageView)findViewById(R.id.imageView3);
        get = (Button)findViewById(R.id.button4);
        Bundle bundle = this.getIntent().getExtras();
        datapath = bundle.getString("datapath");
        Toast.makeText(Page2.this,datapath, Toast.LENGTH_SHORT).show();
        imageView.setImageBitmap(getBitmap(datapath));
    }

    private static Bitmap getBitmap(String file)
    {
        try {
            Bitmap temp = BitmapFactory.decodeFile(file);
            Matrix m =new Matrix();
            int width = temp.getWidth();
            int height = temp.getHeight();
            m.postRotate(90);
            Bitmap newBitmap = Bitmap.createBitmap(temp,0,0,width,height,m,true);
            return newBitmap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }
}

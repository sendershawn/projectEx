package com.example.mycamera;

import android.annotation.SuppressLint;
import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.OutputStream;

@SuppressLint("AppCompatCustomView")
public class PaintBoard extends ImageView {
    private Paint mPaint = null;
    private Bitmap mBitmap = null;
    private Canvas mBitmapCanvas = null;

    public void setSize(int width,int height)
        {
            mBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
            mBitmapCanvas = new Canvas(mBitmap);
            mBitmapCanvas.drawColor(Color.GRAY);
            mBitmap.setHasAlpha(true);
            mPaint = new Paint();
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(150);
            setFocusable(true);
        }

    public  void switchColor(int color){
        if(color==1)
        {
            mPaint.setColor(Color.WHITE);
        }else{
            mPaint.setColor(Color.BLACK);
        }

    }
    public PaintBoard(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,0);

    }
    private float startX;
    private float startY ;


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float stopX = event.getX();
                float stopY = event.getY();
                mBitmapCanvas.drawLine(startX, startY, stopX, stopY, mPaint);
                startX = event.getX();
                startY = event.getY();
                invalidate();
                break;

            default:

                super.onTouchEvent(event);

                break;
        }
        return true;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mBitmap != null) {
            canvas.drawBitmap(mBitmap, 0, 0, mPaint);

        }
    }
    public Bitmap getBitmap() {
        if (mBitmap != null) {
            return mBitmap;
        }else {
            return null;
        }
    }
}

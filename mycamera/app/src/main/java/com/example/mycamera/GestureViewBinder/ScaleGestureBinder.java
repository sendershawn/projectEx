package com.example.mycamera.GestureViewBinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

/*********縮放Binder***********/

public class ScaleGestureBinder extends ScaleGestureDetector {

    ScaleGestureBinder(Context context, ScaleGestureListener scaleGestureListener) {
        super(context, scaleGestureListener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

}
package com.example.mycamera.GestureViewBinder;

import android.gesture.GestureLibraries;
import android.gesture.GesturePoint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.GestureDetector;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

/********縮放計算&監聽*********/

public class ScaleGestureListener implements ScaleGestureDetector.OnScaleGestureListener /*, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener */ {

    private View targetView;
    private float scale = 1;
    private float scaleTemp = 1;
    private boolean isFullGroup = false;


    ScaleGestureListener(View targetView, ViewGroup viewGroup) {
        this.targetView = targetView;
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if(detector.getPreviousSpan()==detector.getCurrentSpan())
        {
            return false;
        }
        scale = scaleTemp * detector.getScaleFactor();
        targetView.setScaleX(scale);
        targetView.setScaleY(scale);
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        scaleTemp = scale;
    }



    float getScale() {
        return scale;
    }

    public boolean isFullGroup() {
        return isFullGroup;
    }

    void setFullGroup(boolean fullGroup) {
        isFullGroup = fullGroup;
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    void onActionUp() {
        if (isFullGroup && scaleTemp < 1) {
            scale = 1;
            targetView.setScaleX(scale);
            targetView.setScaleY(scale);
            scaleTemp = scale;
        }
    }



}
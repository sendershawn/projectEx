package com.example.mycamera.GestureViewBinder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.Timer;
import java.util.TimerTask;

/********手勢感知器*********/

public class GestureViewBinder {

    private ScaleGestureBinder scaleGestureBinder=null;
    private ScrollGestureBinder scrollGestureBinder=null;
    private ScaleGestureListener scaleGestureListener=null;
    private ScrollGestureListener scrollGestureListener=null;
    private View targetView=null;
    private ViewGroup viewGroup=null;
    private boolean isScaleEnd = true;
    private OnScaleListener onScaleListener;

    private boolean isFullGroup = false;

    /*****旋轉****/
    private float degree=0;
    private float rotation=0;
    private float moveType=0;

    public static GestureViewBinder bind(Context context, ViewGroup viewGroup, View targetView) {
        return new GestureViewBinder(context, viewGroup, targetView);
    }

    private GestureViewBinder(Context context, ViewGroup viewGroup, final View targetView) {
        this.targetView = targetView;
        this.viewGroup = viewGroup;
        scaleGestureListener = new ScaleGestureListener(targetView, viewGroup);
        scrollGestureListener = new ScrollGestureListener(targetView, viewGroup);
        scaleGestureBinder = new ScaleGestureBinder(context, scaleGestureListener);
        scrollGestureBinder = new ScrollGestureBinder(context, scrollGestureListener);
        targetView.setClickable(false);
        viewGroup.setOnTouchListener(new View.OnTouchListener() {
            @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() == 1 && isScaleEnd) {
                    return scrollGestureBinder.onTouchEvent(event);
                } else if (event.getPointerCount() == 2 || !isScaleEnd) {

                    isScaleEnd = event.getAction() == MotionEvent.ACTION_UP;
                    if (isScaleEnd) {
                        scaleGestureListener.onActionUp();
                    }

                    scrollGestureListener.setScale(scaleGestureListener.getScale());
                    if (onScaleListener != null) {
                                setRotation(event); //旋轉
                                targetView.setRotation(rotation);//旋轉ser
                        onScaleListener.onScale(scaleGestureListener.getScale());
                    }
                    return scaleGestureBinder.onTouchEvent(event);
                }
                return false;
            }
        });

    }

    /***是否讓targetView之滿版函式***/

    private void fullGroup() {
        targetView.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        targetView.getViewTreeObserver().removeOnPreDrawListener(this);
                        float viewWidth = targetView.getWidth();
                        float viewHeight = targetView.getHeight();
                        float groupWidth = viewGroup.getWidth();
                        float groupHeight = viewGroup.getHeight();
                        ViewGroup.LayoutParams layoutParams = targetView.getLayoutParams();
                        float widthFactor = groupWidth / viewWidth;
                        float heightFactor = groupHeight / viewHeight;
                        if (viewWidth < groupWidth && widthFactor * viewHeight <= groupHeight) {
                            layoutParams.width = (int) groupWidth;
                            layoutParams.height = (int) (widthFactor * viewHeight);
                        } else if (viewHeight < groupHeight && heightFactor * viewWidth <= groupWidth) {
                            layoutParams.height = (int) groupHeight;
                            layoutParams.width = (int) (heightFactor * viewWidth);
                        }
                        targetView.setLayoutParams(layoutParams);
                        return true;
                    }
                });
    }

    /***是否讓targetView之滿版設定***/

    public boolean isFullGroup() {
        return isFullGroup;
    }

    /***是否讓targetView之滿版設定***/

    public void setFullGroup(boolean fullGroup) {
        isFullGroup = fullGroup;
        scaleGestureListener.setFullGroup(fullGroup);
        scrollGestureListener.setFullGroup(fullGroup);
        fullGroup();
    }

    public void setOnScaleListener(OnScaleListener onScaleListener) {
        this.onScaleListener = onScaleListener;
    }

    public interface OnScaleListener {
        void onScale(float scale);
    }

    /*********量角器*********/

    private float getDegree(MotionEvent event) {
        double radians;
        double delta_x;
        double delta_y;
        /**** if 計算手指位置避免旋轉過頭****/
        if (event.getX(0)<event.getX(1)) {
           delta_x = targetView.getX()- event.getX(1);
           delta_y = targetView.getY()- event.getY(1);
        }else {
            delta_x = targetView.getX()- event.getX(0);
            delta_y = targetView.getY()- event.getY(0);
        }
        radians = Math.atan2(delta_y, delta_x);

        return (float) Math.toDegrees(radians)/(float)(3);
    }

    /*********旋轉工具**********/

    private void setRotation(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
           case MotionEvent.ACTION_DOWN:
                moveType = 1;

                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                moveType = 2;
                degree = getDegree(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if(moveType==2){
                    rotation = rotation + getDegree(event) - degree;
                    if (rotation >= 360) {
                        rotation = rotation - 360;
                    }
                    if (rotation < -360) {
                        rotation = rotation + 360;
                    }
                    Log.i("旋轉角度", rotation+" ");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                moveType = 0;
        }
    }

}
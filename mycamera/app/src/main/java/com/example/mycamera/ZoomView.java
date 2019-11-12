package com.example.mycamera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class ZoomView extends FrameLayout {
    // 屬性變量
    private float translationX; // 移動X
    private float translationY; // 移動Y
    private float scale = 1; // 伸縮比例
    private float rotation; // 旋轉角度

    // 移動過程中臨時變量
    private float actionX;
    private float actionY;
    private float spacing;
    private float degree;
    private int moveType; // 0=未選擇，1=拖動，2=縮放

    public ZoomView(Context context) {
        this(context, null);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.onInterceptTouchEvent(ev);
    }
    public float getScale(){
        return scale;
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                moveType = 1;
                actionX = event.getRawX();
                actionY = event.getRawY();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                moveType = 2;
                spacing = getSpacing(event);
                degree = getDegree(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (moveType == 1) {
                    translationX = translationX + event.getRawX() - actionX;
                    translationY = translationY + event.getRawY() - actionY;
                    setTranslationX(translationX);
                    setTranslationY(translationY);
                    actionX = event.getRawX();
                    actionY = event.getRawY();
                } else if (moveType == 2) {
                    scale = scale * getSpacing(event) / spacing;
                    setScaleX(scale);
                    setScaleY(scale);
                    rotation = rotation + getDegree(event) - degree;
                    if (rotation > 360) {
                        rotation = rotation - 360;
                    }
                    if (rotation < -360) {
                        rotation = rotation + 360;
                    }
                    setRotation(rotation);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                moveType = 0;
        }
        return super.onTouchEvent(event);
    }

    // 觸碰兩點間距離
    private float getSpacing(MotionEvent event) {
        //通過三角函數得到兩點間的距離
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 取旋轉角度
    private float getDegree(MotionEvent event) {
        //得到兩個手指間的旋轉角度
        double delta_x = event.getX(0) - event.getX(1);
        double delta_y = event.getY(0) - event.getY(1);
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }


}
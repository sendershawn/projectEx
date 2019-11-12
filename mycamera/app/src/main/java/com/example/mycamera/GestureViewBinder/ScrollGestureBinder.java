package com.example.mycamera.GestureViewBinder;

import android.content.Context;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;

/*********移動Binder***********/
class ScrollGestureBinder extends GestureDetector {

    ScrollGestureBinder(Context context, ScrollGestureListener scrollGestureListener) {
        super(context, scrollGestureListener);
    }

}
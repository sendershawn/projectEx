package com.example.mycamera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

@SuppressLint("AppCompatCustomView")
public class GestureImageView extends ImageView implements View.OnTouchListener {

    public class ZoomMode{
        public  final  static  int Ordinary=0;
        public  final  static  int  ZoomIn=1;
        public  final  static  int DoubleZoomIn=2;
    }
    private  int curMode=0;
    private Matrix matrix;
    private PointF viewSize;
    private  PointF imageSize;
    private  PointF scaleSize;
    //記錄圖片當前座標
    private  PointF curPoint;
    private  PointF originScale;
    //0:寬度適應 1:高度適應
    private  int fitMode=0;
    private  PointF start;
    private  PointF center;
    private  float scaleDoubleZoom=0;
    private PointF relativePoint;
    private  float doubleFingerDistance=0;
    long doubleClickTimeSpan=280;
    long lastClickTime=0;
    int rationZoomIn=2;
    public  void GestureImageViewInit(){
        this.setOnTouchListener(this);
        this.setScaleType(ScaleType.MATRIX);
        matrix=new Matrix();
        originScale=new PointF();
        scaleSize=new PointF();
        start=new PointF();
        center=new PointF();
        curPoint=new PointF();

    }

    public PointF sizex(){
        return scaleSize;
    }

    public PointF locationx(){
        PointF lo = new PointF(start.x - relativePoint.x * scaleSize.x -100, start.y - relativePoint.y * scaleSize.y);
        return  lo;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
        viewSize=new PointF(width,height);

        //獲取當前Drawable的大小
        Drawable drawable=getDrawable();
        if(drawable==null){
            Log.e("no drawable","drawable is nullPtr");
        }else {
            imageSize=new PointF(drawable.getMinimumWidth(),drawable.getMinimumHeight());
            Log.d("test5", String.valueOf(imageSize));
        }

        FitCenter();
    }

    /**
     * 使圖片儲存在中央
     */
    public void FitCenter(){
        float scaleH=viewSize.y/imageSize.y;
        float scaleW=viewSize.x/imageSize.x;
        //選擇小的縮放因子確保圖片全部顯示在視野內
        float scale =scaleH<scaleW?scaleH:scaleW;
        //根據view適應大小
        setImageScale(new PointF(scale, scale));

        originScale.set(scale, scale);
        //根據縮放因子大小來將圖片中心調整到view 中心
        if(scaleH<scaleW) {
            setImageTranslation(new PointF(viewSize.x / 2 - scaleSize.x / 2, 0));
            fitMode=1;
        }
        else {
            fitMode=0;
            setImageTranslation(new PointF(0, viewSize.y / 2 - scaleSize.y / 2));
        }

        //記錄縮放因子 下次繼續從這個比例縮放
        scaleDoubleZoom=originScale.x;
    }






    public GestureImageView(Context context) {
        super(context);
        GestureImageViewInit();
    }

    public GestureImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        GestureImageViewInit();
    }

    public GestureImageView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        GestureImageViewInit();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                start.set(event.getX(),event.getY());
                //手指按下事件
                if(event.getPointerCount()==1){
                    if(event.getEventTime()-lastClickTime<=doubleClickTimeSpan){
                        //雙擊事件觸發
                        Log.e("TouchEvent", "DoubleClick");
                        if(curMode==ZoomMode.Ordinary) {
                            curMode=ZoomMode.ZoomIn;
                            relativePoint=new PointF();
                            //計算歸一化座標
                            relativePoint.set(( start.x-curPoint.x )/ scaleSize.x,(start.y-curPoint.y)/scaleSize.y);

                            setImageScale(new PointF(originScale.x * rationZoomIn, originScale.y * rationZoomIn));
                            setImageTranslation(new PointF(start.x - relativePoint.x * scaleSize.x , start.y - relativePoint.y * scaleSize.y));
                        }else {
                            curMode=ZoomMode.Ordinary;
                            FitCenter();
                        }
                    }else {
                        lastClickTime=event.getEventTime();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                //螢幕上已經有一個點按住 再按下一點時觸發該事件
                doubleFingerDistance=getDoubleFingerDistance(event);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //螢幕上已經有兩個點按住 再鬆開一點時觸發該事件
                curMode=ZoomMode.ZoomIn;
                scaleDoubleZoom=scaleSize.x/imageSize.x;
                if(scaleSize.x<viewSize.x&&scaleSize.y<viewSize.y){
                    curMode=ZoomMode.Ordinary;
                    FitCenter();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                //手指移動時觸發事件
                if(event.getPointerCount()==1){
                    if(curMode==ZoomMode.ZoomIn){
                        setImageTranslation(new PointF(event.getX() - start.x,  event.getY() - start.y));
                        start.set(event.getX(),event.getY());
                    }
                }else {
                    //雙指縮放時判斷是否滿足一定距離
                    if (Math.abs(getDoubleFingerDistance(event) - doubleFingerDistance) > 50 && curMode != ZoomMode.DoubleZoomIn) {
                        //獲取雙指中點
                        center.set((event.getX(0) + event.getX(1)) / 2, (event.getY(0) + event.getY(1)) / 2);
                        //設定起點
                        start.set(center);
                        curMode = ZoomMode.DoubleZoomIn;
                        doubleFingerDistance = getDoubleFingerDistance(event);
                        relativePoint = new PointF();

                        //根據圖片當前座標值計算歸一化座標
                        relativePoint.set(( start.x-curPoint.x )/ scaleSize.x,(start.y-curPoint.y)/scaleSize.y);
                    }
                    if(curMode==ZoomMode.DoubleZoomIn)
                    {
                        float scale =scaleDoubleZoom*getDoubleFingerDistance(event)/doubleFingerDistance;
                        setImageScale(new PointF(scale, scale));
                        setImageTranslation(new PointF(start.x - relativePoint.x * scaleSize.x, start.y - relativePoint.y * scaleSize.y));
                    }

                }
                break;
            case MotionEvent.ACTION_UP:
                //手指鬆開時觸發事件

                break;
        }

        //注意這裡return 的一定要是true 否則只會觸發按下事件
        return true;
    }

    public  void setImageScale(PointF scale){
        matrix.setScale(scale.x, scale.y);
        scaleSize.set(scale.x*imageSize.x,scale.y*imageSize.y);
        this.setImageMatrix(matrix);
    }

    /**
     * 根據偏移量改變圖片位置
     * @param offset
     */
    public void setImageTranslation(PointF offset){
        matrix.postTranslate(offset.x, offset.y);
        curPoint.set(offset);
        this.setImageMatrix(matrix);
    }


    public static   float  getDoubleFingerDistance(MotionEvent event){
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return  (float)Math.sqrt(x * x + y * y) ;
    }



}

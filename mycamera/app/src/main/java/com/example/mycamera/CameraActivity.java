package com.example.mycamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraActivity extends AppCompatActivity implements AsyncResponse{
    /*按鈕*/
    private ImageButton btnCapture;
    private ImageButton uploadbtn;
    private ImageButton cameraSwitch;
    private ImageButton flashlight;
    /*預覽*/
    private Size mPreviewSize;
    private TextureView textureView;
    /*閃光*/
    private boolean flashOn=false;
    private CaptureRequest.Key<Integer> mode=CaptureRequest.CONTROL_AE_MODE;
    private int mode_type=CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;
    /*相機*/
    private int checkedId=0;
    private String mCameraId;
    private CameraManager manager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSessions;
    private CaptureRequest.Builder captureRequestBuilder;
    private CaptureRequest captureRequest;
    private Size imageDimension;
    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";
    private static  String cameraId = CAMERA_BACK;
    private ImageReader imageReader;
    //儲存檔案
    private File file;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Context mContext;
    private String path;
    private ImageView rota;
    private String message = "init";



    /*********CALLBACK**********/

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    CameraDevice.StateCallback stateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        }
    };

    /***********全螢幕***********/

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

    /************接口**********/

    public void processFinish(String output ) {
        message=output;
        Log.d("正確信息:message",message);
        Intent intent = new Intent(CameraActivity.this, emotionDetect.class);
        intent.putExtra("dataPath", path);
        intent.putExtra("message", message);
        startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_acitivity);

        /**********宣告**********/

        cameraSwitch=(ImageButton) findViewById(R.id.cameraSwitch);
        flashlight = (ImageButton) findViewById(R.id.flashBtn);
        uploadbtn = (ImageButton) findViewById(R.id.uploadBtn);
        textureView = (TextureView) findViewById(R.id.textureView);
        btnCapture = (ImageButton) findViewById(R.id.btnCapture);
        uploadbtn.setEnabled(false);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        /***********flashlight 開關***********/

        flashlight.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(flashOn ==true) {
                   // setFlashlight();
                    Log.e("FlashOn:","true->false");
                    flashlight.setBackground(getResources().getDrawable(R.drawable.button_fliushlight_off));
                    flashOn =false;
                }else{
                   // setFlashlight();
                    Log.e("FlashOn:","false->true");
                    flashlight.setBackground(getResources().getDrawable(R.drawable.button_fliushlight));
                    flashOn =true;
                }


            }
        });

        /***********快門***********/

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                takePicture();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    uploadbtn.setEnabled(true);
                }
            }
        });

        /***********上傳***********/

        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
                Log.d("正確信息","國家機器上傳囉");
            }
        });

        cameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

    }

    /***********調整螢幕1.0***********/

    private void resize(View view){
        Window window = CameraActivity.this.getWindow();
        Point point = new Point();
        window.getWindowManager().getDefaultDisplay().getRealSize(point);
        int newWidth = (int) (point.x);
        int newHeight = newWidth * 4/3;
        view.getLayoutParams().height = newHeight;
        view.getLayoutParams().width = newWidth;
        view.requestLayout();
    }

    /***********調整螢幕計算式2.0***********/

    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight )
    {
        int DSI_height;
        int DSI_width;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionWidth)/ResolutionHeight);
            updateTextureViewSize(newWidth,newHeight);

        }else {
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionHeight)/ResolutionWidth);
            updateTextureViewSize(newWidth,newHeight);
        }

    }

    /***********調整螢幕實作2.0***********/

    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        Log.d("螢幕", "TextureView Width : " + viewWidth + " TextureView Height : " + viewHeight);
        textureView.setLayoutParams(new RelativeLayout.LayoutParams(viewWidth, viewHeight));

        /**********下調view**********/

        int dpValue = 70; // margin in dips
        float d = this.getResources().getDisplayMetrics().density;
        int margin = (int)(dpValue * d);
        RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams)textureView.getLayoutParams();
        relativeParams.setMargins(0, margin, 0, 0);  // left, top, right, bottom
        textureView.setLayoutParams(relativeParams);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)//版本API
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)//需求

    /***********拍照***********/

    private void takePicture() {
        if (cameraDevice == null)
            return;
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null)
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            /**********圖片大小**********/

            int width = 1920;
            int height = 1440;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth() * 3 / 4;
                height = jpegSizes[0].getHeight() * 3 / 4;
            }
            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(cameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO);
            /************FLASH實作**********/
            setFlashlight();
            captureBuilder.set(mode,mode_type);

            cameraCaptureSessions.stopRepeating();

            /**********儲存地址**********/

            path = Environment.getExternalStorageDirectory() + "/Pictures/" + UUID.randomUUID().toString() + ".JPEG";
            Log.d("camera get path", path);
            file = new File(path);
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Image image = null;
                    image = reader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    try {
                        Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        Matrix m = new Matrix();
                        int width = temp.getWidth();
                        int height = temp.getHeight();

                        /**********存等轉向*********/

                        if(cameraId==CAMERA_BACK)
                        {
                            m.postRotate(90);
                        }else {
                            m.postRotate(270);
                        }
                        Bitmap newBitmap = Bitmap.createBitmap(temp, 0, 0, width, height, m, true);

                        save(newBitmap);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        {
                            if (image != null)
                                image.close();
                            buffer.clear();
                        }
                    }
                }

                /**********save**********/

                private void save(Bitmap bm) throws IOException {
                    FileOutputStream outputStream = null;
                    try {
                        outputStream = new FileOutputStream(file);
                        bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.flush();
                        Uri uri = Uri.fromFile(file);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                    } finally {
                        if (outputStream != null)
                            outputStream.close();
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(CameraActivity.this, "Saved " + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /*********預覽**********/

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null)
                        return;
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(CameraActivity.this, "Changed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /***********更新預覽**********/

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updatePreview() {
        if (cameraDevice == null)
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            /***********螢幕預覽2.0************/

            setAspectRatioTextureView(imageDimension.getHeight(), imageDimension.getWidth());
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallBack, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**********surface Listener*********/

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            /**********螢幕預覽比例1.0*********/
            //resize(textureView);
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**********resume**********/

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if(textureView.isAvailable())
            openCamera();
        else
            textureView.setSurfaceTextureListener(textureListener);
    }


    @Override
    protected void onPause() {

        /**********release when activity change**********/

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        stopBackgroundThread();
        super.onPause();
    }

    /**********background thread stop*********/

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread=null;
            mBackgroundHandler=null;
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /**********background thread open*********/

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**********鏡頭切換**********/

    public void switchCamera() {
        if (cameraId.equals(CAMERA_FRONT)) {
            cameraId = CAMERA_BACK;
            cameraDevice.close();
            openCamera();


        } else if (cameraId.equals(CAMERA_BACK)) {
            cameraId = CAMERA_FRONT;
            cameraDevice.close();
            openCamera();

        }
    }

    /***********上傳script**********/

    private void upload() {
        UploadAsycTask uploadAsyncTask = new UploadAsycTask(CameraActivity.this, path);
        uploadAsyncTask.delegate = this;
        uploadAsyncTask.execute();
    }
    /**********Flash設定**********/
    private void setFlashlight(){
        if(flashOn) {
            mode=CaptureRequest.FLASH_MODE;
            Log.d("modeFLash",Integer.toString(mode_type));
            mode_type=CaptureRequest.FLASH_MODE_SINGLE;
            Log.d("modeFLash",Integer.toString(mode_type));
        }
        else{
            mode=CaptureRequest.CONTROL_AE_MODE;
            mode_type=CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH;

        }

    }



}

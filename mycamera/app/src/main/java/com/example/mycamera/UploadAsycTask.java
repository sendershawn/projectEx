package com.example.mycamera;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.SocketTimeoutException;

public class UploadAsycTask extends AsyncTask<String, String, String> {
    public CameraActivity delegate = null;
    private final String TAG = "ShelfAsyncTask";
    private DataOutputStream dos = null;
    private Context mContext;
    private String lineEnd = "\r\n";
    private String twoHyphens = "--";
    private String boundary = "*****";
    private int bytesRead,bytesAvailable,bufferSize;
    private byte[] buffer;
    private int maxBufferSize = 1 * 1024 * 1024;
    private int serverResponseCode = 0;
    private String message;
    private Bitmap bitmap = null;
    private String filepath;
    ProgressDialog dialog;

    public void setBitmap(Bitmap bitmap){
        this.bitmap = bitmap;
    }


    public UploadAsycTask(Context context, String path){
        super();
        mContext = context;
        filepath=path;
    }
    @Override
    protected void onPreExecute() {
        dialog=ProgressDialog.show(mContext,"","Detecting...",true);
        if(dialog.isShowing())
        {
            Log.d("progress dialog","載入中!");
        }

    }
    @Override
    protected String doInBackground(String... strings) {
        URL url = null;
        File sourceFIle = new File(filepath);
        try {

            FileInputStream fileInputStream = new FileInputStream(sourceFIle);
            url = new URL("http://hianiku.ddns.net:8001/upload_img/");
            //url = new URL("http://192.168.137.1:8000/upload_img/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setDoInput(true); //允許輸入流，即允許下載
            conn.setDoOutput(true); //允許輸出流，即允許上傳
            conn.setUseCaches(false); //設置是否使用緩存
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.connect();

            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"attachment_file\";filename=\""
                    + "uploadfile" + "\";" + lineEnd);
            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer,0,bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer,0,bufferSize);
            }

            dos.writeBytes(lineEnd);



            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);



            if (conn.getResponseCode()==200){
                InputStreamReader reader = new InputStreamReader(conn.getInputStream(),"UTF-8");
                BufferedReader in = new BufferedReader(reader);
                message = in.readLine();
                Log.d("FromServer",message);
                return  message;
            }

            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (SocketTimeoutException e){
            Toast.makeText(mContext,"連線逾時",Toast.LENGTH_LONG);
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onPostExecute(String result) {
        delegate.processFinish(result);
        Log.d("PostExecute",result);
        dialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        dialog.setProgress(Integer.parseInt(values[0]));
    }

    @Override
    protected void onCancelled() {
        Toast.makeText(mContext,"Upload cancel",Toast.LENGTH_LONG).show();
    }
}

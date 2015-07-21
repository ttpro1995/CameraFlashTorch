package com.meow.thaithien.testcameraflash;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    public Point size;
    Preview preview;
    ImageButton buttonClick;
   public static Camera camera;
public String path;
public ImageButton myImage;
    public static int currentId;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        camera = getCameraInstance();
        preview = new Preview(this,camera);
        ((FrameLayout) findViewById(R.id.preview)).addView(preview);



        Display display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        Log.d(TAG,"Screen size: " + size.x + "-" + size.y);









        Log.i(TAG, "onCreate'd");
    }

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    /** Handles data for raw pictcure */
    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture */
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            Log.d(TAG, "Saving file..." + data.length);

                  createFile createfile = new createFile();
                    createfile.execute(data);


        }

    };


    private class createFile extends AsyncTask<byte[],Bitmap,Bitmap>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
            Log.d(TAG, values[0].getWidth() + "-" + values[0].getHeight());
        //    int nh = (int) ( values[0].getHeight() * (512.0 / values[0].getWidth()) );
         //   Bitmap scaled = Bitmap.createScaledBitmap(values[0], 512, nh, true);
           // if(size.x < values[0].getWidth() && size.y < values[0].getHeight())
           // {
                Bitmap scaled = Bitmap.createScaledBitmap(values[0],size.x/4, size.y/4, true);
                values[0] = scaled;
           // }
            myImage.setImageBitmap(values[0]);
        }

        @Override
        protected Bitmap doInBackground(byte[]... params) {

            FileOutputStream outStream = null;
            try {
                // write to local sandbox file system
                // outStream =
                // CameraDemo.this.openFileOutput(String.format("%d.jpg",
                // System.currentTimeMillis()), 0);
                // Or write to sdcard
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/"+ String.format("%d_lap.jpg", System.currentTimeMillis());

             //   outStream = new FileOutputStream(path);
             //   outStream.write(params[0]);
             //   outStream.close();

                try {
             //       File f = new File(path);
                  /*  ExifInterface exif = new ExifInterface(f.getPath());
                    int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                    int angle = 0;

                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                        angle = 90;
                    }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                        angle = 180;
                    }
                    else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                        angle = 270;
                    }*/

                    camera.startPreview();
                    Matrix mat = new Matrix();
                    mat.postRotate(Preview.rotation);

                /// /    Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
                    Bitmap bmp = BitmapFactory.decodeByteArray(params[0],0,params[0].length);
                    Bitmap correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);


                    publishProgress(correctBmp);
                    // bmp.recycle();

                    File file = new File(path);
                    FileOutputStream fOut = new FileOutputStream(file);

                    correctBmp.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    fOut.flush();
                    fOut.close();


                    Log.i(TAG, "onPictureTaken - wrote bytes: " + params[0].length);
                    return correctBmp;
                }
                catch (IOException e) {
                    Log.w("TAG", "-- Error in setting image");
                }
                catch(OutOfMemoryError oom) {
                    Log.w("TAG", "-- OOM Error in setting image");
                }






                //  SetImage();

         //   } catch (FileNotFoundException e) {
         //       e.printStackTrace();
         //   } catch (IOException e) {
         //       e.printStackTrace();
            } finally {
            }
         return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);


            Toast.makeText(MainActivity.this,"Done!!!",Toast.LENGTH_SHORT);
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();

        if(camera == null) {
            camera = getCameraInstance();
            preview = new Preview(this,camera);
            ((FrameLayout) findViewById(R.id.preview)).addView(preview);

            Log.i(TAG, "onResume");

        }
    }



    @Override
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        // Release the Camera because we don't need it when paused
        // and other activities might need to use it.
       /* if (camera != null) {
            //TODO: mewo;
            camera.release();
            camera = null;
            Log.i(TAG, "Release camera");
        }
*/
    }



    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
                int numberOfCameras = Camera.getNumberOfCameras();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        //Log.d(DEBUG_TAG, "Camera found");
                        currentId = i;
                        break;
                    }
                }

            c = Camera.open(currentId); // attempt to get a Camera instance
            Log.i(TAG,"open camera");
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e("CAMERA","No Camera");
        }

        return c; // returns null if camera is unavailable
    }





}
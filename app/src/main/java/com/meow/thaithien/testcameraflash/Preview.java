package com.meow.thaithien.testcameraflash;

/**
 * Created by user on 6/10/2015.
 */


import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.security.Policy;
import java.util.List;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Preview";

    SurfaceHolder mHolder;
    public Context context;
    public static Camera camera;
    public static int rotation;
    Preview(Context context, Camera camera) {
        super(context);
        this.camera = camera;
        this.context = context;
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }



    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.

        Log.i(TAG, "surfaceCreated");
        try {
            if(camera == null)
               return;

            camera.setPreviewDisplay(holder);

            camera.setPreviewCallback(new PreviewCallback() {

                public void onPreviewFrame(byte[] data, Camera arg1) {
                   Preview.this.invalidate();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


  @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.

        if(camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            mHolder.removeCallback(this);
            camera.release();
            camera = null;
            MainActivity.camera = null;
        }
      //camera.stopPreview();


      Log.i(TAG,"SurfaceDestroy");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        Log.i(TAG,"SurfaceChange");
        // You need to choose the most appropriate previewSize for your app
        Camera.Size previewSize = previewSizes.get(0);
        parameters.setPreviewSize(previewSize.width, previewSize.height);

        //FLASHLIGHT Mode Torch always on
        parameters.setFlashMode(parameters.FLASH_MODE_TORCH);

        rotation= setCameraDisplayOrientation((Activity)context,MainActivity.currentId,camera);

        camera.setParameters(parameters);
        camera.startPreview();
        Log.i(TAG, "Start preview in SurfaceChange");
    }




    public static int setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
        return result;
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint p = new Paint(Color.RED);
        Log.d(TAG, "draw");
        canvas.drawText("PREVIEW dsaasdasa", canvas.getWidth() / 2,
                canvas.getHeight() / 2, p);
    }
}

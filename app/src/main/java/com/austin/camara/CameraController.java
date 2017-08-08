package com.austin.camara;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by gy on 2017/8/8.
 */

public class CameraController implements SurfaceHolder.Callback {
    private Context context;
    private boolean mHasNoCameraFlag = false;
    public CameraView.CameraSettingInterface mCameraSettingInterface;
    private Camera c = null;
    private int mRotateDegree = 0;
    private SurfaceView mSurfaceView;
    private CameraView cameraView;
    private SurfaceHolder mSurfaceHolder;
    private View mMaskView;
    private android.hardware.Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = new File(Environment.getExternalStorageDirectory(), "custom.jpeg");
            if (pictureFile == null){
                return;
            }

            if(pictureFile.exists()){
                pictureFile.delete();
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            c.startPreview();
        }
    };


    public CameraController(CameraView cameraView) {
        this.cameraView = cameraView;
        this.context = cameraView.getContext();
        mSurfaceView = new SurfaceView(context);
        cameraView.addView(mSurfaceView);
        checkCameraHardware();
    }


    public void checkCameraHardware() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            mHasNoCameraFlag = true;
        } else {
            mHasNoCameraFlag = false;
        }
    }

    public Camera initCameraAndBindSurfaceHolder() {

        if (mCameraSettingInterface != null) {
            if (mHasNoCameraFlag) {
                mCameraSettingInterface.onNoCamara();
                return null;
            }
        } else {
            throw new IllegalArgumentException(context.getClass().getSimpleName() + "请设置setCamaraInterface");
        }

        try {
            c = Camera.open(); // attempt to get a Camera instance
            setCameraParameters();
            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.addCallback(this);
//            mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            c.setPreviewDisplay(mSurfaceHolder);

        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            if (mCameraSettingInterface != null) {
                mCameraSettingInterface.onCameraInavailable();
            }
        }
        return c; // returns null if camera is unavailable
    }

    private void setCameraParameters() {
        Camera.Parameters parameters = c.getParameters();
        if (mCameraSettingInterface != null) {
            mRotateDegree = mCameraSettingInterface.onSetOrientation();

            int[] size = mCameraSettingInterface.onGetProposalPreviewSize();
            if(size!=null) {
                choosePreviewSize(parameters, size[0], size[1]);
                cameraView.requestLayout();
                c.setParameters(parameters);
            }else {
                setMaxSize(parameters);
            }
            c.setParameters(parameters);

            Log.e("TAG", "设定的宽高：" + size[0] + ":" + size[1]);

        }


        c.setDisplayOrientation(mRotateDegree);
    }

    private void setMaxSize(Camera.Parameters parameters) {
        Camera.Size maxPictureSize = parameters.getSupportedPictureSizes().get(0);
        Camera.Size maxPreviewSize = parameters.getSupportedPreviewSizes().get(0);
        for (int i = 0; i < parameters.getSupportedPictureSizes().size(); i++) {
            Camera.Size s = parameters.getSupportedPictureSizes().get(i);
            if (s.width > maxPictureSize.width) {
                maxPictureSize = s;
            }
            if(s.width==maxPictureSize.width&&s.height>maxPictureSize.height){
                maxPictureSize = s;
            }
        }
        for (int i = 0; i < parameters.getSupportedPreviewSizes().size(); i++) {
            Camera.Size s = parameters.getSupportedPreviewSizes().get(i);
            if (s.width > maxPreviewSize.width) {
                maxPreviewSize = s;
            }
            if(s.width==maxPreviewSize.width&&s.height>maxPreviewSize.height){
                maxPreviewSize = s;
            }
        }

        parameters.setPictureSize(maxPictureSize.width, maxPictureSize.height);
        parameters.setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
    }

    private void choosePreviewSize(Camera.Parameters parameters, int width, int height) {
        Camera.Size finalSize = null;
        Camera.Size finalSize2 = null;

        List sizeList = parameters.getSupportedPreviewSizes();
        ArrayList validSize = new ArrayList();
        Iterator validSizeIterator = sizeList.iterator();

        while (validSizeIterator.hasNext()) {
            Camera.Size item = (Camera.Size) validSizeIterator.next();
            if (item.width >= width && item.height >= height && item.height != item.width) {
                validSize.add(item);
            }
        }


        Camera.Size[] sortedArray = (Camera.Size[]) validSize.toArray(new Camera.Size[0]);
        Arrays.sort(sortedArray, new Comparator<Camera.Size>() {
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.width * lhs.height - rhs.width * rhs.height;
            }
        });

        if (sortedArray.length == 0) {
            finalSize =parameters.getPreviewSize();
        } else {
            finalSize = sortedArray[0];
        }


        List sizeList2 = parameters.getSupportedPictureSizes();
        ArrayList validSize2 = new ArrayList();
        Iterator validSizeIterator2 = sizeList2.iterator();

        while (validSizeIterator2.hasNext()) {
            Camera.Size item = (Camera.Size) validSizeIterator2.next();
            if (item.width >= width && item.height >= height && item.height != item.width) {
                validSize2.add(item);
            }
        }


        Camera.Size[] sortedArray2 = (Camera.Size[]) validSize2.toArray(new Camera.Size[0]);
        Arrays.sort(sortedArray2, new Comparator<Camera.Size>() {
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.width * lhs.height - rhs.width * rhs.height;
            }
        });

        if (sortedArray2.length == 0) {
            finalSize2 =parameters.getPreviewSize();
        } else {
            finalSize2 = sortedArray2[0];
        }

        Log.e("TAG", "计算的Preview宽高：" + finalSize.width + ":" + finalSize.height);
        Log.e("TAG", "计算的Picture宽高：" + finalSize2.width + ":" + finalSize2.height);
        parameters.setPreviewSize(finalSize.width, finalSize.height);
        parameters.setPictureSize(finalSize2.width, finalSize2.height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            c.setPreviewDisplay(holder);
            c.startPreview();
            cameraView.addMaskView();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
            stopPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (holder.getSurface() == null) {
            return;
        }

        try {
            c.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        try {
            c.setPreviewDisplay(holder);
            c.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            e.printStackTrace();
            stopPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void startPreview() {
        initCameraAndBindSurfaceHolder();
    }

    public void stopPreview() {
        if (c != null) {
            c.stopPreview();
            c.release();
            c = null;
        }
    }


    public CameraView.MaskViewHolder addMaskView(int layoutid) {
        mMaskView = LayoutInflater.from(context).inflate(layoutid, cameraView, false);
        cameraView.addView(mMaskView);
        return cameraView.new MaskViewHolder(mMaskView);
    }


    public View.OnClickListener takePicture() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c.takePicture(null, null, pictureCallback);
            }
        };
    }
}

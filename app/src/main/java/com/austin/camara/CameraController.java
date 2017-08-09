package com.austin.camara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * Created by gy on 2017/8/8.
 */

public class CameraController implements SurfaceHolder.Callback {
    private Context context;
    public CameraView.CameraSettingInterface mCameraSettingInterface;
    private Camera camera = null;
    private SurfaceView mSurfaceView;
    private CameraView cameraView;
    private SurfaceHolder mSurfaceHolder;
    private View mMaskView;

    private CameraView.MaskViewHolder maskViewHolder;


    public CameraController(CameraView cameraView) {
        this.cameraView = cameraView;
        this.context = cameraView.getContext();
    }

    public void startPreview() {
            mSurfaceView = new SurfaceView(context);
            cameraView.addView(mSurfaceView);
            cameraView.addMaskView();
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        synchronized (CameraController.this) {
                            initCamera();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (mCameraSettingInterface != null) {
                            mCameraSettingInterface.onCameraInavailable();
                        }
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    try {
                        mSurfaceHolder = mSurfaceView.getHolder();
                        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                        mSurfaceHolder.addCallback(CameraController.this);

                        if (camera != null) {
                            mSurfaceView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (camera != null) {
                                        camera.autoFocus(null);
                                    }
                                }
                            });
                            camera.setPreviewDisplay(mSurfaceHolder);
                            camera.startPreview();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error setting camera preview: " + e.getMessage());
                    }
                }
            }.execute();

        /*try {
            initCamera();
            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mSurfaceHolder.addCallback(CameraController.this);

            if(camera !=null) {
                mSurfaceView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(camera !=null){
                            camera.autoFocus(null);
                        }
                    }
                });
                camera.setPreviewDisplay(mSurfaceHolder);
                camera.startPreview();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            if (mCameraSettingInterface != null) {
                mCameraSettingInterface.onCameraInavailable();
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();*/

    }

    public void stopPreview() {
        synchronized (this) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }

        mSurfaceHolder.removeCallback(CameraController.this);
        cameraView.removeView(mMaskView);
        cameraView.removeView(mSurfaceView);
    }

    private void initCamera() {
        camera = Camera.open();
        Camera.Parameters parameters = camera.getParameters();
        if (mCameraSettingInterface != null) {
            int[] size = mCameraSettingInterface.onGetProposalPreviewSize();
            Log.e("TAG", "设定的宽高：" + size[0] + ":" + size[1]);

            if(size!=null) {
                CamaraUtil.choosePreviewSize(parameters, size[1], size[0]);
            }else {
                CamaraUtil.chooseMaxSize(parameters);
            }
            camera.setParameters(parameters);
        }
        camera.setDisplayOrientation(90);
    }

    public CameraView.MaskViewHolder addMaskView(int layoutid) {
            mMaskView = LayoutInflater.from(context).inflate(layoutid, cameraView, false);
            cameraView.addView(mMaskView);
            maskViewHolder = cameraView.new MaskViewHolder(mMaskView);
        return maskViewHolder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (holder.getSurface() == null) {
            return;
        }
        mSurfaceHolder = holder;

        // set preview size and make any resize, rotate or
        // reformatting changes here

        try {
            camera.stopPreview();
            camera.setPreviewDisplay(holder);
            camera.startPreview();

        } catch (Exception e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public View.OnClickListener takePicture() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(camera !=null){
                    camera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if(success){
                                CameraController.this.camera.takePicture(null, null, pictureCallback);
                            }
                        }
                    });
                }
            }
        };
    }

    private android.hardware.Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            File pictureFile = new File(Environment.getExternalStorageDirectory(), "custom.jpeg");

            if(pictureFile.exists()){
                pictureFile.delete();
            }

            if (!pictureFile.getParentFile().exists()) {
                pictureFile.getParentFile().mkdirs(); // 创建文件夹
            }

            bmp = rotateBitmapByDegree(bmp, 90);

            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(pictureFile));
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos); // 向缓冲区之中压缩图片
                bos.flush();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                ExifInterface exifInterface = new ExifInterface(pictureFile.getAbsolutePath());
                // 修正图片的旋转角度，设置其不旋转。这里也可以设置其旋转的角度，可以传值过去，
                // 例如旋转90度，传值ExifInterface.ORIENTATION_ROTATE_90，需要将这个值转换为String类型的
                exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, "no");
                exifInterface.saveAttributes();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CameraController.this.camera.stopPreview();
            CameraController.this.camera.startPreview();
        }
    };

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

}

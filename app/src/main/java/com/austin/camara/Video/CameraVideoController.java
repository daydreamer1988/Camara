package com.austin.camara.Video;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.austin.camara.CamaraUtil;
import com.austin.camara.CameraSettingInterface;
import com.austin.camara.R;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static android.animation.AnimatorInflater.loadAnimator;
import static android.content.ContentValues.TAG;

/**
 * Created by gy on 2017/8/8.
 */

public class CameraVideoController implements SurfaceHolder.Callback {
    private Context context;
    public CameraSettingInterface mCameraSettingInterface;
    private Camera camera = null;
    private SurfaceView mSurfaceView;
    private CameraVideoView cameraView;
    private SurfaceHolder mSurfaceHolder;
    private View mMaskView;

    private CameraVideoView.MaskViewHolder maskViewHolder;
    private View recordView;
    private MediaRecorder mMediaRecorder;
    private boolean isVideoRecorderReady;
    private boolean isRecording;
    private Camera.Size previewSize;
    private String videoPath;
    private Camera.Size supportedVideoSize;
    private boolean isBackCamera = true;
    private int cameraId = 0;
    private MediaPlayer mediaPlayer;
    private SurfaceView playSurfaceView;
    private SurfaceHolder playHolder;
    private View mPlayMaskView;
    private Animator animator;
    private long startTime;
    private long stopTime;


    public CameraVideoController(CameraVideoView cameraView) {
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
                        synchronized (CameraVideoController.this) {
                            initCamera(cameraId);
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
                        mSurfaceHolder.setKeepScreenOn(true);

                        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                        mSurfaceHolder.addCallback(CameraVideoController.this);

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
    }

    public void stopPreview() {
        synchronized (this) {
            if (camera != null) {
                camera.stopPreview();
                camera.release();
                camera = null;
            }

            releaseMediaRecorder();

            if(mSurfaceHolder!=null)
                mSurfaceHolder.removeCallback(CameraVideoController.this);
            cameraView.removeView(mMaskView);
            cameraView.removeView(mSurfaceView);
        }


    }

    private void initCamera(int cameraId) {
        camera = Camera.open(cameraId);
        Camera.Parameters parameters = camera.getParameters();

        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }

        if (mCameraSettingInterface != null) {
            int[] size = mCameraSettingInterface.onGetProposalPreviewSize();
            Log.e("TAG", "设定的宽高：" + size[0] + ":" + size[1]);

            if(size!=null) {
                CamaraUtil.choosePreviewSize(parameters, size[1], size[0]);
            }else {
                CamaraUtil.chooseMaxSize(parameters);
            }
        }
//        getSupportedVideoSize(parameters);

        previewSize = parameters.getPreviewSize();
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
    }

    private void getSupportedVideoSize(Camera.Parameters parameters) {
        List<Camera.Size> supportedVideoSizes = parameters.getSupportedVideoSizes();

        if(null != supportedVideoSizes && supportedVideoSizes.size() > 0){
            supportedVideoSize = supportedVideoSizes.get(supportedVideoSizes.size());

            Arrays.sort(supportedVideoSizes.toArray(new Camera.Size[0]), new Comparator<Camera.Size>() {
                @Override
                public int compare(Camera.Size size, Camera.Size t1) {
                    return (size.width - previewSize.width)-(t1.width - previewSize.width);
                }
            });


        }else{
        }
    }


    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        camera.unlock();
        mMediaRecorder.setCamera(camera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        Log.e("TAG", "默认videoSize:" + profile.videoFrameWidth + ":" + profile.videoFrameHeight);
        mMediaRecorder.setOrientationHint(cameraId == 0 ? 90 : 270);
        mMediaRecorder.setProfile(profile);

        mMediaRecorder.setMaxDuration(1000*5);
        int finalWidth = previewSize.width;
        int finalHeight = previewSize.height;
        mMediaRecorder.setVideoSize(finalWidth, finalHeight);
        Log.e("TAG", "设置videoSize:" + finalWidth + ":" + finalHeight);
//        mMediaRecorder.setMaxFileSize(1024 * 1024 * 10);
        // Step 4: Set output file
        videoPath = Environment.getExternalStorageDirectory() + "/video.mp4";
        mMediaRecorder.setOutputFile(videoPath);

        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());


        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if(what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                    stopRecording();
                }
            }
        });

        mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                try {
                    if (mediaRecorder != null)
                        mediaRecorder.reset();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.e(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.e(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            camera.lock();           // lock camera for later use
        }
    }

    public CameraVideoView.MaskViewHolder addMaskView(int layoutid) {
            mMaskView = LayoutInflater.from(context).inflate(layoutid, cameraView, false);
            if(Camera.getNumberOfCameras()<=1){
                mMaskView.findViewById(R.id.changeCamera).setVisibility(View.GONE);
            }
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

    public View.OnTouchListener record() {
        return new View.OnTouchListener() {
            public boolean animationCancel = false;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        animationCancel = false;
                        view.setBackgroundResource(R.drawable.video_record_selected);
                        animator = loadAnimator(context, R.animator.record_animation);
                        animator.setTarget(view);
                        animator.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (!animationCancel) {
                                    animator.start();
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                                super.onAnimationRepeat(animation);

                            }
                        });
                        animator.start();

                        isVideoRecorderReady = prepareVideoRecorder();
                        if(isVideoRecorderReady) {
                            if (camera != null) {
                                mMediaRecorder.start();
                                startTime = System.currentTimeMillis();
                                isRecording = true;
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        animationCancel = true;
                        view.setBackgroundResource(R.drawable.video_record);
                        if(isRecording){
                            stopRecording();
                        }
                        break;
                }


                return true;
            }
        };
    }

    private void stopRecording() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaRecorder.setPreviewDisplay(null);
            try {
                mMediaRecorder.stop();
                stopTime = System.currentTimeMillis();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isRecording = false;
        releaseMediaRecorder(); // release the MediaRecorder object
        try {

            if((stopTime-startTime)/1000>=5) {
                playVideo();
            }else{
                maskViewHolder.mTooShortHint.setVisibility(View.VISIBLE);
                maskViewHolder.mTooShortHint.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        maskViewHolder.mTooShortHint.setVisibility(View.GONE);
                    }
                }, 2000);
            }
            /*Intent intent = new Intent(Intent.ACTION_VIEW);
            String type = "video/mp4";
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory()+"/video.mp4");
            intent.setDataAndType(uri, type);
            context.startActivity(intent);*/


            File file = new File(videoPath);
            double mb = file.length() * 1.0 / 1024 / 1024;
            MediaMetadataRetriever retr = new MediaMetadataRetriever();
            retr.setDataSource(videoPath);
            String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT); // 视频高度
            String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH); // 视频宽度
            String rotation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION); // 视频旋转方向
            String duration = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            Log.e("TAG", "录取视频信息：w:h  " + width + ":" + height + "  duration:" + duration + "  rotation:" + rotation + "  大小：" + mb + "M");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void playVideo(){
        try {
            cameraView.setPlaying(true);
            playSurfaceView = new SurfaceView(context);
            playSurfaceView.setZOrderOnTop(true);
            playSurfaceView.setZOrderMediaOverlay(true);
            playSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            playHolder = playSurfaceView.getHolder();
            playHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    mediaPlayer.setDisplay(playHolder);
                }
                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                }
                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                }
            });
            playHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            mediaPlayer =new MediaPlayer();
            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    cameraView.addView(playSurfaceView);
                    cameraView.addPlayMaskView();
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.prepareAsync();

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
                         cameraView.setPlaying(false);

            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            cameraView.setPlaying(false);

            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            cameraView.setPlaying(false);

            e.printStackTrace();
        }



    }

    public View.OnClickListener changeCamera() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isBackCamera) {
                        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                            Camera.CameraInfo info = new Camera.CameraInfo();
                            Camera.getCameraInfo(i, info);
                            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                                CameraVideoController.this.stopPreview();
                                cameraId = i;
                                startPreview();
                            }
                        }
                }else {
                    CameraVideoController.this.stopPreview();
                    startPreview();
                }
            }
        };
    }


    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            //关键语句
            mediaPlayer.reset();

            mediaPlayer.release();
            mediaPlayer = null;
        }

    }


    public View addPlayMaskView(int layoutId) {
        mPlayMaskView = LayoutInflater.from(context).inflate(layoutId, cameraView, false);
        cameraView.addView(mPlayMaskView);
        maskViewHolder = cameraView.new MaskViewHolder(mPlayMaskView);
        return mPlayMaskView;
    }

    public View.OnClickListener choose() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        };
    }

    public View.OnClickListener returnPreview() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopPlaying();
            }
        };
    }

    public void stopPlaying() {
        releasePlayer();
        cameraView.setPlaying(false);
        cameraView.removeView(playSurfaceView);
        cameraView.removeView(mPlayMaskView);
    }
}

package com.austin.camara.Video;

import android.content.Context;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.austin.camara.CameraControllInterface;
import com.austin.camara.CameraSettingInterface;
import com.austin.camara.R;

/**
 * Created by gy on 2017/8/8.
 */

public class CameraVideoView extends RelativeLayout implements CameraControllInterface{
    private Context context;
    private CameraSettingInterface mCameraInterface;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraVideoController mController;
    private MaskViewHolder maskViewHolder;
    private boolean isPlaying = false;

    public CameraVideoView(Context context) {
        super(context);
        init(context);
    }


    public CameraVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public CameraVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mController = new CameraVideoController(this);
    }


    public void onResume(CameraSettingInterface cameraInterface) {
        mController.mCameraSettingInterface = cameraInterface;
        mController.startPreview();
    }

    public void onStop() {
        mController.stopPreview();
    }

    public void addMaskView() {
        maskViewHolder = mController.addMaskView(R.layout.layout_camera_video_mask);
        maskViewHolder.mTakePicture.setOnTouchListener(mController.record());
        maskViewHolder.mChangeCamera.setOnClickListener(mController.changeCamera());
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                maskViewHolder.background.setVisibility(GONE);
            }
        }.start();
    }

    public View addPlayMaskView() {
        View view = mController.addPlayMaskView(R.layout.layout_camera_video_play_mask2);
        view.findViewById(R.id.choose).setOnClickListener(mController.choose());
        view.findViewById(R.id.back).setOnClickListener(mController.returnPreview());
        return view;
    }

    public void setPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public void stopPlaying() {
        mController.stopPlaying();
    }


    class MaskViewHolder{
        private Button mTakePicture;
        private View background;
        private ImageView mChangeCamera;


        public MaskViewHolder(View maskView) {
            mTakePicture = (Button) maskView.findViewById(R.id.takePicture);
            background = maskView.findViewById(R.id.background);
            mChangeCamera = (ImageView) findViewById(R.id.changeCamera);
        }
    }

}

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



    class MaskViewHolder{
        private Button mTakePicture;
        private View background;
        public MaskViewHolder(View maskView) {
            mTakePicture = (Button) maskView.findViewById(R.id.takePicture);
            background = maskView.findViewById(R.id.background);
        }
    }

}

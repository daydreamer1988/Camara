package com.austin.camara;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

/**
 * Created by gy on 2017/8/8.
 */

public class CameraView extends RelativeLayout implements CameraControllInterface{
    private Context context;
    private CameraSettingInterface mCameraInterface;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraController mController;
    private MaskViewHolder maskViewHolder;

    public CameraView(Context context) {
        super(context);
        init(context);
    }


    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);

    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CameraView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        mController = new CameraController(this);
    }

    public void setCamaraSettingInterface(CameraSettingInterface cameraInterface){
        mController.mCameraSettingInterface = cameraInterface;
    }

    public void onResume() {
        mController.startPreview();
    }

    public void onPause() {
        mController.stopPreview();
    }

    public void addMaskView() {
        maskViewHolder = mController.addMaskView(R.layout.layout_camera_surface);
        maskViewHolder.mTakePicture.setOnClickListener(mController.takePicture());
    }


    interface CameraSettingInterface {
        void onNoCamara();

        void onCameraInavailable();

        int[] onGetProposalPreviewSize();

        int onSetOrientation();

    }

    class MaskViewHolder{
        private Button mTakePicture;

        public MaskViewHolder(View maskView) {
            mTakePicture = (Button) maskView.findViewById(R.id.takePicture);
        }
    }


}

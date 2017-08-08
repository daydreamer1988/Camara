package com.austin.camara;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

public class CamaraPictureActivity extends AppCompatActivity implements CameraView.CameraSettingInterface {
    private CameraView mCameraView;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camara_picture);
        mCameraView = (CameraView) findViewById(R.id.cameraView);
        mCameraView.setCamaraSettingInterface(this);
    }


    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, CamaraPictureActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }


    @Override
    public void onNoCamara() {
        new AlertDialog.Builder(this)
                .setMessage("onNoCamara").show();
    }

    @Override
    public void onCameraInavailable() {
        new AlertDialog.Builder(this)
                .setMessage("onCameraInavailable").show();
    }


    @Override
    public int[] onGetProposalPreviewSize() {
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
//        return new int[]{480, 320};

        return new int[]{widthPixels,heightPixels};
//        return null;
    }
}

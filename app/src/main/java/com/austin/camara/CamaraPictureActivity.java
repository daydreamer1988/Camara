package com.austin.camara;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CamaraPictureActivity extends AppCompatActivity implements CameraView.CameraSettingInterface {
    private CameraView mCameraView;
    private Camera mCamera;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camara_picture);
        root = (LinearLayout) findViewById(R.id.root);
        checkCameraHardware();
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, CamaraPictureActivity.class));
    }
    public void checkCameraHardware() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "当前没有相机设备", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView = new CameraView(this);
        root.addView(mCameraView);
        mCameraView.onResume(CamaraPictureActivity.this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        root.removeView(mCameraView);
        mCameraView.onStop();

    }

    @Override
    public void onNoCamara() {

        new AlertDialog.Builder(this)
                .setMessage("onNoCamara").show();
    }

    @Override
    public void onCameraInavailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(CamaraPictureActivity.this)
                        .setMessage("onCameraInavailable").show();
            }
        });

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

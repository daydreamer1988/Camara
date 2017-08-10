package com.austin.camara.Video;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.austin.camara.CameraSettingInterface;
import com.austin.camara.R;

public class CameraVideoActivity extends AppCompatActivity implements CameraSettingInterface {
    private CameraVideoView mCameraVideoView;
    private Camera mCamera;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_camara_root);
        root = (LinearLayout) findViewById(R.id.root);
        checkCameraHardware();
    }

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, CameraVideoActivity.class));
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
        mCameraVideoView = new CameraVideoView(this);
        root.addView(mCameraVideoView);
        mCameraVideoView.onResume(CameraVideoActivity.this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        mCameraVideoView.onStop();
        root.removeView(mCameraVideoView);

    }



    @Override
    public void onCameraInavailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(CameraVideoActivity.this)
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

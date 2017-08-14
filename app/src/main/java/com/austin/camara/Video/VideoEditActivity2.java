package com.austin.camara.Video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.austin.camara.CustomScrollView;
import com.austin.camara.R;

public class VideoEditActivity2 extends AppCompatActivity {

    private String filePath = "";
    private CustomScrollView scrollView;
    private LinearLayout linearLayout;
    private static Context context;
    private ImageView imageviewPreview;
    private ProgressBar mProgressBar;


    public static void startActivity(Context context, String filePath, int requestCode) {
        Intent intent = new Intent(context, VideoEditActivity2.class);
        intent.putExtra("filePath", filePath);
        ((Activity)context).startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_edit);
        context=this;
        filePath = getIntent().getStringExtra("filePath");
        scrollView = (CustomScrollView) findViewById(R.id.scrollView);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
        imageviewPreview=(ImageView) findViewById(R.id.imageView);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        scrollView.setOnScrollListener(new CustomScrollView.OnScrollListener() {
            @Override
            public void onScroll(Bitmap bitmap) {
                imageviewPreview.setImageBitmap(bitmap);
                mProgressBar.setVisibility(View.GONE);
            }
        });

        scrollView.setVideoSource(filePath);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        scrollView.onDestory();
    }
}

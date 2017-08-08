package com.austin.camara;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1234;
    private RecyclerView mRecyclerView;
    private List<String> mData = new ArrayList<>();
    private Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(MainActivity.this, RecyclerView.VERTICAL));
        mRecyclerView.setAdapter(new MainAdapter());
    }

    private void initData() {
        mData.add("系统相机-拍照");
        mData.add("系统相机-录像");
        mData.add("自定义相机-拍照");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CamaraUtil.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
//                PicturePreviewActivity.startActivity(MainActivity.this, uri);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "image/*");
                startActivity(intent);
            }
        } else if (requestCode == CamaraUtil.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "video/mp4");
                startActivity(intent);
                try {
                    File file = new File(new URI(uri.toString()));
                    Log.e("TAG", "filesize:" + file.length()/1024);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MainAdapter extends RecyclerView.Adapter {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            TextView textView = new TextView(MainActivity.this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpTopx(50));
            textView.setBackgroundColor(Color.GRAY);
            textView.setGravity(Gravity.CENTER);
            textView.setLayoutParams(layoutParams);
            return new MainHolder(textView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((TextView) ((MainHolder) holder).itemView).setText(mData.get(position));

            ((MainHolder) holder).itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (position){
                        case 0:
                            File file = new File(Environment.getExternalStorageDirectory(), "pic.png");
                            uri = new CamaraUtil(MainActivity.this).startPictureCamara(file);
                            break;

                        case 1:
                            File file1 = new File(Environment.getExternalStorageDirectory(), "video.mp4");
                            if(file1.exists()){
                                file1.delete();
                            }
                            uri = new CamaraUtil(MainActivity.this).startVideoCamara(file1);

                            break;
                        case 2:
                            CamaraPictureActivity.startActivity(MainActivity.this);
                            break;
                    }
                }
            });

        }


        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    private int dpTopx(int dp) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics())+0.5);
    }

    private class MainHolder extends RecyclerView.ViewHolder{

        public MainHolder(View itemView) {
            super(itemView);
        }
    }
}

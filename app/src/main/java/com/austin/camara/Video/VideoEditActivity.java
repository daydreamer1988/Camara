package com.austin.camara.Video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.austin.camara.R;

import java.util.ArrayList;
import java.util.List;

public class VideoEditActivity extends AppCompatActivity {

    private String filePath = "";
    private HorizontalScrollView scrollView;
    private LinearLayout linearLayout;

    public static void startActivity(Context context, String filePath, int requestCode) {
        Intent intent = new Intent(context, VideoEditActivity.class);
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

        scrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
        linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
//        rl_l=(RelativeLayout) findViewById(R.id.rl_l);
        imageviewPreview=(ImageView) findViewById(R.id.imageView);
//        ivw_to2=(ImageView) findViewById(R.id.ivw_to2);
//        Uri uri=Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.test);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowWidth = wm.getDefaultDisplay().getWidth();
        Uri uri = Uri.parse(filePath);
        bitmaps=new ArrayList<Bitmap>();
        getVideoThumbnail(uri);
       /* linearLayout.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return (mGestureDetector != null)&& mGestureDetector.onTouchEvent(event);
            }
        });*/
        lp=(android.widget.LinearLayout.LayoutParams) imageviewPreview.getLayoutParams();
//        mGestureDetector = new GestureDetector(this,new CoverGestureListener());
        //


    }


    private static Context context;
//    private RelativeLayout rl_l;
    private ImageView imageviewPreview;
    private GestureDetector mGestureDetector;
    private LinearLayout.LayoutParams lp;
    private List<Bitmap> bitmaps;
    private int windowWidth, imageWidth,tWidth;
    private int arrayWidth[];

    public void onWindowFocusChanged(boolean hasFocus) {
        imageWidth =imageviewPreview.getWidth();
        imageWidth = windowWidth - imageWidth;
        tWidth= imageWidth /10;
        arrayWidth=new int[10];
        for(int i=0;i<10;i++){
            arrayWidth[i]=tWidth+(i*tWidth);
        }
        seekTo(0);
        super.onWindowFocusChanged(hasFocus);
    }
    private void seekTo(float paramFloat) {
        float f = paramFloat - this.imageviewPreview.getWidth() / 2;
        if(f<0){
            f=0;
        }if(f> imageWidth){
            f= imageWidth;
        }
        lp.leftMargin = ((int) f);
        for(int i=0;i<arrayWidth.length;i++){
            if(arrayWidth[i]>=f){
                imageviewPreview.setImageBitmap(bitmaps.get(i));
//                ivw_to2.setImageBitmap(bitmaps.get(i));
                break;
            }
        }
        this.imageviewPreview.setLayoutParams(this.lp);
    }
    /*private  class CoverGestureListener extends GestureDetector.SimpleOnGestureListener {
        public boolean onDown(MotionEvent e) {
            seekTo(e.getX());
            return true;
        }
        public boolean onScroll(MotionEvent e1, MotionEvent e2,float distanceX, float distanceY) {
            float f = e2.getX();
            seekTo(f);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }*/
    public void getVideoThumbnail(Uri uri) {
        Bitmap bitmap=null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            int seconds=Integer.valueOf(time)/1000;
            int timeS=Integer.valueOf(time)/10;
            for(int i=1;i<=10;i++){
                bitmap=retriever.getFrameAtTime(i*timeS*1000,MediaMetadataRetriever.OPTION_CLOSEST);
                bitmaps.add(bitmap);
                addImgView(bitmap);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
    }
    public  void addImgView(Bitmap bitmap){



        ImageView imageView=new ImageView(context);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(windowWidth/7, ViewGroup.LayoutParams.MATCH_PARENT);
//        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        lp.weight=1;
        imageView.setLayoutParams(params);
        imageView.setImageBitmap(bitmap);
        linearLayout.addView(imageView);
    }


}

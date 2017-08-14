package com.austin.camara;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gy on 2017/8/14.
 */

public class CustomScrollView extends HorizontalScrollView {

    private int viewWidth;
    private int viewHeight;
    private WindowManager windowManager;
    private Context context;
    private int screenWidth;
    private int videoBitmapWidth;
    private int videoBitmapHeight;
    private int targetBitmapHeight;
    public static int targetBitmapWidth;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private LinearLayout container;
    public static int margin;
    private OnScrollListener listener;
    private int scrollLength;
    private int videoLength;
    private MediaMetadataRetriever retriever;
    private boolean isRetriving;
    private AsyncTask<Integer, Void, Bitmap> asyncTask;

    public CustomScrollView(Context context) {
        super(context);
        init(context);
    }


    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context) {
        setBackgroundColor(Color.LTGRAY);
        setClickable(true);
        setFocusableInTouchMode(true);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        setClipToPadding(false);
        this.context = context;
        screenWidth = windowManager.getDefaultDisplay().getWidth();
    }



    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewWidth = w;
        viewHeight = h;
        targetBitmapHeight = viewHeight;
        targetBitmapWidth = (int) (videoBitmapWidth * 1.0 * targetBitmapHeight / videoBitmapHeight);
        container = (LinearLayout) getChildAt(0);
        for (int i = 0; i < bitmaps.size(); i++) {
            Bitmap bitmap = bitmaps.get(i);
            Bitmap bitmapScaled = Bitmap.createScaledBitmap(bitmap, targetBitmapWidth, targetBitmapHeight-margin*2, true);
            bitmap.recycle();
            bitmaps.set(i, bitmapScaled);
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(targetBitmapWidth, targetBitmapHeight-margin*2);
            layoutParams.setMargins(margin/2, 0, margin/2, 0);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageBitmap(bitmaps.get(i));
            container.addView(imageView);
        }

        onScrollChanged(0, 0, 0, 0);

        int left = w / 2 - targetBitmapWidth / 2;

        setPadding(left, margin, left-margin, margin);
        setClipToPadding(false);
    }



    public void setVideoSource(String path){
        Uri uri = null;
        if(Build.VERSION.SDK_INT>=24){
            uri = FileProvider.getUriForFile(context, context.getPackageName(), new File(path));
        }else{
            uri = Uri.parse(path);
        }
        getVideoThumbnail(uri);
    }

    public void getVideoThumbnail(Uri uri) {
        Bitmap bitmap=null;
        retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            videoLength = Integer.valueOf(time);
            int millis=Integer.valueOf(time);

            int span = 0;
            if(millis>90000){
                span = millis / 30;
            }else if (millis > 30000) {
                span = millis / 20;
            }else if(millis > 10000){
                span = millis / 10;
            }else{
                span = 1000;
            }

            for(int i=1;i<=millis;i+=span){
                bitmap = retriever.getFrameAtTime(i*1000, MediaMetadataRetriever.OPTION_CLOSEST);

                videoBitmapWidth = bitmap.getWidth();
                videoBitmapHeight = bitmap.getHeight();
                bitmaps.add(bitmap);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.e("TAG", "l:" + l + "oldl:" + oldl);
        scrollLength = container.getWidth() - (targetBitmapWidth + margin);
        int currentPositionMillis = l * videoLength / scrollLength;
        Log.e("TAG", currentPositionMillis + "");
        if (currentPositionMillis > videoLength) {
            currentPositionMillis = videoLength;
        }
        if(listener!=null && !isRetriving){
            asyncTask = new AsyncTask<Integer, Void, Bitmap>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    isRetriving = true;
                }

                @Override
                protected Bitmap doInBackground(Integer... params) {
                    return retriever.getFrameAtTime(params[0], MediaMetadataRetriever.OPTION_CLOSEST);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    super.onPostExecute(bitmap);
                    isRetriving = false;
                    if (listener != null) {
                        listener.onScroll(bitmap);
                    }
                }
            };

            asyncTask.execute(currentPositionMillis * 1000);
        }
    }


    public void onDestory() {
        try {
            if (retriever != null) retriever.release();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public interface OnScrollListener{
        void onScroll(Bitmap bitmap);
    }

    public void setOnScrollListener(OnScrollListener listener) {
        this.listener = listener;
    }
}

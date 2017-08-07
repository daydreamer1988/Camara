package com.austin.camara;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;

/**
 * Created by gy on 2017/8/7.
 */

public class CamaraUtil {

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 65;
    public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 228;

    private Context context;

    public CamaraUtil(Context context) {
        this.context = context;
    }

    /**
     * 启动系统相机照相
     * 如果自定义存储路径的话，在onActivityResult中获得的intent为null
     * @param file
     */
    public Uri startPictureCamara(File file) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri fileUri = getOutputMediaFileUri(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        ((Activity) context).startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        return fileUri;
    }


    private Uri getOutputMediaFileUri(File file) {
        if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(context, context.getPackageName(), file);
        }else{
            return Uri.fromFile(file);
        }
    }

    public Uri startVideoCamara(File file) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri fileUri = getOutputMediaFileUri(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT , 500*1024);
        ((Activity) context).startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        return fileUri;
    }
}

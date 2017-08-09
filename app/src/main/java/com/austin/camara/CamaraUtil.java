package com.austin.camara;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

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


    public static void choosePreviewSize(Camera.Parameters parameters, int width, int height) {
        Camera.Size finalSize = null;
        Camera.Size finalSize2 = null;

        List sizeList = parameters.getSupportedPreviewSizes();
        ArrayList validSize = new ArrayList();
        Iterator validSizeIterator = sizeList.iterator();

        while (validSizeIterator.hasNext()) {
            Camera.Size item = (Camera.Size) validSizeIterator.next();
            if (item.width >= width && item.height >= height && item.height != item.width) {
                validSize.add(item);
            }
        }

        Camera.Size[] sortedArray = (Camera.Size[]) validSize.toArray(new Camera.Size[0]);
        Arrays.sort(sortedArray, new Comparator<Camera.Size>() {
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.width * lhs.height - rhs.width * rhs.height;
            }
        });

        if (sortedArray.length == 0) {
            finalSize =parameters.getPreviewSize();
        } else {
            finalSize = sortedArray[0];
        }

        List sizeList2 = parameters.getSupportedPictureSizes();
        ArrayList validSize2 = new ArrayList();
        Iterator validSizeIterator2 = sizeList2.iterator();

        while (validSizeIterator2.hasNext()) {
            Camera.Size item = (Camera.Size) validSizeIterator2.next();
            if (item.width >= width && item.height >= height && item.height != item.width) {
                validSize2.add(item);
            }
        }


        Camera.Size[] sortedArray2 = (Camera.Size[]) validSize2.toArray(new Camera.Size[0]);
        Arrays.sort(sortedArray2, new Comparator<Camera.Size>() {
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return lhs.width * lhs.height - rhs.width * rhs.height;
            }
        });

        if (sortedArray2.length == 0) {
            finalSize2 =parameters.getPreviewSize();
        } else {
            finalSize2 = sortedArray2[0];
        }

        Log.e("TAG", "计算的Preview宽高：" + finalSize.width + ":" + finalSize.height);
        Log.e("TAG", "计算的Picture宽高：" + finalSize2.width + ":" + finalSize2.height);

        parameters.setPreviewSize(finalSize.width, finalSize.height);
        parameters.setPictureSize(finalSize2.width, finalSize2.height);
    }

    public static void chooseMaxSize(Camera.Parameters parameters) {
        Camera.Size maxPictureSize = parameters.getSupportedPictureSizes().get(0);
        Camera.Size maxPreviewSize = parameters.getSupportedPreviewSizes().get(0);
        for (int i = 0; i < parameters.getSupportedPictureSizes().size(); i++) {
            Camera.Size s = parameters.getSupportedPictureSizes().get(i);
            if (s.width > maxPictureSize.width) {
                maxPictureSize = s;
            }
            if(s.width==maxPictureSize.width&&s.height>maxPictureSize.height){
                maxPictureSize = s;
            }
        }
        for (int i = 0; i < parameters.getSupportedPreviewSizes().size(); i++) {
            Camera.Size s = parameters.getSupportedPreviewSizes().get(i);
            if (s.width > maxPreviewSize.width) {
                maxPreviewSize = s;
            }
            if(s.width==maxPreviewSize.width&&s.height>maxPreviewSize.height){
                maxPreviewSize = s;
            }
        }

        Log.e("TAG", "最大Preview宽高：" + maxPreviewSize.width + ":" + maxPreviewSize.height);
        Log.e("TAG", "最大Picture宽高：" + maxPictureSize.width + ":" + maxPictureSize.height);
        parameters.setPictureSize(maxPictureSize.width, maxPictureSize.height);
        parameters.setPreviewSize(maxPreviewSize.width, maxPreviewSize.height);
    }
}

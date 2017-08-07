package com.austin.camara;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class PicturePreviewActivity extends AppCompatActivity {

    private Uri uri;
    private ImageView mImageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_preview);

        mImageView = (ImageView) findViewById(R.id.imageView);

        ContentResolver contentResolver = getContentResolver();
        uri = getIntent().getParcelableExtra("uri");
        try {
            bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
            mImageView.setImageBitmap(bitmap);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void startActivity(Context context, Uri uri) {
        Intent intent = new Intent(context, PicturePreviewActivity.class);
        intent.putExtra("uri", uri);
        context.startActivity(intent);

    }
}

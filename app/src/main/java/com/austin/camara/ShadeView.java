package com.austin.camara;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import static com.austin.camara.CustomScrollView.margin;

/**
 * Created by gy on 2017/8/14.
 */

public class ShadeView extends View {

    private RectF rectF;
    private Paint paint;

    public ShadeView(Context context) {
        super(context);
        init();
    }


    public ShadeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShadeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int left = w / 2 - CustomScrollView.targetBitmapWidth / 2;
        rectF = new RectF(left, margin/2, left+margin + CustomScrollView.targetBitmapWidth, h-margin/2);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#80ffffff"));
        canvas.drawRoundRect(rectF, 10, 10, paint);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.parseColor("#55C485"));
        paint.setStrokeWidth(margin);
        canvas.drawRoundRect(rectF, 10, 10, paint);
    }
}

package com.aaroncohen.chiefdup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

public class PaintPreview extends View {

    private float width;
    private Paint paint;

    public PaintPreview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        width = 25f;

        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.black));
        paint.setStrokeWidth(5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(this.getWidth()/2, this.getHeight()/2, width/2, paint);
    }

    //methods to interface with the view
    public void setWidth(float width) {
        this.width = width;
        invalidate();
    }

    public void setColor(@ColorInt int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        invalidate();
    }

    public void setEraserColor() {
        paint.setColor(getResources().getColor(R.color.black));
        paint.setStyle(Paint.Style.STROKE);
        invalidate();
    }
}

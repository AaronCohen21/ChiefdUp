package com.aaroncohen.chiefdup;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawCanvas extends View {

    Paint paint;

    float xPos, yPos;

    public DrawCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawCircle(xPos, yPos, 100, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPos = event.getRawX();
        yPos = event.getRawY();
        invalidate();

        return super.onTouchEvent(event);
    }
}

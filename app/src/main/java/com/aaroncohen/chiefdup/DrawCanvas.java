package com.aaroncohen.chiefdup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

public class DrawCanvas extends View {

    private Paint paint;
    private Path path;

    //used to store old lines with their specific paths and paints
    class OldLine {
        public Paint paint;
        public Path path;

        public OldLine(Path path, Paint paint) {
            this.paint = paint;
            this.path = path;
        }
    }
    private ArrayList<OldLine> oldLines;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public DrawCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);

        paint.setColor(getResources().getColor(R.color.black));
        paint.setStrokeWidth(50f);

        path = new Path();
        oldLines = new ArrayList<>();
    }

    /*
    =========
    DRAW CODE
    =========
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw old conserved paths
        for (OldLine oldLine : oldLines) {
            canvas.drawPath(oldLine.path, oldLine.paint);
        }
        //draw current path
        canvas.drawPath(path, paint);
    }

    //handles all the code for drawing to the screen
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xPos = event.getX();
        float yPos = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(xPos, yPos);
                return true;

            case MotionEvent.ACTION_MOVE:
                path.lineTo(xPos, yPos);
                break;

            case MotionEvent.ACTION_UP:
                /*
                this code stores the line drawn and resets the paint and path
                this allows there to be an undo() function
                 */

                //preserve color and width
                @ColorInt int color = paint.getColor();
                float width = paint.getStrokeWidth();

                //store the settings for the previous line
                oldLines.add(new OldLine(path, paint));

                //reset path and paint as to not damage old lines
                path = new Path();
                paint = new Paint();
                paint.setAntiAlias(true);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(width);

                //restore current settings
                paint.setColor(color);
                paint.setStrokeWidth(width);
                break;

            default:
                return false;
        }

        invalidate();

        return super.onTouchEvent(event);
    }

    /*
    ==============
    INTERFACE CODE
    ==============
     */

    public void undo() {
        if (oldLines.size() > 0) {
            oldLines.remove(oldLines.size() - 1);
            invalidate();
        }
    }

    //use to change draw color of the DrawCanvas
    public void setDrawColor(@ColorInt int color) {
        //preserve width
        float width = paint.getStrokeWidth();

        //store the settings for the previous line
        oldLines.add(new OldLine(path, paint));

        //reset path and paint as to not damage old lines
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(width);

        //change paint color
        paint.setColor(color);
    }

    //use to change stroke width of the DrawCanvas
    public void setDrawStroke(float width) {
        //preserve color
        @ColorInt int color = paint.getColor();

        //store the settings for the previous line
        oldLines.add(new OldLine(path, paint));

        //reset path and paint as to not damage old lines
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);

        //change paint color
        paint.setStrokeWidth(width);
    }

    /**
     * Original code from: https://stackoverflow.com/questions/5536066/convert-view-to-bitmap-on-android
     * the code has been modified to return a bitmap of the instantiated View
     *
     * @return a bitmap of the DrawCanvas object
     */
    public Bitmap getBitmap() {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = this.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        this.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
}

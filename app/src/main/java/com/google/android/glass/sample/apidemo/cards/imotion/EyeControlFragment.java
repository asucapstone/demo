package com.google.android.glass.sample.apidemo.cards.imotion;

/**
 * Created by Hector on 10/10/15.
 */

import java.text.DecimalFormat;

import com.eyetechds.quicklink.*;
import com.eyetechds.quicklink.QLCalibrationType;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;

public class EyeControlFragment extends QLFragment
{
    EyeControlSurfaceView surfaceView = null;

    public EyeControlFragment()
    {
        super();
        objectName = "EyeControl";
    }

    // Creates a thread that polls the QLDeviceGetFrameData function
    // and draws the cursor
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {

        surfaceView = new EyeControlSurfaceView(getActivity());
        return surfaceView;
    }

    @Override
    public void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();
        surfaceView.onResumeSurfaceView();
    }

    @Override
    public void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
        surfaceView.onPauseSurfaceView();
    }

    class EyeControlSurfaceView extends SurfaceView implements Runnable
    {
        // Example usage of surface view taken from:
        // http://android-coding.blogspot.com/2011/05/basic-implementation-of-surfaceview.html
        Thread thread = null;
        SurfaceHolder surfaceHolder;
        volatile boolean running = false;
        Paint paint = new Paint();
        private Bitmap mPointer;
        private Bitmap background;
        int fontSize = 20;
        float fontSizeDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, fontSize, getResources()
                        .getDisplayMetrics());
        int circleRadius = 10;
        float circleRadiusDp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, circleRadius, getResources()
                        .getDisplayMetrics());

        public EyeControlSurfaceView(Context context)
        {
            super(context);
            // TODO Auto-generated constructor stub
            surfaceHolder = getHolder();

            //mPointer = BitmapFactory.decodeResource(getResources(),R.drawable.pointer);
            background = generateRadialGradientBitmap();
        }

        void onResumeSurfaceView()
        {
            running = true;
            thread = new Thread(this);
            thread.start();
        }

        void onPauseSurfaceView()
        {
            boolean retry = true;
            running = false;
            while (retry)
            {
                try
                {
                    thread.join();
                    retry = false;
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        //		@Override
        public void run()
        {
            QLFrameData frame = new QLFrameData();
            DecimalFormat df = new DecimalFormat("#.0");
            double x = 0;
            double y = 0;
            int width;
            int height;
            String XYtext = "";
            // TODO Auto-generated method stub
            while (running)
            {
                frame = qlDevice.getFrame(500);

                if (frame != null)
                {
                    if (frame.weightedGazePoint.valid &&
                            (frame.leftEye.calibrated || frame.rightEye.calibrated))
                    {
                        x = frame.weightedGazePoint.x;
                        y = frame.weightedGazePoint.y;
                    }
                    else
                    {
                        x = 0;
                        y = 0;
                    }
                    // TODO: Make sure that these values don't come through be ensuring
                    // a valid calibration is loaded.
                    if(Math.abs(x) > 200)
                        x = -99;
                    if(Math.abs(y) > 200)
                        y = -99;

                    XYtext = "x = " + df.format(x) + "; y = " + df.format(y)
                            + "; f = " + df.format(frame.focus);
                }
                else
                {
                    XYtext = "Null Frame...: " + qlDevice.getLastError();
                    // continue;
                }

                if (surfaceHolder.getSurface().isValid())
                {
                    Canvas canvas = surfaceHolder.lockCanvas();
                    // ... actual drawing on canvas
                    width = canvas.getWidth();
                    height = canvas.getHeight();
                    if(height != QLFragment.calibratedHeight)
                        Log.d("QL2", "CalibratedHeight != SurfaceViewHeight : " + height + " " + QLFragment.calibratedHeight);
                    x = x*width/100.;
                    y = y*height/100.;

                    // Fill the background with gray
                    canvas.drawRGB(192, 192, 192);

                    // Draw a grid of 100 px squares
                    drawGrid(canvas, paint);

                    //canvas.drawBitmap(background, (float) x - background.getWidth()/2, (float) y - background.getHeight()/2, paint);

                    // Draw a grid of 100 px squares
                    drawGrid(canvas, paint);

                    // Draw the cursor
//					canvas.drawBitmap(mPointer, (float) x, (float) y, paint);

                    Paint redPaint = new Paint();
                    redPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
                    redPaint.setColor(Color.RED);
                    canvas.drawCircle((float)x, (float)y, (float)circleRadiusDp, redPaint);

                    if (XYtext != null && XYtext.length() > 0)
                    {
                        paint.setTextSize(fontSizeDp);
                        paint.setColor(Color.BLACK);
                        paint.setStyle(Style.FILL);
                        paint.setTextAlign(Align.CENTER);
                        canvas.drawText(XYtext, canvas.getWidth() / 2,
                                canvas.getHeight() / 2, paint);
                    }
                    // canvas.drawARGB(0, 0, 0, 0);
                    // Paint redPaint = new Paint();
                    // redPaint.setColor(Color.RED);
                    // canvas.drawCircle(100, 100, 30, redPaint);

                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        void drawGrid(Canvas canvas, Paint paint)
        {
            int frameWidth = 8;
            int width, height;
            Rect mRect = new Rect();
            width = canvas.getWidth();
            height = canvas.getHeight();

            // draw a red frame around the outside edge
            paint.setColor(Color.RED);
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(frameWidth);// 8 pixels wide
            mRect.set(frameWidth / 2, frameWidth / 2, width - (frameWidth / 2),
                    height - (frameWidth / 2));
            canvas.drawRect(mRect, paint);

            //
            paint.setStrokeWidth(1);
            paint.setColor(Color.BLACK);
            for (int lineY = 100; lineY < height; lineY += 100)
            {
                canvas.drawLine(frameWidth, lineY, width - frameWidth, lineY,
                        paint);
            }
            for (int lineX = 100; lineX < width; lineX += 100)
            {
                canvas.drawLine(lineX, frameWidth, lineX, height - frameWidth,
                        paint);
            }
        }

        private Bitmap generateRadialGradientBitmap()
        {
            // http://stackoverflow.com/questions/2936803/how-to-draw-a-smooth-dithered-gradient-on-a-canvas-in-android
            int radius = 400;
            RadialGradient gradient = new RadialGradient(radius, radius, radius,
                    0xFFFFFFFF, 0x00FFFFFF,
                    android.graphics.Shader.TileMode.CLAMP);
            Paint paint = new Paint();
            paint.setDither(true);
            paint.setShader(gradient);

            Bitmap bitmap = Bitmap.createBitmap(radius*2, radius*2, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawCircle(radius, radius, radius, paint);

            return bitmap;
        }
    }
}
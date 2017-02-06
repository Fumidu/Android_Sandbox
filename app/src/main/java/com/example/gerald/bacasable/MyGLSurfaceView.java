package com.example.gerald.bacasable;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by Gérald on 21/01/2017.
 */

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;
    private ScaleGestureDetector mScaleDetector;
    private OrientationListener mOrientationListener;
    private float mScaleFactor = 1.f;
    private float [] mDeviceOrientation;

    private final float MOVE_FACTOR = 0.3f;
    private final float SCALE_FACTOR = 2f;
    private float mPreviousX;
    private float mPreviousY;
    private boolean isAnchored = false;

    public MyGLSurfaceView(Context context) {
        super(context);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        mOrientationListener = new OrientationListener(context);
        mOrientationListener.setOnNewOrientationListener(new OrientationListener.NewsOrientationListener() {
            @Override
            public void onNewOrientation(float[] orientationMatrix) {
                mDeviceOrientation = orientationMatrix;
            }
        });
        //mOrientationListener.setOnNewOrientationListener(
        //        matrix -> mDeviceOrientation = matrix);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mRenderer = new MyGLRenderer(this.getContext());

        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (isAnchored) return true;

        float x = e.getX();
        float y = e.getY();

        switch(e.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                float angleY = mRenderer.getAngleY() + (-dy * MOVE_FACTOR);
                angleY = Utils.Clamp(-90, 90, angleY);
                mRenderer.setAngleY(angleY);

                float angleX = mRenderer.getAngleX() + (dx * MOVE_FACTOR);
                mRenderer.setAngleX(angleX);
                requestRender();

                break;
        }

        mPreviousX = x;
        mPreviousY = y;

        mScaleDetector.onTouchEvent(e);

        return true;
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor /= Math.pow(detector.getScaleFactor(), SCALE_FACTOR);

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 2.0f));

            mRenderer.setScale(mScaleFactor);
            requestRender();
            return true;
        }
    }

    public boolean IsAnchored() { return isAnchored;}

    public void SetAnchored(boolean newVal) { isAnchored = newVal;}

    public void SwitchAnchored() { isAnchored = !isAnchored;}
}

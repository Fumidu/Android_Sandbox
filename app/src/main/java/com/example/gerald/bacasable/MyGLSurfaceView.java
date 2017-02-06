package com.example.gerald.bacasable;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
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
    private float [] mDeviceOrientation = new float[16];
    private float [] mInverted = new float[16];
    private float [] mRotationMatrixX = new float[16];
    private float [] mRotationMatrixY = new float[16];
    private float [] mRotationInit = new float[16];

    private final float MOVE_FACTOR = 0.3f;
    private final float SCALE_FACTOR = 2f;
    private final float RAD2DEG = 180.0f / (float)Math.PI;
    private float mPreviousX;
    private float mPreviousY;
    private float angleX = 0.0f;
    private float angleY = 0.0f;
    private boolean isAnchored = false;

    public MyGLSurfaceView(Context context) {
        super(context);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

        mOrientationListener = new OrientationListener(context);
        mOrientationListener.setOnNewOrientationListener(new OrientationListener.NewsOrientationListener() {
            @Override
            public void onNewOrientation(float[] orientation) {
                if (!isAnchored) return;

                Log.d("onNewOrientation", String.format("Azimuth : %1$.3f Pitch : %2$.3f Roll : %3$.3f",
                        orientation[0], orientation[1], orientation[2]));

                //System.arraycopy(orientationMatrix, 0, mDeviceOrientation, 0, mDeviceOrientation.length);
                //Matrix.invertM(mInverted, 0, mDeviceOrientation, 0);
                //Matrix.rotateM(mDeviceOrientation, 0, -90, 1, 0, 0);
                //Matrix.rotateM(mInverted, 0, -90, 1, 0, 0);
                Matrix.setIdentityM(mDeviceOrientation, 0);
                Matrix.rotateM(mDeviceOrientation, 0, -90, 1, 0, 0);
                Matrix.rotateM(mDeviceOrientation, 0, orientation[2] * RAD2DEG, 0, 0, -1);
                Matrix.rotateM(mDeviceOrientation, 0, orientation[1] * RAD2DEG, -1, 0, 0);
                Matrix.rotateM(mDeviceOrientation, 0, orientation[0] * RAD2DEG, 0, 1, 0);

                mRenderer.setOrientation(mDeviceOrientation);
                requestRender();
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
        mScaleDetector.onTouchEvent(e);

        if (isAnchored) return true;

        float x = e.getX();
        float y = e.getY();

        switch(e.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                angleY = angleY + (-dy * MOVE_FACTOR);
                angleY = Utils.Clamp(-90, 90, angleY);
                angleX = angleX + (dx * MOVE_FACTOR);

                Matrix.setRotateM(mRotationMatrixY, 0, angleY, -1.0f, 0, 0);
                Matrix.setRotateM(mRotationMatrixX, 0, angleX, 0, -1.0f, 0);
                Matrix.multiplyMM(mDeviceOrientation, 0, mRotationMatrixY, 0, mRotationMatrixX, 0);
                mRenderer.setOrientation(mDeviceOrientation);

                requestRender();

                break;
        }

        mPreviousX = x;
        mPreviousY = y;

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

    @Override
    public void onResume(){
        super.onResume();
        mOrientationListener.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mOrientationListener.pause();
    }

}

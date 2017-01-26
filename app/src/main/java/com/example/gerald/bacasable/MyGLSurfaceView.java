package com.example.gerald.bacasable;


import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Gérald on 21/01/2017.
 */

public class MyGLSurfaceView extends GLSurfaceView {

    private final MyGLRenderer mRenderer;

    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    public MyGLSurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        mRenderer = new MyGLRenderer(this.getContext());

        setRenderer(mRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch(e.getAction()){
            case MotionEvent.ACTION_MOVE:
                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                float angleY = mRenderer.getAngleY() + (-dy * TOUCH_SCALE_FACTOR);
                angleY = Utils.Clamp(-90, 90, angleY);
                mRenderer.setAngleY(angleY);

                float angleX = mRenderer.getAngleX() + (dx * TOUCH_SCALE_FACTOR);
                mRenderer.setAngleX(angleX);
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}

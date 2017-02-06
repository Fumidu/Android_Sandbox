package com.example.gerald.bacasable;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.transition.Scene;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gérald on 21/01/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private CubeScene scene;
    private Context context;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public MyGLRenderer(Context c) {
        context = c;
        Matrix.setIdentityM(mOrientationMatrix, 0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glDepthRangef(0.0f, 1.0f);
        //GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        //GLES20.glDepthMask( true );

        scene = new CubeScene(context, R.drawable.test_pano);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        ratio = (float) width / height;
    }

    private void setProjectionMatrix(float ratio) {
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        float near = 2f;
        float far = 20f;
        if (ratio > 1) {
            Matrix.frustumM(mProjectionMatrix, 0, -ratio * mScale, ratio * mScale, -1 * mScale, 1 * mScale, near, far);
        } else {
            Matrix.frustumM(mProjectionMatrix, 0, -1f * mScale, 1f * mScale , -1f / ratio * mScale, 1f / ratio * mScale, near, far);
        }
    }

    private float[] mRotationMatrix = new float[16];
    @Override
    public void onDrawFrame(GL10 gl) {
        setProjectionMatrix(ratio);

        float[] scratch = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, 0f, 0f, 1f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation transformation
        //Matrix.setRotateM(mRotationMatrix, 0, mAngleY, -1.0f, 0, 0);
        //Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        //Matrix.setRotateM(mRotationMatrix, 0, mAngleX, 0, -1.0f, 0);
        //Matrix.multiplyMM(mMVPMatrix, 0, scratch, 0, mRotationMatrix, 0);

        //scene.draw(mMVPMatrix);

        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mOrientationMatrix, 0);
        scene.draw(scratch);
    }

    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public volatile float mScale = 1;

    public void setScale(float scale) { mScale = scale; }

    private float ratio = 1;

    public volatile float[] mOrientationMatrix = new float[16];

    public void setOrientation(float[] matrix) { mOrientationMatrix = matrix; }
}

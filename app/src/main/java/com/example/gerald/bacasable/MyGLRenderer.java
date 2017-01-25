package com.example.gerald.bacasable;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Gérald on 21/01/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    private Triangle mTriangle;
    //private Square mSquare;
    private Sprite mSprite;
    private Vector<Square> Squares = new Vector<Square>();
    private Vector<Sprite> Sprites = new Vector<Sprite>();
    private Context context;

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    public MyGLRenderer(Context c) {
        context = c;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        //GLES20.glDepthRangef(0.0f, 1.0f);
        //GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        //GLES20.glDepthMask( true );

        float size = 3f;

        //mSprite = new Sprite(1, 0, 0, 0, 0, 0, -3f ,context, R.drawable.test_texture);

        // -Z
        Sprites.add(new Sprite(3, 180, 0, 0, 0, 0, 3f ,context, R.drawable.back));

        // +Z
        Sprites.add(new Sprite(3, 0, 0, 180, 0, 0, 3f ,context, R.drawable.front));

        // -Y
        Sprites.add(new Sprite(3, 90, 0, 180, 0, 0, 3f ,context, R.drawable.bottom));

        //+Y
        Sprites.add(new Sprite(3, 270, 0, 180, 0, 0, 3f ,context, R.drawable.top));

        //-X
        Sprites.add(new Sprite(3, 180, 270, 0, 0, 0, 3f ,context, R.drawable.left));

        //+X
        Sprites.add(new Sprite(3, 180, 90, 0, 0, 0, 3f ,context, R.drawable.right));


        //Squares.add(new Square());
        // -Y
        Squares.add(new Square(
                new float[] {
                        -size, -size,  size,   // top left
                        -size, -size, -size,   // bottom left
                        size, -size, -size,    // bottom right
                        size, -size,  size,    // top right
                } ,
                new float[] {1f, 0f, 0f, 1.0f})); // red
        // +Y
        Squares.add(new Square(
                new float[] {
                        -size, size,  size,   // top left
                        -size, size, -size,   // bottom left
                        size, size, -size,    // bottom right
                        size, size,  size,    // top right
                } ,
                new float[] {0f, 1f, 0f, 1.0f})); // green
        // -Z
        Squares.add(new Square(
                new float[] {
                        -size, size, -size,  // top left
                        -size, -size, -size,   // bottom left
                        size, -size, -size,   // bottom right
                        size, size, -size,   // top right
                } ,
                new float[] {0f, 0f, 1f, 1.0f})); // blue
        // +Z
        Squares.add(new Square(
                new float[] {
                        -size, size, size,  // top left
                        -size, -size, size,   // bottom left
                        size, -size, size,   // bottom right
                        size, size, size,   // top right
                } ,
                new float[] {1f, 1f, 0f, 1.0f})); // yellow
        // -X
        Squares.add(new Square(
                new float[] {
                        -size, -size, size,   // top left
                        -size, -size, -size,  // bottom left
                        -size, size, -size,   // bottom right
                        -size, size, size,    // top right
                } ,
                new float[] {1f, 0f, 1f, 1.0f})); // magenta
        // +X
        Squares.add(new Square(
                new float[] {
                        size, -size, size,   // top left
                        size, -size, -size,  // bottom left
                        size, size, -size,   // bottom right
                        size, size, size,    // top right
                } ,
                new float[] {0f, 1f, 1f, 1.0f})); // cyan


        mTriangle = new Triangle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        float near = 2f;
        float far = 7;
        if (ratio > 1) {
            Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, near, far);
        } else {
            Matrix.frustumM(mProjectionMatrix, 0, -1f, 1f , -1f / ratio, 1f / ratio, near, far);
        }
    }

    private float[] mRotationMatrix = new float[16];
    @Override
    public void onDrawFrame(GL10 gl) {
        float[] scratch = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClearDepthf(1.0f);

        // Set the camera position (View matrix)
        //Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 0, 0f, 0f, 1f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        // Create a rotation transformation for the triangle
        //long time = SystemClock.uptimeMillis() % 4000L;
        //float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, mAngleY, -1.0f, 0, 0);
        //Matrix.setRotateM(mRotationMatrix, 0, angle, -1.0f, 0, 0);
        //Matrix.setRotateM(mRotationMatrix, 0, angle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);


        Matrix.setRotateM(mRotationMatrix, 0, mAngleX, 0, -1.0f, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, scratch, 0, mRotationMatrix, 0);

        //mSprite.draw(mMVPMatrix);
        for(Square s : Squares) {
            //s.draw(mMVPMatrix);
        }

        for(Sprite s : Sprites) {
            s.draw(mMVPMatrix);
        }
        //mSquare.draw(mMVPMatrix);
        //mTriangle.draw(mMVPMatrix);
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

    public volatile float mAngleY;

    public float getAngleY() {
        return mAngleY;
    }

    public void setAngleY(float angle) {
        mAngleY = angle;
    }

    public volatile float mAngleX;

    public float getAngleX() {
        return mAngleX;
    }

    public void setAngleX(float angle) {
        mAngleX = angle;
    }
}

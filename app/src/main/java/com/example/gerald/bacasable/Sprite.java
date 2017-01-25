package com.example.gerald.bacasable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Gérald on 24/01/2017.
 */

public class Sprite {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_TexCoordinate;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_TexCoordinate = a_TexCoordinate;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoordinate;" +
            "void main() {" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
            "}";

    private int mProgram;

    // Use to access and set the view transformation
    private int mMVPMatrixHandle;
    private int mPositionHandle;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    static final int COORDS_PER_VERTEX = 3;
    static final int vertexStride = COORDS_PER_VERTEX * 4;
    /** How many bytes per float. */
    private final int mBytesPerFloat = 4;

    static final float squareCoords[] = {
            -1f,  1f, 0f,  // top left
            -1f, -1f, 0f,  // bottom left
             1f,  1f, 0f,  // top right
             1f, -1f, 0f,  // bottom right
        };
    static final short drawOrder[] = { 0, 1, 2, 3, 2, 1 }; // order to draw vertices

    private float mPositionMatrix[];

    /** Store our model data in a float buffer. */
    private FloatBuffer mCubeTextureCoordinates;

    /** This will be used to pass in the texture. */
    private int mTextureUniformHandle;

    /** This will be used to pass in model texture coordinate information. */
    private int mTextureCoordinateHandle;

    /** Size of the texture coordinate data in elements. */
    private final int mTextureCoordinateDataSize = 2;

    /** This is a handle to our texture data. */
    private int mTextureDataHandle;

    // S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
    final float[] cubeTextureCoordinateData =
    {
        // Front face
            0f, 1f,
            0f, 0f,
            1f, 1f,
            1f, 0f,
            1f, 1f,
            0f, 0f,
    };

    public Sprite(float scale, float rotX, float rotY, float rotZ, float mvX, float mvY, float mvZ,
                  Context context, int texId)
    {
        InitPositionMatrix(scale, rotX, rotY, rotZ, mvX, mvY, mvZ);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;   // No pre-scaling
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), texId, options);

        Init(bitmap);
    }

    public Sprite(float scale, float rotX, float rotY, float rotZ, float mvX, float mvY, float mvZ,
                  Bitmap bitmap)
    {
        InitPositionMatrix(scale, rotX, rotY, rotZ, mvX, mvY, mvZ);
        Init(bitmap);
    }

    private void Init(Bitmap bitmap) {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());

        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinateData.length * mBytesPerFloat)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinateData).position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        //mTextureDataHandle = loadTexture(context, texId);
        mTextureDataHandle = loadTexture(bitmap);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        //GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram, "u_Texture");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Pass in the texture coordinate information
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false,
                0, mCubeTextureCoordinates);

        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        float[] scratch = new float[16];
        Matrix.multiplyMM(scratch, 0, mvpMatrix, 0, mPositionMatrix, 0);

        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, scratch, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    //public static int loadTexture(final Context context, final int resourceId)
    public static int loadTexture(Bitmap bitmap)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            //final BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inScaled = false;   // No pre-scaling

            // Read in the resource
            //final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }

    private void InitPositionMatrix(float scale, float rotX, float rotY, float rotZ, float mvX, float mvY, float mvZ){

        float[] scaleM = new float [16];
        float[] rotxM = new float [16];
        float[] rotyM = new float [16];
        float[] rotzM = new float [16];
        float[] transM = new float [16];
        float[] tmp1M = new float [16];
        float[] tmp2M = new float [16];
        mPositionMatrix = new float[16];

        Matrix.setIdentityM(rotxM, 0);
        Matrix.setIdentityM(scaleM, 0);
        Matrix.setIdentityM(rotyM, 0);
        Matrix.setIdentityM(rotzM, 0);
        Matrix.setIdentityM(transM, 0);

        Matrix.rotateM(rotxM, 0, rotX, 1f, 0f, 0f);
        Matrix.rotateM(rotyM, 0, rotY, 0f, 1f, 0f);
        Matrix.rotateM(rotzM, 0, rotZ, 0f, 0f, 1f);
        Matrix.translateM(transM, 0, mvX, mvY, mvZ);
        Matrix.scaleM(scaleM, 0, scale, scale, scale);

        Matrix.multiplyMM(tmp2M, 0, rotxM, 0, rotyM, 0);
        Matrix.multiplyMM(tmp1M, 0, tmp2M, 0, rotzM, 0);
        Matrix.multiplyMM(tmp2M, 0, tmp1M, 0, transM, 0);
        Matrix.multiplyMM(mPositionMatrix, 0, tmp2M, 0, scaleM, 0);
        LogMatrix(mPositionMatrix, "rotxM * rotyM * rotzM * tranM * scaleM = mPositionMatrix");
    }
    
    private void LogMatrix(float[] m, String name) {
        Log.d("Sprite", name);
        Log.d("Sprite", String.format("%1$.3f %2$.3f %3$.3f %4$.3f",
                m[0], m[1], m[2], m[3]));
        Log.d("Sprite", String.format("%1$.3f %2$.3f %3$.3f %4$.3f",
                m[4], m[5], m[6], m[7]));
        Log.d("Sprite", String.format("%1$.3f %2$.3f %3$.3f %4$.3f",
                m[8], m[9], m[10], m[11]));
        Log.d("Sprite", String.format("%1$.3f %2$.3f %3$.3f %4$.3f",
                m[12], m[13], m[14], m[15]));
    }
}

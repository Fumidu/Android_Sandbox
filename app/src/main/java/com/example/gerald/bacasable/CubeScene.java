package com.example.gerald.bacasable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by Gérald on 25/01/2017.
 */

public class CubeScene {

    private enum Face {
        Front,
        Back,
        Left,
        Right,
        Top,
        Bottom
    }

    private final Bitmap.Config bmpConfig = Bitmap.Config.ARGB_8888;
    private static final int resolution = 512;
    private static HashMap<String, byte[]> mBmpByteCache = new HashMap<>();

    private Vector<Sprite> Sprites = new Vector<>();
    private EnumMap<Face,Bitmap> faces = new EnumMap<>(Face.class);
    private Bitmap equirectangularBmp;

    static final float PI = (float)(Math.PI);
    static final float HALF_PI = (float)(Math.PI * 0.5);
    static final float TWO_PI = (float)Math.PI * 2;

    public CubeScene(final Context context, final int resourceId) {
        equirectangularBmp = BitmapFactory.decodeResource(context.getResources(), resourceId);
        LoadFaces(resourceId);
        float cubeSize = 6f;
        float factor = ((float)resolution + 1.0f) / (float)resolution;
        Sprites.add(new Sprite(cubeSize * factor, 0, 0, 180, 0, 0, cubeSize, faces.get(Face.Front)));
        Sprites.add(new Sprite(cubeSize * factor, 180, 0, 0, 0, 0, cubeSize, faces.get(Face.Back)));
        Sprites.add(new Sprite(cubeSize * factor, 180, 270, 0, 0, 0, cubeSize, faces.get(Face.Right)));
        Sprites.add(new Sprite(cubeSize * factor, 180, 90, 0, 0, 0, cubeSize, faces.get(Face.Left)));
        Sprites.add(new Sprite(cubeSize * factor, 90, 0, 180, 0, 0, cubeSize, faces.get(Face.Bottom)));
        Sprites.add(new Sprite(cubeSize * factor, 270, 0, 180, 0, 0, cubeSize, faces.get(Face.Top)));
    }

    public void draw(float[] mvpMatrix) {
        for (Sprite s : Sprites) {
            s.draw(mvpMatrix);
        }
    }

    private class Point { float x, y, z;}

    public interface InitPoint { void init(float i, float j, Point p);}

    private void LoadFaces(final int resourceId) {

        List<Pair<Face,InitPoint>> faceTreatment = new ArrayList<>();
        faceTreatment.add(new Pair<>(Face.Front,  (i, j, p) -> {p.x = -i; p.y =  j; p.z =  1;}));
        faceTreatment.add(new Pair<>(Face.Back,   (i, j, p) -> {p.x =  i; p.y =  j; p.z = -1;}));
        faceTreatment.add(new Pair<>(Face.Top,    (i, j, p) -> {p.x = -i; p.y = -1; p.z =  j;}));
        faceTreatment.add(new Pair<>(Face.Bottom, (i, j, p) -> {p.x = -i; p.y =  1; p.z = -j;}));
        faceTreatment.add(new Pair<>(Face.Right,  (i, j, p) -> {p.x = -1; p.y =  j; p.z = -i;}));
        faceTreatment.add(new Pair<>(Face.Left,   (i, j, p) -> {p.x =  1; p.y =  j; p.z =  i;}));

        Bitmap bmp;
        for(Pair<Face,InitPoint> pair : faceTreatment) {
            String bmpId = CreateBitmapId(resourceId, pair.first);
            if (mBmpByteCache.containsKey(bmpId))
            {
                bmp = ByteToBitmap(mBmpByteCache.get(bmpId));
            }
            else
            {
                bmp = LoadFace(pair.second);
                mBmpByteCache.put(bmpId, BitmapToByte(bmp));
            }
            faces.put(pair.first, bmp);
        }
    }

    private byte[] BitmapToByte(Bitmap bitmap)
    {
        int size = bitmap.getRowBytes() * bitmap.getHeight();
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        bitmap.copyPixelsToBuffer(byteBuffer);
        return byteBuffer.array();
    }

    private Bitmap ByteToBitmap(byte[] byteArray)
    {
        Bitmap bitmap_tmp = Bitmap.createBitmap(resolution, resolution, bmpConfig);
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        bitmap_tmp.copyPixelsFromBuffer(buffer);
        return bitmap_tmp;
    }

    private String CreateBitmapId(int resourceId, Face face)
    {
        String res = "Id" + resourceId + face;
        Log.d("CubeScene", res);
        return res;
    }

    private Bitmap LoadFace(InitPoint initPoint) {
        Bitmap bmp = Bitmap.createBitmap(resolution, resolution, bmpConfig);

        Point p = new Point();
        for (int i = 0; i < resolution; i++)
            for (int j = 0; j < resolution; j++) {
                float ifloat = Utils.ChangeRange(0, resolution, -1, 1, i);
                float jfloat = Utils.ChangeRange(0, resolution, -1, 1, j);

                initPoint.init(ifloat, jfloat, p);

                double r = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
                float lat = (float) Math.asin(p.y / r);
                float lon = (float) Math.atan2(p.z, p.x);

                bmp.setPixel(i, j, GetPixel(lon, lat));
            }
        return bmp;
    }

    private int GetPixel(float lon, float lat) {
        // Clamp values
        while (lat > HALF_PI) {
            lat = PI  - lat;
            lon += PI;
        }

        while (lat < -1 * HALF_PI) {
            lat = -1 * lat - PI;
            lon += Math.PI;
        }

        while (lon < 0) {
            lon += TWO_PI;
        }

        while (lon > TWO_PI) {
            lon -= TWO_PI;
        }

        // TODO add linear interpolation
        int x = (int)Utils.ChangeRange(0, TWO_PI, 0, equirectangularBmp.getWidth(), lon);
        int y = (int)Utils.ChangeRange(-HALF_PI, HALF_PI, 0, equirectangularBmp.getHeight(), lat);
        x = Math.min(x, equirectangularBmp.getWidth() - 1);
        y = Math.min(y, equirectangularBmp.getHeight() - 1);
        return equirectangularBmp.getPixel(x, y);
    }
}

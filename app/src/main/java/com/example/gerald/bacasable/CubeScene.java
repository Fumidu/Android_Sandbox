package com.example.gerald.bacasable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.EnumMap;

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

    private Sprite front;

    private EnumMap<Face,Bitmap> faces = new EnumMap<Face, Bitmap>(Face.class);
    private Bitmap equirectangularBmp;

    private int resolution = 512;

    static final float PI = (float)(Math.PI);
    static final float HALF_PI = (float)(Math.PI * 0.5);
    static final float TWO_PI = (float)Math.PI * 2;

    public CubeScene(final Context context, final int resourceId) {
        equirectangularBmp = BitmapFactory.decodeResource(context.getResources(), resourceId);
        LoadFaces();

        front = new Sprite(3, 0, 0, 180, 0, 0, 3f , faces.get(Face.Front));
    }

    public void draw(float[] mvpMatrix) {
        front.draw(mvpMatrix);
    }

    private class Point { float x, y, z;}

    private void LoadFaces() {
        Bitmap frontBmp = Bitmap.createBitmap(resolution, resolution, Bitmap.Config.ARGB_8888);

        Point p = new Point();
        for (int i = 0; i < resolution; i++)
        for (int j = 0; j < resolution; j++) {
            float ifloat = ChangeRange(0, resolution, -1, 1, i);
            float jfloat = ChangeRange(0, resolution, -1, 1, j);
            p.x = ifloat;
            p.y = jfloat;
            p.z = 1;

            double r = Math.sqrt(p.x * p.x + p.y * p.y + p.z * p.z);
            float lat = (float)Math.asin(p.z / r);
            float lon = (float)Math.atan2(p.y, p.x);

            int pixel = GetPixel(lon, lat);
            frontBmp.setPixel(i, j, pixel);
        }

        faces.put(Face.Front, frontBmp);
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

        int x = (int)ChangeRange(0, TWO_PI, 0, equirectangularBmp.getWidth(), lon);
        int y = (int)ChangeRange(-HALF_PI, HALF_PI, 0, equirectangularBmp.getHeight(), lat);
        x = Math.min(x, equirectangularBmp.getWidth() - 1);
        y = Math.min(y, equirectangularBmp.getHeight() - 1);
        return equirectangularBmp.getPixel(x, y);
    }

    private float ChangeRange(float r1Start, float r1Stop, float r2Start, float r2Stop, float val)
    {
        float r1Range = r1Stop - r1Start;
        float r2Range = r2Stop - r2Start;
        return (val - r1Start) / r1Range * r2Range + r2Start;
    }

}

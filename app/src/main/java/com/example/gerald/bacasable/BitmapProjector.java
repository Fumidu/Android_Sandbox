package com.example.gerald.bacasable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.Random;

/**
 * Created by Gérald on 16/01/2017.
 */

public class BitmapProjector {

    private Bitmap bitmap;
    private int[] panoPixels;

    private float maxfov = (float)(180. / 180. * Math.PI); // 40° field of view converted in radians

    private float PI = (float)(Math.PI);
    private float HALF_PI = (float)(Math.PI * 0.5);
    private float TWO_PI = (float)Math.PI * 2;

    // han, c'est mal...
    double cosr;
    double cost;
    double sinr;
    double sint;

    public BitmapProjector(Bitmap b){

        bitmap = b;
        panoPixels = new int[bitmap.getHeight() * bitmap.getWidth()];
        bitmap.getPixels(panoPixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }

    public Bitmap RandomFill(Bitmap dest) {
        Log.d("PROJECTOR", "Enter RandomFill");
        int r, g, b;
        int width = dest.getWidth();
        int height = dest.getHeight();
        int size =  width * height;
        int[] pixels = new int[size];
        dest.getPixels(pixels, 0, width, 0, 0, width, height);
        int c = 0;

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++)
            {
                if ((x + y) % 100 == 0) {
                    r = (int) (Math.random() * 255);
                    g = (int) (Math.random() * 255);
                    b = (int) (Math.random() * 255);
                    c = Color.rgb(r, g, b);
                }
                pixels[x * height + y] = c;
            }
        }

        dest.setPixels(pixels, 0, width, 0, 0, width, height);
        Log.d("PROJECTOR", "Exit RandomFill");
        return dest;
    }

    public Bitmap Project2(Bitmap dest, double rho, double theta, double phi) {
        Log.d("PROJECTOR", "Enter project");
        double destWidth = dest.getWidth();
        double destHeight = dest.getHeight();
        double origWidth = bitmap.getWidth();
        double origHeight = bitmap.getHeight();

        int[] pixels = new int[(int)destWidth * (int)destHeight];
        dest.getPixels(pixels, 0, (int)destWidth, 0, 0, (int)destWidth, (int)destHeight);

        double hfov, vfov;
        if (destWidth > destHeight) {
            // landscape
            hfov = maxfov;
            vfov = maxfov * destHeight / destWidth;
            theta -= HALF_PI;
        } else {
            // portrait
            vfov = maxfov;
            hfov = maxfov * destWidth / destHeight;
            theta = phi - HALF_PI;
        }

        rho = -rho;
        cost = Math.cos(rho);
        cosr = Math.cos(theta);
        sint = Math.sin(rho);
        sinr = Math.sin(theta);

        double x_orig_rad, y_orig_rad;
        int x_orig_px, y_orig_px, c;
        double[] pIn = new double[2];
        double[] pOut = new double[2];
        for(int x = 0; x < destWidth; x++)
            for(int y = 0; y < destHeight; y++) {
                pIn[0] = (hfov * (-0.5 + (double)(x) / destWidth));
                pIn[1] = (vfov * (-0.5 + (double)(y) / destHeight));

                RotatePoint(pIn, pOut);
                x_orig_rad = pOut[0];
                y_orig_rad = pOut[1];

                while (y_orig_rad > HALF_PI) {
                    y_orig_rad = PI  - y_orig_rad;
                    x_orig_rad += PI;
                }

                while (y_orig_rad < -1 * HALF_PI) {
                    y_orig_rad = -1 * y_orig_rad - PI;
                    x_orig_rad += Math.PI;
                }

                while (x_orig_rad < 0) {
                    x_orig_rad += TWO_PI;
                }

                while (x_orig_rad > TWO_PI) {
                    x_orig_rad -= TWO_PI;
                }

                // 0 < x_orig_rad < 2PI
                x_orig_px = (int)(x_orig_rad / TWO_PI * origWidth);

                // -PI/2 < y_orig_rad < PI/2
                y_orig_px = (int)((y_orig_rad + HALF_PI) / Math.PI * origHeight);

                c = GetPixel(x_orig_px, y_orig_px);
                //dest.setPixel(x, y, c);
                pixels[x + y * (int)destWidth] = c;
            }

        dest.setPixels(pixels, 0, (int)destWidth, 0, 0, (int)destWidth, (int)destHeight);
        Log.d("PROJECTOR", "Exit project");
        return dest;
    }

    private void RotatePoint(double[] pointOrig, double[] pointDest) {
        double longitude = pointOrig[0];
        double latitude = pointOrig[1];

        double x = Math.cos(longitude) * Math.cos(latitude);
        double y = Math.sin(longitude) * Math.cos(latitude);
        double z = Math.sin(latitude);

        double x2 = cosr *  cost * x + sint * y + sinr * cost * z;
        double y2 = -1 * cosr * sint * x + cost * y - sinr * sint * z;
        double z2 = -1 * sinr * x + cosr * z;

        double lat2 = Math.asin(z2);
        double lon2 = Math.atan2(y2, x2);

        pointDest[0] = lon2;
        pointDest[1] = lat2;
    }

    public Bitmap Project(Bitmap dest, float rho, float theta, float phi) {
        Log.d("PROJECTOR", "Enter project");
        float destWidth = dest.getWidth();
        float destHeight = dest.getHeight();
        float origWidth = bitmap.getWidth();
        float origHeight = bitmap.getHeight();

        int[] pixels = new int[(int)destWidth * (int)destHeight];
        dest.getPixels(pixels, 0, (int)destWidth, 0, 0, (int)destWidth, (int)destHeight);

        float hfov, vfov;
        if (destWidth > destHeight) {
            // landscape
            hfov = maxfov;
            vfov = maxfov * destHeight / destWidth;
        } else {
            // portrait
            vfov = maxfov;
            hfov = maxfov * destWidth / destHeight;
        }

        float x_orig_rad, y_orig_rad;
        int x_orig_px, y_orig_px, c;

        for(int x = 0; x < destWidth; x++)
        for(int y = 0; y < destHeight; y++) {
            //Log.d("PROJECTOR", String.format("x = %1d", x));
            x_orig_rad = (float)(PI - hfov * (0.5 + (float)(x) / destWidth) - rho);
            y_orig_rad = (float)(PI - vfov * (0.5 + (float)(y) / destHeight) - phi);

            while (y_orig_rad > HALF_PI) {
                y_orig_rad = PI  - y_orig_rad;
                x_orig_rad += PI;
            }

            while (y_orig_rad < -1 * HALF_PI) {
                y_orig_rad = -1 * y_orig_rad - PI;
                x_orig_rad += Math.PI;
            }

            while (x_orig_rad < 0) {
                x_orig_rad += TWO_PI;
            }

            while (x_orig_rad > TWO_PI) {
                x_orig_rad -= TWO_PI;
            }

            // 0 < x_orig_rad < 2PI
            x_orig_px = (int)(x_orig_rad / TWO_PI * origWidth);

            // -PI/2 < y_orig_rad < PI/2
            y_orig_px = (int)((y_orig_rad + HALF_PI) / Math.PI * origHeight);

            c = GetPixel(x_orig_px, y_orig_px);
            //dest.setPixel(x, y, c);
            pixels[x + y * (int)destWidth] = c;
        }

        dest.setPixels(pixels, 0, (int)destWidth, 0, 0, (int)destWidth, (int)destHeight);
        Log.d("PROJECTOR", "Exit project");
        return dest;
    }

    private int GetPixel(int x, int y){
        if (x < 0) x = 0;
        if (x >= bitmap.getWidth()) x = bitmap.getWidth() - 1;
        if (y < 0) y = 0;
        if (y >= bitmap.getHeight()) y = bitmap.getHeight() - 1;
        //return bitmap.getPixel(x, y);
        return panoPixels[x + y * bitmap.getWidth()];
    }

    private double ChangeRange(double r1Start, double r1Stop, double r2Start, double r2Stop, double val)
    {
        double r1Range = r1Stop - r1Start;
        double r2Range = r2Stop - r2Start;
        return (val - r1Start) / r1Range * r2Range + r2Start;
    }
}

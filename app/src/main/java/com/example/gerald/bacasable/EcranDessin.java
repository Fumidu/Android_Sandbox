package com.example.gerald.bacasable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class EcranDessin extends SurfaceView implements Runnable{

    Thread t = null;

    SurfaceHolder holder;
    int r = 128;
    int g = 128;
    int b = 128;

    double myRho = 0;
    double myTheta = 0;
    double myPhi = 0;

    boolean isItOk = false;

    Bitmap pano;
    BitmapProjector proj;

    public EcranDessin(Context context) {
        super(context);

        holder = getHolder();
        pano = BitmapFactory.decodeResource(getResources(),R.drawable.test_pano);
        proj = new BitmapProjector(pano);
        Log.d("EcranDessin", "ctor OK");
    }

    public void SetAngle(double rho, double theta, double phi){
        myRho = rho;
        myPhi = phi;
        myTheta = theta;

        //AngleToColor();
    }

    private void AngleToColor()
    {
        // rho = azimuth [-pi, pi]
        // theta = pitch [-pi, pi]
        // phi = roll [-pi/2, pi/2]
        r = DoubleToColor(ChangeRange(-1 * Math.PI, Math.PI, 0, 255, myRho));
        g = DoubleToColor(ChangeRange(-1 * Math.PI, Math.PI, 0, 255, myTheta));
        b = DoubleToColor(ChangeRange(-1 * Math.PI, Math.PI, 0, 255, myPhi));
    }

    private int DoubleToColor(double d)
    {
        int c = (int) d;
        if (c > 255) return 255;
        if (c < 0) return 0;
        return c;
    }

    private double ChangeRange(double r1Start, double r1Stop, double r2Start, double r2Stop, double val)
    {
        double r1Range = r1Stop - r1Start;
        double r2Range = r2Stop - r2Start;
        return (val - r1Start) / r1Range * r2Range + r2Start;
    }

    private Bitmap bitmap = null;
    public void run() {
        Log.d("EcranDessin", "run");
        while (isItOk){
            if (!holder.getSurface().isValid())
                continue;
            Canvas c = holder.lockCanvas();
            //c.drawRGB(r, g, b);

            if (bitmap == null || bitmap.getWidth() != c.getWidth() || bitmap.getHeight() != c.getHeight()) {
                bitmap = Bitmap.createBitmap(c.getWidth(), c.getHeight(), Bitmap.Config.ARGB_8888);
                Log.d("ECRAN DESSIN", "Create Bitmap");
            }

            bitmap = proj.Project2(bitmap, (float)myRho, (float)myTheta, (float)myPhi);
            //bitmap = proj.RandomFill(bitmap);
            c.drawBitmap(bitmap, 0, 0, null);

            //proj.RandomFillCanvas(c);
            //proj.ProjectOnCanvas(c, myRho, myTheta, myPhi);

            holder.unlockCanvasAndPost(c);
        }
    }

    public void pause() {
        isItOk = false;
        //while (true){
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        //    break;
        //}
        t = null;
    }

    public void resume() {
        isItOk = true;
        t = new Thread(this);
        t.start();
    }
}

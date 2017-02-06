package com.example.gerald.bacasable;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Created by Gérald on 05/02/2017.
 */

public class OrientationListener implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private Sensor mSensorMag;
    private float[] mAcceleration;
    private float[] mMagneticField;
    private float[] mOrientation;
    private float[] mOrientationSmooth;
    private float[] mRotationMatrix;
    private final float SMOOTH_FACTOR = 0.7f;

    private ArrayList<NewsOrientationListener> listeners = new ArrayList<NewsOrientationListener> ();

    public OrientationListener(Context context) {
        mSensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mAcceleration = new float[3];
        mMagneticField = new float[3];
        mOrientation = new float[3];
        mOrientationSmooth = new float[3];
        mRotationMatrix = new float[16];
    }

    public void setOnNewOrientationListener (NewsOrientationListener listener)
    {
        // Store the listener object
        this.listeners.add(listener);
    }

    public interface NewsOrientationListener
    {
        void onNewOrientation(float[] orientationMatrix);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            mAcceleration[0] = event.values[0];
            mAcceleration[1] = event.values[1];
            mAcceleration[2] = event.values[2];
        } else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagneticField[0] = event.values[0];
            mMagneticField[1] = event.values[1];
            mMagneticField[2] = event.values[2];
        }

        if(mSensorManager.getRotationMatrix(mRotationMatrix, null, mAcceleration, mMagneticField)) {

            mSensorManager.getOrientation(mRotationMatrix, mOrientation);

            //mOrientationSmooth[0] = smooth(mOrientationSmooth[0], mOrientation[0], SMOOTH_FACTOR);
            //mOrientationSmooth[1] = smooth(mOrientationSmooth[1], mOrientation[1], SMOOTH_FACTOR);
            //mOrientationSmooth[2] = smooth(mOrientationSmooth[2], mOrientation[2], SMOOTH_FACTOR);

            mOrientationSmooth[0] = circularSmooth(mOrientationSmooth[0], mOrientation[0], SMOOTH_FACTOR, (float)Math.PI);
            mOrientationSmooth[1] = circularSmooth(mOrientationSmooth[1], mOrientation[1], SMOOTH_FACTOR, (float)Math.PI);
            mOrientationSmooth[2] = circularSmooth(mOrientationSmooth[2], mOrientation[2], SMOOTH_FACTOR, (float)Math.PI * 0.5f);

            for (NewsOrientationListener l : listeners)
            {
                //l.onNewOrientation(mOrientation);
                l.onNewOrientation(mOrientationSmooth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private float smooth(float old, float newval, double factor)
    {
        return (float)(old * factor + newval * (1 - factor));
    }

    private float circularSmooth(float old, float newval, float factor, float range)
    {
        if (Math.abs(old - newval) > range)
        {
            if(old < 0) old += 2.0f * range;
            else newval += 2.0f * range;
        }
        float smooth = old * factor + newval * (1 - factor);
        if (smooth > range) return smooth - 2.0f * range;
        return smooth;
    }

    protected void resume() {
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorMag, SensorManager.SENSOR_DELAY_UI);
    }

    protected void pause() {
        mSensorManager.unregisterListener(this);
    }
}

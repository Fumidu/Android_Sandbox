package com.example.gerald.bacasable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayMessageActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensorAcc;
    private Sensor mSensorMag;
    private float[] mAcceleration;
    private float[] mMagneticField;
    private float[] mOrientation;
    private float[] mOrientationSmooth;
    private float[] mRotationMatrix;
    EcranDessin surface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

//        Intent intent = getIntent();
//        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
//        TextView textView = new TextView(this);
//        textView.setTextSize(40);
//        textView.setText(message);

//        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
//        layout.addView(textView);

        mSensorManager = (SensorManager) getSystemService(this.SENSOR_SERVICE);
        mSensorAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMag = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        float[] orientation;
        mAcceleration = new float[3];
        mMagneticField = new float[3];
        mOrientation = new float[3];
        mOrientationSmooth = new float[3];
        mRotationMatrix = new float[9];

        surface = new EcranDessin(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_display_message);
        layout.addView(surface);
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
            TextView accx = (TextView) findViewById(R.id.acc_x_text);
            TextView accy = (TextView) findViewById(R.id.acc_y_text);
            TextView accz = (TextView) findViewById(R.id.acc_z_text);

            accx.setText(Float.toString(mOrientation[0]));
            accy.setText(Float.toString(mOrientation[1]));
            accz.setText(Float.toString(mOrientation[2]));

            mOrientationSmooth[0] = smooth(mOrientationSmooth[0], mOrientation[0], 0.9);
            mOrientationSmooth[1] = smooth(mOrientationSmooth[1], mOrientation[1], 0.9);
            mOrientationSmooth[2] = smooth(mOrientationSmooth[2], mOrientation[2], 0.9);

            surface.SetAngle(mOrientationSmooth[0], mOrientationSmooth[1], mOrientationSmooth[2]);
        }
    }

    private float smooth(float old, float newval, double factor)
    {
        return (float)(old * factor + newval * (1 - factor));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mSensorAcc, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, mSensorMag, SensorManager.SENSOR_DELAY_UI);
        if (surface != null) surface.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        if (surface != null) surface.pause();
    }
}

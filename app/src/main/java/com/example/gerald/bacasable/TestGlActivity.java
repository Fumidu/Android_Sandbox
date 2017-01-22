package com.example.gerald.bacasable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TestGlActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyGLSurfaceView view = new MyGLSurfaceView(this);
        setContentView(view);
    }
}

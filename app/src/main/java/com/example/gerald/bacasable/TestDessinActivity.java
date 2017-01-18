package com.example.gerald.bacasable;

import android.graphics.Canvas;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class TestDessinActivity extends AppCompatActivity {

    EcranDessin surface = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dessin);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        surface = new EcranDessin(this);
        ViewGroup layout = (ViewGroup) findViewById(R.id.content_test_dessin);
        layout.addView(surface);
    }

    @Override protected void onPause(){
        super.onPause();
        if (surface != null)
            surface.pause();
    }

    @Override protected void onResume(){
        super.onResume();
        if (surface != null)
            surface.resume();
    }
}

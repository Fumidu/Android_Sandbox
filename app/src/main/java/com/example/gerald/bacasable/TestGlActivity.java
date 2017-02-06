package com.example.gerald.bacasable;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

public class TestGlActivity extends AppCompatActivity {

    MyGLSurfaceView mGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mGLSurfaceView = new MyGLSurfaceView(this);
        setContentView(mGLSurfaceView);

        LinearLayout ll = new LinearLayout(this);
        Button b = new Button(this);
        b.setText("Anchor");
        b.setOnClickListener(getOnClickDoSomething(b));
        ll.addView(b);
        ll.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        this.addContentView(ll,
                new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
    }

    private View.OnClickListener getOnClickDoSomething(final Button button) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGLSurfaceView.SwitchAnchored();
            }
        };
    }

    @Override
    protected void onResume()
    {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLSurfaceView.onResume();
    }

    @Override
    protected void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLSurfaceView.onPause();
    }
}

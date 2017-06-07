package com.example.florianporada.theassistant2;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

public class BarcodeActivity extends AppCompatActivity {

    private static final String TAG = BarcodeActivity.class.getSimpleName();

    //private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Create an instance of Camera
        //mCamera = getCameraInstance();


        // Create our Preview view and set it as the content of our activity.
        //mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

//    protected Camera getCameraInstance(){
//        Camera c = null;
//        try {
//            c = Camera.open();
//        } catch (Exception e){
//            Log.d(TAG, "Error getting camera instance: " + e.getMessage());
//        }
//        return c;
//    }

}

package com.example.florianporada.theassistant2;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class BarcodeActivity extends AppCompatActivity {

    private static final String TAG = BarcodeActivity.class.getSimpleName();
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    private SurfaceView cameraView;
    private TextView barcodeInfo;
    private TextView socketPort;
    private TextView socketUrl;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d(TAG, "onClick: " + socketUrl.getText().toString());
                Log.d(TAG, "onClick: " + Integer.parseInt(socketPort.getText().toString()));

                sharedPreferencesEditor.putString("keySocketIp", socketUrl.getText().toString());
                sharedPreferencesEditor.putInt("keySocketPort", Integer.parseInt(socketPort.getText().toString()));
                sharedPreferencesEditor.commit();

                Snackbar.make(view, getResources().getText(R.string.settings_toast_save), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BarcodeActivity.this.finish();
                    }
                }, 2000);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cameraView = (SurfaceView) findViewById(R.id.camera_view);
        barcodeInfo = (TextView) findViewById(R.id.code_info);
        socketPort = (TextView) findViewById(R.id.qr_code_socketport);
        socketUrl = (TextView) findViewById(R.id.qr_code_socketurl);

        barcodeDetector = new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        cameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setRequestedPreviewSize(640, 480)
                .build();

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {


                try {
                    if (ActivityCompat.checkSelfPermission(BarcodeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    barcodeInfo.post(new Runnable() {
                        public void run() {
                            barcodeInfo.setText(barcodes.valueAt(0).displayValue);
                            try {
                                socketUrl.setText(barcodes.valueAt(0).displayValue.split(":")[0]);
                                socketPort.setText(barcodes.valueAt(0).displayValue.split(":")[1]);
                            } catch (Exception e) {
                                Log.w(TAG, "run: wrong qrcode format. try: '<ipaddress>:<port>'", e);
                            }
                        }
                    });
                }
            }
        });
    }
}

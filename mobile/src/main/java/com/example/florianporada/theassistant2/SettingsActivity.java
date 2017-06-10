package com.example.florianporada.theassistant2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SETTINGS_ACTIVITY";
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";

    private EditText mSocketPort;
    private EditText mSocketIp;
    private FloatingActionButton mSettingsSave;
    private Button mSettingsBarcode;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSocketIp = (EditText) findViewById(R.id.settings_editText_socketip);
        mSocketPort = (EditText) findViewById(R.id.settings_editText_socketport);

        mSocketIp.setText(sharedPreferences.getString("keySocketIp", null));
        mSocketPort.setText(String.valueOf(sharedPreferences.getInt("keySocketPort", 0)));

        mSettingsSave = (FloatingActionButton) findViewById(R.id.fab);
        mSettingsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mSocketIp.getText().toString().matches("") && !mSocketPort.getText().toString().matches("")) {
                    sharedPreferencesEditor.putString("keySocketIp", mSocketIp.getText().toString());
                    sharedPreferencesEditor.putInt("keySocketPort", Integer.parseInt(mSocketPort.getText().toString()));
                    sharedPreferencesEditor.commit();

                    Snackbar.make(view, getResources().getString(R.string.settings_toast_save), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    startService(new Intent(getApplicationContext(), ServerConnectionService.class));
                }
            }
        });

        mSettingsBarcode = (Button) findViewById(R.id.settings_barcode);
        mSettingsBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

}

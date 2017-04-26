package com.example.florianporada.theassistant2;

import android.content.Context;
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

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SETTINGS_ACTIVITY";
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";

    private EditText mSocketPort;
    private EditText mSocketIp;
    private Button mSettingsSave;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSocketIp = (EditText) findViewById(R.id.settings_editText_socketip);
        mSocketPort = (EditText) findViewById(R.id.settings_editText_socketport);


        mSettingsSave = (Button) findViewById(R.id.settings_save);
        mSettingsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked save");
                if (!mSocketIp.getText().toString().matches("") && !mSocketPort.getText().toString().matches("")) {
                    sharedPreferencesEditor.putString("keySocketIp", mSocketIp.getText().toString());
                    sharedPreferencesEditor.putInt("keySocketPort", Integer.parseInt(mSocketPort.getText().toString()));
                    Log.d(TAG, sharedPreferencesEditor.commit() + "");
                }
            }
        });

        mSettingsBarcode = (Button) findViewById(R.id.settings_barcode);
        mSettingsBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked barcode");
                String test = sharedPreferences.getString("keySocketIp", null);
                Log.d(TAG, test);
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

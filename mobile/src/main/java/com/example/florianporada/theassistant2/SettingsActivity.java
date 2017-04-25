package com.example.florianporada.theassistant2;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SETTINGS_ACTIVITY";
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";

    EditText mSocketPort;
    EditText mSocketUrl;
    Button mSettingsSave;
    Button mSettingsBarcode;

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

        mSocketUrl = (EditText) findViewById(R.id.settings_editText_socketurl);
        mSocketPort = (EditText) findViewById(R.id.settings_editText_socketport);


        mSettingsSave = (Button) findViewById(R.id.settings_save);
        mSettingsSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked save");
                if (!mSocketUrl.getText().toString().matches("") && !mSocketPort.getText().toString().matches("")) {
                    sharedPreferencesEditor.putString("keySocketURL", mSocketUrl.getText().toString());
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
                String test = sharedPreferences.getString("keySocketURL", null);
                Log.d(TAG, test);
            }
        });

    }

}

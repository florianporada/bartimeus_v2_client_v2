package com.io.florianporada.theassistant2;

import android.content.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static android.graphics.Color.*;

public class MainActivity extends WearableActivity implements
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final SimpleDateFormat AMBIENT_DATE_FORMAT = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";


    private VibrationPatterns vibrations = new VibrationPatterns();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private BoxInsetLayout mContainerView;
    private TextView mTextView;
    private TextView mClockView;
    private boolean notifierLocked = false;
    private Queue<String> notificationQueue = new PriorityQueue<>();
    //private GoogleApiClient mGoogleApiClient;

    private long[] StringArrayToLongArray(String[] numbers) {
        long[] result = new long[numbers.length];
        for (int i = 0; i < numbers.length; i++)
            result[i] = Long.parseLong(numbers[i]);

        return result;
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            Log.d(TAG, "onReceive: " + intent.getAction());
            if (intent.getAction().equals("wearServiceIntent")) {
                String wearServiceString = intent.getStringExtra("wearServiceString");
                sharedPreferencesEditor.putString("lastPattern", wearServiceString).commit();
                queuePattern(wearServiceString);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mTextView = (TextView) findViewById(R.id.text);
        mClockView = (TextView) findViewById(R.id.clock);

//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//        mGoogleApiClient.connect();


        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(
                mMessageReceiver, new IntentFilter("wearServiceIntent"));

        mTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                playLastPattern();

                return false;
            }
        });
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        //updateDisplay();
        mContainerView.setBackgroundColor(WHITE);
        mTextView.setText(R.string.welcome);
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        //updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        //updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
            mTextView.setTextColor(getResources().getColor(android.R.color.white));
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setText(AMBIENT_DATE_FORMAT.format(new Date()));
        } else {
            mContainerView.setBackground(null);
            mTextView.setTextColor(getResources().getColor(android.R.color.black));
            mClockView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {

    }

    private void playLastPattern() {
        if (!notifierLocked) {
            String lastPattern = sharedPreferences.getString("lastPattern", "");
            queuePattern(lastPattern);
            Log.d(TAG, "onLongClick: last pattern is: " + lastPattern);
        }
    }

    private void queuePattern(String string) {
        notificationQueue.add(string);

        if (!notifierLocked) {
            startNotifier();
        }
    }


    private void startNotifier() {
        try {
            notifierLocked = true;
            String string = notificationQueue.remove();
            final String notificationText = string.split("notification:")[string.split("notification:").length - 1].trim();
            final String notificationPattern = string.split("notification:")[0].trim();
            long[] pattern = StringArrayToLongArray(notificationPattern.split(";"));

            Log.d(TAG, "startNotifier: " + notificationText + " " + notificationPattern);
            Vibrator v = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
            //vibrations.ConvertMessageToVibrations(v, message);

            v.vibrate(pattern, -1);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    int color;
                    String infoText;
                    CharSequence transNotifText = getResources().getText(R.string.no_notif);

                    switch (notificationText) {
                        case "wrong ring":
                            color = RED;
                            transNotifText = getResources().getText(R.string.bad_ring);
                            break;
                        case "ring":
                            color = BLUE;
                            transNotifText = getResources().getText(R.string.ring);
                            break;
                        case "motion":
                            color = MAGENTA;
                            transNotifText = getResources().getText(R.string.motion);
                            break;
                        case "warning notification":
                            color = BLACK;
                            transNotifText = getResources().getText(R.string.incoming);
                            break;
                        case "incomming":
                        case "test":
                        default:
                            color = WHITE;
                    }

                    mContainerView.setBackgroundColor(color);
                    mTextView.setText(transNotifText.toString());
                }
            });

            final Handler handler = new Handler();
            int delay = 0;
            for (int i = 0; i < pattern.length; i++) {
                delay += pattern[i];
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    notifierLocked = false;
                    if (!notificationQueue.isEmpty()) {
                        startNotifier();
                    }
                }
            }, delay);


        } catch (Exception e) {
            Log.e(TAG, "startNotifier: could not transform string", e);
        }
    }
}

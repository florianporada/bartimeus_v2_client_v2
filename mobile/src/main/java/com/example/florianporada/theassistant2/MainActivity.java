package com.example.florianporada.theassistant2;

import android.app.Notification;
import android.content.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MAIN_ACTIVITY";
    private static final String TO_WEAR = "/to_wear";
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor;
    private String socketIp;
    private int socketPort;
    private boolean isWearConnected = false;
    private boolean isServerConnected = false;

    private ImageView ivWear;
    private ImageView ivServer;

    private ImageButton bReloadWear;
    private ImageButton bReloadServer;

    private void sendNotification(View view, String string) {
        String toSend = string;
        if(toSend.isEmpty())
            toSend = "You sent an empty notification";
        Notification notification = new NotificationCompat.Builder(getApplication())
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Info")
                .setVibrate(new long[] { 1000, 1000})
                .setContentText(toSend)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
        int notificationId = 1;
        notificationManager.notify(notificationId, notification);
    }

/*    private void sendMessageToWear(final String message) {
        new Thread() {
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), TO_WEAR, message.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.v(TAG, "Message: {" + message + "} sent to: " + node.getDisplayName());
                    } else {
                        // Log an error
                        Log.e(TAG, "ERROR: failed to send Message");
                    }
                }
            }
        }.start();
    }*/

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isWearConnected = intent.getBooleanExtra("wearStatus", false);
            if (isWearConnected) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivWear.setImageResource(android.R.drawable.presence_online);
                    }
                });
                Log.d(TAG, "Wearable connected: " + String.valueOf(intent.getBooleanExtra("wearStatus", false)));

            }

            isServerConnected = intent.getBooleanExtra("serverStatus", false);
            if (isServerConnected) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ivServer.setImageResource(android.R.drawable.presence_online);
                    }
                });
                Log.d(TAG, "Server connected: " + String.valueOf(intent.getBooleanExtra("serverStatus", false)));
            }
        }
    };

    private void startServerService () {
        Intent serverConnectionIntent = new Intent(this, ServerConnectionService.class);
        serverConnectionIntent.putExtra("reloadServerService", true);
        startService(serverConnectionIntent);
    }

    private void startWearableService () {
        Intent wearConnectionIntent = new Intent(this, WearConnectionService.class);
        wearConnectionIntent.putExtra("reloadWearableService", true);
        startService(wearConnectionIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final EditText editTextDescription = (EditText) findViewById(R.id.editText);

        /**
         * indicator for server/wear connection
         */
        ivServer = (ImageView) findViewById(R.id.imageViewServer);
        ivWear = (ImageView) findViewById(R.id.imageViewWear);

        /**
         * reload buttons
         */
        bReloadServer = (ImageButton) findViewById(R.id.reloadServer);
        bReloadWear = (ImageButton) findViewById(R.id.reloadWear);

        /**
         * onclick listener reload buttons
         */
        bReloadServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServerService();
                Snackbar.make(view, R.string.reloaded_server_service, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        bReloadWear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWearableService();
                Snackbar.make(view, R.string.reloaded_wearable_service, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        /**
         * get shared preferences for socket ip and port
         */
        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        socketIp = sharedPreferences.getString("keySocketIp", "127.0.0.1");
        socketPort = sharedPreferences.getInt("keySocketPort", 3030);

        /**
         * starting service for the google wear connection
         */
        startWearableService();

        /**
         * starting the service for the server connection
         */
        startServerService();


        /**
         * start intent filter for server connection check
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("serverStatus"));

        /**
         * start intent filter for wearable connection check
         */
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("wearStatus"));



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                if (editTextDescription.getText() != null) {
                    String toSend = editTextDescription.getText().toString();
                    sendNotification(view, toSend);
                } else {
                    Snackbar.make(view, "Please enter some text", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(getApplicationContext(), BarcodeActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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

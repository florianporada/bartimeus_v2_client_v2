package com.example.florianporada.theassistant2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class WearConnectionService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "WEAR_CONNECTION_SERVICE";
    private static final String TO_WEAR = "/to_wear";

    private GoogleApiClient mGoogleApiClient;

    public WearConnectionService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();
        initGoogleClient();
        Log.v(TAG, "wearConnectionService is running now");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getBooleanExtra("reloadWearableService", false)) {
            Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                    if (nodes.getNodes().size() > 0) {
                        broadcastWearStatus(true);
                    } else {
                        broadcastWearStatus(false);
                    }
                }
            });
        }

        if (intent.getStringExtra("VibrationPattern") != null) {
            Log.v(TAG, intent.getStringExtra("VibrationPattern"));

            long[] pattern = patternConverter(intent.getStringExtra("VibrationPattern"));

            if (pattern.length > 0) {
                sendPatternToWear(pattern);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.v(TAG, "google client api connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * sends a long array for the pattern to the connected android wear
     */
    private void sendPatternToWear(final long[] pattern) {
        final StringBuilder patternString = new StringBuilder();

        for (int i = 0; i < pattern.length; i++) {
            patternString.append(Long.toString(pattern[i])).append(";");
        }

        final String finalPatternString = patternString.toString();

        new Thread() {
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), TO_WEAR, finalPatternString.getBytes()).await();
                    if (result.getStatus().isSuccess()) {
                        Log.v(TAG, "Pattern: {" + patternString + "} sent to: " + node.getDisplayName());
                    } else {
                        // Log an error
                        Log.v("TAG", "ERROR: failed to send Message");
                    }
                }
            }
        }.start();
    }

    /**
     * initializes the google client, which is required for the android wear connection
     */
    private void initGoogleClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the data layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
    }

    /**
     * converts incoming string to vibration pattern
     */
    private long[] patternConverter(String string) {
        // referecnce : E/WEAR_CONNECTION_SERVICE: Error tansforming the pattern: [5|Ring| A known person is at the door|patternlayout: 200,200,500,200,400]

        string = string.split("patternlayout")[string.split("patternlayout:").length - 1];
        String[] stringPattern = string.split(",");
        long[] pattern = new long[stringPattern.length];

        for (int i = 0; i < stringPattern.length; i++){
            try {
                String convertedString = stringPattern[i].replaceAll("[^0-9]","");
                pattern[i] = Long.parseLong(convertedString);
            } catch (Exception e) {
                Log.e(TAG, "Error tansforming the pattern: " + stringPattern[i] + " e: " + e);
            }
        }

        Log.d(TAG, "Transformed pattern: " + pattern.length);

        return pattern;
    }

    /**
     * send intent
     */
    private void broadcastWearStatus(boolean status) {
        Intent intent = new Intent("wearStatus");
        intent.putExtra("wearStatus",  status);
        sendStatusBroadcast(intent);
    }

    private void sendStatusBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}

package com.example.florianporada.theassistant2;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {

    private static final String TO_WEAR = "/to_wear";
    private static final String TAG = ListenerService.class.getSimpleName();


    private void sendMessageToActivity(String wearServiceString) {
        Intent intent = new Intent("wearServiceIntent");
        // You can also include some extra data.
        intent.putExtra("wearServiceString", wearServiceString);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(TO_WEAR)) {
            final String message = new String(messageEvent.getData());
            Log.v(TAG, "Message received on watch is: " + message);
            sendMessageToActivity(message);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}

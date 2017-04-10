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

    private void sendMessageToActivity(String msg) {
        Intent intent = new Intent("intentKey");
        // You can also include some extra data.
        intent.putExtra("key", msg);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (messageEvent.getPath().equals(TO_WEAR)) {
            final String message = new String(messageEvent.getData());
            Log.v("myTag", "Message path received on watch is: " + messageEvent.getPath());
            Log.v("myTag", "Message received on watch is: " + message);
            sendMessageToActivity(message);
        }
        else {
            super.onMessageReceived(messageEvent);
        }
    }
}

package com.example.florianporada.theassistant2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;

public class ServerConnectionService extends Service {
    private static final String TAG = "SRV_CONNECTION_SERVICE";
    private static final String TO_WEAR = "/to_wear";
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";

    private String socketIp;
    private int socketPort;
    private connectTask mTask;
    private SharedPreferences sharedPreferences;

    public ServerConnectionService() {}

    @Override
    public void onCreate(){
        super.onCreate();
        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        socketIp = sharedPreferences.getString("keySocketIp", "127.0.0.1");
        socketPort = sharedPreferences.getInt("keySocketPort", 3030);

        Log.v(TAG, "serverConnectionService is running now");

        //mTask = new connectTask();
        //mTask.execute("");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getBooleanExtra("reloadServerService", false)) {
                if (mTask != null) mTask.stopClient();
                mTask = new connectTask();
                mTask.execute("");
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class connectTask extends AsyncTask<String,String,TCPClient> {
        private TCPClient mTcpClient;

        @Override
        protected TCPClient doInBackground(String... message) {
            //we create a TCPClient object and
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            }, socketIp, socketPort, getApplicationContext());

            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            Log.d(TAG, "Received Data from Server: " + Arrays.toString(values));

            Intent intent = new Intent(getApplicationContext(), WearConnectionService.class);
            intent.putExtra("VibrationPattern", Arrays.toString(values));

            startService(intent);
        }

        protected void stopClient () {
            if (mTcpClient != null) {
                mTcpClient.stopClient();
                mTcpClient = null;
            }
        }
    }
}

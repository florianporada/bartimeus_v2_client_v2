package com.example.florianporada.theassistant2;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;

public class ServerConnectionService extends Service {
    private static final String TAG = "SRV_CONNECTION_SERVICE";
    private static final String TO_WEAR = "/to_wear";
    private static final String PREFERENCE_FILE_KEY = "TheAssistantFile";

    private String socketIp;
    private int socketPort;
    private TCPClient mTcpClient;
    private SharedPreferences sharedPreferences;



    public ServerConnectionService() {
    }

    public void onCreate(){
        super.onCreate();
        sharedPreferences = getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);

        socketIp = sharedPreferences.getString("keySocketIp", "127.0.0.1");
        socketPort = sharedPreferences.getInt("keySocketPort", 3030);

        new connectTask().execute("");
        Log.v(TAG, "serverConnectionService is running now");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class connectTask extends AsyncTask<String,String,TCPClient> {

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
            }, socketIp, socketPort);

            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            Log.v(TAG, Arrays.toString(values));
            //sendMessageToWear(Arrays.toString(values));
        }
    }
}

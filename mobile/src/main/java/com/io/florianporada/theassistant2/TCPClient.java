package com.io.florianporada.theassistant2;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private static final String TAG = "TCP_CLIENT";
    private String serverMessage;
    private String serverIp = "192.168.2.100";
    private int serverPort = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    private Context context;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener, String socketIp, int socketPort, Context context) {
        mMessageListener = listener;
        serverIp = socketIp;
        serverPort = socketPort;
        this.context = context;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    private void broadcastServerStatus(boolean status) {
        Intent intent = new Intent("serverStatus");
        intent.putExtra("serverStatus",  status);
        sendStatusBroadcast(intent);
    }

    private void sendStatusBroadcast(Intent intent){
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void stopClient() {
        Log.d(TAG, "TCPClient stopped");

        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(serverIp);

            Log.v(TAG, "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, serverPort);

            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                Log.v(TAG, "C: Sent.");

                Log.v(TAG, "C: Done.");

                broadcastServerStatus(true);

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }

                    serverMessage = null;

                }

                Log.v(TAG, "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e(TAG, "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {
            Log.e(TAG, "C: Error", e);

            broadcastServerStatus(false);
        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        void messageReceived(String message);
    }
}
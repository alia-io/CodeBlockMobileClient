package com.example.codeblockmobileclient.communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.codeblockmobileclient.R;
import com.example.codeblockmobileclient.SignupLoginActivity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import tech.gusavila92.websocketclient.WebSocketClient;

// https://developer.android.com/guide/components/bound-services
// https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/app/MessengerService.java
// https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/app/MessengerServiceActivities.java

public class SocketMessageService extends Service {

    private WebSocketClient webSocketMessageClient;     // socket communication
    private NotificationManager notificationManager;    // manages badges & notifications
    private final Messenger incomingMessenger = new Messenger(new SocketMessageHandler());  // messenger with messages from user to send to server
                                                                        // messages should go client(user)->service(this)->server(remote)
    ArrayList<Messenger> clients = new ArrayList<Messenger>();  // list of messengers belonging to subscribed clients (activities)
                                                                        // for messages that should go server(remote)->service(this)->client(user)

    // service received message types
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_TO_SERVER = 3;

    @Override
    public void onCreate() {
        startNotificationManager();
        startWebSocketClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;    // keep it open even when all clients unsubscribe
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SocketMessageService", "Binding service to subscriber");
        return incomingMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(R.string.notification_manager);
        Log.i("SocketMessageService", "Destroying service");
    }

    /**
     * Handler of incoming messages from clients.
     */
    public class SocketMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:   // register a new client
                    clients.add(msg.replyTo);
                    Log.i("SocketMessageService", "Client registered to service");
                    break;
                case MSG_UNREGISTER_CLIENT: // unregister client
                    clients.remove(msg.replyTo);
                    Log.i("SocketMessageService", "Unregistered client from service");
                    break;
                case MSG_SEND_TO_SERVER:    // send a message to the server (remote)
                    Bundle bundle = msg.getData();  // get bundled data from Message object
                    String messageString = bundle.getString("jsonMsg"); // get JSON string from bundled data
                    webSocketMessageClient.send(messageString); // send it
                    Log.i("SocketMessageService", "Sending message to server:\n" + messageString);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Sets up notification manager to work when app is closed or minimized
     */
    private void startNotificationManager() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //showNotification(); // display notification about us starting
        // TODO: fix notifications, currently getting error.
    }

    /**
     * Establishes socket connection to server
     */
    private void startWebSocketClient() {

        URI uri;
        try {
            uri = new URI("ws://10.0.2.2:8080/websocket");  // Connect to localhost
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketMessageClient = new WebSocketClient(uri) {

            @Override
            public void onOpen() {
                Log.i("SocketMessageClient", "Starting web socket session");
            }

            @Override
            public void onTextReceived(String message) {
                Log.i("SocketMessageClient", "Socket message received from server:\n" + message);
                for (int i = clients.size() - 1; i >= 0; i--) { // broadcast received message to all clients
                    try {
                        Message msg = Message.obtain(null, 0);
                        Bundle bundle = new Bundle();   // bundle stores message data
                        bundle.putString("jsonMsg", message);
                        msg.setData(bundle);
                        clients.get(i).send(msg);
                    } catch (RemoteException e) {
                        clients.remove(i);
                    }
                }
            }

            @Override
            public void onCloseReceived() {
                Log.i("SocketMessageClient", "Closing web socket client");
            }

            @Override public void onBinaryReceived(byte[] data) { }
            @Override public void onPingReceived(byte[] data) { }
            @Override public void onPongReceived(byte[] data) { }
            @Override public void onException(Exception e) { }
        };

        webSocketMessageClient.setConnectTimeout(10000);
        webSocketMessageClient.setReadTimeout(60000);
        webSocketMessageClient.enableAutomaticReconnection(5000);
        webSocketMessageClient.connect();
    }

    /**
     * Show notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "notification text";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SignupLoginActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.test_notification)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("New message")  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        notificationManager.notify(R.string.notification_manager, notification);
    }
}
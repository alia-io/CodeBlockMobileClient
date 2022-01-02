package com.example.codeblockmobileclient.communication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.codeblockmobileclient.R;
import com.example.codeblockmobileclient.SignupLoginActivity;
import com.example.codeblockmobileclient.communication.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URISyntaxException;

import lombok.SneakyThrows;
import tech.gusavila92.websocketclient.WebSocketClient;

public class SocketMessageService extends Service {

    private final IBinder binder = new SocketMessageServiceBinder();    // Binder given to clients
    private WebSocketClient webSocketMessageClient;
    private NotificationManager notificationManager;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class SocketMessageServiceBinder extends Binder {
        SocketMessageService getService() {
            // Return this instance of SocketMessageService so clients can call public methods
            return SocketMessageService.this;
        }
    }

    @Override
    public void onCreate() {

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://10.0.2.2:8080/websocket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketMessageClient = new WebSocketClient(uri) {

            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
            }

            @SneakyThrows
            @Override
            public void onTextReceived(String message) {
                Log.i("WebSocket", "String message received: " + message);
                ObjectMapper mapper = new ObjectMapper();
                MessageDTO messageDTO = mapper.readValue(message, MessageDTO.class);
                Log.i("WebSocket", "Converted message body: " + messageDTO.getBody());
                Context ctx = getApplication().getApplicationContext();
                Log.i("WebSocket", "context: " + ctx.getClass().getName());
                if (ctx instanceof MessagingAppCompatActivity) {
                    Log.i("WebSocket", "instanceof?");
                    MessagingAppCompatActivity activity = (MessagingAppCompatActivity) ctx;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                activity.receiveMessage(messageDTO);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }

            @Override public void onBinaryReceived(byte[] data) { }
            @Override public void onPingReceived(byte[] data) { }
            @Override public void onPongReceived(byte[] data) { }
            @Override public void onException(Exception e) { }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
            }
        };

        webSocketMessageClient.setConnectTimeout(10000);
        webSocketMessageClient.setReadTimeout(60000);
        webSocketMessageClient.enableAutomaticReconnection(5000);
        webSocketMessageClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SocketMessageService", "onStartCommand");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SocketMessageService", "onBind");
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(R.string.notification_manager);
        Log.i("SocketMessageService", "onDestroy");
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "notification text";

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SignupLoginActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                //.setSmallIcon(R.drawable.stat_sample)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                //.setContentTitle(getText(R.string.local_service_label))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        notificationManager.notify(R.string.notification_manager, notification);
    }

    /** method for clients */
    public void sendMessage(MessageDTO message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String msg = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        webSocketMessageClient.send(msg);
    }
}
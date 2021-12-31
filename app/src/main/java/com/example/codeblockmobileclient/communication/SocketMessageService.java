package com.example.codeblockmobileclient.communication;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

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
                /*if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                activity.receiveMessage(messageDTO);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    });
                }*/
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("SocketMessageService", "onBind");
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("SocketMessageService", "onDestroy");
    }

    /** method for clients */
    public void sendMessage(MessageDTO message) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String msg = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        webSocketMessageClient.send(msg);
    }
}
package com.example.codeblockmobileclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.codeblockmobileclient.dto.MessageDTO;

import java.net.URI;
import java.net.URISyntaxException;

public class ConnectClientActivity extends AppCompatActivity {

    private WebSocketMessageClient webSocketMessageClient;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_client);
        tv = findViewById(R.id.tv);
        createWebSocketClient();
    }

    private void createWebSocketClient() {

        URI uri;
        try {
            // Connect to local host
            uri = new URI("ws://10.0.2.2:8080/websocket");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        webSocketMessageClient = new WebSocketMessageClient(uri) {

            @Override
            public void onOpen() {
                Log.i("WebSocket", "Session is starting");
                //MessageDTO msg = new MessageDTO("Hello World!");
                //webSocketMessageClient.send(msg);
            }

            @Override
            public void onMessageDTOReceived(Object message) {
                Log.i("WebSocket", "Message received");
                MessageDTO messageDTO = (MessageDTO) message;
                final String body = messageDTO.getBody();
                Log.i("WebSocket", "Message body = " + body);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            tv.setText(body);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onCloseReceived() {
                Log.i("WebSocket", "Closed ");
                System.out.println("onCloseReceived");
            }
        };

        webSocketMessageClient.setConnectTimeout(10000);
        webSocketMessageClient.setReadTimeout(60000);
        webSocketMessageClient.enableAutomaticReconnection(5000);
        webSocketMessageClient.connect();
    }

    public void onClickSendMessage(View view) {
        Log.i("WebSocket", "Button was clicked");
        MessageDTO messageDTO = new MessageDTO("HELLO CAN YOU HEAR ME (from MessageDTO)");
        webSocketMessageClient.send(messageDTO.toString());
    }
}
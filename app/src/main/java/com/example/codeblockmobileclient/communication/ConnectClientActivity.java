package com.example.codeblockmobileclient.communication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.codeblockmobileclient.R;
import com.example.codeblockmobileclient.communication.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ConnectClientActivity extends MessagingAppCompatActivity {

    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_client);
        tv = findViewById(R.id.tv);
    }

    @Override
    protected MessagingAppCompatActivity getActivity() {
        return ConnectClientActivity.this;
    }

    public void onClickSendMessage(View view) throws JsonProcessingException {
        Log.i("WebSocket", "Button was clicked");
        MessageDTO messageDTO = new MessageDTO(7, "HELLO CAN YOU HEAR ME (from MessageDTO)");
        sendMessage(messageDTO);
    }

    @Override
    protected void receiveMessage(MessageDTO message) {
        tv.setText(message.getBody());
    }
}
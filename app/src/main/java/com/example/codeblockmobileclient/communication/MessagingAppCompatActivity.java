package com.example.codeblockmobileclient.communication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import androidx.appcompat.app.AppCompatActivity;

import com.example.codeblockmobileclient.communication.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

public abstract class MessagingAppCompatActivity extends AppCompatActivity {

    private MessagingAppCompatActivity activity;
    private SocketMessageService service;
    private Messenger messenger = null;
    protected boolean bound = false;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            SocketMessageService.SocketMessageServiceBinder binder
                    = (SocketMessageService.SocketMessageServiceBinder) iBinder;
            service = binder.getService();
            messenger = new Messenger(iBinder);
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public static class MessageHandler extends Handler {
        private MessagingAppCompatActivity context;
        MessageHandler(MessagingAppCompatActivity ctx) { context = ctx; }
        @SneakyThrows
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String jsonMessage = bundle.getString("json");
            ObjectMapper mapper = new ObjectMapper();
            MessageDTO messageDTO = mapper.readValue(jsonMessage, MessageDTO.class);
            context.receiveMessage(messageDTO);
        }
    }

    public MessagingAppCompatActivity() { super(); }

    @Override
    protected void onStart() {
        super.onStart();
        activity = getActivity();
        if (activity instanceof ConnectClientActivity) {
            ConnectClientActivity currentActivity = (ConnectClientActivity) activity;
            Intent intent = new Intent(currentActivity, SocketMessageService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    protected abstract MessagingAppCompatActivity getActivity();
    protected abstract void receiveMessage(MessageDTO message);

    protected void sendMessage(MessageDTO message) throws JsonProcessingException {
        if (bound) service.sendMessage(message);
    }
}
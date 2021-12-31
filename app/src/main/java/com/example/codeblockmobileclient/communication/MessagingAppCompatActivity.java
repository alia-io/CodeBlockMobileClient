package com.example.codeblockmobileclient.communication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.appcompat.app.AppCompatActivity;

import com.example.codeblockmobileclient.communication.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class MessagingAppCompatActivity extends AppCompatActivity {

    private SocketMessageService service;
    protected boolean bound = false;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            SocketMessageService.SocketMessageServiceBinder binder
                    = (SocketMessageService.SocketMessageServiceBinder) iBinder;
            service = binder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    public MessagingAppCompatActivity() { super(); }

    @Override
    protected void onStart() {
        super.onStart();
        MessagingAppCompatActivity activity = getActivity();
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
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
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.codeblockmobileclient.communication.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;
import tech.gusavila92.websocketclient.WebSocketClient;

public abstract class MessagingAppCompatActivity extends AppCompatActivity {

    private MessagingAppCompatActivity activity;
    //private SocketMessageService service;

    private Messenger serviceMessenger = null;
    private Messenger activityMessenger = new Messenger(new ActivityMessageHandler());
    protected boolean bound = false;

    /**
     * Handler of incoming messages from clients.
     */
    public class ActivityMessageHandler extends Handler {
        @SneakyThrows
        @Override
        public void handleMessage(Message msg) {
            //switch (msg.what) {
                //case MSG_SAY_HELLO:
                    //Toast.makeText(applicationContext, "hello!", Toast.LENGTH_SHORT).show();
                    Bundle bundle = msg.getData();
                    String messageString = bundle.getString("jsonMsg");
                    Log.i("Socket", "handleMessage, msg: " + messageString);
                    //webSocketClient.send(messageString);
                    //break;
                //default:
                    //super.handleMessage(msg);
            //}
            ObjectMapper mapper = new ObjectMapper();
            MessageDTO messageDTO = mapper.readValue(messageString, MessageDTO.class);
            Log.i("WebSocket", "Converted message body: " + messageDTO.getBody());
            receiveMessage(messageDTO);
        }
    }

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            //SocketMessageService.SocketMessageServiceBinder binder
            //        = (SocketMessageService.SocketMessageServiceBinder) iBinder;
            //service = binder.getService();
            //serviceMessenger = new Messenger(iBinder);
            serviceMessenger = new Messenger(iBinder);
            Log.i("Socket", "Service bound");
            try {
                Message message = Message.obtain(null, SocketMessageService.MSG_REGISTER_CLIENT);
                message.replyTo = activityMessenger;
                serviceMessenger.send(message);
                Log.i("Socket", "Client subscribed");
            } catch (RemoteException e) {
                Log.i("Socket", "Error: " + e.getMessage());
            }
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
            Log.i("Socket", "Service disconnected");
            bound = false;
        }
    };

    /*public static class MessageHandler extends Handler {
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
    }*/

    public MessagingAppCompatActivity() { super(); }

    @Override
    protected void onStart() {
        super.onStart();
        activity = getActivity();
        bindSocketMessageService();
    }

    protected void bindSocketMessageService() {
        if (!bound && activity instanceof ConnectClientActivity) {
            ConnectClientActivity currentActivity = (ConnectClientActivity) activity;
            Intent intent = new Intent(currentActivity, SocketMessageService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
            Log.i("Socket", "Bind Service Complete");
        }
    }

    protected void unbindSocketMessageService() {
        if (bound && serviceMessenger != null) {
            try {
                Message message = Message.obtain(null, SocketMessageService.MSG_UNREGISTER_CLIENT);
                message.replyTo = activityMessenger;
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                Log.i("Socket", "Error: " + e.getMessage());
            }
            unbindService(connection);
            Log.i("Socket", "Unbind Service Complete");
        }
    }

    protected abstract MessagingAppCompatActivity getActivity();
    protected abstract void receiveMessage(MessageDTO message);

    protected void sendMessage(MessageDTO message) throws JsonProcessingException {
        //if (bound) service.sendMessage(message);
        if (!bound) return;
        Message msg = Message.obtain(null, SocketMessageService.MSG_SEND_TO_SERVER);
        Bundle bundle = new Bundle();
        ObjectMapper mapper = new ObjectMapper();
        bundle.putString("jsonMsg", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
        msg.setData(bundle);
        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
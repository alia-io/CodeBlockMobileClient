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

import androidx.appcompat.app.AppCompatActivity;

import com.example.codeblockmobileclient.communication.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.SneakyThrows;

/**
 * Abstract activity to be inherited by any activity that needs to receive messages
 * from the server through socket connection
 */
public abstract class MessagingAppCompatActivity extends AppCompatActivity {

    private MessagingAppCompatActivity activity;    // the current child activity ("this")
    private Messenger serviceMessenger = null;      // messenger with messages from user to send to server
                                            // messages should go client(this)->service(local)->server(remote)
    private Messenger activityMessenger = new Messenger(new ActivityMessageHandler());
                                                    // messenger for messages from server to client
                                            // messages should go server(remote)->service(local)->client(this)
    protected boolean bound = false;    // is activity currently bound to service

    private ServiceConnection connection = new ServiceConnection() {    // the connection to the service

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            serviceMessenger = new Messenger(iBinder);
            Log.i("MessagingAppCompatActivity", "Service bound");
            try {
                Message message = Message.obtain(null, SocketMessageService.MSG_REGISTER_CLIENT);
                message.replyTo = activityMessenger;    // send client messenger to service
                serviceMessenger.send(message);
                Log.i("MessagingAppCompatActivity", "Client subscribed");
            } catch (RemoteException e) {
                Log.i("MessagingAppCompatActivity", "RemoteException: " + e.getMessage());
            }
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            serviceMessenger = null;
            Log.i("MessagingAppCompatActivity", "Service disconnected");
            bound = false;
        }
    };

    /**
     * Handler of incoming messages from clients.
     */
    public class ActivityMessageHandler extends Handler {
        @SneakyThrows
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String messageString = bundle.getString("jsonMsg");
            Log.i("MessagingAppCompatActivity", "Receiving message from service:\n" + messageString);
            ObjectMapper mapper = new ObjectMapper();
            MessageDTO messageDTO = mapper.readValue(messageString, MessageDTO.class);
            receiveMessage(messageDTO);
        }
    }

    public MessagingAppCompatActivity() {
        super();
    }

    @Override
    protected void onStart() {
        super.onStart();
        activity = getActivity();
        bindSocketMessageService();
    }

    // get the child activity (for some reason "this" sometimes returns a ptr to THIS abstract parent class)
    protected abstract MessagingAppCompatActivity getActivity();

    protected abstract void receiveMessage(MessageDTO message); // action taken when a message is received

    protected void sendMessage(MessageDTO message) throws JsonProcessingException {
        if (!bound) return;
        Message msg = Message.obtain(null, SocketMessageService.MSG_SEND_TO_SERVER); // message type = send to server
        Bundle bundle = new Bundle();   // bundle to carry JSON string
        ObjectMapper mapper = new ObjectMapper();
        bundle.putString("jsonMsg", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message));
        msg.setData(bundle);    // add bundle to Message object
        Log.i("MessagingAppCompatActivity", "Sending message to service");
        try {
            serviceMessenger.send(msg); // send it
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.i("MessagingAppCompatActivity", "RemoteException: " + e.getMessage());
        }
    }

    protected void bindSocketMessageService() {
        if (!bound && activity instanceof ConnectClientActivity) {
            ConnectClientActivity currentActivity = (ConnectClientActivity) activity;
            Intent intent = new Intent(currentActivity, SocketMessageService.class);
            bindService(intent, connection, Context.BIND_AUTO_CREATE);
            Log.i("MessagingAppCompatActivity", "Binding service");
        }
    }

    protected void unbindSocketMessageService() {
        if (bound && serviceMessenger != null) {
            try {
                Message message = Message.obtain(null, SocketMessageService.MSG_UNREGISTER_CLIENT);
                message.replyTo = activityMessenger;    // send client messenger to service
                Log.i("MessagingAppCompatActivity", "Sending unregister message to service");
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                Log.i("MessagingAppCompatActivity", "RemoteException: " + e.getMessage());
            }
            unbindService(connection);
            Log.i("MessagingAppCompatActivity", "Unbinding service");
        }
    }
}
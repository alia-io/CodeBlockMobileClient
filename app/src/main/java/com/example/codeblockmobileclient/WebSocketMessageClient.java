package com.example.codeblockmobileclient;

import com.example.codeblockmobileclient.dto.MessageDTO;

import org.apache.commons.lang3.SerializationUtils;

import java.net.URI;

import tech.gusavila92.websocketclient.WebSocketClient;

public abstract class WebSocketMessageClient extends WebSocketClient {

    /**
     * Initialize all the variables
     *
     * @param uri URI of the WebSocket server
     */
    public WebSocketMessageClient(URI uri) {
        super(uri);
    }

    @Override
    public void onBinaryReceived(byte[] data) {
        MessageDTO message = SerializationUtils.deserialize(data);
        onMessageDTOReceived(message);
    }

    /**
     * Called when MessageDTO object has been received
     *
     * @param message The binary message received
     */
    public abstract void onMessageDTOReceived(MessageDTO message);

    public void send(MessageDTO message) {
        byte[] data = SerializationUtils.serialize(message);
        send(data);
    }
}

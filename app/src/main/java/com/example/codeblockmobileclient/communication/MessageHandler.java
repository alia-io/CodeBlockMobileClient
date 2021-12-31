package com.example.codeblockmobileclient.communication;

import com.example.codeblockmobileclient.communication.dto.MessageDTO;

public interface MessageHandler {
    void sendMessage(MessageDTO message);
    void receiveMessage(MessageDTO message);
}
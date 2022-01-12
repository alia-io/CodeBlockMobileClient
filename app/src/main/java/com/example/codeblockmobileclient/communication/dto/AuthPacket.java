package com.example.codeblockmobileclient.communication.dto;

import lombok.Data;

@Data
public class AuthPacket {
    private long userId;
    private String authToken;
}
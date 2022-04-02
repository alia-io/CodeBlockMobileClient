package com.example.codeblockmobileclient.communication.dto;

import java.io.Serializable;

public class MessageDTO implements Serializable {
    long id;
    String body;

    public MessageDTO() { }

    public MessageDTO(long id, String body) {
        this.id = id;
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

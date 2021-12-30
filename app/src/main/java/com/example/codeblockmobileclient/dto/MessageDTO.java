package com.example.codeblockmobileclient.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO implements Serializable {
    String body;

    @Override
    public String toString() {
        return "MessageDTO{body=" + body + "}";
    }
}

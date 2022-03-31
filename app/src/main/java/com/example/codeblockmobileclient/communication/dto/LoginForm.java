package com.example.codeblockmobileclient.communication.dto;

import lombok.Data;

@Data
public class LoginForm {

    private String email;
    private String password;

    public LoginForm(String email, String encryptedPass) {
        this.email = email;
        this.password = encryptedPass;
    }
}
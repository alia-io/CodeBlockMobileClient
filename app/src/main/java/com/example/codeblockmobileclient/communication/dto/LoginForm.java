package com.example.codeblockmobileclient.communication.dto;

public class LoginForm {

    private String email;
    private String password;

    public LoginForm(String email, String encryptedPass) {
        this.email = email;
        this.password = encryptedPass;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
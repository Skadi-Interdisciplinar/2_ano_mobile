package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("username")
    private String username;

    @SerializedName("senha")
    private String senha;

    @SerializedName("password")
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String senha) {
        this.username = username;
        this.senha = senha;
        this.password = senha;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
        this.password = senha;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
        this.senha = password;
    }
}

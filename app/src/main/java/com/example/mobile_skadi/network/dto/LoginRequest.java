package com.example.mobile_skadi.network.dto;

public class LoginRequest {
    private String username;
    private String senha;

    public LoginRequest() {
    }

    public LoginRequest(String username, String senha) {
        this.username = username;
        this.senha = senha;
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
    }
}

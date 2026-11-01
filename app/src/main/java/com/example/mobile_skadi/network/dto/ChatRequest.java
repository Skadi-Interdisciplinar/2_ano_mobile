package com.example.mobile_skadi.network.dto;

public class ChatRequest {
    private String mensagem;
    private String token;

    public ChatRequest() {
    }

    public ChatRequest(String mensagem, String token) {
        this.mensagem = mensagem;
        this.token = token;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

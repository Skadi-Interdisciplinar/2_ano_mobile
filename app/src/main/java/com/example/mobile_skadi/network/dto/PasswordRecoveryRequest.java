package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class PasswordRecoveryRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("senha")
    private String senha;

    public PasswordRecoveryRequest(String email) {
        this.email = email;
        this.senha = null;
    }

    public PasswordRecoveryRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

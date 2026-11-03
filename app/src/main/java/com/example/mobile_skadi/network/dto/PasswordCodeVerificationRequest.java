package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class PasswordCodeVerificationRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("codigo")
    private String codigo;

    public PasswordCodeVerificationRequest(String email, String codigo) {
        this.email = email;
        this.codigo = codigo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}

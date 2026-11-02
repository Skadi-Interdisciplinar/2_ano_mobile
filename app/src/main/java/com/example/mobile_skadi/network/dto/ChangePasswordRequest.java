package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {
    @SerializedName("senhaAtual")
    private String senhaAtual;

    @SerializedName("novaSenha")
    private String novaSenha;

    @SerializedName("confirmacaoSenha")
    private String confirmacaoSenha;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String senhaAtual, String novaSenha, String confirmacaoSenha) {
        this.senhaAtual = senhaAtual;
        this.novaSenha = novaSenha;
        this.confirmacaoSenha = confirmacaoSenha;
    }

    public String getSenhaAtual() {
        return senhaAtual;
    }

    public void setSenhaAtual(String senhaAtual) {
        this.senhaAtual = senhaAtual;
    }

    public String getNovaSenha() {
        return novaSenha;
    }

    public void setNovaSenha(String novaSenha) {
        this.novaSenha = novaSenha;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }
}

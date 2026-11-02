package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class SupportRequestRequest {
    @SerializedName("categoria")
    private String categoria;

    @SerializedName("descricao")
    private String descricao;

    public SupportRequestRequest() {
    }

    public SupportRequestRequest(String categoria, String descricao) {
        this.categoria = categoria;
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}

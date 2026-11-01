package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class RelatorioPdfRequest {
    private String token;

    @SerializedName("distribuicao_id")
    private Long distribuicaoId;

    @SerializedName("camara_id")
    private Long camaraId;

    @SerializedName("data_inicial")
    private String dataInicial;

    @SerializedName("data_final")
    private String dataFinal;

    public RelatorioPdfRequest() {
    }

    public RelatorioPdfRequest(String token, Long distribuicaoId, Long camaraId, String dataInicial, String dataFinal) {
        this.token = token;
        this.distribuicaoId = distribuicaoId;
        this.camaraId = camaraId;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getDistribuicaoId() {
        return distribuicaoId;
    }

    public void setDistribuicaoId(Long distribuicaoId) {
        this.distribuicaoId = distribuicaoId;
    }

    public Long getCamaraId() {
        return camaraId;
    }

    public void setCamaraId(Long camaraId) {
        this.camaraId = camaraId;
    }

    public String getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(String dataInicial) {
        this.dataInicial = dataInicial;
    }

    public String getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(String dataFinal) {
        this.dataFinal = dataFinal;
    }
}

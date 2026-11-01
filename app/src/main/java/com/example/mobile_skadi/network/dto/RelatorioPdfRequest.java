package com.example.mobile_skadi.network.dto;

import com.google.gson.annotations.SerializedName;

public class RelatorioPdfRequest {

    @SerializedName("cd_id")
    private Long cdId;

    @SerializedName("camara_id")
    private Long camaraId;

    @SerializedName("data_inicial")
    private String dataInicial;

    @SerializedName("data_final")
    private String dataFinal;

    public RelatorioPdfRequest() {
    }

    public RelatorioPdfRequest(Long cdId, Long camaraId, String dataInicial, String dataFinal) {
        this.cdId = cdId;
        this.camaraId = camaraId;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
    }

    public Long getCdId() {
        return cdId;
    }

    public void setCdId(Long cdId) {
        this.cdId = cdId;
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

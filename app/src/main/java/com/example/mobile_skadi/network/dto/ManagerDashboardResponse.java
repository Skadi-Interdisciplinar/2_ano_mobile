package com.example.mobile_skadi.network.dto;

import java.util.List;

public class ManagerDashboardResponse {
    private Long cdId;
    private String cdName;
    private int criticosCount;
    private int slaVencidoCount;
    private int foraFaixaCount;
    private int semResponsavelCount;
    private int totalCamaras;
    private int naFaixaCamaras;
    private int foraFaixaCamaras;
    private int offlineCamaras;
    private int semDadosCamaras;
    private List<PriorityAlertDto> priorities;
    private List<Float> trendReadings;

    public ManagerDashboardResponse() {
    }

    public Long getCdId() {
        return cdId;
    }

    public void setCdId(Long cdId) {
        this.cdId = cdId;
    }

    public String getCdName() {
        return cdName;
    }

    public void setCdName(String cdName) {
        this.cdName = cdName;
    }

    public int getCriticosCount() {
        return criticosCount;
    }

    public void setCriticosCount(int criticosCount) {
        this.criticosCount = criticosCount;
    }

    public int getSlaVencidoCount() {
        return slaVencidoCount;
    }

    public void setSlaVencidoCount(int slaVencidoCount) {
        this.slaVencidoCount = slaVencidoCount;
    }

    public int getForaFaixaCount() {
        return foraFaixaCount;
    }

    public void setForaFaixaCount(int foraFaixaCount) {
        this.foraFaixaCount = foraFaixaCount;
    }

    public int getSemResponsavelCount() {
        return semResponsavelCount;
    }

    public void setSemResponsavelCount(int semResponsavelCount) {
        this.semResponsavelCount = semResponsavelCount;
    }

    public int getTotalCamaras() {
        return totalCamaras;
    }

    public void setTotalCamaras(int totalCamaras) {
        this.totalCamaras = totalCamaras;
    }

    public int getNaFaixaCamaras() {
        return naFaixaCamaras;
    }

    public void setNaFaixaCamaras(int naFaixaCamaras) {
        this.naFaixaCamaras = naFaixaCamaras;
    }

    public int getForaFaixaCamaras() {
        return foraFaixaCamaras;
    }

    public void setForaFaixaCamaras(int foraFaixaCamaras) {
        this.foraFaixaCamaras = foraFaixaCamaras;
    }

    public int getOfflineCamaras() {
        return offlineCamaras;
    }

    public void setOfflineCamaras(int offlineCamaras) {
        this.offlineCamaras = offlineCamaras;
    }

    public int getSemDadosCamaras() {
        return semDadosCamaras;
    }

    public void setSemDadosCamaras(int semDadosCamaras) {
        this.semDadosCamaras = semDadosCamaras;
    }

    public List<PriorityAlertDto> getPriorities() {
        return priorities;
    }

    public void setPriorities(List<PriorityAlertDto> priorities) {
        this.priorities = priorities;
    }

    public List<Float> getTrendReadings() {
        return trendReadings;
    }

    public void setTrendReadings(List<Float> trendReadings) {
        this.trendReadings = trendReadings;
    }
}
